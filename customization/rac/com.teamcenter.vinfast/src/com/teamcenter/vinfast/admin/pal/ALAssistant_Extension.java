package com.teamcenter.vinfast.admin.pal;

import java.util.LinkedList;

import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentProfile;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;

public class ALAssistant_Extension {
	public static int getActionByTasktype(String taskType) {
		if (taskType.compareToIgnoreCase(ALAssistant_Constant.TASKTEMPLATE_REVIEW) == 0)
			return ALAssistant_Constant.AL_ACTION_REVIEW;
		else if (taskType.compareToIgnoreCase(ALAssistant_Constant.TASKTEMPLATE_CONDITION) == 0)
			return ALAssistant_Constant.AL_ACTION_PERFORM;
		else if (taskType.compareToIgnoreCase(ALAssistant_Constant.TASKTEMPLATE_ACKNOWLEDGE) == 0)
			return ALAssistant_Constant.AL_ACTION_ACKNOW;
		else if (taskType.compareToIgnoreCase(ALAssistant_Constant.TASKTEMPLATE_DO) == 0)
			return ALAssistant_Constant.AL_ACTION_PERFORM;
		else if (taskType.compareToIgnoreCase(ALAssistant_Constant.TASKTEMPLATE_TASK) == 0)
			return ALAssistant_Constant.AL_ACTION_PERFORM;
		else
			return ALAssistant_Constant.AL_ACTION_REVIEW;
	}

	public static boolean validateUpdateConditionTask(LinkedList<TCComponent> newMembers, TCComponent[] addUserLists) {
		if (newMembers.size() > 0)
			return false;

		if (addUserLists.length > 1)
			return false;

		return true;
	}

	public static ResourceMember addNewUserToResourceNotNull(ResourceMember resourcesTask, TCComponentTaskTemplate taskTemplate, LinkedList<TCComponentGroupMember> userLists) {
		TCComponentProfile[] currentProfile = resourcesTask.getProfiles();
		Integer[] currentActions = resourcesTask.getActions();
		TCComponent[] currentMembers = resourcesTask.getResources();
		int i = -100;
		int j = -100;
		int k = 0;

		LinkedList<TCComponentProfile> newProfile = new LinkedList<TCComponentProfile>();
		if (currentProfile != null && currentProfile.length > 0) {
			for (TCComponentProfile profile : currentProfile) {
				newProfile.add(profile);
			}
		}

		LinkedList<Integer> newAction = new LinkedList<Integer>();
		if (currentActions != null && currentActions.length > 0) {
			for (Integer action : currentActions) {
				newAction.add(action);
			}
		}

		LinkedList<TCComponent> newMembers = new LinkedList<TCComponent>();
		if (currentMembers != null && currentMembers.length > 0) {
			for (TCComponent member : currentMembers) {
				newMembers.add(member);
			}
		}

		String taskType = taskTemplate.getType();
		if (taskType.compareToIgnoreCase(ALAssistant_Constant.TASKTEMPLATE_CONDITION) != 0 || ALAssistant_Extension.validateUpdateConditionTask(newMembers, userLists.toArray(new TCComponent[0]))) {
			int actionValue = ALAssistant_Extension.getActionByTasktype(taskType);
			for (TCComponentGroupMember member : userLists) {
				if (!newMembers.contains(member)) {
					newProfile.add(null);
					newAction.add(actionValue);
					newMembers.add(member);
				}
			}
		}

		ResourceMember newResource = new ResourceMember(taskTemplate, newMembers.toArray(new TCComponent[0]), newProfile.toArray(new TCComponentProfile[0]), newAction.toArray(new Integer[0]), i, j, k);
		return newResource;
	}

	public static ResourceMember updateResourceInfo(ResourceMember resourcesTask, TCComponentTaskTemplate taskTemplate, int revQuorum, int ackQuorum) {
		TCComponentProfile[] currentProfile = resourcesTask.getProfiles();
		Integer[] currentActions = resourcesTask.getActions();
		TCComponent[] currentMembers = resourcesTask.getResources();
		int k = 0;

		ResourceMember newResource = new ResourceMember(taskTemplate, currentMembers, currentProfile, currentActions, revQuorum, ackQuorum, k);
		return newResource;
	}

