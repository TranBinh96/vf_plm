package com.vinfast.car.sap.superbom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.vinfast.sap.bom.BOMBOPData;
import com.vinfast.sap.bom.BOMBOPData.ActionType;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.util.PropertyDefines;

public class CarSuperBomBopBusiness {
	private HashMap<TCComponent, HashMap<String, BOMBOPData>> workingBOMValues = new HashMap<TCComponent, HashMap<String, BOMBOPData>>(); // <<MainGroup+SubGroup+PartNumber+BOMLineID, BOMBOPData> >
	private HashMap<TCComponent, HashMap<String, BOMBOPData>> releaseBOMValues = new HashMap<TCComponent, HashMap<String, BOMBOPData>>(); // <<MainGroup+SubGroup+PartNumber+BOMLineID, BOMBOPData> >
	private HashMap<TCComponent, HashMap<String, BOMBOPData>> workingBOPValues = new HashMap<TCComponent, HashMap<String, BOMBOPData>>(); // <<MainGroup+SubGroup+PartNumber+BOMLineID, BOMBOPData> >
	private HashMap<TCComponent, HashMap<String, BOMBOPData>> releaseBOPValues = new HashMap<TCComponent, HashMap<String, BOMBOPData>>(); // <<MainGroup+SubGroup+PartNumber+BOMLineID, BOMBOPData> >
	private HashSet<String> deleteBopKey = new HashSet<String>();
	private BomTrackingRawDataHandler bomTracking = null;
	private HashSet<String> itemNeedToBeRelease = new HashSet<String>();
	private HashMap<TCComponent, HashMap<String, BOMBOPData>> OWPBOPValues = null;

	BOMManager bomManager = null;

	public void setBomManager(BOMManager bomManager) {
		this.bomManager = bomManager;
	}

	public StringBuffer getBomTrackingData() {
		return bomTracking.getBomTrackingFile();
	}

	int count = 1;

	ArrayList<SuperBomBopTransferData> data2Send = new ArrayList<SuperBomBopTransferData>();

	public HashMap<TCComponent, HashMap<String, BOMBOPData>> getWorkingBOMValues() {
		return workingBOMValues;
	}

	public void setWorkingBOMValues(HashMap<TCComponent, HashMap<String, BOMBOPData>> workingBOMValues) {
		if (workingBOMValues != null) {
			this.workingBOMValues = workingBOMValues;
		}
	}

	public HashMap<TCComponent, HashMap<String, BOMBOPData>> getReleaseBOMValues() {
		return releaseBOMValues;
	}

	public void setReleaseBOMValues(HashMap<TCComponent, HashMap<String, BOMBOPData>> releaseBOMValues) {
		if (releaseBOMValues != null) {
			this.releaseBOMValues = releaseBOMValues;
		}
	}

	public HashMap<TCComponent, HashMap<String, BOMBOPData>> getWorkingBOPValues() {
		return workingBOPValues;
	}

	public void setWorkingBOPValues(HashMap<TCComponent, HashMap<String, BOMBOPData>> workingBOPValues) {
		if (workingBOPValues != null) {
			this.workingBOPValues = workingBOPValues;
		}
	}

	public HashMap<TCComponent, HashMap<String, BOMBOPData>> getReleaseBOPValues() {
		return releaseBOPValues;
	}

	public void setReleaseBOPValues(HashMap<TCComponent, HashMap<String, BOMBOPData>> releaseBOPValues) {
		if (releaseBOPValues != null) {
			this.releaseBOPValues = releaseBOPValues;
		}
	}

	public HashMap<TCComponent, HashMap<String, BOMBOPData>> getOWPBOPValues() {
		return OWPBOPValues;
	}

	public void setOWPBOPValues(HashMap<TCComponent, HashMap<String, BOMBOPData>> oWPBOPValues) {
		OWPBOPValues = oWPBOPValues;
	}

	public ArrayList<SuperBomBopTransferData> getData2Send() {
		return data2Send;
	}

