using PubSub;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using Welding_Tool.Common;
using Welding_Tool.DataModel;
using Welding_Tool.Processing;
using Welding_Tool.ViewModel.Setting;

namespace Welding_Tool.ViewModel
{
    public class VMSetting : ObservableObjectModel
    {
        public ObservableCollection<VMSettingViewBase> ListViewSetting { get; set; }
        public Stack<int> PreviousViews { get; set; }

        private VMSettingViewBase _selectedView;
        public VMSettingViewBase SelectedView
        {
            get { return _selectedView; }
            set
            {
                PreviousViews?.Push(ListViewSetting.IndexOf(_selectedView));
                _selectedView = value;
                NotifyPropertyChanged();
                CmdPreView.NotifyCanExecuteChanged();
            }
        }

        private DelegateCommand _cmdPreView;
        public DelegateCommand CmdPreView
        {
            get
            {
                return _cmdPreView ?? (_cmdPreView = new DelegateCommand(
                    () => PreviousViews?.Count > 0,
                    () =>
                    {
                        _selectedView = ListViewSetting[PreviousViews.Pop()];
                        NotifyPropertyChanged(nameof(SelectedView));
                        CmdPreView.NotifyCanExecuteChanged();
                    }));
            }
        }

        private DelegateCommandWithParam _cmdSave;
        public DelegateCommandWithParam CmdSave
        {
            get
            {
                return _cmdSave ?? (_cmdSave = new DelegateCommandWithParam(
                    () => setSpot?.HasDataValidationError == false,
                    (wd) =>
                    {
                        _settCurrentData.IsCancel = false;
                        ((Window)wd)?.Close();
                    }
                ));
            }
        }

        private DelegateCommandWithParam _cmdCancel;
        public DelegateCommandWithParam CmdCancel
        {
            get
            {
                return _cmdCancel ?? (_cmdCancel = new DelegateCommandWithParam(
                    () => true,
                    (wd) =>
                    {
                        _settCurrentData.IsCancel = true;
                        ((Window)wd)?.Close();
                    }
                ));
            }
        }

        private ModelSettingData _settOriginData;
        private ModelSettingData _settCurrentData;
        private Hub _hub = Hub.Default;
        VMSettGeneral setGen;
        VMSettSpotWeld setSpot;
        VMSettMagWeld setMag;

        /// <summary>
        /// 
        /// </summary>
        public VMSetting()
        {
            ListViewSetting = new ObservableCollection<VMSettingViewBase>();

            _hub.Subscribe<ModelSettingData>(this, data => InitData(data));
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="data"></param>
        private void InitData(ModelSettingData data)
        {
            _hub.Unsubscribe<ModelSettingData>(this);
            _settCurrentData = BL.DeepClone(data);
            _settOriginData = data;

            ListViewSetting.Add(setGen = new VMSettGeneral("General", _settCurrentData.General));
            ListViewSetting.Add(setSpot = new VMSettSpotWeld("Spot Welding", _settCurrentData.SpotWeld));
            ListViewSetting.Add(setMag = new VMSettMagWeld("Mag Welding", _settCurrentData.MagWeld));

            setSpot.RaiseValidationError = () => CmdSave.NotifyCanExecuteChanged();
            setSpot.RaiseValidationError();

            SelectedView = ListViewSetting[0];
            PreviousViews = new Stack<int>();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        internal void OnClosingWindow(object sender, CancelEventArgs e)
        {
            _settOriginData.IsDataChanged = !BL.IsEquals(_settCurrentData, _settOriginData);

            if (_settOriginData.IsDataChanged)
            {
                if (_settCurrentData.IsCancel)
                {
                    if (!BL.ShowWarnQuesMsg("Your chanegs won't be saved!\nDo you want to exit?", "Close"))
                    {
                        e.Cancel = true;
                    }
                    else
                    {
                        _settOriginData.IsDataChanged = false;
                    }
                }
                else
                {
                    BL.ShallowCopy(_settCurrentData, _settOriginData);
                }
            }
        }
    }
}
