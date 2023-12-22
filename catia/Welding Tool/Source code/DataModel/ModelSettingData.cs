using ProductStructureTypeLib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Permissions;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Windows.Media.Animation;

namespace Welding_Tool.DataModel
{
    [Serializable]
    public class ModelSettingData
    {
        [Serializable]
        public class SettGeneral
        {
            public bool IsClearTemp { get; set; }
            public bool IsRearrange { get; set; }
            public bool IsMarkPtDone { get; set; }
            public bool IsAutoCreReport { get; set; }
        }

        [Serializable]
        public class SettSpotWelding
        {
            public WeldingShape Shape { get; set; }
            public double Radius { get; set; }
            public double Spacing { get; set; }
            public double MaxAllowanceGap { get; set; }
            public double DetectRange { get; set; }
        }

        [Serializable]
        public class SettMagWelding
        {

        }

        public SettGeneral General { get; set; }
        public SettSpotWelding SpotWeld { get; set; }
        public SettMagWelding MagWeld { get; set; }
        public bool IsCancel;
        public bool IsDataChanged;

        /// <summary>
        /// 
        /// </summary>
        public ModelSettingData()
        {
            IsCancel = false;
            General = new SettGeneral();
            SpotWeld = new SettSpotWelding();
            MagWeld = new SettMagWelding();
        }
    }

    /// <summary>
    /// 
    /// </summary>
    public enum WeldingShape
    {
        Cylinder,
        Sphere,
    }
}
