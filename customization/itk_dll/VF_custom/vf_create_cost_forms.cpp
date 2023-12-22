#include <stdio.h>
#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
#define  DEBUG_PRINT	
#include <fclasses/tc_stdlib.h>
#include <fclasses/tc_string.h>
#include <tccore/tctype.h>
#include <sa/tcvolume.h>
#include <tccore/item.h>
#include <tccore/item_msg.h>
#include <tc/emh.h>
#include <tc/emh_errors.h>
#include <tccore/custom.h>
#include <tccore/method.h>
#include <pom/pom/pom.h>
#include <tc/preferences.h>
#include <tc/prm.h>
#include <property/prop.h>
#include <property/prop_msg.h>
#include <sa/sa.h>
#include <ss/ss_const.h>
#include <ss/ss_types.h>
#include <textsrv/textserver.h>
#include <user_exits/user_exits.h>
#include <tccore/workspaceobject.h>
#include <ict/ict_userservice.h>
#include <unidefs.h>
#include <time.h>
#include <ps/gcr.h>
#include <tccore/grmtype.h>
#include <ae/dataset.h>
#include <ae/ae.h>
#include <tccore/aom.h>
#include <form/form.h>
#include <tccore/aom_prop.h>
#include <lov/lov.h>
#include <lov/lov_msg.h>
#include <qry/qry.h>
#include <property\nr.h>
#include <ae/dataset_msg.h>
#include <sa/am.h>
#include <tccore/tctype.h>
#include <fclasses/tc_ctype.h>
#include <sa/tcfile.h>
#include <tc/tc_macros.h>
#include <fclasses/tc_stdio.h>
#include <fclasses/tc_date.h>
#include <bom/bom_msg.h>
#include <bom/bom.h>
#include <tc/folder.h>
#include <tccore\uom.h>
#include <tccore\grm_msg.h>
#include <tccore\grm.h>

#include "VF_custom.h"

#define _CRT_SECURE_NO_WARNINGS
using namespace std;


tag_t createCostForm(const char *costFormTypeName, const char *formName);
void generateCostFormName(std::string postFix, const tag_t &costRev, std::string &costFormName);
void checkAndDeleteActualOrTargetCost(const tag_t &costRev, char *relationType);
void copyCostFormToCostRev(const tag_t &costForm, const tag_t &costRev, const char* relationTypeName);

DLLAPI int vf_create_cost_forms ( METHOD_message_t *message, va_list args)
{
	TC_write_syslog("\n[vf] Enter %s\n", __FUNCTION__);

	tag_t costRev = message->object_tag;

	std::string actualCostFormName, targetCostFormName, sapCostFormName;

	generateCostFormName("_ActualPieceCost", costRev, actualCostFormName);
	generateCostFormName("_TargetCost", costRev, targetCostFormName);
	generateCostFormName("_SAPCost", costRev, sapCostFormName);
	
	// create piece cost
	// create target cost
	// create sap cost form
	tag_t actualCostForm = createCostForm("VF4_PieceCostForm", actualCostFormName.c_str());
	tag_t targetCostForm = createCostForm("VF4_TargetCostForm", targetCostFormName.c_str());
	tag_t sapCostForm = createCostForm("VF4_SAPCostForm", sapCostFormName.c_str());
	

	// if piece & target cost is there, cut & delete them
	checkAndDeleteActualOrTargetCost(costRev, "VF4_SourcingCostFormRela");
	checkAndDeleteActualOrTargetCost(costRev, "VF4_SAPCostRelation");

	// copy to all above forms into the cost revision with coressponding releation	
	copyCostFormToCostRev(actualCostForm, costRev, "VF4_SourcingCostFormRela");
	copyCostFormToCostRev(targetCostForm, costRev, "VF4_SourcingCostFormRela");
	copyCostFormToCostRev(sapCostForm, costRev, "VF4_SAPCostRelation");

	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);
	return ITK_ok;
}

void generateCostFormName(std::string postFix, const tag_t &costRev, std::string &costFormName)
{
	TC_write_syslog("\n[vf] Enter %s\n", __FUNCTION__);
	char *costId = NULL;
	int retcode = ITK_ok;

	CHECK_ITK(retcode, AOM_ask_value_string(costRev, "item_id", &costId));

	costFormName.clear();
	costFormName.append(costId);
	costFormName.append(postFix);

	SAFE_MEM_free(costId);
	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);
}

