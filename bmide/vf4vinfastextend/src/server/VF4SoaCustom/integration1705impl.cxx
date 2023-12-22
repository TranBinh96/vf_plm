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

#include <integration1705impl.hxx>

using namespace VF4::Soa::Custom::_2017_05;
using namespace Teamcenter::Soa::Server;

#include <map>
#include <string>
#include <vector>
#include "qry/qry.h"
#include "tccore/aom_prop.h"
#include "tccore/aom.h"
#include "tccore/item.h"
#include "fclasses/tc_string.h"
#include "user_exits\epm_toolkit_utils.h"
#include "tc/preferences.h"
#include "pom/pom/pom.h"
#include <iostream>
#include <fstream>
#include <ctime>
#include <set>
#include <stdio.h>
#include <stdlib.h>
#include <base_utils/TcResultStatus.hxx>
#include <base_utils/ScopedSmPtr.hxx>
#include <base_utils/ScopedPtr.hxx>
#include "vf_custom.hxx"
using namespace std;
#define EXIT_FAILURE 1

void initSourceParts(const string &itemIDsStr, vector<tag_t> &sourceParts)
{
	tag_t tQuery = NULLTAG;
	int iNItem = 0;
	const char* queryName = "Source Part";
	Teamcenter::scoped_smptr<tag_t> ptItem;

	ERROR_CHECK(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		exit(EXIT_FAILURE);
	}

	char* queryEntries[] = {"VF Part Number"};
	char** queryValues = (char**) MEM_alloc(sizeof(char*)*1);
	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)itemIDsStr.length() + 1));

	tc_strcpy(queryValues[0], "");
	tc_strcpy(queryValues[0], itemIDsStr.c_str());

	ERROR_CHECK(QRY_execute(tQuery, 1, queryEntries, queryValues, &iNItem, &ptItem));

	for (int i = 0; i < iNItem; i++)
	{
		sourceParts.push_back(ptItem[i]);
	}

	SAFE_SM_FREE(queryValues[0]);
	SAFE_SM_FREE(queryValues);
}

void initParts(const string &itemIDsStr, const string &partType, vector<tag_t> &parts)
{
	tag_t tQuery = NULLTAG;
	int iNItem = 0;
	const char* queryName = "Item...";
	Teamcenter::scoped_smptr<tag_t> ptItem;

	ERROR_CHECK(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		exit(EXIT_FAILURE);
	}

	char* queryEntries[] = {"Item ID", "Type"};
	char** queryValues = (char**) MEM_alloc(sizeof(char*)*2);
	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)itemIDsStr.length() + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)partType.length() + 1));

	tc_strcpy(queryValues[0], itemIDsStr.c_str());
	tc_strcpy(queryValues[1], partType.c_str());

	ERROR_CHECK(QRY_execute(tQuery, 1, queryEntries, queryValues, &iNItem, &ptItem));

	for (int i = 0; i < iNItem; i++)
	{
		parts.push_back(ptItem[i]);
	}

	SAFE_SM_FREE(queryValues[0]);
	SAFE_SM_FREE(queryValues[1]);
	SAFE_SM_FREE(queryValues);
}

tag_t getSupplier(const char *supplierCode)
{
	tag_t tQuery = NULLTAG;
	int iNItem = 0;
	char* queryName = "Vendor";
	Teamcenter::scoped_smptr<tag_t> ptItem;

	ERROR_CHECK(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		exit(EXIT_FAILURE);
	}

	char* queryEntries[] = {"Vendor Code"};
	char** queryValues = (char**) MEM_alloc(sizeof(char*)*1);
	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(supplierCode) + 1));

	tc_strcpy(queryValues[0], "");
	tc_strcpy(queryValues[0], supplierCode);

	ERROR_CHECK(QRY_execute(tQuery, 1, queryEntries, queryValues, &iNItem, &ptItem));

	tag_t supplier = NULLTAG;
	if (iNItem == 1)
	{
		supplier = ptItem[0];
	}

	SAFE_SM_FREE(queryValues[0]);
	SAFE_SM_FREE(queryValues);

	return supplier;
}

