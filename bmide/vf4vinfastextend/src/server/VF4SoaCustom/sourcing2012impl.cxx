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

#include <sourcing2012impl.hxx>

using namespace VF4::Soa::Custom::_2020_12;
using namespace Teamcenter::Soa::Server;

#include <sa/am.h>
#include <base_utils/TcResultStatus.hxx>
#include <base_utils/ScopedSmPtr.hxx>
#include <base_utils/ScopedPtr.hxx>
#include "vf_custom.hxx"



int getNewBuyerName(std::string buyer, std::string &sNewBuyerName);
int getSourcePart(std::string partNumber, std::string sourcingProgram, tag_t* tSourcePart);
int getExistingBuyerUserId(std::string existingBuyer, std::string &sExistingBuyerUserId);

int getLatestItemRev(tag_t tItemTag, tag_t* tItemRevTag);
int setCostFormsProperties(tag_t tDesignItem, tag_t tCostItemRevision);
int changeOwnership(tag_t tObject, tag_t tOwningUser, tag_t tOwningGroup);
int getDesignItem(std::string partNumber, std::string partType, tag_t* tDesignItem);
int getCostItemRevision(std::string partNumber, std::string partType, tag_t* tCostItemRevision);
int validateCostForms(tag_t tCostItemRevision, logical* bValidTargetCostForm, logical* bValidPieceCostForm);
int getDesignItemRevision(std::string partNumber, std::string partRev, std::string partType, tag_t* tDesignItemRev);
int createCostRevision(std::string partNumber, std::string sCostItemName, tag_t* tCostItem, tag_t* tCostItemRevision);


