#include "VF_custom.h"

using namespace std;

auto change_ownership(tag_t sourcing_item_rev, tag_t sourcing_item, tag_t user_cl) -> void;
std::string getBuyerOsName(const char* buyerUserId)
{
	tag_t tQuery = NULLTAG;
	auto retCode = ITK_ok;
	CHECK_ITK(retCode, QRY_find2("Admin - Employee Information", &tQuery));
	if (retCode != ITK_ok)
	{
		TC_write_syslog("[VF_custom]__%d__%s__Cannot find query [Admin - Employee Information]: %d\n", __LINE__, __FUNCTION__, retCode);
		return "";
	}

	const int nEntries = 1;
	char* entries[1] = { "User ID" };
	char** values = nullptr;

	values = (char**)MEM_alloc(nEntries * sizeof(char*));

	values[0] = (char*)MEM_alloc(strlen(buyerUserId) + 1);
	tc_strcpy(values[0], buyerUserId);

	auto userCount = 0;
	tag_t * ptUser = nullptr;

	CHECK_ITK(retCode, QRY_execute(tQuery, nEntries, entries, values, &userCount, &ptUser));
	if (retCode != ITK_ok || userCount != 1)
	{
		TC_write_syslog("[VF_custom]__%d__%s__Cannot find user: %s\n", __LINE__, __FUNCTION__, buyerUserId);
		SAFE_MEM_free(ptUser);
		return "";
	}
	else
	{
		TC_write_syslog("[VF_custom]__%d__%s__Found user: %s\n", __LINE__, __FUNCTION__, buyerUserId);

		char* buyerOsName = NULL;
		AOM_ask_value_string(ptUser[0], "object_string", &buyerOsName);
		std::string output(buyerOsName);
		SAFE_MEM_free(ptUser);
		SAFE_MEM_free(buyerOsName);

		return output;
	}
}

