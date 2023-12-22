package com.vinfast.scooter.mes.operation;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.integration.ulti.BOMManagement;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class ScooterObjectsAndDetailsV2 {
	public HashMap<TCComponent, MEOPDataAbstract> objectDetails(BOMManagement BOMManager, TCComponent[] impactedObjects, DataManagementService dataManagementService, TCSession session) {
		boolean isError = false;
		HashMap<String, ArrayList<TCComponent>> duplicateMEOP = new HashMap<String, ArrayList<TCComponent>>();

		if (impactedObjects.length != 0) {
			for (TCComponent impactedObject : impactedObjects) {
				try {
					String ID = impactedObject.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
					if (duplicateMEOP.containsKey(ID)) {
						ArrayList<TCComponent> meopList = duplicateMEOP.get(ID);
						meopList.add(impactedObject);
					} else {
						ArrayList<TCComponent> meopList = new ArrayList<TCComponent>();
						meopList.add(impactedObject);
						duplicateMEOP.put(ID, meopList);
					}
				} catch (NotLoadedException e) {
					e.printStackTrace();
				}
			}

			impactedObjects = new TCComponent[duplicateMEOP.size()];
			int count = 0;
			for (String key : duplicateMEOP.keySet()) {
				impactedObjects[count] = duplicateMEOP.get(key).get(0);
				count++;
			}
		}

		HashMap<TCComponent, MEOPDataAbstract> objectDetails = new HashMap<TCComponent, MEOPDataAbstract>();
		try {
			HashMap<TCComponent, TCComponent[]> childLines = UIGetValuesUtility.expandBOMOneLevel(session, impactedObjects);
			for (TCComponent lineObject : impactedObjects) {
				MEOPDataAbstract operationMap = null;
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
					ArrayList<TCComponent> meopList = duplicateMEOP.get(operationID);
					String workstationID = "";

					for (TCComponent meop : meopList) {
						TCComponentBOMLine meopLine = (TCComponentBOMLine) meop;
						if (workstationID.equals("")) {
							workstationID = UIGetValuesUtility.getWorkStationID(meopLine, PropertyDefines.BOM_NAME);
						} else {
							workstationID = workstationID + "," + UIGetValuesUtility.getWorkStationID(meopLine, PropertyDefines.BOM_NAME);
						}
					}

					if (workstationID.length() != 0) {
						ArrayList<String> materials = new ArrayList<String>();
						ArrayList<String> tools = new ArrayList<String>();

						MEOPDataFactory.getInstance();
						operationMap = MEOPDataFactory.getMEOPData(operationType);
						operationMap.setMEOPID(operationID);
						operationMap.setMEOPRevID(operationRevID);
						operationMap.setMEOPName(object.getPropertyDisplayableValue(PropertyDefines.BOM_NAME));
						operationMap.setMEOPType(operationType);
						operationMap.setHwVersion(object.getPropertyDisplayableValue(PropertyDefines.BOM_HW_VERSION));
						operationMap.setSwVersion(object.getPropertyDisplayableValue(PropertyDefines.BOM_SW_VERSION));
						operationMap.setHwPartNumber(object.getPropertyDisplayableValue(PropertyDefines.BOM_HW_NUMBER));
						operationMap.setSwPartNumber(object.getPropertyDisplayableValue(PropertyDefines.BOM_SW_NUMBER));
						operationMap.setKeyData(object.getPropertyDisplayableValue(PropertyDefines.BOM_KEYDATA));
						operationMap.setReferenceComponent(object.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID));
						operationMap.setProgramID(object.getPropertyDisplayableValue(PropertyDefines.BOM_PROGRAM_ID));
						operationMap.setWorkStationID(workstationID);
						operationMap.setIsMHU(objectRevision.getProperty(PropertyDefines.REV_ISMHU));
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

						if (childLines != null) {
							TCComponent[] BOMLines = childLines.get(object);
							operationMap.setChildItems(BOMLines);
							if (BOMLines != null) {
								for (TCComponent childComponent : BOMLines) {
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

						StringBuilder error = new StringBuilder();
						if (operationMap.isValidOperation().isEmpty()) {
							objectDetails.put(objectRevision, operationMap);
						} else {
							isError = true;
							String[] response = new String[] { Integer.toString(BOMManager.getSerialNo()), operationID + "/" + operationRevID, error.toString(), operationType, "-", "Error" };
							BOMManager.printReport("PRINT", response);
						}
					} else {
						String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), operationID + "/" + operationRevID, "PLM: Error in generating workstation ID.", operationType, "-", "Error" };
						BOMManager.printReport("PRINT", printValues);
					}
				}
			}
			if (isError) {
				return null;
			}
		} catch (TCException e1) {
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		return objectDetails;
	}
}
