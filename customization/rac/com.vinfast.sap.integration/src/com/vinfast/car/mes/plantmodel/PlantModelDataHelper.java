package com.vinfast.car.mes.plantmodel;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.PlantReport;
import com.vinfast.integration.model.ReportMessage.UpdateType;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class PlantModelDataHelper {

	private ArrayList<PlantReport> listReport = new ArrayList<PlantReport>();

	public ArrayList<PlantReport> getListReport() {
		return listReport;
	}

	public void setListReport(ArrayList<PlantReport> listReport) {
		this.listReport = listReport;
	}

	private HashMap<String, TCComponent> getWorkstationInformation(DataManagementService dmCoreService, TCComponentBOMLine MEStation) {
		HashMap<String, TCComponent> dataMap = new HashMap<String, TCComponent>();
		TCComponentBOMLine MEStationLine = null;
		TCComponentBOMLine MELineLine = null;
		TCComponentBOMLine MEShopLine = null;
		TCComponentBOMLine MEPlantLine = null;

		try {

			dmCoreService.getProperties(new TCComponent[] { MEStation }, new String[] { PropertyDefines.BOM_OBJECT_TYPE, PropertyDefines.BOM_PARENT });

			if (MEStation.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MESTATIONREVISION)) {

				MEStationLine = MEStation;

				TCComponentBOMLine LineRevisionLine = (TCComponentBOMLine) MEStation.getReferenceProperty(PropertyDefines.BOM_PARENT);

				dmCoreService.getProperties(new TCComponent[] { LineRevisionLine }, new String[] { PropertyDefines.BOM_OBJECT_TYPE, PropertyDefines.BOM_PARENT });

				if (LineRevisionLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MELINEREVISION)) {

					MELineLine = LineRevisionLine;

					TCComponentBOMLine MEShopRevisionLine = (TCComponentBOMLine) LineRevisionLine.getReferenceProperty(PropertyDefines.BOM_PARENT);

					if (MEShopRevisionLine != null && (MEShopRevisionLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MESHOPREVISION) || MEShopRevisionLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MELINEREVISION))) {

						MEShopLine = MEShopRevisionLine;

						TCComponentBOMLine MEPlantRevisionLine = (TCComponentBOMLine) MEShopLine.getReferenceProperty(PropertyDefines.BOM_PARENT);

						if (MEPlantRevisionLine != null && MEPlantRevisionLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MEPLANTREVISION)) {

							MEPlantLine = MEPlantRevisionLine;
						}
					} else {

						if (MEShopRevisionLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MEPLANTREVISION)) {

							MEShopLine = MELineLine;

							MEPlantLine = MEShopRevisionLine;
						}
					}
				}
			}

			if (MEStationLine != null) {
				dataMap.put("workstation", MEStationLine);
			}
			if (MELineLine != null) {
				dataMap.put("line", MELineLine);
			}
			if (MEShopLine != null) {
				dataMap.put("shop", MEShopLine);
			}
			if (MEPlantLine != null) {
				dataMap.put("plant", MEPlantLine);
			}

		} catch (TCException e1) {
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

		return dataMap;
	}

	public HashMap<TCComponentItemRevision, ArrayList<HashMap<String, StringBuffer>>> loadDataPlantView(ArrayList<String> transferIDs, TCSession clientSession, TCComponentItemRevision obj_shopTopNode, TCComponentItemRevision obj_Shop) throws NotLoadedException, TCException {
		HashMap<TCComponentItemRevision, ArrayList<HashMap<String, StringBuffer>>> transferToMES = null;
		TCComponent BOMLine = null;
		TCComponent stationLine = null;
		TCComponent traverseStructure = null;
		DataManagementService dmCoreService = DataManagementService.getService(clientSession);
		int count = 0;
		String searchIDs = UIGetValuesUtility.convertArrayToString(transferIDs, ";");

		OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(clientSession, obj_shopTopNode.getItem());

		for (OpenContextInfo views : createdBOMViews) {

			if (views.context.getType().equals("BOMLine")) {
				BOMLine = views.context;
			}
			if (views.context.getType().equals("Mfg0BvrWorkarea")) {
				stationLine = views.context;
			}
		}

		TCComponentForm topLineMasterForm = (TCComponentForm) obj_Shop.getItem().getRelatedComponent("IMAN_master_form");
		String searchPlantModel = topLineMasterForm.getProperty("user_data_3");

		if (searchPlantModel.length() != 0) {

			ArrayList<TCComponent> plantModelList = UIGetValuesUtility.searchPartsInStruture(clientSession, new String[] { searchPlantModel }, stationLine);
			if (!plantModelList.isEmpty()) {

				traverseStructure = plantModelList.get(0);
				UIGetValuesUtility.setViewReference(clientSession, BOMLine, stationLine);
			} else {

				TCComponentItem plantModel = UIGetValuesUtility.findItem(clientSession, searchPlantModel);
				OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(clientSession, plantModel);
				stationLine = createdBOPView[0].context;
				UIGetValuesUtility.setViewReference(clientSession, BOMLine, stationLine);
				traverseStructure = stationLine;
			}
		} else {

			UIGetValuesUtility.setViewReference(clientSession, BOMLine, stationLine);
			traverseStructure = stationLine;
		}

		ArrayList<TCComponent> stationsList = UIGetValuesUtility.searchPartsInStruture(clientSession, new String[] { searchIDs }, traverseStructure);

		if (stationsList.isEmpty() == false) {

			TCComponent[] stationBOMLines = new TCComponent[stationsList.size()];

			for (int i = 0; i < stationsList.size(); i++) {

				stationBOMLines[i] = stationsList.get(i);
			}

			if (stationBOMLines.length != 0) {

				transferToMES = new HashMap<TCComponentItemRevision, ArrayList<HashMap<String, StringBuffer>>>();

				dmCoreService.getProperties(stationBOMLines, new String[] { PropertyDefines.BOM_ITEM_ID, PropertyDefines.BOM_ITEM_REV_ID });

				for (TCComponent plantStation : stationsList) {

					StringBuffer workstationID = new StringBuffer();

					String stationID = plantStation.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);

					String stationRev = plantStation.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID);

					TCComponentBOMLine plantModel = (TCComponentBOMLine) plantStation;

					HashMap<String, StringBuffer> stationDetails = processAddStation(dmCoreService, plantModel, workstationID);

					if (stationDetails == null) {
						PlantReport rp = new PlantReport();
						rp.setNo(Integer.toString(count++));
						rp.setPlantID(String.format("%s/%s", stationID, stationRev));
						rp.setMessage("Wrong workstation architecture.");
						rp.setStation(workstationID.toString());
						rp.setResult("Error");
						rp.setType(UpdateType.UPDATE_BODY_ERROR);
						listReport.add(rp);
					} else {

						if (transferToMES.containsKey(plantModel.getItemRevision())) {
							ArrayList<HashMap<String, StringBuffer>> list = transferToMES.get(plantModel.getItemRevision());
							list.add(stationDetails);
						} else {
							ArrayList<HashMap<String, StringBuffer>> stationList = new ArrayList<HashMap<String, StringBuffer>>();
							stationList.add(stationDetails);
							transferToMES.put(plantModel.getItemRevision(), stationList);
						}
						PlantReport rp = new PlantReport();
						rp.setNo(Integer.toString(count++));
						rp.setPlantID(String.format("%s/%s", stationID, stationRev));
						rp.setMessage("Prepare data successfully");
						rp.setStation(workstationID.toString());
						rp.setResult("Info");
						rp.setType(UpdateType.UPDATE_BODY_INFO);
						listReport.add(rp);
					}

					transferIDs.remove(stationID);
				}
			}

			UIGetValuesUtility.closeAllContext(clientSession, createdBOMViews);
		}
		return transferToMES;
	}

	public HashMap<String, StringBuffer> processAddStation(DataManagementService dmCoreService, TCComponentBOMLine MEStation, StringBuffer workPlaceID) {

		HashMap<String, TCComponent> workStationDetails = getWorkstationInformation(dmCoreService, MEStation);
		HashMap<String, StringBuffer> dataMap = null;
		if (workStationDetails.size() == 4) {

			TCComponent[] components = new TCComponent[workStationDetails.size()];
			int i = 0;
			for (String key : workStationDetails.keySet()) {
				components[i] = workStationDetails.get(key);
				i++;
			}

			dmCoreService.getProperties(components, new String[] { PropertyDefines.BOM_ITEM_REV_NAME });
			dataMap = new HashMap<String, StringBuffer>();

			StringBuffer dataString = new StringBuffer();
			dataString.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			dataString.append("<Plants xmlns=\"VINFAST.Manufacturing.Engineering\">");

			try {
				String plantName = workStationDetails.get("plant").getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_NAME);
				String plantCode = plantName.substring(0, 4);

				dataString.append("<Plant ID=\"" + plantCode.trim() + "\">");
				dataString.append("<Name>" + plantName.trim() + "</Name>");
				dataString.append("<Areas>");

				String ShopName = workStationDetails.get("shop").getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_NAME).trim();
				String ShopCode = plantCode + "_" + ShopName.substring(0, 2).trim();

				dataString.append("<Area ID=\"" + ShopCode + "\">");
				dataString.append("<Name>" + ShopName + "</Name>");
				dataString.append("<ProductionLines>");

				// LINE Code
				String LineName = workStationDetails.get("line").getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_NAME).trim();
				String ProductionLine = ShopCode + LineName.substring(0, 2).trim();
				String LineCode = ProductionLine + "_S".trim();

				dataString.append("<ProductionLine ID=\"" + ProductionLine + "\">");
				dataString.append("<Name>" + LineName + "</Name>");
				dataString.append("<Sections>");
				dataString.append("<Section ID=\"" + LineCode + "\">");
				dataString.append("<Name>" + LineName + "</Name>");

				dataString.append("<Workstations>");
				String workstationCode = "";
				String objectName = workStationDetails.get("workstation").getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_NAME);
				if (objectName.contains("-")) {
					String[] tempArr = objectName.split("-");
					workstationCode = ProductionLine + tempArr[1].substring(0, 2).trim();
				} else {
					workstationCode = ProductionLine + objectName.substring(0, 2).trim();
				}
				dataString.append("<Workstation ID=\"" + workstationCode + "\">");
				dataString.append("<Name>" + workstationCode + "</Name>");
				dataString.append("<DomainID>" + LineName.trim() + "</DomainID>");

				dataString.append("<Workplaces>");

				// Station Codes
				String StationName = workStationDetails.get("workstation").getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_NAME).trim();

				if (StationName.contains("-")) {

					String[] stationname = StationName.split("-");
					workPlaceID.append(ProductionLine + stationname[1].trim());

				} else {
					workPlaceID.append(ProductionLine + StationName);
				}

				dataString.append("<Workplace ID=\"" + workPlaceID + "\">");
				dataString.append("<Name>" + LineName.trim() + "</Name>");
				dataString.append("<DAC>" + StationName.trim() + "</DAC>");
				dataString.append("</Workplace>");

				dataString.append("</Workplaces>");

				String machineID = workStationDetails.get("workstation").getProperty(PropertyDefines.BOM_TOOL_ID);

				dataString.append("<Machines>");
				if (machineID.length() > 0) {
					String[] machines = machineID.split(",");
					for (int inx = 0; inx < machines.length; inx++) {
						dataString.append("<Machine ID=\"" + machines[inx].trim() + "\"/>");
					}
				}
				dataString.append("</Machines>");

				dataString.append("</Workstation>");
				dataString.append("</Workstations>");

				dataString.append("</Section>");
				dataString.append("</Sections>");

				dataString.append("</ProductionLine>");
				dataString.append("</ProductionLines>");

				dataString.append("</Area>");
				dataString.append("</Areas>");

				dataString.append("</Plant>");
				dataString.append("</Plants>");

				if (dataString.length() > 0) {
					dataMap.put(workPlaceID.toString(), dataString);
					return dataMap;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return dataMap;
	}

}
