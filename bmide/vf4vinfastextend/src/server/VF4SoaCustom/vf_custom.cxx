/*
 * vf_custom.cxx
 *
 *  Created on: Oct 8, 2020
 *      Author: vinfastplm
 */

#include <reportdatasource2010impl.hxx>

#include "vf_custom.hxx"

const char* ATTR_ST_APPROVED_DATE = "vf4_cost_approval_date";
const char* ATTR_ECR_RELEASE_DATE = "date_released";

using namespace VF4::Soa::Custom::_2020_10;
using namespace Teamcenter::Soa::Server;

void vf_split(std::string str, const std::string &delimiter, std::vector<std::string> &result)
{
	//TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	result.clear();
	size_t pos = 0;
	std::string token;

	while ((pos = str.find(delimiter)) != std::string::npos)
	{
		token = str.substr(0, pos);
		result.push_back(token);
		str.erase(0, pos + delimiter.length());
	}
	result.push_back(str);

	//TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

const std::string WHITESPACE = " \n\r\t\f\v";

std::string ltrim(const std::string& s)
{
	size_t start = s.find_first_not_of(WHITESPACE);
	return (start == std::string::npos) ? "" : s.substr(start);
}

std::string rtrim(const std::string& s)
{
	size_t end = s.find_last_not_of(WHITESPACE);
	return (end == std::string::npos) ? "" : s.substr(0, end + 1);
}

std::string trim(const std::string& s)
{
	return rtrim(ltrim(s));
}

ReportDataSourceImpl::VFCost getEcrFormCost(tag_t ecrCostForm, const char *partNumber)
{
	char *formType = NULL;
	char *formName = NULL;
	ReportDataSourceImpl::VFCost cost;
	ERROR_CHECK(AOM_ask_value_string(ecrCostForm, "object_type", &formType));
	ERROR_CHECK(AOM_ask_value_string(ecrCostForm, "object_name", &formName));
	double pieceCost = 0, edndCost = 0, toolingCost = 0, packingCost = 0, logisticCost = 0;
	if (tc_strcmp("Vf6_manufacturing", formType) == 0 && tc_strstr(formName, partNumber))
	{
		ERROR_CHECK(AOM_ask_value_double(ecrCostForm, "vf6_manu_piece_costs", &pieceCost));
		ERROR_CHECK(AOM_ask_value_double(ecrCostForm, "vf6_manu_supplier_eng_costs", &edndCost));
		ERROR_CHECK(AOM_ask_value_double(ecrCostForm, "vf6_manu_tooling_costs", &toolingCost));
		ERROR_CHECK(AOM_ask_value_double(ecrCostForm, "vf6_manuf_packing_cost", &packingCost));
		ERROR_CHECK(AOM_ask_value_double(ecrCostForm, "vf6_manuf_logistic_cost", &logisticCost));
		//AOM_ask_value_double(ecrCostForm, "vf6_manuf_total_piece_cost", &totalPieceCost);

		cost.edndCost = edndCost;
		cost.toolingCost = toolingCost;
		cost.pieceCost = pieceCost;
		cost.totalPieceCost = pieceCost + logisticCost + packingCost;
		cost.logisticCost = logisticCost;
		cost.packingCost = packingCost;
	}
	else if (tc_strcmp("Vf6_purchasing", formType) == 0 && tc_strstr(formName, partNumber))
	{
		ERROR_CHECK(AOM_ask_value_double(ecrCostForm, "vf6_material_costs", &pieceCost));
		ERROR_CHECK(AOM_ask_value_double(ecrCostForm, "vf6_supplier_eng_costs", &edndCost));
		ERROR_CHECK(AOM_ask_value_double(ecrCostForm, "vf6_tooling_costs", &toolingCost));
		ERROR_CHECK(AOM_ask_value_double(ecrCostForm, "vf6_pur_packing_cost", &packingCost));
		ERROR_CHECK(AOM_ask_value_double(ecrCostForm, "vf6_pur_logistic_cost", &logisticCost));
		//AOM_ask_value_double(ecrCostForm, "vf6_pur_total_piece_cost", &totalPieceCost);

		cost.edndCost = edndCost;
		cost.toolingCost = toolingCost;
		cost.pieceCost = pieceCost;
		cost.totalPieceCost = pieceCost + logisticCost + packingCost;
		cost.logisticCost = logisticCost;
		cost.packingCost = packingCost;
	}

	if (formType) MEM_free(formType);
	if (formName) MEM_free(formName);
	return cost;
}

ReportDataSourceImpl::VFCost getVFCostFromEcr(tag_t ecr, string partNumber)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	ReportDataSourceImpl::VFCost ecrCost;
	tag_t *costForms = NULL;
	tag_t costImpactsRel = NULLTAG;
	int costFormsCount = 0;
	ERROR_CHECK(TCTYPE_find_type("Vf6_change_forms", "Vf6_change_forms", &costImpactsRel));
	ERROR_CHECK(GRM_list_secondary_objects_only(ecr, costImpactsRel, &costFormsCount, &costForms));

	for (int i = 0; i < costFormsCount; i++)
	{
		ReportDataSourceImpl::VFCost ecrFormCost = getEcrFormCost(costForms[i], partNumber.c_str());
		ecrCost.edndCost += ecrFormCost.edndCost;
		ecrCost.logisticCost += ecrFormCost.logisticCost;
		ecrCost.packingCost += ecrFormCost.packingCost;
		ecrCost.pieceCost += ecrFormCost.pieceCost;
		ecrCost.toolingCost += ecrFormCost.toolingCost;
		ecrCost.totalPieceCost += ecrFormCost.totalPieceCost;
	}

	if (costForms) MEM_free(costForms);

	TC_write_syslog("\n[VF] LEAVE %s ", __FUNCTION__);

	return ecrCost;
}

vector<string>  getOldPartNumbersOf(string partNumber)
{
	vector<string> oldPartNumbers;
	tag_t vfDesignItem = NULLTAG;

	tag_t query = NULLTAG;
	ERROR_CHECK(QRY_find2("Item...", &query));
	//vf4_platform_module
	//vf4_bom_vfPartNumber
	char *entries[2] = { "Item ID", "Type"};
	char **values = (char**) MEM_alloc(sizeof(char*)*2);
	values[0] = (char*) MEM_alloc(sizeof(char)*(tc_strlen(partNumber.c_str()) + 1));
	values[1] = (char*) MEM_alloc(sizeof(char)*(tc_strlen("VF4_Design") + 1));

	partNumber = trim(partNumber);
	tc_strcpy(values[0], partNumber.c_str());
	tc_strcpy(values[1], "VF4_Design");

	int foundsNum = -1;
	tag_t *founds = NULL;
	ERROR_CHECK(QRY_execute(query, 2, entries, values, &foundsNum, &founds));
	if (foundsNum > 0 && founds)
	{
		vfDesignItem = founds[0];
		char *oldPartNumbersStr = NULL;
		ERROR_CHECK(AOM_ask_value_string(vfDesignItem, "vf4_orginal_part_number", &oldPartNumbersStr));

		if (tc_strlen(oldPartNumbersStr) > 2)
			vf_split(oldPartNumbersStr, ";", oldPartNumbers);

		if (oldPartNumbersStr) MEM_free(oldPartNumbersStr);
	}

	if(values && values[0]) MEM_free(values[0]);
	if(values && values[1]) MEM_free(values[1]);
	if(values) MEM_free(values);
	if(founds) MEM_free(founds);

	return oldPartNumbers;
}

void createMaps(vector<string> pns, map<string, vector<string>> &oldToNews, map<string, vector<string>> &newToOlds)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	for (string pn : pns)
	{
		TC_write_syslog("\nprocess %s", pn.c_str());
		vector<string> oldPns = getOldPartNumbersOf(pn);
		TC_write_syslog("\n oldPns=%d", oldPns.size());
		set<string> oldPnsSet(oldPns.begin(), oldPns.end());
		if (newToOlds.find(pn) != newToOlds.end())
		{
			vector<string> existedOldPns = newToOlds[pn];
			oldPnsSet.insert(existedOldPns.begin(), existedOldPns.end());
		}

		vector<string> oldPnsOfPn(oldPnsSet.begin(), oldPnsSet.end());
		if (oldPnsOfPn.size() > 0) newToOlds[pn] = oldPnsOfPn;

		for (string oldPn : oldPns)
		{
			set<string> newPnSet;
			newPnSet.insert(pn);
			if (oldToNews.find(oldPn) != oldToNews.end())
			{
				vector<string> existedNewPns = oldToNews[oldPn];
				newPnSet.insert(existedNewPns.begin(), existedNewPns.end());
			}

			vector<string> newPnsOfOldPn(newPnSet.begin(), newPnSet.end());
			if (newPnsOfOldPn.size() > 0) oldToNews[oldPn] = newPnsOfOldPn;
		}
	}
	// loop all pns do
	//     oldPNs = getOldPNsFrom(pn)
	//	   newToOlds[pn] += oldPNs;
	//     loop_oldPNs -> oldPn
	//         oldToNew[oldPn] += pn
	//     endloop_oldPNs
	// endloop

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}


double getRate(string sourceCurrency, string destCurrency)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	double rate = 1;
	int count = 0;
	char **vals = NULL;
	// USD_VND=23000
	ERROR_CHECK(PREF_ask_char_values("VF_EXCHANGE_RATE", &count, &vals));
	destCurrency.append("_").append(sourceCurrency);
	for (int i = 0; i < count; i++)
	{
		if (tc_strstr(vals[i], destCurrency.c_str()))
		{
			vector<string> words;
			vf_split(vals[i], "=", words);
			if (words.size() == 2)
			{
				string rateStr = words[1];
				rate = std::stof(rateStr);
			}
		}
	}

	if (vals) MEM_free(vals);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] Rate of %s=%8.8f", destCurrency.c_str(), rate);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return rate;
}

tag_t getSTOf(string partNumber, string stProgram)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	tag_t st = NULLTAG;

	tag_t query = NULLTAG;
	ERROR_CHECK(QRY_find2("Source Part", &query));
	//vf4_platform_module
	//vf4_bom_vfPartNumber
	char *entries[2] = { "VF Part Number", "Sourcing Program"};
	char **values = (char**) MEM_alloc(sizeof(char*)*2);
	values[0] = (char*) MEM_alloc(sizeof(char)*(tc_strlen(partNumber.c_str()) + 1));
	values[1] = (char*) MEM_alloc(sizeof(char)*(tc_strlen(stProgram.c_str()) + 1));

	partNumber = trim(partNumber);
	tc_strcpy(values[0], partNumber.c_str());
	tc_strcpy(values[1], stProgram.c_str());

	int foundsNum = -1;
	tag_t *founds = NULL;
	ERROR_CHECK(QRY_execute(query, 2, entries, values, &foundsNum, &founds));

	TC_write_syslog("\n [VF] values[0]=%s\nvalues[1]=%s\nfoundsNum=%d",values[0],values[1], foundsNum);
	if (foundsNum > 0 && founds)
	{
		st = founds[0];
	}

	if(values && values[0]) MEM_free(values[0]);
	if(values && values[1]) MEM_free(values[1]);
	if(values) MEM_free(values);
	if(founds) MEM_free(founds);

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return st;
}

ReportDataSourceImpl::VFCost getVFCostFromST(tag_t st)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	ReportDataSourceImpl::VFCost stVFCost;

	double tooling,ednd,piece,packing,logistic,totalPieceCost;
	char *pieceCurr = NULL, *edndCurr = NULL, *toolingCurr = NULL;

	ERROR_CHECK(AOM_ask_value_double(st, "vf4_tooling_order_2", &tooling));
	ERROR_CHECK(AOM_ask_value_double(st, "vf4_eddorder_value_2", &ednd));
	ERROR_CHECK(AOM_ask_value_double(st, "vf4_supplier_piece_cost_exw", &piece));
	ERROR_CHECK(AOM_ask_value_double(st, "vf4_supplier_pkg_amount", &packing));
	ERROR_CHECK(AOM_ask_value_double(st, "vf4_Supplier_logistic_cost", &logistic));
	ERROR_CHECK(AOM_ask_value_double(st, "vf4_piece_cost_auto", &totalPieceCost));

	ERROR_CHECK(AOM_ask_value_string(st, "vf4_piece_currency", &pieceCurr));
	ERROR_CHECK(AOM_ask_value_string(st, "vf4_eddorder_currency", &edndCurr));
	ERROR_CHECK(AOM_ask_value_string(st, "vf4_tooling_currency", &toolingCurr));

	if (tc_strcasecmp(pieceCurr, "USD") != 0)
	{
		double rate = getRate(pieceCurr, "USD");
		if (totalPieceCost > 0.000000001) totalPieceCost /= rate;
		if (piece > 0.000000001) piece /= rate;
		if (logistic > 0.000000001) logistic /= rate;
		if (packing > 0.000000001) packing /= rate;
	}

	if (ednd > 0.000000001 && tc_strcasecmp(edndCurr, "USD") != 0)
	{
		double rate = getRate(edndCurr, "USD");
		ednd /= rate;
	}

	if (tooling > 0.000000001 && tc_strcasecmp(toolingCurr, "USD") != 0)
	{
		double rate = getRate(toolingCurr, "USD");
		tooling /= rate;
	}

	stVFCost.edndCost = ednd;
	stVFCost.logisticCost = logistic;
	stVFCost.packingCost = packing;
	stVFCost.pieceCost = piece;
	stVFCost.toolingCost = tooling;
	stVFCost.totalPieceCost = totalPieceCost;

	if (pieceCurr) MEM_free(pieceCurr);
	if (edndCurr) MEM_free(edndCurr);
	if (toolingCurr) MEM_free(toolingCurr);

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return stVFCost;
}

