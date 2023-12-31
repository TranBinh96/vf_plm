
//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/*====================================================================================================
File description:

    Filename: initializevf4vinfastextend.cxx
    Module  : vf4vinfastextenddispatch

    @BMIDE autogenerated
====================================================================================================*/

#include <vf4vinfastextenddispatch/initializevf4vinfastextend.hxx>
#include <metaframework/BusinessObjectRegistry.hxx>
#include <string>

#include <vf4vinfastextenddispatch/VF4_PieceCostFormDispatch.hxx>
#include <vf4vinfastextenddispatch/VF4_line_itemRevisionDispatch.hxx>

void initializevf4vinfastextend()
{
    vf4vinfastextend::initializevf4vinfastextendInternal();
}

void vf4vinfastextend::initializevf4vinfastextendInternal()
{
    // Register the Business Object constructor and initializeClass
    Teamcenter::BusinessObjectRegistry& boReg = Teamcenter::BusinessObjectRegistry::instance();
    std::string name;
    name = "VF4_PieceCostForm";
    boReg.initializeRegister( name, &vf4vinfastextend::VF4_PieceCostFormDispatch::constructor, 
                             &vf4vinfastextend::VF4_PieceCostFormDispatch::initializeClass);
    name = "VF4_line_itemRevision";
    boReg.initializeRegister( name, &vf4vinfastextend::VF4_line_itemRevisionDispatch::constructor, 
                             &vf4vinfastextend::VF4_line_itemRevisionDispatch::initializeClass);
}

