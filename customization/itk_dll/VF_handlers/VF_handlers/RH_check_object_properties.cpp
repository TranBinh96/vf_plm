/***************************************************************************************
File         : RH_check_object_properties.cpp

Description  : To check if 01 revision contains in Solution folder.

Input        : None

Output       : None

Author       : Vinfast

Revision History :
Date            Revision    Who             Description
-----------------------------------------------------------------------------
March,11 2022     1.0       HoaVQ3          Initial Creation
Oct,21 2023       1.1       ThienBQ2        Add '-operator' argument

******************************************************************************************/
#include "Vinfast_Custom.h"
#include <vector>

/// <summary>
/// 
/// </summary>
/// <param name="s"></param>
/// <returns></returns>
char* stringToChar(std::string s) {
    char* ret = new char[s.length() + 1];
    strcpy(ret, s.c_str());
    return ret;
}

/// <summary>
/// 
/// </summary>
/// <param name="c"></param>
/// <returns></returns>
std::string charToString(char* c) {
    return std::string(c);
}

/// <summary>
/// 
/// </summary>
/// <param name="realVal"></param>
/// <param name="cp"></param>
/// <param name="oper"></param>
/// <returns></returns>
bool compareWithOperator(std::string realVal, std::string cp, std::string oper) {
    if (tc_strcmp(stringToChar(oper), "=") == 0) {
        return realVal == cp || (cp == "NNULL" && realVal != "");
    }
    else {
        return (realVal != cp && cp != "NNULL") || (cp == "NNULL" && realVal == "");
    }
}

/// <summary>
/// 
/// </summary>
/// <param name="msg"></param>
/// <returns></returns>
EPM_decision_t RH_check_object_properties(EPM_rule_message_t msg)
{
    int			iRetCode = ITK_ok;
    int			iNumArg = 0;
    tag_t		tRootTask = NULLTAG;
    char* pcFlag = NULL;
    char* pcValue = NULL;
    char* pcCompareValue = NULL;
    char* pcProperty = NULL;
    char* pcLogic = NULL;
    char* pcType = NULL;
    char* pcValueType = NULL;
    char* pcOperator = NULL;
    char		ErrorMessage[2048] = "";

    EPM_decision_t decision = EPM_go;

    /*getting number of arguments*/
    iNumArg = TC_number_of_arguments(msg.arguments);

    if (iNumArg > 0)
    {
        for (int iArgs = 0; iArgs < iNumArg; iArgs++)
        {
            iRetCode = ITK_ask_argument_named_value(TC_next_argument(msg.arguments), &pcFlag, &pcValue);
            /*for argument template name*/
            if (tc_strcmp(pcFlag, "type") == 0)
            {
                pcType = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
                tc_strcpy(pcType, pcValue);
            }
            else if (tc_strcmp(pcFlag, "property") == 0)
            {
                pcProperty = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
                tc_strcpy(pcProperty, pcValue);
            }
            else if (tc_strcmp(pcFlag, "value") == 0)
            {
                pcCompareValue = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
                tc_strcpy(pcCompareValue, pcValue);
            }
            else if (tc_strcmp(pcFlag, "logic") == 0)
            {
                pcLogic = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
                tc_strcpy(pcLogic, pcValue);
            }
            else if (tc_strcmp(pcFlag, "value_type") == 0)
            {
                pcValueType = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
                tc_strcpy(pcValueType, pcValue);
            }
            else if (tc_strcmp(pcFlag, "operator") == 0)
            {
                pcOperator = (char*)MEM_alloc(((int)tc_strlen(pcValue) + 1) * sizeof(char));
                tc_strcpy(pcOperator, pcValue);
            }
        }
        MEM_free(pcValue);
        MEM_free(pcFlag);
    }
    else
    {
        iRetCode = EPM_wrong_number_of_arguments;
        return EPM_nogo;
    }

    std::string logic(pcLogic);
    char deli = ',';
    std::vector<std::string> types = split_string(std::string(pcType), deli);
    std::vector<std::string> properties = split_string(std::string(pcProperty), deli);
    std::vector<std::string> compareValues = split_string(std::string(pcCompareValue), deli);
    std::vector<std::string> valueTypes = split_string(std::string(pcValueType), deli);
    std::vector<std::string> operators;
    if (pcOperator != NULL)
    {
        operators = split_string(std::string(pcOperator), deli);
    }

    SAFE_MEM_free(pcValueType);
    SAFE_MEM_free(pcType);
    SAFE_MEM_free(pcProperty);
    SAFE_MEM_free(pcCompareValue);
    SAFE_MEM_free(pcLogic);
    SAFE_MEM_free(pcOperator);

    if (operators.size() > 0) {
        if (operators.size() != properties.size()) {
            iRetCode = EPM_invalid_argument;
            sprintf(ErrorMessage, "Wrong workflow handler config: Operators array and Properties array are not same size!");
            EMH_store_error_s1(EMH_severity_user_error, CHECK_SUBGROUP, ErrorMessage);
            return EPM_nogo;
        }
    }
    else {
        operators = std::vector<std::string>(properties.size(), "=");
    }

    /*getting root task*/
    CHECK_ITK(iRetCode, EPM_ask_root_task(msg.task, &tRootTask));
    if (tRootTask != 0)
    {
        int			iTgt = 0;
        tag_t* ptTgt = NULL;

        /*getting target objects*/
        CHECK_ITK(iRetCode, EPM_ask_attachments(tRootTask, EPM_target_attachment, &iTgt, &ptTgt));
        char* itemID = NULL;
        int sz = properties.size();
        char* val = NULL;
        for (int i = 0; i < iTgt; i++)
        {
            char* pcTgtType = NULL;
            CHECK_ITK(iRetCode, WSOM_ask_object_type2(ptTgt[i], &pcTgtType));
            if (!containElement(types, charToString(pcTgtType)))continue;
            bool objectOk = logic == "AND";
            
            for (int j = 0; j < sz; j++) {
                std::string p = properties.at(j);
                std::string cp = compareValues.at(j);
                std::string op = operators.at(j);
                char* property = stringToChar(p);

                if (valueTypes[j] == "string") {
                    CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[i], property, &val));
                }
                else {
                    bool logical = NULL;
                    CHECK_ITK(iRetCode, AOM_ask_value_logical(ptTgt[i], property, &logical));
                    if (logical) {
                        val = stringToChar("True");
                    }
                    else {
                        val = stringToChar("False");
                    }
                }

                std::string realVal = charToString(val);
                if (logic == "AND") {
                    if (!compareWithOperator(realVal, cp, op)) {
                        objectOk = false;
                        break;
                    }
                }
                else if (logic == "OR") {
                    if (compareWithOperator(realVal, cp, op)) {
                        objectOk = true;
                        break;
                    }
                }
            }

            if (!objectOk) {
                CHECK_ITK(iRetCode, AOM_ask_value_string(ptTgt[i], "object_string", &itemID));
                if (logic == "OR") {
                    sprintf(ErrorMessage, "Please check all properties of %s ! At least 01 property must have correct value", itemID);
                }
                else {
                    sprintf(ErrorMessage, "Please check all properties of %s ! All properties must have correct value", itemID);
                }

                EMH_store_error_s1(EMH_severity_user_error, CHECK_SUBGROUP, ErrorMessage);
                decision = EPM_nogo;
            }
        }
        SAFE_MEM_free(itemID);
    }
    return decision;
}