bool checkShouldConsiderST(tag_t st)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	if (st == NULLTAG) return false;

	ReportDataSourceImpl::VFCost stVFCost = getVFCostFromST(st);

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return (stVFCost.edndCost + stVFCost.totalPieceCost + stVFCost.toolingCost) > 0.000000001;
}

bool isSAExisted(tag_t st)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	char *saNumber = NULL;
	ERROR_CHECK(AOM_ask_value_string(st, "vf4_sched_agreement", &saNumber));
	TC_write_syslog("\n [VF] saNumber=%s\n", saNumber);
	bool isSAExisted = (tc_strlen(saNumber) >2);
	if (saNumber) MEM_free(saNumber);

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return isSAExisted;
}

ReportDataSourceImpl::VFCost getSACost(tag_t st)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	double tooling, ednd, piece;
	char* toolingCurr = NULL, *edndCurr = NULL, *pieceCurr = NULL;

	ERROR_CHECK(AOM_ask_value_string(st, "vf4_sap_gross_price_curr", &pieceCurr));
	ERROR_CHECK(AOM_ask_value_string(st, "vf4_sap_edd_cost_curr", &edndCurr));
	ERROR_CHECK(AOM_ask_value_string(st, "vf4_sap_tooling_curr", &toolingCurr));

	ERROR_CHECK(AOM_ask_value_double(st, "vf4_sap_tooling_cost", &tooling));
	ERROR_CHECK(AOM_ask_value_double(st, "vf4_sap_edd_cost", &ednd));
	ERROR_CHECK(AOM_ask_value_double(st, "vf4_sap_gross_price", &piece));

	if (tc_strcasecmp(pieceCurr, "USD") != 0)
	{
		double rate = getRate(pieceCurr, "USD");
		TC_write_syslog("\n [VF] pieceCurrRate=%8.2f", rate);
		TC_write_syslog("\n [VF] piece=%8.2f", piece);
		piece /= rate;
	}

	if (tc_strcasecmp(edndCurr, "USD") != 0)
	{
		double rate = getRate(edndCurr, "USD");
		TC_write_syslog("\n [VF] edndCurrRate=%8.2f", rate);
		TC_write_syslog("\n [VF] ednd=%8.2f", ednd);
		ednd /= rate;
	}

	if (tc_strcasecmp(toolingCurr, "USD") != 0)
	{
		double rate = getRate(toolingCurr, "USD");
		TC_write_syslog("\n [VF] toolingCurrRate=%8.2f", rate);
		TC_write_syslog("\n [VF] tooling=%8.2f", tooling);
		tooling /= rate;
	}

	ReportDataSourceImpl::VFCost saCost;
	saCost.edndCost = ednd;
	saCost.toolingCost = tooling;
	saCost.totalPieceCost = piece;
	saCost.logisticCost = 0;
	saCost.packingCost = 0;
	saCost.pieceCost = piece;

	if (pieceCurr) MEM_free(pieceCurr);
	if (edndCurr) MEM_free(edndCurr);
	if (toolingCurr) MEM_free(toolingCurr);

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return saCost;
}

tag_t getLatestApprovedEcrInProposal(string partNumber)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	tag_t ecr = NULLTAG;

	tag_t query = NULLTAG;
	ERROR_CHECK(QRY_find2("_LatestApprovedECR1", &query));
	//vf4_platform_module
	//vf4_bom_vfPartNumber
	char *entries[1] = { "ID",};
	char **values = (char**) MEM_alloc(sizeof(char*)*1);
	values[0] = (char*) MEM_alloc(sizeof(char)*(tc_strlen(partNumber.c_str()) + 1));

	partNumber = trim(partNumber);
	tc_strcpy(values[0], partNumber.c_str());

	int foundsNum = -1;
	tag_t *founds = NULL;
	ERROR_CHECK(QRY_execute(query, 1, entries, values, &foundsNum, &founds));
	if (foundsNum > 0 && founds)
	{
		ecr = founds[0];
	}

	TC_write_syslog("\n [VF] foundsNum=%d",foundsNum);
	TC_write_syslog("\n [VF] partNumber=%s",values[0]);

	if (values && values[0]) MEM_free(values[0]);
	if (values) MEM_free(values);
	if(founds) MEM_free(founds);

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return ecr;
}

tag_t getLatestApprovedEcrInImpacted(string partNumber)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	tag_t ecr = NULLTAG;

	tag_t query = NULLTAG;
	ERROR_CHECK(QRY_find2("_LatestApprovedECR2", &query));
	//vf4_platform_module
	//vf4_bom_vfPartNumber
	char *entries[1] = { "ID",};
	char **values = (char**) MEM_alloc(sizeof(char*)*1);
	values[0] = (char*) MEM_alloc(sizeof(char)*(tc_strlen(partNumber.c_str()) + 1));

	partNumber = trim(partNumber);
	tc_strcpy(values[0], partNumber.c_str());

	int foundsNum = -1;
	tag_t *founds = NULL;
	ERROR_CHECK(QRY_execute(query, 1, entries, values, &foundsNum, &founds));
	if (foundsNum > 0 && founds)
	{
		ecr = founds[0];
	}

	TC_write_syslog("\n [VF] foundsNum=%d",foundsNum);
	TC_write_syslog("\n [VF] partNumber=%s",values[0]);

	if (values && values[0]) MEM_free(values[0]);
	if (values) MEM_free(values);
	if(founds) MEM_free(founds);

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return ecr;
}

tag_t getLatestApprovedEcrOf(string partNumber)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	tag_t ecr = NULLTAG;

	tag_t ecr1 = getLatestApprovedEcrInProposal(partNumber);
	tag_t ecr2 = getLatestApprovedEcrInImpacted(partNumber);

	if (ecr1 != NULLTAG && ecr2 == NULLTAG)
		ecr = ecr1;
	else if (ecr2 != NULLTAG && ecr1 == NULLTAG)
		ecr = ecr2;
	else if (ecr1 != NULLTAG && ecr2 != NULLTAG)
	{
		date_t ecr1Date, ecr2Date;
		int answer = -99;
		ERROR_CHECK(AOM_ask_value_date(ecr1, ATTR_ECR_RELEASE_DATE, &ecr1Date));
		ERROR_CHECK(AOM_ask_value_date(ecr2, ATTR_ECR_RELEASE_DATE, &ecr2Date));
		ERROR_CHECK(POM_compare_dates(ecr1Date, ecr2Date, &answer));

		ecr = (answer >= 0) ? ecr1 : ecr2;
	}

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return ecr;
}

ReportDataSourceImpl::VFCost getVFCostFromCostObject(string partNumber)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	ReportDataSourceImpl::VFCost returnCost;
	tag_t costObj = NULLTAG;

	tag_t query = NULLTAG;
	ERROR_CHECK(QRY_find2("VF Cost", &query));
	//vf4_platform_module
	//vf4_bom_vfPartNumber
	char *entries[1] = { "ID"};
	char **values = (char**) MEM_alloc(sizeof(char*)*1);
	values[0] = (char*) MEM_alloc(sizeof(char)*(tc_strlen(partNumber.c_str()) + 1));

	partNumber = trim(partNumber);
	tc_strcpy(values[0], partNumber.c_str());

	int foundsNum = -1;
	tag_t *founds = NULL;
	ERROR_CHECK(QRY_execute(query, 1, entries, values, &foundsNum, &founds));
	if (foundsNum > 0 && founds)
	{
		costObj = founds[0];
		tag_t sourcingRel = NULLTAG;
		TCTYPE_find_type("VF4_SourcingCostFormRela", "VF4_SourcingCostFormRela", &sourcingRel);

		int vfCostFormsCount = -1;
		tag_t *vfCostForms = NULL;
		ERROR_CHECK(GRM_list_secondary_objects_only(costObj, sourcingRel, &vfCostFormsCount, &vfCostForms));

		bool shouldGetCostFromPieceCostForm = false;
		ReportDataSourceImpl::VFCost vfPieceCost;
		ReportDataSourceImpl::VFCost vfTargetCost;
		for (int i = 0; i < vfCostFormsCount; i++)
		{
			char *formType = NULL;
			double pieceCost = 0, toolingCost = 0, edndCost = 0, packingCost = 0, logisticCost = 0;
			tag_t vfCostForm = vfCostForms[i];
			ERROR_CHECK(AOM_ask_value_string(vfCostForm, "object_type", &formType));

			if (tc_strcmp("VF4_PieceCostForm", formType) == 0)
			{
				char *pieceCostCurr = NULL, *toolingCostCurr = NULL, *edndCostCurr = NULL;
				char *qualityOfFinance = NULL;

				ERROR_CHECK(AOM_ask_value_string(vfCostForm, "vf4_piece_cost_curr", &pieceCostCurr));
				ERROR_CHECK(AOM_ask_value_string(vfCostForm, "vf4Ednd_curr", &edndCostCurr));
				ERROR_CHECK(AOM_ask_value_string(vfCostForm, "vf4_tooling_invtest_curr", &toolingCostCurr));

				ERROR_CHECK(AOM_ask_value_double(vfCostForm, "vf4_piece_cost_value_status", &pieceCost));
				ERROR_CHECK(AOM_ask_value_double(vfCostForm, "vf4_tooling_invest_value", &toolingCost));
				ERROR_CHECK(AOM_ask_value_double(vfCostForm, "vf4EdndCost", &edndCost));
				ERROR_CHECK(AOM_ask_value_double(vfCostForm, "vf4_supplier_logisis_cost", &logisticCost));
				ERROR_CHECK(AOM_ask_value_double(vfCostForm, "vf4_supplier_package_amount", &packingCost));

				ERROR_CHECK(AOM_ask_value_string(vfCostForm, "vf4_quality_of_finance", &qualityOfFinance));

				shouldGetCostFromPieceCostForm = (tc_strcmp("Cost Engineering Estimate", qualityOfFinance) == 0 && (pieceCost + logisticCost + packingCost + toolingCost + edndCost) > 0.000000001);

				vfPieceCost.edndCost = edndCost / getRate(edndCostCurr, "USD");
				vfPieceCost.pieceCost = pieceCost / getRate(pieceCostCurr, "USD");
				vfPieceCost.packingCost = packingCost / getRate(pieceCostCurr, "USD");
				vfPieceCost.logisticCost = logisticCost / getRate(pieceCostCurr, "USD");
				vfPieceCost.toolingCost = toolingCost / getRate(toolingCostCurr, "USD");
				vfPieceCost.totalPieceCost =  vfPieceCost.pieceCost + vfPieceCost.packingCost + vfPieceCost.logisticCost;

				if (pieceCostCurr) MEM_free(pieceCostCurr);
				if (edndCostCurr) MEM_free(edndCostCurr);
				if (toolingCostCurr) MEM_free(toolingCostCurr);
				if (qualityOfFinance) MEM_free(qualityOfFinance);

				if (shouldGetCostFromPieceCostForm) break;
			}

			if (tc_strcmp("VF4_TargetCostForm", formType) == 0)
			{
				pieceCost = 0; toolingCost = 0; edndCost = 0;

				ERROR_CHECK(AOM_ask_value_double(vfCostForm, "vf4_piece_cost_value_target", &pieceCost));
				ERROR_CHECK(AOM_ask_value_double(vfCostForm, "vf4_tooling_invest_target", &toolingCost));
				ERROR_CHECK(AOM_ask_value_double(vfCostForm, "vf4_ednd_cost_value_target", &edndCost));
				// TODO: consider case target cost is in parent part

				vfTargetCost.edndCost = edndCost;
				vfTargetCost.toolingCost = toolingCost;
				vfTargetCost.pieceCost = pieceCost;
				vfTargetCost.totalPieceCost = pieceCost;
			}

			if (formType) MEM_free(formType);
		}

		returnCost = (shouldGetCostFromPieceCostForm) ? vfPieceCost : vfTargetCost;
		TC_write_syslog("\n[VF] returnCost cost object of %s (%s)", partNumber.c_str(), shouldGetCostFromPieceCostForm ? "piece" : "target");
		printCost("returnCost", returnCost);

		if (vfCostForms) MEM_free(vfCostForms);
	}

	if(values && values[0]) MEM_free(values[0]);
	if(values) MEM_free(values);
	if(founds) MEM_free(founds);

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return returnCost;
}

ReportDataSourceImpl::VFCost plusVFCost(ReportDataSourceImpl::VFCost cost1, ReportDataSourceImpl::VFCost cost2)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	ReportDataSourceImpl::VFCost cost;

	cost.edndCost = cost1.edndCost + cost2.edndCost;
	cost.logisticCost = cost1.logisticCost + cost2.logisticCost;
	cost.packingCost = cost1.packingCost + cost2.packingCost;
	cost.pieceCost = cost1.pieceCost + cost2.pieceCost;
	cost.toolingCost = cost1.toolingCost + cost2.toolingCost;
	cost.totalPieceCost = cost1.totalPieceCost + cost2.totalPieceCost;

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return cost;
}