SourcingImpl::AssignSourcePartToBuyerResponse SourcingImpl::assignSourcePartToBuyer ( const std::vector< STBuyerAssignmentInfo >& inputs )
{
	SourcingImpl::AssignSourcePartToBuyerResponse response;

	TC_write_syslog("*********************************************************************\n");
	TC_write_syslog("***                                                               ***\n");
	TC_write_syslog("***             !Assign to Buyer Service Log!                     ***\n");
	TC_write_syslog("***                                                               ***\n");
	TC_write_syslog("*********************************************************************\n");

	TC_write_syslog("Size of Input Vector : %zd\n", inputs.size());

	if (inputs.size() > 0)
	{
		char* user_name_string = NULL;
		tag_t tUserTag = NULLTAG;

		ERROR_CHECK(POM_get_user(&user_name_string, &tUserTag));

		if (tUserTag == NULLTAG)
		{
			tag_t tCurrentGroupMember = NULLTAG;

			ERROR_CHECK(SA_ask_current_groupmember(&tCurrentGroupMember));

			if (tCurrentGroupMember == NULLTAG)
			{
				TC_write_syslog("Current Group Member tag is NULLTAG\n");
			}
			else
			{
				ERROR_CHECK(SA_ask_groupmember_user(tCurrentGroupMember, &tUserTag));
			}
		}
		else
		{
			//TC_write_syslog("Current Session user - [%s]\n", user_name_string);
		}

		SAFE_SM_FREE(user_name_string);

		for (STBuyerAssignmentInfo stbuyerinfo : inputs)
		{
			try
			{
				AssignSourcePartToBuyerOutput outputStatus;
				std::string partNumber = stbuyerinfo.vfPartNumber;
				std::string sourcingProgram = stbuyerinfo.sourcingProgram;
				std::string buyer = stbuyerinfo.assigningUserID;
				tag_t tSourcePartTag = NULLTAG;

				outputStatus.vfPartNumber.assign(partNumber);
				outputStatus.sourcingProgram.assign(sourcingProgram);

				TC_write_syslog("[Assign To Buyer] Part Number - %s\tSourcing Program - %s\tBuyer - %s\n", partNumber.c_str(), sourcingProgram.c_str(), buyer.c_str());

				ERROR_CHECK(getSourcePart(partNumber, sourcingProgram, &tSourcePartTag));

				if (tSourcePartTag == NULLTAG)
				{
					TC_write_syslog("[Assign To Buyer] No Object Found for Part Number - %s and Sourcing Program - %s\n", partNumber.c_str(), sourcingProgram.c_str());
					outputStatus.errorMessage.assign("Part not found in Teamcenter");
					response.outputs.push_back(outputStatus);
				}
				else
				{
					int ifail = ITK_ok;
					int length = 0;
					int ii = 0;
					int n_granted = 0;
					int n_revoked = 0;
					tag_t eff_acl = NULLTAG;
					tag_t source_acl = NULLTAG;
					tag_t accessor = NULLTAG;
					//tag_t newAccessor = NULLTAG;
					//tag_t read_priv = NULLTAG;
					//tag_t write_priv = NULLTAG;
					Teamcenter::scoped_smptr<tag_t> granted;
					Teamcenter::scoped_smptr<tag_t> revoked;
					Teamcenter::scoped_smptr<char> type;
					Teamcenter::scoped_smptr<char> name;
					Teamcenter::scoped_smptr<char> acl_name;
					Teamcenter::scoped_smptr<char> existingBuyer;


					if (tUserTag != NULLTAG)
					{
						logical writePriv = false;

						ERROR_CHECK(AM_check_users_privilege(tUserTag, tSourcePartTag, "WRITE", &writePriv));
						if (writePriv)
						{
							logical isModifiable = false;

							ERROR_CHECK(AOM_ask_if_modifiable(tSourcePartTag, &isModifiable));
							if (isModifiable)
							{
								ERROR_CHECK(AOM_ask_value_string(tSourcePartTag, (const char*)"vf4_buyer", &existingBuyer));

								if (tc_strlen(existingBuyer.getString()) > 0)
								{
									std::string sExistingBuyerUserId;

									ERROR_CHECK(getExistingBuyerUserId(existingBuyer.getString(), sExistingBuyerUserId));
									ERROR_CHECK(AM_get_effective_acl(tSourcePartTag, &eff_acl, &length));

									for (ii = 0; ii < length; ii++)
									{
										ERROR_CHECK(AM_effective_acl_line(eff_acl, ii, &source_acl, &accessor, &n_granted, &granted, &n_revoked, &revoked));
										ERROR_CHECK(AM_ask_acl_name(source_acl, &acl_name));

										if (tc_strcmp(acl_name.getString(), "OBJECT") == 0)
										{
											ERROR_CHECK(AM_accessor_info(accessor, &type, &name));
											if (tc_strcmp(type.getString(), "User") == 0 && tc_strcmp(name.getString(), sExistingBuyerUserId.c_str()) == 0)
											{
												TC_write_syslog("[Assign To Buyer] Deleteing OBJECT acl: %s - %s\n", type.getString(), name.getString());
												ifail = AM_delete_acl(source_acl);
												if (ifail != ITK_ok)
												{
													outputStatus.errorMessage.assign("Failed to delete existing Buyer ACL");
													response.outputs.push_back(outputStatus);
													continue;
												}
											}
										}
									}

									ERROR_CHECK(AM_free_effective_acl(eff_acl));
								}


								if ((ifail = POM_refresh_instances_any_class(1, &tSourcePartTag, POM_modify_lock)) == ITK_ok)
								{
									std::string sNewBuyerName;
									tag_t vfBuyer_attr = NULLTAG;
									ERROR_CHECK(getNewBuyerName(buyer, sNewBuyerName));
									ifail = POM_attr_id_of_attr("vf4_buyer", "VF4_line_itemRevision", &vfBuyer_attr);
									if (sNewBuyerName.length() == 0)
									{
										//ifail = AOM_set_value_string(tSourcePartTag, (const char*)"vf4_buyer", (const char*)buyer.c_str());
										ifail = POM_set_attr_string(1,&tSourcePartTag, vfBuyer_attr,(const char*)buyer.c_str());
										if (ifail != ITK_ok)
										{
											TC_write_syslog("[Assign To Buyer] ifail=%d\n", ifail);
											outputStatus.errorMessage.assign("Failed to set Buyer");
											response.outputs.push_back(outputStatus);
										}
										else
										{
											TC_write_syslog("[Assign To Buyer] [%s] New Buyer Set succesfully...\n", buyer.c_str());
										}
									}
									else
									{
										//ifail = AOM_set_value_string(tSourcePartTag, (const char*)"vf4_buyer", (const char*)sNewBuyerName.c_str());
										ifail = POM_set_attr_string(1,&tSourcePartTag, vfBuyer_attr, (const char*)sNewBuyerName.c_str());
										if (ifail != ITK_ok)
										{
											TC_write_syslog("[Assign To Buyer] ifail=%d\n", ifail);
											outputStatus.errorMessage.assign("Failed to set Buyer");
											response.outputs.push_back(outputStatus);
										}
										else
										{
											TC_write_syslog("[Assign To Buyer] [%s] New Buyer Set succesfully...\n", sNewBuyerName.c_str());
										}
									}
									ERROR_CHECK(POM_save_instances(1, &tSourcePartTag, true));
									//ERROR_CHECK(AOM_save_without_extensions(tSourcePartTag));
									ERROR_CHECK(POM_refresh_instances_any_class(1, &tSourcePartTag, POM_no_lock));
								}

//								if ((ifail = AM_find_privilege("READ", &read_priv)) == ITK_ok)
//								{
//									if ((ifail = AM_find_privilege("WRITE", &write_priv)) == ITK_ok)
//									{
//										if ((ifail = AM_find_accessor("User", (const char*)buyer.c_str(), &newAccessor)) == ITK_ok)
//										{
//											if (newAccessor != NULLTAG)
//											{
//												if ((ifail = AM_grant_privilege(tSourcePartTag, newAccessor, read_priv)) == ITK_ok)
//												{
//													if ((ifail = AM_grant_privilege(tSourcePartTag, newAccessor, write_priv)) == ITK_ok)
//													{
//														if ((ifail = AM_save_acl(tSourcePartTag)) == ITK_ok)
//														{
//															if ((ifail = AOM_refresh(tSourcePartTag, TRUE)) == ITK_ok)
//															{
//																std::string sNewBuyerName;
//																tag_t vfBuyer_attr = NULLTAG;
//																ERROR_CHECK(getNewBuyerName(buyer, sNewBuyerName));
//																ifail = POM_attr_id_of_attr("vf4_buyer", "ItemRevision", &vfBuyer_attr);
//																if (sNewBuyerName.length() == 0)
//																{
//																	//ifail = AOM_set_value_string(tSourcePartTag, (const char*)"vf4_buyer", (const char*)buyer.c_str());
//																	ifail = POM_set_attr_string(1,&tSourcePartTag, vfBuyer_attr,(const char*)buyer.c_str());
//																	if (ifail != ITK_ok)
//																	{
//																		outputStatus.errorMessage.assign("Failed to set Buyer");
//																		response.outputs.push_back(outputStatus);
//																	}
//																	else
//																	{
//																		TC_write_syslog("[Assign To Buyer] [%s] New Buyer Set succesfully...\n", buyer.c_str());
//																	}
//																}
//																else
//																{
//																    //ifail = AOM_set_value_string(tSourcePartTag, (const char*)"vf4_buyer", (const char*)sNewBuyerName.c_str());
//																	ifail = POM_set_attr_string(1,&tSourcePartTag, vfBuyer_attr, (const char*)sNewBuyerName.c_str());
//																	if (ifail != ITK_ok)
//																	{
//																		outputStatus.errorMessage.assign("Failed to set Buyer");
//																		response.outputs.push_back(outputStatus);
//																	}
//																	else
//																	{
//																		TC_write_syslog("[Assign To Buyer] [%s] New Buyer Set succesfully...\n", sNewBuyerName.c_str());
//																	}
//																}
//																ERROR_CHECK(POM_save_instances(1, &tSourcePartTag, true));
//															    //ERROR_CHECK(AOM_save_without_extensions(tSourcePartTag));
//																ERROR_CHECK(AOM_refresh(tSourcePartTag, FALSE));
//															}
//															else
//															{
//																outputStatus.errorMessage.assign("Failed to place WRITE lock on Source Part Object");
//																response.outputs.push_back(outputStatus);
//															}
//														}
//														else
//														{
//															outputStatus.errorMessage.assign("Failed to Assign ACL");
//															response.outputs.push_back(outputStatus);
//														}
//													}
//													else
//													{
//														outputStatus.errorMessage.assign("Failed to Grant WRITE Access");
//														response.outputs.push_back(outputStatus);
//													}
//												}
//												else
//												{
//													outputStatus.errorMessage.assign("Failed to Grant READ Access");
//													response.outputs.push_back(outputStatus);
//												}
//											}
//											else
//											{
//												outputStatus.errorMessage.assign("Accessor not found in Teamcenter");
//												response.outputs.push_back(outputStatus);
//											}
//										}
//										else
//										{
//											outputStatus.errorMessage.assign("Failed to find User Accessor ACL for Buyer");
//											response.outputs.push_back(outputStatus);
//										}
									}
//									else
//									{
//										outputStatus.errorMessage.assign("Failed to find WRITE Privilege");
//										response.outputs.push_back(outputStatus);
//									}
//								}
//								else
//								{
//									outputStatus.errorMessage.assign("Failed to find READ Privilege");
//									response.outputs.push_back(outputStatus);
//								}
//							}
							else
							{
								outputStatus.errorMessage.assign("Source Part Object is not modifiable by user");
								response.outputs.push_back(outputStatus);
							}
						}
						else
						{
							outputStatus.errorMessage.assign("User does not has write access to Source Part item revision");
							response.outputs.push_back(outputStatus);
						}
					}
					else
					{
						TC_write_syslog("[Assign To Buyer] Failed to find the current session user...\n");
						outputStatus.errorMessage.assign("Unable to find the current session user");
						response.outputs.push_back(outputStatus);
					}
				}
			}
			catch(const IFail &e)
			{
				std::cout << e.getMessage();
				response.serviceData.addErrorStack();
				TC_write_syslog("Error ::%s\n", e.getMessage());
			}
		}
	}

	TC_write_syslog("\n------------ [Assign To Buyer] EXIT FROM Assign to Buyer Service ------------\n");
	return response;
}


