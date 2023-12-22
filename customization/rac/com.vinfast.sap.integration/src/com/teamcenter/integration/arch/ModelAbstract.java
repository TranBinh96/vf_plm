package com.teamcenter.integration.arch;

public class ModelAbstract {
	public enum ModelType{
		UNKNOWN,
		ACTION_COMMAND,
		BOMBOP_DATA,
		MCN_INFORMATION,
		REPORT_MESSAGE,
		NOTICE_MESSAGE,
		PROCESS_STATUS,
		SERVER_INFORMATION,
		DATA_SEND,
	}
	
	public enum PREPARE_STATUS {
		VALIDATE, 
		ALREADY_TRANSFER, 
		NOT_VALIDATE, 
		NOT_YET_RELEASE
	}
	private String message = "";
	private String transferResult = "";
	private boolean alreadyTransfer = false;
	private ModelType modelType;
	private PREPARE_STATUS prepareStatus = PREPARE_STATUS.VALIDATE;
	
	public ModelAbstract() {
		
	}
	
	public ModelAbstract(ModelType type) {
		this.modelType = type;
	}

	public ModelType getModelType() {
		return modelType;
	}

	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}

	public PREPARE_STATUS getPrepareStatus() {
		return prepareStatus;
	}
	
	public void setPrepareStatus(PREPARE_STATUS prepareStatus) {
		this.prepareStatus = prepareStatus;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTransferResult() {
		return transferResult;
	}

	public void setTransferResult(String transferResult) {
		this.transferResult = transferResult;
	}

	public boolean isAlreadyTransfer() {
		return alreadyTransfer;
	}
}
