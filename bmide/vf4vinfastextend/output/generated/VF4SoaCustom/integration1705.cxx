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

#include <integration1705.hxx>


#include <teamcenter/soa/internal/server/SdmParser.hxx>
#include <teamcenter/soa/internal/server/SdmStream.hxx>

using namespace VF4::Soa::Custom::_2017_05;

const std::string Integration::XSD_NAMESPACE ="http://vf4.com/Schemas/Custom/2017-05/Integration";




void Integration::NominateSupplierInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "vfPartNumbers", vfPartNumbers );
    _sdmParser->parseStructMember( "supplierCode", supplierCode );
    _sdmParser->parseStructMember( "manufacturingLocation", manufacturingLocation );
    _sdmParser->parseStructMember( "isTriggerKarafEvent", isTriggerKarafEvent );
}




Integration::Integration()
{
}

Integration::~Integration()
{
}

