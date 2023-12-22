using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Media;
using Welding_Tool.Common;
using Welding_Tool.ViewModel;

namespace Welding_Tool.DataModel
{
    public delegate Task AsyncEvent();
    public class ModelLoadingScreen : ObservableObjectModel
    {
        private Visibility _visib = Visibility.Collapsed;
        public Visibility Visibility
        {
            get { return _visib; }
            set
            {
                _visib = value;
                NotifyPropertyChanged();
            }
        }

        private Brush _coverBrush = Brushes.Gray;
        public Brush CoverBrush
        {
            get { return _coverBrush; }
            set
            {
                _coverBrush = value;
                NotifyPropertyChanged();
            }
        }

        private double _coverOpaci = 0.3;
        public double CoverOpaci
        {
            get { return _coverOpaci; }
            set
            {
                _coverOpaci = value;
                NotifyPropertyChanged();
            }
        }

        private Visibility visibility = Visibility.Visible;
        public Visibility VisiLoading
        {
            get { return visibility; }
            set
            {
                visibility = value;
                NotifyPropertyChanged();
            }
        }

        private Visibility _visiCatNotFound = Visibility.Visible;
        public Visibility VisiCatNotFound
        {
            get { return _visiCatNotFound; }
            set
            {
                _visiCatNotFound = value;
                NotifyPropertyChanged();
            }
        }

        private string _loadingMsg;
        public string LoadingMsg
        {
            get { return _loadingMsg; }
            set
            {
                _loadingMsg = value;
                NotifyPropertyChanged();
            }
        }

        private bool _isIndeter;

        public bool IsIndeterminate
        {
            get { return _isIndeter; }
            set
            {
                _isIndeter = value;
                NotifyPropertyChanged();
            }
        }

        private double _progressValue = 0;
        public double ProgressValue
        {
            get { return _progressValue; }
        }

        public double MaxProgressValue { get; } = 1000;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="isVisibl"></param>
        public void SetVisible(bool isVisibl)
        {
            Visibility = isVisibl ? Visibility.Visible : Visibility.Collapsed;

            if (!isVisibl)
            {
                CoverOpaci = 0.5;
                CoverBrush = Brushes.Gray;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="mode"></param>
        public void ShowMode(LoadScrMode mode, string msg = "")
        {
            switch (mode)
            {
                case LoadScrMode.Loading:
                    VisiCatNotFound = Visibility.Collapsed;
                    VisiLoading = Visibility.Visible;
                    IsIndeterminate = true;
                    LoadingMsg = msg;
                    break;

                case LoadScrMode.Percentage:
                    VisiCatNotFound = Visibility.Collapsed;
                    VisiLoading = Visibility.Visible;
                    IsIndeterminate = false;
                    SetProgress(0);
                    LoadingMsg = msg;
                    break;

                case LoadScrMode.CatNotFound:
                    VisiLoading = Visibility.Collapsed;
                    VisiCatNotFound = Visibility.Visible;
                    LoadingMsg = "CAITA application not found!\n";
                    LoadingMsg += "Please open the CATIA application to use the tool!";
                    break;

                case LoadScrMode.Selection:
                    VisiLoading = Visibility.Collapsed;
                    VisiCatNotFound = Visibility.Collapsed;
                    CoverOpaci = 0.2;
                    CoverBrush = Brushes.Black;
                    break;

                case LoadScrMode.None:
                    SetVisible(false);
                    return;
            }

            SetVisible(true);
        }


        public AsyncEvent CheckCatiaAppEvent;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        public async Task CheckCatiaApp()
        {
            _isBtnEnable = false;
            CmdCheckCatia.NotifyCanExecuteChanged();

            await CheckCatiaAppEvent.Invoke();

            _isBtnEnable = true;
            CmdCheckCatia.NotifyCanExecuteChanged();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="value"></param>
        public void SetProgress(double value)
        {
            _progressValue = value * MaxProgressValue;
            NotifyPropertyChanged(nameof(ProgressValue));
        }

        private DelegateCommand _cmdChecCatia;
        public DelegateCommand CmdCheckCatia
        {
            get
            {
                return _cmdChecCatia ??
                (_cmdChecCatia = new DelegateCommand(
                    // Can execute
                    () => EnableBtn(),

                    // Execute
                    async () =>
                    {
                        await CheckCatiaApp();
                    }
                    )
                );
            }
        }

        private bool _isBtnEnable = true;
        private bool EnableBtn()
        {
            return _isBtnEnable;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="msg"></param>
        public void SetLoadingMsg(string msg)
        {
            LoadingMsg = msg;
        }
    }

    public enum LoadScrMode
    {
        None,
        Loading,
        Percentage,
        CatNotFound,
        Selection,
    }
}
