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


package com.vf4.services.rac.custom._2020_12;


/**
 *
 */
@com.teamcenter.soa.internal.client.parse.XMLNamespace("http://vf4.com/Schemas/Custom/2020-12/ReportDataSource")
public interface ReportDataSource
{

    public class Get150BomWithVariantsDSInput
    {
        /**
         * top line tag
         */
        public com.teamcenter.rac.kernel.TCComponentBOMLine toplineTag = null;
        /**
         * TopNode Item Id where Configaration Context is attached
         */
        public String topItemId = "";
        /**
         * TopNode Item Type
         */
        public String topItemType = "";
    }


    public class Get150BomWithVariantsDSOutput
    {
        /**
         * Level
         */
        public String level = "";
        /**
         * BOMLINE Tag
         */
        public com.teamcenter.rac.kernel.TCComponentBOMLine bomlineTag = null;
        /**
         * Vector of Variant Rule tag
         */
        public com.teamcenter.rac.kernel.TCComponent[] variantRule = new com.teamcenter.rac.kernel.TCComponent[0];
        /**
         * error message
         */
        public String errorMessage = "";
    }


    public class Get150BomWithVariantsDSResponse
    {
        /**
         * response
         */
        public com.vf4.services.rac.custom._2020_12.ReportDataSource.Get150BomWithVariantsDSOutput[] outputs = new com.vf4.services.rac.custom._2020_12.ReportDataSource.Get150BomWithVariantsDSOutput[0];
        /**
         * Service Data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    public class PFEPReportInput
    {
        /**
         * Topnode Item ID
         */
        public String ebomTopLine = "";
        /**
         * Ebom Revision Rule
         */
        public String revisionRule = "";
        /**
         * Item Type of TopNode
         */
        public String itemType = "";
        /**
         * Sourcing Program
         */
        public String sourcingProgram = "";
    }


    public class PFEPReportOutput
    {
        /**
         * Item ID
         */
        public String itemId = "";
        /**
         * Item Name
         */
        public String itemName = "";
        /**
         * Module Group English
         */
        public String moduleGroupEnglish = "";
        /**
         * Main Module English
         */
        public String mainModuleEnglish = "";
        /**
         * Purchase Level Vinfast
         */
        public String purchaseLevelVinfast = "";
        /**
         * Change Index
         */
        public String changeIndex = "";
        /**
         * Donor Vehicle
         */
        public String donorVehicle = "";
        /**
         * SOR Released Date - Actual
         */
        public java.util.Calendar sorReleasedDateActual;
        /**
         * Supplier nomination date - Plan
         */
        public java.util.Calendar supplierNominationDatePlan;
        /**
         * Supplier nomination date - Reality
         */
        public java.util.Calendar supplierNominationDateReality;
        /**
         * Nominated supplier
         */
        public String nominatedSupplier = "";
        /**
         * Manufacturing country
         */
        public String manufacturingCountry = "";
        /**
         * Supplier nomination status
         */
        public String supplierNominationStatus = "";
        /**
         * P In Progress
         */
        public String pInProgress = "";
        /**
         * P Release Plan Date
         */
        public java.util.Calendar pReleasePlanDate;
        /**
         * P Released Date
         */
        public java.util.Calendar pReleasedDate;
        /**
         * P release Status
         */
        public String pReleaseStatus = "";
        /**
         * I In Progress
         */
        public String iInProgress = "";
        /**
         * I Release Plan Date
         */
        public java.util.Calendar iReleasePlanDate;
        /**
         * I Released Date
         */
        public java.util.Calendar iReleasedDate;
        /**
         * I release Stastus
         */
        public String iReleaseStatus = "";
        /**
         * PR In Progress
         */
        public String prInProgress = "";
        /**
         * PR Release Plan Date
         */
        public java.util.Calendar prReleasePlanDate;
        /**
         * PR Released Date
         */
        public java.util.Calendar prReleasedDate;
        /**
         * PR release Status
         */
        public String prReleasedStatus = "";
        /**
         * Origin Part
         */
        public String originPart = "";
    }


    public class PFEPReportResponse
    {
        /**
         * Response
         */
        public com.vf4.services.rac.custom._2020_12.ReportDataSource.PFEPReportOutput[] outputs = new com.vf4.services.rac.custom._2020_12.ReportDataSource.PFEPReportOutput[0];
        /**
         * Service Data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    public class ProcessStatusReportInput
    {
        /**
         * Selected Process
         */
        public String workflowTemplate = "";
        /**
         * Creation Date Before
         */
        public String creationDateBefore = "";
        /**
         * Creation Date After
         */
        public String creationDateAfter = "";
        /**
         * Modify Date Before
         */
        public String modifyDateBefore = "";
        /**
         * Modify Date After
         */
        public String modifyDateAfter = "";
        /**
         * Process Name
         */
        public String processName = "";
        /**
         * Running Process
         */
        public boolean runningProccess;
    }


    public class ProcessStatusReportOutput
    {
        /**
         * Process Name
         */
        public String processName = "";
        /**
         * Process Status
         */
        public String processStatus = "";
        /**
         * Start Date
         */
        public java.util.Calendar startDate;
        /**
         * Due Date
         */
        public java.util.Calendar dueDate;
        /**
         * Completed Date
         */
        public java.util.Calendar completedDate;
        /**
         * itemID of target object
         */
        public String itemID = "";
        /**
         * itemRevision id of target object
         */
        public String itemRevision = "";
        /**
         * Object Name of target
         */
        public String itemName = "";
        /**
         * object description
         */
        public String description = "";
        /**
         * Owning User
         */
        public String owningUser = "";
        /**
         * Owning Group
         */
        public String owningGroup = "";
        /**
         * Module Group
         */
        public String moduleGroup = "";
        /**
         * Creation Date
         */
        public java.util.Calendar processStartDate;
        /**
         * pending Task
         */
        public String pendingTask = "";
        /**
         * Pending User
         */
        public String pendingUser = "";
        /**
         * workflow Template
         */
        public String wfTemplate = "";
        /**
         * target object
         */
        public String targets = "";
        /**
         * Reviewer Comment
         */
        public String reviewerComment = "";
        /**
         * Reviewer
         */
        public String reviewer = "";
        /**
         * Task Status
         */
        public String taskStatus = "";
        /**
         * Responsible Party
         */
        public String resParty = "";
        /**
         * Sub Task Name
         */
        public String subTaskName = "";
        /**
         * classification
         */
        public String classification = "";
        /**
         * priority
         */
        public String priority = "";
        /**
         * Exchange New Part
         */
        public String exchangeNewPart = "";
        /**
         * Exchange Old Part
         */
        public String exchangeOldPart = "";
        /**
         * Process End Date
         */
        public java.util.Calendar processEndDate;
    }


    public class ProcessStatusReportResponse
    {
        /**
         * list of process details
         */
        public com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportOutput[] outputs = new com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportOutput[0];
        /**
         * Service Data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }



    

    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param inputs
     *        input
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "inputs" })
    public com.vf4.services.rac.custom._2020_12.ReportDataSource.Get150BomWithVariantsDSResponse get150BomWithVariantsDS ( com.vf4.services.rac.custom._2020_12.ReportDataSource.Get150BomWithVariantsDSInput[] inputs );


    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param input
     *        input for PFEP report
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "input" })
    public com.vf4.services.rac.custom._2020_12.ReportDataSource.PFEPReportResponse getPFEPReport ( com.vf4.services.rac.custom._2020_12.ReportDataSource.PFEPReportInput[] input );


    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param input
     *        input
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "input" })
    public com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportResponse getProcessStatusReport ( com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportInput input );



}
