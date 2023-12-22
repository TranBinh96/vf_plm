/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_ADMINUTILS_2020_12_ORGANIZATIONUTILS_IMPL_HXX 
#define TEAMCENTER_SERVICES_ADMINUTILS_2020_12_ORGANIZATIONUTILS_IMPL_HXX


#include <organizationutils2012.hxx>

#include <AdminUtils_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace AdminUtils
        {
            namespace _2020_12
            {
                class OrganizationUtilsImpl;
            }
        }
    }
}


class SOAADMINUTILS_API VF4::Soa::AdminUtils::_2020_12::OrganizationUtilsImpl : public VF4::Soa::AdminUtils::_2020_12::OrganizationUtils

{
public:

    virtual OrganizationUtilsImpl::ChangeUserPasswordResponse checkAndChangeUserPassword ( const std::vector< ChangeUserPasswordInput >& inputs );


};

#include <AdminUtils_undef.h>
#endif
