/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_INTEGRATION_2020_12_SAPINTEGRATION_IMPL_HXX 
#define TEAMCENTER_SERVICES_INTEGRATION_2020_12_SAPINTEGRATION_IMPL_HXX


#include <sapintegration2012.hxx>

#include <Integration_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace Integration
        {
            namespace _2020_12
            {
                class SAPIntegrationImpl;
            }
        }
    }
}


class SOAINTEGRATION_API VF4::Soa::Integration::_2020_12::SAPIntegrationImpl : public VF4::Soa::Integration::_2020_12::SAPIntegration

{
public:

    virtual SAPIntegrationImpl::GetMasterMaterialsResponse getMasterMaterials ( const std::vector< GetMasterMaterialsInput >& inputs );
    virtual SAPIntegrationImpl::TransferOperationToJSONResponse transferOperationJSONToMES ( const TransferOperationToJSONInputs& inputs );
    virtual SAPIntegrationImpl::TransferOperationToMESResponse transferOperationToMES ( const TransferOperationToMESInputs& inputs );


};


#include <Integration_undef.h>

#include <curl/curl.h>

#include <stdio.h>
#include <iostream>
#include <fstream>
#include <string.h>
#include <string>
#include <vector>
#include <map>
#include <tc/tc_startup.h>
#include <epm/epm.h>
#include <epm/epm_errors.h>
#include <epm/epm_toolkit_tc_utils.h>
#include <user_exits/epm_toolkit_utils.h>
#include <tc/tc_util.h>
#include <sa/tcfile.h>
#include <tccore/tctype.h>
#include <tc/tc_arguments.h>
#include <sa/sa.h>
#include <tccore/item.h>
#include <tccore/aom.h>
#include <tccore/project.h>
#include <epm/cr.h>
#include <epm/cr_action_handlers.h>
#include <tccore/custom.h>
#include <ae/ae.h>
#include <tccore/aom_prop.h>
#include <epm/epm_task_template_itk.h>
#include <direct.h>
#include <fclasses/tc_date.h>
#include <tc/emh.h>
#include <tc/emh_errors.h>
#include <tc/envelope.h>
#include <tc/folder.h>
#include <tccore/grm.h>
#include <tccore/grm_errors.h>
#include <tccore/grmtype.h>
#include <ict/ict_userservice.h>
#include <tccore/item.h>
#include <tccore/method.h>
#include <property/nr.h>
#include <pom/pom/pom.h>
#include <pom/pom/pom_errors.h>
#include <tc/preferences.h>
#include <property/prop.h>
#include <ps/ps.h>
#include <sa/sa.h>
#include <sa/sa_errors.h>
#include <ss/ss_const.h>
#include <ss/ss_errors.h>
#include <time.h>
#include <sa/user.h>
#include <user_exits/user_exits.h>
#include <tccore/workspaceobject.h>
#include <tc/wsouif_errors.h>
#include <tccore/project.h>
#include <tc/tc_arguments.h>
#include <sa/groupmember.h>
#include <ecm/ecm.h>
#include <qry/qry.h>
#include <bom/bom.h>
#include <epm/signoff.h>
#include <ics\ics.h>
#include <ics\ics2.h>
#include <res\res_itk.h>
#include <sa/am.h>
#include <algorithm>
#include <cctype>
#include <locale>
#include <tc/lm.h>
#include <tc/log.h>
#include <sstream>
#include <iostream>
#include <vector>
#include <set>
#include <lov/lov.h>
#include <cfm/cfm.h>
#include <base_utils/ScopedSmPtr.hxx>
#include <base_utils/ScopedPtr.hxx>
#include <form/form.h>
#include <tc/emh.h>
#include <stdarg.h>
#include <qry/qry.h>
#include <tc/aliaslist.h>
#include <sa/audit.h>
#include <tccore/item.h>
#include <ae/dataset.h>
#include <tccore/workspaceobject.h>
#include <sa/tcfile.h>
#include <tccore/item.h>
#include <ae/dataset.h>
#include <sa/tcfile_cache.h>



#endif
