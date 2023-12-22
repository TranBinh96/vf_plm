//@<COPYRIGHT>@
//==================================================
//Copyright $2022.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/* 
 * @file 
 *
 *   This file contains the implementation for the Extension VF4_BOMViewRevSavePre
 *
 */
#include <VF4_VinFastExtend/VF4_BOMViewRevSavePre.hxx>
#include <extensionframework/OperationDispatcher.hxx>
#include <tccore/aom.h>
#include <tc/emh.h>
#include <bom/bom.h>
#include <set>
#include <map>
#include <vector>
#include <string>
#include <string.h>
#include <cfm/cfm.h>
#include <sa/user.h>
#include <ae/dataset.h>
#include <tc/preferences.h>
#include <tc/emh_errors.h>
#include <tccore/aom_prop.h>
#include <fclasses/tc_string.h>
#include <base_utils/ScopedPtr.hxx>
#include <base_utils/ScopedSmPtr.hxx>
#include <base_utils/TcResultStatus.hxx>
#include <epm/epm_toolkit_tc_utils.h>

using namespace std;
using namespace Teamcenter;

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

#define MAINMODEMPTY 			"Main Module English cannot be empty"
#define MODGRPEMPTY  			"Module Group English cannot be empty"
#define MODNMPEMPTY  			"Module Name cannot be empty"
//#define POSIDEMPTY  			"Pos Id cannot be empty"
#define PURCHASELVLEMPTY  		"Purchase Level cannot be empty"
#define PURCHASELEVEL 			"Purchase Level H can only be set on Items marked for Make"

std::vector<std::string> specialChar {", ",","};

void replaceSpecialChar(string& input);
int getFileDatasets(const std::map<string, string>& attrMap, set<tag_t>& vecDatasetTags);