ReportDataSource::VFCostElement::VFCostElement()
{
	cost = VF_DOUBLE_NULL;
	approvalTimeStamp = VF_DATE_LONG_NULL;
	approvalDate.init(1970, 01, 01, 0, 0, 0);
}

ReportDataSource::VFCost::VFCost()
{
	toolingCost=edndCost=logisticCost=packingCost=pieceCost=totalPieceCost=0;
	isPieceNoCost = isToolingNoCost = isEDnDNoCost= false;
	approvalDate.init(1970, 01, 01, 0, 0, 0);
}

ReportDataSource::VFPartCost::VFPartCost()
{
	partChangeType = ReportDataSource::PartChangeType::new_part;
}

ReportDataSourceImpl::VFCost getCurrentCost_avoidRecursive(string partNumber, string stProgram)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	tag_t st = getSTOf(partNumber, stProgram);

	char *stUid = NULL;
	ITK__convert_tag_to_uid(st, &stUid);
	TC_write_syslog("\n [VF]st=%d\nstUid=%s",st,stUid);
	if (stUid) MEM_free(stUid);

	ReportDataSourceImpl::VFCost returnCost;
	date_t ST_CUT_OFF_DATE;

	ST_CUT_OFF_DATE.day =12;
	ST_CUT_OFF_DATE.month = 9;
	ST_CUT_OFF_DATE.year = 2020;
	ST_CUT_OFF_DATE.minute = ST_CUT_OFF_DATE.hour = ST_CUT_OFF_DATE.second = 0;

	bool shouldConsiderST = checkShouldConsiderST(st);
	if (shouldConsiderST)
	{

		if (isSAExisted(st))
		{
			returnCost = getSACost(st);
		}
		else
		{
			tag_t latestApprovedEcr = getLatestApprovedEcrOf(partNumber);
			if (latestApprovedEcr == NULLTAG)
			{
				returnCost = getVFCostFromST(st);
			}
			else
			{
				int valsNum;
				char **vals;
				ERROR_CHECK(AOM_ask_displayable_values(st, ATTR_ST_APPROVED_DATE, &valsNum, &vals));
				date_t stApprovedDate;
				if (valsNum <= 0 || tc_strlen(vals[0]) == 0)
				{
					stApprovedDate = ST_CUT_OFF_DATE;
				}
				else
				{
					ERROR_CHECK(AOM_ask_value_date(st, ATTR_ST_APPROVED_DATE, &stApprovedDate));
				}

				date_t latestEcrApprovedDate;
				ERROR_CHECK(AOM_ask_value_date(latestApprovedEcr, ATTR_ECR_RELEASE_DATE, &latestEcrApprovedDate));

				int answer = -99;
				POM_compare_dates(stApprovedDate, latestEcrApprovedDate, &answer);

				if (answer >= 0)
				{
					returnCost = getVFCostFromST(st);
				}
				else
				{
					returnCost = getVFCostFromEcr(latestApprovedEcr, partNumber);
				}

				if (vals) MEM_free(vals);
			}
		}
	}
	else
	{
		tag_t latestApprovedEcr = getLatestApprovedEcrOf(partNumber);
		if (latestApprovedEcr != NULLTAG)
		{
			returnCost = getVFCostFromEcr(latestApprovedEcr, partNumber);
		}
		else
		{
			// TODO: distinguish case split & case one-one
			vector<string> oldPartNumbers = getOldPartNumbersOf(partNumber);
			if (oldPartNumbers.empty())
			{
				returnCost = getVFCostFromCostObject(partNumber);
			}
			else
			{
				// stop recursion
			}
		}
	}

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return returnCost;
}


ReportDataSourceImpl::VFCost getCurrentCost(string partNumber, string stProgram)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	tag_t st = getSTOf(partNumber, stProgram);

	char *stUid = NULL;
	ITK__convert_tag_to_uid(st, &stUid);
	TC_write_syslog("\n [VF]st=%d\nstUid=%s",st,stUid);
	if (stUid) MEM_free(stUid);

	ReportDataSourceImpl::VFCost returnCost;
	date_t ST_CUT_OFF_DATE;

	ST_CUT_OFF_DATE.day =12;
	ST_CUT_OFF_DATE.month = 10;
	ST_CUT_OFF_DATE.year = 2020;
	ST_CUT_OFF_DATE.minute = ST_CUT_OFF_DATE.hour = ST_CUT_OFF_DATE.second = 0;

	bool shouldConsiderST = checkShouldConsiderST(st);
	if (shouldConsiderST)
	{

		if (isSAExisted(st))
		{
			returnCost = getSACost(st);
		}
		else
		{
			tag_t latestApprovedEcr = getLatestApprovedEcrOf(partNumber);
			if (latestApprovedEcr == NULLTAG)
			{
				returnCost = getVFCostFromST(st);
			}
			else
			{
				int valsNum;
				char **vals;
				ERROR_CHECK(AOM_ask_displayable_values(st, ATTR_ST_APPROVED_DATE, &valsNum, &vals));
				date_t stApprovedDate;
				if (valsNum <= 0 || tc_strlen(vals[0]) == 0)
				{
					stApprovedDate = ST_CUT_OFF_DATE;
				}
				else
				{
					ERROR_CHECK(AOM_ask_value_date(st, ATTR_ST_APPROVED_DATE, &stApprovedDate));
				}

				date_t latestEcrApprovedDate;
				ERROR_CHECK(AOM_ask_value_date(latestApprovedEcr, ATTR_ECR_RELEASE_DATE, &latestEcrApprovedDate));
				TC_write_syslog("\n [VF]stApprovedDate=%d/%d/%d %d:%d", stApprovedDate.year, stApprovedDate.month, stApprovedDate.day, stApprovedDate.hour, stApprovedDate.minute);
				TC_write_syslog("\n [VF]latestEcrApprovedDate=%d/%d/%d %d:%d", latestEcrApprovedDate.year, latestEcrApprovedDate.month, latestEcrApprovedDate.day, latestEcrApprovedDate.hour, latestEcrApprovedDate.minute);

				int answer = -99;
				POM_compare_dates(stApprovedDate, latestEcrApprovedDate, &answer);
				TC_write_syslog("\n [VF]compare stApprovedDate and latestEcrApprovedDate =%d",answer);
				if (answer >= 0)
				{
					returnCost = getVFCostFromST(st);
				}
				else
				{
					returnCost = getVFCostFromEcr(latestApprovedEcr, partNumber);
				}

				if (vals) MEM_free(vals);
			}
		}
	}
	else
	{
		tag_t latestApprovedEcr = getLatestApprovedEcrOf(partNumber);
		if (latestApprovedEcr != NULLTAG)
		{
			returnCost = getVFCostFromEcr(latestApprovedEcr, partNumber);
		}
		else
		{
			// TODO: distinguish case split & case one-one
			vector<string> oldPartNumbers = getOldPartNumbersOf(partNumber);
			if (oldPartNumbers.empty())
			{
				returnCost = getVFCostFromCostObject(partNumber);
			}
			else
			{
				ReportDataSourceImpl::VFCost totalCost;
				for (auto oldPartNumber : oldPartNumbers)
				{
					ReportDataSourceImpl::VFCost oldCost = getCurrentCost_avoidRecursive(oldPartNumber, stProgram);
					totalCost = plusVFCost(totalCost, oldCost);
				}
				returnCost = totalCost;
			}
		}
	}

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return returnCost;
}


void printCost(char *prefix, ReportDataSourceImpl::VFCost cost)
{
	TC_write_syslog("\n\n[VF] ~print_cost:");
	TC_write_syslog("\n %s",prefix);
	TC_write_syslog("\n edndCost=%8.2f", cost.edndCost);
	TC_write_syslog("\n logisticCost=%8.2f", cost.logisticCost);
	TC_write_syslog("\n packingCost=%8.2f", cost.packingCost);
	TC_write_syslog("\n pieceCost=%8.2f", cost.pieceCost);
	TC_write_syslog("\n toolingCost=%8.2f", cost.toolingCost);
	TC_write_syslog("\n totalPieceCost=%8.2f", cost.totalPieceCost);
	TC_write_syslog("\n~[VF] end_print_cost");
}

void report_error( char *file, int line, char *function, int return_code)
{
	if (return_code != ITK_ok)
	{
		char *error_message_string;
		EMH_ask_error_text (return_code, &error_message_string);
		TC_write_syslog("ERROR: %d ERROR MSG: %s.\n", return_code, error_message_string);
		TC_write_syslog ("FUNCTION: %s\nFILE: %s LINE: %d\n", function, file, line);
		if(error_message_string) MEM_free(error_message_string);
		TC_write_syslog("\nExiting program!\n");
		exit(EXIT_FAILURE);
	}
}

void putAndSort(vector<ReportDataSourceImpl::VFCost> &sortedCosts, const ReportDataSourceImpl::VFCost &cost)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	vector<ReportDataSourceImpl::VFCost>::iterator it = sortedCosts.begin();
	for (;it != sortedCosts.end(); it++)
	{
		if (cost.approvalTimeStamp >= it->approvalTimeStamp)
		{
			sortedCosts.insert(it, cost);
			break;
		}
	}
	if (it == sortedCosts.end())
	{
		sortedCosts.push_back(cost);
		sortedCosts[sortedCosts.size() - 1].costType = cost.costType;
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

Teamcenter::DateTime convertVFDateLongToDate(long long vfLongDate)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	// int year, int month, int day, int hour = 0, int minutes = 0, int seconds = 0
	long long tmp = vfLongDate;
	int year = (int) (tmp / 10000000000);

	tmp %=  10000000000;
	int month = (int) (tmp / 100000000);

	tmp %=  100000000;
	int day = (int) (tmp / 1000000);

	tmp %=  1000000;
	int hour = (int) (tmp / 10000);

	tmp %=  10000;
	int minutes = (int) (tmp / 100);

	tmp %=  100;
	int seconds = (int) tmp;

	Teamcenter::DateTime tcDate;
	if (vfLongDate > 19700001010101)
	{
		TC_write_syslog("\n[VF] Date: %d/%d/%d %d:%d:%d", year, month, day, hour, minutes, seconds);
		//Teamcenter::DateTime tmp(year, month, day, hour, minutes, seconds);

		bool isValidDate = true;
		if (month < 1 || month > 12) isValidDate = false;
		if (day < 1 || day > 31) isValidDate = false;
		if (hour < 0 || hour > 23) hour = 0;
		if (minutes < 0 || minutes > 59) minutes = 0;
		if (seconds < 0 || seconds > 59) seconds = 0;

		if (isValidDate)
		{
			tcDate.init(year, month, day, hour, minutes, seconds);
		}
		else
		{
			tcDate.init(1970, 01, 01, 0, 0, 0);
		}
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return tcDate;
}


tag_t getPart(string partNumber, string partType, vector<string> &errorMsgs)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	map<string, string> keyAndVal;
	keyAndVal["Item ID"] = partNumber;
	keyAndVal["Type"] = partType;
	vector<tag_t> founds;
	tag_t found = NULLTAG;
	queryObj("Item...", keyAndVal, founds, errorMsgs);
	if (founds.size() > 0) found = founds[0];

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return found;
}

long long convertDatetToLong(date_t datet)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	long long longDate = VF_DATE_LONG_NULL;

	if (datet.day > 0)
	{
		//2021 12 30 08 15 10
		//2021/ 1/24  0: 0: 0
		longDate = 0;
		longDate += (long long) datet.year * 10000000000 + (long long)(datet.month + 1) * 100000000 + (long long)datet.day * 1000000 + (long long)datet.hour * 10000 + (long long)datet.minute * 100 + (long long)datet.second;
	}
	else
	{
		longDate = 19700001000000;

	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] original date : %4d/%2d/%2d %2d:%2d:%2d", datet.year, datet.month, datet.day, datet.hour, datet.minute, datet.second);
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] converted date: %lld", longDate);
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return longDate;
}

long long getDateProperty (tag_t obj, const char *datePropName)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	/*tag_t attrId = NULLTAG;
	char *objType = NULL;
	logical isNull = false, isEmpty = false;

	ERROR_CHECK(WSOM_ask_object_type2(obj, &objType));
	string realFormClass(objType);
	realFormClass.append("Storage");
	ERROR_CHECK(POM_attr_id_of_attr(datePropName, realFormClass.c_str(), &attrId));
	date_t val;
	ERROR_CHECK(POM_ask_attr_date(obj, attrId, &val, &isNull, &isEmpty));*/

	date_t val;
	ERROR_CHECK(AOM_ask_value_date(obj, datePropName, &val));
	long long date = convertDatetToLong(val);

	//SAFE_SM_FREE(objType);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return date;
}

string getStringProperty(tag_t obj, const char *stringPropName)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s - %s", __FUNCTION__, stringPropName);

	char *val = NULL;
	ERROR_CHECK(AOM_ask_value_string(obj, stringPropName, &val));
	string result(val);
	SAFE_SM_FREE(val);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return result;
}

string getDisplayStringProperty(tag_t obj, const char *stringPropName)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s - %s", __FUNCTION__, stringPropName);

	char *val = NULL;
	ERROR_CHECK(AOM_UIF_ask_value(obj, stringPropName, &val));
	string result(val);
	SAFE_SM_FREE(val);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return result;
}

