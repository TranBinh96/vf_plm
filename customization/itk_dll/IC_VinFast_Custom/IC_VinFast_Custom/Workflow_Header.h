///////////////////////////////////////////////////
//	System Header Files
///////////////////////////////////////////////////
#include <windows.h>
#include <algorithm>
#include <fstream>
#include <iostream>
#include <list>
#include <map>
#include <set>
#include <string>
#include <vector>

using namespace std;


///////////////////////////////////////////////////
//	Iman Header Files
///////////////////////////////////////////////////
#include <tc/tc_startup.h>
#include <textsrv/textserver.h>
#include <epm/epm.h>
#include <epm/epm_errors.h>
#include <epm/epm_toolkit_tc_utils.h>
#include <user_exits/epm_toolkit_utils.h>
#include <tc/tc.h>
#include <tc/tc_util.h>
#include <sa/tcfile.h>
#include <tccore/tctype.h>
#include <tc/tc_arguments.h>
#include <sa/sa.h>
#include <tccore/item.h>
#include <tccore/aom.h>
#include <tccore/project.h>
#include <epm/cr.h>
#include <epm/cr_action_handlers.h>
#include <epm/cr_errors.h>
#include <tccore/custom.h>
#include <ae/ae.h>
#include <epm/aliaslist.h>
#include <stdio.h>
#include <tccore/aom_prop.h>
#include <epm/epm_task_template_itk.h>
#include <direct.h>
#include <epm/distributionlist.h>
#include <fclasses/tc_date.h>
#include <tc/emh.h>
#include <tc/emh_errors.h>
#include <tc/envelope.h>
#include <tc/folder.h>
#include <tccore/grm.h>
#include <tccore/grm_errors.h>
#include <tccore/grmtype.h>
#include <ict/ict_userservice.h>
#include <tc/iman.h>
#include <tc/iman_util.h>
#include <sa/imanfile.h>
#include <tccore/imantype.h>
#include <tccore/item.h>
#include <tccore/method.h>
#include <property/nr.h>
#include <pom/pom/pom.h>
#include <pom/pom/pom_errors.h>
#include <tc/preferences.h>
#include <property/prop.h>
#include <ps/ps.h>
#include <sa/sa.h>
#include <sa/sa_errors.h>
#include <ss/ss_const.h>
#include <ss/ss_errors.h>
#include <time.h>
#include <sa/user.h>
#include <user_exits/user_exits.h>
#include <tccore/workspaceobject.h>
#include <tc/wsouif_errors.h>
#include <tc/iman_arguments.h>
#include <epm/aliaslist.h>
#include <tccore/project.h>
#include <epm/distributionlist.h>
#include <tc/tc_arguments.h>

#include <sa/groupmember.h>
#include <ecm/ecm.h>
#include <qry/qry.h>
#include <bom/bom.h>
#include <epm/signoff.h>
#include <ecm/genealogy.h>
#include <epm/cr_effectivity.h>
#include <ics\ics.h>
#include <ics\ics2.h>
#include <form\form.h>




struct delta_equation
{
public:
	delta_equation(){lhs="";rhs="";}
	std::string lhs;
	std::string rhs;
};

#define ITK_CALL(X) (report_error( __FILE__, __LINE__, #X, (X)))


// HSM Error Base Definition
#define DC2_CM_WF_ERROR_BASE	(EMH_USER_error_base) /* 919000 */

#define NO_VALID_RENDERING_FILE_ATTACHED				(DC2_CM_WF_ERROR_BASE + 1)	// 919001
#define MATERIAL_NOT_RELEASED							(DC2_CM_WF_ERROR_BASE + 2)
#define UNRELEASED_OR_UNCLASSIFIED_DESIGN				(DC2_CM_WF_ERROR_BASE + 3)
#define NO_VALID_ARGUMENT_RELATION_TYPE					(DC2_CM_WF_ERROR_BASE + 4)
#define NO_VALID_ENGINEERING_PART_ATTACHED				(DC2_CM_WF_ERROR_BASE + 5)
#define NO_VALID_ARGUMENT_TARGET_OBJECT_TYPE			(DC2_CM_WF_ERROR_BASE + 6)
#define	UNRELEASED_BOMLINES								(DC2_CM_WF_ERROR_BASE + 7)



///////////////////////////////////////////////////
//	Macros
///////////////////////////////////////////////////

/* Macro definition */
#define CHECK_ITK(iSts,fn){\
    if(iSts == ITK_ok){\
        if((iSts = (fn) ) != ITK_ok){\
            char* error_str = NULL;\
            EMH_ask_error_text(iSts, &error_str);\
            TC_write_syslog ("+++ERROR: %d ERROR MSG: %s. Error in line %d, function %s",\
                iSts, error_str, __LINE__, __FUNCTION__);\
            MEM_free(error_str);\
        }\
    }\
}

#define BAL_free_memory(x){\
 if(x != NULL)\
     MEM_free(x);\
 x = NULL;\
}

#define CHECK_FAIL if(status!=ITK_ok) {printf("Error at (line:%d) with ifail:%d\n",__LINE__,status);exit(0);}

static int report_error ( char *file, int line, char *function, int rc)
{
	if ((rc) != ITK_ok) 
	{
		char *msg = NULL;
		EMH_ask_error_text (rc, &msg);
		if (msg != NULL) {
			cout << "ERROR: "<< rc << " ERROR MSG: " << msg << endl;
			TC_write_syslog("ERROR: %d ERROR MSG: %s.\n", rc, msg);
			cout << "FUNCTION: " << function << endl << "FILE: " << file << "LINE: " << line << endl;
			TC_write_syslog("FUNCTION: %s\nFILE: %s LINE: %d\n", function, file, line);
			SAFE_SM_FREE(msg);
		}
	}
	return rc;
}

#define SAFE_SM_FREE( a )   \
do                          \
{                           \
    if ( (a) != NULL )      \
    {                       \
        MEM_free( (a) );    \
        (a) = NULL;         \
    }                       \
}                           \
while ( 0 )

