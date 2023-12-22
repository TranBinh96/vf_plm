using HybridShapeTypeLib;
using INFITF;
using MECMOD;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.Common;
using Welding_Tool.DataModel;
using Welding_Tool.Processing;
using Welding_Tool.ViewModel.SpotWeld;

namespace Welding_Tool.ViewModel
{
    public class VMSpotWeld : VMBase
    {
        public ModelJoinFile JFData { get; set; }
        public ModelLoadingScreen LoadScrDC;
        public DelegateCommand CmdCreWeldSpot;
        public DelegateCommand CmdCreReport;
        public Action SCA_CreWeldBody;

        public SpotWeldMode Mode;
        public bool isDataChanged;
        public ObservableCollection<VMSpotWeldBase> ListSpotWeldMode { get; set; } = new ObservableCollection<VMSpotWeldBase>();

        private VMSpotWeldBase _selectedSpotWeld;
        public VMSpotWeldBase SelectedSpotWeld
        {
            get => _selectedSpotWeld;
            set
            {
                _selectedSpotWeld = value;
                if (SelectedSpotWeld != null)
                {
                    Mode = (SpotWeldMode)ListSpotWeldMode.IndexOf(SelectedSpotWeld);
                }
                CmdCreWeldSpot.NotifyCanExecuteChanged();
                CmdCreReport.NotifyCanExecuteChanged();
                NotifyPropertyChanged();
                Status = $"Set the Spot Welding mode to '{value.Name}'.";
            }
        }

        private DelegateCommand _cmdSelSur;
        public DelegateCommand CmdSelSurChild
        {
            get
            {
                return _cmdSelSur ?? (_cmdSelSur = new DelegateCommand(
                    () => JFData?.Product != null,
                    async () => await SelSurface()));
            }
        }

        private bool _isSelectingSur;
        public bool IsSelectingSur
        {
            get { return _isSelectingSur; }
            set
            {
                _isSelectingSur = value;
                if (value)
                {
                    BtnSelSurContent = Constant.NoSel;
                }
                NotifyPropertyChanged();
            }
        }

        public override bool IsCreWeldSpotEn
        {
            get
            {
                return (Mode == SpotWeldMode.OnSurface && JFData.PointOnLineCount > 0 && !isDataChanged) ||
                       (Mode == SpotWeldMode.ByPoints && JFData.GeoPoints != null);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="name"></param>
        public VMSpotWeld(string name)
        {
            Name = name;
            CmdSelSur = CmdSelSurChild;
            SCA_SelSurface = () => SCA_SelSurfaceChild();
        }

        /// <summary>
        /// 
        /// </summary>
        public void Init()
        {
            var byPt = new VMSpotWeldByPoints("Spot Weld by Points")
            {
                Parent = this,
            };
            var onSur = new VMSpotWeldOnSurface("Spot Weld on Surface")
            {
                Parent = this,
            };

            onSur.Offset = "0";
            onSur.PointCount = "0";
            onSur.Distance = "0";
            onSur.Spacing = "0";
            onSur.IsOption1 = true;

            ListSpotWeldMode.Add(byPt);
            ListSpotWeldMode.Add(onSur);
            SelectedSpotWeld = byPt;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async Task SelSurface()
        {
            LoadScrDC.ShowMode(LoadScrMode.Selection);
            if (!CPr.SetForegroundCATIA(CATIA))
            {
                LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                return;
            }
            if (await CPr.CheckWorkbenchTop(CATIA, JFData) == false)
            {
                BL.ShowWarnMsg("Please activate the top Assembly on the Specification Tree by double clicking!");
                LoadScrDC.ShowMode(LoadScrMode.None);
                return;
            }

            if (JFData.Surface != null)
            {
                try
                {
                    JFData.DeleteObj(JFData.Surface);
                }
                catch { }
                JFData.Surface = null;
            }

            IsSelectingSur = true;
            while (true)
            {
                if (!CPr.SetForegroundCATIA(CATIA))
                {
                    LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                    return;
                }

                var (isOK, isCopy, obj) = await SelectThenCopy<HybridShapeSurfaceExplicit, HybridShapeAssemble>("Final Surface", JFData.SelectionProd, JFData.GeoInput);
                if (isOK == true)
                {
                    JFData.Surface = obj;
                    BtnSelSurContent = JFData.Surface.get_Name();

                    if (isCopy) { BL.ShowInfoMsg("The Surface has been copied to 'Input Geometry'!"); }
                    Status = "Select Surface done!";
                    break;
                }
                else if (isOK == false)
                {
                    BL.ShowCritMsg("Cannot copy selected surface!\nPlease select the surface again!");
                }
                else
                {
                    Status = "Cancel selection!";
                    break;
                }
            }

            IsSelectingSur = false;
            LoadScrDC.ShowMode(LoadScrMode.None);
            //BL.SetBtnNoSel(BtnSelSurface, JFData.Surface == null);
            SCA_SelSurfaceChild();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="objName"></param>
        /// <param name="sel"></param>
        /// <returns></returns>
        private async Task<(bool?, bool, T1)> SelectThenCopy<T1, T2>(string objName, Selection sel, AnyObject targ) where T1 : AnyObject where T2 : AnyObject
        {
            try
            {
                Status = $"Waiting for user to select a {objName} in CATIA ...";
                var msg = $"Select the {objName} ...";
                var (isOK, isCopy, obj) = await CPr.SelectAndCopy<T1, T2>(sel, msg, targ, JFData.Product);
                if (isOK == true)
                {
                    return (true, isCopy, obj);
                }
                return (isOK, isCopy, default);
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return (false, false, default);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        public async Task<(bool, T)> SelectObj<T>(string objName, Selection sel) where T : AnyObject
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
                            try
                            {
                                var mea = JFData.WBench.GetMeasurable(vtx);
                                Array co = new object[3];
                                mea.GetPoint(co);
                                JFData.Temp = co;
                            }
                            catch { }
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
        public async Task CheckDeleteOldData()
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

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        private void SCA_SelSurfaceChild()
        {
             ListSpotWeldMode.ToList().ForEach(async x => await x.SCA_FromParent());
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="ReportData"></param>
        /// <param name="reporter"></param>
        /// <returns></returns>
        public override async Task<bool> CreateWeldBody(List<ModelReportData> ReportData, IProgress<ModelReport> reporter)
        {
            return await CPr.CreateWeldBody(JFData, ReportData, Mode, reporter);
        }
    }
}
