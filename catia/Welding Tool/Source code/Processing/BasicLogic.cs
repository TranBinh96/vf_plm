using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using Welding_Tool.Common;

namespace Welding_Tool.Processing
{
    public static class BL
    {
        /// <summary>
        /// 
        /// </summary>
        /// <param name="msg"></param>
        /// <param name="tit"></param>
        public static void ShowInfoMsg(string msg, string tit = "Info")
        {
            Application.Current.MainWindow.Activate();
            MessageBox.Show(Application.Current.MainWindow, msg, tit, MessageBoxButton.OK, MessageBoxImage.Information);
            MainWindow.LOG.Info(msg);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="msg"></param>
        /// <param name="tit"></param>
        public static void ShowWarnMsg(string msg, string tit = "Warning")
        {
            Application.Current.MainWindow.Activate();
            MessageBox.Show(Application.Current.MainWindow, msg, tit, MessageBoxButton.OK, MessageBoxImage.Warning);
            MainWindow.LOG.Warn(msg);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="msg"></param>
        /// <param name="tit"></param>
        public static void ShowCritMsg(string msg, string tit = "Error")
        {
            Application.Current.MainWindow.Activate();
            MessageBox.Show(Application.Current.MainWindow, msg, tit, MessageBoxButton.OK, MessageBoxImage.Error);
            MainWindow.LOG.Error(msg);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="msg"></param>
        /// <param name="tit"></param>
        /// <returns></returns>
        public static bool ShowQuesMsg(string msg, string tit = "Question")
        {
            Application.Current.MainWindow.Activate();
            return MessageBoxResult.OK == MessageBox.Show(Application.Current.MainWindow, msg, tit, MessageBoxButton.OKCancel, MessageBoxImage.Question);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="msg"></param>
        /// <param name="tit"></param>
        /// <returns></returns>
        public static bool ShowWarnQuesMsg(string msg, string tit = "Question")
        {
            Application.Current.MainWindow.Activate();
            return MessageBoxResult.OK == MessageBox.Show(Application.Current.MainWindow, msg, tit, MessageBoxButton.OKCancel, MessageBoxImage.Warning);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="msg"></param>
        /// <param name="tit"></param>
        /// <returns></returns>
        public static bool ShowQuesYNMsg(string msg, string tit = "Question")
        {
            Application.Current.MainWindow.Activate();
            return MessageBoxResult.Yes == MessageBox.Show(Application.Current.MainWindow, msg, tit, MessageBoxButton.YesNo, MessageBoxImage.Question, MessageBoxResult.No);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="msg"></param>
        /// <param name="tit"></param>
        /// <returns></returns>
        public static bool ShowWarnQuesYNMsg(string msg, string tit = "Question")
        {
            Application.Current.MainWindow.Activate();
            return MessageBoxResult.Yes == MessageBox.Show(Application.Current.MainWindow, msg, tit, MessageBoxButton.YesNo, MessageBoxImage.Warning);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="msg"></param>
        /// <param name="tit"></param>
        public static void ShowExcpMsg(Exception ex)
        {
            var msg = ex.Message;
#if DEBUG
            msg += "\n" + ex.Source;
            msg += "\n" + ex.InnerException;
            msg += "\n" + ex.TargetSite;
            msg += "\n" + ex.StackTrace;
#endif
            Application.Current.MainWindow.Activate();
            MessageBox.Show(Application.Current.MainWindow, msg, "Exception", MessageBoxButton.OK, MessageBoxImage.Error);

#if !DEBUG

            msg += "\n" + ex.Source;
            msg += "\n" + ex.InnerException;
            msg += "\n" + ex.TargetSite;
            msg += "\n" + ex.StackTrace;
#endif
            MainWindow.LOG.Fatal(msg, ex);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="ex"></param>
        public static void Log(Exception ex)
        {
            var msg = ex.Message;
            msg += "\n" + ex.Source;
            msg += "\n" + ex.InnerException;
            msg += "\n" + ex.TargetSite;
            msg += "\n" + ex.StackTrace;
            MainWindow.LOG.Fatal(msg, ex);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="info"></param>
        public static void Log(string info)
        {
            MainWindow.LOG.Info(info);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="btn"></param>
        public static void SetBtnSelecting(object btn)
        {
            ((Button)btn).Background = Brushes.Blue;
            ((Button)btn).Foreground = Brushes.White;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="btn"></param>
        public static void SetBtnNoSel(object btn, bool isNoSel)
        {
            var btnn = (Button)btn;
            btnn.Background = isNoSel ? Brushes.White : Brushes.LightGreen;
            btnn.Foreground = Brushes.Black;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="obj"></param>
        /// <returns></returns>
        public static string GetCOMType(object obj)
        {
            return Microsoft.VisualBasic.Information.TypeName(obj);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="path"></param>
        /// <returns></returns>
        public static bool SelectFolderDialog(ref string path, string desc)
        {
            using (var dialog = new System.Windows.Forms.FolderBrowserDialog())
            {
                dialog.Description = desc;
                if (!string.IsNullOrEmpty(path))
                {
                    dialog.SelectedPath = path;
                }

                if (dialog.ShowDialog() == System.Windows.Forms.DialogResult.OK)
                {
                    path = dialog.SelectedPath;
                    return true;
                }
                return false;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="obj"></param>
        /// <returns></returns>
        public static T DeepClone<T>(T obj)
        {
            using (var ms = new MemoryStream())
            {
                var formatter = new BinaryFormatter();
                formatter.Serialize(ms, obj);
                ms.Position = 0;

                return (T)formatter.Deserialize(ms);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="obj1"></param>
        /// <param name="obj2"></param>
        /// <returns></returns>
        public static bool IsEquals<T>(T obj1, T obj2)
        {
            foreach (PropertyInfo property in obj1.GetType().GetProperties().Where(p => p.CanWrite))
            {
                var val1 = property.GetValue(obj1, null);
                var val2 = property.GetValue(obj2, null);
                if (val1.GetType().IsValueType)
                {
                    if (!Equals(val1, val2))
                    {
                        return false;
                    }
                }
                else if (!IsEquals(val1, val2))
                {
                    return false;
                }
            }

            return true;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="src"></param>
        /// <param name="targ"></param>
        public static void ShallowCopy<T>(T src, T targ)
        {
            foreach (PropertyInfo property in typeof(T).GetProperties().Where(p => p.CanWrite))
            {
                property.SetValue(targ, property.GetValue(src, null), null);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="input"></param>
        /// <returns></returns>
        public static string GetSubStrFrLastDash(string input, bool isCheckDotToo = false)
        {
            var lstPtIdx = input.LastIndexOf("_");
            if (lstPtIdx < 0 && isCheckDotToo)
            {
                lstPtIdx = input.LastIndexOf(".");
            }
            if (lstPtIdx < 0)
            {
                lstPtIdx = input.Length;
            }
            return input.Substring(0, lstPtIdx);
        }
    }
}
