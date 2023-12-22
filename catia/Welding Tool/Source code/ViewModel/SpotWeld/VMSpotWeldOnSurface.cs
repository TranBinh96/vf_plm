using HybridShapeTypeLib;
using INFITF;
using MECMOD;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.Common;
using Welding_Tool.DataModel;
using Welding_Tool.Processing;

namespace Welding_Tool.ViewModel.SpotWeld
{
    public class VMSpotWeldOnSurface : VMSpotWeldBase
    {
        private DelegateCommand CmdCreWeldSpot => Parent.CmdCreWeldSpot;
        private DelegateCommand CmdCreReport => Parent.CmdCreReport;

        private DelegateCommand _cmdSelEdge;
        public DelegateCommand CmdSelEdge
        {
            get
            {
                return _cmdSelEdge ?? (_cmdSelEdge = new DelegateCommand(
                    () => JFData?.Surface != null,
                    async () => await SelEdge()));
            }
        }

        private string _btnSelEdgeContent = Constant.NoSel;
        public string BtnSelEdgeContent
        {
            get { return _btnSelEdgeContent; }
            set
            {
                _btnSelEdgeContent = value;
                NotifyPropertyChanged();
            }
        }

        private bool _isSelectingEdge;
        public bool IsSelectingEdge
        {
            get { return _isSelectingEdge; }
            set
            {
                _isSelectingEdge = value;
                if (value)
                {
                    BtnSelEdgeContent = Constant.NoSel;
                }
                NotifyPropertyChanged();
            }
        }

        private bool _isMultiSelEdge;
        public bool IsMultiSelEdge
        {
            get { return _isMultiSelEdge; }
            set
            {
                _isMultiSelEdge = value;
                NotifyPropertyChanged();
                var en = value ? "enable" : "disable";
                Status = $"Select multiple Edge is {en}.";
            }
        }

        private double _selEdgeLen;
        public double SelEdgeLen
        {
            get { return _selEdgeLen; }
            set
            {
                _selEdgeLen = value;
                NotifyPropertyChanged();
            }
        }

        private string _offset;
        public string Offset
        {
            get { return _offset; }
            set
            {
                _offset = value;
                TbxOffset_TextChanged(value);
                NotifyPropertyChanged();
            }
        }

        private DelegateCommand _cmdPreviewOffset;
        public DelegateCommand CmdPreviewOffset
        {
            get
            {
                return _cmdPreviewOffset ?? (_cmdPreviewOffset = new DelegateCommand(
                    () => JFData?.LstEdge.Count > 0 && JFData.Offset != null,
                    async () => await CreateWeldLine()));
            }
        }

        private bool _isOption1;
        public bool IsOption1
        {
            get { return _isOption1; }
            set
            {
                JFData.IsOption1 = _isOption1 = value;
                JFData.IsOption2 = _isOption2 = !value;
                NotifyPropertyChanged();
                NotifyPropertyChanged(nameof(IsOption2));
                TriggerEnableBtnCrePoints();
                MakeChangeData();

                var en = value ? "enable" : "disable";
                Status = $"Option 1 is {en}.";
            }
        }

        private string _pointCount;
        public string PointCount
        {
            get { return _pointCount; }
            set
            {
                _pointCount = value;
                TbxPointCount_TextChanged(value);
                NotifyPropertyChanged();
            }
        }

        private bool _isOption2;
        public bool IsOption2
        {
            get { return _isOption2; }
            set
            {
                JFData.IsOption2 = _isOption2 = value;
                JFData.IsOption1 = _isOption1 = !value;
                NotifyPropertyChanged();
                NotifyPropertyChanged(nameof(IsOption1));
                TriggerEnableBtnCrePoints();
                MakeChangeData();

                var en = value ? "enable" : "disable";
                Status = $"Option 2 is {en}.";
            }
        }

        private string _btnSelRefStartContent = Constant.NoSel;
        public string BtnSelRefStartContent
        {
            get { return _btnSelRefStartContent; }
            set
            {
                _btnSelRefStartContent = value;
                NotifyPropertyChanged();
            }
        }

        private DelegateCommand _cmdSelRefSt;
        public DelegateCommand CmdSelRefSt
        {
            get
            {
                return _cmdSelRefSt ?? (_cmdSelRefSt = new DelegateCommand(
                    () => JFData?.IsOption2 == true && JFData?.LstEdge.Count > 0,
                    async () => await SelStartObj()));
            }
        }

