package com.teamcenter.vinfast.escooter.sorprocess.model;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCException;

public class TaskListModel {
	private String name = null;
	private String type = null;
	private String taskType = "";
	private TCComponent task = null;

	public TaskListModel(TCComponent task) throws TCException {
		this.task = task;
		this.name = task.toString();
		this.type = task.getProperty("task_type");
		taskType = ((TCComponentTaskTemplate) task).getType();
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public TCComponent getTask() {
		return task;
	}

	public String getTaskType() {
		return taskType;
	}
}
