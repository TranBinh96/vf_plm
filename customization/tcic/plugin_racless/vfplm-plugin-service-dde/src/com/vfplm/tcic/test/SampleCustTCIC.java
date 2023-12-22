package com.vfplm.tcic.test;

import javax.swing.JOptionPane;

import com.ebsolutions.soacore.process.ProcessConfiguration;
import com.ebsolutions.soacore.process.RACLessProcessAbstract;
import com.ebsolutions.soacore.process.messages.MessageMgtAbstract;
import com.ebsolutions.soacore.sessionmanager.ConnectionInfo;
import com.ebsolutions.soacore.sessionmanager.SOASession;
import com.ebsolutions.utility.config.LocalConfiguration;
import com.ebsolutions.utility.config.PreferencesManager;
//import com.teamcenter.soa.client.Connection;

public class SampleCustTCIC extends RACLessProcessAbstract {

	MessageMgtAbstract msg_manager = null;
	@Override
	protected int coreProcess() {
		int state0 = lockServer();
		if(state0 !=0) {
			msg_manager.showMessage("3. another command is running! cancel action", "Cancel action!", 1);
			return 1;
		}
		
	    msg_manager.showMessage("coreProcess message","2. Core Process finished", 2);
//	   JOptionPane.showMessageDialog(null, "coreProcess message");
	    unlockServer();
	    
	   return 0;
	}

	@Override
	protected int initProcessConfiguration() {
		//0
//	      _process_configuration = ProcessConfiguration.getDefaultSilentProcessConfiguration();
	      _process_configuration = ProcessConfiguration.getDefaultProcessConfiguration();
	      _process_configuration.initLogFile("my_process.log", false);

	      msg_manager = _process_configuration.get_message_manager();
	      
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
