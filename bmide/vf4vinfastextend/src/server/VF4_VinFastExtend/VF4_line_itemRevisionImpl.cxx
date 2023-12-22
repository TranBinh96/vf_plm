//@<COPYRIGHT>@
//==================================================
//Copyright $2021.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

// 
//  @file
//  This file contains the implementation for the Business Object VF4_line_itemRevisionImpl
//

#include <VF4_VinFastExtend/VF4_line_itemRevisionImpl.hxx>

#include <fclasses/tc_string.h>

using namespace vf4vinfastextend;

//----------------------------------------------------------------------------------
// VF4_line_itemRevisionImpl::VF4_line_itemRevisionImpl(VF4_line_itemRevision& busObj)
// Constructor for the class
//----------------------------------------------------------------------------------
VF4_line_itemRevisionImpl::VF4_line_itemRevisionImpl( VF4_line_itemRevision& busObj )
   : VF4_line_itemRevisionGenImpl( busObj )
{
}

//----------------------------------------------------------------------------------
// VF4_line_itemRevisionImpl::~VF4_line_itemRevisionImpl()
// Destructor for the class
//----------------------------------------------------------------------------------
VF4_line_itemRevisionImpl::~VF4_line_itemRevisionImpl()
{
}

//----------------------------------------------------------------------------------
// VF4_line_itemRevisionImpl::initializeClass
// This method is used to initialize this Class
//----------------------------------------------------------------------------------
int VF4_line_itemRevisionImpl::initializeClass()
{
    int ifail = ITK_ok;
    static bool initialized = false;

    if( !initialized )
    {
        ifail = VF4_line_itemRevisionGenImpl::initializeClass( );
        if ( ifail == ITK_ok )
        {
            initialized = true;
        }
    }
    return ifail;
}

double VF4_line_itemRevisionImpl::calculateTotalCost() const
{
	double sup_package_amount = 0;
	logical isNull = true;

	//Supplier Packaging Amount
	getVF4_line_itemRevision()->getDouble("vf4_supplier_pkg_amount", sup_package_amount, isNull);

	double sup_logisis_cost = 0;//Supplier Logisistics Cost
	getVF4_line_itemRevision()->getDouble("vf4_Supplier_logistic_cost", sup_logisis_cost, isNull);

	double piece_cost_value = 0;//Supplier Basic Piece Price
	getVF4_line_itemRevision()->getDouble("vf4_supplier_piece_cost_exw", piece_cost_value, isNull);

	//Total Cost
	double value = sup_package_amount + sup_logisis_cost + piece_cost_value;
	return value;
}

///
/// Getter for a Double Property
/// @param value - Parameter Value
/// @param isNull - Returns true if the Parameter value is null
/// @return - Status. 0 if successful
///
int  VF4_line_itemRevisionImpl::getVf4_piece_cost_autoBase( double & value, bool & isNull ) const
{
	TC_write_syslog("[VF] ENTER: %s\n", __FUNCTION__);
	int ifail = ITK_ok;

	value = calculateTotalCost();
	isNull = false;

	TC_write_syslog("[VF] LEAVE: %s\n", __FUNCTION__);
	return ifail;
}

///
/// Getter for Property Display Value
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  VF4_line_itemRevisionImpl::getVf4_piece_cost_autoDisplayableValuesBase( std::vector< std::string > & values )
{
	TC_write_syslog("[VF] ENTER: %s\n", __FUNCTION__);
	int ifail = ITK_ok;

	double value = calculateTotalCost();

	std::string valueStr(std::to_string(value));
	values.push_back(valueStr);

	TC_write_syslog("[VF] LEAVE: %s\n", __FUNCTION__);
	return ifail;
}

