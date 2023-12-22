using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.DataModel;

namespace Welding_Tool.ViewModel.Setting
{
    public class VMSettGeneral : VMSettingViewBase
    {
        private string _name;
        public override string Name
        {
            get => _name;
            private protected set
            {
                _name = value;
            }
        }

        public bool IsClearTemp
        {
            get { return data.IsClearTemp; }
            set
            {
                data.IsClearTemp = value;
                NotifyPropertyChanged();
            }
        }

        public bool IsRearrange
        {
            get { return data.IsRearrange; }
            set
            {
                data.IsRearrange = value;
                NotifyPropertyChanged();
            }
        }

        public bool IsMarkPointDone
        {
            get { return data.IsMarkPtDone; }
            set
            {
                data.IsMarkPtDone = value;
                NotifyPropertyChanged();
            }
        }

        public bool IsAutoCreReport
        {
            get { return data.IsAutoCreReport; }
            set
            {
                data.IsAutoCreReport = value;
                NotifyPropertyChanged();
            }
        }

        private ModelSettingData.SettGeneral data;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="name"></param>
        /// <param name="_data"></param>
        public VMSettGeneral(string name, ModelSettingData.SettGeneral _data)
        {
            Name = name;
            data = _data;
        }
    }
}
