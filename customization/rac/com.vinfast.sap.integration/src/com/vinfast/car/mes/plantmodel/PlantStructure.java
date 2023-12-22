package com.vinfast.car.mes.plantmodel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.integration.ulti.BOMManagement;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.dialogs.MESTransferDialog;
import com.vinfast.sap.services.CallPlantModelWebService;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class PlantStructure extends AbstractHandler {
	private TCSession session;
	private TCComponentItemRevision selectedObject = null;
	private MESTransferDialog dlg = null;
	private DataManagementService dmCoreService = null;
	private BOMManagement bomManager = null;

	private TCComponentItemRevision obj_Shop = null;
	private TCComponentItemRevision obj_shopTopNode = null;
	private File logFile = null;
	private HashMap<TCComponentItemRevision, ArrayList<HashMap<String, StringBuffer>>> transferToMES = null;
	private int count = 1;
	private int percentage = 1;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmCoreService = DataManagementService.getService(session);

			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

			if (validObjectSelect(targetComponents)) {
				MessageBox.post("Please Select 1 MCN Revision.", "Error", MessageBox.ERROR);
				return null;
			}

			if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
				MessageBox.post("You are not authorized to transfer MCN.", "Please check group/role and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			String company = PropertyDefines.VIN_FAST;
			String command = event.getCommand().toString();
			if (command.contains(PropertyDefines.VIN_FAST_ELECTRIC)) {
				company = PropertyDefines.VIN_FAST_ELECTRIC;
			} else if (command.contains(PropertyDefines.VIN_ES)) {
				company = PropertyDefines.VIN_ES;
			}
			bomManager = new BOMManagement(session, selectedObject, company);
			obj_Shop = (TCComponentItemRevision) bomManager.getShopItem();
			if(obj_Shop == null) {
				MessageBox.post("No or more items exists in impacted shop.", "Error", MessageBox.ERROR);
				return null;
			}

			obj_shopTopNode = bomManager.getTopLevelItemRevision();
			String err = UIGetValuesUtility.checkValidShop(obj_shopTopNode, bomManager.getPlant());
			if (!err.equals("")) {
				MessageBox.post(err, "Error", MessageBox.ERROR);
				return null;
			}

			if(bomManager.getSolutionItems(PropertyDefines.TYPE_MESTATIONREVISION).length == 0) {
				MessageBox.post("No Stations found to transfer in Solution Items folder.", "Error", MessageBox.ERROR);
				return null;
			}

			dlg = new MESTransferDialog(new Shell(), session, company);
			dlg.create();
			dlg.setTitle("Plant Model(s) Transfer");
			dlg.txtPlant.setText(bomManager.getPlant());
			dlg.txtMCN.setText(bomManager.getMCN());
			dlg.txtShop.setText(bomManager.getShop());
			dlg.setTotal(10);
			dlg.btnSave.setEnabled(false);
			dlg.btnPrepare.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					if (dlg.cbServer.getText().isEmpty() || dlg.cbIP.getText().isEmpty()) {
						MessageBox.post("Please choose server to transfer.", "Error", MessageBox.ERROR);
						return;
					}

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							boolean isError = prepareData();
							if(isError == false) {
								dlg.btnSave.setEnabled(true);
							}
						}
					});
				}
			});
			dlg.btnSave.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					if (dlg.cbServer.getText().isEmpty() || dlg.cbIP.getText().isEmpty()) {
						MessageBox.post("Please choose server to transfer.", "Error", MessageBox.ERROR);
						return;
					}
					
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							transferData();
							dlg.getShell().dispose();
						}
					});
				}
			});

			dlg.open();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
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

	protected boolean prepareData() {
		String MES_SERVER_IP = dlg.cbIP.getText();
		String mcnValue = dlg.txtMCN.getText();
		String serverValue = dlg.cbServer.getText();
		TCComponent[] solutionItems = bomManager.getSolutionItems(PropertyDefines.TYPE_MESTATIONREVISION);
		UIGetValuesUtility.createLogFolder("MCN" + mcnValue);
		
		if(UIGetValuesUtility.isServerOn(dlg.cbIP.getText()) == false) {
			MessageBox.post("Connection to "+dlg.cbIP.getText()+" failed. Check with MES team if server is running", "Error", MessageBox.ERROR);
			return false;
		}
	
		if(UIGetValuesUtility.isPortOpen(dlg.cbIP.getText(), 80) == false) {
			MessageBox.post("Connection to "+dlg.cbIP.getText()+" on port:"+"80"+" is blocked by firewall. Check with network team to open port", "Error", MessageBox.ERROR);
			return false;
		}
		
		count = 1;
		percentage = 1;
		boolean isError = false;
		try {
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("<html><body>");
			Logger.bufferResponse(Logger.BUFFERRESPONSE_DETAILS, new String[] { "User :" + session.getUserName(), "MCN :"+mcnValue, "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) }, strBuilder);
			Logger.bufferResponse(Logger.BUFFERRESPONSE_HEADER, new String[] { "S.No", "Plant ID/Rev", "Message", "Station", "Server", "Result" }, strBuilder);
			int total = solutionItems.length + 3;
			ArrayList<String> transferIDs = new ArrayList<String>();

			for (TCComponent MEStation : solutionItems) {

				TCComponentItemRevision MEStnRevision = (TCComponentItemRevision) MEStation;
				String MESTNID = MEStnRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				String MESTNRevID = MEStnRevision.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
				String MEStationID = MEStnRevision.getPropertyDisplayableValue(PropertyDefines.REV_TO_MES);

				if (serverValue.equals("PRODUCTION")) {

					if (MEStationID.isEmpty()) {
						transferIDs.add(MESTNID);
					} else {
						dlg.setProgressStatus(getProgress(percentage, total));
						Logger.bufferResponse(Logger.BUFFERRESPONSE_PRINT, new String[] { Integer.toString(count), MESTNID + "/" + MESTNRevID, "WorkStation Station already transferred to MES.", MEStationID, MES_SERVER_IP, "Info" }, strBuilder);
						count++;
						percentage++;
					}

				} else {
					transferIDs.add(MESTNID);
				}
			}

			if (!transferIDs.isEmpty()) {
				TCComponent BOMLine = null;
				TCComponent BOPLine = null;
				TCComponent traverseStructure = null;

				String searchIDs = String.join(";", transferIDs);// UIGetValuesUtility.convertArrayToString(transferIDs, ";");
				OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(session, obj_shopTopNode.getItem());
				dlg.setProgressStatus(getProgress(percentage, total));
				percentage++;

				for (OpenContextInfo views : createdBOMViews) {
					if (views.context.getType().equals("BOMLine")) {
						BOMLine = views.context;
					}
					if (views.context.getType().equals("Mfg0BvrWorkarea")) {
						BOPLine = views.context;
					}
				}

				TCComponentForm topLineMasterForm = (TCComponentForm) obj_Shop.getItem().getRelatedComponent("IMAN_master_form");
				String searchPlantModel = topLineMasterForm.getProperty("user_data_3");

				if (!searchPlantModel.isEmpty()) {
					TCComponent[] plantModelList = UIGetValuesUtility.searchStruture(session, searchPlantModel, BOPLine);
					if (plantModelList != null && plantModelList.length > 0) {
						traverseStructure = plantModelList[0];
						UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
					} else {
						TCComponentItem plantModel = UIGetValuesUtility.findItem(session, searchPlantModel);
						OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(session, plantModel);
						BOPLine = createdBOPView[0].context;
						UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
						traverseStructure = BOPLine;
					}
				} else {
					UIGetValuesUtility.setViewReference(session, BOMLine, BOPLine);
					traverseStructure = BOPLine;
				}

				dlg.setProgressStatus(getProgress(percentage, total));
				percentage++;

				TCComponent[] stationsList = UIGetValuesUtility.searchStruture(session, searchIDs, traverseStructure);
				if (stationsList != null && stationsList.length > 0) {
					dlg.setProgressStatus(getProgress(percentage, total));
					percentage++;

					transferToMES = new HashMap<TCComponentItemRevision, ArrayList<HashMap<String, StringBuffer>>>();
					for (TCComponent plantStation : stationsList) {
						String workstationID = "";
						String stationID = plantStation.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
						String stationRev = plantStation.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_ID);
						TCComponentBOMLine plantModel = (TCComponentBOMLine) plantStation;
						HashMap<String, StringBuffer> stationDetails = processAddStation(plantModel);
						if (stationDetails == null) {
							dlg.setProgressStatus(getProgress(percentage, total));
							Logger.bufferResponse(Logger.BUFFERRESPONSE_PRINT, new String[] { Integer.toString(count), stationID + "/" + stationRev, "Wrong Workstation Information.", workstationID, MES_SERVER_IP, "Error" }, strBuilder);
							isError = true;
							count++;
							percentage++;
						} else {
							if (transferToMES.containsKey(plantModel.getItemRevision())) {
								ArrayList<HashMap<String, StringBuffer>> list = transferToMES.get(plantModel.getItemRevision());
								list.add(stationDetails);
							} else {
								ArrayList<HashMap<String, StringBuffer>> stationList = new ArrayList<HashMap<String, StringBuffer>>();
								stationList.add(stationDetails);
								transferToMES.put(plantModel.getItemRevision(), stationList);
							}
						}

						transferIDs.remove(stationID);
					}

					UIGetValuesUtility.closeAllContext(session, createdBOMViews);
				}

				if (!transferIDs.isEmpty()) {
					for (String ID : transferIDs) {
						Logger.bufferResponse(Logger.BUFFERRESPONSE_PRINT, new String[] { Integer.toString(count), ID, "Not found in Plant Model", "-", "-", "Error" }, strBuilder);
						dlg.setProgressStatus(getProgress(percentage, total));
						count++;
						percentage++;
					}
				}

				if (transferToMES != null) {
					if (!isError && !transferToMES.isEmpty()) {
						for (Entry<TCComponentItemRevision, ArrayList<HashMap<String, StringBuffer>>> entry : transferToMES.entrySet()) {
							TCComponentItemRevision plantRev = entry.getKey();
							ArrayList<HashMap<String, StringBuffer>> valueList = entry.getValue();

							String workPlaceID = "";
							for (HashMap<String, StringBuffer> value : valueList) {
								for (Map.Entry<String, StringBuffer> valueEntry : value.entrySet()) {
									dlg.setProgressStatus(getProgress(percentage, total));
									String wsID = valueEntry.getKey();
									if (workPlaceID.equals("")) {
										workPlaceID = wsID;
									} else {
										workPlaceID = workPlaceID + "," + wsID;
									}
									String ID = plantRev.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
									String Rev = plantRev.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);
									Logger.bufferResponse(Logger.BUFFERRESPONSE_PRINT, new String[] { Integer.toString(count), ID + "/" + Rev, "PLM: Ready to transfer to MES", wsID, MES_SERVER_IP, "Info" }, strBuilder);
									count++;
									percentage++;

								}
							}
						}
					}
				}
			} else {
				dlg.setProgressStatus(100);
			}

			strBuilder.append("</table>");
			strBuilder.append("</body></html>");
			dlg.brwReport.setText(strBuilder.toString());
			/*logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "PLANTMODEL");
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						StringViewerDialog viewdialog = new StringViewerDialog(logFile);
						viewdialog.setTitle("Transfer Status");
						viewdialog.setSize(600, 400);
						viewdialog.setLocationRelativeTo(null);
						viewdialog.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});*/
		} catch (TCException e1) {
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isError;
	}

	public void transferData() {
		count = 1;
		percentage = 1;
		String serverValue = dlg.cbServer.getText();
		String userName = session.getUserName();
		String MES_SERVER_IP = dlg.cbIP.getText();
		String mcnValue = dlg.txtMCN.getText();
		dlg.brwReport.setText("");
		String logFolder = UIGetValuesUtility.createLogFolder("MCN" + mcnValue);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<html><body>");
		Logger.bufferResponse(Logger.BUFFERRESPONSE_DETAILS, new String[] { "User :" + session.getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) }, strBuilder);
		Logger.bufferResponse(Logger.BUFFERRESPONSE_HEADER, new String[] { "S.No", "Plant ID/Rev", "Message", "Station", "Server", "Result" }, strBuilder);
		try {
			if (transferToMES != null) {
					for (Entry<TCComponentItemRevision, ArrayList<HashMap<String, StringBuffer>>> entry : transferToMES.entrySet()) {
						TCComponentItemRevision plantRev = entry.getKey();
						ArrayList<HashMap<String, StringBuffer>> valueList = entry.getValue();

						String workPlaceID = "";
						for (HashMap<String, StringBuffer> value : valueList) {
							for (Map.Entry<String, StringBuffer> valueEntry : value.entrySet()) {
								dlg.setProgressStatus(getProgress(percentage, transferToMES.size()));
								String wsID = valueEntry.getKey();
								if (workPlaceID.equals("")) {
									workPlaceID = wsID;
								} else {
									workPlaceID = workPlaceID + "," + wsID;
								}
								StringBuffer transferData = valueEntry.getValue();
								String ID = plantRev.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
								String Rev = plantRev.getPropertyDisplayableValue(PropertyDefines.ITEM_REV_ID);

								int retCode = new CallPlantModelWebService().callService(transferData, workPlaceID, MES_SERVER_IP, logFolder);
								switch (retCode) {
								case 200:
									if (serverValue.equals("PRODUCTION")) {
										LocalDateTime now = LocalDateTime.now();
										String setValue = workPlaceID + "~" + userName + "~" + dtf.format(now);
										UIGetValuesUtility.setProperty(dmCoreService, plantRev, PropertyDefines.REV_TO_MES, setValue);
									}

									Logger.bufferResponse(Logger.BUFFERRESPONSE_PRINT, new String[] { Integer.toString(count), ID + "/" + Rev, "PLM: Transferred to MES", wsID, MES_SERVER_IP, "Success" }, strBuilder);
									count++;
									percentage++;
									break;
								case 401:
									Logger.bufferResponse(Logger.BUFFERRESPONSE_PRINT, new String[] { Integer.toString(count), ID + "/" + Rev, "MES: Error Code:-" + retCode + " Failed transfer to MES", "-", MES_SERVER_IP, "Error" }, strBuilder);
									count++;
									percentage++;
									break;
								default:
									MessageBox.post("Unable to connect to the MES system. Please re-check your connection or contact MES administrator", "Error", MessageBox.ERROR);
									return;
								}
							}
						}
					}
				
				strBuilder.append("</table>");
				strBuilder.append("</body></html>");
				dlg.brwReport.setText(strBuilder.toString());
				logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, "PLANTMODEL");
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							StringViewerDialog viewdialog = new StringViewerDialog(logFile);
							viewdialog.setTitle("Transfer Status");
							viewdialog.setSize(600, 400);
							viewdialog.setLocationRelativeTo(null);
							viewdialog.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				
				if (serverValue.equals("PRODUCTION")) {
					TCComponentDataset newDataset = UIGetValuesUtility.createDataset(dmCoreService, selectedObject, "IMAN_specification", logFile.getName(), "Transfer Report", "HTML", "IExplore");
					if (newDataset != null) {
						UIGetValuesUtility.uploadNamedReference(session, newDataset, logFile, "HTML", true, true);
					}
				}
			}
		}catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, StringBuffer> processAddStation(TCComponentBOMLine MEStation) {
		HashMap<String, TCComponent> workStationDetails = getWorkstationInformation(MEStation);
		HashMap<String, StringBuffer> dataMap = null;
		if (workStationDetails.size() == 4) {

			TCComponent[] components = new TCComponent[workStationDetails.size()];
			int i = 0;
			for (String key : workStationDetails.keySet()) {
				components[i] = workStationDetails.get(key);
				i++;
			}

			dmCoreService.getProperties(components, new String[] { PropertyDefines.BOM_ITEM_REV_NAME });
			dataMap = new HashMap<String, StringBuffer>();

			StringBuffer dataString = new StringBuffer();
			dataString.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			dataString.append("<Plants xmlns=\"VINFAST.Manufacturing.Engineering\">");

			try {
				String plantName = workStationDetails.get("plant").getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_NAME);
				String plantCode = plantName.substring(0, 4);

				dataString.append("<Plant ID=\"" + plantCode.trim() + "\">");
				dataString.append("<Name>" + plantName.trim() + "</Name>");
				dataString.append("<Areas>");

				String ShopName = workStationDetails.get("shop").getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_NAME).trim();
				String ShopCode = plantCode + "_" + ShopName.substring(0, 2).trim();

				dataString.append("<Area ID=\"" + ShopCode + "\">");
				dataString.append("<Name>" + ShopName + "</Name>");
				dataString.append("<ProductionLines>");

				// LINE Code
				String LineName = workStationDetails.get("line").getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_NAME).trim();
				String ProductionLine = ShopCode + LineName.substring(0, 2).trim();
				String LineCode = ProductionLine + "_S".trim();

				dataString.append("<ProductionLine ID=\"" + ProductionLine + "\">");
				dataString.append("<Name>" + LineName + "</Name>");
				dataString.append("<Sections>");
				dataString.append("<Section ID=\"" + LineCode + "\">");
				dataString.append("<Name>" + LineName + "</Name>");

				dataString.append("<Workstations>");
				String workstationCode = "";
				String objectName = workStationDetails.get("workstation").getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_NAME);
				if (objectName.contains("-")) {
					String[] tempArr = objectName.split("-");
					workstationCode = ProductionLine + tempArr[1].substring(0, 2).trim();
				} else {
					workstationCode = ProductionLine + objectName.substring(0, 2).trim();
				}
				dataString.append("<Workstation ID=\"" + workstationCode + "\">");
				dataString.append("<Name>" + workstationCode + "</Name>");
				dataString.append("<DomainID>" + LineName.trim() + "</DomainID>");

				dataString.append("<Workplaces>");

				// Station Codes
				String StationName = workStationDetails.get("workstation").getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_REV_NAME).trim();
				String workPlaceID = "";
				if (StationName.contains("-")) {
					String[] stationname = StationName.split("-");
					workPlaceID = ProductionLine + stationname[1].trim();
				} else {
					workPlaceID = ProductionLine + StationName;
				}

				dataString.append("<Workplace ID=\"" + workPlaceID + "\">");
				dataString.append("<Name>" + LineName.trim() + "</Name>");
				dataString.append("<DAC>" + StationName.trim() + "</DAC>");
				dataString.append("</Workplace>");
				dataString.append("</Workplaces>");

				String machineID = workStationDetails.get("workstation").getProperty(PropertyDefines.BOM_TOOL_ID);

				dataString.append("<Machines>");
				if (machineID.length() > 0) {
					String[] machines = machineID.split(",");
					for (int inx = 0; inx < machines.length; inx++) {
						dataString.append("<Machine ID=\"" + machines[inx].trim() + "\"/>");
					}
				}
				dataString.append("</Machines>");

				dataString.append("</Workstation>");
				dataString.append("</Workstations>");

				dataString.append("</Section>");
				dataString.append("</Sections>");

				dataString.append("</ProductionLine>");
				dataString.append("</ProductionLines>");

				dataString.append("</Area>");
				dataString.append("</Areas>");

				dataString.append("</Plant>");
				dataString.append("</Plants>");

				if (dataString.length() > 0) {
					dataMap.put(workPlaceID, dataString);
					return dataMap;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return dataMap;
	}

	private HashMap<String, TCComponent> getWorkstationInformation(TCComponentBOMLine MEStation) {
		HashMap<String, TCComponent> dataMap = new HashMap<String, TCComponent>();
		TCComponentBOMLine MEStationLine = null;
		TCComponentBOMLine MELineLine = null;
		TCComponentBOMLine MEShopLine = null;
		TCComponentBOMLine MEPlantLine = null;

		try {
			dmCoreService.getProperties(new TCComponent[] { MEStation }, new String[] { PropertyDefines.BOM_OBJECT_TYPE, PropertyDefines.BOM_PARENT });
			if (MEStation.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MESTATIONREVISION)) {
				MEStationLine = MEStation;
				TCComponentBOMLine LineRevisionLine = (TCComponentBOMLine) MEStation.getReferenceProperty(PropertyDefines.BOM_PARENT);
				dmCoreService.getProperties(new TCComponent[] { LineRevisionLine }, new String[] { PropertyDefines.BOM_OBJECT_TYPE, PropertyDefines.BOM_PARENT });

				if (LineRevisionLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MELINEREVISION)) {
					MELineLine = LineRevisionLine;
					TCComponentBOMLine MEShopRevisionLine = (TCComponentBOMLine) LineRevisionLine.getReferenceProperty(PropertyDefines.BOM_PARENT);
					if (MEShopRevisionLine != null
							&& (MEShopRevisionLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MESHOPREVISION)
									|| MEShopRevisionLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MELINEREVISION))) {

						MEShopLine = MEShopRevisionLine;
						TCComponentBOMLine MEPlantRevisionLine = (TCComponentBOMLine) MEShopLine.getReferenceProperty(PropertyDefines.BOM_PARENT);
						if (MEPlantRevisionLine != null && MEPlantRevisionLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MEPLANTREVISION)) {
							MEPlantLine = MEPlantRevisionLine;
						}
					} else {
						if (MEShopRevisionLine.getPropertyDisplayableValue(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_MEPLANTREVISION)) {
							MEShopLine = MELineLine;
							MEPlantLine = MEShopRevisionLine;
						}
					}
				}
			}

			if (MEStationLine != null) {
				dataMap.put("workstation", MEStationLine);
			}
			if (MELineLine != null) {
				dataMap.put("line", MELineLine);
			}
			if (MEShopLine != null) {
				dataMap.put("shop", MEShopLine);
			}
			if (MEPlantLine != null) {
				dataMap.put("plant", MEPlantLine);
			}
		} catch (TCException e1) {
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

		return dataMap;
	}

	public int getProgress(int current, int total) {
		float value = (float) current / (float) total;
		int percent = (int) (value * 100);
		return percent;
	}
}