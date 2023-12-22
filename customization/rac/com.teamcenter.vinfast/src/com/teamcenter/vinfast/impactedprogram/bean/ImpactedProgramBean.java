package com.teamcenter.vinfast.impactedprogram.bean;

import java.util.ArrayList;

import com.teamcenter.rac.kernel.TCComponent;

public class ImpactedProgramBean {
	private TCComponent tcComponent = null;
	private ArrayList<TCComponent> impactedPartList = null;
	private String strPartList;
	private String strProgramName;

	public TCComponent getTcComponent() {
		return tcComponent;
	}

	public void setTcComponent(TCComponent tcComponent) {
		this.tcComponent = tcComponent;
	}

	public ArrayList<TCComponent> getImpactedPartList() {
		return impactedPartList;
	}

	public void setImpactedPartList(ArrayList<TCComponent> impactedPartList) {
		this.impactedPartList = impactedPartList;
	}

	public String getStrPartList() {
		return strPartList;
	}

	public void setStrPartList(String strPartList) {
		this.strPartList = strPartList;
	}

	public String getStrProgramName() {
		return strProgramName;
	}

	public void setStrProgramName(String strProgramName) {
		this.strProgramName = strProgramName;
	}
}
