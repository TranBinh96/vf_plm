#include "Vinfast_Custom.h"

#include <map>

extern int AH_send_parts_transfer_sqp(EPM_action_message_t msg)
{
	int			iRetCode = ITK_ok;
	int			iNumArg = 0;
	tag_t		tRootTask = NULLTAG;

std:string status("");
	std::map<std::string, int> allowedStatusesAndAge;
	int	iNumArgs = 0;
	iNumArgs = TC_number_of_arguments(msg.arguments);
	char* pcFlag = NULL;
	char* pcValue = NULL;
	char* pcObject = NULL;
	char* pcEvent = NULL;


	if (iNumArgs > 0)
	{
		for (int i = 0; i < iNumArgs; i++)
		{
			/*getting arguments*/
			CHECK_ITK(iRetCode, ITK_ask_argument_named_value(TC_next_argument(msg.arguments), &pcFlag, &pcValue));
			if (iRetCode == ITK_ok)
			{
				if (tc_strcasecmp(pcFlag, "object_type") == 0 && pcValue != NULL)
				{
					pcObject = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
					tc_strcpy(pcObject, pcValue);
				}
				else if (tc_strcmp(pcFlag, "event_type") == 0)
				{
					pcEvent = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
					tc_strcpy(pcEvent, pcValue);
				}
			}
		}
		SAFE_MEM_free(pcFlag);
		SAFE_MEM_free(pcValue);

	}
	else
	{
		iRetCode = EPM_invalid_argument;
	}

	char* isDebug = NULL;
	int ret_code = ITK_ok;
	CHECK_ITK(ret_code, PREF_ask_char_value("VF_PLM_DEBUG", 0, &isDebug));

	LOG(TC_write_syslog("[VF] Enter %s\n", __FUNCTION__));

	/*getting root task*/
	CHECK_ITK(iRetCode, EPM_ask_root_task(msg.task, &tRootTask));
	if (tRootTask != 0)
	{
		int			iTgt = 0;
		tag_t* ptTgt = NULL;

		/*getting target objects*/
		CHECK_ITK(iRetCode, EPM_ask_attachments(tRootTask, EPM_target_attachment, &iTgt, &ptTgt));

		for (int i = 0; i < iTgt; i++)
		{
			tag_t partRev = ptTgt[i];
			logical isItemRev = false;
			tag_t itemRevType = NULLTAG;
			char* objType = NULL;
			CHECK_ITK(iRetCode, AOM_ask_value_string(partRev, "object_type", &objType));
			if (tc_strcmp(objType, pcObject) == 0) {

				char* partId = NULL;
				char* revId = NULL;
				char* uid = NULL;
				CHECK_ITK(iRetCode, AOM_ask_value_string(partRev, "item_id", &partId));
				CHECK_ITK(iRetCode, AOM_ask_value_string(partRev, "item_revision_id", &revId));

				LOG(TC_write_syslog("[VF] item_id: %s\n", partId));
				LOG(TC_write_syslog("[VF] revId: %s\n", revId));
				LOG(TC_write_syslog("[VF] object_type: %s\n", objType));


				ITK__convert_tag_to_uid(partRev, &uid);

				std::string json;
				json.append("{");
				json.append("type:\"").append(pcEvent).append("\",");
				json.append("uid:\"").append(uid).append("\"");
				json.append("}");

				try
				{
					char* targetDir = "";
					CHECK_ITK(ret_code, PREF_ask_char_value("VF_Subscription_Event_Trigger_Folder", 0, &targetDir));
					targetDir = (targetDir && strlen(targetDir) > 0) ? targetDir : "\\\\10.128.49.113\\SAP_Transfer\\sap_mm\\";
					std::string jsonFileName(targetDir);
					jsonFileName.append(partId).append("_").append(revId).append("_").append(objType).append(".json");
					LOG(TC_write_syslog("[VF] targetDir: %s\n", targetDir));
					LOG(TC_write_syslog("[VF] jsonFileName: %s\n", jsonFileName.c_str()));
					LOG(TC_write_syslog("[VF] json: %s\n", json.c_str()));
					std::ofstream os(jsonFileName.c_str());
					os << json;
					os.close();
				}

				catch (const std::ios_base::failure& err)
				{
					TC_write_syslog("[VF] ERROR: err.what: %s\n", err.what());
				}

				SAFE_SM_FREE(partId);
				SAFE_SM_FREE(revId);
				SAFE_SM_FREE(uid);



				SAFE_SM_FREE(objType);
			}

		}

		SAFE_SM_FREE(ptTgt);
	}

	LOG(TC_write_syslog("[VF] Leave %s\n", __FUNCTION__));
	SAFE_SM_FREE(isDebug);

	return iRetCode;
}