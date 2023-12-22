/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@
*/

#ifndef TEAMCENTER_SERVICES_CUSTOMSRV_2017_05_MAILSENDER_IMPL_HXX 
#define TEAMCENTER_SERVICES_CUSTOMSRV_2017_05_MAILSENDER_IMPL_HXX


#include <mailsender1705.hxx>

#include <CustomSrv_exports.h>

namespace VF98
{
    namespace Soa
    {
        namespace CustomSrv
        {
            namespace _2017_05
            {
                class MailSenderImpl;
            }
        }
    }
}


class SOACUSTOMSRV_API VF98::Soa::CustomSrv::_2017_05::MailSenderImpl : public VF98::Soa::CustomSrv::_2017_05::MailSender

{
public:

    virtual bool sendEmail ( const std::string* mailFrom, const std::string* mailTo, const std::string* mailCC, const std::string* mailSubject, const std::string* mailContent, const std::string* mailAttachment );


};

#include <CustomSrv_undef.h>
#endif