std::string formatTime()
{
	std::string str;
	char buffer[80];
	std::time_t now;
	struct std::tm when;

	time(&now);
	localtime_s(&when, &now);

	strftime(buffer, sizeof(buffer), "%d%m%Y%H%M%S", &when);
	str = std::string(buffer);

	return str;
}

void exportJson(std::string uid, std::string part_number, std::string supplier_code, std::string pathfile, int iCounter, int randomNum)
{
	TC_write_syslog("\n[VF-SupplierNominate] DOWNLOAD JSON: %s", pathfile.c_str());
	std::string fullpathfile = "";
	std::string content = "";
	std::string jsonStr = "";
	std::string fileName = supplier_code + "_" + part_number + "_" + formatTime() + "_" + to_string(iCounter) + "_" + to_string(randomNum) + ".json";

	fullpathfile = pathfile + fileName;
	TC_write_syslog("\n[VF-SupplierNominate] FILE PATH : %s", fullpathfile.c_str());

	jsonStr = "{\"type\":\"ITEM_SUPPLIER_UPDATE\",\"uid\":\"" + uid + "\"}";


	tag_t logingFormInput = NULLTAG;
	std::string loginFormName = "VF_NOMINATE_SUPP_LOG_" + fileName;
	//Form
	tag_t formType = NULLTAG;
	TCTYPE_find_type("Form", "Form", &formType);
	TCTYPE_construct_create_input(formType, &logingFormInput);
	try
	{
		std::ofstream o(fullpathfile);
		content.append(jsonStr);
		o << content << std::endl;
		o.flush();
		o.close();
		//   create a form for logging with name is the fileName succes
		loginFormName.append(".success");
		AOM_set_value_string(logingFormInput, "object_name", loginFormName.c_str());
	}
	catch(std::exception ex)
	{
		//   create a form for logging with name is the fileName fail
		loginFormName.append(".fail");
		AOM_set_value_string(logingFormInput, "object_name", loginFormName.c_str());
	}

	tag_t logingForm = NULLTAG;
	TCTYPE_create_object(logingFormInput, &logingForm);
	AOM_save_with_extensions(logingForm);
	AOM_unlock(logingForm);
	//in case no exception
	//   create a form for logging with name is the fileName
	TC_write_syslog("\n[VF-SupplierNominate] DOWNLOADED JSON");
}

