package com.teamcenter.vinfast.handlers;

public class ProcessReportObject {
	private String processName;
	private String processStatus;
	private String startDate;
	private String dueDate;
	private String completedDate;
	private String itemID;
	private String itemRevision;
	private String itemName;
	private String description;
	private String owningUser;
	private String owningGroup;
	private String moduleGroup;
	private String creationDate;
	private String pendingTask;
	private String pendingUser;
	private String wfTemplate;
	private String targets;
	private String reviewerComment;
	private String reviewer;
	private String taskStatus;
	private String resParty;
	private String subTaskName;
	
	
	/*using for EScooter-ECN-Workflow*/
	private String classification;
	private String priority;
	private String exchangeNewPart;
	private String exchangeOldPart;
	


	public ProcessReportObject() {
		super();
		this.processName 	= "";
		this.processStatus 	= "";
		this.startDate 		= "";
		this.dueDate 		= "";
		this.completedDate 	= "";
		this.itemID 		= "";
		this.itemRevision 	= "";
		this.itemName 		= "";
		this.description 	= "";
		this.owningUser 	= "";
		this.owningGroup 	= "";
		this.moduleGroup 	= "";
		this.creationDate 	= "";
		this.pendingTask	= "";
		this.pendingUser	= "";
		this.wfTemplate		= "";
		this.classification = "";
		this.priority		= "";
		this.exchangeNewPart = "";
		this.exchangeOldPart = "";
		this.reviewerComment = "";
		this.taskStatus		= "";
		this.resParty		= "";
		this.reviewer		= "";
	}
	
	public String getReviewer() {
		return reviewer;
	}

	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}

	public String getSubTaskName() {
		return subTaskName;
	}

	public void setSubTaskName(String subTaskName) {
		this.subTaskName = subTaskName;
	}

	public String getResParty() {
		return resParty;
	}

	public void setResParty(String resParty) {
		this.resParty = resParty;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public String getReviewerComment() {
		return reviewerComment;
	}

	public void setReviewerComment(String reviewerComment) {
		this.reviewerComment = reviewerComment;
	}


	public String getPendingTask() {
		return pendingTask;
	}

	public void setPendingTask(String pendingTask) {
		this.pendingTask = pendingTask;
	}

	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public String getProcessStatus() {
		return processStatus;
	}
	public void setProcessStatus(String processStatus) {
		this.processStatus = processStatus;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getDueDate() {
		return dueDate;
	}
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}
	public String getCompletedDate() {
		return completedDate;
	}
	public void setCompletedDate(String completedDate) {
		this.completedDate = completedDate;
	}
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	public String getItemRevision() {
		return itemRevision;
	}
	public void setItemRevision(String itemRevision) {
		this.itemRevision = itemRevision;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getOwningUser() {
		return owningUser;
	}
	public void setOwningUser(String owningUser) {
		this.owningUser = owningUser;
	}
	public String getOwningGroup() {
		return owningGroup;
	}
	public void setOwningGroup(String owningGroup) {
		this.owningGroup = owningGroup;
	}
	public String getModuleGroup() {
		return moduleGroup;
	}
	public void setModuleGroup(String moduleGroup) {
		this.moduleGroup = moduleGroup;
	}
	public String getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getExchangeNewPart() {
		return exchangeNewPart;
	}

	public void setExchangeNewPart(String exchangeNewPart) {
		this.exchangeNewPart = exchangeNewPart;
	}

	public String getExchangeOldPart() {
		return exchangeOldPart;
	}

	public void setExchangeOldPart(String exchangeOldPart) {
		this.exchangeOldPart = exchangeOldPart;
	}

	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		this.targets = targets;
	}

	public String getPendingUser() {
		return pendingUser;
	}

	public void setPendingUser(String pendingUser) {
		this.pendingUser = pendingUser;
	}

	public String getWfTemplate() {
		return wfTemplate;
	}

	public void setWfTemplate(String wfTemplate) {
		this.wfTemplate = wfTemplate;
	}
	
}
