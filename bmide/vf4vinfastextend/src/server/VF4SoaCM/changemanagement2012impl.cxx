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

#include <changemanagement2012impl.hxx>
#include <tc\tc_startup.h>
#include <user_exits\epm_toolkit_utils.h>
#include <tccore\aom_prop.h>
#include <tccore\grm.h>
#include <tccore\item.h>
#include <qry\qry.h>
#include <lov\lov.h>
#include <fclasses\tc_string.h>
#include <fclasses\tc_date.h>
#include <tccore\workspaceobject.h>
#include <tccore\aom.h>
#include <tcinit\tcinit.h>
#include <SA\user.h>
#include <SA\am.h>
#include <ae\ae.h>
#include <me\me.h>
#include <sa\tcfile.h>
#include <ss\ss_const.h>
#include <user_exits\user_exits.h>
#include <ae\ae.h>
#include <tc\preferences.h>
#include <string>
#include <string.h>
#include <sstream>
#include <bom/bom.h>
#include <cfm/cfm.h>
#include <base_utils/ScopedSmPtr.hxx>
#include <base_utils/ScopedPtr.hxx>
#include "tccore\item.h"

#include <metaframework/BusinessObjectRef.hxx>
#include <tccore/Item.hxx>
#include "vf_custom.hxx"

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
using namespace VF4::Soa::CM::_2020_12;
using namespace Teamcenter::Soa::Server;
using namespace std;

ChangeManagement::PartNumberChangeHistory createPartNumberChangeHistory(tag_t partRev, tag_t oldPartRev, tag_t ecrRev, tag_t part, bool isIncludeCost)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	ChangeManagement::PartNumberChangeHistory historyRow;
	historyRow.ecrRev = ecrRev;
	historyRow.oldPartRev = oldPartRev;
	historyRow.partRev = partRev;
	historyRow.toolingCost = -1;
	historyRow.edndCost = -1;
	historyRow.saCost = -1;
	historyRow.totalCost = -1;

	int ifail = ITK_ok;

	Teamcenter::scoped_smptr<char> ecrString, partString, oldPartString;

	CHECK_FAIL(AOM_load(partRev));
	if (ecrRev != NULLTAG) CHECK_FAIL(AOM_load(ecrRev));
	if (oldPartRev != NULLTAG) CHECK_FAIL(AOM_load(oldPartRev));
	CHECK_FAIL(AOM_ask_value_string(partRev, "object_string", &partString));
	if (ecrRev != NULLTAG) CHECK_FAIL(AOM_ask_value_string(ecrRev, "object_string", &ecrString));
	if (oldPartRev != NULLTAG) CHECK_FAIL(AOM_ask_value_string(oldPartRev, "object_string", &oldPartString));

	historyRow.ecrString = ecrString.getString();
	historyRow.partString = partString.getString();
	historyRow.oldPartString = oldPartString.getString();

	if (isIncludeCost)
	{
		tag_t* configured_revs = NULL;
		char** how_configured = NULL;
		tag_t rev_rule = NULLTAG;
		CHECK_FAIL(CFM_find("VINFAST_WORKING_RULE", &rev_rule));
		CHECK_FAIL(CFM_items_ask_configured(rev_rule, 1, &part, &configured_revs, &how_configured));
		tag_t latestWorkingRev = NULLTAG;

		if (configured_revs != NULL && configured_revs[0] != NULL)
		{
			latestWorkingRev = configured_revs[0];
		}

		if (latestWorkingRev != NULL)
		{
			Teamcenter::scoped_smptr<char> latestWorkingRevString;
			CHECK_FAIL(AOM_ask_value_string(latestWorkingRev, "object_string", &latestWorkingRevString));


			TC_write_syslog("\n[VF] latestWorkingRevString=%s", latestWorkingRevString.getString());
			Teamcenter::scoped_smptr<tag_t> costObjs;
			int costObjCount = 0;
			tag_t costObjRelType = NULLTAG;

			CHECK_FAIL(TCTYPE_ask_type("VF4_Costing_Reference", &costObjRelType));
			CHECK_FAIL(GRM_list_secondary_objects_only(latestWorkingRev, costObjRelType, &costObjCount, &costObjs));

			if (costObjs != NULLTAG && costObjCount > 0)
			{
				tag_t costObj = costObjs[0];

				tag_t sapCostFormRelType = NULLTAG;
				Teamcenter::scoped_smptr<tag_t> sapCostForms;
				int sapCostFormsCount = 0;
				CHECK_FAIL(TCTYPE_ask_type("VF4_SAPCostRelation", &sapCostFormRelType));
				CHECK_FAIL(GRM_list_secondary_objects_only(costObj, sapCostFormRelType, &sapCostFormsCount, &sapCostForms));

				if (sapCostFormsCount > 0)
				{
					tag_t sapCostForm = sapCostForms[0];
					double edndCost,toolingCost,saCost;
					Teamcenter::scoped_smptr<char> edndCurrency,toolingCurrency,saCurrency;
					edndCost = toolingCost = saCost = -1;

					logical isNull = true;
					CHECK_FAIL(AOM_is_null_empty(sapCostForm, "vf4EDDCost", false, &isNull));
					if (!isNull) CHECK_FAIL(AOM_ask_value_double(sapCostForm, "vf4EDDCost", &edndCost));

					isNull = true;
					CHECK_FAIL(AOM_is_null_empty(sapCostForm, "vf4ToolingCost", false, &isNull));
					if (!isNull) CHECK_FAIL(AOM_ask_value_double(sapCostForm, "vf4ToolingCost", &toolingCost));

					isNull = true;
					CHECK_FAIL(AOM_is_null_empty(sapCostForm, "vf4SAPieceCost", false, &isNull));
					if (!isNull) CHECK_FAIL(AOM_ask_value_double(sapCostForm, "vf4SAPieceCost", &saCost));

					CHECK_FAIL(AOM_ask_value_string(sapCostForm, "vf4EDDCostCurrency", &edndCurrency));
					CHECK_FAIL(AOM_ask_value_string(sapCostForm, "vf4ToolingCostCurrency", &toolingCurrency));
					CHECK_FAIL(AOM_ask_value_string(sapCostForm, "vf4SAPieceCostCurrency", &saCurrency));

					//historyRow.sourcePart = sourcePart;
					historyRow.edndCost = edndCost;
					historyRow.edndCurrency = edndCurrency.getString();
					historyRow.toolingCost = toolingCost;
					historyRow.toolingCurrency = toolingCurrency.getString();
					historyRow.saCost = saCost;
					historyRow.saCurrency = saCurrency.getString();
					//historyRow.totalCost = totalCost;
					//historyRow.totalCostStatus = totalCostStatus.getString();
				}
			}
		}

		SAFE_SM_FREE(configured_revs);
		SAFE_SM_FREE(how_configured);
	}

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return historyRow;
}

