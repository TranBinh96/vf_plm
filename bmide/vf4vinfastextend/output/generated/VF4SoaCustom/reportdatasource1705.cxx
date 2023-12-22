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

#include <string>
#include <map>

#include <reportdatasource1705.hxx>


#include <teamcenter/soa/internal/server/SdmParser.hxx>
#include <teamcenter/soa/internal/server/SdmStream.hxx>

using namespace VF4::Soa::Custom::_2017_05;

const std::string ReportDataSource::XSD_NAMESPACE ="http://vf4.com/Schemas/Custom/2017-05/ReportDataSource";




void ReportDataSource::SourcePartCostInfo::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "vf4SupplierPieceCostExw", (double)vf4SupplierPieceCostExw );
    _sdmStream->writeStructMember( "vf4ToolingOrder", (double)vf4ToolingOrder );
    _sdmStream->writeStructMember( "vf4EddorderValue", (double)vf4EddorderValue );
    _sdmStream->writeStructMember( "vf4PieceCost", (double)vf4PieceCost );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE, true );

}

void ReportDataSource::CostInfo::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"sourcePart", sourcePart );
    _sdmStream->writeStructMember( _prefix+"ecrs", ecrs );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::CostReportDSInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "partRevs", partRevs );
    _sdmParser->parseStructMember( "sourcingProgram", sourcingProgram );
}

void ReportDataSource::CostReportDSOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"response", response, true, false,  VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::ECRCostInfo::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "vf6MaterialCosts", (double)vf6MaterialCosts );
    _sdmStream->writeStructMember( "vf6ToolingCosts", (double)vf6ToolingCosts );
    _sdmStream->writeStructMember( "vf6Fixtures", (double)vf6Fixtures );
    _sdmStream->writeStructMember( "vf6SupplierEngCosts", (double)vf6SupplierEngCosts );
    _sdmStream->writeStructMember( "vf6ScrapCosts", (double)vf6ScrapCosts );
    _sdmStream->writeStructMember( "vf6LeadTimeP", (double)vf6LeadTimeP );
    _sdmStream->writeStructMember( "ecrName", ecrName );
    _sdmStream->writeStructMember( "ecrNumber", ecrNumber );
    _sdmStream->writeStructMember( "vf6VehicleGroup", vf6VehicleGroup );
    _sdmStream->writeStructMember( "vf6ModuleGroup", vf6ModuleGroup );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2017_05::ReportDataSource::XSD_NAMESPACE, true );

}




ReportDataSource::ReportDataSource()
{
}

ReportDataSource::~ReportDataSource()
{
}

