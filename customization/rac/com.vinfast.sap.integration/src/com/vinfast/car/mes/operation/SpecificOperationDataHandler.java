package com.vinfast.car.mes.operation;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.bom.MEOPData;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SpecificOperationDataHandler {
	public HashMap<TCComponent, MEOPData> objectDetails(BOMManager BOMManager, TCComponent operation, HashMap<TCComponent, TCComponent[]> operationBopData) {

		boolean isError = false;

		TCComponent[] impactedObjects = new TCComponent[1];
		impactedObjects[0] = operation;

		HashMap<TCComponent, MEOPData> objectDetails = new HashMap<TCComponent, MEOPData>();

		try {

			HashMap<TCComponent, TCComponent[]> childLines = operationBopData;

			DataManagementService dataManagementService = BOMManager.getDataManagementService();

			String[] properties = new String[15];
			properties[0] = PropertyDefines.BOM_ITEM_ID;
			properties[1] = PropertyDefines.BOM_NAME;
			properties[2] = PropertyDefines.BOM_OPERATION_TYPE;
			properties[3] = PropertyDefines.BOM_HW_VERSION;
			properties[4] = PropertyDefines.BOM_SW_VERSION;
			properties[5] = PropertyDefines.BOM_HW_NUMBER;
			properties[6] = PropertyDefines.BOM_SW_NUMBER;
			properties[7] = PropertyDefines.BOM_KEYDATA;
			properties[8] = PropertyDefines.BOM_SCOPE_FLOW;
			properties[9] = PropertyDefines.BOM_PROGRAM_ID;
			properties[10] = PropertyDefines.BOM_REF_COMP;
			properties[11] = PropertyDefines.BOM_PREDECESSOR_ID;
			properties[12] = PropertyDefines.BOM_REV_ID;
			properties[13] = PropertyDefines.REV_AGINGTIME;
			properties[14] = PropertyDefines.REV_WORKPLACE_SEQ;

			dataManagementService.getProperties(impactedObjects, properties);

			for (TCComponent lineObject : impactedObjects) {

				MEOPData operationMap = null;
				TCComponentBOMLine object = (TCComponentBOMLine) lineObject;
				TCComponentItemRevision objectRevision = object.getItemRevision();

				String operationID = object.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
				String operationRevID = object.getPropertyDisplayableValue(PropertyDefines.BOM_REV_ID);
				String operationType = object.getPropertyDisplayableValue(PropertyDefines.BOM_OPERATION_TYPE);

				if (operationType.equals("")) {

					String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), operationID + "/" + operationRevID, "PLM: Operation Type value not filled. Please fill Operation Type.", "-", "-", "Error" };
					BOMManager.printReport("PRINT", printValues);

				} else if (operationType.equals("NA")) {

					String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), operationID + "/" + operationRevID, "PLM: NA operations not required to send to MES.", operationType, "-", "Info" };
					BOMManager.printReport("PRINT", printValues);
					UIGetValuesUtility.setProperty(dataManagementService, objectRevision, PropertyDefines.REV_TO_MES, "NA");

				} else {

					String[] WSID = object.getItemRevision().getProperty(PropertyDefines.REV_TO_MES).split("~");

					if (WSID.length == 0) {

						WSID = object.getItemRevision().getProperty(PropertyDefines.REV_TO_SAP).split("~");
					}

					String workstationID = WSID[0];

					if (workstationID.length() != 0) {

						ArrayList<String> materials = new ArrayList<String>();
						ArrayList<String> tools = new ArrayList<String>();

						operationMap = new MEOPData();
						operationMap.setShop("???");// TODO: use the new BOMManager
						operationMap.setMEOPID(operationID);
						operationMap.setMEOPRevID(operationRevID);
						operationMap.setMEOPName(object.getPropertyDisplayableValue(PropertyDefines.BOM_NAME));
						operationMap.setMEOPType(operationType);
						operationMap.setHWVersion(object.getPropertyDisplayableValue(PropertyDefines.BOM_HW_VERSION));
						operationMap.setSWVersion(object.getPropertyDisplayableValue(PropertyDefines.BOM_SW_VERSION));
						operationMap.setHWPartNumber(object.getPropertyDisplayableValue(PropertyDefines.BOM_HW_NUMBER));
						operationMap.setSWPartNumber(object.getPropertyDisplayableValue(PropertyDefines.BOM_SW_NUMBER));
						operationMap.setKeyData(object.getPropertyDisplayableValue(PropertyDefines.BOM_KEYDATA));
						operationMap.setReferenceComponent(object.getPropertyDisplayableValue(PropertyDefines.BOM_REF_COMP));
						operationMap.setProgramID(object.getPropertyDisplayableValue(PropertyDefines.BOM_PROGRAM_ID));
						operationMap.setWorkStationID(workstationID);
						operationMap.setMHU(objectRevision.getProperty(PropertyDefines.REV_ISMHU));
						operationMap.setIsVcu(objectRevision.getProperty(PropertyDefines.REV_ISVCU));
						operationMap.setWorkplaceSequence(objectRevision.getProperty(PropertyDefines.REV_WORKPLACE_SEQ));
						operationMap.setAgingTime(objectRevision.getProperty(PropertyDefines.REV_AGINGTIME));
						String predecessorValue = object.getProperty(PropertyDefines.BOM_PREDECESSOR_ID);
						if (!predecessorValue.isEmpty()) {
							if (predecessorValue.contains(";")) {
								for (String str : predecessorValue.split(";")) {
									if (!str.isEmpty())
										operationMap.setPredecessor(str);
								}
							} else {
								operationMap.setPredecessor(predecessorValue);
							}
						}

						TCComponent[] BOMLines = null;
						if (childLines != null) {

							String[] childProperties = new String[5];
							childProperties[0] = PropertyDefines.BOM_ITEM_ID;
							childProperties[1] = PropertyDefines.BOM_OBJECT_TYPE;
							childProperties[2] = PropertyDefines.BOM_GM_NUMBER;
							childProperties[3] = PropertyDefines.BOM_TOOL_ID;
							childProperties[4] = PropertyDefines.BOM_ITEM_REV_ID;

							BOMLines = childLines.get(object);

							if (BOMLines != null) {

								dataManagementService.getProperties(BOMLines, childProperties);

								for (TCComponent childComponent : BOMLines) {

									HashMap<String, String> materialDetails = new HashMap<String, String>();

									TCComponentBOMLine bomline = (TCComponentBOMLine) childComponent;

									TCComponentItemRevision revision = bomline.getItemRevision();

									String[] revProperties = new String[5];
									revProperties[0] = PropertyDefines.REV_DID;
									revProperties[1] = PropertyDefines.REV_FILE;
									revProperties[2] = PropertyDefines.REV_TYPE;
									revProperties[3] = PropertyDefines.REV_VERISON;
									revProperties[4] = PropertyDefines.REV_PROGRAM_DATE;

									dataManagementService.getProperties(new TCComponent[] { revision }, revProperties);

									materialDetails.put("MATERIALID", bomline.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));
									materialDetails.put("MATERIALREV", bomline.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID));
									materialDetails.put(PropertyDefines.REV_DID, revision.getPropertyDisplayableValue(PropertyDefines.REV_DID));
									materialDetails.put(PropertyDefines.REV_FILE, revision.getPropertyDisplayableValue(PropertyDefines.REV_FILE));
									materialDetails.put(PropertyDefines.REV_TYPE, bomline.getPropertyDisplayableValue(PropertyDefines.REV_TYPE));
									materialDetails.put(PropertyDefines.REV_VERISON, revision.getPropertyDisplayableValue(PropertyDefines.REV_VERISON));
									materialDetails.put(PropertyDefines.REV_PROGRAM_DATE, revision.getPropertyDisplayableValue(PropertyDefines.REV_PROGRAM_DATE));

									operationMap.setMaterialDetails(materialDetails);

									String object_Type = childComponent.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE);

									if (object_Type.equals(PropertyDefines.TYPE_ACAR_REVISION)) {

										String GMPartNumber = childComponent.getPropertyDisplayableValue(PropertyDefines.BOM_GM_NUMBER);

										if (!GMPartNumber.isEmpty()) {

											materials.add(GMPartNumber);

										} else {

											materials.add(childComponent.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));
										}

									} else if (object_Type.equals(PropertyDefines.TYPE_MECNTOOL_REVISION)) {

										String toolID = childComponent.getPropertyDisplayableValue(PropertyDefines.BOM_TOOL_ID);

										if (!toolID.isEmpty()) {
											tools.add(toolID);
										}

									} else {

										materials.add(childComponent.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID));
									}
								}
							}
						}

						operationMap.setMaterialList(materials);

						operationMap.setToolList(tools);

						if (isError == false) {
							objectDetails.put(objectRevision, operationMap);
						}

					} else {

						String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), operationID + "/" + operationRevID, "PLM: Error in generating workstation ID.", operationType, "-", "Error" };
						BOMManager.printReport("PRINT", printValues);
					}
				}
			}

			if (isError) {
				objectDetails = null;
			}
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objectDetails;
	}

}
