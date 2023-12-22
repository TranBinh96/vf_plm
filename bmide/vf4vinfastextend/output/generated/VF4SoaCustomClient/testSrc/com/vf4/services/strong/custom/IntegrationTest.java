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

public class IntegrationTest extends TestCase
{
    private Connection        connection;
    private ModelManager      manager;
    private IntegrationService   service;
    

    public IntegrationTest( String name )
    {
        super( name );
    }

    protected void setUp( ) throws Exception
    {
        super.setUp( );

        connection  = SoaSession.getConnection();
        manager     = connection.getModelManager();       
        service     = IntegrationService.getService(connection);

    }
        
    
    public void testNominateSupplierToSQP()
    {
        // TODO write test code, then remove fail()
        // service.nominateSupplierToSQP(  )
        fail("This test has not been implemented yet");
    }

    public void testRemoveSupplierInfo()
    {
        // TODO write test code, then remove fail()
        // service.removeSupplierInfo(  )
        fail("This test has not been implemented yet");
    }

    public void testUpdateQcheckerProperty()
    {
        // TODO write test code, then remove fail()
        // service.updateQcheckerProperty(  )
        fail("This test has not been implemented yet");
    }

    public void testStructureSearchInMPP()
    {
        // TODO write test code, then remove fail()
        // service.structureSearchInMPP(  )
        fail("This test has not been implemented yet");
    }


}

