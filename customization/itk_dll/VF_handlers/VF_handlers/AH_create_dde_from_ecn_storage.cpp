	#include "Vinfast_Custom.h"

	struct DDE_Create_Input {
		std::string item_id;
		std::string object_name;
		std::string object_type;
		std::string object_desc;
		std::string status;
		std::string revision_rule;
		std::string export_type;
	};

	string getUser(tag_t tRoot)
	{
		int	iRetCode = ITK_ok;
		tag_t* ptAllTaskTags = NULLTAG;
		int iAllTasks = 0;
		char* mail_user = "";
		CHECK_ITK(iRetCode, EPM_ask_sub_tasks(tRoot, &iAllTasks, &ptAllTaskTags));
		if (ptAllTaskTags != NULLTAG)
		{
			for (int iLoopAllTasks = 0; iLoopAllTasks < iAllTasks; iLoopAllTasks++)
			{
				char* object_name = NULL;
				CHECK_ITK(iRetCode, AOM_ask_value_string(ptAllTaskTags[iLoopAllTasks], "object_name", &object_name));
				int		iAllSubTasks = 0;
				tag_t* ptAllSubTaskTags = NULL;
				if (tc_strcmp(object_name, "[Quotation] Send Data to Supplier") == 0 || tc_strcmp(object_name, "Data To Vinfast and Suppliers") == 0) {
					CHECK_ITK(iRetCode, EPM_ask_sub_tasks(ptAllTaskTags[iLoopAllTasks], &iAllSubTasks, &ptAllSubTaskTags));
					for (int iLoopAllSubTasks = 0; iLoopAllSubTasks < iAllSubTasks; iLoopAllSubTasks++)
					{
						int		iNumAttachs = 0;
						tag_t* ptAttachsTagList = NULL;
						//get signoff attachments
						CHECK_ITK(iRetCode, EPM_ask_attachments(ptAllSubTaskTags[iLoopAllSubTasks], EPM_signoff_attachment, &iNumAttachs, &ptAttachsTagList));
						for (int iUsers = 0; iUsers < iNumAttachs; iUsers++)
						{
							tag_t userTag = NULL;
							tag_t tOwningUser = NULL;
							CHECK_ITK(iRetCode, AOM_ask_value_tag(ptAllTaskTags[iLoopAllTasks], "owning_user", &tOwningUser));
							CHECK_ITK(iRetCode, EPM_get_user_email_addr(tOwningUser, &mail_user));
						}
					}
				}
			}
		}
		return  mail_user;
	}

	auto createDDEObject(DDE_Create_Input dde_input, tag_t& dde_item) -> int;
	auto setRelated(tag_t primaryObject, tag_t secondaryObject, tag_t relationObject) -> int;

	tag_t getVendor(std::string vendorCode) {
		tag_t vendor = NULLTAG;
		int iRetCode = ITK_ok;
		int iCount = 0;
		tag_t tQuery = NULLTAG;
		tag_t* tQryResults;

		char** queryValues = (char**)MEM_alloc(sizeof(char*) * 1);
		queryValues[0] = (char*)MEM_alloc(tc_strlen(vendorCode.c_str()) + 1);
		queryValues[0] = tc_strcpy(queryValues[0], vendorCode.c_str());

		CHECK_ITK(iRetCode, QRY_find2("Vendor", &tQuery));
		char* queryEntriesVender[] = { "Vendor Code" };
		if (tQuery == NULLTAG) {
			TC_write_syslog("ERROR: Cannot find query %s:\n", "Vendor");
			return ITK_ok;
		}
		CHECK_ITK(iRetCode, QRY_execute(tQuery, 1, queryEntriesVender, queryValues, &iCount, &tQryResults));
		tag_t contact_rela_type = NULLTAG;
		int iVenderCode = 0;
		tag_t* venderCode = NULLTAG;
		CHECK_ITK(iRetCode, GRM_find_relation_type("ContactInCompany", &contact_rela_type));
		CHECK_ITK(iRetCode, GRM_list_secondary_objects_only(tQryResults[0], contact_rela_type, &iVenderCode, &venderCode));
		if (iVenderCode > 0)
		{
			for (int iVender = 0; iVender < iVenderCode; iVender++)
			{
				char* lastname = NULL;
				char* first_name = NULL;
				CHECK_ITK(iRetCode, AOM_ask_value_string(venderCode[iVender], "object_name", &lastname));
				CHECK_ITK(iRetCode, AOM_ask_value_string(venderCode[iVender], "first_name", &first_name));
				std::string name = lastname;
				if (tc_strcmp(first_name, "VINES") == 0 || tc_strcmp(first_name, "SCF") == 0 || tc_strstr(lastname, "0002"))
				{
					char* email_address = NULL;
					CHECK_ITK(iRetCode, AOM_ask_value_string(venderCode[iVender], "email_address", &email_address));
					if (tc_strcmp(email_address, "change_my_mail@dummy.net") == 0)
						continue;
					else
						vendor = venderCode[iVender];
				}
			}
		}
		else {
			TC_write_syslog("[] More than 1 Items found for given Vendor Code [%s]\n", vendorCode.c_str());
		}
		SAFE_SM_FREE(queryValues[0]);
		SAFE_SM_FREE(queryValues);
		return vendor;
	}

	extern int AH_create_dde_from_ecn_storage(EPM_action_message_t msg) {
		int	iRetCode = ITK_ok;
		char ErrorMessage[2048] = "";
		tag_t tRootTask = NULLTAG;
		const char* ecnObjType = NULLTAG;
		const char* ddeObjType = NULLTAG;
		char* pcRevisionRule = "";
		char* pcMail = "";
		int iNumArgs = 0;
		stringstream mailBody;
		vector<string> szMailList;
		std::string vendorList;
		iNumArgs = TC_number_of_arguments(msg.arguments);
		if (iNumArgs > 0)
		{
			char* pcFlag = NULL;
			char* pcValue = NULL;
			for (int i = 0; i < iNumArgs; i++)
			{
				CHECK_ITK(iRetCode, ITK_ask_argument_named_value(TC_next_argument(msg.arguments), &pcFlag, &pcValue));
				if (iRetCode == ITK_ok)
				{
					if (tc_strcasecmp(pcFlag, "revision_rule") == 0 && pcValue != NULL)
					{
						pcRevisionRule = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
						tc_strcpy(pcRevisionRule, pcValue);
					}
					if (tc_strcasecmp(pcFlag, "mail") == 0 && pcValue != NULL)
					{
						pcMail = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
						tc_strcpy(pcMail, pcValue);
					}
				}
			}
			SAFE_MEM_free(pcFlag);
			SAFE_MEM_free(pcValue);
		}
		else
		{
			iRetCode = EPM_invalid_argument;
		}

		if (tc_strcmp(pcMail, "") != 0)
		{
			char* next_p;
			char seps[] = "; ,\t\n";
			char* chars_array = strtok_s(pcMail, seps, &next_p);
			while (chars_array)
			{
				if (std::find(szMailList.begin(), szMailList.end(), chars_array) == szMailList.end())
				{
					szMailList.push_back(chars_array);
				}
				chars_array = strtok_s(NULL, ",", &next_p);
			}
		}

		CHECK_ITK(iRetCode, EPM_ask_root_task(msg.task, &tRootTask));
		if (tRootTask != 0) {
			int	iTgt = 0;
			ecnObjType = ("Vf6_ECNRevision");
			ddeObjType = ("VF4_SCDDE");
			Teamcenter::scoped_smptr<tag_t> ptTgt;
			string user = getUser(tRootTask);
			CHECK_ITK(iRetCode, EPM_ask_attachments(tRootTask, EPM_target_attachment, &iTgt, &ptTgt));
			for (int i = 0; i < iTgt; i++) {
				tag_t ecnItem = NULLTAG;
				tag_t dde_item_create_input_tag = NULLTAG;
				tag_t dde_rev_create_input_tag = NULLTAG;

				Teamcenter::scoped_smptr<char> objType;
				Teamcenter::scoped_smptr<char> ecnRevType;
				Teamcenter::scoped_smptr<char> ecnID;
				Teamcenter::scoped_smptr<char> ecnRevision;
				Teamcenter::scoped_smptr<char> ecnName;

				CHECK_ITK(iRetCode, ITEM_ask_item_of_rev(ptTgt[i], &ecnItem));
				CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[i], "object_type", &ecnRevType));
				TC_write_syslog("Item Revision Type %d\n", ecnRevType.getString());
				CHECK_ITK(iRetCode, AOM_ask_value_string(ecnItem, "object_type", &objType));
				if (tc_strcmp(ecnRevType.getString(), ecnObjType) == 0 || tc_strcmp(ecnRevType.getString(), "Vf6_ECRRevision") == 0) {
					//Vf6_DDE_store_table: object name
					//vf6_dde_store_table: property name
					tag_t* tableRow = NULL;
					int numberOfRow = 0;

					CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[i], "item_id", &ecnID));
					CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[i], "item_revision_id", &ecnRevision));
					CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[i], "object_name", &ecnName));

					DDE_Create_Input dde_input = {};
					dde_input.object_type = "VF4_SCDDE";
					std::stringstream name;
					std::stringstream desc;

					name << ecnID.getString() << "/" << ecnRevision.getString();
					desc << ecnID.getString() << "/" << ecnRevision.getString() << "-" << ecnName.getString();

					dde_input.object_name = name.str();
					dde_input.object_desc = desc.str();
					dde_input.status = "REQUEST";
					dde_input.export_type = "Default";
					if (tc_strcmp(pcRevisionRule, "") == 0)
						dde_input.revision_rule = "VINFAST_RELEASE_RULE";
					else
						dde_input.revision_rule = pcRevisionRule;
					std::map<std::string, std::vector<tag_t>> ddeMap;
					CHECK_ITK(iRetCode, AOM_ask_table_rows(ptTgt[i], "vf6_dde_store_table", &numberOfRow, &tableRow));
					if (numberOfRow > 0) {
						for (int j = 0; j < numberOfRow; j++) {
							char* isSendValue = NULL;
							tag_t part = NULLTAG;
							char* vendorCodeValue = NULL;
							CHECK_ITK(iRetCode, AOM_ask_value_string(tableRow[j], "vf6_is_send", &isSendValue));
							if (tc_strcmp(isSendValue, "Yes") == 0) {
								CHECK_ITK(iRetCode, AOM_ask_value_string(tableRow[j], "vf6_vendor_code", &vendorCodeValue));
								CHECK_ITK(iRetCode, AOM_ask_value_tag(tableRow[j], "vf6_object_reference", &part));

								std::vector<std::string> vendorCodes = split_string(vendorCodeValue, ';');
								for (int iVendorCode = 0; iVendorCode < vendorCodes.size(); iVendorCode++) {
									std::string vendorCodeTmp = vendorCodes[iVendorCode];
									ltrim(vendorCodeTmp);
									rtrim(vendorCodeTmp);

									if (ddeMap.find(vendorCodeTmp) == ddeMap.end()) {
										std::vector<tag_t> vec1;
										vec1.push_back(part);
										ddeMap[vendorCodeTmp] = vec1;
									}
									else {
										std::map<std::string, std::vector<tag_t>>::iterator var = ddeMap.find(vendorCodeTmp);
										vector<tag_t>::iterator it = std::find(var->second.begin(), var->second.end(), part);
										if (it == var->second.end()) {
											var->second.push_back(part);
										}
									}
								}
							}
						}

						if (ddeMap.size() > 0) {
							tag_t iman_rela_type = NULLTAG;
							tag_t contact_rela_type = NULLTAG;
							tag_t implementBy_rela_type = NULLTAG;
							tag_t cmImplements = NULLTAG;
							CHECK_ITK(iRetCode, GRM_find_relation_type("IMAN_requirement", &iman_rela_type));
							CHECK_ITK(iRetCode, GRM_find_relation_type("ContactInCompany", &contact_rela_type));
							CHECK_ITK(iRetCode, GRM_find_relation_type("Vf6_ecn_data_exchange", &implementBy_rela_type));

							CHECK_ITK(iRetCode, AOM_refresh(ptTgt[i], TRUE));
							for (auto x : ddeMap) {
								tag_t dde_item = NULLTAG;
								iRetCode = createDDEObject(dde_input, dde_item);
								if (iRetCode == ITK_ok) {
									for (tag_t part : x.second) {
										iRetCode = setRelated(dde_item, part, iman_rela_type);
									}
									string vender = x.first;
									tag_t _vendor = getVendor(vender);
									if (_vendor != NULL) {
										iRetCode = setRelated(dde_item, _vendor, contact_rela_type);
									}
									else
									{
										vendorList.append(vender).append(";");
									}
									CHECK_ITK(iRetCode, GRM_create_relation(ptTgt[i], dde_item, implementBy_rela_type, NULLTAG, &cmImplements));
									CHECK_ITK(iRetCode, GRM_save_relation(cmImplements));
								}
							}
							CHECK_ITK(iRetCode, AOM_refresh(ptTgt[i], FALSE));
						}
					}
				}
			}

			//send mail admim vendor not contact
			if (vendorList.length() != 0)
			{
				string data = (vendorList.substr(vendorList.length() - 1, vendorList.length())) == ";" ? (vendorList.erase(vendorList.length() - 1, vendorList.length())) : vendorList;
				szMailList.push_back(user);
				mailBody << "<html><meta charset='UTF-8'>";
				mailBody << genHtmlNote("Dear Admin");
				mailBody << "<div style='margin-bottom:2.25pt'><p class=MsoNormal><span style='font-family:Arial'> Not Found Vender Contact Please Check : " << data << "<o:p></o:p></span></p></div>";
				mailBody << "<div style='margin-bottom:2.25pt'><p class=MsoNormal><span style='font-family:Arial'>Or Email Format 'change_my_mail@dummy.net' <o:p></o:p></span></p></div>";
				mailBody << "<div><p class=MsoNormal><span style='font-family:Arial;color:gray'> This email was sent from Vinfast IT-PLM Team. <o:p></o:p></span></p></div>";
				mailBody << "</html>";
				sendEmail("Not found Vendor", mailBody.str(), szMailList, false);
			}
		}

		return iRetCode;
	}

	int setRelated(tag_t primaryObject, tag_t secondaryObject, tag_t relationObject) {
		int iRetCode = ITK_ok;

		tag_t relation_created = NULLTAG;
		CHECK_ITK(iRetCode, GRM_create_relation(primaryObject, secondaryObject, relationObject, NULLTAG, &relation_created));
		CHECK_ITK(iRetCode, AOM_refresh(primaryObject, 0));
		CHECK_ITK(iRetCode, GRM_save_relation(relation_created));

		if (relation_created != NULL) {
			relation_created = NULL;
		}

		return iRetCode;
	}

	int createDDEObject(DDE_Create_Input dde_input, tag_t& dde_item) {
		int ret_code = ITK_ok;
		tag_t item_type_tag = NULLTAG;
		tag_t itemRev_type_tag = NULLTAG;
		tag_t item_create_input_tag = NULLTAG;
		tag_t itemRev_create_input_tag = NULLTAG;
		//tag_t item_tag = NULLTAG;

		/*creating item*/
		CHECK_ITK(ret_code, TCTYPE_find_type(dde_input.object_type.c_str(), dde_input.object_type.c_str(), &item_type_tag));
		if (item_type_tag == NULLTAG) {
			TC_write_syslog("[VF_handlers]__%d__%s__Could not find tag of object type: %s\n", __LINE__, __FUNCTION__, dde_input.object_type);
		}
		CHECK_ITK(ret_code, TCTYPE_construct_create_input(item_type_tag, &item_create_input_tag));

		/*find item revision tag type*/
		const std::string dde_partRev_object_type = dde_input.object_type.append("Revision");
		CHECK_ITK(ret_code, TCTYPE_find_type(dde_partRev_object_type.c_str(), dde_partRev_object_type.c_str(), &itemRev_type_tag));
		if (itemRev_type_tag == NULLTAG) {
			TC_write_syslog("[VF_handlers]__%d__%s__Could not find tag of object type: %s\n", __LINE__, __FUNCTION__, dde_partRev_object_type);
		}
		const char* tmp_obj_name = dde_input.object_name.c_str();
		const char* tmp_obj_desc = dde_input.object_desc.c_str();
		const char* tmp_status = dde_input.status.c_str();
		const char* tmp_revision_rule = dde_input.revision_rule.c_str();
		const char* tmp_export_type = dde_input.export_type.c_str();

		TC_write_syslog("[VF_handlers]__%d__%s__Create sourcing part: %s\n", __LINE__, __FUNCTION__, dde_input.item_id);
		tag_t tCLUser = NULLTAG;

		CHECK_ITK(ret_code, TCTYPE_set_create_display_value(item_create_input_tag, "object_name", 1, &tmp_obj_name));
		CHECK_ITK(ret_code, TCTYPE_set_create_display_value(item_create_input_tag, "object_desc", 1, &tmp_obj_desc));
		CHECK_ITK(ret_code, TCTYPE_set_create_display_value(item_create_input_tag, "vf4_status", 1, &tmp_status));
		CHECK_ITK(ret_code, TCTYPE_set_create_display_value(item_create_input_tag, "vf4_revision_rule", 1, &tmp_revision_rule));
		CHECK_ITK(ret_code, TCTYPE_set_create_display_value(item_create_input_tag, "vf4_export_type", 1, &tmp_export_type));

		CHECK_ITK(ret_code, TCTYPE_construct_create_input(itemRev_type_tag, &itemRev_create_input_tag));

		CHECK_ITK(ret_code, AOM_set_value_tag(item_create_input_tag, "revision", itemRev_create_input_tag));
		CHECK_ITK(ret_code, TCTYPE_create_object(item_create_input_tag, &dde_item));
		CHECK_ITK(ret_code, AOM_save_with_extensions(dde_item));
		//CHECK_ITK(ret_code, ITEM_ask_latest_rev(item_tag, &dde_rev));

		return ret_code;
	}