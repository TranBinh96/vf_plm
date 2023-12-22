using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Welding_Tool.DataModel;

namespace Welding_Tool.ViewModel
{
    public class VMMagWeld : VMBase
    {
        public VMMagWeld(string name)
        {
            Name = name;
        }

        public override bool IsCreWeldSpotEn => false;

        public override Task<bool> CreateWeldBody(List<ModelReportData> ReportData, IProgress<ModelReport> reporter)
        {
            throw new NotImplementedException();
        }
    }
}
