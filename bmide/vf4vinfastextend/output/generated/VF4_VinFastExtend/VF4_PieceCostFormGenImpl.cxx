//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/*==================================================================================================
File description:
    This file contains the implementation for the Business Object, VF4_PieceCostFormGenImpl
    Filename:   VF4_PieceCostFormGenImpl.cxx
    Module:     VF4_VinFastExtend
    @BMIDE autogenerated
==================================================================================================*/


#ifdef  WNT
#define RAPIDJSON_NO_INT64DEFINE
#include <stdint.h>
#endif

#include <pom/pom/pom_errors.h>
#include <pom/pom/pom.h>
#include <mld/journal/journal.h>
#include <tc/tc_errors.h>
#include <base_utils/ResultCheck.hxx>
#include <base_utils/IFail.hxx>
#include <base_utils/DateTime.hxx>
#include <base_utils/Mem.h>
#include <VF4_VinFastExtend/VF4_PieceCostFormImpl.hxx>
#include <tccore/method.h>
#include <tccore/tctype.h>
#include <tccore/tctype.hxx>
#include <property/prop_msg.h>
#include <stdarg.h>
#include <ug_va_copy.h>

#include <VF4_VinFastExtend/VF4_PieceCostForm.hxx>
#include <VF4_VinFastExtend/VF4_PieceCostFormGenImpl.hxx>

#include <VF4_VinFastExtend/VF4_PieceCostFormDelegate.hxx>
#include <metaframework/BusinessObjectRegistry.hxx>
#include <metaframework/BusinessObject.hxx>
#include <metaframework/BusinessObjectRef.hxx>
#include <extensionframework/OperationDispatcherRegistry.hxx>


using  namespace  ::vf4vinfastextend;


extern  "C"
{
    //  callback function declarations for owned Property Operations
    static int  getVf4_ls_total_cost_1Double( METHOD_message_t*  msg, va_list  args );
    static int  getVf4_ls_total_costDisplayableValues_2Strings( METHOD_message_t*  msg, va_list  args );
    static int  getVf4_pp_total_cost_3Double( METHOD_message_t*  msg, va_list  args );
    static int  getVf4_pp_total_costDisplayableValues_4Strings( METHOD_message_t*  msg, va_list  args );
    static int  getVf4_pto_total_cost_5Double( METHOD_message_t*  msg, va_list  args );
    static int  getVf4_pto_total_costDisplayableValues_6Strings( METHOD_message_t*  msg, va_list  args );
    static int  getVf4_total_cost_7Double( METHOD_message_t*  msg, va_list  args );
    static int  getVf4_total_costDisplayableValues_8Strings( METHOD_message_t*  msg, va_list  args );
    static int  setVf4_total_cost_9Double( METHOD_message_t*  msg, va_list  args );

}


//----------------------------------------------------------------------------------
//  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::VF4_PieceCostFormGenImpl( ::vf4vinfastextend::VF4_PieceCostForm &)
//  Constructor for the class
//----------------------------------------------------------------------------------
::vf4vinfastextend::VF4_PieceCostFormGenImpl::VF4_PieceCostFormGenImpl( ::vf4vinfastextend::VF4_PieceCostForm&  busObj )
    : m_busObj( &busObj )
{
}


//----------------------------------------------------------------------------------
//  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::~VF4_PieceCostFormGenImpl()
//  Destructor for the class
//----------------------------------------------------------------------------------
::vf4vinfastextend::VF4_PieceCostFormGenImpl::~VF4_PieceCostFormGenImpl()
{
    m_busObj = 0;
}

