/*******************************************************************************
File         : RH_check_task_state.cpp

Description  : To check state of all task
  
Input        : None
                        
Output       : None

Author       : Siemens

Revision History :
Date            Revision    Who              Description
-----------------------------------------------------------------------------
Jan,17 2020     1.0         Kantesh		 Initial Creation

/*******************************************************************************/
#include "Vinfast_Custom.h"
#include <chrono>
#include <iomanip>

auto is_contain(const char* str, vector<string> lst) -> boolean;

EPM_decision_t RH_check_task_state_new(EPM_rule_message_t msg)
{
	int			iRetCode				= ITK_ok;
	tag_t		tRootTask				= NULLTAG;
	EPM_decision_t decision				= EPM_go;

	char* InclGates = nullptr;
	char* ExclGates = nullptr;
	char* FlagProp = nullptr;
	char* FlagVal = nullptr;

	int iArgCnt = 0;

	iArgCnt = TC_number_of_arguments(msg.arguments);
	if (iArgCnt > 0)
	{
		Teamcenter::scoped_smptr<char> name, val;
		const char* ArgInclGate = "include_gate";
		const char* ArgExclGate = "exclude_gate";
		const char* ArgFlagProp = "flag_prop";

		for (int i = 0; i < iArgCnt; i++)
		{
			iRetCode = ITK_ask_argument_named_value(TC_next_argument(msg.arguments), &name, &val);
			if (val.getString() == NULL)
			{
				continue;
			}

			if (tc_strcmp(name.getString(), ArgInclGate) == 0)
			{
				InclGates = (char*)MEM_alloc(((int)tc_strlen(val.getString()) + 1) * sizeof(char));
				tc_strcpy(InclGates, val.getString());
			}
			else if (tc_strcmp(name.getString(), ArgExclGate) == 0)
			{
				ExclGates = (char*)MEM_alloc(((int)tc_strlen(val.getString()) + 1) * sizeof(char));
				tc_strcpy(ExclGates, val.getString());
			}
			else if (tc_strcmp(name.getString(), ArgFlagProp) == 0)
			{
				FlagProp = (char*)MEM_alloc(((int)tc_strlen(val.getString()) + 1) * sizeof(char));
				tc_strcpy(FlagProp, val.getString());
			}
		}
	}

	CHECK_ITK(iRetCode,EPM_ask_root_task(msg.task,&tRootTask));
	if(tRootTask != 0)
	{
		// Extract arguments infor
		vector<string> lstIncl, lstExcl;
		char* tok = NULL;

		if (InclGates != NULL)
		{
			tok = tc_strtok(InclGates, ",");
			while (tok != NULL)
			{
				lstIncl.push_back(tok);
				tok = tc_strtok(NULL, ",");
			}
		}

		if (ExclGates != NULL)
		{
			tok = tc_strtok(ExclGates, ",");
			while (tok != NULL)
			{
				lstExcl.push_back(tok);
				tok = tc_strtok(NULL, ",");
			}
		}

		int			iAllTasks			= 0;
		Teamcenter::scoped_smptr<tag_t>	ptAllTaskTags;

		CHECK_ITK(iRetCode,EPM_ask_sub_tasks(tRootTask,&iAllTasks,&ptAllTaskTags));
		for(int iLoopAllTasks = 0 ; iLoopAllTasks < iAllTasks; iLoopAllTasks++)
		{
			// Check include/exclude gates
			Teamcenter::scoped_smptr<char> taskName;
			CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks], "object_name", &taskName));
			if (lstExcl.size() > 0 && is_contain(taskName.getString(), lstExcl))
			{
				continue;
			}
			else if (lstIncl.size() > 0 && !is_contain(taskName.getString(), lstIncl))
			{
				continue;
			}

			//char		*pcTaskState		= NULL;
			Teamcenter::scoped_smptr<char> pcTaskState;

			CHECK_ITK(iRetCode,AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks],"task_state",&pcTaskState));
			/*if any of the task state is pending then do not start the task*/
			if(tc_strcmp("Started", pcTaskState.getString()) == 0)
			{
				decision = EPM_nogo;
				break;
			}
			else
			{
				continue;
			}
		}
	}

	if (FlagProp != NULL)
	{
		// Log execution time and decision
		auto now = std::chrono::system_clock::now();
		std::time_t time = std::chrono::system_clock::to_time_t(now);
		char strTime[20]; //yymmdd.hhmmss
		std::strftime(strTime, std::size(strTime), "%y%m%d.%H%M%S", std::localtime(&time));
		const char* strDeci = decision == EPM_go ? " - Go" : " - No Go";

		//??? Fail to write info to task
		CHECK_ITK(iRetCode, AOM_set_value_string(msg.task, FlagProp, strcat(strTime, strDeci)));
	}

	//??? Free memory
	SAFE_SM_FREE(InclGates);
	SAFE_SM_FREE(ExclGates);
	SAFE_SM_FREE(FlagProp);
	SAFE_SM_FREE(FlagVal);

	return decision;
}

boolean is_contain(const char* str, vector<string> lst)
{
	return lst.size() > 0 && std::find(lst.begin(), lst.end(), str) != lst.end();
}