bool getLogicalProperty(tag_t obj, const char *stringPropName)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	bool val = false;
	ERROR_CHECK(AOM_ask_value_logical(obj, stringPropName, &val));

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return val;
}

double getDoubleProperty(tag_t obj, const char *doublePropName)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	double val = VF_DOUBLE_NULL;
	tag_t attrId = NULLTAG, formData = NULLTAG, objTypeTag = NULLTAG;
	char *objType = NULL;
	logical isNull = true, isEmpty = true, isForm = false;

	ERROR_CHECK(AOM_load(obj));
	ERROR_CHECK(TCTYPE_ask_object_type(obj, &objTypeTag));
	ERROR_CHECK(TCTYPE_is_type_of_as_str(objTypeTag, "Form", &isForm));
	ERROR_CHECK(WSOM_ask_object_type2(obj, &objType));

	if (isForm)
	{
		string realFormClass(objType);
		realFormClass.append("Storage");
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF] obj=%d", obj);
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF] realFormClass=%s", realFormClass.c_str());
		ERROR_CHECK(AOM_ask_value_tag(obj, "data_file", &formData));
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF] formData=%d", formData);
		if (formData)
		{
			ERROR_CHECK(POM_attr_id_of_attr(doublePropName, realFormClass.c_str(), &attrId));
			ERROR_CHECK(AOM_load(formData));
			ERROR_CHECK(POM_ask_attr_double(formData, attrId, &val, &isNull, &isEmpty));
		}
	}
	else
	{
		ERROR_CHECK(POM_attr_id_of_attr(doublePropName, objType, &attrId));
		ERROR_CHECK(POM_ask_attr_double(obj, attrId, &val, &isNull, &isEmpty));
	}
	double result = (isNull || isEmpty) ? VF_DOUBLE_NULL : val;
	SAFE_SM_FREE(objType);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return result;
}


ReportDataSourceImpl::VFPartCost getFinalCostMadePart(vector<ReportDataSourceImpl::VFCost> sortedCosts, bool isAseembly, bool isIncludePieceCostOnMadeAssembly)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	ReportDataSourceImpl::VFPartCost finalCost;
	setAllCostNull((finalCost.partCost));
	//TC_write_syslog("\n[VF] %8.8f", finalCost.partCost.pieceCost);

	finalCost.pieceCost.cost = VF_DOUBLE_NULL;
	finalCost.packingCost.cost = VF_DOUBLE_NULL;
	finalCost.logisticCost.cost = VF_DOUBLE_NULL;
	finalCost.labourCost.cost = VF_DOUBLE_NULL;
	finalCost.tax.cost = VF_DOUBLE_NULL;
	finalCost.edndCost.cost = VF_DOUBLE_NULL;
	finalCost.toolingCost.cost = VF_DOUBLE_NULL;
	finalCost.partCost.totalPieceCostStatus = "";

	ReportDataSourceImpl::VFCostElement piece;
	ReportDataSourceImpl::VFCostElement ednd;
	ReportDataSourceImpl::VFCostElement tooling;
	ReportDataSourceImpl::VFCostElement tax;
	ReportDataSourceImpl::VFCostElement logistic;
	ReportDataSourceImpl::VFCostElement labour;
	ReportDataSourceImpl::VFCostElement packing;

	TC_write_syslog("\n[VF] Descending Cost Array (%d):", sortedCosts.size());
	for (int i = 0; i < sortedCosts.size(); i++)
	{
		ReportDataSourceImpl::VFCost sortedCost = sortedCosts[i];
		TC_write_syslog("\n-----", sortedCost.costType.c_str());
		string indexStr = std::to_string(i);
		string printStr("Cost Array Index ");printStr.append(indexStr).append(":");
		printCost((char*)printStr.c_str(), sortedCost);

		if ((piece.cost == VF_DOUBLE_NULL && !tc_strstr(finalCost.partCost.totalPieceCostStatus.c_str(), "NO COST")) && (sortedCost.pieceCost != VF_DOUBLE_NULL || sortedCost.isPieceNoCost) && (!isAseembly || isIncludePieceCostOnMadeAssembly))
		{
			piece.cost = sortedCost.pieceCost;
			piece.approvalTimeStamp = sortedCost.approvalTimeStamp;
			piece.costType = sortedCost.costType;
			piece.approvalDate = sortedCost.approvalDate;
			finalCost.pieceCost = piece;
			finalCost.partCost.pieceCost = piece.cost;
			finalCost.partCost.costType = sortedCost.costType;
			//finalCost.partCost.totalPieceCostStatus = sortedCost.costType;
			if (sortedCost.isPieceNoCost)
			{
				finalCost.partCost.totalPieceCostStatus.append(" - NO COST");
			}

			if (piece.approvalTimeStamp > VF_DATE_LONG_NULL)
			{
				Teamcenter::DateTime tmpDate = convertVFDateLongToDate(sortedCost.approvalTimeStamp);
				finalCost.partCost.approvalDate = tmpDate;
			}
		}

		if (ednd.cost == VF_DOUBLE_NULL && (sortedCost.edndCost != VF_DOUBLE_NULL || sortedCost.isEDnDNoCost) && (!isAseembly || isIncludePieceCostOnMadeAssembly))
		{
			ednd.cost = sortedCost.edndCost;
			ednd.approvalTimeStamp = sortedCost.approvalTimeStamp;
			ednd.approvalDate = sortedCost.approvalDate;
			ednd.costType = sortedCost.costType;
			finalCost.edndCost = ednd;
			finalCost.partCost.edndCost = ednd.cost;
		}

		if (tooling.cost == VF_DOUBLE_NULL && (sortedCost.toolingCost != VF_DOUBLE_NULL || sortedCost.isToolingNoCost) && (!isAseembly || isIncludePieceCostOnMadeAssembly))
		{
			tooling.cost = sortedCost.toolingCost;
			tooling.approvalTimeStamp = sortedCost.approvalTimeStamp;
			tooling.approvalDate = sortedCost.approvalDate;
			tooling.costType = sortedCost.costType;
			finalCost.toolingCost = tooling;
			finalCost.partCost.toolingCost = tooling.cost;
		}

		if ((tax.cost == VF_DOUBLE_NULL && !tc_strstr(finalCost.partCost.totalPieceCostStatus.c_str(), "NO COST")) && (sortedCost.tax != VF_DOUBLE_NULL || sortedCost.isPieceNoCost))
		{
			tax.cost = sortedCost.tax;
			tax.approvalTimeStamp = sortedCost.approvalTimeStamp;
			tax.approvalDate = sortedCost.approvalDate;
			tax.costType = sortedCost.costType;
			finalCost.tax = tax;
			finalCost.partCost.tax = tax.cost;

			if (finalCost.partCost.costType.empty())
			{
				finalCost.partCost.costType = sortedCost.costType;
			}

			if (sortedCost.isPieceNoCost)
			{
				finalCost.partCost.costType.append(" - NO COST");
			}
		}

		if ((packing.cost == VF_DOUBLE_NULL && !tc_strstr(finalCost.partCost.totalPieceCostStatus.c_str(), "NO COST")) && (sortedCost.packingCost != VF_DOUBLE_NULL || sortedCost.isPieceNoCost) && (!isAseembly || isIncludePieceCostOnMadeAssembly))
		{
			packing.cost = sortedCost.packingCost;
			packing.approvalTimeStamp = sortedCost.approvalTimeStamp;
			packing.approvalDate = sortedCost.approvalDate;
			packing.costType = sortedCost.costType;
			finalCost.packingCost = packing;
			finalCost.partCost.packingCost = packing.cost;

			if (finalCost.partCost.costType.empty())
			{
				finalCost.partCost.costType = sortedCost.costType;
			}

			if (sortedCost.isPieceNoCost)
			{
				finalCost.partCost.costType.append(" - NO COST");
			}
		}

		if ((logistic.cost == VF_DOUBLE_NULL && !tc_strstr(finalCost.partCost.totalPieceCostStatus.c_str(), "NO COST")) && (sortedCost.logisticCost != VF_DOUBLE_NULL || sortedCost.isPieceNoCost) && (!isAseembly || isIncludePieceCostOnMadeAssembly))
		{
			//TC_write_syslog("\n[VF] found logistic");
			logistic.cost = sortedCost.logisticCost;
			logistic.approvalTimeStamp = sortedCost.approvalTimeStamp;
			logistic.approvalDate = sortedCost.approvalDate;
			logistic.costType = sortedCost.costType;
			finalCost.logisticCost = logistic;
			finalCost.partCost.logisticCost = logistic.cost;

			if (finalCost.partCost.costType.empty())
			{
				finalCost.partCost.costType = sortedCost.costType;
			}

			if (sortedCost.isPieceNoCost)
			{
				finalCost.partCost.costType.append(" - NO COST");
			}
		}

		if ((labour.cost == VF_DOUBLE_NULL && !tc_strstr(finalCost.partCost.totalPieceCostStatus.c_str(), "NO COST")) && (sortedCost.labourCost != VF_DOUBLE_NULL || sortedCost.isPieceNoCost))
		{
			//TC_write_syslog("\n[VF] found labour");
			labour.cost = sortedCost.labourCost;
			labour.approvalTimeStamp = sortedCost.approvalTimeStamp;
			labour.approvalDate = sortedCost.approvalDate;
			labour.costType = sortedCost.costType;
			finalCost.labourCost = labour;
			finalCost.partCost.labourCost = labour.cost;

			if (finalCost.partCost.costType.empty())
			{
				finalCost.partCost.costType = sortedCost.costType;
			}

			if (sortedCost.isPieceNoCost)
			{
				finalCost.partCost.costType.append(" - NO COST");
			}
		}
	}

	if (finalCost.partCost.pieceCost != VF_DOUBLE_NULL || finalCost.partCost.packingCost != VF_DOUBLE_NULL || finalCost.partCost.logisticCost != VF_DOUBLE_NULL || finalCost.partCost.labourCost != VF_DOUBLE_NULL || finalCost.partCost.tax != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost = 0.0;

	if (finalCost.partCost.pieceCost != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost += finalCost.partCost.pieceCost;
	if (finalCost.partCost.packingCost != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost += finalCost.partCost.packingCost;
	if (finalCost.partCost.logisticCost != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost += finalCost.partCost.logisticCost;
	if (finalCost.partCost.labourCost != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost += finalCost.partCost.labourCost;
	if (finalCost.partCost.tax != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost += finalCost.partCost.tax;

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return finalCost;
}
ReportDataSourceImpl::VFPartCost getFinalCostBuyPart(const vector<ReportDataSourceImpl::VFCost> &sortedCosts)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	ReportDataSourceImpl::VFPartCost finalCost;
	setAllCostNull((finalCost.partCost));
	//TC_write_syslog("\n[VF] %8.8f", finalCost.partCost.pieceCost);

	finalCost.pieceCost.cost = VF_DOUBLE_NULL;
	finalCost.packingCost.cost = VF_DOUBLE_NULL;
	finalCost.logisticCost.cost = VF_DOUBLE_NULL;
	finalCost.labourCost.cost = VF_DOUBLE_NULL;
	finalCost.tax.cost = VF_DOUBLE_NULL;
	finalCost.edndCost.cost = VF_DOUBLE_NULL;
	finalCost.toolingCost.cost = VF_DOUBLE_NULL;
	finalCost.partCost.totalPieceCostStatus = "";

	ReportDataSourceImpl::VFCostElement piece;
	ReportDataSourceImpl::VFCostElement ednd;
	ReportDataSourceImpl::VFCostElement tooling;
	ReportDataSourceImpl::VFCostElement tax;
	ReportDataSourceImpl::VFCostElement logistic;
	ReportDataSourceImpl::VFCostElement labour;
	ReportDataSourceImpl::VFCostElement packing;

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] Descending Cost Array (%d):", sortedCosts.size());
	bool pieceCostGotFromSAP = false;
	for (int i = 0; i < sortedCosts.size(); i++)
	{
		ReportDataSourceImpl::VFCost sortedCost = sortedCosts[i];
		if (VF_IS_DEBUG) TC_write_syslog("\n----- %s", sortedCost.costType.c_str());
		string indexStr = std::to_string(i);
		string printStr("Cost Array Index ");printStr.append(indexStr).append(":");
		printCost((char*)printStr.c_str(), sortedCost);

		if (piece.cost == VF_DOUBLE_NULL && sortedCost.pieceCost != VF_DOUBLE_NULL)
		{
			piece.cost = sortedCost.pieceCost;
			piece.approvalTimeStamp = sortedCost.approvalTimeStamp;
			piece.costType = sortedCost.costType;
			piece.approvalDate = sortedCost.approvalDate;
			finalCost.pieceCost = piece;
			finalCost.partCost.pieceCost = piece.cost;
			finalCost.partCost.costType = sortedCost.costType;
			finalCost.partCost.totalPieceCostStatus = sortedCost.costType;
			if (piece.approvalTimeStamp > VF_DATE_LONG_NULL)
			{
				//Teamcenter::DateTime tmpDate = convertVFDateLongToDate(sortedCost.approvalTimeStamp);
				finalCost.partCost.approvalDate = sortedCost.approvalDate;
			}
			pieceCostGotFromSAP = (tc_strstr(finalCost.partCost.totalPieceCostStatus.c_str(), VF_COST_TYPE_SAP) != NULL);
		}

		if (ednd.cost == VF_DOUBLE_NULL && sortedCost.edndCost != VF_DOUBLE_NULL)
		{
			ednd.cost = sortedCost.edndCost;
			ednd.approvalTimeStamp = sortedCost.approvalTimeStamp;
			ednd.approvalDate = sortedCost.approvalDate;
			ednd.costType = sortedCost.costType;
			finalCost.edndCost = ednd;
			finalCost.partCost.edndCost = ednd.cost;
		}

		if (tooling.cost == VF_DOUBLE_NULL && sortedCost.toolingCost != VF_DOUBLE_NULL)
		{
			tooling.cost = sortedCost.toolingCost;
			tooling.approvalTimeStamp = sortedCost.approvalTimeStamp;
			tooling.approvalDate = sortedCost.approvalDate;
			tooling.costType = sortedCost.costType;
			finalCost.toolingCost = tooling;
			finalCost.partCost.toolingCost = tooling.cost;
		}

		if (tax.cost == VF_DOUBLE_NULL && sortedCost.tax != VF_DOUBLE_NULL)
		{
			tax.cost = sortedCost.tax;
			tax.approvalTimeStamp = sortedCost.approvalTimeStamp;
			tax.approvalDate = sortedCost.approvalDate;
			tax.costType = sortedCost.costType;
			finalCost.tax = tax;
			finalCost.partCost.tax = tax.cost;

			if (finalCost.partCost.costType.empty())
			{
				finalCost.partCost.costType = sortedCost.costType;
			}
		}

		if (packing.cost == VF_DOUBLE_NULL && sortedCost.packingCost != VF_DOUBLE_NULL && !pieceCostGotFromSAP)
		{
			packing.cost = sortedCost.packingCost;
			packing.approvalTimeStamp = sortedCost.approvalTimeStamp;
			packing.approvalDate = sortedCost.approvalDate;
			packing.costType = sortedCost.costType;
			finalCost.packingCost = packing;
			finalCost.partCost.packingCost = packing.cost;

			if (finalCost.partCost.costType.empty())
			{
				finalCost.partCost.costType = sortedCost.costType;
			}
		}

		if (logistic.cost == VF_DOUBLE_NULL && sortedCost.logisticCost != VF_DOUBLE_NULL && !pieceCostGotFromSAP)
		{
			//TC_write_syslog("\n[VF] found logistic");
			logistic.cost = sortedCost.logisticCost;
			logistic.approvalTimeStamp = sortedCost.approvalTimeStamp;
			logistic.approvalDate = sortedCost.approvalDate;
			logistic.costType = sortedCost.costType;
			finalCost.logisticCost = logistic;
			finalCost.partCost.logisticCost = logistic.cost;

			if (finalCost.partCost.costType.empty())
			{
				finalCost.partCost.costType = sortedCost.costType;
			}
		}

		if (labour.cost == VF_DOUBLE_NULL && sortedCost.labourCost != VF_DOUBLE_NULL)
		{
			//TC_write_syslog("\n[VF] found labour");
			labour.cost = sortedCost.labourCost;
			labour.approvalTimeStamp = sortedCost.approvalTimeStamp;
			labour.approvalDate = sortedCost.approvalDate;
			labour.costType = sortedCost.costType;
			finalCost.labourCost = labour;
			finalCost.partCost.labourCost = labour.cost;

			if (finalCost.partCost.costType.empty())
			{
				finalCost.partCost.costType = sortedCost.costType;
			}
		}
	}

	if (pieceCostGotFromSAP) finalCost.partCost.packingCost = finalCost.partCost.logisticCost = VF_DOUBLE_NULL;// sap piece cost already included packing & logistic

	if (finalCost.partCost.pieceCost != VF_DOUBLE_NULL || finalCost.partCost.packingCost != VF_DOUBLE_NULL || finalCost.partCost.logisticCost != VF_DOUBLE_NULL || finalCost.partCost.labourCost != VF_DOUBLE_NULL || finalCost.partCost.tax != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost = 0.0;

	if (finalCost.partCost.pieceCost != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost += finalCost.partCost.pieceCost;
	if (finalCost.partCost.packingCost != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost += finalCost.partCost.packingCost;
	if (finalCost.partCost.logisticCost != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost += finalCost.partCost.logisticCost;
	//if (finalCost.partCost.labourCost != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost += finalCost.partCost.labourCost;
	if (finalCost.partCost.tax != VF_DOUBLE_NULL) finalCost.partCost.totalPieceCost += finalCost.partCost.tax;

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return finalCost;
}

tag_t getLatestForm(tag_t costRev, const char *rel)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	tag_t relType = NULLTAG;
	ERROR_CHECK(TCTYPE_find_type(rel, rel, &relType));

	int foundsCount = 0;
	tag_t *founds = NULL;

	ERROR_CHECK(GRM_list_secondary_objects_only(costRev, relType, &foundsCount, &founds));

	tag_t latestForm = NULL;
	long long previousReleaseDate = -999;
	for (int i = 0; i < foundsCount; i++)
	{
		string str("costrev-");
		str.append(rel).append("-form");
		printObjString(founds[i], str.c_str());
		long long releaseDate = getDateProperty(founds[i], "date_released");
		if (releaseDate > previousReleaseDate)
		{
			latestForm = founds[i];
		}
	}

	SAFE_SM_FREE(founds);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return latestForm;
}

tag_t getLatestForm(tag_t costRev, const char *rel, const char *filterType)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	tag_t relType = NULLTAG;
	ERROR_CHECK(TCTYPE_find_type(rel, rel, &relType));

	int foundsCount = 0;
	tag_t *founds = NULL;

	ERROR_CHECK(GRM_list_secondary_objects_only(costRev, relType, &foundsCount, &founds));

	tag_t latestForm = NULLTAG;
	long long previousReleaseDate = -999;
	for (int i = 0; i < foundsCount; i++)
	{
		string str("costrev-");
		str.append(rel).append("-form");
		printObjString(founds[i], str.c_str());

		char *type = NULL;
		ERROR_CHECK(WSOM_ask_object_type2(founds[i], &type));
		if (tc_strcmp(type, filterType) == 0)
		{
			long long releaseDate = getDateProperty(founds[i], "date_released");
			if (releaseDate > previousReleaseDate)
			{
				latestForm = founds[i];
			}
		}
		SAFE_SM_FREE(type);
	}

	SAFE_SM_FREE(founds);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return latestForm;
}

tag_t createCostForm(const char *costFormTypeName, const char *formName)
{
	TC_write_syslog("\n[vf] Enter %s\n", __FUNCTION__);
	tag_t form = NULLTAG;
	tag_t formType = NULLTAG;
	int retcode = ITK_ok;

	ERROR_CHECK(TCTYPE_find_type(costFormTypeName, "Form", &formType));

	tag_t createInput = NULLTAG;
	ERROR_CHECK(TCTYPE_construct_create_input(formType, &createInput));
	ERROR_CHECK(TCTYPE_set_create_display_value(createInput, "object_name", 1, &formName));
	ERROR_CHECK(TCTYPE_create_object(createInput, &form));
	ERROR_CHECK(AOM_save_with_extensions(form));
	ERROR_CHECK(AOM_refresh(form, FALSE));
	if (retcode == ITK_ok) TC_write_syslog("\n[vf] Created cost form");

	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);
	return form;
}

void createRelation(const tag_t &secondary, const tag_t &primary, const char* relationTypeName)
{
	TC_write_syslog("\n[vf] Enter %s\n", __FUNCTION__);
	tag_t relationType = NULLTAG;
	tag_t rel = NULLTAG;
	int retcode = ITK_ok;

	ERROR_CHECK(TCTYPE_find_type(relationTypeName, relationTypeName, &relationType));
	ERROR_CHECK(GRM_create_relation(primary, secondary, relationType, NULL, &rel));
	ERROR_CHECK(AOM_save_without_extensions(rel));
	ERROR_CHECK(AOM_refresh(rel, FALSE));
	if (retcode == ITK_ok) TC_write_syslog("\n[vf] Linked form to rev");

	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);
}

vector<tag_t> getRelatedForms(tag_t wso, const char *rel, const char* filterType)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	vector<tag_t> forms;

	tag_t relType = NULLTAG;
	ERROR_CHECK(TCTYPE_find_type(rel, rel, &relType));

	int foundsCount = 0;
	tag_t *founds = NULL;

	ERROR_CHECK(GRM_list_secondary_objects_only(wso, relType, &foundsCount, &founds));

	for (int i = 0; i < foundsCount; i++)
	{
		string str("ecr-cost");
		str.append(rel).append("-form");
		//printObjString(founds[i], str.c_str());

		char *type = NULL;
		ERROR_CHECK(WSOM_ask_object_type2(founds[i], &type));

		if (tc_strcmp(filterType, type) == 0 || tc_strlen(filterType) == 0)
		{
			forms.push_back(founds[i]);
		}
	}

	SAFE_SM_FREE(founds);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return forms;
}

vector<tag_t> getAllForms(tag_t costRev, const char *rel)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	vector<tag_t> forms;

	tag_t relType = NULLTAG;
	ERROR_CHECK(TCTYPE_find_type(rel, rel, &relType));

	int foundsCount = 0;
	tag_t *founds = NULL;

	ERROR_CHECK(GRM_list_secondary_objects_only(costRev, relType, &foundsCount, &founds));

	for (int i = 0; i < foundsCount; i++)
	{
		string str("ecr-cost");
		str.append(rel).append("-form");
		printObjString(founds[i], str.c_str());

		forms.push_back(founds[i]);
	}

	SAFE_SM_FREE(founds);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return forms;
}

map<long long, tag_t> getForms(tag_t costRev, const char *rel, const char *filterType)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	map<long long, tag_t> forms;

	tag_t relType = NULLTAG;
	ERROR_CHECK(TCTYPE_find_type(rel, rel, &relType));

	int foundsCount = 0;
	tag_t *founds = NULL;

	ERROR_CHECK(GRM_list_secondary_objects_only(costRev, relType, &foundsCount, &founds));

	for (int i = 0; i < foundsCount; i++)
	{
		string str("ecr-cost");
		str.append(rel).append("-form");
		printObjString(founds[i], str.c_str());

		char *type = NULL;
		ERROR_CHECK(WSOM_ask_object_type2(founds[i], &type));
		if (tc_strcmp(type, filterType) == 0)
		{
			long long releaseDate = getDateProperty(founds[i], "date_released");
			if (releaseDate > (long long)VF_CUT_OFF_DATE)
			{
				forms[releaseDate] = founds[i];
			}
		}
		SAFE_SM_FREE(type);
	}

	SAFE_SM_FREE(founds);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return forms;
}