//----------------------------------------------------------------------------------
//  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::registerPropertyFunctions()
//  registerPropertyFunctions
//----------------------------------------------------------------------------------
int  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::registerPropertyFunctions()
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormGenImpl::registerPropertyFunctions" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        ResultCheck  rStat = 0;
        METHOD_id_t  method = {0};

        rStat = METHOD__register_prop_operationFn( "VF4_PieceCostForm", "vf4_ls_total_cost",
                                                   PROP_ask_value_double_msg, &getVf4_ls_total_cost_1Double, 0, &method );

        rStat = METHOD__register_prop_operationFn( "VF4_PieceCostForm", "vf4_ls_total_cost",
                                                   PROP_ask_displayable_values_msg, &getVf4_ls_total_costDisplayableValues_2Strings, 0, &method );

        rStat = METHOD__register_prop_operationFn( "VF4_PieceCostForm", "vf4_pp_total_cost",
                                                   PROP_ask_value_double_msg, &getVf4_pp_total_cost_3Double, 0, &method );

        rStat = METHOD__register_prop_operationFn( "VF4_PieceCostForm", "vf4_pp_total_cost",
                                                   PROP_ask_displayable_values_msg, &getVf4_pp_total_costDisplayableValues_4Strings, 0, &method );

        rStat = METHOD__register_prop_operationFn( "VF4_PieceCostForm", "vf4_pto_total_cost",
                                                   PROP_ask_value_double_msg, &getVf4_pto_total_cost_5Double, 0, &method );

        rStat = METHOD__register_prop_operationFn( "VF4_PieceCostForm", "vf4_pto_total_cost",
                                                   PROP_ask_displayable_values_msg, &getVf4_pto_total_costDisplayableValues_6Strings, 0, &method );

        rStat = METHOD__register_prop_operationFn( "VF4_PieceCostForm", "vf4_total_cost",
                                                   PROP_ask_value_double_msg, &getVf4_total_cost_7Double, 0, &method );

        rStat = METHOD__register_prop_operationFn( "VF4_PieceCostForm", "vf4_total_cost",
                                                   PROP_ask_displayable_values_msg, &getVf4_total_costDisplayableValues_8Strings, 0, &method );

        rStat = METHOD__register_prop_operationFn( "VF4_PieceCostForm", "vf4_total_cost",
                                                   PROP_set_value_double_msg, &setVf4_total_cost_9Double, 0, &method );

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



//----------------------------------------------------------------------------------
//  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVF4_PieceCostForm()
//  Accessor for the business object
//----------------------------------------------------------------------------------
::vf4vinfastextend::VF4_PieceCostForm*
::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVF4_PieceCostForm() const
{
    return  m_busObj;
}


//----------------------------------------------------------------------------------
//  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::initializeClass()
//  Initialize Class
//----------------------------------------------------------------------------------
int  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::initializeClass()
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormGenImpl::initializeClass" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        //  invoke callback function registration for Property Operations
        ::Teamcenter::BusinessObjectRegistry::instance().initializePropertyFnRegister( "VF4_PieceCostForm", &( VF4_PieceCostFormGenImpl::registerPropertyFunctions ) );
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



//----------------------------------------------------------------------------------
//  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::newInstance()
//  Create a new instance of this Business Object
//----------------------------------------------------------------------------------
::Teamcenter::RootObjectImpl*  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::newInstance()
{
    ::Teamcenter::RootObjectImpl*  pRootObj = 0;
    ::vf4vinfastextend::VF4_PieceCostForm*  busObj = dynamic_cast< ::vf4vinfastextend::VF4_PieceCostForm* >
         ( ::Teamcenter::BusinessObjectRegistry::instance().createBusinessObject( ::vf4vinfastextend::VF4_PieceCostForm::getInterfaceName() ) );
    if( busObj != 0 )
    {
        pRootObj = ( ::Teamcenter::RootObjectImpl* )( busObj->getVF4_PieceCostFormImpl() );
    }

    return  pRootObj;
}

