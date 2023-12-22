#include "VF_custom.h"
#include "property\prop_msg.h";
#include "sub_mgr\tceventmgr.h"
char* error_text;

auto split(const std::string& s, const std::string& delimiter)->vector<string>;

int updateItemCode(tag_t tItem, tag_t tMasterForm, char* pcRevID, tag_t tRevMaster, string sznewItemId)
{
	int ifail = ITK_ok;

	/*setting new item id*/
	CHECK_FAIL(AOM_lock(tItem));
	CHECK_FAIL(ITEM_set_id(tItem, sznewItemId.c_str()));
	CHECK_FAIL(AOM_save(tItem));

	/*setting item master name*/
	CHECK_FAIL(AOM_lock(tMasterForm));
	CHECK_FAIL(AOM_set_value_string(tMasterForm, "object_name", sznewItemId.c_str()));
	CHECK_FAIL(AOM_save(tMasterForm));

	sznewItemId.append("/");
	sznewItemId.append(pcRevID);

	/*setting item rev master name*/
	CHECK_FAIL(AOM_lock(tRevMaster));
	CHECK_FAIL(AOM_set_value_string(tRevMaster, "object_name", sznewItemId.c_str()));
	CHECK_FAIL(AOM_save(tRevMaster));

	return ifail;
}

string subString(const char* origin, int begin, int length) {
	char* res = new char[length + 1];
	for (int i = 0; i < length; i++)
		res[i] = *(origin + begin + i);
	res[length] = 0;
	return res;
}

/*Move selected item to user newstuff folder*/
void moveToNewstuff(tag_t tModItem)
{
	int			ifail = ITK_ok;
	tag_t		tUser = NULLTAG;
	tag_t		tFolder = NULLTAG;
	tag_t		tAttr = NULLTAG;

	logical		lNull = FALSE;
	logical		lEmpty = FALSE;

	/*to get user tag*/
	CHECK_FAIL(POM_attr_id_of_attr("owning_user", "POM_application_object", &tAttr));
	if (tAttr != NULLTAG)
	{
		CHECK_FAIL(POM_refresh_instances(1, &tModItem, NULLTAG, POM_modify_lock));
		if (ifail == ITK_ok)
		{
			CHECK_FAIL(POM_ask_attr_tag(tModItem, tAttr, &tUser, &lNull, &lEmpty));
			if (tUser != NULLTAG)
			{
				/*to get newstuff folder*/
				CHECK_FAIL(POM_save_instances(1, &tModItem, true));
				CHECK_FAIL(SA_ask_user_newstuff_folder(tUser, &tFolder));
				if (tFolder != NULLTAG)
				{
					/*insert item to newstuff*/
					CHECK_FAIL(AOM_refresh(tFolder, TRUE));
					CHECK_FAIL(FL_insert(tFolder, tModItem, 999));
					CHECK_FAIL(AOM_save(tFolder));
					CHECK_FAIL(AOM_refresh(tFolder, FALSE));
				}
			}
		}
	}
}

/*to get next squential module number based on creation date*/
string getNextMod(string szModPrf)
{
	int			ifail = ITK_ok;
	char* pcEntries[] = { "Item ID", "Type" };
	char** pcValues = NULL;
	int			n_entries = 2;
	int			iNItem = 0;
	char* pcKeys[] = { "creation_date" };
	int			pcOrder[] = { 2 };
	char* pcOldID = NULL;
	string		szSearchType = "Scooter Module";
	string		szSearchID = "";
	tag_t		tQuery = NULLTAG;
	tag_t* ptItem = NULL;
	string		szNewID = "";

	szSearchID.assign(szModPrf);
	szSearchID.append("*");

	/*put input values to pcValues*/
	pcValues = (char**)MEM_alloc(n_entries * sizeof(char*));
	for (int ii = 0; ii < n_entries; ii++)
	{
		if (tc_strcmp(pcEntries[ii], "Item ID") == 0)
		{
			pcValues[ii] = (char*)MEM_alloc(tc_strlen(szSearchID.c_str()) + 1);
			tc_strcpy(pcValues[ii], szSearchID.c_str());
		}
		if (tc_strcmp(pcEntries[ii], "Type") == 0)
		{
			pcValues[ii] = (char*)MEM_alloc(tc_strlen(szSearchType.c_str()) + 1);
			tc_strcpy(pcValues[ii], szSearchType.c_str());
		}
	}

	/*using Item query and sort by creation date*/
	CHECK_FAIL(QRY_find2("Item...", &tQuery));
	CHECK_FAIL(QRY_execute_with_sort(tQuery, 2, pcEntries, pcValues, 1, pcKeys, pcOrder, &iNItem, &ptItem));
	if (ifail == ITK_ok && iNItem > 0)
	{
		string			szOldID = "";
		int				iTemp = 0;

		/*get item id*/
		CHECK_FAIL(AOM_ask_value_string(ptItem[0], "item_id", &pcOldID));
		if (pcOldID != NULL)
		{
			szOldID.assign(pcOldID);

			/*truncate first 3 alphabets*/
			szOldID = szOldID.substr(szOldID.length() - 4);

			/*convert to integer*/
			iTemp = stoi(szOldID);
			if (iTemp < 9999)
			{
				/*increment item id*/
				string		szTempID = "";
				iTemp++;

				if (to_string(iTemp).length() == 1)
				{
					szTempID.assign("000");
					szTempID.append(to_string(iTemp));
				}
				else if (to_string(iTemp).length() == 2)
				{
					szTempID.assign("00");
					szTempID.append(to_string(iTemp));
				}
				else if (to_string(iTemp).length() == 3)
				{
					szTempID.assign("0");
					szTempID.append(to_string(iTemp));
				}
				else
				{
					szTempID.assign(to_string(iTemp));
				}
				/*get module id with module name and squential number*/
				szNewID.assign(szModPrf);
				szNewID.append(szTempID);
			}
		}
	}
	/*if module is not existing then create item id with 0001*/
	else
	{
		szNewID.assign(szModPrf);
		szNewID.append("0001");
	}

	return szNewID;
}

/*create module with new ID and name and set UOM*/
int createMod(int iModules, string szNewID, string pcModNames, tag_t* tModItem, string esVehLine)
{
	int			ifail = ITK_ok;
	int			iNumItem = iModules;
	tag_t		tModType = NULLTAG;
	tag_t		tNewstuff = NULLTAG;
	tag_t		tCreatInputItem = NULLTAG;
	tag_t		tAttr = NULLTAG;
	tag_t		tUom = NULLTAG;

	/*get scooter module type*/
	CHECK_FAIL(TCTYPE_find_type("VF4_scooter_mod", "VF4_scooter_mod", &tModType));
	if (tModType != NULLTAG)
	{
		CHECK_FAIL(TCTYPE_construct_create_input(tModType, &tCreatInputItem));
		if (tCreatInputItem != NULLTAG)
		{
			/*set item id, name and description*/
			CHECK_FAIL(AOM_set_value_string(tCreatInputItem, "object_name", pcModNames.c_str()));
			CHECK_FAIL(AOM_set_value_string(tCreatInputItem, "object_desc", pcModNames.c_str()));
			CHECK_FAIL(AOM_set_value_string(tCreatInputItem, "item_id", szNewID.c_str()));
			CHECK_FAIL(AOM_set_value_string(tCreatInputItem, "vf4_es_model_veh_line", esVehLine.c_str()));

			if (ifail == ITK_ok)
			{
				/*create item*/
				CHECK_FAIL(TCTYPE_create_object(tCreatInputItem, tModItem));
				if (tModItem != NULLTAG)
				{
					CHECK_FAIL(AOM_save(*tModItem));
					if (ifail == ITK_ok)
					{
						CHECK_FAIL(POM_attr_id_of_attr("uom_tag", "Item", &tAttr));
						if (tAttr != NULLTAG)
						{
							CHECK_FAIL(UOM_find_by_symbol("SET", &tUom));
							if (tUom != NULLTAG)
							{
								/*update uom as "SET"*/
								CHECK_FAIL(POM_refresh_instances(1, tModItem, NULLTAG, POM_modify_lock));
								if (ifail == ITK_ok)
								{
									CHECK_FAIL(POM_set_attr_tag(1, tModItem, tAttr, tUom));
									if (ifail == ITK_ok)
									{
										CHECK_FAIL(POM_save_instances(1, tModItem, true));
									}
								}
							}
						}
					}
				}
			}
		}
	}


	/*move created item to newstuff*/
	if (tModItem != NULLTAG)
	{
		moveToNewstuff(*tModItem);
	}

	return ifail;
}

