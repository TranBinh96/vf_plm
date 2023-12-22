using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace Welding_Tool.Common
{
    internal class Common
    {
    }

    public class ObservableObjectModel : INotifyPropertyChanged, INotifyDataErrorInfo
    {
        public event PropertyChangedEventHandler PropertyChanged;
        /// <summary>
        /// 
        /// </summary>
        /// <param name="propertyName"></param>
        internal void NotifyPropertyChanged([CallerMemberName] string propertyName = "")
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        #region DataError

        internal readonly Dictionary<string, ICollection<string>>
            _validationErrors = new Dictionary<string, ICollection<string>>();

        public bool HasErrors => _validationErrors.Count > 0;

        public event EventHandler<DataErrorsChangedEventArgs> ErrorsChanged;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="propertyName"></param>
        /// <returns></returns>
        public IEnumerable GetErrors([CallerMemberName] string propertyName = "")
        {
            if (string.IsNullOrEmpty(propertyName) || !_validationErrors.ContainsKey(propertyName))
            {
                return null;
            }

            return _validationErrors[propertyName];
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="propertyName"></param>
        internal void RaiseErrorsChanged([CallerMemberName] string propertyName = "")
        {
            ErrorsChanged?.Invoke(this, new DataErrorsChangedEventArgs(propertyName));
        }

        #endregion
    }

    /// <summary>
    /// 
    /// </summary>
    public static class GeoNames
    {
        public const string FinalSur = "*Final Surface";

        public const string Input = "Input Geometry";
        public const string Temp = "Temp Geometry";
        public const string Construct = "Constructions";
        public const string OutputPoints = "OUTPUT WELD POINTS";
        public const string OutputAxis = "WELD AXIS";
        public const string OutputSpot = "WELD SPOTS";

        public const string Spot2T = "2_COMPONENTS";
        public const string Spot3T = "3_COMPONENTS";
        public const string Spot4T = "4_COMPONENTS";
    }

    /// <summary>
    /// 
    /// </summary>
    public static class PastOpt
    {
        public const string AsResult = "CATPrtResultWithOutLink";
    }

    public static class Constant
    {
        public const double WeldBodyHeight = 1.5;
        public const string CreJF = "Create JF Part";
        public const string NoSel = "No Selection";
        public const string TechObjGroup = "Groups";
        public const string TechObjClash = "Clashes";

        public const string TemplateExcelFile = @"Resources\TemplateReportFile.xlsx";
        public const string ReportFolder = @"VinFast\Welding Tool\Report Files";
        public const string ConfigFile = @"Resources\ConfigParameters.ini";
    }

    public static class JFProperties
    {
        public const string PropStartupModelName = "Startupmodel";
        public const string PropStartupModelValue = "VinFast-JF-Startmodel-Version-2.0";
    }

    public enum SpotWeldMode
    {
        ByPoints,
        OnSurface,
    }
}