ChangeManagementImpl::DeriveChangeResponse ChangeManagementImpl::deriveChange(
		const std::vector<DeriveChangeInput>& inputs) {
	int ifail = ITK_ok;
	ChangeManagementImpl::DeriveChangeResponse response;
	TC_write_syslog("[VF] ENTER %s", __FUNCTION__);

	TC_write_syslog("\n[VF]Size of Input Vector : %d\n", inputs.size());
	if (inputs.size() > 0) {
		for (DeriveChangeInput ChangeInput : inputs) {
			try {
				ifail = ITK_ok;
				tag_t changeObjRev = ChangeInput.changeObjRev;
				std::vector<PropertyAndValues> propAndValues =
						ChangeInput.propertyAndValues;
				std::string relation = ChangeInput.relationToAttachChangeObj;
				std::string objType = ChangeInput.derivedObjType;

				bool idFlag = false;
				tag_t item_type_tag = NULLTAG;
				tag_t item_create_input_tag = NULLTAG;
				tag_t tNewChange = NULLTAG;
				tag_t tCMImplements = NULLTAG;
				tag_t changeItem = NULLTAG;
				Teamcenter::scoped_smptr<char> item_id;
				std::string errorMsg;
				tag_t cmImplements = NULLTAG;
				tag_t newChangeRev = NULLTAG;

				CHECK_FAIL(ITEM_ask_item_of_rev(changeObjRev, &changeItem));
				CHECK_FAIL(
						TCTYPE_find_type(objType.c_str(), NULLTAG, &item_type_tag));
				CHECK_FAIL(
						TCTYPE_construct_create_input(item_type_tag,
								&item_create_input_tag));
				for (PropertyAndValues propertyArray : propAndValues) {
					std::string propName = propertyArray.propertyName;
					std::string propvalue = propertyArray.propertyValue;

					const char *value[] = { propvalue.c_str() };
					if (tc_strcmp(propName.c_str(), "item_id") == 0) {
						idFlag = true;
						TCTYPE_set_create_display_value(item_create_input_tag,
								"item_id", 1, value);
					} else {
						CHECK_FAIL(
								TCTYPE_set_create_display_value(
										item_create_input_tag,
										(char* )propName.c_str(), 1, value));
					}
				}

				if (!idFlag) {
					Teamcenter::scoped_smptr<char> newObjId;
					CHECK_FAIL(
							AOM_ask_value_string(changeItem, "item_id",
									&item_id));
					CHECK_FAIL(
							STRNG_replace_str(item_id.getString(), "ECR", "ECN",
									&newObjId));
					const char *value1[] = { newObjId.getString() };
					TC_write_syslog("newObjId%s\n", newObjId.getString());
					TC_write_syslog("value1%s\n", value1);
					CHECK_FAIL(
							TCTYPE_set_create_display_value(
									item_create_input_tag, "item_id", 1,
									value1));
				}

				CHECK_FAIL(
						TCTYPE_create_object(item_create_input_tag,
								&tNewChange));
				TC_write_syslog("item_tag %d \n", tNewChange);
				CHECK_FAIL(ITEM_save_item(tNewChange));
				TC_write_syslog("Item Created Successfully\n");
				ifail += ITEM_ask_latest_rev(tNewChange, &newChangeRev);
				TC_write_syslog("newChangeRev %d \n", newChangeRev);

				//attaching new change object with CMImplements

				ifail += GRM_find_relation_type(relation.c_str(),
						&tCMImplements);
				ifail += GRM_create_relation(newChangeRev, changeObjRev,
						tCMImplements, NULLTAG, &cmImplements);
				CHECK_FAIL(GRM_save_relation(cmImplements));
				TC_write_syslog("Attached  Created Successfully\n");

				//reading pref for relations
				int relCount = 0;
				int isecIny = 0;
				Teamcenter::scoped_smptr<char*> relValues;

				CHECK_FAIL(
						PREF_ask_char_values("VF_ECR_ECN_Propergate_Relations",
								&relCount, &relValues));
				TC_write_syslog("\t relCount: %d \n", relCount);

				for (int i = 0; i < relCount; i++) {
					int iCountSecObjs = 0;
					tag_t tRepresentedBy = NULLTAG;
					Teamcenter::scoped_smptr<tag_t> ptSecondaryObjs;
					TC_write_syslog("\t  relValues[%d]: %s \n", i,
							relValues[i]);
					ifail += GRM_find_relation_type(relValues[i],
							&tRepresentedBy);
					ifail += GRM_list_secondary_objects_only(changeObjRev,
							tRepresentedBy, &iCountSecObjs, &ptSecondaryObjs);
					TC_write_syslog("Inside Relation for loop for copy\n");
					for (isecIny = 0; isecIny < iCountSecObjs; isecIny++) {
						tag_t tcreatedRelation = NULLTAG;
						ifail += GRM_create_relation(newChangeRev,
								ptSecondaryObjs[isecIny], tRepresentedBy,
								NULLTAG, &tcreatedRelation);
						ifail += GRM_save_relation(tcreatedRelation);
					}
				}

				//reading pref for rev properties
				int revPropCount = 0;
				Teamcenter::scoped_smptr<char*> revProp;
				CHECK_FAIL(
						PREF_ask_char_values(
								"VF_ECR_ECN_Propergate_Properties_Rev",
								&revPropCount, &revProp));
				TC_write_syslog("\t Revision property Count: %d \n",
						revPropCount);
				for (int j = 0; j < revPropCount; j++) {
					int nValues = 0;
					Teamcenter::scoped_smptr<char*> spValues;
					CHECK_FAIL(
							AOM_ask_displayable_values(changeObjRev, revProp[j],
									&nValues, &spValues));
					TC_write_syslog("\t Revision property spValues: %s \n",
							spValues.getString()[0]);
					ifail += AOM_UIF_set_value(newChangeRev, revProp[j],
							spValues[0]);
					CHECK_FAIL(AOM_save_without_extensions(newChangeRev));
				}

				//reading pref for Item properties
				int propCount = 0;
				Teamcenter::scoped_smptr<char*> itemProp;
				CHECK_FAIL(
						PREF_ask_char_values(
								"VF_ECR_ECN_Propergate_Properties_Item",
								&propCount, &itemProp));
				TC_write_syslog("\t Item Prop Count: %d \n", propCount);
				for (int k = 0; k < propCount; k++) {
					int nValues = 0;
					Teamcenter::scoped_smptr<char*> spValues;
					CHECK_FAIL(
							AOM_ask_displayable_values(changeItem, itemProp[k],
									&nValues, &spValues));
					CHECK_FAIL(
							AOM_UIF_set_value(tNewChange, itemProp[k],
									spValues[0]));
					CHECK_FAIL(AOM_save_without_extensions(tNewChange));
				}

				CHECK_FAIL(AOM_refresh(newChangeRev, FALSE));
				CHECK_FAIL(AOM_refresh(tNewChange, FALSE));

				DeriveChangeOutput output;
				output.chnageObjTag = tNewChange;
				output.errorMessage =
						(ifail == ITK_ok) ?
								"" :
								"There are some errors in between, please find details in syslog!";
				response.outputs.push_back(output);
				errorMsg.clear();
			} catch (const IFail &e) {
				std::cout << e.getMessage();
				response.serviceData.addErrorStack();
				TC_write_syslog("\n[VF]Error ::%s\n", e.getMessage());
			}
		}
	}

	TC_write_syslog("[VF] EXIT %s", __FUNCTION__);
	return response;
}

