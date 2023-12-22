package com.teamcenter.vinfast.escooter.sorprocess.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCException;

public class AssigmentListModel {
	private String name = null;
	private String alName = "";
	private TaskListModel[] taskList = null;
	private ResourcesModel[] resources = null;
	private LinkedList<TCComponentGroupMember[]> userList = null;

	public AssigmentListModel(TCComponentAssignmentList rawModel) {
		try {
			this.name = rawModel.getProperty("list_desc");
			this.alName = rawModel.getProperty("object_string");

			TCComponent[] resRaw = rawModel.getRelatedComponents("resources");
			TCComponent[] taskRaw = rawModel.getRelatedComponents("task_templates");

			if (taskRaw != null) {
				this.taskList = new TaskListModel[taskRaw.length];
				for (int i = 0; i < taskRaw.length; i++) {
					taskList[i] = new TaskListModel(taskRaw[i]);
				}
			}

			if (resRaw != null) {
				userList = new LinkedList<TCComponentGroupMember[]>();
				this.resources = new ResourcesModel[resRaw.length];
				for (int i = 0; i < resRaw.length; i++) {
					resources[i] = new ResourcesModel(resRaw[i]);
					// Phong
					TCComponentGroupMember[] res = null;
					TCComponent[] items = resRaw[i].getRelatedComponents("resources");
					if (items != null && items.length > 0) {
						res = new TCComponentGroupMember[items.length];
						for (int j = 0; j < items.length; j++) {
							res[j] = (TCComponentGroupMember) items[j];
						}
					}
					userList.add(res);
				}
			}
		} catch (TCException e) {
			System.out.println("[AssignmentListModel] Exception: " + e.toString());
		}
	}

	public String getName() {
		return name;
	}

	public String getALName() {
		return alName;
	}

	public TaskListModel[] getTaskList() {
		return taskList;
	}

	public void setTaskList(TaskListModel[] taskList) {
		this.taskList = taskList;
	}

	public ResourcesModel[] getResources() {
		return resources;
	}

	public void setResources(ResourcesModel[] resources) {
		this.resources = resources;
	}

	public ResourcesModel getResourceByTaskName(String taskName) {
		ResourcesModel output = null;
		for (int i = 0; i < this.taskList.length; i++) {
			if (this.taskList[i].getName().compareToIgnoreCase(taskName) == 0) {
				output = this.resources[i];
				break;
			}
		}
		return output;
	}

	public void setResourceByTaskName(String taskName, ResourcesModel rm) {
		for (int i = 0; i < this.taskList.length; i++) {
			if (this.taskList[i].getName().compareToIgnoreCase(taskName) == 0) {
				this.resources[i] = rm;
				break;
			}
		}
	}

	public TCComponentGroupMember[] getUserListByTaskName(String taskName) {
		for (int i = 0; i < this.taskList.length; i++) {
			if (this.taskList[i].getName().compareToIgnoreCase(taskName) == 0) {
				return userList.get(i);
			}
		}
		return null;
	}

	public LinkedHashMap<String, Set<TCComponentGroupMember>> getReviewerEachGate() {
		LinkedHashMap<String, Set<TCComponentGroupMember>> returnList = new LinkedHashMap<String, Set<TCComponentGroupMember>>();
		for (TaskListModel task : taskList) {
			ResourcesModel reviewer = getResourceByTaskName(task.getName());
			if (reviewer != null) {
				Set<TCComponentGroupMember> userList = new HashSet<TCComponentGroupMember>();
				for (TCComponentGroupMember resourcesModel : reviewer.getGroupMember()) {
					userList.add(resourcesModel);
				}
				returnList.put(task.getName(), userList);
			}
		}

		return returnList;
	}
}