int createObject(int iModules, string objectID, string objectName, string objectDesc, string modelCode, string moduleName, string objectType, tag_t* objectItem)
{
	int	ifail = ITK_ok;
	int	iNumItem = iModules;
	tag_t tObjectType = NULLTAG;
	tag_t tCreatInputItem = NULLTAG;
	tag_t tNewstuff = NULLTAG;
	tag_t tAttr = NULLTAG;
	tag_t tUom = NULLTAG;

	/*get item from object type*/
	CHECK_FAIL(TCTYPE_find_type(objectType.c_str(), objectType.c_str(), &tObjectType));
	if (tObjectType != NULLTAG)
	{
		CHECK_FAIL(TCTYPE_construct_create_input(tObjectType, &tCreatInputItem));
		if (tCreatInputItem != NULLTAG)
		{
			/*set item id, name and description*/
			CHECK_FAIL(AOM_set_value_string(tCreatInputItem, "item_id", objectID.c_str()));
			CHECK_FAIL(AOM_set_value_string(tCreatInputItem, "object_name", objectName.c_str()));
			CHECK_FAIL(AOM_set_value_string(tCreatInputItem, "object_desc", objectDesc.c_str()));
			CHECK_FAIL(AOM_set_value_string(tCreatInputItem, "vf3_veh_category", "AUTOMOBILE"));
			CHECK_FAIL(AOM_set_value_string(tCreatInputItem, "vf3_doc_type", "DVPR"));
			CHECK_FAIL(AOM_set_value_string(tCreatInputItem, "vf3_model_code", modelCode.c_str()));
			CHECK_FAIL(AOM_set_value_string(tCreatInputItem, "vf3_module_name", moduleName.c_str()));

			if (ifail == ITK_ok)
			{
				/*create item*/
				CHECK_FAIL(TCTYPE_create_object(tCreatInputItem, objectItem));
				if (objectItem != NULLTAG)
				{
					CHECK_FAIL(AOM_save(*objectItem));
				}
			}
		}
	}

	/*move created item to newstuff*/
	if (objectItem != NULLTAG)
	{
		moveToNewstuff(*objectItem);
	}

	return ifail;
}

/*add itemrevision to bom line of scooter vehicle*/
int addBOMLine(tag_t tModItem, tag_t tWindow, tag_t tTopLine, string pcFindNo)
{
	int			ifail = ITK_ok;
	int			bl_find_num = 0;
	int			bl_quantity = 0;
	tag_t		tModRev = NULLTAG;
	tag_t		tChildItem = NULLTAG;

	/*get itemrevision*/
	CHECK_FAIL(ITEM_ask_latest_rev(tModItem, &tModRev));
	CHECK_FAIL(BOM_line_look_up_attribute("bl_sequence_no", &bl_find_num));
	CHECK_FAIL(BOM_line_look_up_attribute("bl_quantity", &bl_quantity));
	if (tModRev != NULLTAG)
	{
		CHECK_FAIL(AOM_lock(tModItem));
		if (ifail == ITK_ok)
		{
			/*add to bomline*/
			CHECK_FAIL(BOM_line_add(tTopLine, tModItem, tModRev, NULLTAG, &tChildItem));
			CHECK_FAIL(BOM_line_set_attribute_string(tChildItem, bl_find_num, pcFindNo.c_str()));
			CHECK_FAIL(BOM_line_set_attribute_string(tChildItem, bl_quantity, "1"));

			/*save window and item*/
			CHECK_FAIL(BOM_save_window(tWindow));
			CHECK_FAIL(AOM_save(tModItem));
		}
	}

	return ifail;
}


DLLAPI int VF_updateItemIDMEScooter(METHOD_message_t* message, va_list args)
{
	int		ifail = ITK_ok;
	char* pcItemID = NULL;
	char* pcItemName = NULL;
	char* pcItemType = NULL;
	char* pcRevID = NULL;
	char* pcMatLov = NULL;
	char* pcClrLov = NULL;
	tag_t* tItem = NULLTAG;
	tag_t* tRev = NULLTAG;
	tag_t	tMaster = NULLTAG;
	tag_t	tRevMaster = NULLTAG;
	string	szNewID = "";

	pcItemID = va_arg(args, char*);
	pcItemName = va_arg(args, char*);
	pcItemType = va_arg(args, char*);
	pcRevID = va_arg(args, char*);
	tItem = va_arg(args, tag_t*);
	tRev = va_arg(args, tag_t*);
	tMaster = va_arg(args, tag_t);
	tRevMaster = va_arg(args, tag_t);

	TC_write_syslog("\n Entering VF_updateItemIDMEScooter \n");

	/* getting Material and Color code LOV */
	CHECK_FAIL(AOM_ask_value_string(*tItem, "vf3_mat_group", &pcMatLov));
	CHECK_FAIL(AOM_ask_value_string(*tItem, "vf3_color_code", &pcClrLov));

	/*for ME Scooter part*/
	if (tc_strcmp(pcItemType, "VF3_me_scooter") == 0 && tc_strcmp(pcMatLov, "") != NULL)
	{
		/*if user manually enter item id contianing ES...skip*/
		if (tc_strstr(pcItemID, "ES") == 0)
		{
			/*if color code is blank*/
			if (tc_strlen(pcClrLov) == NULL || tc_strcmp(pcClrLov, "") == NULL)
			{
				/*add material group as prefix to item id*/
				szNewID.assign(pcMatLov);
				szNewID.append(pcItemID);

				/*to update item id, master form and rev master form*/
				CHECK_FAIL(updateItemCode(*tItem, tMaster, pcRevID, tRevMaster, szNewID));
			}
			else
			{
				/*add material group as prefix to item id and color code as suffix*/
				szNewID.assign(pcMatLov);
				szNewID.append(pcItemID);
				szNewID.append(pcClrLov);

				/*to update item id, master form and rev master form*/
				CHECK_FAIL(updateItemCode(*tItem, tMaster, pcRevID, tRevMaster, szNewID));
			}
		}
	}

	/*memory free*/
	if (pcMatLov != NULL)
		MEM_free(pcMatLov);
	if (pcClrLov != NULL)
		MEM_free(pcClrLov);

	TC_write_syslog("\n Exiting VF_updateItemIDMEScooter \n");

	return ifail;
}

DLLAPI int VF_updateItemIDScooter(METHOD_message_t* message, va_list args)
{
	int		ifail = ITK_ok;
	char* pcItemID = NULL;
	char* pcItemName = NULL;
	char* pcItemType = NULL;
	char* pcRevID = NULL;
	char* pcMatLov = NULL;
	char* pcClrLov = NULL;
	tag_t* tItem = NULLTAG;
	tag_t* tRev = NULLTAG;
	tag_t	tMaster = NULLTAG;
	tag_t	tRevMaster = NULLTAG;
	string	szNewID = "";

	pcItemID = va_arg(args, char*);
	pcItemName = va_arg(args, char*);
	pcItemType = va_arg(args, char*);
	pcRevID = va_arg(args, char*);
	tItem = va_arg(args, tag_t*);
	tRev = va_arg(args, tag_t*);
	tMaster = va_arg(args, tag_t);
	tRevMaster = va_arg(args, tag_t);

	TC_write_syslog("\n Entering VF_updateItemIDScooter \n");

	/* getting Material and Color code LOV */
	CHECK_FAIL(AOM_ask_value_string(*tItem, "vf3_mat_group", &pcMatLov));
	CHECK_FAIL(AOM_ask_value_string(*tItem, "vf3_color_code", &pcClrLov));

	/*for Scooter part*/
	if (tc_strcmp(pcItemType, "VF3_Scooter_part") == 0 && tc_strcmp(pcMatLov, "") != NULL)
	{
		/*if color code is blank*/
		if (tc_strlen(pcClrLov) == NULL || tc_strcmp(pcClrLov, "") == NULL)
		{
			/*add material group as prefix to item id*/
			szNewID.assign(pcMatLov);
			szNewID.append(pcItemID);

			/*to update item id, master form and rev master form*/
			CHECK_FAIL(updateItemCode(*tItem, tMaster, pcRevID, tRevMaster, szNewID));
		}
		else
		{
			/*add material group as prefix to item id and color code as suffix*/
			szNewID.assign(pcMatLov);
			szNewID.append(pcItemID);
			szNewID.append(pcClrLov);

			/*to update item id, master form and rev master form*/
			CHECK_FAIL(updateItemCode(*tItem, tMaster, pcRevID, tRevMaster, szNewID));
		}
	}

	/*memory free*/
	if (pcMatLov != NULL)
		MEM_free(pcMatLov);
	if (pcClrLov != NULL)
		MEM_free(pcClrLov);

	TC_write_syslog("\n Exiting VF_updateItemIDScooter \n");

	return ifail;
}

