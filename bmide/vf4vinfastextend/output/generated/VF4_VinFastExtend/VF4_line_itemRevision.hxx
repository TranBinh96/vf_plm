//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/*==================================================================================================
File description:
    This file contains the declaration for the Business Object, VF4_line_itemRevision
    Filename:   VF4_line_itemRevision.hxx
    Module:     VF4_VinFastExtend
    @BMIDE autogenerated
==================================================================================================*/

#ifndef VF4VINFASTEXTEND__VF4_LINE_ITEMREVISION__HXX
#define VF4VINFASTEXTEND__VF4_LINE_ITEMREVISION__HXX

#include <vector>
#include <common/tc_deprecation_macros.h>
#include <tccore/ItemRevision.hxx>

#include <VF4_VinFastExtend/libvf4_vinfastextend_exports.h>

namespace vf4vinfastextend
{
    class VF4_line_itemRevision;
}
namespace vf4vinfastextend
{
    class VF4_line_itemRevisionImpl;
}
namespace vf4vinfastextend
{
    class VF4_line_itemRevisionDelegate;
}
namespace vf4vinfastextend
{
    class VF4_line_itemRevisionDispatch;
}
namespace vf4vinfastextend
{
    class VF4_line_itemRevisionGenImpl;
}
namespace Teamcenter
{
    class RootObjectImpl;
}

class  VF4_VINFASTEXTEND_API  vf4vinfastextend::VF4_line_itemRevision
     : public  ::Teamcenter::ItemRevision
{
public:

    /**
     * Get the Interface Name
     * @return - return desc for getInterfaceName
     */
    static const std::string&  getInterfaceName();


    ///
    /// Getter for a Double Property
    /// @version ActiveWorkspace4.0
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf4_piece_cost_auto( double &value, bool &isNull ) const;

    ///
    /// Getter for Property Display Value
    /// @version ActiveWorkspace4.0
    /// @param values - Parameter value
    /// @return - Status. 0 if successful
    ///
    int  getVf4_piece_cost_autoDisplayableValues( std::vector< std::string > &values );
protected:
    //  Method to initialize
    virtual void initialize( ::Teamcenter::RootObjectImpl*  impl );

    /**
     * Constructor
     */
    VF4_line_itemRevision();

    /**
     * Destructor
     */
    virtual ~VF4_line_itemRevision();

private:
    //  Getter method for the Implementation Object
    VF4_line_itemRevisionImpl*  getVF4_line_itemRevisionImpl() const;
    //  Setter method for the Implementation Object
    ::Teamcenter::RootObjectImpl*  setVF4_line_itemRevisionImpl( ::Teamcenter::RootObjectImpl*  impl );

    //  Method to set the delete impl boolean
    void setDeleteImpl( bool  del );

    // Pointer to the Implementation object
    VF4_line_itemRevisionImpl*  m_vf4_line_itemrevisionImpl;

    //  Flag to indicate if Implementation object needs to be deleted
    bool  m_deleteImpl;

    /**
     * Name of the Business Object
     */
    static std::string  name;

    /**
     * Private default constructor. We do not want this class instantiated without the business object passed in.
     * @param VF4_line_itemRevision - desc for VF4_line_itemRevision parameter
     */
    VF4_line_itemRevision( const VF4_line_itemRevision& );
    VF4_line_itemRevision& operator=( const VF4_line_itemRevision& );

    friend class  VF4_line_itemRevisionDelegate;
    friend class  VF4_line_itemRevisionDispatch;
    friend class  VF4_line_itemRevisionGenImpl;
};

#include <VF4_VinFastExtend/libvf4_vinfastextend_undef.h>
#endif //   VF4VINFASTEXTEND__VF4_LINE_ITEMREVISION__HXX
