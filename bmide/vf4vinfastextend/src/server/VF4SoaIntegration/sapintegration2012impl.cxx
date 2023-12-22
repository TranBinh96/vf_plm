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

#include <sapintegration2012impl.hxx>

using namespace VF4::Soa::Integration::_2020_12;
using namespace Teamcenter::Soa::Server;
#define CHECK_FAIL(x)																													\
{																																\
	if (x != ITK_ok)																								\
	{																															\
		char *error_str = NULL;																								\
		EMH_ask_error_text(x, &error_str);																			\
		TC_write_syslog("ERROR: %d, ERROR MSG: %s. at Line: %d in File: %s\n", x, error_str, __LINE__, __FILE__);	\
		SAFE_SM_FREE(error_str);																								\
	}																															\
}


SAPIntegrationImpl::GetMasterMaterialsResponse SAPIntegrationImpl::getMasterMaterials ( const std::vector< GetMasterMaterialsInput >& inputs )
{
	TC_write_syslog("[VF] ENTER %s", __FUNCTION__);
    SAPIntegrationImpl::GetMasterMaterialsResponse response;
	TC_write_syslog("Size of Input Vector : %d\n", inputs.size());
	if (inputs.size() > 0)
	{
		for (GetMasterMaterialsInput plantInput : inputs)
		{
			try
			{
				tag_t uom = NULLTAG;
				tag_t tPartTag = NULLTAG;

				GetMasterMaterialsOutput outputStatus;

				Teamcenter::scoped_smptr<char> partType;
				Teamcenter::scoped_smptr<char> revisionNumber;
				Teamcenter::scoped_smptr<char> brdCode;
				Teamcenter::scoped_smptr<char> functionalClass;
				Teamcenter::scoped_smptr<char> oldMaterialNumber;
				Teamcenter::scoped_smptr<char> materialType;
				Teamcenter::scoped_smptr<char> uomName;
				Teamcenter::scoped_smptr<char> gmPart;
				Teamcenter::scoped_smptr<char> materialNumber;
				Teamcenter::scoped_smptr<char> description;
				Teamcenter::scoped_smptr<char> approvalClass;
				Teamcenter::scoped_smptr<char> traceablePart;
				Teamcenter::scoped_smptr<char> descriptionVietnamese;
				Teamcenter::scoped_smptr<char> tcItemType;
				Teamcenter::scoped_smptr<char> partClassName;
				Teamcenter::scoped_smptr<char> partRevClassName;

				logical pCategory = false;
				logical revID = false;
				logical broadcast = false;
				logical fCode = false;
				logical oldPartNo = false;
				logical matType = false;
				logical uomTag = false;
				logical gmnumber = false;
				logical itemid = false;
				logical objName = false;
				logical appCode = false;
				logical itemTrace = false;
				logical des = false;

				tag_t partClassId = NULLTAG;
				tag_t partRevClassId = NULLTAG;

				tag_t tPartRev = plantInput.partRevUIDs;

				CHECK_FAIL(ITEM_ask_item_of_rev(tPartRev, &tPartTag));

				//fetching the properties for item and item revision
				CHECK_FAIL(AOM_ask_value_string(tPartTag,"object_type",&tcItemType));
				if(tc_strcmp(tcItemType.getString(),"VF3_car_part") == 0 || tc_strcmp(tcItemType.getString(),"Vf8_AcarPart") == 0 || tc_strcmp(tcItemType.getString(),"VF4_Design") == 0 || tc_strcmp(tcItemType.getString(),"Design") == 0 || tc_strcmp(tcItemType.getString(),"VF3_manuf_part") == 0 || tc_strcmp(tcItemType.getString(),"VF3_Scooter_part") == 0 || tc_strcmp(tcItemType.getString(),"VF3_me_scooter") == 0 || tc_strcmp(tcItemType.getString(),"VF4_BP_Design") == 0 || tc_strcmp(tcItemType.getString(),"VF7_service_chpt") == 0 || tc_strcmp(tcItemType.getString(),"VF7_service_kit") == 0 || tc_strcmp(tcItemType.getString(),"VF4_Compo_Design") == 0 || tc_strcmp(tcItemType.getString(),"VF4_Cell_Design") == 0 )
				{

					CHECK_FAIL(POM_class_of_instance(tPartTag, &partClassId));
					CHECK_FAIL(POM_name_of_class(partClassId, &partClassName));

					CHECK_FAIL(POM_class_of_instance(tPartRev, &partRevClassId));
					CHECK_FAIL(POM_name_of_class(partRevClassId, &partRevClassName));

					CHECK_FAIL(POM_attr_exists("vf4_part_category",partClassName.getString(),&pCategory));
					CHECK_FAIL(POM_attr_exists("item_revision_id",partRevClassName.getString(),&revID));
					CHECK_FAIL(POM_attr_exists("vf8_Broadcast",partRevClassName.getString(),&broadcast));
					CHECK_FAIL(POM_attr_exists("vf4_function_code",partClassName.getString(),&fCode));
					CHECK_FAIL(POM_attr_exists("vf3_old_part_number",partClassName.getString(),&oldPartNo));
					CHECK_FAIL(POM_attr_exists("vf4_itm_material_type",partClassName.getString(),&matType));
					CHECK_FAIL(POM_attr_exists("uom_tag",partClassName.getString(),&uomTag));
					CHECK_FAIL(POM_attr_exists("vf8_GmNumber",partClassName.getString(),&gmnumber));
					CHECK_FAIL(POM_attr_exists("item_id",partClassName.getString(),&itemid));
					CHECK_FAIL(POM_attr_exists("object_name",partRevClassName.getString(),&objName));
					CHECK_FAIL(POM_attr_exists("vf4_approval_code",partClassName.getString(),&appCode));
					CHECK_FAIL(POM_attr_exists("vf4_item_is_traceable",partClassName.getString(),&itemTrace));
					CHECK_FAIL(POM_attr_exists("vf3_viet_desciption",partRevClassName.getString(),&des));
					if(pCategory)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartTag,"vf4_part_category",&partType));
					}
					if(revID)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartRev,"item_revision_id",&revisionNumber));
					}
					if(broadcast)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartRev,"vf8_Broadcast",&brdCode));
					}
					if(fCode)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartTag,"vf4_function_code",&functionalClass));
					}
					if(oldPartNo)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartTag,"vf3_old_part_number",&oldMaterialNumber));
					}
					if(matType)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartTag,"vf4_itm_material_type",&materialType));
					}
					if(uomTag)
					{
						CHECK_FAIL(AOM_ask_value_tag(tPartTag,"uom_tag",&uom));
						CHECK_FAIL(AOM_ask_value_string(uom,"object_string",&uomName));
					}
					if(gmnumber)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartTag,"vf8_GmNumber",&gmPart));
					}
					if(itemid)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartTag,"item_id",&materialNumber));
					}
					if(objName)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartRev,"object_name",&description));
					}
					if(appCode)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartTag,"vf4_approval_code",&approvalClass));
					}if(itemTrace)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartTag,"vf4_item_is_traceable",&traceablePart));
					}
					if(des)
					{
						CHECK_FAIL(AOM_ask_value_string(tPartRev,"vf3_viet_desciption",&descriptionVietnamese));
					}

					//fetching the plant information
					if(tc_strcmp(tcItemType.getString(),"VF3_car_part") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("VF3_plant_rel", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf3_plant",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf3_make_buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}else if(tc_strcmp(tcItemType.getString(),"Vf8_AcarPart") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("Vf8_PlantFormRel", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf8_Plantcode",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf8_Make_Buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}else if(tc_strcmp(tcItemType.getString(),"VF4_Design") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("VF4_plant_form_relation", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf4_plant",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf4_make_buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}else if(tc_strcmp(tcItemType.getString(),"VF3_manuf_part") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("VF3_plant_rel", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf3_plant",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf3_make_buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}else if(tc_strcmp(tcItemType.getString(),"VF3_Scooter_part") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("VF3_plant_rel", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf3_plant",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf3_make_buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}else if(tc_strcmp(tcItemType.getString(),"VF3_me_scooter") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("VF3_plant_rel", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf3_plant",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf3_make_buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}else if(tc_strcmp(tcItemType.getString(),"VF4_BP_Design") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("VF4_plant_form_relation", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf4_plant",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf4_make_buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}else if(tc_strcmp(tcItemType.getString(),"VF7_service_chpt") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("VF4_plant_form_relation", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf4_plant",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf4_make_buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}else if(tc_strcmp(tcItemType.getString(),"VF7_service_kit") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("VF4_plant_form_relation", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf4_plant",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf4_make_buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}else if(tc_strcmp(tcItemType.getString(),"VF4_Compo_Design") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("VF4_plant_form_relation", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf4_plant",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf4_make_buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}else if(tc_strcmp(tcItemType.getString(),"VF4_Cell_Design") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("VF4_plant_form_relation", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf4_plant",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf4_make_buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}else if(tc_strcmp(tcItemType.getString(),"Design") == 0)
					{
						int plantCount = 0;
						tag_t relTag = NULLTAG;
						Teamcenter::scoped_smptr<tag_t> plantForm;

						CHECK_FAIL(GRM_find_relation_type("VF4_plant_form_relation", &relTag));
						CHECK_FAIL(GRM_list_secondary_objects_only(tPartTag, relTag, &plantCount, &plantForm));
						for(int ii = 0 ; ii < plantCount ; ii++ )
						{
							int procCount = 0;
							PlantInformation plantInfo;
							Teamcenter::scoped_smptr<char> plantCode;
							Teamcenter::scoped_smptr<char*> procurementType;

							CHECK_FAIL(AOM_ask_value_string(plantForm[ii],"vf4_plant",&plantCode));
							CHECK_FAIL(AOM_ask_displayable_values(plantForm[ii],"vf4_make_buy",&procCount,&procurementType));

							plantInfo.plantCode.assign(plantCode.getString());
							if(procCount > 0)
							{
								plantInfo.procurementType.assign(procurementType.getString()[0]);
							}
							outputStatus.sapPlant.push_back(plantInfo);
						}
					}
					outputStatus.partType.assign(partType.getString());
					outputStatus.revisionNumber.assign(revisionNumber.getString());
					outputStatus.brdCode.assign(brdCode.getString());
					outputStatus.functionalClass.assign(functionalClass.getString());
					outputStatus.oldMaterialNumber.assign(oldMaterialNumber.getString());
					outputStatus.materialType.assign(materialType.getString());
					outputStatus.uom.assign(uomName.getString());
					outputStatus.gmPart.assign(gmPart.getString());
					outputStatus.materialNumber.assign(materialNumber.getString());
					outputStatus.description.assign(description.getString());
					outputStatus.approvalClass.assign(approvalClass.getString());
					if(tc_strcmp(traceablePart.getString(),"") == 0)
					{
						outputStatus.traceablePart.assign("N");
					}else
					{
						outputStatus.traceablePart.assign(traceablePart.getString());
					}
					outputStatus.descriptionVietnamese.assign(descriptionVietnamese.getString());
					outputStatus.tcItemType.assign(tcItemType.getString());

					response.outputs.push_back(outputStatus);
				}else{

					outputStatus.errorMessage.assign("Input object type is not a valid object type");
					response.outputs.push_back(outputStatus);
					continue;
				}
			}
			catch(const IFail &e)
			{
				std::cout << e.getMessage();
				response.serviceData.addErrorStack();
				TC_write_syslog("[UOM]Error ::%s\n", e.getMessage());
			}
		}
	}
	TC_write_syslog("[VF] EXIT %s", __FUNCTION__);
	return response;
}

/*convert string  to base64 send MES*/
static char SixBitToChar(char b) {
	char lookupTable[] = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
	};

	if ((b >= 0) && (b <= 63))
	{
		return lookupTable[(int)b];
	}
	else
	{
		return ' ';
	}
}

static string Encode(string data) {
	int length, length2;
	int blockCount;
	int paddingCount;

	length = data.length();

	if ((length % 3) == 0)
	{
		paddingCount = 0;
		blockCount = length / 3;
	}
	else
	{
		paddingCount = 3 - (length % 3);
		blockCount = (length + paddingCount) / 3;
	}

	length2 = length + paddingCount;

	char* source2;
	source2 = new char[length2];

	for (int x = 0; x < length2; x++)
	{
		if (x < length)
		{
			source2[x] = data[x];
		}
		else
		{
			source2[x] = 0;
		}
	}

	char b1, b2, b3;
	char temp, temp1, temp2, temp3, temp4;
	char* buffer = new char[blockCount * 4];
	string result;

	for (int x = 0; x < blockCount; x++)
	{
		b1 = source2[x * 3];
		b2 = source2[x * 3 + 1];
		b3 = source2[x * 3 + 2];

		temp1 = (char)((b1 & 252) >> 2);

		temp = (char)((b1 & 3) << 4);
		temp2 = (char)((b2 & 240) >> 4);
		temp2 += temp;

		temp = (char)((b2 & 15) << 2);
		temp3 = (char)((b3 & 192) >> 6);
		temp3 += temp;

		temp4 = (char)(b3 & 63);

		buffer[x * 4] = temp1;
		buffer[x * 4 + 1] = temp2;
		buffer[x * 4 + 2] = temp3;
		buffer[x * 4 + 3] = temp4;

	}

	for (int x = 0; x < blockCount * 4; x++)
	{
		result += SixBitToChar(buffer[x]);
	}

	switch (paddingCount)
	{
	case 0:
		break;
	case 1:
		result[blockCount * 4 - 1] = '=';
		break;
	case 2:
		result[blockCount * 4 - 1] = '=';
		result[blockCount * 4 - 2] = '=';
		break;
	default:
		break;
	}

	delete[] source2;
	delete[] buffer;

	return result;
}


static size_t WriteCallback(void* contents, size_t length, size_t nmemb, void* userp)
{
    ((std::string*)userp)->append((char*)contents, length * nmemb);
    return length * nmemb;
}


// get access token from server by get method
std::string getAuthToken(string username, string password, string client_id , string client_secret,string scope, string mess)
{
    CURL *curl;
	CURLcode res;
	string _data = "grant_type=password&username="+username+"&password="+password+"&scope="+scope+"&client_id="+client_id+"&client_secret="+client_secret;
	string host= "http://"+mess+"/sit-auth/OAuth/Token?";
	char* _host = const_cast<char*>(host.c_str());
	char *data =const_cast<char*>(_data.c_str());
	string readBuffer ="";
	long response_code= -1;
	curl = curl_easy_init();
	if(curl) {
		  curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, "POST");
		  curl_easy_setopt(curl, CURLOPT_URL,_host);
		  curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
		  curl_easy_setopt(curl, CURLOPT_DEFAULT_PROTOCOL, "https");
		  struct curl_slist *headers = NULL;
		  headers = curl_slist_append(headers, "Content-Type: application/x-www-form-urlencoded");
		  headers = curl_slist_append(headers, "Authorization: Basic aGVsbG86d29ybGQ=");
		  curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
		  curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data);
		  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
		  curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);
		  res = curl_easy_perform(curl);
	  }
	  curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code);
	  if(response_code == 200){
		readBuffer.erase(readBuffer.begin(), readBuffer.begin()+ readBuffer.find("access_token", 0)+15 );
		readBuffer.erase(readBuffer.begin()+readBuffer.find("token_type", 0) - 3, readBuffer.end());
	  }
	 curl_easy_cleanup(curl);
	return readBuffer;
}

