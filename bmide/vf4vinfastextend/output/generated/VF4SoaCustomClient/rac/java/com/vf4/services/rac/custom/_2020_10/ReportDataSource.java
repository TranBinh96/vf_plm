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


package com.vf4.services.rac.custom._2020_10;


/**
 *
 */
@com.teamcenter.soa.internal.client.parse.XMLNamespace("http://vf4.com/Schemas/Custom/2020-10/ReportDataSource")
public interface ReportDataSource
{

    public class FGValidationInput
    {
        /**
         * Top BOM Item ID
         */
        public String itemId = "";
        /**
         * Top BOM Revision ID
         */
        public String revId = "";
        /**
         * Item Type
         */
        public String itemType = "";
        /**
         * Program name
         */
        public String program = "";
        /**
         * Revision Rule
         */
        public String revisionRule = "";
        /**
         * Saved-variant to load BOM
         */
        public String savedVariant2LoadBom = "";
        /**
         * Variant to validate BOM
         */
        public String variant2Validate = "";
    }


    public class FGValidationOutput
    {
        /**
         * Input
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationInput input = new com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationInput();
        /**
         * List of validation results
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationResult[] validationResults = new com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationResult[0];
        /**
         * Final validation result
         */
        public String finalValidationResult = "";
        /**
         * error messages
         */
        public String[] errorMessages = new String[0];
        /**
         * Returning errors
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    public class FGValidationPartInfo
    {
        /**
         * Part Revision of the validating part
         */
        public com.teamcenter.rac.kernel.TCComponent partRev = null;
        /**
         * Total of the part in validating BOM
         */
        public double quantity;
    }


    public class FGValidationResult
    {
        /**
         * Family Group Code
         */
        public String fgCode = "";
        /**
         * Family group description
         */
        public String fgDescription = "";
        /**
         * Min quantity
         */
        public double minQuantity;
        /**
         * Max quantity
         */
        public double maxQuantity;
        /**
         * Total parts belongs to the Family Group in BOM
         */
        public double fgCountInBom;
        /**
         * Relevant parts information
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationPartInfo[] relevantPartsInfo = new com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationPartInfo[0];
        /**
         * Validation result for the current Family Group
         */
        public String validationResult = "";
        /**
         * Family Group Validation Type
         */
        public String fgValidationType = "";
    }


    public class VFCost
    {
        /**
         * Tooling Cost
         */
        public double toolingCost;
        /**
         * EDnD Cost
         */
        public double edndCost;
        /**
         * Piece Cost
         */
        public double pieceCost;
        /**
         * Logistic Cost
         */
        public double logisticCost;
        /**
         * Packing Cost
         */
        public double packingCost;
        /**
         * Labour Cost
         */
        public double labourCost;
        /**
         * Tax
         */
        public double tax;
        /**
         * Total Piece Cost
         */
        public double totalPieceCost;
        /**
         * Total Piece Cost Status
         */
        public String totalPieceCostStatus = "";
        /**
         * Cost Type
         */
        public String costType = "";
        /**
         * Approval date
         */
        public java.util.Calendar approvalDate;
    }


    public class VFCostElement
    {
        /**
         * Cost Value
         */
        public double cost;
        /**
         * Cost Type
         */
        public String costType = "";
        /**
         * Approval Date
         */
        public java.util.Calendar approvalDate;
    }


    public class VFCostHistory
    {
        /**
         * SAP cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost sapPieceCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost();
        /**
         * SAP cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost sapToolingCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost();
        /**
         * SAP cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost sapEdndCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost();
        /**
         * Sourcing cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost stCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost();
        /**
         * ECR cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost ecrCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost();
        /**
         * ENG cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost engCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost();
        /**
         * Target cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost targetCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost();
        /**
         * approvalDate
         */
        public java.util.Calendar approvalDate;
    }


    public class VFEcrCostResponse
    {
        /**
         * Service Data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
        /**
         * Map of ecrs and part costs
         */
        public java.util.Map<com.teamcenter.rac.kernel.TCComponent, com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartChange[]> ecrAndPartsCost = new java.util.HashMap<com.teamcenter.rac.kernel.TCComponent, com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartChange[]>();
    }


    public class VFPartChange
    {
        /**
         * partChanges
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartCost[] partChanges = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartCost[0];
        /**
         * Delta Part Cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost deltaPartCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost();
    }


    public class VFPartCost
    {
        /**
         * Part Number
         */
        public String partNumber = "";
        /**
         * Part Change Type
         */
        public String partChangeType = "";
        /**
         * Part Cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost partCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCost();
        /**
         * Piece Cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement pieceCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement();
        /**
         * Packing Cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement packingCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement();
        /**
         * Logistic Cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement logisticCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement();
        /**
         * Labour Cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement labourCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement();
        /**
         * Tax
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement tax = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement();
        /**
         * Tooling Cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement toolingCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement();
        /**
         * EDnD Cost
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement edndCost = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostElement();
        /**
         * Cost history
         */
        public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostHistory costHistory = new com.vf4.services.rac.custom._2020_10.ReportDataSource.VFCostHistory();
    }


    public class VFPartCostCalcResponse
    {
        /**
         * Part Number and Cost Result
         */
        public java.util.Map<String, com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartCost> result = new java.util.HashMap<String, com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartCost>();
        /**
         * Error messages
         */
        public String[] errorMessages = new String[0];
        /**
         * Service Data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }



    

    /**
     * .
     *
     * <br><br><b>Version:</b> VF_2020_10<br>
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
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "partNumbers", "programName", "isDryrun" })
    public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartCostCalcResponse calculatePartsCost ( String[] partNumbers, String programName, String isDryrun );


    /**
     * .
     *
     * <br><br><b>Version:</b> VF_2020_10<br>
     *
     * @param ecrs
     *        List of input ECRs
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "ecrs" })
    public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFEcrCostResponse getEcrsCost ( com.teamcenter.rac.kernel.TCComponent[] ecrs );


    /**
     * .
     *
     * <br><br><b>Version:</b> VF_2020_10<br>
     *
     * @param input
     *        Validation input
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "input" })
    public com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationOutput validateFamilyGroup ( com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationInput input );



}
