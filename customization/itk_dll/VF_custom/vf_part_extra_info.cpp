#include "VF_custom.h"

using namespace std;


bool is_part_of(const std::string& word, const std::string& sentence) {
	return sentence.find(word) != std::string::npos;
}

DLLAPI int vf_set_ecr_number(METHOD_message_t* msg, va_list args)
{
	TC_write_syslog("\n[VF] ENTER %s\n", __FUNCTION__);

	int ret_code = ITK_ok;

	tag_t ecr_rev = msg->object_tag;
	boolean is_approved_ecr = false;
	if (ecr_rev != NULL_TAG)
	{

		/*get preference VINF_ENABLE_PART_EXTRA_FORM*/
		char** allowed_objType = NULL;
		vector<std::string> vec_allowedObjT;
		int count = 0;
		CHECK_ITK(ret_code, PREF_ask_char_values("VINF_ENABLE_PART_EXTRA_FORM", &count, &allowed_objType));
		for (int i = 0; i < count; i++)
		{
			std::string one_value = allowed_objType[i];
			vec_allowedObjT.push_back(one_value);
		}
		SAFE_MEM_free(allowed_objType);


		/*get all proposal parts*/
		tag_t proposal_rela = NULLTAG;
		int count_proposal_part = 0;


		//check status is Approved to procceed
		int sz_rel_status = 0;
		Teamcenter::scoped_smptr<tag_t> rel_status_lst;
		CHECK_ITK(ret_code, AOM_ask_value_tags(ecr_rev, "release_status_list", &sz_rel_status, &rel_status_lst));
		for (int i = 0; i < sz_rel_status; i++)
		{
			Teamcenter::scoped_smptr<char> sz_status_tmp;
			CHECK_ITK(ret_code, RELSTAT_ask_release_status_type(rel_status_lst[i], &sz_status_tmp));
			if (tc_strcmp(sz_status_tmp.getString(), "Approved") == 0)
			{
				is_approved_ecr = true;
				break;
			}
		}

		if (!is_approved_ecr)
		{
			return ITK_ok;
		}

		Teamcenter::scoped_smptr<tag_t> lst_pro_rev;
		CHECK_ITK(ret_code, GRM_find_relation_type("Cm0HasProposal", &proposal_rela));
		CHECK_ITK(ret_code, GRM_list_secondary_objects_only(ecr_rev, proposal_rela, &count_proposal_part, &lst_pro_rev));

		Teamcenter::scoped_smptr<char> ecr_id;
		CHECK_ITK(ret_code, AOM_ask_value_string(ecr_rev, "item_id", &ecr_id));
		for (int i = 0; i < count_proposal_part; i++)
		{
			Teamcenter::scoped_smptr<char> object_type;
			CHECK_ITK(ret_code, WSOM_ask_object_type2(lst_pro_rev[i], &object_type));
			if (std::find(vec_allowedObjT.begin(), vec_allowedObjT.end(), object_type.getString()) == vec_allowedObjT.end())
			{
				continue;
			}

			/*get Part Extra Info Form for each part*/
			Teamcenter::scoped_smptr<char> proposal_itm_id;
			CHECK_ITK(ret_code, AOM_ask_value_string(lst_pro_rev[i], "item_id", &proposal_itm_id));
			TC_write_syslog("\n[VF] %s processing: %s\n", __FUNCTION__, proposal_itm_id.getString());

			tag_t pro_itm = NULL_TAG;
			CHECK_ITK(ret_code, ITEM_ask_item_of_rev(lst_pro_rev[i], &pro_itm));
			Teamcenter::scoped_smptr<char> proposal_itm_name;
			CHECK_ITK(ret_code, AOM_ask_value_string(pro_itm, "object_name", &proposal_itm_name));
			std::string str_prosal_itm_name(proposal_itm_name.get());
			const char* tmp = str_prosal_itm_name.c_str();
			tag_t part_extra_rela = NULLTAG;
			CHECK_ITK(ret_code, GRM_find_relation_type("VF4_part_extra_infor_rela", &part_extra_rela));
			tag_t* part_extra_info_form = nullptr;
			int sz_extra_form = 0;
			CHECK_ITK(ret_code, GRM_list_secondary_objects_only(pro_itm, part_extra_rela, &sz_extra_form, &part_extra_info_form));
			if (sz_extra_form == 0)
			{
				//TODO add logic to create form
				tag_t form_typ = NULLTAG;
				CHECK_ITK(ret_code, TCTYPE_find_type("VF4_part_extra_information", "Form", &form_typ));
				tag_t create_input = NULLTAG;
				CHECK_ITK(ret_code, TCTYPE_construct_create_input(form_typ, &create_input));
				CHECK_ITK(ret_code, TCTYPE_set_create_display_value(create_input, "object_name", 1, &tmp));
				tag_t new_form = NULLTAG;
				CHECK_ITK(ret_code, TCTYPE_create_object(create_input, &new_form));
				//TODO create relation
				tag_t newRela = NULLTAG;
				CHECK_ITK(ret_code, GRM_create_relation(pro_itm, new_form, part_extra_rela, NULLTAG, &newRela));

				CHECK_ITK(ret_code, AOM_refresh(new_form, true));
				CHECK_ITK(ret_code, AOM_set_value_string(new_form, "vf4_ecr_number", ecr_id.getString()));

				CHECK_ITK(ret_code, AOM_save_with_extensions(new_form));
				CHECK_ITK(ret_code, AOM_refresh(new_form, false));
			}
			else
			{
				Teamcenter::scoped_smptr<char> curr_value;
				CHECK_ITK(ret_code, AOM_ask_value_string(part_extra_info_form[0], "vf4_ecr_number", &curr_value));
				CHECK_ITK(ret_code, AOM_refresh(part_extra_info_form[0], true));
				if (curr_value != nullptr && curr_value.getString()[0] == '\0')
				{
					CHECK_ITK(ret_code, AOM_set_value_string(part_extra_info_form[0], "vf4_ecr_number", ecr_id.getString()));
				}
				else
				{
					if (!is_part_of(ecr_id.getString(), curr_value.getString()))
					{
						std::string new_value = std::string(curr_value.getString()).append(";").append(ecr_id.getString());
						CHECK_ITK(ret_code, AOM_set_value_string(part_extra_info_form[0], "vf4_ecr_number", new_value.c_str()));
					}
				}
				CHECK_ITK(ret_code, AOM_save_with_extensions(part_extra_info_form[0]));
				CHECK_ITK(ret_code, AOM_refresh(part_extra_info_form[0], false));
				SAFE_MEM_free(part_extra_info_form);
			}
		}
	}

	TC_write_syslog("\n[VF] LEAVE %s\n", __FUNCTION__);
	return ITK_ok;
}


