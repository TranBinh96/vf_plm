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

package com.vf4.services.rac.cm;

import com.teamcenter.soa.SoaConstants;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.rac.kernel.TCSession;

/**
 * custom service for change management
 * <br>
 * <br>
 * <br>
 * <b>Library Reference:</b>
 * <ul>
 * <li type="disc">VF4SoaCMRac.jar
 * </li>
 * </ul>
 */

public abstract class ChangeManagementService
  implements     com.vf4.services.rac.cm._2020_12.ChangeManagement
{

    /**
     * 
     * @param session 
     * @return A instance of the service stub for the given TCSession
     */
    public static ChangeManagementService getService( TCSession session )
    {

        Connection connection = session.getSoaConnection();
        if(connection.getBinding().equalsIgnoreCase( SoaConstants.REST ))
        {
            return new ChangeManagementRestBindingStub( connection );
        }

        throw new IllegalArgumentException("The "+connection.getBinding()+" binding is not supported.");
    }

}