/*
//transfer json to mes
std::vector<string> transferOldJsonToMES(string data,string username, string password, string client_id , string client_secret,string scope, string mess ){
	int response_code = -1;
	std::vector<string> result;
	string readBuffer = "";
	std::string posh = "http://"+mess+"/sit-svc/application/FBAPIIAPP/odata/ImportOperationDefinitionWS";
	std::string token = "Authorization: Bearer " +getAuthToken(username,password,client_id,client_secret,scope,mess);
	char* _token = const_cast<char*>(token.c_str());
	char* _posh = const_cast<char*>(posh.c_str());
	CURL *curl;
	CURLcode res;
	curl = curl_easy_init();
	if(curl) {
	  curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, "POST");
	  curl_easy_setopt(curl, CURLOPT_URL, _posh);
	  curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
	  curl_easy_setopt(curl, CURLOPT_DEFAULT_PROTOCOL, "https");
	  struct curl_slist *headers = NULL;
	  headers = curl_slist_append(headers, _token );
	  curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
	  const char *_data = data.c_str();
	  curl_easy_setopt(curl, CURLOPT_POSTFIELDS, _data);
	  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
	  curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);
	  res = curl_easy_perform(curl);
	  curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code);
	}
	curl_easy_cleanup(curl);

	result.push_back( std::to_string(response_code));
	result.push_back(readBuffer);
	return result;
}
*/



