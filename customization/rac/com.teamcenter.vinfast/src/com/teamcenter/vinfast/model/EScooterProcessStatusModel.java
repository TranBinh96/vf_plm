package com.teamcenter.vinfast.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.vf.utils.DateTimeExtension;

public class EScooterProcessStatusModel {
	private String itemID;
	private String itemRevision;
	private String itemName;
	private String description;
	private String moduleGroup;
	private String impactModule;
	private String classification;
	private String priority;
	private String exchangeNewPart;
	private String exchangeOldPart;
	private String disposalCode;
	private String changeReason;
	private String problems;
	private String solutions;
	private String wfTemplate;
	private String processDesc;
	private String bigCostImpact;
	private String pendingTask;
	private String pendingUser;
	private String targets;
	private String taskStatus;
	private String creationDate;
	private String startDate;
	private String completedDate;
	private String owningUser;
	private String owningGroup;

	private String processName;
	private String processStatus;
	private String dueDate;
	private String reviewerComment;
	private String reviewer;
	private String resParty;
	private String subTaskName;
	private String documentType;

	private DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");

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

	public String getModuleGroup() {
		return moduleGroup;
	}

	public void setModuleGroup(String moduleGroup) {
		this.moduleGroup = moduleGroup;
	}

	public String getImpactModule() {
		return impactModule;
	}

	public void setImpactModule(String impactModule) {
		this.impactModule = impactModule;
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

	public String getDisposalCode() {
		return disposalCode;
	}

	public void setDisposalCode(String disposalCode) {
		this.disposalCode = disposalCode;
	}

	public String getChangeReason() {
		return changeReason;
	}

	public void setChangeReason(String changeReason) {
		this.changeReason = changeReason;
	}

	public String getProblems() {
		return problems;
	}

	public void setProblems(String problems) {
		this.problems = problems;
	}

	public String getSolutions() {
		return solutions;
	}

	public void setSolutions(String solutions) {
		this.solutions = solutions;
	}

	public String getWfTemplate() {
		return wfTemplate;
	}

	public void setWfTemplate(String wfTemplate) {
		this.wfTemplate = wfTemplate;
	}

	public String getPendingTask() {
		return pendingTask;
	}

	public void setPendingTask(String pendingTask) {
		this.pendingTask = pendingTask;
	}

	public String getPendingUser() {
		return pendingUser;
	}

	public void setPendingUser(String pendingUser) {
		this.pendingUser = pendingUser;
	}

	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		this.targets = targets;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getCompletedDate() {
		return completedDate;
	}

	public void setCompletedDate(String completedDate) {
		this.completedDate = completedDate;
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

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public String getReviewerComment() {
		return reviewerComment;
	}

	public void setReviewerComment(String reviewerComment) {
		this.reviewerComment = reviewerComment;
	}

	public String getReviewer() {
		return reviewer;
	}

	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}

	public String getResParty() {
		return resParty;
	}

	public void setResParty(String resParty) {
		this.resParty = resParty;
	}

	public String getSubTaskName() {
		return subTaskName;
	}

	public void setSubTaskName(String subTaskName) {
		this.subTaskName = subTaskName;
	}

	public String getProcessDesc() {
		return processDesc;
	}

	public void setProcessDesc(String processDesc) {
		this.processDesc = processDesc;
	}

	public String getBigCostImpact() {
		return bigCostImpact;
	}

	public void setBigCostImpact(String bigCostImpact) {
		this.bigCostImpact = bigCostImpact;
	}

	public String getCurrentDate() {
		Date today = Calendar.getInstance().getTime();
		return df.format(today);
	}

	public String getOverDueDate() {
		if (!dueDate.isEmpty()) {
			try {
				Calendar currentCal = Calendar.getInstance();
				Calendar dueCal = Calendar.getInstance();
				dueCal.setTime(df.parse(dueDate));
				int diff = (int) DateTimeExtension.getDayBetweenDates(currentCal, dueCal);
				return String.valueOf(diff);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return "";
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}
}