DLLAPI int vf_save_cost_info(METHOD_message_t* message, va_list args)
{
	int		sumIfail = ITK_ok;

	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	tag_t sourcePartRev = message->object_tag;

	tag_t rev = va_arg(args, tag_t);
	char* newPropValue = va_arg(args, char*);

	if (sourcePartRev != NULL)
	{
		int formsCount = 0;
		tag_t* forms = NULL;
		TC_write_syslog("\n[VF]*get item revision master form");
		char* oldPropVal = NULL;
		AOM_ask_value_string(sourcePartRev, "vf4_piece_cost_status", &oldPropVal);
		TC_write_syslog("\n[VF]old=%s", oldPropVal);
		TC_write_syslog("\n[VF]new=%s", newPropValue);

		if (tc_strcasecmp("Approved Sourcing Recommendation - Supplier On Board (SOB)", newPropValue) == 0 || tc_strcasecmp("Approved Sourcing Recommendation - Supplier On Board (SOB)", oldPropVal) == 0)
		{
			sumIfail += (AOM_ask_value_tags(sourcePartRev, "IMAN_master_form_rev", &formsCount, &forms) != ITK_ok);
			if (formsCount > 0 && forms != NULL)
			{
				tag_t form = forms[0];
				TC_write_syslog("\n[VF]**check write access");
				logical isWritable = false;
				sumIfail += (AM_check_privilege(form, "WRITE", &isWritable) != ITK_ok);
				if (isWritable && sumIfail == ITK_ok)
				{
					TC_write_syslog("\n[VF]***concat props to string");

					if (tc_strcasecmp("Approved Sourcing Recommendation - Supplier On Board (SOB)", newPropValue) == 0)
					{
						// in case buyer change status to SOB >> get current columns (don't need to get save column) => don't need to save
						// ==> DO NOTHING
					}
					else
					{
						std::string concatStr;
						std::vector<std::string> props;
						props.push_back("vf4_eddorder_value_2"); props.push_back("vf4_eddorder_currency");
						props.push_back("vf4_supplier_piece_cost_exw"); props.push_back("vf4_piece_currency");
						props.push_back("vf4_tooling_order_2"); props.push_back("vf4_tooling_currency");
						size_t n = props.size();

						for (size_t i = 0; i < n; i++)
						{
							const char* propName = props.at(i).c_str();
							PROP_value_type_t propType;
							char* szPropType = NULL;

							TC_write_syslog("\n[VF]****processing prop %s", propName);

							sumIfail += (ITK_ok != AOM_ask_value_type(sourcePartRev, propName, &propType, &szPropType));

							std::string strVal;
							if (propType == PROP_value_type_t::PROP_string)
							{
								char* value = NULL;
								sumIfail += (ITK_ok != AOM_ask_value_string(sourcePartRev, propName, &value));
								strVal = (sumIfail == ITK_ok && value) ? value : "ERROR";
								if (value) MEM_free(value);
							}
							else if (propType == PROP_value_type_t::PROP_double)
							{
								double value = -1;
								sumIfail += (ITK_ok != AOM_ask_value_double(sourcePartRev, propName, &value));
								strVal = (sumIfail == ITK_ok) ? std::to_string(value) : "ERROR";
							}
							else
							{
								strVal = "NO_SUPPORT_";
								strVal.append(propName);
							}

							concatStr.append(strVal).append("#");

							if (szPropType) MEM_free(szPropType);
						}
						concatStr.erase(concatStr.rfind("#"));

						if (sumIfail == ITK_ok && !concatStr.empty())
						{
							TC_write_syslog("\n[VF]***set props concated string to form");
							AOM_refresh(form, TRUE);
							AOM_set_value_string(form, "object_desc", concatStr.c_str());
							AOM_save_with_extensions(form);
							AOM_refresh(form, FALSE);
						}
					}
				}

				MEM_free(forms);
			}
		}
	}



	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);

	return ITK_ok;
}

DLLAPI int VF_ModDDEObject(METHOD_message_t* message, va_list args)
{
	int		ifail = ITK_ok;
	char* pcItemID = NULL;
	char* pcItemName = NULL;
	char* pcItemType = NULL;
	char* pcRevID = NULL;
	char* pcMatLov = NULL;
	char* pcClrLov = NULL;
	tag_t* tItem = NULLTAG;
	tag_t* tRev = NULLTAG;
	tag_t	tMaster = NULLTAG;
	tag_t	tRevMaster = NULLTAG;
	string	szNewName = "";

	pcItemID = va_arg(args, char*);
	pcItemName = va_arg(args, char*);
	pcItemType = va_arg(args, char*);
	pcRevID = va_arg(args, char*);
	tItem = va_arg(args, tag_t*);
	tRev = va_arg(args, tag_t*);
	tMaster = va_arg(args, tag_t);
	tRevMaster = va_arg(args, tag_t);

	TC_write_syslog("\n Entering VF_ModDDEObject \n");

	szNewName.assign(pcItemID);
	szNewName.append("-");
	szNewName.append(pcItemName);

	CHECK_FAIL(AOM_refresh(*tRev, true));
	CHECK_FAIL(AOM_set_value_string(*tRev, "object_name", szNewName.c_str()));
	CHECK_FAIL(AOM_save(*tRev));
	CHECK_FAIL(AOM_refresh(*tRev, false));

	TC_write_syslog("\n Exiting VF_ModDDEObject \n");

	return ifail;
}

DLLAPI int VF_updateItemIDMEOP(METHOD_message_t* message, va_list args)
{
	int		ifail = ITK_ok;
	char* pcItemID = NULL;
	char* pcItemName = NULL;
	char* pcItemType = NULL;
	char* pcRevID = NULL;
	char* pcMatLov = NULL;
	char* pcPlatform = NULL;
	tag_t* tItem = NULLTAG;
	tag_t* tRev = NULLTAG;
	tag_t	tMaster = NULLTAG;
	tag_t	tRevMaster = NULLTAG;
	string	szNewID = "";

	pcItemID = va_arg(args, char*);
	pcItemName = va_arg(args, char*);
	pcItemType = va_arg(args, char*);
	pcRevID = va_arg(args, char*);
	tItem = va_arg(args, tag_t*);
	tRev = va_arg(args, tag_t*);
	tMaster = va_arg(args, tag_t);
	tRevMaster = va_arg(args, tag_t);

	TC_write_syslog("\n Entering VF_updateItemIDMEOP \n");

	/* getting Material and Color code LOV */
	CHECK_FAIL(AOM_ask_value_string(*tItem, "vf4_material_group", &pcMatLov));
	CHECK_FAIL(AOM_ask_value_string(*tItem, "vf4_platform", &pcPlatform));

	/*for MEOP part*/
	if (tc_strcmp(pcItemType, "MEOP") == NULL)
	{
		/*if Material and platform filled,*/
		if (tc_strcmp(pcMatLov, "") != NULL && tc_strcmp(pcPlatform, "") != NULL)
		{
			/*if Platform is SUV, ItemID is U@@@NNNNNN*/
			if (tc_strcmp(pcPlatform, "SUV") == NULL)
			{
				szNewID.assign("U");
				szNewID.append(pcMatLov);
				szNewID.append(pcItemID);

				/*to update item id, master form and rev master form*/
				CHECK_FAIL(updateItemCode(*tItem, tMaster, pcRevID, tRevMaster, szNewID));
			}

			/*if Platform is SEDAN, ItemID is S@@@NNNNNN*/
			if (tc_strcmp(pcPlatform, "SEDAN") == NULL)
			{
				szNewID.assign("S");
				szNewID.append(pcMatLov);
				szNewID.append(pcItemID);

				/*to update item id, master form and rev master form*/
				CHECK_FAIL(updateItemCode(*tItem, tMaster, pcRevID, tRevMaster, szNewID));
			}
		}

		/*if onlt platform is filled*/
		if (tc_strcmp(pcMatLov, "") == NULL && tc_strcmp(pcPlatform, "") != NULL)
		{
			/*if Platform is SUV, ItemID is UNNNNNN*/
			if (tc_strcmp(pcPlatform, "SUV") == NULL)
			{
				szNewID.assign("U");
				szNewID.append(pcItemID);

				/*to update item id, master form and rev master form*/
				CHECK_FAIL(updateItemCode(*tItem, tMaster, pcRevID, tRevMaster, szNewID));
			}

			/*if Platform is SEDAN, ItemID is SNNNNNN*/
			if (tc_strcmp(pcPlatform, "SEDAN") == NULL)
			{
				szNewID.assign("S");
				szNewID.append(pcItemID);

				/*to update item id, master form and rev master form*/
				CHECK_FAIL(updateItemCode(*tItem, tMaster, pcRevID, tRevMaster, szNewID));
			}
		}
	}

	/*memory free*/
	if (pcMatLov != NULL)
		MEM_free(pcMatLov);
	if (pcPlatform != NULL)
		MEM_free(pcPlatform);

	TC_write_syslog("\n Exiting VF_updateItemIDMEOP \n");

	return ifail;
}

