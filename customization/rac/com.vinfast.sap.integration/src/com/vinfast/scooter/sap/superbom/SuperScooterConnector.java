package com.vinfast.scooter.sap.superbom;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.integration.arch.ConnectorAbstract;
import com.teamcenter.integration.arch.ModelAbstract;
import com.teamcenter.integration.arch.TCHelper;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.ActionCommand;
import com.vinfast.integration.model.ActionCommand.Command;
import com.vinfast.integration.model.ProcessStatus;
import com.vinfast.integration.model.ProcessStatus.ProcessUpdateType;
import com.vinfast.integration.model.ReportMessage.UpdateType;
import com.vinfast.integration.model.ScooterSuperBomBopDataSend;
import com.vinfast.integration.model.SuperScooterReport;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class SuperScooterConnector extends ConnectorAbstract{

	public SuperScooterConnector(String processName) {
		super(processName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	String auth = PropertyDefines.SERVER_SAP_VF_AUTH;

	@Override
	public boolean send() {
		publish(PropertyDefines.UI_STORE, new ProcessStatus("Transfering"));
		SAPURL SAPConnect		 	= new SAPURL();
		int count = 0;
		String logFolder = UIGetValuesUtility.createLogFolder("MCN"+this.getMcn().getMcnID());
		publish(PropertyDefines.UI_STORE, new ProcessStatus(0));
		ProcessStatus status = new ProcessStatus(ProcessUpdateType.INIT_TOTAL);
		status.setTotal(this.getData2Send().size());
		publish(PropertyDefines.UI_STORE, status);
		try {
			
		
		for(ModelAbstract absData :  this.getData2Send()) {
			publish(PropertyDefines.UI_STORE, new ProcessStatus(ProcessUpdateType.UPDATE_PROCESS_BAR));
			ScooterSuperBomBopDataSend data2Send = (ScooterSuperBomBopDataSend)absData;
			if(data2Send == null) {
				continue;
			}
			
			//bom
			TCComponentItemRevision item = data2Send.getItem();
			HashMap<String, String> bomMap = data2Send.getBomData();
			if(bomMap != null && bomMap.size() > 0) {
				String BOMFile = bomMap.get("PARTNO") + "_" + bomMap.get("BOMLINEID");
				String[]  message = null;
				
				if(this.getServerInfo().getServerType().equals(PropertyDefines.SERVER_DEV_TEST)) {
					message = CreateSoapHttpRequest.sendRequest(SAPConnect.esbomWebserviceURL(""), bomMap,SAPURL.ES_BOM_HEADER, SAPURL.ES_BOM_TAG, SAPURL.ES_BOM_NAMESPACE,"I_BOM_"+bomMap.get("ACTION")+"_"+ BOMFile, logFolder, auth);
				}else {
					message = CreateSoapHttpRequest.sendRequest(SAPConnect.esbomWebserviceURL(this.getServerInfo().getIp()), bomMap,SAPURL.ES_BOM_HEADER, SAPURL.ES_BOM_TAG, SAPURL.ES_BOM_NAMESPACE,"I_BOM_"+bomMap.get("ACTION")+"_"+ BOMFile, logFolder, auth);
					if(message[0].equals("S")){
						SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++), data2Send.getSubGroupId() , bomMap.get("PARTNO"),bomMap.get("BOMLINEID"),message[1] ,bomMap.get("ACTION"),"Success");
						rp.setType(UpdateType.UPDATE_BODY_INFO);
						publish(PropertyDefines.REPORT_PROCESSOR_NAME, rp);
					}else {
						SuperScooterReport rp = new SuperScooterReport(Integer.toString(count++), data2Send.getSubGroupId(), bomMap.get("PARTNO"),bomMap.get("BOMLINEID"),message[1],bomMap.get("ACTION"),"Error");
						rp.setType(UpdateType.UPDATE_BODY_ERROR);
						publish(PropertyDefines.REPORT_PROCESSOR_NAME, rp);
					}
				}

			}

			
			//bop
			ArrayList<HashMap<String, String>> bopMap = data2Send.getBopData();
			if(bopMap != null && bopMap.size() > 0) {
				for(HashMap<String, String> BOP : bopMap) {
					String BOPFile = BOP.get("BOPID") + "_" + BOP.get("WORKSTATION");
					String []msg = null;
					if(this.getServerInfo().getServerType().equals(PropertyDefines.SERVER_DEV_TEST)) {
						msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(""), BOP,SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE,"I_BOP_"+BOP.get("ACTION")+"_"+ BOPFile, logFolder, auth);
					}else {
						msg = CreateSoapHttpRequest.sendRequest(SAPConnect.assybopWebserviceURL(this.getServerInfo().getIp()), BOP,SAPURL.ASSY_BOP_HEADER, SAPURL.ASSY_BOP_TAG, SAPURL.ASSY_BOP_NAMESPACE,"I_BOP_"+BOP.get("ACTION")+"_"+ BOPFile, logFolder, auth);
						if(msg[0].equals("S")){
							SuperScooterReport rp = new SuperScooterReport(Integer.toString(count),data2Send.getSubGroupId(),BOP.get("BOPID"),BOP.get("BOMLINEID"),msg[1], BOP.get("ACTION"),"Success");
							rp.setType(UpdateType.UPDATE_BODY_INFO);
							publish(PropertyDefines.REPORT_PROCESSOR_NAME, rp);
						}else {				
							SuperScooterReport rp = new SuperScooterReport(Integer.toString(count),data2Send.getSubGroupId(),BOP.get("BOPID"),BOP.get("BOMLINEID"),msg[1], BOP.get("ACTION"),"Error");
							rp.setType(UpdateType.UPDATE_BODY_ERROR);
							publish(PropertyDefines.REPORT_PROCESSOR_NAME, rp);
						}
					}
				}
			}
			
			if(getServerInfo().getServerType().equals(PropertyDefines.SERVER_PRODUCTION) && item != null)
			{
				try {
					if(item.getPropertyDisplayableValue(PropertyDefines.REV_TO_SAP).length() == 0) {
						UIGetValuesUtility.setProperty(TCHelper.getInstance().dmService, item, "vf3_transfer_to_sap", "Yes");
					}
				} catch (NotLoadedException e) {
					e.printStackTrace();
				}	
			}
			
		}
		}catch(Exception e) {
			e.printStackTrace();
		}
		publish(PropertyDefines.REPORT_PROCESSOR_NAME, new ActionCommand(Command.COMMAND_POPUP_REPORT));
		return true;
	}

}
