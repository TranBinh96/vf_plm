package com.vinfast.car.mes.plantmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.teamcenter.integration.arch.BusinessHandlerAbstract;
import com.teamcenter.integration.arch.ModelAbstract;
import com.teamcenter.integration.arch.TCHelper;
import com.teamcenter.integration.model.MCNInformation;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.ActionCommand;
import com.vinfast.integration.model.ActionCommand.Command;
import com.vinfast.integration.model.NoticeMessage;
import com.vinfast.integration.model.NoticeMessage.NoticeType;
import com.vinfast.integration.model.PlantModelDataSend;
import com.vinfast.integration.model.PlantReport;
import com.vinfast.integration.model.ProcessStatus;
import com.vinfast.integration.model.ProcessStatus.ProcessUpdateType;
import com.vinfast.integration.model.ReportMessage;
import com.vinfast.integration.model.ReportMessage.UpdateType;
import com.vinfast.integration.model.ServerInformation;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class PlantModelBusiness extends BusinessHandlerAbstract {
	TCComponentItemRevision obj_shopTopNode = null;
	TCComponentItemRevision obj_Shop = null;
	TCComponent[] workStations = null;
	MCNInformation mcn = null;
	ServerInformation serverInfo = null;
	int count = 0;
	PlantModelDataHelper helper = new PlantModelDataHelper();

	public PlantModelBusiness(String processName) {
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

	private boolean prepare() {
		if (serverInfo == null) {
			publish(PropertyDefines.UI_STORE, new NoticeMessage("Please select server first!", NoticeType.ERROR));
			return false;
		}
		publish(PropertyDefines.REPORT_PROCESSOR_NAME, new ActionCommand(Command.COMMAND_INIT_REPORT));
		// 1. get workstations
		publish(PropertyDefines.UI_STORE, new ProcessStatus("Loading solution folder"));
		workStations = UIGetValuesUtility.getRelatedComponents(TCHelper.getInstance().dmService, TCHelper.getInstance().changeObject, new String[] { PropertyDefines.TYPE_MESTATIONREVISION }, PropertyDefines.REL_SOL_ITEMS);
		if (workStations == null || workStations.length < 1) {
			publish(PropertyDefines.UI_STORE, new NoticeMessage("No Stations found to transfer in Solution Items folder.", NoticeType.ERROR));
			return false;
		}
		ProcessStatus status = new ProcessStatus(ProcessUpdateType.INIT_TOTAL);
		status.setTotal(workStations.length);
		publish(PropertyDefines.UI_STORE, status);

		// 2.get impacted shop
		publish(PropertyDefines.UI_STORE, new ProcessStatus("Loading impacted shop"));
		TCComponent[] impactedShop;
		try {
			impactedShop = TCHelper.getInstance().changeObject.getRelatedComponents(PropertyDefines.REL_IMPACT_SHOP);
			if (impactedShop.length == 0 || impactedShop.length > 1) {
				publish(PropertyDefines.UI_STORE, new NoticeMessage("No or more items exists in impacted shop.", NoticeType.ERROR));
				return false;
			} else {
				obj_Shop = (TCComponentItemRevision) impactedShop[0];

				obj_shopTopNode = UIGetValuesUtility.getTopLevelItemRevision(TCHelper.getInstance().session, obj_Shop, PropertyDefines.RULE_WORKING);

				String err = UIGetValuesUtility.checkValidShop(obj_Shop, mcn.getPlant());

				if (!err.equals("")) {
					publish(PropertyDefines.UI_STORE, new NoticeMessage(err, NoticeType.ERROR));
					return false;
				}
			}
		} catch (TCException e) {
			publish(PropertyDefines.UI_STORE, new NoticeMessage("No or more items exists in impacted shop.", NoticeType.ERROR));
			return false;
		}
		publish(PropertyDefines.UI_STORE, new ProcessStatus(ProcessUpdateType.UPDATE_PROCESS_BAR));
		// 3.get work stations Id that need to be transfer
		publish(PropertyDefines.UI_STORE, new ProcessStatus("Check transfer to mes flag"));
		ArrayList<String> transferIDs = listTransferToMES(serverInfo.getServerType());

		// 4. open plant model view
		publish(PropertyDefines.UI_STORE, new ProcessStatus("Loading plantmodel"));
		HashMap<TCComponentItemRevision, ArrayList<HashMap<String, StringBuffer>>> data2Send = new HashMap<TCComponentItemRevision, ArrayList<HashMap<String, StringBuffer>>>();
		try {
			data2Send = helper.loadDataPlantView(transferIDs, TCHelper.getInstance().session, obj_shopTopNode, obj_Shop);
			for (ReportMessage msg : helper.getListReport()) {
				publish(PropertyDefines.REPORT_PROCESSOR_NAME, msg);
			}
			if (data2Send.size() > 0) {
				publish(PropertyDefines.REPORT_PROCESSOR_NAME, new ActionCommand(Command.COMMAND_POPUP_REPORT));
				publish(PropertyDefines.UI_STORE, new ActionCommand(Command.ALLOW_TO_TRANSFER));
				forwardDataToConnector(data2Send);
				return true;
			}
		} catch (NotLoadedException | TCException e) {
			publish(PropertyDefines.UI_STORE, new NoticeMessage("An exception occurs, please contact Admin", NoticeType.ERROR));
			return false;
		}
		return false;
	}

	private void loadMCNInformation() {
		TCHelper.getInstance().dmService.getProperties(new TCComponent[] { TCHelper.getInstance().changeObject }, new String[] { PropertyDefines.ITEM_ID, PropertyDefines.ECM_PLANT, PropertyDefines.REL_IMPACT_SHOP });
		try {
			publish(PropertyDefines.UI_STORE, new ProcessStatus("Update MCN Info"));
			String plant = TCHelper.getInstance().changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);
			String MCN_SAPID = TCHelper.getInstance().changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
			String MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);
			mcn = new MCNInformation();
			mcn.setPlant(plant);
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

	TCComponent[] getWorkStationsInSolutionFolder() {
		TCComponent[] workstations = UIGetValuesUtility.getRelatedComponents(TCHelper.getInstance().dmService, TCHelper.getInstance().changeObject, new String[] { PropertyDefines.TYPE_MESTATIONREVISION }, PropertyDefines.REL_SOL_ITEMS);
		return workstations;
	}

	ArrayList<String> listTransferToMES(String server) {
		ArrayList<String> transferIDs = new ArrayList<String>();

		for (TCComponent MEStation : workStations) {
			TCComponentItemRevision MEStnRevision = (TCComponentItemRevision) MEStation;
			String MESTNID;
			try {
				MESTNID = MEStnRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				String MESTNRevID = MEStnRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
				String MEStationID = MEStnRevision.getPropertyDisplayableValue(PropertyDefines.REV_TO_MES);
				if (server.equals(PropertyDefines.SERVER_PRODUCTION)) {
					if (MEStationID.length() == 0) {
						transferIDs.add(MESTNID);
					} else {
						PlantReport rp = new PlantReport();
						rp.setNo(Integer.toString(count));
						rp.setPlantID(String.format("%s/%s", MESTNID, MESTNRevID));
						rp.setMessage("WorkStation Station already transferred to MES.");
						rp.setStation(MEStationID);
						PlantReport.setServer(server);
						rp.setResult("Info");
						rp.setType(UpdateType.UPDATE_BODY_INFO);
						publish(PropertyDefines.REPORT_PROCESSOR_NAME, rp);
					}
				} else {
					transferIDs.add(MESTNID);
				}
			} catch (NotLoadedException e) {
				e.printStackTrace();
			}
		}
		return transferIDs;
	}

	void forwardDataToConnector(HashMap<TCComponentItemRevision, ArrayList<HashMap<String, StringBuffer>>> data) {
		for (Entry<TCComponentItemRevision, ArrayList<HashMap<String, StringBuffer>>> entry : data.entrySet()) {
			PlantModelDataSend msg = new PlantModelDataSend();
			msg.setItem(entry.getKey());
			msg.setData(entry.getValue());
			publish(PropertyDefines.CONNECTION_PROCESSOR_NAME, msg);
		}
		publish(PropertyDefines.UI_STORE, new ProcessStatus("Ready for transfer"));
	}
}
