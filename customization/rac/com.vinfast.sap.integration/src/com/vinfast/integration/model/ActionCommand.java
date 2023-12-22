package com.vinfast.integration.model;

import com.teamcenter.integration.arch.ModelAbstract;

public class ActionCommand extends ModelAbstract{
	public enum Command{
		COMMAND_PREPARE,
		COMMAND_TRANSFER,
		COMMAND_SEND_TO_SAP,
		COMMAND_SEND_TO_MES,
		COMMAND_INIT_REPORT,
		COMMAND_POPUP_REPORT,
		COMMAND_LOAD_MCN_INFO,
		ALLOW_TO_TRANSFER,
	}
	public ActionCommand(Command cmd) {
		super(ModelType.ACTION_COMMAND);
		this.command = cmd;
	}
	
	private Command command;

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}
	
}