/*transfer xml to MES*/
int transferOldXmlToMES(string data, string mesServerIP, int port)
{
    std::string readBuffer;
    CURL* curl;
    int response_code = -1;
    port;
    CURLcode res;
    std::string postField = "<s:Envelope xmlns:a=\"http://www.w3.org/2005/08/addressing\" xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"> <s:Header> <a:Action>http://tempuri.org/IHttpsServer/SetData</a:Action> <a:To>http://10.128.11.100/ImportOperations</a:To> </s:Header> <s:Body> <SetData xmlns=\"http://tempuri.org/\"> <data>" + data + "</data> <enc>UTF8</enc> </SetData> </s:Body> </s:Envelope>";
    curl = curl_easy_init();
    if (curl)
    {
        char* url = const_cast<char*>(mesServerIP.c_str());
        struct curl_slist* slist = NULL;
        slist = curl_slist_append(slist, "Content-Type: application/soap+xml");
        std::string content_length = "Content-Length: " + std::to_string(postField.size());
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, slist);
        curl_easy_setopt(curl, CURLOPT_VERBOSE, 1L);
        curl_easy_setopt(curl, CURLOPT_HEADER, 0);
        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_TIMEOUT, 30);
        curl_easy_setopt(curl, CURLOPT_CONNECTTIMEOUT, 10L);
		curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, FALSE);
		curl_easy_setopt(curl, CURLOPT_VERBOSE, TRUE);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, postField.c_str());
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
		curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);
        res = curl_easy_perform(curl);
        if (res != CURLE_OK)
        {
        	response_code = 404;
        }
		curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code);
        curl_easy_cleanup(curl);
        curl_slist_free_all(slist);
    }
    return response_code;
}