	public static ResourceMember updateActionForUser(ResourceMember resourcesTask, TCComponentTaskTemplate taskTemplate, TCComponentGroupMember member, boolean isRequired) {
		TCComponentProfile[] currentProfile = resourcesTask.getProfiles();
		Integer[] currentActions = resourcesTask.getActions();
		TCComponent[] currentMembers = resourcesTask.getResources();
		int i = resourcesTask.getReviewQuorum();
		int j = resourcesTask.getAcknowQuorum();
		int k = 0;

		String taskType = taskTemplate.getType();
		int action = -1;
		if (taskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_REVIEW) == 0) {
			if (isRequired)
				action = ALAssistant_Constant.AL_ACTION_REVIEW_REQUIRED;
			else
				action = ALAssistant_Constant.AL_ACTION_REVIEW;
		} else if (taskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_ACKNOWLEDGE) == 0) {
			if (isRequired)
				action = ALAssistant_Constant.AL_ACTION_ACKNOW_REQUIRED;
			else
				action = ALAssistant_Constant.AL_ACTION_ACKNOW;
		}

		if (action < 0)
			return null;

		for (int l = 0; l < currentMembers.length; l++) {
			if (currentMembers[l] == member)
				currentActions[l] = action;
		}

		ResourceMember newResource = new ResourceMember(taskTemplate, currentMembers, currentProfile, currentActions, i, j, k);
		return newResource;
	}

	public static ResourceMember addNewUserToResourceNull(TCComponentTaskTemplate taskTemplate, LinkedList<TCComponentGroupMember> userLists) {
		int i = -100;
		int j = -100;
		int k = 0;
		LinkedList<TCComponentProfile> newProfile = new LinkedList<TCComponentProfile>();
		LinkedList<Integer> newAction = new LinkedList<Integer>();
		LinkedList<TCComponent> newMembers = new LinkedList<TCComponent>();

		String taskType = taskTemplate.getType();
		if (taskType.compareToIgnoreCase(ALAssistant_Constant.TASKTEMPLATE_CONDITION) != 0 || ALAssistant_Extension.validateUpdateConditionTask(newMembers, userLists.toArray(new TCComponent[0]))) {
			int actionValue = ALAssistant_Extension.getActionByTasktype(taskType);
			for (TCComponentGroupMember member : userLists) {
				newProfile.add(null);
				newAction.add(actionValue);
				newMembers.add(member);
			}
		}

		ResourceMember newResource = new ResourceMember(taskTemplate, newMembers.toArray(new TCComponent[0]), newProfile.toArray(new TCComponentProfile[0]), newAction.toArray(new Integer[0]), i, j, k);
		return newResource;
	}

	public static ResourceMember replaceUserFromResourceNotNull(ResourceMember resourcesTask, TCComponentTaskTemplate taskTemplate, TCComponentGroupMember oldUser, TCComponentGroupMember newUser) {
		TCComponentProfile[] currentProfile = resourcesTask.getProfiles();
		Integer[] currentActions = resourcesTask.getActions();
		TCComponent[] currentMembers = resourcesTask.getResources();
		int i = -100;
		int j = -100;
		int k = 0;

		LinkedList<TCComponentProfile> newProfile = new LinkedList<TCComponentProfile>();
		if (currentProfile != null && currentProfile.length > 0) {
			for (TCComponentProfile profile : currentProfile) {
				newProfile.add(profile);
			}
		}

		LinkedList<Integer> newAction = new LinkedList<Integer>();
		if (currentActions != null && currentActions.length > 0) {
			for (Integer action : currentActions) {
				newAction.add(action);
			}
		}

		LinkedList<TCComponent> newMembers = new LinkedList<TCComponent>();
		if (currentMembers != null && currentMembers.length > 0) {
			for (TCComponent member : currentMembers) {
				newMembers.add(member);
			}
		}

		for (int l = 0; l < newMembers.size(); l++) {
			if (newMembers.get(l) == oldUser) {
				String taskType = taskTemplate.getType();
				int actionValue = ALAssistant_Extension.getActionByTasktype(taskType);
				if (newUser == null) {
					newProfile.remove(l);
					newAction.remove(l);
					newMembers.remove(l);
				} else {
					newProfile.set(l, null);
					newAction.set(l, actionValue);
					newMembers.set(l, newUser);
				}

				break;
			}
		}

		if (newMembers.size() == 0)
			return null;

		ResourceMember newResource = new ResourceMember(taskTemplate, newMembers.toArray(new TCComponent[0]), newProfile.toArray(new TCComponentProfile[0]), newAction.toArray(new Integer[0]), i, j, k);
		return newResource;
	}

	public static int getActionOfMember(ResourceMember resourcesTask, TCComponentGroupMember user) {
		Integer[] currentActions = resourcesTask.getActions();
		TCComponent[] currentMembers = resourcesTask.getResources();

		if (currentMembers != null) {
			for (int i = 0; i < currentMembers.length; i++) {
				if (currentMembers[i] == user) {
					return currentActions[i];
				}
			}
		}

		return -1;
	}
}
