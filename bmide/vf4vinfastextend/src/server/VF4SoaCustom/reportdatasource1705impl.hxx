/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_CUSTOM_2017_05_REPORTDATASOURCE_IMPL_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2017_05_REPORTDATASOURCE_IMPL_HXX


#include <reportdatasource1705.hxx>

#include <Custom_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2017_05
            {
                class ReportDataSourceImpl;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2017_05::ReportDataSourceImpl : public VF4::Soa::Custom::_2017_05::ReportDataSource

{
public:

    virtual VF4::Soa::Custom::_2017_05::ReportDataSourceImpl::CostReportDSOutput getCostReportDS ( const CostReportDSInput& input );


};

#include <Custom_undef.h>
#endif
