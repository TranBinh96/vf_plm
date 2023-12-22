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




import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.internal.client.Sender;



 /**
  * @unpublished
  */
public class ReportDataSourceRestBindingStub extends ReportDataSourceService
{
    private Sender                restSender;
    private Connection            localConnection;
    
   /**
     * Constructor
     * @param connection
     * @unpublished
     */
    public ReportDataSourceRestBindingStub( Connection connection )
    {
        this.localConnection = connection;
        this.restSender      = connection.getSender();
    }



    // each child interface has its own factory and methods for calling



    static final String REPORTDATASOURCE_201705_PORT_NAME          = "Custom-2017-05-ReportDataSource";
	
    @Override 
    public com.vf4.services.rac.custom._2017_05.ReportDataSource.CostReportDSOutput getCostReportDS( com.vf4.services.rac.custom._2017_05.ReportDataSource.CostReportDSInput input ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.getCostReportDS");

        Object[] sourceArgs = new Object[] { input };

        Object outObj = restSender.invoke3( REPORTDATASOURCE_201705_PORT_NAME, "getCostReportDS", sourceArgs, 
                        com.vf4.services.rac.custom._2017_05.ReportDataSource.class);
		


        com.vf4.services.rac.custom._2017_05.ReportDataSource.CostReportDSOutput localOut =
                        (com.vf4.services.rac.custom._2017_05.ReportDataSource.CostReportDSOutput) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.getCostReportDS");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }


    static final String REPORTDATASOURCE_202010_PORT_NAME          = "Custom-2020-10-ReportDataSource";
	
    @Override 
    public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartCostCalcResponse calculatePartsCost( String[] partNumbers, String programName, String isDryrun ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.calculatePartsCost");

        Object[] sourceArgs = new Object[] { partNumbers, programName, isDryrun };

        Object outObj = restSender.invoke3( REPORTDATASOURCE_202010_PORT_NAME, "calculatePartsCost", sourceArgs, 
                        com.vf4.services.rac.custom._2020_10.ReportDataSource.class);
		


        com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartCostCalcResponse localOut =
                        (com.vf4.services.rac.custom._2020_10.ReportDataSource.VFPartCostCalcResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.calculatePartsCost");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public com.vf4.services.rac.custom._2020_10.ReportDataSource.VFEcrCostResponse getEcrsCost( com.teamcenter.rac.kernel.TCComponent[] ecrs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.getEcrsCost");

        Object[] sourceArgs = new Object[] { ecrs };

        Object outObj = restSender.invoke3( REPORTDATASOURCE_202010_PORT_NAME, "getEcrsCost", sourceArgs, 
                        com.vf4.services.rac.custom._2020_10.ReportDataSource.class);
		


        com.vf4.services.rac.custom._2020_10.ReportDataSource.VFEcrCostResponse localOut =
                        (com.vf4.services.rac.custom._2020_10.ReportDataSource.VFEcrCostResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.getEcrsCost");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationOutput validateFamilyGroup( com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationInput input ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.validateFamilyGroup");

        Object[] sourceArgs = new Object[] { input };

        Object outObj = restSender.invoke3( REPORTDATASOURCE_202010_PORT_NAME, "validateFamilyGroup", sourceArgs, 
                        com.vf4.services.rac.custom._2020_10.ReportDataSource.class);
		


        com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationOutput localOut =
                        (com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationOutput) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.validateFamilyGroup");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }


    static final String REPORTDATASOURCE_202012_PORT_NAME          = "Custom-2020-12-ReportDataSource";
	
    @Override 
    public com.vf4.services.rac.custom._2020_12.ReportDataSource.Get150BomWithVariantsDSResponse get150BomWithVariantsDS( com.vf4.services.rac.custom._2020_12.ReportDataSource.Get150BomWithVariantsDSInput[] inputs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.get150BomWithVariantsDS");

        Object[] sourceArgs = new Object[] { inputs };

        Object outObj = restSender.invoke3( REPORTDATASOURCE_202012_PORT_NAME, "get150BomWithVariantsDS", sourceArgs, 
                        com.vf4.services.rac.custom._2020_12.ReportDataSource.class);
		


        com.vf4.services.rac.custom._2020_12.ReportDataSource.Get150BomWithVariantsDSResponse localOut =
                        (com.vf4.services.rac.custom._2020_12.ReportDataSource.Get150BomWithVariantsDSResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.get150BomWithVariantsDS");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public com.vf4.services.rac.custom._2020_12.ReportDataSource.PFEPReportResponse getPFEPReport( com.vf4.services.rac.custom._2020_12.ReportDataSource.PFEPReportInput[] input ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.getPFEPReport");

        Object[] sourceArgs = new Object[] { input };

        Object outObj = restSender.invoke3( REPORTDATASOURCE_202012_PORT_NAME, "getPFEPReport", sourceArgs, 
                        com.vf4.services.rac.custom._2020_12.ReportDataSource.class);
		


        com.vf4.services.rac.custom._2020_12.ReportDataSource.PFEPReportResponse localOut =
                        (com.vf4.services.rac.custom._2020_12.ReportDataSource.PFEPReportResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.getPFEPReport");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportResponse getProcessStatusReport( com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportInput input ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.getProcessStatusReport");

        Object[] sourceArgs = new Object[] { input };

        Object outObj = restSender.invoke3( REPORTDATASOURCE_202012_PORT_NAME, "getProcessStatusReport", sourceArgs, 
                        com.vf4.services.rac.custom._2020_12.ReportDataSource.class);
		


        com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportResponse localOut =
                        (com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.ReportDataSourceRestBindingStub.getProcessStatusReport");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }


}
