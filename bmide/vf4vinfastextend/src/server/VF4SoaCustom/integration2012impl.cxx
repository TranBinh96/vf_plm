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

#include <integration2012impl.hxx>
#include <me/me.h>
#include <qry/qry.h>
#include <tc/tc_startup.h>
#include <fclasses\tc_string.h>
#include <user_exits\epm_toolkit_utils.h>

#define CHECK_FAIL(x)																													\
{\
	ifail += x; \
	if (x != ITK_ok)																								\
	{																															\
		char *error_str = NULL;																								\
		EMH_ask_error_text(x, &error_str);																			\
		TC_write_syslog("ERROR: %d, ERROR MSG: %s. at Line: %d in File: %s\n", x, error_str, __LINE__, __FILE__);	\
		SAFE_SM_FREE(error_str);																								\
	}																															\
}

using namespace VF4::Soa::Custom::_2020_12;
using namespace Teamcenter::Soa::Server;

IntegrationImpl::StructureSearchInMPPOutput IntegrationImpl::structureSearchInMPP ( const StructureSearchInMPPInput& input )
{
    int ifail = ITK_ok;
	IntegrationImpl::StructureSearchInMPPOutput response;

	TC_write_syslog("ENTER %s \n", __FUNCTION__);

	try
	{
		tag_t searchScope = input.searchScope;
		std::string queryName = input.queryName;
		std::vector<string> queryCriteria(input.queryCriteria);
		std::vector<string> queryCriteriaValues(input.queryCriteriaValues);
		TC_write_syslog("MPPSearch queryCriteria size %d \n", queryCriteria.size());
		if( queryCriteria.size() ==  queryCriteriaValues.size())
		{
			tag_t tagItemIDQuery = NULLTAG;
			if (searchScope == NULLTAG)
			{
				TC_write_syslog("Search Scope is NULL...");
				response.errorString.assign("Search Scope is NULL...");
				return response;
			}
			// structure search uses Saved Query and/or Occ Note expression
			CHECK_FAIL(QRY_find2(queryName.c_str(), &tagItemIDQuery));
			if (tagItemIDQuery == NULLTAG)
			{
				TC_write_syslog("Query [%s] doesn't exist.",queryName.c_str());
				response.errorString.assign("Query doesn't exist");
				return response;
			}
			int n_entries = 0;
			n_entries = queryCriteria.size();

			Teamcenter::scoped_ptr<char*, Teamcenter::StringArrayFreer> entries, values;
			entries = (char **)MEM_alloc(sizeof(char*) * n_entries);
			values = (char **)MEM_alloc(sizeof(char*) * n_entries);
			for(int i = 0 ; i < n_entries ; i++)
			{
				entries[i] = MEM_string_copy(queryCriteria[i].c_str());
				values[i] = MEM_string_copy(queryCriteriaValues[i].c_str());
			}

			ME_saved_query_expression_s qry_exp;
			qry_exp.saved_qry_tag = tagItemIDQuery;
			qry_exp.num_entries = n_entries;
			qry_exp.entries = entries;
			qry_exp.values = values;

			ME_search_expression_set_t search_exp;
			search_exp.num_saved_query_expressions = 1;
			search_exp.saved_query_expressions = &qry_exp;
			search_exp.num_occ_note_expressions = 0;
			search_exp.occ_note_expressions = 0;
			ME_mfg_search_criteria_t search_criteria; // empty


			// execute structure search
			int nLines = 0;
			tag_t* tagBOMLines = NULL;
			CHECK_FAIL(ME_execute_structure_search(1, &searchScope, &search_exp, &search_criteria, &nLines, &tagBOMLines));
			TC_write_syslog("nLines %d\n",nLines);
			if(nLines > 0)
			{
				for(int ii = 0; ii < nLines; ii++)
				{
					response.foundLines.push_back(tagBOMLines[ii]);
				}
			}else
			{
				TC_write_syslog("No Bomlines found for given criteria and criteria value");
				response.errorString.assign("No Bomlines found for given criteria and criteria value");
				return response;
			}
		}else
		{
			TC_write_syslog("Input Query Criteria and Criteria values not matching.Please re-check once..");
			response.errorString.assign("Input Query Criteria and Criteria values not matching.Please re-check Input once..");
			return response;
		}
	}
	catch(const IFail &e)
	{
		std::cout << e.getMessage();
		response.serviceData.addErrorStack();
		TC_write_syslog("\n[VF]Error ::%s\n", e.getMessage());
	}
	TC_write_syslog("EXIT %s \n", __FUNCTION__);
	return response;
}
