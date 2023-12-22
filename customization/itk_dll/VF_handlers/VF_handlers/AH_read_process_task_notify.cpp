#include "Vinfast_Custom.h"

extern int AH_read_process_task_notify(EPM_action_message_t msg)
{
	int iRetCode = ITK_ok;
	int iNumArgs = 0;
	tag_t tRootTask = NULLTAG;
	char* pcMailSubject = NULL;
	char* pcTaskName = NULL;
	char* pcProperty = NULL;
	char* pcFlag = NULL;
	char* pcValue = NULL;
	std::vector<tag_t> lstitem;

	boolean isDebug = false;
	iNumArgs = TC_number_of_arguments(msg.arguments);
	std::stringstream mailBody;
	std::vector<string> szMailList;

	if (iNumArgs >= 1)
	{
		for(int i = 0; i < iNumArgs; i++)
		{
			/*getting arguments*/
			iRetCode = ITK_ask_argument_named_value(TC_next_argument(msg.arguments), &pcFlag, &pcValue );
			if ( iRetCode == ITK_ok )
			{
				if (tc_strcasecmp(pcFlag, "task_name") == 0 && pcValue != NULL)
				{
					pcTaskName = (char*) MEM_alloc(( (int)tc_strlen(pcValue) + 1) * sizeof(char));
					tc_strcpy( pcTaskName, pcValue);
				}
				if (tc_strcasecmp(pcFlag, "property") == 0 && pcValue != NULL)
				{
					pcProperty = (char*) MEM_alloc(( (int)tc_strlen(pcValue) + 1) * sizeof(char));
					tc_strcpy( pcProperty, pcValue);
				}
				if (tc_strcasecmp(pcFlag, "subject") == 0 && pcValue != NULL)
				{
					pcMailSubject = (char*) MEM_alloc(( (int)tc_strlen(pcValue) + 1) * sizeof(char));
					tc_strcpy( pcMailSubject, pcValue);
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
	if (tc_strcmp(pcTaskName, "") == 0 || tc_strcmp(pcProperty, "") == 0)
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
		tag_t* ptAllTaskTags = NULL;
		string szRejMailList = "";
		char* process_desc = NULL;
		char* uid = "";
		bool status = false;

		CHECK_ITK(iRetCode, EPM_ask_sub_tasks(tRootTask, &iAllTasks, &ptAllTaskTags));
		CHECK_ITK(iRetCode, EPM_ask_attachments(tRootTask, EPM_target_attachment, &iTgt, &ptTgt));
		/*getting mail id of all participants from review task*/
		
		for (int iLoopAllTasks = 0; iLoopAllTasks < iAllTasks; iLoopAllTasks++)
		{
			char* pcObjectName = NULL;

			CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks], "object_string", &pcObjectName));
			

			if (tc_strcmp(pcObjectName, pcTaskName) == 0)
			{
				char* pcValueDesc = "";
				char* uid = "";
				CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks], pcProperty, &pcValueDesc));
				ITK__convert_tag_to_uid(ptAllTaskTags[iLoopAllTasks], &uid);
				if (tc_strcmp(pcValueDesc, "") != 0)
				{
					int count = 0;
					char** member = NULL;
					tag_t a_list = NULLTAG;
					CHECK_ITK(iRetCode, MAIL_find_alias_list2(pcValueDesc, &a_list));
					if (a_list != NULLTAG) {
						CHECK_ITK(iRetCode, MAIL_ask_alias_list_members(a_list, &count, &member));
						for (int iCnt = 0; iCnt < count; iCnt++) {
							if (std::find(szMailList.begin(), szMailList.end(), member[iCnt]) == szMailList.end())
							{
								szMailList.push_back(member[iCnt]);

							}
						}
					}
				}
			}
		}
		for (int iInd = 0; iInd < iTgt; iInd++)
		{
			char* pcObjType = "";
			CHECK_ITK(iRetCode, WSOM_ask_object_type2(ptTgt[iInd], &pcObjType));
			if (tc_strcmp(pcObjType, "MEOPRevision") ==0) {
				lstitem.push_back(ptTgt[iInd]);
				char* pcObjectName = NULL;
				CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[iInd], "object_desc", &pcObjectName));
				if (tc_strcmp(pcObjectName, "") != 0 && szMailList.size() == 0)
				{
					int count = 0;
					char** member = NULL;
					tag_t a_list = NULLTAG;
					CHECK_ITK(iRetCode, MAIL_find_alias_list2(pcObjectName, &a_list));
					if (a_list != NULLTAG) {
						CHECK_ITK(iRetCode, MAIL_ask_alias_list_members(a_list, &count, &member));
						for (int iCnt = 0; iCnt < count; iCnt++) {
							if (std::find(szMailList.begin(), szMailList.end(), member[iCnt]) == szMailList.end())
							{
								szMailList.push_back(member[iCnt]);

							}
						}
					}
				}				
			}	
		}

		if (lstitem.size() > 0) {
			
			char* pcObjType = "";
			char* pcDesc = "";
			char* pcSynopsis = "";
			char* pcObjString = "";
			char* pcItemID = "";
			char* pcRevID = "";
			char* pcName = "";
			tag_t	tChangeItem = NULLTAG;
			tag_t	tClass = NULLTAG;
			tag_t	tRevClass = NULLTAG;
			string	szTgtParts = "";
			for (tag_t item : lstitem)
			{
				/*get solution items*/
				CHECK_ITK(iRetCode, ITEM_ask_item_of_rev(item, &tChangeItem));
				CHECK_ITK(iRetCode, ITEM_ask_id2(tChangeItem, &pcItemID));
				CHECK_ITK(iRetCode, ITEM_ask_rev_id2(item, &pcRevID));
				CHECK_ITK(iRetCode, AOM_ask_value_string(item, "object_name", &pcSynopsis));
				CHECK_ITK(iRetCode, AOM_ask_value_string(item, "object_desc", &pcDesc));
				CHECK_ITK(iRetCode, AOM_ask_value_string(item, "object_string", &pcObjString));
			}

			std::string awc_link = "https://tcweb.vinfast.vn/#/com.siemens.splm.clientfx.tcui.xrt.showObject?uid=";
			TC_write_syslog("before awc_link: %s", awc_link.c_str());
			std::string rich_client_link = "http://tcweb.vinfast.vn/tc/launchapp?-attach=true&-s=226TCSession&-o=";
			TC_write_syslog("before rich_client_link: %s", rich_client_link.c_str());
			awc_link.append(uid);
			rich_client_link.append(uid);

			//Over view
			mailBody << "<html><meta charset='UTF - 8'>";
			mailBody << "<div style='margin-bottom:2.25pt'><p class=MsoNormal><b><span style='font-family:'Arial',sans-serif;color:#448DA6'>Overview:<o:p></o:p></span></b></p></div>";
			mailBody << "<table class=MsoNormalTable border=0 cellspacing=3 cellpadding=0>";
			mailBody << "<tr><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif;color:gray'>Current Task: <o:p></o:p></span></p></td><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif'>Notify <o:p></o:p></span></p></td></tr>";
			mailBody << "<tr><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif;color:gray'>Process Name: <o:p></o:p></span></p></td><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif'>" << pcItemID << "/" << pcRevID << " - " << pcSynopsis << " <o:p></o:p></span></p></td></tr>";
			mailBody << "<tr><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif;color:gray'>Due Date: <o:p></o:p></span></p></td><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif'>None <o:p></o:p></span></p></td></tr>";
			mailBody << "</table>";

			//Select the preferred
			mailBody << "<p class=MsoNormal><span style='font-family:'Arial',sans-serif'><o:p>&nbsp;</o:p></span></p>";
			mailBody << "<div style='margin-bottom:2.25pt'><p class=MsoNormal><b><span style='font-family:'Arial',sans-serif;color:#448DA6'>Select the preferred client to view the task:<o:p></o:p></span></b></p></div>";
			mailBody << "<a href='";
			mailBody << rich_client_link.c_str();
			mailBody << "'>Rich Client</a></p></br>";
			mailBody << "<a href='";
			mailBody << awc_link.c_str();
			mailBody << "'>Active Workspace</a></p></br>";

			//Target
			mailBody << "<p class=MsoNormal><span style='font-family:'Arial',sans-serif'><o:p>&nbsp;</o:p></span></p>";
			mailBody << "<div style='margin-bottom:2.25pt'><p class=MsoNormal><b><span style='font-family:'Arial',sans-serif;color:#448DA6'>Target:<o:p></o:p></span></b></p></div>";
			mailBody << "<table class=MsoNormalTable border=1 cellspacing=0 cellpadding=0 width='100%' style='width:100.0%;border-collapse:collapse;border:none'><tr>";
			mailBody << "<td style='border:solid gray 1.0pt;background:#F0F0F0;padding:1.5pt 1.5pt 1.5pt 1.5pt'><p class=MsoNormal align=center style='text-align:center'><b><span style='font-family:'Arial',sans-serif;color:gray'>Name<o:p></o:p></span></b></p></td>";
			mailBody << "<td style='border:solid gray 1.0pt;border-left:none;background:#F0F0F0;padding:1.5pt 1.5pt 1.5pt 1.5pt'><p class=MsoNormal align=center style='text-align:center'><b><span style='font-family:'Arial',sans-serif;color:gray'>Type<o:p></o:p></span></b></p></td></tr>";

			for (int iPrt = 0; iPrt < iTgt; iPrt++)
			{
				char* pcName = NULL;
				char* pcTgtType = NULL;

				CHECK_ITK(iRetCode, WSOM_ask_id_string(ptTgt[iPrt], &pcName));
				CHECK_ITK(iRetCode, WSOM_ask_object_type2(ptTgt[iPrt], &pcTgtType));

				mailBody << "<tr><td style='border:solid gray 1.0pt;border-top:none;padding:1.5pt 1.5pt 1.5pt 1.5pt'><p class=MsoNormal align=center style='text-align:center'><span style='font-family:'Arial',sans-serif'>" << pcName << "<o:p></o:p></span></p></td>";
				mailBody << "<td style='border-top:none;border-left:none;border-bottom:solid gray 1.0pt;border-right:solid gray 1.0pt;padding:1.5pt 1.5pt 1.5pt 1.5pt'><p class=MsoNormal align=center style='text-align:center'><span style='font-family:'Arial',sans-serif'>" << pcTgtType << "<o:p></o:p></span></p></td></tr>";

				SAFE_MEM_free(pcName);
				SAFE_MEM_free(pcTgtType);
			}

			mailBody << "</table>";
			mailBody << "<p class=MsoNormal><span style='font-family:'Arial',sans-serif'><o:p>&nbsp;</o:p></span></p>";
			mailBody << "<div><p class=MsoNormal><b><span style='font-family:'Arial',sans-serif;color:gray'>This email was sent from Teamcenter.<o:p></o:p></span></b></p></div>";
			mailBody << "</html>";

			
		}
		
	}
	if (szMailList.size() == 0)
	{
		char ErrorMessage[2048] = "";
		sprintf_s(ErrorMessage, "Addresslist not created. Please contact system administrator");
		EMH_store_error_s1(EMH_severity_user_error, CHECK_REV_STS, ErrorMessage);
	}
	else {
		sendEmail(pcMailSubject, mailBody.str(), szMailList, isDebug);
	}
	

	

	return iRetCode;

	
}