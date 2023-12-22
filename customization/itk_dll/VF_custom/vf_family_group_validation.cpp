#include "VF_custom.h"

using namespace std;

bool check_quantity(double minQuantity, double maxQuantity);

DLLAPI int vf_fg_rule_save_pre_action(METHOD_message_t *messages, va_list vaList)
{
	TC_write_syslog("[vf]ENTER %s", __FUNCTION__);

	int ifail = ITK_ok;

	tag_t ruleForm = messages->object;

	double minQuantity = -1,
		maxQuantity = -1;
	AOM_ask_value_double(ruleForm, "vf4_min_quantity", &minQuantity);
	AOM_ask_value_double(ruleForm, "vf4_max_quantity", &maxQuantity);

	bool isValid = check_quantity(minQuantity, maxQuantity);

	if (!isValid)
	{
		ifail = VF_ERR_INVALID_FG_CODE;
		add_error_stack(ifail, "Min Quantity or Max Quantity is invalid! The valid value is 0 <= Min Quantity <= Max Quantity");
	}

	TC_write_syslog("[vf]LEAVE %s", __FUNCTION__);

	return ifail;
}

DLLAPI int vf_fg_save_pre_action(METHOD_message_t *messages, va_list vaList)
{
	TC_write_syslog("[vf]ENTER %s", __FUNCTION__);

	int ifail = ITK_ok;
	tag_t ruleForm = messages->object;

	double minQuantity = -1,
		maxQuantity = -1;
	AOM_ask_value_double(ruleForm, "vf4_def_min_quantity", &minQuantity);
	AOM_ask_value_double(ruleForm, "vf4_def_max_quantity", &maxQuantity);
	bool isValid = check_quantity(minQuantity, maxQuantity);

	if (!isValid)
	{
		ifail = VF_ERR_INVALID_FG_RULE_CODE;
		add_error_stack(ifail, "Default Min Quantity or Deafault Max Quantity is invalid! The valid value is 0 <= Min Quantity <= Max Quantity");
	}

	TC_write_syslog("[vf]LEAVE %s", __FUNCTION__);
	return ifail;
}

bool check_quantity(double minQuantity, double maxQuantity)
{
	return (minQuantity <= maxQuantity) && (minQuantity >= -0.0000000001) && (maxQuantity >= -0.0000000001);
}

int add_error_stack(int errCode, const char *errMsg)
{
	char errorMessage [1024];
	sprintf_s(errorMessage, errMsg);
	int ifail = EMH_store_error_s1(EMH_severity_error, errCode, errorMessage);
	return ifail;
}