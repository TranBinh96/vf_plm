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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
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

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2007_06.DataManagement.RelationAndTypesFilter;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsData2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsOutput2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsPref2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsResponse2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationship;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.dialog.ProcessStatusReportFrame;
import com.vf.utils.Query;
import com.vf4.services.rac.custom.ReportDataSourceService;
import com.vf4.services.rac.custom._2020_12.ReportDataSource;
import com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportInput;
import com.vf4.services.rac.custom._2020_12.ReportDataSource.ProcessStatusReportResponse;

public class ProcessStatusReportDetail extends AbstractHandler {
	private static XSSFWorkbook wb;
	private static ProcessStatusReportFrame frame = null;
	private static TCSession session = null;
	private static TCComponentGroup group = null;
	private static TCComponentRole role = null;
	private static TCComponentUser user = null;
	private static String selectedProcess = null;
	private static String creationDateBefore = null;
	private static String creationDateAfter = null;
	private static String modifyDateBefore = null;
	private static String modifyDateAfter = null;
	private static String processName = null;
	private static long startTime = 0;
	private final String DEFAULT_DATASET_TEMPLATE_NAME = "VF_Process_Status_Detail_Template";
	private final String DEFAULT_DATASET_TEMPLATE_NAME_DEV = "VF_Process_Status_Detail_Template_DEV";
	private final String SCOOTER_ECN_PROCESS_STATUS_REPORT = "Scooter_ECN_Process_Status_Report_Template";
	private static String REPORT_PREFIX = "VF_Process_Status_Report_";
	private static String TEMP_DIR;
	private final String TOKEN_1 = " - ";
	private final String TOKEN_2 = ", ";
	private final String TOKEN_3 = "\n";
	private final String PENDING_STATUS = "PENDING";
	private final String APPROVED_STATUS = "APPROVED";
	private final String STUCK_STATUS = "STUCK";
	private final String REJECTED_STATUS = "REJECTED";
	private static String rootTargetObjectType = null;
	private static Logger LOGGER;
	private LinkedHashMap<String, ProcessReportObject> dataToExcel;
	private LinkedHashMap<String, String> mapProcess2RootTargetType;
	private static String VINFAST_PROCESS_STATUS_REPORT_PREFERENCE = "VINFAST_PROCESS_STATUS_REPORT_2";
	private final String[] VINFAST_PROCESS_STATUS_REPORT_VALUE;