/*to create scooter modules and add to bomline of scooter vehicle while creation*/
DLLAPI int VF_genScooterMod(METHOD_message_t* message, va_list args)
{
	int				ifail = ITK_ok;
	char* pcItemID = NULL;
	char* pcItemName = NULL;
	char* pcItemType = NULL;
	char* pcRevID = NULL;
	char* pcMatLov = NULL;
	char* pcPlatform = NULL;
	char* pcESVehLine = NULL;
	tag_t* tItem = NULLTAG;
	tag_t* tRev = NULLTAG;
	tag_t			tMaster = NULLTAG;
	tag_t			tRevMaster = NULLTAG;

	pcItemID = va_arg(args, char*);
	pcItemName = va_arg(args, char*);
	pcItemType = va_arg(args, char*);
	pcRevID = va_arg(args, char*);
	tItem = va_arg(args, tag_t*);
	tRev = va_arg(args, tag_t*);
	tMaster = va_arg(args, tag_t);
	tRevMaster = va_arg(args, tag_t);

	int				iModules = 0;
	char** pcPrefNames = NULL;
	tag_t			tBView = NULLTAG;
	tag_t			tBVRev = NULLTAG;
	tag_t			tWindow = NULLTAG;
	tag_t			tTopLine = NULLTAG;

	vector<string>pcModID;
	vector<string>pcModNames;

	TC_write_syslog("\n Entering VF_genScooterMod \n");

	/*create bom view and bom view revision and set top line in bom window*/
	if (tItem != NULLTAG && tRev != NULLTAG)
	{
		CHECK_ITK(ifail, AOM_ask_value_string(*tItem, "vf4_es_model_veh_line", &pcESVehLine));
		if (ifail != ITK_ok)
		{
			EMH_store_error_s1(EMH_severity_error, CHECK_PROCUREMENT_TYPE, "Cannot get EScooter Model/Vehicle Line");
			return -999;
		}

		CHECK_FAIL(PS_create_bom_view(NULLTAG, "", "", *tItem, &tBView));
		if (tBView != NULLTAG)
		{
			CHECK_FAIL(AOM_save(tBView));
			CHECK_FAIL(PS_create_bvr(tBView, "", "", false, *tRev, &tBVRev));
			if (tBVRev != NULLTAG)
			{
				CHECK_FAIL(AOM_save(tBVRev));
				CHECK_FAIL(AOM_save(*tItem));
				CHECK_FAIL(BOM_create_window(&tWindow));
				if (tWindow != NULLTAG)
				{
					CHECK_FAIL(BOM_set_window_top_line(tWindow, *tItem, *tRev, NULLTAG, &tTopLine));
				}
			}
		}

		if (ifail == ITK_ok)
		{
			/*get pref values*/
			CHECK_FAIL(PREF_ask_char_values("VF_Scooter_Modules", &iModules, &pcPrefNames));

			/*split pref values into ModId and ModName and store in resp vector*/
			for (int iInd = 0; iInd < iModules; iInd++)
			{
				char* pcModItemID = NULL;
				char* pcModItemName = NULL;

				pcModItemID = tc_strtok(pcPrefNames[iInd], "=");
				pcModID.push_back(pcModItemID);

				pcModItemName = pcPrefNames[iInd] + (tc_strlen(pcModItemID) + 1);
				pcModNames.push_back(pcModItemName);
			}

			if (pcModID.size() > 0 && pcModNames.size() > 0)
			{
				for (int iInd1 = 0; iInd1 < iModules; iInd1++)
				{
					tag_t		tModItem = NULLTAG;
					tag_t		tModRev = NULLTAG;
					string		szNewID = "";
					char* pcLine = NULL;
					char* pcToken = NULL;

					vector<string> AttrValue;

					pcLine = (char*)MEM_alloc((int)(pcModNames[iInd1].length() + 1) * sizeof(char));
					tc_strcpy(pcLine, pcModNames[iInd1].c_str());

					pcToken = tc_strtok(pcLine, ",");

					while (pcToken != NULL)
					{
						AttrValue.push_back(pcToken);
						pcToken = tc_strtok(NULL, ",");
					}

					/*get next Mod ID*/
					szNewID = getNextMod(pcModID[iInd1]);

					if (szNewID.length() > 0)
					{
						/*create new Modules*/
						CHECK_FAIL(createMod(iModules, szNewID, AttrValue[0], &tModItem, pcESVehLine));
						if (tModItem != NULLTAG && tTopLine != NULLTAG)
						{
							/*add to bom line*/
							CHECK_FAIL(addBOMLine(tModItem, tWindow, tTopLine, AttrValue[1]));
						}
					}
				}
			}
		}

		/*close window*/
		if (tWindow != NULLTAG)
		{
			CHECK_FAIL(BOM_close_window(tWindow));
		}
	}
	else
	{
		TC_write_syslog("\n Failed to create Scooter Vehicle.. \n");
	}
	SAFE_MEM_free(pcESVehLine);
	TC_write_syslog("\n Exiting VF_genScooterMod \n");

	return ifail;
}

DLLAPI int VF_VF3_spec_doc_Create(METHOD_message_t* message, va_list args)
{
	int	ifail = ITK_ok;
	char* pcItemID = NULL;
	char* pcItemName = NULL;
	char* pcItemType = NULL;
	char* pcItemDesc = NULL;
	char* pcRevID = NULL;
	char* pcMatLov = NULL;
	char* pcPlatform = NULL;
	tag_t* tItem = NULLTAG;
	tag_t* tRev = NULLTAG;
	tag_t tMaster = NULLTAG;
	tag_t tRevMaster = NULLTAG;

	pcItemID = va_arg(args, char*);
	pcItemName = va_arg(args, char*);
	pcItemType = va_arg(args, char*);
	pcRevID = va_arg(args, char*);
	tItem = va_arg(args, tag_t*);
	tRev = va_arg(args, tag_t*);
	tMaster = va_arg(args, tag_t);
	tRevMaster = va_arg(args, tag_t);

	int	iModules = 0;
	char** pcPrefNames = NULL;
	tag_t tBView = NULLTAG;
	tag_t tBVRev = NULLTAG;
	tag_t tWindow = NULLTAG;
	tag_t tTopLine = NULLTAG;

	vector<string>pcModID;
	vector<string>pcModNames;

	TC_write_syslog("\n Entering VF_VF3_spec_doc_Create \n");
	if (tItem != NULLTAG && tRev != NULLTAG) {
		CHECK_FAIL(AOM_ask_value_string(*tItem, "object_desc", &pcItemDesc));
		//split ID
		string prefix = subString(pcItemID, 0, 4);
		string modelCode = subString(pcItemID, 4, 2);
		string moduleName = subString(pcItemID, 6, 3);
		string number = subString(pcItemID, 9, 4);
		if (prefix == "VFDS") {
			TC_write_syslog("\n Auto create DVPR and link to VFDS \n");
			tag_t dvpItem_rela_type = NULLTAG;
			tag_t tDVPRItem = NULLTAG;
			tag_t cmDVPRImplements = NULLTAG;
			string newDVPRID = "DVPR" + modelCode + moduleName + number;
			CHECK_FAIL(GRM_find_relation_type("VF4_DVPItems", &dvpItem_rela_type));
			CHECK_FAIL(createObject(iModules, newDVPRID, pcItemName, pcItemDesc, modelCode, moduleName, "VF3_AT_DVPR_doc", &tDVPRItem));
			CHECK_FAIL(GRM_create_relation(*tRev, tDVPRItem, dvpItem_rela_type, NULLTAG, &cmDVPRImplements));
			CHECK_FAIL(GRM_save_relation(cmDVPRImplements));
			CHECK_FAIL(AOM_save_without_extensions(*tRev));

			TC_write_syslog("\n Auto create DFMEA and link to VFDS \n");
			tag_t fmeaItem_rela_type = NULLTAG;
			tag_t tFMEAItem = NULLTAG;
			tag_t cmFMEAImplements = NULLTAG;
			string newFMEAID = "FMEA" + modelCode + moduleName + number;
			CHECK_FAIL(GRM_find_relation_type("VF4_DFMEA_Items", &fmeaItem_rela_type));
			CHECK_FAIL(createObject(iModules, newFMEAID, pcItemName, pcItemDesc, modelCode, moduleName, "VF4_AT_DFMEA_doc", &tFMEAItem));
			CHECK_FAIL(GRM_create_relation(*tRev, tFMEAItem, fmeaItem_rela_type, NULLTAG, &cmFMEAImplements));
			CHECK_FAIL(GRM_save_relation(cmFMEAImplements));
			CHECK_FAIL(AOM_save_without_extensions(*tRev));
			CHECK_FAIL(AOM_save_without_extensions(tFMEAItem));

			tag_t* itemRevList = NULLTAG;
			int itemRevCount = 0;
			CHECK_FAIL(ITEM_list_all_revs(tDVPRItem, &itemRevCount, &itemRevList));
			if (itemRevCount > 0) {
				TC_write_syslog("\n Auto create VFTE and link to DVPR \n");
				tag_t vfteItem_rela_type = NULLTAG;
				tag_t tVFTEItem = NULLTAG;
				tag_t cmVFTEImplements = NULLTAG;
				string newVFTEID = "VFTE" + modelCode + moduleName + number;
				CHECK_FAIL(GRM_find_relation_type("VF4_TestReports", &vfteItem_rela_type));
				CHECK_FAIL(createObject(iModules, newVFTEID, pcItemName, pcItemDesc, modelCode, moduleName, "VF3_AT_VFTE_Doc", &tVFTEItem));
				CHECK_FAIL(GRM_create_relation(itemRevList[0], tVFTEItem, vfteItem_rela_type, NULLTAG, &cmVFTEImplements));
				CHECK_FAIL(GRM_save_relation(cmVFTEImplements));
				CHECK_FAIL(AOM_save_without_extensions(*tRev));
				CHECK_FAIL(AOM_save_without_extensions(tVFTEItem));
			}
		}
	}
	else {
		TC_write_syslog("\n Failed to create VFDS doc... \n");
	}

	TC_write_syslog("\n Exiting VF_GenVFDSCreate \n");

	return ifail;
}


bool is_email_valid(const std::string& email_address)
{
	const std::regex pattern("(\\w+)(\\.|_)?(\\w*)@(\\w+)(\\.(\\w+))+");
	return std::regex_match(email_address, pattern);
}

DLLAPI int VF_Validate_sc0SponsorEmail(METHOD_message_t* message, va_list args)
{
	int				ifail = ITK_ok;
	char* pcPropValue = NULL;
	tag_t			tObject = NULLTAG;
	logical			isNew = false;

	tObject = va_arg(args, tag_t);

	TC_write_syslog("\n Entering VF_Validate_sc0SponsorEmail \n");

	if (tObject != NULLTAG)
	{
		CHECK_FAIL(ifail, AOM_ask_value_string(tObject, "sc0SponsorEmail", &pcPropValue));

		if (pcPropValue != NULL)
		{
			/*if value contains @, into loop*/
			if (is_email_valid(pcPropValue) == true)
			{
				return ifail;
			}
			/*if value does not contain @, into loop*/
			else
			{
				TC_write_syslog("\n Failed to update Sc0DesignXChangeRevision.. \n");
				ifail = 6969;
				EMH_store_error_s1(EMH_severity_error, 6969, "Invalid email format");
				return ifail;

			}
		}
	}
	else
	{
		TC_write_syslog("\n Property value is null.. \n");
	}

	TC_write_syslog("\n Exiting VF_Validate_sc0SponsorEmail \n");

	return ifail;
}

