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
@com.teamcenter.soa.internal.client.parse.XMLNamespace("http://vf4.com/Schemas/Custom/2020-12/Sourcing")
public interface Sourcing
{

    public class AssignSourcePartToBuyerOutput
    {
        /**
         * VF Part Number
         */
        public String vfPartNumber = "";
        /**
         * Sourcing Program
         */
        public String sourcingProgram = "";
        /**
         * Error message
         */
        public String errorMessage = "";
    }


    public class AssignSourcePartToBuyerResponse
    {
        /**
         * List of ouput
         */
        public com.vf4.services.rac.custom._2020_12.Sourcing.AssignSourcePartToBuyerOutput[] outputs = new com.vf4.services.rac.custom._2020_12.Sourcing.AssignSourcePartToBuyerOutput[0];
        /**
         * Service Data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    public class GetOrCreateVFCostInput
    {
        /**
         * Part number
         */
        public String partNumber = "";
        /**
         * Revision id of the part
         */
        public String partRevId = "";
        /**
         * Part type
         */
        public String partType = "";
        /**
         * Create and relate VF Cost object to part if not existed
         */
        public boolean createIfNotExisted;
    }


    public class GetOrCreateVFCostOutput
    {
        /**
         * Part number
         */
        public String partNumber = "";
        /**
         * Part type
         */
        public String partType = "";
        /**
         * VF Cost object
         */
        public com.teamcenter.rac.kernel.TCComponent vfCost = null;
        /**
         * Error message (if any)
         */
        public String errorMessage = "";
    }


    public class GetOrCreateVFCostResponse
    {
        /**
         * List of output
         */
        public com.vf4.services.rac.custom._2020_12.Sourcing.GetOrCreateVFCostOutput[] outputs = new com.vf4.services.rac.custom._2020_12.Sourcing.GetOrCreateVFCostOutput[0];
        /**
         * Service data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    public class STBuyerAssignmentInfo
    {
        /**
         * VF Part Number to search source part
         */
        public String vfPartNumber = "";
        /**
         * Sourcing Program
         */
        public String sourcingProgram = "";
        /**
         * Assigning TC user ID
         */
        public String assigningUserID = "";
    }



    

    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param inputs
     *        List of assignment info
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "inputs" })
    public com.vf4.services.rac.custom._2020_12.Sourcing.AssignSourcePartToBuyerResponse assignSourcePartToBuyer ( com.vf4.services.rac.custom._2020_12.Sourcing.STBuyerAssignmentInfo[] inputs );


    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param inputs
     *        List of inputs
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "inputs" })
    public com.vf4.services.rac.custom._2020_12.Sourcing.GetOrCreateVFCostResponse getOrCreateVFCost ( com.vf4.services.rac.custom._2020_12.Sourcing.GetOrCreateVFCostInput[] inputs );



}