int getSourcePart(std::string partNumber, std::string sourcingProgram, tag_t* tSourcePart)
{
	int ifail = ITK_ok;
	int iCount = 0;
	tag_t tQuery = NULLTAG;
	const char* queryName = "Source Part";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	ERROR_CHECK(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		return ITK_ok;
	}

	char* queryEntries[] = {"VF Part Number", "Sourcing Program"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partNumber.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(sourcingProgram.c_str()) + 1));

	queryValues[0] = tc_strcpy(queryValues[0], "");
	queryValues[0] = tc_strcpy(queryValues[0], partNumber.c_str());

	queryValues[1] = tc_strcpy(queryValues[1], "");
	queryValues[1] = tc_strcpy(queryValues[1], sourcingProgram.c_str());

	ERROR_CHECK(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));

	if (iCount == 0)
	{
		//TC_write_syslog("[Assign To Buyer] Source Part not found for given Part Number [%s] and Sourcing Program [%s]\n", partNumber.c_str(), sourcingProgram.c_str());
	}
	else if (iCount == 1)
	{
		*tSourcePart = tQryResults[0];
	}
	else
	{
		TC_write_syslog("[Assign To Buyer] More than 1 Source Part not found for given Part Number [%s] and Sourcing Program [%s]\n", partNumber.c_str(), sourcingProgram.c_str());
	}

	SAFE_SM_FREE(queryValues[0]);
	SAFE_SM_FREE(queryValues[1]);
	SAFE_SM_FREE(queryValues);

	return ifail;
}

int getNewBuyerName(std::string buyer, std::string &sNewBuyerName)
{
	int ifail = ITK_ok;
	tag_t tUserTag = NULLTAG;

	ERROR_CHECK(SA_find_user2(buyer.c_str(), &tUserTag));

	if (tUserTag != NULLTAG)
	{
		char* sPersonName = NULL;

		ERROR_CHECK(SA_ask_user_person_name2(tUserTag, &sPersonName));

		sNewBuyerName.assign(sPersonName);
		sNewBuyerName.append(" (");
		sNewBuyerName.append(buyer);
		sNewBuyerName.append(")");

		SAFE_SM_FREE(sPersonName);
	}
	else
	{
		sNewBuyerName.assign("");
	}

	return ifail;
}

