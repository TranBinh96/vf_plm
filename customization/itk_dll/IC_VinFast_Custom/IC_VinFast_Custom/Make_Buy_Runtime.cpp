#include <stdio.h>
#include <iostream>
#include <string>
#include <tc/tc.h>
#include <tc/emh.h>
#include <tc/preferences.h>
#include <tc/envelope.h>
#include <res/res_itk.h>
#include <sa/user.h>
#include <ae/ae.h>
#include <ps/ps.h>
#include <cfm/cfm.h>
#include <bom/bom.h>
#include <bom/bom_attr.h>
#include <epm/epm.h>
#include <epm/signoff.h>
#include <epm/epm_task_template_itk.h>
#include <epm/epm_toolkit_tc_utils.h>
#include <form/form.h>
#include <tccore/aom.h>
#include <tccore/grm.h>
#include <tccore/item.h>
#include <tccore/custom.h>
#include <tccore/tctype.h>
#include <tccore/method.h>
#include <tccore/aom_prop.h>
#include <tccore/tc_msg.h>
#include <tccore/workspaceobject.h>
#include <base_utils/Mem.h>
#include <pom/enq/enq.h>
#include <property/prop_msg.h>
#include <user_exits/user_exits.h>
#include <user_exits/epm_toolkit_utils.h>
#include <ict/ict_userservice.h>
#include <fclasses/tc_date.h>
#include <textsrv/textserver.h>
#include <sa/am.h>
#include <tc/folder.h>
#include <tccore/project.h>
#include <tc\tc_util.h>
#include <tccore\item_msg.h>
#include <form\form.h>
#include <math.h>
#include <ics\ics.h>
#include <ics\ics2.h>
#include <tc\tc_errors.h>
#include <ug_va_copy.h>

using namespace std;

#define CHECK_ITK(iSts,fn){\
    if(iSts == ITK_ok){\
        if((iSts = (fn) ) != ITK_ok){\
            char* error_str = NULL;\
            EMH_ask_error_text(iSts, &error_str);\
            TC_write_syslog ("+++attach deliverables ERROR: %d ERROR MSG: %s. Error in line %d, function %s",\
                iSts, error_str, __LINE__, __FUNCTION__);\
            MEM_free(error_str);\
        }\
    }\
}

#define SAFE_MEM_free(x)\
  if(x != NULL)\
  MEM_free(x);\
  x = NULL;\

