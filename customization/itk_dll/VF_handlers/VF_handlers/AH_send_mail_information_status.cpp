#include "Vinfast_Custom.h"

extern int AH_send_mail_information_status(EPM_action_message_t msg)
{
	int	iRetCode = ITK_ok;
	int	iNumArgs = 0;
	tag_t tRootTask = NULLTAG;
	char* pcProcessName = "";
	char* MailFile = "";
	char* processName = NULL;
	bool status_Reword = false;
	bool status_Complete = false;
	std::string lstMail = "";
	std::stringstream mailBody;
	std::vector<string> szMailList;
	string subject = "";
	bool statusProcess = false;
	string iDDE = "";
	std::string lstCheckECN = "";

	/*getting root task*/
	CHECK_ITK(iRetCode, EPM_ask_root_task(msg.task, &tRootTask));


	if (tRootTask != 0)
	{
		int	iAllTasks = 0;
		int	iTgt = 0;
		tag_t* ptTgt = NULL;
		tag_t* ptAllTaskTags = NULL;
		tag_t tInitiator = NULL;
		string initiatorMail;
		string nameModuleGroup = "";
		std::vector<string> lstDDE;
		Teamcenter::scoped_smptr<char*> vehicleGroup;
		char* moduleGroup = NULL;
		int tmpCount = 0;		
		int iCountRe = 0;
		char* ecn_name = "";
		tag_t  tag_ecn = NULLTAG;
		tag_t  implement_rela_type = NULLTAG;
		tag_t * ptSecondaryObjRes = NULLTAG;

		CHECK_ITK(iRetCode, EPM_ask_sub_tasks(tRootTask, &iAllTasks, &ptAllTaskTags));
		CHECK_ITK(iRetCode, EPM_ask_attachments(tRootTask, EPM_target_attachment, &iTgt, &ptTgt));
		CHECK_ITK(iRetCode, AOM_ask_value_tag(tRootTask, "fnd0WorkflowInitiator", &tInitiator));
		CHECK_ITK(iRetCode, AOM_ask_value_string(msg.task, "object_name", &processName));
		CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[0], "current_name", &ecn_name));
		string rev = ecn_name;
		string rev_id = ecn_name;
		rev.erase(rev.end() - 3, rev.end());
		rev_id.erase(rev_id.begin(), rev_id.end() - 2);
		CHECK_ITK(iRetCode, ITEM_find_rev(rev.c_str(), rev_id.c_str(), &tag_ecn));
		CHECK_ITK(iRetCode, GRM_find_relation_type("CMImplements", &implement_rela_type));
		CHECK_ITK(iRetCode, GRM_list_secondary_objects_only(tag_ecn, implement_rela_type, &iCountRe, &ptSecondaryObjRes))
		CHECK_ITK(iRetCode, AOM_ask_value_string(ptSecondaryObjRes[0], "vf6_module_group_comp", &moduleGroup));
		CHECK_ITK(iRetCode, AOM_ask_displayable_values(tag_ecn, "vf6_vehicle_group", &tmpCount, &vehicleGroup));
		if (strcmp(processName, "DDE Rework") == 0 )
		{
			lstMail.append(toLower(vehicleGroup.getString()[0])).append("_").append(toLower(moduleGroup)).append("_reword");
			MailFile = &lstMail[0];
			status_Reword = true;
		}
		if (strcmp(processName, "Check DDE Status") == 0) {
			
			lstMail.append(toLower(vehicleGroup.getString()[0])).append("_").append(toLower(moduleGroup)).append("_completed");
			MailFile = &lstMail[0];

			for (int i = 0; i < iTgt; i++)
			{
				tag_t implementBy_rela_type = NULLTAG;
				int iCountSecObjs = 0;
				tag_t* ptSecondaryObjs = NULL;
				char* dde_name = NULL;
				char* dde_status = NULL;	
				CHECK_ITK(iRetCode, GRM_find_relation_type("Vf6_ecn_data_exchange", &implementBy_rela_type));				
				CHECK_ITK(iRetCode, GRM_list_secondary_objects_only(tag_ecn, implementBy_rela_type, &iCountSecObjs, &ptSecondaryObjs));
				for (int isecIny = 0; isecIny < iCountSecObjs; isecIny++)
				{
					CHECK_ITK(iRetCode, AOM_ask_value_string(ptSecondaryObjs[isecIny], "current_id", &dde_name));
					CHECK_ITK(iRetCode, AOM_ask_value_string(ptSecondaryObjs[isecIny], "vf4_status", &dde_status));					
					if (tc_strcmp(dde_status,"COMPLETED")==0) {
						if (tc_strstr(&lstCheckECN[0], dde_name) == false)
						{
							lstCheckECN.append(dde_name);
							iDDE.append(dde_name);
							iDDE.append(isecIny == iCountSecObjs - 1 ? "" : ";");
						}						
						status_Complete = true;
						
					}
					else {
						status_Complete = false;
						MailFile = "";
					}
				}
				SAFE_MEM_free(ptSecondaryObjs);					
			}		
		}

		//binhtt edit get mail form AddressList
		if (tc_strcmp(MailFile, "") != 0)
		{
			int count = 0;
			char** member = NULL;
			tag_t a_list = NULLTAG;
			CHECK_ITK(iRetCode, MAIL_find_alias_list2(MailFile, &a_list));
			if (a_list != NULLTAG) {
				CHECK_ITK(iRetCode, MAIL_ask_alias_list_members(a_list, &count, &member));
				for (int iCnt = 0; iCnt < count; iCnt++) {
					if (std::find(szMailList.begin(), szMailList.end(), member[iCnt]) == szMailList.end())
					{
						szMailList.push_back(member[iCnt]);						
					}
				}
			}
			else {
				statusProcess = true;
				status_Reword = false;
				status_Complete = false;
				szMailList.push_back("v.chungdv8@vinfast.vn");
			}			
		}

		if (status_Reword == true)
		{
			mailBody << "<html><meta charset='UTF-8'>";
			mailBody << genHtmlTitle("Hello User");
			mailBody << genHtmlTitle("Your transfer requested FAILED : ");
			mailBody << "<p><span>&nbsp;</span></p>";
			mailBody << genHtmlTitle("Target:");
			mailBody << "<table class=MsoNormalTable border=1 cellspacing=0 cellpadding=0 width='100%' style='width:100.0%;border-collapse:collapse;border:none'><tr>";
			mailBody << "<tr>" << genHtmlTableHeader("ID") << "</tr>";
			for (int iPrt = 0; iPrt < iTgt; iPrt++)
			{
				char* itemID = NULL;
				CHECK_ITK(iRetCode, WSOM_ask_id_string(ptTgt[iPrt], &itemID));
				if(tc_strstr(itemID, "ECN") == false)
					mailBody << "<tr>" << genHtmlTableBody(itemID) << "</tr>";				
				SAFE_MEM_free(itemID);
			}
			mailBody << "</table>";
			mailBody << "<p><span>&nbsp;</span></p>";
			mailBody << genHtmlNote("This email was sent from Vinfast IT-PLM Team.");
			mailBody << "</html>";
			subject.append("Your transfer status.");
			status_Reword = false;
		}
		if (status_Complete == true) {
			std::string body = "";
			string ecn_name_id = "";
			string dde_name_id = "";
			body.append("<b>Dear Supplier,</b>").append(" <p></p>").append("Vinfast has send the below packge for you to dowload.");
			//Over view
			mailBody << "<html><meta charset='UTF-8'>";
			mailBody << genHtmlTitle(&body[0]);
			mailBody << "<p></p>";
			mailBody << "<table class=MsoNormalTable border=1 cellspacing=0 cellpadding=0 width='100%' style='width:100.0%;border-collapse:collapse;border:none'><tr>";
			mailBody << "<tr>" << genHtmlTableHeader("ECN Name") << genHtmlTableHeader("DDE Name") <<  "</tr>";			
			subject.append(ecn_name).append(" successfully send to supplier.");
			mailBody << "<tr>" << genHtmlTableBody("ECN Name") << genHtmlTableBody(ecn_name) << "</tr>";
			mailBody << "<tr>" << genHtmlTableBody("DDE Name") << genHtmlTableBody(&iDDE[0]) << "</tr>";
			mailBody << "<tr>" << genHtmlTableBody("File Size") << genHtmlTableBody("23KB") << "</tr>";
			mailBody << "<tr>" << genHtmlTableBody("Suplier Name") << genHtmlTableBody("IT_TEST_TRUONG_VENDOR") << "</tr>";
			mailBody << "<tr>" << genHtmlTableBody("Suplier Code") << genHtmlTableBody("TEST001IT") << "</tr>";
			mailBody << "<tr>" << genHtmlTableBody("Suplier Email") << genHtmlTableBody("v.trutruongdd3@vinfast.vn") << "</tr>";
			mailBody << "<tr>" << genHtmlTableBody("Sender Name") << genHtmlTableBody("Đoàn Văn Chung 8(admin.chungdv8)") << "</tr>";
			mailBody << "<tr>" << genHtmlTableBody("Sender Email") << genHtmlTableBody("v.chungdv8@vinfast.vn") << "</tr>";
			mailBody << "</table>";
			mailBody << "<p><span>&nbsp;</span></p>";
			mailBody << genHtmlNote("This email was sent from Vinfast IT-PLM Team.");
			mailBody << "</html>";	
			status_Complete = false;
			SAFE_MEM_free(processName);
		}
		if(statusProcess == true) {
			statusProcess = false;
			subject.append("Not found AddressList");		
			mailBody << "<html><meta charset='UTF-8'>";
			mailBody << genHtmlTitle("Not Found AddressList :") << genHtmlTableBody(&lstMail[0]);
			mailBody << "</html>";	
		}		
		SAFE_MEM_free(ecn_name);
		SAFE_MEM_free(ptTgt);
		SAFE_MEM_free(ptAllTaskTags);	
		lstCheckECN.clear();
	}
	if(szMailList.size() != 0)
		sendEmail(&subject[0], mailBody.str(), szMailList, false);
	TC_write_syslog("\n [VF] LEAVE %s", __FUNCTION__);
	return iRetCode;
}