//
//  Callback Function for Property Operation,    getVf4_ls_total_cost
//
static int  getVf4_ls_total_cost_1Double( METHOD_message_t*  msg, va_list  args )
{
    int ifail = 0;

    va_list  largs;
    va_copy( largs, args );

    va_arg( largs, tag_t );
    double* value  = va_arg( largs, double* );
    logical* is_null = va_arg( largs, logical* );
    logical* is_empty = va_arg( largs, logical* );

    va_end( largs );

    tag_t  tagObject = NULLTAG;
    *value = 0;
    *is_null = false;
    *is_empty = false;
    METHOD_PROP_MESSAGE_OBJECT( msg, tagObject );
    BusinessObjectRef< ::vf4vinfastextend::VF4_PieceCostForm >  owningObj( tagObject );
    ifail = ::vf4vinfastextend::VF4_PieceCostFormDelegate::getInstance().getVf4_ls_total_costBase( const_cast< ::vf4vinfastextend::VF4_PieceCostForm & >( *owningObj ), *value, *is_null );

    return  ifail;
}

//
//  Callback Function for Property Operation,    getVf4_ls_total_costDisplayableValues
//
static int  getVf4_ls_total_costDisplayableValues_2Strings( METHOD_message_t*  msg, va_list  args )
{
    int ifail = 0;

    va_list  largs;
    va_copy( largs, args );

    va_arg( largs, tag_t );
    int* n_values = va_arg( largs, int* );
    char*** values = va_arg( largs, char*** );

    va_end( largs );

    tag_t  tagObject = NULLTAG;
    *n_values = 0;
    *values = 0;
    METHOD_PROP_MESSAGE_OBJECT( msg, tagObject );
    BusinessObjectRef< ::vf4vinfastextend::VF4_PieceCostForm >  owningObj( tagObject );
    if( owningObj.isInstanceOf< ::vf4vinfastextend::VF4_PieceCostForm >() )
    {
        std::vector< std::string >  strings;
        ifail = ::vf4vinfastextend::VF4_PieceCostFormDelegate::getInstance().getVf4_ls_total_costDisplayableValuesBase( const_cast< ::vf4vinfastextend::VF4_PieceCostForm & >( *owningObj ), strings );
        if( ifail == 0 )
        {
            *n_values = static_cast< int >( strings.size() );
            *values = (char**)MEM_alloc( (*n_values) * sizeof(char*) );
            for( int inx = 0; inx < (*n_values); ++inx )
            {
                std::string  aString = strings[inx];
                (*values)[inx] = MEM_string_copy( aString.c_str() );
            }
        }
    }

    return  ifail;
}

//
//  Callback Function for Property Operation,    getVf4_pp_total_cost
//
static int  getVf4_pp_total_cost_3Double( METHOD_message_t*  msg, va_list  args )
{
    int ifail = 0;

    va_list  largs;
    va_copy( largs, args );

    va_arg( largs, tag_t );
    double* value  = va_arg( largs, double* );
    logical* is_null = va_arg( largs, logical* );
    logical* is_empty = va_arg( largs, logical* );

    va_end( largs );

    tag_t  tagObject = NULLTAG;
    *value = 0;
    *is_null = false;
    *is_empty = false;
    METHOD_PROP_MESSAGE_OBJECT( msg, tagObject );
    BusinessObjectRef< ::vf4vinfastextend::VF4_PieceCostForm >  owningObj( tagObject );
    ifail = ::vf4vinfastextend::VF4_PieceCostFormDelegate::getInstance().getVf4_pp_total_costBase( const_cast< ::vf4vinfastextend::VF4_PieceCostForm & >( *owningObj ), *value, *is_null );

    return  ifail;
}

