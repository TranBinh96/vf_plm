using HybridShapeTypeLib;
using ProductStructureTypeLib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.Common;

namespace Welding_Tool.DataModel
{
    /// <summary>
    /// 
    /// </summary>
    public class ModelReportData
    {
        public string SpotWeldName;
        public string JoinPartsName
        {
            get
            {
                var str = "";
                WeldedParts.ForEach(x => 
                {
                    var name = x.Name;
                    var idxUnderScore = name.IndexOf("_");
                    if (idxUnderScore != -1)
                    {
                        name = name.Substring(0, idxUnderScore);
                    }
                    str += $"{name}_";
                });
                return str.Substring(0, str.Length - 1 >= 0 ? str.Length - 1 : 0);
            }
        }

        public Point Coordinate;
        public PointCoord Coord;
        public int PanelsCount;
        public string StackUpType { get { return $"{PanelsCount}T"; } }
        public double JoinThickness;
        public double JoinDiameter;
        public List<ReportPartInfo> WeldedParts = new List<ReportPartInfo>();
        public PointCoord AxisDirection;

        /// <summary>
        /// 
        /// </summary>
        public void CalCoord()
        {
            Array co = new object[3];
            Coordinate.GetCoordinates(co);
            Coord = new PointCoord();
            if (double.TryParse(co.GetValue(0).ToString(), out var val))
            {
                Coord.X = val;
            }
            if (double.TryParse(co.GetValue(1).ToString(), out val))
            {
                Coord.Y = val;
            }
            if (double.TryParse(co.GetValue(2).ToString(), out val))
            {
                Coord.Z = val;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="axis"></param>
        public void GetWeldAxis(HybridShapeLinePtPt axis)
        {
            Array dir = new object[3];
            axis.GetDirection(dir);
            AxisDirection = new PointCoord();
            if (double.TryParse(dir.GetValue(0).ToString(), out var val))
            {
                AxisDirection.X = val;
            }
            if (double.TryParse(dir.GetValue(1).ToString(), out val))
            {
                AxisDirection.Y = val;
            }
            if (double.TryParse(dir.GetValue(2).ToString(), out val))
            {
                AxisDirection.Z = val;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="prd"></param>
        /// <returns></returns>
        public string GetMaterial(Product prd)
        {
            string value;
            try
            {
                value = prd.Parameters.Item("Material").ValueAsString();
            }
            catch
            {
                value = "N/A";
            }

            return value;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        public double TotalPanelThick()
        {
            return WeldedParts.Select(x => x.Gauge).Sum();
        }

        public double BlankSpace { get; private set; }
        public bool IsSkip { get; private set; }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        public bool CheckSkip(double maxGap)
        {
            BlankSpace = JoinThickness - TotalPanelThick();
            IsSkip = PanelsCount == 1 ||
                     BlankSpace > maxGap;
            return IsSkip;
        }
    }

    /// <summary>
    /// 
    /// </summary>
    public class PointCoord
    {
        public double X;
        public double Y;
        public double Z;
    }

    /// <summary>
    /// 
    /// </summary>
    public class ReportPartInfo
    {
        public string Name;
        public string Material;
        public double Gauge;
    }
}
