package com.vf.utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Set;

import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2010_09.DataManagement;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.services.rac.workflow._2014_06.Workflow;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffInfo;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffs;
import com.teamcenter.vinfast.admin.ras.ReAssignTask_Constant;
import com.teamcenter.vinfast.utils.Utils;

public class TriggerProcess {
	private TCSession session = null;
	private final WorkflowService wfService;

	private String workProcessTemplace = "";
	private String workProcessName = "";
	private String workProcessDesc = "";
	private String[] workProcessAttachment = null;
	private int[] workProcessAttachmentType = null;
	private LinkedHashMap<String, Set<TCComponentGroupMember>> assignmentList;
	private String alName = "";

	public TriggerProcess(TCSession _session) {
		session = _session;
		wfService = WorkflowService.getService(session);
	}

	public String run() {
		try {
			ServiceData output = triggerWorkProcess();
			if (output.sizeOfPartialErrors() > 0) {
				return Utils.HanlderServiceData(output);
			} else {
				if (assignmentList != null)
					assignPerformer((TCComponentProcess) output.getCreatedObject(0));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
		return "";
	}

	public TCComponent trigger() {
		try {
			ServiceData output = triggerWorkProcess();
			if (output.sizeOfPartialErrors() == 0) {
				if (assignmentList != null)
					assignPerformer((TCComponentProcess) output.getCreatedObject(0));
				return output.getCreatedObject(0);
			} else {
				String abcString = Utils.HanlderServiceData(output);
				System.out.println(abcString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private ServiceData triggerWorkProcess() throws TCException {
		if (workProcessAttachmentType == null) {
			workProcessAttachmentType = new int[workProcessAttachment.length];
			Arrays.fill(workProcessAttachmentType, 1);
		}

		final com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData data = new com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData();
		data.attachmentCount = workProcessAttachment.length;
		data.attachments = workProcessAttachment;
		data.processTemplate = workProcessTemplace;
		data.attachmentTypes = workProcessAttachmentType;
		if (!alName.isEmpty())
			data.processAssignmentList = alName;

		InstanceInfo intInfo = wfService.createInstance(true, null, workProcessName, null, workProcessDesc, data);
		return intInfo.serviceData;
	}

	private boolean assignPerformer(TCComponentProcess process) throws TCException, ServiceException {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		TCComponentTask rootTask = ((TCComponentProcess) process).getRootTask();
		TCComponentTask[] subtasks = rootTask.getSubtasks();
		for (TCComponentTask subTask : subtasks) {
			String taskType = subTask.getTaskType();
			String taskName = subTask.getProperty("object_name");

			if (taskType.compareTo(ReAssignTask_Constant.TASKTYPE_DO) == 0) {
				if (assignmentList.containsKey(taskName)) {
					if (assignmentList.get(taskName).size() > 0) {
						for (TCComponentGroupMember groupMember : assignmentList.get(taskName)) {
							try {
								subTask.setResponsibleParty(groupMember.getUser());
							} catch (Exception e) {
								e.printStackTrace();
							}

							break;
						}
					}
				}
			} else if (taskType.compareToIgnoreCase(ReAssignTask_Constant.TASKTYPE_REVIEW) == 0 || taskType.compareToIgnoreCase(ReAssignTask_Constant.TASKTYPE_ACKNOWLEDGE) == 0) {
				if (assignmentList.containsKey(taskName)) {
					Set<TCComponentGroupMember> groupMembers = assignmentList.get(taskName);
					if (groupMembers.size() > 0) {
						TCComponentTask selectSignOffTask = subTask.getSubtasks()[0];
						if (selectSignOffTask.getTaskType().compareToIgnoreCase("EPMSelectSignoffTask") != 0) {
							selectSignOffTask = subTask.getSubtasks()[1];
						}
						CreateSignoffs createSignoff = new CreateSignoffs();
						createSignoff.task = selectSignOffTask;
						createSignoff.signoffInfo = new CreateSignoffInfo[groupMembers.size()];
						int j = 0;
						for (TCComponentGroupMember tcGroupMember : groupMembers) {
							createSignoff.signoffInfo[j] = new CreateSignoffInfo();
							createSignoff.signoffInfo[j].originType = "SOA_EPM_ORIGIN_UNDEFINED";
							createSignoff.signoffInfo[j].signoffAction = "SOA_EPM_Review";
							createSignoff.signoffInfo[j].signoffMember = tcGroupMember;
							j++;
						}

						ServiceData addSignoffsResponse = WorkflowService.getService(session).addSignoffs(new CreateSignoffs[] { createSignoff });
						if (addSignoffsResponse.sizeOfPartialErrors() > 0) {
							return false;
						}

						// Set adhoc done
						DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
						propInfo.object = selectSignOffTask;
						propInfo.timestamp = Calendar.getInstance();
						propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
						propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
						propInfo.vecNameVal[0].name = "task_result";
						propInfo.vecNameVal[0].values = new String[] { "Completed" };
						DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[] { propInfo }, new String[0]);
						if (setPropertyResponse.data.sizeOfPartialErrors() > 0) {
							System.out.println("[assignPerformer]: " + Utils.HanlderServiceData(setPropertyResponse.data));
							return false;
						}

						// Complete selection signoff task if it started
						if (selectSignOffTask.getTaskState().compareToIgnoreCase("Started") == 0) {
							try {
								Thread.sleep(1000);
								process.refresh();
								subTask.refresh();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							Workflow.PerformActionInputInfo paii = new Workflow.PerformActionInputInfo();
							paii.clientId = "complete" + selectSignOffTask.getUid();
							paii.action = "SOA_EPM_complete_action";
							paii.actionableObject = selectSignOffTask;
							paii.propertyNameValues.put("comments", new String[] { "Auto Completed" });
							paii.supportingValue = "SOA_EPM_completed";

							ServiceData sd = WorkflowService.getService(session).performAction3(new Workflow.PerformActionInputInfo[] { paii });
							if (sd.sizeOfPartialErrors() > 0) {
								return false;
							}
						}
					}
				}
			}
		}

		return true;
	}

	public String getWorkProcessName() {
		return workProcessName;
	}

	public void setWorkProcessName(String workProcessName) {
		this.workProcessName = workProcessName;
	}

	public String getWorkProcessDesc() {
		return workProcessDesc;
	}

	public void setWorkProcessDesc(String workProcessDesc) {
		this.workProcessDesc = workProcessDesc;
	}

	public String[] getWorkProcessAttachment() {
		return workProcessAttachment;
	}

	public void setWorkProcessAttachment(String[] workProcessAttachment) {
		this.workProcessAttachment = workProcessAttachment;
	}

	public LinkedHashMap<String, Set<TCComponentGroupMember>> getAssignmentList() {
		return assignmentList;
	}

	public void setAssignmentList(LinkedHashMap<String, Set<TCComponentGroupMember>> assignmentList) {
		this.assignmentList = assignmentList;
	}

	public String getWorkProcessTemplace() {
		return workProcessTemplace;
	}

	public void setWorkProcessTemplace(String workProcessTemplace) {
		this.workProcessTemplace = workProcessTemplace;
	}

	public String getAlName() {
		return alName;
	}

	public void setAlName(String alName) {
		this.alName = alName;
	}

	public int[] getWorkProcessAttachmentType() {
		return workProcessAttachmentType;
	}

	public void setWorkProcessAttachmentType(int[] workProcessAttachmentType) {
		this.workProcessAttachmentType = workProcessAttachmentType;
	}
}