/**********************************************************************************
/*Desc: To set Make/Buy value on BOM line reading from Plant form*/
/*********************************************************************************/
int set_make_buy(METHOD_message_t* msg, va_list args)
{
    int		iRetCode				= ITK_ok;
    int		plantCount				= 0;
    tag_t	tBomLine				= NULLTAG; 
    tag_t	tChildItemRevision		= NULLTAG;
    tag_t	tItemRevision			= NULLTAG;
    tag_t	Item					= NULLTAG;
    tag_t	plant_rel				= NULLTAG;
    tag_t	*plant_form				= NULL;
    tag_t	tPropTag				= va_arg(args, tag_t);
    char	**value					= va_arg(args, char**);
    char	*pcObjectType			= NULL;
    char	*task					= NULL;
    char	*Make_Buy				= NULL;
    char	*pcType					= NULL;
    
    /*get bom line tag*/
	tBomLine = msg->object_tag;
	//CHECK_ITK(iRetCode, PROP_ask_owning_object(tPropTag, &tBomLine));
    if(iRetCode == ITK_ok && tBomLine != NULLTAG)
    {
        /*get item revision tag*/
        CHECK_ITK(iRetCode, AOM_ask_value_tag(tBomLine,"bl_line_object",&tItemRevision));
        if(iRetCode == ITK_ok && tItemRevision != NULLTAG)
        {
            /*get item tag*/
            CHECK_ITK(iRetCode, ITEM_ask_item_of_rev(tItemRevision , &Item));
            CHECK_ITK(iRetCode,WSOM_ask_object_type2(tItemRevision,&pcType));
            if(tc_strcmp(pcType,"VF3_car_partRevision") == 0 || tc_strcmp(pcType,"VF3_Scooter_partRevision") == 0)
            {
                if(iRetCode == ITK_ok && Item != NULLTAG)
                {
                    /*get plant form relation*/
                    CHECK_ITK(iRetCode, GRM_find_relation_type("VF3_plant_rel",&plant_rel));
                    if(iRetCode == ITK_ok && plant_rel != NULLTAG)
                    {
                        /*get plant forms*/
                        CHECK_ITK(iRetCode, GRM_list_secondary_objects_only(Item,plant_rel,&plantCount,&plant_form));
                        TC_write_syslog("\nNUMBER OF PLANT FORMS ARE : %d",plantCount);
    
                        for (int inx = 0 ; inx < plantCount ; inx ++)
                        {
                            char	*Plant			= NULL ;
                            tag_t	plant_form_tag	= plant_form[inx] ;

                            /*if there is only one plant form*/
                            if (plantCount == 1)
                            {
                                CHECK_ITK(iRetCode, AOM_ask_value_string(plant_form_tag , "vf3_make_buy" , &Make_Buy));
                                TC_write_syslog("\nMAKE/BUY VALUE : %s",Make_Buy);
                            }

                            /*if there are multiple forms, get value from CAR or SCOOTER plant*/
                            else
                            {
                                CHECK_ITK(iRetCode, AOM_ask_value_string(plant_form_tag , "vf3_plant" , &Plant));
                                if (tc_strcmp("3001" , Plant) == 0)
                                {
                                    CHECK_ITK(iRetCode, AOM_ask_value_string(plant_form_tag , "vf3_make_buy" , &Make_Buy));
                                    TC_write_syslog("\nMAKE/BUY VALUE : %s",Make_Buy);
                                }

                                if (tc_strcmp("2001" , Plant) == 0)
                                {
                                    CHECK_ITK(iRetCode, AOM_ask_value_string(plant_form_tag , "vf3_make_buy" , &Make_Buy));
                                    TC_write_syslog("\nMAKE/BUY VALUE : %s",Make_Buy);
                                }
                            }
                        }

                        *value = Make_Buy;
                    }
                }
            }

            if(tc_strcmp(pcType,"Vf8_AcarPartRevision") == 0)
            {
                if(iRetCode == ITK_ok && Item != NULLTAG)
                {
                    /*get plant form relation*/
                    CHECK_ITK(iRetCode, GRM_find_relation_type("Vf8_PlantFormRel",&plant_rel));
                    if(iRetCode == ITK_ok && plant_rel != NULLTAG)
                    {
                        /*get plant forms*/
                        CHECK_ITK(iRetCode, GRM_list_secondary_objects_only(Item,plant_rel,&plantCount,&plant_form));
                        TC_write_syslog("\nNUMBER OF PLANT FORMS ARE : %d",plantCount);
    
                        for (int inx = 0 ; inx < plantCount ; inx ++)
                        {
                            char	*Plant			= NULL ;
                            tag_t	plant_form_tag	= plant_form[inx] ;

                            /*if there is only one plant form*/
                            if (plantCount == 1)
                            {
                                CHECK_ITK(iRetCode, AOM_ask_value_string(plant_form_tag , "vf8_Make_Buy" , &Make_Buy));
                                TC_write_syslog("\nMAKE/BUY VALUE : %s",Make_Buy);
                            }

                            /*if there are multiple forms, get value from ACAR plant
                            else
                            {
                                CHECK_ITK(iRetCode, AOM_ask_value_string(plant_form_tag , "vf3_plant" , &Plant));
                                if (tc_strcmp("3001" , Plant) == 0)
                                {
                                    CHECK_ITK(iRetCode, AOM_ask_value_string(plant_form_tag , "vf8_Make_Buy" , &Make_Buy));
                                    TC_write_syslog("\nMAKE/BUY VALUE : %s",Make_Buy);
                                }

                                if (tc_strcmp("2001" , Plant) == 0)
                                {
                                    CHECK_ITK(iRetCode, AOM_ask_value_string(plant_form_tag , "vf8_Make_Buy" , &Make_Buy));
                                    TC_write_syslog("\nMAKE/BUY VALUE : %s",Make_Buy);
                                }
                            }*/
                        }

                        *value = Make_Buy;
                    }
                }
            }
            return iRetCode ;
        }
    }
}


/**********************************************************************************
/*Desc: To set SCP Vehilcle Type on BOM line reading from SCP Purchase Vehicle Type*/
/*********************************************************************************/
int set_scp_vehicle_type(METHOD_message_t* msg, va_list args)
{
    int		iRetCode				= ITK_ok;
    tag_t	tBomLine				= NULLTAG; 
    tag_t	tChildItemRevision		= NULLTAG;
    tag_t	tItemRevision			= NULLTAG;
    tag_t	Item					= NULLTAG;
    tag_t	tPropTag				= va_arg(args, tag_t);
    char	**value					= va_arg(args, char**);
    
    /*get bom line tag*/
	tBomLine = msg->object_tag;
	//CHECK_ITK(iRetCode, PROP_ask_owning_object(tPropTag, &tBomLine));
    if(iRetCode == ITK_ok && tBomLine != NULLTAG)
    {
        /*get item revision tag*/
        CHECK_ITK(iRetCode, AOM_ask_value_tag(tBomLine,"bl_line_object",&tItemRevision));
        if(iRetCode == ITK_ok && tItemRevision != NULLTAG)
        {
            int			iValue			= 0;
            char		**pcValues		= NULL;
            string		szType			= "";
            char        *pcType			= NULL;
            char        *pcObjType     = NULL;

            CHECK_ITK(iRetCode,WSOM_ask_object_type2(tItemRevision,&pcObjType));

            if(tc_strcmp(pcObjType,"VL5_e_designRevision") == 0)
            {
                /*get vehicle type*/
                CHECK_ITK(iRetCode, AOM_ask_value_strings(tItemRevision,"vl5_scp_vehicle_type",&iValue,&pcValues));
                for(int iCnt = 0; iCnt < iValue; iCnt++)
                {
                    /*copying all vehicle type values to string*/
                    if(iCnt+1 < iValue)
                    {
                        szType.append(pcValues[iCnt]);
                        szType.append(",");
                    }
                    else
                    {
                        szType.append(pcValues[iCnt]);
                    }
                }
                pcType = (char*) MEM_alloc(( (int)tc_strlen(szType.c_str()) + 1) * sizeof(char));
                tc_strcpy( pcType, szType.c_str());
            
                *value = pcType;
            }
        }
    }
    return iRetCode ;
}

