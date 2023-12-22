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
@com.teamcenter.soa.internal.client.parse.XMLNamespace("http://vf4.com/Schemas/Custom/2017-05/Integration")
public interface Integration
{

    public class NominateSupplierInput
    {
        /**
         * List of VF Part Numbers
         */
        public String[] vfPartNumbers = new String[0];
        /**
         * Supplier Code
         */
        public String supplierCode = "";
        /**
         * Manufacturing Location present by String
         */
        public String manufacturingLocation = "";
        /**
         * If true, Karaf event will be triggerred. If false, it's not triggered.
         */
        public boolean isTriggerKarafEvent;
    }



    

    /**
     * Nominate a list of parts to a supplier (SQ portal).
     *
     * <br><br><b>Version:</b> Teamcenter 11.3<br>
     *
     * @param nominateSupplierInput
     *        Parts list and nominated supplier code.
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "nominateSupplierInput" })
    public boolean nominateSupplierToSQP ( com.vf4.services.rac.custom._2017_05.Integration.NominateSupplierInput nominateSupplierInput );


    /**
     * clean supplier info on source part.
     *
     * <br><br><b>Version:</b> Teamcenter 11.3<br>
     *
     * @param sourcePartNo
     *        Source Part Number
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "sourcePartNo" })
    public boolean removeSupplierInfo ( String sourcePartNo );


    /**
     * update v4_q_checker without update last modified user.
     *
     * <br><br><b>Version:</b> Teamcenter 11.3<br>
     *
     * @param designRevision
     *        Vf4 DesignRevision
     *
     * @param result
     *        Qchecker result
     *
     * @return
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "designRevision", "result" })
    public boolean updateQcheckerProperty ( com.teamcenter.rac.kernel.TCComponentItemRevision designRevision, String result );



}
