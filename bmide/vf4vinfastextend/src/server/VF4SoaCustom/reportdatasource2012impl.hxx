/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_CUSTOM_2020_12_REPORTDATASOURCE_IMPL_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2020_12_REPORTDATASOURCE_IMPL_HXX


#include <reportdatasource2012.hxx>

#include <Custom_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2020_12
            {
                class ReportDataSourceImpl;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2020_12::ReportDataSourceImpl : public VF4::Soa::Custom::_2020_12::ReportDataSource

{
public:

    virtual ReportDataSourceImpl::Get150BomWithVariantsDSResponse get150BomWithVariantsDS ( const std::vector< Get150BomWithVariantsDSInput >& inputs );
    virtual ReportDataSourceImpl::PFEPReportResponse getPFEPReport ( const std::vector< PFEPReportInput >& input );
    virtual ReportDataSourceImpl::ProcessStatusReportResponse getProcessStatusReport ( const ProcessStatusReportInput& input );


};

#include <Custom_undef.h>
#endif
