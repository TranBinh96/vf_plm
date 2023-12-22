/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@

 ==================================================

   Auto-generated source from service interface.
                 DO NOT EDIT

 ==================================================
*/


package com.vf4.services.rac.custom._2017_05;


/**
 *
 */
@com.teamcenter.soa.internal.client.parse.XMLNamespace("http://vf4.com/Schemas/Custom/2017-05/ReportDataSource")
public interface ReportDataSource
{

    /**
     * Source Part Cost Info
     */
    public class SourcePartCostInfo
    {
        /**
         * Source-part vf4_supplier_piece_cost_exw
         */
        public double vf4SupplierPieceCostExw;
        /**
         * Source-part vf4_tooling_order
         */
        public double vf4ToolingOrder;
        /**
         * Source-part vf4_eddorder_value
         */
        public double vf4EddorderValue;
        /**
         * Source-part vf4_piece_cost
         */
        public double vf4PieceCost;
    }


    public class CostInfo
    {
        /**
         * Source-Part Cost-Info
         */
        public com.vf4.services.rac.custom._2017_05.ReportDataSource.SourcePartCostInfo sourcePart = new com.vf4.services.rac.custom._2017_05.ReportDataSource.SourcePartCostInfo();
        /**
         * ECRs Cost Info
         */
        public com.vf4.services.rac.custom._2017_05.ReportDataSource.ECRCostInfo[] ecrs = new com.vf4.services.rac.custom._2017_05.ReportDataSource.ECRCostInfo[0];
    }


    public class CostReportDSInput
    {
        /**
         * List of part revision IDs &lt;itemID/revisionID&gt; e.g. PLM12345678/01
         */
        public String[] partRevs = new String[0];
        /**
         * Sourcing program
         */
        public String sourcingProgram = "";
    }


    public class CostReportDSOutput
    {
        /**
         * Cost Report DS response
         */
        public java.util.Map<String, com.vf4.services.rac.custom._2017_05.ReportDataSource.CostInfo> response = new java.util.HashMap<String, com.vf4.services.rac.custom._2017_05.ReportDataSource.CostInfo>();
        /**
         * Service Data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    /**
     * ECR Cost Info
     */
    public class ECRCostInfo
    {
        /**
         * ECR vf6_material_costs
         */
        public double vf6MaterialCosts;
        /**
         * ECR vf6_tooling_costs
         */
        public double vf6ToolingCosts;
        /**
         * ECR vf6_fixtures
         */
        public double vf6Fixtures;
        /**
         * ECR vf6_supplier_eng_costs
         */
        public double vf6SupplierEngCosts;
        /**
         * ECR vf6_scrap_costs
         */
        public double vf6ScrapCosts;
        /**
         * ECR vf6_lead_time_p
         */
        public double vf6LeadTimeP;
        /**
         * ECR Name
         */
        public String ecrName = "";
        /**
         * ECR Number
         */
        public String ecrNumber = "";
        /**
         * ECR Vehicle Group
         */
        public String vf6VehicleGroup = "";
        /**
         * vf6ModuleGroup
         */
        public String vf6ModuleGroup = "";
    }



    

    /**
     * .
     *
     * <br><br><b>Version:</b> Teamcenter 11.3<br>
     *
     * @param input
     *        Cost Report DS Input
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "input" })
    public com.vf4.services.rac.custom._2017_05.ReportDataSource.CostReportDSOutput getCostReportDS ( com.vf4.services.rac.custom._2017_05.ReportDataSource.CostReportDSInput input );



}