/**********************************************************************************
/*Desc: To set ECO Number or ECN Number on BOM Line
/       ECO Number -> Get Pos ID value from BOM Line
/				   -> Query with Pos ID to get change form
/                  -> Read EC Number from Change form and set on BOM Line
/		ECN Number -> Get ECN Revision from which Item Revision is released
/				   -> Set ECN Number
/**********************************************************************************/
int set_eco_number(METHOD_message_t* msg, va_list args)
{
    int		iRetCode			= ITK_ok;
    tag_t	tBomLine			= NULLTAG;
    tag_t   tChildItemRevision	= NULLTAG;
    tag_t	tItemRevision		= NULLTAG;
    tag_t	Item				= NULLTAG;
    tag_t	tPropTag			= va_arg(args, tag_t);
    char	**value				= va_arg(args, char**);
    char	*change_number		= NULL;
    char	*positionIndex		= NULL;

	tBomLine = msg->object_tag;
	//CHECK_ITK(iRetCode, PROP_ask_owning_object(tPropTag, &tBomLine));
    CHECK_ITK(iRetCode, AOM_ask_value_tag(tBomLine,"bl_line_object",&tItemRevision));
    if(iRetCode == ITK_ok && tItemRevision != NULLTAG)
    {
        /*Get POS ID from BOM Line*/
        CHECK_ITK(iRetCode, AOM_ask_value_string(tBomLine,"VF3_pos_ID",&positionIndex));
        /*for ECO Number*/
        if(positionIndex != NULL && tc_strcmp(positionIndex , "") != 0)
        {
            TC_write_syslog("POSITION INDEX : %s\n", positionIndex);

            int		n_found					= 0;
            int     n_entries				= 1;
            int		n_order[]				= {2};
            tag_t	query_tag				= NULLTAG;
            tag_t	*results				= NULL;
            char    **entries				= NULL; 
            char    **values				= NULL;
            char    critrea_entry [1][18]	= {"Name"};
            char    *critrea_value[1][18]	= {positionIndex};
            char	*keys[]					= {"creation_date"};

            entries = (char **) MEM_alloc(n_entries * sizeof(char *));
            values  = (char **) MEM_alloc(n_entries * sizeof(char *));

            for ( int ii = 0; ii < n_entries; ii++ )
            {
                entries[ii] = (char *)MEM_alloc(strlen( critrea_entry[ii] ) + 1);
                tc_strcpy(entries[ii],  critrea_entry[ii] );

                values[ii] = (char *)MEM_alloc( tc_strlen( critrea_value[ii][ii] ) + 1);
                tc_strcpy(values[ii],  critrea_value[ii][ii]);
            }

            /*Query with POS ID using General search and sort the result with creation date in descending order*/
            CHECK_ITK(iRetCode, QRY_find2( "General...", &query_tag ));
            CHECK_ITK(iRetCode, QRY_execute_with_sort( query_tag, n_entries, entries, values, 1, keys, n_order, &n_found, &results ));

            if (n_found > 0)
            {
                AOM_ask_value_string( results[0], "vf3_Ec_number", &change_number );
                TC_write_syslog("\nEC NUMBER : %s",change_number);
                *value = change_number;
            }
    
            SAFE_MEM_free(results);
            SAFE_MEM_free(entries);
            SAFE_MEM_free(values); 
        }

        /*for ECN Number*/
        else
        {
            int		iRelSts			= 0;
            int		iPri			= 0;
            tag_t	*ptECN			= NULL;
            tag_t	*ptSts			= NULL;
            tag_t	tRelation		= NULLTAG;

            /*to check if itemrevision is released*/
            iRetCode = WSOM_ask_release_status_list(tItemRevision,&iRelSts,&ptSts);
            if(iRelSts > 0)
            {
                iRetCode = GRM_find_relation_type("CMHasSolutionItem",&tRelation);
                /*get primary objects of itemrevision, i.e. ECN Revision*/
                iRetCode = GRM_list_primary_objects_only(tItemRevision,tRelation,&iPri,&ptECN);

                for(int iInd = 0; iInd < iPri; iInd++)
                {
                    int			iRelStsEcn		= 0;
                    tag_t		*ptStsEcn		= NULL;
                    tag_t		tItem			= NULLTAG;
                    char		*pcObjType		= NULL;
                    char		*pcID			= NULL;
                    char		*pcRevID		= NULL;
                    char		*pcItem			= NULL;
                    string		szItemRev		= "";
                
                    iRetCode = WSOM_ask_object_type2(ptECN[iInd],&pcObjType);
                    if(tc_strcmp(pcObjType,"Vf6_ECNRevision") == NULL)
                    {
                        /*to check if ECN is released, if so get its revision number*/
                        iRetCode = WSOM_ask_release_status_list(ptECN[iInd],&iRelStsEcn,&ptStsEcn);
                        if(iRelStsEcn > 0)
                        {
                            iRetCode = ITEM_ask_item_of_rev(ptECN[iInd],&tItem);
                            iRetCode = ITEM_ask_id2(tItem,&pcID);
                            iRetCode = ITEM_ask_rev_id2(ptECN[iInd],&pcRevID);

                            szItemRev.assign(pcID);
                            szItemRev.append("/");
                            szItemRev.append(pcRevID);

                            pcItem = (char*) MEM_alloc((int)( szItemRev.length() + 1) * sizeof(char));
                            tc_strcpy(pcItem,szItemRev.c_str());

                            TC_write_syslog("ECN NUMBER : %s\n",pcItem);

                            *value = pcItem;
                        }
                    }
                    SAFE_MEM_free(pcObjType);
                    SAFE_MEM_free(pcID);
                    SAFE_MEM_free(pcRevID);
                }
            }
        }
    }
    return iRetCode ;
}

