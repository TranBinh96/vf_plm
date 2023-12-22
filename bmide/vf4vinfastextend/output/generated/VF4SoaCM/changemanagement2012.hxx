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

#ifndef TEAMCENTER_SERVICES_CM_2020_12_CHANGEMANAGEMENT_HXX 
#define TEAMCENTER_SERVICES_CM_2020_12_CHANGEMANAGEMENT_HXX


#include <metaframework/BusinessObject.hxx>
#include <teamcenter/soa/server/ServiceData.hxx>



#include <teamcenter/soa/server/ServiceException.hxx>
#include <metaframework/BusinessObjectRef.hxx>

#include <CM_exports.h>

namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmStream; }}}}
namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmParser; }}}}
namespace VF4 { namespace Services { namespace CM { namespace _2020_12 { class ChangeManagementIiopSkeleton; }}}}


namespace VF4
{
    namespace Soa
    {
        namespace CM
        {
            namespace _2020_12
            {
                class ChangeManagement;
            }
        }
    }
}


class SOACM_API VF4::Soa::CM::_2020_12::ChangeManagement

{
public:

    static const std::string XSD_NAMESPACE;

    struct DeriveChangeInput;
    struct DeriveChangeOutput;
    struct DeriveChangeResponse;
    struct ImpactedProgramInput;
    struct ImpactedProgramResponse;
    struct PartNumberChangeHistory;
    struct PartNumberChangeHistoryInput;
    struct PartNumberChangeHistoryOutput;
    struct PartNumberChangeHistoryResponse;
    struct PropertyAndValues;

    typedef std::map< BusinessObjectRef<Teamcenter::BusinessObject>, std::vector< BusinessObjectRef<Teamcenter::BusinessObject> > > ImpactedProgramOutput;

    struct  DeriveChangeInput
    {
        /**
         * tag of ECR revision
         */
        BusinessObjectRef<Teamcenter::BusinessObject> changeObjRev;
        /**
         * list of mandotory property and values to set
         */
        std::vector< PropertyAndValues > propertyAndValues;
        /**
         * ECN &amp; ECR relation
         */
        std::string relationToAttachChangeObj;
        /**
         * ECN object type to be created
         */
        std::string derivedObjType;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::CM::_2020_12::ChangeManagementIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  DeriveChangeOutput
    {
        /**
         * Derived ECN object
         */
        BusinessObjectRef<Teamcenter::BusinessObject> chnageObjTag;
        /**
         * Error Message
         */
        std::string errorMessage;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::CM::_2020_12::ChangeManagementIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="DeriveChangeOutput" );
    };

    struct  DeriveChangeResponse
    {
        /**
         * Service output
         */
        std::vector< DeriveChangeOutput > outputs;
        /**
         * Service Data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::CM::_2020_12::ChangeManagementIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="DeriveChangeResponse" );
    };

    struct  ImpactedProgramInput
    {
        /**
         * Impacted parts
         */
        std::vector< BusinessObjectRef<Teamcenter::BusinessObject> > impactedParts;
        /**
         * topNodeType
         */
        std::vector< std::string > topNodeType;
        /**
         * Revision Rule
         */
        std::string revisionRule;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::CM::_2020_12::ChangeManagementIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  ImpactedProgramResponse
    {
        /**
         * Map of outputs
         */
        ImpactedProgramOutput outputs;
        /**
         * Service Data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::CM::_2020_12::ChangeManagementIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="ImpactedProgramResponse" );
    };

    struct  PartNumberChangeHistory
    {
        /**
         * Part revision which may has old part number accompany with relevant ECR
         */
        BusinessObjectRef<Teamcenter::BusinessObject> partRev;
        /**
         * Old part revision of the partRev
         */
        BusinessObjectRef<Teamcenter::BusinessObject> oldPartRev;
        /**
         * Relevant ECR revision to the oldRev and partRev
         */
        BusinessObjectRef<Teamcenter::BusinessObject> ecrRev;
        /**
         * First source part get from latest working revision
         */
        BusinessObjectRef<Teamcenter::BusinessObject> sourcePart;
        /**
         * Part object_string
         */
        std::string partString;
        /**
         * Old part number object_string
         */
        std::string oldPartString;
        /**
         * ECR string
         */
        std::string ecrString;
        /**
         * SA cost
         */
        double saCost;
        /**
         * SA currency
         */
        std::string saCurrency;
        /**
         * Tooling cost
         */
        double toolingCost;
        /**
         * Tooling currency
         */
        std::string toolingCurrency;
        /**
         * EDnD cost
         */
        double edndCost;
        /**
         * EDnD currency
         */
        std::string edndCurrency;
        /**
         * Total cost
         */
        double totalCost;
        /**
         * Total cost status
         */
        std::string totalCostStatus;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::CM::_2020_12::ChangeManagementIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="PartNumberChangeHistory" );
    };

    struct  PartNumberChangeHistoryInput
    {
        /**
         * Part number desired to search part number change's history
         */
        std::string partNumber;
        /**
         * Part type of the partNumber
         */
        std::string partType;
        /**
         * Is include cost
         */
        bool isIncludeCost;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::CM::_2020_12::ChangeManagementIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="PartNumberChangeHistoryInput" );
        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  PartNumberChangeHistoryOutput
    {
        /**
         * Part number change's history input
         */
        PartNumberChangeHistoryInput input;
        /**
         * History list
         */
        std::vector< PartNumberChangeHistory > historyList;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::CM::_2020_12::ChangeManagementIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="PartNumberChangeHistoryOutput" );
    };

    struct  PartNumberChangeHistoryResponse
    {
        /**
         * Output list which is respectively to input list
         */
        std::vector< PartNumberChangeHistoryOutput > outputs;
        /**
         * Service data
         */
        Teamcenter::Soa::Server::ServiceData servicedata;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::CM::_2020_12::ChangeManagementIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="PartNumberChangeHistoryResponse" );
    };

    struct  PropertyAndValues
    {
        /**
         * Name of the Property
         */
        std::string propertyName;
        /**
         * Value for the Property
         */
        std::string propertyValue;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::CM::_2020_12::ChangeManagementIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };



    ChangeManagement();
    virtual ~ChangeManagement();
    

    /**
     * Operation for creating ECN from ECR.
     *
     * @param inputs
     *        input
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual DeriveChangeResponse deriveChange ( const std::vector< DeriveChangeInput >& inputs ) = 0;

    /**
     * .
     *
     * @param inputs
     *        input for getImpactedPrograms SOA service
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual ImpactedProgramResponse getImpactedPrograms ( const ImpactedProgramInput& inputs ) = 0;

    /**
     * Return part number change's history which accompanies with relevant ECR.
     *
     * @param inputs
     *        Inputs
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual PartNumberChangeHistoryResponse getPartNumberChangeHistory ( const std::vector< PartNumberChangeHistoryInput >& inputs ) = 0;


};

#include <CM_undef.h>
#endif

