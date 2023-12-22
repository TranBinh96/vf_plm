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


package com.vf4.services.rac.integration._2020_12;


/**
 *
 */
@com.teamcenter.soa.internal.client.parse.XMLNamespace("http://vf4.com/Schemas/Integration/2020-12/SAPIntegration")
public interface SAPIntegration
{

    public class GetMasterMaterialsInput
    {
        /**
         * PART REV UIDs
         */
        public com.teamcenter.rac.kernel.TCComponent partRevUIDs = null;
    }


    public class GetMasterMaterialsOutput
    {
        /**
         * ITEM_PART_CATEGORY
         */
        public String partType = "";
        /**
         * ITEM_REV_ID
         */
        public String revisionNumber = "";
        /**
         * REV_BROADCODE
         */
        public String brdCode = "";
        /**
         * REV_FUNC_CODE
         */
        public String functionalClass = "";
        /**
         * ITEM_OLD_PARTNO
         */
        public String oldMaterialNumber = "";
        /**
         * ITEM_MATERIAL_TYPE
         */
        public String materialType = "";
        /**
         * ITEM_UOM
         */
        public String uom = "";
        /**
         * ITEM_GM_PARTNO
         */
        public String gmPart = "";
        /**
         * ITEM_ID
         */
        public String materialNumber = "";
        /**
         * REV_NAME
         */
        public String description = "";
        /**
         * ITEM_APPOV_CODE
         */
        public String approvalClass = "";
        /**
         * ITEM_IS_TRACEABLE
         */
        public String traceablePart = "";
        /**
         * REV_VIET_DESCRIPTION
         */
        public String descriptionVietnamese = "";
        /**
         * ITEM_TYPE
         */
        public String tcItemType = "";
        /**
         * list of plant information structure
         */
        public com.vf4.services.rac.integration._2020_12.SAPIntegration.PlantInformation[] sapPlant = new com.vf4.services.rac.integration._2020_12.SAPIntegration.PlantInformation[0];
        /**
         * custom error message
         */
        public String errorMessage = "";
    }


    public class GetMasterMaterialsResponse
    {
        /**
         * output for soa service
         */
        public com.vf4.services.rac.integration._2020_12.SAPIntegration.GetMasterMaterialsOutput[] outputs = new com.vf4.services.rac.integration._2020_12.SAPIntegration.GetMasterMaterialsOutput[0];
        /**
         * service data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    public class PlantInformation
    {
        /**
         * Plant Code
         */
        public String plantCode = "";
        /**
         * Procurement Type
         */
        public String procurementType = "";
    }


    public class TransferOperationToJSONInput
    {
        /**
         * Operation ID
         */
        public String operationID = "";
        /**
         * Operation REV ID
         */
        public String operationRevID = "";
        /**
         * operation JSON
         */
        public String json = "";
    }


    public class TransferOperationToJSONInputs
    {
        /**
         * Input operation
         */
        public com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToJSONInput[] inputs = new com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToJSONInput[0];
        /**
         * MES server IP
         */
        public String mesServerIP = "";
        /**
         * MES server Username
         */
        public String mesSeverUser = "";
        /**
         * MES sever Password
         */
        public String mesSeverPass = "";
        /**
         * MES server Clinet ID
         */
        public String mesServerClientID = "";
        /**
         * MES server Client Secret
         */
        public String mesServerClientSecret = "";
        /**
         * MES server Scope
         */
        public String mesServerScope = "";
    }


    public class TransferOperationToJSONResponse
    {
        /**
         * Servicedata
         */
        public com.teamcenter.rac.kernel.ServiceData servicedata;
        /**
         * OperationIDs
         */
        public String[] operationIDs = new String[0];
        /**
         * Operation Messages
         */
        public String[] operationMessages = new String[0];
        /**
         * Return code received from MES
         */
        public String[] returnCodes = new String[0];
    }


    public class TransferOperationToMESInput
    {
        /**
         * Operation ID
         */
        public String operationID = "";
        /**
         * Operation REV ID
         */
        public String operationRevID = "";
        /**
         * Operation XML
         */
        public String xml = "";
    }


    public class TransferOperationToMESInputs
    {
        /**
         * Input operations
         */
        public com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToMESInput[] inputs = new com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToMESInput[0];
        /**
         * MES server IP
         */
        public String mesServerIP = "";
        /**
         * MES server port
         */
        public int mesServerPort;
        /**
         * Shop name
         */
        public String shopName = "";
    }


    public class TransferOperationToMESResponse
    {
        /**
         * Servicedata
         */
        public com.teamcenter.rac.kernel.ServiceData servicedata;
        /**
         * Operation IDs
         */
        public String[] operationIDs = new String[0];
        /**
         * Return code received from MES
         */
        public String[] returnCodes = new String[0];
    }



    

    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param inputs
     *        inputs
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "inputs" })
    public com.vf4.services.rac.integration._2020_12.SAPIntegration.GetMasterMaterialsResponse getMasterMaterials ( com.vf4.services.rac.integration._2020_12.SAPIntegration.GetMasterMaterialsInput[] inputs );


    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param inputs
     *        inputs
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "inputs" })
    public com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToJSONResponse transferOperationJSONToMES ( com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToJSONInputs inputs );


    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param inputs
     *        inputs
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "inputs" })
    public com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToMESResponse transferOperationToMES ( com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToMESInputs inputs );



}