tag_t createCostForm(const char *costFormTypeName, const char *formName)
{
	TC_write_syslog("\n[vf] Enter %s\n", __FUNCTION__);
	tag_t form = NULLTAG;
	tag_t formType = NULLTAG;
	int retcode = ITK_ok;

	CHECK_ITK(retcode, TCTYPE_find_type(costFormTypeName, "Form", &formType));

	tag_t createInput = NULLTAG;
	CHECK_ITK(retcode, TCTYPE_construct_create_input(formType, &createInput));
	CHECK_ITK(retcode, TCTYPE_set_create_display_value(createInput, "object_name", 1, &formName));
	CHECK_ITK(retcode, TCTYPE_create_object(createInput, &form));
	CHECK_ITK(retcode, AOM_save_with_extensions(form));
	CHECK_ITK(retcode, AOM_refresh(form, FALSE));
	if (retcode == ITK_ok) TC_write_syslog("\n[vf] Created cost form");

	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);
	return form;
}

void checkAndDeleteActualOrTargetCost(const tag_t &costRev, char *relationTypeName)
{
	TC_write_syslog("\n[vf] Enter %s\n", __FUNCTION__);
	int count = 0;
	GRM_relation_t *rels = NULL;
	tag_t relationType = NULLTAG;
	int retcode = ITK_ok;

	CHECK_ITK(retcode, TCTYPE_find_type(relationTypeName, relationTypeName, &relationType));
	CHECK_ITK(retcode, GRM_list_secondary_objects(costRev, relationType, &count, &rels));
	for (int i = 0; i < count; i++)
	{
		CHECK_ITK(retcode, GRM_delete_relation(rels[i].the_relation));
		if (retcode == ITK_ok) TC_write_syslog("\n[vf] Deleted rel");

		CHECK_ITK(retcode, AOM_delete(rels[i].secondary));
		if (retcode == ITK_ok) TC_write_syslog("\n[vf] Deleted form");
	}

	SAFE_MEM_free(rels);

	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);
}

void copyCostFormToCostRev(const tag_t &costForm, const tag_t &costRev, const char* relationTypeName)
{
	TC_write_syslog("\n[vf] Enter %s\n", __FUNCTION__);
	tag_t relationType = NULLTAG;
	tag_t rel = NULLTAG;
	int retcode = ITK_ok;

	CHECK_ITK(retcode, TCTYPE_find_type(relationTypeName, relationTypeName, &relationType));
	CHECK_ITK(retcode, GRM_create_relation(costRev, costForm, relationType, NULL, &rel));
	CHECK_ITK(retcode, AOM_save_without_extensions(rel));
	CHECK_ITK(retcode, AOM_refresh(rel, FALSE));
	if (retcode == ITK_ok) TC_write_syslog("\n[vf] Linked form to rev");

	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);
}

