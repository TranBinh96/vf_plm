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


package com.vf4.services.rac.adminutils._2020_12;


/**
 *
 */
@com.teamcenter.soa.internal.client.parse.XMLNamespace("http://vf4.com/Schemas/AdminUtils/2020-12/AdminUtils")
public interface AdminUtils
{

    public class ChangeUOMInput
    {
        /**
         * Part Number of the Item/Part
         */
        public String partNumber = "";
        /**
         * Part Type of the Item/Part
         */
        public String partType = "";
        /**
         * Part Unit of Measure
         */
        public String partUOM = "";
    }


    public class ChangeUOMOutput
    {
        /**
         * Part Number of the Item/Part
         */
        public String partNumber = "";
        /**
         * Part Type of the Item/Part
         */
        public String partType = "";
        /**
         * Part/Item Unit of Measure
         */
        public String partUOM = "";
        /**
         * Error Message for the Service
         */
        public String errorMessage = "";
    }


    public class ChangeUOMResponse
    {
        /**
         * ChangeUOM Response
         */
        public com.vf4.services.rac.adminutils._2020_12.AdminUtils.ChangeUOMOutput[] outputs = new com.vf4.services.rac.adminutils._2020_12.AdminUtils.ChangeUOMOutput[0];
        /**
         * Service Data  for ChangeUOM
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    public class PropertyAndValues
    {
        /**
         * property which needs to be change
         */
        public String property = "";
        /**
         * vector of property values
         */
        public String[] values = new String[0];
    }


    public class ReassignWFTaskInput
    {
        /**
         * Currently Assigned User Id
         */
        public String userId = "";
        /**
         * Process Template Name
         */
        public String processTemplateName = "";
        /**
         * New User Id
         */
        public String newUserId = "";
        /**
         * New User Group
         */
        public String newUserGroup = "";
        /**
         * Number of processes executed from query result form the first record.
         */
        public int executingProcessesNum;
        /**
         * isDryRun
         */
        public boolean isDryRun;
        /**
         * object Type
         */
        public String objectType = "";
        /**
         * vector of property Name And Value
         */
        public com.vf4.services.rac.adminutils._2020_12.AdminUtils.WFTaskPropertyAndValue[] propertyNameAndValue = new com.vf4.services.rac.adminutils._2020_12.AdminUtils.WFTaskPropertyAndValue[0];
    }


    public class ReassignWFTaskOutput
    {
        /**
         * Reassigned task
         */
        public com.teamcenter.rac.kernel.TCComponent reasignedTask = null;
        /**
         * Process Template Name
         */
        public String templateName = "";
        /**
         * taskName
         */
        public String taskName = "";
        /**
         * taskType
         */
        public String taskType = "";
        /**
         * assigned/signoff Users
         */
        public String assignedUsers = "";
        /**
         * return status
         */
        public String returnStatus = "";
        /**
         * Error Message
         */
        public String errorMessage = "";
    }


    public class ReassignWFTaskResponse
    {
        /**
         * ReassignWFTask Response
         */
        public com.vf4.services.rac.adminutils._2020_12.AdminUtils.ReassignWFTaskOutput[] outputs = new com.vf4.services.rac.adminutils._2020_12.AdminUtils.ReassignWFTaskOutput[0];
        /**
         * Service Data
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    public class SetPropertyError
    {
        /**
         * error Object
         */
        public com.teamcenter.rac.kernel.TCComponent errorObject = null;
        /**
         * error string
         */
        public String errorString = "";
    }


    public class SetPropertyInput
    {
        /**
         * object uid
         */
        public String uid = "";
        /**
         * vector of property and values
         */
        public com.vf4.services.rac.adminutils._2020_12.AdminUtils.PropertyAndValues[] propertyAndValues = new com.vf4.services.rac.adminutils._2020_12.AdminUtils.PropertyAndValues[0];
    }


    public class SetPropertyResponse
    {
        /**
         * error
         */
        public com.vf4.services.rac.adminutils._2020_12.AdminUtils.SetPropertyError[] errors = new com.vf4.services.rac.adminutils._2020_12.AdminUtils.SetPropertyError[0];
        /**
         * serviceData
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }


    public class WFTaskPropertyAndValue
    {
        /**
         * PropertyName
         */
        public String propertyName = "";
        /**
         * Property Value
         */
        public String propertyValue = "";
    }



    

    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param inputs
     *        input for ChangeUOM
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "inputs" })
    public com.vf4.services.rac.adminutils._2020_12.AdminUtils.ChangeUOMResponse changeUOM ( com.vf4.services.rac.adminutils._2020_12.AdminUtils.ChangeUOMInput[] inputs );


    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param inputs
     *        input of reassignWFTask service
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "inputs" })
    public com.vf4.services.rac.adminutils._2020_12.AdminUtils.ReassignWFTaskResponse reassignWFTask ( com.vf4.services.rac.adminutils._2020_12.AdminUtils.ReassignWFTaskInput[] inputs );


    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace5.1<br>
     *
     * @param inputs
     *        inputs
     *
     * @param isKeepModifiedDate
     *        flag to Keep Modified Date
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "inputs", "isKeepModifiedDate" })
    public com.vf4.services.rac.adminutils._2020_12.AdminUtils.SetPropertyResponse setProperties ( com.vf4.services.rac.adminutils._2020_12.AdminUtils.SetPropertyInput[] inputs, boolean isKeepModifiedDate );



}
