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

#include<Sourcing2012IiopSkeleton.hxx>


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




    SourcingIiopSkeleton::SourcingIiopSkeleton():
        IiopSkeleton()
    {
        m_serviceName = "Sourcing";
    
       _svcMap[ "assignSourcePartToBuyer" ]   = assignSourcePartToBuyer;
       _svcMap[ "getOrCreateVFCost" ]   = getOrCreateVFCost;


		_service = new VF4::Soa::Custom::_2020_12::SourcingImpl();

    }
    
    SourcingIiopSkeleton::~SourcingIiopSkeleton()
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



     void SourcingIiopSkeleton::initialize()
    {
   	// If the impl class has not implemented the ServicePolicy interface
    	// Create an instance of the default ServicePolicy
	 	_servicePolicy = dynamic_cast<Teamcenter::Soa::Server::ServicePolicy *>(_service);
    	if(_servicePolicy == NULL)
    	{
    		_servicePolicy = new Teamcenter::Soa::Server::ServicePolicy;
    	}
    }




    static VF4::Soa::Custom::_2020_12::SourcingImpl* _service;
	static Teamcenter::Soa::Server::ServicePolicy*  	 _servicePolicy;


    void SourcingIiopSkeleton::assignSourcePartToBuyer( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Custom::_2020_12::Sourcing::SourcingIiopSkeleton::assignSourcePartToBuyer" );

        std::string _contentType;
        std::vector< VF4::Soa::Custom::_2020_12::Sourcing::STBuyerAssignmentInfo >  inputs;
        VF4::Soa::Custom::_2020_12::Sourcing::AssignSourcePartToBuyerResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Custom::_2020_12::Sourcing::SourcingIiopSkeleton::assignSourcePartToBuyer - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Custom::_2020_12::Sourcing::SourcingImpl::assignSourcePartToBuyer" );
            _out = _service->assignSourcePartToBuyer( inputs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Custom::_2020_12::Sourcing::SourcingIiopSkeleton::assignSourcePartToBuyer - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }

    void SourcingIiopSkeleton::getOrCreateVFCost( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Custom::_2020_12::Sourcing::SourcingIiopSkeleton::getOrCreateVFCost" );

        std::string _contentType;
        std::vector< VF4::Soa::Custom::_2020_12::Sourcing::GetOrCreateVFCostInput >  inputs;
        VF4::Soa::Custom::_2020_12::Sourcing::GetOrCreateVFCostResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Custom::_2020_12::Sourcing::SourcingIiopSkeleton::getOrCreateVFCost - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Custom::_2020_12::Sourcing::SourcingImpl::getOrCreateVFCost" );
            _out = _service->getOrCreateVFCost( inputs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Custom::_2020_12::Sourcing::SourcingIiopSkeleton::getOrCreateVFCost - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }




VF4::Soa::Custom::_2020_12::SourcingImpl* SourcingIiopSkeleton::_service = NULL;
Teamcenter::Soa::Server::ServicePolicy*  	  SourcingIiopSkeleton::_servicePolicy = NULL;

#ifdef __cplusplus
extern "C" {
#endif
#ifdef WNT
__declspec(dllexport)
#else
extern
#endif
void Custom_2020_12_Sourcing_ConstructT2LService( Teamcenter::Soa::Internal::Gateway::T2LService **service )
{
    *service = new SourcingIiopSkeleton();
}
#ifdef __cplusplus
}
#endif

}}}}    // End Namespace

