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

package com.vf4.services.strong.adminutils;


import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelManager;
import com.teamcenter.utest.SoaSession;

import junit.framework.TestCase;

public class AdminUtilsTest extends TestCase
{
    private Connection        connection;
    private ModelManager      manager;
    private AdminUtilsService   service;
    

    public AdminUtilsTest( String name )
    {
        super( name );
    }

    protected void setUp( ) throws Exception
    {
        super.setUp( );

        connection  = SoaSession.getConnection();
        manager     = connection.getModelManager();       
        service     = AdminUtilsService.getService(connection);

    }
        
    
    public void testChangeUOM()
    {
        // TODO write test code, then remove fail()
        // service.changeUOM(  )
        fail("This test has not been implemented yet");
    }

    public void testReassignWFTask()
    {
        // TODO write test code, then remove fail()
        // service.reassignWFTask(  )
        fail("This test has not been implemented yet");
    }

    public void testSetProperties()
    {
        // TODO write test code, then remove fail()
        // service.setProperties(  )
        fail("This test has not been implemented yet");
    }


}

