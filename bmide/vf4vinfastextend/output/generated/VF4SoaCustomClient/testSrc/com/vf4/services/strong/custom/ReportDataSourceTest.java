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

public class ReportDataSourceTest extends TestCase
{
    private Connection        connection;
    private ModelManager      manager;
    private ReportDataSourceService   service;
    

    public ReportDataSourceTest( String name )
    {
        super( name );
    }

    protected void setUp( ) throws Exception
    {
        super.setUp( );

        connection  = SoaSession.getConnection();
        manager     = connection.getModelManager();       
        service     = ReportDataSourceService.getService(connection);

    }
        
    
    public void testGetCostReportDS()
    {
        // TODO write test code, then remove fail()
        // service.getCostReportDS(  )
        fail("This test has not been implemented yet");
    }

    public void testCalculatePartsCost()
    {
        // TODO write test code, then remove fail()
        // service.calculatePartsCost(  )
        fail("This test has not been implemented yet");
    }

    public void testGetEcrsCost()
    {
        // TODO write test code, then remove fail()
        // service.getEcrsCost(  )
        fail("This test has not been implemented yet");
    }

    public void testValidateFamilyGroup()
    {
        // TODO write test code, then remove fail()
        // service.validateFamilyGroup(  )
        fail("This test has not been implemented yet");
    }

    public void testGet150BomWithVariantsDS()
    {
        // TODO write test code, then remove fail()
        // service.get150BomWithVariantsDS(  )
        fail("This test has not been implemented yet");
    }

    public void testGetPFEPReport()
    {
        // TODO write test code, then remove fail()
        // service.getPFEPReport(  )
        fail("This test has not been implemented yet");
    }

    public void testGetProcessStatusReport()
    {
        // TODO write test code, then remove fail()
        // service.getProcessStatusReport(  )
        fail("This test has not been implemented yet");
    }


}

