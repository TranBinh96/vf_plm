//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/*==================================================================================================
File description:
    This file contains the declaration for the Business Object, VF4_PieceCostFormDispatch
    Filename:   VF4_PieceCostFormDispatch.hxx
    Module:     vf4vinfastextenddispatch
    @BMIDE autogenerated
==================================================================================================*/

#ifndef VF4VINFASTEXTEND__VF4_PIECECOSTFORMDISPATCH_HXX
#define VF4VINFASTEXTEND__VF4_PIECECOSTFORMDISPATCH_HXX

#include <vector>
#include <VF4_VinFastExtend/VF4_PieceCostForm.hxx>
#include <VF4_VinFastExtend/VF4_PieceCostFormDelegate.hxx>
#include <foundationdispatch/FormDispatch.hxx>
#include <vf4vinfastextenddispatch/initializevf4vinfastextend.hxx>

#include <vf4vinfastextenddispatch/libvf4vinfastextenddispatch_exports.h>

namespace vf4vinfastextend
{
    class VF4_PieceCostFormDispatch;
}
namespace vf4vinfastextend
{
    class VF4_PieceCostFormDelegate;
}
namespace Teamcenter
{
    class BusinessObjectImpl;
}

class  VF4VINFASTEXTENDDISPATCH_API  vf4vinfastextend::VF4_PieceCostFormDispatch
    : public  vf4vinfastextend::VF4_PieceCostFormDelegate
{
public:
    //  Method to initialize this Class
    static int initializeClass();

    //  Method to get the VF4_PieceCostFormDispatch instance
    static VF4_PieceCostFormDispatch&  getInstance();


private:
    //  Pointer to the dispatch instance
    static VF4_PieceCostFormDispatch*  m_dispatch;

    //  Default Constructor
    VF4_PieceCostFormDispatch();

    //  Default Destructor
    virtual ~VF4_PieceCostFormDispatch();

    //  Constructor Method
    static ::Teamcenter::RootObject*  constructor();

    //  Copy Constructor for a VF4_PieceCostFormDispatch
    VF4_PieceCostFormDispatch( const VF4_PieceCostFormDispatch& );

    //  Assignment operator
    VF4_PieceCostFormDispatch&  operator = ( const VF4_PieceCostFormDispatch& );

    //  friend
    friend void   ::vf4vinfastextend::initializevf4vinfastextendInternal();
    friend void   ::vf4vinfastextend::initializevf4vinfastextendExtenders();
    friend class  vf4vinfastextend::VF4_PieceCostFormDelegate;
};

#include <vf4vinfastextenddispatch/libvf4vinfastextenddispatch_undef.h>
#endif  //  VF4VINFASTEXTEND__VF4_PIECECOSTFORMDISPATCH_HXX