bool IntegrationImpl::nominateSupplierToSQP ( const NominateSupplierInput& nominateSupplierInput )
{
	TC_write_syslog("\n[VF-SupplierNominate] ENTER: %s",__FUNCTION__);
	int iCounter = 0;
	bool result = true;
	std::string itemIDsStr;
	std::set<std::string> unqSet;
	const string partType = "VF4_Design;VF3_manuf_part";

	for (string sourcePart : nominateSupplierInput.vfPartNumbers)
	{
		itemIDsStr.append(sourcePart).append(";");
	}

	vector<tag_t> sourceParts;
	vector<tag_t> parts;

	initSourceParts(itemIDsStr, sourceParts);
	initParts(itemIDsStr, partType, parts);

	if (sourceParts.size() <= 0) {
	   TC_write_syslog("\n[VF-SupplierNominate] - NO PART TO NOMINATE");
	   TC_write_syslog("\n[VF-SupplierNominate] LEAVE: %s",__FUNCTION__);
	   return false;
	}

	Teamcenter::scoped_smptr<char> item_id;
	Teamcenter::scoped_smptr<char> supplier_name;
	Teamcenter::scoped_smptr<char> part_number;
	Teamcenter::scoped_smptr<char> hq_manuf_location;
	Teamcenter::scoped_smptr<char> supplier_code;
	const char* VF_SUPPLIER_RELA = "VF4_Supplier_Info_Relation";
	const char* SUPPLIER_INFO_TYPE = "VF4_Supplier_Info";

	tag_t supplier = NULLTAG;
	tag_t supplier_item = NULLTAG;

	Teamcenter::scoped_smptr<char> prefNomSupFolder;

	supplier = getSupplier(nominateSupplierInput.supplierCode.c_str());
	if (supplier == NULLTAG) {
	   TC_write_syslog("\n[VF-SupplierNominate] - Supplier Code - %s does not exists", nominateSupplierInput.supplierCode.c_str());
	   TC_write_syslog("\n[VF-SupplierNominate] LEAVE: %s",__FUNCTION__);
	   return false;
	}
	ERROR_CHECK(PREF_ask_char_value("VF_NominatedSupplierFolder", 0, &prefNomSupFolder));
	TC_write_syslog("prefNominate Supplier Folder Path : [%s]\n", prefNomSupFolder.getString());

	ERROR_CHECK(AOM_ask_value_string(supplier, "item_id", &item_id));
	ERROR_CHECK(ITEM_find_item(item_id.getString(), &supplier_item));
	ERROR_CHECK(AOM_ask_value_string(supplier_item, "object_name", &supplier_name));
	TC_write_syslog("\n[VF-SupplierNominate] - Supplier Name - %s", supplier_name.getString());

	ERROR_CHECK(AOM_ask_value_string(supplier_item, "vf4_country", &hq_manuf_location));
	TC_write_syslog("\n[VF-SupplierNominate] - Manufacturing Location - %s", hq_manuf_location.getString());

//	tag_t attr_id_supplier_code = NULLTAG;
//	tag_t attr_id_supplier_name = NULLTAG;
//	tag_t attr_id_supplier_country = NULLTAG;
//	tag_t attr_manuf_location = NULLTAG;
	logical isBlankSupplierCode = false;

	if (tc_strcmp(supplier_name.getString(), "BLANK") == 0)
		isBlankSupplierCode = true;

	// Use current time as seed for random generator
    srand(static_cast<unsigned int>(time(NULL)));

    for (tag_t part : parts)
    {
    	vector<tag_t> supplierForms = getRelatedForms(part, VF_SUPPLIER_RELA, SUPPLIER_INFO_TYPE);
    	tag_t supplierForm = NULLTAG;
    	Teamcenter::scoped_smptr<char> partObjectString;
    	//Teamcenter::scoped_smptr<tag_t> item_type_tag;
    	ERROR_CHECK(AOM_ask_value_string(part, "object_string", &partObjectString));
    	if (supplierForms.size() == 0)
    	{
    		tag_t newForm = createCostForm(SUPPLIER_INFO_TYPE, partObjectString.getString());
    		createRelation(newForm, part, VF_SUPPLIER_RELA);
    		supplierForm = newForm;
    	}
    	else
    	{
    		supplierForm = supplierForms[0];
    	}

    	if (supplierForm != NULLTAG)
    	{
    		int isModifable = (AOM_refresh(supplierForm, TRUE) == ITK_ok);
    		if (!isModifable) continue;

    		ERROR_CHECK(AOM_refresh(supplierForm, TRUE));
    		ERROR_CHECK(AOM_set_value_string(supplierForm, "vf4_itm_supplier_code", nominateSupplierInput.supplierCode.c_str()));
    		ERROR_CHECK(AOM_set_value_string(supplierForm, "vf4_itm_supplier_name", supplier_name.getString()));
    		ERROR_CHECK(AOM_set_value_string(supplierForm, "vf4_manuf_location", tc_strlen((const char*)nominateSupplierInput.manufacturingLocation.c_str()) > 0 ? (const char*)nominateSupplierInput.manufacturingLocation.c_str() : (const char*)hq_manuf_location.getString()));
    		ERROR_CHECK(AOM_set_value_string(supplierForm, "vf4_supplier_country", tc_strlen(hq_manuf_location.getString()) > 0 ? hq_manuf_location.getString() : ""));
    		ERROR_CHECK(AOM_save_with_extensions(supplierForm));
    		ERROR_CHECK(AOM_refresh(supplierForm, FALSE));
    	}

    }

	for (tag_t sourcePart : sourceParts)
	{
//		result = (AOM_refresh(sourcePart, TRUE) == ITK_ok);
//		if (!result) continue;
//
//		ERROR_CHECK(POM_attr_id_of_attr("vf4_supplier_code", "VF4_line_itemRevision", &attr_id_supplier_code));
//		ERROR_CHECK(POM_attr_id_of_attr("vf4_bmw_nominated_supplier", "VF4_line_itemRevision", &attr_id_supplier_name));
//		ERROR_CHECK(POM_attr_id_of_attr("vf4_supplier_country", "VF4_line_itemRevision", &attr_id_supplier_country));
//		ERROR_CHECK(POM_attr_id_of_attr("vf4_manuf_location", "VF4_line_itemRevision", &attr_manuf_location));
//
//		if (!isBlankSupplierCode)
//		{
//			ERROR_CHECK(POM_set_attr_string(1, &sourcePart, attr_id_supplier_code, (const char*)nominateSupplierInput.supplierCode.c_str()));
//			ERROR_CHECK(POM_set_attr_string(1, &sourcePart, attr_id_supplier_name, supplier_name.getString()));
//			ERROR_CHECK(POM_set_attr_string(1, &sourcePart, attr_id_supplier_country, tc_strlen(hq_manuf_location.getString()) > 0 ? hq_manuf_location.getString() : ""));
//			ERROR_CHECK(POM_set_attr_string(1, &sourcePart, attr_manuf_location, tc_strlen((const char*)nominateSupplierInput.manufacturingLocation.c_str()) > 0 ? (const char*)nominateSupplierInput.manufacturingLocation.c_str() : (const char*)hq_manuf_location.getString()));
//
//			//ERROR_CHECK(AOM_set_value_string(sourcePart, "vf4_supplier_country", tc_strlen(hq_manuf_location.getString()) > 0 ? hq_manuf_location.getString() : ""));
//			//ERROR_CHECK(AOM_set_value_string(sourcePart, "vf4_manuf_location", tc_strlen((const char*)nominateSupplierInput.manufacturingLocation.c_str()) > 0 ? (const char*)nominateSupplierInput.manufacturingLocation.c_str() : (const char*)hq_manuf_location.getString()));
//		}
//		else
//		{
//			ERROR_CHECK(POM_set_attr_string(1, &sourcePart, attr_id_supplier_code, ""));
//			ERROR_CHECK(POM_set_attr_string(1, &sourcePart, attr_id_supplier_name, ""));
//			ERROR_CHECK(POM_set_attr_string(1, &sourcePart, attr_id_supplier_country, ""));
//			ERROR_CHECK(POM_set_attr_string(1, &sourcePart, attr_manuf_location, ""));
//
//			//ERROR_CHECK(AOM_set_value_string(sourcePart, "vf4_supplier_country", ""));
//			//ERROR_CHECK(AOM_set_value_string(sourcePart, "vf4_manuf_location", ""));
//		}
//
//		ERROR_CHECK(AOM_save_without_extensions(sourcePart));
//		ERROR_CHECK(AOM_refresh(sourcePart, FALSE));
		TC_write_syslog("[VF-SupplierNominate] - Nomination Completed\n");


		TC_write_syslog("[VF-SupplierNominate] - Transferring part to SQ Portal\n");

		ERROR_CHECK(AOM_ask_value_string(sourcePart, "vf4_bom_vfPartNumber", &part_number));
		ERROR_CHECK(AOM_ask_value_string(sourcePart, "vf4cp_itm_supplier_code", &supplier_code));
		std::string transSupCode = isBlankSupplierCode ? "BLANK" : supplier_code.getString();

		std::string setKey;
		setKey.assign(transSupCode);
		setKey.append("_");
		setKey.append(part_number.getString());

		if (unqSet.find(setKey) == unqSet.end())
		{
			int randomNum = 0;
			Teamcenter::scoped_smptr<char> uid;
			randomNum = rand();

            ERROR_CHECK(POM_tag_to_uid(sourcePart, &uid));

			unqSet.insert(setKey);
			if (nominateSupplierInput.isTriggerKarafEvent)
			{
				exportJson(uid.getString(), part_number.getString(), transSupCode, prefNomSupFolder.getString(), iCounter, randomNum);
			}
		}

		iCounter++;
	}

	unqSet.clear();

	TC_write_syslog("\n[VF-SupplierNominate] LEAVE: %s",__FUNCTION__);
    return result;
}

