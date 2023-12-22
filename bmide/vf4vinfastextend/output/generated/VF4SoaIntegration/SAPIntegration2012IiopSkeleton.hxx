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

//#ifndef TEAMCENTER_SERVICES_Integration__2020_12_SAPIntegration_HXX 
//#define TEAMCENTER_SERVICES_Integration__2020_12_SAPIntegration_HXX

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

#include <sapintegration2012impl.hxx>


#include <Integration_exports.h>  
namespace VF4
{
    namespace Services
    {
    
        namespace Integration
        {
             namespace _2020_12
             {


class SOAINTEGRATION_API SAPIntegrationIiopSkeleton : public Teamcenter::Soa::Internal::Server::IiopSkeleton
{

public:

     SAPIntegrationIiopSkeleton();
    ~SAPIntegrationIiopSkeleton();
    
    virtual void initialize();
   


private:

    static VF4::Soa::Integration::_2020_12::SAPIntegrationImpl* _service;
	static Teamcenter::Soa::Server::ServicePolicy*  	 _servicePolicy;


    static void getMasterMaterials( const std::string& xmlIn,  Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse );
    
    static void transferOperationJSONToMES( const std::string& xmlIn,  Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse );
    
    static void transferOperationToMES( const std::string& xmlIn,  Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse );
    


};    // End Class



}}}}    // End Namespace
#include <Integration_undef.h>
//#endif   

