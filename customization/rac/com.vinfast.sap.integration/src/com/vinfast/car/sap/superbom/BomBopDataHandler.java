package com.vinfast.car.sap.superbom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.teamcenter.integration.model.MaterialTransferModel;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.bom.BOMBOPData;
import com.vinfast.sap.bom.BOMBOPData.ActionType;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class BomBopDataHandler {
	public HashMap<TCComponent, HashMap<String, BOMBOPData>> loadBOMBOPData(BOMManager BOMManager, HashMap<TCComponent, ArrayList<BOMBOPData>> dataMap) {
		HashMap<TCComponent, HashMap<String, BOMBOPData>> allGroupDataMap = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();
		try {
			DataManagementService dataManagementService = BOMManager.getDataManagementService();
			String[] bomProperties = new String[6];
			bomProperties[0] = PropertyDefines.BOM_ITEM_ID;
			bomProperties[1] = PropertyDefines.BOM_BOM_ID;
			bomProperties[2] = PropertyDefines.BOM_QUANTITY;
			bomProperties[3] = PropertyDefines.BOM_FORMULA;
			bomProperties[4] = PropertyDefines.BOM_DCR_NUMBER;
			bomProperties[5] = PropertyDefines.BOM_PARENT;

			String[] bopProperties = new String[3];
			bopProperties[0] = PropertyDefines.BOM_FAMILY_ADDR;
			bopProperties[1] = PropertyDefines.BOM_LRHAND;
			bopProperties[2] = PropertyDefines.BOM_PARENT;

			for (TCComponent BOMLine : dataMap.keySet()) {
				TCComponentBOMLine BOMLineObject = (TCComponentBOMLine) BOMLine;
				TCComponentItemRevision BOMLineRevision = BOMLineObject.getItemRevision();
				HashMap<String, BOMBOPData> subGroupDataMap = null;
				ArrayList<BOMBOPData> subGroupData = dataMap.get(BOMLineObject);
				if (subGroupData.isEmpty()) {
					if (BOMLineRevision.getType().equals(PropertyDefines.TYPE_OPERATION_REVISION)) {
						BOMManager.setOperationNoPart(BOMLineObject);
					} else {
						subGroupDataMap = new HashMap<String, BOMBOPData>();
					}
				} else {
					subGroupDataMap = new HashMap<String, BOMBOPData>();

					if (BOMLineRevision.getType().equals(PropertyDefines.TYPE_OPERATION_REVISION) == false) {
						BOMLineObject.getProperty(PropertyDefines.BOM_ITEM_ID);
					}

					for (BOMBOPData data : subGroupData) {
						TCComponent bomLine = data.getMBOMLine();
						TCComponent[] bopLines = data.getMBOPLine();
						dataManagementService.getProperties(new TCComponent[] { bomLine }, bomProperties);
						dataManagementService.getProperties(bopLines, bopProperties);

						String partNumber = MaterialTransferModel.generateMaterialID(((TCComponentBOMLine) bomLine).getItemRevision());
						data.setPartNumber(partNumber);

						String platform = BOMManager.getModel();
						data.setPlatform(platform);

						String modelyear = BOMManager.getYear();
						data.setModelYear(modelyear);

						String plant = BOMManager.getPlant();
						data.setPlantCode(plant);

						String maingroup = BOMManager.getShopName();
						data.setMainGroup(maingroup);

						TCComponentBOMLine parentLine = (TCComponentBOMLine) bomLine.getReferenceProperty(PropertyDefines.BOM_PARENT);
						data.setSubGroup(parentLine.getProperty(PropertyDefines.BOM_ITEM_ID));

						TCComponentItemRevision subGroupRevision = parentLine.getItemRevision();
						String subGroupStatusList = subGroupRevision.getProperty(PropertyDefines.ITEM_RELEASE_STATUS_LIST);
						data.setSubGroupReleased(subGroupStatusList.length() > 0);

						String groupdescription = BOMManager.getShopName();
						data.setDescription(groupdescription);

						String bomlineid = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID).trim();
						if (bomlineid.length() != 0) {
							for (int i = bomlineid.length(); i < 4; i++) {
								bomlineid = "0" + bomlineid;
							}
						}
						data.setBOMLineID(bomlineid);

						String quantity = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_QUANTITY).trim();
						if (quantity.length() == 0) {
							quantity = "1.000";
						}
						data.setQuanity(quantity);

						String option = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_FORMULA);
						if (option.equals("") == false) {

							option = option.replaceAll("\\&", "AND");
							option = option.replaceAll("\\|", "OR");
							//option = option.replaceAll("!=", "<>");

							Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(option);
							while (m.find()) {
								option = option.replace(m.group(1), BOMManager.getModel() + "_" + BOMManager.getYear());
							}
						}
						data.setFormula(option);

						String dcr = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_DCR_NUMBER);
						data.setDCR(dcr);

						String mcn = BOMManager.getMCN();
						data.setMCN(mcn);

						String sequence = UIGetValuesUtility.getSequenceID();
						data.setSequenceTime(sequence);

						data.setAction("A");

						for (TCComponent bopLine : bopLines) {

							String family_addr = bopLine.getPropertyDisplayableValue(PropertyDefines.BOM_FAMILY_ADDR).trim();
							data.setFamilyAddress(family_addr);

							String l_r_hand = bopLine.getPropertyDisplayableValue(PropertyDefines.BOM_LRHAND).trim();
							data.setLeftRightHand(l_r_hand);

							TCComponentBOMLine operation = (TCComponentBOMLine) bopLine.getReferenceProperty(PropertyDefines.BOM_PARENT);

							if (operation.getItemRevision().getType().equals(PropertyDefines.TYPE_OPERATION_REVISION)) {

								String[] operationProperties = new String[6];
								operationProperties[0] = PropertyDefines.BOM_NAME;
								operationProperties[1] = PropertyDefines.BOM_ITEM_ID;
								operationProperties[2] = PropertyDefines.BOM_LINE_SUP_METHOD;
								operationProperties[3] = PropertyDefines.BOM_OPERATION_TYPE;
								operationProperties[4] = PropertyDefines.BOM_REV_ID;
								operationProperties[5] = PropertyDefines.BOM_TRANSFER_TO_MES;
								dataManagementService.getProperties(new TCComponent[] { operation }, operationProperties);
								StringBuffer stationID = new StringBuffer();
								boolean workStationStatusList[] = new boolean[1];
								String workstation = this.getWorkStationID(operation, stationID, workStationStatusList);

								String WSID = operation.getPropertyDisplayableValue(PropertyDefines.BOM_TRANSFER_TO_MES);
								String operationID = operation.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
								if (WSID.isEmpty() && CarSuperBomBop.isOperationInMCN(operationID)) {
									String operationRevisionID = operation.getPropertyDisplayableValue(PropertyDefines.BOM_REV_ID);
									String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), maingroup, partNumber, bomlineid, "Please first transfer operation [" + operationID + "/" + operationRevisionID + "] to MES and then transfer BOM/BOP to SAP", "-", "Error" };
									BOMManager.printReport("PRINT", printValues);
								}
								if (workstation.length() < 13) {

									String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), maingroup, partNumber, bomlineid, "WorkStation ID is incorrect. Please correct it and retry transfer", "-", "Error" };
									BOMManager.printReport("PRINT", printValues);
								}
								data.setWorkStationReleased(workStationStatusList[0]);

								data.setWorkStation(workstation);
								data.setStationID(stationID.toString());

								String linesupplymethod = operation.getPropertyDisplayableValue(PropertyDefines.BOM_LINE_SUP_METHOD);
								if (linesupplymethod.equals("")) {

									linesupplymethod = "JIS";
								}
								data.setLineSupplyMethod(linesupplymethod);
								data.setBOPID(operationID);

								String mesbopindicator = operation.getPropertyDisplayableValue(PropertyDefines.BOM_OPERATION_TYPE);
								if (mesbopindicator.equals("") || mesbopindicator.equalsIgnoreCase("NA") || mesbopindicator.equalsIgnoreCase("Automatic Consumption")) {
									mesbopindicator = "N";
								} else {
									mesbopindicator = "Y";
								}

								data.setMESIndicator(mesbopindicator);
								String operationRevisionID = operation.getPropertyDisplayableValue(PropertyDefines.BOM_REV_ID);
								data.setBOPRevision(operationRevisionID);

								TCComponentItemRevision operationRevision = operation.getItemRevision();
								String operationStatusList = operationRevision.getProperty(PropertyDefines.ITEM_RELEASE_STATUS_LIST);
								data.setOperationReleased(operationStatusList.length() > 0);
							}
						}

						subGroupDataMap.put(data.getMainGroup() + "~" + data.getSubGroup() + "~" + data.getPartNumber() + "~" + data.getBOMLineID(), data);
					}
				}

				allGroupDataMap.put(BOMLineRevision, subGroupDataMap);
			}
		} catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return allGroupDataMap;
	}

	public HashMap<String, BOMBOPData> loadBOPData(BOMManager BOMManager, TCComponentBOMLine bomLine, boolean isDelete) {
		DataManagementService dataManagementService = BOMManager.getDataManagementService();
		BOMBOPData BOPData = new BOMBOPData();
		try {
			BOPData.setPartNumber("");

			String platform = BOMManager.getModel();
			BOPData.setPlatform(platform);

			String modelyear = BOMManager.getYear();
			BOPData.setModelYear(modelyear);

			String plant = BOMManager.getPlant();
			BOPData.setPlantCode(plant);

			BOPData.setMainGroup("");
			BOPData.setSubGroup("");
			BOPData.setDescription("");
			BOPData.setBOMLineID("");
			BOPData.setQuanity("");
			BOPData.setFormula("");
			BOPData.setDCR("");

			String mcn = BOMManager.getMCN();
			BOPData.setMCN(mcn);

			String sequence = UIGetValuesUtility.getSequenceID();
			BOPData.setSequenceTime(sequence);

			BOPData.setFamilyAddress("");

			BOPData.setLeftRightHand("");

			if (bomLine.getItemRevision().getType().equals(PropertyDefines.TYPE_OPERATION_REVISION)) {
				String[] operationProperties = new String[5];
				operationProperties[0] = PropertyDefines.BOM_NAME;
				operationProperties[1] = PropertyDefines.BOM_ITEM_ID;
				operationProperties[2] = PropertyDefines.BOM_LINE_SUP_METHOD;
				operationProperties[3] = PropertyDefines.BOM_OPERATION_TYPE;
				operationProperties[4] = PropertyDefines.BOM_REV_ID;

				dataManagementService.getProperties(new TCComponent[] { bomLine }, operationProperties);
				if (isDelete == true) {
					BOPData.processDataOnActionType(ActionType.DELETE_OPERATION_NO_PART);
				} else {
					BOPData.processDataOnActionType(ActionType.ADD_OPERATION_NO_PART);
				}

				StringBuffer stationID = new StringBuffer();
				boolean workStationStatusList[] = new boolean[1];
				String workstation = this.getWorkStationID(bomLine, stationID, workStationStatusList);
				if (workstation.length() < 13) {

					String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), "OWP", bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID) + "/" + bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_REV_ID), "", "WorkStation ID is incorrect. Please correct it and retry transfer", "-", "Error" };
					BOMManager.printReport("PRINT", printValues);
				}
				BOPData.setWorkStationReleased(workStationStatusList[0]);
				BOPData.setWorkStation(workstation);
				BOPData.setStationID(stationID.toString());

				String linesupplymethod = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_LINE_SUP_METHOD);
				if (linesupplymethod.equals("")) {
					linesupplymethod = "JIS";
				}
				BOPData.setLineSupplyMethod(linesupplymethod);

				BOPData.setBOPID(bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));

				String mesbopindicator = bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_OPERATION_TYPE);
				if (mesbopindicator.equals("") || mesbopindicator.equalsIgnoreCase("NA")) {
					mesbopindicator = "N";
				} else {
					mesbopindicator = "Y";
				}

				BOPData.setMESIndicator(mesbopindicator);
				BOPData.setBOPRevision(bomLine.getPropertyDisplayableValue(PropertyDefines.BOM_REV_ID));
				String operationStatusList = bomLine.getProperty(PropertyDefines.ITEM_RELEASE_STATUS_LIST);
				BOPData.setOperationReleased(operationStatusList.length() > 0);
			}

		} catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (TCException e) {
			e.printStackTrace();
		}

		HashMap<String, BOMBOPData> ret = new HashMap<String, BOMBOPData>();
		ret.put(BOPData.getMainGroup() + "~" + BOPData.getSubGroup() + "~" + BOPData.getPartNumber() + "~" + BOPData.getBOMLineID(), BOPData);
		return ret;
	}

	/**
	 * 
	 * @param operation
	 * @param stationRevision: for checking with stationRev in solution folder in
	 *                         case station need to be released
	 * @return
	 */
	public String getWorkStationID(TCComponentBOMLine operation, StringBuffer stationID, boolean[] workStationRelease) {
		// TODO Auto-generated method stub
		String workstationID = "";
		TCComponentBOMLine procLine = null;
		TCComponentBOMLine shopLine = null;
		TCComponentBOMLine topLine = null;
		try {

			TCComponentItemRevision operationRevision = operation.getItemRevision();
			if (operationRevision.getType().equals("MEOPRevision")) {

				TCComponentBOMLine workstation = (TCComponentBOMLine) operation.getReferenceProperty("bl_parent");
				TCComponentItemRevision workstationRevision = workstation.getItemRevision();
				String workStationStatusList = workstationRevision.getProperty(PropertyDefines.ITEM_RELEASE_STATUS_LIST);
				workStationRelease[0] = workStationStatusList.length() > 0;
				if (workstationRevision.getType().equals("Mfg0MEProcStatnRevision")) {

					String workStationName = workstation.getProperty(PropertyDefines.BOM_NAME);// PT-01LH
					String workStatName = "";
					if (workStationName.contains("-")) {

						String split_StationName[] = workStationName.split("-");
						workStatName = split_StationName[1].trim();// 01LH
					} else {
						workStatName = workStationName;
					}
					procLine = (TCComponentBOMLine) workstation.getReferenceProperty("bl_parent");
					String procLineName = procLine.getProperty(PropertyDefines.BOM_NAME).substring(0, 2).trim();// PT

					String shopName = "";
					String plantName = "";
					shopLine = (TCComponentBOMLine) procLine.getReferenceProperty("bl_parent");// PS

					if (shopLine.getItemRevision().getType().equals("Mfg0MEPlantBOPRevision")) {
						shopName = procLineName;
						String shopLineName = shopLine.getProperty(PropertyDefines.BOM_NAME);
						if (shopLineName.length() >= 4)
							plantName = shopLineName.substring(0, 4).trim();
						else
							System.out.println(shopLineName);
					} else {
						shopName = shopLine.getProperty(PropertyDefines.BOM_NAME).substring(0, 2).trim();
						topLine = (TCComponentBOMLine) shopLine.getReferenceProperty("bl_parent");
						String topLineName = topLine.getProperty(PropertyDefines.BOM_NAME);
						if (topLineName.length() >= 4)
							plantName = topLineName.substring(0, 4).trim();
						else
							System.out.println(topLineName);
					}

					workstationID = plantName + "_" + shopName + procLineName + workStatName;
					stationID.append(workstation.getProperty(PropertyDefines.BOM_ITEM_ID));
				}

			}

		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return workstationID;
	}
}
