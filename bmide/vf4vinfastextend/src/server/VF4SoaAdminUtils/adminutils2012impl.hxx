/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_ADMINUTILS_2020_12_ADMINUTILS_IMPL_HXX 
#define TEAMCENTER_SERVICES_ADMINUTILS_2020_12_ADMINUTILS_IMPL_HXX


#include <adminutils2012.hxx>

#include <AdminUtils_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace AdminUtils
        {
            namespace _2020_12
            {
                class AdminUtilsImpl;
            }
        }
    }
}


class SOAADMINUTILS_API VF4::Soa::AdminUtils::_2020_12::AdminUtilsImpl : public VF4::Soa::AdminUtils::_2020_12::AdminUtils

{
public:

    virtual AdminUtilsImpl::ChangeUOMResponse changeUOM ( const std::vector< ChangeUOMInput >& inputs );
    virtual AdminUtilsImpl::ReassignWFTaskResponse reassignWFTask ( const std::vector< ReassignWFTaskInput >& inputs );

    virtual AdminUtilsImpl::SetPropertyResponse setProperties ( const std::vector< SetPropertyInput >& inputs, bool isKeepModifiedDate );

};

#include <AdminUtils_undef.h>
#endif
