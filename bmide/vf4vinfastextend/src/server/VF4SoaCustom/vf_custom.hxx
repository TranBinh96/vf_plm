/*
 * vf_custom.hxx
 *
 *  Created on: Oct 8, 2020
 *      Author: vinfastplm
 */

#ifndef VF_CUSTOM_HXX_
#define VF_CUSTOM_HXX_

#include <iostream>
#include <stdio.h>
#include <string>
#include <string.h>
#include <fstream>
#include <vector>
#include <tc/tc_startup.h>
#include <textsrv/textserver.h>
#include <epm/epm.h>
#include <epm/epm_errors.h>
#include <epm/epm_toolkit_tc_utils.h>
#include <user_exits/epm_toolkit_utils.h>
#include <tc/tc_util.h>
#include <sa/tcfile.h>
#include <tccore/tctype.h>
#include <tc/tc_arguments.h>
#include <sa/sa.h>
#include <tccore/item.h>
#include <cfm/cfm.h>
#include <tccore/aom.h>
#include <tccore/project.h>
#include <epm/cr.h>
#include <epm/cr_action_handlers.h>
#include <tccore/custom.h>
#include <ae/ae.h>
#include  <RES\res_itk.h>
#include <tccore/aom_prop.h>
#include <epm/epm_task_template_itk.h>
#include <direct.h>
#include <fclasses/tc_date.h>
#include <tc/emh.h>
#include <tc/emh_errors.h>
#include <tc/envelope.h>
#include <tc/folder.h>
#include <tccore/grm.h>
#include <tccore/grm_errors.h>
#include <tccore/grmtype.h>
#include <ict/ict_userservice.h>
#include <tccore/method.h>
#include <property/nr.h>
#include <pom/pom/pom.h>
#include <pom/pom/pom_errors.h>
#include <tc/preferences.h>
#include <property/prop.h>
#include <ps/ps.h>
#include <sa/sa_errors.h>
#include <ss/ss_const.h>
#include <ss/ss_errors.h>
#include <time.h>
#include <sa/user.h>
#include <user_exits/user_exits.h>
#include <tccore/workspaceobject.h>
#include <tc/wsouif_errors.h>
#include <sa/groupmember.h>
#include <ecm/ecm.h>
#include <qry/qry.h>
#include <bom/bom.h>
#include <epm/signoff.h>
#include <ics\ics.h>
#include <ics\ics2.h>
#include <tccore\uom.h>
#include <map>

#include <reportdatasource2010impl.hxx>

using namespace std;

using namespace VF4::Soa::Custom::_2020_10;

#define VF_IS_DEBUG false

#define EXIT_FAILURE 1
#define ERROR_CHECK(X) (report_error( __FILE__, __LINE__, #X, (X)))


#define VF_DOUBLE_NULL -0.000001

#define VF_DATE_LONG_NULL -1
#define VF_DATE_LONG_MAX 30000101000000
// CUT OFF DATE 2020 Oct 20 00:00:00 UTC+7
#define VF_CUT_OFF_DATE 20200919170000

#define VF_RETURN_COST_CURRENCY "USD"

#define VF_COST_TYPE_ST "ST"
#define VF_COST_TYPE_TARGET "Target"
#define VF_COST_TYPE_ENG "Cost Engineering Estimate ( New added part/ no quote)"
#define VF_COST_TYPE_ECR "Latest ECR"
#define VF_COST_TYPE_SAP "Scheduling Agreement"

#define ATTR_LAST_MOD_DATE "last_mod_date"
#define ATTR_BVR "structure_revisions"
#define ATTR_BVR_OCCURENCE "bvr_occurrences"

#define SAP_PIECE_COST "vf4SAPieceCost"
#define SAP_PIECE_COST_CURR "vf4SAPieceCostCurrency"
#define SAP_EDnD_COST "vf4EDDCost"
#define SAP_EDnD_COST_CURR "vf4EDDCostCurrency"
#define SAP_TOOLING_COST "vf4ToolingCost"
#define SAP_TOOLING_COST_CURR "vf4ToolingCostCurrency"
#define SAP_APPROVAL_DATE_PIECE "vf4SACostReleaseDate"
#define SAP_APPROVAL_DATE_EDnD "vf4EDDCostReleaseDate"
#define SAP_APPROVAL_DATE_TOOLING "vf4ToolingCostReleaseDate"

