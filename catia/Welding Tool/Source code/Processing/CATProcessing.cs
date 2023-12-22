using HybridShapeTypeLib;
using INFITF;
using KnowledgewareTypeLib;
using MECMOD;
using NavigatorTypeLib;
using PARTITF;
using ProductStructureTypeLib;
using SPATypeLib;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Welding_Tool.Common;
using Welding_Tool.DataModel;
using Point = HybridShapeTypeLib.Point;
using Thread = System.Threading.Thread;

namespace Welding_Tool.Processing
{
    public static class CPr
    {
        [DllImport("USER32.DLL")]
        private static extern bool SetForegroundWindow(IntPtr hWnd);

        [DllImport("user32.dll")]
        [return: MarshalAs(UnmanagedType.Bool)]
        static extern bool ShowWindow(IntPtr hWnd, ShowWindowEnum flags);

        [DllImport("user32.dll")]
        [return: MarshalAs(UnmanagedType.Bool)]
        static extern bool GetWindowPlacement(IntPtr hWnd, ref Windowplacement lpwndpl);

        private enum ShowWindowEnum
        {
            Hide = 0,
            ShowNormal = 1, ShowMinimized = 2, ShowMaximized = 3,
            Maximize = 3, ShowNormalNoActivate = 4, Show = 5,
            Minimize = 6, ShowMinNoActivate = 7, ShowNoActivate = 8,
            Restore = 9, ShowDefault = 10, ForceMinimized = 11
        };

        private struct Windowplacement
        {
            public int length;
            public int flags;
            public int showCmd;
            public System.Drawing.Point ptMinPosition;
            public System.Drawing.Point ptMaxPosition;
            public System.Drawing.Rectangle rcNormalPosition;
        }

