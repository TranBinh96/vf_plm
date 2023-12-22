#include "VF_custom.h"

using namespace std;

void vf_split(std::string str, const std::string &delimiter, std::vector<std::string> &result)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	result.clear();
	size_t pos = 0;
	std::string token;

	while ((pos = str.find(delimiter)) != std::string::npos)
	{
		token = str.substr(0, pos);
		result.push_back(token);
		str.erase(0, pos + delimiter.length());
	}
	result.push_back(str);

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
}

double getRate(string sourceCurrency, string destCurrency)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);

	double rate = 1;
	int count = 0;
	char **vals = NULL;
	// USD_VND=23000
	(PREF_ask_char_values("VF_EXCHANGE_RATE", &count, &vals));
	destCurrency.append("_").append(sourceCurrency);
	for (int i = 0; i < count; i++)
	{
		if (tc_strstr(vals[i], destCurrency.c_str()))
		{
			vector<string> words;
			vf_split(vals[i], "=", words);
			if (words.size() == 2)
			{
				string rateStr = words[1];
				rate = std::stof(rateStr);
			}
		}
	}

	if (vals) MEM_free(vals);

	TC_write_syslog("\n[VF] Rate of %s=%8.8f", destCurrency.c_str(), rate);

	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return rate;
}

bool checkIsAssembly(tag_t part)
{
	TC_write_syslog("\n[VF] ENTER %s", __FUNCTION__);
	tag_t latestRev = NULLTAG;
	int retcode = ITK_ok;
	CHECK_ITK(retcode, ITEM_ask_latest_rev(part, &latestRev));
	int foundsNum = 0;
	tag_t* founds = NULL;
	tag_t* bvrs = NULLTAG;
	int bvrsNum = 0;
	if (latestRev != NULLTAG)
	{
		CHECK_ITK(retcode, AOM_ask_value_tags(latestRev, "structure_revisions", &bvrsNum, &bvrs));
		if (bvrsNum > 0)
		{
			CHECK_ITK(retcode, AOM_ask_value_tags(bvrs[0], "bvr_occurrences", &foundsNum, &founds));
		}
	}
	else
	{
		TC_write_syslog("\n[VF] WARNING: Cannot found latest revision!");
	}

	SAFE_SM_FREE(founds);
	SAFE_SM_FREE(bvrs);
	
	TC_write_syslog("\n[VF] LEAVE %s", __FUNCTION__);
	return (foundsNum > 0);
}