//
//  Callback Function for Property Operation,    getVf4_pp_total_costDisplayableValues
//
static int  getVf4_pp_total_costDisplayableValues_4Strings( METHOD_message_t*  msg, va_list  args )
{
    int ifail = 0;

    va_list  largs;
    va_copy( largs, args );

    va_arg( largs, tag_t );
    int* n_values = va_arg( largs, int* );
    char*** values = va_arg( largs, char*** );

    va_end( largs );

    tag_t  tagObject = NULLTAG;
    *n_values = 0;
    *values = 0;
    METHOD_PROP_MESSAGE_OBJECT( msg, tagObject );
    BusinessObjectRef< ::vf4vinfastextend::VF4_PieceCostForm >  owningObj( tagObject );
    if( owningObj.isInstanceOf< ::vf4vinfastextend::VF4_PieceCostForm >() )
    {
        std::vector< std::string >  strings;
        ifail = ::vf4vinfastextend::VF4_PieceCostFormDelegate::getInstance().getVf4_pp_total_costDisplayableValuesBase( const_cast< ::vf4vinfastextend::VF4_PieceCostForm & >( *owningObj ), strings );
        if( ifail == 0 )
        {
            *n_values = static_cast< int >( strings.size() );
            *values = (char**)MEM_alloc( (*n_values) * sizeof(char*) );
            for( int inx = 0; inx < (*n_values); ++inx )
            {
                std::string  aString = strings[inx];
                (*values)[inx] = MEM_string_copy( aString.c_str() );
            }
        }
    }

    return  ifail;
}

//
//  Callback Function for Property Operation,    getVf4_pto_total_cost
//
static int  getVf4_pto_total_cost_5Double( METHOD_message_t*  msg, va_list  args )
{
    int ifail = 0;

    va_list  largs;
    va_copy( largs, args );

    va_arg( largs, tag_t );
    double* value  = va_arg( largs, double* );
    logical* is_null = va_arg( largs, logical* );
    logical* is_empty = va_arg( largs, logical* );

    va_end( largs );

    tag_t  tagObject = NULLTAG;
    *value = 0;
    *is_null = false;
    *is_empty = false;
    METHOD_PROP_MESSAGE_OBJECT( msg, tagObject );
    BusinessObjectRef< ::vf4vinfastextend::VF4_PieceCostForm >  owningObj( tagObject );
    ifail = ::vf4vinfastextend::VF4_PieceCostFormDelegate::getInstance().getVf4_pto_total_costBase( const_cast< ::vf4vinfastextend::VF4_PieceCostForm & >( *owningObj ), *value, *is_null );

    return  ifail;
}

//
//  Callback Function for Property Operation,    getVf4_pto_total_costDisplayableValues
//
static int  getVf4_pto_total_costDisplayableValues_6Strings( METHOD_message_t*  msg, va_list  args )
{
    int ifail = 0;

    va_list  largs;
    va_copy( largs, args );

    va_arg( largs, tag_t );
    int* n_values = va_arg( largs, int* );
    char*** values = va_arg( largs, char*** );

    va_end( largs );

    tag_t  tagObject = NULLTAG;
    *n_values = 0;
    *values = 0;
    METHOD_PROP_MESSAGE_OBJECT( msg, tagObject );
    BusinessObjectRef< ::vf4vinfastextend::VF4_PieceCostForm >  owningObj( tagObject );
    if( owningObj.isInstanceOf< ::vf4vinfastextend::VF4_PieceCostForm >() )
    {
        std::vector< std::string >  strings;
        ifail = ::vf4vinfastextend::VF4_PieceCostFormDelegate::getInstance().getVf4_pto_total_costDisplayableValuesBase( const_cast< ::vf4vinfastextend::VF4_PieceCostForm & >( *owningObj ), strings );
        if( ifail == 0 )
        {
            *n_values = static_cast< int >( strings.size() );
            *values = (char**)MEM_alloc( (*n_values) * sizeof(char*) );
            for( int inx = 0; inx < (*n_values); ++inx )
            {
                std::string  aString = strings[inx];
                (*values)[inx] = MEM_string_copy( aString.c_str() );
            }
        }
    }

    return  ifail;
}

