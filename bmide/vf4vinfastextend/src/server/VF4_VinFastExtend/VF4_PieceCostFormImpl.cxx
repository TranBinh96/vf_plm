//@<COPYRIGHT>@
//==================================================
//Copyright $2021.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

// 
//  @file
//  This file contains the implementation for the Business Object VF4_PieceCostFormImpl
//

#include <VF4_VinFastExtend/VF4_PieceCostFormImpl.hxx>

#include <fclasses/tc_string.h>

using namespace vf4vinfastextend;

//----------------------------------------------------------------------------------
// VF4_PieceCostFormImpl::VF4_PieceCostFormImpl(VF4_PieceCostForm& busObj)
// Constructor for the class
//----------------------------------------------------------------------------------
VF4_PieceCostFormImpl::VF4_PieceCostFormImpl( VF4_PieceCostForm& busObj )
   : VF4_PieceCostFormGenImpl( busObj )
{

}

//----------------------------------------------------------------------------------
// VF4_PieceCostFormImpl::~VF4_PieceCostFormImpl()
// Destructor for the class
//----------------------------------------------------------------------------------
VF4_PieceCostFormImpl::~VF4_PieceCostFormImpl()
{
}

//----------------------------------------------------------------------------------
// VF4_PieceCostFormImpl::initializeClass
// This method is used to initialize this Class
//----------------------------------------------------------------------------------
int VF4_PieceCostFormImpl::initializeClass()
{
    int ifail = ITK_ok;
    static bool initialized = false;

    if( !initialized )
    {
        ifail = VF4_PieceCostFormGenImpl::initializeClass( );
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
double VF4_PieceCostFormImpl::calculateTotalCost() const
{
	double sup_package_amount = 0;
	logical isNull = true;

	getVF4_PieceCostForm()->getDouble("vf4_supplier_package_amount", sup_package_amount, isNull);

	double sup_logisis_cost = 0;
	getVF4_PieceCostForm()->getDouble("vf4_supplier_logisis_cost", sup_logisis_cost, isNull);

	double piece_cost_value = 0;
	getVF4_PieceCostForm()->getDouble("vf4_piece_cost_value_status", piece_cost_value, isNull);

	double tax = 0;
	getVF4_PieceCostForm()->getDouble("vf4_prd_tax_absolute", tax, isNull);

	double manuf = 0;
	getVF4_PieceCostForm()->getDouble("vf4_miscellaneous_cost", manuf, isNull);

	double value = sup_package_amount + sup_logisis_cost + piece_cost_value + tax + manuf;
	return value;
}
int  VF4_PieceCostFormImpl::getVf4_total_costBase( double & value, bool & isNull ) const
{
	//TC_write_syslog("[VF] ENTER: %s", __FUNCTION__);

	int ifail = ITK_ok;

	value = calculateTotalCost();
	isNull = false;

	return ifail;
}

///
/// Getter for Property Display Value
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  VF4_PieceCostFormImpl::getVf4_total_costDisplayableValuesBase( std::vector< std::string > & values )
{
	TC_write_syslog("[VF] ENTER: %s\n", __FUNCTION__);
	int ifail = ITK_ok;

	double value = calculateTotalCost();

	std::string valueStr(std::to_string(value));
	values.push_back(valueStr);

    return ifail;
}

///
/// Setter for a Double Property
/// @param value - Value to be set for the parameter
/// @param isNull - If true, set the parameter value to null
/// @return - Status. 0 if successful
///
int  VF4_PieceCostFormImpl::setVf4_total_costBase( double  /*value*/, bool  /*isNull*/ )
{
    int ifail = ITK_ok;

    // Your Implementation

    return ifail;
}


double VF4_PieceCostFormImpl::calculateLsTotalCost() const
{
	double sup_package_amount = 0;
	logical isNull = true;
	getVF4_PieceCostForm()->getDouble("vf4_ls_supplier_pkg_amount", sup_package_amount, isNull);

	double sup_logisis_cost = 0;
	getVF4_PieceCostForm()->getDouble("vf4_ls_supplier_logis_cost", sup_logisis_cost, isNull);

	double piece_cost_value = 0;
	getVF4_PieceCostForm()->getDouble("vf4_ls_piece_cost_value", piece_cost_value, isNull);

	double tax = 0;
	getVF4_PieceCostForm()->getDouble("vf4_ls_tax_absolute", tax, isNull);

	double manuf = 0;
	getVF4_PieceCostForm()->getDouble("vf4_ls_manufacturing_cost", manuf, isNull);

	double value = sup_package_amount + sup_logisis_cost + piece_cost_value + tax + manuf;
	return value;
}
///
/// Getter for a Double Property
/// @param value - Parameter Value
/// @param isNull - Returns true if the Parameter value is null
/// @return - Status. 0 if successful
///
int  VF4_PieceCostFormImpl::getVf4_ls_total_costBase( double & value, bool & isNull ) const
{
	TC_write_syslog("[VF] ENTER: %s\n", __FUNCTION__);

	int ifail = ITK_ok;
	value = calculateLsTotalCost();
	isNull = false;

	return ifail;
}

///
/// Getter for Property Display Value
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  VF4_PieceCostFormImpl::getVf4_ls_total_costDisplayableValuesBase( std::vector< std::string > & values )
{
	TC_write_syslog("[VF] ENTER: %s\n", __FUNCTION__);
	int ifail = ITK_ok;

	double value = calculateLsTotalCost();

	std::string valueStr(std::to_string(value));
	values.push_back(valueStr);

	return ifail;
}


double VF4_PieceCostFormImpl::calculatePpTotalCost() const
{
	double sup_package_amount = 0;
	logical isNull = true;
	getVF4_PieceCostForm()->getDouble("vf4_pp_supplier_pkg_amount", sup_package_amount, isNull);

	double sup_logisis_cost = 0;
	getVF4_PieceCostForm()->getDouble("vf4_pp_supplier_logis_cost", sup_logisis_cost, isNull);

	double piece_cost_value = 0;
	getVF4_PieceCostForm()->getDouble("vf4_pp_piece_cost_value", piece_cost_value, isNull);

	double tax = 0;
	getVF4_PieceCostForm()->getDouble("vf4_pp_tax_absolute", tax, isNull);

	double manuf = 0;
	getVF4_PieceCostForm()->getDouble("vf4_pp_manufacturing_cost", manuf, isNull);

	double value = sup_package_amount + sup_logisis_cost + piece_cost_value + tax + manuf;
	return value;
}
///
/// Getter for a Double Property
/// @param value - Parameter Value
/// @param isNull - Returns true if the Parameter value is null
/// @return - Status. 0 if successful
///
int  VF4_PieceCostFormImpl::getVf4_pp_total_costBase( double & value, bool & isNull ) const
{
	TC_write_syslog("[VF] ENTER: %s\n", __FUNCTION__);

	int ifail = ITK_ok;
	value = calculatePpTotalCost();
	isNull = false;

	return ifail;
}

///
/// Getter for Property Display Value
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  VF4_PieceCostFormImpl::getVf4_pp_total_costDisplayableValuesBase( std::vector< std::string > & values )
{
	TC_write_syslog("[VF] ENTER: %s\n", __FUNCTION__);
	int ifail = ITK_ok;

	double value = calculatePpTotalCost();

	std::string valueStr(std::to_string(value));
	values.push_back(valueStr);

	return ifail;
}


double VF4_PieceCostFormImpl::calculatePtoTotalCost() const
{
	double sup_package_amount = 0;
	logical isNull = true;
	getVF4_PieceCostForm()->getDouble("vf4_pto_supplier_pkg_amount", sup_package_amount, isNull);

	double sup_logisis_cost = 0;
	getVF4_PieceCostForm()->getDouble("vf4_pto_supplier_logis_cost", sup_logisis_cost, isNull);

	double piece_cost_value = 0;
	getVF4_PieceCostForm()->getDouble("vf4_pto_piece_cost_value", piece_cost_value, isNull);

	double tax = 0;
	getVF4_PieceCostForm()->getDouble("vf4_pto_tax_absolute", tax, isNull);

	double manuf = 0;
	getVF4_PieceCostForm()->getDouble("vf4_pto_manufacturing_cost", manuf, isNull);

	double value = sup_package_amount + sup_logisis_cost + piece_cost_value + tax + manuf;
	return value;
}
///
/// Getter for a Double Property
/// @param value - Parameter Value
/// @param isNull - Returns true if the Parameter value is null
/// @return - Status. 0 if successful
///
int  VF4_PieceCostFormImpl::getVf4_pto_total_costBase( double & value, bool & isNull ) const
{
	int ifail = ITK_ok;
	TC_write_syslog("[VF] ENTER: %s\n", __FUNCTION__);

	value = calculatePtoTotalCost();
	isNull = false;

	return ifail;
}

///
/// Getter for Property Display Value
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  VF4_PieceCostFormImpl::getVf4_pto_total_costDisplayableValuesBase( std::vector< std::string > & values )
{
	TC_write_syslog("[VF] ENTER: %s\n", __FUNCTION__);
	int ifail = ITK_ok;

	double value = calculatePtoTotalCost();

	std::string valueStr(std::to_string(value));
	values.push_back(valueStr);

	return ifail;
}

