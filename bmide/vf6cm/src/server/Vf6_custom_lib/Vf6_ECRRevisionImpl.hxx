//@<COPYRIGHT>@
//==================================================
//Copyright $2019.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

// 
//  @file
//  This file contains the declaration for the Business Object Vf6_ECRRevisionImpl
//

#ifndef VF6CM__VF6_ECRREVISIONIMPL_HXX
#define VF6CM__VF6_ECRREVISIONIMPL_HXX

#include <Vf6_custom_lib/Vf6_ECRRevisionGenImpl.hxx>

#include <Vf6_custom_lib/libvf6_custom_lib_exports.h>


namespace vf6cm
{
    class Vf6_ECRRevisionImpl; 
    class Vf6_ECRRevisionDelegate;
}

class  VF6_CUSTOM_LIB_API vf6cm::Vf6_ECRRevisionImpl
    : public vf6cm::Vf6_ECRRevisionGenImpl
{
public:

    ///
    /// Getter for a Double Property
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf6_total_costBase( double &value, bool &isNull ) const;

    ///
    /// Getter for a Boolean Property
    /// @param value - Parameter Value
    /// @param isNull - Returns true if the Parameter value is null
    /// @return - Status. 0 if successful
    ///
    int  getVf6_is_pur_biz_reqBase( bool &value, bool &isNull ) const;

    double calTotal(tag_t ecr_tag) const;

protected:
    ///
    /// Constructor for a Vf6_ECRRevision
    explicit Vf6_ECRRevisionImpl( Vf6_ECRRevision& busObj );

    ///
    /// Destructor
    virtual ~Vf6_ECRRevisionImpl();

private:
    ///
    /// Default Constructor for the class
    Vf6_ECRRevisionImpl();
    
    ///
    /// Private default constructor. We do not want this class instantiated without the business object passed in.
    Vf6_ECRRevisionImpl( const Vf6_ECRRevisionImpl& );

    ///
    /// Copy constructor
    Vf6_ECRRevisionImpl& operator=( const Vf6_ECRRevisionImpl& );

    ///
    /// Method to initialize this Class
    static int initializeClass();

    ///
    ///static data
    friend class vf6cm::Vf6_ECRRevisionDelegate;

};

#include <Vf6_custom_lib/libvf6_custom_lib_undef.h>
#endif // VF6CM__VF6_ECRREVISIONIMPL_HXX
