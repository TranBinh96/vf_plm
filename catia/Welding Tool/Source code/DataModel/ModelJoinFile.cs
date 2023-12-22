using HybridShapeTypeLib;
using INFITF;
using MECMOD;
using ProductStructureTypeLib;
using SPATypeLib;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.Common;
using Point = HybridShapeTypeLib.Point;

namespace Welding_Tool.DataModel
{
    public class ModelJoinFile : ObservableObjectModel
    {
        public string RootName;
        public string JFPartName;

        public Product RootPrd;

        private Product _prodc;
        public Product Product
        {
            get => _prodc;
            set
            {
                _prodc = value;
                NotifyPropertyChanged(nameof(IsJFPartCreated));
            }
        }

        public bool IsJFPartCreated => _prodc != null;

        public ProductDocument ProductDoc;
        public Part Part;
        public PartDocument PartDoc;

        public HybridShapeFactory Factory;
        public SPAWorkbench WBench;
        public Selection SelectionProd;
        public Selection SelectionPart;

        public HybridBody GeoInput;
        public HybridBody GeoConstruction;
        public HybridBody GeoTempPtCons;
        public HybridBody GeoTempCons;
        public HybridBody GeoTemp;
        public HybridBody GeoOutWeldSpot;
        public HybridBody GeoOutWeldPoints;
        public HybridBody GeoOutWeldAxis;

        public HybridBody Geo2T;
        public HybridBody Geo3T;
        public HybridBody Geo4T;

        public object Temp;
        //public Body Body1;
        //public Body Body2;
        //public Body CopiedBody1;
        //public Body CopiedBody2;

        public bool IsSurfaceSelected { get => _surface != null; }

        private HybridShape _surface;
        public HybridShape Surface
        {
            get { return _surface; }
            set
            {
                _surface = value;
                NotifyPropertyChanged(nameof(IsSurfaceSelected));
            }
        }

        public bool IsGeoPointsSelected { get => _geoPoints != null; }

        private HybridBody _geoPoints;
        public HybridBody GeoPoints
        {
            get { return _geoPoints; }
            set
            {
                _geoPoints = value;
                NotifyPropertyChanged(nameof(IsGeoPointsSelected));
            }
        }

        private ObservableCollection<Edge> _lstEdge = new ObservableCollection<Edge>();
        public ObservableCollection<Edge> LstEdge
        {
            get { return _lstEdge; }
            set
            {
                _lstEdge = value;
                NotifyPropertyChanged();
            }
        }

        public bool IsEdgeSelected { get => LstEdge.Count > 0; }

        public ModelListWeldLine LstWeldLine = new ModelListWeldLine();

        public bool IsRefStartObjSelected { get => _refStartObj != null; }

        private AnyObject _refStartObj;
        public AnyObject RefStartObj
        {
            get { return _refStartObj; }
            set
            {
                _refStartObj = value;
                NotifyPropertyChanged(nameof(IsRefStartObjSelected));
            }
        }

        private double? _offset;
        public double? Offset
        {
            get { return _offset; }
            set
            {
                _offset = value;
                NotifyPropertyChanged();
            }
        }

        private double? _pointCount;
        public double? PointsCount
        {
            get { return _pointCount; }
            set
            {
                _pointCount = value;
                NotifyPropertyChanged();
            }
        }

        private double? _spacing;
        public double? Spacing
        {
            get { return _spacing; }
            set
            {
                _spacing = value;
                NotifyPropertyChanged();
            }
        }

        private double? _disFromSt;
        public double? DistFromSt
        {
            get { return _disFromSt; }
            set
            {
                _disFromSt = value;
                NotifyPropertyChanged();
            }
        }

        public IEnumerable<Point> LstPointsOnLine
        {
            get => LstWeldLine.Select(x => x.LstChildPoint).SelectMany(x => x);
        }

        public int PointOnLineCount
        {
            get => LstWeldLine.Select(x => x.LstChildPoint.Count).Sum();
        }

        public string LstPointOnLineCount
        {
            get => PointOnLineCount.ToString();
        }

        internal bool IsOption1;
        internal bool IsOption2;

        //internal double WeldLength;

        public List<HybridShape> LstPointsSelected = new List<HybridShape>();
        //public ObservableCollection<Point> LstPointsOnLine = new ObservableCollection<Point>();
        public List<Point> LstPointsPN = new List<Point>();
        public List<Point> LstOutputWeldPoints = new List<Point>();
        public List<AnyObject> LstWeldBodies = new List<AnyObject>();
        public List<Line> LstAxis = new List<Line>();

        public int SpotWeldIndex;

