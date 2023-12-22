#include "VF_custom.h"
#include "map"
#include <base_utils/TcResultStatus.hxx>
#include <base_utils/ScopedSmPtr.hxx>
#include <base_utils/ScopedPtr.hxx>

void change_ownership_to_item(vector<tag_t> objectsTag, tag_t itemOwner, tag_t itemOwningGroup);
logical checkIsChildOf(tag_t item, char *rev);

DLLAPI int vf_change_ownership_bop_rev(METHOD_message_t* msg, va_list args)
{
	TC_write_syslog("\n[VF] Enter %s", __FUNCTION__);

	tag_t bopRev = msg->object_tag;
	tag_t					new_rev = va_arg(args, tag_t);
	char* peration = va_arg(args, char*);
	tag_t					parent_rev = va_arg(args, tag_t);
	int						copyCount = va_arg(args, int);
	ITEM_deepcopy_info_t* copyInfo = va_arg(args, ITEM_deepcopy_info_t*);
	int* count = va_arg(args, int*);
	tag_t** copied_objects = va_arg(args, tag_t**);

	if (copied_objects == NULL || count == NULL || *count <= 0 || *copied_objects == NULLTAG)
	{
		TC_write_syslog("\n[VF] WARNING: there are nothing to change ownership.");
		return ITK_ok;
	}


	int ret_code = ITK_ok;
	tag_t	item_rev_class = NULLTAG;
	tag_t	sourcing_rev_class = NULLTAG;
	logical is_descendant = false;
	tag_t   itemOwningGroup = NULLTAG;
	tag_t   itemOwner = NULLTAG;
	tag_t   item = NULLTAG;

	CHECK_ITK(ret_code, AOM_ask_value_tag(bopRev, "items_tag", &item));
	CHECK_ITK(ret_code, AOM_ask_value_tag(item, "owning_group", &itemOwningGroup));
	CHECK_ITK(ret_code, AOM_ask_value_tag(item, "owning_user", &itemOwner));

	vector<tag_t> objectTag;
	objectTag.push_back(bopRev);

	logical isMEOP = checkIsChildOf(item, "MEOP");
	if (isMEOP)
	{
		tag_t   rootAct = NULLTAG;
		tag_t* acts = NULL;
		int actsCount = 0;
		CHECK_ITK(ret_code, AOM_ask_value_tag(bopRev, "root_activity", &rootAct));
		CHECK_ITK(ret_code, AOM_ask_value_tags(rootAct, "contents", &actsCount, &acts));
		objectTag.push_back(rootAct);
		for (int actIndex = 0; actIndex < actsCount; actIndex++)
		{
			objectTag.push_back(acts[actIndex]);
		}
	}

	for (int countIndex = 0; countIndex < *count; countIndex++)
	{
		objectTag.push_back((*copied_objects)[countIndex]);
	}

	change_ownership_to_item(objectTag, itemOwner, itemOwningGroup);

	TC_write_syslog("\n[VF] Leave %s", __FUNCTION__);
	return ITK_ok;
}

void change_ownership_to_item(vector<tag_t> objectsTag, tag_t itemOwner, tag_t itemOwningGroup)
{
	TC_write_syslog("\n[VF] Enter %s", __FUNCTION__);
	int ret_code = ITK_ok;

	for (auto i = 0; i < objectsTag.size(); ++i)
	{
		CHECK_ITK(ret_code, AOM_refresh(objectsTag[i], true));
		CHECK_ITK(ret_code, AOM_set_ownership(objectsTag[i], itemOwner, itemOwningGroup));
		CHECK_ITK(ret_code, AOM_save_with_extensions(objectsTag[i]));
		CHECK_ITK(ret_code, AOM_refresh(objectsTag[i], false));
	}

	TC_write_syslog("\n[VF] Leave %s", __FUNCTION__);
}

logical checkIsChildOf(tag_t item, char* classToCheck)
{
	Teamcenter::scoped_smptr<char> itemClass;
	int ret_code = ITK_ok;
	tag_t classToCheckId = -1;
	tag_t itemClassId = -1;
	logical is_descendant = false;
	CHECK_ITK(ret_code, POM_class_of_instance(item, &itemClassId));
	CHECK_ITK(ret_code, POM_class_id_of_class(classToCheck, &classToCheckId));
	CHECK_ITK(ret_code, POM_is_descendant(classToCheckId, itemClassId, &is_descendant));

	return is_descendant;
}