bool IntegrationImpl::updateQcheckerProperty ( const BusinessObjectRef<Teamcenter::ItemRevision>& designRevision, const std::string result )
{
    // TODO implement operation
	ERROR_CHECK(AOM_refresh(designRevision.tag(), true));
	ERROR_CHECK(POM_set_env_info(POM_bypass_attr_update,false,0,0.0,NULL_TAG,NULL));
	ERROR_CHECK(AOM_set_value_string(designRevision.tag(), "vf4_q_checker", result.c_str()));
	ERROR_CHECK(AOM_save_without_extensions(designRevision.tag()));
	ERROR_CHECK(AOM_refresh(designRevision.tag(), false));
	ERROR_CHECK(POM_set_env_info(POM_bypass_attr_update,true,0,0.0,NULL_TAG,NULL));
	return true;
}

void getSourcePart(std::string source, std::vector<tag_t> &list_src)
{
	TC_write_syslog("\n Enter getSourcePart\n");
	int num_obj = 0;
	char** entries = NULL;
	char** values = NULL;
	tag_t query = NULLTAG;
	Teamcenter::scoped_smptr<tag_t> result;

	TC_write_syslog("\n Find Query");
	ERROR_CHECK(QRY_find2("Source Part", &query));

	entries = (char**)MEM_alloc(sizeof(char *) * 1);
	values = (char**)MEM_alloc(sizeof(char *) * 1);

	entries[0] = (char *)MEM_alloc(sizeof(char) * ((int)tc_strlen("VF Part Number") + 1));
	tc_strcpy(entries[0], "");
	tc_strcpy(entries[0], "VF Part Number");

	values[0] = (char*)MEM_alloc(sizeof(char) * ((int)tc_strlen(source.c_str()) + 1));
	tc_strcpy(values[0], "");
	tc_strcpy(values[0], source.c_str());

	TC_write_syslog("\n Execute Query");
	ERROR_CHECK(QRY_execute(query, 1, entries, values, &num_obj, &result));

	TC_write_syslog("\n Put to vector");
	for(int i = 0; i < num_obj; i++)
	{
		list_src.push_back(result[i]);
	}

	TC_write_syslog("\n Leave getSourcePart");

	SAFE_SM_FREE(entries[0]);
	SAFE_SM_FREE(values[0]);
	SAFE_SM_FREE(entries);
	SAFE_SM_FREE(values);
}

