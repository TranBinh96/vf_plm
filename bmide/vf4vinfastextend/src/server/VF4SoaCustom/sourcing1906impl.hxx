/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_CUSTOM_2019_06_SOURCING_IMPL_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2019_06_SOURCING_IMPL_HXX
#define	DEL							"DEL"
#define	QRY_SOURCE_PART				"Source Part"
#define	PREF_AFS_ST 				"VF_AFS_ST"
#define	PREF_Standard_ST 			"VF_Standard_ST"
#define	VF_PURCHASE_LEVEL			"vf4_purchasing_level"

#include <sourcing1906.hxx>
#include <map>
#include <stdio.h>
#include <string>
#include <vector>
#include "qry/qry.h"
#include "tccore/aom_prop.h"
#include "tccore/aom.h"
#include "tccore/item.h"
#include "fclasses/tc_string.h"
#include "user_exits\epm_toolkit_utils.h"
#include "tc/preferences.h"
#include "pom/pom/pom.h"
#include <iostream>
#include <fstream>
#include <ctime>
#include <stdio.h>
#include <tccore/item.h>
#include <tccore/aom_prop.h>
#include <tc/tc_startup.h>
#include <tc/envelope.h>
#include <tc/tc_util.h>
#include <base_utils/Mem.h>
#include <stdlib.h>
#include <sa/user.h>
#include <tc/emh.h>
#include <string.h>
#include <tc/preferences.h>
#include <fclasses/tc_string.h>
#include <conio.h>
#include <qry/qry.h>
#include <user_exits/epm_toolkit_utils.h>
#include <tccore/license.h>
#include <io.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <qry\qry_errors.h>
#include <tccore/grm.h>
#include <bom/bom.h>
#include <me/me.h>
#include <tc/tc_startup.h>
#include <Custom_exports.h>

namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2019_06
            {
                class SourcingImpl;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2019_06::SourcingImpl : public VF4::Soa::Custom::_2019_06::Sourcing

{
public:

    virtual SourcingImpl::STResponse getSourceTrackerDetails ( const std::string sourcingProgram, bool isDELRequiredInPurchaseLevel, bool isCostAttributesRequired, const std::vector< std::string >& selecetdUIDs );


};

#include <Custom_undef.h>
#endif
