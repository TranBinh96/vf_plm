using HybridShapeTypeLib;
using INFITF;
using MECMOD;
using ProductStructureTypeLib;
using PubSub;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using Welding_Tool.Common;
using Welding_Tool.DataModel;
using Welding_Tool.Processing;
using Welding_Tool.View;
using Welding_Tool.ViewModel;
using CATApplication = INFITF.Application;
using Window = System.Windows.Window;

namespace Welding_Tool
{
    public partial class MainWindow : Window, INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;
        private Hub _hub = Hub.Default;
        private ModelSettingData _settData = new ModelSettingData();

        /// <summary>
        /// 
        /// </summary>
        /// <param name="propertyName"></param>
        private void NotifyPropertyChanged([CallerMemberName] string propertyName = "")
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        private string _status = "Click on 'Create JF Part' button to start ...";
        public string Status
        {
            get { return _status; }
            set
            {
                _status = value;
                NotifyPropertyChanged();
            }
        }

        private DelegateCommand _cmdCreateJFPart;
        public DelegateCommand CmdCreateJFPart
        {
            get
            {
                return _cmdCreateJFPart ?? (_cmdCreateJFPart = new DelegateCommand(
                    () => _isEnableCreJFBtn,
                    async () => await CreateJFPart()));
            }
        }

        public ModelLoadingScreen LoadScrDC { get; set; } = new ModelLoadingScreen();

        private CATApplication _cat;
        private CATApplication CATIA
        {
            get => _cat;
            set
            {
                _cat = value;
                if (SelectedWelding != null)
                {
                    SelectedWelding.CATIA = value;
                }
            }
        }
        public ModelJoinFile JFData { get; } = new ModelJoinFile();