DLLAPI int VF_validate_plant_form(METHOD_message_t* message, va_list args)
{
	TC_write_syslog("\nEntering validation plant form");
	int ifail = ITK_ok;
	char* make_buy;
	char* plant_code;
	//bool vf3_vf4 = true;
	tag_t type = NULL_TAG;
	tag_t form_rel = NULL_TAG;
	//tag_t form_rel_vf4 = NULL_TAG;
	tag_t* items = NULL_TAG;
	int count_itm = 0;
	tag_t obj = NULL_TAG;
	char* itm_make_buy;
	char* itm_material_type;
	char* type_name;
	char* type_name_rel;
	logical isNew = FALSE;
	char errorMessage[2048] = "";
	obj = va_arg(args, tag_t);
	isNew = va_arg(args, logical);
	//Check validation Item related plant form
	//Get Procurement Type

	CHECK_FAIL(TCTYPE_ask_object_type(obj, &type));
	CHECK_FAIL(TCTYPE_ask_name2(type, &type_name));
	if (tc_strcmp(type_name, "VF3_plant_form") == 0)
	{
		TC_write_syslog("\nplant form relation : VF3");
		CHECK_FAIL(AOM_ask_value_string(obj, "vf3_make_buy", &make_buy));
		CHECK_FAIL(AOM_ask_value_string(obj, "vf3_plant", &plant_code));
		CHECK_FAIL(GRM_find_relation_type("VF3_plant_rel", &form_rel));
	}
	if (tc_strcmp(type_name, "VF4_plant_form") == 0)
	{
		TC_write_syslog("\nplant form relation: VF4");
		CHECK_FAIL(AOM_ask_value_string(obj, "vf4_make_buy", &make_buy));
		CHECK_FAIL(AOM_ask_value_string(obj, "vf4_plant", &plant_code));
		CHECK_FAIL(GRM_find_relation_type("VF4_plant_form_relation", &form_rel));
	}
	else
	{

		TC_write_syslog("\nPlant Form Relation: %s", type_name);
	}
	TC_write_syslog("\nPlant Form Make Buy: %s", make_buy);
	//TC_write_syslog("\nlen make_buy: %d", tc_strlen(make_buy));
	//TC_write_syslog("\nlen make_buy: %d", tc_strlen("E"));
	if (tc_strcasecmp(make_buy, "E") == 0)
	{
		TC_write_syslog("\nStart validate Item");
		CHECK_FAIL(TCTYPE_ask_name2(form_rel, &type_name_rel));
		TC_write_syslog("\n relation name: %s", type_name_rel);
		//CHECK_FAIL(AOM_refresh(obj, false));
		CHECK_FAIL(GRM_list_primary_objects_only(obj, form_rel, &count_itm, &items));
		//CHECK_FAIL(AOM_refresh(obj, true));
		TC_write_syslog("\n Count Item: %d", count_itm);

		//Check if item not null
		if (count_itm > 0)
		{
			//validate Item
			for (int i = 0; i < count_itm; i++)
			{
				CHECK_FAIL(AOM_ask_value_string(items[i], "vf4_item_make_buy", &itm_make_buy));
				CHECK_FAIL(AOM_ask_value_string(items[i], "vf4_itm_material_type", &itm_material_type));
				if (itm_make_buy == "" || itm_make_buy == NULL)
				{
					TC_write_syslog("\nMake_Buy on Item is NULL or empty");
				}
				if (itm_material_type == "" || itm_material_type == NULL)
				{
					TC_write_syslog("\nMaterial Type on Item is NULL or empty");
				}
				TC_write_syslog("\nItem Make_Buy: %s", itm_make_buy);
				TC_write_syslog("\nItem Material Type: %s", itm_material_type);
				if (tc_strcasecmp(itm_material_type, "ZRAW") == 0)
				{
					sprintf_s(errorMessage, "Procurement type should be \"PURCHASE (F)\" as the \"Material Type\" is ZRAW");
					EMH_store_error_s1(EMH_severity_error, CHECK_PROCUREMENT_TYPE, errorMessage);
					return CHECK_PROCUREMENT_TYPE;
				}
				else if (tc_strcmp(plant_code, "4001") == 0)
				{
					sprintf_s(errorMessage, "Procurement type should be \"PURCHASE (F)\" as the \"Plant Code\" is 4001");
					EMH_store_error_s1(EMH_severity_error, CHECK_PROCUREMENT_TYPE, errorMessage);
					return CHECK_PROCUREMENT_TYPE;
				}
				else if (tc_strcmp(plant_code, "4002") == 0)
				{
					sprintf_s(errorMessage, "Procurement type should be \"PURCHASE (F)\" as the \"Plant Code\" is 4002");
					EMH_store_error_s1(EMH_severity_error, CHECK_PROCUREMENT_TYPE, errorMessage);
					return CHECK_PROCUREMENT_TYPE;
				}
				/*else if(tc_strcmp(itm_material_type, "ZSFG") == 0)
				{
					sprintf_s(errorMessage, "Procurement type should be \"MAKE (E)\" as the \"Material Type\" only are ZFG or ZVEH");
					EMH_store_error_s1(EMH_severity_error, CHECK_PROCUREMENT_TYPE, errorMessage);
					return CHECK_PROCUREMENT_TYPE;
				}
				else if(tc_strcmp(itm_material_type, "ZSOF") == 0)
				{
					sprintf_s(errorMessage, "Procurement type should be \"MAKE (E)\" as the \"Material Type\" only are ZFG or ZVEH");
					EMH_store_error_s1(EMH_severity_error, CHECK_PROCUREMENT_TYPE, errorMessage);
					return CHECK_PROCUREMENT_TYPE;
				}*/
			}
			SAFE_MEM_free(itm_make_buy);
			SAFE_MEM_free(itm_material_type);
			SAFE_MEM_free(type_name_rel);
		}



	}
	if (tc_strcasecmp(make_buy, "F") == 0)
	{
		TC_write_syslog("\nStart validate Item");
		CHECK_FAIL(TCTYPE_ask_name2(form_rel, &type_name_rel));
		TC_write_syslog("\n relation name: %s", type_name_rel);
		//CHECK_FAIL(AOM_refresh(obj, false));
		CHECK_FAIL(GRM_list_primary_objects_only(obj, form_rel, &count_itm, &items));
		//CHECK_FAIL(AOM_refresh(obj, true));
		TC_write_syslog("\n Count Item: %d", count_itm);

		//Check if item not null
		if (count_itm > 0)
		{
			//validate Item
			for (int i = 0; i < count_itm; i++)
			{
				CHECK_FAIL(AOM_ask_value_string(items[i], "vf4_item_make_buy", &itm_make_buy));
				CHECK_FAIL(AOM_ask_value_string(items[i], "vf4_itm_material_type", &itm_material_type));
				if (itm_make_buy == "" || itm_make_buy == NULL)
				{
					TC_write_syslog("\nMake_Buy on Item is NULL or empty");
				}
				if (itm_material_type == "" || itm_material_type == NULL)
				{
					TC_write_syslog("\nMaterial Type on Item is NULL or empty");
				}
				TC_write_syslog("\nItem Make_Buy: %s", itm_make_buy);
				TC_write_syslog("\nItem Material Type: %s", itm_material_type);
				if (tc_strcmp(itm_material_type, "ZFG") == 0)
				{
					sprintf_s(errorMessage, "Procurement type must be \"MAKE (E)\" as the \"Material Type\" is ZFG");
					EMH_store_error_s1(EMH_severity_error, CHECK_PROCUREMENT_TYPE, errorMessage);
					return CHECK_PROCUREMENT_TYPE;
				}
				else if (tc_strcmp(itm_material_type, "ZVEH") == 0)
				{
					sprintf_s(errorMessage, "Procurement type must be \"MAKE (E)\" as the \"Material Type\" is ZVEH");
					EMH_store_error_s1(EMH_severity_error, CHECK_PROCUREMENT_TYPE, errorMessage);
					return CHECK_PROCUREMENT_TYPE;
				}
			}
			SAFE_MEM_free(itm_make_buy);
			SAFE_MEM_free(itm_material_type);
			SAFE_MEM_free(type_name_rel);
		}



	}

	SAFE_MEM_free(make_buy);
	SAFE_MEM_free(plant_code);
	SAFE_MEM_free(type_name);

	TC_write_syslog("\nExiting validation plant form");

	return ifail;
}