	public ProcessStatusReportDetail() {
		LOGGER = Logger.getLogger(this.getClass());
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		TCPreferenceService preferenceService = session.getPreferenceService();
		VINFAST_PROCESS_STATUS_REPORT_VALUE = preferenceService.getStringValues(VINFAST_PROCESS_STATUS_REPORT_PREFERENCE);
		mapProcess2RootTargetType = new LinkedHashMap<>();
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dataToExcel = new LinkedHashMap<String, ProcessReportObject>();
				wb = new XSSFWorkbook();
				group = session.getCurrentGroup();
				role = session.getCurrentRole();
				user = session.getUser();
				ArrayList<String> process2RootTarget = new ArrayList<String>(Arrays.asList(VINFAST_PROCESS_STATUS_REPORT_VALUE));
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
		frame.setTitle("Process Status Report Detail");
		frame.setIconImage(frame_Icon.getImage());
		frame.setMinimumSize(new Dimension(400, 300));

		frame.btnLeft.setIcon(ok_Icon);
		frame.btnRight.setIcon(cancel_Icon);

		for (String key : mapProcess2RootTargetType.keySet()) {
			frame.cbProcess.addItem(key);
		}

		frame.btnLeft.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectedProcess = frame.cbProcess.getSelectedItem().toString();
				rootTargetObjectType = mapProcess2RootTargetType.get(selectedProcess);
				if (selectedProcess == null) {
					MessageBox.post("Please select a process.", "Error", MessageBox.ERROR);
					return;
				}
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
							creationDateBefore = new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(frame.creationBefore.getDate());
							queryCriteria.put("Created Before", creationDateBefore);
							processInput.creationDateBefore = creationDateBefore;
						}
						if (frame.creationAfter.getDate() != null) {
							creationDateAfter = new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(frame.creationAfter.getDate());
							queryCriteria.put("Created After", creationDateAfter);
							processInput.creationDateAfter = creationDateAfter;
						}
						if (frame.modifyBefore.getDate() != null) {
							modifyDateBefore = new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(frame.modifyBefore.getDate());
							queryCriteria.put("Modified Before", modifyDateBefore);
							processInput.modifyDateBefore = modifyDateBefore;
						}
						if (frame.modifyAfter.getDate() != null) {
							modifyDateAfter = new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(frame.modifyAfter.getDate());
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
//						
//						queryCriteria.clear();
//						queryCriteria.put("Workflow Template", "SCP Data Release Process (P - Status)");
//						queryCriteria.put("Modified Before", "26-Sep-2019 11:11");
//						queryCriteria.put("Modified After", "26-Sep-2019 11:09");

//						TCComponent[] queryResult = Query.queryItem(session, queryCriteria, "__TNH_JobNameQuery");
//						if (queryResult == null  ||queryResult.length == 0) {
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

	public void extractData2(ProcessStatusReportResponse response) throws TCException {
		SimpleDateFormat format1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		for (int i = 0; i < response.outputs.length; i++) {
			System.out.println("Item ID: " + response.outputs[i].itemID + " ItemName: " + response.outputs[i].itemName + "ItemRevisionID" + response.outputs[i].itemRevision + "OWNER" + response.outputs[i].owningUser.toString() + "TARGETS" + response.outputs[i].targets.toString() + "startDate" + response.outputs[i].startDate);

			ProcessReportObject aData = new ProcessReportObject();
			aData.setItemID(response.outputs[i].itemID);
			aData.setItemRevision(response.outputs[i].itemRevision);
			aData.setItemName(response.outputs[i].itemName);
			aData.setOwningUser(response.outputs[i].owningUser.toString());
			aData.setOwningGroup(response.outputs[i].owningUser);
			if (response.outputs[i].processStartDate != null) {
				aData.setCreationDate(format1.format(response.outputs[i].processStartDate.getTime()));
			}

			aData.setProcessStatus(response.outputs[i].processStatus);
			aData.setPendingTask(response.outputs[i].pendingTask);
			if (response.outputs[i].completedDate != null) {
				aData.setCompletedDate(format1.format(response.outputs[i].completedDate.getTime()));
			}

			if (response.outputs[i].startDate != null) {
				aData.setStartDate(format1.format(response.outputs[i].startDate.getTime()));
			}
			aData.setWfTemplate(response.outputs[i].wfTemplate);
			aData.setDescription(response.outputs[i].description);
			aData.setModuleGroup(response.outputs[i].moduleGroup);
			aData.setTargets(response.outputs[i].targets);
			aData.setSubTaskName(response.outputs[i].subTaskName);
			aData.setPendingUser(response.outputs[i].pendingUser);
			aData.setTaskStatus(response.outputs[i].taskStatus);
			aData.setResParty(response.outputs[i].resParty);
			if (response.outputs[i].dueDate != null) {
				aData.setDueDate(format1.format(response.outputs[i].dueDate.getTime()));
			}
			aData.setReviewer(response.outputs[i].reviewer);
			aData.setReviewerComment(response.outputs[i].reviewerComment);
			StringBuilder key = new StringBuilder();
			key.append(response.outputs[i].itemID).append(response.outputs[i].subTaskName);
			dataToExcel.put(key.toString(), aData);
		}
	}

	public void extractData(TCComponent[] comps) throws TCException {
		LinkedHashMap<String, String> lastestProcess = new LinkedHashMap<String, String>();
		for (int i = 0; i < comps.length; ++i) {
			TCComponentTask aTask = ((TCComponentProcess) comps[i]).getRootTask();
			String processName = "";
			String processStatus = "";
			String dueDate = "";
			String itemID = "";
			String itemRevision = "";
			String itemName = "";
			String description = "";
			String owningUser = "";
			String owningGroup = "";
			String moduleGroup = "";
			String creationDate = "";
			String pendingTask = "";
			String wfTemplate = "";
			String targets = "";

			try {
//				TCComponent[] rootTarget = aTask.getRelatedComponents("target_attachments");
				ArrayList<TCComponent> rootTarget = Utils.GetSecondaryObjectByRelationName(session, aTask, new String[] { rootTargetObjectType }, "Fnd0EPMTarget");
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

//				processName = aTask.getProperty("job_name");
//				ArrayList<String> tmp = splitObjectNameString(processName);
//				itemID 			= tmp.get(0);
//				itemRevision 	= tmp.get(1);
//				itemName 		= tmp.get(2);
				owningUser = aTask.getProperty("owning_user");
				owningGroup = aTask.getProperty("owning_group");
				creationDate = aTask.getProperty("creation_date");

				/* storage latest process */
				if (lastestProcess.containsKey(itemID) && Utils.compareDate(lastestProcess.get(itemID), creationDate) < 0) {
					continue;
				} else {
					lastestProcess.put(itemID, creationDate);
				}

				wfTemplate = aTask.getProperty("root_task");
				StringBuilder strBuilderTargets = new StringBuilder();
				for (int x = 0; x < aTask.getRelatedComponents("root_target_attachments").length; x++) {
					TCComponent firstTarget = aTask.getRelatedComponents("root_target_attachments")[x];
					if (firstTarget.getProperty("object_type").compareToIgnoreCase("Engineering Change Notice Revision") == 0 || firstTarget.getProperty("object_type").compareToIgnoreCase("Engineering Change Request Revision") == 0 || firstTarget.getProperty("object_type").compareToIgnoreCase("ERN Revision") == 0) {

						description = firstTarget.getProperty("object_desc");
						TCComponent item = firstTarget.getRelatedComponent("items_tag");
						if (item.getProperty("vf6_module_group") != null) {
							moduleGroup = item.getProperty("vf6_module_group");
						}
						if (firstTarget.getProperty("object_type").compareToIgnoreCase("ERN Revision") == 0) {
							moduleGroup = firstTarget.getProperty("vf4_main_module");
						}
					}
					if (firstTarget.getProperty("object_type").compareToIgnoreCase("VF Design Revision") == 0 || firstTarget.getProperty("object_type").compareToIgnoreCase("EBUS Design Revision") == 0 || firstTarget.getProperty("object_type").compareToIgnoreCase("Car Line Itm Revision") == 0 || firstTarget.getProperty("object_type").compareToIgnoreCase("Engineering Change Notice Revision") == 0
							|| firstTarget.getProperty("object_type").compareToIgnoreCase("Engineering Change Request Revision") == 0 || firstTarget.getProperty("object_type").compareToIgnoreCase("ERN Revision") == 0 || firstTarget.getProperty("object_type").compareToIgnoreCase("VF BP Design Revision") == 0 || firstTarget.getProperty("object_type").compareToIgnoreCase("Battery Line Revision") == 0 || firstTarget.getProperty("object_type").compareToIgnoreCase("Spec Document Revision") == 0) {
						if (strBuilderTargets.length() > 0) {
							strBuilderTargets.append(TOKEN_3);
						}
						strBuilderTargets.append(firstTarget.getProperty("object_string"));
					}
				}
				targets = strBuilderTargets.toString();

				/* loop each child task to get detail information */
				TCComponentTask[] subTask = aTask.getSubtasks();
				for (int y = 0; y < subTask.length; ++y) {
					String subTaskName = "";
					String subTaskStatus = "";
					String subTaskResult = "";
					String resParty = "";
					String startDate = "";
					String completedDate = "";
					String taskType = "";
					if (subTask[y].getProperty("task_type") != null) {
						taskType = subTask[y].getProperty("task_type");
						if (taskType.compareToIgnoreCase("EPMOrTask") == 0 || taskType.compareToIgnoreCase("EPMTask") == 0 || taskType.compareToIgnoreCase("EPMAddStatusTask") == 0) {
							continue;
						}
					}

					/* get reviewer OR performer */
					StringBuilder doneBy = new StringBuilder();
					StringBuilder reviewerOrPerformer = new StringBuilder();
					StringBuilder commentBuilder = new StringBuilder();
					StringBuilder pendingUser = new StringBuilder();
					if (subTask[y].getTaskType().compareToIgnoreCase("EPMReviewTask") == 0 || subTask[y].getTaskType().compareToIgnoreCase("EPMAcknowledgeTask") == 0) {
						TCComponentTask[] childTasks = subTask[y].getSubtasks();
						for (int k = 0; k < childTasks.length; k++) {
							if (childTasks[k].getTaskType().compareToIgnoreCase("EPMPerformSignoffTask") == 0) {
								for (int h = 0; h < childTasks[k].getRelated("signoff_attachments").length; h++) {
									if (reviewerOrPerformer.length() > 0) {
										reviewerOrPerformer.append(TOKEN_2);
									}
									TCComponentSignoff signoff = (TCComponentSignoff) childTasks[k].getRelatedComponents("signoff_attachments")[h];
									String[] temp = signoff.toString().split(TOKEN_1);
									reviewerOrPerformer.append(temp[0]);

									if (signoff.getComments().length() > 0) {
										if (commentBuilder.length() > 0) {
											commentBuilder.append(TOKEN_2);
										}
										commentBuilder.append(temp[0]).append(": ").append(signoff.getComments());
									}
									if (signoff.getDecisionDate() != null) {
										if (doneBy.length() > 0) {
											doneBy.append(TOKEN_2);
										}
										doneBy.append(temp[0]);
									} else {
										if (pendingUser.length() > 0) {
											pendingUser.append(TOKEN_2);
										}
										pendingUser.append(temp[0]);
									}

								}
								dueDate = childTasks[k].getProperty("due_date");
							}
						}
					} else {
						if (subTask[y].getProperty("fnd0Performer") != null) {
							reviewerOrPerformer.append(subTask[y].getProperty("fnd0Performer"));
						}
						dueDate = subTask[y].getProperty("due_date");
					}

					if (subTask[y].getProperty("object_name") != null) {
						subTaskName = subTask[y].getProperty("object_name");
					}
					if (subTask[y].getProperty("task_state") != null) {
						subTaskStatus = subTask[y].getProperty("task_state");
					}
					if (subTask[y].getProperty("resp_party") != null) {
						resParty = subTask[y].getProperty("resp_party");
					}
					if (subTask[y].getProperty("fnd0StartDate") != null) {
						startDate = subTask[y].getProperty("fnd0StartDate");
					}
					if (subTask[y].getProperty("fnd0EndDate") != null) {
						completedDate = subTask[y].getProperty("fnd0EndDate");
					}
					if (subTask[y].getProperty("task_result") != null) {
						subTaskResult = subTask[y].getProperty("task_result");
					}
					/* compare current date and due date to find late task */
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(startTime);
					DateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
					if (!dueDate.isEmpty()) {
						if (subTaskStatus.compareToIgnoreCase("Started") == 0 && Utils.compareDate(currentDate.format(cal.getTime()), dueDate) == -1) {
							subTaskStatus = "Late";
						}
					}
					ProcessReportObject aData = new ProcessReportObject();
					aData.setItemID(itemID);
					aData.setItemRevision(itemRevision);
					aData.setItemName(itemName);
					aData.setOwningUser(owningUser);
					aData.setOwningGroup(owningGroup);
					aData.setCreationDate(creationDate);
					aData.setProcessStatus(processStatus);
					aData.setPendingTask(pendingTask);
					aData.setCompletedDate(completedDate);
					aData.setStartDate(startDate);
					aData.setWfTemplate(wfTemplate);
					aData.setDescription(description);
					aData.setModuleGroup(moduleGroup);
					aData.setTargets(targets);
					aData.setSubTaskName(subTaskName);
					if (subTaskStatus.compareToIgnoreCase("Pending") == 0) {
						subTaskStatus = "Unassigned";
					}
					if (subTaskStatus.compareToIgnoreCase("Started") == 0 || subTaskStatus.compareToIgnoreCase("Late") == 0) {
						if (!pendingUser.toString().isEmpty()) {
							aData.setPendingUser(pendingUser.toString());
						} else {
							aData.setPendingUser(reviewerOrPerformer.toString());
						}
					}
					if (taskType.compareToIgnoreCase("EPMConditionTask") == 0 && subTaskStatus.compareToIgnoreCase("Completed") == 0) {
						subTaskStatus = subTaskResult;
					}
					aData.setTaskStatus(subTaskStatus);
					aData.setResParty(reviewerOrPerformer.toString());
					aData.setDueDate(dueDate);
					aData.setReviewer(doneBy.toString());
					aData.setReviewerComment(commentBuilder.toString());
					/* create a unique id */
					StringBuilder id = new StringBuilder();
					id.append(itemID).append(subTaskName);
					dataToExcel.put(id.toString(), aData);
				}

			} catch (TCException | NotLoadedException e) {
				e.printStackTrace();
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

			XSSFCell taskNameCell = row.createCell(10);
			taskNameCell.setCellValue(obj.getSubTaskName());
			taskNameCell.setCellStyle(headerCellStyle);

			XSSFCell taskStatus = row.createCell(11);
			taskStatus.setCellValue(obj.getTaskStatus());
			taskStatus.setCellStyle(headerCellStyle);

			XSSFCell resPartyCell = row.createCell(12);
			resPartyCell.setCellValue(obj.getResParty());
			resPartyCell.setCellStyle(headerCellStyle);

			XSSFCell reviewerCell = row.createCell(13);
			reviewerCell.setCellValue(obj.getReviewer());
			reviewerCell.setCellStyle(headerCellStyle);

			XSSFCell reviewerPendingCell = row.createCell(14);
			reviewerPendingCell.setCellValue(obj.getPendingUser());
			reviewerPendingCell.setCellStyle(headerCellStyle);

			XSSFCell commentCell = row.createCell(15);
			commentCell.setCellValue(obj.getReviewerComment());
			commentCell.setCellStyle(headerCellStyle);

			XSSFCell starDateCell = row.createCell(16);
			starDateCell.setCellValue(obj.getStartDate());
			starDateCell.setCellStyle(headerCellStyle);

			XSSFCell dueDateCell = row.createCell(17);
			dueDateCell.setCellValue(obj.getDueDate());
			dueDateCell.setCellStyle(headerCellStyle);

			XSSFCell completeDateCell = row.createCell(18);
			completeDateCell.setCellValue(obj.getCompletedDate());
			completeDateCell.setCellStyle(headerCellStyle);

			XSSFCell targetsCell = row.createCell(19);
			if (obj.getTargets().length() > 32726) {
				targetsCell.setCellValue("OVER LIMITATION CHARACTER");
			} else {
				targetsCell.setCellValue(obj.getTargets());
			}
			targetsCell.setCellStyle(headerCellStyle);

			rownum++;
		}

		for (int kz = 0; kz < 17; kz++) {
			spreadsheet.autoSizeColumn(kz);
		}
	}

	private void publishReport() {
		try {
			REPORT_PREFIX = ProcessStatusReportDetail.selectedProcess;
			File report = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET, Query.QUERY_JES_ENTRY_DATASET_NAME, DEFAULT_DATASET_TEMPLATE_NAME_DEV, TEMP_DIR, REPORT_PREFIX, startTime);

			InputStream fileIn = new FileInputStream(report);
			ProcessStatusReportDetail.wb = new XSSFWorkbook(fileIn);
			writeARowReportDefault();
			fileIn.close();
			FileOutputStream fos = new FileOutputStream(report);
			ProcessStatusReportDetail.wb.write(fos);
			fos.close();

			report.setWritable(false);
			Desktop.getDesktop().open(report);

		} catch (IOException | TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<String> splitObjectNameString(String input) {
		ArrayList<String> output = new ArrayList<String>();
		String itemID = "";
		String itemRev = "";
		String itemName = "";
		String sub1 = "";
		String sub2 = "";
		int idxOfNextWord = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == ';') {
				sub1 = input.substring(idxOfNextWord, i);
				idxOfNextWord = i + 1;
				sub2 = input.substring(idxOfNextWord);
				break;
			}
		}
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '-') {
				sub1 = input.substring(idxOfNextWord, i);
				idxOfNextWord = i + 1;
				itemName = input.substring(idxOfNextWord);
				break;
			}
		}
		idxOfNextWord = 0;
		for (int j = 0; j < sub1.length(); j++) {
			if (sub1.charAt(j) == '/') {
				itemID = sub1.substring(idxOfNextWord, j);
				idxOfNextWord = j + 1;
				itemRev = sub1.substring(idxOfNextWord);
				break;
			}
		}
		idxOfNextWord = 0;
		for (int k = 0; k < sub2.length(); k++) {
			if (sub2.charAt(k) == '-') {
				idxOfNextWord = k + 1;
				itemName = sub2.substring(idxOfNextWord);
				break;
			}
		}
		/* for case process name is ERN000005400-SWITCHES */
		if (itemID.compareTo("") == 0) {
			idxOfNextWord = 0;
			for (int i = 0; i < input.length(); i++) {
				if (input.charAt(i) == '-') {
					itemID = input.substring(idxOfNextWord, i);
					idxOfNextWord = i + 1;
					itemName = input.substring(idxOfNextWord);
					break;
				}
			}
			if (itemName.compareTo("") == 0) {
				itemName = input;
			}
		}

		/* for case CUV - ERN000001428 */
		if (itemID.contains("-")) {
			String[] idWithoutDash = itemID.split(" - ");
			itemID = idWithoutDash[1];
		}
		output.add(itemID);
		output.add(itemRev);
		output.add(itemName);
		return output;
	}

}
