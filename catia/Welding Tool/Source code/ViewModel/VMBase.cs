using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.Common;
using Welding_Tool.DataModel;
using CATApplication = INFITF.Application;

namespace Welding_Tool.ViewModel
{
    public abstract class VMBase : ObservableObjectModel
    {
        public CATApplication CATIA;
        public Action SCA_SelSurface;
        public MainWindow Parent;
        public DelegateCommand CmdSelSur { get; set; }
        public string Name { get; private protected set; }
        public abstract bool IsCreWeldSpotEn { get; }

        private string _btnSelSurContent = Constant.NoSel;
        public string BtnSelSurContent
        {
            get { return _btnSelSurContent; }
            set
            {
                _btnSelSurContent = value;
                NotifyPropertyChanged();
            }
        }

        public string Status
        {
            set { Parent.Status = value; }
        }

        /// <summary>
        ///     Content to display on the Weld mode combo box
        /// </summary>
        /// <returns></returns>
        public override string ToString()
        {
            return Name;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="ReportData"></param>
        /// <param name="reporter"></param>
        /// <returns></returns>
        public abstract Task<bool> CreateWeldBody(List<ModelReportData> ReportData, IProgress<ModelReport> reporter);
    }
}
