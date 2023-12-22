/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_CUSTOM_2020_12_INTEGRATION_IMPL_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2020_12_INTEGRATION_IMPL_HXX


#include <integration2012.hxx>

#include <Custom_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2020_12
            {
                class IntegrationImpl;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2020_12::IntegrationImpl : public VF4::Soa::Custom::_2020_12::Integration

{
public:

    virtual IntegrationImpl::StructureSearchInMPPOutput structureSearchInMPP ( const StructureSearchInMPPInput& input );


};

#include <Custom_undef.h>
#endif
