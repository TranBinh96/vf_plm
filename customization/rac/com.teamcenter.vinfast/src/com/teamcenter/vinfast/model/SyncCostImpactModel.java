package com.teamcenter.vinfast.model;

import java.util.Date;

public class SyncCostImpactModel {
	private Double packing;
	private Double ednd;
	private Double tooling;
	private Double piece;
	private Double logistic;
	private String partNumber;
	private String revisionNumber;
	private String ecrNumber;
	private Date ecrRelDate;

	private String taskStatus = "";
	private String validateStatus = "";
	
	private long id;
	private int failedCounter;
	private String errorLog;
	
	public SyncCostImpactModel() {
		
	}

	public SyncCostImpactModel(Double ednd, Double tooling, Double piece, Double logistic, Double packing) {
		super();
		this.packing = packing;
		this.ednd = ednd;
		this.tooling = tooling;
		this.piece = piece;
		this.logistic = logistic;
	}

	public Double getPacking() {
		return packing;
	}

	public void setPacking(Double packing) {
		this.packing = packing;
	}

	public Double getEdnd() {
		return ednd;
	}

	public void setEdnd(Double ednd) {
		this.ednd = ednd;
	}

	public Double getTooling() {
		return tooling;
	}

	public void setTooling(Double tooling) {
		this.tooling = tooling;
	}

	public Double getPiece() {
		return piece;
	}

	public void setPiece(Double piece) {
		this.piece = piece;
	}

	public Double getLogistic() {
		return logistic;
	}

	public void setLogistic(Double logistic) {
		this.logistic = logistic;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}

	public String getRevisionNumber() {
		return revisionNumber;
	}

	public void setRevisionNumber(String revisionNumber) {
		this.revisionNumber = revisionNumber;
	}

	public String getEcrNumber() {
		return ecrNumber;
	}

	public void setEcrNumber(String ecrNumber) {
		this.ecrNumber = ecrNumber;
	}

	public Date getEcrRelDate() {
		return ecrRelDate;
	}

	public void setEcrRelDate(Date ecrRelDate) {
		this.ecrRelDate = ecrRelDate;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public String getValidateStatus() {
		return validateStatus;
	}

	public void setValidateStatus(String validateStatus) {
		this.validateStatus = validateStatus;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getFailedCounter() {
		return failedCounter;
	}

	public void setFailedCounter(int failedCounter) {
		this.failedCounter = failedCounter;
	}

	public String getErrorLog() {
		return errorLog;
	}

	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}
	
	public boolean hasCost() {
		return logistic != null || tooling != null || packing!= null || piece!= null || ednd!= null;
	}
}
