using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.Common;

namespace Welding_Tool.ViewModel.Setting
{
    public abstract class VMSettingViewBase : ObservableObjectModel
    {
        public abstract string Name { get; private protected set; }
        public override string ToString()
        {
            return Name;
        }

        public bool HasDataValidationError => _validationErrors.Any();
        public Action RaiseValidationError;
    }
}
