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
public class IntegrationRestBindingStub extends IntegrationService
{
    private Sender                restSender;
    private Connection            localConnection;
    
   /**
     * Constructor
     * @param connection
     * @unpublished
     */
    public IntegrationRestBindingStub( Connection connection )
    {
        this.localConnection = connection;
        this.restSender      = connection.getSender();
    }



    // each child interface has its own factory and methods for calling



    static final String INTEGRATION_201705_PORT_NAME          = "Custom-2017-05-Integration";
	
    @Override 
    public boolean nominateSupplierToSQP( com.vf4.services.rac.custom._2017_05.Integration.NominateSupplierInput nominateSupplierInput ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.IntegrationRestBindingStub.nominateSupplierToSQP");

        Object[] sourceArgs = new Object[] { nominateSupplierInput };

        Object outObj = restSender.invoke3( INTEGRATION_201705_PORT_NAME, "nominateSupplierToSQP", sourceArgs, 
                        com.vf4.services.rac.custom._2017_05.Integration.class);
		


        boolean localOut =
                        (boolean) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.IntegrationRestBindingStub.nominateSupplierToSQP");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public boolean removeSupplierInfo( String sourcePartNo ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.IntegrationRestBindingStub.removeSupplierInfo");

        Object[] sourceArgs = new Object[] { sourcePartNo };

        Object outObj = restSender.invoke3( INTEGRATION_201705_PORT_NAME, "removeSupplierInfo", sourceArgs, 
                        com.vf4.services.rac.custom._2017_05.Integration.class);
		


        boolean localOut =
                        (boolean) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.IntegrationRestBindingStub.removeSupplierInfo");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public boolean updateQcheckerProperty( com.teamcenter.rac.kernel.TCComponentItemRevision designRevision, String result ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.IntegrationRestBindingStub.updateQcheckerProperty");

        Object[] sourceArgs = new Object[] { designRevision, result };

        Object outObj = restSender.invoke3( INTEGRATION_201705_PORT_NAME, "updateQcheckerProperty", sourceArgs, 
                        com.vf4.services.rac.custom._2017_05.Integration.class);
		


        boolean localOut =
                        (boolean) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.IntegrationRestBindingStub.updateQcheckerProperty");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }


    static final String INTEGRATION_202012_PORT_NAME          = "Custom-2020-12-Integration";
	
    @Override 
    public com.vf4.services.rac.custom._2020_12.Integration.StructureSearchInMPPOutput structureSearchInMPP( com.vf4.services.rac.custom._2020_12.Integration.StructureSearchInMPPInput input ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.custom.IntegrationRestBindingStub.structureSearchInMPP");

        Object[] sourceArgs = new Object[] { input };

        Object outObj = restSender.invoke3( INTEGRATION_202012_PORT_NAME, "structureSearchInMPP", sourceArgs, 
                        com.vf4.services.rac.custom._2020_12.Integration.class);
		


        com.vf4.services.rac.custom._2020_12.Integration.StructureSearchInMPPOutput localOut =
                        (com.vf4.services.rac.custom._2020_12.Integration.StructureSearchInMPPOutput) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.custom.IntegrationRestBindingStub.structureSearchInMPP");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }


}
