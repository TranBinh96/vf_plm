//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/*==================================================================================================
File description:
    This file contains the declaration for the Business Object, VF4_PieceCostFormGenImpl
    Filename:   VF4_PieceCostFormGenImpl.hxx
    Module:     VF4_VinFastExtend
    @BMIDE autogenerated
==================================================================================================*/

#ifndef VF4VINFASTEXTEND__VF4_PIECECOSTFORMGENIMPL_HXX
#define VF4VINFASTEXTEND__VF4_PIECECOSTFORMGENIMPL_HXX

#include <common/tc_deprecation_macros.h>
#include <VF4_VinFastExtend/VF4_PieceCostForm.hxx>
#include <metaframework/RootObjectImpl.hxx>
#include <unidefs.h>

#include <extensionframework/getDoubleOperationDispatcher.hxx>
#include <extensionframework/getDisplayableValuesOperationDispatcher.hxx>
#include <extensionframework/setDoubleOperationDispatcher.hxx>

#include <cxpom/attributeaccessor.hxx>
#include <VF4_VinFastExtend/libvf4_vinfastextend_exports.h>

namespace vf4vinfastextend
{
    class VF4_PieceCostFormGenImpl;
}
namespace vf4vinfastextend
{
    class VF4_PieceCostForm;
}
namespace vf4vinfastextend
{
    class VF4_PieceCostFormDelegate;
}

class  VF4_VINFASTEXTEND_API  vf4vinfastextend::VF4_PieceCostFormGenImpl : public ::Teamcenter::RootObjectImpl
{
public:
    //  Accessor for the Interface Object
    ::vf4vinfastextend::VF4_PieceCostForm*  getVF4_PieceCostForm() const;

    



    ///
    /// Getter for a Double Property
    /// @version Teamcenter 11.3
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf4_ls_total_cost( double &value, bool &isNull ) const;

    ///
    /// Getter for Property Display Value
    /// @version Teamcenter 11.3
    /// @param values - Parameter value
    /// @return - Status. 0 if successful
    ///
    int  getVf4_ls_total_costDisplayableValues( std::vector< std::string > &values ); //lint -e1762

    ///
    /// Getter for a Double Property
    /// @version Teamcenter 11.3
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf4_pp_total_cost( double &value, bool &isNull ) const;

    ///
    /// Getter for Property Display Value
    /// @version Teamcenter 11.3
    /// @param values - Parameter value
    /// @return - Status. 0 if successful
    ///
    int  getVf4_pp_total_costDisplayableValues( std::vector< std::string > &values ); //lint -e1762

    ///
    /// Getter for a Double Property
    /// @version Teamcenter 11.3
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf4_pto_total_cost( double &value, bool &isNull ) const;

    ///
    /// Getter for Property Display Value
    /// @version Teamcenter 11.3
    /// @param values - Parameter value
    /// @return - Status. 0 if successful
    ///
    int  getVf4_pto_total_costDisplayableValues( std::vector< std::string > &values ); //lint -e1762

    ///
    /// Getter for a Double Property
    /// @version Teamcenter 11.3
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf4_total_cost( double &value, bool &isNull ) const;

    ///
    /// Getter for Property Display Value
    /// @version Teamcenter 11.3
    /// @param values - Parameter value
    /// @return - Status. 0 if successful
    ///
    int  getVf4_total_costDisplayableValues( std::vector< std::string > &values ); //lint -e1762

    ///
    /// Setter for a Double Property
    /// @version Teamcenter 11.3
    /// @param value - Value to be set for the parameter
    /// @param isNull - If true, set the parameter value to null
    /// @return - Status. 0 if successful
    ///
    int  setVf4_total_cost( double value, bool isNull ); //lint -e1762

    //  OperationDispatcher SubClass declarations



protected:
    //  Constructor for a VF4_PieceCostForm Object
    explicit VF4_PieceCostFormGenImpl( ::vf4vinfastextend::VF4_PieceCostForm&  busObj );
    virtual ~VF4_PieceCostFormGenImpl();

    //  Method to initialize this Class
    static int initializeClass();

    //  Method to create a new Instance
    static ::Teamcenter::RootObjectImpl*  newInstance();
    static int registerPropertyFunctions();

private:
    //  Private default constructor. We do not want this class instantiated without the business object passed in.
    VF4_PieceCostFormGenImpl();

    //  Pointer to the Interface Object
    ::vf4vinfastextend::VF4_PieceCostForm*  m_busObj;

    //  Constructor for a VF4_PieceCostFormGenImpl Object
    VF4_PieceCostFormGenImpl( const VF4_PieceCostFormGenImpl& );

    //  Copy Constructor
    VF4_PieceCostFormGenImpl& operator=( const VF4_PieceCostFormGenImpl& );

    friend class  ::vf4vinfastextend::VF4_PieceCostFormDelegate;
};



#include <VF4_VinFastExtend/libvf4_vinfastextend_undef.h>
#endif  //  VF4VINFASTEXTEND__VF4_PIECECOSTFORMGENIMPL_HXX