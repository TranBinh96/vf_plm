/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_CUSTOM_2017_05_INTEGRATION_IMPL_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2017_05_INTEGRATION_IMPL_HXX


#include <integration1705.hxx>

#include <Custom_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2017_05
            {
                class IntegrationImpl;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2017_05::IntegrationImpl : public VF4::Soa::Custom::_2017_05::Integration

{
public:

    virtual bool nominateSupplierToSQP ( const NominateSupplierInput& nominateSupplierInput );
    virtual bool removeSupplierInfo ( const std::string sourcePartNo );
    virtual bool updateQcheckerProperty ( const BusinessObjectRef<Teamcenter::ItemRevision>& designRevision, const std::string result );


};

#include <Custom_undef.h>
#endif
