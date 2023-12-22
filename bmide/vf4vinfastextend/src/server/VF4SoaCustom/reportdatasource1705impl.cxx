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

#include <reportdatasource1705impl.hxx>

#include <map>
#include <string>
#include <vector>
#include "qry/qry.h"
#include "tccore/aom_prop.h"
#include "tccore/aom.h"
#include "user_exits\epm_toolkit_utils.h"
#include "tccore/item.h"
#include "tccore/grm.h"
#include "tccore/WorkspaceObject.hxx"
#include <metaframework/BusinessObjectRef.hxx>
#include <metaframework/BusinessObjectRegistry.hxx>
#include <stdlib.h>
#include <windows.h>
#include "vf_custom.hxx"
using namespace VF4::Soa::Custom::_2017_05;
using namespace Teamcenter::Soa::Server;
using namespace Teamcenter;
using namespace std;

#define EXIT_FAILURE 1


void initPartRevs(const VF4::Soa::Custom::_2017_05::ReportDataSource::CostReportDSInput& input, vector<BusinessObjectRef<Teamcenter::WorkspaceObject>> &partRevs)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	for (string partRevID : input.partRevs)
	{
		string partID = partRevID;
		partID.erase(partID.find("/"));

		string revID = partRevID;
		revID.erase(0, revID.find("/") + 1);

		int revsFoundNum = -1;
		tag_t *revsFound = NULLTAG;

		char **entries = (char **) MEM_alloc(1 * sizeof(char*));
		entries[0] = (char*) MEM_alloc((7+1) * sizeof(char));
		tc_strcpy(entries[0], "item_id");

		char **values = (char **) MEM_alloc(1 * sizeof(char*));
		values[0] = (char*) MEM_alloc((partID.length()+1) * sizeof(char));
		tc_strcpy(values[0], partID.c_str());

		TC_write_syslog("\n[VF] ITEM_find_item_revs_by_key_attributes\n");
		ERROR_CHECK(ITEM_find_item_revs_by_key_attributes(1, (const char **)entries, (const char **)values, revID.c_str(), &revsFoundNum, &revsFound));
		if (revsFoundNum == 1)
		{
			BusinessObjectRef<Teamcenter::WorkspaceObject> foundPartRev(revsFound[0]);
			partRevs.push_back(foundPartRev);
		}
		else
		{
			//error???
		}

		TC_write_syslog("\n[VF] freeing mem\n");

		SAFE_SM_FREE(entries[0]);
		SAFE_SM_FREE(entries);
		SAFE_SM_FREE(values[0]);
		SAFE_SM_FREE(values);
		SAFE_SM_FREE(revsFound);
	}
	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