ChangeManagementImpl::ImpactedProgramResponse ChangeManagementImpl::getImpactedPrograms(
		const ImpactedProgramInput& inputs) {
	int ifail = ITK_ok;
	ImpactedProgramOutput mapOutput;
	ChangeManagementImpl::ImpactedProgramResponse response;

	TC_write_syslog("[VF4_ImpactedProgram] ENTER %s\n", __FUNCTION__);

	try {
		tag_t type_tag = false;
		tag_t rtype_tag = false;

		CHECK_FAIL(TCTYPE_ask_type("Item", &type_tag));
		CHECK_FAIL(TCTYPE_ask_type("ItemRevision", &rtype_tag));

		std::string revRule = inputs.revisionRule;
		std::vector<string> topNodeTypes(inputs.topNodeType);
		std::vector<BusinessObjectRef<Teamcenter::BusinessObject>> vecImpactedParts(
				inputs.impactedParts);

		TC_write_syslog("[VF4_ImpactedProgram] No. of Input Components - %d\n",
				vecImpactedParts.size());

		for (tag_t impactedPart : vecImpactedParts) {
			logical revFlag = false;
			logical itemFlag = false;
			set<tag_t> setTopNodes;
			vector<BusinessObjectRef<Teamcenter::BusinessObject>> vecTopNodes;

			CHECK_FAIL(AOM_is_type_of(impactedPart, type_tag, &itemFlag));
			CHECK_FAIL(AOM_is_type_of(impactedPart, rtype_tag, &revFlag));

			tag_t tWindow = NULLTAG;
			tag_t rev_tag = NULLTAG;
			tag_t tItemTag = NULLTAG;
			tag_t rev_rule = NULLTAG;

			CHECK_FAIL(BOM_create_window(&tWindow));
			if (tc_strcmp(revRule.c_str(), "") == 0) {
				CHECK_FAIL(CFM_find("VINFAST_WORKING_RULE", &rev_rule));
			} else {
				CHECK_FAIL(CFM_find(revRule.c_str(), &rev_rule));
			}
			CHECK_FAIL(BOM_set_window_config_rule(tWindow, rev_rule));
			if (revFlag) {
				//TC_write_syslog("[VF4_ImpactedProgram] Item Revision Object is passed...");
				CHECK_FAIL(ITEM_ask_item_of_rev(impactedPart, &tItemTag));
			} else if (itemFlag) {
				//TC_write_syslog("[VF4_ImpactedProgram] Item Object is passed...");
				tItemTag = impactedPart;
			} else {
				//TC_write_syslog("[VF4_ImpactedProgram] Unknown Type Object passed as input...");
				continue;
			}

			tag_t tTopBOMLine = NULLTAG;
			Teamcenter::scoped_smptr<tag_t> parents;
			CHECK_FAIL(
					BOM_set_window_top_line(tWindow, tItemTag, NULLTAG, NULLTAG, &tTopBOMLine));
			CHECK_FAIL(AOM_ask_value_tag(tTopBOMLine, "bl_revision", &rev_tag));
			CHECK_FAIL(BOM_close_window(tWindow));

			int *levels = 0;
			int n_parents = 0;
			CHECK_FAIL(
					PS_where_used_configured(rev_tag, rev_rule,PS_where_used_all_levels, &n_parents, &levels, &parents));
			for (int index = 0; index < n_parents; index++) {
				logical idFlag = false;
				if (index == n_parents - 1
						|| levels[index] >= levels[index + 1]) {
					tag_t itemTag = NULLTAG;
					CHECK_FAIL(ITEM_ask_item_of_rev(parents[index], &itemTag));
					Teamcenter::scoped_smptr<char> objType;
					CHECK_FAIL(WSOM_ask_object_type2(itemTag, &objType));

					if (topNodeTypes.size() > 0) {
						for (string topNodeType : topNodeTypes) {
							if (tc_strcmp(topNodeType.c_str(),
									objType.getString()) == 0) {
								idFlag = true;
							}
						}
						if (idFlag) {
							setTopNodes.insert(itemTag);
						}
					} else {
						setTopNodes.insert(itemTag);
					}
				}
			}

			for (auto& itr : setTopNodes) {
				vecTopNodes.push_back(itr);
			}

			Teamcenter::scoped_smptr<char> sItemId;
			CHECK_FAIL(ITEM_ask_id2(tItemTag, &sItemId));

			//TC_write_syslog("\n[VF4_ImpactedProgram] Item Id- %s\tImpacted Program Count - %d\n", sItemId.getString(), vecTopNodes.size());

			response.outputs.insert(
					std::pair<BusinessObjectRef<Teamcenter::BusinessObject>,
							std::vector<
									BusinessObjectRef<Teamcenter::BusinessObject>>>(
							tItemTag, vecTopNodes));
			vecTopNodes.clear();
			setTopNodes.clear();
		}
	} catch (int error) {
		ifail = error;
		TC_write_syslog("\n[VF4_ImpactedProgram] Error ::%d\n", ifail);
	}
	TC_write_syslog("[VF4_ImpactedProgram] EXIT %s", __FUNCTION__);
	return response;
}

