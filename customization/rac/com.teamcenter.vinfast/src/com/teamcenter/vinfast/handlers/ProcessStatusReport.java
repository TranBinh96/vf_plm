package com.teamcenter.vinfast.handlers;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.sql.Array;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.jobs.Job;

import com.teamcenter.rac.aif.AbstractAIFOperation;
//import com.teamcenter.rac.aif.kernel.AIFComponentContext;
//import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
//import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
//import com.teamcenter.rac.kernel.TCComponentGroup;
//import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentProcess;
//import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
//import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
//import com.teamcenter.services.internal.rac.core._2007_01.DataManagement.GetAttributeValuesInputData;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.dialog.ProcessStatusReportFrame;
import com.vf.utils.Query;
import com.vf4.services.rac.custom.ReportDataSourceService;
import com.vf4.services.rac.custom._2020_12.ReportDataSource;
import com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportInput;
import com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportResponse;

import com.vf.utils.TCExtension;
//import org.eclipse.ui.plugin.AbstractUIPlugin;
//import org.osgi.framework.BundleContext;

public class ProcessStatusReport extends AbstractHandler {
	private static XSSFWorkbook wb;
	private static ProcessStatusReportFrame frame = null;
	private static TCSession session = null;
//	private static TCComponentGroup group = null;
//	private static TCComponentRole role = null;
//	private static TCComponentUser user = null;
	private static String selectedProcess = null;
	private static String creationDateBefore = null;
	private static String creationDateAfter = null;
	private static String modifyDateBefore = null;
	private static String modifyDateAfter = null;
	private static long startTime = 0;
	private final String DEFAULT_DATASET_TEMPLATE_NAME = "VF_Process_Status_Report_Template";
	private final String SCOOTER_ECN_PROCESS_STATUS_REPORT = "Scooter_ECN_Process_Status_Report_Template";
	private static String REPORT_PREFIX = "VF_Process_Status_Report_";
	private static String TEMP_DIR;
	private final String TOKEN_1 = " - ";
	private final String TOKEN_2 = ",";
	private final String TOKEN_3 = "\n";
	private final String PENDING_STATUS = "PENDING";
	private final String APPROVED_STATUS = "APPROVED";
	private final String STUCK_STATUS = "STUCK";
	private final String REJECTED_STATUS = "REJECTED";
	private final String ABORTED_STATUS = "ABORTED";
	private LinkedHashMap<String, ProcessReportObject> dataToExcel;
	private static String VINFAST_PROCESS_STATUS_REPORT_PREFERENCE = "VINFAST_PROCESS_STATUS_REPORT_2";
	private LinkedHashMap<String, String> mapProcess2RootTargetType;
	private static String rootTargetObjectType = null;
	private final String[] VINFAST_PROCESS_STATUS_REPORT_VALUE;
	private String[] valueLOV_module_group_ecr = null;
	private static String selectedModuleGroupECR = null;
	
