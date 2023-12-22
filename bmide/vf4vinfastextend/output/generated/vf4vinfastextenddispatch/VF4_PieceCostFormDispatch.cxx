//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/*==================================================================================================
File description:
    This file contains the implementation for the Business Object, VF4_PieceCostFormDispatch
    Filename:   VF4_PieceCostFormDispatch.cxx
    Module:     vf4vinfastextenddispatch
    @BMIDE autogenerated
==================================================================================================*/

#include <vf4vinfastextenddispatch/VF4_PieceCostFormDispatch.hxx>

#include <stdarg.h>

#include <foundationdispatch/BusinessObjectDispatch.hxx>
#include <metaframework/BusinessObjectRegistry.hxx>
#include <metaframework/BusinessObject.hxx>
#include <metaframework/BusinessObjectRef.hxx>
#include <base_utils/ResultCheck.hxx>
#include <base_utils/IFail.hxx>
#include <mld/journal/journal.h>

using  namespace  ::vf4vinfastextend;


::vf4vinfastextend::VF4_PieceCostFormDispatch*  ::vf4vinfastextend::VF4_PieceCostFormDispatch::m_dispatch = 0;


//----------------------------------------------------------------------------------
//  ::vf4vinfastextend::VF4_PieceCostFormDispatch::getInstance()
//  Get the singleton class instance
//----------------------------------------------------------------------------------
::vf4vinfastextend::VF4_PieceCostFormDispatch&  ::vf4vinfastextend::VF4_PieceCostFormDispatch::getInstance()
{
    if( 0 == m_dispatch )   //  Only allow one instance of the class to be generated.
    {
        m_dispatch = new  ::vf4vinfastextend::VF4_PieceCostFormDispatch;
        VF4_PieceCostFormDelegate::setInstance( m_dispatch );
    }
    return  *m_dispatch;
}

//----------------------------------------------------------------------------------
//  ::vf4vinfastextend::VF4_PieceCostFormDispatch::VF4_PieceCostFormDispatch()
//  Constructor for the class
//----------------------------------------------------------------------------------
::vf4vinfastextend::VF4_PieceCostFormDispatch::VF4_PieceCostFormDispatch():VF4_PieceCostFormDelegate()
{
}

//----------------------------------------------------------------------------------
//  ::vf4vinfastextend::VF4_PieceCostFormDispatch::~VF4_PieceCostFormDispatch()
//  Destructor for the class
//----------------------------------------------------------------------------------
::vf4vinfastextend::VF4_PieceCostFormDispatch::~VF4_PieceCostFormDispatch()
{
}

//----------------------------------------------------------------------------------
//  ::vf4vinfastextend::VF4_PieceCostFormDispatch::constructor()
//  Static constructor for the class
//----------------------------------------------------------------------------------
::Teamcenter::RootObject*  ::vf4vinfastextend::VF4_PieceCostFormDispatch::constructor()
{
    vf4vinfastextend::VF4_PieceCostForm*  busObj = new  vf4vinfastextend::VF4_PieceCostForm;
    return  busObj;
}

//----------------------------------------------------------------------------------
//  ::vf4vinfastextend::VF4_PieceCostFormDispatch::initializeClass()
//  Initialize the class
//----------------------------------------------------------------------------------
int  ::vf4vinfastextend::VF4_PieceCostFormDispatch::initializeClass()
{
    
    if( JOURNAL_is_journaling() )
    {
        JOURNAL_routine_start( "::vf4vinfastextend::VF4_PieceCostFormDispatch::initializeClass" );
        JOURNAL_routine_call();
    }
    int ifail = 0;

    try
    {
        static bool  initialized = false;
        if( !initialized )
        {
            initialized = true;
            getInstance();

            //  call parent class initialize
            ResultCheck  rStat = ::Teamcenter::FormDispatch::initializeClass();
            rStat = VF4_PieceCostFormDelegate::initializeClass();
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