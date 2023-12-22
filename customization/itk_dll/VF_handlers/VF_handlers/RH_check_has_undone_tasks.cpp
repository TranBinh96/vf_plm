/*******************************************************************************
File         : RH_check_task_state.cpp

Description  : To check state of all task
  
Input        : None
                        
Output       : None

Author       : Siemens

Revision History :
Date            Revision    Who              Description
-----------------------------------------------------------------------------
Sep,18 2023     1.1         Nguyen           Initial Creation

/*******************************************************************************/
#include "Vinfast_Custom.h"

std::string GetRerouteDecision(tag_t reactiveTsk);
std::string GetTaskNameFromRerouteDecision(std::string decision);

EPM_decision_t RH_check_has_undone_tasks(EPM_rule_message_t msg)
{
	int				iRetCode			= ITK_ok;
	tag_t			tRootTask			= NULLTAG;
	EPM_decision_t	decision			= EPM_nogo;

	std::string rerouteDecision;
	rerouteDecision = GetRerouteDecision(msg.task);
	if (rerouteDecision.empty()) {
		return EPM_nogo;
	}

	std::string decisionTask;
	decisionTask = GetTaskNameFromRerouteDecision(rerouteDecision);
	if (decisionTask.empty()) {
		return EPM_nogo;
	}

	CHECK_ITK(iRetCode,EPM_ask_root_task(msg.task,&tRootTask));
	if(tRootTask != 0)
	{
		int			iAllTasks			= 0;
		tag_t		*ptAllTaskTags		= NULL;

		CHECK_ITK(iRetCode,EPM_ask_sub_tasks(tRootTask,&iAllTasks,&ptAllTaskTags));
		for(int iLoopAllTasks = 0 ; iLoopAllTasks < iAllTasks; iLoopAllTasks++)
		{
			Teamcenter::scoped_smptr<char> pcTaskName;

			CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks], "object_name", &pcTaskName));
			if (tc_strcasecmp(&decisionTask[0], pcTaskName.getString()) == 0) {
				continue;
			}

			Teamcenter::scoped_smptr<char> pcTaskState;
			Teamcenter::scoped_smptr<char> pcTaskResult;

			CHECK_ITK(iRetCode,AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks],"task_state", &pcTaskState));
			CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks], "task_result", &pcTaskResult));
			/*if any of the task state is pending then do not start the task*/
			if(tc_strcasecmp("Rejected", pcTaskResult.getString()) == 0 ||
			   tc_strcasecmp("reject", pcTaskResult.getString()) == 0)
			{				
				decision = EPM_go;
				break;			
			}
		}
	}

	return decision;
}

/// <summary>
/// 
/// </summary>
/// <param name="reactiveTsk">The Re-activate task</param>
/// <returns>The name of decision task from Reroute task</returns>
std::string GetRerouteDecision(tag_t reactiveTsk)
{
	int	iRetCode = ITK_ok;
	tag_t* predTsks = NULL;
	tag_t tskReroute = NULL;
	int cntPredec = 0;

	CHECK_ITK(iRetCode, AOM_ask_value_tags(reactiveTsk, "predecessors", &cntPredec, &predTsks));

	for (int i = 0; i < cntPredec; i++) {
		Teamcenter::scoped_smptr<char> tskName;
		CHECK_ITK(iRetCode, AOM_ask_value_string(predTsks[i], "object_name", &tskName));
		if (tc_strcasecmp("Reroute", tskName.getString()) == 0) {
			tskReroute = predTsks[i];
			break;
		}
	}

	if (tskReroute == NULL) {
		return "";
	}

	Teamcenter::scoped_smptr<char> rdecision;
	CHECK_ITK(iRetCode, AOM_ask_value_string(tskReroute, "task_result", &rdecision));
	return rdecision.getString();
}

/// <summary>
/// 
/// </summary>
/// <param name="decision"></param>
/// <returns></returns>
std::string GetTaskNameFromRerouteDecision(std::string decision)
{
	map<std::string, std::string> mapDecisionAndTskName = {
		{"Supplier Quality Review", "Supplier Quality Manager"},
		{"Change Approval Board", "Change Approval Board"},
		{"Rejected", "Rejected"},
		{"Component TnV Review", "Component TnV Review"},
		{"EE TnV Review", "EE TnV Review"},
		{"Module Group Leader Review", "Module Group leader review"},
		{"Cost Engineering/Product Finance", "Cost Engineering"},
		{"Senior Quality Director Review", "Senior Quality Director Review"},
		{"DM Check", "DM Check"},
		{"Manufacturing Quality", "Manufacturing Quality Review"},
		{"Standard Part Review", "Standard Part Review"},
		{"HTKO 1 Review", "HTKO 1 Review"},
		{"Sale & Product Marketing Review", "Sale & Product Marketing Review"},
		{"Functional Safety Review", "Functional Safety Review"},
		{"After Sale Review", "After Sale Review"},
		{"Vehicle TnV Review", "Vehicle TnV"},
		{"DMU Check", "DMU Check Approval"},
		{"INdirect Purchase Commodity Review", "INdirect Purchase Commodity Review"},
		{"Manufacturing Review", "Manufacturing Review"},
		{"HTKO 2 Review", "HTKO 2 Review"},
		{"Vehicle Architect Review", "Vehicle Architect Approval"},
		{"Homologation Review", "Homologation Review"},
		{"Engineering Quality Review", "Quality Engineering Review"},
		{"Direct Purchase Commodity Review", "Direct Purchase Commodity Review"},
		{"CAE Review", "CAE Review"},
		{"Styling", "Styling & color & trim"},
		{"Testing Validation Review", "Testing Validation Review"},
	};

	if (mapDecisionAndTskName.find(decision) == mapDecisionAndTskName.end()) {
		return "";
	}

	return mapDecisionAndTskName.find(decision)->second;
}