        internal static readonly log4net.ILog LOG = log4net.LogManager
            .GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);


        private bool _isEnableCreJFBtn = true;
        private void SetEnableCreJFBtn(bool isEnable)
        {
            _isEnableCreJFBtn = isEnable;
            CmdCreateJFPart.NotifyCanExecuteChanged();
        }

        private DelegateCommand _cmdCreWeldSpot;
        public DelegateCommand CmdCreWeldSpot
        {
            get
            {
                return _cmdCreWeldSpot ?? (_cmdCreWeldSpot = new DelegateCommand(
                    () => SelectedWelding != null && SelectedWelding.IsCreWeldSpotEn,
                    async () => await CreateWeldBody()));
            }
        }

        private DelegateCommand _cmdCreReport;
        public DelegateCommand CmdCreReport
        {
            get
            {
                return _cmdCreReport ?? (_cmdCreReport = new DelegateCommand(
                    () => CmdCreWeldSpot.CanExecute && JFData.LstWeldBodies.Count > 0,
                    async () => await CreateReportFile(true)
                ));
            }
        }

        private DelegateCommand _cmdOpenSett;
        public DelegateCommand CmdOpenSetting
        {
            get
            {
                return _cmdOpenSett ?? (_cmdOpenSett = new DelegateCommand(
                    () => true,
                    async () =>
                    {
                        var sett = new ViewSetting();
                        sett.Owner = System.Windows.Application.Current.MainWindow;
                        _settData.IsCancel = true; // Always mark isCancel = true
                        _hub.Publish(_settData);
                        Status = "⚠ Warning: Changing setting might reset the tool!";
                        sett.ShowDialog();
                        if (_settData.IsDataChanged)
                        {
                            await ResetTool();
                        }
                    }
                ));
            }
        }

        private string _btnCreJFName = Constant.CreJF;
        public string BtnCreJFContent
        {
            get { return _btnCreJFName; }
            set
            {
                _btnCreJFName = value;
                NotifyPropertyChanged();
            }
        }

        private readonly List<ModelReportData> ReportData;
        private Progress<ModelReport> reporter;

        public ObservableCollection<VMBase> ListWelding { get; set; } = new ObservableCollection<VMBase>();

        private VMBase _selectedWelding;
        public VMBase SelectedWelding
        {
            get => _selectedWelding;
            set
            {
                _selectedWelding = value;
                _selectedWelding.CATIA = CATIA;
                CmdCreWeldSpot.NotifyCanExecuteChanged();
                CmdCreReport.NotifyCanExecuteChanged();
                NotifyPropertyChanged();
                Status = $"Set Welding mode to '{value.Name}'.";
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        private async Task<(bool, T)> SelectObj<T>(string objName, Selection sel) where T : AnyObject
        {
            try
            {
                Status = $"Waiting for user to select a {objName} in CATIA ...";
                T selObj = default;

                while (true)
                {
                    bool? isOk;
                    (isOk, selObj) = await CPr.SelectObj<T>(sel, $"Select a {objName} ...");

                    if (isOk != false)
                    {
                        if (isOk == true && sel.Item(1).Value is Vertex vtx)
                        {
                            var (isOkPt, arr) = await CPr.GetPointCoord(JFData, vtx);
                            if (isOkPt)
                            {
                                JFData.Temp = arr;
                            }
                        }
                        break;
                    }
                    else
                    {
                        Status = "Select wrong object type! Please select again!";
                        await Task.Delay(300);
                    }
                }

                return (selObj != null, selObj);
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return (false, default);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        private async Task<bool?> SelCopySurface()
        {
            try
            {
                var sel = JFData.SelectionProd;
                var (isOK, obj) = await SelectObj<HybridShapeAssemble>("Final Surface", sel);
                if (isOK)
                {
                    return await CPr.PasteAsResultAsync(sel, obj, JFData.GeoInput);
                }
                else
                {
                    return null;
                }
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return null;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        private async Task<bool> GetTheCopiedSur()
        {
            try
            {
                return await Task.Run(() =>
                {
                    JFData.Update();

                    if (JFData.GeoInput.HybridShapes.Count == 0)
                    {
                        JFData.Surface = null;
                        return false;
                    }

                    JFData.Surface = JFData.GeoInput.HybridShapes.Item(1);
                    return true;
                });
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return false;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="objName"></param>
        /// <param name="sel"></param>
        /// <returns></returns>
        private async Task<(bool, Body, Part, Product)> SelectBodyByLeaf(string objName, Selection sel)
        {
            try
            {
                var (isOK, leaf) = await SelectObjLeaf<Body>(objName, sel);
                if (isOK && leaf is Product prd)
                {
                    if (prd.ReferenceProduct.Parent is PartDocument prtDoc &&
                        prtDoc.Part.Bodies.Count > 0)
                    {
                        return (true, prtDoc.Part.Bodies.Item(1), prtDoc.Part, prd);
                    }

                    BL.ShowInfoMsg(BL.GetCOMType(prd.ReferenceProduct.Parent));
                }

                return (false, default, null, null);
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return (false, default, null, null);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        private async Task<(bool, AnyObject)> SelectObjLeaf<T>(string objName, Selection sel) where T : AnyObject
        {
            try
            {
                Status = $"Waiting for user to select a {objName} in CATIA ...";
                AnyObject selPrd = null;

                while (true)
                {
                    bool? isOk;
                    (isOk, selPrd) = await CPr.SelectObjLeaf<T>(sel, $"Select a {objName} ...");

                    if (isOk != false)
                    {
                        break;
                    }
                    else
                    {
                        Status = "Select wrong object type! Please select again!";
                        await Task.Delay(300);
                    }
                }

                return (selPrd != null, selPrd);
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return (false, default);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        private async Task<bool?> CopyTheSur(Body body)
        {
            try
            {
                if (!(body.Parent is Bodies bds) ||
                    !(bds.Parent is Part prt))
                {
                    BL.ShowCritMsg("Cannot identify the Part object of selected Body!");
                    return null;
                }

                HybridBody geoFinalSur = null;
                await Task.Run(() =>
                {
                    var hbds = prt.HybridBodies;
                    for (int i = 1; i <= hbds.Count; i++)
                    {
                        if (hbds.Item(i).get_Name() == GeoNames.FinalSur)
                        {
                            geoFinalSur = hbds.Item(i);
                            break;
                        }
                    }
                });

                if (geoFinalSur == null)
                {
                    BL.ShowCritMsg($"Cannot found '{GeoNames.FinalSur}' geometry! in selected Part");
                    return null;
                }

                if (geoFinalSur.HybridShapes.Count == 0)
                {
                    BL.ShowCritMsg($"There is no suface in '{GeoNames.FinalSur}' geometry!");
                    return null;
                }

                await Task.Run(() =>
                {
                    for (int i = JFData.GeoInput.HybridShapes.Count; i > 0; i--)
                    {
                        JFData.DeleteObj(JFData.GeoInput.HybridShapes.Item(i));
                    }
                });

                if (!JFData.Update())
                {
                    var msg = "There are errors on the Specification Tree!\n";
                    msg += "Please check and fix, then try again!";
                    BL.ShowCritMsg(msg);
                    return null;
                }

                var fnSur = geoFinalSur.HybridShapes.Item(1);
                var selPrd = JFData.SelectionProd;
                if (!await CPr.PasteAsResultAsync(selPrd, fnSur, JFData.GeoInput))
                {
                    BL.ShowWarnMsg("Please activate the top Assembly on the Specification Tree by double clicking!");
                    return null;
                }

                JFData.UpdateObj(JFData.GeoInput);
                JFData.Surface = JFData.GetLastItem(JFData.GeoInput);

                return true;
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return false;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        private void SCA_CreateJFPart()
        {
            SelectedWelding.CmdSelSur?.NotifyCanExecuteChanged();
            SelectedWelding.SCA_SelSurface?.Invoke();
        }

        /// <summary>
        /// 
        /// </summary>
        private void SCA_CreWeldBody()
        {
            CmdCreReport.NotifyCanExecuteChanged();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        private async Task CheckDeleteOldData()
        {
            if (JFData.Part != null)
            {
                try
                {
                    if (JFData.LstWeldBodies.Count > 0)
                    {
                        var del = BL.ShowQuesYNMsg("Do you want to delete created Weld Spot?", "Delete old data");

                        await CPr.DeleteDataWeldBody(JFData, del);
                        await CPr.DeleteDataWeldLine(JFData, true);

                        if (!del)
                        {
                            await CPr.CreateGeoTempCons(JFData);
                        }
                    }

                    JFData.SetShow(JFData.GeoConstruction, true);
                    JFData.SetShow(JFData.GeoTempPtCons, true);
                }
                catch (Exception ex)
                {
                    BL.Log(ex);
                }
            }
        }
    }
}
