package com.vinfast.admin.sap.car;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FoundNodesInfo;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.vinfast.car.sap.superbom.BomBopDataHandler;
import com.vinfast.car.sap.superbom.CarSuperBomBopLoadData;
import com.vinfast.sap.bom.BOMBOPData;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SubGroupDatasetCreate_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponentItemRevision selectedObject = null;
	private SubGroupDatasetCreate_Dialog dlg;
	private BOMManager bomManager = null;
	private BomBopDataHandler bombopDataHandler = null;
	private DataManagementService dmService = null;
	private static String[] GROUP_PERMISSION = { "dba" };
	private String revisionRule = PropertyDefines.REVISION_RULE_RELEASE;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			bombopDataHandler = new BomBopDataHandler();
			dmService = DataManagementService.getService(session);

			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.", "Please change to group: " + String.join(";", GROUP_PERMISSION) + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			if (validObjectSelect(targetComponents)) {
				MessageBox.post("Please Select MCN.", "Error", MessageBox.ERROR);
				return null;
			}
			
			bomManager = new BOMManager(UIGetValuesUtility.getCompanyCode(event)).loadChangeAttachments(session, selectedObject);
			if (bomManager == null) {
				MessageBox.post("No or more items exists in impacted shop.", "Error", MessageBox.ERROR);
				return null;
			}

			dlg = new SubGroupDatasetCreate_Dialog(new Shell(), session);
			dlg.create();
			if (bomManager.getImpactedItems() != null) {
				for (TCComponent impacted : bomManager.getImpactedItems()) {
					dlg.lstImpactedItems.add(impacted.getPropertyDisplayableValue("object_string"));
				}
			}
			dlg.txtShop.setText(bomManager.getShopName());

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					try {
						startProcessing();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void startProcessing() throws Exception {
		if (bomManager.getImpactedItems() != null) {
			String mcnValue = bomManager.getMCN();
			String logFolder = UIGetValuesUtility.createLogFolder("MCN" + mcnValue);
			StringBuilder totalReport = new StringBuilder();
			totalReport.append("<html>" + "<body>");
			for (TCComponent subGroup : bomManager.getImpactedItems()) {
				bomManager.setError(false);
				HashMap<TCComponent, HashMap<String, BOMBOPData>> currentBOMValues = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();
				HashMap<TCComponent, HashMap<String, BOMBOPData>> currentBOPValues = new HashMap<TCComponent, HashMap<String, BOMBOPData>>();

				StringBuilder bomReport = new StringBuilder();
				Logger.bufferResponse(Logger.BUFFERRESPONSE_HEADER, new String[] { "S.No BOM", "Sub-group", "Part Number", "BOM ID", "Message", "Action", "Result" }, bomReport);
				StringBuilder bopReport = new StringBuilder();
				Logger.bufferResponse(Logger.BUFFERRESPONSE_HEADER, new String[] { "S.No BOP", "Sub-group", "BOM ID", "Work Station", "BOP ID", "Revision", "Message", "Action", "Result" }, bopReport);

				// open working revision data
				List<TCComponent> operationList = new LinkedList<TCComponent>();
				if (subGroup != null) {
					if (!bomManager.isWindowOpen())
						bomManager.createMBOMBOPWindow(revisionRule);

					String subGroupID = subGroup.getPropertyDisplayableValue("item_id");
//					currentBOMValues = CarSuperBomBopLoadData.loadSubGroupChilds(bomManager, null, subGroupID);
					TCComponent[] foundObjects = UIGetValuesUtility.searchStruture(session, subGroupID, bomManager.getMBOMTraverseLine());
					if (foundObjects != null) {
						HashMap<TCComponent, TCComponent[]> parentChildLines = UIGetValuesUtility.expandBOMOneLevel(session, foundObjects);
						HashMap<String, TCComponent> isList = new HashMap<String, TCComponent>();
						for (TCComponent parentLine : parentChildLines.keySet()) {
							String parentID = parentLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_STR);
							parentID = parentID.substring(0, parentID.indexOf("-"));
							isList.put(parentID, parentLine);
						}
						FoundNodesInfo[] foundLinkedLines = TCExtension.findPartInBOP(bomManager.getBOPTraverseLine(), parentChildLines, session);
						if (foundLinkedLines != null) {
							HashMap<TCComponent, ArrayList<BOMBOPData>> parentChildDataMap = new HashMap<TCComponent, ArrayList<BOMBOPData>>();
							for (FoundNodesInfo foundParentLines : foundLinkedLines) {
								boolean isLinkError = false;
								String parentID = foundParentLines.clientID;
								ArrayList<BOMBOPData> childLinesList = new ArrayList<BOMBOPData>();
								NodeInfo[] childLinkedNodes = foundParentLines.resultNodes;
								if (childLinkedNodes.length != 0) {
									for (NodeInfo childNode : childLinkedNodes) {
										TCComponent BOM = childNode.originalNode;
										TCComponent[] BOP = childNode.foundNodes;
										if (BOP.length == 1) {
											if (BOP[0] instanceof TCComponentBOPLine) {
												TCComponentBOMLine operation = ((TCComponentBOPLine) BOP[0]).parent();
												operationList.add(operation.getItemRevision());
											}
											BOMBOPData childLineMap = new BOMBOPData();
											childLineMap.setMBOMLine(BOM);
											childLineMap.setMBOPLine(BOP);
											childLinesList.add(childLineMap);
										} else {
											String partID = BOM.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
											String BOMID = BOM.getPropertyDisplayableValue(PropertyDefines.BOM_BOM_ID);
											String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), parentID, partID, BOMID, "Linked to " + BOP.length + " lines in BOP. Please correct BOM to BOP link.", "-", "Error" };
											Logger.bufferResponse(Logger.BUFFERRESPONSE_PRINT, printValues, bomReport);
											isLinkError = true;
											bomManager.setError(true);
										}
									}
								}

								if (!isLinkError) {
									parentChildDataMap.put(isList.get(parentID), childLinesList);
								}
							}
							currentBOMValues = bombopDataHandler.loadBOMBOPData(bomManager, parentChildDataMap);
						}
					}
				}
				if (operationList != null) {
					Set<String> operationIDs = new HashSet<String>();
					for (TCComponent operation : operationList) {
						operationIDs.add(operation.getPropertyDisplayableValue("item_id"));
					}
					if (!bomManager.hasError()) {
						if (operationIDs != null) {
							if (!bomManager.isWindowOpen())
								bomManager.createMBOMBOPWindow(revisionRule);

							currentBOPValues = CarSuperBomBopLoadData.loadOperationChilds(bomManager, null, String.join(";", operationIDs));
						}
					}
				}

				if (bomManager.isWindowOpen()) {
					bomManager.closeMBOMBOPWindows();
				}

				if (currentBOMValues != null) {
					for (Map.Entry<TCComponent, HashMap<String, BOMBOPData>> item : currentBOMValues.entrySet()) {
						if(item.getValue() != null) {
							for (Map.Entry<String, BOMBOPData> dataMap : item.getValue().entrySet()) {
								BOMBOPData bomBopItem = dataMap.getValue();
								String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), bomBopItem.getSubGroup(), bomBopItem.getPartNumber(), bomBopItem.getBOMLineID(), "", "", "" };
								Logger.bufferResponse(Logger.BUFFERRESPONSE_PRINT, printValues, bomReport);
							}
						}
					}
				}
				
				if (currentBOPValues != null) {
					for (Map.Entry<TCComponent, HashMap<String, BOMBOPData>> item : currentBOPValues.entrySet()) {
						if(item.getValue() != null) {
							for (Map.Entry<String, BOMBOPData> dataMap : item.getValue().entrySet()) {
								BOMBOPData bomBopItem = dataMap.getValue();
								String[] printValues = new String[] { Integer.toString(bomManager.getSerialNo()), bomBopItem.getSubGroup(), bomBopItem.getBOMLineID(), bomBopItem.getWorkStation(), bomBopItem.getBOPID(), bomBopItem.getBOPRevision(), "", "", "" };
								Logger.bufferResponse(Logger.BUFFERRESPONSE_PRINT, printValues, bopReport);
							}
						}
					}
				}

				if (!bomManager.hasError()) {
					if (currentBOMValues != null) {
						for (Map.Entry<TCComponent, HashMap<String, BOMBOPData>> item : currentBOMValues.entrySet()) {
							CarSuperBomBopLoadData.uploadDataSet_ReleaseData(item.getKey(), item.getValue(), logFolder, session, dmService);
						}
					}
					if (currentBOPValues != null) {
						for (Map.Entry<TCComponent, HashMap<String, BOMBOPData>> item : currentBOPValues.entrySet()) {
							CarSuperBomBopLoadData.uploadDataSet_ReleaseData(item.getKey(), item.getValue(), logFolder, session, dmService);
						}
					}
				}
				bomReport.append("</table>");
				bopReport.append("</table>");
				totalReport.append("Sub-Group: " + subGroup);
				totalReport.append(bomReport.toString());
				totalReport.append("</br>");
				totalReport.append(bopReport.toString());
			}

			totalReport.append("</body>" + "</html>");
			dlg.browser.setText(totalReport.toString());
			dlg.setMessage("Process success", IMessageProvider.INFORMATION);
		}
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;
		if (targetComponents.length > 1)
			return true;
		if (targetComponents[0] instanceof TCComponentItemRevision) {
			selectedObject = (TCComponentItemRevision) targetComponents[0];
		}
		if (selectedObject == null)
			return true;
		return false;
	}
}
