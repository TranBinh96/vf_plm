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

#include <mailsender1811impl.hxx>
#include <stdlib.h>
#include <windows.h>

using namespace std;
using namespace VF98::Soa::CustomSrv::_2018_11;
using namespace Teamcenter::Soa::Server;

void replaceAll(std::string &str, const std::string &searchStr, const std::string &replacement)
{
	size_t strIndex = str.find(searchStr);
	while (strIndex != std::string::npos)
	{
		str.replace(strIndex, searchStr.length(),replacement);
		strIndex = str.find(searchStr);
	}
}

std::string getCurrentTimeStampStr()
{
	SYSTEMTIME _time;
	GetSystemTime(&_time);
	LONG time_ms = (_time.wSecond * 1000) + _time.wMilliseconds;

	std::string str;
	std::tm *tm;
	std::time_t ctm = 0;
	char buffer[80];
	time(&ctm);
	tm = localtime(&ctm);
	strftime(buffer, sizeof(buffer), "%d%m%Y%H%M%S", tm);
	str = std::string(buffer);
	return str + std::to_string(time_ms);
}

bool MailSenderImpl::sendEmail ( const std::string& to, const std::string& subject, const std::string& content )
{
	TC_write_syslog("[VF] Enter %s\n", __FUNCTION__);

	char *mailLocation = NULL;

	std::string time_ms = getCurrentTimeStampStr();

	PREF_ask_char_value_at_location("VF_PLM_MAIL_LOCATION", TC_preference_site,0,&mailLocation);
	std::string jsonFile(mailLocation);

	TC_write_syslog("mailLocation: %s\n", mailLocation);

	std::string mailToStr(to);
	if (mailToStr.find("@") != std::string::npos)
	{
		mailToStr.erase(mailToStr.find("@"));
	}

	int randomNum = rand();
	std::string randStr(to_string(randomNum));
	randomNum = rand();
	randStr.append(to_string(randomNum));
	randomNum = rand();
	randStr.append(to_string(randomNum));

	jsonFile.append("\\").append(mailToStr).append("_").append(randStr).append("_").append(time_ms);
	TC_write_syslog("jsonFile=%s\n", jsonFile.c_str());

	std::string jsonFileContent;
	std::string mailContent(content);
	replaceAll(mailContent, "\"", "#_@#");
	replaceAll(mailContent, "#_@#", "\\\"");

	jsonFileContent.append("{");
	jsonFileContent.append("to:\"").append(to).append("\",");
	jsonFileContent.append("subject:\"").append(subject).append("\",");
	jsonFileContent.append("message:\"").append(mailContent).append("\"");
	jsonFileContent.append("}");

	TC_write_syslog("jsonFileContent=%s\n", jsonFileContent.c_str());

	ofstream of(jsonFile);
	if (!of.fail())
	{
		of << jsonFileContent;
	}
	of.close();


	ifstream checkWrittenFile;
	checkWrittenFile.open(jsonFile.c_str());
	bool isDumpSuccess = true;
	if (!checkWrittenFile.fail())
	{
		checkWrittenFile.close();
		isDumpSuccess = true;
	}
	else
	{
		TC_write_syslog("@@@@@@ fail to write file\n");
		isDumpSuccess = false;
	}

	TC_write_syslog("[VF] End %s\n", __FUNCTION__);

	return isDumpSuccess;
}



