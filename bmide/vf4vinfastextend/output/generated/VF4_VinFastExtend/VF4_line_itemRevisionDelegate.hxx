//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/*==================================================================================================
File description:
    This file contains the declaration for the Business Object, VF4_line_itemRevisionDelegate
    Filename:   VF4_line_itemRevisionDelegate.hxx
    Module:     VF4_VinFastExtend
    @BMIDE autogenerated
==================================================================================================*/

#ifndef VF4VINFASTEXTEND__VF4_LINE_ITEMREVISIONDELEGATE_HXX
#define VF4VINFASTEXTEND__VF4_LINE_ITEMREVISIONDELEGATE_HXX

#include <common/tc_deprecation_macros.h>
#include <VF4_VinFastExtend/VF4_line_itemRevision.hxx>

#include <VF4_VinFastExtend/libvf4_vinfastextend_exports.h>

namespace vf4vinfastextend
{
    class VF4_line_itemRevisionDelegate;
}
namespace vf4vinfastextend
{
    class VF4_line_itemRevisionImpl;
}
namespace vf4vinfastextend
{
    class VF4_line_itemRevision;
}
namespace vf4vinfastextend
{
    class VF4_line_itemRevisionGenImpl;
}

namespace vf4vinfastextend_alias = ::vf4vinfastextend;


class  VF4_VINFASTEXTEND_API vf4vinfastextend_alias::VF4_line_itemRevisionDelegate
{
public:
    //  Method to initialize this Class
    static int initializeClass();

    //  Method to get the VF4_line_itemRevisionDelegate instance
    static VF4_line_itemRevisionDelegate&  getInstance();

    //  Method to create the VF4_line_itemRevisionImpl pointer
    static VF4_line_itemRevisionImpl*  createImpl( VF4_line_itemRevision&  ifObj );

    //  Method to delete the VF4_line_itemRevisionImpl pointer
    static void deleteImpl( VF4_line_itemRevisionImpl*  impl );


    ///
    /// Getter for a Double Property
    /// @version ActiveWorkspace4.0
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf4_piece_cost_autoBase(const VF4_line_itemRevision &ifObj,  double &value, bool &isNull ) const;


    ///
    /// Getter for Property Display Value
    /// @version ActiveWorkspace4.0
    /// @param values - Parameter value
    /// @return - Status. 0 if successful
    ///
    int  getVf4_piece_cost_autoDisplayableValuesBase(VF4_line_itemRevision &ifObj,  std::vector< std::string > &values );



protected:
    //  Pointer to the instance
    static VF4_line_itemRevisionDelegate*  m_pInstance;

    VF4_line_itemRevisionDelegate();
    virtual ~VF4_line_itemRevisionDelegate();
    static void setInstance( VF4_line_itemRevisionDelegate*  instance );


private:
    friend class  ::vf4vinfastextend::VF4_line_itemRevisionGenImpl;

};

#include <VF4_VinFastExtend/libvf4_vinfastextend_undef.h>
#endif  //  VF4VINFASTEXTEND__VF4_LINE_ITEMREVISIONDELEGATE_HXX