int VF4_BOMViewRevSavePre( METHOD_message_t * msg, va_list /*args*/ )
{
	TC_write_syslog("[fnd0Save] Call from VF4_BOMViewRevSavePre\n");

	int ifail = ITK_ok;
	tag_t bvrTag = msg->object;
	char ErrorMessage[2048] = "";

	int prefCount = 0;
	logical prefValue = false;
	CHECK_FAIL(PREF_ask_value_count("vf_BOMValidation_isEnabled", &prefCount));
	if (prefCount > 0) {
		CHECK_FAIL(PREF_ask_logical_value("vf_BOMValidation_isEnabled", 0, &prefValue));
	}

	if (prefValue) {

		tag_t tCurrentGroupMemberTag = NULLTAG;
		tag_t group_tag = NULLTAG;
		Teamcenter::scoped_smptr<char> groupname;

		CHECK_FAIL(SA_ask_current_groupmember(&tCurrentGroupMemberTag));
		CHECK_FAIL(SA_ask_groupmember_group(tCurrentGroupMemberTag, &group_tag));
		CHECK_FAIL(SA_ask_group_full_name(group_tag, &groupname));

		int n_values = 0;
		int groupCount = 0;
		logical bomValidationFlag = false;
		Teamcenter::scoped_smptr<char*> groupValues;
		CHECK_FAIL(PREF_ask_value_count("VF_BOM_VALIDATION_GROUPS", &groupCount));
		if (groupCount > 0) {
			CHECK_FAIL(PREF_ask_char_values("VF_BOM_VALIDATION_GROUPS", &n_values, &groupValues));
			for(int i = 0; i < n_values; i++) {
				if (tc_strstr(groupValues.getString()[i], groupname.getString()) != NULL) {
					bomValidationFlag = true;
				}
			}
		}

		if (bomValidationFlag) {
			logical lNull = false;
			logical lEmpty = false;
			tag_t userSession = NULLTAG;
			tag_t attrUserName = NULLTAG;
			Teamcenter::scoped_smptr<char> userName;

			CHECK_FAIL(POM_ask_session(&userSession));
			CHECK_FAIL(POM_attr_id_of_attr("user_name", "pom_session", &attrUserName));
			CHECK_FAIL(POM_ask_attr_string(userSession,attrUserName, &userName, &lNull, &lEmpty));

			Teamcenter::scoped_smptr<char> syslogName;
			CHECK_FAIL(EMH_ask_system_log_filename(&syslogName));
			string logName = "";
			logName.assign(syslogName.getString());
			int posStart = logName.find("tcserver.exe");
			int posEnd = logName.find(".syslog");
			int start = posStart + tc_strlen("tcserver.exe");
			int len = posEnd - start;
			string token = logName.substr(start, len);
			string fileName ="BOMValidation_";
			fileName.append(userName.getString());
			fileName.append("_");
			fileName.append(token);
			//TC_write_syslog("[fnd0Save] Token file Name [%s]\n", fileName.c_str());

			set<tag_t> setFoundFileDatasets;
			std::map<string, string> attrMap;
			attrMap["Name"] = fileName;
			attrMap["Dataset Type"] = "Text";

			CHECK_FAIL(getFileDatasets(attrMap, setFoundFileDatasets));

			bool bFileTag = false;
			if (setFoundFileDatasets.size() != 0)
				bFileTag = true;
			setFoundFileDatasets.clear();

			if (bFileTag) {
				tag_t window = NULLTAG;
				CHECK_FAIL(BOM_create_window(&window));

				bool bValidationFailed = false;

				int iMainModuleAttrb = 0;
				int iModuleGroupAttrb = 0;
				//int iPosIdAttrb = 0;
				int iPurchaseLvlAttrb = 0;
				int iFindNumAttrb = 0;
				int iModuleNameAttrb = 0;

				CHECK_FAIL(BOM_line_look_up_attribute("VL5_main_module", &iMainModuleAttrb));
				CHECK_FAIL(BOM_line_look_up_attribute("VL5_module_group", &iModuleGroupAttrb));
				CHECK_FAIL(BOM_line_look_up_attribute("VL5_module_name", &iModuleNameAttrb));
				//CHECK_FAIL(BOM_line_look_up_attribute("VL5_pos_id", &iPosIdAttrb));
				CHECK_FAIL(BOM_line_look_up_attribute("VL5_purchase_lvl_vf", &iPurchaseLvlAttrb));
				CHECK_FAIL(BOM_line_look_up_attribute(bomAttr_occSeqNo, &iFindNumAttrb));

				tag_t top_line = NULLTAG;
				CHECK_FAIL(BOM_set_window_top_line_bvr(window, bvrTag, &top_line));

				Teamcenter::scoped_smptr<char> strParentBLName;
				CHECK_FAIL(AOM_ask_value_string(top_line, bomAttr_lineName, &strParentBLName));

				int n_children;
				Teamcenter::scoped_smptr<tag_t> tChildren;
				CHECK_FAIL(BOM_line_ask_child_lines(top_line, &n_children, &tChildren));
				for (int ii = 0; ii < n_children; ii++) {
					tag_t item_tag = NULLTAG;
					set<string> errorSet;

					Teamcenter::scoped_smptr<char> makeBuy;
					Teamcenter::scoped_smptr<char> sMainModule ;
					Teamcenter::scoped_smptr<char> sModuleGroup;
					Teamcenter::scoped_smptr<char> sModuleName;
					//Teamcenter::scoped_smptr<char> sPosId;
					Teamcenter::scoped_smptr<char> sPurchaseLevel;
					Teamcenter::scoped_smptr<char> sFindNum;
					Teamcenter::scoped_smptr<char> strChildBLName;
					CHECK_FAIL(AOM_ask_value_string(tChildren[ii], bomAttr_lineName, &strChildBLName));

					CHECK_FAIL(AOM_ask_value_tag(tChildren[ii], bomAttr_lineItemTag, &item_tag));
					CHECK_FAIL(AOM_ask_value_string(item_tag, "vf4_item_make_buy", &makeBuy));
					CHECK_FAIL(BOM_line_ask_attribute_string(tChildren[ii], iMainModuleAttrb, &sMainModule));
					CHECK_FAIL(BOM_line_ask_attribute_string(tChildren[ii], iModuleGroupAttrb, &sModuleGroup));
					CHECK_FAIL(BOM_line_ask_attribute_string(tChildren[ii], iModuleNameAttrb, &sModuleName));					
					//CHECK_FAIL(BOM_line_ask_attribute_string(tChildren[ii], iPosIdAttrb, &sPosId));
					CHECK_FAIL(BOM_line_ask_attribute_string(tChildren[ii], iPurchaseLvlAttrb, &sPurchaseLevel));
					CHECK_FAIL(BOM_line_ask_attribute_string(tChildren[ii], iFindNumAttrb, &sFindNum));

					if (tc_strcmp(sMainModule.getString(),"") == 0) {
						errorSet.insert(MAINMODEMPTY);
						bValidationFailed = true;
					}

					if (tc_strcmp(sModuleGroup.getString(),"") == 0) {
						errorSet.insert(MODGRPEMPTY);
						bValidationFailed = true;
					}
					
					if (tc_strcmp(sModuleName.getString(),"") == 0) {
						errorSet.insert(MODNMPEMPTY);
						bValidationFailed = true;
					}

					/*if (tc_strcmp(sPosId.getString(),"") == 0) {
						errorSet.insert(POSIDEMPTY);
						bValidationFailed = true;
					}*/

					if (tc_strcmp(sPurchaseLevel.getString(),"") == 0) {
						errorSet.insert(PURCHASELVLEMPTY);
						bValidationFailed = true;
					}
					else if (tc_strcmp(sPurchaseLevel.getString(),"H") == 0
							&& !(tc_strcmp(makeBuy.getString(),"Make") == 0)) {
						errorSet.insert(PURCHASELEVEL);
						bValidationFailed = true;
					}

					if (errorSet.size() > 0) {
						string errorString;
						bool flag = false;
						string parentBLName ="";
						string childBLName ="";
						parentBLName.assign(strParentBLName.getString());
						childBLName.assign(strChildBLName.getString());

						replaceSpecialChar(parentBLName);
						replaceSpecialChar(childBLName);

						errorString.assign(parentBLName);
						errorString.append(",").append(childBLName);

						if (tc_strlen(sFindNum.getString()) == 0)
							errorString.append(", ");
						else
							errorString.append(",").append(sFindNum.getString());

						errorString.append(",");
						for (auto& error : errorSet) {
							if (flag)
								errorString.append(" ; ");
							errorString.append(error);
							flag = true;
						}

						sprintf_s(ErrorMessage, 2048, "%s", errorString.c_str());
						CHECK_FAIL(EMH_store_error_s1(EMH_severity_error,950003,ErrorMessage));

						errorSet.clear();
					}
				}
				if (bValidationFailed) {
					sprintf_s(ErrorMessage, 2048, "%s,%s,%s,%s", "Parent", "BOM Line", "Find No", "Error Message");
					CHECK_FAIL(EMH_store_error_s1(EMH_severity_error,950003,ErrorMessage));
					return 950003;
				}
				else
					return ifail;
			}
			else
				return ITK_ok;
		}
		else {
			TC_write_syslog("[fnd0Save] No matching group found with user current session group hence skipping the validation\n");
			return ITK_ok;
		}
	}
	else {
		TC_write_syslog("[fnd0Save] Preference value is false hence skipping the validation\n");
		return ITK_ok;
	}
}