void initSourcePartsMap(const VF4::Soa::Custom::_2017_05::ReportDataSource::CostReportDSInput& input, const vector<BusinessObjectRef<Teamcenter::WorkspaceObject>> &partRevs, map<tag_t, tag_t> &partNumbersAndSourceParts)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	string itemIDsStr;
	map<string, tag_t> partIDandPart;
	for (BusinessObjectRef<Teamcenter::WorkspaceObject> partRev : partRevs)
	{
		string itemID;
		bool isNull = true;
		partRev->getString("item_id", itemID, isNull);
		itemIDsStr.append(itemID).append(";");
		partIDandPart[itemID] = partRev->getTag();
	}

	tag_t tQuery;
	int iNItem;
	tag_t *ptItem;
	const char *queryName = "Source Part";
	ERROR_CHECK(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("\nERROR: Cannot find query %s:\n", queryName);
		exit(EXIT_FAILURE);
	}
	char **queryValues = (char**) MEM_alloc(sizeof(char*)*2);
	queryValues[0] = (char*) MEM_alloc(sizeof(char) * (itemIDsStr.length() + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * (input.sourcingProgram.length() + 1));

	tc_strcpy(queryValues[0], itemIDsStr.c_str());
	tc_strcpy(queryValues[1], input.sourcingProgram.c_str());

	char *queryEntries[]	= {"VF Part Number", "Sourcing Program"};

	ERROR_CHECK(QRY_execute(tQuery,2,queryEntries,queryValues,&iNItem,&ptItem));

	for (int i = 0; i < iNItem; i ++)
	{
		BusinessObjectRef<Teamcenter::WorkspaceObject> sourcePart(ptItem[i]);
		string partID;
		bool isNull = true;
		sourcePart->getString("vf4_bom_vfPartNumber", partID, isNull);
		tag_t partRev = partIDandPart[partID];// TODO: check not found
		partNumbersAndSourceParts[partRev] = ptItem[i];

	}

	SAFE_SM_FREE(queryValues[0]);
	SAFE_SM_FREE(queryValues[1]);
	SAFE_SM_FREE(queryValues);
	SAFE_SM_FREE(ptItem);
	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

void initECRsMap (const vector<BusinessObjectRef<Teamcenter::WorkspaceObject>> &partRevs, map<tag_t, vector<tag_t>> &partNumbersAndECRs)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	tag_t *partRevsArray = (tag_t*) MEM_alloc(partRevs.size() * sizeof(tag_t));
	int i = 0;
	for (BusinessObjectRef<Teamcenter::WorkspaceObject> partRev : partRevs)
	{
		partRevsArray[i++] = partRev.tag();
	}

	tag_t relTypes[2];
	ERROR_CHECK(TCTYPE_ask_type("Cm0HasProposal", &(relTypes[0])));
	ERROR_CHECK(	TCTYPE_ask_type("CMHasImpactedItem", &(relTypes[1])));

	GRM_list_relation_input_t relInputs[1];
	relInputs[0].object_count = partRevs.size();
	relInputs[0].objects = partRevsArray;
	relInputs[0].relation_type_count = 2;
	relInputs[0].relation_types = relTypes;
	int foundNum;
	GRM_relation_t *founds;

	ERROR_CHECK(GRM_list_primary_objects_in_bulk(1, relInputs, &foundNum, &founds));

	for (int j = 0; j < foundNum; j++)
	{
		tag_t primary = founds[j].primary;
		char* primaryType = NULL;

		tag_t partRev = founds[j].secondary;

		ERROR_CHECK(WSOM_ask_object_type2(primary, &primaryType));
		if (tc_strcmp(primaryType, "Vf6_ECRRevision") == 0)
		{
			tag_t ecr = primary;
			vector<tag_t> ecrs;

			if (partNumbersAndECRs.find(partRev) == partNumbersAndECRs.end())
			{
				ecrs.push_back(ecr);
			}
			else
			{
				ecrs = partNumbersAndECRs.find(partRev)->second;
				ecrs.push_back(ecr);
			}

			partNumbersAndECRs[partRev] = ecrs;
		}
	}

	SAFE_SM_FREE(partRevsArray);
	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

void initSourcePartCostInfo(const tag_t &sourcePart, VF4::Soa::Custom::_2017_05::ReportDataSourceImpl::SourcePartCostInfo &spCostInfo)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	bool isNull = true;
	BusinessObjectRef<Teamcenter::WorkspaceObject> sourcePartRev(sourcePart);
	sourcePartRev->getDouble("vf4_eddorder_value", spCostInfo.vf4EddorderValue, isNull);
	sourcePartRev->getDouble("vf4_piece_cost", spCostInfo.vf4PieceCost, isNull);
	sourcePartRev->getDouble("vf4_supplier_piece_cost_exw", spCostInfo.vf4SupplierPieceCostExw, isNull);
	sourcePartRev->getDouble("vf4_tooling_order", spCostInfo.vf4ToolingOrder, isNull);
	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

void initECRCostInfo(const tag_t &ecr, VF4::Soa::Custom::_2017_05::ReportDataSourceImpl::ECRCostInfo &ecrCostInfo)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	bool isNull = true;
	BusinessObjectRef<Teamcenter::WorkspaceObject> ecrRev(ecr);

	ecrRev->getString("object_name", ecrCostInfo.ecrName, isNull);
	ecrRev->getString("item_id", ecrCostInfo.ecrNumber, isNull);
	ecrRev->getString("vf6_module_group", ecrCostInfo.vf6ModuleGroup, isNull);
	ecrRev->getString("vf6_vehicle_group", ecrCostInfo.vf6VehicleGroup, isNull);

	int changeFormsRelNum;
	tag_t *changeFormsRel;
	ERROR_CHECK(AOM_ask_relations(ecrRev->getTag(), "Vf6_change_forms", &changeFormsRelNum, &changeFormsRel));
	for (int i = 0; i < changeFormsRelNum; i++)
	{
		tag_t costForm;
		char *costFormType;
		ERROR_CHECK(AOM_ask_value_tag(changeFormsRel[i], "secondary_object", &costForm));
		// TODO: check cost forms related to correct part
		ERROR_CHECK(WSOM_ask_object_type2(costForm, &costFormType));
		if (tc_strcmp(costFormType, "Vf6_purchasing") == 0)
		{
			BusinessObjectRef<Teamcenter::WorkspaceObject> costFormRev(costForm);
			costFormRev->getDouble("vf6_fixtures", ecrCostInfo.vf6Fixtures, isNull);
			costFormRev->getDouble("vf6_lead_time_p", ecrCostInfo.vf6LeadTimeP, isNull);
			costFormRev->getDouble("vf6_material_costs", ecrCostInfo.vf6MaterialCosts, isNull);
			costFormRev->getDouble("vf6_scrap_costs", ecrCostInfo.vf6ScrapCosts, isNull);
			costFormRev->getDouble("vf6_tooling_costs", ecrCostInfo.vf6ToolingCosts, isNull);
			costFormRev->getDouble("vf6_supplier_eng_costs", ecrCostInfo.vf6SupplierEngCosts, isNull);
		}
	}

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

void buildOutput(vector<BusinessObjectRef<Teamcenter::WorkspaceObject>> partRevs, map<tag_t, tag_t> partNumbersAndSourceParts, map<tag_t, vector<tag_t>> partNumbersAndECRs, VF4::Soa::Custom::_2017_05::ReportDataSourceImpl::CostReportDSOutput &output)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	for (BusinessObjectRef<Teamcenter::WorkspaceObject> partRev : partRevs)
	{
		VF4::Soa::Custom::_2017_05::ReportDataSourceImpl::CostInfo costInfo;
		string partID, revID, partRevID;
		bool isNull = true;
		partRev->getString("item_id", partID, isNull);
		partRev->getString("item_revision_id", revID, isNull);
		partRevID = partID;
		partRevID.append("\\").append(revID);

		VF4::Soa::Custom::_2017_05::ReportDataSourceImpl::SourcePartCostInfo spCostInfo;
		tag_t sourcePart = partNumbersAndSourceParts[partRev->getTag()];
		if (sourcePart != NULLTAG)
		{
			initSourcePartCostInfo(sourcePart, spCostInfo);
			costInfo.sourcePart = spCostInfo;
		}
		else
		{
			// add to service data cannot find source part of the part???
		}

		vector<tag_t> ecrs = partNumbersAndECRs[partRev->getTag()];
		if (ecrs.size() > 0)
		{
			for (const tag_t ecr : ecrs)
			{
				VF4::Soa::Custom::_2017_05::ReportDataSourceImpl::ECRCostInfo ecrCostInfo;
				initECRCostInfo(ecr, ecrCostInfo);
				costInfo.ecrs.push_back(ecrCostInfo);
			}
		}
		else
		{
			// add to service data cannot find ECR of the part???
		}

		TC_write_syslog("\n[VF]setting cost info into output.response-partRevID %s", partRevID.c_str());
		output.response[partRevID] = costInfo;
	}
	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

VF4::Soa::Custom::_2017_05::ReportDataSourceImpl::CostReportDSOutput VF4::Soa::Custom::_2017_05::ReportDataSourceImpl::getCostReportDS ( const CostReportDSInput& input )
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	//init response
	_putenv_s("TC_KEEP_SYSTEM_LOG", "Y");
    //get all relevant ecrs

	//get all relevant source part

	map<tag_t, tag_t> partNumbersAndSourceParts;
	map<tag_t, vector<tag_t>> partNumbersAndECRs;
	vector<BusinessObjectRef<Teamcenter::WorkspaceObject>> partRevs;

	TC_write_syslog("\n[VF] partRevs %d", input.partRevs[0].c_str());
	initPartRevs(input, partRevs);
	TC_write_syslog("\n[VF] partRevs %d - input %d", partRevs.size(), input.partRevs.size());
	CostReportDSOutput output;
	if (input.partRevs.size() == partRevs.size())
	{
		initSourcePartsMap(input, partRevs, partNumbersAndSourceParts);
		TC_write_syslog("\n[VF] partNumbersAndSourceParts %d", partNumbersAndSourceParts.size());

		initECRsMap(partRevs, partNumbersAndECRs);
		TC_write_syslog("\n[VF] partNumbersAndECRs %d", partNumbersAndECRs.size());

		buildOutput(partRevs, partNumbersAndSourceParts, partNumbersAndECRs, output);


	}
	else
	{
		// return error???
	}




	//prepare partNumbersAndSourceParts
	// query source part based on list parts number and sourcing program
	// loop found parts and init partNumbersAndSourceParts

	//prepare partNumbersAndECRs
	// loop part rev input
	//    get recenced ecr
	//    add to partNumbersAndECRs


	//prepare output
	//  loop part list
	//      init costInfo
	//      init source part cost info if source part partNumbersAndSourceParts[partnumber] is not null
	//      init ecr part const info if partNumbersAndECRs[partnumber] is not null


//	input;
//	CostReportDSOutput output;


	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return output;
}



