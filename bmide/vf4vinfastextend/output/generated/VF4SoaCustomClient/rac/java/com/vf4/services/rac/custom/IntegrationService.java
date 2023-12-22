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

package com.vf4.services.rac.custom;

import com.teamcenter.soa.SoaConstants;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Services for systems integration
 * <br>
 * <br>
 * <br>
 * <b>Library Reference:</b>
 * <ul>
 * <li type="disc">VF4SoaCustomRac.jar
 * </li>
 * </ul>
 */

public abstract class IntegrationService
  implements     com.vf4.services.rac.custom._2017_05.Integration,
    com.vf4.services.rac.custom._2020_12.Integration
{

    /**
     * 
     * @param session 
     * @return A instance of the service stub for the given TCSession
     */
    public static IntegrationService getService( TCSession session )
    {

        Connection connection = session.getSoaConnection();
        if(connection.getBinding().equalsIgnoreCase( SoaConstants.REST ))
        {
            return new IntegrationRestBindingStub( connection );
        }

        throw new IllegalArgumentException("The "+connection.getBinding()+" binding is not supported.");
    }

}
