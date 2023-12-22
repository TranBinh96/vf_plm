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

#include <adminutils2012.hxx>


#include <teamcenter/soa/internal/server/SdmParser.hxx>
#include <teamcenter/soa/internal/server/SdmStream.hxx>

using namespace VF4::Soa::AdminUtils::_2020_12;

const std::string AdminUtils::XSD_NAMESPACE ="http://vf4.com/Schemas/AdminUtils/2020-12/AdminUtils";




void AdminUtils::ChangeUOMInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "partNumber", partNumber );
    _sdmParser->parseStructMember( "partType", partType );
    _sdmParser->parseStructMember( "partUOM", partUOM );
}

void AdminUtils::ChangeUOMOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "partNumber", partNumber );
    _sdmStream->writeStructMember( "partType", partType );
    _sdmStream->writeStructMember( "partUOM", partUOM );
    _sdmStream->writeStructMember( "errorMessage", errorMessage );
    _sdmStream->writeOpenElementClose(  VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, true );

}

void AdminUtils::ChangeUOMResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"outputs", outputs );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, elementName  );
}

void AdminUtils::PropertyAndValues::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "property", property );
    _sdmParser->parseStructMember( "values", values );
}

void AdminUtils::ReassignWFTaskInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "userId", userId );
    _sdmParser->parseStructMember( "processTemplateName", processTemplateName );
    _sdmParser->parseStructMember( "newUserId", newUserId );
    _sdmParser->parseStructMember( "newUserGroup", newUserGroup );
    _sdmParser->parseStructMember( "executingProcessesNum", executingProcessesNum );
    _sdmParser->parseStructMember( "isDryRun", isDryRun );
    _sdmParser->parseStructMember( "objectType", objectType );
    _sdmParser->parseStructMember( "propertyNameAndValue", propertyNameAndValue );
}

void AdminUtils::ReassignWFTaskOutput::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "templateName", templateName );
    _sdmStream->writeStructMember( "taskName", taskName );
    _sdmStream->writeStructMember( "taskType", taskType );
    _sdmStream->writeStructMember( "assignedUsers", assignedUsers );
    _sdmStream->writeStructMember( "returnStatus", returnStatus );
    _sdmStream->writeStructMember( "errorMessage", errorMessage );
    _sdmStream->writeOpenElementClose(  VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"reasignedTask", reasignedTask );
    _sdmStream->writeCloseElement(  VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, elementName  );
}

void AdminUtils::ReassignWFTaskResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"outputs", outputs );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, elementName  );
}

void AdminUtils::SetPropertyError::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{


    _sdmStream->writeOpenElement2( VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, elementName );
    _sdmStream->writeStructMember( "errorString", errorString );
    _sdmStream->writeOpenElementClose(  VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"errorObject", errorObject );
    _sdmStream->writeCloseElement(  VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, elementName  );
}

void AdminUtils::SetPropertyInput::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "uid", uid );
    _sdmParser->parseStructMember( "propertyAndValues", propertyAndValues );
}

void AdminUtils::SetPropertyResponse::serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName )
{
    _sdmStream->setServiceData( &serviceData );

    _sdmStream->writeOpenElement2( VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, elementName );
    _sdmStream->writeOpenElementClose(  VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, false );
    std::string _prefix =  _sdmStream->getNamespacePrefix( VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE );

    _sdmStream->writeStructMember( _prefix+"errors", errors );
    _sdmStream->writeFrameworkMember( serviceData );
    _sdmStream->writeCloseElement(  VF4::Soa::AdminUtils::_2020_12::AdminUtils::XSD_NAMESPACE, elementName  );
}

void AdminUtils::WFTaskPropertyAndValue::parse( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser )
{
    _sdmParser->parseStructMember( "propertyName", propertyName );
    _sdmParser->parseStructMember( "propertyValue", propertyValue );
}




AdminUtils::AdminUtils()
{
}

AdminUtils::~AdminUtils()
{
}