void replaceSpecialChar(string& input)
{
	for (int i = 0; i < specialChar.size(); i++)
	{
		if (tc_strstr(input.c_str(), specialChar[i].c_str()) != NULL) {
			Teamcenter::scoped_smptr<char> newString;
			CHECK_FAIL(STRNG_replace_str(input.c_str(), specialChar[i].c_str(), " ", &newString));
			input.assign(newString.getString());
		}
	}
}

int getFileDatasets(const std::map<string, string>& attrMap, set<tag_t>& setDatasetTags)
{
	int ifail = ITK_ok;
	int nResults = 0;
	tag_t tQueryTag = NULLTAG;
	const char* queryName = "Dataset...";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	setDatasetTags.clear();

	CHECK_FAIL(QRY_ignore_case_on_search(TRUE));
	CHECK_FAIL(QRY_find2(queryName, &tQueryTag));
	if (tQueryTag == NULLTAG) {
		TC_write_syslog("ERROR: [fnd0Save] Cannot find query %s:\n", queryName);
		return ITK_ok;
	}

	int iCount = (int)attrMap.size();
	Teamcenter::scoped_ptr<char*, Teamcenter::StringArrayFreer> pchNames, pchValues;
	pchNames = (char **)MEM_alloc(sizeof(char*) * iCount);
	pchValues = (char **)MEM_alloc(sizeof(char*) * iCount);
	int iIndex = 0;
	for (auto it = attrMap.begin(); it != attrMap.end(); it++) {
		pchNames[iIndex] = MEM_string_copy(it->first.c_str());
		pchValues[iIndex] = MEM_string_copy(it->second.c_str());
		iIndex++;
	}

	CHECK_FAIL(QRY_execute(tQueryTag, iCount, pchNames, pchValues, &nResults, &tQryResults));
	TC_write_syslog("[fnd0Save] File Token count [%d]\n", nResults);

	for (int ii = 0; ii < nResults; ii++) {
		tag_t tLatestDataset = NULLTAG;
		CHECK_FAIL(AE_ask_dataset(tQryResults[ii], &tLatestDataset));

		if (tLatestDataset != NULLTAG)
			setDatasetTags.insert(tLatestDataset);
	}


	return ifail;
}
