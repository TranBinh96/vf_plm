/*
 * vf_custom.hxx
 *
 *  Created on: Oct 8, 2020
 *      Author: vinfastplm
 */

#ifndef VF_CUSTOM_HXX_
#define VF_CUSTOM_HXX_

#include <iostream>
#include <stdio.h>
#include <string>
#include <string.h>
#include <fstream>
#include <vector>
#include <tc/tc_startup.h>
#include <textsrv/textserver.h>
#include <epm/epm.h>
#include <epm/epm_errors.h>
#include <epm/epm_toolkit_tc_utils.h>
#include <user_exits/epm_toolkit_utils.h>
#include <tc/tc_util.h>
#include <sa/tcfile.h>
#include <tccore/tctype.h>
#include <tc/tc_arguments.h>
#include <sa/sa.h>
#include <tccore/item.h>
#include <cfm/cfm.h>
#include <tccore/aom.h>
#include <tccore/project.h>
#include <epm/cr.h>
#include <epm/cr_action_handlers.h>
#include <tccore/custom.h>
#include <ae/ae.h>
#include  <RES\res_itk.h>
#include <tccore/aom_prop.h>
#include <epm/epm_task_template_itk.h>
#include <direct.h>
#include <fclasses/tc_date.h>
#include <tc/emh.h>
#include <tc/emh_errors.h>
#include <tc/envelope.h>
#include <tc/folder.h>
#include <tccore/grm.h>
#include <tccore/grm_errors.h>
#include <tccore/grmtype.h>
#include <ict/ict_userservice.h>
#include <tccore/method.h>
#include <property/nr.h>
#include <pom/pom/pom.h>
#include <pom/pom/pom_errors.h>
#include <tc/preferences.h>
#include <property/prop.h>
#include <ps/ps.h>
#include <sa/sa_errors.h>
#include <ss/ss_const.h>
#include <ss/ss_errors.h>
#include <time.h>
#include <sa/user.h>
#include <user_exits/user_exits.h>
#include <tccore/workspaceobject.h>
#include <tc/wsouif_errors.h>
#include <sa/groupmember.h>
#include <ecm/ecm.h>
#include <qry/qry.h>
#include <bom/bom.h>
#include <epm/signoff.h>
#include <ics\ics.h>
#include <ics\ics2.h>
#include <tccore\uom.h>
#include <map>

using namespace std;


#define VF_IS_DEBUG false

#define EXIT_FAILURE 1
#define ERROR_CHECK(X) (report_error( __FILE__, __LINE__, #X, (X)))


#define VF_DOUBLE_NULL -0.0000001

void vf_split(std::string str, const std::string &delimiter, std::vector<std::string> &result);
std::string ltrim(const std::string& s);
std::string rtrim(const std::string& s);
std::string ltrim(const std::string& s);
void report_error( char *file, int line, char *function, int return_code);
tag_t getPart(string partNumber, string partType, vector<string> &errorMsgs);
tag_t getFuturePart(string partNumber, string partType, vector<string> &errorMsgs);
bool getLogicalProperty(tag_t obj, const char *stringPropName);
double getDoubleProperty(tag_t obj, const char *doublePropName);
string getStringProperty(tag_t obj, const char *stringPropName);
void queryObj(const char *queryName, map<string, string> keysAndVals, vector<tag_t> &founds, vector<string> &errorMsgs);
void printObjString(tag_t obj, const char *prefixStr);

#endif /* VF_CUSTOM_HXX_ */
