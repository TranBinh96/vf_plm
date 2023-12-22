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

package com.vf4.services.strong.integration;


import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelManager;
import com.teamcenter.utest.SoaSession;

import junit.framework.TestCase;

public class SAPIntegrationTest extends TestCase
{
    private Connection        connection;
    private ModelManager      manager;
    private SAPIntegrationService   service;
    

    public SAPIntegrationTest( String name )
    {
        super( name );
    }

    protected void setUp( ) throws Exception
    {
        super.setUp( );

        connection  = SoaSession.getConnection();
        manager     = connection.getModelManager();       
        service     = SAPIntegrationService.getService(connection);

    }
        
    
    public void testGetMasterMaterials()
    {
        // TODO write test code, then remove fail()
        // service.getMasterMaterials(  )
        fail("This test has not been implemented yet");
    }

    public void testTransferOperationJSONToMES()
    {
        // TODO write test code, then remove fail()
        // service.transferOperationJSONToMES(  )
        fail("This test has not been implemented yet");
    }

    public void testTransferOperationToMES()
    {
        // TODO write test code, then remove fail()
        // service.transferOperationToMES(  )
        fail("This test has not been implemented yet");
    }


}

