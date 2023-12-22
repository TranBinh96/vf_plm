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

package com.vf4.services.rac.cm;




import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.internal.client.Sender;



 /**
  * @unpublished
  */
public class ChangeManagementRestBindingStub extends ChangeManagementService
{
    private Sender                restSender;
    private Connection            localConnection;
    
   /**
     * Constructor
     * @param connection
     * @unpublished
     */
    public ChangeManagementRestBindingStub( Connection connection )
    {
        this.localConnection = connection;
        this.restSender      = connection.getSender();
    }



    // each child interface has its own factory and methods for calling



    static final String CHANGEMANAGEMENT_202012_PORT_NAME          = "CM-2020-12-ChangeManagement";
	
    @Override 
    public com.vf4.services.rac.cm._2020_12.ChangeManagement.DeriveChangeResponse deriveChange( com.vf4.services.rac.cm._2020_12.ChangeManagement.DeriveChangeInput[] inputs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.cm.ChangeManagementRestBindingStub.deriveChange");

        Object[] sourceArgs = new Object[] { inputs };

        Object outObj = restSender.invoke3( CHANGEMANAGEMENT_202012_PORT_NAME, "deriveChange", sourceArgs, 
                        com.vf4.services.rac.cm._2020_12.ChangeManagement.class);
		


        com.vf4.services.rac.cm._2020_12.ChangeManagement.DeriveChangeResponse localOut =
                        (com.vf4.services.rac.cm._2020_12.ChangeManagement.DeriveChangeResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.cm.ChangeManagementRestBindingStub.deriveChange");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public com.vf4.services.rac.cm._2020_12.ChangeManagement.ImpactedProgramResponse getImpactedPrograms( com.vf4.services.rac.cm._2020_12.ChangeManagement.ImpactedProgramInput inputs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.cm.ChangeManagementRestBindingStub.getImpactedPrograms");

        Object[] sourceArgs = new Object[] { inputs };

        Object outObj = restSender.invoke3( CHANGEMANAGEMENT_202012_PORT_NAME, "getImpactedPrograms", sourceArgs, 
                        com.vf4.services.rac.cm._2020_12.ChangeManagement.class);
		


        com.vf4.services.rac.cm._2020_12.ChangeManagement.ImpactedProgramResponse localOut =
                        (com.vf4.services.rac.cm._2020_12.ChangeManagement.ImpactedProgramResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.cm.ChangeManagementRestBindingStub.getImpactedPrograms");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }
	
    @Override 
    public com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistoryResponse getPartNumberChangeHistory( com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistoryInput[] inputs ) 
    
    {
       try
       {
        restSender.pushRequestId();
        com.teamcenter.soa.internal.common.Monitor.markStart("com.vf4.services.rac.cm.ChangeManagementRestBindingStub.getPartNumberChangeHistory");

        Object[] sourceArgs = new Object[] { inputs };

        Object outObj = restSender.invoke3( CHANGEMANAGEMENT_202012_PORT_NAME, "getPartNumberChangeHistory", sourceArgs, 
                        com.vf4.services.rac.cm._2020_12.ChangeManagement.class);
		


        com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistoryResponse localOut =
                        (com.vf4.services.rac.cm._2020_12.ChangeManagement.PartNumberChangeHistoryResponse) outObj;
        
        if(!localConnection.getOption(Connection.OPT_CACHE_MODEL_OBJECTS).equals( "true" ))
      	{
        	localConnection.getClientDataModel().removeAllObjects(); 
        }
        com.teamcenter.soa.internal.common.Monitor.markEnd  ("com.vf4.services.rac.cm.ChangeManagementRestBindingStub.getPartNumberChangeHistory");
         return localOut;
       }
       finally
       {
        restSender.popRequestId();
       }
    }


}
