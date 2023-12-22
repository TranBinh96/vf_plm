/*******************************************************************************
File		: AH_notify_all_participants.cpp
Description	: Send mail notification to all participants
Input		: None
Output		: None
Author		: ThienBQ2
Revision History:
Date			Revision	Who					Description
-------------------------------------------------------------------------------
Nov, 22 2023	1.0			ThienBQ2			Initial Creation
*******************************************************************************/
#include "Vinfast_Custom.h"

struct MailData {
	string TopSectionTitle;
	string LinkSectionTitle = "Select the preferred client to view the task:";
	string AWCLink;
	string RACLink;
	string TargetSectionTitle = "Targets:";
	string Footer = "This email was sent from Teamcenter. Please do not reply.";
	map<string, string> TopSectionInfo;
	vector<vector<string>> TargetData;
};

stringstream genMailBody(MailData data);

/// <summary>
/// 
/// </summary>
/// <param name="msg"></param>
/// <returns></returns>
extern int AH_notify_all_participants(EPM_action_message_t msg)
{
	int iRetCode = ITK_ok;
	int iNumArgs = 0;
	int	iPrefCount = 0;
	int	portPref_count = 0;
	tag_t tRootTask = NULLTAG;
	char* pcMailSubject = NULL;
	char* pcRecipients = NULL;
	char* pcObjType = NULL;
	char* pcFile = NULL;
	char* pcFlag = NULL;
	char* pcValue = NULL;
	char* pcReqType = NULL;

	boolean isDebug = false;
	boolean isNotifyAll = false;

	iNumArgs = TC_number_of_arguments(msg.arguments);

	std::stringstream mailBody;
	std::vector<string> szMailList;

	if (iNumArgs > 0)
	{
		for (int i = 0; i < iNumArgs; i++)
		{
			/*getting arguments*/
			CHECK_ITK(iRetCode, ITK_ask_argument_named_value(TC_next_argument(msg.arguments), &pcFlag, &pcValue));
			if (iRetCode == ITK_ok)
			{
				if (tc_strcasecmp(pcFlag, "subject") == 0 && pcValue != NULL)
				{
					pcMailSubject = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
					tc_strcpy(pcMailSubject, pcValue);
				}
				if (tc_strcasecmp(pcFlag, "type") == 0 && pcValue != NULL)
				{
					pcReqType = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
					tc_strcpy(pcReqType, pcValue);
				}
				if (tc_strcasecmp(pcFlag, "recipient") == 0 && pcValue != NULL)
				{
					pcRecipients = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
					tc_strcpy(pcRecipients, pcValue);
				}
				if (tc_strcasecmp(pcFlag, "debug") == 0 && pcValue != NULL)
				{
					isDebug = true;
				}
				if (tc_strcasecmp(pcFlag, "notifyAll") == 0 && pcValue != NULL)
				{
					isNotifyAll = true;
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

	/*check manadatory arguments*/
	if (tc_strcmp(pcMailSubject, "") == 0 || tc_strcmp(pcReqType, "") == 0)
	{
		iRetCode = EPM_invalid_argument;
	}

	/*getting root task*/
	CHECK_ITK(iRetCode, EPM_ask_root_task(msg.task, &tRootTask));
	if (tRootTask != 0)
	{
		int	iAllTasks = 0;
		int	iTgt = 0;
		tag_t* ptTgt = NULL;
		tag_t  current_job = NULLTAG;
		tag_t* ptAllTaskTags = NULL;
		tag_t tInitiator = NULL;
		string szRejMailList = "";
		char* process_desc = NULL;
		char * uid  ="";

		CHECK_ITK(iRetCode, AOM_ask_value_tag(msg.task, "owning_user", &tInitiator));
		CHECK_ITK(iRetCode, EPM_ask_job(msg.task, &current_job));
		CHECK_ITK(iRetCode, EPM_ask_sub_tasks(tRootTask, &iAllTasks, &ptAllTaskTags));
		CHECK_ITK(iRetCode, AOM_ask_value_string(current_job, "current_desc", &process_desc));
		CHECK_ITK(iRetCode, EPM_ask_attachments(tRootTask, EPM_target_attachment, &iTgt, &ptTgt));
		/*getting mail id of all participants from review task*/
		for (int iLoopAllTasks = 0; iLoopAllTasks < iAllTasks; iLoopAllTasks++)
		{
			char* pcTaskTypeName = NULL;
			char* pcTaskState = NULL;
			char* pcTaskResult = NULL;
			char* pcComments = NULL;
			char* pcTaskName = NULL;

			CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks], "task_type", &pcTaskTypeName));
			CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks], "task_state", &pcTaskState));
			CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks], "task_result", &pcTaskResult));

			if (tc_strcmp(pcTaskTypeName, "EPMReviewTask") == 0 &&
			   (tc_strcmp(pcTaskState, "Started") == 0 || tc_strcmp(pcTaskState, "Completed") == 0))
			//if (tc_strcmp(pcTaskTypeName, "EPMReviewTask") == 0)
			{
				int		iAllSubTasks = 0;
				tag_t* ptAllSubTaskTags = NULL;

				CHECK_ITK(iRetCode, EPM_ask_sub_tasks(ptAllTaskTags[iLoopAllTasks], &iAllSubTasks, &ptAllSubTaskTags));
				for (int iLoopAllSubTasks = 0; iLoopAllSubTasks < iAllSubTasks; iLoopAllSubTasks++)
				{
					char* pcSubTaskTypeName = NULL;

					CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllSubTaskTags[iLoopAllSubTasks], "task_type", &pcSubTaskTypeName));
					if (tc_strcmp(pcSubTaskTypeName, "EPMSelectSignoffTask") == 0)
					{
						int		iNumAttachs = 0;
						tag_t* ptAttachsTagList = NULL;
						ITK__convert_tag_to_uid(ptAllSubTaskTags[iLoopAllSubTasks], &uid);

						//get signoff attachments
						CHECK_ITK(iRetCode, EPM_ask_attachments(ptAllSubTaskTags[iLoopAllSubTasks], EPM_signoff_attachment, &iNumAttachs, &ptAttachsTagList));
						for (int iUsers = 0; iUsers < iNumAttachs; iUsers++)
						{
							tag_t		tUser = NULLTAG;
							char* pcUserMail = NULL;
							CHECK_ITK(iRetCode, AOM_ask_value_tag(ptAttachsTagList[iUsers], "fnd0Performer", &tUser));
							CHECK_ITK(iRetCode, EPM_get_user_email_addr(tUser, &pcUserMail));
							//fprintf(fMailAddress,"%s\n",pcUserMail);
							if (tc_strlen(pcUserMail) > 5)
							{
								if (std::find(szMailList.begin(), szMailList.end(), pcUserMail) == szMailList.end())
								{
									szMailList.push_back(pcUserMail);
								}
							}
							SAFE_SM_FREE(pcUserMail);
						}
					}
					SAFE_SM_FREE(pcSubTaskTypeName);
				}
			}

			/*getting mail id of participants from do or condition task*/
			if ((tc_strcmp(pcTaskTypeName, "EPMDoTask") == 0 || tc_strcmp(pcTaskTypeName, "EPMConditionTask") == 0) &&
				(tc_strcmp(pcTaskState, "Completed") == 0 || tc_strcmp(pcTaskState, "Started") == 0))
			{
				tag_t		tUser = NULLTAG;
				char* pcUserMail = NULL;

				CHECK_ITK(iRetCode, AOM_ask_value_tag(ptAllTaskTags[iLoopAllTasks], "fnd0Performer", &tUser));
				if (tUser != NULLTAG)
				{
					CHECK_ITK(iRetCode, EPM_get_user_email_addr(tUser, &pcUserMail));
					if (pcUserMail != 0) {
						if (tc_strcmp(pcUserMail, "") != NULL)
						{
							if (std::find(szMailList.begin(), szMailList.end(), pcUserMail) == szMailList.end())
							{
								szMailList.push_back(pcUserMail);
							}
						}
					}

					/*getting mail id of participants from do or condition task*/
					if (tc_strcmp(pcTaskState, "Completed") == 0 && tc_strcmp(pcTaskResult, "Rejected") == 0)
					{
						CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks], "comments", &pcComments));
						CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks], "object_name", &pcTaskName));
						CHECK_ITK(iRetCode, EPM_get_user_email_addr(tUser, &pcUserMail));
						//fprintf(fMailAddress,"%s",pcUserMail);
						if (pcUserMail != 0) {
							if (tc_strcmp(pcUserMail, "") != NULL)
							{
								if (szRejMailList.size() > 2)
								{
									szRejMailList.append(",");
								}
								szRejMailList.append(pcUserMail);
							}

						}
					}

					SAFE_SM_FREE(pcUserMail);
				}
			}

			if (isNotifyAll && tc_strcmp(pcTaskTypeName, "EPMAcknowledgeTask") == 0 &&
			   (tc_strcmp(pcTaskState, "Completed") == 0 || tc_strcmp(pcTaskState, "Started") == 0))
			{
				int		iAllSubTasks = 0;
				tag_t* ptAllSubTaskTags = NULL;

				CHECK_ITK(iRetCode, EPM_ask_sub_tasks(ptAllTaskTags[iLoopAllTasks], &iAllSubTasks, &ptAllSubTaskTags));
				for (int iLoopAllSubTasks = 0; iLoopAllSubTasks < iAllSubTasks; iLoopAllSubTasks++)
				{
					char* pcSubTaskTypeName = NULL;

					CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllSubTaskTags[iLoopAllSubTasks], "task_type", &pcSubTaskTypeName));
					if (tc_strcmp(pcSubTaskTypeName, "EPMSelectSignoffTask") == 0)
					{
						int		iNumAttachs = 0;
						tag_t* ptAttachsTagList = NULL;
						tag_t userTag = NULL;

						//get signoff attachments
						CHECK_ITK(iRetCode, EPM_ask_attachments(ptAllSubTaskTags[iLoopAllSubTasks], EPM_signoff_attachment, &iNumAttachs, &ptAttachsTagList));
						for (int iUsers = 0; iUsers < iNumAttachs; iUsers++)
						{
							tag_t		tUser = NULLTAG;
							char* pcUserMail = NULL;
							CHECK_ITK(iRetCode, AOM_ask_value_tag(ptAttachsTagList[iUsers], "fnd0Performer", &tUser));
							CHECK_ITK(iRetCode, EPM_get_user_email_addr(tUser, &pcUserMail));
							//fprintf(fMailAddress,"%s",pcUserMail);
							if (tc_strcmp(pcUserMail, "") != NULL)
							{
								if (std::find(szMailList.begin(), szMailList.end(), pcUserMail) == szMailList.end())
								{
									szMailList.push_back(pcUserMail);

									/*CHECK_ITK(iRetCode, SA_find_user2(pcUserMail, &userTag));
									if (userTag != 0)
										szMailList.push_back(pcUserMail);*/
								}
							}
						}
					}
				}
			}

			SAFE_SM_FREE(pcTaskTypeName);
			SAFE_SM_FREE(pcTaskState);
			SAFE_SM_FREE(pcTaskResult);
		}

		/*getting mail id of initiator of workflow*/
		if (tInitiator != NULLTAG)
		{
			char* pcUserMail = NULL;

			CHECK_ITK(iRetCode, EPM_get_user_email_addr(tInitiator, &pcUserMail));
			if (tc_strcmp(pcUserMail, "") != NULL)
			{
				if (std::find(szMailList.begin(), szMailList.end(), pcUserMail) == szMailList.end())
				{
					szMailList.push_back(pcUserMail);
				}
			}
		}

		//get from recepi
		char* next_p;
		char* pcRecipient = strtok_s(pcRecipients, ",", &next_p);
		while (pcRecipient)
		{
			if (tc_strlen(pcRecipient) > 5) 
			{
				szMailList.push_back(pcRecipient);
			}
			pcRecipient = strtok_s(NULL, ",", &next_p);
		}

		vector<string> objectTypes = split_string(pcReqType, ';');

		for (int iInd = 0; iInd < iTgt; iInd++)
		{
			bool check = false;
			char* pcObjType = NULL;
			char* pcDesc = NULL;
			char* pcSynopsis = NULL;
			char* pcObjString = NULL;
			char* pcItemID = NULL;
			char* pcRevID = NULL;
			char* pcName = NULL;
			char* prcName = NULL;
			tag_t	tChangeItem = NULLTAG;
			tag_t	tClass = NULLTAG;
			tag_t	tRevClass = NULLTAG;
			string	szTgtParts = "";

			/*get description, synopsis, module group*/

			/*CHECK_ITK(iRetCode, ITEM_ask_item_of_rev(ptTgt[iInd], &tChangeItem));
			if (tChangeItem == 0) {
				continue;
			}*/

			CHECK_ITK(iRetCode, WSOM_ask_object_type2(ptTgt[iInd], &pcObjType));

			for (int i = 0; i < objectTypes.size(); i++)
			{
				std::string objectType = objectTypes[i];
				ltrim(objectType);
				rtrim(objectType);
				if (tc_strcmp(pcObjType, objectType.c_str()) == 0)
					check = true;
			}

			/*get solution items*/
			if (check)
			{
				//CHECK_ITK(iRetCode, ITEM_ask_id2(tChangeItem, &pcItemID));
				//CHECK_ITK(iRetCode, ITEM_ask_rev_id2(ptTgt[iInd], &pcRevID));
				//CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[iInd], "object_name", &pcSynopsis));
				//CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[iInd], "object_desc", &pcDesc));
				//CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[iInd], "object_string", &pcObjString));
				CHECK_ITK(iRetCode, AOM_ask_value_string(tRootTask, "job_name", &prcName));

				MailData md = {};
				md.TopSectionTitle = "Overview:";
				md.TopSectionInfo["Current Task:"] = "Notify";
				md.TopSectionInfo["Process Name:"] = string(prcName);
				//string(pcItemID) + "/" + string(pcRevID) + " - " + string(pcSynopsis);
				md.TopSectionInfo["Due Date:"] = "None";

				//prepare email to sending
				md.AWCLink = "https://tcweb.vinfast.vn/#/com.siemens.splm.clientfx.tcui.xrt.showObject?uid=";
				TC_write_syslog("before awc_link: %s", md.AWCLink.c_str());
				md.RACLink = "http://tcweb.vinfast.vn/tc/launchapp?-attach=true&-s=226TCSession&-o=";
				TC_write_syslog("before rich_client_link: %s", md.RACLink.c_str());
				if (tc_strlen(uid) > 0) {
					md.AWCLink.append(uid);
					md.RACLink.append(uid);
				}

				for (int iPrt = 0; iPrt < iTgt; iPrt++){
					char* pcName = NULL;
					int tmpCount = 0;
					vector<string> row;
					Teamcenter::scoped_smptr<char*> strTypes;

					CHECK_ITK(iRetCode, WSOM_ask_id_string(ptTgt[iPrt], &pcName));
					CHECK_ITK(iRetCode, AOM_ask_displayable_values(ptTgt[iPrt], "object_type", &tmpCount, &strTypes));

					row.push_back(pcName);
					row.push_back(strTypes.getString()[0]);
					md.TargetData.push_back(row);

					SAFE_MEM_free(pcName);
				}

				mailBody = genMailBody(md);
			}

			//SAFE_MEM_free(pcDesc);
			SAFE_MEM_free(pcSynopsis);
			SAFE_MEM_free(pcItemID);
			SAFE_MEM_free(pcObjType);
			SAFE_MEM_free(process_desc);
			SAFE_MEM_free(prcName);
		}
	}

	if (tc_strcmp(&mailBody.str()[0], "") != NULL)
	{
		sendEmail(pcMailSubject, mailBody.str(), szMailList, isDebug);
	}
	return iRetCode;
}

