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

#include <sourcing2012.hxx>


#include <teamcenter/soa/internal/server/SdmParser.hxx>
#include <teamcenter/soa/internal/server/SdmStream.hxx>

using namespace VF4::Soa::Custom::_2020_12;

const std::string Sourcing::XSD_NAMESPACE ="http://vf4.com/Schemas/Custom/2020-12/Sourcing";




void Sourcing::AssignSourcePartToBuyerOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "vfPartNumber", vfPartNumber );
    _sdmStream->writeStructMember( "sourcingProgram", sourcingProgram );
    _sdmStream->writeStructMember( "errorMessage", errorMessage );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE, true );

}

void Sourcing::AssignSourcePartToBuyerResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"outputs", outputs );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE, elementName  );
}

void Sourcing::GetOrCreateVFCostInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "partNumber", partNumber );
    _sdmParser->parseStructMember( "partRevId", partRevId );
    _sdmParser->parseStructMember( "partType", partType );
    _sdmParser->parseStructMember( "createIfNotExisted", createIfNotExisted );
}

void Sourcing::GetOrCreateVFCostOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "partNumber", partNumber );
    _sdmStream->writeStructMember( "partType", partType );
    _sdmStream->writeStructMember( "errorMessage", errorMessage );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"vfCost", vfCost );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE, elementName  );
}

void Sourcing::GetOrCreateVFCostResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"outputs", outputs );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_12::Sourcing::XSD_NAMESPACE, elementName  );
}

void Sourcing::STBuyerAssignmentInfo::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "vfPartNumber", vfPartNumber );
    _sdmParser->parseStructMember( "sourcingProgram", sourcingProgram );
    _sdmParser->parseStructMember( "assigningUserID", assigningUserID );
}




Sourcing::Sourcing()
{
}

Sourcing::~Sourcing()
{
}

