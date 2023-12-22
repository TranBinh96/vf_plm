//@<COPYRIGHT>@
//==================================================
//Copyright $2021.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/* 
 * @file 
 *
 *   This file contains the implementation for the Extension VF4_SourcePartRevCreatePost
 *
 */
#include <VF4_VinFastExtend/VF4_SourcePartRevCreatePost.hxx>

#include <ug_va_copy.h>
#include <tc/emh.h>
#include <tc/emh.h>
#include <user_exits/epm_toolkit_utils.h>
#include <tccore/aom_prop.h>
#include <tccore/aom.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <thread>
#include <future>
#include <chrono>
#include <map>
#include "sub_mgr\tceventmgr.h"

#define EXIT_FAILURE 1

#define RETURN_ERR(X) \
{ \
	int return_code = (X);\
	if (return_code != ITK_ok)\
    {\
		report_error__(return_code);\
		goto EXIT;\
    }\
}

void report_error__(int return_code)
{
	char *error_message_string;
	EMH_ask_error_text(return_code, &error_message_string);
	if(error_message_string != NULL)
	{
		TC_write_syslog ("[VF]Error: %d ERROR MSG: %s.\n", return_code, error_message_string);
		TC_write_syslog ("[VF]Function: %s\nFILE: %s LINE: %d\n", __FUNCTION__, __FILE__, __LINE__);
		MEM_free(error_message_string);
	}
}

int execCmd(std::string cmd)
{
	int ret = ITK_ok;
	if (cmd.empty() == false)
	{
		//TC_write_syslog("\n[VF]cmd=%s",cmd.c_str());
		ret = system(cmd.c_str());
	}
	else
	{
		ret = -1;
	}

	return ret;
}

int VF4_SourcePartRevCreatePost( METHOD_message_t  *msg, va_list args )
{
	msg;args;
	TC_write_syslog("\n[VF]Enter VF4_SourcePartRevCreatePost");

	tag_t sourcePartRev = msg->object_tag;
	char *purchaseLevel = NULL;
	char *partID = NULL;
	char *program = NULL;
	char *partRevType = NULL;
	char *sourcePartUid = NULL;
	int ifail = ITK_ok;
	if (sourcePartRev != NULL)
	{
		RETURN_ERR( AOM_ask_value_string(sourcePartRev, "vf4_purchasing_level", &purchaseLevel) );
		RETURN_ERR( AOM_ask_value_string(sourcePartRev, "vf4_bom_vfPartNumber", &partID) );
		RETURN_ERR( AOM_ask_value_string(sourcePartRev, "vf4_platform_module", &program) );
		RETURN_ERR( AOM_ask_value_string(sourcePartRev, "vf4_car_part_bo_type", &partRevType) );

		ITK__convert_tag_to_uid(sourcePartRev, &sourcePartUid);

		if (purchaseLevel && partID && program && partRevType)
		{
			std::string cmd("vinf_trigger_sap_mm_transfer ");// triger post action
			cmd.append(" -partID=").append(partID);
			cmd.append(" -purchaseLevel=").append(purchaseLevel);
			cmd.append(" -program=\"").append(program).append("\"");
			cmd.append(" -partRevType=\"").append(partRevType).append("\"");
			cmd.append(" -sourcePartUid=\"").append(sourcePartUid).append("\"");

			TC_write_syslog("\n[VF] Calls ITK %s", cmd.c_str());
			//std::async(std::launch::async, execCmd, cmd);

			TCEVENTMGR_post_event_2(sourcePartRev, "__Replica_Update");

			SAFE_SM_FREE(purchaseLevel);
			SAFE_SM_FREE(partID);
			SAFE_SM_FREE(program);
			SAFE_SM_FREE(partRevType);
			SAFE_SM_FREE(sourcePartUid);
		}
		else
		{
			TC_write_syslog("\n[VF]Error: one of required parameters are null!");
			TC_write_syslog("\n[VF]Function: %s\nFILE: %s LINE: %d\n", __FUNCTION__, __FILE__, __LINE__);
		}
	}
	else
	{
		TC_write_syslog(" \n[VF] ERROR: sourcePartRev = null");
	}

	TC_write_syslog("\n[VF] end  VF4_SourcePartRevCreatePost");
EXIT:
	return ifail;
}

