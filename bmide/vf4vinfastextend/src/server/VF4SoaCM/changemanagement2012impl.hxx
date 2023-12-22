/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_CM_2020_12_CHANGEMANAGEMENT_IMPL_HXX 
#define TEAMCENTER_SERVICES_CM_2020_12_CHANGEMANAGEMENT_IMPL_HXX


#include <changemanagement2012.hxx>

#include <CM_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace CM
        {
            namespace _2020_12
            {
                class ChangeManagementImpl;
            }
        }
    }
}


class SOACM_API VF4::Soa::CM::_2020_12::ChangeManagementImpl : public VF4::Soa::CM::_2020_12::ChangeManagement

{
public:

    virtual ChangeManagementImpl::DeriveChangeResponse deriveChange ( const std::vector< DeriveChangeInput >& inputs );
    virtual ChangeManagementImpl::ImpactedProgramResponse getImpactedPrograms ( const ImpactedProgramInput& inputs );
    virtual ChangeManagementImpl::PartNumberChangeFullHistoryResponse getPartNumberChangeFullHistory ( const std::vector< PartNumberChangeFullHistoryInput >& intputs );
    virtual ChangeManagementImpl::PartNumberChangeHistoryResponse getPartNumberChangeHistory ( const std::vector< PartNumberChangeHistoryInput >& inputs );


};

#include <CM_undef.h>
#endif
