/*
 * vf_custom.cxx
 *
 *  Created on: Oct 8, 2020
 *      Author: vinfastplm
 */

#include "vf_custom.hxx"

const char* ATTR_ST_APPROVED_DATE = "vf4_cost_approval_date";
const char* ATTR_ECR_RELEASE_DATE = "date_released";

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
