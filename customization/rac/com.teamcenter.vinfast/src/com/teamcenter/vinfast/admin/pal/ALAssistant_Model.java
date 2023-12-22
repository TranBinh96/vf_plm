package com.teamcenter.vinfast.admin.pal;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.Query;
import com.vf.utils.TCExtension;

public class ALAssistant_Model {
	private TCSession session;
	private LinkedHashMap<String, TCComponentTaskTemplate> subTaskList;
	private boolean updateValid = true;
	private TCComponentAssignmentList alObject = null;
	private String alName = "";

	private String alDesc = "";

	private LinkedList<ResourceMember> resourceList = new LinkedList<>();

	private LinkedList<String> taskDetail = new LinkedList<>();

	public ALAssistant_Model(TCSession sesion, LinkedHashMap<String, TCComponentTaskTemplate> subTaskList) {
		this.session = sesion;
		this.subTaskList = subTaskList;
	}

	public void setALName(String alName) {
		this.alName = alName;
		try {
			alObject = TCExtension.GetALByName(alName, session);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (alObject != null) {
			alDesc = alObject.getDescription();
		}
	}

	public void setALDesc(String alDesc) {
		this.alDesc = alDesc;
	}

	public String getALNameHtml() {
		if (alObject != null) {
			return "<p>" + alName + "</p><br>";
		} else {
			return "<p style=\"color:#E91E63\">" + alName + "</p><br>";
		}
	}

	public String getALName() {
		return alName;
	}

	public String getALDesc() {
		return alDesc;
	}

	public TCComponentAssignmentList getALObject() {
		return alObject;
	}

	public LinkedList<ResourceMember> getResourceList() {
		return resourceList;
	}

	public LinkedList<String> getTaskDetail() {
		return taskDetail;
	}

	public boolean getUpdateValid() {
		return updateValid;
	}

	public void setTaskObject(String task, String users) {
		if (users.isEmpty()) {
			taskDetail.add("<p></p>");
			return;
		}

		StringBuilder userTextHtml = new StringBuilder();
		LinkedList<TCComponentGroupMember> userList = new LinkedList<>();
		for (String user1 : users.split(",")) {
			String user = user1.stripLeading().stripTrailing();
			boolean check = false;
			if (user.contains("/")) {
				String[] userSplit = user.split("/");
				if (userSplit.length == 3) {
					TCComponentGroupMember member = seachUser(userSplit[0], userSplit[1], userSplit[2]);
					if (member != null) {
						userList.add(member);
						check = true;
					}
				}
			}

			if (check) {
				userTextHtml.append("<p>" + user + "</p>");
			} else {
				userTextHtml.append("<p style=\"color:#E91E63\">" + user + "</p>");
				updateValid = false;
			}
		}

		if (updateValid)
			resourceList.add(ALAssistant_Extension.addNewUserToResourceNull(subTaskList.get(task), userList));

		taskDetail.add(userTextHtml.toString());
	}

	private TCComponentGroupMember seachUser(String userGroup, String userRole, String userID) {
		LinkedHashMap<String, String> queryInput = new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("Id", userID);
				put("Role", userRole);
				put("Group", userGroup);
			}
		};

		TCComponent[] objectSearch = Query.queryItem(session, queryInput, "__TNH_FindGroupMem");
		if (objectSearch != null) {
			for (TCComponent obj : objectSearch) {
				try {
					if (obj instanceof TCComponentGroupMember) {
						TCComponentGroupMember member = (TCComponentGroupMember) obj;
						return member;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}