bool IntegrationImpl::removeSupplierInfo(const std::string sourcePartNo)
{
	TC_write_syslog("\n Enter removeSupplierInfo");
	std::vector<tag_t> srclist;

	getSourcePart(sourcePartNo, srclist);

	for(int j = 0; j < srclist.size(); j++)
	{
		tag_t item = NULLTAG;
		item = srclist.at(j);
		ERROR_CHECK(AOM_refresh(item, TRUE));
		TC_write_syslog("\n remove vf4_supplier_code\n");
		ERROR_CHECK(AOM_set_value_string(item, "vf4_supplier_code", ""));
		TC_write_syslog("\n remove vf4_supplier_country\n");
		ERROR_CHECK(AOM_set_value_string(item, "vf4_supplier_country", ""));
		TC_write_syslog("\n remove vf4_manuf_location\n");
		ERROR_CHECK(AOM_set_value_string(item, "vf4_manuf_location", ""));
		TC_write_syslog("\n remove vf4_bmw_nominated_supplier\n");
		ERROR_CHECK(AOM_set_value_string(item, "vf4_bmw_nominated_supplier", ""));
		ERROR_CHECK(AOM_save_without_extensions(item));
	}

	std::vector<tag_t>().swap(srclist);
	TC_write_syslog("\n Leave removeSupplierInfo");

	return true;
}