DLLAPI int vf_set_vf4_cost_approval_date(METHOD_message_t* msg, va_list args)
{
	TC_write_syslog("\n[VF] ENTER %s\n", __FUNCTION__);

	int ifail = ITK_ok;

	tag_t sourcePart = msg->object_tag;

	tag_t ecrStForm = NULLTAG;
	tag_t stEcrRelType = NULLTAG;
	tag_t* ecrStForms = NULL;
	int ecrStFormCount = 0;
	date_t stCostApprovalDate; stCostApprovalDate.year = 1990;
	char* error_text = NULL;

	CHECK_FAIL(AOM_ask_value_date(sourcePart, "vf4_cost_approval_date", &stCostApprovalDate));

	if (stCostApprovalDate.year > 2000)
	{
		TC_write_syslog("\n[VF] Getting ecr cost form");
		(TCTYPE_find_type("VF4_ST_ECRLatestCost_Rela", "VF4_ST_ECRLatestCost_Rela", &stEcrRelType));
		(GRM_list_secondary_objects_only(sourcePart, stEcrRelType, &ecrStFormCount, &ecrStForms));

		if (ecrStFormCount > 0 && ecrStForms)
		{
			TC_write_syslog("\n[VF] Getting cost date from ecr cost form and st");
			ecrStForm = ecrStForms[0];
			date_t ecrCostApprovalDate; ecrCostApprovalDate.year = 1990;

			int stIsLater = -99;
			stCostApprovalDate.hour = stCostApprovalDate.minute = stCostApprovalDate.second = 0;

			(AOM_ask_value_date(ecrStForm, "vf4_ecr_latest_rel_date", &ecrCostApprovalDate));
			ecrCostApprovalDate.hour = ecrCostApprovalDate.minute = ecrCostApprovalDate.second = 0;

			// check condition if approvdate > ecr cost form date (only date) or ecr cost form date is blank, then copy list of attribute
			(POM_compare_dates(stCostApprovalDate, ecrCostApprovalDate, &stIsLater));
			char* stPieceCostCurrency = NULL,
				* stToolingCostCurrency = NULL,
				* stEdndCostCurrency = NULL;

			AOM_ask_value_string(sourcePart, "vf4_piece_currency", &stPieceCostCurrency);
			AOM_ask_value_string(sourcePart, "vf4_tooling_currency", &stToolingCostCurrency);
			AOM_ask_value_string(sourcePart, "vf4_eddorder_currency", &stEdndCostCurrency);

			bool hasPieceCost = (tc_strlen(stPieceCostCurrency) > 0 && !tc_strstr(stPieceCostCurrency, "NO COST"));
			bool hasToolingCost = (tc_strlen(stToolingCostCurrency) > 0 && !tc_strstr(stToolingCostCurrency, "NO COST"));
			bool hasEDnDCost = (tc_strlen(stEdndCostCurrency) > 0 && !tc_strstr(stEdndCostCurrency, "NO COST"));

			if (hasPieceCost || hasToolingCost || hasEDnDCost)
			{
				TC_write_syslog("\n[VF] Comparing cost date from ecr cost form and st");
				double toolingRate = hasToolingCost ? getRate(stToolingCostCurrency, "USD") : 1.0;
				double edndRate = hasEDnDCost ? getRate(stEdndCostCurrency, "USD") : 1.0;
				double pieceRate = hasPieceCost ? getRate(stPieceCostCurrency, "USD") : 1.0;

				if (ecrCostApprovalDate.year < 2000 || stIsLater >= 0)
				{
					TC_write_syslog("\n[VF] Copying cost form ecr cost form to st ecr form");

					// prepare list attributes from source part to copy
					map<string, string> srcAndDescAttributes;
					if (hasPieceCost) srcAndDescAttributes["vf4_Supplier_logistic_cost"] = "vf4_logistics_costs";
					if (hasPieceCost) srcAndDescAttributes["vf4_supplier_pkg_amount"] = "vf4_packing_costs";
					if (hasPieceCost) srcAndDescAttributes["vf4_supplier_piece_cost_exw"] = "vf4_piece_costs";
					if (hasPieceCost) srcAndDescAttributes["vf4_piece_cost_auto"] = "vf4_total_piece_cost";
					if (hasEDnDCost) srcAndDescAttributes["vf4_eddorder_value_2"] = "vf4_ednd_costs";
					if (hasToolingCost) srcAndDescAttributes["vf4_tooling_order_2"] = "vf4_tooling_costs";
					srcAndDescAttributes["vf4_cost_approval_date"] = "vf4_ecr_latest_rel_date";
					srcAndDescAttributes["vf4_piece_cost_status"] = "vf4_ecr_num_latest_rel";

					(AOM_refresh(ecrStForm, TRUE));
					for (auto srcAndDescAttribute : srcAndDescAttributes)
					{
						string sourceAttribute = srcAndDescAttribute.first;
						string descAttribute = srcAndDescAttribute.second;

						char* val = NULL;
						char* sourcePartTypeName = NULL;
						const char* sourceAttr = sourceAttribute.c_str();

						tag_t attrId = NULLTAG;
						logical isNull = false;

						AOM_is_null_empty(sourcePart, sourceAttr, false, &isNull);
						WSOM_ask_object_type2(sourcePart, &sourcePartTypeName);

						POM_attr_id_of_attr(sourceAttr, sourcePartTypeName, &attrId);
						TC_write_syslog("\n[VF] ********:  %s - %s - %d", sourceAttr, sourcePartTypeName, isNull);
						if (isNull)
						{
							(AOM_UIF_set_value(ecrStForm, descAttribute.c_str(), ""));
						}
						else
						{
							(AOM_UIF_ask_value(sourcePart, sourceAttr, &val));
							(AOM_UIF_set_value(ecrStForm, descAttribute.c_str(), val));
						}

						if (sourcePartTypeName) MEM_free(sourcePartTypeName);
						if (val) MEM_free(val);
					}

					double piece = 0,
						ednd = 0,
						tooling = 0,
						packing = 0,
						logistic = 0,
						totalPiece = 0;

					if (pieceRate != 1)
					{
						AOM_ask_value_double(sourcePart, "vf4_supplier_piece_cost_exw", &piece);
						AOM_ask_value_double(sourcePart, "vf4_Supplier_logistic_cost", &logistic);
						AOM_ask_value_double(sourcePart, "vf4_supplier_pkg_amount", &packing);

						if (piece > 0.000000001)
						{
							piece /= pieceRate;
							totalPiece += piece;
							AOM_set_value_double(ecrStForm, "vf4_piece_costs", piece);
							AOM_set_value_double(ecrStForm, "vf4_total_piece_cost", totalPiece);
						}

						if (logistic > 0.000000001)
						{
							logistic /= pieceRate;
							totalPiece += logistic;
							AOM_set_value_double(ecrStForm, "vf4_logistics_costs", logistic);
							AOM_set_value_double(ecrStForm, "vf4_total_piece_cost", totalPiece);
						}

						if (packing > 0.000000001)
						{
							packing /= pieceRate;
							totalPiece += packing;
							AOM_set_value_double(ecrStForm, "vf4_packing_costs", packing);
							AOM_set_value_double(ecrStForm, "vf4_total_piece_cost", totalPiece);
						}
					}

					if (toolingRate != 1)
					{
						AOM_ask_value_double(sourcePart, "vf4_tooling_order_2", &tooling);
						if (tooling > 0.000000001)
						{
							tooling /= toolingRate;
							AOM_set_value_double(ecrStForm, "vf4_tooling_costs", tooling);
						}
					}

					if (edndRate != 1)
					{
						AOM_ask_value_double(sourcePart, "vf4_eddorder_value_2", &ednd);
						if (ednd > 0.000000001)
						{
							ednd /= edndRate;
							AOM_set_value_double(ecrStForm, "vf4_ednd_costs", ednd);
						}
					}

					(AOM_save_with_extensions(ecrStForm));
					(AOM_refresh(ecrStForm, FALSE));
					POM_refresh_instances_any_class(1, &ecrStForm, POM_no_lock);
				}

				if (stPieceCostCurrency) MEM_free(stPieceCostCurrency);
				if (stToolingCostCurrency) MEM_free(stToolingCostCurrency);
				if (stEdndCostCurrency) MEM_free(stEdndCostCurrency);

			}
		}
	}

	if (ecrStForms) MEM_free(ecrStForms);

	TC_write_syslog("\n[VF] LEAVE %s\n", __FUNCTION__);
	return ITK_ok;
}

