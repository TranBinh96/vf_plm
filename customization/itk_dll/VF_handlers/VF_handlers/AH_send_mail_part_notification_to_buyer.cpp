#include "Vinfast_Custom.h"
#define Error (EMH_USER_error_base +284)

extern int AH_send_mail_part_notification_to_buyer(EPM_action_message_t msg)
{   
    int	iRetCode = ITK_ok;
    int	iNumArgs = 0;
    tag_t tRootTask = NULLTAG;
    char* pcPrimaryObjectType = "";
    char* pcTargetObjectType = "";
    char* pcSubject = "";
    char* pcMainContent = NULL;
    char* pcRelation = NULL;
    std::vector<string> szMailList;
    std::vector<string> target_list;
    string lstMailUser = "";
    bool  status_mail = false;
    std::stringstream mailBody;
    iNumArgs = TC_number_of_arguments(msg.arguments);

    if (iNumArgs > 0)
    {
        char* pcFlag = NULL;
        char* pcValue = NULL;
        for (int i = 0; i < iNumArgs; i++)
        {
            CHECK_ITK(iRetCode, ITK_ask_argument_named_value(TC_next_argument(msg.arguments), &pcFlag, &pcValue));
            if (iRetCode == ITK_ok)
            {
                if (tc_strcasecmp(pcFlag, "primary_object_type") == 0 && pcValue != NULL)
                {
                    pcPrimaryObjectType = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
                    tc_strcpy(pcPrimaryObjectType, pcValue);
                }
                else if (tc_strcmp(pcFlag, "target_object_type") == 0)
                {
                    pcTargetObjectType = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
                    tc_strcpy(pcTargetObjectType, pcValue);
                }
                else if (tc_strcmp(pcFlag, "relation") == 0) {
                    pcRelation = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
                    tc_strcpy(pcRelation, pcValue);
                }
                else if (tc_strcmp(pcFlag, "subject") == 0) {
                    pcSubject = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
                    tc_strcpy(pcSubject, pcValue);
                }
                else if (tc_strcmp(pcFlag, "main_content") == 0) {
                    pcMainContent = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
                    tc_strcpy(pcMainContent, pcValue);
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
        int	iTgt = 0;
        Teamcenter::scoped_smptr<tag_t> ptTgt;
        std::string  lstPart ="";      

        CHECK_ITK(iRetCode, EPM_ask_attachments(tRootTask, EPM_target_attachment, &iTgt, &ptTgt));
        //Looping the targets
        for (int iLoopTarget = 0; iLoopTarget < iTgt; iLoopTarget++)
        {
            char * primary_object_type= NULL; 
            CHECK_ITK(iRetCode, WSOM_ask_object_type2(ptTgt[iLoopTarget], &primary_object_type));
            
            if(tc_strcmp(primary_object_type,pcPrimaryObjectType) == 0  || tc_strcmp(pcPrimaryObjectType, "") == 0)
            {
                tag_t tPraposal =NULLTAG;                
                int iCountSecObjs =0;
                tag_t * ptSecondaryObjs = NULLTAG;
                string  buyer = "";
                CHECK_ITK(iRetCode, GRM_find_relation_type(pcRelation, &tPraposal));
                CHECK_ITK(iRetCode, GRM_list_secondary_objects_only(ptTgt[0], tPraposal, &iCountSecObjs, &ptSecondaryObjs));
                for (int isecIny = 0; isecIny < iCountSecObjs; isecIny++)
                {
                    char * target_object_type= NULL; 
                    CHECK_ITK(iRetCode, AOM_ask_value_string(ptSecondaryObjs[0], "object_type", &target_object_type));
                    string object_type = target_object_type;
                    if(object_type.find(target_object_type) != std::string::npos)
                    {
                        char * part_name =NULL;
                        char * part_id =NULL;
                        string data = "";                       
                        tag_t tOwningUser = NULLTAG;
                        char* pcUserMail = NULL;
                        char* person_name = NULL;
                        tag_t tPerson = NULL_TAG;                        
                        CHECK_ITK(iRetCode, AOM_ask_value_string(ptSecondaryObjs[isecIny], "current_id", &part_id));
                        CHECK_ITK(iRetCode, AOM_ask_value_string(ptSecondaryObjs[isecIny], "current_name", &part_name));
                        int iCount = 0;
                        tag_t tQuery = NULLTAG;
                        const char* queryName = "Source Part";
                        Teamcenter::scoped_smptr<tag_t> tQryResults;
                        CHECK_ITK(iRetCode, QRY_find2(queryName, &tQuery));
                        if (tQuery == NULLTAG)
                        {
                            TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
                            return ITK_ok;
                        }
                        char* queryEntries[] = { "VF Part Number" };
                        char** queryValues = (char**)MEM_alloc(sizeof(char*) * 1);
                        queryValues[0] = (char*)MEM_alloc(sizeof(char) * ((int)tc_strlen(part_id) + 1));
                        queryValues[0] = tc_strcpy(queryValues[0], part_id);
                        CHECK_ITK(iRetCode, QRY_execute(tQuery, 1, queryEntries, queryValues, &iCount, &tQryResults));
                        if (iCount != 0)
                        {   
                            status_mail = true;
                            for (int iPart = 0; iPart < iCount; ++iPart)
                            {
                                bool check = false;
                                tag_t source_part = tQryResults[iPart];
                                /*CHECK_ITK(iRetCode, AOM_ask_value_string(source_part, "object_string", &part_id));*/
                                CHECK_ITK(iRetCode, AOM_ask_value_tag(source_part, "owning_user", &tOwningUser));
                                CHECK_ITK(iRetCode, SA_ask_user_person(tOwningUser, &tPerson));
                                CHECK_ITK(iRetCode, SA_ask_person_name2(tPerson, &person_name));
                                CHECK_ITK(iRetCode, EPM_get_user_email_addr(tOwningUser, &pcUserMail));
                                string mail = "";

                                if (tc_strcmp(buyer.c_str(), "") == 0)
                                    buyer.append(person_name);

                                if (szMailList.size() == 0)
                                    szMailList.push_back(pcUserMail);

                                for (int i = 0; i < szMailList.size(); i++) {
                                    mail.append(szMailList[i]).append(";");
                                }
                                if (mail.find(pcUserMail) == std::string::npos)
                                    szMailList.push_back(pcUserMail);

                                if (buyer.find(person_name) == std::string::npos)
                                    buyer.append(person_name);

                                if (iPart = iCount)
                                    continue;
                                buyer.append(",");
                            }
                            data.append(part_id).append(";").append(part_name).append(";").append(buyer);
                            target_list.push_back(data);
                            buyer = "";
                        }                       
                        SAFE_MEM_free(person_name);
                        SAFE_MEM_free(part_name);
                        SAFE_MEM_free(part_id);
                                                        
                    }
                }               
            } 
            if (status_mail == true) {
                mailBody << "<html><meta charset='UTF-8'>";
                mailBody << "<b>" << genHtmlTableHeader(pcMainContent) << "</b>";
                mailBody << "<p></p>";
                mailBody << "<table class=MsoNormalTable border=1 cellspacing=0 cellpadding=0 width='100%' style='width:100.0%;border-collapse:collapse;border:none'><tr>";
                mailBody << "<tr>" << genHtmlTableHeader("Part ID") << genHtmlTableHeader("Part Name") << genHtmlTableHeader("Buyer") << "</tr>";

                for (int i = 0; i < target_list.size(); i++) {
                    mailBody << "<tr>";
                    string target = target_list[i];
                    char* next_p;
                    char seps[] = ";";
                    char* chars_array = strtok_s(&target[0], seps, &next_p);
                    while (chars_array)
                    {
                        string name = chars_array;
                        mailBody << genHtmlTableBody(&name[0]);
                        chars_array = strtok_s(NULL, seps, &next_p);
                    }
                    mailBody << "</tr>";
                }
                mailBody << "</table>";
                mailBody << "<p><p>";
                mailBody << genHtmlNote("This email was sent from Vinfast IT-PLM Team.");
                mailBody << "</html>";
                sendEmail(pcSubject, mailBody.str(), szMailList, false);
                TC_write_syslog("\n [VF] LEAVE %s", __FUNCTION__);
            }
            else {
                char ErrorMessage[1024] = "";
                sprintf(ErrorMessage, "Cannot find source part of target part !!!");
                CHECK_ITK(iRetCode, EMH_store_error_s1(EMH_severity_user_error, Error, ErrorMessage));
            }           
        }
    }
    return iRetCode;   
}