#define TARGET_COST_PIECE "vf4_piece_cost_value_target"
#define TARGET_COST_PIECE_ON_PARENT	"vf4_isPieceCostInParentPart"
#define TARGET_COST_EDnD "vf4_ednd_cost_value_target"
#define TARGET_COST_EDnD_ON_PARENT "vf4_isEDDCostInParentPart"
#define TARGET_COST_TOOLING "vf4_tooling_invest_target"
#define TARGET_COST_TOOLING_ON_PARENT "vf4_isToolCostInParentPart"

#define ENG_COST_PIECE "vf4_piece_cost_value_status"
#define ENG_COST_EDnD "vf4EdndCost"
#define ENG_COST_TOOLING "vf4_tooling_invest_value"
#define ENG_COST_TAX "vf4_prd_tax_absolute"
#define ENG_COST_LABOUR "vf4_miscellaneous_cost"
#define ENG_COST_PACKING "vf4_supplier_package_amount"
#define ENG_COST_LOGISTIC "vf4_supplier_logisis_cost"
#define ENG_COST_STATUS "vf4_quality_of_finance"
#define ENG_COST_TOTAL_PIECE "vf4_total_cost"
#define ENG_COST_APPROVAL_DATE "vf4_cost_approval_date"

#define ENG_COST_PIECE_CURR "vf4_piece_cost_curr"
#define ENG_COST_EDnD_CURR "vf4Ednd_curr"
#define ENG_COST_TOOLING_CURR "vf4_tooling_invtest_curr"

#define PURCHASING_COST_PIECE "vf6_material_costs"
#define PURCHASING_COST_EDND "vf6_supplier_eng_costs"
#define PURCHASING_COST_TOOLING "vf6_tooling_costs"
#define PURCHASING_COST_PACKING "vf6_pur_packing_cost"
#define PURCHASING_COST_LOGISTIC "vf6_pur_logistic_cost"

#define MANUFACTURING_COST_PIECE "vf6_manu_piece_costs"
#define MANUFACTURING_COST_EDND "vf6_manu_supplier_eng_costs"
#define MANUFACTURING_COST_TOOLING "vf6_manu_tooling_costs"
#define MANUFACTURING_COST_PACKING "vf6_manuf_packing_cost"
#define MANUFACTURING_COST_LOGISTIC "vf6_manuf_logistic_cost"

#define ST_COST_PIECE "vf4_supplier_piece_cost_exw"
#define ST_COST_EDnD "vf4_eddorder_value_2"
#define ST_COST_TOOLING "vf4_tooling_order_2"
#define ST_COST_PACKING "vf4_supplier_pkg_amount"
#define ST_COST_LOGISTIC "vf4_Supplier_logistic_cost"
#define ST_COST_APPROVAL_DATE "vf4_cost_approval_date"
#define ST_COST_STATUS "vf4_piece_cost_status"
#define ST_COST_PIECE_CURR "vf4_piece_currency"
#define ST_COST_TOOLING_CURR "vf4_tooling_currency"
#define ST_COST_EDnD_CURR "vf4_eddorder_currency"

#define ST_SAP_COST_PIECE "vf4_sap_gross_price"
#define ST_SAP_COST_EDnD "vf4_sap_edd_cost"
#define ST_SAP_COST_TOOLING "vf4_sap_tooling_cost"
#define ST_SAP_COST_PIECE_CURR "vf4_sap_gross_price_curr"
#define ST_SAP_COST_TOOLING_CURR "vf4_sap_tooling_curr"
#define ST_SAP_COST_EDnD_CURR "vf4_sap_edd_cost_curr"

void report_error( char *file, int line, char *function, int return_code);
ReportDataSourceImpl::VFCost getCurrentCost(string partNumber, string stProgram);
void vf_split(std::string str, const std::string &delimiter, std::vector<std::string> &result);
void createMaps(vector<string> pns, map<string, vector<string>> &oldToNews, map<string, vector<string>> &newToOlds);
ReportDataSourceImpl::VFCost getVFCostFromEcr(tag_t ecr, string partNumber);
void printCost(char *prefix, ReportDataSourceImpl::VFCost cost);
std::string trim(const std::string& s);

