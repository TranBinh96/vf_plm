package com.vinfast.car.sap.superbom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.teamcenter.rac.util.MessageBox;
import com.vinfast.sap.bom.BOMBOPData;

public class BomTrackingRawDataHandler {
	HashMap<String, BOMBOPData> rawActions = new HashMap<String, BOMBOPData>();
	ArrayList<BomTrackingModel> rawModels = new ArrayList<BomTrackingModel>();

	public ArrayList<BomTrackingModel> getRawModels() {
		return rawModels;
	}

	HashMap<String, BomTrackingModel> mapModels = new HashMap<String, BomTrackingModel>();

	public void addRawData(BOMBOPData boms) {
		try {
			BOMBOPData clone = boms.clone();
			String key = String.format("%s~%s", clone.getPartNumber(), clone.getAction());
			if (rawActions.containsKey(key)) {
				BOMBOPData curData = rawActions.get(key);
				int quantity = (int) Float.parseFloat(curData.getQuanity()) + (int) Float.parseFloat(clone.getQuanity());
				curData.setQuanity(String.valueOf(quantity));
				rawActions.put(key, curData);
			} else {
				rawActions.put(key, clone);
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	HashMap<String, BomTrackingModel> getMapModels() {
		if (mapModels != null && mapModels.size() > 0) {
			return mapModels;
		}
		for (BomTrackingModel model : rawModels) {
			mapModels.put(String.format("%s~%s", model.getSubGroup(), model.getBomlineID()), model);
		}
		return mapModels;
	}

	private void createBomTrackingData() {
		for (HashMap.Entry<String, BOMBOPData> entry : rawActions.entrySet()) {
			BOMBOPData raw = entry.getValue();
			BomTrackingModel model = new BomTrackingModel();
			model.setActionType(raw.getAction());
			model.setMainGroup(raw.getMainGroup());
			model.setSubGroup(raw.getSubGroup());
			model.setBomlineID(raw.getBOMLineID());
			model.setPartNumber(raw.getPartNumber());
			model.setQuantity(raw.getQuanity());
			model.setNewPartNumber("");
			model.setReplacedPartNumber("");
			model.setIsTransferred("");
			rawModels.add(model);
		}
	}

	public ArrayList<BomTrackingModel> merge(HashSet<String> data) {

		if (data.size() != 2) {
			MessageBox.post("You can only select 01 line Add & 01 line Delete", "Error", MessageBox.ERROR);
			return this.rawModels;
		}
		int sum = 0;
		ArrayList<BomTrackingModel> selectModels = new ArrayList<BomTrackingModel>();
		for (String key : data) {
			BomTrackingModel model = getMapModels().get(key);
			if (model.getActionType().equals("Add")) {
				sum++;
			} else if (model.getActionType().equals("Delete")) {
				sum--;
			}
			selectModels.add(model);

		}

		if (sum != 0) {
			MessageBox.post("You can only select 01 line Add & 01 line Delete", "Error", MessageBox.ERROR);
			return this.rawModels;
		}
		for (String key : data) {
			BomTrackingModel model = getMapModels().get(key);
			rawModels.remove(model);
			mapModels.remove(key);
		}

		BomTrackingModel replacedActionAdd = new BomTrackingModel();
		BomTrackingModel replacedActionDelete = new BomTrackingModel();
		replacedActionAdd.setActionType("Replaced");
		replacedActionDelete.setActionType("Replaced");
		for (BomTrackingModel model : selectModels) {
			if (model.getActionType().equals("Add")) {
				replacedActionAdd.setMainGroup(model.getMainGroup());
				replacedActionAdd.setSubGroup(model.getSubGroup());
				replacedActionAdd.setBomlineID(model.getBomlineID());
				replacedActionAdd.setPartNumber(model.getPartNumber());
				replacedActionAdd.setQuantity(model.getQuantity());
				replacedActionDelete.setNewPartNumber(model.getPartNumber());
			} else {
				replacedActionDelete.setMainGroup(model.getMainGroup());
				replacedActionDelete.setSubGroup(model.getSubGroup());
				replacedActionDelete.setBomlineID(model.getBomlineID());
				replacedActionDelete.setPartNumber(model.getPartNumber());
				replacedActionDelete.setQuantity(model.getQuantity());
				replacedActionAdd.setReplacedPartNumber(model.getPartNumber());
			}
		}
		this.rawModels.add(replacedActionDelete);
		this.rawModels.add(replacedActionAdd);
		this.mapModels.put(String.format("%s~%s", replacedActionDelete.getSubGroup(), replacedActionDelete.getBomlineID()), replacedActionDelete);
		this.mapModels.put(String.format("%s~%s", replacedActionAdd.getSubGroup(), replacedActionAdd.getBomlineID()), replacedActionAdd);
		return this.rawModels;
	}

	public StringBuffer getBomTrackingFile() {
		// create model list
		createBomTrackingData();

		// save model list to file
		StringBuffer data = new StringBuffer();
		for (BomTrackingModel model : rawModels) {
			String line = String.format("%s~%s~%s~%s~%s~%s~%s~%s~%s", getDataToWriteFile(model.getActionType()), getDataToWriteFile(model.getMainGroup()), getDataToWriteFile(model.getSubGroup()), getDataToWriteFile(model.getBomlineID()), getDataToWriteFile(model.getPartNumber()), getDataToWriteFile(model.getQuantity()), getDataToWriteFile(model.getNewPartNumber()), getDataToWriteFile(model.getReplacedPartNumber()), getDataToWriteFile(model.getIsTransferred()));
			data.append(line);
			data.append("\n");
		}
		return data;
	}

	public void print() {
		for (BomTrackingModel model : rawModels) {
			String line = String.format("%s~%s~%s~%s~%s~%s~%s~%s~%s", getDataToWriteFile(model.getActionType()), getDataToWriteFile(model.getMainGroup()), getDataToWriteFile(model.getSubGroup()), getDataToWriteFile(model.getBomlineID()), getDataToWriteFile(model.getPartNumber()), getDataToWriteFile(model.getQuantity()), getDataToWriteFile(model.getNewPartNumber()), getDataToWriteFile(model.getReplacedPartNumber()), getDataToWriteFile(model.getIsTransferred()));
			System.out.println(line);
		}
	}

	public ArrayList<BomTrackingModel> getBomTrackingData(File file) {
		rawModels = new ArrayList<BomTrackingModel>();
		mapModels = new HashMap<String, BomTrackingModel>();
		BufferedReader bufferReader = null;
		try {
			bufferReader = new BufferedReader(new FileReader(file));
			if (bufferReader.ready()) {
				String sCurrentLine = "";
				while ((sCurrentLine = bufferReader.readLine()) != null) {
					BomTrackingModel model = new BomTrackingModel();
					String[] data = sCurrentLine.split("~");
					model.setActionType(data[0]);
					model.setMainGroup(data[1]);
					model.setSubGroup(data[2]);
					model.setBomlineID(data[3]);
					model.setPartNumber(data[4]);
					model.setQuantity(data[5]);
					model.setNewPartNumber(data[6]);
					model.setReplacedPartNumber(data[7]);
					model.setIsTransferred(data[8]);
					rawModels.add(model);
				}
			}

			bufferReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferReader != null) {
				try {
					bufferReader.close();
				} catch (Throwable e) {
					bufferReader = null;
				}
			}
		}
		return rawModels;
	}

	public boolean mergeLine(HashSet<String> setSelectedLine) {
		boolean isOk = false;
		return isOk;
	}

	private String getDataToWriteFile(String data) {
		if (data.isEmpty()) {
			return "*";
		} else {
			return data;
		}
	}
}