void setAllCostNull(ReportDataSourceImpl::VFCost &cost)
{
	cost.edndCost = VF_DOUBLE_NULL;
	cost.labourCost = VF_DOUBLE_NULL;
	cost.logisticCost = VF_DOUBLE_NULL;
	cost.packingCost = VF_DOUBLE_NULL;
	cost.pieceCost = VF_DOUBLE_NULL;
	cost.tax = VF_DOUBLE_NULL;
	cost.toolingCost = VF_DOUBLE_NULL;
	cost.totalPieceCost = VF_DOUBLE_NULL;
	cost.approvalTimeStamp = VF_DATE_LONG_NULL;
	cost.approvalDate.init(1970, 01, 01, 0, 0, 0);
}

bool isMakePart(tag_t part, const std::string &partMakeBuyInput)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	string partMakeBuy = partMakeBuyInput;
	if (partMakeBuy.empty()) partMakeBuy = getStringProperty(part, "vf4_item_make_buy");
	bool isMadePart = (tc_strcasecmp(partMakeBuy.c_str(), "MAKE") == 0 || tc_strcasecmp(partMakeBuy.c_str(), "SELL") == 0);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return isMadePart;
}

bool isBoughtPart(tag_t part, const std::string &partMakeBuyInput)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	string partMakeBuy = partMakeBuyInput;
	if(partMakeBuy.empty()) partMakeBuy = getStringProperty(part, "vf4_item_make_buy");
	bool isBoughtPart = (tc_strcasecmp(partMakeBuy.c_str(), "BUY") == 0);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return isBoughtPart;
}

bool isBuyPart(tag_t part)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	string partMakeBuy = getStringProperty(part, "vf4_item_make_buy");
	bool isBuyPart = tc_strstr(partMakeBuy.c_str(), "Buy") == NULL &&  tc_strstr(partMakeBuy.c_str(), "buy") == NULL &&  tc_strstr(partMakeBuy.c_str(), "purchase") == NULL &&  tc_strstr(partMakeBuy.c_str(), "Purchase") == NULL;

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return isBuyPart;
}

