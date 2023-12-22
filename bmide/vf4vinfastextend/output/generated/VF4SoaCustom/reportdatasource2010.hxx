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

#ifndef TEAMCENTER_SERVICES_CUSTOM_2020_10_REPORTDATASOURCE_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2020_10_REPORTDATASOURCE_HXX


#include <base_utils/DateTime.hxx>
#include <metaframework/BusinessObject.hxx>
#include <teamcenter/soa/server/ServiceData.hxx>



#include <teamcenter/soa/server/ServiceException.hxx>
#include <metaframework/BusinessObjectRef.hxx>

#include <Custom_exports.h>

namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmStream; }}}}
namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmParser; }}}}
namespace VF4 { namespace Services { namespace Custom { namespace _2020_10 { class ReportDataSourceIiopSkeleton; }}}}


namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2020_10
            {
                class ReportDataSource;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2020_10::ReportDataSource

{
public:

    static const std::string XSD_NAMESPACE;

    struct FGValidationInput;
    struct FGValidationOutput;
    struct FGValidationPartInfo;
    struct FGValidationResult;
    struct VFCost;
    struct VFCostElement;
    struct VFCostHistory;
    struct VFEcrCostResponse;
    struct VFPartChange;
    struct VFPartCost;
    struct VFPartCostCalcResponse;

    enum PartChangeType{ old_part,
                 new_part
                 };

    static const std::map<std::string, PartChangeType>  STRING_TO_PARTCHANGETYPE_MAP;
    static const std::map<PartChangeType, std::string>  PARTCHANGETYPE_TO_STRING_MAP;
    typedef std::map< BusinessObjectRef<Teamcenter::BusinessObject>, std::vector< VFPartChange > > EcrsAndPartsCost;

    typedef std::map< std::string, VFPartCost > VFPartCostCalcMap;

    struct  FGValidationInput
    {
        /**
         * Top BOM Item ID
         */
        std::string itemId;
        /**
         * Top BOM Revision ID
         */
        std::string revId;
        /**
         * Item Type
         */
        std::string itemType;
        /**
         * Program name
         */
        std::string program;
        /**
         * Revision Rule
         */
        std::string revisionRule;
        /**
         * Saved-variant to load BOM
         */
        std::string savedVariant2LoadBom;
        /**
         * Variant to validate BOM
         */
        std::string variant2Validate;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Custom::_2020_10::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="FGValidationInput" );
        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  FGValidationOutput
    {
        /**
         * Input
         */
        FGValidationInput input;
        /**
         * List of validation results
         */
        std::vector< FGValidationResult > validationResults;
        /**
         * Final validation result
         */
        std::string finalValidationResult;
        /**
         * error messages
         */
        std::vector< std::string > errorMessages;
        /**
         * Returning errors
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_10::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="FGValidationOutput" );
    };

    struct  FGValidationPartInfo
    {
        /**
         * Part Revision of the validating part
         */
        BusinessObjectRef<Teamcenter::BusinessObject> partRev;
        /**
         * Total of the part in validating BOM
         */
        double quantity;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_10::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="FGValidationPartInfo" );
    };

    struct  FGValidationResult
    {
        /**
         * Family Group Code
         */
        std::string fgCode;
        /**
         * Family group description
         */
        std::string fgDescription;
        /**
         * Min quantity
         */
        double minQuantity;
        /**
         * Max quantity
         */
        double maxQuantity;
        /**
         * Total parts belongs to the Family Group in BOM
         */
        double fgCountInBom;
        /**
         * Relevant parts information
         */
        std::vector< FGValidationPartInfo > relevantPartsInfo;
        /**
         * Validation result for the current Family Group
         */
        std::string validationResult;
        /**
         * Family Group Validation Type
         */
        std::string fgValidationType;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_10::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="FGValidationResult" );
    };

    struct  VFCost
    {
        /**
         * Tooling Cost
         */
        double toolingCost;
        /**
         * EDnD Cost
         */
        double edndCost;
        /**
         * Piece Cost
         */
        double pieceCost;
        /**
         * Logistic Cost
         */
        double logisticCost;
        /**
         * Packing Cost
         */
        double packingCost;
        /**
         * Labour Cost
         */
        double labourCost;
        /**
         * Tax
         */
        double tax;
        /**
         * Total Piece Cost
         */
        double totalPieceCost;
        /**
         * Total Piece Cost Status
         */
        std::string totalPieceCostStatus;
        /**
         * Cost Type
         */
        std::string costType;
        /**
         * Approval date
         */
        Teamcenter::DateTime approvalDate;

	long long approvalTimeStamp;
        bool isPieceNoCost;
        bool isEDnDNoCost;
        bool isToolingNoCost;
        VFCost();

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_10::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="VFCost" );
    };

    struct  VFCostElement
    {
        /**
         * Cost Value
         */
        double cost;
        /**
         * Cost Type
         */
        std::string costType;
        /**
         * Approval Date
         */
        Teamcenter::DateTime approvalDate;

	long long approvalTimeStamp;

        VFCostElement();

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_10::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="VFCostElement" );
    };

    struct  VFCostHistory
    {
        /**
         * SAP cost
         */
        VFCost sapPieceCost;
        /**
         * SAP cost
         */
        VFCost sapToolingCost;
        /**
         * SAP cost
         */
        VFCost sapEdndCost;
        /**
         * Sourcing cost
         */
        VFCost stCost;
        /**
         * ECR cost
         */
        VFCost ecrCost;
        /**
         * ENG cost
         */
        VFCost engCost;
        /**
         * Target cost
         */
        VFCost targetCost;
        /**
         * approvalDate
         */
        Teamcenter::DateTime approvalDate;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_10::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="VFCostHistory" );
    };

    struct  VFEcrCostResponse
    {
        /**
         * Service Data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;
        /**
         * Map of ecrs and part costs
         */
        EcrsAndPartsCost ecrAndPartsCost;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_10::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="VFEcrCostResponse" );
    };

    struct  VFPartChange
    {
        /**
         * partChanges
         */
        std::vector< VFPartCost > partChanges;
        /**
         * Delta Part Cost
         */
        VFCost deltaPartCost;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_10::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="VFPartChange" );
    };

    struct  VFPartCost
    {
        /**
         * Part Number
         */
        std::string partNumber;
        /**
         * Part Change Type
         */
        PartChangeType partChangeType;
        /**
         * Part Cost
         */
        VFCost partCost;
        /**
         * Piece Cost
         */
        VFCostElement pieceCost;
        /**
         * Packing Cost
         */
        VFCostElement packingCost;
        /**
         * Logistic Cost
         */
        VFCostElement logisticCost;
        /**
         * Labour Cost
         */
        VFCostElement labourCost;
        /**
         * Tax
         */
        VFCostElement tax;
        /**
         * Tooling Cost
         */
        VFCostElement toolingCost;
        /**
         * EDnD Cost
         */
        VFCostElement edndCost;
        /**
         * Cost history
         */
        VFCostHistory costHistory;

	VFPartCost();
    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_10::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="VFPartCost" );
    };

    struct  VFPartCostCalcResponse
    {
        /**
         * Part Number and Cost Result
         */
        VFPartCostCalcMap result;
        /**
         * Error messages
         */
        std::vector< std::string > errorMessages;
        /**
         * Service Data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2020_10::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="VFPartCostCalcResponse" );
    };



    ReportDataSource();
    virtual ~ReportDataSource();
    

    /**
     * .
     *
     * @param partNumbers
     *        Part Numbers list
     *
     * @param programName
     *        Program name for the parts
     *
     * @param isDryrun
     *        If &quot;true&quot; or &quot;TRUE&quot;, calculated cost are NOT set into cost object
     *        <br>
     *        Default value is false,  calculated cost are set into cost object
     *
     * @return
     *
     */
    virtual VFPartCostCalcResponse calculatePartsCost ( const std::vector< std::string >& partNumbers,
        const std::string programName,
        const std::string isDryrun ) = 0;

    /**
     * .
     *
     * @param ecrs
     *        List of input ECRs
     *
     * @return
     *
     */
    virtual VFEcrCostResponse getEcrsCost ( const std::vector< BusinessObjectRef<Teamcenter::BusinessObject> >& ecrs ) = 0;

    /**
     * .
     *
     * @param input
     *        Validation input
     *
     * @return
     *
     */
    virtual FGValidationOutput validateFamilyGroup ( const FGValidationInput& input ) = 0;


};

#include <Custom_undef.h>
#endif