//
//  Callback Function for Property Operation,    getVf4_total_cost
//
static int  getVf4_total_cost_7Double( METHOD_message_t*  msg, va_list  args )
{
    int ifail = 0;

    va_list  largs;
    va_copy( largs, args );

    va_arg( largs, tag_t );
    double* value  = va_arg( largs, double* );
    logical* is_null = va_arg( largs, logical* );
    logical* is_empty = va_arg( largs, logical* );

    va_end( largs );

    tag_t  tagObject = NULLTAG;
    *value = 0;
    *is_null = false;
    *is_empty = false;
    METHOD_PROP_MESSAGE_OBJECT( msg, tagObject );
    BusinessObjectRef< ::vf4vinfastextend::VF4_PieceCostForm >  owningObj( tagObject );
    ifail = ::vf4vinfastextend::VF4_PieceCostFormDelegate::getInstance().getVf4_total_costBase( const_cast< ::vf4vinfastextend::VF4_PieceCostForm & >( *owningObj ), *value, *is_null );

    return  ifail;
}

//
//  Callback Function for Property Operation,    getVf4_total_costDisplayableValues
//
static int  getVf4_total_costDisplayableValues_8Strings( METHOD_message_t*  msg, va_list  args )
{
    int ifail = 0;

    va_list  largs;
    va_copy( largs, args );

    va_arg( largs, tag_t );
    int* n_values = va_arg( largs, int* );
    char*** values = va_arg( largs, char*** );

    va_end( largs );

    tag_t  tagObject = NULLTAG;
    *n_values = 0;
    *values = 0;
    METHOD_PROP_MESSAGE_OBJECT( msg, tagObject );
    BusinessObjectRef< ::vf4vinfastextend::VF4_PieceCostForm >  owningObj( tagObject );
    if( owningObj.isInstanceOf< ::vf4vinfastextend::VF4_PieceCostForm >() )
    {
        std::vector< std::string >  strings;
        ifail = ::vf4vinfastextend::VF4_PieceCostFormDelegate::getInstance().getVf4_total_costDisplayableValuesBase( const_cast< ::vf4vinfastextend::VF4_PieceCostForm & >( *owningObj ), strings );
        if( ifail == 0 )
        {
            *n_values = static_cast< int >( strings.size() );
            *values = (char**)MEM_alloc( (*n_values) * sizeof(char*) );
            for( int inx = 0; inx < (*n_values); ++inx )
            {
                std::string  aString = strings[inx];
                (*values)[inx] = MEM_string_copy( aString.c_str() );
            }
        }
    }

    return  ifail;
}

//
//  Callback Function for Property Operation,    setVf4_total_cost
//
static int  setVf4_total_cost_9Double( METHOD_message_t*  msg, va_list  args )
{
    int ifail = 0;

    va_list  largs;
    va_copy( largs, args );

    va_arg( largs, tag_t );
    const double value = va_arg( largs, double );
    logical is_null = va_arg( largs, int ) != 0;

    va_end( largs );

    tag_t  tagObject = NULLTAG;
    METHOD_PROP_MESSAGE_OBJECT( msg, tagObject );
    BusinessObjectRef< ::vf4vinfastextend::VF4_PieceCostForm >  owningObj( tagObject );
    ifail = ::vf4vinfastextend::VF4_PieceCostFormDelegate::getInstance().setVf4_total_costBase( const_cast< ::vf4vinfastextend::VF4_PieceCostForm & >( *owningObj ), value, is_null );

    return  ifail;
}





