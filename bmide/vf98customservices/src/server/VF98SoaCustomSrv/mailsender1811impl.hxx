/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_CUSTOMSRV_2018_11_MAILSENDER_IMPL_HXX 
#define TEAMCENTER_SERVICES_CUSTOMSRV_2018_11_MAILSENDER_IMPL_HXX

#define PREF_MAIL_SERVER_NAME "Mail_server_name"
#define PREF_MAIL_SERVER_PORT "Mail_server_port"
#define PREF_SCRAP_EMAIL_TEMPLATE_PATH "Mail_template_location"
#define PREF_MAIL_UTILITY_PATH "Mail_utility_location"
#define EXTENSION ".html"
#define SLASH "\\"

#define TC_MAIL_SMTP "\\tc_mail_smtp"
#define SPACE " "
#define SUBJECT "-subject="
#define TO "-to="
#define SERVER "-server="
#define PORT "-port="
#define BODYARG "-body="

#include <vector>
#include <algorithm>
#include <unordered_set>
#include <tccore/item.h>
#include <tccore/aom_prop.h>
#include <tc/tc_startup.h>
#include <tc/envelope.h>
#include <tccore/aom.h>
#include <sa/sa.h>
#include <pom/pom/pom.h>
#include <tc/envelope.h>
#include <tc/preferences.h>
#include <stdio.h>
#include <string.h>
#include <tc/tc_util.h>
#include <base_utils/OSEnvironment.hxx>
#include <base_utils/Mem.h>
#include <fclasses/OSFile.hxx>
#include <fclasses/OSDirectory.hxx>
#include <stdlib.h>
#include <sa/user.h>
#include <iostream>
#include <fstream>
#include <cstdlib>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <cstdlib>
#include <cstring>
#include <sstream>
#include <fclasses/tc_string.h>
#include <stdio.h>
#include <mailsender1705impl.hxx>
#include <iostream>
#include <fstream>
#include <string.h>
#include <stdlib.h>
#include <sstream>
#include <windows.h>
#include <winsock2.h>
#include <tc/preferences.h>
#include <string>
#include <iostream>
#include <windows.h>

#include <mailsender1811.hxx>

#include <CustomSrv_exports.h>

namespace VF98
{
    namespace Soa
    {
        namespace CustomSrv
        {
            namespace _2018_11
            {
                class MailSenderImpl;
            }
        }
    }
}


class SOACUSTOMSRV_API VF98::Soa::CustomSrv::_2018_11::MailSenderImpl : public VF98::Soa::CustomSrv::_2018_11::MailSender

{
public:

    virtual bool sendEmail ( const std::string& to, const std::string& subject, const std::string& content );


};

#include <CustomSrv_undef.h>
#endif
