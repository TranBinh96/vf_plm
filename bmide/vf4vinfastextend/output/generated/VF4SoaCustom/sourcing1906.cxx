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

#include <sourcing1906.hxx>


#include <teamcenter/soa/internal/server/SdmParser.hxx>
#include <teamcenter/soa/internal/server/SdmStream.hxx>

using namespace VF4::Soa::Custom::_2019_06;

const std::string Sourcing::XSD_NAMESPACE ="http://vf4.com/Schemas/Custom/2019-06/Sourcing";




void Sourcing::STResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::Custom::_2019_06::Sourcing::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::Custom::_2019_06::Sourcing::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::Custom::_2019_06::Sourcing::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"attrDisplayNames", attrDisplayNames, true, true,  VF4::Soa::Custom::_2019_06::Sourcing::XSD_NAMESPACE );
    _sdmStream->writeStructMember( _prefix+"attrActualValues", attrActualValues, true, false,  VF4::Soa::Custom::_2019_06::Sourcing::XSD_NAMESPACE );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::Custom::_2019_06::Sourcing::XSD_NAMESPACE, elementName  );
}




Sourcing::Sourcing()
{
}

Sourcing::~Sourcing()
{
}