ChangeManagementImpl::PartNumberChangeHistoryResponse ChangeManagementImpl::getPartNumberChangeHistory(
		const std::vector<PartNumberChangeHistoryInput>& inputs) {

	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	PartNumberChangeHistoryResponse response;
	for (PartNumberChangeHistoryInput input : inputs) {

		TC_write_syslog("\n[VF] input.isIncludeCost=%d", (int)(input.isIncludeCost));

		const char *partNumber = input.partNumber.c_str();
		const char *partType = input.partType.c_str();

		PartNumberChangeHistoryOutput output;
		output.input = input;

		vector<string> errorMsgs;
		tag_t part = getPart(partNumber, partType, errorMsgs);

//		printObjString(part, "part");
		if (part != NULLTAG) {
			tag_t rev = NULLTAG;
			int revsCount = 0;
			int ifail = ITK_ok;
			Teamcenter::scoped_smptr<tag_t> revs;

			CHECK_FAIL(ITEM_list_all_revs(part, &revsCount, &revs));
			if (revsCount > 0) {
				rev = revs.get()[0];
			}

			if (rev != NULLTAG) {
//				printObjString(rev, "rev");

				string originalBasePartNumber = getStringProperty(part,
						"vf4_orginal_part_number");
				string allOriginalPartNumber("");

				tag_t ecrRev = NULLTAG;
				tag_t oldPartRev = NULLTAG;
				tag_t proposalRelationType = NULLTAG;
				CHECK_FAIL(
						TCTYPE_ask_type("Cm0HasProposal",
								&proposalRelationType));

//				TC_write_syslog("\n[VF]START getting history");
				while (originalBasePartNumber.empty() == false
						&& tc_strstr(allOriginalPartNumber.c_str(),
								originalBasePartNumber.c_str()) == NULL
						&& rev != NULLTAG) {
					int ecrsNum = 0;
					Teamcenter::scoped_smptr<tag_t> ecrs;

					oldPartRev = NULLTAG;
					ecrRev = NULLTAG;

					allOriginalPartNumber.append(originalBasePartNumber).append(
							";");
					CHECK_FAIL(
							GRM_list_primary_objects_only(rev,
									proposalRelationType, &ecrsNum, &ecrs));
					if (ecrsNum > 0) {
						for (int ecrsInx = 0; ecrsInx < ecrsNum; ecrsInx++) {
							Teamcenter::scoped_smptr<char> ecrType;
							CHECK_FAIL(
									WSOM_ask_object_type2(ecrs.get()[ecrsInx],
											&ecrType));
							if (tc_strcmp(ecrType.getString(),
									"Vf6_ECRRevision") == 0) {
								ecrRev = ecrs.get()[ecrsInx];
//								printObjString(ecrRev, "ecrRev");
								break;
							}
						}
					}

					tag_t oldPart = getPart(originalBasePartNumber, partType,
							errorMsgs);
					if (oldPart != NULLTAG) {
						int oldRevsCount = 0;
						Teamcenter::scoped_smptr<tag_t> oldRevs;
						CHECK_FAIL(
								ITEM_list_all_revs(oldPart, &oldRevsCount,
										&oldRevs));

						if (oldRevsCount > 0) {
							oldPartRev = oldRevs.get()[0];
//							printObjString(oldPartRev, "oldPartRev");
						}

						originalBasePartNumber = getStringProperty(oldPart,
								"vf4_orginal_part_number");
					}

					PartNumberChangeHistory historyRow = createPartNumberChangeHistory(rev, oldPartRev, ecrRev, part, input.isIncludeCost);
					output.historyList.push_back(historyRow);

					rev = oldPartRev;
					part = oldPart;
				}

				if (tc_strstr(allOriginalPartNumber.c_str(),
						originalBasePartNumber.c_str()) != NULL) {

					tag_t oldPart = getPart(originalBasePartNumber.c_str(),
							partType, errorMsgs);
					int oldRevsCount = 0;
					Teamcenter::scoped_smptr<tag_t> oldRevs;

					CHECK_FAIL(
							ITEM_list_all_revs(oldPart, &oldRevsCount,
									&oldRevs));

					if (oldRevsCount > 0) {
						oldPartRev = oldRevs.get()[0];
					}

					PartNumberChangeHistory historyRow = createPartNumberChangeHistory(rev, oldPartRev, ecrRev, part, input.isIncludeCost);
					output.historyList.push_back(historyRow);
				}
				else
				{
					PartNumberChangeHistory historyRow = createPartNumberChangeHistory(rev, NULLTAG, NULLTAG, part, input.isIncludeCost);
					output.historyList.push_back(historyRow);
				}

			}

//			TC_write_syslog("\n[VF]END getting history");
		}

		response.outputs.push_back(output);
	}

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return response;
}


