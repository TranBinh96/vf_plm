package com.vinfast.car.sap.superbom;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.bom.BOMBOPData;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.bom.FindConnectedLines;
import com.vinfast.sap.bom.ValidateBOMLines;
import com.vinfast.sap.util.MutableBoolean;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class CarSuperBomBopLoadData {
	public static HashMap<TCComponent, HashMap<String, BOMBOPData>> loadSubGroupChilds(BOMManager BOMManager,  IProgressMonitor monitor,String searchObjects) throws TCException {
		BomBopDataHandler bombopDataHandler = new BomBopDataHandler();
		HashMap<TCComponent, HashMap<String, BOMBOPData>> traversalDataMap = new HashMap<TCComponent, HashMap<String,BOMBOPData>>();
		TCSession session = BOMManager.getSession();
		long startTime = System.currentTimeMillis();
		TCComponent[] foundObjects = UIGetValuesUtility.searchStruture(session, searchObjects, BOMManager.getMBOMTraverseLine());
		long endTime = System.currentTimeMillis();
		System.out.println("searchStruture MBOM take times : " + (endTime - startTime));
		if (foundObjects != null) {
			HashMap<TCComponent, TCComponent[]> parentChildLines = UIGetValuesUtility.expandBOMOneLevel(session, foundObjects);
			if (!parentChildLines.isEmpty()) {
				parentChildLines = ValidateBOMLines.superBOM(BOMManager, parentChildLines);
				if (BOMManager.hasError() == false) {
					if (parentChildLines.isEmpty() == false) {
						HashMap<TCComponent, ArrayList<BOMBOPData>> parentChildDataMap = new FindConnectedLines().inBOP(BOMManager, parentChildLines, 1);
						if (BOMManager.hasError() == false) {
							if (parentChildDataMap != null) {
								traversalDataMap = bombopDataHandler.loadBOMBOPData(BOMManager, parentChildDataMap);
							}
						}
					}
				}
			}
		}

		return traversalDataMap;
	}

	public static HashMap<TCComponent, HashMap<String, BOMBOPData>> loadOperationChilds(BOMManager BOMManager,  IProgressMonitor monitor, String operations) throws TCException {
		HashMap<TCComponent, HashMap<String, BOMBOPData>> traversalDataMap = null;
		BomBopDataHandler bombopDataHandler = new BomBopDataHandler();
		TCSession session = BOMManager.getSession();
		long startTime = System.currentTimeMillis();
		TCComponent[] foundObjects = UIGetValuesUtility.searchStruture(session, operations, BOMManager.getBOPTraverseLine());
		long endTime = System.currentTimeMillis();
		System.out.println("searchStruture BOP take times : " + (endTime - startTime));
		if (foundObjects != null) {
			HashMap<TCComponent, TCComponent[]> parentChildLines = UIGetValuesUtility.expandBOPOneLevel(session, foundObjects);
			if (!parentChildLines.isEmpty()) {
				FindConnectedLines findLines = new FindConnectedLines();
				HashMap<TCComponent, ArrayList<BOMBOPData>> parentChildDataMap = findLines.inBOM(BOMManager, parentChildLines, 1);
				if (BOMManager.hasError() == false) {
					if (parentChildDataMap != null) {
						HashMap<TCComponent, TCComponent[]> allBOPBOMLines = findLines.getBOMLines();
						parentChildLines = ValidateBOMLines.superBOM(BOMManager, allBOPBOMLines);
						if (BOMManager.hasError() == false) {
							traversalDataMap = bombopDataHandler.loadBOMBOPData(BOMManager, parentChildDataMap);
						}
					}
				}
			}
		}

		return traversalDataMap;
	}

	public static HashMap<TCComponent, HashMap<String, BOMBOPData>> loadOperationsNoPart(BOMManager BOMManager, IProgressMonitor monitor, boolean isDeleted) {
		HashMap<TCComponent, HashMap<String, BOMBOPData>> traversalDataMap = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();
		BomBopDataHandler bombopDataHandler = new BomBopDataHandler();

		if (!BOMManager.getOperationNoPart().isEmpty()) {
			ArrayList<TCComponentBOMLine> BOPNoPart = BOMManager.getOperationNoPart();
			for (TCComponentBOMLine MEOPBOMLine : BOPNoPart) {
				BOMManager.getDataManagementService().getProperties(new TCComponent[] { MEOPBOMLine }, new String[] { PropertyDefines.BOM_OPERATION_TYPE });
				String operationType;
				try {
					operationType = MEOPBOMLine.getPropertyDisplayableValue(PropertyDefines.BOM_OPERATION_TYPE);
					String WSID = MEOPBOMLine.getPropertyDisplayableValue(PropertyDefines.BOM_TRANSFER_TO_MES);
					if (operationType.compareTo("NA") == 0 || operationType.isEmpty()) {
						try {
							TCComponentItemRevision currentRev = MEOPBOMLine.getItemRevision();
							TCComponentItemRevision previousRev = UIGetValuesUtility.getPreviousRevision(currentRev);
							if (previousRev != currentRev) {
								String opeType = previousRev.getPropertyDisplayableValue("vf4_operation_type");
								if (!opeType.isEmpty()) {
									if (opeType.compareTo("NA") != 0) {
										TCComponent[] problemList = BOMManager.getProblemItems();
										if (!checkObjectHaveInArray(problemList, previousRev)) {
											BOMManager.setError(true);
											String operationID = previousRev.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
											String operationRevisionID = previousRev.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
											String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), "", "", WSID, "Please copy operation [" + operationID + "/" + operationRevisionID + "] to problems folder.", "-", "Error" };
											BOMManager.printReport("PRINT", printValues);
										}
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						if (WSID.trim().isEmpty() && !isDeleted) {
							BOMManager.setError(true);
							String operationID = MEOPBOMLine.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
							String operationRevisionID = MEOPBOMLine.getPropertyDisplayableValue(PropertyDefines.BOM_REV_ID);
							String[] printValues = new String[] { Integer.toString(BOMManager.getSerialNo()), "", "", WSID, "Please first transfer operation [" + operationID + "/" + operationRevisionID + "] to MES and then transfer BOM/BOP to SAP", "-", "Error" };
							BOMManager.printReport("PRINT", printValues);
						} else {
							HashMap<String, BOMBOPData> BOPData = bombopDataHandler.loadBOPData(BOMManager, MEOPBOMLine, isDeleted);
							try {
								traversalDataMap.put(MEOPBOMLine.getItemRevision(), BOPData);
							} catch (TCException e) {
								e.printStackTrace();
							}
						}
					}
				} catch (NotLoadedException e) {
					e.printStackTrace();
				}
			}
		}
		return traversalDataMap;

	}

	public static HashMap<TCComponent, HashMap<String, BOMBOPData>> loadSubGroupReleaseData(BOMManager bomManager, String searchObjects, MutableBoolean isEnoughDataSet) throws TCException {
		HashMap<TCComponent, HashMap<String, BOMBOPData>> saveData = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();

		ArrayList<TCComponent> components = new ArrayList<TCComponent>();
		// get all items revision in impacted folder
		if (bomManager.getImpactedItemsCount() > 0) {
			for (TCComponent cpn : bomManager.getImpactedItems()) {
				TCComponentItemRevision item = (TCComponentItemRevision) cpn;
				if (searchObjects.contains(item.getProperty(PropertyDefines.ITEM_ID))) {
					components.add(cpn);
				}
			}
		}

		Set<TCComponentItemRevision> listItemReleasedRevision = new HashSet<TCComponentItemRevision>();

		// get all released item revision
		for (TCComponent cpn : components) {
			TCComponentItemRevision curRevision = (TCComponentItemRevision) cpn;
			String itemRevID = curRevision.getProperty(PropertyDefines.ITEM_REV_ID);
			int revID = Integer.parseInt(itemRevID);

			if (revID > 1) {
				String search_rev = Integer.toString(revID - 1);
				for (int i1 = search_rev.length(); i1 < itemRevID.length(); i1++) {
					search_rev = "0" + search_rev;
				}
				// release Item
				try {
					TCComponentItemRevision releaseItemRevision = curRevision.findRevision(search_rev);
					listItemReleasedRevision.add(releaseItemRevision);
				} catch (Exception ex) {
					TCComponentItemRevision releaseItemRevision = curRevision.findRevision(search_rev);
					listItemReleasedRevision.add(releaseItemRevision);
					ex.printStackTrace();
				}
			}
		}

		// download dataset for all released item
		isEnoughDataSet.bVal = true;
		Set<String> notDatasetList = new HashSet<String>();
		for (TCComponentItemRevision revision : listItemReleasedRevision) {
			HashMap<String, BOMBOPData> data = downloadDataSet(revision, bomManager, bomManager.getSession());
			if (data == null) {
				notDatasetList.add(revision.getProperty(PropertyDefines.ITEM_ID));
			} else {
				saveData.put(revision, data);
			}
		}
		if (notDatasetList.size() > 0) {
			if (!bomManager.isWindowOpen()) {
				bomManager.createMBOMBOPWindow(PropertyDefines.REVISION_RULE_RELEASE);
			}
			
			saveData.putAll(loadSubGroupChilds(bomManager, null, String.join(";", notDatasetList)));
		}
		return saveData;
	}

	public static void loadOperationReleaseData(BOMManager bomManager, String searchObjects, MutableBoolean isEnoughDataSet, HashMap<TCComponent, HashMap<String, BOMBOPData>> operationHavePart, HashMap<TCComponent, HashMap<String, BOMBOPData>> operationNoPart) throws TCException {
		ArrayList<TCComponent> components = new ArrayList<TCComponent>();
		// get all items revision in impacted folder
		if (bomManager.getSolutionItems() != null) {
			for (TCComponent cpn : bomManager.getSolutionItems()) {
				TCComponentItemRevision item = (TCComponentItemRevision) cpn;
				if (searchObjects.contains(item.getProperty(PropertyDefines.ITEM_ID))) {
					components.add(cpn);
				}
			}
		}

		Set<TCComponentItemRevision> listItemReleasedRevision = new HashSet<TCComponentItemRevision>();

		// get all released item revision
		for (TCComponent cpn : components) {
			TCComponentItemRevision curRevision = (TCComponentItemRevision) cpn;
			if (UIGetValuesUtility.isReleasedItem(curRevision)) {
				listItemReleasedRevision.add(curRevision);
			} else {
				String itemRevID = curRevision.getProperty(PropertyDefines.ITEM_REV_ID);
				int revID = Integer.parseInt(itemRevID);

				if (revID > 1) {
					String search_rev = Integer.toString(revID - 1);
					for (int i1 = search_rev.length(); i1 < itemRevID.length(); i1++) {
						search_rev = "0" + search_rev;
					}
					// release Item
					TCComponentItemRevision releaseItemRevision = curRevision.findRevision(search_rev);
					listItemReleasedRevision.add(releaseItemRevision);
				}
			}
		}

		// operation in problem folder is released operation
		if (bomManager.getProblemItemsCount() > 0) {
			for (TCComponent cpn : bomManager.getProblemItems()) {
				listItemReleasedRevision.add((TCComponentItemRevision) cpn);
			}
		}

		// download dataset for all released item
		isEnoughDataSet.bVal = true;
		for (TCComponentItemRevision revision : listItemReleasedRevision) {
			HashMap<String, BOMBOPData> data = downloadDataSet(revision, bomManager, bomManager.getSession());
			
			if (data == null) {
				CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMWindow(bomManager.getSession(), new TCComponent[] {revision});
				HashMap<TCComponent, TCComponent[]> parentLines = UIGetValuesUtility.expandBOPOneLevel(bomManager.getSession(), new TCComponent[] {output[0].bomLine});
				if(parentLines.get(output[0].bomLine).length != 0) {
					UIGetValuesUtility.closeWindow(bomManager.getSession(), output[0].bomWindow);
					isEnoughDataSet.bVal = false;
					break;
				}
				UIGetValuesUtility.closeWindow(bomManager.getSession(), output[0].bomWindow);
			} else {
				Iterator<Map.Entry<String, BOMBOPData>> iterator = data.entrySet().iterator();
				Entry<String, BOMBOPData> val = iterator.next();
				if (val.getValue().isOperationNoPart()) {
					operationNoPart.put(revision, data);
				} else {
					operationHavePart.put(revision, data);
				}
			}
		}
	}

	private static HashMap<String, BOMBOPData> downloadDataSet(TCComponentItemRevision itemRevision, BOMManager BOMManager, TCSession session) throws TCException {
		HashMap<String, BOMBOPData> res = null;
		String dataset_name = String.format("%s_%s_TRANSFERRED.txt", itemRevision.getProperty(PropertyDefines.ITEM_ID), itemRevision.getProperty(PropertyDefines.ITEM_REV_ID));
		TCComponent[] oldDatasets = UIGetValuesUtility.searchDataset(session, new String[] { "Name", "Dataset Type" }, new String[] { dataset_name, "Text" }, "Dataset...");
		DataManagementService dataManagementService = BOMManager.getDataManagementService();
		if (oldDatasets != null) {
			HashMap<Date, TCComponent> datasetMap = new HashMap<Date, TCComponent>();
			dataManagementService.getProperties(oldDatasets, new String[] { "creation_date" });
			for (TCComponent dataset : oldDatasets) {
				Date date_creation = dataset.getDateProperty("creation_date");
				datasetMap.put(date_creation, dataset);
			}
			ArrayList<Date> sortedKeys = new ArrayList<Date>(datasetMap.keySet());
			Collections.sort(sortedKeys, Collections.reverseOrder());
			TCComponentDataset olddataset = (TCComponentDataset) datasetMap.get(sortedKeys.get(0));
			File oldBOMFile = UIGetValuesUtility.downloadDataset(session, System.getProperty("java.io.tmpdir"), olddataset);
			if(oldBOMFile != null) {
				res = UIGetValuesUtility.previousSuperBOMStructure(session, oldBOMFile, BOMManager.getMCN());
			}
		}

		return res;
	}

	public static void uploadDataSet(SuperBomBopTransferData data, BOMManager BOMManager, String logFolder) {
		try {
			TCComponentItemRevision itemRevision = (TCComponentItemRevision) data.getItem();
			if (data.getWorkingData() == null || UIGetValuesUtility.isReleasedItem(itemRevision))
				return;
			String fileName = String.format("%s_%s_TRANSFERRED.txt", itemRevision.getProperty(PropertyDefines.ITEM_ID), itemRevision.getProperty(PropertyDefines.ITEM_REV_ID));
			File file = new File(String.format("%s\\%s", logFolder, fileName));
			UIGetValuesUtility.uploadSuperBOMStructure(BOMManager.getSession(), file, data.getWorkingData());
			TCComponentDataset dataSet = UIGetValuesUtility.createDataset(BOMManager.getDataManagementService(), null, null, file.getName(), "Transfered data", "Text", "TextEditor");
			if (dataSet != null) {
				UIGetValuesUtility.uploadNamedReference(BOMManager.getSession(), dataSet, file, "Text", true, true);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public static void uploadDataSet_ReleaseData(TCComponent item, HashMap<String, BOMBOPData> data, String logFolder, TCSession session, DataManagementService dmService) {
		try {
			TCComponentItemRevision itemRevision = (TCComponentItemRevision) item;
			String fileName = String.format("%s_%s_TRANSFERRED.txt", itemRevision.getProperty(PropertyDefines.ITEM_ID), itemRevision.getProperty(PropertyDefines.ITEM_REV_ID));
			File file = new File(String.format("%s\\%s", logFolder, fileName));
			UIGetValuesUtility.uploadSuperBOMStructure(session, file, data);
			TCComponentDataset dataSet = UIGetValuesUtility.createDataset(dmService, null, null, file.getName(), "Transfered data", "Text", "TextEditor");
			if (dataSet != null) {
				UIGetValuesUtility.uploadNamedReference(session, dataSet, file, "Text", true, true);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private static boolean checkObjectHaveInArray(TCComponent[] array, TCComponent object) {
		if (array == null || array.length == 0)
			return false;
		return Arrays.asList(array).contains(object);
	}
}