/**********************************************************************************
/*Desc: To set ECR Number on BOM Line
/		ECR Number -> Get ECR Revision from which Item Revision is released
/				   -> Set ECR Number
/**********************************************************************************/
int set_ecr_number(METHOD_message_t* msg, va_list args)
{
    int		iRetCode			= ITK_ok;
    tag_t	tBomLine			= NULLTAG;
    tag_t   tChildItemRevision	= NULLTAG;
    tag_t	tItemRevision		= NULLTAG;
    tag_t	Item				= NULLTAG;
    tag_t	tPropTag			= va_arg(args, tag_t);
    char	**value				= va_arg(args, char**);
    char	*change_number		= NULL;
    char	*positionIndex		= NULL;

	tBomLine = msg->object_tag;
	//CHECK_ITK(iRetCode, PROP_ask_owning_object(tPropTag, &tBomLine));
    CHECK_ITK(iRetCode, AOM_ask_value_tag(tBomLine,"bl_line_object",&tItemRevision));
    if(iRetCode == ITK_ok && tItemRevision != NULLTAG)
    {
        int		iRelSts			= 0;
        int		iPri			= 0;
        tag_t	*ptECR			= NULL;
        tag_t	*ptSts			= NULL;
        tag_t	tRelation		= NULLTAG;
        string	szECR			= "";
        char    *pcECR			= NULL;

        /*to check if itemrevision is released*/
        iRetCode = WSOM_ask_release_status_list(tItemRevision,&iRelSts,&ptSts);
        if(iRelSts > 0)
        {
            iRetCode = GRM_find_relation_type("CMHasProblemItem",&tRelation);
            /*get primary objects of itemrevision, i.e. ECN Revision*/
            iRetCode = GRM_list_primary_objects_only(tItemRevision,tRelation,&iPri,&ptECR);

            for(int iInd = 0; iInd < iPri; iInd++)
            {
                int			iRelStsEcn		= 0;
                tag_t		*ptStsEcn		= NULL;
                tag_t		tItem			= NULLTAG;
                char		*pcObjType		= NULL;
                char		*pcID			= NULL;
                char		*pcRevID		= NULL;
                char		*pcItem			= NULL;
                string		szItemRev		= "";
                
                iRetCode = WSOM_ask_object_type2(ptECR[iInd],&pcObjType);
                if(tc_strcmp(pcObjType,"Vf6_ECRRevision") == NULL)
                {
                    /*to check if ECN is released, if so get its revision number*/
                    iRetCode = WSOM_ask_release_status_list(ptECR[iInd],&iRelStsEcn,&ptStsEcn);
                    
                    iRetCode = ITEM_ask_item_of_rev(ptECR[iInd],&tItem);
                    iRetCode = ITEM_ask_id2(tItem,&pcID);
                    iRetCode = ITEM_ask_rev_id2(ptECR[iInd],&pcRevID);

                    szItemRev.assign(pcID);
                    szItemRev.append("/");
                    szItemRev.append(pcRevID);

                    pcItem = (char*) MEM_alloc((int)( szItemRev.length() + 1) * sizeof(char));
                    tc_strcpy(pcItem,szItemRev.c_str());

                    TC_write_syslog("ECR NUMBER : %s\n",pcItem);

                    /*copying all vehicle type values to string*/
                    if(iInd+1 < iPri)
                    {
                        szECR.append(pcItem);
                        szECR.append(",");
                    }
                    else
                    {
                        szECR.append(pcItem);
                    }
                    
                }
                SAFE_MEM_free(pcObjType);
                SAFE_MEM_free(pcID);
                SAFE_MEM_free(pcRevID);
            }

            if(szECR.length() > 0)
            {
                pcECR = (char*) MEM_alloc(( (int)tc_strlen(szECR.c_str()) + 1) * sizeof(char));
                tc_strcpy( pcECR, szECR.c_str());

                *value = pcECR;
            }
        }
    }
    
    return iRetCode ;
}