	public ProcessStatusReport() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		TCPreferenceService preferenceService = session.getPreferenceService();
		VINFAST_PROCESS_STATUS_REPORT_VALUE = preferenceService
				.getStringValues(VINFAST_PROCESS_STATUS_REPORT_PREFERENCE);
		mapProcess2RootTargetType = new LinkedHashMap<>();
		valueLOV_module_group_ecr = TCExtension.GetLovValues("vf6_module_group", "Vf6_ECR", session);
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dataToExcel = new LinkedHashMap<String, ProcessReportObject>();
				wb = new XSSFWorkbook();
//				group = session.getCurrentGroup();
//				role = session.getCurrentRole();
//				user = session.getUser();
				ArrayList<String> process2RootTarget = new ArrayList<String>(
						Arrays.asList(VINFAST_PROCESS_STATUS_REPORT_VALUE));
				for (String aLinePre : process2RootTarget) {
					String[] arrTmp = aLinePre.split(",");
					mapProcess2RootTargetType.put(arrTmp[0], arrTmp[1]);
				}
				createDialog();

			}
		});
		return null;
	}

	@SuppressWarnings("unchecked")
	public void createDialog() {

		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/KIT.png"));
		Icon ok_Icon = new ImageIcon(getClass().getResource("/icons/ok.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));

		frame = new ProcessStatusReportFrame();
		frame.setTitle("Process Status Report");
		frame.setIconImage(frame_Icon.getImage());
//		frame.setMinimumSize(new Dimension(700, 300));

        frame.setSize(new Dimension(700, 300));
		frame.btnLeft.setIcon(ok_Icon);
		frame.btnRight.setIcon(cancel_Icon);

		for (String key : mapProcess2RootTargetType.keySet()) {
			frame.cbProcess.addItem(key);
		}
	

		if (!mapProcess2RootTargetType.entrySet().toArray()[0].toString().equalsIgnoreCase("VinFast ECR")) {
			frame.panel_2.remove(frame.lblModuleGroupECR);
        	frame.panel_2.remove(frame.cbModuleGroupECR);
		}
		
		
		for (String valueLOV: valueLOV_module_group_ecr) {
			frame.cbModuleGroupECR.addItem(valueLOV);
		}
		
		frame.cbProcess.addActionListener(new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent e) {
		        selectedProcess = frame.cbProcess.getSelectedItem().toString();
		        if (selectedProcess.equalsIgnoreCase("VinFast ECR") ) {
		        	frame.panel_2.add(frame.lblModuleGroupECR,2);
		        	frame.panel_2.add(frame.cbModuleGroupECR,3);
			        frame.setSize(new Dimension(700, 301));
		        } else {
		        	frame.panel_2.remove(frame.lblModuleGroupECR);
		        	frame.panel_2.remove(frame.cbModuleGroupECR);
			        frame.setSize(new Dimension(700, 300));
		        }
		    }
			
		});

		frame.btnLeft.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectedProcess = frame.cbProcess.getSelectedItem().toString();
				selectedModuleGroupECR = frame.cbModuleGroupECR.getSelectedItem().toString();
				rootTargetObjectType = mapProcess2RootTargetType.get(selectedProcess);
				if (selectedProcess == null) {
					MessageBox.post("Please select a process.", "Error", MessageBox.ERROR);
					return;
				}
//				// TODO call function query data
				AbstractAIFOperation op = new AbstractAIFOperation() {

					@Override
					public void executeOperation() throws Exception {
						// TODO Auto-generated method stub
						frame.btnLeft.setEnabled(false);
						startTime = System.currentTimeMillis();
						LinkedHashMap<String, String> queryCriteria = new LinkedHashMap<String, String>();
						queryCriteria.put("Workflow Template", selectedProcess);

						ProcessStatusReportInput processInput = new ProcessStatusReportInput();
						processInput.workflowTemplate = selectedProcess;
						if (frame.creationBefore.getDate() != null) {
							creationDateBefore = new SimpleDateFormat("dd-MMM-yyyy HH:mm")
									.format(frame.creationBefore.getDate());
							queryCriteria.put("Created Before", creationDateBefore);
							processInput.creationDateBefore = creationDateBefore;
						}
						if (frame.creationAfter.getDate() != null) {
							creationDateAfter = new SimpleDateFormat("dd-MMM-yyyy HH:mm")
									.format(frame.creationAfter.getDate());
							queryCriteria.put("Created After", creationDateAfter);
							processInput.creationDateAfter = creationDateAfter;
						}
						if (frame.modifyBefore.getDate() != null) {
							modifyDateBefore = new SimpleDateFormat("dd-MMM-yyyy HH:mm")
									.format(frame.modifyBefore.getDate());
							queryCriteria.put("Modified Before", modifyDateBefore);
							processInput.modifyDateBefore = modifyDateBefore;
						}
						if (frame.modifyAfter.getDate() != null) {
							modifyDateAfter = new SimpleDateFormat("dd-MMM-yyyy HH:mm")
									.format(frame.modifyAfter.getDate());
							queryCriteria.put("Modified After", modifyDateAfter);
							processInput.modifyDateAfter = modifyDateAfter;
						}
						if (frame.txtprocessName.getText() != null && !frame.txtprocessName.getText().isEmpty()) {
							if (!frame.txtprocessName.getText().contains(";")) {
								queryCriteria.put("Job Name", "*" + frame.txtprocessName.getText() + "*");
								processInput.processName = "*" + frame.txtprocessName.getText() + "*";
							} else {
								String[] split = frame.txtprocessName.getText().split(";");
								for (int i = 0; i < split.length; i++) {
									split[i] = "*" + split[i] + "*";
								}
								String.join(";", split);
								queryCriteria.put("Job Name", String.join(";", split));
								processInput.processName = String.join(";", split);
							}
						}

						if (frame.chckbxNewCheckBox.isSelected()) {
							queryCriteria.put("State", "4");
							processInput.runningProccess = true;
						}

						ReportDataSource reportSource = ReportDataSourceService.getService(session);
						ProcessStatusReportResponse response = reportSource.getProcessStatusReport(processInput);
						if (response.outputs == null || response.outputs.length == 0) {
							MessageBox.post("There are not object has been found", "INFO", MessageBox.INFORMATION);
						} else {
							extractData2(response);
							publishReport();
						}

//						TCComponent[] queryResult = Query.queryItem(session, queryCriteria, "__TNH_JobNameQuery");
//						if (queryResult == null  || queryResult.length == 0) {
//							MessageBox.post("There are not object has been found", "INFO", MessageBox.INFORMATION);
//						}else {
//							extractData(queryResult);
//							publishReport();
//						}
						frame.dispose();
						return;
					}

				};
				session.queueOperation(op);
			}
		});

		frame.btnRight.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}

		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void extractData(TCComponent[] comps) throws TCException {
		LinkedHashMap<String, String> lastestProcess = new LinkedHashMap<String, String>();
		DataManagementService.getService(session).refreshObjects(comps);
		for (int i = 0; i < comps.length; ++i) {
			TCComponentTask aTask = ((TCComponentProcess) comps[i]).getRootTask();
			ProcessReportObject aData = new ProcessReportObject();
			String processName = "";
			String processStatus = "";
			String startDate = "";
//			String dueDate = "";
			String completedDate = "";
			String itemID = "";
			String itemRevision = "";
			String itemName = "";
			String description = "";
			String owningUser = "";
			String owningGroup = "";
			String moduleGroup = "";
			String creationDate = "";
			String pendingTask = "";
//			String responsibleParty = "";
			String wfTemplate = "";
			String pendingUser = "";
			String targets = "";
			String classification = "";
			String priority = "";
			String exchangeNewPart = "";
			String exchangeOldPart = "";
			try {
				processName = aTask.getProperty("job_name");
//				TCComponent[] rootTarget = aTask.getRelatedComponents("target_attachments");
				ArrayList<TCComponent> rootTarget = Utils.GetSecondaryObjectByRelationName(session, aTask,
						new String[] { rootTargetObjectType }, "Fnd0EPMTarget");
				if (rootTarget == null || rootTarget.size() == 0) {
					continue;
				}
				String[] className = rootTarget.get(0).getClassNameHierarchy();
				if (Arrays.asList(className).contains("VF4_line_itemRevision")) {
					itemID = rootTarget.get(0).getPropertyDisplayableValue("vf4_bom_vfPartNumber");
				} else {
					itemID = rootTarget.get(0).getPropertyDisplayableValue("item_id");
				}
				itemRevision = rootTarget.get(0).getPropertyDisplayableValue("item_revision_id");
				itemName = rootTarget.get(0).getPropertyDisplayableValue("object_name");
				owningUser = aTask.getProperty("owning_user");
				owningGroup = aTask.getProperty("owning_group");
				creationDate = aTask.getProperty("creation_date");

				/* storage latest process */
				if (lastestProcess.containsKey(itemID)
						&& Utils.compareDate(lastestProcess.get(itemID), creationDate) < 0) {
					continue;
				} else {
					lastestProcess.put(itemID, creationDate);
				}

				startDate = aTask.getProperty("fnd0StartDate");
				wfTemplate = aTask.getProperty("root_task");
				String currentStatus = aTask.getProperty("real_state");
				String releaseStatus = aTask.getProperty("release_status_attachments");
				StringBuilder strBuilderTargets = new StringBuilder();
				for (int x = 0; x < aTask.getRelatedComponents("root_target_attachments").length; x++) {
					TCComponent firstTarget = aTask.getRelatedComponents("root_target_attachments")[x];
					if (firstTarget.getProperty("object_type")
							.compareToIgnoreCase("Engineering Change Notice Revision") == 0
							|| firstTarget.getProperty("object_type")
									.compareToIgnoreCase("Engineering Change Request Revision") == 0
							|| firstTarget.getProperty("object_type").compareToIgnoreCase("ERN Revision") == 0) {
						description = firstTarget.getProperty("object_desc");
						TCComponent item = firstTarget.getRelatedComponent("items_tag");
						if (item.getProperty("vf6_module_group") != null) {
							moduleGroup = item.getProperty("vf6_module_group");
						}
						if (firstTarget.getProperty("object_type").compareToIgnoreCase("ERN Revision") == 0) {
							moduleGroup = firstTarget.getProperty("vf4_main_module");
						}
						classification = firstTarget.getProperty("vf6_es_classification");
						priority = firstTarget.getProperty("vf6_es_priority");
						exchangeNewPart = firstTarget.getProperty("vf6_es_exchange_newpart");
						exchangeOldPart = firstTarget.getProperty("vf6_es_exchange_oldpart");
					}
					if (firstTarget.getProperty("object_type").compareToIgnoreCase("VF Design Revision") == 0
							|| firstTarget.getProperty("object_type").compareToIgnoreCase("EBUS Design Revision") == 0
							|| firstTarget.getProperty("object_type").compareToIgnoreCase("Car Line Itm Revision") == 0
							|| firstTarget.getProperty("object_type")
									.compareToIgnoreCase("Engineering Change Notice Revision") == 0
							|| firstTarget.getProperty("object_type")
									.compareToIgnoreCase("Engineering Change Request Revision") == 0
							|| firstTarget.getProperty("object_type").compareToIgnoreCase("ERN Revision") == 0
							|| firstTarget.getProperty("object_type").compareToIgnoreCase("VF BP Design Revision") == 0
							|| firstTarget.getProperty("object_type")
									.compareToIgnoreCase("Battery Line Revision") == 0) {
						if (strBuilderTargets.length() > 0) {
							strBuilderTargets.append(TOKEN_3);
						}
						strBuilderTargets.append(firstTarget.getProperty("object_string"));
					}
				}
				for (int x = 0; x < aTask.getRelatedComponents("root_reference_attachments").length; x++) {
					TCComponent firstRefer = aTask.getRelatedComponents("root_reference_attachments")[x];
					if (firstRefer.getProperty("object_type")
							.compareToIgnoreCase("Engineering Change Notice Revision") == 0
							|| firstRefer.getProperty("object_type")
									.compareToIgnoreCase("Engineering Change Request Revision") == 0) {
						description = firstRefer.getProperty("object_desc");
						TCComponent item = firstRefer.getRelatedComponent("items_tag");
						if (item.getProperty("vf6_module_group") != null) {
							moduleGroup = item.getProperty("vf6_module_group");
						}
						classification = firstRefer.getProperty("vf6_es_classification");
						priority = firstRefer.getProperty("vf6_es_priority");
						exchangeNewPart = firstRefer.getProperty("vf6_es_exchange_newpart");
						exchangeOldPart = firstRefer.getProperty("vf6_es_exchange_oldpart");
					}
					if (firstRefer.getProperty("object_type").compareToIgnoreCase("VF Design Revision") == 0
							|| firstRefer.getProperty("object_type").compareToIgnoreCase("EBUS Design Revision") == 0
							|| firstRefer.getProperty("object_type").compareToIgnoreCase("Car Line Itm Revision") == 0
							|| firstRefer.getProperty("object_type")
									.compareToIgnoreCase("Engineering Change Notice Revision") == 0
							|| firstRefer.getProperty("object_type")
									.compareToIgnoreCase("Engineering Change Request Revision") == 0) {
						if (strBuilderTargets.length() > 0) {
							strBuilderTargets.append(TOKEN_3);
						}
						strBuilderTargets.append(firstRefer.getProperty("object_string"));
					}
				}
				/* set job name to target if target blank */
				if (strBuilderTargets.length() <= 0) {
					strBuilderTargets.append(processName);
				}
				targets = strBuilderTargets.toString();
				if (currentStatus.compareToIgnoreCase("Completed") == 0) {
					if (releaseStatus.contains("Rejected")) {
						processStatus = REJECTED_STATUS;
					} else {
						processStatus = APPROVED_STATUS;
					}
					completedDate = aTask.getProperty("fnd0EndDate");
				} else if (currentStatus.compareToIgnoreCase("Aborted") == 0) {
					processStatus = ABORTED_STATUS;
					completedDate = aTask.getProperty("fnd0EndDate");
				} else {
					processStatus = PENDING_STATUS;
					pendingTask = removeDupicateWord(aTask.getProperty("fnd0StartedTasks"), TOKEN_2);
					List<String> splitPendingTask = Arrays.asList(pendingTask.split(TOKEN_2));
					StringBuilder strBuilder = new StringBuilder();
					List<TCComponentSignoff> requiredSignOff = new LinkedList<TCComponentSignoff>();
					boolean isAllRequiredDecised = true;
					for (int j = 0; j < splitPendingTask.size(); j++) {
						TCComponentTask subTask = aTask.getSubtask(splitPendingTask.get(j));
						if (subTask != null) {
							if (subTask.getTaskType().compareToIgnoreCase("EPMReviewTask") == 0
									|| subTask.getTaskType().compareToIgnoreCase("EPMAcknowledgeTask") == 0) {
								TCComponentTask[] childTasks = subTask.getSubtasks();
								for (int k = 0; k < childTasks.length; k++) {
									if (childTasks[k].getTaskType().compareToIgnoreCase("EPMPerformSignoffTask") == 0) {
										for (int h = 0; h < childTasks[k]
												.getRelated("signoff_attachments").length; h++) {
											TCComponentSignoff signoff = (TCComponentSignoff) childTasks[k]
													.getRelatedComponents("signoff_attachments")[h];
											if (signoff.isDecisionRequired()) {
												requiredSignOff.add(signoff);
											}
											if (signoff.getDecisionDate() == null) {
												if (strBuilder.length() > 0) {
													strBuilder.append(", ");
												}
												String[] temp = signoff.toString().split(TOKEN_1);
												strBuilder.append(temp[0]);
											}
										}
									}
								}
							} else {
								if (subTask.getTaskType().compareToIgnoreCase("EPMTask") == 0) {
									processStatus = STUCK_STATUS;
								} else {
									if (j > 0) {
										strBuilder.append(TOKEN_2);
									}
									if (subTask.getProperty("fnd0Performer") != null) {
										strBuilder.append(subTask.getProperty("fnd0Performer"));
									}
								}
							}
						} else {
							processStatus = STUCK_STATUS;
						}
					}
					if (requiredSignOff.size() > 0) {
						for (TCComponentSignoff signOff : requiredSignOff) {
							if (signOff.getDecisionDate() == null) {
								isAllRequiredDecised = false;
							}
						}
						if (isAllRequiredDecised == true) {
							processStatus = STUCK_STATUS;
						}
					}
					pendingUser = strBuilder.toString();
				}

				aData.setItemID(itemID);
				aData.setItemRevision(itemRevision);
				aData.setItemName(itemName);
				aData.setOwningUser(owningUser);
				aData.setOwningGroup(owningGroup);
				aData.setDescription(description);
				aData.setCreationDate(creationDate);
				aData.setProcessStatus(processStatus);
				aData.setPendingTask(pendingTask);
				aData.setCompletedDate(completedDate);
				aData.setStartDate(startDate);
				aData.setPendingUser(pendingUser);
				aData.setWfTemplate(wfTemplate);
				aData.setDescription(description);
				aData.setModuleGroup(moduleGroup);
				aData.setTargets(targets);
				aData.setClassification(classification);
				aData.setPriority(priority);
				aData.setExchangeNewPart(exchangeNewPart);
				aData.setExchangeOldPart(exchangeOldPart);
				dataToExcel.put(itemID, aData);
			} catch (TCException | NotLoadedException e) {
				e.printStackTrace();
			}
		}
	}

	public void extractData2(ProcessStatusReportResponse response) throws TCException {
		SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		for (int i = 0; i < response.outputs.length; i++) {
			System.out.println("Item ID: " + response.outputs[i].itemID + " ItemName: " + response.outputs[i].itemName
					+ "ItemRevisionID" + response.outputs[i].itemRevision + "OWNER"
					+ response.outputs[i].owningUser.toString() + "TARGETS" + response.outputs[i].targets.toString()
					+ "startDate" + response.outputs[i].startDate);
			ProcessReportObject aData = null;
			String itemId = response.outputs[i].itemID;
			if (!dataToExcel.containsKey(itemId)) {
				if ((!selectedModuleGroupECR.isEmpty() && response.outputs[i].moduleGroup.compareTo(selectedModuleGroupECR) == 0) || selectedModuleGroupECR.isEmpty()) {
				aData = new ProcessReportObject();
				aData.setItemID(response.outputs[i].itemID);
				aData.setItemRevision(response.outputs[i].itemRevision);
				aData.setItemName(response.outputs[i].itemName);
				aData.setOwningUser(response.outputs[i].owningUser.toString());
				aData.setOwningGroup(response.outputs[i].owningGroup);
				aData.setWfTemplate(response.outputs[i].wfTemplate);
				if (response.outputs[i].processStartDate != null) {
					aData.setCreationDate(format1.format(response.outputs[i].processStartDate.getTime()));
				}
				aData.setDescription(response.outputs[i].description);
				aData.setModuleGroup(response.outputs[i].moduleGroup);
				aData.setTargets(response.outputs[i].targets);

				// TODO update custom soa to return processStatus
				aData.setProcessStatus(response.outputs[i].processStatus);
				// TODO update custom soa to return completedDate
				if (response.outputs[i].processEndDate != null) {
					aData.setCompletedDate(format1.format(response.outputs[i].processEndDate.getTime()));
				}
				// TODO update custom soa to return startDate
				if (response.outputs[i].processStartDate != null) {
					aData.setStartDate(format1.format(response.outputs[i].processStartDate.getTime()));
				}

				if (response.outputs[i].taskStatus.compareToIgnoreCase("Started") == 0
						|| response.outputs[i].taskStatus.compareToIgnoreCase("Late") == 0) {
					aData.setPendingTask(response.outputs[i].subTaskName);
					if (response.outputs[i].pendingUser.isEmpty()) {
						aData.setPendingUser(response.outputs[i].resParty);
					} else {
						aData.setPendingUser(response.outputs[i].pendingUser);
					}
				}
				dataToExcel.put(itemId, aData);
				}
			} else {
				aData = dataToExcel.get(itemId);
				if (response.outputs[i].taskStatus.compareToIgnoreCase("Started") == 0
						|| response.outputs[i].taskStatus.compareToIgnoreCase("Late") == 0) {
					if (aData.getPendingTask().isEmpty()) {
						aData.setPendingTask(response.outputs[i].subTaskName);
					} else {
						aData.setPendingTask(aData.getPendingTask() + TOKEN_2 + response.outputs[i].subTaskName);
					}

					if (aData.getPendingUser().isEmpty()) {
						if (response.outputs[i].pendingUser.isEmpty()) {
							aData.setPendingUser(response.outputs[i].resParty);
						} else {
							aData.setPendingUser(response.outputs[i].pendingUser);
						}
					} else {
						if (response.outputs[i].pendingUser.isEmpty()) {
							aData.setPendingUser(aData.getPendingUser() + TOKEN_2 + response.outputs[i].resParty);
						} else {
							aData.setPendingUser(aData.getPendingUser() + TOKEN_2 + response.outputs[i].pendingUser);
						}
					}
				}
			}
		}
	}

	public String removeDupicateWord(String input, String token) {
		final String[] strWords = input.split(token);
		final Set<String> setOfWords = new LinkedHashSet<String>(Arrays.asList(strWords));
		final StringBuilder builder = new StringBuilder();
		int index = 0;

		for (String s : setOfWords) {

			if (index > 0)
				builder.append(token);

			builder.append(s);
			index++;
		}
		String output = builder.toString();
		return output;
	}

	@SuppressWarnings("rawtypes")
	public void writeARowReportDefault() {

		// TODO loop propmap and write file
		XSSFSheet spreadsheet = wb.getSheet("Report");
		int rownum = 1;
		XSSFWorkbook wb = spreadsheet.getWorkbook();

		CellStyle headerCellStyle = wb.createCellStyle();
		headerCellStyle.setAlignment(HorizontalAlignment.LEFT);
		headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerCellStyle.setBorderTop(BorderStyle.THIN);
		headerCellStyle.setBorderBottom(BorderStyle.THIN);
		headerCellStyle.setBorderLeft(BorderStyle.THIN);
		headerCellStyle.setBorderRight(BorderStyle.THIN);

		for (Map.Entry entry : dataToExcel.entrySet()) {
			ProcessReportObject obj = (ProcessReportObject) entry.getValue();
			XSSFRow row = spreadsheet.createRow(rownum);

			XSSFCell numCell = row.createCell(0);
			numCell.setCellValue(rownum);
			numCell.setCellStyle(headerCellStyle);

			XSSFCell idCell = row.createCell(1);
			idCell.setCellValue(obj.getItemID());
			idCell.setCellStyle(headerCellStyle);

			XSSFCell revisionCell = row.createCell(2);
			revisionCell.setCellValue(obj.getItemRevision());
			revisionCell.setCellStyle(headerCellStyle);

			XSSFCell nameCell = row.createCell(3);
			nameCell.setCellValue(obj.getItemName());
			nameCell.setCellStyle(headerCellStyle);

			XSSFCell descripCell = row.createCell(4);
			descripCell.setCellValue(obj.getDescription());
			descripCell.setCellStyle(headerCellStyle);

			XSSFCell ownerCell = row.createCell(5);
			ownerCell.setCellValue(obj.getOwningUser());
			ownerCell.setCellStyle(headerCellStyle);

			XSSFCell owningGroupCell = row.createCell(6);
			owningGroupCell.setCellValue(obj.getOwningGroup());
			owningGroupCell.setCellStyle(headerCellStyle);

			XSSFCell moduleGroupCell = row.createCell(7);
			moduleGroupCell.setCellValue(obj.getModuleGroup());
			moduleGroupCell.setCellStyle(headerCellStyle);

			XSSFCell creationDateCell = row.createCell(8);
			creationDateCell.setCellValue(obj.getCreationDate());
			creationDateCell.setCellStyle(headerCellStyle);

			XSSFCell wfTempCell = row.createCell(9);
			wfTempCell.setCellValue(obj.getWfTemplate());
			wfTempCell.setCellStyle(headerCellStyle);

			XSSFCell pendingTaskCell = row.createCell(10);
			pendingTaskCell.setCellValue(obj.getPendingTask());
			pendingTaskCell.setCellStyle(headerCellStyle);

			XSSFCell pendingUserCell = row.createCell(11);
			pendingUserCell.setCellValue(obj.getPendingUser());
			pendingUserCell.setCellStyle(headerCellStyle);

			XSSFCell targetsCell = row.createCell(12);
			if (obj.getTargets().length() > 32726) {
				targetsCell.setCellValue("OVER LIMITATION CHARACTER");
			} else {
				targetsCell.setCellValue(obj.getTargets());
			}
			targetsCell.setCellStyle(headerCellStyle);

			XSSFCell statusCell = row.createCell(13);
			statusCell.setCellValue(obj.getProcessStatus());
			statusCell.setCellStyle(headerCellStyle);

			XSSFCell starDateCell = row.createCell(14);
			starDateCell.setCellValue(obj.getStartDate());
			starDateCell.setCellStyle(headerCellStyle);

			XSSFCell completeDateCell = row.createCell(15);
			completeDateCell.setCellValue(obj.getCompletedDate());
			completeDateCell.setCellStyle(headerCellStyle);

			rownum++;
		}

		for (int kz = 0; kz < 14; kz++) {
			spreadsheet.autoSizeColumn(kz);
		}
	}

	private void publishReport() {
		try {
			String template = "";
			REPORT_PREFIX = ProcessStatusReport.selectedProcess;
			if (ProcessStatusReport.selectedProcess.contains("EScooter")
					|| ProcessStatusReport.selectedProcess.contains("Escooter")) {
				template = SCOOTER_ECN_PROCESS_STATUS_REPORT;
			} else {
				template = DEFAULT_DATASET_TEMPLATE_NAME;
			}
			File report = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET,
					Query.QUERY_JES_ENTRY_DATASET_NAME, template, TEMP_DIR, REPORT_PREFIX, startTime);

			InputStream fileIn = new FileInputStream(report);
			ProcessStatusReport.wb = new XSSFWorkbook(fileIn);

			if (ProcessStatusReport.selectedProcess.contains("EScooter")
					|| ProcessStatusReport.selectedProcess.contains("Escooter")) {
				writeARowReportECNScooter();
			} else {
				writeARowReportDefault();
			}
			fileIn.close();
			FileOutputStream fos = new FileOutputStream(report);
			ProcessStatusReport.wb.write(fos);
			fos.close();

			report.setWritable(false);
			Desktop.getDesktop().open(report);

		} catch (IOException | TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	private ArrayList<String> splitObjectNameString(String input) {
//		ArrayList<String> output = new ArrayList<String>();
//		String itemID = "";
//		String itemRev = "";
//		String itemName = "";
//		String sub1 = "";
//		String sub2 = "";
//		int idxOfNextWord = 0;
//		for (int i = 0; i < input.length(); i++) {
//			if (input.charAt(i) == ';') {
//				sub1 = input.substring(idxOfNextWord, i);
//				idxOfNextWord = i + 1;
//				sub2 = input.substring(idxOfNextWord);
//				break;
//			}
//		}
//		idxOfNextWord = 0;
//		for (int j = 0; j < sub1.length(); j++) {
//			if (sub1.charAt(j) == '/') {
//				itemID = sub1.substring(idxOfNextWord, j);
//				idxOfNextWord = j + 1;
//				itemRev = sub1.substring(idxOfNextWord);
//				break;
//			}
//		}
//		idxOfNextWord = 0;
//		for (int k = 0; k < sub2.length(); k++) {
//			if (sub2.charAt(k) == '-') {
//				idxOfNextWord = k + 1;
//				itemName = sub2.substring(idxOfNextWord);
//				break;
//			}
//		}
//		/* for case process name is ERN000005400-SWITCHES */
//		if (itemID.compareTo("") == 0) {
//			idxOfNextWord = 0;
//			for (int i = 0; i < input.length(); i++) {
//				if (input.charAt(i) == '-') {
//					itemID = input.substring(idxOfNextWord, i);
//					idxOfNextWord = i + 1;
//					itemName = input.substring(idxOfNextWord);
//					break;
//				}
//			}
//			if (itemName.compareTo("") == 0) {
//				itemName = input;
//			}
//		}
//		/* for case CUV - ERN000001428 */
//		if (itemID.contains("-")) {
//			String[] idWithoutDash = itemID.split(" - ");
//			itemID = idWithoutDash[1];
//		}
//		output.add(itemID);
//		output.add(itemRev);
//		output.add(itemName);
//		return output;
//	}

	@SuppressWarnings("rawtypes")
	public void writeARowReportECNScooter() {

//		 TODO loop propmap and write file
		XSSFSheet spreadsheet = wb.getSheet("Report");
		int rownum = 1;
		XSSFWorkbook wb = spreadsheet.getWorkbook();

		CellStyle headerCellStyle = wb.createCellStyle();
		headerCellStyle.setAlignment(HorizontalAlignment.LEFT);
		headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerCellStyle.setBorderTop(BorderStyle.THIN);
		headerCellStyle.setBorderBottom(BorderStyle.THIN);
		headerCellStyle.setBorderLeft(BorderStyle.THIN);
		headerCellStyle.setBorderRight(BorderStyle.THIN);

		for (Map.Entry entry : dataToExcel.entrySet()) {
			ProcessReportObject obj = (ProcessReportObject) entry.getValue();
			XSSFRow row = spreadsheet.createRow(rownum);

			XSSFCell numCell = row.createCell(0);
			numCell.setCellValue(rownum);
			numCell.setCellStyle(headerCellStyle);

			XSSFCell idCell = row.createCell(1);
			idCell.setCellValue(obj.getItemID());
			idCell.setCellStyle(headerCellStyle);

			XSSFCell revisionCell = row.createCell(2);
			revisionCell.setCellValue(obj.getItemRevision());
			revisionCell.setCellStyle(headerCellStyle);

			XSSFCell nameCell = row.createCell(3);
			nameCell.setCellValue(obj.getItemName());
			nameCell.setCellStyle(headerCellStyle);

			XSSFCell descripCell = row.createCell(4);
			descripCell.setCellValue(obj.getDescription());
			descripCell.setCellStyle(headerCellStyle);

			XSSFCell ownerCell = row.createCell(5);
			ownerCell.setCellValue(obj.getOwningUser());
			ownerCell.setCellStyle(headerCellStyle);

			XSSFCell owningGroupCell = row.createCell(6);
			owningGroupCell.setCellValue(obj.getOwningGroup());
			owningGroupCell.setCellStyle(headerCellStyle);

			XSSFCell moduleGroupCell = row.createCell(7);
			moduleGroupCell.setCellValue(obj.getModuleGroup());
			moduleGroupCell.setCellStyle(headerCellStyle);

			XSSFCell creationDateCell = row.createCell(8);
			creationDateCell.setCellValue(obj.getCreationDate());
			creationDateCell.setCellStyle(headerCellStyle);

			XSSFCell wfTempCell = row.createCell(9);
			wfTempCell.setCellValue(obj.getWfTemplate());
			wfTempCell.setCellStyle(headerCellStyle);

			XSSFCell pendingTaskCell = row.createCell(10);
			pendingTaskCell.setCellValue(obj.getPendingTask());
			pendingTaskCell.setCellStyle(headerCellStyle);

			XSSFCell classCell = row.createCell(11);
			classCell.setCellValue(obj.getClassification());
			classCell.setCellStyle(headerCellStyle);

			XSSFCell priorityCell = row.createCell(12);
			priorityCell.setCellValue(obj.getPriority());
			priorityCell.setCellStyle(headerCellStyle);

			XSSFCell exNewPartCell = row.createCell(13);
			exNewPartCell.setCellValue(obj.getExchangeNewPart());
			exNewPartCell.setCellStyle(headerCellStyle);

			XSSFCell exNewOldCell = row.createCell(14);
			exNewOldCell.setCellValue(obj.getExchangeOldPart());
			exNewOldCell.setCellStyle(headerCellStyle);

			XSSFCell pendingUserCell = row.createCell(15);
			pendingUserCell.setCellValue(obj.getPendingUser());
			pendingUserCell.setCellStyle(headerCellStyle);

			XSSFCell targetsCell = row.createCell(16);
			targetsCell.setCellValue(obj.getTargets());
			targetsCell.setCellStyle(headerCellStyle);

			XSSFCell statusCell = row.createCell(17);
			statusCell.setCellValue(obj.getProcessStatus());
			statusCell.setCellStyle(headerCellStyle);

			XSSFCell starDateCell = row.createCell(18);
			starDateCell.setCellValue(obj.getStartDate());
			starDateCell.setCellStyle(headerCellStyle);

			XSSFCell completeDateCell = row.createCell(19);
			completeDateCell.setCellValue(obj.getCompletedDate());
			completeDateCell.setCellStyle(headerCellStyle);

			rownum++;
		}

		for (int kz = 0; kz < 18; kz++) {
			spreadsheet.autoSizeColumn(kz);
		}
	}

}
