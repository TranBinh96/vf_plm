package com.vf.utils;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;

public class VFAssignmentList {
	TCComponent assignmentList;

	public VFAssignmentList(TCComponent assignmentList) {
		super();
		this.assignmentList = assignmentList;
	}

	public TCComponent getAssignmentList() {
		return assignmentList;
	}

	public void setAssignmentList(TCComponent assignmentList) {
		this.assignmentList = assignmentList;
	}
	
	public String toString() {
		try {
			return assignmentList.getProperty("list_desc");
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public String getName() {
		try {
			return assignmentList.getProperty("list_name");
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		return "";
	}
}
