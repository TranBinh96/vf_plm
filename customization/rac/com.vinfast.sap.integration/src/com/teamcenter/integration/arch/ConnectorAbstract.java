package com.teamcenter.integration.arch;

import java.util.ArrayList;

import com.teamcenter.integration.model.MCNInformation;
import com.vinfast.integration.model.ActionCommand;
import com.vinfast.integration.model.ServerInformation;

public abstract class ConnectorAbstract extends ProcessorAbstract{
	
	public ConnectorAbstract(String processName) {
		super(processName, 1000);
	}
	public abstract void init();
	public abstract boolean send();
	private ServerInformation serverInfo = null;
	private ArrayList<ModelAbstract> data2Send = new ArrayList<ModelAbstract>();
	boolean isConnected = false;
	private MCNInformation mcn = null;
	
	
	
	public MCNInformation getMcn() {
		return mcn;
	}
	public ServerInformation getServerInfo() {
		return serverInfo;
	}
	public ArrayList<ModelAbstract> getData2Send() {
		return data2Send;
	}

	
	public void onCommand(ActionCommand cmd) {
		switch(cmd.getCommand()) {
			case COMMAND_TRANSFER:
				send();
				break;
			default:
				break;
		}
	}
	
	@Override
	public void onMessage(ModelAbstract msg) {
		switch(msg.getModelType()) {
			case SERVER_INFORMATION:
				serverInfo = (ServerInformation)msg;
				break;
			case ACTION_COMMAND:
				onCommand((ActionCommand)msg);
				break;
			case DATA_SEND:
				data2Send.add(msg);
				break;
			case MCN_INFORMATION:
				onMCNInformation((MCNInformation)msg);
				break;
			default:
				break;
		}
	}
	
	private void onMCNInformation(MCNInformation info) {
		mcn = info;
	}

}