std::vector<string> split_string2(std::string s, std::string delimiter) {
	size_t pos_start = 0, pos_end, delim_len = delimiter.length();
	std::string token;
	std::vector<std::string> res;

	while ((pos_end = s.find(delimiter, pos_start)) != std::string::npos) {
		token = s.substr(pos_start, pos_end - pos_start);
		pos_start = pos_end + delim_len;
		res.push_back(token);
	}
	res.push_back(s.substr(pos_start));
	return res;
}

//
//int transfer_op_to_mes(string item_id, string rev_id, string mesServerIP, int port) {
//    int ret_code = ITK_ok;
//    int ret_mes = -1;
//    int acount;
//    vector<string>::iterator itr;
//    ITEM_attached_object_t* objects;
//    char* wso_name = NULL;
//    char* wso_type = NULL;
//    tag_t query = NULLTAG;
//    tag_t item_rev_tag = NULLTAG;
//    int foundsNum = -1;
//    tag_t* founds = NULL;
//    tag_t dataset = NULL;
//    tag_t text_file = NULLTAG;;
//    IMF_file_t file_descriptor = NULL;
//    vector<std::string> xmlnew;
//    CHECK_FAIL(QRY_find2("Item...", &query));
//    char* entries[2] = { "Item ID", "Type" };
//    char** values = (char**)MEM_alloc(sizeof(char*) * 2);
//    values[0] = (char*)MEM_alloc(sizeof(char) * (tc_strlen(item_id.c_str()) + 1));
//    values[1] = (char*)MEM_alloc(sizeof(char) * (tc_strlen("Operation") + 1));
//    tc_strcpy(values[0], item_id.c_str());
//    tc_strcpy(values[1], "Operation");
//    CHECK_FAIL(QRY_execute(query, 2, entries, values, &foundsNum, &founds));
//    tag_t ptRevs = founds[0];
//    CHECK_FAIL(ITEM_find_revision(ptRevs, rev_id.c_str(), &item_rev_tag));
//    CHECK_FAIL(ITEM_list_rev_attachs_of_type(item_rev_tag, ITEM_specification_atth, &acount, &objects));
//    for (int i = 0; i < acount; i++) {
//        CHECK_FAIL(WSOM_ask_name2(objects[i].attachment, &wso_name));
//        CHECK_FAIL(WSOM_ask_object_type2(objects[i].attachment, &wso_type));
//        if (string(wso_name).find(item_id + "_" + rev_id, 0) != -1) {
//            if (tc_strcmp(wso_type, "HTML") == 0) {
//                CHECK_FAIL(AE_find_dataset2(wso_name, &dataset))
//                if (ret_code == ITK_ok && dataset != NULL_TAG)
//                {
//                    CHECK_FAIL(AOM_refresh(dataset, FALSE));
//                    AE_reference_type_t ref_type;
//                    tag_t text_file = NULL_TAG;
//                    CHECK_FAIL(AE_ask_dataset_named_ref2(dataset, "HTML", &ref_type, &text_file));
//                    if (ret_code != ITK_ok)
//                    {
//                        TC_write_syslog("[VF_handlers]__%d__%s__Could not get dataset file: %d\n", __LINE__, __FUNCTION__, ret_code);
//                    }
//                    char textLine[SS_MAXLLEN + 1];
//                    if (ret_code == ITK_ok && text_file != NULL_TAG)
//                    {
//                        CHECK_FAIL(IMF_ask_file_descriptor(text_file, &file_descriptor));
//                        CHECK_FAIL(IMF_open_file(file_descriptor, SS_RDONLY));
//                        char* textLine = NULL;
//                        while (IMF_read_file_line2(file_descriptor, &textLine) == ITK_ok)//@SKIP_DEPRECATED
//                        {
//                            std::string strLine(textLine);
//                            xmlnew = split_string2(textLine, "~~");	printf("\n");
//                        }
//                    }
//                    itr = find(xmlnew.begin(), xmlnew.end(), "<?xml");
//                    if (ret_code == ITK_ok && itr == xmlnew.end()) {
//                        string xml = xmlnew[0];
//                        xml.erase(xml.begin() + xml.find("<?xml", 0), xml.begin() + xml.find("?>", 0) + 2);
//                        char* set = NULL;
//                        char* pc = &xml[0];
//                        ret_mes = transferOldXmlToMES(base64Encoder(pc, xml.length()), mesServerIP,port);
//                    }
//                }
//                SAFE_SM_FREE(wso_type);
//                SAFE_SM_FREE(wso_name);
//            }
//        }
//
//    }
//    return ret_mes;
//}
//
//
//


