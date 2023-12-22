/*******************************************************************************
File         : AH_send_mail_notify.cpp
Description  : To send mail notification
Input        : None
Output       : None
Author       : Binhtt
Revision History :
Date            Revision    Who              Description
-----------------------------------------------------------------------------
Aug, 27 2023    1.0         Binhtt			Initial Creation
Oct, 24 2023	2.0			Binhtt			Adding Acar RecipientList
Nov, 11 2023	2.1			ThienBQ2		Fix blank mail when no target objects; Add rich client link
*******************************************************************************/
#include "Vinfast_Custom.h"

extern int AH_send_mail_notify(EPM_action_message_t msg)
{
	int			iRetCode			= ITK_ok;
	int			iNumArgs			= 0;
	tag_t		tRootTask			= NULLTAG;
	char		*pcMailSubject		= NULL;
	char		*pcReciepent		= NULL;
	char		*pcFlag				= NULL;
	char		*pcValue			= NULL;
	char		*pcReqType			= "";
	boolean		isDebug				= false;
	char		*current_task_name	= NULL;
	char		*object_type		= NULL;
	char		*sub_object_type	= NULL;
	char		*process_desc		= NULL;
	char		*process_job_name	= NULL;
	char		*date_string		= NULL;
	int			count_task			= NULL;
	int			signoff_count		= NULL;
	tag_t		*subtasks			= NULLTAG;
	tag_t		*attachment			= NULLTAG;
	date_t		task_due_date;
	tag_t		_performer			= NULLTAG;
	vector<string>		ReqType;
	std::stringstream	mailBody;
	std::vector<string> szMailList;
	std::vector<tag_t>  lstPart;
	iNumArgs = TC_number_of_arguments(msg.arguments);
	

	if (iNumArgs >= 1)
	{
		for (int i = 0; i < iNumArgs; i++)
		{
			/*getting arguments*/
			iRetCode = ITK_ask_argument_named_value(TC_next_argument(msg.arguments), &pcFlag, &pcValue);
			if (iRetCode == ITK_ok)
			{
				if (tc_strcasecmp(pcFlag, "subject") == 0 && pcValue != NULL)
				{
					pcMailSubject = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
					tc_strcpy(pcMailSubject, pcValue);
				}
				if (tc_strcasecmp(pcFlag, "recipient") == 0 && pcValue != NULL)
				{
					pcReciepent = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
					tc_strcpy(pcReciepent, pcValue);
					
				}
				if (tc_strcasecmp(pcFlag, "object_type") == 0 && pcValue != NULL)
				{
					pcReqType = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
					tc_strcpy(pcReqType, pcValue);					
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
	

	/*getting root task*/
	CHECK_ITK(iRetCode, EPM_ask_root_task(msg.task, &tRootTask));
	if (tRootTask != 0)
	{	
		int		iTgt				= 0;
		tag_t*	ptTgt				= NULL;
		tag_t	current_job			= NULLTAG;
		char	*instruction		= NULL;
		char	*uid				= NULL;
		char	*usr_email			= NULL;
		char	*next_p				= NULL;
		/*getting mail id of all participants from review task*/
		CHECK_ITK(iRetCode, WSOM_ask_object_type2(msg.task, &object_type));
		CHECK_ITK(iRetCode, EPM_ask_attachments(tRootTask, EPM_target_attachment, &iTgt, &ptTgt));
		CHECK_ITK(iRetCode, EPM_ask_job(msg.task, &current_job));
		CHECK_ITK(iRetCode, AOM_ask_value_string(msg.task, "current_desc", &instruction));
		string instructionMailContent(instruction);		
		ITK__convert_tag_to_uid(msg.task, &uid);
		//instructionMailContent = replace_str(instructionMailContent, "\n", "</br>");
		CHECK_ITK(iRetCode, AOM_ask_value_string(current_job, "current_desc", &process_desc));
		CHECK_ITK(iRetCode, AOM_ask_value_string(current_job, "current_name", &process_job_name));
		if(tc_strcmp(object_type, "EPMDoTask") == 0 || tc_strcmp(object_type, "EPMConditionTask") == 0)
		{			
			//get object_name, due_date, performer
			CHECK_ITK(iRetCode, AOM_ask_value_string(msg.task, "object_name", &current_task_name));
			CHECK_ITK(iRetCode, AOM_ask_value_date(msg.task, "due_date", &task_due_date));
			CHECK_ITK(iRetCode, DATE_date_to_string(task_due_date, "%Y-%m-%d %H:%M:%S", &date_string));
			CHECK_ITK(iRetCode, AOM_ask_value_tag(msg.task, "fnd0Performer", &_performer));
			CHECK_ITK(iRetCode, EPM_get_user_email_addr(_performer, &usr_email));
			//get process name, process description
		}
		else if(tc_strcmp(object_type, "EPMPerformSignoffTask") == 0)
		{
			tag_t parent_task = NULLTAG;
			CHECK_ITK(iRetCode, EPM_ask_parent_task(msg.task, &parent_task));
			CHECK_ITK(iRetCode, EPM_ask_sub_tasks(parent_task, &count_task, &subtasks));
			for(int i = 0; i < count_task; i++)
			{
				CHECK_ITK(iRetCode, WSOM_ask_object_type2(subtasks[i], &sub_object_type));
				if(tc_strcmp(sub_object_type, "EPMPerformSignoffTask") == 0)
				{
					CHECK_ITK(iRetCode, AOM_ask_value_string(subtasks[i], "object_name", &current_task_name));
					CHECK_ITK(iRetCode, AOM_ask_value_date(subtasks[i], "due_date", &task_due_date));
					CHECK_ITK(iRetCode, DATE_date_to_string(task_due_date, "%Y-%m-%d %H:%M:%S", &date_string));					
				}
				if(tc_strcmp(sub_object_type, "EPMSelectSignoffTask") == 0)
				{
					CHECK_ITK(iRetCode, EPM_ask_attachments(subtasks[i], EPM_signoff_attachment, &signoff_count, &attachment));
					for(int j=0;j < signoff_count; j++)
					{
						tag_t tUser = NULLTAG;
						char *user_email = NULL;
						CHECK_ITK(iRetCode, AOM_ask_value_tag(attachment[j], "fnd0Performer", &tUser));
						CHECK_ITK(iRetCode, EPM_get_user_email_addr(tUser, &user_email));
						if(tc_strcmp(pcReciepent, "$REVIEWERS") == 0 && tc_strcmp(user_email,"" )!= NULL)
							szMailList.push_back(user_email);						
					}
				}
			}
		}
		else if (tc_strcmp(object_type,"EPMAcknowledgeTask") == 0 ) 
		{
			int		iAllSubTasks			= 0;
			tag_t	*ptAllSubTaskTags		= NULL;
			tag_t parent_task = NULLTAG;
			CHECK_ITK(iRetCode, EPM_ask_parent_task(msg.task, &parent_task));			
			CHECK_ITK(iRetCode,EPM_ask_sub_tasks(parent_task,&iAllSubTasks,&ptAllSubTaskTags));
			for(int iLoopAllSubTasks =0 ; iLoopAllSubTasks < iAllSubTasks; iLoopAllSubTasks++)
			{	
				char *pcSubTaskTypeName		= NULL;
				CHECK_ITK(iRetCode,AOM_ask_value_string(ptAllSubTaskTags[iLoopAllSubTasks], "task_type", &pcSubTaskTypeName));
				if(tc_strcmp(pcSubTaskTypeName,"EPMSelectSignoffTask")==0)
				{
					int		iNumAttachs				= 0;
					tag_t	*ptAttachsTagList		= NULL;
					//get signoff attachments
					CHECK_ITK(iRetCode,EPM_ask_attachments(ptAllSubTaskTags[iLoopAllSubTasks], EPM_signoff_attachment, &iNumAttachs, &ptAttachsTagList));
					for(int iUsers = 0; iUsers < iNumAttachs; iUsers++)
					{
						tag_t		tUser				= NULLTAG;
						char		*pcUserMail			= NULL;
						CHECK_ITK(iRetCode,AOM_ask_value_tag(ptAttachsTagList[iUsers],"fnd0Performer",&tUser));
						CHECK_ITK(iRetCode,EPM_get_user_email_addr(tUser, &pcUserMail));
						//fprintf(fMailAddress,"%s\n",pcUserMail);
						if(tc_strcmp(pcReciepent, "$REVIEWERS") == 0 && tc_strcmp(pcUserMail,"" )!= NULL)
							szMailList.push_back(pcUserMail);
					}
					SAFE_MEM_free(ptAttachsTagList);
				}
			}
			SAFE_MEM_free(ptAllSubTaskTags);
		}

		if (usr_email != NULL && tc_strcmp(pcReciepent, "$RESPONSIBLE_PARTY") == 0) 
			szMailList.push_back(usr_email);
		
		
		
		//check object_type from arguments

		if (tc_strcmp(pcReqType, "") == 0) {
			for (int iInd = 0; iInd < iTgt; iInd++)
			{				
				lstPart.push_back(ptTgt[iInd]);					
			}
		}
		else {
			char* chars_array = strtok_s(pcReqType, ",", &next_p);
			while (chars_array)
			{
				if (std::find(szMailList.begin(), szMailList.end(), chars_array) == szMailList.end())
					ReqType.push_back(chars_array);
				chars_array = strtok_s(NULL, ",", &next_p);
			}
			for (int iInd = 0; iInd < iTgt; iInd++)
			{
				char* pcObjType = NULL;
				CHECK_ITK(iRetCode, WSOM_ask_object_type2(ptTgt[iInd], &pcObjType));
				for (int iCnt = 0; iCnt < ReqType.size(); iCnt++)
				{
					if (tc_strcmp(pcObjType, ReqType[iCnt].c_str()) == 0)
					{
						lstPart.push_back(ptTgt[iInd]);
					}
				}
			}
		}

		//prepare email to sending
		char *awcUrl = NULL;
		string task_Name =  current_task_name ? current_task_name : "";
		string job_Name =  process_job_name ? process_job_name : ""; 
		string date =  date_string ? date_string : ""; 
		CHECK_ITK(iRetCode, PREF_ask_char_value("ActiveWorkspaceHosting.URL", 0, &awcUrl));
		std::string awc_link = "https://tcweb.vinfast.vn/#/com.siemens.splm.clientfx.tcui.xrt.showObject?uid=";
		std::string rich_client_link = "http://tcweb.vinfast.vn/tc/launchapp?-attach=true&-s=226TCSession&-o=";
		if (tc_strlen(awcUrl) > 2)
		{
			awc_link = awcUrl;
			if (awc_link.at(awc_link.size() - 1) != '/') awc_link.append("/");
			awc_link.append("#/com.siemens.splm.clientfx.tcui.xrt.showObject?uid=");
			awc_link.append(uid);
			rich_client_link.append(uid);
		}
		mailBody << "<html><meta charset='UTF - 8'>";
		mailBody << "<div style='margin-bottom:2.25pt'><p class=MsoNormal><b><span style='font-family:'Arial',sans-serif;color:#448DA6'>Overview:<o:p></o:p></span></b></p></div>";
		mailBody << "<table class=MsoNormalTable border=0 cellspacing=3 cellpadding=0>";
		mailBody << "<tr><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif;color:gray'>Current Task: <o:p></o:p></span></p></td><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif'>"+task_Name +" <o:p></o:p></span></p></td></tr>";
		mailBody << "<tr><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif;color:gray'>Process Name: <o:p></o:p></span></p></td><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif'>"+job_Name +" <o:p></o:p></span></p></td></tr>";
		mailBody << "<tr><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif;color:gray'>Due Date: <o:p></o:p></span></p></td><td valign=top style='padding:.75pt .75pt .75pt .75pt'><p class=MsoNormal><span style='font-family:'Arial',sans-serif'>"+date+" <o:p></o:p></span></p></td></tr>";
		mailBody << "</table>";
			
		mailBody << "<p class=MsoNormal><span style='font-family:'Arial',sans-serif'><o:p>&nbsp;</o:p></span></p>";
		mailBody << "<div style='margin-bottom:2.25pt'><p class=MsoNormal><b><span style='font-family:'Arial',sans-serif;color:#448DA6'>Select the preferred client to view the task:<o:p></o:p></span></b></p></div>";
		mailBody << "<a href='";
		mailBody << rich_client_link.c_str();
		mailBody << "'>Rich Client</a></p></br>";
		mailBody << "<a href='";
		mailBody << awc_link.c_str();
		mailBody << "'>Active Workspace</a></p>";
		//Target
		mailBody << "<p class=MsoNormal><span style='font-family:'Arial',sans-serif'><o:p>&nbsp;</o:p></span></p>";
		mailBody << "<div style='margin-bottom:2.25pt'><p class=MsoNormal><b><span style='font-family:'Arial',sans-serif;color:#448DA6'>Target:<o:p></o:p></span></b></p></div>";
		mailBody << "<table class=MsoNormalTable border=1 cellspacing=0 cellpadding=0 width='100%' style='width:100.0%;border-collapse:collapse;border:none'><tr>";
		mailBody << "<td style='border:solid gray 1.0pt;background:#F0F0F0;padding:1.5pt 1.5pt 1.5pt 1.5pt'><p class=MsoNormal align=center style='text-align:center'><b><span style='font-family:'Arial',sans-serif;color:gray'>Name<o:p></o:p></span></b></p></td>";
		mailBody << "<td style='border:solid gray 1.0pt;border-left:none;background:#F0F0F0;padding:1.5pt 1.5pt 1.5pt 1.5pt'><p class=MsoNormal align=center style='text-align:center'><b><span style='font-family:'Arial',sans-serif;color:gray'>Type<o:p></o:p></span></b></p></td></tr>";

		for (tag_t part : lstPart)
		{
			int tmpCount = 0;
			char* pcName    	= NULL;
			char* pcTgtType 	= NULL;
			Teamcenter::scoped_smptr<char*> strTypes;
			CHECK_ITK(iRetCode, AOM_ask_value_string(part, "object_string", &pcName));
			CHECK_ITK(iRetCode, AOM_ask_displayable_values(part, "object_type", &tmpCount, &strTypes));
			mailBody << "<tr><td style='border:solid gray 1.0pt;border-top:none;padding:1.5pt 1.5pt 1.5pt 1.5pt'><p class=MsoNormal align=center style='text-align:center'><span style='font-family:'Arial',sans-serif'>" << pcName << "<o:p></o:p></span></p></td>";
			mailBody << "<td style='border-top:none;border-left:none;border-bottom:solid gray 1.0pt;border-right:solid gray 1.0pt;padding:1.5pt 1.5pt 1.5pt 1.5pt'><p class=MsoNormal align=center style='text-align:center'><span style='font-family:'Arial',sans-serif'>" << strTypes.getString()[0] << "<o:p></o:p></span></p></td></tr>";
			SAFE_MEM_free(pcName);
			SAFE_MEM_free(pcTgtType);
		}
		mailBody << "</table>";
		mailBody << "<p class=MsoNormal><span style='font-family:'Arial',sans-serif'><o:p>&nbsp;</o:p></span></p>";
		mailBody << "<div><p class=MsoNormal><b><span style='font-family:'Arial',sans-serif;color:gray'>This email was sent from Teamcenter.<o:p></o:p></span></b></p></div>";
		mailBody << "</html>";
		sendEmail(pcMailSubject, mailBody.str(), szMailList, isDebug);

		SAFE_MEM_free(subtasks);
		SAFE_MEM_free(attachment);
		SAFE_MEM_free(ptTgt);
		TC_write_syslog("[vf] LEAVE %s", __FUNCTION__);
		return iRetCode;		
	}
}