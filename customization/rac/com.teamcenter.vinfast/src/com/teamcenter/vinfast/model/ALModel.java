package com.teamcenter.vinfast.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.vinfast.admin.pal.ALAssistant_Constant;
import com.teamcenter.vinfast.escooter.sorprocess.model.TaskListModel;

public class ALModel {
	private String alName;
	private String alDesc;
	private LinkedHashMap<TaskListModel, Set<TCComponentGroupMember>> reviewGateList = new LinkedHashMap<TaskListModel, Set<TCComponentGroupMember>>();
	private LinkedHashMap<String, Set<TCComponentGroupMember>> addingReviewGateList = new LinkedHashMap<String, Set<TCComponentGroupMember>>();

	public ALModel(TCComponentAssignmentList rawModel, Set<TCComponent> subTaskList) {
		try {
			alName = rawModel.getProperty("object_string");
			alDesc = rawModel.getProperty("list_desc");

			TCComponent[] resRaws = rawModel.getRelatedComponents("resources");
			TCComponent[] taskRaws = rawModel.getRelatedComponents("task_templates");
			for (TCComponent task : subTaskList) {
				String taskType = task.getType();
				if (taskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_REVIEW) != 0 && taskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_ACKNOWLEDGE) != 0 && taskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_DO) != 0 && taskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_CONDITION) != 0)
					continue;

				String taskName = ((TCComponentTaskTemplate) task).getName();

				Set<TCComponentGroupMember> userList = new HashSet<TCComponentGroupMember>();
				int index = -1;
				int i = 0;
				for (TCComponent taskRaw : taskRaws) {
					if (taskRaw.toString().compareTo(taskName) == 0) {
						index = i;
						break;
					}
					i++;
				}

				if (index > -1) {
					TCComponent[] items = resRaws[index].getRelatedComponents("resources");
					if (items != null) {
						for (TCComponent item : items) {
							userList.add((TCComponentGroupMember) item);
						}
					}
				}

				reviewGateList.put(new TaskListModel(task), userList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getALName() {
		return alName;
	}

	public String getALDesc() {
		return alDesc;
	}

	public LinkedHashMap<String, Set<TCComponentGroupMember>> getGateMapping() {
		LinkedHashMap<String, Set<TCComponentGroupMember>> gateMapping = new LinkedHashMap<String, Set<TCComponentGroupMember>>();
		for (Map.Entry<TaskListModel, Set<TCComponentGroupMember>> entrySet : reviewGateList.entrySet()) {
			String gate = entrySet.getKey().getName();
			Set<TCComponentGroupMember> reviewerList = entrySet.getValue();

			if (reviewerList.size() > 0) {
				gateMapping.put(gate, reviewerList);
			}
		}
		return gateMapping;
	}

	public LinkedHashMap<TaskListModel, Set<TCComponentGroupMember>> getReviewGateList() {
		return reviewGateList;
	}

	public LinkedHashMap<String, Set<TCComponentGroupMember>> getAddingReviewGateList() {
		return addingReviewGateList;
	}

	public Set<TCComponentGroupMember> getReviewerByGate(String gate) {
		for (Map.Entry<TaskListModel, Set<TCComponentGroupMember>> entrySet : reviewGateList.entrySet()) {
			if (entrySet.getKey().getName().compareTo(gate) == 0) {
				return entrySet.getValue();
			}
		}

		return null;
	}

	public void addReviewer(String gate, TCComponentGroupMember reviewer) {
		for (Map.Entry<TaskListModel, Set<TCComponentGroupMember>> entrySet : reviewGateList.entrySet()) {
			if (entrySet.getKey().getName().compareTo(gate) == 0) {
				entrySet.getValue().add(reviewer);
				break;
			}
		}

		if (addingReviewGateList.containsKey(gate)) {
			addingReviewGateList.get(gate).add(reviewer);
		} else {
			Set<TCComponentGroupMember> reviewerList = new HashSet<TCComponentGroupMember>();
			reviewerList.add(reviewer);
			addingReviewGateList.put(gate, reviewerList);
		}
	}

	public void addReviewer(String gate, Set<TCComponentGroupMember> reviewers) {
		for (Map.Entry<TaskListModel, Set<TCComponentGroupMember>> entrySet : reviewGateList.entrySet()) {
			if (entrySet.getKey().getName().compareTo(gate) == 0) {
				entrySet.getValue().addAll(reviewers);
				break;
			}
		}

		if (addingReviewGateList.containsKey(gate)) {
			addingReviewGateList.get(gate).addAll(reviewers);
		} else {
			addingReviewGateList.put(gate, reviewers);
		}
	}

	public void replaceReviewer(String gate, TCComponentGroupMember oldReviewer, TCComponentGroupMember newReviewer) {
		for (Map.Entry<TaskListModel, Set<TCComponentGroupMember>> entrySet : reviewGateList.entrySet()) {
			if (entrySet.getKey().getName().compareTo(gate) == 0) {
				Set<TCComponentGroupMember> reviewerList = entrySet.getValue();

				if (oldReviewer == null) {
					reviewerList.add(newReviewer);
				} else {
					if (reviewerList.contains(oldReviewer)) {
						reviewerList.remove(oldReviewer);
						reviewerList.add(newReviewer);
					}
				}

				break;
			}
		}
	}
}