int getExistingBuyerUserId(std::string existingBuyer, std::string &sExistingBuyerUserId)
{
	int ifail = ITK_ok;

	std::size_t openBracket = existingBuyer.find(" (");
	if (openBracket != std::string::npos)
	{
		std::size_t closingBracket = existingBuyer.length() - openBracket - 3;
		sExistingBuyerUserId = existingBuyer.substr(openBracket+2, closingBracket);
	}
	else
	{
		sExistingBuyerUserId = existingBuyer;
	}

	return ifail;
}

int changeOwnership(tag_t tObject, tag_t tOwningUser, tag_t tOwningGroup)
{
	int ifail = ITK_ok;

	ERROR_CHECK(AOM_refresh(tObject, TRUE));
	ERROR_CHECK(AOM_set_ownership(tObject, tOwningUser, tOwningGroup));
	ERROR_CHECK(AOM_save_without_extensions(tObject));
	ERROR_CHECK(AOM_refresh(tObject, FALSE));


	return ifail;
}

SourcingImpl::GetOrCreateVFCostResponse SourcingImpl::getOrCreateVFCost ( const std::vector< GetOrCreateVFCostInput >& inputs )
{
	SourcingImpl::GetOrCreateVFCostResponse response;
	int ifail = ITK_ok;

	TC_write_syslog("*********************************************************************\n");
	TC_write_syslog("***                                                               ***\n");
	TC_write_syslog("***				Get Or Create VFCost Service Log               ***\n");
	TC_write_syslog("***                                                               ***\n");
	TC_write_syslog("*********************************************************************\n");

	TC_write_syslog("Size of Input Vector : %zd\n", inputs.size());

	if (inputs.size() > 0)
	{
		tag_t tSessionUserTag = NULLTAG;
		tag_t tSessionUserGroup = NULLTAG;
        tag_t tCurrentGroupMember = NULLTAG;

        //TC_write_syslog("[VFCost] Finding GroupMember Tag...\n");
		ERROR_CHECK(SA_ask_current_groupmember(&tCurrentGroupMember));
        if (tCurrentGroupMember == NULLTAG)
		{
			TC_write_syslog("[VFCost] Current Group Member tag is NULLTAG\n");
		}
		else
		{
		    ERROR_CHECK(SA_ask_groupmember_user(tCurrentGroupMember, &tSessionUserTag));
			ERROR_CHECK(SA_ask_groupmember_group(tCurrentGroupMember, &tSessionUserGroup));
		}
		if(tSessionUserTag == NULLTAG)
		{
			TC_write_syslog("[VFCost] Failed to find the current session user...\n");
			return response;
		}
		if(tSessionUserGroup == NULLTAG)
		{
			TC_write_syslog("[VFCost] Failed to find the current session group...\n");
			return response;
		}

		//TC_write_syslog("[VFCost] Session user and Session user group found...\n");

		for (GetOrCreateVFCostInput vfCostInput : inputs)
		{
			try
			{
				GetOrCreateVFCostOutput outputStatus;
				std::string partNumber = vfCostInput.partNumber;
				//TC_write_syslog("[VFCost] Part Number - %s\n", partNumber.c_str());
				std::string partRevId = vfCostInput.partRevId;
				//TC_write_syslog("[VFCost] Part Rev Id - %s\n", partRevId.c_str());
				std::string partType = vfCostInput.partType;
				//TC_write_syslog("[VFCost] Part Type - %s\n", partType.c_str());
				bool createIfNotExisted = vfCostInput.createIfNotExisted;
				if (createIfNotExisted)
				{
					TC_write_syslog("[VFCost] Boolean is Create Cost Info is True\n");
				}
				else
				{
					TC_write_syslog("[VFCost] Boolean is Create Cost Info is False\n");
				}

				tag_t tDesignItem = NULLTAG;
				tag_t tDesignItemRev = NULLTAG;

				outputStatus.partNumber.assign(partNumber);
				outputStatus.partType.assign(partType);

	            if (partRevId.empty())
				{
					ERROR_CHECK(getDesignItem(partNumber, partType, &tDesignItem));
					ERROR_CHECK(getLatestItemRev(tDesignItem, &tDesignItemRev));
				}
				else
				{
					ERROR_CHECK(getDesignItemRevision(partNumber, partRevId, partType, &tDesignItemRev));
				}

				if (tDesignItemRev == NULLTAG)
				{
					TC_write_syslog("[VFCost] No Item Revision found for given Part Number [%s], Part Rev Id [%s] and Part Type [%s]\n", partNumber.c_str(), partRevId.c_str(), partType.c_str());
					outputStatus.vfCost = NULLTAG;
					outputStatus.errorMessage.assign("Item Revision not found in Teamcenter");
					response.outputs.push_back(outputStatus);
					continue;
				}

				//finding owning group/user for Design Item Revision
				tag_t tDesignRevOwingGroup = NULLTAG;
				tag_t tDesignRevOwingUser = NULLTAG;
				ERROR_CHECK(AOM_ask_value_tag(tDesignItemRev,"owning_group", &tDesignRevOwingGroup));
				ERROR_CHECK(AOM_ask_value_tag(tDesignItemRev,"owning_user", &tDesignRevOwingUser));

				// Find associated VFCostRevision
				int iSecCount = 0;
				tag_t tCostItemRevision = NULLTAG;
				tag_t tCostingRefRelation = NULLTAG;
				Teamcenter::scoped_smptr<tag_t> tSecObjList;

				TC_write_syslog("[VFCost] Finding existing cost revision to the Design Rev\n");
				ERROR_CHECK(GRM_find_relation_type("VF4_Costing_Reference", &tCostingRefRelation));
				ERROR_CHECK(GRM_list_secondary_objects_only(tDesignItemRev, tCostingRefRelation, &iSecCount, &tSecObjList));

				if (iSecCount > 0)
				{
					int k = 0;
					logical bChanged = false;

					ERROR_CHECK(AOM_refresh(tDesignItemRev, TRUE));

					for (k = 0; k < iSecCount; k++)
					{
						Teamcenter::scoped_smptr<char> sObjectType;

						ERROR_CHECK(WSOM_ask_object_type2(tSecObjList[k], &sObjectType));

						if (tc_strcmp(sObjectType.getString(), "VF4_CostRevision") == 0)
						{
							Teamcenter::scoped_smptr<char> sItemId;
							ERROR_CHECK(AOM_ask_value_string(tSecObjList[k], "item_id", &sItemId));

							if (partNumber.compare(sItemId.getString()) == 0)
							{
								tCostItemRevision = tSecObjList[k];
							}
							else
							{
								tag_t tRelation = NULLTAG;

								bChanged = true;
								TC_write_syslog("[VFCost] [%s] Wrong Cost associated to Design Revision...cutting the wrong cost revision\n", sItemId.getString());
								ERROR_CHECK(GRM_find_relation(tDesignItemRev, tSecObjList[k], tCostingRefRelation, &tRelation));
								ERROR_CHECK(GRM_delete_relation(tRelation));
							}
						}
					}

					if (bChanged)
					{
						ERROR_CHECK(AOM_save_without_extensions(tDesignItemRev));
					}

					ERROR_CHECK(AOM_refresh(tDesignItemRev, FALSE));
				}

				if (tCostItemRevision != NULLTAG)
				{
					logical bValidTargetCostForm = false;
					logical bValidPieceCostForm = false;

					ERROR_CHECK(validateCostForms(tCostItemRevision, &bValidTargetCostForm, &bValidPieceCostForm));
					if (!(bValidTargetCostForm && bValidPieceCostForm))
					{
						outputStatus.errorMessage.assign("Cost forms associated to Cost Revision are not valid");
						outputStatus.vfCost = NULLTAG;
						response.outputs.push_back(outputStatus);
					}
					else
					{
						outputStatus.vfCost = tCostItemRevision;
						response.outputs.push_back(outputStatus);
					}
				}
				else
				{
					//search in TC for Cost Revision
					std::string costRevType;
					costRevType.assign("VF4_CostRevision");
					ERROR_CHECK(getCostItemRevision(partNumber, costRevType, &tCostItemRevision));

					if (tCostItemRevision != NULLTAG)
					{
						logical bValidTargetCostForm = false;
						logical bValidPieceCostForm = false;

						ERROR_CHECK(validateCostForms(tCostItemRevision, &bValidTargetCostForm, &bValidPieceCostForm));
						if (!(bValidTargetCostForm && bValidPieceCostForm))
						{
							TC_write_syslog("[VFCost] Cost Revision is not associated with Design Revision. Cost forms associated to Cost Revision are not valid...\n");
							outputStatus.errorMessage.assign("Cost Revision is not associated with Design Revision. Cost forms associated to Cost Revision are not valid");
							outputStatus.vfCost = NULLTAG;
							response.outputs.push_back(outputStatus);
						}
						else
						{
							logical writePriv = false;

							ERROR_CHECK(AM_check_users_privilege(tSessionUserTag, tDesignItemRev, "WRITE", &writePriv));
							if (writePriv)
							{
								logical isModifiable = false;

								ERROR_CHECK(AOM_ask_if_modifiable(tDesignItemRev, &isModifiable));
								if (isModifiable)
								{
									tag_t tNewRelation = NULLTAG;

									ERROR_CHECK(GRM_create_relation(tDesignItemRev, tCostItemRevision, tCostingRefRelation, NULLTAG, &tNewRelation));
									ifail = GRM_save_relation(tNewRelation);
									if (ifail == ITK_ok)
									{
										TC_write_syslog("[VFCost] Cost Revision is associated with Design Revision...\n");
										outputStatus.vfCost = tCostItemRevision;
										response.outputs.push_back(outputStatus);
									}
									else
									{
										TC_write_syslog("[VFCost] Failed to associated Cost Revision to Design Item Revision...\n");
										outputStatus.errorMessage.assign("Failed to associated Cost Revision to Design Item Revision");
										outputStatus.vfCost = NULLTAG;
										response.outputs.push_back(outputStatus);
									}
								}
								else
								{
									outputStatus.errorMessage.assign("Design Item Revision Object is not modifiable by user");
									outputStatus.vfCost = NULLTAG;
									response.outputs.push_back(outputStatus);
								}
							}
							else
							{
								outputStatus.errorMessage.assign("User does not has write access to Design Item Revision");
								outputStatus.vfCost = NULLTAG;
								response.outputs.push_back(outputStatus);
							}
						}
					}
					// If Cost Revision does not existing...proceed with VFCost creation...
					else if (createIfNotExisted)
					{
						logical writePriv = false;

						ERROR_CHECK(AM_check_users_privilege(tSessionUserTag, tDesignItemRev, "WRITE", &writePriv));
						if (writePriv)
						{
							logical isModifiable = false;

							ERROR_CHECK(AOM_ask_if_modifiable(tDesignItemRev, &isModifiable));
							if (isModifiable)
							{
								tag_t tCostItem = NULLTAG;
								Teamcenter::scoped_smptr<char> sDesignItemRevName;

								ERROR_CHECK(WSOM_ask_name2(tDesignItemRev, &sDesignItemRevName));
								ERROR_CHECK(ITEM_ask_item_of_rev(tDesignItemRev, &tDesignItem));
								ERROR_CHECK(createCostRevision(partNumber, sDesignItemRevName.getString(), &tCostItem, &tCostItemRevision));
								if (tCostItemRevision != NULLTAG)
								{
									tag_t tNewRelation = NULLTAG;
									ERROR_CHECK(GRM_create_relation(tDesignItemRev, tCostItemRevision, tCostingRefRelation, NULLTAG, &tNewRelation));
									ifail = GRM_save_relation(tNewRelation);
									if (ifail == ITK_ok)
									{
										outputStatus.vfCost = tCostItemRevision;
										response.outputs.push_back(outputStatus);


										if (tDesignRevOwingGroup != tSessionUserGroup)
										{
											int iCount = 0;
											int ii = 0;
											tag_t tSourcingCostFormRelType = NULLTAG;
											Teamcenter::scoped_smptr<tag_t> tCostForms;

											ERROR_CHECK(GRM_find_relation_type("VF4_SourcingCostFormRela", &tSourcingCostFormRelType));
											ERROR_CHECK(GRM_list_secondary_objects_only(tCostItemRevision, tSourcingCostFormRelType, &iCount, &tCostForms));
											for (ii = 0; ii < iCount; ii++)
											{
												ERROR_CHECK(changeOwnership(tCostForms[ii], tDesignRevOwingUser, tDesignRevOwingGroup));
											}

											ERROR_CHECK(changeOwnership(tCostItemRevision, tDesignRevOwingUser, tDesignRevOwingGroup));
											ERROR_CHECK(changeOwnership(tCostItem, tDesignRevOwingUser, tDesignRevOwingGroup));

										}
										ERROR_CHECK(setCostFormsProperties(tDesignItem,tCostItemRevision));
										TC_write_syslog("[VFCost] VFCost onject is created and associated with Design Revision successfully...\n");
									}
									else
									{
										outputStatus.errorMessage.assign("Failed to associated Cost Revision to Design Item Revision");
										outputStatus.vfCost = NULLTAG;
										response.outputs.push_back(outputStatus);
									}
								}
								else
								{
									outputStatus.errorMessage.assign("Failed to create Cost Revision Object");
									outputStatus.vfCost = NULLTAG;
									response.outputs.push_back(outputStatus);
								}
							}
							else
							{
								outputStatus.errorMessage.assign("Design Item Revision Object is not modifiable by user");
								outputStatus.vfCost = NULLTAG;
								response.outputs.push_back(outputStatus);
							}
						}
						else
						{
							outputStatus.errorMessage.assign("User does not has write access to Design Item Revision");
							outputStatus.vfCost = NULLTAG;
							response.outputs.push_back(outputStatus);
						}
					}
					else
					{
						TC_write_syslog("[VFCost] Failed to find Cost Revision in Teamcenter\n");
						outputStatus.errorMessage.assign("Cost Revision is not found in Teamcenter");
						outputStatus.vfCost = NULLTAG;
						response.outputs.push_back(outputStatus);
					}
				}
			}
			catch(const IFail &e)
			{
				std::cout << e.getMessage();
				response.serviceData.addErrorStack();
				TC_write_syslog("Error ::%s\n", e.getMessage());
			}
		}
	}

	TC_write_syslog("\n------------ [VFCost] EXIT FROM Get or Create VFCost Service ------------\n");
	return response;
}

