using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.DataModel;

namespace Welding_Tool.ViewModel.Setting
{
    public class VMSettMagWeld : VMSettingViewBase
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

        private ModelSettingData.SettMagWelding data;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="_name"></param>
        public VMSettMagWeld(string _name, ModelSettingData.SettMagWelding _data)
        {
            Name = _name;
            data = _data;
        }
    }
}
