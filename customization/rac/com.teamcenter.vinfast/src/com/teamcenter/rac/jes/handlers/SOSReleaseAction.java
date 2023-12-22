package com.teamcenter.rac.jes.handlers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.spire.xls.FileFormat;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.jes.SOSFormGenerate;
import com.teamcenter.rac.jes.SOSFormModel;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2010_09.DataManagement;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2014_06.Workflow.PerformActionInputInfo;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffInfo;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffs;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.dialog.VFJESSOSDialog;
import com.vf.utils.TCExtension;

public class SOSReleaseAction extends AbstractHandler {
	private TCSession session = null;
	private TCComponentBOPLine selectedObject = null;
	LinkedList<Hashtable<String, String>> activitiesList = null;
	String stationName = null;
	String stationRevNumber = null;
	String vehicleModel = "";
	String error = "";
	private ProgressMonitorDialog progressMonitorDialog = null;
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		InterfaceAIFComponent[] aifComp = AIFUtility.getCurrentApplication().getTargetComponents();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		try {
			if (validateSelection(aifComp) == true) {
				TCComponentItemRevision stationRevision = selectedObject.getItemRevision();
				HashMap<String, String> parentMap = getShopParents(selectedObject);
				if (parentMap != null) {
					File excelFile = SOSFormGenerate.getSOSExcelFile(stationRevision);
					if (excelFile == null) {
						MessageBox.post("SOS file not created. Please create SOS and release.", "Error", MessageBox.ERROR);
						return null;
					}
					VFJESSOSDialog dialog = new VFJESSOSDialog(new Shell(), session);
					dialog.create();
					dialog.itxtStation.setText(parentMap.get("Station"));
					dialog.icbChangeRequestType.setText("Other");
					dialog.icbChangeRequestType.setEnabled(false);
					dialog.getSearchButton().setEnabled(false);
					dialog.getTriggerButton().addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							error = "";
							if (progressMonitorDialog == null)
								progressMonitorDialog = new ProgressMonitorDialog(dialog.getShell());
							try {
								progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
									@Override
									public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
										monitor.beginTask("Release SOS...", IProgressMonitor.UNKNOWN);
										try {
											HashMap<String, TCComponentGroupMember[]> signoffProfiles = getSignoffUsers(parentMap.get("Shop"), parentMap.get("Line"), parentMap.get("Station"));
											if(signoffProfiles.isEmpty()) {
												error = "Signoff Profiles not found. Process initiated failed to release SOS.";
											}else {
												monitor.subTask("Updating SOS file...");
												File excelFile = SOSFormGenerate.getSOSExcelFile(stationRevision);
												if (excelFile == null) {
													error = "SOS file not created. Please create SOS and release.";
												}else {
													TCComponent[] released = stationRevision.getRelatedComponents("VF4_sos_soft_copy");
													SOSFormGenerate sosFormGenerate = new SOSFormGenerate(session, selectedObject, stationRevision, excelFile);
													String errorMessage = sosFormGenerate.generateSOS();
													if(!errorMessage.isEmpty()) {
														error = "Failed to Create/Update SOS. Please contact with admin.";
													}else {
														SOSFormModel sosForm = sosFormGenerate.getSOSFormModel();
														File sosExcelFile = sosFormGenerate.getSOSExcelFile();
														if(sosExcelFile != null) {
															monitor.subTask("Creating PDF...");
															String fileName = sosForm.getLocationName() + " (Ver: "+String.format("%02d", released.length+1)+")";
															String SOSFilePath =  createPDF(sosExcelFile);
															TCComponentDataset SOSDataset = createDataset(fileName, SOSFilePath);
															if(SOSDataset != null) {
																if(!errorMessage.isEmpty()) {
																	SOSDataset.delete();
																	errorMessage = "Failed to Create/Update SOS. Please contact with admin.";
																}else {
																	monitor.subTask("Attaching PDF...");
																	stationRevision.add("VF4_sos_soft_copy", new TCComponent[] {SOSDataset});
																	sosExcelFile.delete();
																	new File(SOSFilePath).delete();
																	monitor.subTask("Initiating Process...");
																	ServiceData data = TCExtension.TriggerProcess(new String[] {stationRevision.getUid(),SOSDataset.getUid()}, "VF_JES_SOS_Release", "", stationRevision.getObjectString(), session);
																	if(data.sizeOfPartialErrors() > 0) {
																		error = SoaUtil.buildErrorMessage(data);
																	}else {
																		monitor.subTask("Adding signoffs...");
																		TCComponent process = data.getCreatedObject(0);
																		setSignoffProfiles((TCComponentProcess)process, signoffProfiles);
																	}
																}
															}
														}
													}
												}
											}
										} catch (Exception e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
										monitor.done();
										if (monitor.isCanceled())
											throw new InterruptedException("The long running operation was cancelled");
									}
								});
								
								if(error.isEmpty() || error.isBlank()) {
									MessageBox.post("SOS created successfully and Process Initiated Successfully. Refer station for more details", "Success", MessageBox.INFORMATION);
									dialog.close();
								}else {
									MessageBox.post(error, "Error", MessageBox.ERROR);
								}
							} catch (InvocationTargetException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					});
					dialog.open();
				}
				else {
					MessageBox.post("Please open the whole BOP before viewing JES/SOS report!", "Info", MessageBox.INFORMATION);
				}

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private TCComponentDataset createDataset(String datasetName, String namedReferenceFile) {

		TCComponentDataset newDataset = null;
		try {

			TCComponentDatasetDefinitionType tcDatasetDefinitionType = (TCComponentDatasetDefinitionType) session.getTypeComponent("DatasetType");
			TCComponentDatasetDefinition datasetDefinition = tcDatasetDefinitionType.find("PDF");
			NamedReferenceContext[] namedReferenceContext = datasetDefinition.getNamedReferenceContexts();
			String namedRef = namedReferenceContext[0].getNamedReference();
			String[] type = { namedRef };
			TCComponentDatasetType datasetTypeComponent = (TCComponentDatasetType) session.getTypeComponent("Dataset");
			newDataset = datasetTypeComponent.create(datasetName, datasetName, datasetDefinition.toString());
			newDataset.setFiles(new String[] { namedReferenceFile }, type);

		} catch (TCException e) {
			e.printStackTrace();
		}
		return newDataset;
	}

	private String createPDF(File Flile) {

		String pdfFile = Flile.getAbsolutePath().replace("xlsx", "pdf");
		com.spire.xls.Workbook workbook = new com.spire.xls.Workbook();
		workbook.loadFromFile(Flile.getAbsolutePath());
		//workbook.getConverterSetting().setSheetFitToPage(true);
		workbook.saveToFile(pdfFile, FileFormat.PDF);
		return pdfFile;
	}

	private void setSignoffProfiles(TCComponentProcess process, HashMap<String, TCComponentGroupMember[]> signoffProfiles) {

		try {
			WorkflowService wfService = WorkflowService.getService(session);
			TCComponentTask rootTask = (process).getRootTask();
			TCComponentTask[] subtasks = rootTask.getSubtasks();
			TCComponentTask autoCompleteTask = null;
			for(int i = 0; i < subtasks.length; i++) {
				if(subtasks[i].getTaskType().equals("EPMTask") && subtasks[i].getName().equals("Auto-Assign-Signoff")) {
					autoCompleteTask = subtasks[i];
				}
				if(subtasks[i].getTaskType().compareToIgnoreCase("EPMReviewTask") == 0 ) {
					TCComponentTask[] selectSignOffTasks = subtasks[i].getSubtasks();
					for(TCComponentTask selectSignOffTask : selectSignOffTasks) {
						if(selectSignOffTask.getTaskType().equals("EPMSelectSignoffTask")  && selectSignOffTask.getName().equals("ME Controller Signoff")) {
							selectSignOffTask = subtasks[i].getSubtasks()[0];
							addSignoff(wfService, selectSignOffTask, signoffProfiles.get("ME"));
						} else if(selectSignOffTask.getTaskType().equals("EPMSelectSignoffTask")  && selectSignOffTask.getName().equals("GL Signoff (Shift 1)")) {
							selectSignOffTask = subtasks[i].getSubtasks()[0];
							addSignoff(wfService, selectSignOffTask, signoffProfiles.get("GL_Shift_1"));
						} else if(selectSignOffTask.getTaskType().equals("EPMSelectSignoffTask")  && selectSignOffTask.getName().equals("GL Signoff (Shift 2)")) {
							selectSignOffTask = subtasks[i].getSubtasks()[0];
							addSignoff(wfService, selectSignOffTask, signoffProfiles.get("GL_Shift_2"));
						} else if(selectSignOffTask.getTaskType().equals("EPMSelectSignoffTask")  && selectSignOffTask.getName().equals("GL Signoff (Shift 3)")) {
							selectSignOffTask = subtasks[i].getSubtasks()[0];
							addSignoff(wfService, selectSignOffTask, signoffProfiles.get("GL_Shift_3"));
						}
					}
				}
			}
			PerformActionInputInfo inputInfo =  new PerformActionInputInfo();
			inputInfo.actionableObject = autoCompleteTask;
			inputInfo.action = "SOA_EPM_complete_action";
			inputInfo.supportingValue = "SOA_EPM_completed";
			
			ServiceData data = wfService.performAction3(new PerformActionInputInfo[] {inputInfo});
			System.out.println(SoaUtil.buildErrorMessage(data));
			//autoCompleteTask.setProperty("task_result", "Completed");
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addSignoff(WorkflowService wfService,TCComponentTask selectSignOffTask, TCComponentGroupMember[] performerList) {

		try {
			if (performerList == null || performerList.length == 0) return;

			CreateSignoffs createSignoff = new CreateSignoffs();
			createSignoff.task = selectSignOffTask;
			createSignoff.signoffInfo = new CreateSignoffInfo[performerList.length];
			int j = 0;
			for (TCComponentGroupMember tcGroupMember : performerList) {
				createSignoff.signoffInfo[j] = new CreateSignoffInfo();
				createSignoff.signoffInfo[j].originType = "SOA_EPM_ORIGIN_UNDEFINED";
				createSignoff.signoffInfo[j].signoffAction = "SOA_EPM_Review";
				createSignoff.signoffInfo[j].signoffMember = tcGroupMember;
				j++;
			}
			ServiceData addSignoffsResponse = wfService.addSignoffs(new CreateSignoffs[] { createSignoff });
			addSignoffsResponse.sizeOfPartialErrors();
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
			propInfo.object = selectSignOffTask;
			propInfo.timestamp = Calendar.getInstance();
			propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
			propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
			propInfo.vecNameVal[0].name = "task_result";
			propInfo.vecNameVal[0].values = new String[]{"Completed"};
			DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[]{propInfo}, new String[0]);
			if(setPropertyResponse.data.sizeOfPartialErrors() > 0) {
				System.out.println("[assignPerformer]: " + Utils.HanlderServiceData(setPropertyResponse.data));

			}
			selectSignOffTask.fireComponentSaveEvent();
			selectSignOffTask.refresh();//selectSignOffTask.getProperty("task_result");
			selectSignOffTask.setProperty("task_result", "Completed");
			selectSignOffTask.fireComponentSaveEvent();
			//Complete selection signoff task if it started
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(selectSignOffTask.getTaskState().compareToIgnoreCase("Started") == 0) {
				PerformActionInputInfo paii = new PerformActionInputInfo();
				paii.clientId = "complete" + selectSignOffTask.getUid();
				paii.action = "SOA_EPM_complete_action";
				paii.actionableObject = selectSignOffTask;
				paii.propertyNameValues.put("comments", new String[]{"Auto Completed"});
				paii.supportingValue = "SOA_EPM_completed";

				ServiceData sd = wfService.performAction3(new PerformActionInputInfo[]{paii});
				if(sd.sizeOfPartialErrors() > 0) {
					System.out.println("[assignPerformer]: " + Utils.HanlderServiceData(sd));

				}
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean validateSelection(InterfaceAIFComponent[] aifComp) throws Exception {
		if(aifComp == null || aifComp.length != 1) {
			MessageBox.post("Please select a Station.", "Warning", MessageBox.WARNING);
			return false;
		}

		if (!(aifComp[0] instanceof TCComponentBOPLine)) {
			MessageBox.post("Please select a Station.", "Warning", MessageBox.WARNING);
			return false;
		}

		selectedObject = (TCComponentBOPLine) aifComp[0];
		if (!(selectedObject.getType().equals("Mfg0BvrProcessStation"))) {
			MessageBox.post("Please select a Station.", "Warning", MessageBox.WARNING);
			return false;
		}

		return true;
	}

	private HashMap<String, String> getShopParents(TCComponentBOMLine bopLine) {

		HashMap<String, String> parentMap = new HashMap<String, String>();
		parentMap.put("Station", "");
		parentMap.put("Line", "");
		parentMap.put("Shop", "");
		parentMap.put("Program", "");
		try {
			TCComponentBOPLine stationLine = (TCComponentBOPLine) bopLine;
			if (stationLine != null && stationLine.getType().equals("Mfg0BvrProcessStation")) {
				String stationName = stationLine.getProperty("bl_item_object_name");
				stationName = stationName.substring(stationName.length()-4, stationName.length());
				TCComponent line = stationLine.getReferenceProperty("bl_parent");
				TCComponentBOMLine lineLine = (TCComponentBOMLine) line;
				if(lineLine != null && lineLine.getType().equals("Mfg0BvrProcessLine")) {
					String lineName = lineLine.getProperty("bl_item_object_name");
					lineName = lineName.substring(0,2);
					parentMap.put("Line", lineName);
					parentMap.put("Station", lineName+"-"+stationName);
					TCComponent shop = lineLine.getReferenceProperty("bl_parent");
					TCComponentBOMLine shopLine = (TCComponentBOMLine) shop;
					if(shopLine != null && (shopLine.getType().equals("Mfg0BvrProcessLine") ||shopLine.getType().equals("Mfg0BvrProcessArea"))) {
						TCComponentItem shopItem = shopLine.getItem();
						String shopName = shopItem.getProperty("vf4_shop");
						if(shopName.equals("")) {
							shopName = shopLine.getProperty("bl_item_object_name");
						}
						parentMap.put("Shop", shopName);
						TCComponent program = shopLine.getReferenceProperty("bl_parent");
						TCComponentBOMLine programLine = (TCComponentBOMLine) program;
						TCComponentItem programItem = programLine.getItem();
						String programName = programItem.getProperty("vf4_program_model_name");
						if(programName.equals("")) {
							programName = programLine.getProperty("bl_item_object_name");
						}
						parentMap.put("Program", programName);
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		if(parentMap.get("Station").isEmpty() || parentMap.get("Shop").isEmpty() || parentMap.get("Program").isEmpty()) {
			return null;
		}
		return parentMap;
	}

	private HashMap<String, TCComponentGroupMember[]> getSignoffUsers(String shop, String line, String station) {
		HashMap<String, TCComponentGroupMember[]> groupMembers = new HashMap<String, TCComponentGroupMember[]>();

		TCPreferenceService prefService = session.getPreferenceService();
		String[] prefME = prefService.getStringValues("VF_JES_Assignment_ME");
		HashMap<String, String[]> MEList = extractSignOffs(prefME);
		String[] MEUserIDs = MEList.get(shop);
		if(MEUserIDs != null) {
			ArrayList<TCComponentGroupMember> userList = new ArrayList<TCComponentGroupMember>();
			for(String MEUserID : MEUserIDs) {
				TCComponentGroupMember member = TCExtension.GetGroupMemberByUserID(MEUserID.toString(), session);
				if(member != null) {
					userList.add(member);
				}
			}
			groupMembers.put("ME", userList.toArray(new TCComponentGroupMember[0]));
		}

		String[] prefGL1 = prefService.getStringValues("VF_JES_Assignment_GL_Shift_1");
		HashMap<String, String[]> GLList1 = extractSignOffs(prefGL1);

		String[] prefGL2 = prefService.getStringValues("VF_JES_Assignment_GL_Shift_2");
		HashMap<String, String[]> GLList2 = extractSignOffs(prefGL2);

		String[] prefGL3 = prefService.getStringValues("VF_JES_Assignment_GL_Shift_3");
		HashMap<String, String[]> GLList3 = extractSignOffs(prefGL3);

		TCComponentGroupMember[] glGroupMembersShift1 = createSignOffGroupMembers(shop, line, GLList1);
		groupMembers.put("GL_Shift_1", glGroupMembersShift1);

		TCComponentGroupMember[] glGroupMembersShift2 = createSignOffGroupMembers(shop, line, GLList2);
		groupMembers.put("GL_Shift_2", glGroupMembersShift2);

		TCComponentGroupMember[] glGroupMembersShift3 = createSignOffGroupMembers(shop, line, GLList3);
		groupMembers.put("GL_Shift_3", glGroupMembersShift3);

		return groupMembers;
	}

	private TCComponentGroupMember[] createSignOffGroupMembers(String shop, String line, HashMap<String, String[]> GLList1) {
		String[] GLUserIDs = GLList1.get(shop+"_"+line);
		TCComponentGroupMember[] ul = null;
		if(GLUserIDs != null) {
			ArrayList<TCComponentGroupMember> userList = new ArrayList<TCComponentGroupMember>();
			for(String GLUserID : GLUserIDs) {
				TCComponentGroupMember member = TCExtension.GetGroupMemberByUserID(GLUserID.toString(), session);
				if(member != null) {
					userList.add(member);
				}
			}
			ul = userList.toArray(new TCComponentGroupMember[0]);
		}
		return ul;
	}

	private HashMap<String, String[]> extractSignOffs(String[] prefGL) {
		HashMap<String, String[]> GLList =  new HashMap<String, String[]>();
		for(String GLValue : prefGL) {
			String[] splitvalue = GLValue.split("=");
			if(splitvalue.length == 2) {
				if(splitvalue[1].contains(";")){
					String[] users = splitvalue[1].split(";");
					GLList.put(splitvalue[0], users);
				}else {
					GLList.put(splitvalue[0], new String[] {splitvalue[1]});
				}
			}
		}

		return GLList;
	}

}