/**********************************************************************************
/*Desc: To set ECO Release date or ECN Release date on BOM Line
/       ECO Number -> Get Pos ID value from BOM Line
/				   -> Query with Pos ID to get change form
/                  -> Read vf3_string_property1 from Change form and set on BOM Line
/		ECN Number -> Get ECN Revision from which Item Revision is released
/				   -> Set ECN Release date*/
/*********************************************************************************/
int set_eco_release_date(METHOD_message_t* msg, va_list args)
{
    int		iRetCode			= ITK_ok;
    tag_t	tBomLine			= NULLTAG;
    tag_t	tChildItemRevision	= NULLTAG;
    tag_t	tItemRevision		= NULLTAG;
    tag_t	Item				= NULLTAG;
    tag_t	tPropTag			= va_arg(args, tag_t);
    char	**value				= va_arg(args, char**);
    int     n_entries           = 1;
    char	*change_number		= NULL ;
    char	*positionIndex		= NULL ;

	tBomLine = msg->object_tag;
	//CHECK_ITK(iRetCode, PROP_ask_owning_object(tPropTag, &tBomLine));
    CHECK_ITK(iRetCode, AOM_ask_value_tag(tBomLine,"bl_line_object",&tItemRevision));
    if(iRetCode == ITK_ok && tItemRevision != NULLTAG)
    {
        /*Get POS ID from BOM Line*/
        CHECK_ITK(iRetCode, AOM_ask_value_string(tBomLine,"VF3_pos_ID",&positionIndex));
        /*for ECO Number*/
        if (positionIndex != NULL && tc_strcmp(positionIndex , "") != 0)
        {
            TC_write_syslog("POSITION INDEX : %s\n", positionIndex);

            int		n_found						= 0;
            int		n_order[]					= {2};
            char    critrea_entry [1][18]       = {"Name"};
            char    *critrea_value[1][18]       = {positionIndex};
            char	*keys[]						= {"creation_date"};
            char    **entries					= NULL; 
            char    **values					= NULL;
            tag_t	query_tag					= NULLTAG;
            tag_t	*results					= NULL;
        
            entries = (char **) MEM_alloc(n_entries * sizeof(char *));
            values  = (char **) MEM_alloc(n_entries * sizeof(char *));

            for ( int ii = 0; ii < n_entries; ii++ )
            {
                entries[ii] = (char *)MEM_alloc(strlen( critrea_entry[ii] ) + 1);
                tc_strcpy(entries[ii],  critrea_entry[ii] );
                TC_write_syslog("entries=%s\n", entries[ii]);

                values[ii] = (char *)MEM_alloc( tc_strlen( critrea_value[ii][ii] ) + 1);
                tc_strcpy(values[ii],  critrea_value[ii][ii]);
                TC_write_syslog("values=%s\n", values[ii]);
            }

            /*Query with POS ID using General search and sort the result with creation date in descending order*/
            CHECK_ITK(iRetCode, QRY_find2( "General...", &query_tag ));
            CHECK_ITK(iRetCode, QRY_execute_with_sort( query_tag, n_entries, entries, values, 1, keys, n_order, &n_found, &results ));

            if (n_found > 0)
            {
                CHECK_ITK(iRetCode, AOM_ask_value_string( results[0], "vf3_string_property1", &change_number ));
                *value = change_number;
            }
    
            SAFE_MEM_free(results);
            SAFE_MEM_free(entries);
            SAFE_MEM_free(values);
        }

        /*for ECN Number*/
        else
        {
            int		iRelSts			= 0;
            int		iPri			= 0;
            tag_t	*ptECN			= NULL;
            tag_t	*ptSts			= NULL;
            tag_t	tRelation		= NULLTAG;

            /*to check if itemrevision is released*/
            CHECK_ITK(iRetCode, WSOM_ask_release_status_list(tItemRevision,&iRelSts,&ptSts));
            if(iRelSts > 0)
            {
                CHECK_ITK(iRetCode, GRM_find_relation_type("CMHasSolutionItem",&tRelation));
                if(iRetCode == ITK_ok && tRelation != NULLTAG)
                {
                    /*get primary objects of itemrevision, i.e. ECN Revision*/
                    CHECK_ITK(iRetCode, GRM_list_primary_objects_only(tItemRevision,tRelation,&iPri,&ptECN));

                    for(int iInd = 0; iInd < iPri; iInd++)
                    {
                        int			iRelStsEcn		= 0;
                        tag_t		*ptStsEcn		= NULL;
                        tag_t		tItem			= NULLTAG;
                        char		*pcObjType		= NULL;
                        char		*pcRelDate		= NULL;
                        date_t		dReleased;
                
                        CHECK_ITK(iRetCode, WSOM_ask_object_type2(ptECN[iInd],&pcObjType));
                        if(tc_strcmp(pcObjType,"Vf6_ECNRevision") == NULL)
                        {
                            /*to check if ECN is released, if so get its released date*/
                            CHECK_ITK(iRetCode, WSOM_ask_release_status_list(ptECN[iInd],&iRelStsEcn,&ptStsEcn));
                            if(iRelStsEcn > 0)
                            {
                                CHECK_ITK(iRetCode, AOM_ask_value_date(ptECN[iInd],"date_released",&dReleased));
                                CHECK_ITK(iRetCode, DATE_date_to_string(dReleased, "%d-%b-%Y", &pcRelDate));

                                TC_write_syslog("ECN Release Date : %s\n",pcRelDate);

                                *value = pcRelDate;
                            }
                        }
                        SAFE_MEM_free(pcObjType);
                    }
                }
            }
        }
    }

    return iRetCode ;
}

