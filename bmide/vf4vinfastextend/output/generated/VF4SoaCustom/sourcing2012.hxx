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

#ifndef TEAMCENTER_SERVICES_CUSTOM_2020_12_SOURCING_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2020_12_SOURCING_HXX


#include <metaframework/BusinessObject.hxx>
#include <teamcenter/soa/server/ServiceData.hxx>



#include <teamcenter/soa/server/ServiceException.hxx>
#include <metaframework/BusinessObjectRef.hxx>

#include <Custom_exports.h>

namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmStream; }}}}
namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmParser; }}}}
namespace VF4 { namespace Services { namespace Custom { namespace _2020_12 { class SourcingIiopSkeleton; }}}}


namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2020_12
            {
                class Sourcing;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2020_12::Sourcing

{
public:

    static const std::string XSD_NAMESPACE;

    struct AssignSourcePartToBuyerOutput;
    struct AssignSourcePartToBuyerResponse;
    struct GetOrCreateVFCostInput;
    struct GetOrCreateVFCostOutput;
    struct GetOrCreateVFCostResponse;
    struct STBuyerAssignmentInfo;

    struct  AssignSourcePartToBuyerOutput
    {
        /**
         * VF Part Number
         */
        std::string vfPartNumber;
        /**
         * Sourcing Program
         */
        std::string sourcingProgram;
        /**
         * Error message
         */
        std::string errorMessage;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_12::SourcingIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="AssignSourcePartToBuyerOutput" );
    };

    struct  AssignSourcePartToBuyerResponse
    {
        /**
         * List of ouput
         */
        std::vector< AssignSourcePartToBuyerOutput > outputs;
        /**
         * Service Data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_12::SourcingIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="AssignSourcePartToBuyerResponse" );
    };

    struct  GetOrCreateVFCostInput
    {
        /**
         * Part number
         */
        std::string partNumber;
        /**
         * Revision id of the part
         */
        std::string partRevId;
        /**
         * Part type
         */
        std::string partType;
        /**
         * Create and relate VF Cost object to part if not existed
         */
        bool createIfNotExisted;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Custom::_2020_12::SourcingIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  GetOrCreateVFCostOutput
    {
        /**
         * Part number
         */
        std::string partNumber;
        /**
         * Part type
         */
        std::string partType;
        /**
         * VF Cost object
         */
        BusinessObjectRef<Teamcenter::BusinessObject> vfCost;
        /**
         * Error message (if any)
         */
        std::string errorMessage;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_12::SourcingIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="GetOrCreateVFCostOutput" );
    };

    struct  GetOrCreateVFCostResponse
    {
        /**
         * List of output
         */
        std::vector< GetOrCreateVFCostOutput > outputs;
        /**
         * Service data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_12::SourcingIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="GetOrCreateVFCostResponse" );
    };

    struct  STBuyerAssignmentInfo
    {
        /**
         * VF Part Number to search source part
         */
        std::string vfPartNumber;
        /**
         * Sourcing Program
         */
        std::string sourcingProgram;
        /**
         * Assigning TC user ID
         */
        std::string assigningUserID;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Custom::_2020_12::SourcingIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };



    Sourcing();
    virtual ~Sourcing();
    

    /**
     * .
     *
     * @param inputs
     *        List of assignment info
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual AssignSourcePartToBuyerResponse assignSourcePartToBuyer ( const std::vector< STBuyerAssignmentInfo >& inputs ) = 0;

    /**
     * .
     *
     * @param inputs
     *        List of inputs
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual GetOrCreateVFCostResponse getOrCreateVFCost ( const std::vector< GetOrCreateVFCostInput >& inputs ) = 0;


};

#include <Custom_undef.h>
#endif

