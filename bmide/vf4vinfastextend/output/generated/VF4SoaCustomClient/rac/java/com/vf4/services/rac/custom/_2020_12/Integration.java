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
@com.teamcenter.soa.internal.client.parse.XMLNamespace("http://vf4.com/Schemas/Custom/2020-12/Integration")
public interface Integration
{

    public class StructureSearchInMPPInput
    {
        /**
         * Search Scope for Structure Search
         */
        public com.teamcenter.rac.kernel.TCComponentBOMLine searchScope = null;
        /**
         * Query for Structure Search
         */
        public String queryName = "";
        /**
         * Query Criteria for Structure Search
         */
        public String[] queryCriteria = new String[0];
        /**
         * Query Criteria Values for Structure Search
         */
        public String[] queryCriteriaValues = new String[0];
    }


    public class StructureSearchInMPPOutput
    {
        /**
         * Found lines from structure search
         */
        public com.teamcenter.rac.kernel.TCComponentBOMLine[] foundLines = new com.teamcenter.rac.kernel.TCComponentBOMLine[0];
        /**
         * Service Data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
        /**
         * custom error msg
         */
        public String errorString = "";
    }



    

    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param input
     *        Input for service StructureSearchInMPP
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "input" })
    public com.vf4.services.rac.custom._2020_12.Integration.StructureSearchInMPPOutput structureSearchInMPP ( com.vf4.services.rac.custom._2020_12.Integration.StructureSearchInMPPInput input );



}