// Cost Form Validateion method added by TTL
extern DLLAPI int VF_validateTargetCostForm(METHOD_message_t* message, va_list args)
{
	TC_write_syslog ("\nIn VF_validateTargetCostForm -- added by TTL\n");
		
	int ifail = ITK_ok; 
	int iCount = 0;

	tag_t tSourcingCostFormRelType = NULLTAG;
	tag_t tCostItemRevision = NULLTAG;
	tag_t tDesignItemRevision = NULLTAG;
	
	tag_t tTargetCostForm = message->object_tag;

	Teamcenter::scoped_smptr<char> isCheckOut;
	AOM_ask_value_string(tTargetCostForm, "checked_out", &isCheckOut);
	if (tc_strstr(isCheckOut.getString(), "Y"))
	{
		//AE_reference_type_t reftype;
		TC_write_syslog("\n In Form Data Validation method ");

		Teamcenter::scoped_smptr<char> sTargetCostFormName;
		ifail = AOM_ask_value_string(tTargetCostForm, "object_string", &sTargetCostFormName);
		if (ifail != ITK_ok)
		{
			TC_write_syslog("\n Error Code : %d", ifail);
		}
		else
		{
			TC_write_syslog("\nTarget Cost Form Object name : %s", sTargetCostFormName.getString());
			TC_write_syslog("\nTarget Cost Form Object name : %s", sTargetCostFormName.getString());
		}

		ifail = GRM_find_relation_type("VF4_SourcingCostFormRela", &tSourcingCostFormRelType);
		if (ifail != ITK_ok)
		{
			TC_write_syslog("\n VF4_SourcingCostFormRela Relation not found");
			TC_write_syslog("\n VF4_SourcingCostFormRela Relation not found");
		}
		else
		{
			Teamcenter::scoped_smptr<tag_t> tPrimaryObjs;
			ifail = GRM_list_primary_objects_only(tTargetCostForm, tSourcingCostFormRelType, &iCount, &tPrimaryObjs);
			if (ifail != ITK_ok)
			{
				TC_write_syslog("\n GRM_list_primary_objects_only  failed");
				TC_write_syslog("\n GRM_list_primary_objects_only  failed");
			}
			else
			{
				TC_write_syslog("\n primary count with save relation : %d", iCount);
				TC_write_syslog("\n primary count with save relation : %d", iCount);

				if (iCount > 0)
				{
					for (int k = 0; k < iCount; k++)
					{
						Teamcenter::scoped_smptr<char> sObjectType;

						ifail = AOM_ask_value_string(tPrimaryObjs[k], "object_type", &sObjectType);
						if (ifail != ITK_ok)
						{
							TC_write_syslog("\n Error Code : %d ", ifail);
							TC_write_syslog("\n Error Code : %d ", ifail);
						}
						else
						{
							TC_write_syslog("\n Primary Object type is : %s  ", sObjectType.getString());
							TC_write_syslog("\n Primary Object type is : %s  ", sObjectType.getString());

							if (tc_strcmp(sObjectType.getString(), "VF4_CostRevision") == 0)
							{
								tCostItemRevision = tPrimaryObjs[k];
							}
						}
					}

					if (tCostItemRevision != NULLTAG)
					{
						tag_t tCostingRefRelation = NULLTAG;

						ifail = GRM_find_relation_type("VF4_Costing_Reference", &tCostingRefRelation);
						if (ifail != ITK_ok)
						{
							TC_write_syslog("\n VF4_Costing_Reference Relation not found");
							TC_write_syslog("\n VF4_Costing_Reference Relation not found");
						}
						else
						{
							int iDesignRevCount = 0;
							Teamcenter::scoped_smptr<tag_t> tDesignRevPrimaryObjs;
							ifail = GRM_list_primary_objects_only(tCostItemRevision, tCostingRefRelation, &iDesignRevCount, &tDesignRevPrimaryObjs);
							if (ifail != ITK_ok)
							{
								TC_write_syslog("\n GRM_list_primary_objects_only  failed");
								TC_write_syslog("\n GRM_list_primary_objects_only  failed");
							}
							else
							{
								TC_write_syslog("\n primary count with save relation : %d", iDesignRevCount);
								TC_write_syslog("\n primary count with save relation : %d", iDesignRevCount);

								if (iDesignRevCount == 0)
								{
									ifail = GRM_find_relation_type("VF4_Costing_Reference2", &tCostingRefRelation);
									ifail = GRM_list_primary_objects_only(tCostItemRevision, tCostingRefRelation, &iDesignRevCount, &tDesignRevPrimaryObjs);
								}

								if (iDesignRevCount > 0)
								{
									for (int k = 0; k < iDesignRevCount; k++)
									{
										Teamcenter::scoped_smptr<char> sDesignRevObjectType;

										ifail = AOM_ask_value_string(tDesignRevPrimaryObjs[k], "object_type", &sDesignRevObjectType);
										if (ifail != ITK_ok)
										{
											TC_write_syslog("\n Error Code : %d ", ifail);
											TC_write_syslog("\n Error Code : %d ", ifail);
										}
										else
										{
											TC_write_syslog("\n Primary Object type is : %s  ", sDesignRevObjectType.getString());
											TC_write_syslog("\n Primary Object type is : %s  ", sDesignRevObjectType.getString());

											if (tc_strcmp(sDesignRevObjectType.getString(), "VF4_DesignRevision") == 0)
											{
												tDesignItemRevision = tDesignRevPrimaryObjs[k];
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if (tDesignItemRevision != NULLTAG)
		{
			logical bPieceCostInParnt = false;
			logical bToolCostInParnt = false;
			logical bEDDCostInParnt = false;
			Teamcenter::scoped_smptr<char> sSupplierType;
			Teamcenter::scoped_smptr<char> makeOrBuy;
			Teamcenter::scoped_smptr<char> sPartCategory;

			double dPieceCost = 0.0l;
			double dToolCost = 0.0l;
			double dEDDCost = 0.0l;
			tag_t tDesignItem = NULL;
			ifail = ITEM_ask_item_of_rev(tDesignItemRevision, &tDesignItem);
			ifail = AOM_ask_value_string(tDesignItem, "vf4_supplier_type", &sSupplierType);
			ifail = AOM_ask_value_string(tDesignItem, "vf4_item_make_buy", &makeOrBuy);
			ifail = AOM_ask_value_string(tDesignItem, "vf4_part_category", &sPartCategory);
			ifail = AOM_ask_value_logical(tTargetCostForm, "vf4_isPieceCostInParentPart", &bPieceCostInParnt);
			ifail = AOM_ask_value_logical(tTargetCostForm, "vf4_isToolCostInParentPart", &bToolCostInParnt);
			ifail = AOM_ask_value_logical(tTargetCostForm, "vf4_isEDDCostInParentPart", &bEDDCostInParnt);
			ifail = AOM_ask_value_double(tTargetCostForm, "vf4_piece_cost_value_target", &dPieceCost);
			ifail = AOM_ask_value_double(tTargetCostForm, "vf4_tooling_invest_target", &dToolCost);
			ifail = AOM_ask_value_double(tTargetCostForm, "vf4_ednd_cost_value_target", &dEDDCost);

			if (tc_strcmp(sSupplierType.getString(), "B2P") == 0 && tc_strcmp(makeOrBuy.getString(), "Buy") == 0)
			{
				if (!bPieceCostInParnt && dPieceCost == 0)
				{
					ifail = EMH_store_error_s1(EMH_severity_error, TargetCostForm_Validation_Error, "Piece Cost Value must be filled");
					TC_write_syslog("\nPiece Cost Value should be filled & Tool, EDnD Cost Values are not required");
					return TargetCostForm_Validation_Error;
				}
			}
			else if (tc_strcmp(sSupplierType.getString(), "FSS") == 0 && tc_strcmp(makeOrBuy.getString(), "Buy") == 0)
			{
				if ((!bPieceCostInParnt && !bToolCostInParnt && !bEDDCostInParnt) && (dPieceCost == 0 || dToolCost == 0 || dEDDCost == 0))
				{
					ifail = EMH_store_error_s1(EMH_severity_error, TargetCostForm_Validation_Error, "Piece, Tooling & EDnD Cost Value Target must be filled");
					TC_write_syslog("\n Piece, Tooling & EDnD Cost Value Target must be filled");
					return TargetCostForm_Validation_Error;
				}
				else if (bEDDCostInParnt && (dPieceCost == 0 || dToolCost == 0))
				{
					ifail = EMH_store_error_s1(EMH_severity_error, TargetCostForm_Validation_Error, "Piece & Tool Cost Value Target must be filled");
					TC_write_syslog("\nPiece & Tool Cost Value Target must be filled");
					return TargetCostForm_Validation_Error;
				}
				else if (bToolCostInParnt && (dPieceCost == 0 || dEDDCost == 0))
				{
					ifail = EMH_store_error_s1(EMH_severity_error, TargetCostForm_Validation_Error, "Piece & EDnD Cost Value Target must be filled");
					TC_write_syslog("\nPiece & EDnD Cost Value Target must be filled");
					return TargetCostForm_Validation_Error;
				}
				else if (bPieceCostInParnt && (dToolCost == 0 || dEDDCost == 0))
				{
					ifail = EMH_store_error_s1(EMH_severity_error, TargetCostForm_Validation_Error, "Tool & EDnD Cost Value Target must be filled");
					TC_write_syslog("\nTool & EDnD Cost Value Target must be filled");
					return TargetCostForm_Validation_Error;
				}
			}

			if (tc_strcmp(sPartCategory.getString(), "OTS") == 0 && tc_strcmp(makeOrBuy.getString(), "Buy") == 0)
			{
				if (!bPieceCostInParnt && (dPieceCost == 0))
				{
					ifail = EMH_store_error_s1(EMH_severity_error, TargetCostForm_Validation_Error, "Piece Cost Value Target must be filled for OTS part");
					TC_write_syslog("\nPiece Cost Value Target must be filled");
					return TargetCostForm_Validation_Error;
				}
			}
		}
	}

	

	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);
	return ifail;
}
