/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2014
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@

 ==================================================

   Auto-generated source from service interface.
                 DO NOT EDIT

 ==================================================
*/


#include <unidefs.h>
#if defined(SUN)
#    include <unistd.h>
#endif

#include<Integration2012IiopSkeleton.hxx>


#include <teamcenter/soa/internal/common/PolicyMarshaller.hxx>
#include <teamcenter/soa/internal/server/PolicyManager.hxx>
#include <teamcenter/soa/internal/server/PerServicePropertyPolicy.hxx>
#include <teamcenter/soa/internal/server/SdmParser.hxx>
#include <teamcenter/soa/internal/server/SdmStream.hxx>

using namespace std;
using namespace Teamcenter::Soa::Server;
using namespace Teamcenter::Soa::Internal::Server;
using namespace Teamcenter::Soa::Internal::Common;
using namespace Teamcenter::Soa::Common::Exceptions;


namespace VF4
{
    namespace Services
    {
    
        namespace Custom
        {
             namespace _2020_12
             {




    IntegrationIiopSkeleton::IntegrationIiopSkeleton():
        IiopSkeleton()
    {
        m_serviceName = "Integration";
    
       _svcMap[ "structureSearchInMPP" ]   = structureSearchInMPP;


		_service = new VF4::Soa::Custom::_2020_12::IntegrationImpl();

    }
    
    IntegrationIiopSkeleton::~IntegrationIiopSkeleton()
    {
    	// If the implementing class did not implement the ServicePolicy
    	// then delete it, since it was allocated here in the skeleton
	 	Teamcenter::Soa::Server::ServicePolicy* sp = dynamic_cast<Teamcenter::Soa::Server::ServicePolicy *>(_service);
    	if(sp == NULL)
    	{
    		delete _servicePolicy;
    	}
        delete _service;
        _service = NULL;
    }



     void IntegrationIiopSkeleton::initialize()
    {
   	// If the impl class has not implemented the ServicePolicy interface
    	// Create an instance of the default ServicePolicy
	 	_servicePolicy = dynamic_cast<Teamcenter::Soa::Server::ServicePolicy *>(_service);
    	if(_servicePolicy == NULL)
    	{
    		_servicePolicy = new Teamcenter::Soa::Server::ServicePolicy;
    	}
    }




    static VF4::Soa::Custom::_2020_12::IntegrationImpl* _service;
	static Teamcenter::Soa::Server::ServicePolicy*  	 _servicePolicy;


    void IntegrationIiopSkeleton::structureSearchInMPP( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Custom::_2020_12::Integration::IntegrationIiopSkeleton::structureSearchInMPP" );

        std::string _contentType;
        VF4::Soa::Custom::_2020_12::Integration::StructureSearchInMPPInput  input;
        VF4::Soa::Custom::_2020_12::Integration::StructureSearchInMPPOutput _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Custom::_2020_12::Integration::IntegrationIiopSkeleton::structureSearchInMPP - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "input", input );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Custom::_2020_12::Integration::IntegrationImpl::structureSearchInMPP" );
            _out = _service->structureSearchInMPP( input );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Custom::_2020_12::Integration::IntegrationIiopSkeleton::structureSearchInMPP - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }




VF4::Soa::Custom::_2020_12::IntegrationImpl* IntegrationIiopSkeleton::_service = NULL;
Teamcenter::Soa::Server::ServicePolicy*  	  IntegrationIiopSkeleton::_servicePolicy = NULL;

#ifdef __cplusplus
extern "C" {
#endif
#ifdef WNT
__declspec(dllexport)
#else
extern
#endif
void Custom_2020_12_Integration_ConstructT2LService( Teamcenter::Soa::Internal::Gateway::T2LService **service )
{
    *service = new IntegrationIiopSkeleton();
}
#ifdef __cplusplus
}
#endif

}}}}    // End Namespace

