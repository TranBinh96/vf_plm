package com.vinfast.car.sap.superbom;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponent;
import com.vinfast.sap.bom.BOMBOPData;

public class SuperBomBopTransferData {
	private TCComponent item;
	private ArrayList<BOMBOPData> data2Send;
	private HashMap<String, BOMBOPData> workingData;
	private HashMap<String, BOMBOPData> releaseData;

	public HashMap<String, BOMBOPData> getWorkingData() {
		return workingData;
	}

	public void setWorkingData(HashMap<String, BOMBOPData> workingData) {
		this.workingData = workingData;
	}
	
	public HashMap<String, BOMBOPData> getReleaseData() {
		return releaseData;
	}

	public void setReleaseData(HashMap<String, BOMBOPData> releaseData) {
		this.releaseData = releaseData;
	}

	public ArrayList<BOMBOPData> getData2Send() {
		return data2Send;
	}

	public void setData2Send(ArrayList<BOMBOPData> data2Send) {
		this.data2Send = data2Send;
	}

	public TCComponent getItem() {
		return item;
	}

	public void setItem(TCComponent item) {
		this.item = item;
	}
}