DLLAPI int VF_VF3_VTSS_doc_Create(METHOD_message_t* message, va_list args)
{
	int	ifail = ITK_ok;
	char* pcItemID = NULL;
	char* pcItemName = NULL;
	char* pcItemType = NULL;
	char* pcItemDesc = NULL;
	char* pcRevID = NULL;
	char* pcMatLov = NULL;
	char* pcPlatform = NULL;
	tag_t* tItem = NULLTAG;
	tag_t* tRev = NULLTAG;
	tag_t tMaster = NULLTAG;
	tag_t tRevMaster = NULLTAG;

	pcItemID = va_arg(args, char*);
	pcItemName = va_arg(args, char*);
	pcItemType = va_arg(args, char*);
	pcRevID = va_arg(args, char*);
	tItem = va_arg(args, tag_t*);
	tRev = va_arg(args, tag_t*);
	tMaster = va_arg(args, tag_t);
	tRevMaster = va_arg(args, tag_t);

	int	iModules = 0;
	char** pcPrefNames = NULL;
	tag_t tBView = NULLTAG;
	tag_t tBVRev = NULLTAG;
	tag_t tWindow = NULLTAG;
	tag_t tTopLine = NULLTAG;

	vector<string>pcModID;
	vector<string>pcModNames;
	TC_write_syslog("\n Entering VF_VF3_VTSS_doc_Create \n");
	if (tItem != NULLTAG && tRev != NULLTAG) {
		CHECK_FAIL(AOM_ask_value_string(*tItem, "object_desc", &pcItemDesc));
		//split ID
		string prefix = subString(pcItemID, 0, 4);
		string modelCode = subString(pcItemID, 4, 2);
		string moduleName = subString(pcItemID, 6, 3);
		string number = subString(pcItemID, 9, 4);

		if (prefix == "VTSS") {
			TC_write_syslog("\n Auto create DVPR and link to VTSS \n");
			tag_t dvpItem_rela_type = NULLTAG;
			tag_t tDVPRItem = NULLTAG;
			tag_t cmDVPRImplements = NULLTAG;
			string newDVPRID = "DVPR" + modelCode + moduleName + number;
			CHECK_FAIL(GRM_find_relation_type("VF4_DVPItems", &dvpItem_rela_type));
			CHECK_FAIL(createObject(iModules, newDVPRID, pcItemName, pcItemDesc, modelCode, moduleName, "VF3_AT_DVPR_doc", &tDVPRItem));
			CHECK_FAIL(GRM_create_relation(*tRev, tDVPRItem, dvpItem_rela_type, NULLTAG, &cmDVPRImplements));
			CHECK_FAIL(GRM_save_relation(cmDVPRImplements));
			CHECK_FAIL(AOM_save_without_extensions(*tRev));

			tag_t* itemRevList = NULLTAG;
			int itemRevCount = 0;
			CHECK_FAIL(ITEM_list_all_revs(tDVPRItem, &itemRevCount, &itemRevList));
			if (itemRevCount > 0) {
				TC_write_syslog("\n Auto create VFTE and link to DVPR \n");
				tag_t vfteItem_rela_type = NULLTAG;
				tag_t tVFTEItem = NULLTAG;
				tag_t cmVFTEImplements = NULLTAG;
				string newVFTEID = "VFTE" + modelCode + moduleName + number;
				CHECK_FAIL(GRM_find_relation_type("VF4_TestReports", &vfteItem_rela_type));
				CHECK_FAIL(createObject(iModules, newVFTEID, pcItemName, pcItemDesc, modelCode, moduleName, "VF3_AT_VFTE_Doc", &tVFTEItem));
				CHECK_FAIL(GRM_create_relation(itemRevList[0], tVFTEItem, vfteItem_rela_type, NULLTAG, &cmVFTEImplements));
				CHECK_FAIL(GRM_save_relation(cmVFTEImplements));
				CHECK_FAIL(AOM_save_without_extensions(*tRev));
				CHECK_FAIL(AOM_save_without_extensions(tVFTEItem));
			}
		}

	}
	else {

		TC_write_syslog("\n Failed to create VTSS doc... \n");
	}

	TC_write_syslog("\n Exiting VF_GenVTSSCreate \n");

	return ifail;
}

/// <summary>
///		Force the first created revision is 01
/// </summary>
/// <param name="message"></param>
/// <param name="args"></param>
/// <returns></returns>
DLLAPI int VF4_Design_Create(METHOD_message_t* message, va_list args)
{
	int	ifail = ITK_ok;
	char errorMessage[2048] = "";
	char* pcRevision = NULL;

	char* pcItemID = NULL;
	char* pcItemName = NULL;
	char* pcItemType = NULL;
	char* pcRevID = NULL;

	tag_t* tItem = NULLTAG;
	tag_t* tRev = NULLTAG;
	tag_t tMaster = NULLTAG;
	tag_t tRevMaster = NULLTAG;

	pcItemID = va_arg(args, char*);
	pcItemName = va_arg(args, char*);
	pcItemType = va_arg(args, char*);
	pcRevID = va_arg(args, char*);
	tItem = va_arg(args, tag_t*);
	tRev = va_arg(args, tag_t*);
	tMaster = va_arg(args, tag_t);
	tRevMaster = va_arg(args, tag_t);

	TC_write_syslog("\n Entering VF4_Design_Create \n");

	if (tItem != NULLTAG && tRev != NULLTAG) {
		CHECK_FAIL(AOM_ask_value_string(*tRev, "item_revision_id", &pcRevision));
		if (tc_strcmp(pcRevision, "01") != 0) {
			ifail = -1;
			sprintf_s(errorMessage, "The initiate revision number must be 01!");
			TC_write_syslog(errorMessage);
			EMH_store_error_s1(EMH_severity_error, ifail, errorMessage);
		}
	}
	else {
		TC_write_syslog("\n Failed to create VF Design... \n");
	}

	TC_write_syslog("\n Exiting VF4_Design_Create \n");

	return ifail;
}

DLLAPI int set_vf4_tc_material_class(METHOD_message_t* msg, va_list args)
{
	TC_write_syslog("\n[vf] set_vf4_tc_material_class executing...\n");
	int retCode = ITK_ok;
	int counter = 0;
	tag_t obj = msg->object_tag;
	std::string matCategory = "";
	//Get preference
	char** preVal = NULL;
	std::map<std::string, std::string> matDetail2MatCategory;
	CHECK_ITK(retCode, PREF_ask_char_values("VF_IMDS_MATERIAL_CLASSIFICATION", &counter, &preVal));
	if (retCode == ITK_ok)
	{
		for (int i = 0; i < counter; i++)
		{
			if (std::string(preVal[i]).find("=") != std::string::npos)
			{
				matDetail2MatCategory.insert(std::pair<std::string, std::string>(split(preVal[i], "=")[0], split(preVal[i], "=")[1]));
			}
		}
		SAFE_MEM_free(preVal);

		//get value sci0MaterialClassification
		char* matDetail = NULL;
		CHECK_ITK(retCode, AOM_ask_value_string(obj, "sci0MaterialClassification", &matDetail));
		if (matDetail2MatCategory.find(matDetail) != matDetail2MatCategory.end())
		{
			matCategory = matDetail2MatCategory.find(matDetail)->second;
			CHECK_ITK(retCode, AOM_set_value_string(obj, "vf4_tc_material_class", matCategory.c_str()));

		}
		else
		{
			TC_write_syslog("[VF_custom]__%d__%s__Material type not available for material: %s\n", __LINE__, __FUNCTION__, matDetail);
		}
	}
	else
	{
		TC_write_syslog("[VF_custom]__%d__%s__Cannot get preference VF_MATERIAL_CLASSIFICATION\n", __LINE__, __FUNCTION__);
	}

	return ITK_ok;
}

