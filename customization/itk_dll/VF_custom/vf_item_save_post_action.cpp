#include "VF_custom.h"
#include "map"
#include <base_utils/TcResultStatus.hxx>
#include <base_utils/ScopedSmPtr.hxx>
#include <base_utils/ScopedPtr.hxx>

using namespace std;

extern DLLAPI int vf_item_save_post_action(METHOD_message_t *msg, va_list args)
{
	TC_write_syslog("[vf]ENTER %s", __FUNCTION__);
	tag_t item = msg->object_tag;

	map<string, string> makeBuyAndMatTypeMap;
	makeBuyAndMatTypeMap["Make"] = "ZSFG";
	makeBuyAndMatTypeMap["make"] = "ZSFG";
	makeBuyAndMatTypeMap["MAKE"] = "ZSFG";
	makeBuyAndMatTypeMap["Buy"] = "ZRAW";
	makeBuyAndMatTypeMap["buy"] = "ZRAW";
	makeBuyAndMatTypeMap["BUY"] = "ZRAW";
	makeBuyAndMatTypeMap["Purchase"] = "ZRAW";
	makeBuyAndMatTypeMap["Purchased"] = "ZRAW";
	makeBuyAndMatTypeMap["Sell"] = "ZSEL";

	char *makeBuyVal = NULL;
	char *materialType = NULL;
	int retcode = ITK_ok;
	Teamcenter::scoped_smptr<char> swPartType;
	tag_t designTypeTag = NULLTAG;
	tag_t cellTypeTag = NULLTAG;
	tag_t vesTypeTag = NULLTAG;
	logical isDesign = false;
	logical isCellDesign = false;
	logical isVESDesign = false;

	if (item == NULLTAG) return ITK_ok;

	retcode=AOM_load(item);
	//TC_write_syslog("\n[vf]AOM_refresh=%d",retcode);

	retcode=AOM_ask_value_string(item, "vf4_item_make_buy", &makeBuyVal);
	retcode=AOM_ask_value_string(item, "vf4_itm_material_type", &materialType);
	TCTYPE_find_type("Design", "Design", &designTypeTag);
	TCTYPE_find_type("VF4_Cell_Design", "VF4_Cell_Design", &cellTypeTag);
	TCTYPE_find_type("VF4_BP_Design", "VF4_BP_Design", &vesTypeTag);
	AOM_is_type_of(item, designTypeTag, &isDesign);
	AOM_is_type_of(item, cellTypeTag, &isCellDesign);
	AOM_is_type_of(item, vesTypeTag, &isVESDesign);

	//binhtt28 Edit

	Teamcenter::scoped_smptr<char> itemID;
	retcode = AOM_ask_value_string(item, "item_id", &itemID);
	std::string item_name = itemID.getString();
	  
	if (tc_strcmp(materialType, "ZLIC") != 0 && tc_strcmp(materialType, "ZDUM") != 0)
	{
		if (tc_strlen(makeBuyVal) > 2)
		{
			const char *matTypeVal = makeBuyAndMatTypeMap[makeBuyVal].c_str();
			retcode=AOM_set_value_string(item, "vf4_itm_material_type", matTypeVal);
		}
 
		if (isDesign)
		{
			retcode = AOM_ask_value_string(item, "vf4_software_part_type", &swPartType);
			
			if (tc_strlen(swPartType.getString()) > 0)
			{
				if (tc_strcmp(swPartType.getString(), "In House") == 0)
				{
					if (tc_strcmp(makeBuyVal, "Make") == 0)
					{
						retcode = AOM_set_value_string(item, "vf4_itm_material_type", "ZSFG");
					}
					else
					{
						retcode = AOM_set_value_string(item, "vf4_itm_material_type", "ZSFG");
						retcode = AOM_set_value_string(item, "vf4_item_make_buy", "Make");
					}
				}
				else if (tc_strcmp(swPartType.getString(), "PURCHASE") == 0)
				{
					if (tc_strcmp(makeBuyVal, "Buy") == 0)
					{
						retcode = AOM_set_value_string(item, "vf4_itm_material_type", "ZRAW");
					}
					else
					{
						retcode = AOM_set_value_string(item, "vf4_itm_material_type", "ZRAW");
						retcode = AOM_set_value_string(item, "vf4_item_make_buy", "Buy");
					}
				}
				else
				{
					retcode = AOM_set_value_string(item, "vf4_itm_material_type", "ZSOF");
					/*if (tc_strcmp(makeBuyVal, "Make") == 0)
					{
						retcode = AOM_set_value_string(item, "vf4_itm_material_type", "ZSOF");
					}
					else
					{
						retcode = AOM_set_value_string(item, "vf4_itm_material_type", "ZSOF");
						retcode = AOM_set_value_string(item, "vf4_item_make_buy", "Make");
					}*/
				}
			}
			else
			{
				/*Teamcenter::scoped_smptr<char> itemID;
				retcode = AOM_ask_value_string(item, "item_id", &itemID);
				if (tc_strstr(itemID.getString(), "SOW") || tc_strstr(itemID.getString(), "sow"))
				{
					EMH_store_error_s1(EMH_severity_warning, SW_PART_TYPE_ERROR, "Please fill \"Software Part Type\" for part number starting with SOW!");
				}*/
			}

			if (isCellDesign || isVESDesign)
			{
				logical isCellFinishedGood = false;
				retcode = AOM_ask_value_logical(item, "vf4_is_finish_goods", &isCellFinishedGood);
				if (isCellFinishedGood)
				{
					retcode = AOM_set_value_string(item, "vf4_item_make_buy", "Make");
					retcode = AOM_set_value_string(item, "vf4_itm_material_type", "ZFG");
				}
			}
			

		}
	}

	if (item_name.find("SOW", 0) != -1)
	{
		retcode = AOM_set_value_string(item, "vf4_itm_material_type", "ZSOF");
	}
	else if (item_name.find("LIC", 0) != -1) {
		retcode = AOM_set_value_string(item, "vf4_itm_material_type", "ZLIC");
	}

	retcode = AOM_save_without_extensions(item);

	if (makeBuyVal) MEM_free(makeBuyVal);
	if (materialType) MEM_free(materialType);

	TC_write_syslog("\n[vf]LEAVE %s\n", __FUNCTION__);
	return ITK_ok;
}
extern DLLAPI int vf_item_save_post_action__UOM(METHOD_message_t *msg, va_list args)
{
	TC_write_syslog("[vf]ENTER %s", __FUNCTION__);
	int ifail = ITK_ok;
	char *error_text = NULL;
	TCTYPE_save_operation_context_t saveContext;
	TCTYPE_ask_save_operation_context(&saveContext);
	if ((saveContext == TCTYPE_save_on_create))
	{
		int n_values = 0;  
		int prefCount = 0;
		char **values = NULL;
		char *objectType = NULL;
		tag_t uomEATag = NULLTAG;	
		tag_t itemUOM = NULLTAG;
		
		tag_t item = msg->object_tag;
		
		CHECK_FAIL(UOM_find_by_symbol("EA", &uomEATag));
		CHECK_FAIL(PREF_ask_value_count("VF_SET_DEFAULT_UOM", &prefCount));
		if(prefCount == 0)
		{
			TC_write_syslog("[vf]Preference [%s] not found in Database","VF_SET_DEFAULT_UOM");
			return ITK_ok;
		}
		CHECK_FAIL(AOM_ask_value_tag(item, "uom_tag", &itemUOM));
		if(itemUOM != NULLTAG)
		{
			TC_write_syslog("[vf]UOM is not empty hence skipping");
			return ITK_ok;
		}
		CHECK_FAIL(AOM_ask_value_string(item, "object_type", &objectType));
		CHECK_FAIL(PREF_ask_char_values("VF_SET_DEFAULT_UOM", &n_values, &values));
		if(n_values > 0)
		{
			//CHECK_FAIL(AOM_refresh(item, TRUE));
			CHECK_FAIL(AOM_assign_tag(item, "uom_tag", uomEATag));
			CHECK_FAIL(AOM_save_without_extensions(item));
			//CHECK_FAIL(AOM_refresh(item, FALSE));
			for(int i = 0; i < n_values; i++)
			{
				std::string objType;
				std::string prefUOM;
				int t = 0;
				tag_t prefUOMTag = NULLTAG;
				char *token = strtok(values[i], "=");
				
				while (token != NULL)
				{
					if(t==0)
					{
						objType = token;
					}
					else
					{
						prefUOM = token;
					}
					token = strtok(NULL, "=");
					t++;
				}
				if(tc_strcmp(objectType,objType.c_str()) == 0)
				{
					CHECK_FAIL(UOM_find_by_symbol(prefUOM.c_str(), &prefUOMTag));
					if(prefUOMTag!= NULLTAG)
					{
						//CHECK_FAIL(AOM_refresh(item, TRUE));
						CHECK_FAIL(AOM_assign_tag(item, "uom_tag", prefUOMTag));
						CHECK_FAIL(AOM_save_without_extensions(item));
						//CHECK_FAIL(AOM_refresh(item, FALSE));
						break;
					}
					else
					{
						//CHECK_FAIL(AOM_refresh(item, TRUE));
						CHECK_FAIL(AOM_assign_tag(item, "uom_tag", NULLTAG));
						CHECK_FAIL(AOM_save_without_extensions(item));
						//CHECK_FAIL(AOM_refresh(item, FALSE));
						CHECK_FAIL(EMH_store_error_s1(EMH_severity_warning,UOM_Validation_Error,"UOM does not exist in Database"));
					}
				}
			}
		}
	}
	TC_write_syslog("[vf]LEAVE %s", __FUNCTION__);
	return ITK_ok;
}

