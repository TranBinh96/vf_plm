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

#include <organizationutils2012impl.hxx>
#include "vf_custom.hxx"
#include <sa\user.h>
#include <sa\am.h>
#include <tc\emh.h>
#include <tccore\aom.h>
#include <tcinit\tcinit.h>
#include <fclasses\tc_string.h>
#include <base_utils/ScopedSmPtr.hxx>
#include <base_utils/ScopedPtr.hxx>


using namespace VF4::Soa::AdminUtils::_2020_12;
using namespace Teamcenter::Soa::Server;

OrganizationUtilsImpl::ChangeUserPasswordResponse OrganizationUtilsImpl::checkAndChangeUserPassword ( const std::vector< ChangeUserPasswordInput >& inputs )
{
	int ifail = ITK_ok;
	OrganizationUtilsImpl::ChangeUserPasswordResponse response;

	for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++)
	{
		ChangeUserPasswordInput input = inputs[inputIndex];
		ChangeUserPasswordOutput output;
		output.isSuccess = false;

		char *user_id = (char*)input.userId.c_str();
		char *oldPassword = (char*)input.userPassword.c_str();
		char *newPassword = (char*)input.newPassword.c_str();
		tag_t userTag = NULLTAG;

		ERROR_CHECK(SA_find_user2(user_id,&userTag));
		if(userTag == NULLTAG)
		{
			output.errorMessage = "Input user does not present in DB.";
		}
		else
		{
			if(tc_strlen(oldPassword) > 0)
			{
				//validating password
				logical result = false;
				ERROR_CHECK(POM_check_password(userTag,oldPassword,&result));
				if(!result)
				{
					output.errorMessage = "Old password for given user does not match with DB value.";
				}
			}
			else
			{
				if(newPassword == NULL || tc_strlen(newPassword) == 0)
				{
					output.errorMessage = "newPassword cannot be empty.";
				}
				else
				{
					//resetting the password
					ERROR_CHECK(AOM_refresh(userTag,true));
					ERROR_CHECK(POM_set_password(userTag,NULL,newPassword));
					if(ifail != ITK_ok)
					{
						output.errorMessage = "Failed to set password. Please find details in syslog.";
					}
					else
					{
						output.isSuccess = true;
					}

					ERROR_CHECK(AOM_save_without_extensions(userTag));
					ERROR_CHECK(AOM_refresh(userTag,false));
				}
			}
		}

		response.outputs.push_back(output);
	}

	return response;
}



