using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.DataModel;

namespace Welding_Tool.ViewModel.Setting
{
    public class VMSettSpotWeld : VMSettingViewBase
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

        public ObservableCollection<WeldingShape> LstShape { get; private set; }

        public WeldingShape WeldShape
        {
            get { return data.Shape; }
            set
            {
                data.Shape = value;
                NotifyPropertyChanged();
            }
        }

        private string _radius;
        public string Radius
        {
            get { return _radius; }
            set
            {
                _radius = value;
                async void act() => data.Radius = await ValidateRadius(value, double.Epsilon); act();
            }
        }

        private string _spacing;
        public string Spacing
        {
            get { return _spacing; }
            set
            {
                _spacing = value;
                async void act() => data.Spacing = await ValidateRadius(value, double.Epsilon); act();
            }
        }

        private string _maxAllowGap;
        public string MaxAllowGap
        {
            get { return _maxAllowGap; }
            set
            {
                _maxAllowGap = value;
                async void act() => data.MaxAllowanceGap = await ValidateRadius(value, 0); act();
            }
        }

        private ModelSettingData.SettSpotWelding data;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="_name"></param>
        public VMSettSpotWeld(string _name, ModelSettingData.SettSpotWelding _data)
        {
            Name = _name;
            data = _data;

            // Generate shape combo box source
            var temp = Enum.GetValues(typeof(WeldingShape)).Cast<WeldingShape>();
            LstShape = new ObservableCollection<WeldingShape>(temp);

            _radius = data.Radius.ToString();
            _spacing = data.Spacing.ToString();
            _maxAllowGap = data.MaxAllowanceGap .ToString();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="value"></param>
        private async Task<double> ValidateRadius(string value, double? Min = null, double? Max = null,
                                                 [CallerMemberName] string propertyName = "")
        {
            double dblVal = 0;
            var validationErrors = new List<string>();

            if (!await Task.Run(() => double.TryParse(value, out dblVal)))
            {
                validationErrors.Add("Value must be number!");
            }
            else if ((Min != null && dblVal < Min) || (Max != null && dblVal > Max))
            {
                validationErrors.Add(Min == double.Epsilon ? "Value must larger than 0!"
                                                           : "Value cannot be negative!");
            }

            if (validationErrors.Count != 0)
            {
                /* Update the collection in the dictionary returned by the GetErrors method */
                _validationErrors[propertyName] = validationErrors;

                /* Raise event to tell WPF to execute the GetErrors method */
                RaiseErrorsChanged(propertyName);
            }
            else if (_validationErrors.ContainsKey(propertyName))
            {
                /* Remove all errors for this property */
                _validationErrors.Remove(propertyName);

                /* Raise event to tell WPF to execute the GetErrors method */
                RaiseErrorsChanged(propertyName);
            }

            RaiseValidationError();
            return dblVal;
        }
    }
}