	public boolean process() {
		bomTracking = new BomTrackingRawDataHandler();
		count = 1;
		workOnSubGroup();
		workOnOperationHavingPart();
		workOnOperationNoPart();
		validateItemNeedToBeRelease();
		return true;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, BOMBOPData> deletedBOMs(HashMap<String, BOMBOPData> releaseBOM, HashMap<String, BOMBOPData> workingBOM) {
		if (workingBOM == null) {
			return releaseBOM;
		}
		HashMap<String, BOMBOPData> oldBOM = (HashMap<String, BOMBOPData>) releaseBOM.clone();
		HashMap<String, BOMBOPData> newBOM = (HashMap<String, BOMBOPData>) workingBOM.clone();
		Set<String> keyNewBOM = newBOM.keySet();
		oldBOM.keySet().removeAll(keyNewBOM);
		return oldBOM;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, BOMBOPData> addBOMBOPs(HashMap<String, BOMBOPData> releaseBOM, HashMap<String, BOMBOPData> workingBOM) {
		if (releaseBOM == null) {
			return workingBOM;
		}
		HashMap<String, BOMBOPData> oldBOM = (HashMap<String, BOMBOPData>) releaseBOM.clone();
		HashMap<String, BOMBOPData> newBOM = (HashMap<String, BOMBOPData>) workingBOM.clone();
		Set<String> keyOldBOM = oldBOM.keySet();
		newBOM.keySet().removeAll(keyOldBOM);
		return newBOM;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, BOMBOPData> changedBOMLines(HashMap<String, BOMBOPData> releaseBOM, HashMap<String, BOMBOPData> workingBOM) {
		HashMap<String, BOMBOPData> addSTBOM = (HashMap<String, BOMBOPData>) workingBOM.clone();
		HashMap<String, BOMBOPData> addSTPart = (HashMap<String, BOMBOPData>) releaseBOM.clone();
		HashMap<String, BOMBOPData> common_Parts = new HashMap<String, BOMBOPData>();
		Set<String> addBOM = addSTBOM.keySet();
		Set<String> addST = addSTPart.keySet();
		addBOM.retainAll(addST);

		for (String key : addSTBOM.keySet()) {
			try {
				BOMBOPData solutionItem = addSTBOM.get(key);
				BOMBOPData problemItem = addSTPart.get(key);

				String solQty = solutionItem.getQuanity();
				String prbQty = problemItem.getQuanity();

				float solq = Float.parseFloat(solQty);
				float prbq = Float.parseFloat(prbQty);

				String solOption = solutionItem.getFormula();
				String prbOption = problemItem.getFormula();

				if ((solq == prbq && solOption.equals(prbOption)) == false) {
					common_Parts.put(key, solutionItem);
				}
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return common_Parts;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, BOMBOPData> moveBetweenStations(HashMap<String, BOMBOPData> releaseBOP, HashMap<String, BOMBOPData> workingBOP) {
		if (releaseBOP == null || workingBOP == null) {
			return new HashMap<String, BOMBOPData>();
		}
		HashMap<String, BOMBOPData> oldBOP = (HashMap<String, BOMBOPData>) releaseBOP.clone();
		HashMap<String, BOMBOPData> newBOP = (HashMap<String, BOMBOPData>) workingBOP.clone();
		HashMap<String, BOMBOPData> differentWorkStation = new HashMap<String, BOMBOPData>();

		for (String key : newBOP.keySet()) {
			try {
				BOMBOPData newLine = newBOP.get(key);
				BOMBOPData oldLine = oldBOP.get(key);

				if (oldLine != null) {
					String newLineOperation = newLine.getBOPID();
					String oldLineOperation = oldLine.getBOPID();

					String newLineWS = newLine.getWorkStation();
					String oldLineWS = oldLine.getWorkStation();

					String newLineMESInd = newLine.getMESIndicator();
					String oldLineMESInd = oldLine.getMESIndicator();

					String newLineFD = newLine.getFamilyAddress();
					String oldLineFD = oldLine.getFamilyAddress();

					if ((newLineOperation.equals(oldLineOperation)) == false || (newLineWS.equals(oldLineWS)) == false || (newLineMESInd.equals(oldLineMESInd)) == false || (newLineFD.equals(oldLineFD)) == false) {
						differentWorkStation.put(key, newLine);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return differentWorkStation;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, BOMBOPData> addedPartsInOperation(HashMap<String, BOMBOPData> releaseBOP, HashMap<String, BOMBOPData> workingBOP) {
		if (workingBOP == null) {
			return new HashMap<String, BOMBOPData>();
		}
		if (releaseBOP == null) {
			return workingBOP;
		}
		HashMap<String, BOMBOPData> oldBOP = (HashMap<String, BOMBOPData>) releaseBOP.clone();
		HashMap<String, BOMBOPData> newBOP = (HashMap<String, BOMBOPData>) workingBOP.clone();
		Set<String> keyOldBOP = oldBOP.keySet();
		newBOP.keySet().removeAll(keyOldBOP);
		return newBOP;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, BOMBOPData> deletedPartsInOperation(HashMap<String, BOMBOPData> releaseBOP, HashMap<String, BOMBOPData> workingBOP) {
		if (releaseBOP == null) {
			return new HashMap<String, BOMBOPData>();
		}
		if (workingBOP == null) {
			return releaseBOP;
		}
		HashMap<String, BOMBOPData> oldBOP = (HashMap<String, BOMBOPData>) releaseBOP.clone();
		HashMap<String, BOMBOPData> newBOP = (HashMap<String, BOMBOPData>) workingBOP.clone();
		Set<String> keyNewBOP = newBOP.keySet();
		oldBOP.keySet().removeAll(keyNewBOP);
		return oldBOP;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, BOMBOPData> changeInOperationRevisionOnly(HashMap<String, BOMBOPData> releaseBOP, HashMap<String, BOMBOPData> workingBOP) {
		if (releaseBOP == null || workingBOP == null) {
			return new HashMap<String, BOMBOPData>();
		}
		HashMap<String, BOMBOPData> oldBOP = (HashMap<String, BOMBOPData>) releaseBOP.clone();
		HashMap<String, BOMBOPData> newBOP = (HashMap<String, BOMBOPData>) workingBOP.clone();
		HashMap<String, BOMBOPData> differentOperationRevision = new HashMap<String, BOMBOPData>();

		for (String key : newBOP.keySet()) {
			try {
				BOMBOPData newLine = newBOP.get(key);
				BOMBOPData oldLine = oldBOP.get(key);
				if (oldLine != null) {
					String newLineOperation = newLine.getBOPID();
					String oldLineOperation = oldLine.getBOPID();

					String newLineWS = newLine.getWorkStation();
					String oldLineWS = oldLine.getWorkStation();

					String newRevision = newLine.getBOPRevision();
					String oldRevision = oldLine.getBOPRevision();

					String newMesFlag = newLine.getMESIndicator();
					String oldMesFlag = oldLine.getMESIndicator();

					if ((newLineOperation.equals(oldLineOperation) == true) && (newLineWS.equals(oldLineWS) == true) && (newRevision.equals(oldRevision) == false) && (newMesFlag.equals(oldMesFlag) == true)) {
						differentOperationRevision.put(key, newLine);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return differentOperationRevision;
	}

	private TCComponentItemRevision getItemPreRevision(TCComponent item) {
		TCComponentItemRevision itemRevision = (TCComponentItemRevision) item;
		int currentRevision = 0;
		String revSID = "";
		try {
			revSID = itemRevision.getProperty(PropertyDefines.ITEM_REV_ID);
			currentRevision = Integer.parseInt(revSID);
		} catch (NumberFormatException e) {
			return null;
		} catch (TCException e) {
			return null;
		}

		if (currentRevision > 1) {
			try {
				String preRevision = Integer.toString(currentRevision - 1);
				for (int i = preRevision.length(); i < revSID.length(); i++) {
					preRevision = "0" + preRevision;
				}
				TCComponentItemRevision itemPreRevision = itemRevision.findRevision(String.valueOf(preRevision));
				return itemPreRevision;
			} catch (TCException e) {
				return null;
			}

		}
		return null;
	}

	private void workOnSubGroup() {
		Set<TCComponent> listSubGroupLine = workingBOMValues.keySet();
		for (TCComponent impactedItem : listSubGroupLine) {
			ArrayList<BOMBOPData> list = new ArrayList<BOMBOPData>();
			HashMap<String, BOMBOPData> workingBOM = null;
			HashMap<String, BOMBOPData> releaseBOM = null;
			if (workingBOMValues != null) {
				workingBOM = workingBOMValues.get(impactedItem);
			}

			if (releaseBOMValues != null) {
				if (releaseBOMValues.get(impactedItem) != null) {
					// case user doesn't change revision, just modify some part, not add or delete
					// any parts
					releaseBOM = releaseBOMValues.get(impactedItem);
				} else {
					// case user change revision to modify, add or delete some parts
					TCComponentItemRevision preRevisionImpactedItem = getItemPreRevision(impactedItem);
					if (preRevisionImpactedItem != null) {
						releaseBOM = releaseBOMValues.get(preRevisionImpactedItem);
					}
				}
			}

			// all part is new
			if (releaseBOM == null) {
				for (HashMap.Entry<String, BOMBOPData> entry : workingBOM.entrySet()) {
					BOMBOPData mbom = entry.getValue();

					mbom.processDataOnActionType(ActionType.ADD_PART_IN_BOM);
					String[] printValues = new String[] { Integer.toString(count++), mbom.getSubGroup(), mbom.getPartNumber(), mbom.getBOMLineID(), "Add New Part in BOM", "A", "Ready" };
					bomManager.printReport("PRINT", printValues);
					addToListItemNeedToBeReleased(mbom.getSubGroup(), mbom.isSubGroupReleased());
					list.add(mbom);
					bomTracking.addRawData(mbom);

					try {
						BOMBOPData mbom2 = mbom.clone();
						mbom2.processDataOnActionType(ActionType.ADD_PART_IN_BOP);
//						printValues = new String[]{Integer.toString(count++),mbom2.getSubGroup(),mbom2.getBOPID(),mbom2.getBOMLineID(),"Add New Part in BOP", "A", "Ready"};
//						bomManager.printReport("PRINT", printValues);
						addToListItemNeedToBeReleased(mbom.getBOPID(), mbom.isOperationReleased());
						addToListItemNeedToBeReleased(mbom.getStationID(), mbom.isWorkStationReleased());
//						list.add(mbom2);				
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}

				}
				SuperBomBopTransferData data = new SuperBomBopTransferData();
				data.setItem(impactedItem);
				data.setData2Send(list);
				data.setWorkingData(workingBOMValues.get(impactedItem));
				data.setReleaseData(releaseBOMValues.get(impactedItem));
				data2Send.add(data);
				continue;
			} else {
				// delete BOM Data found in release revision but not found in working revision
				HashMap<String, BOMBOPData> deletedLines = deletedBOMs(releaseBOM, workingBOM);
				for (HashMap.Entry<String, BOMBOPData> entry : deletedLines.entrySet()) {
					BOMBOPData mbom = entry.getValue();
					mbom.processDataOnActionType(ActionType.DELETE_PART_IN_BOM);
					String[] printValues = new String[] { Integer.toString(count++), mbom.getSubGroup(), mbom.getPartNumber(), mbom.getBOMLineID(), "Delete Part in BOM", "D", "Ready" };
					bomManager.printReport("PRINT", printValues);

					addToListItemNeedToBeReleased(mbom.getSubGroup(), false);
					addToListItemNeedToBeReleased(mbom.getBOPID(), false);
					addToListItemNeedToBeReleased(mbom.getStationID(), mbom.isWorkStationReleased());
					list.add(mbom);
					bomTracking.addRawData(mbom);
					deleteBopKey.add(String.format("%s~%s~%s", mbom.getSubGroup(), mbom.getBOMLineID(), mbom.getPartNumber()));
				}

				// add BOM & BOP Data found in working revision but not found in release
				// revision
				HashMap<String, BOMBOPData> addedLines = addBOMBOPs(releaseBOM, workingBOM);
				for (HashMap.Entry<String, BOMBOPData> entry : addedLines.entrySet()) {
					BOMBOPData mbom = entry.getValue();

					mbom.processDataOnActionType(ActionType.ADD_PART_IN_BOM);
					String[] printValues = new String[] { Integer.toString(count++), mbom.getSubGroup(), mbom.getPartNumber(), mbom.getBOMLineID(), "Add new Part in BOM", "A", "Ready" };
					bomManager.printReport("PRINT", printValues);
					addToListItemNeedToBeReleased(mbom.getSubGroup(), false);
					list.add(mbom);
					bomTracking.addRawData(mbom);

					try {
						BOMBOPData mbom2 = mbom.clone();
						mbom2.processDataOnActionType(ActionType.ADD_PART_IN_BOP);
						addToListItemNeedToBeReleased(mbom.getBOPID(), false);
						addToListItemNeedToBeReleased(mbom.getStationID(), mbom.isWorkStationReleased());
						// list.add(mbom2);
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
				}

				// change BOM Data
				HashMap<String, BOMBOPData> changedLines = changedBOMLines(releaseBOM, workingBOM);
				for (HashMap.Entry<String, BOMBOPData> entry : changedLines.entrySet()) {
					BOMBOPData mbom = entry.getValue();
					mbom.processDataOnActionType(ActionType.CHANGE_PART_IN_BOM);

					String[] printValues = new String[] { Integer.toString(count++), mbom.getSubGroup(), mbom.getPartNumber(), mbom.getBOMLineID(), "Change Part in BOM", "C", "Ready" };
					bomManager.printReport("PRINT", printValues);

					list.add(mbom);
				}
				SuperBomBopTransferData data = new SuperBomBopTransferData();
				data.setItem(impactedItem);
				data.setData2Send(list);
				data.setWorkingData(workingBOMValues.get(impactedItem));
				data.setReleaseData(releaseBOMValues.get(impactedItem));
				data2Send.add(data);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void workOnOperationHavingPart() {
		Set<TCComponent> listOperationLine = workingBOPValues.keySet();
		for (TCComponent operationItem : listOperationLine) {
			ArrayList<BOMBOPData> list = new ArrayList<BOMBOPData>();
			HashMap<String, BOMBOPData> workingBOP = workingBOPValues.get(operationItem);
			HashMap<String, BOMBOPData> releaseBOP = new HashMap<String, BOMBOPData>();

			if (releaseBOPValues != null) {
				if (releaseBOPValues.get(operationItem) != null) {
					// case user doesn't change revision of operation, just moving operation or
					// something else
					releaseBOP = releaseBOPValues.get(operationItem);
				} else {
					// case user change revision of operation to add or delete parts
					TCComponentItemRevision preRevisionOperationItem = getItemPreRevision(operationItem);
					if (preRevisionOperationItem != null) {
						releaseBOP = releaseBOPValues.get(preRevisionOperationItem);
					}
				}
			}
			try {
				// deleted & added BOP because of moving operation between stations
				HashMap<String, BOMBOPData> movOperations = moveBetweenStations(releaseBOP, workingBOP);
				Set<String> moveOpKeys = movOperations.keySet();

				HashMap<String, BOMBOPData> addBOPMovingOperation = (HashMap<String, BOMBOPData>) workingBOP.clone();
				HashMap<String, BOMBOPData> deleteBOPMovingOperation = (HashMap<String, BOMBOPData>) releaseBOP.clone();
				Iterator<String> iterator = moveOpKeys.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					BOMBOPData opAdd = addBOPMovingOperation.get(key);
					opAdd.processDataOnActionType(ActionType.ADD_PART_IN_BOP);

					String[] printValues = new String[] { Integer.toString(count++), opAdd.getSubGroup(), opAdd.getBOMLineID(), opAdd.getWorkStation(), opAdd.getBOPID(), opAdd.getBOPRevision(), "Moving operation between stations || Change MESFlag", "A", "Ready" };
					bomManager.printReport2("PRINT", printValues);
					addToListItemNeedToBeReleased(opAdd.getBOPID(), false);
					addToListItemNeedToBeReleased(opAdd.getStationID(), opAdd.isWorkStationReleased());
					list.add(opAdd);

					BOMBOPData opDelete = deleteBOPMovingOperation.get(key);
					opDelete.processDataOnActionType(ActionType.DELETE_PART_IN_BOP);

					printValues = new String[] { Integer.toString(count++), opDelete.getSubGroup(), opDelete.getBOMLineID(), opDelete.getWorkStation(), opDelete.getBOPID(), opDelete.getBOPRevision(), "Moving operation between stations || Change MESFlag", "D", "Ready" };
					bomManager.printReport2("PRINT", printValues);
					addToListItemNeedToBeReleased(opDelete.getStationID(), opAdd.isWorkStationReleased());
					list.add(opDelete);
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			try {
				// added Part in BOP
				HashMap<String, BOMBOPData> addPartsInOperation = addedPartsInOperation(releaseBOP, workingBOP);
				Set<String> addOpKeys = addPartsInOperation.keySet();
				Iterator<String> iterator = addOpKeys.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					BOMBOPData opAdd = addPartsInOperation.get(key);
					opAdd.processDataOnActionType(ActionType.ADD_PART_IN_BOP);

					String[] printValues = new String[] { Integer.toString(count++), opAdd.getSubGroup(), opAdd.getBOMLineID(), opAdd.getWorkStation(), opAdd.getBOPID(), opAdd.getBOPRevision(), "Add new part in BOP", "A", "Ready" };
					bomManager.printReport2("PRINT", printValues);
					addToListItemNeedToBeReleased(opAdd.getBOPID(), false);
					addToListItemNeedToBeReleased(opAdd.getStationID(), opAdd.isWorkStationReleased());
					list.add(opAdd);
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			try {
				// dont send action delete bop to SAP because it may cause some faults on SAP.
				// deleted Part in BOP
				HashMap<String, BOMBOPData> deletePartsInOperation = deletedPartsInOperation(releaseBOP, workingBOP);
				Set<String> deleteOpKeys = deletePartsInOperation.keySet();
				Iterator<String> iterator = deleteOpKeys.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					BOMBOPData opDelete = deletePartsInOperation.get(key);
					opDelete.processDataOnActionType(ActionType.DELETE_PART_IN_BOP);
					if (!deleteBopKey.contains(String.format("%s~%s~%s", opDelete.getSubGroup(), opDelete.getBOMLineID(), opDelete.getPartNumber()))) {
						// only send delete bop in case move part between operation, don't send delete
						// bop when remove part from MBOM
						list.add(opDelete);
						String[] printValues = new String[] { Integer.toString(count++), opDelete.getSubGroup(), opDelete.getBOMLineID(), opDelete.getWorkStation(), opDelete.getBOPID(), opDelete.getBOPRevision(), "Delete part in BOP", "D", "Ready" };
						bomManager.printReport2("PRINT", printValues);
					}
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			try {
				// change BOP because of changing revision of operation
				HashMap<String, BOMBOPData> changedOpertionRevision = changeInOperationRevisionOnly(releaseBOP, workingBOP);
				Set<String> revChangeKeys = changedOpertionRevision.keySet();
				Iterator<String> iter = revChangeKeys.iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					BOMBOPData opChange = changedOpertionRevision.get(key);
					opChange.processDataOnActionType(ActionType.CHANGE_OPERATION_REVISION);

					String[] printValues = new String[] { Integer.toString(count++), opChange.getSubGroup(), opChange.getBOMLineID(), opChange.getWorkStation(), opChange.getBOPID(), opChange.getBOPRevision(), "Change operation revision", "C", "Ready" };
					bomManager.printReport2("PRINT", printValues);
					addToListItemNeedToBeReleased(opChange.getBOPID(), false);
					list.add(opChange);
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			SuperBomBopTransferData data = new SuperBomBopTransferData();
			data.setItem(operationItem);
			data.setData2Send(list);
			data.setWorkingData(workingBOPValues.get(operationItem));
			data.setReleaseData(releaseBOPValues.get(operationItem));
			data2Send.add(data);
		}
	}

	private void workOnOperationNoPart() {
		// operation no part
		for (HashMap.Entry<TCComponent, HashMap<String, BOMBOPData>> entry : OWPBOPValues.entrySet()) {
			ArrayList<BOMBOPData> list = new ArrayList<BOMBOPData>();
			HashMap<String, BOMBOPData> map = entry.getValue();
			Iterator<Map.Entry<String, BOMBOPData>> iterator = map.entrySet().iterator();
			Entry<String, BOMBOPData> val = iterator.next();
			BOMBOPData op = val.getValue();
			String[] printValues = new String[] { Integer.toString(count++), op.getSubGroup(), "---", op.getWorkStation(), op.getBOPID(), op.getBOPRevision(), String.format("Operation no part (%s) ", op.getAction()), op.getAction(), "Ready" };
			bomManager.printReport2("PRINT", printValues);
			addToListItemNeedToBeReleased(op.getStationID(), op.isWorkStationReleased());
			addToListItemNeedToBeReleased(op.getBOPID(), op.isOperationReleased());
			list.add(op);

			SuperBomBopTransferData data = new SuperBomBopTransferData();
			data.setItem(entry.getKey());
			data.setData2Send(list);
			data.setWorkingData(OWPBOPValues.get(entry.getKey()));
			data.setReleaseData(OWPBOPValues.get(entry.getKey()));
			data2Send.add(data);
		}
	}

	private void validateItemNeedToBeRelease() {
		count = 1;
		Set<String> setItemInReleaseFolder = bomManager.getSetItemInReleaseFolder();
		itemNeedToBeRelease.removeAll(setItemInReleaseFolder);

		if (itemNeedToBeRelease.size() > 0) {
			for (String item : itemNeedToBeRelease) {
				if (item == null || item.isEmpty())
					continue;
				String[] printValues = new String[] { Integer.toString(count++), "...", "...", "....", String.format("Newest revision of item [%s] must be in [Solution Folder] for release", item), "...", "Error" };
				bomManager.printReport("PRINT", printValues);
			}
		}
	}

	private void addToListItemNeedToBeReleased(String data, boolean isReleased) {
		if (!isReleased) {
			itemNeedToBeRelease.add(data);
		} else {
			System.out.println(String.format("%s was released", data));
		}
	}

}
