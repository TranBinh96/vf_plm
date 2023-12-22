/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2015
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@

 ==================================================

   Auto-generated source from service interface.
                 DO NOT EDIT

 ==================================================
*/

//#ifndef TEAMCENTER_SERVICES_Custom__2017_05_Integration_HXX 
//#define TEAMCENTER_SERVICES_Custom__2017_05_Integration_HXX

#include <unidefs.h>
#if defined(SUN)
#    include <unistd.h>
#endif

#include <string>
#include <iostream>
#include <sstream>



#include <teamcenter/soa/server/ServiceData.hxx>
#include <teamcenter/soa/server/ServiceException.hxx>
#include <teamcenter/soa/server/PartialErrors.hxx>
#include <teamcenter/soa/server/Preferences.hxx>
#include <teamcenter/soa/server/ServicePolicy.hxx> 
#include <teamcenter/soa/internal/server/IiopSkeleton.hxx>
#include <teamcenter/soa/internal/gateway/TcServiceManager.hxx>
#include <teamcenter/soa/internal/xml/XMLNode.hxx>
#include <teamcenter/soa/internal/xml/XmlUtils.hxx>
#include <teamcenter/soa/common/exceptions/ExceptionMapper.hxx>
#include <teamcenter/soa/common/exceptions/DomException.hxx>
#include <teamcenter/schemas/soa/_2006_03/exceptions/ServiceException.hxx>

#include <integration1705impl.hxx>


#include <Custom_exports.h>  
namespace VF4
{
    namespace Services
    {
    
        namespace Custom
        {
             namespace _2017_05
             {


class SOACUSTOM_API IntegrationIiopSkeleton : public Teamcenter::Soa::Internal::Server::IiopSkeleton
{

public:

     IntegrationIiopSkeleton();
    ~IntegrationIiopSkeleton();
    
    virtual void initialize();
   


private:

    static VF4::Soa::Custom::_2017_05::IntegrationImpl* _service;
	static Teamcenter::Soa::Server::ServicePolicy*  	 _servicePolicy;


    static void nominateSupplierToSQP( const std::string& xmlIn,  Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse );
    
    static void removeSupplierInfo( const std::string& xmlIn,  Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse );
    
    static void updateQcheckerProperty( const std::string& xmlIn,  Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse );
    


};    // End Class



}}}}    // End Namespace
#include <Custom_undef.h>
//#endif   