///
/// Getter for a Double Property
/// @version Teamcenter 11.3
/// @param value - Parameter Value
/// @param isNull - Returns true if the Parameter value is null
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_ls_total_cost( double &value, bool &isNull ) const
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_ls_total_cost" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        ResultCheck  rStat = 0;
        //  look up the OperationDispatcher and cache it
        static int  propMsgIndex = -1;
        BusinessObjectRef< ::Teamcenter::BusinessObject >  owningObj( m_busObj->getTag() );
        ::Teamcenter::OperationDispatcher*  pOprnDisp = 0;
        rStat = TCTYPE_find_prop_operation_dispatcher( owningObj->getTypeTag(), "vf4_ls_total_cost", PROP_ask_value_double_msg, &propMsgIndex, &pOprnDisp );
        ::Teamcenter::getDoubleOperationDispatcher*  pSubPropOprnDisp = static_cast< ::Teamcenter::getDoubleOperationDispatcher* >( pOprnDisp );
        if( pSubPropOprnDisp != 0 )
        {
            //  Base Action
            pSubPropOprnDisp->getDoubleBaseAction( owningObj, value, isNull  );
            pSubPropOprnDisp->getDoublePostActions( owningObj, value, isNull  );
        }
    }
    catch( const IFail& ex )
    {
        ifail = ex.ifail();
    }

    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_double_out( "value", value );
        JOURNAL_logical_out( "isNull", isNull );
        JOURNAL_return_value( ifail );
        JOURNAL_routine_end();
    }
    return  ifail;
}


///
/// Getter for Property Display Value
/// @version Teamcenter 11.3
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_ls_total_costDisplayableValues( std::vector< std::string > &values )
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_ls_total_costDisplayableValues" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        ResultCheck  rStat = 0;
        //  look up the OperationDispatcher and cache it
        static int  propMsgIndex = -1;
        BusinessObjectRef< ::Teamcenter::BusinessObject >  owningObj( m_busObj->getTag() );
        ::Teamcenter::OperationDispatcher*  pOprnDisp = 0;
        rStat = TCTYPE_find_prop_operation_dispatcher( owningObj->getTypeTag(), "vf4_ls_total_cost", PROP_ask_displayable_values_msg, &propMsgIndex, &pOprnDisp );
        ::Teamcenter::getDisplayableValuesOperationDispatcher*  pSubPropOprnDisp = static_cast< ::Teamcenter::getDisplayableValuesOperationDispatcher* >( pOprnDisp );
        if( pSubPropOprnDisp != 0 )
        {
            //  Base Action
            pSubPropOprnDisp->getDisplayableValuesBaseAction( owningObj, values  );
            pSubPropOprnDisp->getDisplayableValuesPostActions( owningObj, values  );
        }
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
int  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_pp_total_cost( double &value, bool &isNull ) const
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_pp_total_cost" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        ResultCheck  rStat = 0;
        //  look up the OperationDispatcher and cache it
        static int  propMsgIndex = -1;
        BusinessObjectRef< ::Teamcenter::BusinessObject >  owningObj( m_busObj->getTag() );
        ::Teamcenter::OperationDispatcher*  pOprnDisp = 0;
        rStat = TCTYPE_find_prop_operation_dispatcher( owningObj->getTypeTag(), "vf4_pp_total_cost", PROP_ask_value_double_msg, &propMsgIndex, &pOprnDisp );
        ::Teamcenter::getDoubleOperationDispatcher*  pSubPropOprnDisp = static_cast< ::Teamcenter::getDoubleOperationDispatcher* >( pOprnDisp );
        if( pSubPropOprnDisp != 0 )
        {
            //  Base Action
            pSubPropOprnDisp->getDoubleBaseAction( owningObj, value, isNull  );
            pSubPropOprnDisp->getDoublePostActions( owningObj, value, isNull  );
        }
    }
    catch( const IFail& ex )
    {
        ifail = ex.ifail();
    }

    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_double_out( "value", value );
        JOURNAL_logical_out( "isNull", isNull );
        JOURNAL_return_value( ifail );
        JOURNAL_routine_end();
    }
    return  ifail;
}


