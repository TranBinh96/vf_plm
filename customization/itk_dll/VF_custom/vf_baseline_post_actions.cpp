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

extern DLLAPI int DateCompareMethod( METHOD_message_t *message , va_list args );



DLLAPI int vf_baseline_post_actions_register ()
{
	TC_write_syslog("\n[vf] Enter %s", __FUNCTION__);

	int	ifail  = ITK_ok;

	METHOD_id_t VF4_DesignRevision_post_action;
	METHOD_id_t VF4_BP_DesignRevision_post_action;
	METHOD_id_t VF4_Study_PartRevision_post_action;
	METHOD_id_t PR4D_MS_IT_VINFRevision_post_action;
	METHOD_id_t VF3_Scooter_part_post_action;

	//Postaction Method for VF Design Revision
	ifail = METHOD_find_method ("VF4_DesignRevision" ,"ITEM_baseline_rev" , &VF4_DesignRevision_post_action );

	if (VF4_DesignRevision_post_action.id != NULLTAG)
	{
		TC_write_syslog("\n ITEM_baseline_rev Method found for VF4_DesignRevision");
		ifail = METHOD_add_action ( VF4_DesignRevision_post_action , METHOD_post_action_type , DateCompareMethod , NULL );
	} 
	else
	{
		TC_write_syslog("\n ITEM_baseline_rev Method not found for VF4_DesignRevision ");
	}

	
	//Postaction Method for VF BP Design Revision
	ifail = METHOD_find_method ("VF4_BP_DesignRevision" ,"ITEM_baseline_rev" , &VF4_BP_DesignRevision_post_action );

	if (VF4_BP_DesignRevision_post_action.id != NULLTAG)
	{
		TC_write_syslog("\n  ITEM_baseline_rev Method found for VF4_BP_DesignRevision ");
		ifail = METHOD_add_action ( VF4_BP_DesignRevision_post_action , METHOD_post_action_type , DateCompareMethod , NULL );
	} 
	else
	{
		TC_write_syslog("\n ITEM_baseline_rev Method not found for VF4_BP_DesignRevision ");
	}

	
	//Postaction Method for Study Part Revision
	ifail = METHOD_find_method ("VF4_Study_PartRevision" ,"ITEM_baseline_rev" , &VF4_Study_PartRevision_post_action );

	if (VF4_Study_PartRevision_post_action.id != NULLTAG)
	{
		TC_write_syslog("\n ITEM_baseline_rev  Method found for VF4_Study_PartRevision");
		ifail = METHOD_add_action ( VF4_Study_PartRevision_post_action , METHOD_post_action_type , DateCompareMethod , NULL );
	} 
	else
	{
		TC_write_syslog("\nITEM_baseline_rev  Method not found for VF4_Study_PartRevision ");
	}

	//post action Method for PR4D_MS_IT_VINFRevision
	ifail = METHOD_find_method ("PR4D_MS_IT_VINFRevision" ,"ITEM_baseline_rev" , &PR4D_MS_IT_VINFRevision_post_action );

	if (PR4D_MS_IT_VINFRevision_post_action.id != NULLTAG)
	{
		TC_write_syslog("\n ITEM_baseline_rev  Method found for PR4D_MS_IT_VINFRevision");
		ifail = METHOD_add_action ( PR4D_MS_IT_VINFRevision_post_action , METHOD_post_action_type , DateCompareMethod , NULL );
	} 
	else
	{
		TC_write_syslog("\n ITEM_baseline_rev  Method found for PR4D_MS_IT_VINFRevision ");
	}

	//Post action Method for Escooter Part Revision
	ifail = METHOD_find_method ("VF3_Scooter_partRevision" ,"ITEM_baseline_rev" , &VF3_Scooter_part_post_action );

	if (VF3_Scooter_part_post_action.id != NULLTAG)
	{
		TC_write_syslog("\n ITEM_baseline_rev  Method found for VF3_Scooter_part");
		ifail = METHOD_add_action ( VF3_Scooter_part_post_action , METHOD_post_action_type , DateCompareMethod , NULL );
	} 
	else
	{
		TC_write_syslog("\n ITEM_baseline_rev  Method not found for VF3_Scooter_part ");
	}

	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);
	return ifail;
}