        public static ModelSettingData Config;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="catApp"></param>
        /// <returns></returns>
        public static async Task<(bool, Application catApp)> GetCatiaApp()
        {
            return await Task.Run(() =>
            {
                Application catApp = null;
                try
                {
                    catApp = (Application)Marshal.GetActiveObject("Catia.Application");
                    return (true, catApp);
                }
                catch //(Exception ex)
                {
                    return (false, catApp);
                }
            });
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="title"></param>
        /// <returns></returns>
        public static bool SetForegroundCATIA(Application catia)
        {
            Process[] processes = Process.GetProcessesByName("CNEXT");

            // Verify that Calculator is a running process.
            if (processes.Count() == 0 || processes[0].MainWindowHandle == IntPtr.Zero)
            {
                return false;
            }

            var wdwIntPtr = processes[0].MainWindowHandle;

            // Get the hWnd of the process
            Windowplacement placement = new Windowplacement();
            GetWindowPlacement(wdwIntPtr, ref placement);

            // Check if window is minimized
            if (placement.showCmd == 2)
            {
                //the window is hidden so we restore it
                ShowWindow(wdwIntPtr, ShowWindowEnum.Restore);
            }

            // Make CATIA the foreground application
            SetForegroundWindow(wdwIntPtr);
            catia.RefreshDisplay = true;
            return true;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="catia"></param>
        /// <returns></returns>
        public static async Task<bool?> CheckWorkbenchTop(Application catia, ModelJoinFile jf)
        {
            try
            {
                return await Task.Run(() =>
                {
                    if (catia.GetWorkbenchId() != "Assembly")
                    {
                        try
                        {
                            catia.StartWorkbench("Assembly");
                            //jf.ProductDoc.Activate();
                            return true;
                        }
                        catch
                        {
                            return false;
                        }
                    }
                    return true;
                });
            }
            catch (Exception ex)
            {
                BL.Log(ex);
                return null;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="catia"></param>
        /// <param name="data"></param>
        /// <returns></returns>
        public static async Task<bool?> CreateJFPart(Application catia, ModelJoinFile data)
        {
            try
            {
                bool? res = true;

                if (!(catia.ActiveWindow.Parent is ProductDocument prdDoc))
                {
                    BL.ShowCritMsg("The top node of the Specification tree must be an Assembly!");
                    return false;
                }

                data.ProductDoc = prdDoc;
                data.RootPrd = prdDoc.Product;
                data.RootName = prdDoc.Product.get_PartNumber();

                if (!await CreateJFName(prdDoc, data))
                {
                    if (!await GetExistedData(data))
                    {
                        //
                    }
                    else
                    {
                        BL.ShowInfoMsg("JF Part already existed!");
                        res = null;
                        //if (data.Surface != null)
                        //{
                        //    BL.ShowInfoMsg("Make sure that the existed Input Surface is valid");
                        //}
                    }
                }
                else
                {
                    await Task.Run(() =>
                    {
                        var prds = prdDoc.Product.Products;
                        data.Product = prds.AddNewComponent(nameof(Part), "");

                        data.Product.ActivateDefaultShape();
                        data.Product.set_Name(data.JFPartName);

                        int cnt = 8;
                        while (true)
                        {
                            try
                            {
                                data.Product.set_PartNumber(data.JFPartName);
                                break;
                            }
                            catch
                            {
                                Thread.Sleep(20);
                                cnt--;
                            }
                        }

                        catia.RefreshDisplay = true;

                        data.PartDoc = (PartDocument)data.Product.ReferenceProduct.Parent;
                        data.Part = data.PartDoc.Part;

                        var hbs = data.Part.HybridBodies;

                        data.GeoOutWeldSpot = CreateGeo(hbs, GeoNames.OutputSpot);
                        data.GeoOutWeldAxis = CreateGeo(hbs, GeoNames.OutputAxis);
                        data.GeoOutWeldPoints = CreateGeo(hbs, GeoNames.OutputPoints);
                        data.GeoInput = CreateGeo(hbs, GeoNames.Input);
                        data.GeoConstruction = CreateGeo(hbs, GeoNames.Construct);
                        data.GeoTemp = CreateGeo(hbs, GeoNames.Temp);

                        data.Factory = (HybridShapeFactory)data.Part.HybridShapeFactory;

                        data.Update();
                    });

                    BL.ShowInfoMsg("Create JF Part successfully!");
                    //BL.ShowInfoMsg("Please copy the *Final Surface to 'Input Geometry' of JF Part manually!");
                }

                // Create new folder
                await CreateGeoTempCons(data);

                await Task.Run(() =>
                {
                    if (Config.General.IsRearrange)
                    {
                        data.Geo2T = CheckExistOrCreateNew(data.GeoOutWeldSpot, GeoNames.Spot2T);
                        data.Geo3T = CheckExistOrCreateNew(data.GeoOutWeldSpot, GeoNames.Spot3T);
                        data.Geo4T = CheckExistOrCreateNew(data.GeoOutWeldSpot, GeoNames.Spot4T);
                    }

                    data.WBench = (SPAWorkbench)data.PartDoc.GetWorkbench("SPAWorkbench");
                    data.SelectionPart = data.PartDoc.Selection;
                    data.SelectionProd = data.ProductDoc.Selection;
                    data.Product.set_Nomenclature(data.RootPrd.get_Nomenclature());
                });

                await CreateStrProperty(data, JFProperties.PropStartupModelName, JFProperties.PropStartupModelValue);

                return res;
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
        /// <param name="jf"></param>
        public static async Task CreateGeoTempCons(ModelJoinFile jf)
        {
            jf.GeoTempPtCons = await CreateGeoTempPtCons(jf, "Temp Points Data");
            jf.GeoTempCons = await CreateGeoTempPtCons(jf, "Temp Data");
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        public static async Task<HybridBody> CreateGeoTempPtCons(ModelJoinFile jf, string name)
        {
            try
            {
                return await Task.Run(() =>
                {
                    int consIdx = 1;
                    while (true)
                    {
                        bool isFound = false;
                        for (int i = 1; i <= jf.GeoConstruction.HybridBodies.Count; i++)
                        {
                            if (jf.GeoConstruction.HybridBodies.Item(i).get_Name() == $"{name} {consIdx}")
                            {
                                isFound = true;
                                break;
                            }
                        }
                        if (isFound)
                        {
                            consIdx++;
                        }
                        else
                        {
                            break;
                        }
                    }
                    return CreateGeo(jf.GeoConstruction.HybridBodies, $"{name} {consIdx}");
                });
            }
            catch //(Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="prds"></param>
        /// <returns></returns>
        private static async Task<bool> CreateJFName(ProductDocument prdDoc, ModelJoinFile data)
        {
            data.RootName = BL.GetSubStrFrLastDash(data.RootName, true);
            data.JFPartName = data.RootName + "_JF";

            return await Task.Run(() =>
            {
                var prds = prdDoc.Product.Products;
                for (int i = 1; i <= prds.Count; i++)
                {
                    if (prds.Item(i).get_Name().IndexOf(data.JFPartName) == 0)
                    {
                        data.Product = prds.Item(i);
                        data.JFPartName = prds.Item(i).get_Name();
                        return false;
                    }
                }

                return true;
            });
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="data"></param>
        /// <returns></returns>
        private static async Task<bool> GetExistedData(ModelJoinFile data)
        {
            return await Task.Run(() =>
            {
                data.PartDoc = (PartDocument)data.Product.ReferenceProduct.Parent;
                data.Part = data.PartDoc.Part;
                data.Factory = (HybridShapeFactory)data.Part.HybridShapeFactory;

                var hbs = data.Part.HybridBodies;

                for (int i = hbs.Count; i > 0; i--)
                {
                    switch (hbs.Item(i).get_Name())
                    {
                        case GeoNames.Input:
                            data.GeoInput = hbs.Item(i);
                            break;

                        case GeoNames.Temp:
                            data.GeoTempCons = hbs.Item(i);
                            break;

                        case GeoNames.Construct:
                            data.GeoConstruction = hbs.Item(i);
                            break;

                        case GeoNames.OutputAxis:
                            data.GeoOutWeldAxis = hbs.Item(i);
                            break;

                        case GeoNames.OutputPoints:
                            data.GeoOutWeldPoints = hbs.Item(i);
                            break;

                        case GeoNames.OutputSpot:
                            data.GeoOutWeldSpot = hbs.Item(i);
                            break;
                    }
                }

                if (data.GeoInput == null)
                {
                    data.GeoInput = CreateGeo(hbs, GeoNames.Input);
                }
                //else if (data.GeoInput.HybridShapes.Count > 0)
                //{
                //    data.Surface = data.GeoInput.HybridShapes.Item(1);
                //}

                if (data.GeoTempCons == null)
                {
                    data.GeoTempCons = CreateGeo(hbs, GeoNames.Temp);
                }
                if (data.GeoConstruction == null)
                {
                    data.GeoConstruction = CreateGeo(hbs, GeoNames.Construct);
                }
                if (data.GeoOutWeldPoints == null)
                {
                    data.GeoOutWeldPoints = CreateGeo(hbs, GeoNames.OutputPoints);
                }
                if (data.GeoOutWeldAxis == null)
                {
                    data.GeoOutWeldAxis = CreateGeo(hbs, GeoNames.OutputAxis);
                }
                if (data.GeoOutWeldSpot == null)
                {
                    data.GeoOutWeldSpot = CreateGeo(hbs, GeoNames.OutputSpot);
                }

                return data.Update();
            });
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="hbs"></param>
        /// <param name="name"></param>
        /// <returns></returns>
        private static HybridBody CreateGeo(HybridBodies hbs, string name)
        {
            var hb = hbs.Add();
            hb.set_Name(name);
            return hb;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="hbs"></param>
        /// <param name="childName"></param>
        /// <returns></returns>
        public static HybridBody CheckExistOrCreateNew(HybridBody hbs, string childName)
        {
            foreach (HybridBody geo in hbs.HybridBodies)
            {
                if (geo.get_Name() == childName)
                {
                    return geo;
                }
            }
            return CreateGeo(hbs.HybridBodies, childName);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="sel"></param>
        /// <param name="msg"></param>
        /// <returns></returns>
        public static async Task<(bool?, T obj)> SelectObj<T>(Selection sel, string msg) where T : AnyObject
        {
            try
            {
                var fil = new object[1] { typeof(T).Name };
                string status = "";
                await Task.Run(() =>
                {
                    sel.Clear();
                    status = sel.SelectElement2(fil, msg, false);
                });

                if (status == "Cancel")
                {
                    return (null, default);
                }

                if (status != "Normal" || sel.Count < 1 || sel.Item(1) == null)
                {
                    return (false, default);
                }

                return (true, (T)sel.Item(1).Value);
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return (false, default);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="sel"></param>
        /// <param name="msg"></param>
        /// <returns></returns>
        public static async Task<(bool?, IEnumerable<T>)> SelectObj3<T>(Selection sel, string msg) where T : AnyObject
        {
            try
            {
                var fil = new object[1] { typeof(T).Name };
                string status = "";
                await Task.Run(() =>
                {
                    sel.Clear();
                    status = sel.SelectElement3(fil, msg, false, CATMultiSelectionMode.CATMultiSelTriggWhenUserValidatesSelection, true);
                });

                if (status == "Cancel")
                {
                    return (null, default);
                }

                var selItems = new List<T>();
                for (int i = 1; i <= sel.Count; i++)
                {
                    selItems.Add((T)sel.Item(i).Value);
                }

                if (status != "Normal" || selItems.Count < 1 || selItems.Any(x => x == null))
                {
                    return (false, default);
                }

                return (true, selItems);
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return (false, default);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="sel"></param>
        /// <param name="msg"></param>
        /// <returns></returns>
        public static async Task<(bool?, bool, T1 obj)> SelectAndCopy<T1, T2>(Selection sel, string msg, AnyObject targ, Product prd) where T1 : AnyObject where T2 : AnyObject
        {
            try
            {
                var fil = new object[2] { typeof(T2).Name, "" };
                if (typeof(T1).Name != typeof(T2).Name)
                {
                    fil[1] = typeof(T1).Name;
                }
                else
                {
                    fil = fil.Take(1).ToArray();
                }
                string status = "";
                await Task.Run(() =>
                {
                    sel.Clear();
                    status = sel.SelectElement2(fil, msg, false);
                });

                if (status == "Cancel")
                {
                    return (null, false, default);
                }

                if (status != "Normal" || sel.Count < 1 || sel.Item(1) == null)
                {
                    return (false, false, default);
                }

                var isCopy = false;
                var isOK = await IsSelectedBelongTo(prd, sel.Item(1));
                if (isOK == false)
                {
                    sel.Copy();
                    sel.Clear();
                    sel.Add(targ);
                    sel.PasteSpecial(PastOpt.AsResult);
                    isCopy = true;
                }
                else if (isOK == null)
                {
                    return (false, false, default);
                }

                return (true, isCopy, (T1)sel.Item(1).Value);
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return (false, false, default);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="sel"></param>
        /// <param name="msg"></param>
        /// <returns></returns>
        public static async Task<(bool?, AnyObject obj)> SelectObjLeaf<T>(Selection sel, string msg) where T : AnyObject
        {
            try
            {
                var fil = new object[1] { typeof(T).Name };
                string status = "";
                await Task.Run(() =>
                {
                    sel.Clear();
                    status = sel.SelectElement2(fil, msg, false);
                });

                if (status == "Cancel")
                {
                    return (null, default);
                }

                if (status != "Normal" || sel.Count < 1 || sel.Item2(1) == null)
                {
                    return (false, default);
                }

                return (true, sel.Item2(1).LeafProduct);
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return (false, default);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sel"></param>
        /// <param name="obj"></param>
        /// <param name="tar"></param>
        public static async Task<bool> PasteAsResultAsync(Selection sel, AnyObject obj, AnyObject tar)
        {
            return await Task.Run(() =>
            {
                return PasteAsResult(sel, obj, tar);
            });
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sel"></param>
        /// <param name="obj"></param>
        /// <param name="tar"></param>
        /// <returns></returns>
        public static bool PasteAsResult(Selection sel, AnyObject obj, AnyObject tar)
        {
            sel.Clear();
            sel.Add(obj);
            try
            {
                sel.Copy();
            }
            catch
            {
                return false;
            }

            sel.Clear();
            sel.Add(tar);
            sel.PasteSpecial(PastOpt.AsResult);
            //sel.Clear();

            return true;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        public static async Task<bool> CreateWeldLine(ModelJoinFile jf, bool isCreMulti, bool isMultiSelEd)
        {
            try
            {
                return await Task.Run(async () =>
                {
                    if (!isCreMulti)
                    {
                        if (Config.General.IsClearTemp)
                        {
                            jf.LstWeldLine.ForEach(x =>
                            {
                                x.DeletePoints(jf);
                                jf.DeleteObj(x.Line);
                            });
                        }

                        jf.LstWeldLine.Clear();
                    }
                    jf.LstWeldLine.ForEach(x => x.IsWorking = false);

                    if (!isMultiSelEd)
                    {
                        if (!await CreateWeldLineSingle(jf, jf.LstEdge.Last()))
                        {
                            return false;
                        }
                    }
                    else
                    {
                        foreach (var ed in jf.LstEdge)
                        {
                            if (!await CreateWeldLineSingle(jf, ed))
                            {
                                return false;
                            }
                        }
                    }

                    return true;
                });
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
        /// <param name="jf"></param>
        /// <param name="ed"></param>
        /// <returns></returns>
        private static async Task<bool> CreateWeldLineSingle(ModelJoinFile jf, Edge ed)
        {
            return await Task.Run(() =>
            {
                var refLine = jf.CreateRef(ed);
                var refSup = jf.CreateRef(jf.Surface);

                Thread.Sleep(100);

                var line = jf.Factory.AddNewCurvePar(refLine, refSup, jf.Offset.Value, false, false);

                Thread.Sleep(500);

                if (jf.AddAndUpdate(jf.GeoTempPtCons, line))
                {
                    jf.LstWeldLine.Add(new WeldLine(line));
                    jf.Update();
                    return true;
                }
                else
                {
                    return false;
                }
            });
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        public static async Task<bool?> CreateWeldPoints(ModelJoinFile jf, bool isCreMulti, bool isSelMultiEd)
        {
            try
            {
                if (!await Task.Run(() =>
                {
                    if (!isCreMulti && Config.General.IsClearTemp)
                    {
                        jf.LstWeldLine.ForEach(ln => ln.DeletePoints(jf));
                    }
                    return jf.Update();
                }))
                {
                    var msg = "There are error on Specification Tree!";
                    msg += "Please manually update to check and fix, then try again!";
                    BL.ShowCritMsg(msg);
                    return false;
                }

                if (!isSelMultiEd)
                {
                    return await CreateWeldPointSingle(jf, jf.LstWeldLine.Last());
                }
                else
                {
                    var res = new List<bool?>();
                    bool? nullBo = null;
                    foreach (var ln in jf.LstWeldLine.Where(x => x.IsWorking))
                    {
                        res.Add(await CreateWeldPointSingle(jf, ln));
                    }
                    return res.All(x => x == true) ? true :
                           res.All(x => x == false) ? false : nullBo;
                }
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
        /// <param name="jf"></param>
        /// <param name="ln"></param>
        /// <returns></returns>
        private static async Task<bool?> CreateWeldPointSingle(ModelJoinFile jf, WeldLine ln)
        {
            var len = await Task.Run(() => { return jf.MeasureLen(ln.Line); });
            if (jf.IsOption1)
            {
                return await CreateWeldPtOpt1(jf, ln, len);
            }
            else if (jf.IsOption2)
            {
                return await CreateWeldPtOpt2(jf, ln, len);
            }
            return null;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        private static async Task<bool?> CreateWeldPtOpt1(ModelJoinFile jf, WeldLine ln, double len)
        {
            try
            {
                var space = len / jf.PointsCount.Value;
                if (jf.PointsCount > 1 && space <= Config.SpotWeld.Spacing)
                {
                    var msg = $"The spacing between welding points is too small! ({space:F2}mm)\n";
                    msg += $"It needs to be larger than '{Config.SpotWeld.Spacing}mm'";
                    BL.ShowCritMsg(msg);
                    return null;
                }

                return await Task.Run(() =>
                {
                    var refWLine = jf.CreateRef(ln.Line);

                    for (int i = 0; i < jf.PointsCount; i++)
                    {
                        var dis = space * (0.5 + i);
                        var pt = jf.Factory.AddNewPointOnCurveFromDistance(refWLine, dis, false);
                        if (jf.AddAndUpdate(jf.GeoTempPtCons, pt))
                        {
                            ln.LstChildPoint.Add(pt);
                        }
                    }

                    return jf.Update();
                });
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
        /// <param name="jf"></param>
        /// <returns></returns>
        public static async Task<bool?> CreateWeldPtOpt2_old(ModelJoinFile jf, WeldLine ln, double len)
        {
            try
            {
                if (jf.Spacing <= Config.SpotWeld.Spacing)
                {
                    var msg = $"The spacing between welding points is too small! ({jf.Spacing:F2}mm)\n";
                    msg += $"It needs to be larger than '{Config.SpotWeld.Spacing}mm'";
                    BL.ShowCritMsg(msg);
                    return null;
                }

                Reference refWLine = null, refStPt = null;
                var orient = false;
                if (!await Task.Run(() =>
                {
                    refWLine = jf.CreateRef(ln.Line);
                    refStPt = jf.CreateRef(jf.RefStartObj);

                    var aDis = len / 10;
                    var ptCen = jf.Factory.AddNewPointOnCurveFromPercent(refWLine, 0.5, false);
                    var pt1 = jf.Factory.AddNewPointOnCurveWithReferenceFromDistance(refWLine, refStPt, aDis, orient);
                    var pt2 = jf.Factory.AddNewPointOnCurveWithReferenceFromDistance(refWLine, refStPt, aDis * 2, orient);

                    jf.AddTempAndUpdate(ptCen);
                    jf.AddTempAndUpdate(pt1);
                    jf.AddTempAndUpdate(pt2);
                    Thread.Sleep(200);

                    var refCen = jf.CreateRef(ptCen);
                    var line1 = jf.Factory.AddNewLinePtPt(refCen, jf.CreateRef(pt1));
                    var line2 = jf.Factory.AddNewLinePtPt(refCen, jf.CreateRef(pt2));

                    Thread.Sleep(200);
                    jf.AddTempAndUpdate(line1);
                    jf.AddTempAndUpdate(line2);

                    if (jf.MeasureLen(line1) < jf.MeasureLen(line2))
                    {
                        orient = !orient;
                    }

                    jf.DeleteObj(ptCen);
                    jf.DeleteObj(pt1);
                    jf.DeleteObj(pt2);
                    jf.DeleteObj(line1);
                    jf.DeleteObj(line2);

                    return jf.Update();
                }))
                {
                    var msg = "There are error on Specification Tree!";
                    msg += "Please manually update to check and fix, then try again!";
                    BL.ShowCritMsg(msg);
                    return false;
                }

                double dis = 0;
                int cnt = 0;
                while (true)
                {
                    dis = jf.DistFromSt.Value + cnt * jf.Spacing.Value;
                    if (dis > len)
                    {
                        break;
                    }

                    var pt = jf.Factory.AddNewPointOnCurveWithReferenceFromDistance(refWLine, refStPt, dis, orient);
                    if (jf.AddAndUpdate(jf.GeoTempPtCons, pt))
                    {
                        ln.LstChildPoint.Add(pt);
                    }

                    cnt++;
                }

                return jf.Update();
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
        /// <param name="jf"></param>
        /// <returns></returns>
        public static async Task<bool?> CreateWeldPtOpt2(ModelJoinFile jf, WeldLine ln, double len)
        {
            try
            {
                if (jf.Spacing <= Config.SpotWeld.Spacing)
                {
                    var msg = $"The spacing between welding points is too small! ({jf.Spacing:F2}mm)\n";
                    msg += $"It needs to be larger than '{Config.SpotWeld.Spacing}mm'";
                    BL.ShowCritMsg(msg);
                    return null;
                }

                if (jf.DistFromSt > len)
                {
                    BL.ShowWarnMsg("The distance from 1st Point to the Start was larger than " +
                                   "the length of the weld line, no point was created!");
                    return null;
                }

                bool orient = false;
                Reference refWLine = null;
                if (!await Task.Run(() =>
                {
                    refWLine = jf.CreateRef(ln.Line);
                    var pt1 = jf.Factory.AddNewPointOnCurveFromDistance(refWLine, 0, orient);
                    var pt2 = jf.Factory.AddNewPointOnCurveFromDistance(refWLine, len, orient);

                    jf.AddTempAndUpdate(pt1);
                    jf.AddTempAndUpdate(pt2);

                    var refSt = jf.CreateRef(jf.RefStartObj);

                    GetMinDis(jf, pt1, refSt, out var dis1);
                    GetMinDis(jf, pt2, refSt, out var dis2);

                    if (dis1 > dis2)
                    {
                        orient = !orient;
                    }

                    jf.DeleteObj(pt1);
                    jf.DeleteObj(pt2);
                    return jf.Update();
                }))
                {
                    return false;
                }

                return await Task.Run(() =>
                {
                    double dis = 0;
                    int cnt = 0;
                    while (true)
                    {
                        dis = jf.DistFromSt.Value + cnt * jf.Spacing.Value;
                        if (dis > len)
                        {
                            break;
                        }

                        var pt = jf.Factory.AddNewPointOnCurveFromDistance(refWLine, dis, orient);
                        if (jf.AddAndUpdate(jf.GeoConstruction, pt))
                        {
                            ln.LstChildPoint.Add(pt);
                        }

                        cnt++;
                    }

                    return jf.Update();
                });
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
        /// <param name="jf"></param>
        /// <param name="pt"></param>
        /// <param name="refSt"></param>
        private static void GetMinDis(ModelJoinFile jf, Point pt, Reference refSt, out double dis)
        {
            var mea1 = jf.WBench.GetMeasurable(jf.CreateRef(pt));
            try
            {
                dis = mea1.GetMinimumDistance(refSt);
            }
            catch
            {
                try
                {
                    var arr = jf.Temp as Array;
                    var tempPt = jf.Factory.AddNewPointCoord((double)arr.GetValue(0),
                                                             (double)arr.GetValue(1),
                                                             (double)arr.GetValue(2));
                    jf.AddTempAndUpdate(tempPt);
                    dis = mea1.GetMinimumDistance(jf.CreateRef(tempPt));
                    jf.DeleteObj(tempPt);
                }
                catch
                {
                    dis = 0;
                }
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        public static async Task<bool> CreateWeldBody(ModelJoinFile jf, List<ModelReportData> rpData, SpotWeldMode mode, IProgress<ModelReport> pg)
        {
            try
            {
                // Create Spot Weld body
                IEnumerable<HybridShape> lstPts = null;
                switch (mode)
                {
                    case SpotWeldMode.OnSurface:
                        lstPts = jf.LstPointsOnLine;
                        break;

                    case SpotWeldMode.ByPoints:
                        lstPts = jf.LstPointsSelected;
                        break;
                }

                var rp = new ModelReport
                {
                    PgValue = lstPts.Count() > 5 ? 1 / lstPts.Count() : 0.1,
                    Message = "Clearing old data ..."
                };
                pg.Report(rp);
                rpData.Clear();

                if (!await Task.Run(() =>
                {
                    try
                    {
                        jf.LstWeldBodies.ForEach(x => jf.DeleteObj(x));
                        jf.LstPointsPN.ForEach(x => jf.DeleteObj(x));
                        jf.LstAxis.ForEach(x => jf.DeleteObj(x));
                        jf.LstOutputWeldPoints.ForEach(x => jf.DeleteObj(x));

                        jf.LstWeldBodies.Clear();
                        jf.LstPointsPN.Clear();
                        jf.LstAxis.Clear();
                        jf.LstOutputWeldPoints.Clear();
                        jf.Update();
                        Thread.Sleep(500);
                        return true;
                    }
                    catch// (Exception)
                    {
                        return false;
                    }
                }))
                {
                    BL.ShowCritMsg("Cannot delete pre-created welding bodies!");
                    return false;
                }

                jf.SpotWeldIndex = jf.GeoOutWeldSpot.HybridShapes.Count + 1;

                var step = (0.96 - rp.PgValue) / lstPts.Count();
                var cnt = 0;

                foreach (var pt in lstPts)
                {
                    rp.PgValue += step;
                    rp.Message = $"Creating spot weld bodies [ {cnt++} / {lstPts.Count()} ] ...";
                    pg.Report(rp);

                    var aRp = new ModelReportData();
                    if (await Task.Run(() => CreateBodySingle(jf, pt, aRp)) && mode == SpotWeldMode.ByPoints)
                    {
                        pt.set_Name("Constructed_" + pt.get_Name());
                    }
                    rpData.Add(aRp);
                }

                rp.PgValue = 1;
                rp.Message = "Finishing up ...";
                pg.Report(rp);

                await Task.Delay(200);
                await Task.Run(() =>
                {
                    jf.SetShow(jf.Surface, false);
                    jf.SetShow(jf.GeoTempCons, false);
                    jf.SetShow(jf.GeoConstruction, false);
                    jf.SetShow(jf.GeoTempPtCons, false);
                    jf.SetShow(jf.GeoOutWeldAxis);
                    jf.SetShow(jf.GeoOutWeldPoints);
                    jf.SetShow(jf.GeoOutWeldSpot);
                    jf.Update();
                });

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
        /// <param name="jf"></param>
        /// <param name="bd"></param>
        /// <returns></returns>
        private static bool CopyBody(ModelJoinFile jf, Body bdIn, ref Body bd)
        {
            if (bd == null)
            {
                if (!PasteAsResult(jf.SelectionProd, bdIn, jf.Part))
                {
                    return false;
                }

                bd = jf.Part.Bodies.Item(jf.Part.Bodies.Count);

                if (!jf.UpdateObj(bd))
                {
                    return false;
                }
            }

            return true;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <param name="point"></param>
        private static bool CreateBodySingle(ModelJoinFile jf, HybridShape point, ModelReportData rp)
        {
            try
            {
                var pln = CreateTangentPln(jf, point, jf.Surface);
                jf.AddTempAndUpdate(pln);

                var line = CreateLinePtDir(jf, point, pln);
                jf.AddTempAndUpdate(line);

                // Check clash
                var (isOK, points) = CheckClash(jf, line, point, rp);

                rp.PanelsCount = points.Count;
                if (rp.CheckSkip(Config.SpotWeld.MaxAllowanceGap))
                {
                    BL.Log($"Skip a spot weld '{rp.WeldedParts.Count}T'");
                    return false;
                }

                var refLine = jf.CreateRef(line);
                var ptP = jf.Factory.AddNewPointOnCurveFromPercent(refLine, 0, false);
                var ptN = jf.Factory.AddNewPointOnCurveFromPercent(refLine, 1, false);
                jf.AddTempAndUpdate(ptP);
                jf.AddTempAndUpdate(ptN);

                //var refBd1 = jf.CreateRef(jf.CopiedBody1);
                //var itsc1 = jf.Factory.AddNewIntersection(refLine, refBd1);
                //jf.AddTempAndUpdate(itsc1);

                //var refBd2 = jf.CreateRef(jf.CopiedBody2);
                //var itsc2 = jf.Factory.AddNewIntersection(refLine, refBd2);
                //jf.AddTempAndUpdate(itsc2);

                Thread.Sleep(20);
                var lstPtDisP = new List<PointAndDis>();
                var lstPtDisN = new List<PointAndDis>();

                points.ForEach(x =>
                {
                    MeasureDis(jf, x, ptP, lstPtDisP);
                    MeasureDis(jf, x, ptN, lstPtDisN);
                });

                if (lstPtDisP.Count > 0 && lstPtDisN.Count > 0)
                {
                    var FinalP = lstPtDisP.OrderBy(x => x.Distance).FirstOrDefault();
                    var FinalN = lstPtDisN.OrderBy(x => x.Distance).FirstOrDefault();

                    if (FinalP != null && FinalN != null)
                    {
                        var ptFnP = FinalP.Point;
                        var ptFnN = FinalN.Point;

                        var shiftP = jf.Factory.AddNewPointOnCurveWithReferenceFromDistance(
                                jf.CreateRef(line), jf.CreateRef(ptFnP), -FinalP.Thickness / 2, false);
                        var shiftN = jf.Factory.AddNewPointOnCurveWithReferenceFromDistance(
                                jf.CreateRef(line), jf.CreateRef(ptFnN), FinalN.Thickness / 2, false);

                        jf.AddTempAndUpdate(shiftP);
                        jf.AddTempAndUpdate(shiftN);

                        var name = $"{jf.SpotWeldIndex}_{jf.RootName}";
                        rp.SpotWeldName = name;

                        var newPtP = CreatePointNoLink(jf, shiftP);
                        var newPtN = CreatePointNoLink(jf, shiftN);
                        jf.GeoTempPtCons.AppendHybridShape(newPtP);
                        jf.GeoTempPtCons.AppendHybridShape(newPtN);
                        jf.LstPointsPN.Add(newPtP);
                        jf.LstPointsPN.Add(newPtN);

                        var weldAxis = jf.Factory.AddNewLinePtPt(jf.CreateRef(newPtP), jf.CreateRef(newPtN));
                        jf.GeoOutWeldAxis.AppendHybridShape(weldAxis);
                        weldAxis.set_Name(name);
                        jf.LstAxis.Add(weldAxis);

                        jf.UpdateObj(weldAxis);
                        rp.JoinThickness = jf.MeasureLen(weldAxis);
                        rp.JoinDiameter = Config.SpotWeld.Radius;      // Tata set wrong name
                        rp.GetWeldAxis(weldAxis);

                        if (rp.CheckSkip(Config.SpotWeld.MaxAllowanceGap))
                        {
                            BL.Log($"Skip a spot weld '{rp.WeldedParts.Count}T' has blank space '{rp.BlankSpace}'");
                            jf.SetShow(newPtP, false);
                            jf.SetShow(newPtN, false);
                            jf.SetShow(weldAxis, false);
                            return false;
                        }

                        var weldPt = jf.Factory.AddNewPointOnCurveFromPercent(jf.CreateRef(weldAxis), 0.5, false);
                        jf.GeoOutWeldPoints.AppendHybridShape(weldPt);
                        weldPt.set_Name(name);
                        jf.LstOutputWeldPoints.Add(weldPt);

                        // Save coordinage
                        jf.UpdateObj(weldPt);
                        rp.Coordinate = weldPt;
                        rp.CalCoord();

                        HybridShape fillP, fillN;
                        if (Config.SpotWeld.Shape == WeldingShape.Cylinder)
                        {
                            fillP = CreateBodyCylinder(jf, shiftP, pln, -1);
                            fillN = CreateBodyCylinder(jf, shiftN, pln, 1);
                        }
                        else
                        {
                            fillP = CreateBodySphere(jf, shiftP);
                            fillN = CreateBodySphere(jf, shiftN);
                        }

                        var join = jf.Factory.AddNewJoin(jf.CreateRef(fillP), jf.CreateRef(fillN));
                        join.SetConnex(false);

                        var newBd = SaveWeldFeatureToGeo(jf, rp, join, name);
                        jf.Update();
                        Thread.Sleep(20);

                        // Set color depends on panel count
                        switch (rp.PanelsCount)
                        {
                            case 2:
                                jf.SetColor(newBd, Color.Lime);
                                break;

                            case 3:
                                jf.SetColor(newBd, Color.Blue);
                                break;

                            case 4:
                                jf.SetColor(newBd, Color.Fuchsia);
                                break;
                        }
                        jf.LstWeldBodies.Add(newBd);
                        jf.SpotWeldIndex++;

                        return true;
                    }
                }

                return false;
            }
            finally
            {
                if (Config.General.IsClearTemp)
                {
                    jf.ClearGeoTemp();
                }
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <param name="weldFeature"></param>
        /// <param name="name"></param>
        /// <returns></returns>
        private static HybridShape SaveWeldFeatureToGeo(ModelJoinFile jf, ModelReportData rp,
                                                        HybridShapeAssemble weldFeature, string name)
        {
            HybridBody containGeo;
            if (Config.General.IsRearrange)
            {
                var pt1Name = BL.GetSubStrFrLastDash(rp.WeldedParts.First().Name);
                var pt2Name = BL.GetSubStrFrLastDash(rp.WeldedParts.Last().Name);
                var containGeoName = $"{pt1Name}_{pt2Name}";
                containGeo = CheckExistOrCreateNew(jf.WorkingOutputGeo(rp), containGeoName);
            }
            else
            {
                containGeo = jf.GeoOutWeldSpot;
            }

            if (Config.General.IsClearTemp)
            {
                jf.AddTempAndUpdate(weldFeature);
                PasteAsResult(jf.SelectionProd, weldFeature, containGeo);
                var newBd = jf.GetLastItem(containGeo);
                newBd.set_Name(name);
                return newBd;
            }
            else
            {
                jf.AddAndUpdate(containGeo, weldFeature);
                weldFeature.set_Name(name);
                return weldFeature;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <param name="pt"></param>
        /// <param name="surface"></param>
        /// <returns></returns>
        private static Plane CreateTangentPln(ModelJoinFile jf, HybridShape pt, HybridShape surface)
        {
            var refPt = jf.CreateRef(pt);
            var refSur = jf.CreateRef(surface);
            return jf.Factory.AddNewPlaneTangent(refSur, refPt);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <param name="pt"></param>
        /// <param name="plane"></param>
        /// <returns></returns>
        private static Line CreateLinePtDir(ModelJoinFile jf, HybridShape pt, Plane plane)
        {
            var refPt = jf.CreateRef(pt);
            var refPln = jf.CreateRef(plane);
            var dir = jf.Factory.AddNewDirection(refPln);
            return jf.Factory.AddNewLinePtDir(refPt, dir, 20, -20, false);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <param name="its"></param>
        /// <param name="pt"></param>
        /// <param name="lstPD"></param>
        private static bool MeasureDisNear(ModelJoinFile jf, HybridShapeIntersection its, Point pt, List<PointAndDis> lstPD)
        {
            HybridShapeNear nearPt = null;
            HybridShapeLinePtPt lineNear = null;
            try
            {
                nearPt = jf.Factory.AddNewNear(jf.CreateRef(its), jf.CreateRef(pt));
                jf.AddTempAndUpdate(nearPt);
                lineNear = jf.Factory.AddNewLinePtPt(jf.CreateRef(nearPt), jf.CreateRef(pt));
                jf.AddTempAndUpdate(lineNear);
                var dis = jf.MeasureLen(lineNear);
                lstPD.Add(new PointAndDis { Point = nearPt, Distance = dis });
                return true;
            }
            catch
            {
                if (nearPt != null) { try { jf.DeleteObj(nearPt); } catch { } }
                if (lineNear != null) { try { jf.DeleteObj(lineNear); } catch { } }
                return false;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <param name="its"></param>
        /// <param name="pt"></param>
        /// <param name="lstPD"></param>
        private static bool MeasureDis(ModelJoinFile jf, (Point, double) pt, Point pt0, List<PointAndDis> lstPD)
        {
            HybridShapeLinePtPt line = null;
            try
            {
                line = jf.Factory.AddNewLinePtPt(jf.CreateRef(pt.Item1), jf.CreateRef(pt0));
                jf.AddTempAndUpdate(line);
                var dis = jf.MeasureLen(line);
                lstPD.Add(new PointAndDis { Point = pt.Item1, Distance = dis, Thickness = pt.Item2 });
                return true;
            }
            catch
            {
                if (line != null) { try { jf.DeleteObj(line); } catch { } }
                return false;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <param name="shiftP"></param>
        /// <param name="pln"></param>
        /// <returns></returns>
        private static HybridShapeExtrude CreateBodyCylinder(ModelJoinFile jf, Point shiftP, Plane pln, int pole)
        {
            var cirP = jf.Factory.AddNewCircleCtrRad(jf.CreateRef(shiftP), jf.CreateRef(pln), false, Config.SpotWeld.Radius);
            jf.AddTempAndUpdate(cirP);
            var fillP = FillAndExtrude(jf, cirP, pole * Constant.WeldBodyHeight);
            jf.AddTempAndUpdate(fillP);
            return fillP;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <param name="shiftP"></param>
        /// <param name="pln"></param>
        /// <returns></returns>
        private static HybridShapeSphere CreateBodySphere(ModelJoinFile jf, Point shiftP)
        {
            var sph = jf.Factory.AddNewSphere(jf.CreateRef(shiftP), null, Config.SpotWeld.Radius, -90, 90, -180, 180);
            jf.AddTempAndUpdate(sph);
            return sph;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <param name="circle"></param>
        /// <returns></returns>
        private static HybridShapeExtrude FillAndExtrude(ModelJoinFile jf, HybridShapeCircle circle, double extrdLen, double fin = 0)
        {
            var fill = jf.Factory.AddNewFill();
            fill.AddBound(jf.CreateRef(circle));
            fill.Continuity = 1;
            fill.Detection = 2;
            fill.AdvancedTolerantMode = 2;
            jf.AddTempAndUpdate(fill);
            var refFill = jf.CreateRef(fill);
            var dirFill = jf.Factory.AddNewDirection(refFill);
            return jf.Factory.AddNewExtrude(refFill, extrdLen, fin, dirFill);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <param name="origin"></param>
        /// <returns></returns>
        public static Point CreatePointNoLink(ModelJoinFile jf, AnyObject origin)
        {
            var refPt = jf.CreateRef(origin);
            return jf.Factory.AddNewPointDatum(refPt);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        public static async Task<bool> DeleteDataWeldLine(ModelJoinFile jf, bool isDelete)
        {
            return await Task.Run(() =>
            {
                try
                {
                    if (isDelete && Config.General.IsClearTemp)
                    {
                        jf.LstWeldLine.ForEach(x =>
                        {
                            x.DeletePoints(jf);
                            jf.DeleteObj(x.Line);
                        });
                    }

                    jf.LstWeldLine.Clear();
                    //jf.RefStartObj = null;

                    jf.Update();
                    return true;
                }
                catch// (Exception)
                {
                    return false;
                }
            });
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        public static async Task<bool> DeleteDataWeldBody(ModelJoinFile jf, bool isDelete)
        {
            return await Task.Run(() =>
            {
                try
                {
                    if (isDelete)
                    {
                        jf.LstWeldBodies.ForEach(x => jf.DeleteObj(x));
                        jf.LstPointsPN.ForEach(x => jf.DeleteObj(x));
                        jf.LstAxis.ForEach(x => jf.DeleteObj(x));
                        jf.LstOutputWeldPoints.ForEach(x => jf.DeleteObj(x));
                    }

                    jf.LstWeldBodies.Clear();
                    jf.LstPointsPN.Clear();
                    jf.LstAxis.Clear();
                    jf.LstOutputWeldPoints.Clear();

                    jf.Update();
                    return true;
                }
                catch// (Exception)
                {
                    return false;
                }
            });
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="prd"></param>
        /// <param name="selElm"></param>
        /// <returns></returns>
        public static async Task<bool?> IsSelectedBelongTo(Product prd, SelectedElement selElm)
        {
            try
            {
                return await Task.Run(() =>
                {
                    return selElm.LeafProduct is Product leafPrd &&
                           leafPrd.get_PartNumber() == prd.get_PartNumber();
                });
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return null;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        public static (bool, List<(Point Point, double Thickness)>) CheckClash(ModelJoinFile jf, Line ln, HybridShape pt, ModelReportData rp)
        {
            var pts = new List<(Point Point, double Thickness)>();
            HybridShapeExtrude clBody = null;
            try
            {
                // Create the body to clash
                var profile = jf.Factory.AddNewCircleCenterAxis(jf.CreateRef(ln), jf.CreateRef(pt), 0.1, false);
                jf.AddTempAndUpdate(profile);
                clBody = FillAndExtrude(jf, profile, Config.SpotWeld.DetectRange, Config.SpotWeld.DetectRange);
                jf.AddAndUpdate(jf.GeoConstruction, clBody);
                HideAllElm(jf);
                jf.SetShow(jf.GeoConstruction);

                // Get root product
                var rootPrd = jf.ProductDoc.Product;

                var grps = (Groups)rootPrd.GetTechnologicalObject(Constant.TechObjGroup);
                var grp1 = grps.Add();
                var grp2 = grps.Add();

                grp1.AddExplicit(jf.Product);
                grp2.AddExplicit(rootPrd);

                var cls = (Clashes)rootPrd.GetTechnologicalObject(Constant.TechObjClash);
                var clash = cls.Add();

                clash.FirstGroup = grp1;
                clash.SecondGroup = grp2;
                clash.ComputationType = CatClashComputationType.catClashComputationTypeBetweenTwo;

                clash.Compute();

                for (int i = 1; i <= clash.Conflicts.Count; i++)
                {
                    var prdReport = new ReportPartInfo();

                    Array arr = new object[3];
                    var cfl = clash.Conflicts.Item(i);
                    Thread.Sleep(20);
                    Product prd;
                    if (cfl.FirstProduct.get_Name() == jf.JFPartName)
                    {
                        cfl.GetFirstPointCoordinates(arr);
                        prd = cfl.SecondProduct;
                    }
                    else
                    {
                        cfl.GetSecondPointCoordinates(arr);
                        prd = cfl.FirstProduct;
                    }

                    prdReport.Name = prd.get_PartNumber();
                    prdReport.Material = rp.GetMaterial(prd);


                    var cpt = jf.Factory.AddNewPointCoord((double)arr.GetValue(0),
                                                          (double)arr.GetValue(1),
                                                          (double)arr.GetValue(2));
                    jf.AddTempAndUpdate(cpt);

                    double thick = 0;
                    if (prd.ReferenceProduct.Parent is PartDocument parDoc &&
                        parDoc.Part is Part prt &&
                        prt.Bodies.Count > 0 && prt.Bodies.Item(1).Shapes.Count > 0 &&
                        prt.Bodies.Item(1).Shapes.Item(1) is ThickSurface sh)
                    {
                        thick = sh.TopOffset.Value - sh.BotOffset.Value;
                    }

                    prdReport.Gauge = thick;
                    rp.WeldedParts.Add(prdReport);

                    pts.Add((cpt, thick));
                    Thread.Sleep(30);
                }

                return (true, pts);
            }
            catch (Exception ex)
            {
                BL.Log(ex);
                //BL.ShowExcpMsg(ex);
                return (false, pts);
            }
            finally
            {
                if (clBody != null) { jf.DeleteObj(clBody); }
            }
        }

        /// <summary>
        ///     Note: This function shouldn't be called too frequently
        /// </summary>
        /// <param name="jf"></param>
        private static void HideAllElm(ModelJoinFile jf)
        {
            for (int i = 1; i <= jf.Part.HybridBodies.Count; i++)
            {
                jf.SetShow(jf.Part.HybridBodies.Item(i), false);
            }
            for (int i = 1; i <= jf.GeoConstruction.HybridBodies.Count; i++)
            {
                jf.SetShow(jf.GeoConstruction.HybridBodies.Item(i), false);
            }
            //for (int i = 1; i <= jf.Part.Bodies.Count; i++)
            //{
            //    jf.SetShow(jf.Part.Bodies.Item(i), false);
            //}
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="jf"></param>
        /// <returns></returns>
        public static async Task<bool> GetPointInGeo(ModelJoinFile jf)
        {
            try
            {
                await Task.Run(() =>
                {
                    jf.LstPointsSelected.Clear();

                    for (int i = 1; i <= jf.GeoPoints.HybridShapes.Count; i++)
                    {
                        var shp = jf.GeoPoints.HybridShapes.Item(i);
                        if (!(shp is Point))
                        {
                            if (!(shp is HybridShapeSymmetry sm &&
                                  GetPointCoord(jf, sm.ElemToSymmetry).Result.isOK))
                            {
                                continue;
                            }
                        }

                        jf.LstPointsSelected.Add(shp);
                    }
                });

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
        /// <param name="jf"></param>
        /// <param name="propName"></param>
        /// <param name="propVal"></param>
        /// <returns></returns>
        public static async Task<bool> CreateStrProperty(ModelJoinFile jf, string propName, string propVal)
        {
            try
            {
                return await Task.Run(() =>
                {
                    var paras = jf.PartDoc.Product.UserRefProperties;
                    foreach (var pr in paras)
                    {
                        if (pr is StrParam strPr &&
                            Path.GetFileName(strPr.get_Name()) == propName)
                        {
                            if (strPr.get_Value() != propVal)
                            {
                                strPr.set_Value(propVal);
                            }
                            return true;
                        }
                    }

                    _ = paras.CreateString(propName, propVal);
                    return true;
                });
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
        /// <param name="jf"></param>
        /// <param name="refPt"></param>
        /// <returns></returns>
        public static async Task<(bool isOK, Array arr)> GetPointCoord(ModelJoinFile jf, Reference refPt)
        {
            return await Task.Run(() =>
            {
                try
                {
                    var mea = jf.WBench.GetMeasurable(refPt);
                    Array co = new object[3];
                    mea.GetPoint(co);
                    return (co.GetValue(0) != null &&
                            co.GetValue(1) != null &&
                            co.GetValue(2) != null, co);
                }
                catch
                {
                    return (false, null);
                }
            });
        }
    }
}