void getHistoryParts(tag_t currentPart, std::vector<tag_t> &historyParts, tag_t &repeatedPart)
{
	if (VF_IS_DEBUG) TC_write_syslog("[VF] ENTER %s", __FUNCTION__);

	std::string originalBasePartNumber = getStringProperty(currentPart,
									"vf4_orginal_part_number");

	vector<string> errorMsgs;
	tag_t oldPart = getPart(originalBasePartNumber, "VF4*Design;VF3_Scooter_part", errorMsgs);

	if (oldPart != NULLTAG)
	{
		bool isFoundOldPart = false;
		for (auto historyPart : historyParts)
		{
			if (historyPart == oldPart)
			{
				isFoundOldPart = true;
				repeatedPart = oldPart;
				break;
			}
		}

		if (isFoundOldPart == false)
		{
			historyParts.push_back(oldPart);
			getHistoryParts(oldPart, historyParts, repeatedPart);
		}
	}

	if (VF_IS_DEBUG) TC_write_syslog("[VF] EXIT %s", __FUNCTION__);
}

tag_t getFuturePart(string partNumber, string partType, vector<string> &errorMsgs)
{
	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	map<string, string> keyAndVal;
	keyAndVal["Original Base Part Number"] = partNumber;
	keyAndVal["Type"] = partType;
	vector<tag_t> founds;
	tag_t found = NULLTAG;
	queryObj("VF Replacing Part", keyAndVal, founds, errorMsgs);
	if (founds.size() > 0) found = founds[0];

	if (VF_IS_DEBUG) TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return found;
}

