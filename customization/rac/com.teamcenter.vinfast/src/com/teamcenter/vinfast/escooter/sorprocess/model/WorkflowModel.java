package com.teamcenter.vinfast.escooter.sorprocess.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCException;

public class WorkflowModel {
	private String wfName = null;
	private AssigmentListModel[] assignmentList = null;
	private TCComponentTaskTemplate wf = null;

	public WorkflowModel(TCComponentTaskTemplate wf) {
		try {
			this.wfName = wf.getName();
			this.wf = wf;
			setAssignmentListModel(wf);
		} catch (TCException e) {
			System.out.println("[WorkflowModel] Exception: " + e.toString());
		}
	}

	public String getWfName() {
		return wfName;
	}

	public void setWfName(String wfName) {
		this.wfName = wfName;
	}

	public AssigmentListModel[] getAssignmentList() {
		return assignmentList;
	}

	public void setAssignmentListModel(TCComponentTaskTemplate wf) throws TCException {
		TCComponent[] agmList = wf.getRelatedComponents("assignment_lists");
		if (agmList != null && agmList.length > 0) {
			this.assignmentList = new AssigmentListModel[agmList.length];
			for (int i = 0; i < agmList.length; i++) {
				AssigmentListModel m = new AssigmentListModel((TCComponentAssignmentList) agmList[i]);
				this.assignmentList[i] = m;
			}
		}
	}

	public String[] getAssignmentListDispName() {
		if (getAssignmentList() != null) {
			String[] output = new String[getAssignmentList().length];
			for (int i = 0; i < getAssignmentList().length; i++) {
				output[i] = getAssignmentList()[i].getName();
			}
			return output;
		} else {
			return null;
		}
	}

	public LinkedHashMap<String, String> getAssignmentListDispNameByProgram(String programSelect) {
		if (getAssignmentList() != null) {
			LinkedHashMap<String, String> alList = new LinkedHashMap<>();
			for (AssigmentListModel alItem : getAssignmentList()) {
				String alName = alItem.getALName();
				String alDesc = alItem.getName();
				if (alDesc.contains(";")) {
					String[] str = alDesc.split(";");
					String module = str[0];
					List<String> programs = Arrays.asList(str[1].split(","));
					if (programs.contains(programSelect)) {
						alList.put(alName, module);
					}
				}
			}
			return alList;
		}

		return null;
	}

	public AssigmentListModel getAssignmentListByDesc(String desc) {
		if (getAssignmentList() != null) {
			for (AssigmentListModel m : this.assignmentList) {
				if (m.getName().compareToIgnoreCase(desc) == 0) {
					return m;
				}
			}
		}
		return null;
	}

	public AssigmentListModel getAssignmentListByName(String name) {
		if (getAssignmentList() != null) {
			for (AssigmentListModel m : this.assignmentList) {
				if (m.getALName().compareToIgnoreCase(name) == 0) {
					return m;
				}
			}
		}
		return null;
	}
}