ReportDataSourceImpl::VFCost getSapCostFromVFCost(tag_t part, const std::string &partMakeBuyInput, tag_t costRev, const char *type)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	ReportDataSourceImpl::VFCost sapCost;
	setAllCostNull(sapCost);
	sapCost.costType = VF_COST_TYPE_SAP;

	vector<tag_t> sapCostForms = getAllForms(costRev, "VF4_SAPCostRelation");
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] sapCostForms.len=%d", sapCostForms.size());
	bool isBuyPart = isBoughtPart(part, partMakeBuyInput);
	for (auto sapCostForm : sapCostForms)
	{
		printObjString(sapCostForm, "sapCostForm");
		logical saIsActive = getLogicalProperty(sapCostForm, "vf4_is_SA_active");

		if (isBuyPart && sapCostForm && saIsActive)
		{
			string edndCurrency = getStringProperty(sapCostForm, SAP_EDnD_COST_CURR);
			string pieceCurrency = getStringProperty(sapCostForm, SAP_PIECE_COST_CURR);
			string toolingCurrency = getStringProperty(sapCostForm, SAP_TOOLING_COST_CURR);
			double edndRate = 1.0/getRate(edndCurrency, VF_RETURN_COST_CURRENCY);
			double pieceRate = 1.0/getRate(pieceCurrency, VF_RETURN_COST_CURRENCY);
			double toolingRate = 1.0/getRate(toolingCurrency, VF_RETURN_COST_CURRENCY);
			TC_write_syslog("\n[VF] sapCost.pieceCost=%8.5f", sapCost.pieceCost);
			TC_write_syslog("\n[VF] pieceCurrency=%s", pieceCurrency.c_str());


			if (pieceCurrency.size() > 0 && tc_strstr(type, "piece"))
			{
				sapCost.pieceCost = getDoubleProperty(sapCostForm, SAP_PIECE_COST);
				if (sapCost.pieceCost != VF_DOUBLE_NULL)
				{
					sapCost.pieceCost *= pieceRate;
				}

				sapCost.approvalTimeStamp = getDateProperty(sapCostForm, SAP_APPROVAL_DATE_PIECE);
				if (VF_IS_DEBUG) TC_write_syslog("\n[VF] approval timestamp sap=%lld", sapCost.approvalTimeStamp);
				sapCost.approvalDate = convertVFDateLongToDate(sapCost.approvalTimeStamp);

				// sap cost has highest priority
				sapCost.approvalTimeStamp = VF_DATE_LONG_MAX;
				break;
			}
			else
			{
				sapCost.pieceCost = VF_DOUBLE_NULL;
			}

			if (tc_strstr(type, "ednd") && edndCurrency.size() > 0)
			{
				sapCost.edndCost = getDoubleProperty(sapCostForm, SAP_EDnD_COST);
				if (sapCost.edndCost != VF_DOUBLE_NULL)
				{
					sapCost.edndCost *= edndRate;
				}

				sapCost.approvalTimeStamp = getDateProperty(sapCostForm, SAP_APPROVAL_DATE_EDnD);
				if (VF_IS_DEBUG) TC_write_syslog("\n[VF] approval timestamp sap=%lld", sapCost.approvalTimeStamp);
				sapCost.approvalDate = convertVFDateLongToDate(sapCost.approvalTimeStamp);

				// sap cost has highest priority
				sapCost.approvalTimeStamp = VF_DATE_LONG_MAX;
				break;
			}

			if (tc_strstr(type, "tooling") && toolingCurrency.size() > 0)
			{
				sapCost.toolingCost = getDoubleProperty(sapCostForm, SAP_TOOLING_COST);
				if (sapCost.toolingCost != VF_DOUBLE_NULL)
				{
					sapCost.toolingCost *= toolingRate;
				}

				sapCost.approvalTimeStamp = getDateProperty(sapCostForm, SAP_APPROVAL_DATE_TOOLING);
				if (VF_IS_DEBUG) TC_write_syslog("\n[VF] approval timestamp sap=%lld", sapCost.approvalTimeStamp);
				sapCost.approvalDate = convertVFDateLongToDate(sapCost.approvalTimeStamp);

				// sap cost has highest priority
				sapCost.approvalTimeStamp = VF_DATE_LONG_MAX;
				break;
			}
		}
	}
	//tag_t sapCostForm = getLatestForm(costRev, "VF4_SAPCostRelation");


	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return sapCost;
}

ReportDataSourceImpl::VFCost getSapCostFromST(tag_t part, const std::string &partMakeBuyInput, tag_t st, const char *type)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	ReportDataSourceImpl::VFCost sapCost;
	setAllCostNull(sapCost);
	sapCost.costType = VF_COST_TYPE_ST;
	sapCost.costType.append(" - ").append(VF_COST_TYPE_SAP);

	bool isBuyPart = isBoughtPart(part, partMakeBuyInput);
	if (isBuyPart && st)
	{
		string edndCurrency = getStringProperty(st, ST_SAP_COST_EDnD_CURR);
		string pieceCurrency = getStringProperty(st, ST_SAP_COST_PIECE_CURR);
		string toolingCurrency = getStringProperty(st, ST_SAP_COST_TOOLING_CURR);
		double edndRate = 1.0/getRate(edndCurrency, VF_RETURN_COST_CURRENCY);
		double pieceRate = 1.0/getRate(pieceCurrency, VF_RETURN_COST_CURRENCY);
		double toolingRate = 1.0/getRate(toolingCurrency, VF_RETURN_COST_CURRENCY);

		if (pieceCurrency.size() > 0 && tc_strstr(type, "piece"))
		{
			sapCost.pieceCost = getDoubleProperty(st, ST_SAP_COST_PIECE);// * pieceRate;
			if (sapCost.pieceCost != VF_DOUBLE_NULL)
			{
				sapCost.pieceCost *= pieceRate;
			}

			// sap cost has highest priority
			sapCost.approvalTimeStamp = VF_DATE_LONG_MAX;
		}
		else
		{
			sapCost.pieceCost = VF_DOUBLE_NULL;
		}

		if (tc_strstr(type, "ednd") && edndCurrency.size() > 0)
		{
			sapCost.edndCost = getDoubleProperty(st, ST_SAP_COST_EDnD);
			if (sapCost.edndCost != VF_DOUBLE_NULL)
			{
				sapCost.edndCost *= edndRate;
			}

			// sap cost has highest priority
			sapCost.approvalTimeStamp = VF_DATE_LONG_MAX;
		}

		if (tc_strstr(type, "tooling") && toolingCurrency.size() > 0)
		{
			sapCost.toolingCost = getDoubleProperty(st, ST_SAP_COST_TOOLING);
			if (sapCost.toolingCost != VF_DOUBLE_NULL)
			{
				sapCost.toolingCost *= toolingRate;
			}

			// sap cost has highest priority
			sapCost.approvalTimeStamp = VF_DATE_LONG_MAX;
		}
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return sapCost;
}

ReportDataSourceImpl::VFCost getSapCost(tag_t part, const std::string &partMakeBuyInput, tag_t costRev, const char *type)
{
	ReportDataSourceImpl::VFCost sapCost;
	setAllCostNull(sapCost);

	if (costRev != NULLTAG)
	{
		//logical saIsActive = getLogicalProperty(st, "vf4_is_SA_active");
		//if (saIsActive)
		//{
			sapCost = getSapCostFromVFCost(part, partMakeBuyInput, costRev, type);
//			if (sapCost.edndCost == VF_DOUBLE_NULL && sapCost.toolingCost == VF_DOUBLE_NULL && sapCost.pieceCost == VF_DOUBLE_NULL && st != NULLTAG)
//			{
//				sapCost = getSapCostFromST(part, partMakeBuyInput, st, type);
//			}
		//}
	}

	return sapCost;
}

ReportDataSourceImpl::VFCost getStCost(tag_t part, tag_t costRev, tag_t &sourcePart, const std::string &partMakeBuyInput)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	costRev;

	ReportDataSourceImpl::VFCost stCost;
	setAllCostNull(stCost);
	stCost.costType = VF_COST_TYPE_ST;

	bool isBuyPart = isBoughtPart(part, partMakeBuyInput);
	string partNumber = getStringProperty(part, "item_id");
	string partType = getStringProperty(part, "object_type");
	string donorAttr = tc_strstr(partType.c_str(), "Scooter") ? "vf4_es_model_veh_line" : "vf4_donor_vehicle";

	string donorVehicle = getDisplayStringProperty(part, donorAttr.c_str());// NOT count scooter/vf compo design
	char *donorUpper = NULL;
	tc_strupr(donorVehicle.c_str(), &donorUpper);
	donorVehicle.assign((const char*)donorUpper);
	if (isBuyPart)
	{
		map<string, string> keyAndVal;
		vector<tag_t> founds;
		vector<string> errorMsg;
		keyAndVal["VF Part Number"] = partNumber.c_str();
		//keyAndVal["Purchase Level Vinfast"] = "*";

		queryObj("Source Part", keyAndVal, founds, errorMsg);

		tag_t validSourcePart = NULLTAG;
		tag_t snsST = NULLTAG;
		if (founds.size() == 1)
		{
			validSourcePart = founds[0];
		}
		else if (founds.size() > 1)
		{
			std::vector<tag_t> stContainsDonorCandidates;
			for (tag_t found : founds)
			{
				AOM_refresh(found, FALSE);
				string sourcingProgram = getDisplayStringProperty(found, "vf4_platform_module");
				if (VF_IS_DEBUG) TC_write_syslog("\n[VF] sourcingProgram=%s",  sourcingProgram.c_str());
				if (VF_IS_DEBUG) TC_write_syslog("\n[VF] donorVehicle=%s",  donorVehicle.c_str());

				char *sourcingProgramUpper = NULL;
				tc_strupr(sourcingProgram.c_str(), &sourcingProgramUpper);
				sourcingProgram.assign((const char*)sourcingProgramUpper);

				TC_write_syslog("\n[VF] sourcingProgram=%s",  sourcingProgram.c_str());
				TC_write_syslog("\n[VF] donorVehicle=%s",  donorVehicle.c_str());


				if (tc_strstr(sourcingProgram.c_str(), donorVehicle.c_str()))
				{
					stContainsDonorCandidates.push_back(found);
				}
				else if (tc_strstr(sourcingProgram.c_str(), "S&S"))
				{
					snsST = found;
				}
				else if (tc_strcasecmp(sourcingProgram.c_str(), "EMOTOR") == 0 || tc_strcasecmp(sourcingProgram.c_str(), "3IN1") == 0 || tc_strcasecmp(sourcingProgram.c_str(), "BATTERY") == 0 ) //3IN1  BATTERY
				{
					validSourcePart = found;
					break;
				}
			}

			if (stContainsDonorCandidates.size() == 1)
			{
				validSourcePart = stContainsDonorCandidates[0];
			}
			else
			{
				if (validSourcePart == NULLTAG)
				{
					for (tag_t stContainsDonorCandidate : stContainsDonorCandidates)
					{
						string purchaseLevel = getStringProperty(stContainsDonorCandidate, "vf4_purchasing_level");
						if (tc_strcmp(purchaseLevel.c_str(), "P") == 0)
						{
							validSourcePart = stContainsDonorCandidate;
							break;
						}
					}

					if (validSourcePart == NULLTAG && stContainsDonorCandidates.size() > 0)
					{
						validSourcePart = stContainsDonorCandidates[0];
					}
				}

				if (validSourcePart == NULLTAG)
				{
					validSourcePart = snsST;
				}
			}
		}

		if (validSourcePart == NULLTAG)
		{
			if (VF_IS_DEBUG) TC_write_syslog("\n[VF] WARNING: Cannot find any valid \"%s\"'s source part.",  partNumber.c_str());
		}
		else
		{
			sourcePart = validSourcePart;
			string totalCostStatus = getStringProperty(validSourcePart, ST_COST_STATUS);
			if (!totalCostStatus.empty())
			{
				if (VF_IS_DEBUG) TC_write_syslog("\n[VF] found st for %s", partNumber.c_str());
				stCost.costType.append(" - ").append(totalCostStatus);

				string edndCurrency = getStringProperty(sourcePart, ST_COST_EDnD_CURR);
				string pieceCurrency = getStringProperty(sourcePart, ST_COST_PIECE_CURR);
				string toolingCurrency = getStringProperty(sourcePart, ST_COST_TOOLING_CURR);
				double edndRate = 1.0/getRate(edndCurrency, VF_RETURN_COST_CURRENCY);
				double pieceRate =1.0/ getRate(pieceCurrency, VF_RETURN_COST_CURRENCY);
				double toolingRate = 1.0/getRate(toolingCurrency, VF_RETURN_COST_CURRENCY);

				stCost.pieceCost = getDoubleProperty(validSourcePart, ST_COST_PIECE);
				stCost.toolingCost = getDoubleProperty(validSourcePart, ST_COST_TOOLING);
				stCost.edndCost = getDoubleProperty(validSourcePart, ST_COST_EDnD);
				stCost.logisticCost = getDoubleProperty(validSourcePart, ST_COST_LOGISTIC);
				stCost.packingCost = getDoubleProperty(validSourcePart, ST_COST_PACKING);
				stCost.approvalTimeStamp = getDateProperty(validSourcePart, ST_COST_APPROVAL_DATE);

				if (stCost.pieceCost != VF_DOUBLE_NULL) stCost.pieceCost *= pieceRate;
				if (stCost.logisticCost != VF_DOUBLE_NULL) stCost.logisticCost *= pieceRate;
				if (stCost.packingCost != VF_DOUBLE_NULL) stCost.packingCost *= pieceRate;
				if (stCost.toolingCost != VF_DOUBLE_NULL) stCost.toolingCost *= toolingRate;
				if (stCost.edndCost != VF_DOUBLE_NULL) stCost.edndCost *= edndRate;

				if (stCost.approvalTimeStamp == VF_DATE_LONG_NULL)
				{
					date_t val;
					val.day = 13;
					val.month = 8;
					val.year = 2021;
					val.hour = val.minute = val.second = 0;

					long long cutOffDate = convertDatetToLong(val);
					stCost.approvalTimeStamp = cutOffDate;
				}

				stCost.totalPieceCostStatus = totalCostStatus;
				if (VF_IS_DEBUG) TC_write_syslog("\n[VF] approval timestamp st=%lld", stCost.approvalTimeStamp);
				stCost.approvalDate = convertVFDateLongToDate(stCost.approvalTimeStamp);
			}
		}
	}
	else
	{
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF] WARNING: \"%s\" is not buy part",  partNumber.c_str());
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return stCost;
}