/**********************************************************************************
/*Desc: To set 2D Drwaing value on BOM Line
/*********************************************************************************/
int set_2d_drawing_value(METHOD_message_t* msg, va_list args)
{
	int ret_code = ITK_ok;
	tag_t t_bomline = NULLTAG;
	tag_t t_item_rev = NULLTAG;
	tag_t t_relation = NULLTAG;
	const tag_t t_prop_tag = va_arg(args, tag_t);
	char** value = va_arg(args, char**);

	t_bomline = msg->object_tag;
	//CHECK_ITK(ret_code, PROP_ask_owning_object(t_prop_tag, &t_bomline))
	CHECK_ITK(ret_code, AOM_ask_value_tag(t_bomline,"bl_line_object", &t_item_rev))
	if (ret_code == ITK_ok && t_item_rev != NULLTAG)
	{
		char* object_type = nullptr;
		CHECK_ITK(ret_code, WSOM_ask_object_type2(t_item_rev, &object_type))

		if (tc_strcmp(object_type, "VF3_Scooter_partRevision") == 0 || tc_strcmp(object_type, "VF3_me_scooterRevision") == 0)
		{
			/*Get IMAN_specification relation*/
			logical has_PDF = false;
			CHECK_ITK(ret_code, GRM_find_relation_type("IMAN_specification", &t_relation))
			/*check for PDF*/
			if (t_relation != NULLTAG)
			{
				int counter = 0;
				tag_t* secondary_obj = nullptr;

				CHECK_ITK(ret_code, GRM_list_secondary_objects_only(t_item_rev, t_relation, &counter, &secondary_obj))

				for (int i = 0; i < counter; i++)
				{
					char* secondary_obj_type = nullptr;

					CHECK_ITK(ret_code, WSOM_ask_object_type2(secondary_obj[i], &secondary_obj_type))
					if (tc_strcmp("PDF", secondary_obj_type) == 0)
					{
						has_PDF = true;
					}
					SAFE_MEM_free(secondary_obj_type)
				}
				SAFE_MEM_free(secondary_obj)
			}
			/*Get IMAN_Rendering relation*/
			CHECK_ITK(ret_code, GRM_find_relation_type("IMAN_Rendering", &t_relation))
			/*check for PDF*/
			if (t_relation != NULLTAG)
			{
				int counter = 0;
				tag_t* secondary_obj = nullptr;

				CHECK_ITK(ret_code, GRM_list_secondary_objects_only(t_item_rev, t_relation, &counter, &secondary_obj))
				for (auto i = 0; i < counter; i++)
				{
					char* secondary_obj_type = nullptr;
					CHECK_ITK(ret_code, WSOM_ask_object_type2(secondary_obj[i], &secondary_obj_type))
					if (tc_strcmp("PDF", secondary_obj_type) == 0)
					{
						has_PDF = true;
					}
					SAFE_MEM_free(secondary_obj_type)
				}
				SAFE_MEM_free(secondary_obj)
			}

			if (has_PDF == true)
			{
				*value = (char*)MEM_alloc(4);
				tc_strcpy(*value, "YES");
			}
			else
			{
				*value = (char*)MEM_alloc(3);
				tc_strcpy(*value, "NO");
			}
		}
		SAFE_MEM_free(object_type)
	}
	return ret_code;
}


