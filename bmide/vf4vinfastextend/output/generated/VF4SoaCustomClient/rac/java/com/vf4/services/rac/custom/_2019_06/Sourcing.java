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


package com.vf4.services.rac.custom._2019_06;


/**
 *
 */
@com.teamcenter.soa.internal.client.parse.XMLNamespace("http://vf4.com/Schemas/Custom/2019-06/Sourcing")
public interface Sourcing
{

    public class STResponse
    {
        /**
         * First Row of the excel(Display Names)
         */
        public java.util.Map<java.math.BigInteger, String> attrDisplayNames = new java.util.HashMap<java.math.BigInteger, String>();
        /**
         * Get the Attributes Values for the body of the excel
         */
        public java.util.Map<String, String[]> attrActualValues = new java.util.HashMap<String, String[]>();
        /**
         * Get the servicedata Object
         */
        public com.teamcenter.rac.kernel.ServiceData serviceData;
    }



    

    /**
     * .
     *
     * <br><br><b>Version:</b> ActiveWorkspace4.2<br>
     *
     * @param sourcingProgram
     *        Give the Sourcing Program as Input
     *
     * @param isDELRequiredInPurchaseLevel
     *        Is DEL attributes required, make it true
     *
     * @param isCostAttributesRequired
     *        If cost attributes required, make it true
     *
     * @param selecetdUIDs
     *        Selected UID
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "sourcingProgram", "isDELRequiredInPurchaseLevel", "isCostAttributesRequired", "selecetdUIDs" })
    public com.vf4.services.rac.custom._2019_06.Sourcing.STResponse getSourceTrackerDetails ( String sourcingProgram, boolean isDELRequiredInPurchaseLevel, boolean isCostAttributesRequired, String[] selecetdUIDs );



}
