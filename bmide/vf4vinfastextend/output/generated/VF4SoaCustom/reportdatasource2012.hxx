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

#ifndef TEAMCENTER_SERVICES_CUSTOM_2020_12_REPORTDATASOURCE_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2020_12_REPORTDATASOURCE_HXX


#include <base_utils/DateTime.hxx>
#include <bom/BOMLine.hxx>
#include <metaframework/BusinessObject.hxx>
#include <teamcenter/soa/server/ServiceData.hxx>



#include <teamcenter/soa/server/ServiceException.hxx>
#include <metaframework/BusinessObjectRef.hxx>

#include <Custom_exports.h>

namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmStream; }}}}
namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmParser; }}}}
namespace VF4 { namespace Services { namespace Custom { namespace _2020_12 { class ReportDataSourceIiopSkeleton; }}}}


namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2020_12
            {
                class ReportDataSource;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2020_12::ReportDataSource

{
public:

    static const std::string XSD_NAMESPACE;

    struct Get150BomWithVariantsDSInput;
    struct Get150BomWithVariantsDSOutput;
    struct Get150BomWithVariantsDSResponse;
    struct PFEPReportInput;
    struct PFEPReportOutput;
    struct PFEPReportResponse;
    struct ProcessStatusReportInput;
    struct ProcessStatusReportOutput;
    struct ProcessStatusReportResponse;

    struct  Get150BomWithVariantsDSInput
    {
        /**
         * top line tag
         */
        BusinessObjectRef<Teamcenter::BOMLine> toplineTag;
        /**
         * TopNode Item Id where Configaration Context is attached
         */
        std::string topItemId;
        /**
         * TopNode Item Type
         */
        std::string topItemType;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Custom::_2020_12::ReportDataSourceIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  Get150BomWithVariantsDSOutput
    {
        /**
         * Level
         */
        std::string level;
        /**
         * BOMLINE Tag
         */
        BusinessObjectRef<Teamcenter::BOMLine> bomlineTag;
        /**
         * Vector of Variant Rule tag
         */
        std::vector< BusinessObjectRef<Teamcenter::BusinessObject> > variantRule;
        /**
         * error message
         */
        std::string errorMessage;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_12::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="Get150BomWithVariantsDSOutput" );
    };

    struct  Get150BomWithVariantsDSResponse
    {
        /**
         * response
         */
        std::vector< Get150BomWithVariantsDSOutput > outputs;
        /**
         * Service Data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_12::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="Get150BomWithVariantsDSResponse" );
    };

    struct  PFEPReportInput
    {
        /**
         * Topnode Item ID
         */
        std::string ebomTopLine;
        /**
         * Ebom Revision Rule
         */
        std::string revisionRule;
        /**
         * Item Type of TopNode
         */
        std::string itemType;
        /**
         * Sourcing Program
         */
        std::string sourcingProgram;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Custom::_2020_12::ReportDataSourceIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  PFEPReportOutput
    {
        /**
         * Item ID
         */
        std::string itemId;
        /**
         * Item Name
         */
        std::string itemName;
        /**
         * Module Group English
         */
        std::string moduleGroupEnglish;
        /**
         * Main Module English
         */
        std::string mainModuleEnglish;
        /**
         * Purchase Level Vinfast
         */
        std::string purchaseLevelVinfast;
        /**
         * Change Index
         */
        std::string changeIndex;
        /**
         * Donor Vehicle
         */
        std::string donorVehicle;
        /**
         * SOR Released Date - Actual
         */
        Teamcenter::DateTime sorReleasedDateActual;
        /**
         * Supplier nomination date - Plan
         */
        Teamcenter::DateTime supplierNominationDatePlan;
        /**
         * Supplier nomination date - Reality
         */
        Teamcenter::DateTime supplierNominationDateReality;
        /**
         * Nominated supplier
         */
        std::string nominatedSupplier;
        /**
         * Manufacturing country
         */
        std::string manufacturingCountry;
        /**
         * Supplier nomination status
         */
        std::string supplierNominationStatus;
        /**
         * P In Progress
         */
        std::string pInProgress;
        /**
         * P Release Plan Date
         */
        Teamcenter::DateTime pReleasePlanDate;
        /**
         * P Released Date
         */
        Teamcenter::DateTime pReleasedDate;
        /**
         * P release Status
         */
        std::string pReleaseStatus;
        /**
         * I In Progress
         */
        std::string iInProgress;
        /**
         * I Release Plan Date
         */
        Teamcenter::DateTime iReleasePlanDate;
        /**
         * I Released Date
         */
        Teamcenter::DateTime iReleasedDate;
        /**
         * I release Stastus
         */
        std::string iReleaseStatus;
        /**
         * PR In Progress
         */
        std::string prInProgress;
        /**
         * PR Release Plan Date
         */
        Teamcenter::DateTime prReleasePlanDate;
        /**
         * PR Released Date
         */
        Teamcenter::DateTime prReleasedDate;
        /**
         * PR release Status
         */
        std::string prReleasedStatus;
        /**
         * Origin Part
         */
        std::string originPart;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_12::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="PFEPReportOutput" );
    };

    struct  PFEPReportResponse
    {
        /**
         * Response
         */
        std::vector< PFEPReportOutput > outputs;
        /**
         * Service Data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_12::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="PFEPReportResponse" );
    };

    struct  ProcessStatusReportInput
    {
        /**
         * Selected Process
         */
        std::string workflowTemplate;
        /**
         * Creation Date Before
         */
        std::string creationDateBefore;
        /**
         * Creation Date After
         */
        std::string creationDateAfter;
        /**
         * Modify Date Before
         */
        std::string modifyDateBefore;
        /**
         * Modify Date After
         */
        std::string modifyDateAfter;
        /**
         * Process Name
         */
        std::string processName;
        /**
         * Running Process
         */
        bool runningProccess;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Custom::_2020_12::ReportDataSourceIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  ProcessStatusReportOutput
    {
        /**
         * Process Name
         */
        std::string processName;
        /**
         * Process Status
         */
        std::string processStatus;
        /**
         * Start Date
         */
        Teamcenter::DateTime startDate;
        /**
         * Due Date
         */
        Teamcenter::DateTime dueDate;
        /**
         * Completed Date
         */
        Teamcenter::DateTime completedDate;
        /**
         * itemID of target object
         */
        std::string itemID;
        /**
         * itemRevision id of target object
         */
        std::string itemRevision;
        /**
         * Object Name of target
         */
        std::string itemName;
        /**
         * object description
         */
        std::string description;
        /**
         * Owning User
         */
        std::string owningUser;
        /**
         * Owning Group
         */
        std::string owningGroup;
        /**
         * Module Group
         */
        std::string moduleGroup;
        /**
         * Creation Date
         */
        Teamcenter::DateTime processStartDate;
        /**
         * pending Task
         */
        std::string pendingTask;
        /**
         * Pending User
         */
        std::string pendingUser;
        /**
         * workflow Template
         */
        std::string wfTemplate;
        /**
         * target object
         */
        std::string targets;
        /**
         * Reviewer Comment
         */
        std::string reviewerComment;
        /**
         * Reviewer
         */
        std::string reviewer;
        /**
         * Task Status
         */
        std::string taskStatus;
        /**
         * Responsible Party
         */
        std::string resParty;
        /**
         * Sub Task Name
         */
        std::string subTaskName;
        /**
         * classification
         */
        std::string classification;
        /**
         * priority
         */
        std::string priority;
        /**
         * Exchange New Part
         */
        std::string exchangeNewPart;
        /**
         * Exchange Old Part
         */
        std::string exchangeOldPart;
        /**
         * Process End Date
         */
        Teamcenter::DateTime processEndDate;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_12::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="ProcessStatusReportOutput" );
    };

    struct  ProcessStatusReportResponse
    {
        /**
         * list of process details
         */
        std::vector< ProcessStatusReportOutput > outputs;
        /**
         * Service Data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_12::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="ProcessStatusReportResponse" );
    };



    ReportDataSource();
    virtual ~ReportDataSource();
    

    /**
     * .
     *
     * @param inputs
     *        input
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual Get150BomWithVariantsDSResponse get150BomWithVariantsDS ( const std::vector< Get150BomWithVariantsDSInput >& inputs ) = 0;

    /**
     * .
     *
     * @param input
     *        input for PFEP report
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual PFEPReportResponse getPFEPReport ( const std::vector< PFEPReportInput >& input ) = 0;

    /**
     * .
     *
     * @param input
     *        input
     *
     * @return
     *
     *
     * @version ActiveWorkspace5.1
     */
    virtual ProcessStatusReportResponse getProcessStatusReport ( const ProcessStatusReportInput& input ) = 0;


};

#include <Custom_undef.h>
#endif

