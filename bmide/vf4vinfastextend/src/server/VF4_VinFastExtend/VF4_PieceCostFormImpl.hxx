//@<COPYRIGHT>@
//==================================================
//Copyright $2021.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

// 
//  @file
//  This file contains the declaration for the Business Object VF4_PieceCostFormImpl
//

#ifndef VF4VINFASTEXTEND__VF4_PIECECOSTFORMIMPL_HXX
#define VF4VINFASTEXTEND__VF4_PIECECOSTFORMIMPL_HXX

#include <VF4_VinFastExtend/VF4_PieceCostFormGenImpl.hxx>

#include <VF4_VinFastExtend/libvf4_vinfastextend_exports.h>


namespace vf4vinfastextend
{
    class VF4_PieceCostFormImpl; 
    class VF4_PieceCostFormDelegate;
}

class  VF4_VINFASTEXTEND_API vf4vinfastextend::VF4_PieceCostFormImpl
    : public vf4vinfastextend::VF4_PieceCostFormGenImpl
{
public:

    ///
    /// Getter for a Double Property
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf4_total_costBase( double &value, bool &isNull ) const;

    ///
    /// Getter for Property Display Value
    /// @param values - Parameter value
    /// @return - Status. 0 if successful
    ///
    int  getVf4_total_costDisplayableValuesBase( std::vector< std::string > &values );

    ///
    /// Setter for a Double Property
    /// @param value - Value to be set for the parameter
    /// @param isNull - If true, set the parameter value to null
    /// @return - Status. 0 if successful
    ///
    int  setVf4_total_costBase( double value, bool isNull );

    ///
    /// Getter for a Double Property
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf4_ls_total_costBase( double &value, bool &isNull ) const;

    ///
    /// Getter for Property Display Value
    /// @param values - Parameter value
    /// @return - Status. 0 if successful
    ///
    int  getVf4_ls_total_costDisplayableValuesBase( std::vector< std::string > &values );

    ///
    /// Getter for a Double Property
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf4_pp_total_costBase( double &value, bool &isNull ) const;

    ///
    /// Getter for Property Display Value
    /// @param values - Parameter value
    /// @return - Status. 0 if successful
    ///
    int  getVf4_pp_total_costDisplayableValuesBase( std::vector< std::string > &values );

    ///
    /// Getter for a Double Property
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf4_pto_total_costBase( double &value, bool &isNull ) const;

    ///
    /// Getter for Property Display Value
    /// @param values - Parameter value
    /// @return - Status. 0 if successful
    ///
    int  getVf4_pto_total_costDisplayableValuesBase( std::vector< std::string > &values );

    double calculateTotalCost() const;
    double calculateLsTotalCost() const;
    double calculatePpTotalCost() const;
    double calculatePtoTotalCost() const;
protected:
    ///
    /// Constructor for a VF4_PieceCostForm
    explicit VF4_PieceCostFormImpl( VF4_PieceCostForm& busObj );

    ///
    /// Destructor
    virtual ~VF4_PieceCostFormImpl();

private:
    ///
    /// Default Constructor for the class
    VF4_PieceCostFormImpl();
    
    ///
    /// Private default constructor. We do not want this class instantiated without the business object passed in.
    VF4_PieceCostFormImpl( const VF4_PieceCostFormImpl& );

    ///
    /// Copy constructor
    VF4_PieceCostFormImpl& operator=( const VF4_PieceCostFormImpl& );

    ///
    /// Method to initialize this Class
    static int initializeClass();

    ///
    ///static data
    friend class vf4vinfastextend::VF4_PieceCostFormDelegate;

};

#include <VF4_VinFastExtend/libvf4_vinfastextend_undef.h>
#endif // VF4VINFASTEXTEND__VF4_PIECECOSTFORMIMPL_HXX