///
/// Getter for Property Display Value
/// @version Teamcenter 11.3
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_pp_total_costDisplayableValues( std::vector< std::string > &values )
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_pp_total_costDisplayableValues" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        ResultCheck  rStat = 0;
        //  look up the OperationDispatcher and cache it
        static int  propMsgIndex = -1;
        BusinessObjectRef< ::Teamcenter::BusinessObject >  owningObj( m_busObj->getTag() );
        ::Teamcenter::OperationDispatcher*  pOprnDisp = 0;
        rStat = TCTYPE_find_prop_operation_dispatcher( owningObj->getTypeTag(), "vf4_pp_total_cost", PROP_ask_displayable_values_msg, &propMsgIndex, &pOprnDisp );
        ::Teamcenter::getDisplayableValuesOperationDispatcher*  pSubPropOprnDisp = static_cast< ::Teamcenter::getDisplayableValuesOperationDispatcher* >( pOprnDisp );
        if( pSubPropOprnDisp != 0 )
        {
            //  Base Action
            pSubPropOprnDisp->getDisplayableValuesBaseAction( owningObj, values  );
            pSubPropOprnDisp->getDisplayableValuesPostActions( owningObj, values  );
        }
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
int  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_pto_total_cost( double &value, bool &isNull ) const
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_pto_total_cost" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        ResultCheck  rStat = 0;
        //  look up the OperationDispatcher and cache it
        static int  propMsgIndex = -1;
        BusinessObjectRef< ::Teamcenter::BusinessObject >  owningObj( m_busObj->getTag() );
        ::Teamcenter::OperationDispatcher*  pOprnDisp = 0;
        rStat = TCTYPE_find_prop_operation_dispatcher( owningObj->getTypeTag(), "vf4_pto_total_cost", PROP_ask_value_double_msg, &propMsgIndex, &pOprnDisp );
        ::Teamcenter::getDoubleOperationDispatcher*  pSubPropOprnDisp = static_cast< ::Teamcenter::getDoubleOperationDispatcher* >( pOprnDisp );
        if( pSubPropOprnDisp != 0 )
        {
            //  Base Action
            pSubPropOprnDisp->getDoubleBaseAction( owningObj, value, isNull  );
            pSubPropOprnDisp->getDoublePostActions( owningObj, value, isNull  );
        }
    }
    catch( const IFail& ex )
    {
        ifail = ex.ifail();
    }

    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_double_out( "value", value );
        JOURNAL_logical_out( "isNull", isNull );
        JOURNAL_return_value( ifail );
        JOURNAL_routine_end();
    }
    return  ifail;
}


///
/// Getter for Property Display Value
/// @version Teamcenter 11.3
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_pto_total_costDisplayableValues( std::vector< std::string > &values )
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_pto_total_costDisplayableValues" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        ResultCheck  rStat = 0;
        //  look up the OperationDispatcher and cache it
        static int  propMsgIndex = -1;
        BusinessObjectRef< ::Teamcenter::BusinessObject >  owningObj( m_busObj->getTag() );
        ::Teamcenter::OperationDispatcher*  pOprnDisp = 0;
        rStat = TCTYPE_find_prop_operation_dispatcher( owningObj->getTypeTag(), "vf4_pto_total_cost", PROP_ask_displayable_values_msg, &propMsgIndex, &pOprnDisp );
        ::Teamcenter::getDisplayableValuesOperationDispatcher*  pSubPropOprnDisp = static_cast< ::Teamcenter::getDisplayableValuesOperationDispatcher* >( pOprnDisp );
        if( pSubPropOprnDisp != 0 )
        {
            //  Base Action
            pSubPropOprnDisp->getDisplayableValuesBaseAction( owningObj, values  );
            pSubPropOprnDisp->getDisplayableValuesPostActions( owningObj, values  );
        }
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
int  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_total_cost( double &value, bool &isNull ) const
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_total_cost" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        ResultCheck  rStat = 0;
        //  look up the OperationDispatcher and cache it
        static int  propMsgIndex = -1;
        BusinessObjectRef< ::Teamcenter::BusinessObject >  owningObj( m_busObj->getTag() );
        ::Teamcenter::OperationDispatcher*  pOprnDisp = 0;
        rStat = TCTYPE_find_prop_operation_dispatcher( owningObj->getTypeTag(), "vf4_total_cost", PROP_ask_value_double_msg, &propMsgIndex, &pOprnDisp );
        ::Teamcenter::getDoubleOperationDispatcher*  pSubPropOprnDisp = static_cast< ::Teamcenter::getDoubleOperationDispatcher* >( pOprnDisp );
        if( pSubPropOprnDisp != 0 )
        {
            //  Base Action
            pSubPropOprnDisp->getDoubleBaseAction( owningObj, value, isNull  );
            pSubPropOprnDisp->getDoublePostActions( owningObj, value, isNull  );
        }
    }
    catch( const IFail& ex )
    {
        ifail = ex.ifail();
    }

    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_double_out( "value", value );
        JOURNAL_logical_out( "isNull", isNull );
        JOURNAL_return_value( ifail );
        JOURNAL_routine_end();
    }
    return  ifail;
}