DLLAPI int vf_set_ecn_number(METHOD_message_t* msg, va_list args)
{
	TC_write_syslog("\n[VF] ENTER %s\n", __FUNCTION__);

	int ret_code = ITK_ok;

	tag_t ecn_rev = msg->object_tag;
	boolean is_approved_ecn = false;
	if (ecn_rev != NULL_TAG)
	{

		/*get preference VINF_ENABLE_PART_EXTRA_FORM*/
		char** allowed_objType = NULL;
		vector<std::string> vec_allowedObjT;
		int count = 0;
		CHECK_ITK(ret_code, PREF_ask_char_values("VINF_ENABLE_PART_EXTRA_FORM", &count, &allowed_objType));
		for (int i = 0; i < count; i++)
		{
			std::string one_value = allowed_objType[i];
			vec_allowedObjT.push_back(one_value);
		}
		SAFE_MEM_free(allowed_objType);


		/*get all proposal parts*/
		tag_t solution_rela = NULLTAG;
		int sz_sol_part = 0;


		//check status is Approved to procceed
		int sz_rel_status = 0;
		Teamcenter::scoped_smptr<tag_t> rel_status_lst;
		CHECK_ITK(ret_code, AOM_ask_value_tags(ecn_rev, "release_status_list", &sz_rel_status, &rel_status_lst));
		for (int i = 0; i < sz_rel_status; i++)
		{
			Teamcenter::scoped_smptr<char> sz_status_tmp;
			CHECK_ITK(ret_code, RELSTAT_ask_release_status_type(rel_status_lst[i], &sz_status_tmp));
			if (tc_strcmp(sz_status_tmp.getString(), "Approved") == 0)
			{
				is_approved_ecn = true;
				break;
			}
		}

		if (!is_approved_ecn)
		{
			return ITK_ok;
		}

		Teamcenter::scoped_smptr<tag_t> lst_sol_rev;
		CHECK_ITK(ret_code, GRM_find_relation_type("CMHasSolutionItem", &solution_rela));
		CHECK_ITK(ret_code, GRM_list_secondary_objects_only(ecn_rev, solution_rela, &sz_sol_part, &lst_sol_rev));

		Teamcenter::scoped_smptr<char> ecn_id;
		CHECK_ITK(ret_code, AOM_ask_value_string(ecn_rev, "item_id", &ecn_id));
		for (int i = 0; i < sz_sol_part; i++)
		{
			Teamcenter::scoped_smptr<char> object_type;
			CHECK_ITK(ret_code, WSOM_ask_object_type2(lst_sol_rev[i], &object_type));
			if (std::find(vec_allowedObjT.begin(), vec_allowedObjT.end(), object_type.getString()) == vec_allowedObjT.end())
			{
				continue;
			}


			/*get Part Extra Info Form for each part*/
			Teamcenter::scoped_smptr<char> sol_itm_id;
			CHECK_ITK(ret_code, AOM_ask_value_string(lst_sol_rev[i], "item_id", &sol_itm_id));
			TC_write_syslog("\n[VF] %s processing: %s\n", __FUNCTION__, sol_itm_id.getString());

			tag_t sol_itm = NULL_TAG;
			CHECK_ITK(ret_code, ITEM_ask_item_of_rev(lst_sol_rev[i], &sol_itm));
			Teamcenter::scoped_smptr<char> sol_itm_name;
			CHECK_ITK(ret_code, AOM_ask_value_string(sol_itm, "object_name", &sol_itm_name));
			std::string str_sol_itm_name(sol_itm_name.get());
			const char* tmp = str_sol_itm_name.c_str();
			tag_t part_extra_rela = NULLTAG;
			CHECK_ITK(ret_code, GRM_find_relation_type("VF4_part_extra_infor_rela", &part_extra_rela));
			tag_t* part_extra_info_form = nullptr;
			int sz_extra_form = 0;
			CHECK_ITK(ret_code, GRM_list_secondary_objects_only(sol_itm, part_extra_rela, &sz_extra_form, &part_extra_info_form));
			if (sz_extra_form == 0)
			{
				//TODO add logic to create form
				tag_t form_typ = NULLTAG;
				CHECK_ITK(ret_code, TCTYPE_find_type("VF4_part_extra_information", "Form", &form_typ));
				tag_t create_input = NULLTAG;
				CHECK_ITK(ret_code, TCTYPE_construct_create_input(form_typ, &create_input));
				CHECK_ITK(ret_code, TCTYPE_set_create_display_value(create_input, "object_name", 1, &tmp));
				tag_t new_form = NULLTAG;
				CHECK_ITK(ret_code, TCTYPE_create_object(create_input, &new_form));
				//TODO create relation
				tag_t newRela = NULLTAG;
				CHECK_ITK(ret_code, GRM_create_relation(sol_itm, new_form, part_extra_rela, NULLTAG, &newRela));

				CHECK_ITK(ret_code, AOM_refresh(new_form, true));
				CHECK_ITK(ret_code, AOM_set_value_string(new_form, "vf4_ecn_number", ecn_id.getString()));

				CHECK_ITK(ret_code, AOM_save_with_extensions(new_form));
				CHECK_ITK(ret_code, AOM_refresh(new_form, false));
			}
			else
			{
				Teamcenter::scoped_smptr<char> curr_value;
				CHECK_ITK(ret_code, AOM_ask_value_string(part_extra_info_form[0], "vf4_ecn_number", &curr_value));
				CHECK_ITK(ret_code, AOM_refresh(part_extra_info_form[0], true));
				if (curr_value != nullptr && curr_value.getString()[0] == '\0')
				{
					CHECK_ITK(ret_code, AOM_set_value_string(part_extra_info_form[0], "vf4_ecn_number", ecn_id.getString()));
				}
				else
				{
					if (!is_part_of(ecn_id.getString(), curr_value.getString()))
					{
						std::string new_value = std::string(curr_value.getString()).append(";").append(ecn_id.getString());
						CHECK_ITK(ret_code, AOM_set_value_string(part_extra_info_form[0], "vf4_ecn_number", new_value.c_str()));
					}
				}
				CHECK_ITK(ret_code, AOM_save_with_extensions(part_extra_info_form[0]));
				CHECK_ITK(ret_code, AOM_refresh(part_extra_info_form[0], false));
				SAFE_MEM_free(part_extra_info_form);
			}
		}
	}

	TC_write_syslog("\n[VF] LEAVE %s\n", __FUNCTION__);
	return ITK_ok;
}


