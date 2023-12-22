using Microsoft.Office.Core;
using Microsoft.Office.Interop.Excel;
using Microsoft.Win32;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.DataModel;

namespace Welding_Tool.Processing
{
    internal class ExcelProcessing
    {
        [DllImport("user32.dll")]
        private static extern int GetWindowThreadProcessId(IntPtr hWnd, ref int processId);

        [DllImport("USER32.DLL")]
        private static extern bool SetForegroundWindow(IntPtr hWnd);

        private Application ExlApp;
        private int ProcessID;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="hWnd"></param>
        /// <param name="lpdwProcessId"></param>
        /// <returns></returns>
        private bool GetExcelProcessId(IntPtr hWnd, ref int lpdwProcessId)
        {
            try
            {
                // Get WindowThreadProcess ID
                GetWindowThreadProcessId(hWnd, ref lpdwProcessId);
                return true;
            }
            catch //(Exception ex)
            {
                return false;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        public async Task<bool> Init()
        {
            if (ExlApp != null)
            {
                return true;
            }

            try
            {
                return await Task.Run(() =>
                {
                    ExlApp = new Application();
                    if (!GetExcelProcessId((IntPtr)ExlApp.Hwnd, ref ProcessID))
                    {
                        ProcessID = 0;
                    }

#if DEBUG
                    ExlApp.Visible = true;
#else
                    ExlApp.Visible = false;
#endif
                    ExlApp.DisplayAlerts = false;
                    ExlApp.AskToUpdateLinks = false;

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
        /// <returns></returns>
        public async Task ShowExcel()
        {
            try
            {
                await Task.Run(() => ExlApp.Visible = true);
                SetForegroundWindow((IntPtr)ExlApp.Hwnd);
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        public async Task Dispose()
        {
            await Task.Run(() => DestroyExcelApp(ref ExlApp, ref ProcessID));
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="excelApp"></param>
        /// <param name="processId"></param>
        /// <returns></returns>
        private bool DestroyExcelApp(ref Application excelApp, ref int processId)
        {
            try
            {
                if (excelApp is object)
                {
                    // Quit Excel app
                    excelApp.Quit();

                    // Clear resources
                    MRComObject(ref excelApp, true);
                }

                // Kill the process
                if (processId != 0)
                {
                    try
                    {
                        var p = Process.GetProcessById(processId);
                        if (p is object)
                        {
                            p.Kill();
                        }
                    }
                    catch { }

                    processId = 0;
                }

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
        /// <typeparam name="T"></typeparam>
        /// <param name="objCom"></param>
        /// <param name="force"></param>
        private void MRComObject<T>(ref T objCom, bool force = false) where T : class
        {
            if (objCom == null)
            {
                return;
            }

            try
            {
                if (Marshal.IsComObject(objCom))
                {
                    if (force)
                    {
                        Marshal.FinalReleaseComObject(objCom);
                    }
                    else
                    {
                        Marshal.ReleaseComObject(objCom);
                    }
                }
            }
            finally
            {
                objCom = null;
            }
        }

        /// <summary>
        ///     Open the Excel file
        /// </summary>
        /// <param name="filePath"></param>
        /// <returns></returns>
        public async Task<(bool, Workbook)> OpenFile(string filePath, bool isReadOnly = true)
        {
            try
            {
                // Create Excel app if not created
                if (ExlApp == null && !await Task.Run(() => Init()))
                {
                    BL.ShowCritMsg("Cannot initiate Excel application!");
                    return (false, null);
                }

                var workbook = await Task.Run(() =>
                {
                    return ExlApp.Workbooks.Open(filePath, UpdateLinks: true,
                                                 IgnoreReadOnlyRecommended: true,
                                                 ReadOnly: isReadOnly);
                });

                return (true, workbook);
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return (false, null);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sht"></param>
        /// <param name="row"></param>
        /// <param name="col"></param>
        /// <returns></returns>
        public async Task<string> GetDataFromSheet(Worksheet sht, int row, int col)
        {
            return await Task.Run(() =>
            {
                var value = sht.Cells[row, col].Value2;
                return value == null ? "" : value.ToString();
            });
        }

        /// <summary>
        ///     Convert column name to column number
        /// </summary>
        /// <param name="columnName"></param>
        /// <returns></returns>
        public static int ExlColStr2Num(string columnName)
        {
            if (string.IsNullOrEmpty(columnName))
            {
                throw new ArgumentNullException("Exel column name");
            }

            if (columnName.Any(char.IsDigit))
            {
                throw new FormatException("Excel column name contain number");
            }

            columnName = columnName.ToUpperInvariant();

            int sum = 0;
            for (int i = 0; i < columnName.Length; i++)
            {
                sum *= 26;
                sum += (columnName[i] - 'A' + 1);
            }

            return sum;
        }

        /// <summary>
        /// 
        /// </summary>
        public async Task CloseAllFile()
        {
            await Task.Run(() => ExlApp.Workbooks.Close());
        }

        /// <summary>
        /// 
        /// </summary>
        public async Task CloseFile(Workbook book)
        {
            await Task.Run(() => book.Close());
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sht"></param>
        /// <param name="data"></param>
        /// <returns></returns>
        public async Task<bool> WriteData(Worksheet sht, IEnumerable<ModelReportData> data)
        {
            try
            {
                return await Task.Run(() =>
                {
                    var curRow = RowPos.Start;
                    foreach (var item in data)
                    {
                        if (string.IsNullOrEmpty(item.SpotWeldName))
                        {
                            continue;
                        }

                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.SpotWeldName)] = item.SpotWeldName;
                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.JoinedParts)] = item.JoinPartsName;

                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.Coord)] = Math.Round(item.Coord.X, 3);
                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.Coord) + 1] = Math.Round(item.Coord.Y, 3);
                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.Coord) + 2] = Math.Round(item.Coord.Z, 3);

                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.PanelCnt)] = item.PanelsCount;
                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.StackUpType)] = item.StackUpType;
                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.JoinThickness)] = Math.Round(item.JoinThickness, 3);
                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.JoinDiameter)] = Math.Round(item.JoinDiameter, 3);

                        for (int i = 0; i < item.WeldedParts.Count; i++)
                        {
                            sht.Cells[curRow, ExlColStr2Num(ColumnPos.PartName) + i] = item.WeldedParts[i].Name;
                            sht.Cells[curRow, ExlColStr2Num(ColumnPos.PartMaterial) + i] = item.WeldedParts[i].Material;
                            sht.Cells[curRow, ExlColStr2Num(ColumnPos.PartGauge) + i] = item.WeldedParts[i].Gauge;
                            if (i == 2)
                            {
                                // Template file show only 3 part
                                break;
                            }
                        }

                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.WeldAxis)] = Math.Round(item.AxisDirection.X, 3);
                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.WeldAxis) + 1] = Math.Round(item.AxisDirection.Y, 3);
                        sht.Cells[curRow, ExlColStr2Num(ColumnPos.WeldAxis) + 2] = Math.Round(item.AxisDirection.Z, 3);

                        curRow++;
                    }

                    sht.Cells.Select();
                    sht.Cells.EntireColumn.AutoFit();
                    sht.Cells[1, 1].Select();

                    return true;
                });
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
                return false;
            }
        }

        private const string Delemiter = ",";
        /// <summary>
        /// 
        /// </summary>
        /// <param name="sht"></param>
        /// <param name="data"></param>
        /// <returns></returns>
        public async Task<bool> WriteDataCSV(string path, IEnumerable<ModelReportData> data)
        {
            try
            {
                path = path.Substring(0, path.Length - 5) + ".csv";
                if (File.Exists(path))
                {
                    File.Delete(path);
                }

                using (StreamWriter sw = File.CreateText(path))
                {
                    // Create header
                    string rowCont = ColumnHeader.SpotWeldName;
                    rowCont += Delemiter + ColumnHeader.JoinedParts;
                    rowCont += Delemiter + string.Format(ColumnHeader.Coord, "X");
                    rowCont += Delemiter + string.Format(ColumnHeader.Coord, "Y");
                    rowCont += Delemiter + string.Format(ColumnHeader.Coord, "Z");
                    rowCont += Delemiter + ColumnHeader.PanelCnt;
                    rowCont += Delemiter + ColumnHeader.StackUpType;
                    rowCont += Delemiter + ColumnHeader.JoinThickness;
                    rowCont += Delemiter + ColumnHeader.JoinDiameter;
                    rowCont += Delemiter + string.Format(ColumnHeader.PartName, 1);
                    rowCont += Delemiter + string.Format(ColumnHeader.PartName, 2);
                    rowCont += Delemiter + string.Format(ColumnHeader.PartName, 3);
                    rowCont += Delemiter + string.Format(ColumnHeader.PartMaterial, 1);
                    rowCont += Delemiter + string.Format(ColumnHeader.PartMaterial, 2);
                    rowCont += Delemiter + string.Format(ColumnHeader.PartMaterial, 3);
                    rowCont += Delemiter + string.Format(ColumnHeader.PartGauge, 1);
                    rowCont += Delemiter + string.Format(ColumnHeader.PartGauge, 2);
                    rowCont += Delemiter + string.Format(ColumnHeader.PartGauge, 3);
                    rowCont += Delemiter + string.Format(ColumnHeader.WeldAxis, "X");
                    rowCont += Delemiter + string.Format(ColumnHeader.WeldAxis, "Y");
                    rowCont += Delemiter + string.Format(ColumnHeader.WeldAxis, "Z");
                    await sw.WriteLineAsync(rowCont);

                    foreach (var item in data)
                    {
                        if (string.IsNullOrEmpty(item.SpotWeldName))
                        {
                            continue;
                        }

                        rowCont = item.SpotWeldName;
                        rowCont += Delemiter + item.JoinPartsName;

                        rowCont += Delemiter + Math.Round(item.Coord.X, 3);
                        rowCont += Delemiter + Math.Round(item.Coord.Y, 3);
                        rowCont += Delemiter + Math.Round(item.Coord.Z, 3);

                        rowCont += Delemiter + item.PanelsCount;
                        rowCont += Delemiter + item.StackUpType;
                        rowCont += Delemiter + Math.Round(item.JoinThickness, 3);
                        rowCont += Delemiter + Math.Round(item.JoinDiameter, 3);

                        for (int i = 0; i < 3; i++)
                        {
                            rowCont += Delemiter + (item.WeldedParts.Count > i ? item.WeldedParts[i].Name : "");
                        }
                        for (int i = 0; i < 3; i++)
                        {
                            rowCont += Delemiter + (item.WeldedParts.Count > i ? item.WeldedParts[i].Material : "");
                        }
                        for (int i = 0; i < 3; i++)
                        {
                            rowCont += Delemiter + (item.WeldedParts.Count > i ? item.WeldedParts[i].Gauge.ToString() : "");
                        }

                        rowCont += Delemiter + Math.Round(item.AxisDirection.X, 3);
                        rowCont += Delemiter + Math.Round(item.AxisDirection.Y, 3);
                        rowCont += Delemiter + Math.Round(item.AxisDirection.Z, 3);

                        await sw.WriteLineAsync(rowCont);
                    }
                }
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
        /// <param name="addinDes"></param>
        /// <returns>True: AddIn is disabled, or not found; False: Cannot disable</returns>
        public async Task<bool> FindAndDisableAddIn(string addinDes)
        {
            try
            {
                var addins = await Task.Run(() => ExlApp.COMAddIns);
                COMAddIn foxit = null;

                await Task.Run(() =>
                {
                    foreach (COMAddIn addin in addins)
                    {
                        if (Equals(addin.Description, addinDes))
                        {
                            foxit = addin;
                            break;
                        }
                    }
                });

                if (foxit == null || foxit.Connect == false)
                {
                    return true;
                }

                var msg = $"The '{addinDes}' in Excel is conflict with current application, ";
                msg += "which need to be disabled to be able to create the report file.\n\n";
                msg += $"Do you want to disable '{addinDes}' of Excel?";
                if (BL.ShowWarnQuesMsg(msg, "Add-In conflict"))
                {
                    try
                    {
                        await Task.Run(() => foxit.Connect = false);
                    }
                    catch
                    {
                        var str = @"SOFTWARE\Microsoft\Office\Excel\Addins\" + foxit.ProgId;
                        var key = Registry.CurrentUser.OpenSubKey(str, true);

                        if (key == null)
                        {
                            return false;
                        }

                        key.SetValue(AddInRegKey.LoadBehavior, 2, RegistryValueKind.DWord);
                        key.Close();
                    }

                    // Restart the Excel application
                    await Dispose();
                    await Init();

                    return true;
                }
                else
                {
                    return false;
                }
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
    public static class ColumnPos
    {
        public const string SpotWeldName = "A";
        public const string JoinedParts = "B";
        public const string Coord = "C";
        public const string PanelCnt = "F";
        public const string StackUpType = "G";
        public const string JoinThickness = "H";
        public const string JoinDiameter = "I";
        public const string PartName = "J";
        public const string PartMaterial = "M";
        public const string PartGauge = "P";
        public const string WeldAxis = "S";
    }

    /// <summary>
    /// 
    /// </summary>
    public static class ColumnHeader
    {
        public const string SpotWeldName = "Joinery ID";
        public const string JoinedParts = "Joined Parts";
        public const string Coord = "{0}";
        public const string PanelCnt = "Number of Panels";
        public const string StackUpType = "Stack-up Type";
        public const string JoinThickness = "Joint Thickness (mm)";
        public const string JoinDiameter = "Joint Diameter (mm)";
        public const string PartName = "PART {0}";
        public const string PartMaterial = "PART {0} Material";
        public const string PartGauge = "PART {0} Gauge (mm)";
        public const string WeldAxis = "Weld Axis - {0} Direction";
    }

    public static class ExcelAddInName
    {
        public const string Foxit = "FoxitReader PDF Creator COM Add-in";
    }

    public static class AddInRegKey
    {
        public const string LoadBehavior = "LoadBehavior";
    }

    /// <summary>
    /// 
    /// </summary>
    public static class RowPos
    {
        public const int Start = 3;
    }
}
