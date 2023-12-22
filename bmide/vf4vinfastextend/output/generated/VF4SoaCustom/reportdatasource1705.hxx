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

#ifndef TEAMCENTER_SERVICES_CUSTOM_2017_05_REPORTDATASOURCE_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_2017_05_REPORTDATASOURCE_HXX


#include <teamcenter/soa/server/ServiceData.hxx>



#include <teamcenter/soa/server/ServiceException.hxx>
#include <metaframework/BusinessObjectRef.hxx>

#include <Custom_exports.h>

namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmStream; }}}}
namespace Teamcenter { namespace Soa {  namespace Internal { namespace Server { class SdmParser; }}}}
namespace VF4 { namespace Services { namespace Custom { namespace _2017_05 { class ReportDataSourceIiopSkeleton; }}}}


namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            namespace _2017_05
            {
                class ReportDataSource;
            }
        }
    }
}


class SOACUSTOM_API VF4::Soa::Custom::_2017_05::ReportDataSource

{
public:

    static const std::string XSD_NAMESPACE;

    struct SourcePartCostInfo;
    struct CostInfo;
    struct CostReportDSInput;
    struct CostReportDSOutput;
    struct ECRCostInfo;

    typedef std::map< std::string, CostInfo > CostReportDSResponse;

    /**
     * Source Part Cost Info
     */
    struct  SourcePartCostInfo
    {
        /**
         * Source-part vf4_supplier_piece_cost_exw
         */
        double vf4SupplierPieceCostExw;
        /**
         * Source-part vf4_tooling_order
         */
        double vf4ToolingOrder;
        /**
         * Source-part vf4_eddorder_value
         */
        double vf4EddorderValue;
        /**
         * Source-part vf4_piece_cost
         */
        double vf4PieceCost;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2017_05::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="SourcePartCostInfo" );
    };

    struct  CostInfo
    {
        /**
         * Source-Part Cost-Info
         */
        SourcePartCostInfo sourcePart;
        /**
         * ECRs Cost Info
         */
        std::vector< ECRCostInfo > ecrs;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2017_05::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="CostInfo" );
    };

    struct  CostReportDSInput
    {
        /**
         * List of part revision IDs &lt;itemID/revisionID&gt; e.g. PLM12345678/01
         */
        std::vector< std::string > partRevs;
        /**
         * Sourcing program
         */
        std::string sourcingProgram;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmParser;
        friend class VF4::Services::Custom::_2017_05::ReportDataSourceIiopSkeleton;

        void parse    ( Teamcenter::Soa::Internal::Server::SdmParser* _sdmParser );
    };

    struct  CostReportDSOutput
    {
        /**
         * Cost Report DS response
         */
        CostReportDSResponse response;
        /**
         * Service Data
         */
        Teamcenter::Soa::Server::ServiceData serviceData;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2017_05::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="CostReportDSOutput" );
    };

    /**
     * ECR Cost Info
     */
    struct  ECRCostInfo
    {
        /**
         * ECR vf6_material_costs
         */
        double vf6MaterialCosts;
        /**
         * ECR vf6_tooling_costs
         */
        double vf6ToolingCosts;
        /**
         * ECR vf6_fixtures
         */
        double vf6Fixtures;
        /**
         * ECR vf6_supplier_eng_costs
         */
        double vf6SupplierEngCosts;
        /**
         * ECR vf6_scrap_costs
         */
        double vf6ScrapCosts;
        /**
         * ECR vf6_lead_time_p
         */
        double vf6LeadTimeP;
        /**
         * ECR Name
         */
        std::string ecrName;
        /**
         * ECR Number
         */
        std::string ecrNumber;
        /**
         * ECR Vehicle Group
         */
        std::string vf6VehicleGroup;
        /**
         * vf6ModuleGroup
         */
        std::string vf6ModuleGroup;

    private:
        friend class Teamcenter::Soa::Internal::Server::SdmStream;
        friend class VF4::Services::Custom::_2017_05::ReportDataSourceIiopSkeleton;

        void serialize( Teamcenter::Soa::Internal::Server::SdmStream* _sdmStream, const std::string& elementName="ECRCostInfo" );
    };



    ReportDataSource();
    virtual ~ReportDataSource();
    

    /**
     * .
     *
     * @param input
     *        Cost Report DS Input
     *
     * @return
     *
     *
     * @version Teamcenter 11.3
     */
    virtual CostReportDSOutput getCostReportDS ( const CostReportDSInput& input ) = 0;


};

#include <Custom_undef.h>
#endif