DLLAPI int vf_set_maturity_level(METHOD_message_t* msg, va_list args)
{
	TC_write_syslog("\n[VF] ENTER %s\n", __FUNCTION__);

	map<string, string> status_map;
	status_map["VF4_Sourcing"] = "Sourcing";
	status_map["VL5_P"] = "P";
	status_map["PR4D_P"] = "P";
	status_map["Vl5_I"] = "I";
	status_map["VL5_PR"] = "PR";
	status_map["PR4D_PR"] = "PR";
	status_map["Vf6_PPR"] = "PPR";
	status_map["VF5_P"] = "P";
	int ret_code = ITK_ok;

	tag_t part_rev = msg->object_tag;


	/*get preference VINF_ENABLE_PART_EXTRA_FORM*/
	char** allowed_objType = NULL;
	vector<std::string> vec_allowedObjT;
	int count = 0;
	CHECK_ITK(ret_code, PREF_ask_char_values("VINF_ENABLE_PART_EXTRA_FORM", &count, &allowed_objType));
	for (int i = 0; i < count; i++)
	{
		std::string one_value = allowed_objType[i];
		vec_allowedObjT.push_back(one_value);
	}
	SAFE_MEM_free(allowed_objType);

	Teamcenter::scoped_smptr<char> object_type;
	CHECK_ITK(ret_code, WSOM_ask_object_type2(part_rev, &object_type));
	if (std::find(vec_allowedObjT.begin(), vec_allowedObjT.end(), object_type.getString()) == vec_allowedObjT.end())
	{
		return ITK_ok;
	}

	tag_t part_itm = NULL_TAG;
	CHECK_ITK(ret_code, ITEM_ask_item_of_rev(part_rev, &part_itm));

	Teamcenter::scoped_smptr<char> part_name;
	CHECK_ITK(ret_code, AOM_ask_value_string(part_itm, "object_name", &part_name));
	std::string str_part_name(part_name.get());
	const char* tmp = str_part_name.c_str();

	tag_t part_extra_rela = NULLTAG;
	CHECK_ITK(ret_code, GRM_find_relation_type("VF4_part_extra_infor_rela", &part_extra_rela));
	tag_t* part_extra_info_form = nullptr;
	int sz_extra_form = 0;
	CHECK_ITK(ret_code, GRM_list_secondary_objects_only(part_itm, part_extra_rela, &sz_extra_form, &part_extra_info_form));

	tag_t part_extra_form = NULL_TAG;
	if (sz_extra_form == 0)
	{
		//TODO add logic to create form
		tag_t form_typ = NULLTAG;
		CHECK_ITK(ret_code, TCTYPE_find_type("VF4_part_extra_information", "Form", &form_typ));
		tag_t create_input = NULLTAG;
		CHECK_ITK(ret_code, TCTYPE_construct_create_input(form_typ, &create_input));
		CHECK_ITK(ret_code, TCTYPE_set_create_display_value(create_input, "object_name", 1, &tmp));
		CHECK_ITK(ret_code, TCTYPE_create_object(create_input, &part_extra_form));
		//TODO create relation
		tag_t newRela = NULLTAG;
		CHECK_ITK(ret_code, GRM_create_relation(part_itm, part_extra_form, part_extra_rela, NULLTAG, &newRela));
	}
	else
	{
		part_extra_form = part_extra_info_form[0];
		SAFE_MEM_free(part_extra_info_form);
	}

	int sz_rel_status = 0;
	Teamcenter::scoped_smptr<tag_t> rel_status_lst;
	CHECK_ITK(ret_code, AOM_ask_value_tags(part_rev, "release_status_list", &sz_rel_status, &rel_status_lst));
	for (int i = 0; i < sz_rel_status; i++)
	{
		Teamcenter::scoped_smptr<char> curr_value_of_status;
		Teamcenter::scoped_smptr<char> curr_value_of_relDate;
		CHECK_ITK(ret_code, AOM_ask_value_string(part_extra_form, "vf4_maturity_level", &curr_value_of_status));
		CHECK_ITK(ret_code, AOM_ask_value_string(part_extra_form, "vf4_all_release_date", &curr_value_of_relDate));

		Teamcenter::scoped_smptr<char> sz_status_tmp;
		date_t rel_date;
		char* str_rel_date = NULL;
		CHECK_ITK(ret_code, AOM_ask_name(rel_status_lst[i], &sz_status_tmp));
		CHECK_ITK(ret_code, AOM_ask_value_date(rel_status_lst[i], "date_released", &rel_date));
		CHECK_ITK(ret_code, DATE_date_to_string(rel_date, "%d-%b-%Y %H:%M", &str_rel_date));
		if (status_map.count(sz_status_tmp.getString()))
		{
			CHECK_ITK(ret_code, AOM_refresh(part_extra_form, true));
			if (curr_value_of_status != nullptr && curr_value_of_status.getString()[0] == '\0')
			{
				CHECK_ITK(ret_code, AOM_set_value_string(part_extra_form, "vf4_maturity_level", status_map[sz_status_tmp.getString()].c_str()));
				CHECK_ITK(ret_code, AOM_set_value_string(part_extra_form, "vf4_all_release_date", str_rel_date));
			}
			else
			{
				if (!is_part_of(status_map[sz_status_tmp.getString()].c_str(), curr_value_of_status.getString()))
				{
					std::string new_value_status = std::string(curr_value_of_status.getString()).append(";").append(status_map[sz_status_tmp.getString()].c_str());
					std::string new_value_relDate = std::string(curr_value_of_relDate.getString()).append(";").append(str_rel_date);
					CHECK_ITK(ret_code, AOM_set_value_string(part_extra_form, "vf4_maturity_level", new_value_status.c_str()));
					CHECK_ITK(ret_code, AOM_set_value_string(part_extra_form, "vf4_all_release_date", new_value_relDate.c_str()));
				}
			}
			CHECK_ITK(ret_code, AOM_save_with_extensions(part_extra_form));
			CHECK_ITK(ret_code, AOM_refresh(part_extra_form, false));
		}
		SAFE_MEM_free(str_rel_date);
	}
	TC_write_syslog("\n[VF] LEAVE %s\n", __FUNCTION__);
	return ITK_ok;
}