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

#include <reportdatasource2012impl.hxx>
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
#include <tccore/item.h>
#include <tccore/aom.h>
#include <tccore/grm.h>
#include <tccore/uom.h>
#include <tccore/aom_prop.h>
#include <cfm/cfm.h>
#include <fclasses/tc_string.h>
#include <ps/ps.h>
#include <tc/emh.h>
#include <tc/preferences.h>
#include <tc/emh_errors.h>
#include <list>
#include <stack>
#include <map>
#include <set>
#include <fclasses/tc_date.h>
#include <epm/epm_toolkit_tc_utils.h>
#include <user_exits/epm_toolkit_utils.h>

using namespace std;
using namespace VF4::Soa::Custom::_2020_12;
using namespace Teamcenter::Soa::Server;


date_t getCurrentDate();
string btos(bool value);
logical evaluateFinalExp();
bool nottedResult(bool input);
string checkIfNotPresent(string str);
char * removeLeadingSpace(char *pcString);
string checkAndReplaceNotEqual(string str);
string checkAndAppendOpenBrack(string str);
string checkAndAppendClosedBrack(string str);
int getLogicalExpression(char* input1, char* input2);
bool isExprPresentInInput2(string exp, string input2);
int getPart(std::string partNumber, std::string partType, tag_t* tPart);
string getResultInString(bool firstOperand, bool secondOperand, string Operator);
void replaceAll(std::string& formula, const std::string& from, const std::string& to);
int getRootBOMLine(tag_t toplineTag , string toplineId ,string toplineType, ReportDataSourceImpl::Get150BomWithVariantsDSResponse &response);
int parseBOM(tag_t tBOMLineTag , vector<tag_t> &parentVarRule , string toplineId, ReportDataSourceImpl::Get150BomWithVariantsDSResponse &response);


int iSeqNoAttrb = 0;
int iLevelAttrb = 0;
int iItemIdAttrb = 0;
int iItemRevIdAttrb = 0;
int iVariantFormulaAttrb = 0;

string closedBrkts;
list <string> finalList;
map<tag_t, char*> savedVarMap;
vector<string> vFinalReport;
set<string> uniquePartID;
list<list<map<string,string>>> bom150;
map<string,string>SOURCING;
map<string,string>PFEP;
map<string, string> listPartID_SupplierCode;
map<string, map<string,string>> costingMasterData;
bool isNotPresent = false;
bool isNotEqualPresent = false;

struct PartReleaseInfo
{
    tag_t pPartRev = NULLTAG;
    tag_t iPartRev = NULLTAG;
    tag_t prPartRev = NULLTAG;
	tag_t pprPartRev = NULLTAG;
	tag_t pprPart = NULLTAG;
    bool isInEcrCombineReleaseP = false;
    bool isInEcrCombineReleaseI = false;
    bool isInEcrCombineReleasePR = false;
	date_t pReleaseDate = NULLDATE;
    date_t iReleaseDate = NULLDATE;
    date_t prReleaseDate = NULLDATE;
	date_t pprReleaseDate = NULLDATE;
};
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
#define DATE_IS_NULL(d1	)\
	((d1.year == NULLDATE.year ) ?                  \
	((d1.month == NULLDATE.month) ?               \
	((d1.day == NULLDATE.day  ) ?             \
	((d1.hour == NULLDATE.hour) ?           \
	((d1.minute  == NULLDATE.minute) ?      \
	((d1.second  == NULLDATE.second) ? 1  \
	: 0) \
	: 0)   \
	: 0)        \
	: 0)          \
	: 0)            \
	: 0)
ReportDataSourceImpl::Get150BomWithVariantsDSResponse ReportDataSourceImpl::get150BomWithVariantsDS ( const std::vector< Get150BomWithVariantsDSInput >& inputs )
{
	int ifail = ITK_ok;
	ReportDataSourceImpl::Get150BomWithVariantsDSResponse response;
	TC_write_syslog("[VF] ENTER %s", __FUNCTION__);


	TC_write_syslog("*********************************************************************\n");
	TC_write_syslog("***                                                               ***\n");
	TC_write_syslog("***	    150BomWithVariantsDS Service Log                       ***\n");
	TC_write_syslog("***                                                               ***\n");
	TC_write_syslog("*********************************************************************\n");

	time_t now = time(0);
	char* date_time = ctime(&now);
	TC_write_syslog("Start Time Processing BOM: %s\n", date_time);
	TC_write_syslog("Size of Input Vector : %d\n", inputs.size());
	if (inputs.size() > 0)
	{

		for (Get150BomWithVariantsDSInput variantInput : inputs)
		{
			try
			{
				tag_t topNodeTag = variantInput.toplineTag;
				string topNodeId = variantInput.topItemId;
				string topNodeType = variantInput.topItemType;

				ifail = getRootBOMLine(topNodeTag ,topNodeId ,topNodeType ,response);

			}
			catch(const IFail &e)
			{
				std::cout << e.getMessage();
				response.serviceData.addErrorStack();
				TC_write_syslog("[[VF]]Error ::%s\n", e.getMessage());
			}
		}
	}
	time_t eNow = time(0);
	char* endDateTime = ctime(&eNow);
	TC_write_syslog("End Time Processing BOM: %s\n", endDateTime);
	TC_write_syslog("[VF] LEAVE %s", __FUNCTION__);
	return response;
}

int getRootBOMLine(tag_t toplineTag , string toplineId ,string toplineType, ReportDataSourceImpl::Get150BomWithVariantsDSResponse &response)
{
	TC_write_syslog("[VF] ENTER %s", __FUNCTION__);

	int ifail = ITK_ok;
	int iIndex = 0;
	int iChildCount = 0;
	int varCount = 0;
	int RevCount = 0;

	tag_t tTopLineTag = toplineTag;

	Teamcenter::scoped_smptr<tag_t> tChildLines;
	Teamcenter::scoped_smptr<tag_t> tSavedVar;
	Teamcenter::scoped_smptr<tag_t> tProductRev;

	tag_t tItemTag = NULLTAG;

	tag_t relTag = NULLTAG;
	tag_t varRelTag = NULLTAG;

	char* variantObjectType = NULL;
	char* revType = NULL;
	char* variantName = NULL;
	char* variantRuleText = NULL;
	char* toplineLevel = NULL;

	ReportDataSourceImpl::Get150BomWithVariantsDSOutput outputStatus;
	vector<tag_t> pVariantRule;
	//string pVariantRule;

	//finding the configurator object_name
	if(tc_strcmp(toplineId.c_str(), "NO_EVALUATE") == 0)
	{
		CHECK_FAIL(AOM_ask_value_tag(tTopLineTag, "bl_item", &tItemTag));
	}
	else if(toplineId.empty() && toplineType.empty())
	{
		CHECK_FAIL(AOM_ask_value_tag(tTopLineTag, "bl_item", &tItemTag));
	}else
	{
		ifail = getPart(toplineId, toplineType, &tItemTag);
		if (tItemTag == NULLTAG)
		{
			TC_write_syslog("BOM.traverseBOM TopNode item[%s]is null \n", toplineId);
			return ifail;
		}
	}
	CHECK_FAIL(GRM_find_relation_type("Smc0HasVariantConfigContext", &varRelTag));
	CHECK_FAIL(GRM_list_secondary_objects_only(tItemTag, varRelTag, &RevCount, &tProductRev));
	for(int p = 0 ; p < RevCount ; p++ )
	{
		CHECK_FAIL(AOM_ask_value_string(tProductRev[p],"object_type",&revType));

		//finding the saved variant
		if(tc_strcmp(revType,"Cfg0ProductItem") == 0)
		{
			CHECK_FAIL(GRM_find_relation_type("IMAN_reference", &relTag));
			CHECK_FAIL(GRM_list_secondary_objects_only(tProductRev[p], relTag, &varCount, &tSavedVar));
			for(int v = 0 ; v < varCount ; v++ )
			{
				CHECK_FAIL(AOM_ask_value_string(tSavedVar[v],"object_type",&variantObjectType));
				CHECK_FAIL(AOM_ask_value_string(tSavedVar[v],"object_name",&variantName));
				if(tc_strcmp(variantObjectType,"VariantRule") == 0)
				{
					CHECK_FAIL(AOM_ask_value_string(tSavedVar[v],"cfg0VariantRuleText",&variantRuleText));
					savedVarMap.insert(pair<tag_t,char*>(tSavedVar[v],variantRuleText));
					pVariantRule.push_back(tSavedVar[v]);
					outputStatus.variantRule.push_back(tSavedVar[v]);

				}
			}
		}
	}

	CHECK_FAIL(BOM_line_look_up_attribute(bomAttr_itemId, &iItemIdAttrb));
	CHECK_FAIL(BOM_line_look_up_attribute(bomAttr_itemRevId, &iItemRevIdAttrb));
	CHECK_FAIL(BOM_line_look_up_attribute(bomAttr_variantFormula, &iVariantFormulaAttrb));
	CHECK_FAIL(BOM_line_look_up_attribute("bl_level_starting_0", &iLevelAttrb));
	CHECK_FAIL(BOM_line_look_up_attribute(bomAttr_occSeqNo, &iSeqNoAttrb));

	CHECK_FAIL(BOM_line_ask_child_lines(tTopLineTag, &iChildCount, &tChildLines));
	ifail = BOM_line_ask_attribute_string(tTopLineTag, iLevelAttrb, &toplineLevel);

	outputStatus.level.assign(toplineLevel);
    outputStatus.bomlineTag = tTopLineTag;
	response.outputs.push_back(outputStatus);
	for (iIndex=0; iIndex<iChildCount; iIndex++)
	{
		ifail = parseBOM(tChildLines[iIndex],pVariantRule,toplineId,response);
	}

	SAFE_SM_FREE(variantObjectType);
	SAFE_SM_FREE(revType);
	SAFE_SM_FREE(variantName);
	SAFE_SM_FREE(variantRuleText);

	TC_write_syslog("[VF] LEAVE %s", __FUNCTION__);
	return ifail;
}
int parseBOM(tag_t tBOMLineTag , vector<tag_t> &parentVarRule , string toplineId, ReportDataSourceImpl::Get150BomWithVariantsDSResponse &response)
{
	int ifail = ITK_ok;
	int iIndex = 0;
	int iChildCount = 0;

	bool lineCongfigured = false;
	bool lineFlag = false;

	char* sLevel = NULL;
	char* sVariantFormula = NULL;

	Teamcenter::scoped_smptr<tag_t> tChildLines;

	ReportDataSourceImpl::Get150BomWithVariantsDSOutput outputStatus;

	vector<tag_t> vartRule;
	string outputValue;
	string tempVariantRule;

	map<tag_t, char*>::iterator itr;

	ifail = BOM_line_ask_attribute_string(tBOMLineTag , iVariantFormulaAttrb, &sVariantFormula);
	ifail = BOM_line_ask_attribute_string(tBOMLineTag, iLevelAttrb, &sLevel);

	if(tc_strcmp(toplineId.c_str(), "NO_EVALUATE") != 0)
	{
		if((sVariantFormula != NULL) && (sVariantFormula[0] != '\0'))
		{
			for (int j=0; j<parentVarRule.size(); j++)
			{
				 for (itr = savedVarMap.begin(); itr != savedVarMap.end(); ++itr)
				{
					tag_t variantTag;
					string variantValue;
					string sInput;
					string notCheckedString;
					variantTag = itr->first;
					variantValue = itr->second;

					if ( parentVarRule[j] == variantTag )
					{
						sInput = sVariantFormula;
						notCheckedString = checkIfNotPresent(sInput);
						ifail = getLogicalExpression((char*)notCheckedString.c_str(), (char*)variantValue.c_str());
						logical fResult =evaluateFinalExp();
						bool finalResult = nottedResult(fResult);
						if(finalResult)
						{
							lineCongfigured = true;
							vartRule.push_back(variantTag);
						}
						break;
					}
				}
			}
		}
		else
		{
			lineFlag = true;
			vartRule = parentVarRule;
		}

		if(!lineCongfigured && !lineFlag)
			vartRule.push_back(NULLTAG);

	}

	outputStatus.level.assign(sLevel);
	tag_t temp = tBOMLineTag;
    outputStatus.bomlineTag = temp;

	for(int p = 0; p < vartRule.size(); p++ )
	{
		outputStatus.variantRule.push_back(vartRule[p]);
	}
	response.outputs.push_back(outputStatus);

	SAFE_SM_FREE(sLevel);
	SAFE_SM_FREE(sVariantFormula);

	ifail = BOM_line_ask_child_lines(tBOMLineTag, &iChildCount, &tChildLines);
	for (iIndex=0; iIndex<iChildCount; iIndex++)
	{
		ifail = parseBOM(tChildLines[iIndex], vartRule, toplineId, response);
	}

	return ifail;
}

