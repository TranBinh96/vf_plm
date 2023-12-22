package com.vfplm.tcic.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ebsolutions.locale.InternalUsage;
import com.ebsolutions.locale.RegistryManager;
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
public class MySampleSilentExport extends RACLessProcessAbstract
{
    /**
     * API class used to manage Export process.
     */
    private UserExportCommand _export_api =null;
    
     /**
     *  Boolean to define if the process is in silent mode.
     *  By default, the value is true so that the process is performed in silent mode
     */
    private boolean _is_silent = true;

	private MessageMgtAbstract msg_manager;
    
    
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
    
    /**
     * Core process method 
     */
    @Override
    protected int coreProcess() 
    {
    	
        int failcode = _CteSTATUS_OK;
      	net_report_status("TCIC_CORE");
        writeMessageInLogFile("ENTERED TCIC_CORE");

         int nb_process = 0;
        
        try {
         nb_process = _export_api.getNumberOfProcess();
        
         
        } catch (Exception e) {
            e.printStackTrace();
            failcode = _CteSTATUS_ERROR;
            writeMessageInLogFile(e.getLocalizedMessage());
        }
        
        //writeMessageInLogFile("Number Of Process = "+nb_process );
        /*
         * loop on each input
         */
        for (int i = 0; i < nb_process; i++) 
        {
           // writeMessageInLogFile("\nProcess N° = " + (i + 1) + ":");
            try
            {

                /* Create the export spreadsheet */
                failcode = _export_api.createSpreadsheet(i);

            }
            catch(Exception e)
            {
                e.printStackTrace();
                failcode = _CteSTATUS_ERROR;
                String message = RegistryManager.get_instance()
                                                .getString("soacore.process.UserExportCommand.exceptionSpreadsheet");
                /* write message in process log file */
                writeMessageInLogFile(message);
            }
            
          	if(!net_report_status(failcode == _CteSTATUS_OK? "TCIC_CORE:SPREADSHEET:OK" : "TCIC_CORE:SPREADSHEET:FAIL")){};

          	//TODO: post process of remove unwanted objects in exported spreadsheet (Part by MML link, CATdrawings)
          	boolean _b_catlist_stt = vfex_execute_cat_list_export_file();
          	if(!net_report_status(_b_catlist_stt? "TCIC_CORE:SPREADSHEET:MANUAL:OK" : "TCIC_CORE:SPREADSHEET:MANUAL:FAIL")){};

          	
            if ((_CteSTATUS_OK == failcode) && _b_catlist_stt)
            {
                try
                {
                    /* Export */
                    failcode = _export_api.export(i);

                }
                catch(Exception e)
                {
                	String err = e.getMessage();
                	//msg_manager.showMessage("Error Exporting:"+ err, "Exporting", 1);
                    e.printStackTrace();
                    failcode = _CteSTATUS_ERROR;
                    String message = RegistryManager.get_instance()
                                                    .getString("soacore.process.UserExportCommand.exceptionExport");
                    writeMessageInLogFile(message);
                }
            }
        }

      	if(!net_report_status(failcode == _CteSTATUS_OK? "TCIC_CORE:EXPORT:OK" : "TCIC_CORE:EXPORT:FAIL")){};

        
        get_process_configuration().clearSelectedElements();
        // Attempt to unlock the socket server
        unlockServer();
        
        if(!net_report_status(failcode == _CteSTATUS_OK? "TCIC_CORE:OK" : "TCIC_CORE:FAIL")){}// return _CteSTATUS_ERROR;
        
        return failcode;
    }

    ////////////////////////////////////////////////////////////////////////////


	/**
     * Override the initProcessConfiguration method
     */
    @Override
    protected int initProcessConfiguration() 
    {
    	
    	if(!net_report_status("TCIC_INIT")){}; //return _CteSTATUS_ERROR;
    	
        /*initialize API and define silent mode for the process*/
        _export_api = new UserExportCommand(_is_silent);
        
        /* retrieve the default configuration */
        ProcessConfiguration configuration = _export_api.getDefaultSampleConfiguration();
        
        /* Abort if no valid process configuration is returned */
        if(null==configuration)
        {
        	if(!net_report_status("TCIC_INIT:FAIL")){}// return _CteSTATUS_ERROR;
            return _CteSTATUS_ERROR;
        }
      
        /*set process configuration*/
        set_process_configuration(configuration);
       
	      msg_manager = _process_configuration.get_message_manager();

	      
        /**
         * if ProcessConfiguraiton customization,
         * it is necessary to update UserExportCommand configuration.
         *    _export_api.set_process_configuration(configuration);
         */

      	if(!net_report_status("TCIC_INIT:OK")) {}//return _CteSTATUS_ERROR;

        return _CteSTATUS_OK;
    }
    