int getDesignItem(std::string partNumber, std::string partType, tag_t* tDesignItem)
{
	int ifail = ITK_ok;
	int iCount = 0;
	tag_t tQuery = NULLTAG;
	const char* queryName = "Item...";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	ERROR_CHECK(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		return ITK_ok;
	}

	char* queryEntries[] = {"Item ID", "Type"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partNumber.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partType.c_str()) + 1));

	queryValues[0] = tc_strcpy(queryValues[0], "");
	queryValues[0] = tc_strcpy(queryValues[0], partNumber.c_str());

	queryValues[1] = tc_strcpy(queryValues[1], "");
	queryValues[1] = tc_strcpy(queryValues[1], partType.c_str());

	ERROR_CHECK(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));
	if (iCount == 0)
	{
		//TC_write_syslog("[VFCost] No Item found for given Part Number [%s] and Part Type [%s]\n", partNumber.c_str(), partType.c_str());
	}
	else if (iCount == 1)
	{
		*tDesignItem = tQryResults[0];
	}
	else
	{
		TC_write_syslog("[VFCost] More than 1 Items not found for given Part Number [%s] and Part Type [%s]\n", partNumber.c_str(), partType.c_str());
	}

	SAFE_SM_FREE(queryValues[0]);
	SAFE_SM_FREE(queryValues[1]);
	SAFE_SM_FREE(queryValues);

	return ifail;
}