int ECO_get_change_date_value(METHOD_message_t* msg, va_list args)
{
    int iRetCode =ITK_ok;
    int count=0;
    //Get values from va_list
    tag_t *primary_objects=NULLTAG;
    tag_t tPropTag = va_arg(args, tag_t);
    char** value  = va_arg( args, char**);
    tag_t rel=NULLTAG;
    *value = NULL;
    
    tag_t tENG_item_Revision = NULLTAG;
    tag_t primary_objects_type=NULLTAG;
    tag_t tItemRevision = NULLTAG;
    char *primary_objects_type_name = NULL ;
    char *object_type=NULL;
    tag_t ITEM=NULLTAG;
    char *itemid = NULL;
    itemid = (char*)malloc(128);
    char *date_str = NULL ;
    date_t released_date;

    printf ("\nFETCHING DATE VALUES\n") ;
	tENG_item_Revision = msg->object_tag;
     //CHECK_ITK(iRetCode, PROP_ask_owning_object(tPropTag, &tENG_item_Revision));
     CHECK_ITK(iRetCode, WSOM_ask_object_type2(tENG_item_Revision,&object_type));
     CHECK_ITK(iRetCode,GRM_find_relation_type("VF3_solution_items_rel",&rel));
     CHECK_ITK(iRetCode,GRM_list_primary_objects_only	(tENG_item_Revision,rel,&count,&primary_objects	));	

     if (count>0)
     {
             for(int t=0;t<count;t++)
                {
                    CHECK_ITK(iRetCode,TCTYPE_ask_object_type(primary_objects[t],&primary_objects_type));
                    CHECK_ITK(iRetCode,TCTYPE_ask_name2(primary_objects_type, &primary_objects_type_name));
                    if(primary_objects_type_name && tc_strcmp(primary_objects_type_name,"VF3_Change_ItemRevision")== 0)
                    { 
                            printf("\n Primary object Name  is %s",primary_objects_type_name);
                            CHECK_ITK(iRetCode,ITEM_ask_item_of_rev(primary_objects[t],&ITEM)); 
                            CHECK_ITK(iRetCode,AOM_ask_value_date(primary_objects[t] , "date_released" , &released_date)); 
                            CHECK_ITK(iRetCode,DATE_date_to_string(released_date,"%d-%b-%Y-%H_%M",&date_str));
                            printf("\n DATE STRING : \n" , date_str) ;
                    }

					SAFE_SM_FREE(primary_objects_type_name);
                }
        
              *value = (char*)MEM_alloc ( 50 );
              tc_strcpy ( *value, date_str);
              MEM_free(date_str);
              printf("\n Code Executed Successfully\n" ) ;
         
     }
    return iRetCode;
}


int ECO_get_change_no_value(METHOD_message_t* msg, va_list args)
{
    int iRetCode =ITK_ok;
    int count=0;
    //Get values from va_list
    tag_t *primary_objects=NULLTAG;
    tag_t tPropTag = va_arg(args, tag_t);
    char** value  = va_arg( args, char**);
    tag_t rel=NULLTAG;
    *value = NULL;
    
    tag_t tENG_item_Revision = NULLTAG;
    tag_t primary_objects_type=NULLTAG;
    tag_t tItemRevision = NULLTAG;
    char *primary_objects_type_name = NULL ;
    char *object_type=NULL;
    tag_t ITEM=NULLTAG;
    char *itemid = NULL;
    itemid = (char*)malloc(128);

	tENG_item_Revision = msg->object_tag;
     //CHECK_ITK(iRetCode, PROP_ask_owning_object(tPropTag, &tENG_item_Revision));
     CHECK_ITK(iRetCode, WSOM_ask_object_type2(tENG_item_Revision,&object_type));
     CHECK_ITK(iRetCode,GRM_find_relation_type("VF3_solution_items_rel",&rel));
     CHECK_ITK(iRetCode,GRM_list_primary_objects_only	(tENG_item_Revision,rel,&count,&primary_objects	));	

     if (count>0)
     {
		for(int t=0;t<count;t++)
		{
			CHECK_ITK(iRetCode,TCTYPE_ask_object_type(primary_objects[t],&primary_objects_type));
			CHECK_ITK(iRetCode,TCTYPE_ask_name2(primary_objects_type, &primary_objects_type_name));
			if(primary_objects_type_name && tc_strcmp(primary_objects_type_name,"VF3_Change_ItemRevision")== 0)
			{ 
					printf("\n Primary object Name  is %s",primary_objects_type_name);
					ITEM_ask_item_of_rev(primary_objects[t],&ITEM); 
					CHECK_ITK(iRetCode,ITEM_ask_id2(ITEM,&itemid));
					printf("\n ITEM_ID : %s \n" , itemid) ;
			}

			SAFE_SM_FREE(primary_objects_type_name);
		}
        
              *value = (char*)MEM_alloc ( strlen ( itemid ) + 1 );
              tc_strcpy ( *value, itemid);
              MEM_free(itemid);
              printf("\n Code Executed Successfully\n" ) ;
         
     }
    return iRetCode;
}

