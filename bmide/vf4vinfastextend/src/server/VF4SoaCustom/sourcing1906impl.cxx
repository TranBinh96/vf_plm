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

#include <sourcing1906impl.hxx>

using namespace VF4::Soa::Custom::_2019_06;
using namespace Teamcenter::Soa::Server;

SourcingImpl::STResponse SourcingImpl::getSourceTrackerDetails ( const std::string sourcingProgram, bool isDELRequiredInPurchaseLevel, bool isCostAttributesRequired, const std::vector< std::string >& selecetdUIDs )
{
	SourcingImpl::STResponse response;
	tag_t tQryTag = NULLTAG;
	int ifail  = ITK_ok;
	char *cError=NULL;
	TC_write_syslog("getSTDetails called....................\n");
	TC_write_syslog("*********************************************************************\n");
	TC_write_syslog("***                                                               ***\n");
	TC_write_syslog("***                   Sourcing Service Log                        ***\n");
	TC_write_syslog("***                                                               ***\n");
	TC_write_syslog("*********************************************************************\n");

	if(!sourcingProgram.empty())
	{
		ifail=QRY_find2(QRY_SOURCE_PART, &tQryTag);
		if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}
		if (tQryTag != NULLTAG)
		{
			try
			{
				int n_STCount = 0;
				int stCountIndex = 0;
				char* actual_preference = NULL;
				char** entries = NULL;
				char** values = NULL;
				tag_t* stObjects = NULL;

				if (sourcingProgram.find("AF") == 0)
				{
					actual_preference = (char*)MEM_alloc(sizeof(char) * ((int)tc_strlen(PREF_AFS_ST) + 1));
					actual_preference = tc_strcpy(actual_preference, "");
					actual_preference = tc_strcpy(actual_preference, PREF_AFS_ST);
				}
				else
				{
					actual_preference = (char*)MEM_alloc(sizeof(char) * ((int)tc_strlen(PREF_Standard_ST) + 1));
					actual_preference = tc_strcpy(actual_preference, "");
					actual_preference = tc_strcpy(actual_preference, PREF_Standard_ST);
				}

				entries = (char**)MEM_alloc(sizeof(char*) * 1);
				values = (char**)MEM_alloc(sizeof(char*) * 1);

				entries[0] = (char*)MEM_alloc(sizeof(char) * ((int)tc_strlen("Sourcing Program") + 1));
				values[0] = (char*)MEM_alloc(sizeof(char) * ((int)tc_strlen((char*)sourcingProgram.c_str()) + 1));

				tc_strcpy(entries[0], "");
				tc_strcpy(entries[0], "Sourcing Program");

				tc_strcpy(values[0], "");
				tc_strcpy(values[0], (char*)sourcingProgram.c_str());

				ifail=QRY_execute(tQryTag, 1, entries, values, &n_STCount, &stObjects);
				if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}
				for (stCountIndex = 0; stCountIndex < n_STCount; stCountIndex++)
				{
					int num_values_disp_del = 0;
					std::string vfPurchaseLevel;
					char** prop_value_vf_purchase_level = NULL;

					ifail = AOM_ask_displayable_values(stObjects[stCountIndex], VF_PURCHASE_LEVEL, &num_values_disp_del, &prop_value_vf_purchase_level);

					if (num_values_disp_del > 0)
					{
						vfPurchaseLevel.assign(prop_value_vf_purchase_level[0]);
					}
					else
					{
						vfPurchaseLevel.assign("");
					}

					SAFE_SM_FREE(prop_value_vf_purchase_level);

					if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}

					if ((isDELRequiredInPurchaseLevel==false && tc_strcmp(vfPurchaseLevel.c_str(), DEL) != 0) || isDELRequiredInPurchaseLevel==true)
					{
						char** prop_value_disp = NULL;
						int pref_count = 0;
						char** pref_values = NULL;
						vector<std::string> propertiesValuesVect;
						int num_values_disp = 0;

						ifail=AOM_ask_displayable_values(stObjects[stCountIndex], "item_id", &num_values_disp, &prop_value_disp);
						if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}
						std::string stObjName;

						for (int k = 0; k < num_values_disp; k++)
						{
							if (k == 0)
							{
								stObjName.assign(prop_value_disp[k]);
							}
							else
							{
								stObjName.append(",");
								stObjName.append(prop_value_disp[k]);
							}
						}
						ifail=PREF_ask_char_values(actual_preference, &pref_count, &pref_values);
						if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}

						for (int j = 0; j < pref_count; j++)
						{
							string pref_values_string;
							pref_values_string.assign(pref_values[j]);
							string real_name=pref_values_string.substr(0, pref_values_string.find(','));
							string costAttr_logical = pref_values_string.substr(pref_values_string.find(",") + 1);
							bool isCostProp = false;
							char* realName_char;
							realName_char = &real_name[0];

							if (costAttr_logical.compare("0") == 0)
							{
								isCostProp = false;
							}
							else
							{
								isCostProp = true;
							}

							char* displayname = NULL;
							ifail=AOM_UIF_ask_name(stObjects[stCountIndex], realName_char, &displayname);
							if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}
							if (tc_strlen(displayname) > 0)
							{
								if ((isCostAttributesRequired==false && isCostProp==false) || isCostAttributesRequired==true)
								{
									int num_values = 0;
									char** prop_value = NULL;
									std::string stDispalyname(displayname);

									ifail=AOM_ask_displayable_values(stObjects[stCountIndex], realName_char, &num_values, &prop_value);
									if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}
									std::string propValue;

									for (int k = 0; k < num_values; k++)
									{
										if (k == 0)
										{
											propValue.assign(prop_value[k]);
										}
										else
										{
											propValue.append(",");
											propValue.append(prop_value[k]);
										}
									}

									propertiesValuesVect.push_back(propValue);
									response.attrDisplayNames.insert(std::pair<int, std::string>(j, stDispalyname));
									SAFE_SM_FREE(prop_value);
									SAFE_SM_FREE(displayname);
								}
							}
							else
							{
								response.attrDisplayNames.insert(std::pair<int, std::string>(j, ""));
								propertiesValuesVect.push_back("");
							}
						}

						response.attrActualValues.insert(std::pair<std::string, vector<string>>(stObjName, propertiesValuesVect));
						SAFE_SM_FREE(pref_values);
						SAFE_SM_FREE(prop_value_disp);
					}
				}
				SAFE_SM_FREE(entries[0]);
				SAFE_SM_FREE(values[0]);
				SAFE_SM_FREE(entries);
				SAFE_SM_FREE(values);
				SAFE_SM_FREE(actual_preference);
			}
			catch(const IFail &e)
			{
				std::cout << e.getMessage();
				response.serviceData.addErrorStack();
				TC_write_syslog("Error::%s\n", e.getMessage());
			}
		}
		else
		{
			TC_write_syslog("%s Qry not found in Teamcenter....................\n", QRY_SOURCE_PART);
		}
	}
	else if(selecetdUIDs.size()>0)
	{
		int num_values_platModule = 0;
		char** prop_value_platModule = NULL;
		tag_t selectedUID_wso = NULLTAG;
		tag_t selectedObjsUID = NULLTAG;
		char* actual_preference = NULL;
		try
		{
			std::string selecetdUIDs_string(selecetdUIDs[0]);
			const char* uid = selecetdUIDs_string.c_str();
			ITK__convert_uid_to_tag(uid, &selectedUID_wso);
			ifail=AOM_ask_displayable_values(selectedUID_wso, "vf4_platform_module", &num_values_platModule, &prop_value_platModule);
			if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}
			std::string stPlatformModuleName;

			for (int k = 0; k < num_values_platModule; k++)
			{
				if (k == 0)
				{
					stPlatformModuleName.assign(prop_value_platModule[k]);
				}
				else
				{
					stPlatformModuleName.append(",");
					stPlatformModuleName.append(prop_value_platModule[k]);
				}
			}

			if (stPlatformModuleName.find("AF") == 0)
			{
				actual_preference = (char*)MEM_alloc(sizeof(char) * ((int)tc_strlen(PREF_AFS_ST) + 1));
				actual_preference = tc_strcpy(actual_preference, "");
				actual_preference = tc_strcpy(actual_preference, PREF_AFS_ST);
			}
			else
			{
				actual_preference = (char*)MEM_alloc(sizeof(char) * ((int)tc_strlen(PREF_Standard_ST) + 1));
				actual_preference = tc_strcpy(actual_preference, "");
				actual_preference = tc_strcpy(actual_preference, PREF_Standard_ST);
			}

			if(selecetdUIDs.size()>0)
			{
				for(int i=0; i<selecetdUIDs.size(); i++)
				{
					std::string selecetdObjUIDs_string(selecetdUIDs[i]);
					const char* selectedObjUID = selecetdObjUIDs_string.c_str();

					ITK__convert_uid_to_tag(selectedObjUID, &selectedObjsUID);
					int pref_count = 0;
					char** pref_values = NULL;
					vector<std::string> propertiesValuesVect;
					int num_values_disp = 0;
					int num_values_disp_del = 0;
					std::string vfPurchaseLevel;
					char** prop_value_disp = NULL;
					char** prop_value_vf_purchase_level = NULL;

					ifail = AOM_ask_displayable_values(selectedObjsUID, VF_PURCHASE_LEVEL, &num_values_disp_del, &prop_value_vf_purchase_level);

					if (num_values_disp_del > 0)
					{
						vfPurchaseLevel.assign(prop_value_vf_purchase_level[0]);
					}
					else
					{
						vfPurchaseLevel.assign("");
					}

					SAFE_SM_FREE(prop_value_vf_purchase_level);

					if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}

					if ((isDELRequiredInPurchaseLevel==false && tc_strcmp(vfPurchaseLevel.c_str(), DEL) != 0) || isDELRequiredInPurchaseLevel==true)
					{
						ifail=AOM_ask_displayable_values(selectedObjsUID, "item_id", &num_values_disp, &prop_value_disp);
						if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}
						std::string stObjName;

						for (int k = 0; k < num_values_disp; k++)
						{
							if (k == 0)
							{
								stObjName.assign(prop_value_disp[k]);
							}
							else
							{
								stObjName.append(",");
								stObjName.append(prop_value_disp[k]);
							}
						}

						ifail=PREF_ask_char_values(actual_preference, &pref_count, &pref_values);
						for (int j = 0; j < pref_count; j++)
						{
							string pref_values_string;
							pref_values_string.assign(pref_values[j]);
							string real_name=pref_values_string.substr(0, pref_values_string.find(','));
							string costAttr_logical = pref_values_string.substr(pref_values_string.rfind(",") + 1);
							bool isCostProp = false;
							char* realName_char;
							realName_char = &real_name[0];
							if (costAttr_logical.compare("0") == 0)
							{
								isCostProp = false;
							}
							else
							{
								isCostProp = true;
							}
							char* displayname = NULL;
							ifail=AOM_UIF_ask_name(selectedObjsUID, realName_char, &displayname);
							if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}
							if (tc_strlen(displayname) > 0)
							{
								if ((isCostAttributesRequired==false && isCostProp==false) || isCostAttributesRequired==true)
								{
									int num_values = 0;
									char** prop_value = NULL;
									std::string stDispalyname(displayname);

									ifail=AOM_ask_displayable_values(selectedObjsUID, realName_char, &num_values, &prop_value);
									if (ifail != ITK_ok){EMH_ask_error_text(ifail,&cError);TC_write_syslog("[VF]::ERROR: %s \n",cError); SAFE_SM_FREE(cError);}
									std::string propValue;

									for (int k = 0; k < num_values; k++)
									{
										if (k == 0)
										{
											propValue.assign(prop_value[k]);
										}
										else
										{
											propValue.append(",");
											propValue.append(prop_value[k]);
										}
									}

									propertiesValuesVect.push_back(propValue);
									response.attrDisplayNames.insert(std::pair<int, std::string>(j, stDispalyname));
									SAFE_SM_FREE(prop_value);
									SAFE_SM_FREE(displayname);
								}
							}
							else
							{
								response.attrDisplayNames.insert(std::pair<int, std::string>(j, ""));
								propertiesValuesVect.push_back("");
							}
						}
						response.attrActualValues.insert(std::pair<std::string, vector<string>>(stObjName, propertiesValuesVect));
						SAFE_SM_FREE(pref_values);
						SAFE_SM_FREE(prop_value_disp);
					}
				}
			}
			SAFE_SM_FREE(prop_value_platModule);
			SAFE_SM_FREE(actual_preference);
		}
		catch(const IFail &e)
		{
			std::cout << e.getMessage();
			response.serviceData.addErrorStack();
			TC_write_syslog("Error ::%s\n", e.getMessage());
		}
	}
	TC_write_syslog("getSTDetails exit....................\n");
	return response;
}