ReportDataSourceImpl::VFCost getTargetCost(tag_t part, tag_t costRev)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	part;

	ReportDataSourceImpl::VFCost targetCost;
	setAllCostNull(targetCost);
	targetCost.costType = VF_COST_TYPE_TARGET;

	tag_t targetCostForm = getLatestForm(costRev, "VF4_SourcingCostFormRela", "VF4_TargetCostForm");
	printObjString(targetCostForm, "targetCostForm");

	if (targetCostForm)
	{
		bool isPieceOnParent = getLogicalProperty(targetCostForm, TARGET_COST_PIECE_ON_PARENT);
		if (!isPieceOnParent)
		{
			targetCost.pieceCost = getDoubleProperty(targetCostForm, TARGET_COST_PIECE);
		}
		else
		{
			targetCost.pieceCost = 0.0;
		}

		bool isEDnDOnParent = getLogicalProperty(targetCostForm, TARGET_COST_EDnD_ON_PARENT);
		if (!isEDnDOnParent)
		{
			targetCost.edndCost = getDoubleProperty(targetCostForm, TARGET_COST_EDnD);
		}
		else
		{
			targetCost.edndCost = 0.0;
		}

		bool isToolingOnParent = getLogicalProperty(targetCostForm, TARGET_COST_TOOLING_ON_PARENT);
		if (!isToolingOnParent)
		{
			targetCost.toolingCost = getDoubleProperty(targetCostForm, TARGET_COST_TOOLING);
		}
		else
		{
			targetCost.toolingCost = 0.0;
		}

		targetCost.approvalTimeStamp = VF_DATE_LONG_NULL - 1;
	}
	else
	{
		printObjString(costRev, "WARNING: Cannot find target cost form of ");
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return targetCost;
}

ReportDataSourceImpl::VFCost getEngCost(tag_t part, tag_t costRev, const std::string &partMakeBuyInput)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	ReportDataSourceImpl::VFCost engCost;
	setAllCostNull(engCost);
	engCost.costType = VF_COST_TYPE_ENG;

	tag_t targetCostForm = getLatestForm(costRev, "VF4_SourcingCostFormRela", "VF4_TargetCostForm");
	printObjString(targetCostForm, "targetCostForm");

	bool isPieceOnParent = false;
	bool isEDnDOnParent = false;
	bool isToolingOnParent = false;
	if (targetCostForm)
	{
//		isPieceOnParent = getLogicalProperty(targetCostForm, TARGET_COST_PIECE_ON_PARENT);
//		isEDnDOnParent = getLogicalProperty(targetCostForm, TARGET_COST_EDnD_ON_PARENT);
//		isToolingOnParent = getLogicalProperty(targetCostForm, TARGET_COST_TOOLING_ON_PARENT);
		isPieceOnParent = isEDnDOnParent = isToolingOnParent = false;// only check is on parent while calculating target cost
	}

	tag_t pieceCostForm = getLatestForm(costRev, "VF4_SourcingCostFormRela", "VF4_PieceCostForm");
	printObjString(pieceCostForm, "pieceCostForm");

	if (pieceCostForm)
	{
		string pieceCostStatus = getStringProperty(pieceCostForm, ENG_COST_STATUS);

		string edndCurrency = getStringProperty(pieceCostForm, ENG_COST_EDnD_CURR);
		string pieceCurrency = getStringProperty(pieceCostForm, ENG_COST_PIECE_CURR);
		string toolingCurrency = getStringProperty(pieceCostForm, ENG_COST_TOOLING_CURR);
		double edndRate = 1.0/getRate(edndCurrency, VF_RETURN_COST_CURRENCY);
		double pieceRate =1.0/ getRate(pieceCurrency, VF_RETURN_COST_CURRENCY);
		double toolingRate = 1.0/getRate(toolingCurrency, VF_RETURN_COST_CURRENCY);


		//if (!isPieceOnParent && !tc_strstr(pieceCurrency.c_str(), "NO COST") && pieceCurrency.size() > 0)
		if (!isPieceOnParent && pieceCurrency.size() > 0)
		{
			if (tc_strstr(pieceCurrency.c_str(), "NO COST"))
			{
				engCost.pieceCost = engCost.labourCost = engCost.tax = engCost.logisticCost = engCost.packingCost = engCost.totalPieceCost = VF_DOUBLE_NULL;
				engCost.isPieceNoCost = true;
			}
			else
			{
				engCost.pieceCost = getDoubleProperty(pieceCostForm, ENG_COST_PIECE);
				if (engCost.pieceCost != VF_DOUBLE_NULL) engCost.pieceCost *= pieceRate;

				engCost.labourCost = getDoubleProperty(pieceCostForm, ENG_COST_LABOUR);
				if (engCost.labourCost != VF_DOUBLE_NULL) engCost.labourCost *= pieceRate;

				engCost.tax = getDoubleProperty(pieceCostForm, ENG_COST_TAX) * pieceRate;
				if (engCost.tax != VF_DOUBLE_NULL) engCost.tax *= pieceRate;

				engCost.logisticCost = getDoubleProperty(pieceCostForm, ENG_COST_LOGISTIC);
				if (engCost.logisticCost != VF_DOUBLE_NULL) engCost.logisticCost *= pieceRate;

				engCost.packingCost = getDoubleProperty(pieceCostForm, ENG_COST_PACKING);
				if (engCost.packingCost != VF_DOUBLE_NULL) engCost.packingCost *= pieceRate;

				engCost.totalPieceCost = getDoubleProperty(pieceCostForm, ENG_COST_TOTAL_PIECE);
				if (engCost.totalPieceCost != VF_DOUBLE_NULL) engCost.totalPieceCost *= pieceRate;
			}
		}

		if (!isToolingOnParent && toolingCurrency.size() > 0)
		{
			if (tc_strstr(toolingCurrency.c_str(), "NO COST"))
			{
				engCost.toolingCost = VF_DOUBLE_NULL;
				engCost.isToolingNoCost = true;
			}
			else
			{
				engCost.toolingCost = getDoubleProperty(pieceCostForm, ENG_COST_TOOLING);
				if (engCost.toolingCost != VF_DOUBLE_NULL) engCost.toolingCost *= toolingRate;
			}
		}

		if (!isEDnDOnParent && edndCurrency.size() > 0)
		{
			if (tc_strstr(edndCurrency.c_str(), "NO COST"))
			{
				engCost.edndCost = VF_DOUBLE_NULL;
				engCost.isEDnDNoCost = true;
			}
			else
			{
				engCost.edndCost = getDoubleProperty(pieceCostForm, ENG_COST_EDnD);
				if (engCost.edndCost != VF_DOUBLE_NULL) engCost.edndCost *= edndRate;
			}
		}

		bool isMadePart = isMakePart(part, partMakeBuyInput);
		long long approvalDateTimeStamp;
		if (isMadePart)
		{
			approvalDateTimeStamp = getDateProperty(pieceCostForm, ENG_COST_APPROVAL_DATE);
			engCost.approvalDate = convertVFDateLongToDate(approvalDateTimeStamp);
		}
		else
		{
			approvalDateTimeStamp = VF_DATE_LONG_NULL;
		}
		engCost.approvalTimeStamp = approvalDateTimeStamp;
	}
	else
	{
		printObjString(costRev, "WARNING: Cannot find piece cost form of ");
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return engCost;
}

ReportDataSourceImpl::VFCost getEcrCostMadePart(tag_t part, tag_t costRev)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	part;

	ReportDataSourceImpl::VFCost ecrCost;
	setAllCostNull(ecrCost);
	ecrCost.costType = VF_COST_TYPE_ECR;

	map<long long, tag_t> releaseDateAndPurchasing = getForms(costRev, "VF4_ECRCostImpactRelation", "Vf6_purchasing");
	//printObjString(purchasingCostForm, "purchasingCostForm");

	map<long long, tag_t> releaseDateAndManufacturing = getForms(costRev, "VF4_ECRCostImpactRelation", "Vf6_manufacturing");
	//printObjString(manufacuringCostForm, "manufacuringCostForm");

	set<long long> releaseDates;
	for (auto keyAndVal : releaseDateAndPurchasing)
	{
		releaseDates.insert(keyAndVal.first);
	}

	for (auto keyAndVal : releaseDateAndManufacturing)
	{
		releaseDates.insert(keyAndVal.first);
	}

	for (auto it = releaseDates.rbegin(); it != releaseDates.rend(); it++)
	{
		long long releaseDate = *it;

		tag_t purchasingCostForm = releaseDateAndPurchasing[releaseDate];
		if (purchasingCostForm)
		{
			if (ecrCost.pieceCost == VF_DOUBLE_NULL) ecrCost.pieceCost = getDoubleProperty(purchasingCostForm, PURCHASING_COST_PIECE);
			if (ecrCost.edndCost == VF_DOUBLE_NULL) ecrCost.edndCost = getDoubleProperty(purchasingCostForm, PURCHASING_COST_EDND);
			if (ecrCost.toolingCost == VF_DOUBLE_NULL) ecrCost.toolingCost = getDoubleProperty(purchasingCostForm, PURCHASING_COST_TOOLING);
			if (ecrCost.packingCost == VF_DOUBLE_NULL) ecrCost.packingCost = getDoubleProperty(purchasingCostForm, PURCHASING_COST_PACKING);
			if (ecrCost.logisticCost == VF_DOUBLE_NULL) ecrCost.logisticCost = getDoubleProperty(purchasingCostForm, PURCHASING_COST_LOGISTIC);
		}

		tag_t manufacuringCostForm = releaseDateAndManufacturing[releaseDate];
		if (manufacuringCostForm)
		{
			double manufPiece = getDoubleProperty(manufacuringCostForm, MANUFACTURING_COST_PIECE);
			if (ecrCost.pieceCost != VF_DOUBLE_NULL && manufPiece != VF_DOUBLE_NULL)
			{
				ecrCost.pieceCost += manufPiece;
			}
			else if (manufPiece != VF_DOUBLE_NULL)
			{
				ecrCost.pieceCost = manufPiece;
			}

			double manufEdnd = getDoubleProperty(manufacuringCostForm, MANUFACTURING_COST_EDND);
			if (ecrCost.edndCost != VF_DOUBLE_NULL && manufEdnd != VF_DOUBLE_NULL)
			{
				ecrCost.edndCost += manufEdnd;
			}
			else if (ecrCost.edndCost == VF_DOUBLE_NULL && manufEdnd != VF_DOUBLE_NULL)
			{
				ecrCost.edndCost = manufEdnd;
			}

			double manufTooling = getDoubleProperty(manufacuringCostForm, MANUFACTURING_COST_TOOLING);
			if (ecrCost.toolingCost != VF_DOUBLE_NULL && manufTooling != VF_DOUBLE_NULL)
			{
				ecrCost.toolingCost += manufTooling;
			}
			else if (ecrCost.toolingCost == VF_DOUBLE_NULL && manufTooling != VF_DOUBLE_NULL)
			{
				ecrCost.toolingCost = manufTooling;
			}

			double manufPacking = getDoubleProperty(manufacuringCostForm, MANUFACTURING_COST_PACKING);
			if (ecrCost.packingCost != VF_DOUBLE_NULL && manufPacking != VF_DOUBLE_NULL)
			{
				ecrCost.packingCost += manufPacking;
			}
			else if (ecrCost.packingCost == VF_DOUBLE_NULL && manufPacking != VF_DOUBLE_NULL)
			{
				ecrCost.packingCost = manufPacking;
			}

			double manufLogistic = getDoubleProperty(manufacuringCostForm, MANUFACTURING_COST_LOGISTIC);
			if (ecrCost.logisticCost != VF_DOUBLE_NULL && manufLogistic != VF_DOUBLE_NULL)
			{
				ecrCost.logisticCost += manufLogistic;
			}
			else if (ecrCost.logisticCost == VF_DOUBLE_NULL && manufLogistic != VF_DOUBLE_NULL)
			{
				ecrCost.logisticCost = manufLogistic;
			}
		}

		if ((ecrCost.edndCost != VF_DOUBLE_NULL || ecrCost.logisticCost != VF_DOUBLE_NULL || ecrCost.packingCost != VF_DOUBLE_NULL || ecrCost.pieceCost != VF_DOUBLE_NULL || ecrCost.toolingCost != VF_DOUBLE_NULL))
		{
			ecrCost.approvalTimeStamp = releaseDate;
			TC_write_syslog("\n[VF] approval timestamp ecr=%lld", ecrCost.approvalTimeStamp);
			ecrCost.approvalDate = convertVFDateLongToDate(ecrCost.approvalTimeStamp);
		}
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return ecrCost;
}

