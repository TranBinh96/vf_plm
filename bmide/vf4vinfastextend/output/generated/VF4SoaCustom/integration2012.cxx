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

#include <integration2012.hxx>


#include <teamcenter/soa/internal/server/SdmParser.hxx>
#include <teamcenter/soa/internal/server/SdmStream.hxx>

using namespace VF4::Soa::Custom::_2020_12;

const std::string Integration::XSD_NAMESPACE ="http://vf4.com/Schemas/Custom/2020-12/Integration";




void Integration::StructureSearchInMPPInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "searchScope", searchScope );
    _sdmParser->parseStructMember( "queryName", queryName );
    _sdmParser->parseStructMember( "queryCriteria", queryCriteria );
    _sdmParser->parseStructMember( "queryCriteriaValues", queryCriteriaValues );
}

void Integration::StructureSearchInMPPOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2020_12::Integration::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "errorString", errorString );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2020_12::Integration::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2020_12::Integration::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"foundLines", foundLines );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2020_12::Integration::XSD_NAMESPACE, elementName  );
}




Integration::Integration()
{
}

Integration::~Integration()
{
}

