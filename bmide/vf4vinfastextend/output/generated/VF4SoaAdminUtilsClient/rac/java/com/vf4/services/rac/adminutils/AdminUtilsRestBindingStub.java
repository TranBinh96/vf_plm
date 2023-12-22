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

package com.vf4.services.rac.adminutils;




import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.internal.client.Sender;



 /**
  * @unpublished
  */
public class AdminUtilsRestBindingStub extends AdminUtilsService
{
    private Sender                restSender;
    private Connection            localConnection;
    
   /**
     * Constructor
     * @param connection
     * @unpublished
     */
    public AdminUtilsRestBindingStub( Connection connection )
    {
        this.localConnection = connection;
        this.restSender      = connection.getSender();
    }



    // each child interface has its own factory and methods for calling



    static final String ADMINUTILS_202012_PORT_NAME          = "AdminUtils-2020-12-AdminUtils";
	
    @Override 
    public com.vf4.services.rac.adminutils._2020_12.AdminUtils.ChangeUOMResponse changeUOM( com.vf4.services.rac.adminutils._2020_12.AdminUtils.ChangeUOMInput[] inputs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.adminutils.AdminUtilsRestBindingStub.changeUOM");

        Object[] sourceArgs = new Object[] { inputs };

        Object outObj = restSender.invoke3( ADMINUTILS_202012_PORT_NAME, "changeUOM", sourceArgs, 
                        com.vf4.services.rac.adminutils._2020_12.AdminUtils.class);
		


        com.vf4.services.rac.adminutils._2020_12.AdminUtils.ChangeUOMResponse localOut =
                        (com.vf4.services.rac.adminutils._2020_12.AdminUtils.ChangeUOMResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.adminutils.AdminUtilsRestBindingStub.changeUOM");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public com.vf4.services.rac.adminutils._2020_12.AdminUtils.ReassignWFTaskResponse reassignWFTask( com.vf4.services.rac.adminutils._2020_12.AdminUtils.ReassignWFTaskInput[] inputs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.adminutils.AdminUtilsRestBindingStub.reassignWFTask");

        Object[] sourceArgs = new Object[] { inputs };

        Object outObj = restSender.invoke3( ADMINUTILS_202012_PORT_NAME, "reassignWFTask", sourceArgs, 
                        com.vf4.services.rac.adminutils._2020_12.AdminUtils.class);
		


        com.vf4.services.rac.adminutils._2020_12.AdminUtils.ReassignWFTaskResponse localOut =
                        (com.vf4.services.rac.adminutils._2020_12.AdminUtils.ReassignWFTaskResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.adminutils.AdminUtilsRestBindingStub.reassignWFTask");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public com.vf4.services.rac.adminutils._2020_12.AdminUtils.SetPropertyResponse setProperties( com.vf4.services.rac.adminutils._2020_12.AdminUtils.SetPropertyInput[] inputs, boolean isKeepModifiedDate ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.adminutils.AdminUtilsRestBindingStub.setProperties");

        Object[] sourceArgs = new Object[] { inputs, isKeepModifiedDate };

        Object outObj = restSender.invoke3( ADMINUTILS_202012_PORT_NAME, "setProperties", sourceArgs, 
                        com.vf4.services.rac.adminutils._2020_12.AdminUtils.class);
		


        com.vf4.services.rac.adminutils._2020_12.AdminUtils.SetPropertyResponse localOut =
                        (com.vf4.services.rac.adminutils._2020_12.AdminUtils.SetPropertyResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.adminutils.AdminUtilsRestBindingStub.setProperties");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }


}
