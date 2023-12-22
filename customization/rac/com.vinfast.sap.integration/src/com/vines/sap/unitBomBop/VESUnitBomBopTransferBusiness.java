package com.vines.sap.unitBomBop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.teamcenter.integration.arch.BusinessHandlerAbstract;
import com.teamcenter.integration.arch.ModelAbstract;
import com.teamcenter.integration.arch.ModelAbstract.PREPARE_STATUS;
import com.teamcenter.integration.arch.TCHelper;
import com.teamcenter.integration.model.MCNInformation;
import com.teamcenter.integration.model.UnitBOMBOPModel;
import com.teamcenter.integration.model.UnitBOMBOPTransferModel;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.ActionCommand;
import com.vinfast.integration.model.NoticeMessage;
import com.vinfast.integration.model.NoticeMessage.NoticeType;
import com.vinfast.integration.model.ProcessStatus;
import com.vinfast.integration.model.ServerInformation;
import com.vinfast.sap.util.OpenWindowConfig;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class VESUnitBomBopTransferBusiness extends BusinessHandlerAbstract {
	ArrayList<UnitBOMBOPTransferModel> subGroupDataList = null;
	ArrayList<UnitBOMBOPModel> owpBOPDataList = null;
	TCComponentItemRevision obj_shopTopNode = null;
	TCComponentItemRevision obj_Shop = null;
	TCComponent[] workStations = null;
	MCNInformation mcn = null;
	ServerInformation serverInfo = null;
	int count = 0;

	public VESUnitBomBopTransferBusiness(String processName) {
		super(processName);
	}

	@Override
	public void onMessage(ModelAbstract msg) {
		switch (msg.getModelType()) {
		case ACTION_COMMAND:
			onCommand((ActionCommand) msg);
			break;
		case SERVER_INFORMATION:
			serverInfo = (ServerInformation) msg;
			System.out.println(String.format("SERVER_INFORMATION %s : %s", serverInfo.getServerType(), serverInfo.getIp()));
		default:
			break;
		}
	}

	public void onCommand(ActionCommand cmd) {
		switch (cmd.getCommand()) {
		case COMMAND_TRANSFER:
			transfer();
			break;
		case COMMAND_PREPARE:
			prepare();
			break;
		case COMMAND_LOAD_MCN_INFO:
			loadMCNInformation();
			break;
		default:
			break;
		}
	}

	private void transfer() {

	}

	private boolean prepare() {
		if (serverInfo == null) {
			publish(PropertyDefines.UI_STORE, new NoticeMessage("Please select server first!", NoticeType.ERROR));
			return false;
		}

		TCComponent impactedShop = null;
		TCComponentChangeItemRevision change = (TCComponentChangeItemRevision) TCHelper.getInstance().changeObject;
		try {
			impactedShop = change.getContents(PropertyDefines.REL_IMPACT_SHOP)[0];
		} catch (Exception e) {
			e.printStackTrace();
		}

		OpenWindowConfig windowConfig = new OpenWindowConfig(TCHelper.getInstance().session, (TCComponentItemRevision) impactedShop);
		if (windowConfig.isLoaded() == false) {
			publish(PropertyDefines.UI_STORE, new NoticeMessage("BOM and BOP linked error. Please check BOM and BOP is linked or Contact Teamcenter Admin.", NoticeType.ERROR));
			return false;
		}

		subGroupDataList = new ArrayList<UnitBOMBOPTransferModel>();
		owpBOPDataList = new ArrayList<UnitBOMBOPModel>();

		TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision) TCHelper.getInstance().changeObject;
		DataManagementService dmService = TCHelper.getInstance().dmService;
		TCSession session = TCHelper.getInstance().session;

		try {
			TCComponent[] impactedItems = UIGetValuesUtility.getRelatedComponents(dmService, changeRevision, new String[] {}, PropertyDefines.REL_IMPACT_ITEMS);
			Set<String> transferIDs = new HashSet<String>();
			if (impactedItems != null) {
				for (TCComponent impactedItem : impactedItems) {
					String itemID = impactedItem.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
					String sapTransfer = impactedItem.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP);
					if (serverInfo.getServerType().equals(PropertyDefines.SERVER_PRODUCTION)) {
						if (sapTransfer.isEmpty()) {
							transferIDs.add(itemID);
						} else {
							UnitBOMBOPTransferModel newData2SendItem = new UnitBOMBOPTransferModel();
							newData2SendItem.setItemRev(impactedItem);
							newData2SendItem.setPrepareStatus(PREPARE_STATUS.ALREADY_TRANSFER);
							subGroupDataList.add(newData2SendItem);
						}
					} else {
						transferIDs.add(itemID);
					}
				}
			}
			if (!transferIDs.isEmpty()) {
				ArrayList<TCComponent> impactedBOMLines = UIGetValuesUtility.searchPartsInStruture(session, transferIDs.toArray(new String[0]), windowConfig.SearchIn);
				if (impactedBOMLines != null) {
					for (TCComponent impactedLine : impactedBOMLines) {
						TCComponentBOMLine solutionBomLine = (TCComponentBOMLine) impactedLine;
						TCComponentItemRevision solutionRevision = solutionBomLine.getItemRevision();
						TCComponentBOMLine problemBomLine = null;
						TCComponentItemRevision previousRevision = UIGetValuesUtility.getPreviousRevision(solutionRevision);

						if (previousRevision != null) { // Revision > 1
							CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session, previousRevision);
							problemBomLine = output[0].bomLine;
							UIGetValuesUtility.closeWindow(session, output[0].bomWindow);
						}

						UnitBOMBOPTransferModel newData2SendItem = new UnitBOMBOPTransferModel();
						newData2SendItem.setItemRev(solutionBomLine);
						newData2SendItem.setPreviousItemRevision(problemBomLine);
						newData2SendItem.process(session);
						subGroupDataList.add(newData2SendItem);
					}
				}
			}

			TCComponent[] problemItems = UIGetValuesUtility.getRelatedComponents(dmService, changeRevision, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_PRB_ITEMS);
			if (problemItems != null) {
				for (TCComponent items : problemItems) {
					TCComponentItemRevision problem = (TCComponentItemRevision) items;
					CreateBOMWindowsOutput[] output = UIGetValuesUtility.createBOMNoRuleWindow(session, problem);
					TCComponentBOMWindow operationWindow = output[0].bomWindow;
					TCComponentBOMLine operationLine = output[0].bomLine;
					if (!UIGetValuesUtility.hasMaterials(operationLine)) {
						UnitBOMBOPModel newDataOwpBOP = new UnitBOMBOPModel();
						newDataOwpBOP.generateOWPBOPItem(operationLine, "D");
						owpBOPDataList.add(newDataOwpBOP);
					}
					operationWindow.close();
				}
			}

			TCComponent[] solutionItems = UIGetValuesUtility.getRelatedComponents(dmService, changeRevision, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_SOL_ITEMS);
			if (solutionItems != null && solutionItems.length > 0) {
				Set<String> operationIDs = new HashSet<String>();
				for (TCComponent solutionItem : solutionItems) {
					TCComponentItemRevision operations = (TCComponentItemRevision) solutionItem;
					operationIDs.add(operations.getProperty("item_id"));
				}
				TCComponent[] operationsList = UIGetValuesUtility.searchStruture(session, String.join(";", operationIDs), windowConfig.FindIn);
				if (operationsList != null && operationsList.length > 0) {
					for (TCComponent bomline : operationsList) {
						TCComponentBOMLine bomlinetag = (TCComponentBOMLine) bomline;
						if (!UIGetValuesUtility.hasMaterials(bomlinetag)) {
							UnitBOMBOPModel newDataOwpBOP = new UnitBOMBOPModel();
							newDataOwpBOP.generateOWPBOPItem(bomline, "A");
							owpBOPDataList.add(newDataOwpBOP);
						}
					}
				}
			}
		} catch (TCException e1) {
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

		return true;
	}

	private void loadMCNInformation() {
		TCHelper.getInstance().dmService.getProperties(new TCComponent[] { TCHelper.getInstance().changeObject }, new String[] { PropertyDefines.ITEM_ID, PropertyDefines.ECM_PLANT, PropertyDefines.REL_IMPACT_SHOP });
		try {
			publish(PropertyDefines.UI_STORE, new ProcessStatus("Update MCN Info"));
			String plant = TCHelper.getInstance().changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);
			String shop = TCHelper.getInstance().changeObject.getPropertyDisplayableValue("vf6_shop");
			String MCN_SAPID = TCHelper.getInstance().changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
			String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);
			mcn = new MCNInformation();
			mcn.setPlant(plant);
			mcn.setShop(shop);
			mcn.setSapID(MCN_SAPID);
			mcn.setMcnID(MCN);
			// send to update ui
			publish(PropertyDefines.UI_STORE, mcn);
			publish(PropertyDefines.REPORT_PROCESSOR_NAME, mcn);
			publish(PropertyDefines.CONNECTION_PROCESSOR_NAME, mcn);
			publish(PropertyDefines.UI_STORE, new ProcessStatus("Update MCN Info done"));
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
	}
}