int getLatestItemRev(tag_t tItemTag, tag_t* tItemRevTag)
{
	int ifail = ITK_ok;
	tag_t tRevTag = NULLTAG;
	Teamcenter::scoped_smptr<char> revId;

	ERROR_CHECK(ITEM_ask_latest_rev(tItemTag, &tRevTag));
	ERROR_CHECK(ITEM_ask_rev_id2(tRevTag, &revId));

	if (tc_strstr(revId.getString(), ".") != NULL)
	{
		tag_t tbaserev = NULLTAG;

		ERROR_CHECK(ITEM_rev_find_base_rev(tRevTag, &tbaserev));

		if (tbaserev != NULLTAG)
		{
			tRevTag = tbaserev;
		}
		else
		{
			TC_write_syslog("[VFCost] Baseline Rev TAG is a NULLTAG\n");
		}
	}

	*tItemRevTag = tRevTag;

	return ifail;
}

int getDesignItemRevision(std::string partNumber, std::string partRev, std::string partType, tag_t* tDesignItemRev)
{
	int ifail = ITK_ok;
	int iCount = 0;
	tag_t tQuery = NULLTAG;
	const char* queryName = "Item Revision...";
	Teamcenter::scoped_smptr<tag_t> tQryResults;


	ERROR_CHECK(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		return ITK_ok;
	}

	char* queryEntries[] = {"Item ID", "Revision", "Type"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 3);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partNumber.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partRev.c_str()) + 1));
	queryValues[2] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partType.c_str()) + 1));

	queryValues[0] = tc_strcpy(queryValues[0], "");
	queryValues[0] = tc_strcpy(queryValues[0], partNumber.c_str());

	queryValues[1] = tc_strcpy(queryValues[1], "");
	queryValues[1] = tc_strcpy(queryValues[1], partRev.c_str());

	queryValues[2] = tc_strcpy(queryValues[2], "");
	queryValues[2] = tc_strcpy(queryValues[2], partType.c_str());

	ERROR_CHECK(QRY_execute(tQuery, 3, queryEntries, queryValues, &iCount, &tQryResults));

	if (iCount == 0)
	{
		//TC_write_syslog("[VFCost] No Item Revision found for given Part Number [%s], Part Rev Id [%s] and Part Type [%s]\n", partNumber.c_str(), partRev.c_str(), partType.c_str());
	}
	else if (iCount == 1)
	{
		*tDesignItemRev = tQryResults[0];
	}
	else
	{
		TC_write_syslog("[VFCost] More than 1 Items Revisions found not found for given Part Number [%s], Part Rev Id [%s] and Part Type [%s]\n", partNumber.c_str(), partRev.c_str(), partType.c_str());
	}

	SAFE_SM_FREE(queryValues[0]);
	SAFE_SM_FREE(queryValues[1]);
	SAFE_SM_FREE(queryValues[2]);
	SAFE_SM_FREE(queryValues);

	return ifail;
}

