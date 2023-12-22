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
    public class VMSpotWeldByPoints : VMSpotWeldBase
    {
        private DelegateCommand _cmdSelGeoPoints;
        public DelegateCommand CmdSelGeoPoints
        {
            get
            {
                return _cmdSelGeoPoints ?? (_cmdSelGeoPoints = new DelegateCommand(
                    () => JFData?.Surface != null,
                    async () => await SelGeoPoints()));
            }
        }

        private bool _isSelectingGeoPoints;
        public bool IsSelectingGeoPoints
        {
            get { return _isSelectingGeoPoints; }
            set
            {
                _isSelectingGeoPoints = value;
                if (value)
                {
                    BtnSelGeoPointsContent = Constant.NoSel;
                }
                NotifyPropertyChanged();
            }
        }

        private string _btnSelGeoPointsContent = Constant.NoSel;
        public string BtnSelGeoPointsContent
        {
            get { return _btnSelGeoPointsContent; }
            set
            {
                _btnSelGeoPointsContent = value;
                NotifyPropertyChanged();
            }
        }

        /// <summary>
        /// 
        /// </summary>
        public VMSpotWeldByPoints(string name)
        {
            Name = name;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async Task SelGeoPoints()
        {
            await Parent.CheckDeleteOldData();

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

            JFData.GeoPoints = null;
            IsSelectingGeoPoints = true;
            while (true)
            {
                if (!CPr.SetForegroundCATIA(CATIA))
                {
                    LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                    return;
                }

                var (isOK, obj) = await Parent.SelectObj<HybridBody>("Geometrical Set of Points", JFData.SelectionPart);
                if (isOK)
                {
                    if (await CPr.IsSelectedBelongTo(JFData.Product, JFData.SelectionPart.Item(1)) != true)
                    {
                        var msg = "Selected Geometrical Set is not belong to the JF Part!\n";
                        msg += "Please select again!";
                        BL.ShowCritMsg(msg);
                    }
                    else
                    {
                        JFData.GeoPoints = obj;
                        BtnSelGeoPointsContent = obj.get_Name();
                        Status = "Select done!";

                        await CPr.GetPointInGeo(JFData);
                        break;
                    }
                }
                else
                {
                    Status = "Cancel selection!";
                    break;
                }
            }

            IsSelectingGeoPoints = false;
            LoadScrDC.ShowMode(LoadScrMode.None);
            SCA_SelGeoPoints();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        public override async Task SCA_FromParent()
        {
            JFData.GeoPoints = null;
            BtnSelGeoPointsContent = Constant.NoSel;
            CmdSelGeoPoints.NotifyCanExecuteChanged();
            await Task.Delay(5);
            SCA_SelGeoPoints();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        private void SCA_SelGeoPoints()
        {
            Parent.CmdCreWeldSpot.NotifyCanExecuteChanged();
            Parent.SCA_CreWeldBody();
        }

    }
}