DLLAPI int VF_validatePieceCostForm(METHOD_message_t* message, va_list args)
{
	TC_write_syslog("[VF_custom]__%d__%s__Started\n", __LINE__, __FUNCTION__);

	int ifail = ITK_ok;

	tag_t tPieceCostObj = message->object_tag;
	Teamcenter::scoped_smptr<char> pieceCostName;
	CHECK_ITK(ifail, AOM_ask_value_string(tPieceCostObj, "object_string", &pieceCostName));
	if (ifail != ITK_ok) return ifail;

	TC_write_syslog("[VF_custom]__%d__%s__Validating piece cost form: %s\n", __LINE__, __FUNCTION__, pieceCostName.getString());

	if (std::string(pieceCostName.getString()).find("_") == std::string::npos)
	{
		TC_write_syslog("[VF_custom]__%d__%s__Invalid form name: %s\n", __LINE__, __FUNCTION__, pieceCostName.getString());
		return -9999;
	}

	//query design by part id
	std::string partId = split(pieceCostName.getString(), "_")[0];
	tag_t tQuery = NULLTAG;
	CHECK_ITK(ifail, QRY_find2("Item...", &tQuery));
	if (ifail != ITK_ok)
	{
		TC_write_syslog("[VF_custom]__%d__%s__Cannot find query [Item...]: %d\n", __LINE__, __FUNCTION__, ifail);
		return -9999;
	}

	const int nEntries = 2;
	char* entries[2] = { "Item ID", "Type" };
	char** values = nullptr;

	values = (char**)MEM_alloc(nEntries * sizeof(char*));

	values[0] = (char*)MEM_alloc(strlen(partId.c_str()) + 1);
	values[1] = (char*)MEM_alloc(56);
	tc_strcpy(values[0], partId.c_str());
	tc_strcpy(values[1], "*Design;VF3_Scooter_part;VF3_manuf_part;VF3_me_scooter");
	int iDesignCount = 0;
	tag_t* tDesignItem = nullptr;

	CHECK_ITK(ifail, QRY_execute(tQuery, nEntries, entries, values, &iDesignCount, &tDesignItem));

	CHECK_ITK(ifail, QRY_find2("VF Cost", &tQuery));
	char* costQueryEntry = "ID";
	char* szPartID = (char*)(partId.c_str());
	tag_t tCostRev = NULLTAG;

	{
		int iCostRevsCount = 0;
		Teamcenter::scoped_smptr<tag_t> tCostRevs;
		CHECK_ITK(ifail, QRY_execute(tQuery, 1, &costQueryEntry, &szPartID, &iCostRevsCount, &tCostRevs));
		if (iCostRevsCount > 0) tCostRev = tCostRevs[0];
	}

	if (ifail != ITK_ok) return ifail;

	if (iDesignCount == 0)
	{
		TC_write_syslog("[VF_custom]__%d__%s__Cannot get Design Part: %s\n", __LINE__, __FUNCTION__, partId.c_str());
		return -9999;
	}

	//check part is make single or make assembly
	logical isAssembly = checkIsAssembly(tDesignItem[0]);

	Teamcenter::scoped_smptr<char> makeBuy;
	CHECK_ITK(ifail, AOM_ask_value_string(tDesignItem[0], "vf4_item_make_buy", &makeBuy));
	if (ifail != ITK_ok) return ifail;

	logical isPieceCostEmpty = true;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4_piece_cost_value_status", true, &isPieceCostEmpty));
	if (ifail != ITK_ok) return ifail;

	logical isPackingCostEmpty = true;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4_supplier_package_amount", true, &isPackingCostEmpty));
	if (ifail != ITK_ok) return ifail;

	logical isLogisticsCostEmpty = true;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4_supplier_logisis_cost", true, &isLogisticsCostEmpty));
	if (ifail != ITK_ok) return ifail;

	logical isPieceCostCurrEmpty = true;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4_piece_cost_curr", true, &isPieceCostCurrEmpty));
	if (ifail != ITK_ok) return ifail;

	logical isToolingCostCurrEmpty = true;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4_tooling_invtest_curr", true, &isToolingCostCurrEmpty));
	if (ifail != ITK_ok) return ifail;

	logical isToolingCostEmpty = true;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4_tooling_invest_value", true, &isToolingCostEmpty));
	if (ifail != ITK_ok) return ifail;

	logical isEDDCostCurrEmpty = true;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4Ednd_curr", true, &isEDDCostCurrEmpty));
	if (ifail != ITK_ok) return ifail;

	logical isEDDCostEmpty = true;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4EdndCost", true, &isEDDCostEmpty));
	if (ifail != ITK_ok) return ifail;

	if (isAssembly && tc_strcmp("Make", makeBuy.getString()) == 0)
	{
		TC_write_syslog("[VF_custom]__%d__%s__Apply logic for make assembly Part: %s\n", __LINE__, __FUNCTION__, partId.c_str());


		logical isAllowInputCost = false;
		if (tCostRev) CHECK_ITK(ifail, AOM_ask_value_logical(tCostRev, "vf4_is_included_h_asem_cost", &isAllowInputCost));
		if (ifail != ITK_ok) return ifail;

		if ((!isPieceCostEmpty || !isPackingCostEmpty || !isLogisticsCostEmpty) && !isAllowInputCost)
		{
			char errorMessage[2048] = "";
			TC_write_syslog("[VF_custom]__%d__%s__Not allow input Piece Cost, Logistics Cost and Packaging Cost for Make Assembly part: %s\n", __LINE__, __FUNCTION__, partId.c_str());
			sprintf_s(errorMessage, "Cannot input Piece Cost, Logistics Cost, Packaging Cost of Make Assembly");
			EMH_store_error_s1(EMH_severity_user_error, ActualCostForm_Validation_Error, errorMessage);
			return ActualCostForm_Validation_Error;
		}
	}

	//5-Oct: thanhpn6 add logic validate currency and cost
	if (isPieceCostCurrEmpty && (!isPieceCostEmpty || !isPackingCostEmpty || !isLogisticsCostEmpty))
	{
		char errorMessage[2048] = "";
		sprintf_s(errorMessage, "Please input Piece Cost Currency Status");
		EMH_store_error_s1(EMH_severity_user_error, ActualCostForm_Validation_Error, errorMessage);
		return ActualCostForm_Validation_Error;
	}

	if (isToolingCostCurrEmpty && !isToolingCostEmpty)
	{
		char errorMessage[2048] = "";
		sprintf_s(errorMessage, "Please input Tooling Investment Currency Status");
		EMH_store_error_s1(EMH_severity_user_error, ActualCostForm_Validation_Error, errorMessage);
		return ActualCostForm_Validation_Error;
	}

	if (isEDDCostCurrEmpty && !isEDDCostEmpty)
	{
		char errorMessage[2048] = "";
		sprintf_s(errorMessage, "Please input ED&D Cost Currency");
		EMH_store_error_s1(EMH_severity_user_error, ActualCostForm_Validation_Error, errorMessage);
		return ActualCostForm_Validation_Error;
	}


	// get manufacutring cost
	// check if it's not empty, then, checck if cost < 0 || >200 then save error
	const double MIN_MFG_COST = 0;
	const double MAX_MFG_COST = 800.0;
	logical isLsMfgCostEmpty = false;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4_ls_manufacturing_cost", true, &isLsMfgCostEmpty));
	/*if (isLsMfgCostEmpty == false)
	{
		double mfgCost = 0;
		CHECK_ITK(ifail, AOM_ask_value_double(tPieceCostObj, "vf4_ls_manufacturing_cost", &mfgCost));
		if (mfgCost < MIN_MFG_COST || mfgCost > MAX_MFG_COST)
		{
			EMH_store_error_s1(EMH_severity_user_error, ActualCostForm_Validation_Error, "Only allow Manufacturing cost from 0 to 200. Please correct LS Manufacturing Cost!");
			return ActualCostForm_Validation_Error;
		}
	}*/

	logical isProdMfgCostEmpty = false;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4_miscellaneous_cost", true, &isProdMfgCostEmpty));
	if (isProdMfgCostEmpty == false)
	{
		double mfgCost = 0;
		CHECK_ITK(ifail, AOM_ask_value_double(tPieceCostObj, "vf4_miscellaneous_cost", &mfgCost));
		if (mfgCost < MIN_MFG_COST || mfgCost > MAX_MFG_COST)
		{
			EMH_store_error_s1(EMH_severity_user_error, ActualCostForm_Validation_Error, "Only allow Manufacturing cost from 0 to 200. Please correct Production Manufacturing Cost!");
			return ActualCostForm_Validation_Error;
		}
	}

	/*logical isPpMfgCostEmpty = false;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4_pp_manufacturing_cost", true, &isPpMfgCostEmpty));
	if (isPpMfgCostEmpty == false)
	{
		double mfgCost = 0;
		CHECK_ITK(ifail, AOM_ask_value_double(tPieceCostObj, "vf4_pp_manufacturing_cost", &mfgCost));
		if (mfgCost < MIN_MFG_COST || mfgCost > MAX_MFG_COST)
		{
			EMH_store_error_s1(EMH_severity_user_error, ActualCostForm_Validation_Error, "Only allow Manufacturing cost from 0 to 200. Please correct PP Manufacturing Cost!");
			return ActualCostForm_Validation_Error;
		}
	}

	logical isPtoMfgCostEmpty = false;
	CHECK_ITK(ifail, AOM_is_null_empty(tPieceCostObj, "vf4_pto_manufacturing_cost", true, &isPtoMfgCostEmpty));
	if (isPtoMfgCostEmpty == false)
	{
		double mfgCost = 0;
		CHECK_ITK(ifail, AOM_ask_value_double(tPieceCostObj, "vf4_pto_manufacturing_cost", &mfgCost));
		if (mfgCost < MIN_MFG_COST || mfgCost > MAX_MFG_COST)
		{
			EMH_store_error_s1(EMH_severity_user_error, ActualCostForm_Validation_Error, "Only allow Manufacturing cost from 0 to 200. Please correct PTO Manufacturing Cost!");
			return ActualCostForm_Validation_Error;
		}
	}*/

	SAFE_MEM_free(tDesignItem);
	TC_write_syslog("[VF_custom]__%d__%s__Ended\n", __LINE__, __FUNCTION__);
	return ifail;
}

vector<string> split(const std::string& s, const std::string& delimiter) {
	size_t pos_start = 0;
	size_t pos_end;
	const size_t delimiter_len = delimiter.length();
	std::vector<std::string> res;

	while ((pos_end = s.find(delimiter, pos_start)) != std::string::npos) {
		std::string token = s.substr(pos_start, pos_end - pos_start);
		pos_start = pos_end + delimiter_len;
		res.push_back(token);
	}

	res.push_back(s.substr(pos_start));
	return res;
}