int getCostItemRevision(std::string partNumber, std::string partType, tag_t* tCostItemRevision)
{
	int ifail = ITK_ok;
	int iCount = 0;
	tag_t tQuery = NULLTAG;
	const char* queryName = "Working Item Revision...";
	Teamcenter::scoped_smptr<tag_t> tQryResults;

	ERROR_CHECK(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		return ITK_ok;
	}

	char* queryEntries[] = {"Item ID", "Type"};
	char** queryValues = (char**)MEM_alloc(sizeof(char*) * 2);

	queryValues[0] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partNumber.c_str()) + 1));
	queryValues[1] = (char*) MEM_alloc(sizeof(char) * ((int)tc_strlen(partType.c_str()) + 1));

	queryValues[0] = tc_strcpy(queryValues[0], "");
	queryValues[0] = tc_strcpy(queryValues[0], partNumber.c_str());

	queryValues[1] = tc_strcpy(queryValues[1], "");
	queryValues[1] = tc_strcpy(queryValues[1], partType.c_str());

	ERROR_CHECK(QRY_execute(tQuery, 2, queryEntries, queryValues, &iCount, &tQryResults));

	if (iCount == 0)
	{
		//TC_write_syslog("[VFCost] No Item Revision found for given Part Number [%s] and Part Type [%s]\n", partNumber.c_str(), partType.c_str());
	}
	else if (iCount == 1)
	{
		*tCostItemRevision = tQryResults[0];
	}
	else
	{
		TC_write_syslog("[VFCost] More than 1 Items Revisions found not found for given Part Number [%s] and Part Type [%s]\n", partNumber.c_str(), partType.c_str());
	}

	SAFE_SM_FREE(queryValues[0]);
	SAFE_SM_FREE(queryValues[1]);
	SAFE_SM_FREE(queryValues);

	return ifail;
}

