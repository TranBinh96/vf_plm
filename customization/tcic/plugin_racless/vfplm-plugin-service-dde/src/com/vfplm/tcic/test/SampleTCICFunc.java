package com.vfplm.tcic.test;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import com.ebsolutions.journaling.JNLManager;
import com.ebsolutions.soacore.process.ProcessConfiguration;
import com.ebsolutions.soacore.process.ProcessConfiguration.EnumProcessFamilyType;
import com.ebsolutions.soacore.process.ProcessConfiguration.EnumProcessType;
import com.ebsolutions.soacore.process.RACLessProcessAbstract;
import com.ebsolutions.soacore.process.UserExportCommand;
import com.ebsolutions.soacore.process.messages.MessageMgtAbstract;
import com.ebsolutions.soacore.sessionmanager.ConnectionInfo;
import com.ebsolutions.soacore.sessionmanager.SOASession;
import com.ebsolutions.utility.config.LocalConfiguration;
import com.ebsolutions.utility.config.PreferencesManager;
import com.ebsolutions.utility.process.SelectedElement;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;

public class SampleTCICFunc extends RACLessProcessAbstract {

	/**
	 * 
121146/01
Any Status; Working
VF4_Study_Part
QA: http://10.128.49.113:80/tc
	 */
	
	MessageMgtAbstract msg_manager = null;
    SOASession tcic_session ;
    Connection conn;
    
    
    final class MyProcessConfiguration extends ProcessConfiguration {

		@Override
		public void clearSelectedElements() {
			_tcic_print("clearSelectedElements");
			// TODO Auto-generated method stub
			super.clearSelectedElements();
		}


		@Override
		public LinkedList<SelectedElement> getSelectedElements() {
			_tcic_print("getSelectedElements");

			// TODO Auto-generated method stub
			return super.getSelectedElements();
		}


		@Override
		public boolean get_is_first_command() {
			_tcic_print("get_is_first_command");

			// TODO Auto-generated method stub
			return super.get_is_first_command();
		}


		@Override
		public boolean get_is_last_command() {
			_tcic_print("get_is_last_command");

			// TODO Auto-generated method stub
			return true;
		}


		@Override
		public boolean get_is_staging_dir_locked() {
			_tcic_print("get_is_staging_dir_locked");

			// TODO Auto-generated method stub
			return false;
		}


		@Override
		public JNLManager get_journal() {
			_tcic_print("get_journal");

			// TODO Auto-generated method stub
			return super.get_journal();
		}


		@Override
		public LocalConfiguration get_local_config() {
			_tcic_print("get_local_config");

			// TODO Auto-generated method stub
			return super.get_local_config();
		}


		@Override
		public MessageMgtAbstract get_message_manager() {
			_tcic_print("get_message_manager");

			// TODO Auto-generated method stub
			return msg_manager;
		}


		@Override
		public PreferencesManager get_preferences_config() {
			_tcic_print("get_preferences_config");

			// TODO Auto-generated method stub
			return super.get_preferences_config();
		}


		@Override
		public String get_process_classname() {
			_tcic_print("get_process_classname");

			// TODO Auto-generated method stub
			return "my_proc_config";
		}


		@Override
		public EnumProcessFamilyType get_process_family_type() {
			_tcic_print("get_process_classname");

			// TODO Auto-generated method stub
			return EnumProcessFamilyType._CteLOAD_PROCESS;
		}


		@Override
		public EnumProcessType get_process_type() {
			_tcic_print("get_process_type");

			// TODO Auto-generated method stub
			return EnumProcessType._CteSILENT_EXPORT_PROCESS;
		}
		
		@Override
		public boolean get_release_server() {
			_tcic_print("get_release_server");
			// TODO Auto-generated method stub
			return super.get_release_server();
		}


		@Override
		public String[] get_staging_dirs() {
			_tcic_print("get_staging_dirs");

			// TODO Auto-generated method stub
			return super.get_staging_dirs();
		}


		@Override
		public void setSelectedElements(LinkedList<SelectedElement> arg0) {
			// TODO Auto-generated method stub
			super.setSelectedElements(arg0);
		}


		@Override
		public void setSelectedElements(ModelObject[] arg0) {
			// TODO Auto-generated method stub
			super.setSelectedElements(arg0);
		}


		@Override
		public void set_is_first_command(boolean i_is_first_command) {
			// TODO Auto-generated method stub
			super.set_is_first_command(i_is_first_command);
		}


		@Override
		public void set_is_last_command(boolean i_is_last_command) {
			// TODO Auto-generated method stub
			super.set_is_last_command(i_is_last_command);
		}


		@Override
		public void set_is_staging_dir_locked(boolean i_is_staging_dir_locked) {
			_tcic_print("set_is_staging_dir_locked");

			// TODO Auto-generated method stub
			super.set_is_staging_dir_locked(i_is_staging_dir_locked);
		}


		@Override
		public void set_release_server(boolean i_release_server) {
			_tcic_print("set_release_server");

			// TODO Auto-generated method stub
			super.set_release_server(i_release_server);
		}


		@Override
		public void set_staging_dirs(String[] i_staging_dirs) {
			_tcic_print("set_staging_dirs");

			// TODO Auto-generated method stub
			super.set_staging_dirs(i_staging_dirs);
		}


		public MyProcessConfiguration(
				boolean i_is_silent,
				MessageMgtAbstract i_error_mgt,
				EnumProcessFamilyType i_process_family_type,
				EnumProcessType i_process_type,
				String i_current_process_classname
				) {
			super(i_is_silent, i_error_mgt, i_process_family_type, i_process_type,
					i_current_process_classname);
			
			
		}


