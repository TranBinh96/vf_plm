//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/*==================================================================================================
File description:
    This file contains the declaration for the Business Object, VF4_line_itemRevisionDispatch
    Filename:   VF4_line_itemRevisionDispatch.hxx
    Module:     vf4vinfastextenddispatch
    @BMIDE autogenerated
==================================================================================================*/

#ifndef VF4VINFASTEXTEND__VF4_LINE_ITEMREVISIONDISPATCH_HXX
#define VF4VINFASTEXTEND__VF4_LINE_ITEMREVISIONDISPATCH_HXX

#include <vector>
#include <VF4_VinFastExtend/VF4_line_itemRevision.hxx>
#include <VF4_VinFastExtend/VF4_line_itemRevisionDelegate.hxx>
#include <foundationdispatch/ItemRevisionDispatch.hxx>
#include <vf4vinfastextenddispatch/initializevf4vinfastextend.hxx>

#include <vf4vinfastextenddispatch/libvf4vinfastextenddispatch_exports.h>

namespace vf4vinfastextend
{
    class VF4_line_itemRevisionDispatch;
}
namespace vf4vinfastextend
{
    class VF4_line_itemRevisionDelegate;
}
namespace Teamcenter
{
    class BusinessObjectImpl;
}

class  VF4VINFASTEXTENDDISPATCH_API  vf4vinfastextend::VF4_line_itemRevisionDispatch
    : public  vf4vinfastextend::VF4_line_itemRevisionDelegate
{
public:
    //  Method to initialize this Class
    static int initializeClass();

    //  Method to get the VF4_line_itemRevisionDispatch instance
    static VF4_line_itemRevisionDispatch&  getInstance();


private:
    //  Pointer to the dispatch instance
    static VF4_line_itemRevisionDispatch*  m_dispatch;

    //  Default Constructor
    VF4_line_itemRevisionDispatch();

    //  Default Destructor
    virtual ~VF4_line_itemRevisionDispatch();

    //  Constructor Method
    static ::Teamcenter::RootObject*  constructor();

    //  Copy Constructor for a VF4_line_itemRevisionDispatch
    VF4_line_itemRevisionDispatch( const VF4_line_itemRevisionDispatch& );

    //  Assignment operator
    VF4_line_itemRevisionDispatch&  operator = ( const VF4_line_itemRevisionDispatch& );

    //  friend
    friend void   ::vf4vinfastextend::initializevf4vinfastextendInternal();
    friend void   ::vf4vinfastextend::initializevf4vinfastextendExtenders();
    friend class  vf4vinfastextend::VF4_line_itemRevisionDelegate;
};

#include <vf4vinfastextenddispatch/libvf4vinfastextenddispatch_undef.h>
#endif  //  VF4VINFASTEXTEND__VF4_LINE_ITEMREVISIONDISPATCH_HXX