int validateCostForms(tag_t tCostItemRevision, logical* bValidTargetCostForm, logical* bValidPieceCostForm)
{
	int ifail = ITK_ok;
	int iCount = 0;
	tag_t tSourcingCostFormRelType = NULLTAG;
	Teamcenter::scoped_smptr<tag_t> tCostForms;

	ERROR_CHECK(GRM_find_relation_type("VF4_SourcingCostFormRela", &tSourcingCostFormRelType));
	ERROR_CHECK(GRM_list_secondary_objects_only(tCostItemRevision, tSourcingCostFormRelType, &iCount, &tCostForms));
	if (iCount == 2)
	{
		int ii = 0;
		std::string cSubStr;
		Teamcenter::scoped_smptr<char> sItemId;

		ERROR_CHECK(AOM_ask_value_string(tCostItemRevision, "item_id", &sItemId));
		cSubStr.assign(sItemId.getString());
		cSubStr.append("_");

		for (ii = 0; ii < iCount; ii++)
		{
			Teamcenter::scoped_smptr<char> sObjectName;
			Teamcenter::scoped_smptr<char> sObjectType;

			ERROR_CHECK(WSOM_ask_name2(tCostForms[ii], &sObjectName));
			ERROR_CHECK(WSOM_ask_object_type2(tCostForms[ii], &sObjectType));

			//TC_write_syslog("[VFCost] Form Name [%s]\tForm Type [%s]\n", sObjectName.getString(), sObjectType.getString());
			if (tc_strcmp(sObjectType.getString(), "VF4_TargetCostForm") == 0 && tc_strstr(sObjectName.getString(), cSubStr.c_str()) != NULL)
			{
				*bValidTargetCostForm = true;
			}
			else if (tc_strcmp(sObjectType.getString(), "VF4_PieceCostForm") == 0 && tc_strstr(sObjectName.getString(), cSubStr.c_str()) != NULL)
			{
				*bValidPieceCostForm = true;
			}
		}
	}

	return ifail;
}
int setCostFormsProperties(tag_t tDesignItem, tag_t tCostItemRevision)
{
	int ifail = ITK_ok;
	int iCount = 0;
	tag_t tSourcingCostFormRelType = NULLTAG;
	Teamcenter::scoped_smptr<tag_t> tCostForms;

	ERROR_CHECK(GRM_find_relation_type("VF4_SourcingCostFormRela", &tSourcingCostFormRelType));
	ERROR_CHECK(GRM_list_secondary_objects_only(tCostItemRevision, tSourcingCostFormRelType, &iCount, &tCostForms));
	if (iCount == 2)
	{
		int ii = 0;
		Teamcenter::scoped_smptr<char> makeValue;

		ERROR_CHECK(AOM_ask_value_string(tDesignItem,"vf4_item_make_buy",&makeValue));
		for (ii = 0; ii < iCount; ii++)
		{
			Teamcenter::scoped_smptr<char> sObjectType;

			ERROR_CHECK(WSOM_ask_object_type2(tCostForms[ii], &sObjectType));

			//TC_write_syslog("[VFCost] Form Name [%s]\tForm Type [%s]\n", sObjectName.getString(), sObjectType.getString());
			if (tc_strcmp(sObjectType.getString(), "VF4_PieceCostForm") == 0)
			{
				if(tc_strcmp(makeValue.getString(),"make")== 0 || tc_strcmp(makeValue.getString(),"Make")== 0)
				{
					ERROR_CHECK(AOM_refresh(tCostForms[ii], TRUE));
					ERROR_CHECK(AOM_set_value_string(tCostForms[ii],"vf4_quality_of_finance","Cost Engineering Estimate"));
					ERROR_CHECK(AOM_save_without_extensions(tCostForms[ii]));
					ERROR_CHECK(AOM_refresh(tCostForms[ii], FALSE));
				}
			}
		}
	}

	return ifail;
}
int createCostRevision(std::string partNumber, std::string sCostItemName, tag_t* tCostItem, tag_t* tCostItemRevision)
{
	int ifail = ITK_ok;
	tag_t tCostItemTag = NULLTAG;
	tag_t tCostItemRevTag = NULLTAG;
	tag_t tCostItemTypeTag = NULLTAG;
	tag_t tCostItemRevTypeTag = NULLTAG;
	tag_t tCostItemCreateInputTag = NULLTAG;
	tag_t tCostItemRevCreateInputTag = NULLTAG;


	/***** Create Input for CAT_Cost Change Revision Object *****/
	ERROR_CHECK(TCTYPE_find_type("VF4_CostRevision", NULL, &tCostItemRevTypeTag));
	ERROR_CHECK(TCTYPE_construct_create_input(tCostItemRevTypeTag, &tCostItemRevCreateInputTag));
	ERROR_CHECK(AOM_set_value_string(tCostItemRevCreateInputTag, (const char*)"object_name", (const char*)sCostItemName.c_str()));

	/***** Create Input for CAT_Cost Change Object *****/
	ERROR_CHECK(TCTYPE_find_type("VF4_Cost", NULL, &tCostItemTypeTag));
	ERROR_CHECK(TCTYPE_construct_create_input(tCostItemTypeTag, &tCostItemCreateInputTag));
	ERROR_CHECK(AOM_set_value_string(tCostItemCreateInputTag, "item_id", partNumber.c_str()));
	ERROR_CHECK(AOM_set_value_string(tCostItemCreateInputTag, "object_name", sCostItemName.c_str()));
	ERROR_CHECK(AOM_set_value_tag(tCostItemCreateInputTag, "revision", tCostItemRevCreateInputTag));

	ERROR_CHECK(TCTYPE_create_object(tCostItemCreateInputTag, &tCostItemTag));
	ERROR_CHECK(AOM_save_with_extensions(tCostItemTag));
	ERROR_CHECK(AOM_unlock(tCostItemTag));

	ERROR_CHECK(ITEM_ask_latest_rev(tCostItemTag, &tCostItemRevTag));

	*tCostItem = tCostItemTag;
	*tCostItemRevision = tCostItemRevTag;

	return ifail;
}