        private bool _isSelectingRefStart;
        public bool IsSelectingRefStart
        {
            get { return _isSelectingRefStart; }
            set
            {
                _isSelectingRefStart = value;
                if (value)
                {
                    BtnSelRefStartContent = Constant.NoSel;
                }
                NotifyPropertyChanged();
            }
        }

        private string _distance;
        public string Distance
        {
            get { return _distance; }
            set
            {
                _distance = value;
                TbxDistance_TextChanged(value);
                NotifyPropertyChanged();
            }
        }

        private string _spacing;
        public string Spacing
        {
            get { return _spacing; }
            set
            {
                _spacing = value;
                TbxSpacing_TextChanged(value);
                NotifyPropertyChanged();
            }
        }

        private bool _isMultiPointCre;
        public bool IsMultiPointCre
        {
            get { return _isMultiPointCre; }
            set
            {
                _isMultiPointCre = value;
                NotifyPropertyChanged();
                var en = value ? "enable" : "disable";
                Status = $"Preserving created Welding Points is {en}.";
            }
        }

        private DelegateCommand _cmdCreWeldPoints;
        public DelegateCommand CmdCreWeldPoints
        {
            get
            {
                return _cmdCreWeldPoints ?? (_cmdCreWeldPoints = new DelegateCommand(
                    () => JFData?.LstEdge.Count > 0 && JFData.Offset != null &&
                        ((JFData.IsOption1 == true && JFData.PointsCount >= 0) ||
                         (JFData.IsOption2 == true && JFData.RefStartObj != null &&
                          JFData.DistFromSt >= 0 && JFData.Spacing >= 0)),
                    async () => await CreateWeldPoints()));
            }
        }

