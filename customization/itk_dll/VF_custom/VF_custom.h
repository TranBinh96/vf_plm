#include <stdio.h>
#include <string>
#include <vector>
#include <algorithm>
#define  DEBUG_PRINT	
#include <fclasses/tc_stdlib.h>
#include <fclasses/tc_string.h>
#include <tccore/tctype.h>
#include <sa/tcvolume.h>
#include <tccore/item.h>
#include <tccore/item_msg.h>
#include <tc/emh.h>
#include <tc/emh_errors.h>
#include <tccore/custom.h>
#include <tccore/method.h>
#include <pom/pom/pom.h>
#include <tc/preferences.h>
#include <tc/prm.h>
#include <property/prop.h>
#include <property/prop_msg.h>
#include <sa/sa.h>
#include <ss/ss_const.h>
#include <ss/ss_types.h>
#include <textsrv/textserver.h>
#include <user_exits/user_exits.h>
#include <tccore/workspaceobject.h>
#include <ict/ict_userservice.h>
#include <unidefs.h>
#include <time.h>
#include <ps/gcr.h>
//#include <tccore/iman_grmtype.h>
#include <tccore/grmtype.h>
#include <ae/dataset.h>
#include <ae/ae.h>
#include <tccore/aom.h>
#include <form/form.h>
#include <tccore/aom_prop.h>
#include <lov/lov.h>
#include <lov/lov_msg.h>
#include <qry/qry.h>
#include <property\nr.h>
#include <ae/dataset_msg.h>
#include <sa/am.h>
#include <tccore/tctype.h>
#include <fclasses/tc_ctype.h>
#include <sa/tcfile.h>
#include <tc/tc_macros.h>
#include <fclasses/tc_stdio.h>
#include <fclasses/tc_date.h>
#include <bom/bom_msg.h>
#include <bom/bom.h>
#include <tc/folder.h>
#include <tccore\uom.h>
#include <user_exits\epm_toolkit_utils.h>

#include    <base_utils/TcResultStatus.hxx>
#include    <base_utils/ScopedSmPtr.hxx>
#include    <base_utils/ScopedPtr.hxx>

#include <regex>
#include <iostream>
#include <fstream>
#include <string.h>
#include <map>
#include <metaframework/BulkInput.hxx>
#include <metaframework/BulkData.hxx>
#define _CRT_SECURE_NO_WARNINGS

using namespace std;

#define CHECK_FAIL { \
	if (ifail != ITK_ok) { \
	ifail = EMH_ask_error_text(ifail,&error_text); TC_write_syslog("Failed due to following error:\n%s",error_text);exit (EXIT_FAILURE); \
	} \
}

#define SAFE_MEM_free(x)     \
  MEM_free(x);               \
  x = NULL;

#define CHECK_ITKCALL(rc,f) {\
  char *msg = NULL;\
  (rc) = (f);\
  if ((rc) != ITK_ok) {\
    EMH_ask_error_text ((rc), &msg);\
    if (msg != NULL) {\
      if( DEBUG_PRINT ) {\
	  TC_write_syslog("ERROR[%d]: %s\n\t(file: %s, line:%d)\n", (rc), msg, __FILE__, __LINE__);\
	  }\
      SAFE_MEM_free (msg);\
      msg=NULL;\
    }\
  }\
}

#define CHECK_ITK(iSts,fn){\
	if(iSts == ITK_ok){\
		if((iSts = (fn) ) != ITK_ok){\
			char* error_str = NULL;\
			EMH_ask_error_text(iSts, &error_str);\
			TC_write_syslog ("+++ERROR: %d ERROR MSG: %s. Error in line %d, function %s",\
				iSts, error_str, __LINE__, __FUNCTION__);\
			MEM_free(error_str);\
		}\
	}\
}

#define	CHECK_PROCUREMENT_TYPE	    (EMH_USER_error_base + 270)
#define	VF_ERR_INVALID_FG_CODE      (EMH_USER_error_base + 271)
#define	VF_ERR_INVALID_FG_RULE_CODE	(EMH_USER_error_base + 272)

#define	CHECK_PROCUREMENT_TYPE				(EMH_USER_error_base + 270)
#define TargetCostForm_Validation_Error		(EMH_USER_error_base +272)
#define ActualCostForm_Validation_Error     (EMH_USER_error_base +276)
#define UOM_Validation_Error		(EMH_USER_error_base +274)
#define SW_PART_TYPE_ERROR                  (EMH_USER_error_base +277)

DLLAPI int vf_baseline_post_actions_register();
DLLAPI int vf_create_cost_forms(METHOD_message_t*, va_list);
DLLAPI int vf_item_save_post_action(METHOD_message_t*, va_list);
DLLAPI int vf_item_save_post_action__UOM(METHOD_message_t*, va_list);
DLLAPI int VF_save_last_login_date(METHOD_message_t*, va_list);
DLLAPI int vf_is_frozen_getter(METHOD_message_t*, va_list);
DLLAPI int vf_original_part_number_is_modifable(METHOD_message_t*, va_list);
DLLAPI int vf_fg_rule_save_pre_action(METHOD_message_t*, va_list);
DLLAPI int vf_fg_save_pre_action(METHOD_message_t*, va_list);
DLLAPI int vf_set_vf4_cost_approval_date(METHOD_message_t* msg, va_list args);
DLLAPI int VF_validateTargetCostForm(METHOD_message_t* msg, va_list args);
DLLAPI int VF_assign2Buyer(METHOD_message_t* msg, va_list args);
DLLAPI int vf_change_ownership_bop_rev(METHOD_message_t* msg, va_list args);
DLLAPI int vf_set_ecr_number(METHOD_message_t* msg, va_list args);
DLLAPI int vf_set_ecn_number(METHOD_message_t* msg, va_list args);
DLLAPI int vf_set_maturity_level(METHOD_message_t* msg, va_list args);

void vf_split(std::string str, const std::string& delimiter, std::vector<std::string>& result);
double getRate(string sourceCurrency, string destCurrency);

int add_error_stack(int errCode, const char* errMsg); DLLAPI int vf_item_save_post_action(METHOD_message_t*, va_list);
bool checkIsAssembly(tag_t part);