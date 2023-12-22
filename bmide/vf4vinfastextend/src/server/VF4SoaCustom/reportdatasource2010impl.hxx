/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_CUSTOM_2020_10_REPORTDATASOURCE_IMPL_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2020_10_REPORTDATASOURCE_IMPL_HXX


#include <reportdatasource2010.hxx>

#include <Custom_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2020_10
            {
                class ReportDataSourceImpl;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2020_10::ReportDataSourceImpl : public VF4::Soa::Custom::_2020_10::ReportDataSource

{
public:

    virtual ReportDataSourceImpl::VFEcrCostResponse getEcrsCost ( const std::vector< BusinessObjectRef<Teamcenter::BusinessObject> >& ecrs );
    virtual ReportDataSourceImpl::FGValidationOutput validateFamilyGroup ( const FGValidationInput& input );
    virtual ReportDataSourceImpl::VFPartCostCalcResponse calculatePartsCost ( const std::vector< std::string >& partNumbers, const std::string programName, const std::string isDryRun );


};

#include <Custom_undef.h>
#endif