DLLAPI int VF_custom_register_methods()
{
	int				ifail = ITK_ok;
	METHOD_id_t		tMethod;

	TC_write_syslog("\n[vf] Enter %s", __FUNCTION__);

	/*find method for ITEM_create*/
	ifail = METHOD_find_method("VF3_Scooter_part", ITEM_create_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		/*adding method at post action of creation of scooter part*/
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, VF_updateItemIDScooter, NULL);
	}

	ifail = METHOD_find_method("VF3_me_scooter", ITEM_create_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		/*adding method at post action of creation of ME scooter part*/
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, VF_updateItemIDMEScooter, NULL);
	}

	ifail = METHOD_find_method("MEOP", ITEM_create_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		/*adding method at post action of creation of ME scooter part*/
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, VF_updateItemIDMEOP, NULL);
	}

	ifail = METHOD_find_method("VF4_scooter_veh", ITEM_create_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		/*adding method at post action of creation of ME scooter part*/
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, VF_genScooterMod, NULL);
	}

	ifail = METHOD_find_method("VF3_spec_doc", ITEM_create_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		/*adding method at post action of creation of Spec Doc*/
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, VF_VF3_spec_doc_Create, NULL);
	}


	ifail = METHOD_find_method("Sc0DesignXChange", ITEM_create_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		/*adding method at post action of creation of ME scooter part*/
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, VF_ModDDEObject, NULL);
	}

	ifail = METHOD_find_prop_method("VF4_line_itemRevision", "vf4_piece_cost_status", PROP_set_value_string_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_pre_action_type, vf_save_cost_info, NULL);
		TC_write_syslog("\n[VF] Register VF4_line_itemRevision::vf4_piece_cost_status ifail=%d\n", ifail);
		//double dval= 34.4492;
		//std::string strVal = "VND";
		//double dval2= 99999.0;
		//std::string strVal2 = "VND";
		//OUTPUT STRING >> 34.449200VND#99999.000000#VND
	}

	TC_write_syslog("\n[vf] adding vf_create_cost_forms ...");
	ifail = METHOD_find_method("VF4_CostRevision", ITEM_create_rev_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		/*adding method at pre condition of save of DDE revision*/
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, vf_create_cost_forms, NULL);
		if (ifail == ITK_ok) TC_write_syslog("\n[vf] added vf_create_cost_forms.");
	}

	TC_write_syslog("\n[vf] adding vf_item_save_post_action ...");
	ifail = METHOD_find_method("Item", TC_save_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		/*adding method at pre condition of save of DDE revision*/
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, vf_item_save_post_action, NULL);
		if (ifail == ITK_ok) TC_write_syslog("\n[vf] added vf_item_save_post_action.");

	}
	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);

	TC_write_syslog("\n[VF] Validation plant form for make_buy vf3");
	ifail = METHOD_find_method("VF3_plant_form", TC_save_msg, &tMethod);
	if (tMethod.id != NULL_TAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_pre_action_type, VF_validate_plant_form, NULL);
	}

	TC_write_syslog("\n[VF] Validation plant form for make_buy vf4");
	ifail = METHOD_find_method("VF4_plant_form", TC_save_msg, &tMethod);
	if (tMethod.id != NULL_TAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_pre_action_type, VF_validate_plant_form, NULL);
	}

	TC_write_syslog("\n[VF] Design::vf4_is_frozen registering");
	ifail = METHOD_find_prop_method("Design", "vf4_is_frozen", PROP_ask_value_string_msg, &tMethod);
	if (tMethod.id != NULL_TAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, vf_is_frozen_getter, NULL);
		TC_write_syslog("\n[VF]registered retcode %d", ifail);
	}

	TC_write_syslog("\n[VF] Design::vf4_orginal_part_number registering");
	ifail = METHOD_find_prop_method("Design", "vf4_orginal_part_number", PROP_is_modifiable_msg, &tMethod);
	if (tMethod.id != NULL_TAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, vf_original_part_number_is_modifable, NULL);
		TC_write_syslog("\n[VF]registered retcode %d", ifail);
	}

	TC_write_syslog("\n[VF] Validation family group from");
	ifail = METHOD_find_method("VF4_Family_Group", TC_save_msg, &tMethod);
	if (tMethod.id != NULL_TAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_pre_action_type, vf_fg_save_pre_action, NULL);
	}

	TC_write_syslog("\n[VF] Validation fg rule from");
	ifail = METHOD_find_method("VF4_FG_Rule", TC_save_msg, &tMethod);
	if (tMethod.id != NULL_TAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_pre_action_type, vf_fg_rule_save_pre_action, NULL);
	}

	ifail = METHOD_find_prop_method("Mat1MaterialRevision", "sci0MaterialClassification", PROP_set_value_string_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, set_vf4_tc_material_class, NULL);
		TC_write_syslog("\n[VF] Register pre-action Mat1MaterialRevision::sci0MaterialClassification ifail=%d\n", ifail);
	}

	ifail = METHOD_find_method("VF4_line_itemRevision", TC_save_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		//ifail = METHOD_add_action( tMethod , METHOD_post_action_type , vf_set_vf4_cost_approval_date, NULL );
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, vf_set_vf4_cost_approval_date, NULL);
		TC_write_syslog("\n[VF] Register post-action VF4_line_itemRevision post save ifail=%d\n", ifail);
	}

	ifail = METHOD_find_method("VF4_line_itemRevision", ITEM_create_rev_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		//ifail = METHOD_add_action( tMethod , METHOD_post_action_type , vf_set_vf4_cost_approval_date, NULL );
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, VF_assign2Buyer, NULL);
		TC_write_syslog("\n[VF] Register post-action VF4_line_itemRevision post create ifail=%d\n", ifail);
	}

	//Changes made by TTL to validate the cost form
	TC_write_syslog("\n[VF] Validate Cost Form properties before Saving\n");
	ifail = METHOD_find_method("VF4_TargetCostForm", TC_save_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_pre_action_type, VF_validateTargetCostForm, NULL);
	}

	TC_write_syslog("\n[VF] Change ownership for BOP revisios\n");
	ifail = METHOD_find_method("Mfg0MEProcAreaRevision", ITEM_deep_copy_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, vf_change_ownership_bop_rev, NULL);
	}
	ifail = METHOD_find_method("MEOPRevision", ITEM_deep_copy_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, vf_change_ownership_bop_rev, NULL);
	}

	//thanhpn6 14-Jun: not allow input piece cost, packing cost and logistics cost for Make Assembly
	TC_write_syslog("\n[VF] Validate Piece Form properties before Saving\n");
	ifail = METHOD_find_method("VF4_PieceCostForm", TC_save_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_pre_action_type, VF_validatePieceCostForm, NULL);
	}

	//thanhpn6 26-Aug: ECR release post action
	ifail = METHOD_find_prop_method("Vf6_ECRRevision", "date_released", PROP_set_value_date_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, vf_set_ecr_number, NULL);
		TC_write_syslog("\n[VF] Register post-action on Vf6_ECRRevision::date_released ifail=%d\n", ifail);
	}

	//thanhpn6 5-Sep: ECN release post action
	ifail = METHOD_find_prop_method("Vf6_ECNRevision", "date_released", PROP_set_value_date_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, vf_set_ecn_number, NULL);
		TC_write_syslog("\n[VF] Register post-action on Vf6_ECNRevision::date_released ifail=%d\n", ifail);
	}

	//thanhpn6 5-Sep: Item release post action
	ifail = METHOD_find_prop_method("ItemRevision", "date_released", PROP_set_value_date_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, vf_set_maturity_level, NULL);
		TC_write_syslog("\n[VF] Register post-action on ItemRevision::date_released ifail=%d\n", ifail);
	}

	//binhtt28 6_12:
	ifail = METHOD_find_method("VF3_VTSS_doc", ITEM_create_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		/*adding method at post action of creation of Spec Doc*/
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, VF_VF3_VTSS_doc_Create, NULL);
	}

	//thienbq2 23-Oct:
	ifail = METHOD_find_method("VF4_Design", ITEM_create_msg, &tMethod);
	if (tMethod.id != NULLTAG)
	{
		/*adding method at post action of creation of Spec Doc*/
		ifail = METHOD_add_action(tMethod, METHOD_post_action_type, VF4_Design_Create, NULL);
	}

	return ifail;
}

DLLAPI int post_user_init_module(int* decision, va_list args)
{
	int ifail = ITK_ok;
	*decision = ALL_CUSTOMIZATIONS;

	TC_write_syslog("\n[vf] Enter %s", __FUNCTION__);

	ifail = VF_custom_register_methods();
	if (ifail != ITK_ok) TC_write_syslog("\n[vf] ERROR: VF_custom_register_methods retcode=%d", ifail);

	ifail = vf_baseline_post_actions_register();
	if (ifail != ITK_ok) TC_write_syslog("\n[vf] ERROR: vf_baseline_post_actions_register retcode=%d", ifail);

	tag_t obj = NULLTAG;
	ITK__convert_uid_to_tag("y7PA1U3j47MsRA", &obj);

	//ITK__convert_uid_to_tag("ByDAB2T847MsRA", &obj);
	if (obj)
	{
		tag_t gm = NULLTAG;
		char* username = NULL;
		SA_ask_current_groupmember(&gm);
		AOM_ask_value_string(gm, "user_name", &username);
		TC_write_syslog("\n [vf] username %s\n", username);
		if (tc_strcasecmp(username, "datamanager") != 0 && tc_strcasecmp(username, "infodba") != 0)
		{
			int status = TCEVENTMGR_post_event_2(obj, "Awp0Create_Baseline_Complete");
			TC_write_syslog("\n [vf] TCEVENTMGR_post_event_2 %d\n", status);
			EMH_ask_error_text(status, &error_text); TC_write_syslog("Failed due to following error:\n%s", error_text); SAFE_MEM_free(error_text);
			status = ITK_ok;
		}

		if (username) MEM_free(username);
	}
	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);
	return ifail;
}

extern "C" DLLAPI int VF_custom_register_callbacks()
{
	int status = ITK_ok;
	int decision = ONLY_CURRENT_CUSTOMIZATION;

	TC_write_syslog("\n Registering VF_custom...\n");
	printf("\nEnter %s", __FUNCTION__);

	/*register dll*/
	status = CUSTOM_register_exit("VF_custom", "USER_init_module", (CUSTOM_EXIT_ftn_t)post_user_init_module);



	printf("status=%d\n", status);
	printf("Leave %s\n", __FUNCTION__);
	return status;
}