int transfer_op_to_mes(string xml, string mesServerIP, int port) {
	xml.erase(xml.begin() + xml.find("<?xml", 0), xml.begin() + xml.find("?>", 0) + 2);
	char* pc = &xml[0];
	int ret_mes = transferOldXmlToMES(Encode(pc), mesServerIP,port);
    return ret_mes;
}


SAPIntegrationImpl::TransferOperationToJSONResponse SAPIntegrationImpl::transferOperationJSONToMES ( const TransferOperationToJSONInputs& inputs )
{
    // TODO implement operation
	SAPIntegrationImpl::TransferOperationToJSONResponse res;

	for (auto input : inputs.inputs)
	{
		string readBuffer = "";
		int response_code = -1;
		if (input.json.empty() != 0)	{
			std::string posh = "http://"+inputs.mesServerIP+"/sit-svc/application/FBAPIIAPP/odata/ImportOperationDefinitionWS";
			std::string token = "Authorization: Bearer " +getAuthToken(inputs.mesSeverUser,inputs.mesSeverPass,inputs.mesServerClientID,inputs.mesServerClientSecret,inputs.mesServerScope,inputs.mesServerIP);
			char* _token = const_cast<char*>(token.c_str());
			char* _posh = const_cast<char*>(posh.c_str());
			CURL *curl;
			CURLcode res;
			curl = curl_easy_init();
			if(curl) {
			  curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, "POST");
			  curl_easy_setopt(curl, CURLOPT_URL, _posh);
			  curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
			  curl_easy_setopt(curl, CURLOPT_DEFAULT_PROTOCOL, "https");
			  struct curl_slist *headers = NULL;
			  headers = curl_slist_append(headers, _token );
			  curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
			  const char *_data = input.json.c_str();
			  curl_easy_setopt(curl, CURLOPT_POSTFIELDS, _data);
			  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
			  curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);
			  res = curl_easy_perform(curl);
			  curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code);
			}
			curl_easy_cleanup(curl);

		}
		res.operationIDs.push_back(input.operationID);
		res.returnCodes.push_back(std::to_string(response_code));
		res.operationMessages.push_back(readBuffer);
	}
	return res;
}


SAPIntegrationImpl::TransferOperationToMESResponse SAPIntegrationImpl::transferOperationToMES ( const TransferOperationToMESInputs& inputs )
{
	SAPIntegrationImpl::TransferOperationToMESResponse res;

		for (auto input : inputs.inputs)
		{
			int mesReturnCode = -1;
			if (input.xml.empty())
			{
				//mesReturnCode = transfer_op_to_mes(input.operationID, input.operationRevID, inputs.mesServerIP, inputs.mesServerPort);
			}
			else
			{
				mesReturnCode = transfer_op_to_mes(input.xml, inputs.mesServerIP, inputs.mesServerPort);
			}

			res.operationIDs.push_back(input.operationID);
			res.returnCodes.push_back(std::to_string(mesReturnCode));
		}

		return res;
}



