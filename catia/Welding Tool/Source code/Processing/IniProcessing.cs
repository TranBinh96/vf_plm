using IniParser;
using IniParser.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.Common;
using Welding_Tool.DataModel;

namespace Welding_Tool.Processing
{
    /// <summary>
    /// 
    /// </summary>
    public static class IniProcessing
    {
        /// <summary>
        /// 
        /// </summary>
        /// <param name="path"></param>
        /// <param name="config"></param>
        /// <returns></returns>
        public static bool ReadIniFile(string path, ModelSettingData config)
        {
            try
            {
                var parser = new FileIniDataParser();
                IniData data = parser.ReadFile(path);

                if (!int.TryParse(data[IniSec.SecGeneral][IniKey.KeyClearTemp], out var tmp))
                {
                    return false;
                }

                if (!int.TryParse(data[IniSec.SecGeneral][IniKey.KeyRearrange], out var rea))
                {
                    return false;
                }

                if (!int.TryParse(data[IniSec.SecGeneral][IniKey.KeyMarkPtDone], out var mrk))
                {
                    return false;
                }

                if (!int.TryParse(data[IniSec.SecSpotWeld][IniKey.KeyWeldShape], out var shp))
                {
                    return false;
                }

                if (!double.TryParse(data[IniSec.SecSpotWeld][IniKey.KeyWeldRadius], out var radius))
                {
                    return false;
                }

                if (!double.TryParse(data[IniSec.SecSpotWeld][IniKey.KeyWeldSpacing], out var space))
                {
                    return false;
                }

                if (!double.TryParse(data[IniSec.SecSpotWeld][IniKey.KeyMaxAirGap], out var gap))
                {
                    return false;
                }

                if (!double.TryParse(data[IniSec.SecSpotWeld][IniKey.KeyClashRange], out var rng))
                {
                    return false;
                }

                config.General.IsClearTemp = tmp != 0;
                config.General.IsRearrange = rea != 0;
                config.General.IsMarkPtDone = mrk != 0;

                config.SpotWeld.Shape = (WeldingShape)shp;
                config.SpotWeld.Radius = radius;
                config.SpotWeld.Spacing = space;
                config.SpotWeld.MaxAllowanceGap = gap;
                config.SpotWeld.DetectRange = rng;
                return true;
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return false;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="path"></param>
        /// <param name="config"></param>
        public static bool WriteToIni(string path, ModelSettingData config)
        {
            try
            {
                var parser = new FileIniDataParser();
                IniData data = parser.ReadFile(path);

                data[IniSec.SecGeneral][IniKey.KeyClearTemp] = (config.General.IsClearTemp ? 1 : 0).ToString();
                data[IniSec.SecGeneral][IniKey.KeyRearrange] = (config.General.IsRearrange ? 1 : 0).ToString();
                data[IniSec.SecGeneral][IniKey.KeyMarkPtDone] = (config.General.IsMarkPtDone ? 1 : 0).ToString();

                data[IniSec.SecSpotWeld][IniKey.KeyWeldShape] = ((int)config.SpotWeld.Shape).ToString();
                data[IniSec.SecSpotWeld][IniKey.KeyWeldRadius] = config.SpotWeld.Radius.ToString();
                data[IniSec.SecSpotWeld][IniKey.KeyMaxAirGap] = config.SpotWeld.MaxAllowanceGap.ToString();
                data[IniSec.SecSpotWeld][IniKey.KeyWeldSpacing] = config.SpotWeld.Spacing.ToString();
                data[IniSec.SecSpotWeld][IniKey.KeyClashRange] = config.SpotWeld.DetectRange.ToString();

                parser.WriteFile(path, data);
                return true;
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return false;
            }
        }
    }

    /// <summary>
    /// 
    /// </summary>
    public static class IniSec
    {
        public const string SecGeneral = "GENERAL";
        public const string SecSpotWeld = "SPOT_WELD_PARAMETERS";
    }

    /// <summary>
    /// 
    /// </summary>
    public static class IniKey
    {
        public const string KeyClearTemp = "Clear_Temp";
        public const string KeyRearrange = "Rearrange";
        public const string KeyMarkPtDone = "MarkDonePoint";

        public const string KeyWeldShape = "Weld_Shape";
        public const string KeyWeldRadius = "Weld_Radius";
        public const string KeyWeldSpacing = "Weld_Spacing";
        public const string KeyMaxAirGap = "Max_Air_Gap";
        public const string KeyClashRange = "Detect_Weld_Part_Range";
    }
}
