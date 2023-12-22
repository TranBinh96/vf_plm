//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/*==================================================================================================
File description:
    This file contains the implementation for the Business Object, VF4_PieceCostFormDelegate
    Filename:   VF4_PieceCostFormDelegate.cxx
    Module:     VF4_VinFastExtend
    @BMIDE autogenerated
==================================================================================================*/


#ifdef  WNT
#define RAPIDJSON_NO_INT64DEFINE
#include <stdint.h>
#endif

#include <VF4_VinFastExtend/VF4_PieceCostFormDelegate.hxx>
#include <VF4_VinFastExtend/VF4_PieceCostFormGenImpl.hxx>
#include <form/FormDelegate.hxx>
#include <VF4_VinFastExtend/VF4_PieceCostFormImpl.hxx>
#include <mld/journal/journal.h>
#include <base_utils/ResultCheck.hxx>
#include <base_utils/IFail.hxx>
#include <tccore/tctype_errors.h>
#include <common/basic_definitions.h>



using  namespace  ::vf4vinfastextend;


::vf4vinfastextend::VF4_PieceCostFormDelegate*  ::vf4vinfastextend::VF4_PieceCostFormDelegate::m_pInstance = 0;


//----------------------------------------------------------------------------------
// ::vf4vinfastextend::VF4_PieceCostFormDelegate::getInstance()
// Get the singleton class instance
//----------------------------------------------------------------------------------
::vf4vinfastextend::VF4_PieceCostFormDelegate&  ::vf4vinfastextend::VF4_PieceCostFormDelegate::getInstance()
{
    if( 0 == m_pInstance )
    {
        throw  IFail( TYPE_not_initialized );
    }

    return  *m_pInstance;
}


//----------------------------------------------------------------------------------
// ::vf4vinfastextend::VF4_PieceCostFormDelegate::setInstance()
// Set the singleton class instance
//----------------------------------------------------------------------------------
void ::vf4vinfastextend::VF4_PieceCostFormDelegate::setInstance( ::vf4vinfastextend::VF4_PieceCostFormDelegate*  instance )
{
    m_pInstance = instance;
}

//----------------------------------------------------------------------------------
// ::vf4vinfastextend::VF4_PieceCostFormDelegate::createImpl()
// create the Implementation pointer
//----------------------------------------------------------------------------------
::vf4vinfastextend::VF4_PieceCostFormImpl*  ::vf4vinfastextend::VF4_PieceCostFormDelegate::createImpl( VF4_PieceCostForm&  ifObj )
{
    ::vf4vinfastextend::VF4_PieceCostFormImpl*  impl = 0;
    impl = new  ::vf4vinfastextend::VF4_PieceCostFormImpl( ifObj );
    return  impl;
}


//----------------------------------------------------------------------------------
// ::vf4vinfastextend::VF4_PieceCostFormDelegate::deleteImpl()
// Delete the Implementation pointer
//----------------------------------------------------------------------------------
void ::vf4vinfastextend::VF4_PieceCostFormDelegate::deleteImpl( ::vf4vinfastextend::VF4_PieceCostFormImpl*  impl )
{
    delete  impl;
}


//----------------------------------------------------------------------------------
// ::vf4vinfastextend::VF4_PieceCostFormDelegate::VF4_PieceCostFormDelegate()
// Constructor for the class
//----------------------------------------------------------------------------------
::vf4vinfastextend::VF4_PieceCostFormDelegate::VF4_PieceCostFormDelegate()
{
}


//----------------------------------------------------------------------------------
// ::vf4vinfastextend::VF4_PieceCostFormDelegate::~VF4_PieceCostFormDelegate()
// Destructor for the class
//----------------------------------------------------------------------------------
::vf4vinfastextend::VF4_PieceCostFormDelegate::~VF4_PieceCostFormDelegate()
{
}


