package com.teamcenter.vinfast.model;

import java.util.HashSet;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;

public class ReplaceGroupMemberModel {
	private TCComponentAssignmentList assignmentList = null;
	private TCComponentGroupMember groupMember = null;
	private TCComponentTaskTemplate epmTask = null;
	private TCComponent[] resources = null;
	private TCComponent[] tasks = null;

	public TCComponentAssignmentList getAssignmentList() {
		return assignmentList;
	}

	public void setAssignmentList(TCComponentAssignmentList assignmentList) {
		this.assignmentList = assignmentList;
		try {
			this.resources = assignmentList.getRelatedComponents("resources");
			this.tasks = assignmentList.getRelatedComponents("task_templates");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TCComponentGroupMember getGroupMember() {
		return groupMember;
	}

	public void setGroupMember(TCComponentGroupMember groupMember) {
		this.groupMember = groupMember;
	}

	public TCComponentTaskTemplate getEpmTask() {
		return epmTask;
	}

	public void setEpmTask(TCComponentTaskTemplate epmTask) {
		this.epmTask = epmTask;
	}

	public String getProcessTemplace() {
		try {
			return assignmentList.getProcessTemplate().toString();
		} catch (Exception e) {

		}
		return "";
	}

	public String getAssignmentListName() {
		return assignmentList.getName();
	}

	public String getAssignmentListDesc() {
		return assignmentList.getDescription();
	}

	public String getUser() {
		if (groupMember == null)
			return "";
		return groupMember.toString();
	}

	public String getUserByTask(String taskName) {
		if (taskName.isEmpty())
			return "";
		Set<String> outputList = new HashSet<String>();
		if (resources != null) {
			try {
				for (int i = 0; i < tasks.length; i++) {
					if (((TCComponentTaskTemplate) tasks[i]).getName().compareToIgnoreCase(taskName) == 0) {
						resources[i].refresh();
						TCComponent[] users = resources[i].getRelatedComponents("resources");
						for (TCComponent user : users) {
							if (user instanceof TCComponentGroupMember) {
								TCComponentGroupMember userMember = (TCComponentGroupMember) user;
								outputList.add(userMember.getGroup() + "/" + userMember.getRole() + "/" + userMember.getUserId());
//								outputList.add(user.getPropertyDisplayableValue("object_name"));
							}
						}
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return outputList == null ? "" : String.join(",", outputList);
	}

	public String getEpmTaskName() {
		return epmTask.toString();
	}

	public String getEpmTaskType() {
		return epmTask.getType();
	}

	public TCComponent[] getTaskList() {
		return this.tasks;
	}
}
