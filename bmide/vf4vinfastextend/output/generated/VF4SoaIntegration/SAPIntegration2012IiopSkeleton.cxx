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

#include<SAPIntegration2012IiopSkeleton.hxx>


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
    
        namespace Integration
        {
             namespace _2020_12
             {




    SAPIntegrationIiopSkeleton::SAPIntegrationIiopSkeleton():
        IiopSkeleton()
    {
        m_serviceName = "SAPIntegration";
    
       _svcMap[ "getMasterMaterials" ]   = getMasterMaterials;
       _svcMap[ "transferOperationJSONToMES" ]   = transferOperationJSONToMES;
       _svcMap[ "transferOperationToMES" ]   = transferOperationToMES;


		_service = new VF4::Soa::Integration::_2020_12::SAPIntegrationImpl();

    }
    
    SAPIntegrationIiopSkeleton::~SAPIntegrationIiopSkeleton()
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



     void SAPIntegrationIiopSkeleton::initialize()
    {
   	// If the impl class has not implemented the ServicePolicy interface
    	// Create an instance of the default ServicePolicy
	 	_servicePolicy = dynamic_cast<Teamcenter::Soa::Server::ServicePolicy *>(_service);
    	if(_servicePolicy == NULL)
    	{
    		_servicePolicy = new Teamcenter::Soa::Server::ServicePolicy;
    	}
    }




    static VF4::Soa::Integration::_2020_12::SAPIntegrationImpl* _service;
	static Teamcenter::Soa::Server::ServicePolicy*  	 _servicePolicy;


    void SAPIntegrationIiopSkeleton::getMasterMaterials( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationIiopSkeleton::getMasterMaterials" );

        std::string _contentType;
        std::vector< VF4::Soa::Integration::_2020_12::SAPIntegration::GetMasterMaterialsInput >  inputs;
        VF4::Soa::Integration::_2020_12::SAPIntegration::GetMasterMaterialsResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationIiopSkeleton::getMasterMaterials - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationImpl::getMasterMaterials" );
            _out = _service->getMasterMaterials( inputs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationIiopSkeleton::getMasterMaterials - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }

    void SAPIntegrationIiopSkeleton::transferOperationJSONToMES( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationIiopSkeleton::transferOperationJSONToMES" );

        std::string _contentType;
        VF4::Soa::Integration::_2020_12::SAPIntegration::TransferOperationToJSONInputs  inputs;
        VF4::Soa::Integration::_2020_12::SAPIntegration::TransferOperationToJSONResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationIiopSkeleton::transferOperationJSONToMES - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationImpl::transferOperationJSONToMES" );
            _out = _service->transferOperationJSONToMES( inputs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationIiopSkeleton::transferOperationJSONToMES - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }

    void SAPIntegrationIiopSkeleton::transferOperationToMES( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationIiopSkeleton::transferOperationToMES" );

        std::string _contentType;
        VF4::Soa::Integration::_2020_12::SAPIntegration::TransferOperationToMESInputs  inputs;
        VF4::Soa::Integration::_2020_12::SAPIntegration::TransferOperationToMESResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationIiopSkeleton::transferOperationToMES - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationImpl::transferOperationToMES" );
            _out = _service->transferOperationToMES( inputs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Integration::_2020_12::SAPIntegration::SAPIntegrationIiopSkeleton::transferOperationToMES - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }




VF4::Soa::Integration::_2020_12::SAPIntegrationImpl* SAPIntegrationIiopSkeleton::_service = NULL;
Teamcenter::Soa::Server::ServicePolicy*  	  SAPIntegrationIiopSkeleton::_servicePolicy = NULL;

#ifdef __cplusplus
extern "C" {
#endif
#ifdef WNT
__declspec(dllexport)
#else
extern
#endif
void Integration_2020_12_SAPIntegration_ConstructT2LService( Teamcenter::Soa::Internal::Gateway::T2LService **service )
{
    *service = new SAPIntegrationIiopSkeleton();
}
#ifdef __cplusplus
}
#endif

}}}}    // End Namespace

