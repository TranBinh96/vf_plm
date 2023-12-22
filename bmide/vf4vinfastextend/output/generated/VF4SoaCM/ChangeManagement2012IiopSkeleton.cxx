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

#include<ChangeManagement2012IiopSkeleton.hxx>


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
    
        namespace CM
        {
             namespace _2020_12
             {




    ChangeManagementIiopSkeleton::ChangeManagementIiopSkeleton():
        IiopSkeleton()
    {
        m_serviceName = "ChangeManagement";
    
       _svcMap[ "deriveChange" ]   = deriveChange;
       _svcMap[ "getImpactedPrograms" ]   = getImpactedPrograms;
       _svcMap[ "getPartNumberChangeHistory" ]   = getPartNumberChangeHistory;


		_service = new VF4::Soa::CM::_2020_12::ChangeManagementImpl();

    }
    
    ChangeManagementIiopSkeleton::~ChangeManagementIiopSkeleton()
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



     void ChangeManagementIiopSkeleton::initialize()
    {
   	// If the impl class has not implemented the ServicePolicy interface
    	// Create an instance of the default ServicePolicy
	 	_servicePolicy = dynamic_cast<Teamcenter::Soa::Server::ServicePolicy *>(_service);
    	if(_servicePolicy == NULL)
    	{
    		_servicePolicy = new Teamcenter::Soa::Server::ServicePolicy;
    	}
    }




    static VF4::Soa::CM::_2020_12::ChangeManagementImpl* _service;
	static Teamcenter::Soa::Server::ServicePolicy*  	 _servicePolicy;


    void ChangeManagementIiopSkeleton::deriveChange( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementIiopSkeleton::deriveChange" );

        std::string _contentType;
        std::vector< VF4::Soa::CM::_2020_12::ChangeManagement::DeriveChangeInput >  inputs;
        VF4::Soa::CM::_2020_12::ChangeManagement::DeriveChangeResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementIiopSkeleton::deriveChange - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementImpl::deriveChange" );
            _out = _service->deriveChange( inputs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementIiopSkeleton::deriveChange - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }

    void ChangeManagementIiopSkeleton::getImpactedPrograms( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementIiopSkeleton::getImpactedPrograms" );

        std::string _contentType;
        VF4::Soa::CM::_2020_12::ChangeManagement::ImpactedProgramInput  inputs;
        VF4::Soa::CM::_2020_12::ChangeManagement::ImpactedProgramResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementIiopSkeleton::getImpactedPrograms - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementImpl::getImpactedPrograms" );
            _out = _service->getImpactedPrograms( inputs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementIiopSkeleton::getImpactedPrograms - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }

    void ChangeManagementIiopSkeleton::getPartNumberChangeHistory( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementIiopSkeleton::getPartNumberChangeHistory" );

        std::string _contentType;
        std::vector< VF4::Soa::CM::_2020_12::ChangeManagement::PartNumberChangeHistoryInput >  inputs;
        VF4::Soa::CM::_2020_12::ChangeManagement::PartNumberChangeHistoryResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementIiopSkeleton::getPartNumberChangeHistory - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementImpl::getPartNumberChangeHistory" );
            _out = _service->getPartNumberChangeHistory( inputs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::CM::_2020_12::ChangeManagement::ChangeManagementIiopSkeleton::getPartNumberChangeHistory - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }




VF4::Soa::CM::_2020_12::ChangeManagementImpl* ChangeManagementIiopSkeleton::_service = NULL;
Teamcenter::Soa::Server::ServicePolicy*  	  ChangeManagementIiopSkeleton::_servicePolicy = NULL;

#ifdef __cplusplus
extern "C" {
#endif
#ifdef WNT
__declspec(dllexport)
#else
extern
#endif
void CM_2020_12_ChangeManagement_ConstructT2LService( Teamcenter::Soa::Internal::Gateway::T2LService **service )
{
    *service = new ChangeManagementIiopSkeleton();
}
#ifdef __cplusplus
}
#endif

}}}}    // End Namespace

