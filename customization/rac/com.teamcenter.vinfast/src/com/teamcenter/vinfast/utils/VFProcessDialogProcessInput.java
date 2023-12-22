package com.teamcenter.vinfast.utils;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

import com.teamcenter.rac.kernel.TCComponent;
import com.vf.utils.VFAssignmentList;

public class VFProcessDialogProcessInput {
	private List<VFAssignmentList> assignmentList;
	private IValidator validator;
	private String defaultAssignmentList;
	
	public VFProcessDialogProcessInput(List<TCComponent> assignmentList, IValidator validator, String defaultAssignmentList) {
		this.assignmentList = new LinkedList<VFAssignmentList>();
		for (TCComponent assignmentListTemp : assignmentList) {
			this.assignmentList.add(new VFAssignmentList(assignmentListTemp));
		}
		this.validator = validator;
		this.defaultAssignmentList = defaultAssignmentList;
	}

	public VFProcessDialogProcessInput(List<TCComponent> assignmentList) {
		this.assignmentList = new LinkedList<VFAssignmentList>();
		for (TCComponent assignmentListTemp : assignmentList) {
			this.assignmentList.add(new VFAssignmentList(assignmentListTemp));
		}
	}
	
	public VFProcessDialogProcessInput(TCComponent[] assignmentList) {
		this.assignmentList = new LinkedList<VFAssignmentList>();
		for (TCComponent assignmentListTemp : assignmentList) {
			this.assignmentList.add(new VFAssignmentList(assignmentListTemp));
		}
	}
	
	public List<VFAssignmentList> getAssignmentList() {
		return assignmentList;
	}
	
	public void setAssignmentList(List<VFAssignmentList> assignmentList) {
		this.assignmentList = assignmentList;
	}
	
	public IValidator getValidator() {
		return validator;
	}

	public String getDefaultAssignmentList() {
		return defaultAssignmentList;
	}
	
	public TCComponent getAssignmentListByDesc(String description) {
		for(int i = 0; i < assignmentList.size(); i++) {
			if(assignmentList.get(i).toString().compareToIgnoreCase(description) == 0) {
				return assignmentList.get(i).getAssignmentList();
			}
		}
		return null;
	}
	
	public String[] getDescriptionList() {
		List<String> outputList = new LinkedList<String>();
		for (VFAssignmentList item : assignmentList) {
			outputList.add(item.toString());
		}
		
		return outputList.toArray(new String[0]);
	}
}
