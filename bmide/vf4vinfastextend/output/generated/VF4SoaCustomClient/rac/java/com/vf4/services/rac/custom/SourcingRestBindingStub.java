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
public class SourcingRestBindingStub extends SourcingService
{
    private Sender                restSender;
    private Connection            localConnection;
    
   /**
     * Constructor
     * @param connection
     * @unpublished
     */
    public SourcingRestBindingStub( Connection connection )
    {
        this.localConnection = connection;
        this.restSender      = connection.getSender();
    }



    // each child interface has its own factory and methods for calling



    static final String SOURCING_201906_PORT_NAME          = "Custom-2019-06-Sourcing";
	
    @Override 
    public com.vf4.services.rac.custom._2019_06.Sourcing.STResponse getSourceTrackerDetails( String sourcingProgram, boolean isDELRequiredInPurchaseLevel, boolean isCostAttributesRequired, String[] selecetdUIDs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.SourcingRestBindingStub.getSourceTrackerDetails");

        Object[] sourceArgs = new Object[] { sourcingProgram, isDELRequiredInPurchaseLevel, isCostAttributesRequired, selecetdUIDs };

        Object outObj = restSender.invoke3( SOURCING_201906_PORT_NAME, "getSourceTrackerDetails", sourceArgs, 
                        com.vf4.services.rac.custom._2019_06.Sourcing.class);
		


        com.vf4.services.rac.custom._2019_06.Sourcing.STResponse localOut =
                        (com.vf4.services.rac.custom._2019_06.Sourcing.STResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.SourcingRestBindingStub.getSourceTrackerDetails");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }


    static final String SOURCING_202010_PORT_NAME          = "Custom-2020-10-Sourcing";
	
    @Override 
    public boolean planDateCalculation( String[] sourceparts ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.SourcingRestBindingStub.planDateCalculation");

        Object[] sourceArgs = new Object[] { sourceparts };

        Object outObj = restSender.invoke3( SOURCING_202010_PORT_NAME, "planDateCalculation", sourceArgs, 
                        com.vf4.services.rac.custom._2020_10.Sourcing.class);
		


        boolean localOut =
                        (boolean) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.SourcingRestBindingStub.planDateCalculation");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }


    static final String SOURCING_202012_PORT_NAME          = "Custom-2020-12-Sourcing";
	
    @Override 
    public com.vf4.services.rac.custom._2020_12.Sourcing.AssignSourcePartToBuyerResponse assignSourcePartToBuyer( com.vf4.services.rac.custom._2020_12.Sourcing.STBuyerAssignmentInfo[] inputs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.SourcingRestBindingStub.assignSourcePartToBuyer");

        Object[] sourceArgs = new Object[] { inputs };

        Object outObj = restSender.invoke3( SOURCING_202012_PORT_NAME, "assignSourcePartToBuyer", sourceArgs, 
                        com.vf4.services.rac.custom._2020_12.Sourcing.class);
		


        com.vf4.services.rac.custom._2020_12.Sourcing.AssignSourcePartToBuyerResponse localOut =
                        (com.vf4.services.rac.custom._2020_12.Sourcing.AssignSourcePartToBuyerResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.SourcingRestBindingStub.assignSourcePartToBuyer");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public com.vf4.services.rac.custom._2020_12.Sourcing.GetOrCreateVFCostResponse getOrCreateVFCost( com.vf4.services.rac.custom._2020_12.Sourcing.GetOrCreateVFCostInput[] inputs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.SourcingRestBindingStub.getOrCreateVFCost");

        Object[] sourceArgs = new Object[] { inputs };

        Object outObj = restSender.invoke3( SOURCING_202012_PORT_NAME, "getOrCreateVFCost", sourceArgs, 
                        com.vf4.services.rac.custom._2020_12.Sourcing.class);
		


        com.vf4.services.rac.custom._2020_12.Sourcing.GetOrCreateVFCostResponse localOut =
                        (com.vf4.services.rac.custom._2020_12.Sourcing.GetOrCreateVFCostResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.SourcingRestBindingStub.getOrCreateVFCost");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }


}
