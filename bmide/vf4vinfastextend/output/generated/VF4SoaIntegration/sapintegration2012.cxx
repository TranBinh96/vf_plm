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

#include <sapintegration2012.hxx>


#include <teamcenter/soa/internal/server/SdmParser.hxx>
#include <teamcenter/soa/internal/server/SdmStream.hxx>

using namespace VF4::Soa::Integration::_2020_12;

const std::string SAPIntegration::XSD_NAMESPACE ="http://vf4.com/Schemas/Integration/2020-12/SAPIntegration";




void SAPIntegration::GetMasterMaterialsInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "partRevUIDs", partRevUIDs );
}

void SAPIntegration::GetMasterMaterialsOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "partType", partType );
    _sdmStream->writeStructMember( "revisionNumber", revisionNumber );
    _sdmStream->writeStructMember( "brdCode", brdCode );
    _sdmStream->writeStructMember( "functionalClass", functionalClass );
    _sdmStream->writeStructMember( "oldMaterialNumber", oldMaterialNumber );
    _sdmStream->writeStructMember( "materialType", materialType );
    _sdmStream->writeStructMember( "uom", uom );
    _sdmStream->writeStructMember( "gmPart", gmPart );
    _sdmStream->writeStructMember( "materialNumber", materialNumber );
    _sdmStream->writeStructMember( "description", description );
    _sdmStream->writeStructMember( "approvalClass", approvalClass );
    _sdmStream->writeStructMember( "traceablePart", traceablePart );
    _sdmStream->writeStructMember( "descriptionVietnamese", descriptionVietnamese );
    _sdmStream->writeStructMember( "tcItemType", tcItemType );
    _sdmStream->writeStructMember( "errorMessage", errorMessage );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"sapPlant", sapPlant );
    _sdmStream->writeCloseElement(  VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, elementName  );
}

void SAPIntegration::GetMasterMaterialsResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"outputs", outputs );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, elementName  );
}

void SAPIntegration::PlantInformation::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "plantCode", plantCode );
    _sdmStream->writeStructMember( "procurementType", procurementType );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, true );

}

void SAPIntegration::TransferOperationToJSONInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "operationID", operationID );
    _sdmParser->parseStructMember( "operationRevID", operationRevID );
    _sdmParser->parseStructMember( "json", json );
}

void SAPIntegration::TransferOperationToJSONInputs::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "inputs", inputs );
    _sdmParser->parseStructMember( "mesServerIP", mesServerIP );
    _sdmParser->parseStructMember( "mesSeverUser", mesSeverUser );
    _sdmParser->parseStructMember( "mesSeverPass", mesSeverPass );
    _sdmParser->parseStructMember( "mesServerClientID", mesServerClientID );
    _sdmParser->parseStructMember( "mesServerClientSecret", mesServerClientSecret );
    _sdmParser->parseStructMember( "mesServerScope", mesServerScope );
}

void SAPIntegration::TransferOperationToJSONResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &servicedata );

    _sdmStream->writeOpenElement2( VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE );

    _sdmStream->writeFrameworkMember( servicedata );
    _sdmStream->writeStructMember( _prefix+"operationIDs", operationIDs );
    _sdmStream->writeStructMember( _prefix+"operationMessages", operationMessages );
    _sdmStream->writeStructMember( _prefix+"returnCodes", returnCodes );
    _sdmStream->writeCloseElement(  VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, elementName  );
}

void SAPIntegration::TransferOperationToMESInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "operationID", operationID );
    _sdmParser->parseStructMember( "operationRevID", operationRevID );
    _sdmParser->parseStructMember( "xml", xml );
}

void SAPIntegration::TransferOperationToMESInputs::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "inputs", inputs );
    _sdmParser->parseStructMember( "mesServerIP", mesServerIP );
    _sdmParser->parseStructMember( "mesServerPort", mesServerPort );
    _sdmParser->parseStructMember( "shopName", shopName );
}

void SAPIntegration::TransferOperationToMESResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &servicedata );

    _sdmStream->writeOpenElement2( VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE );

    _sdmStream->writeFrameworkMember( servicedata );
    _sdmStream->writeStructMember( _prefix+"operationIDs", operationIDs );
    _sdmStream->writeStructMember( _prefix+"returnCodes", returnCodes );
    _sdmStream->writeCloseElement(  VF4::Soa::Integration::_2020_12::SAPIntegration::XSD_NAMESPACE, elementName  );
}




SAPIntegration::SAPIntegration()
{
}

SAPIntegration::~SAPIntegration()
{
}

