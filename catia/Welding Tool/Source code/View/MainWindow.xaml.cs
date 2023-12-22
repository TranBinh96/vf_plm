using HybridShapeTypeLib;
using INFITF;
using MECMOD;
using Microsoft.Office.Interop.Excel;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Welding_Tool.Common;
using Welding_Tool.DataModel;
using Welding_Tool.Processing;
using Welding_Tool.ViewModel;
using Path = System.IO.Path;
using Window = System.Windows.Window;

namespace Welding_Tool
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window, INotifyPropertyChanged
    {
        /// <summary>
        /// 
        /// </summary>
        public MainWindow()
        {
            DataContext = this;
            InitializeComponent();
            var ver = Assembly.GetExecutingAssembly().GetName().Version.ToString();
            var arrVer = ver.Split('.');
            this.Title += string.Join(".", arrVer.Take(2));

            LoadScrDC.CheckCatiaAppEvent += LoadScrDC_CheckCatiaAppEvent;

            ReportData = new List<ModelReportData>();

            var spotw = new VMSpotWeld("Spot Weld")
            {
                Parent = this,
                JFData = JFData,
                LoadScrDC = LoadScrDC,
                CmdCreWeldSpot = CmdCreWeldSpot,
                CmdCreReport = CmdCreReport,
            };

            spotw.SCA_CreWeldBody = () => SCA_CreWeldBody();
            spotw.Init();

            ListWelding.Add(spotw);
            ListWelding.Add(new VMMagWeld("Mag Weld"));
            SelectedWelding = spotw;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        /// <exception cref="NotImplementedException"></exception>
        private async Task LoadScrDC_CheckCatiaAppEvent()
        {
            await Task.Delay(300);
            bool isOK;
            (isOK, CATIA) = await CPr.GetCatiaApp();

            if (isOK)
            {
                await ResetTool();
                LoadScrDC.SetVisible(false);
            }
            else
            {
                BL.ShowCritMsg("CATIA Application not found!");
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        private async Task ResetTool()
        {
            if (JFData.Part != null)
            {
                await CPr.DeleteDataWeldBody(JFData, false);
                await CPr.DeleteDataWeldLine(JFData, false);
            }

            BtnCreJFContent = Constant.CreJF;
            JFData.Product = null;
            JFData.Surface = null;

            JFData.GeoConstruction = null;
            JFData.GeoInput = null;
            JFData.GeoOutWeldAxis = null;
            JFData.GeoOutWeldPoints = null;
            JFData.GeoOutWeldSpot = null;
            JFData.GeoTemp = null;
            JFData.GeoTempCons = null;
            JFData.GeoTempPtCons = null;

            if (SelectedWelding != null) { SelectedWelding.BtnSelSurContent = Constant.NoSel; }
            if (JFData.Part != null)
            {
                SCA_CreateJFPart();
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async void Window_Loaded(object sender, RoutedEventArgs e)
        {
            bool isOK;
            (isOK, CATIA) = await CPr.GetCatiaApp();

            if (!isOK)
            {
                LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
            }

            // Create report progress for creating spot weld process
            reporter = new Progress<ModelReport>();
            reporter.ProgressChanged += Reporter_ProgressChanged;

            await Task.Delay(100);
            if (!IniProcessing.ReadIniFile(Constant.ConfigFile, _settData))
            {
                BL.ShowCritMsg("Cannot read 'ConfigParameters.ini' file!");
                BL.ShowWarnMsg("Application will be closed");
                this.Close();
            }
            else
            {
                JFData.Config = _settData;
                CPr.Config = _settData;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Window_Closed(object sender, EventArgs e)
        {
            IniProcessing.WriteToIni(Constant.ConfigFile, _settData);
            // Dispose object
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        private async Task CreateJFPart()
        {
            //SetEnableCreJFBtn(false);

            var msg = "Creating JF Part...";
            LoadScrDC.ShowMode(LoadScrMode.Loading, msg);

            this.Topmost = true;

            await CheckDeleteOldData();

            BtnCreJFContent = Constant.CreJF;
            JFData.Product = null;
            JFData.Surface = null;

            JFData.GeoConstruction = null;
            JFData.GeoInput = null;
            JFData.GeoOutWeldAxis = null;
            JFData.GeoOutWeldPoints = null;
            JFData.GeoOutWeldSpot = null;
            JFData.GeoTemp = null;
            JFData.GeoTempCons = null;
            JFData.GeoTempPtCons = null;

            if (SelectedWelding != null) { SelectedWelding.BtnSelSurContent = Constant.NoSel; }

            var isOK = await CPr.CreateJFPart(CATIA, JFData);
            if (isOK == false)
            {
                BL.ShowCritMsg("Cannot create JF Part");
            }
            else
            {
                BtnCreJFContent = JFData.JFPartName;
            }

            if (!CPr.SetForegroundCATIA(CATIA))
            {
                LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                return;
            }

            await Task.Delay(100);
            Status = "Create JF Part finish!";
            //BtnCreateJFPart.IsEnabled = true;
            //SetEnableCreJFBtn(true);
            LoadScrDC.ShowMode(LoadScrMode.None);
            SCA_CreateJFPart();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async Task CreateWeldBody()
        {
            try
            {
                LoadScrDC.ShowMode(LoadScrMode.Percentage, "Creating Welding Bodies ...");
                var isCheckOk = await CPr.CheckWorkbenchTop(CATIA, JFData);
                if (isCheckOk == null)
                {
                    LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                    return;
                }
                else if (isCheckOk == false)
                {
                    BL.ShowWarnMsg("Please activate the top Assembly on the Specification Tree by double clicking!");
                    LoadScrDC.ShowMode(LoadScrMode.None);
                    return;
                }

                var isOK = await SelectedWelding.CreateWeldBody(ReportData, reporter);
                if (!isOK)
                {
                    BL.ShowCritMsg("Cannot create Welding bodies!");
                }
                else
                {
                    var msg = "Create Welding Bodies done!\n\n";
                    msg += $"'{JFData.LstWeldBodies.Count}' Spot Weld have been created!\n";
                    msg += $"'{ReportData.Count(x => x.IsSkip)}' Spot Weld have been skipped!";
                    BL.ShowInfoMsg(msg);
                }

                if (!CPr.SetForegroundCATIA(CATIA))
                {
                    LoadScrDC.ShowMode(LoadScrMode.CatNotFound);
                    return;
                }

                await Task.Run(() => CATIA.RefreshDisplay = true);
                Status = "Create Welding Bodies finish!";

                if (isOK && JFData.LstWeldBodies.Count > 0 &&
                    _settData.General.IsAutoCreReport)
                {
                    await CreateReportFile();
                }

                LoadScrDC.ShowMode(LoadScrMode.None);
            }
            finally
            {
                SCA_CreWeldBody();
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        /// <exception cref="NotImplementedException"></exception>
        private void Reporter_ProgressChanged(object sender, ModelReport e)
        {
            LoadScrDC.SetLoadingMsg(e.Message);
            LoadScrDC.SetProgress(e.PgValue);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        private async Task CreateReportFile(bool isCloseLoading = false)
        {
            bool isCreCSV = true;
            ExcelProcessing exl = null;
            string newFilePath = "";
            IEnumerable<ModelReportData> rpDat = null;

            try
            {
                LoadScrDC.ShowMode(LoadScrMode.Loading, "Creating Report file ...");

                //var directory = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
                var directory = Assembly.GetEntryAssembly().Location;
                directory = Path.Combine(Path.GetPathRoot(directory), Constant.ReportFolder);
                if (!Directory.Exists(directory))
                {
                    try { Directory.CreateDirectory(directory); }
                    catch { }
                }

                while (true)
                {
                    if (!Directory.Exists(directory))
                    {
                        if (!BL.SelectFolderDialog(ref directory, "Select folder to save 'Report File'"))
                        {
                            BL.ShowCritMsg("Create report process has been cancelled!");
                            return;
                        }
                    }
                    newFilePath = Path.Combine(directory, $"Report Spot Weld_{DateTime.Now:yyyyMMddHHmmss}.xlsx");
                    System.IO.File.Copy(Constant.TemplateExcelFile, newFilePath);

                    if (!System.IO.File.Exists(newFilePath))
                    {
                        BL.ShowCritMsg($"Cannot create Excel file '{newFilePath}'! " +
                                       $"Please select the folder again.");
                    }
                    else
                    {
                        break;
                    }
                }

                rpDat = ReportData.Where(x => !x.IsSkip);
                exl = new ExcelProcessing();

                if (!await exl.Init())
                {
                    BL.ShowCritMsg("Cannot initiate Excel applcation!");
                    return;
                }

                if (!await exl.FindAndDisableAddIn(ExcelAddInName.Foxit))
                {
                    return;
                }

                var (isOK, book) = await exl.OpenFile(newFilePath, false);
                if (!isOK)
                {
                    BL.ShowCritMsg("Cannot open Report file!");
                    return;
                }

                Worksheet sht = null;
                await Task.Run(() => { sht = book.Worksheets[1]; });
                if (!await exl.WriteData(sht, rpDat))
                {
                    BL.ShowCritMsg("Cannot write report data to Excel file!");
                    await exl.Dispose();
                }
                else
                {
                    await Task.Run(() => book.Save());
                    await Task.Delay(300);
                    isCreCSV = false;
                    BL.ShowInfoMsg("Create Report done!");
                    await exl.ShowExcel();
                }
            }
            catch (Exception ex)
            {
                BL.ShowExcpMsg(ex);
            }
            finally
            {
                if (isCreCSV && exl != null && Directory.Exists(Path.GetDirectoryName(newFilePath)))
                {
                    await CreateCSV(exl, newFilePath, rpDat);
                }
                if (isCloseLoading) { LoadScrDC.ShowMode(LoadScrMode.None); }
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="exl"></param>
        /// <param name="path"></param>
        /// <param name="rpDat"></param>
        /// <returns></returns>
        private async Task CreateCSV(ExcelProcessing exl, string path, IEnumerable<ModelReportData> rpDat)
        {
            if (!await exl.WriteDataCSV(path, rpDat))
            {
                BL.ShowCritMsg("Cannot write report data to CSV file too!");
            }
            else
            {
                BL.ShowInfoMsg("Output data has exported to CSV file instead!\nPlease check output folder!");
            }
        }

    }
}
