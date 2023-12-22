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

package com.vf4.services.rac.integration;




import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.internal.client.Sender;



 /**
  * @unpublished
  */
public class SAPIntegrationRestBindingStub extends SAPIntegrationService
{
    private Sender                restSender;
    private Connection            localConnection;
    
   /**
     * Constructor
     * @param connection
     * @unpublished
     */
    public SAPIntegrationRestBindingStub( Connection connection )
    {
        this.localConnection = connection;
        this.restSender      = connection.getSender();
    }



    // each child interface has its own factory and methods for calling



    static final String SAPINTEGRATION_202012_PORT_NAME          = "Integration-2020-12-SAPIntegration";
	
    @Override 
    public com.vf4.services.rac.integration._2020_12.SAPIntegration.GetMasterMaterialsResponse getMasterMaterials( com.vf4.services.rac.integration._2020_12.SAPIntegration.GetMasterMaterialsInput[] inputs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.integration.SAPIntegrationRestBindingStub.getMasterMaterials");

        Object[] sourceArgs = new Object[] { inputs };

        Object outObj = restSender.invoke3( SAPINTEGRATION_202012_PORT_NAME, "getMasterMaterials", sourceArgs, 
                        com.vf4.services.rac.integration._2020_12.SAPIntegration.class);
		


        com.vf4.services.rac.integration._2020_12.SAPIntegration.GetMasterMaterialsResponse localOut =
                        (com.vf4.services.rac.integration._2020_12.SAPIntegration.GetMasterMaterialsResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.integration.SAPIntegrationRestBindingStub.getMasterMaterials");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToJSONResponse transferOperationJSONToMES( com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToJSONInputs inputs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.integration.SAPIntegrationRestBindingStub.transferOperationJSONToMES");

        Object[] sourceArgs = new Object[] { inputs };

        Object outObj = restSender.invoke3( SAPINTEGRATION_202012_PORT_NAME, "transferOperationJSONToMES", sourceArgs, 
                        com.vf4.services.rac.integration._2020_12.SAPIntegration.class);
		


        com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToJSONResponse localOut =
                        (com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToJSONResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.integration.SAPIntegrationRestBindingStub.transferOperationJSONToMES");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToMESResponse transferOperationToMES( com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToMESInputs inputs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.integration.SAPIntegrationRestBindingStub.transferOperationToMES");

        Object[] sourceArgs = new Object[] { inputs };

        Object outObj = restSender.invoke3( SAPINTEGRATION_202012_PORT_NAME, "transferOperationToMES", sourceArgs, 
                        com.vf4.services.rac.integration._2020_12.SAPIntegration.class);
		


        com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToMESResponse localOut =
                        (com.vf4.services.rac.integration._2020_12.SAPIntegration.TransferOperationToMESResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.integration.SAPIntegrationRestBindingStub.transferOperationToMES");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }


}
