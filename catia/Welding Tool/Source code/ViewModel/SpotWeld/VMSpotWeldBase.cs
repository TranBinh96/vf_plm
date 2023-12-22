using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.Common;
using Welding_Tool.DataModel;
using CATApplication = INFITF.Application;

namespace Welding_Tool.ViewModel.SpotWeld
{
    public abstract class VMSpotWeldBase : ObservableObjectModel
    {
        internal VMSpotWeld Parent;
        public ModelJoinFile JFData { get => Parent.JFData; }
        private protected CATApplication CATIA { get => Parent.CATIA; }
        private protected ModelLoadingScreen LoadScrDC { get => Parent.LoadScrDC; }

        public string Name { get; private protected set; }
        private protected string Status
        {
            set { Parent.Status = value; }
        }

        private protected bool IsDataChanged
        {
            set { Parent.isDataChanged = value; }
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
        /// <returns></returns>
        public abstract Task SCA_FromParent();
    }
}
