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

package com.vf4.services.strong.cm;


import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelManager;
import com.teamcenter.utest.SoaSession;

import junit.framework.TestCase;

public class ChangeManagementTest extends TestCase
{
    private Connection        connection;
    private ModelManager      manager;
    private ChangeManagementService   service;
    

    public ChangeManagementTest( String name )
    {
        super( name );
    }

    protected void setUp( ) throws Exception
    {
        super.setUp( );

        connection  = SoaSession.getConnection();
        manager     = connection.getModelManager();       
        service     = ChangeManagementService.getService(connection);

    }
        
    
    public void testDeriveChange()
    {
        // TODO write test code, then remove fail()
        // service.deriveChange(  )
        fail("This test has not been implemented yet");
    }

    public void testGetImpactedPrograms()
    {
        // TODO write test code, then remove fail()
        // service.getImpactedPrograms(  )
        fail("This test has not been implemented yet");
    }

    public void testGetPartNumberChangeHistory()
    {
        // TODO write test code, then remove fail()
        // service.getPartNumberChangeHistory(  )
        fail("This test has not been implemented yet");
    }


}