extern DLLAPI int DateCompareMethod( METHOD_message_t *message , va_list args )
{
	tag_t	base_rev_tag			= va_arg(args,tag_t);
	const	char* baseline_rev_id	= va_arg(args,char*);
	const	char* description		= va_arg(args,char*);
	const	char* rel_proc_name		= va_arg(args,char*);
	const	char* job_name			= va_arg(args,char*);
	const	char* job_description	= va_arg(args,char*);
	logical	dryrun					= va_arg(args,logical);
	tag_t*	new_rev					= va_arg(args,tag_t*);
	int*	deepCopiedObjectCount	= va_arg(args,int*);
	tag_t**	deepCopiedObjects		= va_arg(args,tag_t**);


	TC_write_syslog("\n[vf] Enter %s", __FUNCTION__);

	//ITK_set_bypass(true);
	int ifail								= ITK_ok;
	tag_t		renderingRelationType		= NULLTAG;
	tag_t		specificationRelationType	= NULLTAG;
	int			ren_count_secondary_object	= 0;
	int			spec_count_secondary_object	= 0;
	tag_t		*ren_secondary_object		= NULLTAG;
	tag_t		*spec_secondary_object		= NULLTAG;
	char*		objectType					=NULL;
	date_t		JT_Last_modified_Date		=NULLDATE;
	date_t		CATPart_Last_modified_Date	=NULLDATE;
	char*		JT_Date_String				=NULL;
	char*		CATpart_Date_String			=NULL;
	tag_t		JT_DataSet_tag				=NULLTAG;
	tag_t		CATPart_Dataset_tag			=NULLTAG;
	char*		RevName						=NULL;

	int		answer							=0;
	tag_t	JT_NamedRef_Object				=NULLTAG;
	tag_t	CATPart_NamedRef_Object			=NULLTAG;
	AE_reference_type_t reftype;

	char *String=NULL;
	char *Str=NULL;

	if(*new_rev!=NULLTAG)
	{
		ifail = AOM_ask_value_string(base_rev_tag, "object_string", &RevName);
		if(ifail != ITK_ok)
		{
			TC_write_syslog("\n Error Code : %d",ifail);
			printf("\n Error Code : %d",ifail);
		}
		else
		{
			TC_write_syslog("\n Base Revision Object name : %s", RevName);
			printf("\n Base Revision Object name : %s", RevName);
		}
		

		ifail = GRM_find_relation_type("IMAN_Rendering", &renderingRelationType);
		if(ifail != ITK_ok)
		{
			TC_write_syslog("\n IMAN Rendering Relation not found");
			printf("\n IMAN Rendering Relation not found");
		}
		else
		{
			ifail = GRM_list_secondary_objects_only(base_rev_tag, renderingRelationType, &ren_count_secondary_object, &ren_secondary_object);
			if(ifail != ITK_ok)
			{
				TC_write_syslog("\n GRM_list_secondary_objects_only  failed");
				printf("\n GRM_list_secondary_objects_only  failed");
			}
			else
			{
				TC_write_syslog("\n secondary attachment count with Rendering relation : %d",ren_count_secondary_object);
				printf("\n secondary attachment count with Rendering relation : %d",ren_count_secondary_object);

				if(ren_count_secondary_object >= 1)
				{
					for(int k = 0;k < ren_count_secondary_object;k++)
					{
						ifail = AOM_ask_value_string(ren_secondary_object[k], "object_type", &objectType);
						if(ifail != ITK_ok)
						{
							TC_write_syslog("\n Error Code : %d ", ifail);
							printf("\n Error Code : %d ", ifail);
						}
						else
						{
							TC_write_syslog("\n Secondary Object type is : %s  ", objectType);
							printf("\n Secondary Object type is : %s  ", objectType);

							if(strcmp(objectType,"DirectModel")==0)
							{
									JT_DataSet_tag=ren_secondary_object[k];
									ifail = AE_ask_dataset_named_ref2(JT_DataSet_tag, "JTPART", &reftype, &JT_NamedRef_Object);

									if(JT_NamedRef_Object!=NULLTAG)
									{
										ifail = AOM_ask_value_date(JT_NamedRef_Object, "last_mod_date", &JT_Last_modified_Date);
										if(ifail == ITK_ok)
										{
											ifail = ITK_date_to_string(JT_Last_modified_Date, &JT_Date_String);

											TC_write_syslog("\n JT Last_modified_date  : %s  ", JT_Date_String);
											printf("\n JT Last_modified_date  : %s  ", JT_Date_String);
										}
									}
									else
									{
										printf("\n Error code : %d",ifail);
										TC_write_syslog("\n Error code : %d",ifail);
										TC_write_syslog("\n No JT Reference Found in dataset");
										printf("\n No JT Reference Found in dataset");
									}
							}
						}
					}		
				}
			}
		}
	

		ifail = GRM_find_relation_type("IMAN_specification", &specificationRelationType);
		if(ifail != ITK_ok)
		{
			TC_write_syslog("\n IMAN Specification Relation not found");
			printf("\n IMAN Specification Relation not found");
		}
		else
		{
			ifail = GRM_list_secondary_objects_only(base_rev_tag, specificationRelationType, &spec_count_secondary_object, &spec_secondary_object);
			if(ifail != ITK_ok)
			{
				TC_write_syslog("\n GRM_list_secondary_objects_only  failed");
				printf("\n GRM_list_secondary_objects_only  failed");
			}
			else
			{
				TC_write_syslog("\n secondary attachment count with Specification relation : %d",spec_count_secondary_object);
				printf("\n secondary attachment count with Specifications relation : %d",spec_count_secondary_object);

				if(spec_count_secondary_object >= 1)
				{
					for(int i = 0;i < spec_count_secondary_object;i++)
					{
						ifail = AOM_ask_value_string(spec_secondary_object[i], "object_type", &objectType);
						if(ifail != ITK_ok)
						{
							TC_write_syslog("\n Error Code : %d ", ifail);
							printf("\n Error Code : %d ", ifail);
						}
						else
						{
							TC_write_syslog("\n Secondary Object type is : %s  ", objectType);
							printf("\n Secondary Object type is : %s  ", objectType);

							if(strcmp(objectType,"CATPart")==0)
							{
									CATPart_Dataset_tag=spec_secondary_object[i];
									ifail = AE_ask_dataset_named_ref2(CATPart_Dataset_tag, "catpart", &reftype, &CATPart_NamedRef_Object);

									if(CATPart_NamedRef_Object!=NULLTAG)
									{
										ifail = AOM_ask_value_date(CATPart_NamedRef_Object, "last_mod_date", &CATPart_Last_modified_Date);
										if(ifail == ITK_ok)
										{
											ifail = ITK_date_to_string(CATPart_Last_modified_Date, &CATpart_Date_String);

											TC_write_syslog("\n CATPart Last_modified_date  : %s  ", CATpart_Date_String);
											printf("\n CATPart Last_modified_date  : %s  ", CATpart_Date_String);
										}
									}
									else
									{
										printf("\n Error code : %d",ifail);
										TC_write_syslog("\n Error code : %d",ifail);
										TC_write_syslog("\n No CATPart Reference Found in dataset");
										printf("\n No CATPart Reference Found in dataset");
									}
							}
						}		
					}
				}
			}
		}

		if(JT_DataSet_tag!=NULLTAG && CATPart_Dataset_tag!=NULLTAG && CATPart_NamedRef_Object!=NULLTAG && JT_NamedRef_Object!=NULLTAG)
		{
			ifail = POM_compare_dates(JT_Last_modified_Date, CATPart_Last_modified_Date, &answer);
			if(ifail != ITK_ok)
			{
				TC_write_syslog("\nInvalid date");  
				printf("\n Invalid date");
			}
			else
			{
				// 1 == JTDate > CATPartDate;   -1 == JTDate < CATPartDate
				if(answer==-1)
				{
					TC_write_syslog("\n  JT Modified Date : %s And  CATPart Modified Date : %s",JT_Date_String,CATpart_Date_String);
					printf("\n  JT Modified Date : %s And CATPart Modified Date : %s",JT_Date_String,CATpart_Date_String);

					ifail=AOM_refresh(*new_rev, TRUE);
					if(ifail != ITK_ok)
					{
						TC_write_syslog("\n  Error Code : %d",ifail);  
						printf("\n Error Code : %d",ifail);
					}
					else
					{
						string cmp_Date="";
						string Cat_Date="";

						cmp_Date.assign("JT - ");
						cmp_Date.append(JT_Date_String);
						cmp_Date.append("\nCAT - ");
						cmp_Date.append(CATpart_Date_String);

						TC_write_syslog("\n Compare Date : %s",cmp_Date.c_str());
						printf("\n Compare Date : %s",cmp_Date.c_str());

						if(tc_strlen(cmp_Date.c_str()) > 0)
						{
							ifail=AOM_set_value_string(*new_rev, "vf4_jt_last_modify", cmp_Date.c_str());
							if(ifail != ITK_ok)
							{
								TC_write_syslog("\n Error Code : %d",ifail);
								printf("\n Error Code : %d",ifail);
							}
							else
							{
								TC_write_syslog("\n JT Last modification date stored in description attribute ");
								printf("\n JT Last modification date stored in description attribute ");

								ifail = AOM_save_with_extensions(*new_rev);
								if(ifail == ITK_ok)
								{
									ifail = AOM_refresh(*new_rev,FALSE);
								}
								else
								{
									TC_write_syslog("\n Checkin failed ");
									printf("\n chekin failed ");
								}

								tag_t rel = NULLTAG;
								TC_write_syslog("\n [vf] search to remove jt ..");
								int count = 0;
								GRM_relation_t *seconds = NULL;
								GRM_list_secondary_objects(*new_rev, renderingRelationType, &count, &seconds);
								
								for(int aa = 0;aa < count;aa++)
								{
									ifail = AOM_ask_value_string(seconds[aa].secondary, "object_type", &objectType);
									if(ifail != ITK_ok)
									{
										TC_write_syslog("\n Error Code : %d ", ifail);
										printf("\n Error Code : %d ", ifail);
									}
									else
									{
										TC_write_syslog("\n Secondary Object type is : %s  ", objectType);
										printf("\n Secondary Object type is : %s  ", objectType);

										if(strcmp(objectType,"DirectModel")==0)
										{
											TC_write_syslog("\n [vf] removing jt");
											int retcode = GRM_delete_relation(seconds[aa].the_relation);
											TC_write_syslog("\n [vf] removed jt %d", retcode);
										}
									}
								}

								//SAFE_MEM_free(seconds);
							}
						}
						else
						{
							TC_write_syslog("\n Comapre Date NULL");  
							printf("\n Comapre Date NULL");
						}
					}
				}
				else
				{
					TC_write_syslog("\n No Comparision");  
					printf("\n No Comparision");
				}
			}
		}
		else
		{
			TC_write_syslog("\n Correct JTPart Attached with the revision");
			printf("\n Correct JTPart Attached with the revision");
		}				
	}

	TC_write_syslog("\n[vf] Leave %s\n", __FUNCTION__);
	return ifail;
}