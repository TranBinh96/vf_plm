/***************************************************************************************
File         : RH_validate_original_base_part.cpp

Description  : To check if workflow is already applied on mentioned object.

Input        : None

Output       : None

Author       : Vinfast

Revision History :
Date            Revision    Who              Description
-----------------------------------------------------------------------------
July, 2023     1.0         Binh			Initial Creation
******************************************************************************************/
#include "Vinfast_Custom.h"
#define Error (EMH_USER_error_base +284)

vector<tag_t> getParts(std::string partNumberStr, char *idQueryAttrID, string type, const char* queryName)
{
	int iRetCode = ITK_ok;
	int iCount = 0;
	tag_t tPart = NULLTAG;
	tag_t tQuery = NULLTAG;
	vector<tag_t> foundParts;
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	CHECK_ITK(iRetCode, QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		return foundParts;
	}

	char* queryEntries[] = { idQueryAttrID,"Type","Active sequence"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*)MEM_alloc(sizeof(char) * ((int)tc_strlen(partNumberStr.c_str()) + 1));
	queryValues[1] = (char*)MEM_alloc(sizeof(char) * ((int)tc_strlen(type.c_str()) + 1));

	queryValues[0] = tc_strcpy(queryValues[0], partNumberStr.c_str());
	queryValues[1] = tc_strcpy(queryValues[1], type.c_str());

	CHECK_ITK(iRetCode, QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));
	for (int i = 0; i < iCount; tQryResults) 
	{
		foundParts.push_back(tQryResults[i]);
	}

	SAFE_SM_FREE(queryValues[1]);
	SAFE_SM_FREE(queryValues[0]);
	return foundParts;
}


vector<string> convert_string(const char* inputStr)
{
	char* next_p;
	char seps[] = " ; ,\t\n";
	vector<string> ReqObjType;
	char tempStr[512];
	tc_strcpy(tempStr, inputStr);
	char* chars_array = strtok_s(tempStr, seps, &next_p);
	while (chars_array)
	{
		if (std::find(ReqObjType.begin(), ReqObjType.end(), chars_array) == ReqObjType.end())
		{
			ReqObjType.push_back(chars_array);
		}
		chars_array = strtok_s(NULL, seps, &next_p);
	}

	return ReqObjType;


}

