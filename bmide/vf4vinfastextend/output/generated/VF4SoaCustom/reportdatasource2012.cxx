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

#include <reportdatasource2012.hxx>


#include <teamcenter/soa/internal/server/SdmParser.hxx>
#include <teamcenter/soa/internal/server/SdmStream.hxx>

using namespace VF4::Soa::Custom::_2020_12;

const std::string ReportDataSource::XSD_NAMESPACE ="http://vf4.com/Schemas/Custom/2020-12/ReportDataSource";




void ReportDataSource::Get150BomWithVariantsDSInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "toplineTag", toplineTag );
    _sdmParser->parseStructMember( "topItemId", topItemId );
    _sdmParser->parseStructMember( "topItemType", topItemType );
}

void ReportDataSource::Get150BomWithVariantsDSOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "level", level );
    _sdmStream->writeStructMember( "errorMessage", errorMessage );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"bomlineTag", bomlineTag );
    _sdmStream->writeStructMember( _prefix+"variantRule", variantRule );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::Get150BomWithVariantsDSResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"outputs", outputs );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::PFEPReportInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "ebomTopLine", ebomTopLine );
    _sdmParser->parseStructMember( "revisionRule", revisionRule );
    _sdmParser->parseStructMember( "itemType", itemType );
    _sdmParser->parseStructMember( "sourcingProgram", sourcingProgram );
}

void ReportDataSource::PFEPReportOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "itemId", itemId );
    _sdmStream->writeStructMember( "itemName", itemName );
    _sdmStream->writeStructMember( "moduleGroupEnglish", moduleGroupEnglish );
    _sdmStream->writeStructMember( "mainModuleEnglish", mainModuleEnglish );
    _sdmStream->writeStructMember( "purchaseLevelVinfast", purchaseLevelVinfast );
    _sdmStream->writeStructMember( "changeIndex", changeIndex );
    _sdmStream->writeStructMember( "donorVehicle", donorVehicle );
    _sdmStream->writeStructMember( "sorReleasedDateActual", sorReleasedDateActual );
    _sdmStream->writeStructMember( "supplierNominationDatePlan", supplierNominationDatePlan );
    _sdmStream->writeStructMember( "supplierNominationDateReality", supplierNominationDateReality );
    _sdmStream->writeStructMember( "nominatedSupplier", nominatedSupplier );
    _sdmStream->writeStructMember( "manufacturingCountry", manufacturingCountry );
    _sdmStream->writeStructMember( "supplierNominationStatus", supplierNominationStatus );
    _sdmStream->writeStructMember( "pInProgress", pInProgress );
    _sdmStream->writeStructMember( "pReleasePlanDate", pReleasePlanDate );
    _sdmStream->writeStructMember( "pReleasedDate", pReleasedDate );
    _sdmStream->writeStructMember( "pReleaseStatus", pReleaseStatus );
    _sdmStream->writeStructMember( "iInProgress", iInProgress );
    _sdmStream->writeStructMember( "iReleasePlanDate", iReleasePlanDate );
    _sdmStream->writeStructMember( "iReleasedDate", iReleasedDate );
    _sdmStream->writeStructMember( "iReleaseStatus", iReleaseStatus );
    _sdmStream->writeStructMember( "prInProgress", prInProgress );
    _sdmStream->writeStructMember( "prReleasePlanDate", prReleasePlanDate );
    _sdmStream->writeStructMember( "prReleasedDate", prReleasedDate );
    _sdmStream->writeStructMember( "prReleasedStatus", prReleasedStatus );
    _sdmStream->writeStructMember( "originPart", originPart );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, true );

}

void ReportDataSource::PFEPReportResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"outputs", outputs );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::ProcessStatusReportInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "workflowTemplate", workflowTemplate );
    _sdmParser->parseStructMember( "creationDateBefore", creationDateBefore );
    _sdmParser->parseStructMember( "creationDateAfter", creationDateAfter );
    _sdmParser->parseStructMember( "modifyDateBefore", modifyDateBefore );
    _sdmParser->parseStructMember( "modifyDateAfter", modifyDateAfter );
    _sdmParser->parseStructMember( "processName", processName );
    _sdmParser->parseStructMember( "runningProccess", runningProccess );
}

void ReportDataSource::ProcessStatusReportOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "processName", processName );
    _sdmStream->writeStructMember( "processStatus", processStatus );
    _sdmStream->writeStructMember( "startDate", startDate );
    _sdmStream->writeStructMember( "dueDate", dueDate );
    _sdmStream->writeStructMember( "completedDate", completedDate );
    _sdmStream->writeStructMember( "itemID", itemID );
    _sdmStream->writeStructMember( "itemRevision", itemRevision );
    _sdmStream->writeStructMember( "itemName", itemName );
    _sdmStream->writeStructMember( "description", description );
    _sdmStream->writeStructMember( "owningUser", owningUser );
    _sdmStream->writeStructMember( "owningGroup", owningGroup );
    _sdmStream->writeStructMember( "moduleGroup", moduleGroup );
    _sdmStream->writeStructMember( "processStartDate", processStartDate );
    _sdmStream->writeStructMember( "pendingTask", pendingTask );
    _sdmStream->writeStructMember( "pendingUser", pendingUser );
    _sdmStream->writeStructMember( "wfTemplate", wfTemplate );
    _sdmStream->writeStructMember( "targets", targets );
    _sdmStream->writeStructMember( "reviewerComment", reviewerComment );
    _sdmStream->writeStructMember( "reviewer", reviewer );
    _sdmStream->writeStructMember( "taskStatus", taskStatus );
    _sdmStream->writeStructMember( "resParty", resParty );
    _sdmStream->writeStructMember( "subTaskName", subTaskName );
    _sdmStream->writeStructMember( "classification", classification );
    _sdmStream->writeStructMember( "priority", priority );
    _sdmStream->writeStructMember( "exchangeNewPart", exchangeNewPart );
    _sdmStream->writeStructMember( "exchangeOldPart", exchangeOldPart );
    _sdmStream->writeStructMember( "processEndDate", processEndDate );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, true );

}

void ReportDataSource::ProcessStatusReportResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"outputs", outputs );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_12::ReportDataSource::XSD_NAMESPACE, elementName  );
}




ReportDataSource::ReportDataSource()
{
}

ReportDataSource::~ReportDataSource()
{
}

