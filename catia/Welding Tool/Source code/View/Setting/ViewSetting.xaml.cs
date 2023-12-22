using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using Welding_Tool.ViewModel;

namespace Welding_Tool.View
{
    /// <summary>
    /// Interaction logic for ViewSetting.xaml
    /// </summary>
    public partial class ViewSetting : Window
    {
        public ViewSetting()
        {
            InitializeComponent();
            Closing += ((VMSetting)DataContext).OnClosingWindow;
        }
    }
}