        /// <summary>
        /// 
        /// </summary>
        public VMSpotWeldOnSurface(string name)
        {
            Name = name;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async Task SelEdge()
        {
            LoadScrDC.ShowMode(LoadScrMode.Selection);

            if (!CPr.SetForegroundCATIA(CATIA))
            {
                LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                return;
            }

            if (JFData.Surface == null)
            {
                BL.ShowCritMsg("You haven't select the Surface yet!");
                LoadScrDC.ShowMode(LoadScrMode.None);
                return;
            }
            if (!await Task.Run(() =>
            {
                try
                {
                    JFData.SetShow(JFData.Surface);
                    JFData.SetShow(JFData.GeoInput);
                    return true;
                }
                catch { return false; }
            }))
            {
                BL.ShowCritMsg("Cannot set visible the Surface!");
            }

            JFData.LstEdge.Clear();
            SelEdgeLen = 0;
            IsSelectingEdge = true;
            bool isOK;
            Edge obj = null;
            IEnumerable<Edge> edges = null;
            while (true)
            {
                if (!CPr.SetForegroundCATIA(CATIA))
                {
                    LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                    return;
                }

                if (IsMultiSelEdge)
                {
                    (isOK, edges) = await SelectObj3<Edge>("Offset Edge", JFData.SelectionPart);
                }
                else
                {
                    (isOK, obj) = await Parent.SelectObj<Edge>("Offset Edge", JFData.SelectionPart);
                }
                if (isOK)
                {
                    if (IsMultiSelEdge)
                    {
                        bool isCheckBelong = true;
                        for (int i = 1; i <= JFData.SelectionPart.Count; i++)
                        {
                            if (await CPr.IsSelectedBelongTo(JFData.Product, JFData.SelectionPart.Item(i)) != true)
                            {
                                var msg = "Selected element is not belong to the Extracted Surface!\n";
                                msg += "Please select again!";
                                BL.ShowCritMsg(msg);
                                isCheckBelong = false;
                                break;
                            }
                        }

                        if (isCheckBelong)
                        {
                            edges.ToList().ForEach(x => JFData.LstEdge.Add(x));
                            await Task.Delay(80);
                            BtnSelEdgeContent = $"{JFData.LstEdge.Count} elements";
                            SelEdgeLen = await Task.Run(() => { return JFData.LstEdge.Select(x => JFData.MeasureLen(x)).Sum(); });
                            Status = "Select done!";
                            break;
                        }
                    }
                    else
                    {
                        if (await CPr.IsSelectedBelongTo(JFData.Product, JFData.SelectionPart.Item(1)) != true)
                        {
                            var msg = "Selected element is not belong to the Extracted Surface!\n";
                            msg += "Please select again!";
                            BL.ShowCritMsg(msg);
                        }
                        else
                        {
                            JFData.LstEdge.Add(obj);
                            await Task.Delay(80);
                            BtnSelEdgeContent = CATIA.get_StatusBar();
                            SelEdgeLen = JFData.MeasureLen(obj);
                            Status = "Select done!";
                            break;
                        }
                    }
                }
                else
                {
                    Status = "Cancel selection!";
                    break;
                }
            }

            IsSelectingEdge = false;
            LoadScrDC.ShowMode(LoadScrMode.None);
            await SCA_SelEdge();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void TbxOffset_TextChanged(string txt)
        {
            MakeChangeData();

            if (!double.TryParse(txt, out double val))
            {
                JFData.Offset = null;
            }
            else
            {
                JFData.Offset = val;
            }

            TriggerEnableBtnCrePoints();
            CmdPreviewOffset.NotifyCanExecuteChanged();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        private async Task<bool> CreateWeldLine(bool isContinue = false)
        {
            if (!isContinue)
            {
                LoadScrDC.ShowMode(LoadScrMode.Loading, "Creating preview Welding line ...");
            }

            var value = false;

            await Parent.CheckDeleteOldData();

            if (await CPr.CreateWeldLine(JFData, IsMultiPointCre, IsMultiSelEdge))
            {
                if (!isContinue)
                {
                    BL.ShowInfoMsg("Create Welding line successfully!");
                }
                CATIA.RefreshDisplay = true;
                value = true;
            }
            else
            {
                var msg = "Cannot create Welding Line!\n";
                msg += "Please make sure that the created line is lying on the Surface. " +
                       "You may try reversing the Offset value!";
                BL.ShowCritMsg(msg);
            }

            if (!CPr.SetForegroundCATIA(CATIA))
            {
                LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                return false;
            }

            //BtnCreateWeldLine.IsEnabled = true;
            Status = "Create Welding Line finish!";
            await Task.Delay(100);
            if (!isContinue)
            {
                await SCA_CreateWeldLine();
            }

            if (!isContinue)
            {
                LoadScrDC.ShowMode(LoadScrMode.None);
            }
            return value;
        }

        /// <summary>
        /// 
        /// </summary>
        public void TriggerEnableBtnCrePoints()
        {
            CmdSelRefSt.NotifyCanExecuteChanged();
            CmdCreWeldPoints.NotifyCanExecuteChanged();
            CmdCreWeldSpot.NotifyCanExecuteChanged();
            Parent.SCA_CreWeldBody();
        }

        /// <summary>
        /// 
        /// </summary>
        private void MakeChangeData()
        {
            IsDataChanged = true;
            CmdCreWeldSpot.NotifyCanExecuteChanged();
            CmdCreReport.NotifyCanExecuteChanged();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void TbxPointCount_TextChanged(string txt)
        {
            MakeChangeData();

            if (!int.TryParse(txt, out int val) || val < 0)
            {
                JFData.PointsCount = null;
            }
            else
            {
                JFData.PointsCount = val;
            }

            TriggerEnableBtnCrePoints();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async Task SelStartObj()
        {
            LoadScrDC.ShowMode(LoadScrMode.Selection);
            if (!CPr.SetForegroundCATIA(CATIA))
            {
                LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                return;
            }

            IsSelectingRefStart = true;
            JFData.RefStartObj = null;
            while (true)
            {
                if (!CPr.SetForegroundCATIA(CATIA))
                {
                    LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                    return;
                }

                var (isOK, obj) = await Parent.SelectObj<AnyObject>("Start Reference", JFData.SelectionPart);
                if (isOK)
                {
                    if (await Task.Run(() =>
                    {
                        var typeN = BL.GetCOMType(obj);
                        var hspoc = nameof(HybridShapePointOnCurve);
                        var objName = typeN == hspoc ? obj.get_Name() : ((AnyObject)obj.Parent).get_Name();
                        return JFData.LstPointsOnLine.Any(x => Equals(objName, x.get_Name()));
                    }))
                    {
                        var msg = "Cannot select a created Welding Point as Reference!\n";
                        msg += "Please select again!";
                        BL.ShowCritMsg(msg);
                    }
                    else
                    {
                        JFData.RefStartObj = obj;
                        await Task.Delay(80);
                        BtnSelRefStartContent = CATIA.get_StatusBar();
                        Status = "Select done!";
                        break;
                    }
                }
                else
                {
                    Status = "Cancel selection!";
                    break;
                }
            }

            LoadScrDC.ShowMode(LoadScrMode.None);
            IsSelectingRefStart = false;
            await SCA_SelStartObj();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void TbxDistance_TextChanged(string txt)
        {
            MakeChangeData();

            if (!double.TryParse(txt, out double val) || val < 0)
            {
                JFData.DistFromSt = null;
            }
            else
            {
                JFData.DistFromSt = val;
            }

            TriggerEnableBtnCrePoints();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void TbxSpacing_TextChanged(string txt)
        {
            MakeChangeData();

            if (!double.TryParse(txt, out double val) || val < 0)
            {
                JFData.Spacing = null;
            }
            else
            {
                JFData.Spacing = val;
            }

            TriggerEnableBtnCrePoints();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async Task CreateWeldPoints()
        {
            await Parent.CheckDeleteOldData();

            var msg = "Creating Welding Points ...";
            //msg += "Don't manipulate CATIA while the process is running to avoid unexpected result!";
            LoadScrDC.ShowMode(LoadScrMode.Loading, msg);

            if (await CreateWeldLine(true))
            {
                var resu = await CPr.CreateWeldPoints(JFData, IsMultiPointCre, IsMultiSelEdge);
                if (resu == true)
                {
                    BL.ShowInfoMsg("Create Welding Points successfully!");
                }
                else
                {
                    if (resu == false)
                    {
                        msg = "Cannot create Welding Points!\n";
                        BL.ShowCritMsg(msg);
                    }
                }
            }

            if (!CPr.SetForegroundCATIA(CATIA))
            {
                LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                return;
            }

            await Task.Delay(100);
            IsDataChanged = false;
            Status = "Create Welding Points finish!";
            LoadScrDC.ShowMode(LoadScrMode.None);
            TriggerEnableBtnCrePoints();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        private async Task<(bool, IEnumerable<T>)> SelectObj3<T>(string objName, Selection sel) where T : AnyObject
        {
            try
            {
                Status = $"Waiting for user to select {objName} in CATIA ...";
                IEnumerable<T> lstObj = null;

                while (true)
                {
                    bool? isOk;
                    (isOk, lstObj) = await CPr.SelectObj3<T>(sel, $"Select {objName} ...");

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

                return (lstObj != null, lstObj);
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
        public override async Task SCA_FromParent()
        {
            JFData.LstEdge.Clear();
            SelEdgeLen = 0;
            BtnSelEdgeContent = Constant.NoSel;
            CmdSelEdge.NotifyCanExecuteChanged();

            await SCA_SelEdge();
        }

        /// <summary>
        /// 
        /// </summary>
        private async Task SCA_SelEdge()
        {
            await Task.Run(() =>
            {
                try
                {
                    if (!IsMultiPointCre)
                    {
                        if (JFData.Config.General.IsClearTemp)
                        {
                            JFData.LstWeldLine.ForEach(x =>
                            {
                                x.DeletePoints(JFData);
                                JFData.DeleteObj(x.Line);
                            });
                        }
                        JFData.LstWeldLine.Clear();
                    }
                    JFData.Update();
                }
                catch (Exception ex)
                {
                    BL.Log(ex);
                }
            });

            //DPnOffset.IsEnabled = BtnSelEdge.IsEnabled && JFData.Edge != null;
            CmdPreviewOffset.NotifyCanExecuteChanged();
            await SCA_CreateWeldLine();
        }

        /// <summary>
        /// 
        /// </summary>
        private async Task SCA_CreateWeldLine()
        {
            JFData.RefStartObj = null;
            BtnSelRefStartContent = Constant.NoSel;

            CmdSelRefSt.NotifyCanExecuteChanged();
            await SCA_SelStartObj();
        }

        /// <summary>
        /// 
        /// </summary>
        private async Task SCA_SelStartObj()
        {
            if (!await Task.Run(() =>
            {
                if (!IsMultiPointCre && JFData.Config.General.IsClearTemp)
                {
                    JFData.LstWeldLine.ForEach(ln => ln.DeletePoints(JFData));
                }
                return JFData.Update();
            }))
            {
                var msg = "There are errors on the Specification Tree!\n";
                msg += "Please manually update to check and fix, then try again!";
                BL.ShowCritMsg(msg);
            }
            TriggerEnableBtnCrePoints();
        }
    }
}
