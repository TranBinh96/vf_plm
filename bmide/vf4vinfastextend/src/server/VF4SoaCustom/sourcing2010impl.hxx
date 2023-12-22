/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_CUSTOM_2020_10_SOURCING_IMPL_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2020_10_SOURCING_IMPL_HXX


#include <sourcing2010.hxx>

#include <Custom_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2020_10
            {
                class SourcingImpl;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2020_10::SourcingImpl : public VF4::Soa::Custom::_2020_10::Sourcing

{
public:

    virtual bool planDateCalculation ( const std::vector< std::string >& sourceparts );


};

#include <Custom_undef.h>
#endif