		@Override
		public boolean get_is_silent() {
			_tcic_print("get_is_silent");

			// TODO Auto-generated method stub
			return super.get_is_silent();
		}
		@Override
		public void set_is_silent(boolean _is_silent) {
			_tcic_print("set_is_silent");

			// TODO Auto-generated method stub
			super.set_is_silent(_is_silent);
		}
		
		boolean _is_server_inited = false;
		@Override
		public void set_init_server(boolean i_init_server) {
			_tcic_print("set_init_server");

			// TODO Auto-generated method stub
//			super.set_init_server(i_init_server);
			
			msg_manager.showMessage("I will login!!", "2. Core Process", 2);

			
				tcic_session = com.ebsolutions.soacore.sessionmanager.SOASession.get_instance();

				ConnectionInfo 	i_connection_info = new ConnectionInfo();

		    	i_connection_info._host = "http://10.128.49.113:80/tc";
		    	i_connection_info._user_name = "plmadmin1";
		    	i_connection_info._password = "fix4fun!";
		    	i_connection_info._group = "dba";
		    	i_connection_info._role = "DBA";
		    	i_connection_info._protocol = "HTTP";
		    	
		    	int t = tcic_session.showLoginDlg(i_connection_info);
			    
		    	_is_server_inited= true;
		}
    	
		@Override
		public boolean get_init_server() {
			_tcic_print("get_init_server");

			return _is_server_inited;
		}
    }
    
	public void _tcic_print(String x) {
		
		msg_manager.showMessage(x, "2. Core Process", 2);

	}
	
	
	public void do_Login_TC(){
		
		msg_manager.showMessage("I will login!!", "2. Core Process", 2);

		
			tcic_session = com.ebsolutions.soacore.sessionmanager.SOASession.get_instance();

			ConnectionInfo 	i_connection_info = new ConnectionInfo();

			
	    	i_connection_info._host = "http://10.128.49.113:80/tc";
	    	i_connection_info._user_name = "plmadmin1";
	    	i_connection_info._password = "fix4fun!";
	    	i_connection_info._group = "dba";
	    	i_connection_info._role = "DBA";
	    	i_connection_info._protocol = "HTTP";
	    	
	    	int t ;
//	    	t= tcic_session.showLoginDlg(i_connection_info);
	    	t = tcic_session.login(i_connection_info._host );
//	    			,
//	    			i_connection_info._user_name,
//	    			i_connection_info._password,
//	    			i_connection_info._group,
//	    			i_connection_info._role
//	    			
//	    			);
	    	
		   
	}
	
	@Override
	protected int coreProcess() {
//		int state0 = lockServer();
//		if(state0 !=0) {
//			msg_manager.showMessage("3. another command is running! cancel action", "Cancel action!", 1);
//			return 1;
//		}
		SOASession my_tcic_session = com.ebsolutions.soacore.sessionmanager.SOASession.get_instance();
 
        /*initialize API and define silent mode for the process*/
		UserExportCommand _export_api = new UserExportCommand(true);
		
	    msg_manager.showMessage(my_tcic_session.isConnected()? "connected1" : "NO connected1", "2. Core Process", 2);
	    
        /* retrieve the default configuration */
	    ProcessConfiguration configuration = null;
	    
	    //TODO: login created here!!!
//	    configuration = _export_api.getDefaultSampleConfiguration();
	    
	    this.do_Login_TC();
	    
	    String _config_str= "CONFIG_STRING="
				    +(",i_is_silent="+configuration.get_is_silent())
				    +(",EnumProcessType="+configuration.get_process_type().name())
				    +(",EnumProcessFamilyType="+configuration.get_process_family_type().name())
	    			+(",process_classname="+configuration.get_process_classname());
	    msg_manager.showMessage(_config_str, "2. Core Process", 2);
	    
//	    set_process_configuration(configuration);
	    

	    
	    MyProcessConfiguration my_proc_config = new MyProcessConfiguration(
	    		true,
	    		msg_manager,
	    		EnumProcessFamilyType._CteLOAD_PROCESS,
	    		EnumProcessType._CteSILENT_EXPORT_PROCESS,
	    		"my_proc_config"
	    		);
	    
//	    _export_api.set_process_configuration(my_proc_config);
//	    	    
//	    set_process_configuration(my_proc_config);
        
		msg_manager.showMessage(my_tcic_session.isConnected()? "connected2" : "NO connected2", "2. Core Process", 2);
		    
  
		    conn = my_tcic_session.getConnection();
		    String x = my_tcic_session.getCurrentUserName();
		    msg_manager.showMessage("coreProcess message","Logged in as" + x, 2);

		
		    
	    msg_manager.showMessage("coreProcess message","2. Core Process finished", 2);
//	   JOptionPane.showMessageDialog(null, "coreProcess message");
//	    unlockServer();
	    
	   return 0;
	}

	@Override
	protected int initProcessConfiguration() {
		//0
//	      _process_configuration = ProcessConfiguration.getDefaultSilentProcessConfiguration();
	      _process_configuration = ProcessConfiguration.getDefaultProcessConfiguration();
	      _process_configuration.initLogFile("my_process.log", false);

	      msg_manager = _process_configuration.get_message_manager();
	      PreferencesManager pref_man = _process_configuration.get_preferences_config();
	      LocalConfiguration local_conf = _process_configuration.get_local_config();

	      
//		    ConnectionInfo inf=  tcic_session.get_connection_info();
//	        tcic_session.showLoginDlg(inf);
	      
	      return 0;
	}

	@Override
	protected int postProcessTasks() {
		
	    msg_manager.showMessage("post process message","3. post Process", 2);
		
	      return 0;
	}

	@Override
	protected int preProcessTasks() {

//		   JOptionPane.showMessageDialog(null, "coreProcess message");
//	      msg_manager.showMessage("1. pre message","this is my title", 0);
	      return 0;
	}

}