/// <summary>
/// 
/// </summary>
/// <param name="md"></param>
/// <returns></returns>
stringstream genMailBody(MailData md)
{
	stringstream mailBody;

	//Over view
	mailBody << "<html><meta charset='UTF-8'>";
	mailBody << genHtmlTitle(&md.TopSectionTitle[0]);
	mailBody << "<table class=MsoNormalTable border=0 cellspacing=3 cellpadding=0>";

	for (pair<string, string> info : md.TopSectionInfo) {
		mailBody << "<tr>" << genHtmlPropertyName(&info.first[0]) <<
			genHtmlPropertyValue(&info.second[0]) << "</tr>";
	}

	mailBody << "</table>";

	//Select the preferred
	mailBody << "<p><span>&nbsp;</span></p>";
	mailBody << genHtmlTitle(&md.LinkSectionTitle[0]);
	mailBody << "<a href='";
	mailBody << md.RACLink.c_str();
	mailBody << "'>Rich Client</a></p></br>";
	mailBody << "<a href='";
	mailBody << md.AWCLink.c_str();
	mailBody << "'>Active Workspace</a></p></br>";

	//Target
	mailBody << "<p><span>&nbsp;</span></p>";
	mailBody << genHtmlTitle(&md.TargetSectionTitle[0]);
	mailBody << "<table class=MsoNormalTable border=1 cellspacing=0 cellpadding=0 width='100%' style='width:100.0%;border-collapse:collapse;border:none'>";
	mailBody << "<tr>" << genHtmlTableHeader("Name") << genHtmlTableHeader("Type") << "</tr>";

	for (vector<string> row : md.TargetData) {
		mailBody << "<tr>" << genHtmlTableBody(&row[0][0]) <<
			genHtmlTableBody(&row[1][0]) << "</tr>";
	}

	mailBody << "</table>";
	mailBody << "<p><span>&nbsp;</span></p>";
	mailBody << genHtmlNote(&md.Footer[0]);
	mailBody << "</html>";

	return mailBody;
}