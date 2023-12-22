package com.vinfast.integration.model;

import com.teamcenter.integration.arch.ModelAbstract;

public abstract class ReportMessage extends ModelAbstract{
	
	ReportMessage(){
		super(ModelType.REPORT_MESSAGE);
	}
	
	
	
	public enum UpdateType{
		UPDATE_HEADER,
		UPDATE_BODY_INFO,
		UPDATE_BODY_ERROR,
	}
	protected String[] data = null;
	protected String[] header = null;
	
	public String[] getHeader() {
		return header;
	}

	protected UpdateType type;
	public void setType(UpdateType type) {
		this.type = type;
	}


	public UpdateType getType() {
		return type;
	}
	
	public String[] getData() {
		parseReportToArray();
		return data;
	} 
	
	protected abstract void parseReportToArray();
}
