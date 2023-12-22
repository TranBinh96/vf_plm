package com.teamcenter.vinfast.model;

import com.teamcenter.rac.kernel.TCComponentBOMLine;

public class PartSubstitutesPasteModel {
	private TCComponentBOMLine part = null;
	private String status = "";
	
	public PartSubstitutesPasteModel(TCComponentBOMLine item, String _status) {
		part = item;
		status = _status;
	}
	
	public TCComponentBOMLine getPart() {
		return part;
	}
	public void setPart(TCComponentBOMLine part) {
		this.part = part;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getBomline() {
		String bomline = "";
		try {
			bomline = part.getPropertyDisplayableValue("bl_indented_title");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bomline;
	}
	
	public String getSubstitues() {
		String substitues = "";
		try {
			substitues = part.getPropertyDisplayableValue("bl_substitute_list");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return substitues;
	}
}
