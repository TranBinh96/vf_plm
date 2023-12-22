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

#include <reportdatasource2010.hxx>


#include <teamcenter/soa/internal/server/SdmParser.hxx>
#include <teamcenter/soa/internal/server/SdmStream.hxx>

using namespace VF4::Soa::Custom::_2020_10;

const std::string ReportDataSource::XSD_NAMESPACE ="http://vf4.com/Schemas/Custom/2020-10/ReportDataSource";


const std::map<std::string, ReportDataSource::PartChangeType>::value_type StringPartChangeTypeData[] = 
{
    std::map<std::string, ReportDataSource::PartChangeType>::value_type("old_part", ReportDataSource::old_part),
    std::map<std::string, ReportDataSource::PartChangeType>::value_type("new_part", ReportDataSource::new_part)
};
const std::map<ReportDataSource::PartChangeType, std::string>::value_type PartChangeTypeStringData[] = 
{
    std::map<ReportDataSource::PartChangeType, std::string>::value_type(ReportDataSource::old_part, "old_part"),
    std::map<ReportDataSource::PartChangeType, std::string>::value_type(ReportDataSource::new_part, "new_part")
};

const std::map<std::string, ReportDataSource::PartChangeType>  ReportDataSource::STRING_TO_PARTCHANGETYPE_MAP(StringPartChangeTypeData, StringPartChangeTypeData + (sizeof StringPartChangeTypeData / sizeof StringPartChangeTypeData[0]));
const std::map<ReportDataSource::PartChangeType, std::string>  ReportDataSource::PARTCHANGETYPE_TO_STRING_MAP(PartChangeTypeStringData, PartChangeTypeStringData + (sizeof PartChangeTypeStringData / sizeof PartChangeTypeStringData[0]));



void ReportDataSource::FGValidationInput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "itemId", itemId );
    _sdmStream->writeStructMember( "revId", revId );
    _sdmStream->writeStructMember( "itemType", itemType );
    _sdmStream->writeStructMember( "program", program );
    _sdmStream->writeStructMember( "revisionRule", revisionRule );
    _sdmStream->writeStructMember( "savedVariant2LoadBom", savedVariant2LoadBom );
    _sdmStream->writeStructMember( "variant2Validate", variant2Validate );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, true );

}

void ReportDataSource::FGValidationInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "itemId", itemId );
    _sdmParser->parseStructMember( "revId", revId );
    _sdmParser->parseStructMember( "itemType", itemType );
    _sdmParser->parseStructMember( "program", program );
    _sdmParser->parseStructMember( "revisionRule", revisionRule );
    _sdmParser->parseStructMember( "savedVariant2LoadBom", savedVariant2LoadBom );
    _sdmParser->parseStructMember( "variant2Validate", variant2Validate );
}

void ReportDataSource::FGValidationOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "finalValidationResult", finalValidationResult );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"input", input );
    _sdmStream->writeStructMember( _prefix+"validationResults", validationResults );
    _sdmStream->writeStructMember( _prefix+"errorMessages", errorMessages );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::FGValidationPartInfo::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "quantity", (double)quantity );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"partRev", partRev );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::FGValidationResult::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "fgCode", fgCode );
    _sdmStream->writeStructMember( "fgDescription", fgDescription );
    _sdmStream->writeStructMember( "minQuantity", (double)minQuantity );
    _sdmStream->writeStructMember( "maxQuantity", (double)maxQuantity );
    _sdmStream->writeStructMember( "fgCountInBom", (double)fgCountInBom );
    _sdmStream->writeStructMember( "validationResult", validationResult );
    _sdmStream->writeStructMember( "fgValidationType", fgValidationType );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"relevantPartsInfo", relevantPartsInfo );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::VFCost::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "toolingCost", (double)toolingCost );
    _sdmStream->writeStructMember( "edndCost", (double)edndCost );
    _sdmStream->writeStructMember( "pieceCost", (double)pieceCost );
    _sdmStream->writeStructMember( "logisticCost", (double)logisticCost );
    _sdmStream->writeStructMember( "packingCost", (double)packingCost );
    _sdmStream->writeStructMember( "labourCost", (double)labourCost );
    _sdmStream->writeStructMember( "tax", (double)tax );
    _sdmStream->writeStructMember( "totalPieceCost", (double)totalPieceCost );
    _sdmStream->writeStructMember( "totalPieceCostStatus", totalPieceCostStatus );
    _sdmStream->writeStructMember( "costType", costType );
    _sdmStream->writeStructMember( "approvalDate", approvalDate );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, true );

}

void ReportDataSource::VFCostElement::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "cost", (double)cost );
    _sdmStream->writeStructMember( "costType", costType );
    _sdmStream->writeStructMember( "approvalDate", approvalDate );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, true );

}

void ReportDataSource::VFCostHistory::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "approvalDate", approvalDate );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"sapPieceCost", sapPieceCost );
    _sdmStream->writeStructMember( _prefix+"sapToolingCost", sapToolingCost );
    _sdmStream->writeStructMember( _prefix+"sapEdndCost", sapEdndCost );
    _sdmStream->writeStructMember( _prefix+"stCost", stCost );
    _sdmStream->writeStructMember( _prefix+"ecrCost", ecrCost );
    _sdmStream->writeStructMember( _prefix+"engCost", engCost );
    _sdmStream->writeStructMember( _prefix+"targetCost", targetCost );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::VFEcrCostResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeStructMember( _prefix+"ecrAndPartsCost", ecrAndPartsCost, false, false,  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::VFPartChange::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"partChanges", partChanges );
    _sdmStream->writeStructMember( _prefix+"deltaPartCost", deltaPartCost );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::VFPartCost::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "partNumber", partNumber );
    _sdmStream->writeStructMember( "partChangeType", partChangeType,  VF4::Soa::Custom::_2020_10::ReportDataSource::PARTCHANGETYPE_TO_STRING_MAP );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"partCost", partCost );
    _sdmStream->writeStructMember( _prefix+"pieceCost", pieceCost );
    _sdmStream->writeStructMember( _prefix+"packingCost", packingCost );
    _sdmStream->writeStructMember( _prefix+"logisticCost", logisticCost );
    _sdmStream->writeStructMember( _prefix+"labourCost", labourCost );
    _sdmStream->writeStructMember( _prefix+"tax", tax );
    _sdmStream->writeStructMember( _prefix+"toolingCost", toolingCost );
    _sdmStream->writeStructMember( _prefix+"edndCost", edndCost );
    _sdmStream->writeStructMember( _prefix+"costHistory", costHistory );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName  );
}

void ReportDataSource::VFPartCostCalcResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"result", result, true, false,  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE );
    _sdmStream->writeStructMember( _prefix+"errorMessages", errorMessages );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_10::ReportDataSource::XSD_NAMESPACE, elementName  );
}




ReportDataSource::ReportDataSource()
{
}

ReportDataSource::~ReportDataSource()
{
}