extern DLLAPI int vf_is_frozen_getter(METHOD_message_t *msg, va_list args)
{
	(void) va_arg(args, tag_t);
	char** value = va_arg(args, char**);

	*value = (char*)MEM_alloc(10*sizeof(char*));
	tc_strcpy(*value, "is_frozen_string");
	return ITK_ok;
}

extern DLLAPI int vf_original_part_number_is_modifable(METHOD_message_t *msg, va_list args)
{
	(void) va_arg(args, tag_t);
	logical *isModifiable = (logical*) va_arg(args, logical*);
	

	char *isTriggeredByDialog = NULL;
	char *gmStrVal = NULL;
	char *originalPartNumber = NULL;
	tag_t gm  = NULL;
	tag_t obj = msg->object_tag;
	SA_ask_current_groupmember(&gm);

	//printf("\n gm is %d", gm);
	//printf("\n obj is %d", obj);

	if (gm && obj)
	{
		AOM_ask_value_string(obj, "vf4_hw_version", &isTriggeredByDialog);
		AOM_ask_value_string(gm, "object_string", &gmStrVal);
		AOM_ask_value_string(obj, "vf4_orginal_part_number", &originalPartNumber);

		//printf("\n obj is %s", isTriggeredByDialog);
		//printf("\n obj is %s", gmStrVal);
		//printf("\n obj is %s", originalPartNumber);
	}
	
	if (isTriggeredByDialog && gmStrVal && originalPartNumber)
	{
		*isModifiable = (tc_strstr(gmStrVal, "dba/DBA") || (tc_strcasecmp(isTriggeredByDialog, "true") == 0));
	}
	
	SAFE_MEM_free(isTriggeredByDialog);
	SAFE_MEM_free(originalPartNumber);
	SAFE_MEM_free(gmStrVal);

	return  ITK_ok;
}
