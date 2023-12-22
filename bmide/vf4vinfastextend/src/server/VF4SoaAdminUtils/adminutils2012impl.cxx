/*
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#include <unidefs.h>
#if defined(SUN)
#include <unistd.h>
#endif

#include <adminutils2012impl.hxx>

#include <sa/am.h>
#include <base_utils/TcResultStatus.hxx>
#include <base_utils/ScopedSmPtr.hxx>
#include <base_utils/ScopedPtr.hxx>

#include <sa/user.h>
#include <epm/signoff.h>
#include <qry/qry.h>
#include <bom/bom.h>
#include <string>
#include <string.h>
#include <lov/lov.h>
#include <tccore/item.h>
#include <tccore/aom.h>
#include <tccore/uom.h>
#include <tccore/aom_prop.h>
#include <fclasses/tc_date.h>
#include <fclasses/tc_string.h>
#include <ps/ps.h>
#include <tc/emh.h>
#include <tc/emh_errors.h>
#include <epm/epm_toolkit_tc_utils.h>
#include <user_exits/epm_toolkit_utils.h>
using namespace VF4::Soa::AdminUtils::_2020_12;
using namespace Teamcenter::Soa::Server;

#define CHECK_FAIL(x)																													\
{																																\
	if (x != ITK_ok)																								\
	{																															\
		char *error_str = NULL;																								\
		EMH_ask_error_text(x, &error_str);																			\
		TC_write_syslog("ERROR: %d, ERROR MSG: %s. at Line: %d in File: %s\n", x, error_str, __LINE__, __FILE__);	\
		SAFE_SM_FREE(error_str);																								\
	}																															\
}
std::string btos(bool value);
int modify_item(tag_t tPartTag, tag_t uom_tag);
int changeBomLine(tag_t bomLine, tag_t uom_tag);
tag_t get_New_User(std::string newUserID,std::string newgrp);
bool isSignoffUserAlreadyExists(tag_t taskTag, std::string newUserid);
int getItem(std::string partNumber, std::string partType, tag_t* tPartTag);
int modifyItemOnBoms(std::string partIDString, tag_t itemTag, tag_t uom_tag);
int reassignDoTask(tag_t taskTag, std::string isDryRun, std::string oldUserid, tag_t groupmem_user_tag, AdminUtilsImpl::ReassignWFTaskOutput &outputStatus,AdminUtilsImpl::ReassignWFTaskResponse &response);
int reassignsignoffTask(tag_t taskTag, std::string isDryRun, std::string oldUserid, std::string newUserid, tag_t new_member_tag, AdminUtilsImpl::ReassignWFTaskOutput &outputStatus, AdminUtilsImpl::ReassignWFTaskResponse &response);
int setProperty(tag_t objectTag, std::string className, tag_t classId, std::string propertyName, vector<string> propValue, AdminUtilsImpl::SetPropertyResponse &response);
AdminUtilsImpl::ChangeUOMResponse AdminUtilsImpl::changeUOM ( const std::vector< ChangeUOMInput >& inputs )
{
	int ifail = ITK_ok;
	AdminUtilsImpl::ChangeUOMResponse response;

	TC_write_syslog("*********************************************************************\n");
	TC_write_syslog("***                                                               ***\n");
	TC_write_syslog("***				changeUOM Service Log                          ***\n");
	TC_write_syslog("***                                                               ***\n");
	TC_write_syslog("*********************************************************************\n");

	TC_write_syslog("Size of Input Vector : %zd\n", inputs.size());
	ITK_set_bypass(TRUE);
	if (inputs.size() > 0)
	{

		for (ChangeUOMInput uomInput : inputs)
		{
			try
			{
				ChangeUOMOutput outputStatus;
				tag_t tPartTag = NULLTAG;
				tag_t uom_tag = NULLTAG;
				std::string partNumber = uomInput.partNumber;
				std::string partType = uomInput.partType;
				std::string partUOM= uomInput.partUOM;

				outputStatus.partNumber.assign(partNumber);
				outputStatus.partType.assign(partType);

				if (partNumber.empty())
				{
					outputStatus.errorMessage.assign("Input Part Number is empty");
					response.outputs.push_back(outputStatus);
					continue;
				}
				//querying for Item
				CHECK_FAIL(getItem(partNumber, partType, &tPartTag));
				if (tPartTag == NULLTAG)
				{
					TC_write_syslog("[UOM] No Item found for given Part Number [%s] and Part Type [%s]\n", partNumber.c_str(), partType.c_str());
					outputStatus.errorMessage.assign("Item not found in Teamcenter");
					response.outputs.push_back(outputStatus);
					continue;
				}
				CHECK_FAIL(UOM_find_by_symbol(partUOM.c_str(), &uom_tag));
				if (uom_tag == NULLTAG)
				{
					TC_write_syslog("[UOM] Falied to find UOM symbol with value [%s] \n", partUOM.c_str());
					outputStatus.errorMessage.assign("UOM symbol not found in Teamcenter");
					response.outputs.push_back(outputStatus);
					continue;
				}
				ifail = modify_item(tPartTag, uom_tag);
				if (ifail!=ITK_ok)
				{
					TC_write_syslog("[UOM]ERROR: while changing UOM on BOMS\n");
					outputStatus.errorMessage.assign("ERROR: while changing UOM on BOMS");
					response.outputs.push_back(outputStatus);
					continue;
				}
			}
			catch(const IFail &e)
			{
				std::cout << e.getMessage();
				response.serviceData.addErrorStack();
				TC_write_syslog("[UOM]Error ::%s\n", e.getMessage());
			}
		}
	}

	TC_write_syslog("\n------------ [UOM] EXIT FROM changeUOM Service ------------\n");
	return response;
}

int getItem(std::string partNumber, std::string partType, tag_t* tPartTag)
{
	int ifail = ITK_ok;
	int iCount = 0;
	tag_t tQuery = NULLTAG;
	const char* queryName = "Item...";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	CHECK_FAIL(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("[UOM]ERROR: Cannot find query %s:\n", queryName);
		return ITK_ok;
	}

	char* queryEntries[] = {"Item ID", "Type"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partNumber.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partType.c_str()) + 1));

	queryValues[0] = tc_strcpy(queryValues[0], "");
	queryValues[0] = tc_strcpy(queryValues[0], partNumber.c_str());

	queryValues[1] = tc_strcpy(queryValues[1], "");
	queryValues[1] = tc_strcpy(queryValues[1], partType.c_str());

	CHECK_FAIL(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));
	if (iCount == 0)
	{
		//TC_write_syslog("[UOM] No Item found for given Part Number [%s] and Part Type [%s]\n", partNumber.c_str(), partType.c_str());
	}
	else if (iCount == 1)
	{
		*tPartTag = tQryResults[0];
	}
	else
	{
		TC_write_syslog("[UOM] More than 1 Items not found for given Part Number [%s] and Part Type [%s]\n", partNumber.c_str(), partType.c_str());
	}

	SAFE_SM_FREE(queryValues[0]);
	SAFE_SM_FREE(queryValues[1]);
	SAFE_SM_FREE(queryValues);

	return ifail;
}
int modify_item(tag_t tPartTag, tag_t uom_tag)
{
	int ifail = ITK_ok;

	CHECK_FAIL(AOM_refresh(tPartTag, TRUE));
	CHECK_FAIL(AOM_assign_tag(tPartTag, "uom_tag", uom_tag));
	CHECK_FAIL(AOM_save_without_extensions(tPartTag));
	CHECK_FAIL(AOM_refresh(tPartTag, FALSE));

	return ifail;

}
AdminUtilsImpl::ReassignWFTaskResponse AdminUtilsImpl::reassignWFTask ( const std::vector< ReassignWFTaskInput >& inputs )
{
	int ifail = ITK_ok;
	std::string queryFlag;
	vector<tag_t> allTasks;
	vector<tag_t> taskToProcess;
	AdminUtilsImpl::ReassignWFTaskResponse response;

	TC_write_syslog("[VF] ENTER %s", __FUNCTION__);

	TC_write_syslog("\n[VF]Size of Input Vector : %d\n", inputs.size());
	ITK_set_bypass(TRUE);
	if (inputs.size() > 0)
	{
		for (ReassignWFTaskInput taskInput : inputs)
		{
			try
			{
				ReassignWFTaskOutput outputStatus;
				int doTaksCount = 0;
				int signoffTaksCount = 0;
				int acknowTaksCount = 0;
				int finalCounter = 0;

				tag_t doTaksQtag = NULLTAG;
				tag_t signoffTaksQtag = NULLTAG;
				tag_t acknowTaksQtag = NULLTAG;
				tag_t new_member_tag = NULLTAG;
				tag_t groupmem_user_tag = NULLTAG;
				tag_t newUserTag = NULLTAG;
				tag_t defaultGroup = NULL;

				Teamcenter::scoped_smptr<tag_t> signoffTaskResult;
				Teamcenter::scoped_smptr<tag_t> acknowTaskResult;
				Teamcenter::scoped_smptr<tag_t> doTaskResult;
				Teamcenter::scoped_smptr<char> task_type;

				bool templateFlag = false;

				char* user_name_string = NULL;
				char* defaultGroupName = NULL;

				std::string queryFlag;

				std::string oldUserid = taskInput.userId;
				std::string WFName = taskInput.processTemplateName;
				std::string newUserId = taskInput.newUserId;
				std::string newUserGroup = taskInput.newUserGroup;
				int totalCount = taskInput.executingProcessesNum;
				bool isDryRun = taskInput.isDryRun;
				std::string objectType = taskInput.objectType;
				vector<WFTaskPropertyAndValue> propertyAndValues = taskInput.propertyNameAndValue;

				if (oldUserid.empty())
				{
					outputStatus.errorMessage.assign("User Id can not be empty.Please provide require inputs");
					response.outputs.push_back(outputStatus);
					continue;
				}
				if (newUserId.empty())
				{
					outputStatus.errorMessage.assign("New User Id can not be empty.Please provide require inputs");
					response.outputs.push_back(outputStatus);
					continue;
				}
				//querying for active tasks

				if(!oldUserid.empty())
				{
					if(!WFName.empty())
					{
						templateFlag = true;
						TC_write_syslog("\n ================Executing Query No 01 with template name=================");
						char* queryEntries[4] = {"Parent Task Name","User ID","State Value","Task Type"};
						char** queryValues = (char**)MEM_alloc(sizeof(char*) * 4);

						queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(WFName.c_str()) + 1));
						queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(oldUserid.c_str()) + 1));
						queryValues[2] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen("2;4") + 1));
						queryValues[3] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen("Do Task;Condition Task") + 1));

						queryValues[0] = tc_strcpy(queryValues[0], WFName.c_str());
						queryValues[1] = tc_strcpy(queryValues[1], oldUserid.c_str());
						queryValues[2] = tc_strcpy(queryValues[2], "2;4");
						queryValues[3] = tc_strcpy(queryValues[3], "Do Task;Condition Task");

						ifail = QRY_find2("__Workflow Task",&doTaksQtag);
						if(ifail == ITK_ok)
						{
							 //Execute query to get task from Task to perform folder
							 ifail=QRY_execute(doTaksQtag, 4, queryEntries, queryValues, &doTaksCount, &doTaskResult);

							 TC_write_syslog("\n[VF]Total No of Do and Condition tasks:%d",doTaksCount);

							 if(ifail != ITK_ok)
							 {
								 TC_write_syslog("\n[VF]Failed to execute Query __Workflow Task ");
							 }
						}
						SAFE_SM_FREE(queryValues[0]);
						SAFE_SM_FREE(queryValues[1]);
						SAFE_SM_FREE(queryValues[2]);
						SAFE_SM_FREE(queryValues[3]);
						SAFE_SM_FREE(queryValues);
					}
					else if(WFName.empty())
					{
						TC_write_syslog("\n ================Executing Query No 01 with given userid=================");
						char* queryEntries[3] = {"User ID","State Value","Task Type"};
						char** queryValues = (char**)MEM_alloc(sizeof(char*) * 3);

						queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(oldUserid.c_str()) + 1));
						queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen("2;4") + 1));
						queryValues[2] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen("Do Task;Condition Task") + 1));

						queryValues[0] = tc_strcpy(queryValues[0], oldUserid.c_str());
						queryValues[1] = tc_strcpy(queryValues[1], "2;4");
						queryValues[2] = tc_strcpy(queryValues[2], "Do Task;Condition Task");

						ifail = QRY_find2("__Workflow Task",&doTaksQtag);
						if(ifail == ITK_ok)
						{
							//Execute query to get task from Task to perform folder
							ifail=QRY_execute(doTaksQtag, 3, queryEntries, queryValues, &doTaksCount, &doTaskResult);

							TC_write_syslog("\n[VF]Total No of Do and Condition tasks :%d",doTaksCount);
							if(ifail != ITK_ok)
							{
								TC_write_syslog("\n[VF]Failed to execute Query __Workflow Task");
							}
						}
						SAFE_SM_FREE(queryValues[0]);
						SAFE_SM_FREE(queryValues[1]);
						SAFE_SM_FREE(queryValues[2]);
						SAFE_SM_FREE(queryValues);
					}
					else
					{
						TC_write_syslog("\n [VF]Please Check the input parameter");
					}

					TC_write_syslog("\n================Executing Query No 02=================");
					char* query2Entries[2] = {"Id","State Value"};
					char** query2Values = (char**)MEM_alloc(sizeof(char*) * 2);

					query2Values[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(oldUserid.c_str()) + 1));
					query2Values[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen("2;4;1") + 1));

					query2Values[0] = tc_strcpy(query2Values[0], oldUserid.c_str());
					query2Values[1] = tc_strcpy(query2Values[1], "2;4;1");

					ifail = QRY_find2("__Workflow Task2",&signoffTaksQtag);
					if(ifail == ITK_ok)
					{
						//Execute query to get task from Task to perform folder

						ifail=QRY_execute(signoffTaksQtag, 2, query2Entries, query2Values, &signoffTaksCount, &signoffTaskResult);

						TC_write_syslog("\n[VF]Total No of Signoff tasks found:%d",signoffTaksCount);

						if(ifail != ITK_ok)
						{
							TC_write_syslog("\n[VF]Failed to execute Query __Workflow Task2");
						}
					}

					TC_write_syslog("\n================Executing Query No 03=================");
					char* query3Entries[2] = {"Id","State Value"};
					char** query3Values = (char**)MEM_alloc(sizeof(char*) * 2);

					query3Values[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(oldUserid.c_str()) + 1));
					query3Values[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen("2;4;1") + 1));;

					query3Values[0] = tc_strcpy(query3Values[0], oldUserid.c_str());
					query3Values[1] = tc_strcpy(query3Values[1], "2;4;1");

					ifail = QRY_find2("__Workflow Task3",&acknowTaksQtag);
					if(ifail == ITK_ok)
					{
						//Execute query to get task from Task to perform folder

						ifail=QRY_execute(acknowTaksQtag, 2, query3Entries, query3Values, &acknowTaksCount, &acknowTaskResult);

						TC_write_syslog("\n[VF]Total No of Acknowledge tasks found:%d",acknowTaksCount);
						if(ifail != ITK_ok)
						{
							TC_write_syslog("\n[VF]Failed to execute Query __Workflow Task3");
						}
					}

					SAFE_SM_FREE(query2Values[0]);
					SAFE_SM_FREE(query2Values[1]);
					SAFE_SM_FREE(query2Values);
					SAFE_SM_FREE(query3Values[0]);
					SAFE_SM_FREE(query3Values[1]);
					SAFE_SM_FREE(query3Values);
				}
				if(newUserGroup.empty())
				{
					CHECK_FAIL(SA_find_user2(newUserId.c_str(),&newUserTag));
					CHECK_FAIL(POM_ask_user_default_group(newUserTag,&defaultGroup));
					CHECK_FAIL(SA_ask_group_name2(defaultGroup,&defaultGroupName));
					new_member_tag = get_New_User(newUserId, defaultGroupName);
				}
				else
				{
					new_member_tag = get_New_User(newUserId, newUserGroup); //new member tag for change signoff
				}

				if(new_member_tag == NULLTAG)
				{
					TC_write_syslog("\n[WF Task] No member tag found");
				}
				else
				{
					CHECK_FAIL(SA_ask_groupmember_user(new_member_tag,&groupmem_user_tag));
					if(groupmem_user_tag == NULLTAG)
					{
						TC_write_syslog("\n[WF Task] No User Tag found");
					}
					else
					{
						CHECK_FAIL(AOM_ask_name(groupmem_user_tag,&user_name_string));
					}
				}
				std::string dryRun = btos(isDryRun);
				if(doTaksCount > 0)
				{
					for(int ii = 0; ii < doTaksCount; ii++)
					{
						allTasks.push_back(doTaskResult[ii]);
					}

				}
				if(signoffTaksCount > 0)
				{
					if(templateFlag)
					{
						tag_t root_task = NULLTAG;
						Teamcenter::scoped_smptr<char> root_task_string;
						for(int ii = 0; ii < signoffTaksCount; ii++)
						{
							CHECK_FAIL(EPM_ask_root_task(signoffTaskResult[ii],&root_task));
							CHECK_FAIL(AOM_ask_name(root_task,&root_task_string));
							if(tc_strcmp(root_task_string.getString(),WFName.c_str()) == 0)
							{
								allTasks.push_back(signoffTaskResult[ii]);
							}
						}
					}
					else
					{
						for(int ii = 0; ii < signoffTaksCount; ii++)
						{
							allTasks.push_back(signoffTaskResult[ii]);
						}
					}
				}
				if(acknowTaksCount > 0)
				{
					if(templateFlag)
					{
						tag_t root_task = NULLTAG;
						Teamcenter::scoped_smptr<char> root_task_string;
						for(int ii = 0; ii < acknowTaksCount; ii++)
						{
							CHECK_FAIL(EPM_ask_root_task(acknowTaskResult[ii],&root_task));
							CHECK_FAIL(AOM_ask_name(root_task,&root_task_string));
							if(tc_strcmp(root_task_string.getString(),WFName.c_str()) == 0)
							{
								allTasks.push_back(acknowTaskResult[ii]);
							}
						}
					}
					else
					{
						for(int ii = 0; ii < acknowTaksCount; ii++)
						{
							allTasks.push_back(acknowTaskResult[ii]);
						}
					}
				}

				for(int k = 0; k < allTasks.size(); k++)
				{
					int iNumAttachs = 0;
					tag_t tRootTask = NULLTAG;
					Teamcenter::scoped_smptr<tag_t> ptAttachs;
					Teamcenter::scoped_smptr<char> targetObjType;
					if(!objectType.empty() && propertyAndValues.size() > 0)
					{
						CHECK_FAIL(EPM_ask_root_task(allTasks[k], &tRootTask));
						CHECK_FAIL(EPM_ask_attachments(tRootTask, EPM_target_attachment, &iNumAttachs, &ptAttachs));
						if(iNumAttachs > 0)
						{
							tag_t tTargetAttch = ptAttachs[0];
							CHECK_FAIL(AOM_ask_value_string(tTargetAttch,"object_type",&targetObjType));
							if(tc_strcmp(targetObjType.getString(),objectType.c_str()) == 0)
							{
								bool pushFlag = true;
								for (WFTaskPropertyAndValue propertyArray : propertyAndValues)
								{
									int nValues = 0;
									Teamcenter::scoped_smptr<char*> spValues;
									std::string propName = propertyArray.propertyName;
									std::string propvalue = propertyArray.propertyValue;
									CHECK_FAIL(AOM_ask_displayable_values(tTargetAttch, propName.c_str(), &nValues, &spValues));
									if(tc_strcmp(propvalue.c_str(),spValues.get()[0]) != 0)
									{
										pushFlag = false;
									}
								}
								if(pushFlag)
								{
									taskToProcess.push_back(allTasks[k]);
								}
							}else
							{
								TC_write_syslog("[VF]Target Object Type does not match with the given Object Type:\n");
							}
						}
					}else
					{
						taskToProcess.push_back(allTasks[k]);
					}
				}

				if( tc_strcmp(dryRun.c_str(),"false") == 0)
				{
					if(totalCount > 0 && totalCount <= taskToProcess.size() )
					{
						finalCounter = totalCount;
					}else if(totalCount > taskToProcess.size())
					{
						outputStatus.errorMessage.assign("Executing Processes Number can not be greater than task count");
						response.outputs.push_back(outputStatus);
						return response;
					}else
					{
						finalCounter = taskToProcess.size();
					}
				}else
				{
					finalCounter = taskToProcess.size();
				}
				for(int j = 0; j < finalCounter; j++)
				{
					CHECK_FAIL(AOM_ask_value_string(taskToProcess[j],"object_type",&task_type));
					if(tc_strcmp(task_type.getString(),"EPMDoTask")==0 || tc_strcmp(task_type.getString(),"EPMConditionTask")==0)
					{
						ifail = reassignDoTask(taskToProcess[j], dryRun, oldUserid, groupmem_user_tag, outputStatus, response);
						if(ifail != ITK_ok)
						{
							outputStatus.errorMessage.assign("Error in reassignDoTask..");
							response.outputs.push_back(outputStatus);
							continue;
						}
					}else
					{
						ifail = reassignsignoffTask(taskToProcess[j], dryRun, oldUserid, newUserId, new_member_tag, outputStatus, response);
						if(ifail != ITK_ok)
						{
							outputStatus.errorMessage.assign("Error in reassignsignoffTask..");
							response.outputs.push_back(outputStatus);
							continue;
						}
					}
				}

			}
			catch(const IFail &e)
			{
				std::cout << e.getMessage();
				response.serviceData.addErrorStack();
				TC_write_syslog("\n[VF]Error ::%s\n", e.getMessage());
			}
		}
	}

	TC_write_syslog("[VF] EXIT %s", __FUNCTION__);
	return response;
}
tag_t get_New_User(std::string newUserID,std::string newgrp)
{
	int
        ii, jj,
        num_groups = 0,
        num_members = 0;
    tag_t
        *groups = NULL,
        *members = NULL,
         user = NULLTAG;
    char
        **group_names = NULL;

	char*	user_name;
	//char* newGroup = newgrp;
	char* newUserName =NULL;
	tag_t member_tag = NULL;

	//TC_write_syslog("\n====================Start to Get New member Tag========================\n");

    CHECK_FAIL(SA_list_groups(&num_groups, &groups, &group_names));
    for (ii = 0; ii < num_groups; ii++)
    {
		if(tc_strcmp(group_names[ii],newgrp.c_str())==0)
		{
			CHECK_FAIL(SA_find_groupmembers_by_group(groups[ii],&num_members, &members));

			for (jj = 0; jj < num_members; jj++)
			{
				CHECK_FAIL(SA_ask_groupmember_user(members[jj], &user));
				CHECK_FAIL(AOM_ask_name(user,&user_name));

				CHECK_FAIL(SA_ask_os_user_name2(user,&newUserName));

				if(tc_strcasecmp(newUserName,newUserID.c_str())==0)
				{
					member_tag = members[jj];
					break;
				}
			}
			if (num_members > 0) MEM_free(members);
		}

    }
    if (num_groups > 0)
    {
        SAFE_SM_FREE(group_names);
        SAFE_SM_FREE(groups);
    }
	return member_tag;
}
std::string btos(bool value)
{
	if(value == false)
	  return "false";
	else
	  return "true";
}
int reassignDoTask(tag_t taskTag, std::string isDryRun, std::string oldUserid, tag_t groupmem_user_tag, AdminUtilsImpl::ReassignWFTaskOutput &outputStatus, AdminUtilsImpl::ReassignWFTaskResponse &response)
{
	int ifail = 0;

	std::string output;
	std::string dryRun;

	tag_t root_task = NULL;

	Teamcenter::scoped_smptr<char> taskType;
	Teamcenter::scoped_smptr<char> taskName;
	Teamcenter::scoped_smptr<char> root_task_string;
	Teamcenter::scoped_smptr<char> oldusername;
	Teamcenter::scoped_smptr<char> uid;
	Teamcenter::scoped_smptr<char> assigneeUser;

	CHECK_FAIL(AOM_ask_name(taskTag,&taskName));
	CHECK_FAIL(AOM_ask_value_string(taskTag,"object_type",&taskType));
	CHECK_FAIL(EPM_ask_root_task(taskTag,&root_task));
	CHECK_FAIL(AOM_ask_name(root_task,&root_task_string));
	CHECK_FAIL(POM_tag_to_uid(root_task, &uid));

	tag_t initiator = NULLTAG;
	CHECK_FAIL(AOM_ask_value_tag(taskTag, "fnd0Assignee", &initiator));
	CHECK_FAIL(SA_ask_os_user_name2(initiator,&assigneeUser));

	if(tc_strcmp(taskType.getString(),"EPMDoTask")==0 || tc_strcmp(taskType.getString(),"EPMConditionTask")==0)//EPMDoTask
	{
		if(tc_strcmp(isDryRun.c_str(), "false") == 0)
		{
			ifail = EPM_assign_responsible_party(taskTag,groupmem_user_tag);
			if(ifail != ITK_ok)
			{
				dryRun = "FAIL";
				TC_write_syslog("\n[VF] Error Code : %d :: %s (%s) =  Failed to Re-assign User\n",ifail,taskName.getString(),taskType.getString());
			}
			else
			{
				dryRun = "SUCCESS";
			}
			outputStatus.reasignedTask = taskTag;
			outputStatus.templateName.assign(root_task_string.getString());
			outputStatus.taskName.assign(taskName.getString());
			outputStatus.taskType.assign(taskType.getString());
			outputStatus.assignedUsers.assign(assigneeUser.getString());
			outputStatus.returnStatus.assign(dryRun);
			response.outputs.push_back(outputStatus);

		}
		else if(tc_strcmp(isDryRun.c_str(), "true") == 0 || tc_strcmp(isDryRun.c_str(), "") == 0)
		{
			outputStatus.reasignedTask = taskTag;
			outputStatus.templateName.assign(root_task_string.getString());
			outputStatus.taskName.assign(taskName.getString());
			outputStatus.taskType.assign(taskType.getString());
			outputStatus.assignedUsers.assign(assigneeUser.getString());
			outputStatus.returnStatus.assign("DRYRUN");
			response.outputs.push_back(outputStatus);
		}
	}

	return ITK_ok;
}
int reassignsignoffTask(tag_t taskTag, std::string isDryRun, std::string oldUserid, std::string newUserid, tag_t new_member_tag, AdminUtilsImpl::ReassignWFTaskOutput &outputStatus, AdminUtilsImpl::ReassignWFTaskResponse &response)
{
	int ifail = 0;
	int n_signoff = 0;
	int signoffCount=0;
	bool userFlag = false;
	std::string output;
	std::string dryRunReview ="FAIL";
	string signoffUser;

	Teamcenter::scoped_smptr<char> taskType;
	Teamcenter::scoped_smptr<char> taskName;
	Teamcenter::scoped_smptr<char> parentName;
	Teamcenter::scoped_smptr<char> root_task_string;
	Teamcenter::scoped_smptr<char> state_name;
	Teamcenter::scoped_smptr<char> os_user;

	Teamcenter::scoped_smptr<tag_t> signoffs;

	tag_t mem_tag = 0;
	tag_t user_tag=NULL;
	tag_t root_task = NULL;
	EPM_state_t state;
	SIGNOFF_TYPE_t  memberType;


	CHECK_FAIL(AOM_ask_name(taskTag,&taskName));
	CHECK_FAIL(AOM_ask_value_string(taskTag, "parent_name", &parentName));
	CHECK_FAIL(EPM_ask_root_task(taskTag,&root_task));
	CHECK_FAIL(AOM_ask_name(root_task,&root_task_string));

	CHECK_FAIL(AOM_ask_value_string(taskTag, "object_type", &taskType));
	CHECK_FAIL(EPM_ask_state(taskTag,&state));
	CHECK_FAIL(EPM_ask_state_string2(state,&state_name));
	ifail = EPM_ask_attachments(taskTag, EPM_signoff_attachment, &n_signoff, &signoffs);
	if(ifail == ITK_ok)
	{
		if(n_signoff > 0)
		{
			AOM_refresh(taskTag,TRUE);
			for(int p=0;p<n_signoff;p++)
			{
				EPM_ask_signoff_member(signoffs[p],&mem_tag,&memberType);
				SA_ask_groupmember_user(mem_tag,&user_tag);
				SA_ask_os_user_name2(user_tag,&os_user);
				if(p ==0)
				{
					signoffUser.assign(os_user.getString());
				}
				else
				{
					signoffUser.append(",");
					signoffUser.append(os_user.getString());
				}
				if(strcmp(os_user.getString(), oldUserid.c_str()) == 0)
				{
					userFlag = isSignoffUserAlreadyExists(taskTag,newUserid);
					if(!userFlag)
					{
						if(tc_strcmp(isDryRun.c_str(), "false") == 0 )
						{

							if(tc_strcmp(state_name.getString(),"Started") == 0 || tc_strcmp(state_name.getString(),"Completed") == 0)
							{
								ifail = EPM_delegate_signoff(signoffs[p],new_member_tag,TRUE);
								if(ifail != ITK_ok)
								{
									dryRunReview = "FAIL";
									TC_write_syslog("\n[VF]Failed to Re-assign Select signoff Task");
								}
								else
								{
									dryRunReview = "SUCCESS";
								}
							}
							else
							{
								ifail = EPM_remove_attachments(taskTag,1,&signoffs[p]);
								ifail = EPM_create_adhoc_signoff(taskTag,new_member_tag,&signoffCount,&signoffs);
								if(ifail != ITK_ok)
								{
									dryRunReview = "FAIL";
									TC_write_syslog("\n[VF]Failed to Re-assign Select signoff Task");
								}
								else
								{
									dryRunReview = "SUCCESS";
								}
							}

						}
					}
				}
			}
			AOM_save_without_extensions(taskTag);
			AOM_refresh(taskTag, FALSE);
		}
		else
		{
			TC_write_syslog("\n [VF]No signoffs found");
		}
	}
	if(tc_strcmp(isDryRun.c_str(), "false") == 0)
	{
		outputStatus.reasignedTask = taskTag;
		outputStatus.templateName.assign(root_task_string.getString());
		outputStatus.taskName.assign(parentName.getString());
		outputStatus.taskType.assign(taskType.getString());
		outputStatus.assignedUsers.assign(signoffUser);
		outputStatus.returnStatus.assign(dryRunReview);
		response.outputs.push_back(outputStatus);
	}
	else if(tc_strcmp(isDryRun.c_str(), "true") == 0 || tc_strcmp(isDryRun.c_str(), "") == 0)
	{
		outputStatus.reasignedTask = taskTag;
		outputStatus.templateName.assign(root_task_string.getString());
		outputStatus.taskName.assign(parentName.getString());
		outputStatus.taskType.assign(taskType.getString());
		outputStatus.assignedUsers.assign(signoffUser);
		outputStatus.returnStatus.assign("DRYRUN");
		response.outputs.push_back(outputStatus);
	}

	return ITK_ok;
}
bool isSignoffUserAlreadyExists(tag_t taskTag, std::string newUserid)
{
	int ifail = 0;
	int n_signoff = 0;
	tag_t mem_tag = 0;
	bool userFlag=false;

	tag_t user_tag=NULL;
	SIGNOFF_TYPE_t  memberType;

	Teamcenter::scoped_smptr<tag_t> signoffs;
	Teamcenter::scoped_smptr<char> os_user;

	ifail = EPM_ask_attachments(taskTag, EPM_signoff_attachment, &n_signoff, &signoffs);
	if(ifail == ITK_ok)
	{
		for(int k=0;k<n_signoff;k++)
		{
			EPM_ask_signoff_member(signoffs[k],&mem_tag,&memberType);
			SA_ask_groupmember_user(mem_tag,&user_tag);
			SA_ask_os_user_name2(user_tag,&os_user);
			if(strcmp(os_user.getString(), newUserid.c_str()) == 0)
			{
				userFlag = true;
			}
		}

	}
	return userFlag;
}
AdminUtilsImpl::SetPropertyResponse AdminUtilsImpl::setProperties ( const std::vector< SetPropertyInput >& inputs, bool isKeepModifiedDate )
{
	AdminUtilsImpl::SetPropertyResponse response;

	TC_write_syslog("[VF] ENTER %s", __FUNCTION__);
	TC_write_syslog("\n[VF]Size of Input Vector : %d\n", inputs.size());
	if (inputs.size() > 0)
	{
		for (SetPropertyInput propertyInput : inputs)
		{
			try
			{
				tag_t objectTag = NULLTAG;
				tag_t classTag	= NULLTAG;
				tag_t tClassId = NULLTAG;
				Teamcenter::scoped_smptr <char> className;

				vector<PropertyAndValues> propertyAndValues = propertyInput.propertyAndValues;
				std::string uid = propertyInput.uid;

				ITK__convert_uid_to_tag(uid.c_str(),&objectTag);
				CHECK_FAIL(POM_class_of_instance(objectTag, &classTag));
				CHECK_FAIL(POM_name_of_class(classTag,& className));
				CHECK_FAIL(POM_class_id_of_class(className.getString(), &tClassId));
				CHECK_FAIL(AOM_refresh(objectTag, true));
				if(isKeepModifiedDate)
				{
					if (propertyAndValues.size() > 0)
					{
						for (PropertyAndValues propertyArray : propertyAndValues)
						{

							std::string propertyName = propertyArray.property;
							std::vector<std::string> propertyValues = propertyArray.values;
							if (propertyValues.size() > 0)
							{
								setProperty(objectTag, className.getString(), tClassId, propertyName, propertyValues, response);
							}
						}
					}

				}else
				{
					if (propertyAndValues.size() > 0)
					{
						POM_set_env_info(POM_bypass_attr_update,false,0,0.0,NULL_TAG,NULL);
						for (PropertyAndValues propertyArray : propertyAndValues)
						{

							std::string propertyName = propertyArray.property;
							std::vector<std::string> propertyValues = propertyArray.values;
							if (propertyValues.size() > 0)
							{
								setProperty(objectTag, className.getString(), tClassId, propertyName, propertyValues, response);
							}
						}
						POM_set_env_info(POM_bypass_attr_update,true,0,0.0,NULL_TAG,NULL);
					}
				}
				CHECK_FAIL(AOM_refresh(objectTag, false));
			}
			catch(const IFail &e)
			{
				std::cout << e.getMessage();
				response.serviceData.addErrorStack();
				TC_write_syslog("\n[VF]Error ::%s\n", e.getMessage());
			}
		}
	}

	TC_write_syslog("[VF] EXIT %s", __FUNCTION__);
	return response;
}
int setProperty(tag_t objectTag, std::string className, tag_t classId, std::string propertyName, vector<string> propValue, AdminUtilsImpl::SetPropertyResponse &response)
{
	int ifail = ITK_ok;
	int	IsPropArray;

	tag_t tAttrId = NULLTAG;
	PROP_value_type_t	propType;
	Teamcenter::scoped_smptr <char> propStorageType;
	AdminUtilsImpl::SetPropertyError outputStatus;

	CHECK_FAIL(AOM_ask_value_type(objectTag, propertyName.c_str(), &propType, &propStorageType));
	CHECK_FAIL(POM_attr_id_of_attr(propertyName.c_str(), className.c_str(), &tAttrId));
	CHECK_FAIL(POM_type_of_att(classId, tAttrId, &IsPropArray));

	if (IsPropArray == POM_non_array)
	{
		if (propType == PROP_string)
		{
			ifail = POM_set_attr_string(1, &objectTag, tAttrId, propValue.front().c_str());
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Error Setting the Property: ");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
			ifail = AOM_save_without_extensions(objectTag);
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Error Setting the Property: ");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
		}
		else if (propType == PROP_logical)
		{
			logical propertyVal;
			ifail = STRNG_is_logical(propValue.front().c_str(), &propertyVal);
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Given value is not a logical for property");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
			ifail = POM_set_attr_logical(1, &objectTag, tAttrId, propertyVal);
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Error Setting the Property: ");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
			ifail = AOM_save_without_extensions(objectTag);
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Error Setting the Property: ");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
		}
		else if (propType == PROP_int)
		{

			ifail = POM_set_attr_int(1, &objectTag, tAttrId, std::stoi(propValue.front()));
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Error Setting the Property: ");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
			ifail = AOM_save_without_extensions(objectTag);
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Error Setting the Property: ");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
		}
		else if (propType == PROP_double)
		{
			ifail = POM_set_attr_double(1, &objectTag, tAttrId, std::stod(propValue.front()));
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Error Setting the Property: ");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
			ifail = AOM_save_without_extensions(objectTag);
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Error Setting the Property: ");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
		}
		else if (propType == PROP_date)
		{
			logical  	dateIsValid;
			date_t dateValueDate = NULLDATE;

			ifail = DATE_string_to_date_t((char*)propValue.front().c_str(), &dateIsValid, &dateValueDate);
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Invalid Property value format.Valid Format ex.07-Jan-2022 16:29 :");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
			ifail = POM_set_attr_date(1, &objectTag, tAttrId, dateValueDate);
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Error Setting the Property: ");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
			ifail = AOM_save_without_extensions(objectTag);
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Error Setting the Property: ");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
		}
	}
	else
	{
		if (propType == PROP_string)
		{
			for (int pVl = 0; pVl < propValue.size(); pVl++)
			{
				char* propertyVal = (char*)propValue.at(pVl).c_str();
				ifail = POM_set_attr_strings(1, &objectTag, tAttrId, pVl, 1, &propertyVal);
				if (ifail != ITK_ok)
				{
					outputStatus.errorObject = objectTag;
					outputStatus.errorString.assign("Error Setting the Property: ");
					outputStatus.errorString.append(propertyName.c_str());
					response.errors.push_back(outputStatus);
					return ifail;
				}
			}
			ifail = AOM_save_without_extensions(objectTag);
			if (ifail != ITK_ok)
			{
				outputStatus.errorObject = objectTag;
				outputStatus.errorString.assign("Error Setting the Property: ");
				outputStatus.errorString.append(propertyName.c_str());
				response.errors.push_back(outputStatus);
				return ifail;
			}
		}
		else
		{
			outputStatus.errorObject = objectTag;
			outputStatus.errorString.assign("Invalid Property type : ");
			outputStatus.errorString.append(propertyName.c_str());
			response.errors.push_back(outputStatus);
			return ifail;
		}
	}
	return ifail;
}
