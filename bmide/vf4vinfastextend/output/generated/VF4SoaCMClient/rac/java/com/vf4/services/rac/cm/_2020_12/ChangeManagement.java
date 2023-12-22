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


package com.vf4.services.rac.cm._2020_12;


/**
 *
 */
@com.teamcenter.soa.internal.client.parse.XMLNamespace("http://vf4.com/Schemas/CM/2020-12/ChangeManagement")
public interface ChangeManagement
{

    public class DeriveChangeInput
    {
        /**
         * tag of ECR revision
         */
        public com.teamcenter.rac.kernel.TCComponent changeObjRev = null;
        /**
         * list of mandotory property and values to set
         */
        public com.vf4.services.rac.cm._2020_12.ChangeManagement.PropertyAndValues[] propertyAndValues = new com.vf4.services.rac.cm._2020_12.ChangeManagement.PropertyAndValues[0];
        /**
         * ECN &amp; ECR relation
         */
        public String relationToAttachChangeObj = "";
        /**
         * ECN object type to be created
         */
        public String derivedObjType = "";
    }


    public class DeriveChangeOutput
    {
        /**
         * Derived ECN object
         */
        public com.teamcenter.rac.kernel.TCComponent chnageObjTag = null;
        /**
         * Error Message
         */
        public String errorMessage = "";
    }


    public class DeriveChangeResponse
    {
        /**
         * Service output
         */
        public com.vf4.services.rac.cm._2020_12.ChangeManagement.DeriveChangeOutput[] outputs = new com.vf4.services.rac.cm._2020_12.ChangeManagement.DeriveChangeOutput[0];
        /**
         * Service Data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    public class ImpactedProgramInput
    {
        /**
         * Impacted parts
         */
        public com.teamcenter.rac.kernel.TCComponent[] impactedParts = new com.teamcenter.rac.kernel.TCComponent[0];
        /**
         * topNodeType
         */
        public String[] topNodeType = new String[0];
        /**
         * Revision Rule
         */
        public String revisionRule = "";
    }


    public class ImpactedProgramResponse
    {
        /**
         * Map of outputs
         */
        public java.util.Map<com.teamcenter.rac.kernel.TCComponent, com.teamcenter.rac.kernel.TCComponent[]> outputs = new java.util.HashMap<com.teamcenter.rac.kernel.TCComponent, com.teamcenter.rac.kernel.TCComponent[]>();
        /**
         * Service Data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    public class PartNumberChangeHistory
    {
        /**
         * Part revision which may has old part number accompany with relevant ECR
         */
        public com.teamcenter.rac.kernel.TCComponent partRev = null;
        /**
         * Old part revision of the partRev
         */
        public com.teamcenter.rac.kernel.TCComponent oldPartRev = null;
        /**
         * Relevant ECR revision to the oldRev and partRev
         */
        public com.teamcenter.rac.kernel.TCComponent ecrRev = null;
        /**
         * First source part get from latest working revision
         */
        public com.teamcenter.rac.kernel.TCComponent sourcePart = null;
        /**
         * Part object_string
         */
        public String partString = "";
        /**
         * Old part number object_string
         */
        public String oldPartString = "";
        /**
         * ECR string
         */
        public String ecrString = "";
        /**
         * SA cost
         */
        public double saCost;
        /**
         * SA currency
         */
        public String saCurrency = "";
        /**
         * Tooling cost
         */
        public double toolingCost;
        /**
         * Tooling currency
         */
        public String toolingCurrency = "";
        /**
         * EDnD cost
         */
        public double edndCost;
        /**
         * EDnD currency
         */
        public String edndCurrency = "";
        /**
         * Total cost
         */
        public double totalCost;
        /**
         * Total cost status
         */
        public String totalCostStatus = "";
    }


    public class PartNumberChangeHistoryInput
    {
        /**
         * Part number desired to search part number change's history
         */
        public String partNumber = "";
        /**
         * Part type of the partNumber
         */
        public String partType = "";
        /**
         * Is include cost
         */
        public boolean isIncludeCost;
    }


    public class PartNumberChangeHistoryOutput
    {
        /**
         * Part number change's history input
         */
        public com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistoryInput input = new com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistoryInput();
        /**
         * History list
         */
        public com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistory[] historyList = new com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistory[0];
    }


    public class PartNumberChangeHistoryResponse
    {
        /**
         * Output list which is respectively to input list
         */
        public com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistoryOutput[] outputs = new com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistoryOutput[0];
        /**
         * Service data
         */
        public com.teamcenter.rac.kernel.ServiceData servicedata;
    }


    public class PropertyAndValues
    {
        /**
         * Name of the Property
         */
        public String propertyName = "";
        /**
         * Value for the Property
         */
        public String propertyValue = "";
    }



    

    /**
     * Operation for creating ECN from ECR.
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
    public com.vf4.services.rac.cm._2020_12.ChangeManagement.DeriveChangeResponse deriveChange ( com.vf4.services.rac.cm._2020_12.ChangeManagement.DeriveChangeInput[] inputs );


    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param inputs
     *        input for getImpactedPrograms SOA service
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "inputs" })
    public com.vf4.services.rac.cm._2020_12.ChangeManagement.ImpactedProgramResponse getImpactedPrograms ( com.vf4.services.rac.cm._2020_12.ChangeManagement.ImpactedProgramInput inputs );


    /**
     * Return part number change's history which accompanies with relevant ECR.
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param inputs
     *        Inputs
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "inputs" })
    public com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistoryResponse getPartNumberChangeHistory ( com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistoryInput[] inputs );



}
