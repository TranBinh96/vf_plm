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

#include<ReportDataSource2010IiopSkeleton.hxx>


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
             namespace _2020_10
             {




    ReportDataSourceIiopSkeleton::ReportDataSourceIiopSkeleton():
        IiopSkeleton()
    {
        m_serviceName = "ReportDataSource";
    
       _svcMap[ "calculatePartsCost" ]   = calculatePartsCost;
       _svcMap[ "getEcrsCost" ]   = getEcrsCost;
       _svcMap[ "validateFamilyGroup" ]   = validateFamilyGroup;


		_service = new VF4::Soa::Custom::_2020_10::ReportDataSourceImpl();

    }
    
    ReportDataSourceIiopSkeleton::~ReportDataSourceIiopSkeleton()
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



     void ReportDataSourceIiopSkeleton::initialize()
    {
   	// If the impl class has not implemented the ServicePolicy interface
    	// Create an instance of the default ServicePolicy
	 	_servicePolicy = dynamic_cast<Teamcenter::Soa::Server::ServicePolicy *>(_service);
    	if(_servicePolicy == NULL)
    	{
    		_servicePolicy = new Teamcenter::Soa::Server::ServicePolicy;
    	}
    }




    static VF4::Soa::Custom::_2020_10::ReportDataSourceImpl* _service;
	static Teamcenter::Soa::Server::ServicePolicy*  	 _servicePolicy;


    void ReportDataSourceIiopSkeleton::calculatePartsCost( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceIiopSkeleton::calculatePartsCost" );

        std::string _contentType;
        std::vector< std::string >  partNumbers;
        std::string  programName;
        std::string  isDryrun;
        VF4::Soa::Custom::_2020_10::ReportDataSource::VFPartCostCalcResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceIiopSkeleton::calculatePartsCost - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "partNumbers", partNumbers );
            _sdmParser.parseStructMember( "programName", programName );
            _sdmParser.parseStructMember( "isDryrun", isDryrun );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceImpl::calculatePartsCost" );
            _out = _service->calculatePartsCost( partNumbers, programName, isDryrun );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceIiopSkeleton::calculatePartsCost - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }

    void ReportDataSourceIiopSkeleton::getEcrsCost( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceIiopSkeleton::getEcrsCost" );

        std::string _contentType;
        std::vector< BusinessObjectRef<Teamcenter::BusinessObject> >  ecrs;
        VF4::Soa::Custom::_2020_10::ReportDataSource::VFEcrCostResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceIiopSkeleton::getEcrsCost - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "ecrs", ecrs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceImpl::getEcrsCost" );
            _out = _service->getEcrsCost( ecrs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceIiopSkeleton::getEcrsCost - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }

    void ReportDataSourceIiopSkeleton::validateFamilyGroup( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceIiopSkeleton::validateFamilyGroup" );

        std::string _contentType;
        VF4::Soa::Custom::_2020_10::ReportDataSource::FGValidationInput  input;
        VF4::Soa::Custom::_2020_10::ReportDataSource::FGValidationOutput _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceIiopSkeleton::validateFamilyGroup - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "input", input );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceImpl::validateFamilyGroup" );
            _out = _service->validateFamilyGroup( input );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Custom::_2020_10::ReportDataSource::ReportDataSourceIiopSkeleton::validateFamilyGroup - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }




VF4::Soa::Custom::_2020_10::ReportDataSourceImpl* ReportDataSourceIiopSkeleton::_service = NULL;
Teamcenter::Soa::Server::ServicePolicy*  	  ReportDataSourceIiopSkeleton::_servicePolicy = NULL;

#ifdef __cplusplus
extern "C" {
#endif
#ifdef WNT
__declspec(dllexport)
#else
extern
#endif
void Custom_2020_10_ReportDataSource_ConstructT2LService( Teamcenter::Soa::Internal::Gateway::T2LService **service )
{
    *service = new ReportDataSourceIiopSkeleton();
}
#ifdef __cplusplus
}
#endif

}}}}    // End Namespace