DLLAPI int VF_assign2Buyer(METHOD_message_t* msg, va_list args)
{
	int ifail = ITK_ok;
	char* error_text = NULL;
	tag_t tRev = msg->object_tag;
	tag_t tItem = NULL;
	CHECK_FAIL(ITEM_ask_item_of_rev(tRev, &tItem));
	TC_write_syslog("\n[VF_custom] Entering VF_assign2Buyer \n");

	if (tRev != NULLTAG)
	{
		char* pcStPrg = NULL;
		char* pcChangeIndex = NULL;
		char* pcPartNum = NULL;
		char* pcModuleGrp = NULL;
		CHECK_FAIL(AOM_ask_value_string(tRev, "vf4_platform_module", &pcStPrg));
		CHECK_FAIL(AOM_ask_value_string(tRev, "vf4_bom_change_index", &pcChangeIndex));
		CHECK_FAIL(AOM_ask_value_string(tRev, "vf4_bom_vfPartNumber", &pcPartNum));
		CHECK_FAIL(AOM_ask_value_string(tRev, "vf4_module_group_bom", &pcModuleGrp));

		/*skip COP part*/
		if (tc_strcmp(pcChangeIndex, "COP") == 0)
		{
			TC_write_syslog("\n [VF_custom] Ignore COP part: %s\n", pcPartNum);
			return 0;
		}

		/*skip when Module Group blank*/
		if ((pcModuleGrp == NULL) || (pcModuleGrp[0] == '\0')) {
			TC_write_syslog("\n [VF_custom] Ignore blank Module Group: %s\n", pcPartNum);
			return 0;
		}

		TC_write_syslog("\n [VF_custom] Processing part: %s - program: %s\n", pcPartNum, pcStPrg);
		/*get preference*/
		int	counter = 0;
		char** pcPrefValues = NULL;
		CHECK_FAIL(PREF_ask_char_values("VF_PROG_2_BUYER", &counter, &pcPrefValues));
		/*split pref values VFe32;EXTERIOR=admin.thanh;INTERIOR=admin.thanh;BODY_IN_WHITE=admin.thanh;DOORS_AND_CLOSURES=admin.thanh;CHASSIS=admin.thanh;POWERTRAIN=admin.thanh;ELECTRIC_ELECTRONICS=admin.thanh*/
		for (int iInd = 0; iInd < counter; iInd++)
		{
			std::vector<std::string> vecPreValue;
			vf_split(pcPrefValues[iInd], ";", vecPreValue);
			if (tc_strcmp(pcStPrg, vecPreValue.at(0).c_str()) == 0)
			{
				TC_write_syslog("\n [VF_custom] Valid preference for program: %s\n", pcStPrg);
				for (int i = 1; i < vecPreValue.size(); i++)
				{
					if (vecPreValue.at(i).find(pcModuleGrp) != std::string::npos)
					{
						TC_write_syslog("\n [VF_custom] Valid preference for module: %s\n", pcModuleGrp);
						std::vector<std::string> vecMod2Buyer;
						vf_split(vecPreValue.at(i), "=", vecMod2Buyer);
						std::string module = vecMod2Buyer.at(0);
						std::string groupMember = vecMod2Buyer.at(1);

						std::vector<std::string> grpRoleUserId;
						vf_split(groupMember, "/", grpRoleUserId);
						TC_write_syslog("\n [VF_custom] Group: %s\n", grpRoleUserId.at(0));
						TC_write_syslog("\n [VF_custom] Role: %s\n", grpRoleUserId.at(1));
						TC_write_syslog("\n [VF_custom] User ID: %s\n", grpRoleUserId.at(2));

						TC_write_syslog("\n [VF_custom] Assign part %s to buyer %s\n", pcPartNum, grpRoleUserId.at(2).c_str());


						//query to get cl group member

						tag_t tQuery = NULLTAG;
						CHECK_ITK(ifail, QRY_find2("__TNH_FindGroupMem", &tQuery));
						if (ifail != ITK_ok)
						{
							TC_write_syslog("[VF_custom]__%d__%s__Cannot find query [__TNH_FindGroupMem]: %d\n", __LINE__, __FUNCTION__, ifail);
							return ITK_ok;
						}

						const int nEntries = 4;
						char* entries[4] = { "Group", "Role", "Group Member Status", "Id" };
						char** values = nullptr;

						values = (char**)MEM_alloc(nEntries * sizeof(char*));

						values[0] = (char*)MEM_alloc(strlen(grpRoleUserId.at(0).c_str()) + 1);
						values[1] = (char*)MEM_alloc(strlen(grpRoleUserId.at(1).c_str()) + 1);
						values[2] = (char*)MEM_alloc(6);
						values[3] = (char*)MEM_alloc(strlen(grpRoleUserId.at(2).c_str()) + 1);
						tc_strcpy(values[0], grpRoleUserId.at(0).c_str());
						tc_strcpy(values[1], grpRoleUserId.at(1).c_str());
						tc_strcpy(values[2], "FALSE");
						tc_strcpy(values[3], grpRoleUserId.at(2).c_str());
						int clGrpMemCount = 0;
						tag_t * clGrpMem = nullptr;

						CHECK_ITK(ifail, QRY_execute(tQuery, nEntries, entries, values, &clGrpMemCount, &clGrpMem));
						if (ifail != ITK_ok) return ifail;

						if (clGrpMemCount == 0)
						{
							TC_write_syslog("[VF_custom]__%d__%s__Cannot get Buyer: %s\n", __LINE__, __FUNCTION__, grpRoleUserId.at(2).c_str());
							return ITK_ok;
						}
						TC_write_syslog("[VF_custom]__%d__%s__TESTING___VF_assign2Buyer_1111111111\n", __LINE__, __FUNCTION__);
						change_ownership(tRev, tItem, clGrpMem[0]);

						SAFE_MEM_free(clGrpMem);


						break;
					}
				}
				break;
			}
		}
		SAFE_MEM_free(pcStPrg);
		SAFE_MEM_free(pcChangeIndex);
		SAFE_MEM_free(pcPartNum);
		SAFE_MEM_free(pcModuleGrp);
		SAFE_MEM_free(pcPrefValues);
	}

	TC_write_syslog("\n[VF_custom] LEAVE %s\n", __FUNCTION__);
	return ITK_ok;
}