extern int delta_init_user_bomline_properties(METHOD_message_t* msg, va_list args)
{
    int			iRetCode		= ITK_ok;
    char		*TypeName		= NULL;
    tag_t		tTypeTag		= NULLTAG;
    tag_t		tNewPropTagCost = NULLTAG;
    METHOD_id_t method;

    tTypeTag = va_arg( args,tag_t);

    CHECK_ITK(iRetCode, TCTYPE_ask_name2(tTypeTag,&TypeName));

    CHECK_ITK(iRetCode, METHOD_register_prop_method((const char*)TypeName,"vf3_make_buy",PROP_ask_value_string_msg,set_make_buy,0,&method));
    //CHECK_ITK(iRetCode, METHOD_register_prop_method((const char*)TypeName,"vf3_eco_number",PROP_ask_value_string_msg,set_eco_number,0,&method));
    //CHECK_ITK(iRetCode, METHOD_register_prop_method((const char*)TypeName,"vf3_eco_release_date",PROP_ask_value_string_msg,set_eco_release_date,0,&method));
    CHECK_ITK(iRetCode, METHOD_register_prop_method((const char*)TypeName,"vf3_2drawing_r",PROP_ask_value_string_msg,set_2d_drawing_value,0,&method));
    //CHECK_ITK(iRetCode, METHOD_register_prop_method((const char*)TypeName,"vl5_scp_vehicle_type_bl",PROP_ask_value_string_msg,set_scp_vehicle_type,0,&method));
    //CHECK_ITK(iRetCode, METHOD_register_prop_method((const char*)TypeName,"vf3_ecr_number",PROP_ask_value_string_msg,set_ecr_number,0,&method));
    //CHECK_ITK(iRetCode, METHOD_register_prop_method((const char*)TypeName,"vf5_eco_number",PROP_ask_value_string_msg,set_ecr_number,0,&method));
    
    return iRetCode;
}

extern int ECO_init_runtime_property(METHOD_message_t* msg, va_list args)
{
    int iRetCode=ITK_ok;
    char *TypeName=NULL;
    tag_t tTypeTag =NULLTAG;
    tag_t tNewPropTagCost =NULLTAG;
    tTypeTag = va_arg( args,tag_t);
    METHOD_id_t method;

    CHECK_ITK(iRetCode, TCTYPE_ask_name2(tTypeTag,&TypeName));
    printf("\n Type name is %s ",TypeName);
    
    CHECK_ITK(iRetCode, METHOD_register_prop_method((const char*)TypeName,"vf3_eco_number_r",PROP_ask_value_string_msg,ECO_get_change_no_value,0,&method));
    CHECK_ITK(iRetCode, METHOD_register_prop_method((const char*)TypeName,"vf3_eco_release_date_r",PROP_ask_value_string_msg,ECO_get_change_date_value,0,&method));
    
    return iRetCode;
}

extern int bom_line_properties_register_methods(int *decision, va_list args)
{
    int iRetCode = ITK_ok;
    int i;

    METHOD_id_t methodld; 
    
    USER_prop_init_entry_t user_types_methods[] =
             {
                { "BOMLine" , delta_init_user_bomline_properties , NULL },
                { "VF3_car_partRevision" , ECO_init_runtime_property , NULL },
                { "" , 0 , NULL }
             };
             
    
    int n_types = sizeof(user_types_methods)/sizeof(USER_prop_init_entry_t);
    
    for ( i = 0; i < n_types; i++ )
    {
        iRetCode = METHOD_register_method ( user_types_methods[i].type_name,TCTYPE_init_user_props_msg,user_types_methods[i].user_method,user_types_methods[i].user_args,&methodld );
    }

    if(methodld.id == NULLTAG)
    {
        printf("\n BOM Properties registered successfully \n");
    }
    
    return (iRetCode);
}

extern "C" DLLAPI int IC_VinFast_Custom_register_callbacks()
{
    int		iRetCode   = ITK_ok;
    int		decision   = ONLY_CURRENT_CUSTOMIZATION ;

    printf("\n ENTERING BOM LINE DEVELOPMENT .. \n");

    iRetCode = CUSTOM_register_exit ( "IC_VinFast_Custom", "USER_init_module", (CUSTOM_EXIT_ftn_t) bom_line_properties_register_methods );

    printf("\n EXITING BOM LINE DEVELOPMENT .. \n");

    return iRetCode;
}