ReportDataSourceImpl::VFCost getEcrFormFromST(const tag_t &sourcePart, tag_t &stEcrForm)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	ReportDataSourceImpl::VFCost ecrCost;
	setAllCostNull(ecrCost);
	ecrCost.costType = VF_COST_TYPE_ECR;
	tag_t ecrForm = getLatestForm(sourcePart, "VF4_ST_ECRLatestCost_Rela", "VF4_ST_ECRLatestCost");

	if (ecrForm != NULLTAG)
	{
		TC_write_syslog("n[VF] Found ST ECR form");
		stEcrForm = ecrForm;
		long long releaseDate = getDateProperty(ecrForm, "vf4_ecr_latest_rel_date");
		ecrCost.approvalTimeStamp = releaseDate;
		TC_write_syslog("\n[VF] approval timestamp ecr=%lld", ecrCost.approvalTimeStamp);
		ecrCost.approvalDate = convertVFDateLongToDate(ecrCost.approvalTimeStamp);

		double pieceCost = getDoubleProperty(ecrForm, "vf4_piece_costs");
		double logisticCost = getDoubleProperty(ecrForm, "vf4_logistics_costs");
		double packingCost = getDoubleProperty(ecrForm, "vf4_packing_costs");
		double edndCost = getDoubleProperty(ecrForm, "vf4_ednd_costs");
		double toolingCost = getDoubleProperty(ecrForm, "vf4_tooling_costs");

		ecrCost.pieceCost = pieceCost;
		ecrCost.logisticCost = logisticCost;
		ecrCost.packingCost = packingCost;
		ecrCost.edndCost = edndCost;
		ecrCost.toolingCost = toolingCost;
	}
	else
	{
		TC_write_syslog("n[VF] WARNING: Cannot found ECR cost!");
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return ecrCost;
}

ReportDataSourceImpl::VFCost getEcrCostBuyPart(tag_t part, tag_t costRev, tag_t sourcePart)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	part;
	ReportDataSourceImpl::VFCost ecrCost;
	tag_t stEcrForm = NULLTAG;
	if (sourcePart != NULLTAG)
	{
		ecrCost = getEcrFormFromST(sourcePart, stEcrForm);
	}

	if (stEcrForm == NULLTAG)
	{
		setAllCostNull(ecrCost);
		ecrCost.costType = VF_COST_TYPE_ECR;

		map<long long, tag_t> releaseDateAndPurchasing = getForms(costRev, "VF4_ECRCostImpactRelation", "Vf6_purchasing");
		//printObjString(purchasingCostForm, "purchasingCostForm");

		map<long long, tag_t> releaseDateAndManufacturing = getForms(costRev, "VF4_ECRCostImpactRelation", "Vf6_manufacturing");
		//printObjString(manufacuringCostForm, "manufacuringCostForm");

		set<long long> releaseDates;
		for (auto keyAndVal : releaseDateAndPurchasing)
		{
			releaseDates.insert(keyAndVal.first);
		}

		for (auto keyAndVal : releaseDateAndManufacturing)
		{
			releaseDates.insert(keyAndVal.first);
		}

		for (auto it = releaseDates.rbegin(); it != releaseDates.rend(); it++)
		{
			long long releaseDate = *it;

			tag_t purchasingCostForm = releaseDateAndPurchasing[releaseDate];
			if (purchasingCostForm)
			{
				if (ecrCost.pieceCost == VF_DOUBLE_NULL) ecrCost.pieceCost = getDoubleProperty(purchasingCostForm, PURCHASING_COST_PIECE);
				if (ecrCost.edndCost == VF_DOUBLE_NULL) ecrCost.edndCost = getDoubleProperty(purchasingCostForm, PURCHASING_COST_EDND);
				if (ecrCost.toolingCost == VF_DOUBLE_NULL) ecrCost.toolingCost = getDoubleProperty(purchasingCostForm, PURCHASING_COST_TOOLING);
				if (ecrCost.packingCost == VF_DOUBLE_NULL) ecrCost.packingCost = getDoubleProperty(purchasingCostForm, PURCHASING_COST_PACKING);
				if (ecrCost.logisticCost == VF_DOUBLE_NULL) ecrCost.logisticCost = getDoubleProperty(purchasingCostForm, PURCHASING_COST_LOGISTIC);
			}

			tag_t manufacuringCostForm = releaseDateAndManufacturing[releaseDate];
			if (manufacuringCostForm)
			{
				double manufPiece = getDoubleProperty(manufacuringCostForm, MANUFACTURING_COST_PIECE);
				if (ecrCost.pieceCost != VF_DOUBLE_NULL && manufPiece != VF_DOUBLE_NULL)
				{
					ecrCost.pieceCost += manufPiece;
				}
				else if (manufPiece != VF_DOUBLE_NULL)
				{
					ecrCost.pieceCost = manufPiece;
				}

				double manufEdnd = getDoubleProperty(manufacuringCostForm, MANUFACTURING_COST_EDND);
				if (ecrCost.edndCost != VF_DOUBLE_NULL && manufEdnd != VF_DOUBLE_NULL)
				{
					ecrCost.edndCost += manufEdnd;
				}
				else if (ecrCost.edndCost == VF_DOUBLE_NULL && manufEdnd != VF_DOUBLE_NULL)
				{
					ecrCost.edndCost = manufEdnd;
				}

				double manufTooling = getDoubleProperty(manufacuringCostForm, MANUFACTURING_COST_TOOLING);
				if (ecrCost.toolingCost != VF_DOUBLE_NULL && manufTooling != VF_DOUBLE_NULL)
				{
					ecrCost.toolingCost += manufTooling;
				}
				else if (ecrCost.toolingCost == VF_DOUBLE_NULL && manufTooling != VF_DOUBLE_NULL)
				{
					ecrCost.toolingCost = manufTooling;
				}

				double manufPacking = getDoubleProperty(manufacuringCostForm, MANUFACTURING_COST_PACKING);
				if (ecrCost.packingCost != VF_DOUBLE_NULL && manufPacking != VF_DOUBLE_NULL)
				{
					ecrCost.packingCost += manufPacking;
				}
				else if (ecrCost.packingCost == VF_DOUBLE_NULL && manufPacking != VF_DOUBLE_NULL)
				{
					ecrCost.packingCost = manufPacking;
				}

				double manufLogistic = getDoubleProperty(manufacuringCostForm, MANUFACTURING_COST_LOGISTIC);
				if (ecrCost.logisticCost != VF_DOUBLE_NULL && manufLogistic != VF_DOUBLE_NULL)
				{
					ecrCost.logisticCost += manufLogistic;
				}
				else if (ecrCost.logisticCost == VF_DOUBLE_NULL && manufLogistic != VF_DOUBLE_NULL)
				{
					ecrCost.logisticCost = manufLogistic;
				}
			}

			if ((ecrCost.edndCost != VF_DOUBLE_NULL || ecrCost.logisticCost != VF_DOUBLE_NULL || ecrCost.packingCost != VF_DOUBLE_NULL || ecrCost.pieceCost != VF_DOUBLE_NULL || ecrCost.toolingCost != VF_DOUBLE_NULL))
			{
				ecrCost.approvalTimeStamp = releaseDate;
				TC_write_syslog("\n[VF] approval timestamp ecr=%lld", ecrCost.approvalTimeStamp);
				ecrCost.approvalDate = convertVFDateLongToDate(ecrCost.approvalTimeStamp);
			}
		}
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return ecrCost;
}

tag_t getCostRev(string partNumber, vector<string> &errorMsgs)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	map<string, string> keyAndVal;
	keyAndVal["ID"] = partNumber;
	vector<tag_t> founds;
	tag_t found = NULLTAG;
	queryObj("VF Cost", keyAndVal, founds, errorMsgs);
	if (founds.size() > 0) found = founds[0];

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return found;
}

void queryObj(const char *queryName, map<string, string> keysAndVals,
			  vector<tag_t> &founds, vector<string> &errorMsgs) {
  if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
  tag_t query = NULLTAG;
  ERROR_CHECK(QRY_find2(queryName, &query));

  if (query) {
	  char **entries = (char**) MEM_alloc(keysAndVals.size() * sizeof(char*));
	  char **values = (char**) MEM_alloc(keysAndVals.size() * sizeof(char*));
	  int i = 0;
	  for (auto keyAndVal : keysAndVals) {
		  string key = keyAndVal.first;
		  string val = keyAndVal.second;

		  entries[i] = (char*) MEM_alloc(key.size() * sizeof(char) + 1);
		  values[i] = (char*) MEM_alloc(val.size() * sizeof(char) + 1);

		  tc_strcpy(entries[i], key.c_str());
		  tc_strcpy(values[i], val.c_str());

		  if (VF_IS_DEBUG) TC_write_syslog("\n[VF] qry key=%s", entries[i]);
		  if (VF_IS_DEBUG) TC_write_syslog("\n[VF] qry val=%s", values[i]);

		  i++;
	  }

	  int foundsCount = 0;
	  tag_t *foundTags = NULL;
	  ERROR_CHECK(
		  QRY_execute(query, keysAndVals.size(), entries, values, &foundsCount, &foundTags));
	  if (VF_IS_DEBUG) TC_write_syslog("\n[VF] qry %d foundsCount=%d", query, foundsCount);

	  founds.clear();
	  for (i = 0; i < foundsCount; i++) {
		  founds.push_back(foundTags[i]);
	  }

	  SAFE_SM_FREE(foundTags);
	  for (i = 0; i < keysAndVals.size(); i++) {
		  SAFE_SM_FREE(entries[i]);
		  SAFE_SM_FREE(values[i]);
	  }
	  SAFE_SM_FREE(entries);
	  SAFE_SM_FREE(values);

  } else {
	  string errorMsg = "Cannot find query \"" + string(queryName) + "\"";
	  errorMsgs.push_back(errorMsg);
  }

  if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

void printOutput(ReportDataSourceImpl::VFPartCostCalcResponse output) {
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] Cost Calculation Output:");
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ");
	for (auto resEntry : output.result)
	{
		std::string partNumber = resEntry.first;
		ReportDataSourceImpl::VFPartCost partCost = resEntry.second;
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF]   Part \"%s\"", partNumber.c_str());
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF]      edndCost       =%8.5f", partCost.partCost.edndCost);
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF]      labourCost     =%8.5f", partCost.partCost.labourCost);
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF]      logisticCost   =%8.5f", partCost.partCost.logisticCost);
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF]      packingCost    =%8.5f", partCost.partCost.packingCost);
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF]      pieceCost      =%8.5f", partCost.partCost.pieceCost);
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF]      tax            =%8.5f", partCost.partCost.tax);
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF]      toolingCost    =%8.5f", partCost.partCost.toolingCost);
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF]      totalPieceCost =%8.5f", partCost.partCost.totalPieceCost);
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ");
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] Error messages:");
	for (auto errorMsg : output.errorMessages)
	{
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF] - %s", errorMsg.c_str());
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

void printObjString(tag_t obj, const char *prefixStr)
{
	//if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	char *uid = NULL;
	char *objStr = NULL;
	if (obj)
	{
		ITK__convert_tag_to_uid(obj, &uid);
		ERROR_CHECK(AOM_ask_value_string(obj, "object_string", &objStr));
		string prefix(prefixStr);
		prefix.append(" ").append(objStr);
		prefix.append(" - \"").append(uid).append("\"");
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF] %s", prefix.c_str());
	}
	else
	{
		if (VF_IS_DEBUG) TC_write_syslog("\n[VF] cannot print \"%s\" as obj is null", prefixStr);
	}

	SAFE_SM_FREE(uid);
	SAFE_SM_FREE(objStr);

	//if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

void findItemAndRev(const char* itemId, const char *itemType, const char*revId,
					tag_t &item, tag_t &rev, vector<string> &errorMsgs) {
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	map<string, string> queryInput;
	vector<tag_t> founds;
	queryInput["Item ID"] = itemId;
	queryInput["Type"] = itemType;

	queryObj("Item...", queryInput, founds, errorMsgs);
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF]founds=%d", founds.size());
	if (founds.size() == 1) {
		item = founds[0];
		tag_t *revs = NULL;
		int revsCount = 0;
		ERROR_CHECK(ITEM_list_all_revs(item, &revsCount, &revs));
		for (int i = 0; i < revsCount; i++) {
			char *rid = NULL;
			ERROR_CHECK(ITEM_ask_rev_id2(revs[i], &rid));
			if (tc_strcmp(rid, revId) == 0) {
				rev = revs[i];
				SAFE_SM_FREE(rid);
				break;
			}

			SAFE_SM_FREE(rid);
		}
	}

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

bool checkIsAssembly(tag_t part)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	tag_t latestRev = NULLTAG;
	ERROR_CHECK(ITEM_ask_latest_rev(part, &latestRev));
	int foundsNum = 0;
	tag_t *founds = NULL;
	tag_t *bvrs = NULLTAG;
	int bvrsNum = 0;
	if (latestRev != NULLTAG)
	{
		ERROR_CHECK(AOM_ask_value_tags(latestRev, ATTR_BVR, &bvrsNum, &bvrs));
		if (bvrsNum > 0)
		{
			ERROR_CHECK(AOM_ask_value_tags(bvrs[0], ATTR_BVR_OCCURENCE, &foundsNum, &founds));
		}
	}
	else
	{
		TC_write_syslog("\n[VF] WARNING: Cannot found latest revision!");
	}

	SAFE_SM_FREE(founds);
	SAFE_SM_FREE(bvrs);
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return (foundsNum > 0);
}
