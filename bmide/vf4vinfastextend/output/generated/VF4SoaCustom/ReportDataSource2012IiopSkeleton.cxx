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

#include<ReportDataSource2012IiopSkeleton.hxx>


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




    ReportDataSourceIiopSkeleton::ReportDataSourceIiopSkeleton():
        IiopSkeleton()
    {
        m_serviceName = "ReportDataSource";
    
       _svcMap[ "get150BomWithVariantsDS" ]   = get150BomWithVariantsDS;
       _svcMap[ "getPFEPReport" ]   = getPFEPReport;
       _svcMap[ "getProcessStatusReport" ]   = getProcessStatusReport;


		_service = new VF4::Soa::Custom::_2020_12::ReportDataSourceImpl();

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




    static VF4::Soa::Custom::_2020_12::ReportDataSourceImpl* _service;
	static Teamcenter::Soa::Server::ServicePolicy*  	 _servicePolicy;


    void ReportDataSourceIiopSkeleton::get150BomWithVariantsDS( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceIiopSkeleton::get150BomWithVariantsDS" );

        std::string _contentType;
        std::vector< VF4::Soa::Custom::_2020_12::ReportDataSource::Get150BomWithVariantsDSInput >  inputs;
        VF4::Soa::Custom::_2020_12::ReportDataSource::Get150BomWithVariantsDSResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceIiopSkeleton::get150BomWithVariantsDS - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "inputs", inputs );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceImpl::get150BomWithVariantsDS" );
            _out = _service->get150BomWithVariantsDS( inputs );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceIiopSkeleton::get150BomWithVariantsDS - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }

    void ReportDataSourceIiopSkeleton::getPFEPReport( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceIiopSkeleton::getPFEPReport" );

        std::string _contentType;
        std::vector< VF4::Soa::Custom::_2020_12::ReportDataSource::PFEPReportInput >  input;
        VF4::Soa::Custom::_2020_12::ReportDataSource::PFEPReportResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceIiopSkeleton::getPFEPReport - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "input", input );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceImpl::getPFEPReport" );
            _out = _service->getPFEPReport( input );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceIiopSkeleton::getPFEPReport - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }

    void ReportDataSourceIiopSkeleton::getProcessStatusReport( const std::string& xmlOrJsonDoc, Teamcenter::Soa::Internal::Gateway::GatewayResponse& gatewayResponse )
    {
        ScopedJournal _journalSkeleton( "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceIiopSkeleton::getProcessStatusReport" );

        std::string _contentType;
        VF4::Soa::Custom::_2020_12::ReportDataSource::ProcessStatusReportInput  input;
        VF4::Soa::Custom::_2020_12::ReportDataSource::ProcessStatusReportResponse _out;

        //  ========== Parse the input XML or JSON document to the local input arguments ==========
        {
            ScopedJournal _journalParse( "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceIiopSkeleton::getProcessStatusReport - Parse input document" );
            Teamcenter::Soa::Internal::Server::SdmParser _sdmParser( xmlOrJsonDoc );
            _contentType = _sdmParser.getDocumentType();
            _sdmParser.parseStructMember( "input", input );
        }        


        //  ===================== Call the service operation implementation  ======================
        {
            ScopedJournal journalExecute( "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceImpl::getProcessStatusReport" );
            _out = _service->getProcessStatusReport( input );
        }

        //  ================== Serialize the response to a XML or JSON document  ==================
        {
            ScopedJournal _journalSerialize(  "VF4::Soa::Custom::_2020_12::ReportDataSource::ReportDataSourceIiopSkeleton::getProcessStatusReport - Serialize output document" );
            Teamcenter::Soa::Internal::Server::SdmStream _sdmStream(  _contentType, gatewayResponse.getBodyOutputStream() );
            _out.serialize( &_sdmStream );          
        }
    }




VF4::Soa::Custom::_2020_12::ReportDataSourceImpl* ReportDataSourceIiopSkeleton::_service = NULL;
Teamcenter::Soa::Server::ServicePolicy*  	  ReportDataSourceIiopSkeleton::_servicePolicy = NULL;

#ifdef __cplusplus
extern "C" {
#endif
#ifdef WNT
__declspec(dllexport)
#else
extern
#endif
void Custom_2020_12_ReportDataSource_ConstructT2LService( Teamcenter::Soa::Internal::Gateway::T2LService **service )
{
    *service = new ReportDataSourceIiopSkeleton();
}
#ifdef __cplusplus
}
#endif

}}}}    // End Namespace

