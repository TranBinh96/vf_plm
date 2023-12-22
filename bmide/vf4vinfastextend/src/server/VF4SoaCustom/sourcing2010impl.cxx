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

#include <sourcing2010impl.hxx>
#include <tc/tc_errors.h>
#include <ctime>
#include <qry/qry.h>
#include <fclasses/tc_string.h>
#include <fclasses/tc_date.h>
#include <tccore/aom_prop.h>
#include <tccore/aom.h>
#include <tc/tc_macros.h>
#include "vf_custom.hxx"
using namespace VF4::Soa::Custom::_2020_10;
using namespace Teamcenter::Soa::Server;
using namespace std;
#define EXIT_FAILURE 1

const std::tm* addDayInterval(const std::tm *date, int addDay)
{
	struct tm time;
	time.tm_mday = date->tm_mday + addDay;
	time.tm_mon = date->tm_mon;
	time.tm_year = date->tm_year;
	time.tm_hour = date->tm_hour;
	time.tm_min = date->tm_min;
	time.tm_sec = date->tm_sec;
	std::time_t time_temp = mktime(&time);
	const std::tm *addDate = localtime(&time_temp);
	return addDate;


}
const std::tm* convert_date_t_to_tm(date_t date)
{
		struct tm time_struct;
		int day = 0;
		int month = 0;
		short int year = 0;
		int hour = 0;
		int minute = 0;
		int second = 0;
		day = static_cast<int>(static_cast<unsigned char>(date.day));
		month = static_cast<int>(static_cast<unsigned char>(date.month));
		year = static_cast<short int>(date.year);
		hour = static_cast<int>(static_cast<unsigned char>(date.hour));
		minute = static_cast<int>(static_cast<unsigned char>(date.minute));
		second = static_cast<int>(static_cast<unsigned char>(date.second));
		TC_write_syslog("\n Year: %i", year);
		TC_write_syslog("\n month: %i", month);
		TC_write_syslog("\n day: %i", day);
		TC_write_syslog("\n hour: %i", hour);
		TC_write_syslog("\n min: %i", minute);
		TC_write_syslog("\n sec: %i", second);

		time_struct.tm_year = year - 1900;
		time_struct.tm_mday = day;
		time_struct.tm_mon = month;
		time_struct.tm_hour = hour;
		time_struct.tm_min = minute;
		time_struct.tm_sec = second;
		TC_write_syslog("\n 1");
		std::time_t time_temp = mktime(&time_struct);
		TC_write_syslog("\n time: %s", asctime(localtime(&time_temp)));
		TC_write_syslog("\n 2");
		const std::tm *time_out = localtime(&time_temp);
		return time_out;


}
date_t convert_tm_to_date_t(const std::tm *timeout)
{
	date_t date;
	char buffer[80];
	logical date_valid;
	TC_write_syslog("\n 3");
	strftime(buffer, sizeof(buffer), "%d-%b-%Y %H:%M:%S", timeout);

	//TC_write_syslog("\n 4");
	string test_time = std::string(buffer);
	TC_write_syslog("\n test time : %s", buffer);
	TC_write_syslog("\n 5");
	ERROR_CHECK(DATE_string_to_date_t(buffer, &date_valid, &date));
	return date;

}
date_t plusDay(date_t source_date, int addNo)
{

	date_t dest_date;
	TC_write_syslog("\n addNo first: %i", addNo);
	TC_write_syslog("\n convert_date_t_to_tm(source_date, source_timeout)");
	const std::tm *source_timeout = convert_date_t_to_tm(source_date);
	//Count day with saturday, sunday
	if(addNo/5 == 1)
	{
		addNo = addNo + 2;
	}
	else if(addNo/5 == 2)
	{
		addNo = addNo + 4;
	}
	else if(addNo/5 == 3)
	{
		addNo = addNo + 6;
	}
	if(addNo != 1)
	{
	//if key day is thurdays, friday plus 2 day in addNo
		if(source_timeout->tm_wday == 4 || source_timeout->tm_wday == 5)
		{
			addNo = addNo + 2;
		}

		//if key day is saturday plus 2 day in addNo
		if(source_timeout->tm_wday == 6)
		{
			addNo = addNo + 1;
		}
	}
	TC_write_syslog("\n addNo last: %i", addNo);
	TC_write_syslog("\n convert_tm_to_date_t(source_timeout, dest_date)");
	const std::tm *dest_timeout = addDayInterval(source_timeout,addNo);
	//Result date is Saturday or Sunday

	if(dest_timeout->tm_wday == 6 || source_timeout->tm_wday == 0)
	{
		TC_write_syslog("\n Result date is Saturday or Sunday plus more 2");
		const std::tm *dest_timeout1 = addDayInterval(dest_timeout, 2);
		dest_date = convert_tm_to_date_t(dest_timeout1);
	}
	else
	{
		dest_date = convert_tm_to_date_t(dest_timeout);
	}

	return dest_date;
}
std::vector<tag_t> getSourcePart(std::string sourcepart)
{
	std::vector<tag_t> sourceparts;
	tag_t tQuery;
	int iNItem;
	tag_t *ptItem;
	const char *queryName = "Source Part";
	ERROR_CHECK(QRY_find2(queryName, &tQuery));
	if (tQuery == NULLTAG)
	{
		TC_write_syslog("ERROR: Cannot find query %s:\n", queryName);
		exit(EXIT_FAILURE);
	}
	char **queryValues = (char**) MEM_alloc(sizeof(char*)*1);
	queryValues[0] = (char*) MEM_alloc(sizeof(char) * (sourcepart.length() + 1));

	tc_strcpy(queryValues[0], sourcepart.c_str());

	char *queryEntries[]	= {"VF Part Number"};
	ERROR_CHECK(QRY_execute(tQuery,1,queryEntries,queryValues,&iNItem,&ptItem));
	for(int i=0;i < iNItem; i++)
	{
		sourceparts.push_back(ptItem[i]);
	}
	return sourceparts;
}
bool SourcingImpl::planDateCalculation ( const std::vector< std::string >& sourceparts )
{
	int itk_ok1 = ITK_ok;
	int itk_ok2 = ITK_ok;
	int itk_ok3 = ITK_ok;
	int itk_ok4 = ITK_ok;
	int itk_ok5 = ITK_ok;
	int itk_ok6 = ITK_ok;
	int itk_ok7 = ITK_ok;
	int itk_ok8 = ITK_ok;
	int itk_ok9 = ITK_ok;
	int itk_ok10 = ITK_ok;
	int itk_ok11 = ITK_ok;
	int itk_ok12 = ITK_ok;
	int itk_ok14 = ITK_ok;
	//Get Source part
	//const size_t size = sourceparts.size();
	TC_write_syslog("\n [VF] Enter SourcingImpl::planDateCalculation");
	for(int i=0; i< sourceparts.size(); i++)
	{
		std::vector<tag_t> sourceItems;
		sourceItems = getSourcePart(sourceparts[i]);
		for(int j = 0; j <sourceItems.size();j++)
		{
			//Start calculation
			char *changeindex;
			char *partnumber;
			//Check if source part is COP part if COP part break
			ERROR_CHECK(AOM_ask_value_string(sourceItems[j], "vf4_bom_change_index", &changeindex));
			ERROR_CHECK(AOM_ask_value_string(sourceItems[j], "vf4_bom_vfPartNumber", &partnumber));
			TC_write_syslog("\n Part %s plan date calculation", partnumber);
			if(tc_strcmp(changeindex, "COP")== 0)
			{
				TC_write_syslog("\n Part %s is COP part then break", partnumber);
				break;
			}


			//char *date;
			date_t sor_actual;
			ERROR_CHECK(AOM_ask_value_date(sourceItems[j], "vf4_cad_sor_rel_date", &sor_actual));
			//Plan date - NDA
			//vf4_clPlan_nda_signed
			date_t plandatenda;
			TC_write_syslog("\n plan date - NDA");
			plandatenda = plusDay(sor_actual, 1);
			ERROR_CHECK(AOM_refresh(sourceItems[j], true));
			itk_ok14 = AOM_set_value_date(sourceItems[j], "vf4_clPlan_nda_signed", plandatenda);
			//Plan Date - RFI
			//ERROR_CHECK(DATE_date_to_string(sor_actual, "%Y%m%d%H%M%S", &date));
			//TC_write_syslog("\n sor_date: %s", date);
			date_t plandate_rfi;
			std::vector<int> result;
			TC_write_syslog("\n plandate rfi");
			// plus 2 days
			plandate_rfi = plusDay(sor_actual, 1);
			//ERROR_CHECK(RES_checkout2(sourceItems[j],"Check-out to change plan date", NULL, NULL, RES_EXCLUSIVE_RESERVE));
			//ERROR_CHECK(AOM_load(sourceItems[j]));
			//set value to vf4_plan_rfi
			itk_ok1 = AOM_set_value_date(sourceItems[j], "vf4_plan_rfi", plandate_rfi);
			//Forecast Date - RFI
			date_t forecast_rfi;
			TC_write_syslog("\n forecast rfi");
			forecast_rfi = plusDay(sor_actual, 1);
			itk_ok2 = AOM_set_value_date(sourceItems[j], "vf4_forecast_rfi_date", forecast_rfi);
			//Plan Date - Quote Received
			date_t plandate_quotereceived;
			TC_write_syslog("\n plan date - quote received");
			plandate_quotereceived = plusDay(plandate_rfi, 18);
			itk_ok3 = AOM_set_value_date(sourceItems[j], "vf4_quote_received", plandate_quotereceived);
			//Forecast Date - Quote Received
			date_t forecastdate_quotereceived;
			date_t actual_rfi;
			TC_write_syslog("\n forecast quote received");
			ERROR_CHECK(AOM_ask_value_date(sourceItems[j], "vf4_actual_rfi", &actual_rfi));
			//TC_write_syslog("\n itk_ok : %i", itk_ok);
			//char *actualrfi
			//ERROR_CHECK(DATE_date_to_string(actual_rfi, "%Y%m%d", &actualrfi));
			//TC_write_syslog("\n actual_rfi: %s", actualrfi);

			if(DATE_IS_NULL(actual_rfi) == 1)
			{
				forecastdate_quotereceived = plusDay(forecast_rfi, 18);
			}
			else
			{
				forecastdate_quotereceived = plusDay(actual_rfi, 18);
			}
			itk_ok4 = AOM_set_value_date(sourceItems[j], "vf4_date1", forecastdate_quotereceived);
			//Plan Date - Sourcing Recommendation Approval
			date_t plandate_sourcingrecommendapproval;
			TC_write_syslog("\n plan date sourcing recommendation approval");
			plandate_sourcingrecommendapproval = plusDay(plandate_quotereceived, 11);
			itk_ok5 = AOM_set_value_date(sourceItems[j], "vf4_sourcing_recommendation", plandate_sourcingrecommendapproval);
			//Forecast Date - Sourcing Recommendation Approval
			date_t actual_quotereceived;
			ERROR_CHECK(AOM_ask_value_date(sourceItems[j], "vf4_actual_quote_received", &actual_quotereceived));
			//char *actualquotereceived;
			//ERROR_CHECK(DATE_date_to_string(actual_quotereceived, "%Y%m%d", &actualquotereceived));
			TC_write_syslog("\n forecast date sourcing recommendation approval");
			date_t forecastdate_sourcingrecommendapproval;
			if(DATE_IS_NULL(actual_quotereceived) == 1)
			{
				forecastdate_sourcingrecommendapproval = plusDay(forecastdate_quotereceived, 11);
			}
			else
			{
				forecastdate_sourcingrecommendapproval = plusDay(actual_quotereceived, 11);
			}
			itk_ok6 = AOM_set_value_date(sourceItems[j], "vf4_date2", forecastdate_sourcingrecommendapproval);
			//Plan Date - Supplier Award (LON)
			date_t plandate_supplierawardlon;
			TC_write_syslog("\n plan date supplier award LON");
			plandate_supplierawardlon = plusDay(plandate_sourcingrecommendapproval, 7);
			itk_ok7 = AOM_set_value_date(sourceItems[j], "vf4_plan_award_supplier", plandate_supplierawardlon);
			//Forecast Date - Supplier Award (LON)
			date_t actual_award_supplier;
			date_t forecastdate_supplierawardlon;
			TC_write_syslog("\n forecast date supplier award lon");
			ERROR_CHECK(AOM_ask_value_date(sourceItems[j], "vf4_actual_award_supplier", &actual_award_supplier));
			if(DATE_IS_NULL(actual_award_supplier) == 1)
			{
				forecastdate_supplierawardlon = plusDay(forecastdate_sourcingrecommendapproval, 7);
			}
			else
			{
				forecastdate_supplierawardlon = plusDay(actual_award_supplier, 7);
			}
			itk_ok8 = AOM_set_value_date(sourceItems[j], "vf4_date3", forecastdate_supplierawardlon);
			//Plan Date - ED&D Order
			date_t plandate_eddorder;
			TC_write_syslog("\n plan date eddorder");
			plandate_eddorder = plusDay(plandate_supplierawardlon, 14);
			itk_ok9 = AOM_set_value_date(sourceItems[j], "vf4_ednd_order_plan_d", plandate_eddorder);
			//Forecast Date - ED&D Order
			date_t actual_eddorder;
			date_t forecastdate_eddorder;
			TC_write_syslog("\n forecast  date eddorder");
			ERROR_CHECK(AOM_ask_value_date(sourceItems[j], "vf4_actual_eddorder", &actual_eddorder));
			if(DATE_IS_NULL(actual_eddorder) == 1)
			{
				forecastdate_eddorder = plusDay(forecastdate_supplierawardlon, 14);
			}
			else
			{
				forecastdate_eddorder = plusDay(actual_eddorder, 14);
			}
			itk_ok10 = AOM_set_value_date(sourceItems[j], "vf4_forcecast_date_ednd_ord", forecastdate_eddorder);
			//Plan Date - Tool Order Release
			date_t tool_order_release_plan;
			TC_write_syslog("\n tool order release plan");
			tool_order_release_plan = plusDay(plandate_supplierawardlon, 14);
			itk_ok11 = AOM_set_value_date(sourceItems[j], "vf4_tool_order_release_plan", tool_order_release_plan);
			//Forecast Date - Tool Order Release
			date_t forecastdate_toolorderrelease;
			TC_write_syslog("\n forecastdate_toolorderrelease");
			if(DATE_IS_NULL(actual_eddorder) == 1)
			{
				forecastdate_toolorderrelease = plusDay(forecastdate_supplierawardlon, 14);
			}
			else
			{
				forecastdate_toolorderrelease = plusDay(actual_eddorder, 14);
			}
			itk_ok12 = AOM_set_value_date(sourceItems[j], "vf4_forecastDateToolOrderRe", forecastdate_toolorderrelease);
			//ERROR_CHECK(RES_checkin(sourceItems[j]));
			ERROR_CHECK(AOM_save_without_extensions(sourceItems[j]));
			ERROR_CHECK(AOM_refresh(sourceItems[j], false));
		}
	}
	if(itk_ok1 != 0 || itk_ok2 != 0 || itk_ok3 != 0 || itk_ok4 != 0 || itk_ok5 != 0 || itk_ok6 != 0 || itk_ok7 != 0 || itk_ok8 != 0 || itk_ok9 != 0 || itk_ok10 != 0 || itk_ok10 != 0 || itk_ok11 != 0 || itk_ok11 != 0 || itk_ok12 != 0 || itk_ok14 != 0)
	{
		TC_write_syslog("\n [VF] Leave SourcingImpl::planDateCalculation");
		return false;
	}
	else
	{
		TC_write_syslog("\n [VF] Leave SourcingImpl::planDateCalculation");
		return true;
	}
    // TODO implement operation
}