string checkIfNotPresent(string str)
{
	char * token = NULL;
	string temp;
	stripBlanks((char*)str.c_str());
	token = removeLeadingSpace((char*)str.c_str());
	temp = token;
	string firstThreeChar = temp.substr(0, 3);
	if(firstThreeChar.compare("NOT") == 0)
	{
		isNotPresent = true;
		return temp.substr(3, temp.length()-1);
	}
	else
	{
		return temp;
	}
}
bool nottedResult(bool input)
{
	if(isNotPresent)
	{
		isNotPresent = false;
		return !input;
	}
	else
	{
		return input;
	}

}
string checkAndAppendOpenBrack(string str)
{
	string input = str;
	while(input.at(0) =='(')
	{
		finalList.push_back("(");
		input.erase(input.begin() + 0);
	}
	return input;
}
string checkAndAppendClosedBrack(string str)
{
	string input = str;
	while(input.at(input.length()-1) ==')')
	{
		if(closedBrkts.empty())
		closedBrkts.assign(")");
		else
		closedBrkts.append(")");
		input.erase(input.length()-1);
	}
	return input;
}
bool isExprPresentInInput2(string exp, string input2)
{
	size_t anyFound = exp.find(" = Any");
	if (anyFound != string::npos)
	{
		return true;
	}
	size_t found = input2.find(exp);
	if(isNotEqualPresent)
	{
		if (found != string::npos)
		{
			isNotEqualPresent= false;
			return false;
		}
		else
		{
			isNotEqualPresent= false;
			return true;
		}
	}else
	{
		if (found != string::npos)
		{
			return true;
		}
		else
		{
			char *token = strtok((char*)exp.c_str(), "=");
			size_t substr = input2.find(token);
			if (substr != string::npos)
			{
				return false;
			}
			else{
				return false;
			}
		}
	}
}
string checkAndReplaceNotEqual(string str)
{
	size_t found = str.find("!");
    if (found != string::npos)
	{
		replaceAll(str,"!","");
		isNotEqualPresent = true;
		return str;
	}
	else
	{
	    return str;
	}
}
int getLogicalExpression(char* input1, char* input2)
{
	bool isItClosedBrkt = false;
	vector<string> orSplitted;

	int ifail = 0;
	string input = input1;

	replaceAll(input, " OR ", "#");
	replaceAll(input, " AND ", "$");
	char *token = strtok((char*)input.c_str(), "#");
    while (token != NULL)
    {
		stripBlanks(token);
		token = removeLeadingSpace(token);
		orSplitted.push_back(token);
		token = strtok(NULL, "#");
    }
	for(int i=0; i<=orSplitted.size()-1; i++)
	{
		vector<string> andSplitted;
		char *token = strtok((char*)orSplitted[i].c_str(), "$");
		while (token != NULL)
		{
			stripBlanks(token);
			token = removeLeadingSpace(token);
			andSplitted.push_back(token);
			token = strtok(NULL, "$");
		}
		for(int j=0; j<=andSplitted.size()-1; j++)
		{
			string doOpr = checkAndReplaceNotEqual(andSplitted[j].c_str());
			bool flag = false;
			if(doOpr.at(0) == '(')
			{
				string brktRvmdOut = checkAndAppendOpenBrack(doOpr);
				flag = isExprPresentInInput2(brktRvmdOut, input2);

			}else if(doOpr.at(doOpr.length()-1) == ')')
			{
				string brktRvmdOut =  checkAndAppendClosedBrack(doOpr);
				isItClosedBrkt = true;
				flag = isExprPresentInInput2(brktRvmdOut, input2);
			}else
			{
				flag = isExprPresentInInput2(doOpr, input2);
			}


			if(flag) {
				finalList.push_back("true");
				}
			else {
				finalList.push_back("false");
				}

			if(isItClosedBrkt)
			{
				finalList.push_back(closedBrkts);
				closedBrkts.erase();
			}
			isItClosedBrkt = false;

			if(j != andSplitted.size()-1) {
				finalList.push_back("$");
			}


		}
		if(i != orSplitted.size()-1) {
			finalList.push_back("#");
		}
	}
	return ifail;
}
char * removeLeadingSpace(char *pcString)
{
	int iInx=0;
	int iLength=0;
	if( pcString !=NULL )iLength=strlen(pcString);
	if(( pcString == NULL ) || ( iLength == 0)) return ( NULL );
	while(pcString[iInx] == ' ')
	{
		iInx++;
	}
	return (&pcString[iInx]);
}
logical evaluateFinalExp()
{
	stack<string> stack;
	logical firstOperand ;
	logical secondOperand ;
	string sOperator;

	string isItOpenBrk;
	logical isClosedBrkFound;

	string tempValue;
	logical finalresult = false;
	for(string singleData : finalList)
	{
		if(singleData.compare(")") == 0) {
			isClosedBrkFound = false;
			while(!isClosedBrkFound) {
				STRNG_is_logical(stack.top().c_str(),&firstOperand);
				stack.pop();
				sOperator = stack.top();
				stack.pop();
				STRNG_is_logical(stack.top().c_str(),&secondOperand);
				stack.pop();
				isItOpenBrk = stack.top().c_str();
				stack.pop();

				if(!isItOpenBrk.compare("(") == 0)
				{
					stack.push(isItOpenBrk);
					isClosedBrkFound = false;
				}
				else
				{
					isClosedBrkFound = true;
				}
				string logicResult = getResultInString(firstOperand, secondOperand, sOperator);
				stack.push(logicResult);
			}
		}else
		{
			stack.push(singleData);
		}
	}

	while(stack.size() >=2 ) {
		STRNG_is_logical(stack.top().c_str(),&firstOperand);
		stack.pop();
		sOperator = stack.top();
		stack.pop();
		STRNG_is_logical(stack.top().c_str(),&secondOperand);
		stack.pop();
		string logicResult = getResultInString(firstOperand, secondOperand, sOperator);
		stack.push(logicResult);
	}
	tempValue = stack.top();
	stack.pop();
	STRNG_is_logical(tempValue.c_str(),&finalresult);

	finalList.clear();
	return finalresult;
}

