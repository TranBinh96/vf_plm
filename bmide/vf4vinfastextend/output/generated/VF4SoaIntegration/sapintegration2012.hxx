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

#ifndef TEAMCENTER_SERVICES_INTEGRATION_2020_12_SAPINTEGRATION_HXX 
#define TEAMCENTER_SERVICES_INTEGRATION_2020_12_SAPINTEGRATION_HXX


#include <metaframework/BusinessObject.hxx>
#include <teamcenter/soa/server/ServiceData.hxx>



#include <teamcenter/soa/server/ServiceException.hxx>
#include <metaframework/BusinessObjectRef.hxx>

#include <Integration_exports.h>

namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmStream; }}}}
namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmParser; }}}}
namespace VF4 { namespace Services { namespace Integration { namespace _2020_12 { class SAPIntegrationIiopSkeleton; }}}}


namespace VF4
{
    namespace Soa
    {
        namespace Integration
        {
            namespace _2020_12
            {
                class SAPIntegration;
            }
        }
    }
}


class SOAINTEGRATION_API VF4::Soa::Integration::_2020_12::SAPIntegration

{
public:

    static const std::string XSD_NAMESPACE;

    struct GetMasterMaterialsInput;
    struct GetMasterMaterialsOutput;
    struct GetMasterMaterialsResponse;
    struct PlantInformation;
    struct TransferOperationToJSONInput;
    struct TransferOperationToJSONInputs;
    struct TransferOperationToJSONResponse;
    struct TransferOperationToMESInput;
    struct TransferOperationToMESInputs;
    struct TransferOperationToMESResponse;

    struct  GetMasterMaterialsInput
    {
        /**
         * PART REV UIDs
         */
        BusinessObjectRef<Teamcenter::BusinessObject> partRevUIDs;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Integration::_2020_12::SAPIntegrationIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  GetMasterMaterialsOutput
    {
        /**
         * ITEM_PART_CATEGORY
         */
        std::string partType;
        /**
         * ITEM_REV_ID
         */
        std::string revisionNumber;
        /**
         * REV_BROADCODE
         */
        std::string brdCode;
        /**
         * REV_FUNC_CODE
         */
        std::string functionalClass;
        /**
         * ITEM_OLD_PARTNO
         */
        std::string oldMaterialNumber;
        /**
         * ITEM_MATERIAL_TYPE
         */
        std::string materialType;
        /**
         * ITEM_UOM
         */
        std::string uom;
        /**
         * ITEM_GM_PARTNO
         */
        std::string gmPart;
        /**
         * ITEM_ID
         */
        std::string materialNumber;
        /**
         * REV_NAME
         */
        std::string description;
        /**
         * ITEM_APPOV_CODE
         */
        std::string approvalClass;
        /**
         * ITEM_IS_TRACEABLE
         */
        std::string traceablePart;
        /**
         * REV_VIET_DESCRIPTION
         */
        std::string descriptionVietnamese;
        /**
         * ITEM_TYPE
         */
        std::string tcItemType;
        /**
         * list of plant information structure
         */
        std::vector< PlantInformation > sapPlant;
        /**
         * custom error message
         */
        std::string errorMessage;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Integration::_2020_12::SAPIntegrationIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="GetMasterMaterialsOutput" );
    };

    struct  GetMasterMaterialsResponse
    {
        /**
         * output for soa service
         */
        std::vector< GetMasterMaterialsOutput > outputs;
        /**
         * service data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Integration::_2020_12::SAPIntegrationIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="GetMasterMaterialsResponse" );
    };

    struct  PlantInformation
    {
        /**
         * Plant Code
         */
        std::string plantCode;
        /**
         * Procurement Type
         */
        std::string procurementType;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Integration::_2020_12::SAPIntegrationIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="PlantInformation" );
    };

    struct  TransferOperationToJSONInput
    {
        /**
         * Operation ID
         */
        std::string operationID;
        /**
         * Operation REV ID
         */
        std::string operationRevID;
        /**
         * operation JSON
         */
        std::string json;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Integration::_2020_12::SAPIntegrationIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  TransferOperationToJSONInputs
    {
        /**
         * Input operation
         */
        std::vector< TransferOperationToJSONInput > inputs;
        /**
         * MES server IP
         */
        std::string mesServerIP;
        /**
         * MES server Username
         */
        std::string mesSeverUser;
        /**
         * MES sever Password
         */
        std::string mesSeverPass;
        /**
         * MES server Clinet ID
         */
        std::string mesServerClientID;
        /**
         * MES server Client Secret
         */
        std::string mesServerClientSecret;
        /**
         * MES server Scope
         */
        std::string mesServerScope;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Integration::_2020_12::SAPIntegrationIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  TransferOperationToJSONResponse
    {
        /**
         * Servicedata
         */
        Teamcenter::Soa::Server::ServiceData servicedata;
        /**
         * OperationIDs
         */
        std::vector< std::string > operationIDs;
        /**
         * Operation Messages
         */
        std::vector< std::string > operationMessages;
        /**
         * Return code received from MES
         */
        std::vector< std::string > returnCodes;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Integration::_2020_12::SAPIntegrationIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="TransferOperationToJSONResponse" );
    };

    struct  TransferOperationToMESInput
    {
        /**
         * Operation ID
         */
        std::string operationID;
        /**
         * Operation REV ID
         */
        std::string operationRevID;
        /**
         * Operation XML
         */
        std::string xml;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Integration::_2020_12::SAPIntegrationIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  TransferOperationToMESInputs
    {
        /**
         * Input operations
         */
        std::vector< TransferOperationToMESInput > inputs;
        /**
         * MES server IP
         */
        std::string mesServerIP;
        /**
         * MES server port
         */
        int mesServerPort;
        /**
         * Shop name
         */
        std::string shopName;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Integration::_2020_12::SAPIntegrationIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  TransferOperationToMESResponse
    {
        /**
         * Servicedata
         */
        Teamcenter::Soa::Server::ServiceData servicedata;
        /**
         * Operation IDs
         */
        std::vector< std::string > operationIDs;
        /**
         * Return code received from MES
         */
        std::vector< std::string > returnCodes;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Integration::_2020_12::SAPIntegrationIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="TransferOperationToMESResponse" );
    };



    SAPIntegration();
    virtual ~SAPIntegration();
    

    /**
     * .
     *
     * @param inputs
     *        inputs
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual GetMasterMaterialsResponse getMasterMaterials ( const std::vector< GetMasterMaterialsInput >& inputs ) = 0;

    /**
     * .
     *
     * @param inputs
     *        inputs
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual TransferOperationToJSONResponse transferOperationJSONToMES ( const TransferOperationToJSONInputs& inputs ) = 0;

    /**
     * .
     *
     * @param inputs
     *        inputs
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual TransferOperationToMESResponse transferOperationToMES ( const TransferOperationToMESInputs& inputs ) = 0;


};

#include <Integration_undef.h>
#endif