        public ModelSettingData Config;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="obj"></param>
        /// <returns></returns>
        public Reference CreateRef(AnyObject obj)
        {
            if (obj is Reference refObj)
            {
                return refObj;
            }
            else
            {
                return Part.CreateReferenceFromObject(obj);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="obj"></param>
        public void DeleteObj(AnyObject obj)
        {
            Factory.DeleteObjectForDatum(CreateRef(obj));
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="obj"></param>
        /// <returns></returns>
        public double MeasureLen(AnyObject obj)
        {
            var measure = WBench.GetMeasurable(CreateRef(obj));
            return measure.Length;
        }

        /// <summary>
        /// 
        /// </summary>
        public bool Update()
        {
            try
            {
                Part?.Update();
                return true;
            }
            catch
            {
                return false;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="geo"></param>
        /// <param name="obj"></param>
        /// <returns></returns>
        internal bool AddAndUpdate(HybridBody geo, HybridShape obj)
        {
            geo.AppendHybridShape(obj);
            return UpdateObj(obj);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="obj"></param>
        /// <returns></returns>
        internal bool UpdateObj(AnyObject obj)
        {
            try
            {
                Part.UpdateObject(obj);
                return true;
            }
            catch
            {
                DeleteObj(obj);
                return false;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="shape"></param>
        /// <returns></returns>
        internal bool AddTempAndUpdate(HybridShape shape)
        {
            return AddAndUpdate(GeoTempCons, shape);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="geo"></param>
        /// <returns></returns>
        internal HybridShape GetLastItem(HybridBody geo)
        {
            return geo.HybridShapes.Item(geo.HybridShapes.Count);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="obj"></param>
        internal void SetShow(AnyObject obj, bool isShow = true)
        {
            var sel = SelectionProd;
            sel.Clear();
            sel.Add(obj);
            var vis = sel.VisProperties;
            var cmd = isShow ? CatVisPropertyShow.catVisPropertyShowAttr : CatVisPropertyShow.catVisPropertyNoShowAttr;
            vis.SetShow(cmd);
            sel.Clear();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="obj"></param>
        /// <param name="color"></param>
        internal void SetColor(AnyObject obj, Color color)
        {
            var sel = SelectionProd;
            sel.Clear();
            sel.Add(obj);
            var vis = sel.VisProperties;
            vis.SetRealColor(color.R, color.G, color.B, 1);
            sel.Clear();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        internal bool ClearGeoTemp()
        {
            try
            {
                for (int i = GeoTempCons.HybridShapes.Count; i > 0; i--)
                {
                    DeleteObj(GeoTempCons.HybridShapes.Item(i));
                }

                Update();

                return true;
            }
            catch// (Exception ex)
            {
                return false;
            }
        }

        /// <summary>
        ///     Contructor
        /// </summary>
        public ModelJoinFile()
        {
            LstEdge.CollectionChanged += LstEdge_CollectionChanged;
            LstWeldLine.LstPtChangedEvent += LstPointsOnLine_CollectionChanged;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        /// <exception cref="NotImplementedException"></exception>
        private void LstPointsOnLine_CollectionChanged(object sender, NotifyCollectionChangedEventArgs e)
        {
            NotifyPropertyChanged(nameof(LstPointOnLineCount));
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void LstEdge_CollectionChanged(object sender, NotifyCollectionChangedEventArgs e)
        {
            NotifyPropertyChanged(nameof(IsEdgeSelected));
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="rp"></param>
        /// <returns></returns>
        public HybridBody WorkingOutputGeo(ModelReportData rp)
        {
            switch (rp.PanelsCount)
            {
                case 2:
                    return Geo2T;

                case 3:
                    return Geo3T;

                case 4:
                    return Geo4T;

                default:
                    return null;
            }
        }
    }

    /// <summary>
    /// 
    /// </summary>
    public class PointAndDis
    {
        public AnyObject Point;
        public double Distance;
        public double Thickness;
    }

    /// <summary>
    /// 
    /// </summary>
    public class ModelListWeldLine : List<WeldLine>
    {
        public event NotifyCollectionChangedEventHandler LstPtChangedEvent;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="item"></param>
        public new void Add(WeldLine item)
        {
            base.Add(item);
            item.LstChildPoint.CollectionChanged += LstPtChangedEvent;
        }

        /// <summary>
        /// 
        /// </summary>
        public new void Clear()
        {
            this.ForEach(ln => ln.LstChildPoint.CollectionChanged -= LstPtChangedEvent);
            base.Clear();
        }
    }

    /// <summary>
    /// 
    /// </summary>
    public class WeldLine
    {
        public bool IsWorking = true;
        public HybridShape Line;
        public ObservableCollection<Point> LstChildPoint = new ObservableCollection<Point>();

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        public void DeletePoints(ModelJoinFile jf)
        {
            LstChildPoint.ToList().ForEach(x => jf.DeleteObj(x));
            LstChildPoint.Clear();
        }

        /// <summary>
        /// 
        /// </summary>
        public WeldLine()
        {

        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="_line"></param>
        public WeldLine(HybridShape _line)
        {
            Line = _line;
        }
    }
}
