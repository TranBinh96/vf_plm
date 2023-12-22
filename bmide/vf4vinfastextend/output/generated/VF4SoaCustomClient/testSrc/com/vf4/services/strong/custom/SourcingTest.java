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

package com.vf4.services.strong.custom;


import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelManager;
import com.teamcenter.utest.SoaSession;

import junit.framework.TestCase;

public class SourcingTest extends TestCase
{
    private Connection        connection;
    private ModelManager      manager;
    private SourcingService   service;
    

    public SourcingTest( String name )
    {
        super( name );
    }

    protected void setUp( ) throws Exception
    {
        super.setUp( );

        connection  = SoaSession.getConnection();
        manager     = connection.getModelManager();       
        service     = SourcingService.getService(connection);

    }
        
    
    public void testGetSourceTrackerDetails()
    {
        // TODO write test code, then remove fail()
        // service.getSourceTrackerDetails(  )
        fail("This test has not been implemented yet");
    }

    public void testPlanDateCalculation()
    {
        // TODO write test code, then remove fail()
        // service.planDateCalculation(  )
        fail("This test has not been implemented yet");
    }

    public void testAssignSourcePartToBuyer()
    {
        // TODO write test code, then remove fail()
        // service.assignSourcePartToBuyer(  )
        fail("This test has not been implemented yet");
    }

    public void testGetOrCreateVFCost()
    {
        // TODO write test code, then remove fail()
        // service.getOrCreateVFCost(  )
        fail("This test has not been implemented yet");
    }


}