void change_ownership(tag_t sourcing_item_rev, tag_t sourcing_item, tag_t tCLGrpMem)
{
	TC_write_syslog("[VF_custom]__%d__%s__TESTING___change_ownership_1111111111\n", __LINE__, __FUNCTION__);
	if (sourcing_item_rev == NULLTAG)
	{
		TC_write_syslog("[VF_custom]__%d__%s__Could not change ownership to cl because sourcing_item_rev null\n", __LINE__, __FUNCTION__);
		return;
	}
	if (sourcing_item == NULLTAG)
	{
		TC_write_syslog("[VF_custom]__%d__%s__Could not change ownership to cl because sourcing_item null\n", __LINE__, __FUNCTION__);
		return;
	}

	if (tCLGrpMem == NULLTAG)
	{
		TC_write_syslog("[VF_custom]__%d__%s__Could not change ownership to cl because user_cl null\n", __LINE__, __FUNCTION__);
		return;
	}

	int ret_code = ITK_ok;
	tag_t	item_rev_class = NULLTAG;
	tag_t	sourcing_rev_class = NULLTAG;
	logical is_descendant = false;
	tag_t tUser = NULLTAG;
	tag_t tGroup = NULLTAG;

	CHECK_ITK(ret_code, POM_class_of_instance(sourcing_item_rev, &sourcing_rev_class));
	CHECK_ITK(ret_code, POM_class_id_of_class("ItemRevision", &item_rev_class));
	CHECK_ITK(ret_code, POM_is_descendant(item_rev_class, sourcing_rev_class, &is_descendant));

	CHECK_ITK(ret_code, SA_ask_groupmember_user(tCLGrpMem, &tUser));
	CHECK_ITK(ret_code, SA_ask_groupmember_group(tCLGrpMem, &tGroup));

	/*check for itemrevision*/
	if (is_descendant == true)
	{
		int     total_master_form = 0;
		tag_t	form_relation = NULL;
		tag_t* item_mform = nullptr;
		tag_t* item_rev_mform = nullptr;
		vector<tag_t> objs_tag;
		CHECK_ITK(ret_code, GRM_find_relation_type("IMAN_master_form", &form_relation));
		CHECK_ITK(ret_code, GRM_list_secondary_objects_only(sourcing_item, form_relation, &total_master_form, &item_mform));
		CHECK_ITK(ret_code, GRM_list_secondary_objects_only(sourcing_item_rev, form_relation, &total_master_form, &item_rev_mform));

		objs_tag.push_back(sourcing_item);
		objs_tag.push_back(sourcing_item_rev);
		/*objs_tag.push_back(item_mform[0]);
		objs_tag.push_back(item_rev_mform[0]);*/

		for (auto i = 0; i < objs_tag.size(); ++i)
		{
			logical is_granted = false;
			char* item_id = nullptr;
			CHECK_ITK(ret_code, ITEM_ask_id2(sourcing_item, &item_id));
			CHECK_ITK(ret_code, AM_check_privilege(objs_tag[i], "WRITE", &is_granted));
			if (!is_granted)
			{
				TC_write_syslog("[VF_custom]__%d__%s__No WRITE access: %s\n", __LINE__, __FUNCTION__, item_id);
				continue;
				//grant_access(objs_tag[i]);
			}
			CHECK_ITK(ret_code, AOM_refresh(objs_tag[i], true));
			CHECK_ITK(ret_code, AOM_set_ownership(objs_tag[i], tUser, tGroup));
			CHECK_ITK(ret_code, AOM_save_with_extensions(objs_tag[i]));
			CHECK_ITK(ret_code, AOM_refresh(objs_tag[i], false));
			if (ret_code != ITK_ok)
			{
				TC_write_syslog("[VF_custom]__%d__%s__Failed to change ownership for Item %s__%d\n", __LINE__, __FUNCTION__, item_id, ret_code);
			}
			SAFE_MEM_free(item_id);
		}
	}
}