void getFutureParts(tag_t currentPart, std::vector<tag_t> &futureParts, tag_t &repeatedPart)
{
	if (VF_IS_DEBUG) TC_write_syslog("[VF] ENTER %s", __FUNCTION__);

	std::string currentPartNumber = getStringProperty(currentPart,
									"item_id");

	vector<string> errorMsgs;
	tag_t futurePart = getFuturePart(currentPartNumber, "VF4*Design;VF3_Scooter_part", errorMsgs);

	if (futurePart != NULLTAG)
	{
		bool isFoundFuturePart = false;
		for (auto futurePartTmp : futureParts)
		{
			if (futurePartTmp == futurePart)
			{
				isFoundFuturePart = true;
				repeatedPart = futurePart;
				break;
			}
		}

		if (isFoundFuturePart == false)
		{
			futureParts.push_back(futurePart);
			getFutureParts(futurePart, futureParts, repeatedPart);
		}
	}

	if (VF_IS_DEBUG) TC_write_syslog("[VF] EXIT %s", __FUNCTION__);
}

ChangeManagementImpl::PartNumberChangeFullHistoryResponse ChangeManagementImpl::getPartNumberChangeFullHistory ( const std::vector< PartNumberChangeFullHistoryInput >& inputs )
{
	if (VF_IS_DEBUG) TC_write_syslog("[VF] ENTER %s", __FUNCTION__);

	vector<string> errorMsgs;
	ChangeManagementImpl::PartNumberChangeFullHistoryResponse res;
	int i = 0;
	for (const auto input : inputs)
	{


		////////////////////////////////---

		tag_t currentPart = getPart(inputs[i].partNumber, inputs[i].partType, errorMsgs);


		if (currentPart != NULLTAG)
		{
			// get old parts
			std::vector<tag_t> historyParts;
			tag_t repeatedHistoryPart = NULLTAG;
			getHistoryParts(currentPart, historyParts, repeatedHistoryPart);
			if (repeatedHistoryPart != NULLTAG)
			{
				Teamcenter::scoped_smptr<char> repeatedHistoryPartObjectString;
				AOM_ask_value_string(repeatedHistoryPart, "object_string", &repeatedHistoryPartObjectString);
				std::string errorMsg("[VF] Detected repeated history part:\"");
				errorMsg.append(repeatedHistoryPartObjectString.getString()).append("\"");
				res.servicedata.addErrorStack(currentPart, Teamcenter::ErrorStoreBase::ERRORSTORE_SEVERITY_WARNING, -9999, errorMsg.c_str());
			}


			// get future partss
			std::vector<tag_t> futureParts;
			tag_t repeatedFuturePart = NULLTAG;
			getFutureParts(currentPart, futureParts, repeatedFuturePart);
			if (repeatedFuturePart != NULLTAG)
			{
				Teamcenter::scoped_smptr<char> repeatedFuturePartObjectString;
				AOM_ask_value_string(repeatedFuturePart, "object_string", &repeatedFuturePartObjectString);
				std::string errorMsg("[VF] Detected repeated future part:\"");
				errorMsg.append(repeatedFuturePartObjectString.getString()).append("\"");
				res.servicedata.addErrorStack(currentPart, Teamcenter::ErrorStoreBase::ERRORSTORE_SEVERITY_WARNING, -9999, errorMsg.c_str());
			}

			// consolidate result
			PartNumberChangeFullHistoryOutput fullPartHistoryResponse;
			fullPartHistoryResponse.firstPart = historyParts.size() > 0 ? historyParts[historyParts.size() - 1] : NULLTAG;
			fullPartHistoryResponse.lastPart = futureParts.size() > 0 ? futureParts[futureParts.size() - 1] : NULLTAG;
			fullPartHistoryResponse.futureParts.assign(futureParts.begin(), futureParts.end());
			fullPartHistoryResponse.historyParts.assign(historyParts.begin(), historyParts.end());
			fullPartHistoryResponse.inputPart = currentPart;
			res.outputs.push_back(fullPartHistoryResponse);
			res.servicedata.addPlainObject(currentPart);
		}
		else
		{
			std::string errorMsg("[VF] Cannot find part number - type:\"");
			errorMsg.append(inputs[i].partNumber).append("\" - \"").append(inputs[i].partType).append("\"");
			res.servicedata.addErrorStack(currentPart, Teamcenter::ErrorStoreBase::ERRORSTORE_SEVERITY_WARNING, -9999, errorMsg.c_str());
		}

		////////////////////////////////---

		i++;
	}



	if (VF_IS_DEBUG) TC_write_syslog("[VF] EXIT %s", __FUNCTION__);

	return res;
}