void printOutput(ReportDataSourceImpl::FGValidationOutput);
void findItemAndRev(const char* itemId, const char *itemType, const char*revId,
		tag_t &item, tag_t &rev, vector<string> &errorMsgs);
void getTopLine(tag_t item, tag_t rev, const char *revisionRule,
		const char *savedVariant, tag_t &topLine, vector<string> &errorMsgs);
void getFilteredRules(const char *program, const char *variant,
		map<string, ReportDataSourceImpl::FGValidationResult> &fgCodesAndValResults,
		vector<string> &errorMsgs);
void groupFGFromBomRecursive(tag_t bomline,
		map<string, vector<ReportDataSourceImpl::FGValidationPartInfo>> &fgsAndPartInfos,
		vector<string> &errorMsgs);
void printFGinBOM(const map<string, vector<ReportDataSourceImpl::FGValidationPartInfo>> &fgsInBOM);
void validateFG(const map<string, ReportDataSourceImpl::FGValidationResult> &filteredRules,
		const map<string, vector<ReportDataSourceImpl::FGValidationPartInfo>> &fgsInBOM,
		ReportDataSourceImpl::FGValidationOutput &output, int &failedNum, int &warningNum);
void finalizeOutput(const ReportDataSourceImpl::FGValidationInput &input, const int failedNum,
		const int warningNum, const vector<string> &errorMsgs,
		ReportDataSourceImpl::FGValidationOutput &output);



void queryObj(const char *queryName, map<string, string> keysAndVals,
			  vector<tag_t> &founds, vector<string> &errorMsgs);
void printOutput(ReportDataSourceImpl::VFPartCostCalcResponse);
ReportDataSourceImpl::VFCost getSapCost(tag_t part, const std::string &partMakeBuyInput, tag_t costRev, const char *type);
tag_t getCostRev(string partNumber, vector<string> &errorMsgs);
//ReportDataSourceImpl::VFCost getSapCost(tag_t part, tag_t costRev, const std::string &partMakeBuyInput);
ReportDataSourceImpl::VFCost getTargetCost(tag_t part, tag_t costRev);
ReportDataSourceImpl::VFCost getEngCost(tag_t part, tag_t costRev, const std::string &partMakeBuyInput);
ReportDataSourceImpl::VFCost getEcrCostBuyPart(tag_t part, tag_t costRev, tag_t sourcePart);
ReportDataSourceImpl::VFCost getEcrCostMadePart(tag_t part, tag_t costRev);
ReportDataSourceImpl::VFCost getStCost(tag_t part, tag_t costRev, tag_t &sourcePart, const std::string &partMakeBuyInput);
tag_t getPart(string partNumber, string partType, vector<string> &errorMsgs);
ReportDataSourceImpl::VFPartCost getFinalCostBuyPart(const vector<ReportDataSourceImpl::VFCost> &sortedCosts);
ReportDataSourceImpl::VFPartCost getFinalCostMadePart(vector<ReportDataSourceImpl::VFCost> sortedCosts, bool isAseembly, bool isIncludePieceCostOnMadeAssembly);
void putAndSort(vector<ReportDataSourceImpl::VFCost> &sortedCosts, const ReportDataSourceImpl::VFCost &cost);
void printObjString(tag_t obj, const char *prefix);
void setAllCostNull(ReportDataSourceImpl::VFCost &cost);
bool isMakePart(tag_t part, const std::string &partMakeBuyInput);
bool isBoughtPart(tag_t isBoughtPart, const std::string &partMakeBuyInput);
bool checkIsAssembly(tag_t part);
bool getLogicalProperty(tag_t obj, const char *stringPropName);
tag_t createCostForm(const char *costFormTypeName, const char *formName);
void createRelation(const tag_t &secondary, const tag_t &primary, const char* relationTypeName);
vector<tag_t> getRelatedForms(tag_t wso, const char *rel, const char* filterType);

#endif /* VF_CUSTOM_HXX_ */