///
/// Getter for Property Display Value
/// @version Teamcenter 11.3
/// @param values - Parameter value
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_total_costDisplayableValues( std::vector< std::string > &values )
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormGenImpl::getVf4_total_costDisplayableValues" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        ResultCheck  rStat = 0;
        //  look up the OperationDispatcher and cache it
        static int  propMsgIndex = -1;
        BusinessObjectRef< ::Teamcenter::BusinessObject >  owningObj( m_busObj->getTag() );
        ::Teamcenter::OperationDispatcher*  pOprnDisp = 0;
        rStat = TCTYPE_find_prop_operation_dispatcher( owningObj->getTypeTag(), "vf4_total_cost", PROP_ask_displayable_values_msg, &propMsgIndex, &pOprnDisp );
        ::Teamcenter::getDisplayableValuesOperationDispatcher*  pSubPropOprnDisp = static_cast< ::Teamcenter::getDisplayableValuesOperationDispatcher* >( pOprnDisp );
        if( pSubPropOprnDisp != 0 )
        {
            //  Base Action
            pSubPropOprnDisp->getDisplayableValuesBaseAction( owningObj, values  );
        }
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
/// Setter for a Double Property
/// @version Teamcenter 11.3
/// @param value - Value to be set for the parameter
/// @param isNull - If true, set the parameter value to null
/// @return - Status. 0 if successful
///
int  ::vf4vinfastextend::VF4_PieceCostFormGenImpl::setVf4_total_cost( double value, bool isNull )
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormGenImpl::setVf4_total_cost" );
        JOURNAL_double_in( value );
        JOURNAL_logical_in( isNull );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        ResultCheck  rStat = 0;
        //  look up the OperationDispatcher and cache it
        static int  propMsgIndex = -1;
        BusinessObjectRef< ::Teamcenter::BusinessObject >  owningObj( m_busObj->getTag() );
        ::Teamcenter::OperationDispatcher*  pOprnDisp = 0;
        rStat = TCTYPE_find_prop_operation_dispatcher( owningObj->getTypeTag(), "vf4_total_cost", PROP_set_value_double_msg, &propMsgIndex, &pOprnDisp );
        ::Teamcenter::setDoubleOperationDispatcher*  pSubPropOprnDisp = static_cast< ::Teamcenter::setDoubleOperationDispatcher* >( pOprnDisp );
        if( pSubPropOprnDisp != 0 )
        {
            //  Base Action
            pSubPropOprnDisp->setDoubleBaseAction( owningObj, value, isNull  );
            pSubPropOprnDisp->setDoublePostActions( owningObj, value, isNull  );
        }
    }
    catch( const IFail& ex )
    {
        ifail = ex.ifail();
    }

    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_double_out( "value", value );
        JOURNAL_logical_out( "isNull", isNull );
        JOURNAL_return_value( ifail );
        JOURNAL_routine_end();
    }
    return  ifail;
}






    //  OperationDispatcher SubClass definitions

