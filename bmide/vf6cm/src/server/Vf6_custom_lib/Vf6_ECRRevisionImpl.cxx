//@<COPYRIGHT>@
//==================================================
//Copyright $2019.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

// 
//  @file
//  This file contains the implementation for the Business Object Vf6_ECRRevisionImpl
//

#include <Vf6_custom_lib/Vf6_ECRRevisionImpl.hxx>

#include <fclasses/tc_string.h>
#include <tc/tc.h>
#include <tccore/aom.h>
#include <tccore/grm.h>
#include <tccore/grm_errors.h>
#include <tccore/grmtype.h>
#include <tc/emh.h>
#include <tc/emh_errors.h>
#include <tccore/aom_prop.h>

using namespace vf6cm;

//----------------------------------------------------------------------------------
// Vf6_ECRRevisionImpl::Vf6_ECRRevisionImpl(Vf6_ECRRevision& busObj)
// Constructor for the class
//----------------------------------------------------------------------------------
Vf6_ECRRevisionImpl::Vf6_ECRRevisionImpl( Vf6_ECRRevision& busObj )
   : Vf6_ECRRevisionGenImpl( busObj )
{
}

//----------------------------------------------------------------------------------
// Vf6_ECRRevisionImpl::~Vf6_ECRRevisionImpl()
// Destructor for the class
//----------------------------------------------------------------------------------
Vf6_ECRRevisionImpl::~Vf6_ECRRevisionImpl()
{
}

//----------------------------------------------------------------------------------
// Vf6_ECRRevisionImpl::initializeClass
// This method is used to initialize this Class
//----------------------------------------------------------------------------------
int Vf6_ECRRevisionImpl::initializeClass()
{
    int ifail = ITK_ok;
    static bool initialized = false;

    if( !initialized )
    {
        ifail = Vf6_ECRRevisionGenImpl::initializeClass( );
        if ( ifail == ITK_ok )
        {
            initialized = true;
        }
    }
    return ifail;
}


///
/// Getter for a Double Property
/// @param value - Parameter Value
/// @param isNull - Returns true if the Parameter value is null
/// @return - Status. 0 if successful
///
int  Vf6_ECRRevisionImpl::getVf6_total_costBase( double & value, bool & isNull ) const
{
    int ifail = ITK_ok;
       isNull = false;
       double result = 0;
       // Your Implementation
       tag_t ecr_tag = NULLTAG;
       ecr_tag = this->getVf6_ECRRevision()->getTag();
       result = this->calTotal(ecr_tag);
       value = result;
    // Your Implementation

    return ifail;
}

///
/// Getter for a Boolean Property
/// @param value - Parameter Value
/// @param isNull - Returns true if the Parameter value is null
/// @return - Status. 0 if successful
///
int  Vf6_ECRRevisionImpl::getVf6_is_pur_biz_reqBase( bool & value, bool & isNull ) const
{
    int ifail = ITK_ok;

       // Your Implementation
   	isNull = false;
   	double result = 0;
   	// Your Implementation
   	tag_t ecr_tag = NULLTAG;
   	ecr_tag = this->getVf6_ECRRevision()->getTag();
   	result = this->calTotal(ecr_tag);
   	if(result != 0)
   	{
   		value = true;
   	}
   	else
   	{
   		value = false;
   	}
    // Your Implementation

    return ifail;
}
double Vf6_ECRRevisionImpl::calTotal(tag_t ecr_tag) const
{
	int ifail = ITK_ok;
		tag_t tFormRel = NULLTAG;

		tag_t *listobject = NULLTAG;
		char *objType = "";
		char *objType2 = "";
		int count = 0;
		double mat_cost = 0;
		double supplier_eng_cost = 0;
		double tooling_cost = 0;
		double scrap_cost = 0;
		double total_cost = 0;
		double totalCostInAllForms = 0;
		TC_write_syslog("start get total cost\n");
		TC_write_syslog("NEW version\n");

		//find relation type
		ifail = GRM_find_relation_type("Vf6_change_forms", &tFormRel);
		ifail = WSOM_ask_object_type2(ecr_tag, &objType);

		ifail = GRM_list_secondary_objects_only(ecr_tag, tFormRel, &count, &listobject);
		for (int i = 0; i < count; i++) {
				ifail = WSOM_ask_object_type2(listobject[i], &objType2);
			if (ifail == 0 && tc_strcmp(objType2, "Vf6_purchasing") == 0) {
				ifail = AOM_ask_value_double(listobject[i], "vf6_material_costs", &mat_cost);
				ifail =		AOM_ask_value_double(listobject[i], "vf6_supplier_eng_costs", &supplier_eng_cost);
				ifail =		AOM_ask_value_double(listobject[i], "vf6_tooling_costs", &tooling_cost);
				ifail =		AOM_ask_value_double(listobject[i], "vf6_scrap_costs", &scrap_cost);
				total_cost = mat_cost + supplier_eng_cost + tooling_cost + scrap_cost;
				totalCostInAllForms += total_cost;
				MEM_free(objType2);
			} else {
				TC_write_syslog("WARNING: Object is not Vf6_purchasing!\n");
			}
		}
		return totalCostInAllForms;

}
