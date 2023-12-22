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

#include<AdminUtils2012IiopSkeleton.hxx>


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
    
        namespace AdminUtils
        {
             namespace _2020_12
             {




    AdminUtilsIiopSkeleton::AdminUtilsIiopSkeleton():
        IiopSkeleton()
    {
        m_serviceName = "AdminUtils";
    
       _svcMap[ "changeUOM" ]   = changeUOM;
       _svcMap[ "reassignWFTask" ]   = reassignWFTask;
       _svcMap[ "setProperties" ]   = setProperties;


		_service = new VF4::Soa::AdminUtils::_2020_12::AdminUtilsImpl();

    }
    
    AdminUtilsIiopSkeleton::~AdminUtilsIiopSkeleton()
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



     void AdminUtilsIiopSkeleton::initialize()
    {
   	// If the impl class has not implemented the ServicePolicy interface
    	// Create an instance of the default ServicePolicy
	 	_servicePolicy = dynamic_cast<Teamcenter::Soa::Server::ServicePolicy *>(_service);
    	if(_servicePolicy == NULL)
    	{
    		_servicePolicy = new Teamcenter::Soa::Server::ServicePolicy;
    	}
    }




    static VF4::Soa::AdminUtils::_2020_12::AdminUtilsImpl* _service;
	static Teamcenter::Soa::Server::ServicePolicy*  	 _servicePolicy;


    void AdminUtilsIiopSkeleton::changeUOM( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsIiopSkeleton::changeUOM" );

        std::string _contentType;
        std::vector< VF4::Soa::AdminUtils::_2020_12::AdminUtils::ChangeUOMInput >  inputs;
        VF4::Soa::AdminUtils::_2020_12::AdminUtils::ChangeUOMResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsIiopSkeleton::changeUOM - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsImpl::changeUOM" );
            _out = _service->changeUOM( inputs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsIiopSkeleton::changeUOM - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }

    void AdminUtilsIiopSkeleton::reassignWFTask( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsIiopSkeleton::reassignWFTask" );

        std::string _contentType;
        std::vector< VF4::Soa::AdminUtils::_2020_12::AdminUtils::ReassignWFTaskInput >  inputs;
        VF4::Soa::AdminUtils::_2020_12::AdminUtils::ReassignWFTaskResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsIiopSkeleton::reassignWFTask - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsImpl::reassignWFTask" );
            _out = _service->reassignWFTask( inputs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsIiopSkeleton::reassignWFTask - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }

    void AdminUtilsIiopSkeleton::setProperties( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsIiopSkeleton::setProperties" );

        std::string _contentType;
        std::vector< VF4::Soa::AdminUtils::_2020_12::AdminUtils::SetPropertyInput >  inputs;
        bool  isKeepModifiedDate;
        VF4::Soa::AdminUtils::_2020_12::AdminUtils::SetPropertyResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsIiopSkeleton::setProperties - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
            _sdmParser.parseStructMember( "isKeepModifiedDate", isKeepModifiedDate );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsImpl::setProperties" );
            _out = _service->setProperties( inputs, isKeepModifiedDate );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::AdminUtils::_2020_12::AdminUtils::AdminUtilsIiopSkeleton::setProperties - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }




VF4::Soa::AdminUtils::_2020_12::AdminUtilsImpl* AdminUtilsIiopSkeleton::_service = NULL;
Teamcenter::Soa::Server::ServicePolicy*  	  AdminUtilsIiopSkeleton::_servicePolicy = NULL;

#ifdef __cplusplus
extern "C" {
#endif
#ifdef WNT
__declspec(dllexport)
#else
extern
#endif
void AdminUtils_2020_12_AdminUtils_ConstructT2LService( Teamcenter::Soa::Internal::Gateway::T2LService **service )
{
    *service = new AdminUtilsIiopSkeleton();
}
#ifdef __cplusplus
}
#endif

}}}}    // End Namespace