string getResultInString(bool firstOperand, bool secondOperand, string Operator)
{
	bool result = false;
	if(Operator.compare("$") == 0)
	{
		result = firstOperand && secondOperand;
	}else if(Operator.compare("#")== 0)
	{
		result = firstOperand || secondOperand;
	}
	return btos(result);
}
std::string btos(bool value)
{
	if(value == false)
	  return "false";
	else
	  return "true";
}
void replaceAll(std::string& formula, const std::string& from, const std::string& to)
{
    if(from.empty())
        return;
    size_t start_pos = 0;
    while((start_pos = formula.find(from, start_pos)) != std::string::npos) {
        formula.replace(start_pos, from.length(), to);
        start_pos += to.length(); // In case 'to' contains 'from', like replacing 'x' with 'yx'
    }
}
ReportDataSourceImpl::ProcessStatusReportResponse ReportDataSourceImpl::getProcessStatusReport ( const ProcessStatusReportInput& input )
{
	ReportDataSourceImpl::ProcessStatusReportResponse response;
	TC_write_syslog("[VF] ENTER %s", __FUNCTION__);

	int iCount = 0;
	tag_t tQuery = NULLTAG;
	map<string, string> queryMap;
	map<string, string> mapProcess2RootTargetType;
	map<string, string> lastestProcess;
	map<string, ReportDataSourceImpl::ProcessStatusReportOutput> finalReport;
	const char* queryName = "__TNH_JobNameQuery";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	try
	{
		std::string State = "";
		std::string wfTemplate = input.workflowTemplate ;
		std::string creationDateBefore = input.creationDateBefore ;
		std::string creationDateAfter = input.creationDateAfter;
		std::string modifyDateBefore = input.modifyDateBefore;
		std::string modifyDateAfter = input.modifyDateAfter;
		std::string jobName = input.processName;
		bool isrunningProccess = input.runningProccess;

		if(isrunningProccess)
		{
			State.assign("4");
		}else
		{
			State.assign("");
		}
		CHECK_FAIL(QRY_find2(queryName, &tQuery));
		if (tQuery == NULLTAG)
		{
			TC_write_syslog("[VF]ERROR: Cannot find query %s:\n", queryName);
			return response;
		}

		if(tc_strcmp(wfTemplate.c_str(), "") != 0)
		{
			queryMap.insert(pair<string, string>("Workflow Template", wfTemplate));
		}
		if(tc_strcmp(creationDateBefore.c_str(), "") != 0)
		{
			queryMap.insert(pair<string, string>("Created Before", creationDateBefore));
		}
		if(tc_strcmp(creationDateAfter.c_str(), "") != 0)
		{
			queryMap.insert(pair<string, string>("Created After", creationDateAfter));
		}
		if(tc_strcmp(modifyDateBefore.c_str(), "") != 0)
		{
			queryMap.insert(pair<string, string>("Modified Before", modifyDateBefore));
		}
		if(tc_strcmp(modifyDateAfter.c_str(), "") != 0)
		{
			queryMap.insert(pair<string, string>("Modified After", modifyDateAfter));
		}
		if(tc_strcmp(jobName.c_str(), "") != 0)
		{
			queryMap.insert(pair<string, string>("Job Name", jobName));
		}
		if(tc_strcmp(State.c_str(), "") != 0)
		{
			queryMap.insert(pair<string, string>("State", State));
		}

		int n_entries = 0;
		n_entries = queryMap.size();

		char **queryEntries = NULL;
		char **queryValues = NULL;

		queryEntries = (char **) MEM_alloc(n_entries * sizeof(char *));
		queryValues  = (char **) MEM_alloc(n_entries * sizeof(char *));

		int ii = 0;
		map<string, string>::iterator itr;
		for (itr = queryMap.begin(); itr != queryMap.end(); ++itr) {
			queryEntries[ii] = (char *)MEM_alloc(strlen( itr->first.c_str()) + 1);
			strcpy(queryEntries[ii], itr->first.c_str() );

			queryValues[ii] = (char *)MEM_alloc( strlen( itr->second.c_str()) + 1);
			strcpy(queryValues[ii], itr->second.c_str());

			ii++;
		}
		CHECK_FAIL(QRY_execute(tQuery, n_entries, queryEntries, queryValues, &iCount, &tQryResults));
		if (iCount == 0)
		{
			TC_write_syslog("[VF]There are no processes found\n");
			return response;
		}
		TC_write_syslog("__TNH_JobNameQuery result Count[%d]..\n",iCount);

		//reading pref for object Type
		int valueCount = 0;
		Teamcenter::scoped_smptr<char*> prefValues;

		CHECK_FAIL(PREF_ask_char_values("VINFAST_PROCESS_STATUS_REPORT_2", &valueCount, &prefValues));
		for(int iLoopPref = 0; iLoopPref < valueCount; iLoopPref++)
		{
			int  typeCount	= 0;
			Teamcenter::scoped_smptr<char*> typeValue;

			CHECK_FAIL(EPM__parse_string(prefValues[iLoopPref],",",&typeCount,&typeValue));
			mapProcess2RootTargetType.insert(pair<string, string>(typeValue[0], typeValue[1]));
		}
		for(int i = 0; i < iCount; i++)
		{
			int targetCount = 0;
			string targets	= "";
			bool targetFlag = false;
			tag_t rootTask = NULLTAG;
			tag_t tOwningUser = NULLTAG;
			tag_t tOwningGroup = NULLTAG;

			date_t creationDate_t = NULLDATE;
			date_t pEndDateDate_t = NULLDATE;
			Teamcenter::scoped_smptr<tag_t> rootTarget;


			Teamcenter::scoped_smptr<char> wfTemplate;
			Teamcenter::scoped_smptr<char> description;
			Teamcenter::scoped_smptr<char> moduleGroup;
			Teamcenter::scoped_smptr<char> owningUser;
			Teamcenter::scoped_smptr<char> owningGroup;
			Teamcenter::scoped_smptr<char> creationDate;

			Teamcenter::scoped_smptr<char> objType;
			Teamcenter::scoped_smptr<char> itemID;
			Teamcenter::scoped_smptr<char> itemRevision;
			Teamcenter::scoped_smptr<char> itemName;



			CHECK_FAIL(EPM_ask_root_task(tQryResults[i], &rootTask));
			CHECK_FAIL(AOM_ask_value_string(rootTask,"object_name", &wfTemplate));
			CHECK_FAIL(EPM_ask_attachments(rootTask,EPM_target_attachment, &targetCount, &rootTarget));
			if(rootTarget == NULLTAG || targetCount == 0)
			{
				continue;
			}
			for (int iLoopTarget = 0; iLoopTarget < targetCount;iLoopTarget++) {
				Teamcenter::scoped_smptr<char> objectType;
				CHECK_FAIL(AOM_ask_value_string(rootTarget[iLoopTarget],"object_type", &objectType));
				map<string, string>::iterator itrTarget;
				itrTarget = mapProcess2RootTargetType.find(wfTemplate.getString());
				if (itrTarget != mapProcess2RootTargetType.end()) {
					if (itrTarget->second.find(';') != std::string::npos) {
						int count = 0;
						Teamcenter::scoped_smptr<char*> val;

						CHECK_FAIL(EPM__parse_string(itrTarget->second.c_str(),";", &count, &val));
						for (int i = 0; i < count; i++) {
							if (tc_strcmp(objectType.getString(), val[i]) == 0) {
								targetFlag = true;
								rootTarget[0] = rootTarget[iLoopTarget];
								break;
							}
						}
						if (targetFlag) {
							break;
						}
					}
					else
					{
						if (tc_strcmp(objectType.getString(),itrTarget->second.c_str()) == 0) {
							targetFlag = true;
							rootTarget[0] = rootTarget[iLoopTarget];
							break;
						}
					}
				}
			}
			if(!targetFlag)
			{
				continue;
			}
			CHECK_FAIL(AOM_ask_value_string(rootTarget[0],"object_type", &objType));
			if(tc_strcmp(objType.getString(),"VF4_line_itemRevision") ==0) {
				CHECK_FAIL(AOM_ask_value_string(rootTarget[0],"vf4_bom_vfPartNumber", &itemID));
			}else
			{
				CHECK_FAIL(AOM_ask_value_string(rootTarget[0],"item_id", &itemID));
			}
			CHECK_FAIL(AOM_ask_value_string(rootTarget[0],"item_revision_id", &itemRevision));
			CHECK_FAIL(AOM_ask_value_string(rootTarget[0],"object_name", &itemName));

			CHECK_FAIL(AOM_ask_value_tag(rootTask,"owning_user", &tOwningUser));
			CHECK_FAIL(AOM_ask_value_string(tOwningUser,"object_string", &owningUser));
			CHECK_FAIL(AOM_ask_value_tag(rootTask,"owning_group", &tOwningGroup));
			CHECK_FAIL(AOM_ask_value_string(tOwningGroup,"full_name", &owningGroup));

			int n_statuses = 0;
			Teamcenter::scoped_smptr<tag_t> statuses;
			Teamcenter::scoped_smptr<char> taskState;
			string status = "";
			CHECK_FAIL(AOM_ask_value_string(rootTask,"task_state", &taskState));
			CHECK_FAIL(EPM_ask_attachments(rootTask, EPM_release_status_attachment, &n_statuses, &statuses));
			for(int iLoopStatus = 0; iLoopStatus < n_statuses; iLoopStatus++)
			{
				Teamcenter::scoped_smptr<char> name;
				CHECK_FAIL(AOM_ask_value_string(statuses[iLoopStatus], "name", &name));
				if(status.length() > 0)
				{
					status.append(";");
				}
				status.append(name.getString());

			}
			
			
			//check ECR obsolete

			string name_obsolete = "";
			tag_t obso = NULLTAG;
			Teamcenter::scoped_smptr<char> namerelation;
			int iobso = 0;
			tag_t* tagObsolete = NULLTAG;
			CHECK_FAIL(ITEM_find_rev(itemID.getString(), itemRevision.getString(), &obso));
			CHECK_FAIL(WSOM_ask_release_status_list(obso, &iobso, &tagObsolete));
			for (int c = 0; c < iobso; c++)
			{
				CHECK_FAIL(AOM_ask_name(tagObsolete[c], &namerelation));
				if(tc_strcmp(namerelation.getString(), "Obsolete") == 0){
					name_obsolete.append(namerelation.getString());
				}

			}
			
			CHECK_FAIL(AOM_ask_value_date(rootTask,"fnd0EndDate", &pEndDateDate_t));
			int isDateNull =1;
			CHECK_FAIL(AOM_ask_value_date(rootTask,"creation_date", &creationDate_t));
			isDateNull = DATE_IS_NULL(creationDate_t);
			if(isDateNull == 0)
			{
				CHECK_FAIL(DATE_date_to_string (creationDate_t, "%d-%b-%Y %H:%M:%S", &creationDate));
			}

			/*storage latest process*/
			std::map<string,string>::iterator it;
			it = lastestProcess.find(itemID.getString());
			if (it != lastestProcess.end())
			{
				int result = 0;
				logical isValid = false;
				date_t reqDate = NULLDATE;

				CHECK_FAIL(DATE_string_to_date_t((char*)it->second.c_str(), &isValid, &reqDate));
				CHECK_FAIL(POM_compare_dates(reqDate, creationDate_t, &result));
				if(result > 0)
				{
					continue;
				}else
				{
					it->second = creationDate.getString();
				}
			}else {
				lastestProcess.insert(pair<string, string>(itemID.getString(), creationDate.getString()));
			}
			for (int x = 0; x < targetCount; x++)
			{
				tag_t item = NULLTAG;
				Teamcenter::scoped_smptr<char> objectType;
				Teamcenter::scoped_smptr<char> objectString;

				CHECK_FAIL(AOM_ask_value_string(rootTarget[x],"object_type", &objectType));
				if (tc_strcmp(objectType.getString(),"Vf6_ECNRevision") == 0 || tc_strcmp(objectType.getString(),"Vf6_ECRRevision") == 0 || tc_strcmp(objectType.getString(),"VF4_ERNRevision") == 0)
				{
					CHECK_FAIL(AOM_ask_value_string(rootTarget[x],"object_desc", &description));
					CHECK_FAIL(ITEM_ask_item_of_rev(rootTarget[x], &item));
					if(tc_strcmp(objectType.getString(),"VF4_ERNRevision") == 0) {
						CHECK_FAIL(AOM_ask_value_string(rootTarget[x],"vf4_main_module", &moduleGroup));
					}else{
					CHECK_FAIL(AOM_ask_value_string(item,"vf6_module_group", &moduleGroup));
					}
				}
				if(tc_strcmp(objectType.getString(),"VF4_DesignRevision") == 0 ||
					tc_strcmp(objectType.getString(),"VF4_EBUSDesignRevision") == 0 ||
					tc_strcmp(objectType.getString(),"VF4_Car_LineItmRevision") == 0 ||
					tc_strcmp(objectType.getString(),"Vf6_ECNRevision") == 0 ||
					tc_strcmp(objectType.getString(),"Vf6_ECRRevision") == 0 ||
					tc_strcmp(objectType.getString(),"VF4_ERNRevision") == 0 ||
					tc_strcmp(objectType.getString(),"VF4_BP_DesignRevision") == 0 ||
					tc_strcmp(objectType.getString(),"VF4_BatteryLineRevision") == 0){
					if(targets.length() > 0) {
						targets.append("\n");
					}
					CHECK_FAIL(AOM_ask_value_string(rootTarget[x],"object_string", &objectString));
					targets.append(objectString.getString());
				}
			}
			int subtaskNum = 0;
			Teamcenter::scoped_smptr<tag_t> subTask;
			CHECK_FAIL(EPM_ask_sub_tasks(rootTask, &subtaskNum, &subTask));
			for(int y = 0; y < subtaskNum; y++)
			{
				tag_t tPerformer = NULLTAG;
				string subtaskStatus = "";
				date_t dueDate_t = NULLDATE;
				date_t startDate_t = NULLDATE;
				date_t completedDate_t = NULLDATE;
				Teamcenter::scoped_smptr<char> dueDate;
				Teamcenter::scoped_smptr<char> subTaskName;
				Teamcenter::scoped_smptr<char> subTaskStatus;
				Teamcenter::scoped_smptr<char> subTaskResult;
				Teamcenter::scoped_smptr<char> resParty;
				Teamcenter::scoped_smptr<char> startDate;
				Teamcenter::scoped_smptr<char> completedDate;
				Teamcenter::scoped_smptr<char> taskType;
				Teamcenter::scoped_smptr<char> performer;

				ReportDataSourceImpl::ProcessStatusReportOutput outputStatus;

				CHECK_FAIL(AOM_ask_value_string(subTask[y],"task_type", &taskType));
				if(tc_strcmp(taskType.getString(), "") != 0)
				{
					if (tc_strcmp(taskType.getString(),"EPMOrTask") == 0 || tc_strcmp(taskType.getString(),"EPMTask") == 0 || tc_strcmp(taskType.getString(),"EPMAddStatusTask") == 0) {
						continue;
					}
				}
				/*get reviewer OR performer */
				string doneBy;
				string reviewerOrPerformer;
				string commentBuilder;
				string pendingUser;

				doneBy.assign("");
				reviewerOrPerformer.assign("");
				commentBuilder.assign("");
				pendingUser.assign("");

				if(tc_strcmp(taskType.getString(),"EPMReviewTask") == 0 || tc_strcmp(taskType.getString(),"EPMAcknowledgeTask") == 0)
				{
					int childCount = 0;
					Teamcenter::scoped_smptr<tag_t> childTasks;
					CHECK_FAIL(EPM_ask_sub_tasks(subTask[y], &childCount, &childTasks));
					for(int k = 0; k < childCount; k++)
					{
						int n_signoff = 0;
						Teamcenter::scoped_smptr<tag_t> signoffs;
						Teamcenter::scoped_smptr<char> childType;
						CHECK_FAIL(AOM_ask_value_string(childTasks[k],"task_type", &childType));
						if(tc_strcmp(childType.getString(),"EPMPerformSignoffTask") == 0)
						{
							CHECK_FAIL(EPM_ask_attachments(childTasks[k], EPM_signoff_attachment, &n_signoff, &signoffs));
							for(int h = 0; h < n_signoff ; h++)
							{
								tag_t mem_tag = NULLTAG;
								tag_t user_tag = NULLTAG;

								SIGNOFF_TYPE_t  memberType;
								Teamcenter::scoped_smptr<char> userId;
								Teamcenter::scoped_smptr<char> comments;
								EPM_signoff_decision_t decision;
								date_t decision_date = NULLDATE;
								if(reviewerOrPerformer.length() > 0) {
									reviewerOrPerformer.append(", ");
								}
								CHECK_FAIL(EPM_ask_signoff_member(signoffs[h],&mem_tag,&memberType));
								CHECK_FAIL(SA_ask_groupmember_user(mem_tag,&user_tag));
								CHECK_FAIL(AOM_ask_value_string(user_tag, "object_string", &userId));
								CHECK_FAIL(EPM_ask_decision(childTasks[k], user_tag, &decision, &comments, &decision_date));
								reviewerOrPerformer.append(userId.getString());

								if(tc_strcmp(comments.getString(), "") != 0) {
									if(commentBuilder.length() > 0) {
										commentBuilder.append(", ");
									}
									commentBuilder.append(userId.getString());
									commentBuilder.append(": ");
									commentBuilder.append(comments.getString());
								}
								if(decision != EPM_no_decision){
									if(doneBy.length() > 0) {
										doneBy.append(", ");
									}
									doneBy.append(userId.getString());
								}else {
									if(pendingUser.length() > 0) {
										pendingUser.append(", ");
									}
									pendingUser.append(userId.getString());
								}
							}
							int dueDateNull =1;
							CHECK_FAIL(AOM_ask_value_date(childTasks[k],"due_date", &dueDate_t));
							dueDateNull = DATE_IS_NULL(dueDate_t);
							if(dueDateNull == 0)
							{
								CHECK_FAIL(DATE_date_to_string (dueDate_t, "%d-%b-%Y %H:%M:%S", &dueDate));
							}
						}
					}
				}
				else
				{
					int dueDateNull =1;
					CHECK_FAIL(AOM_ask_value_tag(subTask[y],"fnd0Performer", &tPerformer));
					Teamcenter::scoped_smptr<char> comments;
					if(tPerformer != NULLTAG)
					{
						CHECK_FAIL(AOM_ask_value_string(tPerformer, "object_string", &performer));
						CHECK_FAIL(AOM_ask_value_string(subTask[y], "comments", &comments));
						commentBuilder.append(comments.getString());
						if(tc_strcmp(performer.getString(), "") != 0) {
							reviewerOrPerformer.append(performer.getString());
						}
						CHECK_FAIL(AOM_ask_value_date(subTask[y],"due_date", &dueDate_t));
						dueDateNull = DATE_IS_NULL(dueDate_t);
						if(dueDateNull == 0)
						{
							CHECK_FAIL(DATE_date_to_string (dueDate_t, "%d-%b-%Y %H:%M:%S", &dueDate));
						}
					}
				}
				//bool resetFlag = false;
				//CHECK_FAIL(AOM_ask_value_string(subTask[y],"object_name", &subTaskName));
				CHECK_FAIL(AOM_ask_value_string(subTask[y],"object_string", &subTaskName));

				CHECK_FAIL(AOM_ask_value_string(subTask[y],"task_state", &subTaskStatus));
				CHECK_FAIL(AOM_ask_value_string(subTask[y],"resp_party", &resParty));
				CHECK_FAIL(AOM_ask_value_date(subTask[y],"fnd0StartDate", &startDate_t));
				CHECK_FAIL(AOM_ask_value_date(subTask[y],"fnd0EndDate", &completedDate_t));
				CHECK_FAIL(AOM_ask_value_string(subTask[y],"task_result", &subTaskResult));

				/*start get task status*/
				std::string finalTaskStatus = "";
				if(DATE_IS_NULL(completedDate_t))
				{
					if(tc_strcmp(subTaskStatus.getString(), "Started") != 0)
					{
						finalTaskStatus = "Unassigned";
					}
					else
					{
						finalTaskStatus = "Started";
						date_t currentDate = getCurrentDate();
						if(tc_strcmp(dueDate.getString(), "") != 0){

								int result = 0;
								CHECK_FAIL(POM_compare_dates(currentDate, dueDate_t, &result));
								if(result == 1)
								{
									finalTaskStatus = "Late";
								}
						}
					}
				}
				else
				{
					if(tc_strcmp(subTaskResult.getString(), "Unset") == 0)
					{
						finalTaskStatus = subTaskStatus.getString();
					}
					else
					{
						finalTaskStatus = subTaskResult.getString();
					}
				}
				/*end get task status*/

				outputStatus.startDate = startDate_t;
				outputStatus.dueDate = dueDate_t;
				outputStatus.completedDate = completedDate_t;
				outputStatus.itemID.assign(itemID.getString());
				outputStatus.itemRevision.assign(itemRevision.getString());
				outputStatus.itemName.assign(itemName.getString());
				outputStatus.description.assign(description.getString());
				outputStatus.owningUser.assign(owningUser.getString());
				outputStatus.owningGroup.assign(owningGroup.getString());
				outputStatus.moduleGroup.assign(moduleGroup.getString());
				outputStatus.processStartDate = creationDate_t;
				outputStatus.processEndDate = pEndDateDate_t;
				if(tc_strcmp(taskState.getString(),"Started") == 0)
				{
					outputStatus.processStatus.assign("PENDING");
				}else if(tc_strcmp(taskState.getString(),"Completed") == 0 && status.find("Rejected") != string::npos)
				{
					outputStatus.processStatus.assign("REJECTED");
				}else if(tc_strcmp(taskState.getString(),"Completed") == 0 && status.find("Approved") != string::npos)
				{
					outputStatus.processStatus.assign("APPROVED");
				}else if(tc_strcmp(taskState.getString(),"Aborted") == 0){
					outputStatus.processStatus.assign("ABORT");
				}else{
					outputStatus.processStatus.assign("STUCK");
				}	
				outputStatus.wfTemplate.assign(wfTemplate.getString());
				outputStatus.targets.assign(targets);
				outputStatus.reviewerComment.assign(commentBuilder);
				outputStatus.reviewer.assign(doneBy);
				if(name_obsolete.find("Obsolete") != string::npos) {
					finalTaskStatus =name_obsolete;
					outputStatus.processStatus.assign("OBSOLETE");
				}
				outputStatus.taskStatus.assign(finalTaskStatus);
				outputStatus.resParty.assign(reviewerOrPerformer);
				outputStatus.subTaskName.assign(subTaskName.getString());
				if(tc_strcmp(finalTaskStatus.c_str(), "Late") == 0 || tc_strcmp(finalTaskStatus.c_str(), "Started") == 0)
				{
					if(tc_strcmp(pendingUser.c_str(), "") != 0) {
						outputStatus.pendingUser.assign(pendingUser);
					}else {
						outputStatus.pendingUser.assign(reviewerOrPerformer);
					}
				}
				string id = "";
				id.append("");
				id.append(itemID.getString());
				id.append(subTaskName.getString());
				finalReport.erase(id);
				finalReport.insert(pair<string,ReportDataSourceImpl::ProcessStatusReportOutput>(id,outputStatus));

			}
		}

		map<string,ReportDataSourceImpl::ProcessStatusReportOutput>::iterator pTtr;
		for (pTtr = finalReport.begin(); pTtr != finalReport.end(); ++pTtr)
		{
			response.outputs.push_back(pTtr->second);
		}

		if(n_entries > 0)
		{
			for (int ii = 0; ii < n_entries; ii++)
			{
				SAFE_SM_FREE(queryEntries[ii]);
				SAFE_SM_FREE(queryValues[ii]);
			}
			SAFE_SM_FREE(queryEntries);
			SAFE_SM_FREE(queryValues);
		}
	}
	catch(const IFail &e)
	{
		std::cout << e.getMessage();
		response.serviceData.addErrorStack();
		TC_write_syslog("[VF]Error ::%s\n", e.getMessage());
	}

	lastestProcess.clear();
	TC_write_syslog("[VF] EXIT %s", __FUNCTION__);
	return response;
}
date_t getCurrentDate()
{
	time_t now = time(0);
	date_t req_date = NULLDATE;
	logical isValid = false;
	struct tm ltm;

	time(&now);
	localtime_s(&ltm, &now);
	string currentDate="";

	int year = 1900 + ltm.tm_year;
	int monInt = 1 + ltm.tm_mon;
	int day = ltm.tm_mday;
	int hour = ltm.tm_hour;
	int min = ltm.tm_min;
	int sec = ltm.tm_sec;

	char* yearbuffer=NULL;
	char* daybuffer=NULL;
	char* hourbuffer=NULL;
	char* minbuffer=NULL;
	char* secbuffer=NULL;

	char* month=NULL;
	char* newdate=NULL;


	yearbuffer = (char *)MEM_alloc(sizeof(char) + 4 + 1);
	_itoa_s(year,yearbuffer,sizeof(yearbuffer),10);

	daybuffer = (char *)MEM_alloc(sizeof(char) + 2 + 1);
	_itoa_s(day,daybuffer,sizeof(daybuffer),10);

	hourbuffer = (char *)MEM_alloc(sizeof(char) + 2 + 1);
	_itoa_s(hour,hourbuffer,sizeof(hourbuffer),10);

	minbuffer = (char *)MEM_alloc(sizeof(char) + 2 + 1);
	_itoa_s(min,minbuffer,sizeof(minbuffer),10);

	secbuffer = (char *)MEM_alloc(sizeof(char) + 2 + 1);
	_itoa_s(sec,secbuffer,sizeof(secbuffer),10);

	monInt=monInt-1;

	if (monInt == 0)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("Jan") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "Jan");
	}
	else if (monInt == 1)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("Feb") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "Feb");
	}
	else if (monInt == 2)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("Mar") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "Mar");
	}
	else if (monInt == 3)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("Apr") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "Apr");
	}
	else if (monInt == 4)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("May") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "May");
	}
	else if (monInt == 5)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("Jun") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "Jun");
	}
	else if (monInt == 6)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("Jul") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "Jul");
	}
	else if (monInt == 7)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("Aug") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "Aug");
	}
	else if (monInt == 8)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("Sep") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "Sep");
	}
	else if (monInt == 9)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("Oct") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "Oct");
	}
	else if (monInt == 10)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("Nov") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "Nov");
	}
	else if (monInt == 11)
	{
		month = (char *)MEM_alloc(sizeof(char) + ((int)tc_strlen("Dec") + 1));
		month = tc_strcpy(month, "");
		month = tc_strcpy(month, "Dec");

	}

	newdate = (char *)MEM_alloc(sizeof(char) * ((int)tc_strlen(stripBlanks(yearbuffer)) + (int)tc_strlen(stripBlanks(month)) + (int)tc_strlen(stripBlanks(daybuffer)) +
	(int)tc_strlen(stripBlanks(hourbuffer)) + (int)tc_strlen(stripBlanks(minbuffer)) + (int)tc_strlen(stripBlanks(secbuffer)) + 5 + 1));

	newdate = tc_strcpy(newdate, "");
	newdate = tc_strcpy(newdate, daybuffer);
	newdate = tc_strcat(newdate, "-");
	newdate = tc_strcat(newdate, month);
	newdate = tc_strcat(newdate, "-");
	newdate = tc_strcat(newdate, yearbuffer);
	newdate = tc_strcat(newdate, " ");
	newdate = tc_strcat(newdate, hourbuffer);
	newdate = tc_strcat(newdate, ":");
	newdate = tc_strcat(newdate, minbuffer);
	newdate = tc_strcat(newdate, ":");
	newdate = tc_strcat(newdate, secbuffer);

	CHECK_FAIL(DATE_string_to_date_t(newdate, &isValid, &req_date));
	SAFE_SM_FREE(newdate);
	SAFE_SM_FREE(month);
	SAFE_SM_FREE(yearbuffer);
	SAFE_SM_FREE(hourbuffer);
	SAFE_SM_FREE(minbuffer);
	SAFE_SM_FREE(secbuffer);

	return req_date;
}
int getPart(std::string partNumber, std::string partType, tag_t* tPart)
{
	int ifail = ITK_ok;
	int iCount = 0;
	tag_t tQuery = NULLTAG;
	const char* queryName = "Item...";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	CHECK_FAIL(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		return ITK_ok;
	}

	char* queryEntries[] = {"Item ID", "Type"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partNumber.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partType.c_str()) + 1));

	queryValues[0] = tc_strcpy(queryValues[0], partNumber.c_str());
	queryValues[1] = tc_strcpy(queryValues[1], partType.c_str());

	CHECK_FAIL(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));
	if (iCount == 0)
	{
		//TC_write_syslog("[VFCost] No Item found for given Part Number [%s] and Part Type [%s]\n", partNumber.c_str(), partType.c_str());
	}
	else if (iCount == 1)
	{
		*tPart = tQryResults[0];
	}
	else
	{
		TC_write_syslog("[VFCost] More than 1 Items found for given Part Number [%s] and Part Type [%s]\n", partNumber.c_str(), partType.c_str());
	}

	SAFE_SM_FREE(queryValues[0]);
	SAFE_SM_FREE(queryValues[1]);
	SAFE_SM_FREE(queryValues);

	return ifail;
}
int vf_create_BOM_window(string revisionRule,tag_t* window)
{
	int ifail = ITK_ok;
	tag_t rule = NULLTAG;

	CHECK_FAIL(BOM_create_window(window));
	if (*window != NULLTAG)
	{
		TC_write_syslog("BOM Window created...\n");
	}
	else
	{
		TC_write_syslog("Failed to Create BOM Window.\n");
	}

	CHECK_FAIL(CFM_find(revisionRule.c_str(), &rule));

	CHECK_FAIL(BOM_set_window_config_rule(*window, rule));

	return ifail;
}
int vf_close_BOM_window(tag_t window, logical flag)
{
	int ifail = ITK_ok;
	if (flag)
	{
		ifail = BOM_save_window(window);
	}
	ifail = BOM_close_window(window);

	return ifail;
}
int extractBomline(tag_t tBOMLineTag, std::string donarVehicle)
{
	int ifail = ITK_ok;
	int iIndex = 0;
	int iChildCount = 0;
	int iItemIdAttrb = 0;
	int iItemNameAttrb = 0;
	int iOriginPartAttrb = 0;
	int iLSLeadAttrb = 0;
	int iModuleGrpAttrb = 0;
	int iMainModuleAttrb = 0;
	int iPurchaseLevelAttrb = 0;
	int iDonorVehAttrb = 0;
	int iSORRelDateAttrb = 0;


	Teamcenter::scoped_smptr<char> sItemIdValue;
	Teamcenter::scoped_smptr<char> sItemNameValue;
	Teamcenter::scoped_smptr<char> sOriginPartValue;
	Teamcenter::scoped_smptr<char> sModuleGrp;
	Teamcenter::scoped_smptr<char> sMainModule;
	Teamcenter::scoped_smptr<char> sPurchaseLevel;
	Teamcenter::scoped_smptr<char> sDonorVehicle;
	Teamcenter::scoped_smptr<char> sLongOrShortLead;
	Teamcenter::scoped_smptr<char> longOrShortLead1;
	Teamcenter::scoped_smptr<char> sSORRelDate;
	Teamcenter::scoped_smptr<tag_t> tChildLines;

	ifail = BOM_line_look_up_attribute(bomAttr_itemId, &iItemIdAttrb);
	ifail = BOM_line_ask_attribute_string(tBOMLineTag, iItemIdAttrb, &sItemIdValue);
	string longOrShortLead = "";
	string ItemIdValue = "";
	ItemIdValue.assign(sItemIdValue.getString());
	ifail = BOM_line_look_up_attribute(bomAttr_itemName, &iItemNameAttrb);
	ifail = BOM_line_ask_attribute_string(tBOMLineTag, iItemNameAttrb, &sItemNameValue);
	ifail = BOM_line_look_up_attribute("vf4_DsnRev_long_short_lead", &iLSLeadAttrb);
	ifail = BOM_line_ask_attribute_string(tBOMLineTag, iLSLeadAttrb, &sLongOrShortLead);
	longOrShortLead.assign(sLongOrShortLead.getString());
	STRNG_to_lower2(longOrShortLead.c_str(), &longOrShortLead1);
	if (strstr(longOrShortLead1.getString(), "long")){
		ItemIdValue.append("#LONG_LEAD");
	}

	ifail = BOM_line_look_up_attribute("bl_item_vf4_orginal_part_number", &iOriginPartAttrb);
	ifail = BOM_line_ask_attribute_string(tBOMLineTag, iOriginPartAttrb, &sOriginPartValue);

    if (uniquePartID.find(sItemIdValue.getString()) == uniquePartID.end()) {
		list<map<string, string>> lstmp;
		map<string, string> newMap ;
		uniquePartID.insert(sItemIdValue.getString());

		newMap.insert(pair<string, string>("Item Id", sItemIdValue.getString()));
		newMap.insert(pair<string, string>("Item Name", sItemNameValue.getString()));
		newMap.insert(pair<string, string>("originPart", sOriginPartValue.getString()));
		ifail = BOM_line_look_up_attribute("VL5_module_group", &iModuleGrpAttrb);
		ifail = BOM_line_ask_attribute_string(tBOMLineTag, iModuleGrpAttrb, &sModuleGrp);
		newMap.insert(pair<string, string>("Module Group", sModuleGrp.getString()));
		ifail = BOM_line_look_up_attribute("VL5_main_module", &iMainModuleAttrb);
		ifail = BOM_line_ask_attribute_string(tBOMLineTag, iMainModuleAttrb, &sMainModule);
		newMap.insert(pair<string, string>("Main Module", sMainModule.getString()));
		ifail = BOM_line_look_up_attribute("vf4_DsnRev_donor_veh_type", &iDonorVehAttrb);
		ifail = BOM_line_ask_attribute_string(tBOMLineTag, iDonorVehAttrb, &sDonorVehicle);
		newMap.insert(pair<string, string>("Donor Vehicle", sDonorVehicle.getString()));

		if(tc_strcmp(sDonorVehicle.getString(),"") == 0) {
				newMap.insert(pair<string, string>("Change Index", ""));
		}
		else {
			if(tc_strcmp(sDonorVehicle.getString(),donarVehicle.c_str()) == 0) {
				newMap.insert(pair<string, string>("Change Index", "NEW"));
			}else {
				newMap.insert(pair<string, string>("Change Index", "COP"));
			}
		}
		ifail = BOM_line_look_up_attribute("VL5_purchase_lvl_vf", &iPurchaseLevelAttrb);
		ifail = BOM_line_ask_attribute_string(tBOMLineTag, iPurchaseLevelAttrb, &sPurchaseLevel);
		newMap.insert(pair<string, string>("Purchase Level", sPurchaseLevel.getString()));
		ifail = BOM_line_look_up_attribute("bl_rev_vf4_sor_release_date_rev", &iSORRelDateAttrb);
		ifail = BOM_line_ask_attribute_string(tBOMLineTag, iSORRelDateAttrb, &sSORRelDate);
		newMap.insert(pair<string, string>("SOR release date - Reality", sSORRelDate.getString()));

		lstmp.push_back(newMap);
		bom150.push_back(lstmp);
    }

	ifail = BOM_line_ask_child_lines(tBOMLineTag, &iChildCount, &tChildLines);
	for (iIndex=0; iIndex<iChildCount; iIndex++)
	{
		ifail = extractBomline(tChildLines[iIndex],donarVehicle);
	}

	return ifail;
}
void initializeSOURCING()
{
	SOURCING.insert(pair<string, string>("Plan Date - Supplier Award (LON)", "vf4_plan_award_supplier"));
	SOURCING.insert(pair<string, string>("Actual Date - Supplier Award (LON)", "vf4_actual_award_supplier"));
	SOURCING.insert(pair<string, string>("Award Supplier", "vf4_bmw_nominated_supplier"));
	SOURCING.insert(pair<string, string>("Supplier Code", "vf4_supplier_code"));
	SOURCING.insert(pair<string, string>("Manufacturing Country", "vf4_manuf_location"));
	SOURCING.insert(pair<string, string>("Status", ""));
}
void initializePFEP()
{
	PFEP.insert(pair<string, string>("In Process P", ""));
	PFEP.insert(pair<string, string>("P Release Plan Date", "vf4_P_release_plan_date"));
	PFEP.insert(pair<string, string>("P Released Date", ""));
	PFEP.insert(pair<string, string>("P release Status", ""));
	PFEP.insert(pair<string, string>("In Process I", ""));
	PFEP.insert(pair<string, string>("I Release Plan Date", "vf4_I_release_plan_date"));
	PFEP.insert(pair<string, string>("I Released Date", ""));
	PFEP.insert(pair<string, string>("I release Status", ""));
	PFEP.insert(pair<string, string>("In Process PR", ""));
	PFEP.insert(pair<string, string>("PR Release Plan Date", "vf4_PR_release_plan_date"));
	PFEP.insert(pair<string, string>("PR Released Date", ""));
	PFEP.insert(pair<string, string>("PR release Status", ""));
}
void makeAllPartNumbers(tag_t part,string itemType , string allOldPartNumbers)
{
	Teamcenter::scoped_smptr<char> oldPartNumbers;
	CHECK_FAIL(AOM_ask_value_string(part, "vf4_orginal_part_number", &oldPartNumbers));
	string oldPartNumbersString = "";
	oldPartNumbersString.assign(oldPartNumbers.getString());
	if (allOldPartNumbers.find(oldPartNumbersString) == std::string::npos && oldPartNumbersString.length() > 3
			&& oldPartNumbersString.find(";") == std::string::npos && oldPartNumbersString.find("\n") == std::string::npos
			&& oldPartNumbersString.find(" ") == std::string::npos && oldPartNumbersString.find("\t") == std::string::npos) {


		int iCount = 0;
		tag_t tQuery = NULLTAG;
		const char* queryName = "Item...";
		Teamcenter::scoped_smptr<tag_t> tQryResults;

		CHECK_FAIL(QRY_find2(queryName, &tQuery));
		if (tQuery == NULLTAG)
		{
			TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
			//return ITK_ok;
		}

		char* queryEntries[] = {"Item ID", "Type"};
		char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

		queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(oldPartNumbersString.c_str()) + 1));
		queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(itemType.c_str()) + 1));

		queryValues[0] = tc_strcpy(queryValues[0], oldPartNumbersString.c_str());
		queryValues[1] = tc_strcpy(queryValues[1], itemType.c_str());

		CHECK_FAIL(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));
		list<tag_t> oldParts;
		for(int k = 0;k < iCount; k++)
		{
			oldParts.push_back(tQryResults[k]);
		}
		if (oldParts.size() == 1) {
			allOldPartNumbers.append(oldPartNumbersString);
			allOldPartNumbers.append(";");
			tag_t oldPart = oldParts.front();
			makeAllPartNumbers(oldPart, itemType ,allOldPartNumbers);
		} else if (oldParts.size() == 0) {
			allOldPartNumbers = "";
			TC_write_syslog("Cannot find any parts with id " , oldPartNumbersString );
		} else {
			// find out more than one old part numbers => skip
		}
	} else if (allOldPartNumbers.find(oldPartNumbersString) != std::string::npos && oldPartNumbersString.length() > 3) {
		TC_write_syslog("Endless recursion with oldPartNumbersString= %s %s %s",allOldPartNumbers
				," - oldPartNumbersString=",oldPartNumbersString);
	}
}
list<string> getPartNumbersInEcrCombine(string partIdsString, string releaseRemark)
{
	list<tag_t> partsInEcrCombineRelease;
	int iCount = 0;
	tag_t tQuery = NULLTAG;
	const char* queryName = "__VF_Design_Rev_In_ECR_Combine";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	CHECK_FAIL(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		//return 0;
	}

	char* queryEntries[] = {"ID", "vf4_remark"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partIdsString.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(releaseRemark.c_str()) + 1));

	queryValues[0] = tc_strcpy(queryValues[0], partIdsString.c_str());
	queryValues[1] = tc_strcpy(queryValues[1], releaseRemark.c_str());

	CHECK_FAIL(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));
	for(int j = 0 ; j<iCount ; j++)
	{
		partsInEcrCombineRelease.push_back(tQryResults[j]);
	}
	list<string> partIdsInEcrCombineRelease;
	for (tag_t part : partsInEcrCombineRelease) {
		Teamcenter::scoped_smptr<char> partId;
		CHECK_FAIL(AOM_ask_value_string(part, "item_id", &partId));
		partIdsInEcrCombineRelease.push_back(partId.getString());
	}
	return partIdsInEcrCombineRelease;
}
list<string> getPartNumbersInEcn(string partIdsString, string releaseRemark)
{
	int iCount = 0;
	tag_t tQuery = NULLTAG;
	list<tag_t> partsInEcnRelease;
	const char* queryName = "__VF_Design_Rev_In_ECN_Combine";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	CHECK_FAIL(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		//return ITK_ok;
	}

	char* queryEntries[] = {"ID", "vf4_remark"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partIdsString.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(releaseRemark.c_str()) + 1));

	queryValues[0] = tc_strcpy(queryValues[0], partIdsString.c_str());
	queryValues[1] = tc_strcpy(queryValues[1], releaseRemark.c_str());

	CHECK_FAIL(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));

	for(int j = 0 ; j<iCount ; j++)
	{
		partsInEcnRelease.push_back(tQryResults[j]);
	}
	list<string> partIdsInEcnCombineRelease;
	for (tag_t part : partsInEcnRelease) {
		Teamcenter::scoped_smptr<char> partId;
		CHECK_FAIL(AOM_ask_value_string(part, "item_id", &partId));
		partIdsInEcnCombineRelease.push_back(partId.getString());
	}
	return partIdsInEcnCombineRelease;
}
void setP_ReleaseInfo(string allPartNumbers, PartReleaseInfo *partReleaseInfo)
{
	int iCount = 0;
	tag_t tQuery = NULLTAG;
	string P_FLAG = "VL5_P";
	list<tag_t> pReleaseParts;
	const char* queryName = "__VF Release Design Revision";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	CHECK_FAIL(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		//return ITK_ok;
	}

	char* queryEntries[] = {"Item ID", "Release Status"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(allPartNumbers.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen("VL5_P") + 1));

	queryValues[0] = tc_strcpy(queryValues[0], allPartNumbers.c_str());
	queryValues[1] = tc_strcpy(queryValues[1], "VL5_P");

	CHECK_FAIL(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));
	for(int j = 0 ; j<iCount ; j++)
	{
		pReleaseParts.push_back(tQryResults[j]);
	}
	if (pReleaseParts.size() > 0) {

		int n_statuses = 0;
		Teamcenter::scoped_smptr<tag_t> statuses;

		tag_t pReleaseRev = pReleaseParts.front();

		CHECK_FAIL(WSOM_ask_release_status_list(pReleaseRev, &n_statuses , &statuses));
		for(int ii = 0; ii < n_statuses; ii++)
		{
			date_t pReleaseDateTag = NULLDATE;
			Teamcenter::scoped_smptr<char> name;

			CHECK_FAIL(AOM_ask_value_string(statuses[ii], "name", &name));
			if (strcmp(name.get(), "VL5_P") == 0)
			{
				CHECK_FAIL(AOM_ask_value_date(statuses[ii],"date_released", &pReleaseDateTag));
				partReleaseInfo->pReleaseDate = pReleaseDateTag;
				partReleaseInfo->pPartRev = pReleaseRev;
			}
		}
	}
}
void setI_ReleaseInfo(string allPartNumbers, PartReleaseInfo partReleaseInfo){

	int iCount = 0;
	tag_t tQuery = NULLTAG;
	string I_FLAG = "Vl5_I";
	list<tag_t> iReleaseParts;
	const char* queryName = "__VF Release Design Revision";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	CHECK_FAIL(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		//return ITK_ok;
	}

	char* queryEntries[] = {"Item ID", "Release Status"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(allPartNumbers.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen("Vl5_I") + 1));

	queryValues[0] = tc_strcpy(queryValues[0], allPartNumbers.c_str());
	queryValues[1] = tc_strcpy(queryValues[1], "Vl5_I");

	CHECK_FAIL(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));
	for(int j = 0 ; j<iCount ; j++)
	{
		iReleaseParts.push_back(tQryResults[j]);
	}
	if (iReleaseParts.size() > 0) {

		int n_statuses = 0;
		Teamcenter::scoped_smptr<tag_t> statuses;

		tag_t iReleaseRev = iReleaseParts.front();

		CHECK_FAIL(WSOM_ask_release_status_list(iReleaseRev, &n_statuses , &statuses));
		for(int ii = 0; ii < n_statuses; ii++)
		{
			date_t iReleaseDate = NULLDATE;
			Teamcenter::scoped_smptr<char> name;

			CHECK_FAIL(AOM_ask_value_string(statuses[ii], "name", &name));
			if (strcmp(name.get(), "Vl5_I") == 0)
			{
				CHECK_FAIL(AOM_ask_value_date(statuses[ii],"date_released", &iReleaseDate));
				partReleaseInfo.iReleaseDate = iReleaseDate;
				partReleaseInfo.iPartRev = iReleaseRev;
			}
		}
	}
}
void setPR_ReleaseInfo(string allPartNumbers, PartReleaseInfo partReleaseInfo){

	int iCount = 0;
	tag_t tQuery = NULLTAG;
	string PR_FLAG = "PR4D_PR";
	list<tag_t> prReleaseParts;
	const char* queryName = "__VF Release Design Revision";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	CHECK_FAIL(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		//return ITK_ok;
	}

	char* queryEntries[] = {"Item ID", "Release Status"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(allPartNumbers.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(PR_FLAG.c_str()) + 1));

	queryValues[0] = tc_strcpy(queryValues[0], allPartNumbers.c_str());
	queryValues[1] = tc_strcpy(queryValues[1], PR_FLAG.c_str());

	CHECK_FAIL(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));
	for(int j = 0 ; j<iCount ; j++)
	{
		prReleaseParts.push_back(tQryResults[j]);
	}
	if (prReleaseParts.size() > 0) {

		int n_statuses = 0;
		Teamcenter::scoped_smptr<tag_t> statuses;

		tag_t prReleaseRev = prReleaseParts.front();

		CHECK_FAIL(WSOM_ask_release_status_list(prReleaseRev, &n_statuses , &statuses));
		for(int ii = 0; ii < n_statuses; ii++)
		{
			date_t prReleaseDate = NULLDATE;
			Teamcenter::scoped_smptr<char> name;

			CHECK_FAIL(AOM_ask_value_string(statuses[ii], "name", &name));
			if (strcmp(name.get(), "PR4D_PR") == 0)
			{
				CHECK_FAIL(AOM_ask_value_date(statuses[ii],"date_released", &prReleaseDate));
				partReleaseInfo.prReleaseDate = prReleaseDate;
				partReleaseInfo.prPartRev = prReleaseRev;
			}
		}
	}
}
void setPPR_ReleaseInfo(string allPartNumbers, PartReleaseInfo partReleaseInfo){

	int iCount = 0;
	tag_t tQuery = NULLTAG;
	string PPR_FLAG = "Vf6_PPR";
	list<tag_t> pprReleaseParts;
	const char* queryName = "__VF Release Design Revision";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	CHECK_FAIL(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		//return ITK_ok;
	}

	char* queryEntries[] = {"Item ID", "Release Status"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(allPartNumbers.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(PPR_FLAG.c_str()) + 1));

	queryValues[0] = tc_strcpy(queryValues[0], allPartNumbers.c_str());
	queryValues[1] = tc_strcpy(queryValues[1], PPR_FLAG.c_str());

	CHECK_FAIL(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));

	for(int j = 0 ; j<iCount ; j++)
	{
		pprReleaseParts.push_back(tQryResults[j]);
	}
	if (pprReleaseParts.size() > 0) {

		int n_statuses = 0;
		Teamcenter::scoped_smptr<tag_t> statuses;

		tag_t pprReleaseRev = pprReleaseParts.front();

		CHECK_FAIL(WSOM_ask_release_status_list(pprReleaseRev, &n_statuses , &statuses));
		for(int ii = 0; ii < n_statuses; ii++)
		{
			date_t pprReleaseDate = NULLDATE;
			Teamcenter::scoped_smptr<char> name;

			CHECK_FAIL(AOM_ask_value_string(statuses[ii], "name", &name));
			if (strcmp(name.get(), "Vf6_PPR") == 0)
			{
				CHECK_FAIL(AOM_ask_value_date(statuses[ii],"date_released", &pprReleaseDate));
				partReleaseInfo.pprReleaseDate = pprReleaseDate;
				partReleaseInfo.pprPartRev = pprReleaseRev;
			}
		}
	}
}
string getInProcessP_String(PartReleaseInfo partReleaseInfo) {
	string resultString = "";

	int isPDateNull = 1;
	isPDateNull = DATE_IS_NULL(partReleaseInfo.pReleaseDate);
	int isIDateNull = 1;
	isIDateNull = DATE_IS_NULL(partReleaseInfo.iReleaseDate);
	int isPrDateNull = 1;
	isPrDateNull = DATE_IS_NULL(partReleaseInfo.prReleaseDate);
	int isPPrDateNull = 1;
	isPPrDateNull = DATE_IS_NULL(partReleaseInfo.pprReleaseDate);

	if (isPDateNull == 0 || isIDateNull == 0 || isPrDateNull == 0 || isPPrDateNull == 0) {
		resultString = "";
	} else if (partReleaseInfo.isInEcrCombineReleaseP){
		resultString = "In Process";
	}

	return resultString;
}

