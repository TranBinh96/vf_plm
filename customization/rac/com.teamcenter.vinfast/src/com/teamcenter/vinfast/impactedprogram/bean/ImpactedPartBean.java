package com.teamcenter.vinfast.impactedprogram.bean;

import java.util.ArrayList;

import com.teamcenter.rac.kernel.TCComponent;

public class ImpactedPartBean {
	private TCComponent tcComponent = null;
	private ArrayList<TCComponent> programList = null;
	private String strProgramList;
	private String strPartName;

	public TCComponent getTcComponent() {
		return tcComponent;
	}

	public void setTcComponent(TCComponent tcComponent) {
		this.tcComponent = tcComponent;
	}

	public ArrayList<TCComponent> getProgramList() {
		return programList;
	}

	public void setProgramList(ArrayList<TCComponent> programList) {
		this.programList = programList;
	}

	public String getStrProgramList() {
		return strProgramList;
	}

	public void setStrProgramList(String strProgramList) {
		this.strProgramList = strProgramList;
	}

	public String getStrPartName() {
		return strPartName;
	}

	public void setStrPartName(String strPartName) {
		this.strPartName = strPartName;
	}
}