    /**
     * Method called after coreProcess method call.
     */
    @Override
    protected int postProcessTasks() {
        if(!net_report_status("TCIC_POST")) {}//return _CteSTATUS_ERROR;
        net_close();
        
        return _CteSTATUS_OK;
    }

    
    /*********************************************************************
     * Method called before coreProcess method call.
     */
    @Override
    protected int preProcessTasks() {
    	
        return _CteSTATUS_OK;
    }
    
    
    /**
     * Overload the lockGateway method
     */
    @Override
	protected int lockGateway() 
    {
        System.out.println("====LOCK_GATEWAY===");

    	// If the lock fails then the process is stopped.
        int fail_code = lockServer();
        
        System.out.println("====LOCK_GATEWAY:" + (fail_code== _CteSTATUS_OK? "OK" : "FAIL"));

        // If the lock fails the process has to terminate
        if(fail_code != _CteSTATUS_OK)
        {
            net_report_status("TCIC:BUSY");
            net_close();
        	 String message = RegistryManager.get_instance().getString("soacore.process.RACLessProcessAbstract.serverBusy");
             /*write in RACless log file*/
             System.out.println(message);
             /*write in process log file*/
             writeMessageInLogFile(message);
        }
        
        return fail_code;
    }
   	
    ////////////////////////////////////////////////////////////////////////////
	// ULTILITY
	////////////////////////////////////////////////////////////////////////////


   	public static boolean vfex_execute_cat_list_export_file() {
   		boolean ret = false;
   		
      	//1. check existence of file %WORK_DIR%\\tmp\\vfex_cat_list.txt
   		
   		String wrk_dir = System.getenv("WORK_DIR");
   		
   		File _f_catlist = new File(wrk_dir +"\\tmp\\vfex_cat_list.txt");
   		boolean exists = _f_catlist.exists();
   		
   		if(!exists) return true;
   		
   		//1.1 read %WORK_DIR%\\tmp\\tcic_selection.xml
   		
		String content = null;
		try {
			 content = new String(Files.readAllBytes(Paths.get(wrk_dir +"\\tmp\\tcic_selection.xml")), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(content==null) return true;
		
		
		String item_id="";
		String object_type="";
		String item_revision_id="";
		String revrule_name="";
		
		Matcher matcher = Pattern.compile("item_id=\"(.+?)\"").matcher(content);
		if (matcher.find()) item_id = matcher.group(1);
		
		matcher = Pattern.compile("object_type=\"(.+?)\"").matcher(content);
		if (matcher.find()) object_type = matcher.group(1);
		
		matcher = Pattern.compile("item_revision_id=\"(.+?)\"").matcher(content);
		if (matcher.find()) item_revision_id = matcher.group(1);
		
		matcher = Pattern.compile("revrule name=\"(.+)\"").matcher(content);
		if (matcher.find()) revrule_name = matcher.group(1);
		

		String _select_line = item_id+ "/"+ object_type+ "/"+item_revision_id+ "/"+revrule_name;
		
      	//2. check appropriate info inside
		
		//2.1 read content
		ArrayList<String> lines_required = new ArrayList<>();
		
   		
		try {
		    BufferedReader bufReader = new BufferedReader(new FileReader(_f_catlist));

		    String line = bufReader.readLine();
		    while (line != null) {
		    	lines_required.add(line);
		      line = bufReader.readLine();
		    }

		    bufReader.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		
		String header_catlist = "";
		if(lines_required.size() >0) {
			header_catlist = lines_required.remove(0);
		}
		
		if(!header_catlist.equals(_select_line)) {
			System.out.println("selected_file="+_select_line );
			System.out.println("required="+header_catlist );
			return false;
		}
		
   		//2.2 file_idenfity - (item_id/item_type/item_rev_id/rev_rule)
		
		
      	//3. read %WORK_DIR%\\Export\\ExpSpreadSheet.txt
		ArrayList<String> lines_actual = new ArrayList<>();

		try {
		    BufferedReader bufReader = new BufferedReader(new FileReader(wrk_dir + "\\Export\\ExpSpreadSheet.txt"));

		    String line = bufReader.readLine();
		    while (line != null) {
		    	lines_actual.add(line);
		      line = bufReader.readLine();
		    }

		    bufReader.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		
		String org_head_line = null;
		if(lines_actual.size()>=0) org_head_line = lines_actual.remove(0);
		
      	//4. remove unlisted files not in vfex list
		LinkedHashSet<String> lines_final = new LinkedHashSet<String>();
		lines_final.add(org_head_line);
		
		for(String line: lines_actual) {
			
			if(line.contains(".CATDrawing")) continue; //ignore CATDRawing
			
			for(String required: lines_required) {
				if(line.contains(required)) {
					lines_final.add(line);
				}
			}
		}
		
      	//5. save back to ExpSpreadSheet.txt
		try {
			PrintWriter pw = new PrintWriter(wrk_dir + "\\Export\\ExpSpreadSheet.txt");
			pw.close(); //to clear old content
			
			//total string
			String total_str = "";
			for(String str: lines_final) {
				total_str += str + System.lineSeparator();
			}
			
			//write
			try 
			{
					RandomAccessFile writer = new RandomAccessFile(wrk_dir + "\\Export\\ExpSpreadSheet.txt", "rw");
			        FileChannel channel = writer.getChannel();
			        ByteBuffer buff = ByteBuffer.wrap(total_str.getBytes(StandardCharsets.UTF_8));
			 
			        channel.write(buff);
			        channel.force(true);
			        channel.close();
				     // verify
				     RandomAccessFile reader = new RandomAccessFile(wrk_dir + "\\Export\\ExpSpreadSheet.txt", "r");
				     reader.close();
				     
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

   	////////////////////////////////////////////////////////////////////////////
   	
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
