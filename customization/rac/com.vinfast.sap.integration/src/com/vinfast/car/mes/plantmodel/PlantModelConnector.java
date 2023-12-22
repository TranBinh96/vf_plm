package com.vinfast.car.mes.plantmodel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.teamcenter.integration.arch.ConnectorAbstract;
import com.teamcenter.integration.arch.ModelAbstract;
import com.teamcenter.integration.arch.TCHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.integration.model.ActionCommand;
import com.vinfast.integration.model.ActionCommand.Command;
import com.vinfast.integration.model.PlantModelDataSend;
import com.vinfast.integration.model.PlantReport;
import com.vinfast.integration.model.ProcessStatus;
import com.vinfast.integration.model.ProcessStatus.ProcessUpdateType;
import com.vinfast.integration.model.ReportMessage.UpdateType;
import com.vinfast.sap.services.CallPlantModelWebService;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class PlantModelConnector extends ConnectorAbstract{

	public PlantModelConnector(String processName) {
		super(processName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() {
		//do nothing
	}

	@Override
	public boolean send() {
		System.out.println("Start Send Data!");
		int count = 0;
		String logFolder = UIGetValuesUtility.createLogFolder("MCN"+this.getMcn().getMcnID());
		String userName = TCHelper.getInstance().session.getUserName();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		for(ModelAbstract absData :  this.getData2Send()) {
			PlantModelDataSend data2Send = (PlantModelDataSend)absData;
			if(data2Send == null) {
				continue;
			}
				

			TCComponentItemRevision plantRev = data2Send.getItem();
			ArrayList<HashMap<String, StringBuffer>> valueList = data2Send.getData();

			String workPlaceID = "";
			
			for(HashMap<String, StringBuffer> value : valueList) {
				for (Map.Entry<String, StringBuffer> valueEntry : value.entrySet()) {
					String wsID = valueEntry.getKey();
					if(workPlaceID.equals("")) {
						workPlaceID = wsID;
					}else {
						workPlaceID = workPlaceID+","+wsID;
					}
					StringBuffer transferData = valueEntry.getValue();

					if(this.getServerInfo().getServerType().equals(PropertyDefines.SERVER_DEV_TEST)) {
					
						String sequenceID = UIGetValuesUtility.getSequenceID() ;
						
						try {
							String inputString = transferData.toString().replaceAll("&", "_") ;
							BufferedWriter writer = new BufferedWriter(new FileWriter(logFolder+"\\"+workPlaceID+"_"+sequenceID+".xml"));
							writer.write(inputString);
							writer.newLine();
							writer.close();
							publish(PropertyDefines.UI_STORE, new ProcessStatus(ProcessUpdateType.UPDATE_PROCESS_BAR));
						} catch (IOException e) {
							e.printStackTrace();
						}
						continue;
					}else {
						TCHelper.getInstance().dmService.getProperties(new TCComponent[] {plantRev}, new String[] {PropertyDefines.ITEM_ID,PropertyDefines.ITEM_REV_ID,PropertyDefines.REV_TO_MES});
						
						try {
							String ID = plantRev.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
							String Rev = plantRev.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
							int retCode = new CallPlantModelWebService().callService(transferData, workPlaceID, this.getServerInfo().getIp(), logFolder);

							switch(retCode) {

							case 200:
								if(this.getServerInfo().getServerType().equals(PropertyDefines.SERVER_PRODUCTION)) {
									LocalDateTime now = LocalDateTime.now();  
									String setValue =  workPlaceID+"~"+userName+"~"+dtf.format(now);
									UIGetValuesUtility.setProperty(TCHelper.getInstance().dmService, plantRev, PropertyDefines.REV_TO_MES, setValue);
									//update report
									PlantReport rp = new PlantReport();
									rp.setNo(Integer.toString(count++));
									rp.setPlantID(String.format("%s/%s", ID, Rev));
									rp.setMessage("WorkStation Station is successfully transferred to MES.");
									rp.setStation(wsID);
									PlantReport.setServer(this.getServerInfo().getIp());
									rp.setResult("Success");
									rp.setType(UpdateType.UPDATE_BODY_INFO);
									publish(PropertyDefines.REPORT_PROCESSOR_NAME, rp);
								}

								
								break;

							case 401:
								PlantReport rp = new PlantReport();
								rp.setNo(Integer.toString(count++));
								rp.setPlantID(String.format("%s/%s", ID, Rev));
								rp.setMessage("WorkStation Station is fail to transferred to MES.");
								rp.setStation(wsID);
								PlantReport.setServer(this.getServerInfo().getIp());
								rp.setResult("Error");
								rp.setType(UpdateType.UPDATE_BODY_ERROR);
								publish(PropertyDefines.REPORT_PROCESSOR_NAME, rp);
								break;

							default:

								MessageBox.post("Unable to connect to the MES system. Please re-check your connection or contact MES administrator", "Error", MessageBox.ERROR);
								break;
							}
						
						} catch (NotLoadedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}	
			}
		}
		publish(PropertyDefines.REPORT_PROCESSOR_NAME, new ActionCommand(Command.COMMAND_POPUP_REPORT));
		return true;
	}
}
