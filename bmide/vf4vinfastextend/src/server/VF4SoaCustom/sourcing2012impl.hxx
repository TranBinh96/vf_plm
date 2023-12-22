/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_CUSTOM_2020_12_SOURCING_IMPL_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2020_12_SOURCING_IMPL_HXX


#include <sourcing2012.hxx>

#include <Custom_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2020_12
            {
                class SourcingImpl;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2020_12::SourcingImpl : public VF4::Soa::Custom::_2020_12::Sourcing

{
public:

    virtual SourcingImpl::AssignSourcePartToBuyerResponse assignSourcePartToBuyer ( const std::vector< STBuyerAssignmentInfo >& inputs );
    virtual SourcingImpl::GetOrCreateVFCostResponse getOrCreateVFCost ( const std::vector< GetOrCreateVFCostInput >& inputs );


};

#include <Custom_undef.h>
#endif
