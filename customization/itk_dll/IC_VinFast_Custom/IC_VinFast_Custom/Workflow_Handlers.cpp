
#include "Workflow_Handlers.h"

int IC_generic_attach_related_rendering_types(EPM_action_message_t msg)
{

	int result = 0 , argumentCount = 0 , attachmentCount = 0 , relatedObjectCount = 0 , statusCount = 0 , renderingObjectCount = 0 ;
	char *pcArgName	= NULL , *pcArgVal = NULL , *relationType = NULL , *statusName = NULL  ;
	tag_t RelationTypeTag = NULLTAG , rootTaskTag = NULLTAG , renderingRelationTypeTag = NULLTAG , attachmentTypeTag = NULLTAG , relation = NULLTAG , rel_type_tag = NULLTAG ;
	tag_t *attachmentTags = NULL , *relatedObjectsTags = NULL , *renderingObjectsTags = NULL , *statusTag  = NULL;
	bool HAS_RELEASED_DESIGN = false ;
	char  attachmentTypeName[TCTYPE_name_size_c+1];


	argumentCount = TC_number_of_arguments ( msg.arguments );
	for (int inx = 0 ; inx < argumentCount ; inx++)
	{
		CHECK_ITK(result,ITK_ask_argument_named_value( TC_next_argument(msg.arguments), &pcArgName, &pcArgVal ));

		if ((int) strcmp(pcArgName, "relationType") == 0)
		{
			relationType =  (char*) MEM_alloc(100*sizeof(char));
			tc_strcpy(relationType,pcArgVal);
		}
		
		MEM_free(pcArgName);
		MEM_free(pcArgVal);
	}

	if(relationType == NULL)
	{
		EMH_store_error_s1(EMH_severity_error, NO_VALID_ARGUMENT_RELATION_TYPE, "\n Argument -relationType is missing. Please contact system administrator\n");
		return 0 ;
	}

	CHECK_ITK(result,EPM_ask_root_task( msg.task,&rootTaskTag ) );
	CHECK_ITK(result,EPM_ask_attachments( rootTaskTag, EPM_target_attachment, &attachmentCount, &attachmentTags ) );

	printf("\n ATTACHMENT COUNT : %d \n", attachmentCount) ;
	CHECK_ITK(result,GRM_find_relation_type( relationType ,&RelationTypeTag));
	CHECK_ITK(result,GRM_find_relation_type( "IMAN_Rendering" ,&renderingRelationTypeTag));

	for(int inx = 0 ; inx < attachmentCount ; inx ++)	// For each ItemRevision
	{
		HAS_RELEASED_DESIGN = false ;
		CHECK_ITK(result,GRM_list_secondary_objects_only(attachmentTags[inx],RelationTypeTag,&relatedObjectCount,&relatedObjectsTags));
		printf("\n Number of relatedObjets are : %d \n",relatedObjectCount);
		if (relatedObjectCount > 0 )
		{
			CHECK_ITK(result,WSOM_ask_release_status_list(relatedObjectsTags[0], &statusCount, &statusTag));
			printf("\nNumber of released statuses : %d \n" , statusCount);
			for (int i = 0 ; i < statusCount ; i++)
			{
				CHECK_ITK(result,AOM_ask_value_string(statusTag[i],"object_name",&statusName) );

				printf("\nStatus Name is : %s \n", statusName);
				if(tc_strcmp(statusName , "DC2_Released") == 0)
				{
					HAS_RELEASED_DESIGN = true ;
					break ;
				}
			}
		}
		

		if(HAS_RELEASED_DESIGN)
		{
			CHECK_ITK(result,GRM_list_secondary_objects_only(relatedObjectsTags[0],renderingRelationTypeTag,&renderingObjectCount,&renderingObjectsTags));
			printf("\n Number of rendering Objets : %d \n", renderingObjectCount);
			if (renderingObjectCount > 0 )
			{
				for(int i = 0 ; i < renderingObjectCount ;  i++) // For each associated rendering datasets
				{
					CHECK_ITK( result,TCTYPE_ask_object_type( renderingObjectsTags[i], &attachmentTypeTag ) );
					CHECK_ITK( result, TCTYPE_ask_name(attachmentTypeTag,attachmentTypeName) );
					printf("\n Rendering objet type : %s \n", attachmentTypeName) ;
					if ( (int) strcmp(attachmentTypeName, "DirectModel") == 0 || (int) strcmp(attachmentTypeName, "PDF") == 0 )
					{					
						 CHECK_ITK( result, GRM_create_relation(attachmentTags[inx],renderingObjectsTags[i],renderingRelationTypeTag,NULLTAG,&relation));
						 CHECK_ITK( result, GRM_save_relation(relation));
					}
				}
			}
			MEM_free(renderingObjectsTags) ;
			MEM_free(relatedObjectsTags) ;
		}
	}

	MEM_free(attachmentTags);
	MEM_free(relationType);
	
	return result ;
}