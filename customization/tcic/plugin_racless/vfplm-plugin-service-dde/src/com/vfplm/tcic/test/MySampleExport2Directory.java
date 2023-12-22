package com.vfplm.tcic.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.ebsolutions.locale.InternalUsage;
import com.ebsolutions.locale.RegistryManager;
import com.ebsolutions.soacore.process.DoExportToDirectoryAction;
import com.ebsolutions.soacore.process.DoSilentExportToDirectoryAction;
import com.ebsolutions.soacore.process.ProcessConfiguration;
import com.ebsolutions.soacore.process.RACLessProcessAbstract;
import com.ebsolutions.soacore.process.UserExportCommand;
import com.ebsolutions.soacore.process.messages.MessageMgtAbstract;
import com.ebsolutions.utility.ui.dialogs.DialogAbstract;
import com.teamcenter.soa.client.model.ModelObject;


/**
* Sample  : tcic_selection.xml file is used to define the elements to export during the Export process <br>
* Note :  <br>
* -  by default, silent mode is set.
*   To execute the process in a non silent mode, set « is_silent » parameter to false.<br>
*   TCP sample from https://www.bogotobogo.com/Java/tutorials/tcp_socket_server_client.php
*/
public class MySampleExport2Directory extends DoSilentExportToDirectoryAction
{

    /**
     * This code indicate success
     */
    public final static int _CteSTATUS_OK = 0; 
    
    /**
     * This code indicate general failure.
     */
    public final static int _CteSTATUS_ERROR = 1;
    
	
    ////////////////////////////////////////////////////////////////////////////
	// PROCESS 
	////////////////////////////////////////////////////////////////////////////

	
	@Override
	protected int coreProcess() {
		
		net_report_status("TCIC_CORE");
		
		int x =  super.coreProcess();

		
      	if(!net_report_status(x == this._CteSTATUS_OK? "TCIC_CORE:EXPORT:OK" : "TCIC_CORE:EXPORT:FAIL")){};

		return x;
	}
	
	@Override
	protected int initProcessConfiguration() {
		// TODO Auto-generated method stub
		int x = super.initProcessConfiguration();
		
	    msg_manager = _process_configuration.get_message_manager();
	      
    	if(!net_report_status("TCIC_INIT")){}; //return _CteSTATUS_ERROR;
      	if(!net_report_status("TCIC_INIT:OK")) {}//return _CteSTATUS_ERROR;

		return x;
	}
	
	@Override
	protected int postProcessTasks() {
		// TODO Auto-generated method stub
		int x =  super.postProcessTasks();
		

        if(!net_report_status("TCIC_POST")) {}//return _CteSTATUS_ERROR;
        net_close();
        
		return x;
	}
	
    ////////////////////////////////////////////////////////////////////////////
	// ULTILITY
	////////////////////////////////////////////////////////////////////////////
	private MessageMgtAbstract msg_manager;

	void writeMessageInLogFile(String s) {
    	
        msg_manager.showMessage(s, "", DialogAbstract._CteINFO_DIALOG);

    }
	
	
    ////////////////////////////////////////////////////////////////////////////
	// S O C K E T  M O N I T O R
	////////////////////////////////////////////////////////////////////////////
    Socket socket = null;
    
    private boolean net_connect() {
    	
		int serverPort = 21400;
		InetAddress host;
		try {
			host = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
		System.out.println("Connecting to server on port " + serverPort); 

		try {
			socket = new Socket(host,serverPort);
			 toServer = new PrintWriter(socket.getOutputStream(),true);
			 toServer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("new Socket:" + e.getMessage());
			return false;
		} 
		
		System.out.println("Just connected to " + socket.getRemoteSocketAddress()); 
		
		return true;
    }
    

    PrintWriter toServer = null;
    
    boolean net_close() 
    {
    	try {
    		toServer.close();
			socket.close();
			writeMessageInLogFile("CLOSED SOCKET!");
		} catch (IOException e) {
			e.printStackTrace();
			writeMessageInLogFile("CLOSING SOCKET ERROR!");
			return false;
		}
    	return true;
    }
    
    
    boolean net_report_status(String stt) 
    {
        System.out.println(stt);

		if((socket == null) || !socket.isConnected())
		{
			boolean net_stt = net_connect();
			System.out.println(net_stt? "Net connected!" : "Net no Connection");
			if(!net_stt) {
				return false;
			}
		}
		
		//2. report if net connected
		try {
			toServer.println(stt); 
			toServer.flush();
				return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("toServer.println:"+e.getMessage());

		}

		return false;
    }
    
}
