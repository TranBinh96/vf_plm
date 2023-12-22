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

#ifndef TEAMCENTER_SERVICES_ADMINUTILS_2020_12_ADMINUTILS_HXX 
#define TEAMCENTER_SERVICES_ADMINUTILS_2020_12_ADMINUTILS_HXX


#include <metaframework/BusinessObject.hxx>
#include <teamcenter/soa/server/ServiceData.hxx>



#include <teamcenter/soa/server/ServiceException.hxx>
#include <metaframework/BusinessObjectRef.hxx>

#include <AdminUtils_exports.h>

namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmStream; }}}}
namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmParser; }}}}
namespace VF4 { namespace Services { namespace AdminUtils { namespace _2020_12 { class AdminUtilsIiopSkeleton; }}}}


namespace VF4
{
    namespace Soa
    {
        namespace AdminUtils
        {
            namespace _2020_12
            {
                class AdminUtils;
            }
        }
    }
}


class SOAADMINUTILS_API VF4::Soa::AdminUtils::_2020_12::AdminUtils

{
public:

    static const std::string XSD_NAMESPACE;

    struct ChangeUOMInput;
    struct ChangeUOMOutput;
    struct ChangeUOMResponse;
    struct PropertyAndValues;
    struct ReassignWFTaskInput;
    struct ReassignWFTaskOutput;
    struct ReassignWFTaskResponse;
    struct SetPropertyError;
    struct SetPropertyInput;
    struct SetPropertyResponse;
    struct WFTaskPropertyAndValue;

    struct  ChangeUOMInput
    {
        /**
         * Part Number of the Item/Part
         */
        std::string partNumber;
        /**
         * Part Type of the Item/Part
         */
        std::string partType;
        /**
         * Part Unit of Measure
         */
        std::string partUOM;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::AdminUtils::_2020_12::AdminUtilsIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  ChangeUOMOutput
    {
        /**
         * Part Number of the Item/Part
         */
        std::string partNumber;
        /**
         * Part Type of the Item/Part
         */
        std::string partType;
        /**
         * Part/Item Unit of Measure
         */
        std::string partUOM;
        /**
         * Error Message for the Service
         */
        std::string errorMessage;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::AdminUtils::_2020_12::AdminUtilsIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="ChangeUOMOutput" );
    };

    struct  ChangeUOMResponse
    {
        /**
         * ChangeUOM Response
         */
        std::vector< ChangeUOMOutput > outputs;
        /**
         * Service Data  for ChangeUOM
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::AdminUtils::_2020_12::AdminUtilsIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="ChangeUOMResponse" );
    };

    struct  PropertyAndValues
    {
        /**
         * property which needs to be change
         */
        std::string property;
        /**
         * vector of property values
         */
        std::vector< std::string > values;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::AdminUtils::_2020_12::AdminUtilsIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  ReassignWFTaskInput
    {
        /**
         * Currently Assigned User Id
         */
        std::string userId;
        /**
         * Process Template Name
         */
        std::string processTemplateName;
        /**
         * New User Id
         */
        std::string newUserId;
        /**
         * New User Group
         */
        std::string newUserGroup;
        /**
         * Number of processes executed from query result form the first record.
         */
        int executingProcessesNum;
        /**
         * isDryRun
         */
        bool isDryRun;
        /**
         * object Type
         */
        std::string objectType;
        /**
         * vector of property Name And Value
         */
        std::vector< WFTaskPropertyAndValue > propertyNameAndValue;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::AdminUtils::_2020_12::AdminUtilsIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  ReassignWFTaskOutput
    {
        /**
         * Reassigned task
         */
        BusinessObjectRef<Teamcenter::BusinessObject> reasignedTask;
        /**
         * Process Template Name
         */
        std::string templateName;
        /**
         * taskName
         */
        std::string taskName;
        /**
         * taskType
         */
        std::string taskType;
        /**
         * assigned/signoff Users
         */
        std::string assignedUsers;
        /**
         * return status
         */
        std::string returnStatus;
        /**
         * Error Message
         */
        std::string errorMessage;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::AdminUtils::_2020_12::AdminUtilsIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="ReassignWFTaskOutput" );
    };

    struct  ReassignWFTaskResponse
    {
        /**
         * ReassignWFTask Response
         */
        std::vector< ReassignWFTaskOutput > outputs;
        /**
         * Service Data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::AdminUtils::_2020_12::AdminUtilsIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="ReassignWFTaskResponse" );
    };

    struct  SetPropertyError
    {
        /**
         * error Object
         */
        BusinessObjectRef<Teamcenter::BusinessObject> errorObject;
        /**
         * error string
         */
        std::string errorString;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::AdminUtils::_2020_12::AdminUtilsIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="SetPropertyError" );
    };

    struct  SetPropertyInput
    {
        /**
         * object uid
         */
        std::string uid;
        /**
         * vector of property and values
         */
        std::vector< PropertyAndValues > propertyAndValues;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::AdminUtils::_2020_12::AdminUtilsIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  SetPropertyResponse
    {
        /**
         * error
         */
        std::vector< SetPropertyError > errors;
        /**
         * serviceData
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::AdminUtils::_2020_12::AdminUtilsIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="SetPropertyResponse" );
    };

    struct  WFTaskPropertyAndValue
    {
        /**
         * PropertyName
         */
        std::string propertyName;
        /**
         * Property Value
         */
        std::string propertyValue;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::AdminUtils::_2020_12::AdminUtilsIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };



    AdminUtils();
    virtual ~AdminUtils();
    

    /**
     * .
     *
     * @param inputs
     *        input for ChangeUOM
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual ChangeUOMResponse changeUOM ( const std::vector< ChangeUOMInput >& inputs ) = 0;

    /**
     * .
     *
     * @param inputs
     *        input of reassignWFTask service
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual ReassignWFTaskResponse reassignWFTask ( const std::vector< ReassignWFTaskInput >& inputs ) = 0;

    /**
     * .
     *
     * @param inputs
     *        inputs
     *
     * @param isKeepModifiedDate
     *        flag to Keep Modified Date
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual SetPropertyResponse setProperties ( const std::vector< SetPropertyInput >& inputs,
        bool isKeepModifiedDate ) = 0;


};

#include <AdminUtils_undef.h>
#endif

