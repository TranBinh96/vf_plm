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
@com.teamcenter.soa.internal.client.parse.XMLNamespace("http://vf4.com/Schemas/Custom/2020-10/Sourcing")
public interface Sourcing
{


    

    /**
     * processing plan dates plan dates.
     *
     * <br><br><b>Version:</b> VF_2020_10<br>
     *
     * @param sourceparts
     *        Input: list of source parts
     *
     * @return
     *         return boolean true and false
     *
     */
    @com.teamcenter.soa.internal.client.parse.InputParam(names = { "sourceparts" })
    public boolean planDateCalculation ( String[] sourceparts );



}
