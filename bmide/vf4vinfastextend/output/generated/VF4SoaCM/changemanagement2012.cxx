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

#include <changemanagement2012.hxx>


#include <teamcenter/soa/internal/server/SdmParser.hxx>
#include <teamcenter/soa/internal/server/SdmStream.hxx>

using namespace VF4::Soa::CM::_2020_12;

const std::string ChangeManagement::XSD_NAMESPACE ="http://vf4.com/Schemas/CM/2020-12/ChangeManagement";




void ChangeManagement::DeriveChangeInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "changeObjRev", changeObjRev );
    _sdmParser->parseStructMember( "propertyAndValues", propertyAndValues );
    _sdmParser->parseStructMember( "relationToAttachChangeObj", relationToAttachChangeObj );
    _sdmParser->parseStructMember( "derivedObjType", derivedObjType );
}

void ChangeManagement::DeriveChangeOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "errorMessage", errorMessage );
    _sdmStream->writeOpenElementClose(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"chnageObjTag", chnageObjTag );
    _sdmStream->writeCloseElement(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName  );
}

void ChangeManagement::DeriveChangeResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"outputs", outputs );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName  );
}

void ChangeManagement::ImpactedProgramInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "impactedParts", impactedParts );
    _sdmParser->parseStructMember( "topNodeType", topNodeType );
    _sdmParser->parseStructMember( "revisionRule", revisionRule );
}

void ChangeManagement::ImpactedProgramResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"outputs", outputs, false, false,  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName  );
}

void ChangeManagement::PartNumberChangeHistory::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "partString", partString );
    _sdmStream->writeStructMember( "oldPartString", oldPartString );
    _sdmStream->writeStructMember( "ecrString", ecrString );
    _sdmStream->writeStructMember( "saCost", (double)saCost );
    _sdmStream->writeStructMember( "saCurrency", saCurrency );
    _sdmStream->writeStructMember( "toolingCost", (double)toolingCost );
    _sdmStream->writeStructMember( "toolingCurrency", toolingCurrency );
    _sdmStream->writeStructMember( "edndCost", (double)edndCost );
    _sdmStream->writeStructMember( "edndCurrency", edndCurrency );
    _sdmStream->writeStructMember( "totalCost", (double)totalCost );
    _sdmStream->writeStructMember( "totalCostStatus", totalCostStatus );
    _sdmStream->writeOpenElementClose(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"partRev", partRev );
    _sdmStream->writeStructMember( _prefix+"oldPartRev", oldPartRev );
    _sdmStream->writeStructMember( _prefix+"ecrRev", ecrRev );
    _sdmStream->writeStructMember( _prefix+"sourcePart", sourcePart );
    _sdmStream->writeCloseElement(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName  );
}

void ChangeManagement::PartNumberChangeHistoryInput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "partNumber", partNumber );
    _sdmStream->writeStructMember( "partType", partType );
    _sdmStream->writeStructMember( "isIncludeCost", (bool)isIncludeCost );
    _sdmStream->writeOpenElementClose(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, true );

}

void ChangeManagement::PartNumberChangeHistoryInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "partNumber", partNumber );
    _sdmParser->parseStructMember( "partType", partType );
    _sdmParser->parseStructMember( "isIncludeCost", isIncludeCost );
}

void ChangeManagement::PartNumberChangeHistoryOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"input", input );
    _sdmStream->writeStructMember( _prefix+"historyList", historyList );
    _sdmStream->writeCloseElement(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName  );
}

void ChangeManagement::PartNumberChangeHistoryResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &servicedata );

    _sdmStream->writeOpenElement2( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"outputs", outputs );
    _sdmStream->writeFrameworkMember( servicedata );
    _sdmStream->writeCloseElement(  VF4::Soa::CM::_2020_12::ChangeManagement::XSD_NAMESPACE, elementName  );
}

void ChangeManagement::PropertyAndValues::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "propertyName", propertyName );
    _sdmParser->parseStructMember( "propertyValue", propertyValue );
}




ChangeManagement::ChangeManagement()
{
}

ChangeManagement::~ChangeManagement()
{
}