string getInProcessI_String(PartReleaseInfo partReleaseInfo) {
	string resultString = "";
	int isIDateNull = 1;
	isIDateNull = DATE_IS_NULL(partReleaseInfo.iReleaseDate);
	int isPrDateNull = 1;
	isPrDateNull = DATE_IS_NULL(partReleaseInfo.prReleaseDate);
	int isPPrDateNull = 1;
	isPPrDateNull = DATE_IS_NULL(partReleaseInfo.pprReleaseDate);
	if (isIDateNull == 0 || isPrDateNull == 0 || isPPrDateNull == 0) {
		resultString = "";
	} else if (partReleaseInfo.isInEcrCombineReleaseI){
		resultString = "In Process";
	}

	return resultString;
}

string getInProcessPR_String(PartReleaseInfo partReleaseInfo) {
	string resultString = "";

	int isPrDateNull = 1;
	isPrDateNull = DATE_IS_NULL(partReleaseInfo.prReleaseDate);
	int isPPrDateNull = 1;
	isPPrDateNull = DATE_IS_NULL(partReleaseInfo.pprReleaseDate);
	if (isPrDateNull == 0 || isPPrDateNull == 0) {
		resultString = "";
	} else if (partReleaseInfo.isInEcrCombineReleasePR){
		resultString = "In Process";
	}

	return resultString;
}
ReportDataSourceImpl::PFEPReportResponse ReportDataSourceImpl::getPFEPReport ( const std::vector< PFEPReportInput >& input )
{
	int ifail = 0;

	ReportDataSourceImpl::PFEPReportResponse response;
	TC_write_syslog("[VF] ENTER %s", __FUNCTION__);
	TC_write_syslog("Size of Input Vector : %d\n", input.size());
	if (input.size() > 0)
	{
		try
		{
			for (PFEPReportInput PFEPInput : input)
			{
				int vCount = 0;
				int iDonorVehAttrb = 0;
				tag_t tItemTag = NULLTAG;
				tag_t tWindowTag = NULLTAG;
				tag_t tItemRevTag = NULLTAG;
				tag_t tTopLineTag = NULLTAG;

				Teamcenter::scoped_smptr<tag_t> bvrs;
				Teamcenter::scoped_smptr<char> itemId;
				Teamcenter::scoped_smptr<char> topBomName;
				Teamcenter::scoped_smptr<char> sDonorVehicle;

				string topLine = PFEPInput.ebomTopLine;
				string revisionRule = PFEPInput.revisionRule;
				string itemType = PFEPInput.itemType;
				string sourcingProgram = PFEPInput.sourcingProgram;

				initializeSOURCING();
				initializePFEP();

				ifail = getPart(topLine, itemType, &tItemTag);
				if (tItemTag == NULLTAG)
				{
					TC_write_syslog("BOM.traverseBOM item[%s]is null \n", topLine);
					return response;
				}

				CHECK_FAIL(AOM_ask_value_string(tItemTag, "item_id", &itemId));
				CHECK_FAIL(AOM_ask_value_string(tItemTag, "object_name", &topBomName));
				CHECK_FAIL(ITEM_ask_latest_rev(tItemTag, &tItemRevTag));
				CHECK_FAIL(ITEM_rev_list_bom_view_revs(tItemRevTag, &vCount, &bvrs));

				if (tItemRevTag == NULLTAG || vCount == 0 || bvrs.get()[0] == NULLTAG) {
					TC_write_syslog("BOM.traverseBOM itemRev or bomView is null.\n");
					return response;
				}

				ifail = vf_create_BOM_window(revisionRule,&tWindowTag);
				ifail = BOM_set_window_top_line (tWindowTag, tItemTag, tItemRevTag, bvrs[0], &tTopLineTag);

				ifail = BOM_line_look_up_attribute("vf4_DsnRev_donor_veh_type", &iDonorVehAttrb);
				ifail = BOM_line_ask_attribute_string(tTopLineTag, iDonorVehAttrb, &sDonorVehicle);

				ifail = extractBomline(tTopLineTag,sDonorVehicle.getString());

				//initPFEPMasterData
				set<string>::iterator itr;
				string itemIds = "";
				for (itr = uniquePartID.begin(); itr != uniquePartID.end(); itr++)
				{
					string aUniqueID = *itr;
					itemIds.append(aUniqueID);
					itemIds.append(";");
					map<string, string> costPropAndValue;
					map<string, string>::iterator sItr = SOURCING.begin();
					map<string, string>::iterator pItr = PFEP.begin();
					// Iterate over the map using Iterator till end.
					while (sItr != SOURCING.end())
					{
						costPropAndValue.insert(pair<string, string>(sItr->first, ""));
						sItr++;
					}
					while (pItr != PFEP.end())
					{
						costPropAndValue.insert(pair<string, string>(pItr->first, ""));
						pItr++;
					}
					/*if (aUniqueID.contains("DSU80000000")) {
						System.out.println();;
					}*/
					costingMasterData.insert(pair<string, map<string,string>>(aUniqueID, costPropAndValue));
				}
				//extract sourcing data
				tag_t spQuery = NULLTAG;
				map<string, string> sourcingQuery;
				sourcingQuery.insert(pair<string, string>("VF Part Number", itemIds));
				sourcingQuery.insert(pair<string, string>("Sourcing Program", sourcingProgram));
				const char* queryName = "Source Part";
				Teamcenter::scoped_smptr<tag_t> sourcingQryResults;
				CHECK_FAIL(QRY_find2(queryName, &spQuery));
				if (spQuery == NULLTAG)
				{
					TC_write_syslog("[VF]ERROR: Cannot find query %s:\n", queryName);
				}
				int n_entries = 0;
				int spCount = 0;
				n_entries = sourcingQuery.size();

				char **queryEntries = NULL;
				char **queryValues = NULL;

				queryEntries = (char **) MEM_alloc(n_entries * sizeof(char *));
				queryValues  = (char **) MEM_alloc(n_entries * sizeof(char *));

				int s = 0;
				map<string, string>::iterator spItr;
				for (spItr = sourcingQuery.begin(); spItr != sourcingQuery.end(); ++spItr) {
					queryEntries[s] = (char *)MEM_alloc(strlen( spItr->first.c_str()) + 1);
					strcpy(queryEntries[s], spItr->first.c_str() );

					queryValues[s] = (char *)MEM_alloc( strlen( spItr->second.c_str()) + 1);
					strcpy(queryValues[s], spItr->second.c_str());

					s++;
				}
				CHECK_FAIL(QRY_execute(spQuery, n_entries, queryEntries, queryValues, &spCount, &sourcingQryResults));
				if (spCount == 0)
				{
					TC_write_syslog("[extractSourcingCostData]No Sourcing\n");
				}
				for (int i = 0; i < spCount; i++) {
					Teamcenter::scoped_smptr<char> aSourcingID;
					CHECK_FAIL(AOM_ask_value_string(sourcingQryResults[i], "vf4_bom_vfPartNumber", &aSourcingID));
					TC_write_syslog("[extractSourcingCostData] Processing:%s\n ",aSourcingID.getString());
					map<string, string> costPropAndValue;
					map<string, map<string,string>> ::iterator csmItr = costingMasterData.begin();
					csmItr = costingMasterData.find(aSourcingID.getString());
					if ( csmItr != costingMasterData.end()) {
						costPropAndValue = csmItr->second;
					}
					map<string, string>::iterator sItr = SOURCING.begin();
					// Iterate over the map using Iterator till end.
					while (sItr != SOURCING.end())
					{
						if(tc_strcmp(sItr->second.c_str(),"")!= 0) {
							Teamcenter::scoped_smptr<char*> tmpValue;
							int nValues = 0;
							CHECK_FAIL(AOM_ask_displayable_values(sourcingQryResults[i], sItr->second.data(), &nValues, &tmpValue));
							string tmp = "";
							tmp.assign(tmpValue.get()[0]);
							costPropAndValue[sItr->second] = tmp;
							if(tc_strcmp(sItr->first.c_str(),"Supplier Code") == 0) {
								if(tc_strcmp(tmp.c_str(),"") != 0) {
									if(tmp.length() > 3) {
										if(tc_strcmp(tmp.substr(0, 3).c_str(),"000") == 0) {
											listPartID_SupplierCode.insert(pair<string, string>(aSourcingID.getString(), tmp.substr(3, tmp.length())));
										}
										else listPartID_SupplierCode.insert(pair<string, string>(aSourcingID.getString(), tmp));
									}
									else listPartID_SupplierCode.insert(pair<string, string>(aSourcingID.getString(), tmp));
								} else {
									listPartID_SupplierCode.insert(pair<string, string>(aSourcingID.getString(), ""));
								}
							}
						}
						sItr++;
					}
					costingMasterData[aSourcingID.getString()] = costPropAndValue;
					/*if (aSourcingID.contains("DSU80000000")) {
						System.out.println();;
					}*/
					TC_write_syslog("[extractSourcingCostData] Finish extract sourcing cost: %s " ,aSourcingID.getString());
				}
				//extract PFEP data
				tag_t pfepQuery = NULLTAG;
				map<string, string> PFEPFormQuery;
				PFEPFormQuery.insert(pair<string, string>("ID", itemIds));
				const char* PFEPQName = "PFEP Form";
				Teamcenter::scoped_smptr<tag_t> PFEPQryResults;
				CHECK_FAIL(QRY_find2(PFEPQName, &pfepQuery));
				if (pfepQuery == NULLTAG)
				{
					TC_write_syslog("[VF]ERROR: Cannot find query %s:\n", PFEPQName);
				}
				int pEntries = 0;
				int PFEPCount = 0;
				pEntries = PFEPFormQuery.size();

				char **PFEPEntries = NULL;
				char **PFEPValues = NULL;

				PFEPEntries = (char **) MEM_alloc(pEntries * sizeof(char *));
				PFEPValues  = (char **) MEM_alloc(pEntries * sizeof(char *));

				int p = 0;
				map<string, string>::iterator pfepItr;
				for (pfepItr = PFEPFormQuery.begin(); pfepItr != PFEPFormQuery.end(); ++pfepItr) {
					PFEPEntries[p] = (char *)MEM_alloc(strlen( pfepItr->first.c_str()) + 1);
					strcpy(PFEPEntries[p], pfepItr->first.c_str() );

					PFEPValues[p] = (char *)MEM_alloc( strlen( pfepItr->second.c_str()) + 1);
					strcpy(PFEPValues[p], pfepItr->second.c_str());

					p++;
				}
				CHECK_FAIL(QRY_execute(pfepQuery, pEntries, PFEPEntries, PFEPValues, &PFEPCount, &PFEPQryResults));
				if (PFEPCount == 0)
				{
					TC_write_syslog("[extractPFEPData] No pfep: [%s]\n",itemIds.c_str());
				}
				for (int i = 0; i < PFEPCount; i++) {
					Teamcenter::scoped_smptr<char> aSourcingID;
					CHECK_FAIL(AOM_ask_value_string(PFEPQryResults[i], "vf4_part_number", &aSourcingID));
					map<string, string> costPropAndValue;
					map<string, map<string,string>> ::iterator csmItr = costingMasterData.begin();
					csmItr = costingMasterData.find(aSourcingID.getString());
					if ( csmItr != costingMasterData.end()) {
						costPropAndValue = csmItr->second;
					}
					map<string, string>::iterator pItr = PFEP.begin();
					// Iterate over the map using Iterator till end.
					while (pItr != PFEP.end())
					{
						if(tc_strcmp(pItr->second.c_str(),"")!= 0) {

							Teamcenter::scoped_smptr<char*> tmpValue;
							int nValues = 0;
							CHECK_FAIL(AOM_ask_displayable_values(PFEPQryResults[i], pItr->second.data(), &nValues, &tmpValue));
							costPropAndValue.insert(pair<string, string>(pItr->second, tmpValue.get()[0]));
						}
						pItr++;
					}
					costingMasterData[aSourcingID.getString()] = costPropAndValue;
					/*if (aSourcingID.contains("DSU80000000")) {
						System.out.println();;
					}*/
					TC_write_syslog("[extractSourcingCostData] Finish extract sourcing cost: %s\n " ,aSourcingID.getString());
				}
				//extractActualReleaseDate
				list<string> partIds(uniquePartID.begin(), uniquePartID.end());
				list<string>::iterator lItr;
				string partIdsString = "";
				for (lItr = partIds.begin(); lItr != partIds.end(); lItr++)
				{
					string partId = *lItr;
					partIdsString.append(partId);
					partIdsString.append(";");
				}
				int rCount = 0;
				tag_t itemQuery = NULLTAG;
				const char* itemQName = "Item...";
				Teamcenter::scoped_smptr<tag_t> tQryResults;

				CHECK_FAIL(QRY_find2(itemQName, &itemQuery));
				if (itemQuery == NULLTAG)
				{
					TC_write_syslog("ERROR: Cannot find query %s:\n", itemQName);
				}

				char* itemEntries[] = {"Item ID", "Type"};
				char** itemValues = (char**)MEM_alloc(sizeof(char*) * 2);

				itemValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partIdsString.c_str()) + 1));
				itemValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(itemType.c_str()) + 1));

				itemValues[0] = tc_strcpy(itemValues[0], partIdsString.c_str());
				itemValues[1] = tc_strcpy(itemValues[1], itemType.c_str());

				CHECK_FAIL(QRY_execute(itemQuery, 2, itemEntries, itemValues, &rCount, &tQryResults));
				//prepare PartNumbers And AllOldPartNumbers
				map<string, PartReleaseInfo> partRevsAndReleaseInfos;
				map<string, string> partNumbersAndAllOldPartNumbers;
				for(int j = 0 ; j<rCount ; j++)
				{
					Teamcenter::scoped_smptr<char> partId;
					CHECK_FAIL(AOM_ask_value_string(tQryResults[j], "item_id", &partId));
					string allOldPartNumbers = "";
					allOldPartNumbers.append(partId.getString());
					allOldPartNumbers.append(";");
					makeAllPartNumbers(tQryResults[j], itemType,allOldPartNumbers);

					partNumbersAndAllOldPartNumbers.insert(pair<string, string>(partId.getString(), allOldPartNumbers));
				}
				list<string> partNumbersInEcrCombineP = getPartNumbersInEcrCombine(partIdsString, "P Release");
				list<string> partNumbersInEcnCombineP = getPartNumbersInEcn(partIdsString, "P Release");

				list<string> partNumbersInEcrCombineI = getPartNumbersInEcrCombine(partIdsString, "I Release");
				list<string> partNumbersInEcnCombineI = getPartNumbersInEcn(partIdsString, "I Release");

				list<string> partNumbersInEcrCombinePR = getPartNumbersInEcrCombine(partIdsString, "PR Release");
				list<string> partNumbersInEcnCombinePR = getPartNumbersInEcn(partIdsString, "PR Release");

				map<string, string>::iterator pNumItr = partNumbersAndAllOldPartNumbers.begin();
				// Iterate over the map using Iterator till end.
				while (pNumItr != partNumbersAndAllOldPartNumbers.end())
				{
					bool isInEcrCombineReleaseP;
					bool isInEcrCombineReleaseI;
					bool isInEcrCombineReleasePR;

					string partId = pNumItr->first;
					string allOldPartNumbers =  pNumItr->second;
					string allPartNumbers = "";
					allPartNumbers.append(partId);
					allPartNumbers.append(";");
					allPartNumbers.append(allOldPartNumbers);

					PartReleaseInfo partReleaseInfo;

					setP_ReleaseInfo(allPartNumbers, &partReleaseInfo);
					int isDateNull1 =1;
					Teamcenter::scoped_smptr<char> creationDate1;
					isDateNull1 = DATE_IS_NULL(partReleaseInfo.pReleaseDate);
					if(isDateNull1 == 0)
					{
						CHECK_FAIL(DATE_date_to_string (partReleaseInfo.pReleaseDate, "%d-%b-%Y %H:%M:%S", &creationDate1));
					}
					std::list<string>::iterator findPIter = std::find(partNumbersInEcrCombineP.begin(), partNumbersInEcrCombineP.end(), partId);
					if (findPIter != partNumbersInEcrCombineP.end()) {
						isInEcrCombineReleaseP = true;
					}else
					{
						isInEcrCombineReleaseP = false;
					}
					partReleaseInfo.isInEcrCombineReleaseP = isInEcrCombineReleaseP;

					setI_ReleaseInfo(allPartNumbers, partReleaseInfo);
					std::list<string>::iterator findIIter = std::find(partNumbersInEcrCombineI.begin(), partNumbersInEcrCombineI.end(), partId);
					if(findIIter != partNumbersInEcrCombineI.end()) {
						isInEcrCombineReleaseI = true;
					}else
					{
						isInEcrCombineReleaseI = false;
					}
					partReleaseInfo.isInEcrCombineReleaseI = isInEcrCombineReleaseI;

					setPR_ReleaseInfo(allPartNumbers, partReleaseInfo);
					std::list<string>::iterator findPRIIter = std::find(partNumbersInEcrCombinePR.begin(), partNumbersInEcrCombinePR.end(), partId);
					if (findPRIIter != partNumbersInEcrCombinePR.end()) {
						isInEcrCombineReleasePR = true;
					}else
					{
						isInEcrCombineReleasePR = false;
					}
					partReleaseInfo.isInEcrCombineReleasePR = isInEcrCombineReleasePR;

					int isDateNull = 1;
					isDateNull = DATE_IS_NULL(partReleaseInfo.prReleaseDate);

					if(isDateNull) setPPR_ReleaseInfo(allPartNumbers, partReleaseInfo);
					partRevsAndReleaseInfos.insert(pair<string, PartReleaseInfo>(partId, partReleaseInfo));
					pNumItr++;
				}
				map<string, PartReleaseInfo>::iterator rItr = partRevsAndReleaseInfos.begin();
				while (rItr != partRevsAndReleaseInfos.end())
				{
					string aSourcingID = rItr->first;
					TC_write_syslog("[extractActualReleaseDate] Processing %s \n",aSourcingID.c_str());

					map<string, string> costPropAndValue;
					map<string, map<string,string>> ::iterator csmItr = costingMasterData.begin();
					csmItr = costingMasterData.find(aSourcingID);
					if ( csmItr != costingMasterData.end()) {
						costPropAndValue = csmItr->second;
					}
					PartReleaseInfo partInfo;
					partInfo = rItr->second;

					int isPDateNull = 1;
					isPDateNull = DATE_IS_NULL(partInfo.pReleaseDate);
					int isIDateNull = 1;
					isIDateNull = DATE_IS_NULL(partInfo.iReleaseDate);
					int isPrDateNull = 1;
					isPrDateNull = DATE_IS_NULL(partInfo.prReleaseDate);
					int isPPrDateNull = 1;
					isPPrDateNull = DATE_IS_NULL(partInfo.pprReleaseDate);
					///convert date to string

					Teamcenter::scoped_smptr<char> pReleaseDate_S;
					Teamcenter::scoped_smptr<char> iReleaseDate_S;
					Teamcenter::scoped_smptr<char> prReleaseDate_S;
					if(isPDateNull == 0)
					{
						CHECK_FAIL(DATE_date_to_string (partInfo.pReleaseDate, "%d-%b-%Y %H:%M:%S", &pReleaseDate_S));
						costPropAndValue["P Released Date"] = pReleaseDate_S.getString();
					}
					if(isIDateNull == 0)
					{
						CHECK_FAIL(DATE_date_to_string (partInfo.iReleaseDate, "%d-%b-%Y %H:%M:%S", &iReleaseDate_S));
						costPropAndValue["I Released Date"] = iReleaseDate_S.getString();
					}
					if(isPrDateNull == 0)
					{
						CHECK_FAIL(DATE_date_to_string (partInfo.prReleaseDate, "%d-%b-%Y %H:%M:%S", &prReleaseDate_S));
						costPropAndValue["PR Released Date"] = prReleaseDate_S.getString();
					}
					else
					{
						if(isPPrDateNull == 0)
						costPropAndValue["PR Released Date"] = "Finished";
					}

					costPropAndValue["In Process P"] = getInProcessP_String(partInfo);
					costPropAndValue["In Process I"] = getInProcessI_String(partInfo);
					costPropAndValue["In Process PR"] = getInProcessPR_String(partInfo);

					costingMasterData[rItr->first] = costPropAndValue;
					TC_write_syslog("[extractActualReleaseDate] Finish extract pfep:%s\n", aSourcingID);
					rItr++;
				}
				//convertDataToWriteReport
				map<int, map<string, string>> finalMap;
				int i = 0;
				TC_write_syslog("[bom150 Size] %d\n ",bom150.size());
				for (list<map<string, string>> aLine : bom150) {
					map<string, string> mapping;
					for (map<string, string> pairValue : aLine) {
						mapping.insert(pairValue.begin(), pairValue.end());
					}
					/* TODO merge cost master data into bomline master data */
					string partNumber ="";
					map<string, string> aCostData;
					auto search = mapping.find("Item Id");
					if (search != mapping.end()) {
						partNumber = search->second;
						size_t pos = partNumber.find("#LONG_LEAD");
						if (pos!= std::string::npos)
						{
							partNumber.replace(pos, 10, "");
						}
						auto search1 = costingMasterData.find(partNumber);
						if (search1 != costingMasterData.end()) {
							aCostData = search1->second;
						}
						if(aCostData.empty()) {
							TC_write_syslog("[convertDataToWriteReport] Cost object not existed: %s " , partNumber);
						}else {
							mapping.insert(aCostData.begin(), aCostData.end());
						}
					}
					finalMap.insert(pair<int, map<string,string>>(i, mapping));
					i++;
				}

				//Push data to soa response
				for (auto aLine : finalMap) {
					PFEPReportOutput outputStatus;
					for (auto pairValue : aLine.second) {

						if(tc_strcmp(pairValue.first.c_str(),"Item Id") == 0)
						{
							outputStatus.itemId.assign(pairValue.second);
						}
						if(tc_strcmp(pairValue.first.c_str(),"Item Name") == 0)
						{
							outputStatus.itemName.assign(pairValue.second);
						}
						if(tc_strcmp(pairValue.first.c_str(),"Module Group") == 0)
						{
							outputStatus.moduleGroupEnglish.assign(pairValue.second);
						}
						if(tc_strcmp(pairValue.first.c_str(),"Main Module") == 0)
						{
							outputStatus.mainModuleEnglish.assign(pairValue.second);
						}
						if(tc_strcmp(pairValue.first.c_str(),"Purchase Level") == 0)
						{
							outputStatus.purchaseLevelVinfast.assign(pairValue.second);
						}
						if(tc_strcmp(pairValue.first.c_str(),"Change Index") == 0)
						{
							outputStatus.changeIndex.assign(pairValue.second);
						}
						if(tc_strcmp(pairValue.first.c_str(),"Donor Vehicle") == 0)
						{
							outputStatus.donorVehicle.assign(pairValue.second);
						}
						if(tc_strcmp(pairValue.first.c_str(),"SOR release date - Reality") == 0)
						{
							if(tc_strcmp(pairValue.second.c_str(), "") != 0)
							{
								date_t req_date = NULLDATE;
								logical isValid = false;
								CHECK_FAIL(DATE_string_to_date_t((char*)pairValue.second.c_str(), &isValid, &req_date));
								outputStatus.sorReleasedDateActual = req_date;
							}else{
								outputStatus.sorReleasedDateActual = NULLDATE;
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"Plan Date - Supplier Award (LON)") == 0)
						{
							if(tc_strcmp(pairValue.second.c_str(), "") != 0)
							{
								date_t req_date = NULLDATE;
								logical isValid = false;
								CHECK_FAIL(DATE_string_to_date_t((char*)pairValue.second.c_str(), &isValid, &req_date));
								outputStatus.supplierNominationDatePlan = req_date;
							}else{
								outputStatus.supplierNominationDatePlan = NULLDATE;
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"Actual Date - Supplier Award (LON)") == 0)
						{
							if(tc_strcmp(pairValue.second.c_str(), "") != 0)
							{
								date_t req_date = NULLDATE;
								logical isValid = false;
								CHECK_FAIL(DATE_string_to_date_t((char*)pairValue.second.c_str(), &isValid, &req_date));
								outputStatus.supplierNominationDateReality = req_date;
							}else{
								outputStatus.supplierNominationDateReality = NULLDATE;
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"Award Supplier") == 0)
						{
							outputStatus.nominatedSupplier = pairValue.second;
						}
						if(tc_strcmp(pairValue.first.c_str(),"Manufacturing Country") == 0)
						{
							outputStatus.manufacturingCountry = pairValue.second;
						}
						if(tc_strcmp(pairValue.first.c_str(),"Status") == 0)
						{
							int isDateNull =1;
							date_t tempDate = outputStatus.supplierNominationDatePlan;
							isDateNull = DATE_IS_NULL(tempDate);
							if(tc_strcmp(outputStatus.purchaseLevelVinfast.c_str(),"P")== 0||tc_strcmp(outputStatus.purchaseLevelVinfast.c_str(),"DPT")== 0||tc_strcmp(outputStatus.purchaseLevelVinfast.c_str(),"DPS")== 0||tc_strcmp(outputStatus.purchaseLevelVinfast.c_str(),"BL")== 0)
							{
								if(outputStatus.nominatedSupplier.length()>0)
								{
									outputStatus.supplierNominationStatus.assign("Finished");
								}
								else if(isDateNull == 0)
								{
									int result = 0;
									date_t currentDate = getCurrentDate();
									CHECK_FAIL(POM_compare_dates(outputStatus.supplierNominationDatePlan, currentDate, &result));
									if(result == 1)
									{
										outputStatus.supplierNominationStatus.assign("Delayed");
									}else{
										outputStatus.supplierNominationStatus.assign("Process");
									}

								}else
								{
									outputStatus.supplierNominationStatus.assign("Not plan yet");
								}
							}else
							{
								outputStatus.supplierNominationStatus = "";
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"In Process P") == 0)
						{
							outputStatus.pInProgress = pairValue.second;
						}
						if(tc_strcmp(pairValue.first.c_str(),"P Release Plan Date") == 0)
						{
							if(tc_strcmp(pairValue.second.c_str(), "") != 0)
							{
								date_t req_date = NULLDATE;
								logical isValid = false;
								CHECK_FAIL(DATE_string_to_date_t((char*)pairValue.second.c_str(), &isValid, &req_date));
								outputStatus.pReleasePlanDate = req_date;
							}else{
								outputStatus.pReleasePlanDate = NULLDATE;
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"P Released Date") == 0)
						{
							if(tc_strcmp(pairValue.second.c_str(), "") != 0)
							{
								date_t req_date = NULLDATE;
								logical isValid = false;
								CHECK_FAIL(DATE_string_to_date_t((char*)pairValue.second.c_str(), &isValid, &req_date));
								outputStatus.pReleasedDate = req_date;
							}else{
								outputStatus.pReleasedDate = NULLDATE;
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"P release Status") == 0)
						{

							int isDateNull =1;
							int isPDateNull =1;
							date_t tempDate = outputStatus.pReleasedDate;
							date_t tempDate2 = outputStatus.pReleasePlanDate;
							isDateNull = DATE_IS_NULL(tempDate);
							isPDateNull = DATE_IS_NULL(tempDate2);
							if(isDateNull == 0)
							{
								outputStatus.pReleaseStatus.assign("Finished");
							}
							else if(isPDateNull == 0)
							{
								int result = 0;
								date_t currentDate = getCurrentDate();
								CHECK_FAIL(POM_compare_dates(outputStatus.prReleasePlanDate, currentDate, &result));
								if(result == 1)
								{
									outputStatus.pReleaseStatus.assign("Delayed");
								}else{
									outputStatus.pReleaseStatus.assign("WIP");
								}
							}
							else
							{
								outputStatus.pReleaseStatus.assign("Not plan yet");
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"In Process PR") == 0)
						{
							outputStatus.prInProgress = pairValue.second;
						}
						if(tc_strcmp(pairValue.first.c_str(),"PR Release Plan Date") == 0)
						{
							if(tc_strcmp(pairValue.second.c_str(), "") != 0)
							{
								date_t req_date = NULLDATE;
								logical isValid = false;
								CHECK_FAIL(DATE_string_to_date_t((char*)pairValue.second.c_str(), &isValid, &req_date));
								outputStatus.prReleasePlanDate = req_date;
							}else{
								outputStatus.prReleasePlanDate = NULLDATE;
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"PR Released Date") == 0)
						{
							if(tc_strcmp(pairValue.second.c_str(), "") != 0)
							{
								date_t req_date = NULLDATE;
								logical isValid = false;
								CHECK_FAIL(DATE_string_to_date_t((char*)pairValue.second.c_str(), &isValid, &req_date));
								outputStatus.prReleasedDate = req_date;
							}else{
								outputStatus.prReleasedDate = NULLDATE;
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"PR release Status") == 0)
						{
							int isDateNull = 1;
							int isPDateNull = 1;
							date_t tempDate = outputStatus.prReleasedDate;
							date_t tempDate2 = outputStatus.prReleasePlanDate;
							isDateNull = DATE_IS_NULL(tempDate);
							isPDateNull = DATE_IS_NULL(tempDate2);
							if(isDateNull == 0)
							{
								outputStatus.prReleasedStatus.assign("Finished");
							}
							else if(isPDateNull == 0)
							{
								int result = 0;
								date_t currentDate = getCurrentDate();
								CHECK_FAIL(POM_compare_dates(outputStatus.prReleasePlanDate, currentDate, &result));
								if(result == 1)
								{
									outputStatus.prReleasedStatus.assign("Delayed");
								}else{
									outputStatus.prReleasedStatus.assign("WIP");
								}
							}
							else
							{
								outputStatus.prReleasedStatus.assign("Not plan yet");
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"In Process I") == 0)
						{
							outputStatus.iInProgress = pairValue.second;
						}
						if(tc_strcmp(pairValue.first.c_str(),"I Release Plan Date") == 0)
						{
							if(tc_strcmp(pairValue.second.c_str(), "") != 0)
							{
								date_t req_date = NULLDATE;
								logical isValid = false;
								CHECK_FAIL(DATE_string_to_date_t((char*)pairValue.second.c_str(), &isValid, &req_date));
								outputStatus.iReleasePlanDate = req_date;
							}else{
								outputStatus.iReleasePlanDate = NULLDATE;
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"I Released Date") == 0)
						{
							if(tc_strcmp(pairValue.second.c_str(), "") != 0)
							{
								date_t req_date = NULLDATE;
								logical isValid = false;
								CHECK_FAIL(DATE_string_to_date_t((char*)pairValue.second.c_str(), &isValid, &req_date));
								outputStatus.iReleasedDate = req_date;
							}else{
								outputStatus.iReleasedDate = NULLDATE;
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"I release Status") == 0)
						{
							int isDateNull =1;
							int isPDateNull =1;
							date_t tempDate = outputStatus.iReleasedDate;
							date_t tempDate2 = outputStatus.iReleasePlanDate;
							isDateNull = DATE_IS_NULL(tempDate);
							isPDateNull = DATE_IS_NULL(tempDate2);
							if(tc_strcmp(outputStatus.prReleasedStatus.c_str(),"Finished") == 0||isDateNull == 0)
							{
								outputStatus.iReleaseStatus.assign("Finished");
							}
							else if(isPDateNull == 0)
							{
								int result = 0;
								date_t currentDate = getCurrentDate();
								CHECK_FAIL(POM_compare_dates(outputStatus.iReleasePlanDate, currentDate, &result));
								if(result == 1)
								{
									outputStatus.iReleaseStatus.assign("Delayed");
								}else{
									outputStatus.iReleaseStatus.assign("WIP");
								}
							}
							else
							{
								outputStatus.iReleaseStatus.assign("Not plan yet");
							}
						}
						if(tc_strcmp(pairValue.first.c_str(),"originPart") == 0)
						{
							outputStatus.originPart.assign(pairValue.second);
						}
					}
					response.outputs.push_back(outputStatus);
				}
				ifail = vf_close_BOM_window(tWindowTag, false);

			}
		}
		catch(const IFail &e)
		{
			std::cout << e.getMessage();
			response.serviceData.addErrorStack();
			TC_write_syslog("[VF]Error ::%s\n", e.getMessage());
		}
	}
	TC_write_syslog("[VF] EXIT %s", __FUNCTION__);
	return response;
}