EPM_decision_t RH_validate_original_base_part(EPM_rule_message_t msg)
{
	int			iRetCode = ITK_ok;
	int			iNumArg = 0;
	tag_t		tRootTask = NULLTAG;
	Teamcenter::scoped_smptr<char> pcFlag;
	Teamcenter::scoped_smptr<char> pcValue;
	string      pcObjType("");
	EPM_decision_t decision = EPM_decision_e::EPM_go;
	vector<string> list_parts_fail;
	vector<string> listPartsNotHaveReadAccess;
	map<string, string> suspectedNotValiPartsAndDetailMessages;
	string allFailPartsStr("");

	/*getting number of arguments*/
	iNumArg = TC_number_of_arguments(msg.arguments);

	if (iNumArg > 0)
	{
		for (int iArgs = 0; iArgs < iNumArg; iArgs++)
		{
			iRetCode = ITK_ask_argument_named_value(TC_next_argument(msg.arguments), &pcFlag, &pcValue);
			/*for argument template name*/
			if (tc_strcmp(pcFlag.getString(), "object_type") == 0)
			{
				pcObjType = pcValue.getString();
			}
		}
	}
	else
	{
		iRetCode = EPM_wrong_number_of_arguments;
	}


	/*getting root task*/
	CHECK_ITK(iRetCode, EPM_ask_root_task(msg.task, &tRootTask));
	if (tRootTask != 0)
	{
		int			iTgt = 0;
		Teamcenter::scoped_smptr<tag_t> ptTgt;
		vector<string> ReqObjType;
		vector<string> ReqStatusType;

		ReqObjType = convert_string(pcObjType.c_str());
		/*ReqStatusType = convert_string(pcReStatus.c_str());*/


		/*getting target objects*/
		CHECK_ITK(iRetCode, EPM_ask_attachments(tRootTask, EPM_target_attachment, &iTgt, &ptTgt));
		for (int iInd = 0; iInd < iTgt; iInd++)
		{
			Teamcenter::scoped_smptr<char> pcTgtType;
			Teamcenter::scoped_smptr<char> object_string;
			CHECK_ITK(iRetCode, WSOM_ask_object_type2(ptTgt[iInd], &pcTgtType));
			CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[iInd], "object_string", &object_string));
			for (int iCnt = 0; iCnt < ReqObjType.size(); iCnt++)
			{
				if (tc_strcmp(ReqObjType[iCnt].c_str(), pcTgtType.getString()) == 0)
				{
					Teamcenter::scoped_smptr<char> orginal_part;
					Teamcenter::scoped_smptr<char> part_id;
					tag_t item = NULLTAG;
					bool isFail = true;
					CHECK_ITK(iRetCode, AOM_ask_value_tag(ptTgt[iInd], "items_tag", &item));
					CHECK_ITK(iRetCode, AOM_ask_value_string(item, "vf4_orginal_part_number", &orginal_part));
					CHECK_ITK(iRetCode, AOM_ask_value_string(item, "item_id", &part_id));
					if (tc_strlen(orginal_part.getString()) > 2)					{
						const char* queryName = "__VF Original Latest Matured Revision";
						vector<tag_t> foundParts = getParts(orginal_part.getString(), "ID", "VF*Design*Revision", queryName);
						int  ipart = foundParts.size();
						if (ipart > 0)
						{
							isFail = false;
							continue;							
							/*int relese_cout = 0;
							tag_t* release_list = NULLTAG;
							
							CHECK_ITK(iRetCode, WSOM_ask_release_status_list(part, &relese_cout, &release_list));
							for (int i = 0; i < relese_cout; i++)
							{
								char* release_status = NULL;
								CHECK_ITK(iRetCode, RELSTAT_ask_release_status_type(release_list[i], &release_status));
								for (int i = 0; i < ReqStatusType.size(); i++)
								{
									string StatusType = ReqStatusType[i].c_str();
									if (StatusType.compare(release_status) == 0)
									{
										isFail = false;
										break;
									}
								}
								SAFE_SM_FREE(release_status);
							}*/							
							/*SAFE_SM_FREE(release_list);*/
						}
						if (isFail == true)
						{
							allFailPartsStr.append(part_id.getString()).append(";");
							list_parts_fail.push_back(part_id.getString());
							string detailMsg = "logined user does not have read access on original part ";
							detailMsg.append("\"").append(orginal_part.getString()).append("\"");
							suspectedNotValiPartsAndDetailMessages[part_id.getString()] = detailMsg;
							decision = EPM_decision_e::EPM_nogo;
						}
						/*else
						{
							string msg = "Cannot find part \"";
							msg.append(orginal_part).append("\" with query \"").append(queryName).append("\" or found more than one.");
							list_parts_fail.push_back(msg);
						}*/
					}
				}
			}
		}
		
		if (list_parts_fail.size() > 0)
		{
			vector<tag_t> foundParts = getParts(allFailPartsStr, "Item ID", "VF*Design", "Item...");
			if (foundParts.size() > 0)
			{
				for (tag_t foundPart : foundParts)
				{
					Teamcenter::scoped_smptr<char> foundPartId;
					CHECK_ITK(iRetCode, AOM_ask_value_string(foundPart, "item_id", &foundPartId));
					suspectedNotValiPartsAndDetailMessages[foundPartId.getString()] = "original part's maturity violates VF rule";
				}
			}
			
			string  lstpart = "";
			lstpart.append("The Original base parts'maturity of below parts violate VinFast rule or the current user does NOT have read access on the original base parts.");
			for (int i = 0; i < list_parts_fail.size(); i++)
			{
				string detailMsg = suspectedNotValiPartsAndDetailMessages[list_parts_fail[i]];
				lstpart.append("\n");
				lstpart.append(" -").append(list_parts_fail[i]).append(" => ").append(detailMsg);
			}
			char		ErrorMessage[1024] = "";
			sprintf(ErrorMessage, lstpart.c_str());
			CHECK_ITK(iRetCode, EMH_store_error_s1(EMH_severity_user_error, Error, ErrorMessage));
		}
	}

	return decision;
}

	