//----------------------------------------------------------------------------------
// ::vf4vinfastextend::VF4_PieceCostFormDelegate::initializeClass()
// Initialize the Class
//----------------------------------------------------------------------------------
int ::vf4vinfastextend::VF4_PieceCostFormDelegate::initializeClass()
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormDelegate::initializeClass" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        ResultCheck  rStat = ::vf4vinfastextend::VF4_PieceCostFormImpl::initializeClass();
    }
    catch( const IFail& ex )
    {
        ifail = ex.ifail();
    }

    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_return_value( ifail );
        JOURNAL_routine_end();
    }
    return  ifail;
}


///
/// Getter for a Double Property
/// @version Teamcenter 11.3
/// @param value - Parameter Value
/// @param isNull - Returns true if the Parameter value is null
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormDelegate::getVf4_ls_total_costBase(const VF4_PieceCostForm &ifObj,  double &value, bool &isNull ) const
{
    return  ( ifObj.getVF4_PieceCostFormImpl() )->getVf4_ls_total_costBase(  value, isNull  );
}

///
/// Getter for Property Display Value
/// @version Teamcenter 11.3
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormDelegate::getVf4_ls_total_costDisplayableValuesBase(VF4_PieceCostForm &ifObj,  std::vector< std::string > &values )
{
    return  ( ifObj.getVF4_PieceCostFormImpl() )->getVf4_ls_total_costDisplayableValuesBase(  values  );
}

///
/// Getter for a Double Property
/// @version Teamcenter 11.3
/// @param value - Parameter Value
/// @param isNull - Returns true if the Parameter value is null
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormDelegate::getVf4_pp_total_costBase(const VF4_PieceCostForm &ifObj,  double &value, bool &isNull ) const
{
    return  ( ifObj.getVF4_PieceCostFormImpl() )->getVf4_pp_total_costBase(  value, isNull  );
}

///
/// Getter for Property Display Value
/// @version Teamcenter 11.3
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormDelegate::getVf4_pp_total_costDisplayableValuesBase(VF4_PieceCostForm &ifObj,  std::vector< std::string > &values )
{
    return  ( ifObj.getVF4_PieceCostFormImpl() )->getVf4_pp_total_costDisplayableValuesBase(  values  );
}

///
/// Getter for a Double Property
/// @version Teamcenter 11.3
/// @param value - Parameter Value
/// @param isNull - Returns true if the Parameter value is null
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormDelegate::getVf4_pto_total_costBase(const VF4_PieceCostForm &ifObj,  double &value, bool &isNull ) const
{
    return  ( ifObj.getVF4_PieceCostFormImpl() )->getVf4_pto_total_costBase(  value, isNull  );
}

///
/// Getter for Property Display Value
/// @version Teamcenter 11.3
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormDelegate::getVf4_pto_total_costDisplayableValuesBase(VF4_PieceCostForm &ifObj,  std::vector< std::string > &values )
{
    return  ( ifObj.getVF4_PieceCostFormImpl() )->getVf4_pto_total_costDisplayableValuesBase(  values  );
}

///
/// Getter for a Double Property
/// @version Teamcenter 11.3
/// @param value - Parameter Value
/// @param isNull - Returns true if the Parameter value is null
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormDelegate::getVf4_total_costBase(const VF4_PieceCostForm &ifObj,  double &value, bool &isNull ) const
{
    return  ( ifObj.getVF4_PieceCostFormImpl() )->getVf4_total_costBase(  value, isNull  );
}

///
/// Getter for Property Display Value
/// @version Teamcenter 11.3
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormDelegate::getVf4_total_costDisplayableValuesBase(VF4_PieceCostForm &ifObj,  std::vector< std::string > &values )
{
    return  ( ifObj.getVF4_PieceCostFormImpl() )->getVf4_total_costDisplayableValuesBase(  values  );
}

///
/// Setter for a Double Property
/// @version Teamcenter 11.3
/// @param value - Value to be set for the parameter
/// @param isNull - If true, set the parameter value to null
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormDelegate::setVf4_total_costBase(VF4_PieceCostForm &ifObj,  double value, bool isNull )
{
    return  ( ifObj.getVF4_PieceCostFormImpl() )->setVf4_total_costBase(  value, isNull  );
}
