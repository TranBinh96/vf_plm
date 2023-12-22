package com.vinfast.integration.model;

import com.teamcenter.integration.arch.ModelAbstract;

public class ProcessStatus extends ModelAbstract{
	
	public enum ProcessUpdateType{
		INIT_TOTAL,
		UPDATE_PROCESS_BAR,
		UPDATE_PROCESS_LABEL_STATUS,
		UPDATE_RUNNING_INDICATOR,
		UPDATE_PROCESS_BAR_PERCENT
	}
	
	public ProcessStatus(ProcessUpdateType type) {
		super(ModelType.PROCESS_STATUS);
		this.updateType = type;
		if(this.updateType == ProcessUpdateType.UPDATE_RUNNING_INDICATOR) {
			updateIndicator();
		}
	}
	
	public ProcessStatus(int percent) {
		super(ModelType.PROCESS_STATUS);
		this.updateType = ProcessUpdateType.UPDATE_PROCESS_BAR_PERCENT;
		this.percent = percent;
	}
	
	public ProcessStatus(String statusText) {
		super(ModelType.PROCESS_STATUS);
		this.updateType = ProcessUpdateType.UPDATE_PROCESS_LABEL_STATUS;
		this.processLabelStatus = statusText;
	}
	
	private int total;
	private ProcessUpdateType updateType;
	private String processLabelStatus;
	private int percent = 0;


	static private int counterIndicator = 0;
	private StringBuffer indicator = new StringBuffer();
	public int getPercent() {
		return percent;
	}
	public String getIndicator() {
		return indicator.toString();
	}

	public String getProcessLabelStatus() {
		return processLabelStatus;
	}
	public void setProcessLabelStatus(String processLabelStatus) {
		this.processLabelStatus = processLabelStatus;
	}
	public ProcessUpdateType getUpdateType() {
		return updateType;
	}
	public void setUpdateType(ProcessUpdateType updateType) {
		this.updateType = updateType;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	
	private void updateIndicator() {
		counterIndicator++;
		if(counterIndicator >= 8) {
			counterIndicator = 0;
		}
		for(int i = 0; i < counterIndicator; i++) {
			indicator.append(".");
		}
	}
	
	
}
