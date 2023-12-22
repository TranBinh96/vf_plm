//@<COPYRIGHT>@
//==================================================
//Copyright $2021.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

// 
//  @file
//  This file contains the declaration for the Business Object VF4_line_itemRevisionImpl
//

#ifndef VF4VINFASTEXTEND__VF4_LINE_ITEMREVISIONIMPL_HXX
#define VF4VINFASTEXTEND__VF4_LINE_ITEMREVISIONIMPL_HXX

#include <VF4_VinFastExtend/VF4_line_itemRevisionGenImpl.hxx>

#include <VF4_VinFastExtend/libvf4_vinfastextend_exports.h>


namespace vf4vinfastextend
{
    class VF4_line_itemRevisionImpl; 
    class VF4_line_itemRevisionDelegate;
}

class  VF4_VINFASTEXTEND_API vf4vinfastextend::VF4_line_itemRevisionImpl
    : public vf4vinfastextend::VF4_line_itemRevisionGenImpl
{
public:

    ///
    /// Getter for a Double Property
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf4_piece_cost_autoBase( double &value, bool &isNull ) const;

    ///
    /// Getter for Property Display Value
    /// @param values - Parameter value
    /// @return - Status. 0 if successful
    ///
    int  getVf4_piece_cost_autoDisplayableValuesBase( std::vector< std::string > &values );

    double calculateTotalCost() const;

protected:
    ///
    /// Constructor for a VF4_line_itemRevision
    explicit VF4_line_itemRevisionImpl( VF4_line_itemRevision& busObj );

    ///
    /// Destructor
    virtual ~VF4_line_itemRevisionImpl();

private:
    ///
    /// Default Constructor for the class
    VF4_line_itemRevisionImpl();
    
    ///
    /// Private default constructor. We do not want this class instantiated without the business object passed in.
    VF4_line_itemRevisionImpl( const VF4_line_itemRevisionImpl& );

    ///
    /// Copy constructor
    VF4_line_itemRevisionImpl& operator=( const VF4_line_itemRevisionImpl& );

    ///
    /// Method to initialize this Class
    static int initializeClass();

    ///
    ///static data
    friend class vf4vinfastextend::VF4_line_itemRevisionDelegate;

};

#include <VF4_VinFastExtend/libvf4_vinfastextend_undef.h>
#endif // VF4VINFASTEXTEND__VF4_LINE_ITEMREVISIONIMPL_HXX
