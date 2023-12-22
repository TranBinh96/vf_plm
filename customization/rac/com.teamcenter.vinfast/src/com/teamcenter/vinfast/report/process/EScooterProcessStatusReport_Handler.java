package com.teamcenter.vinfast.report.process;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.model.EScooterProcessStatusModel;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class EScooterProcessStatusReport_Handler extends AbstractHandler {
	private EScooterProcessStatusReport_Dialog dlg;
	private TCSession session;
	private DataManagementService dmService;
	private final String TOKEN_1 = " - ";
	private final String TOKEN_2 = ",";
	private final String PENDING_STATUS = "PENDING";
	private final String APPROVED_STATUS = "APPROVED";
	private final String STUCK_STATUS = "STUCK";
	private final String REJECTED_STATUS = "REJECTED";
	private final String ABORTED_STATUS = "ABORTED";

	private LinkedHashMap<String, String[]> mapProcess2RootTargetType;
	private LinkedHashMap<String, String> modelDataForm;
	private LinkedHashMap<String, String> modelMapping;
	private LinkedHashMap<String, String> mapProcess2ReportTemplate;
	private LinkedHashMap<String, EScooterProcessStatusModel> dataToExcel;
	private List<String> modelFilters;

	private ProgressMonitorDialog progressMonitorDialog = null;

	public EScooterProcessStatusReport_Handler() {
		super();

	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);

			mapProcess2RootTargetType = new LinkedHashMap<String, String[]>();
			mapProcess2ReportTemplate = new LinkedHashMap<String, String>();
			modelDataForm = new LinkedHashMap<String, String>();
			modelMapping = new LinkedHashMap<>();
			String[] values = TCExtension.GetPreferenceValues("VINF_SPECBOOK_MODEL_CODE", session);
			for (String value : values) {
				String[] str = value.split("=");
				if (str.length > 2) {
					if (str[1].compareTo("E-SCOOTER") == 0) {
						modelMapping.put(str[0], str[2]);
					}
				}
			}

			String[] prefValue = TCExtension.GetPreferenceValues("VINFAST_ESCOOTER_PROCESS_STATUS_REPORT", session);
			for (String aLinePre : prefValue) {
				String[] arrTmp = aLinePre.split("=");
				if (arrTmp.length > 2) {
					mapProcess2RootTargetType.put(arrTmp[0], arrTmp[1].split(";"));
					mapProcess2ReportTemplate.put(arrTmp[0], arrTmp[2]);
				}
			}
			modelDataForm = TCExtension.GetLovValueAndDisplay("vf4_donor_veh", "VF3_Scooter_part", session);

			dlg = new EScooterProcessStatusReport_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);

			dlg.cbProcess.setItems(mapProcess2RootTargetType.keySet().toArray(new String[0]));
			dlg.cbModel.setItems(modelDataForm.values().toArray(new String[0]));

			dlg.btnAddModel.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					addModel();
				}
			});

			dlg.btnRemoveModel.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					removeModel();
				}
			});

			dlg.ckbCreateBefore.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dlg.datCreateBefore.setVisible(dlg.ckbCreateBefore.getSelection());
				}
			});

			dlg.ckbCreateAfter.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dlg.datCreateAfter.setVisible(dlg.ckbCreateAfter.getSelection());
				}
			});

			dlg.ckbModifyBefore.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dlg.datModifyBefore.setVisible(dlg.ckbModifyBefore.getSelection());
				}
			});

			dlg.ckbModifyAfter.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dlg.datModifyAfter.setVisible(dlg.ckbModifyAfter.getSelection());
				}
			});

			dlg.btnAccept.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					extractData();
				}
			});
			updateDateTimeControl();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void updateDateTimeControl() {
		dlg.datCreateBefore.setVisible(dlg.ckbCreateBefore.getSelection());
		dlg.datCreateAfter.setVisible(dlg.ckbCreateAfter.getSelection());
		dlg.datModifyBefore.setVisible(dlg.ckbModifyBefore.getSelection());
		dlg.datModifyAfter.setVisible(dlg.ckbModifyAfter.getSelection());
	}

	private void extractData() {
		String processTemplate = dlg.cbProcess.getText();

		if (processTemplate.isEmpty()) {
			MessageBox.post("Please select a process.", "Error", MessageBox.ERROR);
			return;
		}

		try {
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			queryInput.put("Workflow Template", processTemplate);
			SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
			if (dlg.ckbCreateBefore.getSelection())
				queryInput.put("Created Before", df.format(StringExtension.getDatetimeFromWidget(dlg.datCreateBefore).getTime()));
			if (dlg.ckbCreateAfter.getSelection())
				queryInput.put("Created After", df.format(StringExtension.getDatetimeFromWidget(dlg.datCreateAfter).getTime()));
			if (dlg.ckbModifyBefore.getSelection())
				queryInput.put("Modify Before", df.format(StringExtension.getDatetimeFromWidget(dlg.datModifyBefore).getTime()));
			if (dlg.ckbModifyAfter.getSelection())
				queryInput.put("Modify After", df.format(StringExtension.getDatetimeFromWidget(dlg.datModifyAfter).getTime()));
			if (!dlg.txtProcessName.getText().isEmpty()) {
				if (!dlg.txtProcessName.getText().contains(";")) {
					queryInput.put("Job Name", "*" + dlg.txtProcessName.getText() + "*");
				} else {
					String[] split = dlg.txtProcessName.getText().split(";");
					for (int i = 0; i < split.length; i++) {
						split[i] = "*" + split[i] + "*";
					}
					queryInput.put("Job Name", String.join(";", split));
				}
			}
			if (dlg.ckbRunningProcess.getSelection())
				queryInput.put("State", "4");

			TCComponent[] queryOutput = Query.queryItem(session, queryInput, "__TNH_JobNameQuery");
			if (queryOutput == null || queryOutput.length == 0) {
				MessageBox.post("There are not object has been found", "INFO", MessageBox.INFORMATION);
				return;
			}

			modelFilters = null;
			if (dlg.lstModel.getItemCount() > 0)
				modelFilters = Arrays.asList(dlg.lstModel.getItems());

			dataToExcel = new LinkedHashMap<String, EScooterProcessStatusModel>();

			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Export processing...", IProgressMonitor.UNKNOWN);
					try {
						int totalValue = queryOutput.length;
						int processedValue = 0;
						LinkedHashMap<String, String> lastestProcess = new LinkedHashMap<String, String>();
						String[] rootTargetObjectType = mapProcess2RootTargetType.get(processTemplate);
						dmService.getProperties(queryOutput, new String[] { "job_name", "owning_user", "owning_group", "fnd0StartDate", "root_task", "real_state", "release_status_attachments", "root_reference_attachments", "root_target_attachments", "fnd0EndDate", "fnd0EndDate" });
						for (TCComponent queryItem : queryOutput) {
							processedValue++;
							monitor.subTask("Processed: " + processedValue + "/" + totalValue);
							EScooterProcessStatusModel aData = new EScooterProcessStatusModel();
							String itemID = "";
							String itemRevision = "";
							String itemName = "";
							String description = "";
							String moduleGroup = "";
							String impactModule = "";
							String classification = "";
							String priority = "";
							String exchangeNewPart = "";
							String exchangeOldPart = "";
							String disposalCode = "";
							String changeReason = "";
							String problems = "";
							String solutions = "";
							String wfTemplate = "";
							String processDesc = "";
							String bigCostImpact = "";
							Set<String> pendingTasks = new HashSet<String>();
							String pendingUser = "";
							String targets = "";
							String taskStatus = "";
							String creationDate = "";
							String startDate = "";
							String completedDate = "";
							String owningUser = "";
							String owningGroup = "";

							String processName = "";
							String processStatus = "";
							String dueDate = "";
							String reviewerComment = "";
							String reviewer = "";
							String resParty = "";
							String subTaskName = "";
							String documentType = "";
							String model = "";

							try {
								TCComponentTask rootTask = ((TCComponentProcess) queryItem).getRootTask();
								ArrayList<TCComponent> rootTarget = Utils.GetSecondaryObjectByRelationName(session, rootTask, rootTargetObjectType, "Fnd0EPMTarget");
								dmService.getProperties(rootTarget.toArray(new TCComponent[0]), new String[] { "item_id", "item_revision_id", "object_name", "vf4_bom_vfPartNumber" });
								if (rootTarget == null || rootTarget.size() == 0)
									continue;

								String[] className = rootTarget.get(0).getClassNameHierarchy();
								if (Arrays.asList(className).contains("VF4_line_itemRevision"))
									itemID = rootTarget.get(0).getPropertyDisplayableValue("vf4_bom_vfPartNumber");
								else
									itemID = rootTarget.get(0).getPropertyDisplayableValue("item_id");

								if (lastestProcess.containsKey(itemID) && Utils.compareDate(lastestProcess.get(itemID), creationDate) < 0)
									continue;

								lastestProcess.put(itemID, creationDate);

								processName = rootTask.getProperty("job_name");
								owningUser = rootTask.getProperty("owning_user");
								owningGroup = rootTask.getProperty("owning_group");
								startDate = rootTask.getProperty("fnd0StartDate");
								wfTemplate = rootTask.getProperty("root_task");
								itemRevision = rootTarget.get(0).getPropertyDisplayableValue("item_revision_id");
								itemName = rootTarget.get(0).getPropertyDisplayableValue("object_name");

								String currentStatus = rootTask.getProperty("real_state");
								String releaseStatus = rootTask.getProperty("release_status_attachments");
								LinkedList<String> targetList = new LinkedList<String>();

								TCComponent[] targetAttachments = rootTask.getRelatedComponents("root_target_attachments");
								if (targetAttachments != null) {
									dmService.getProperties(targetAttachments, new String[] { "object_type" });
									for (TCComponent firstTarget : targetAttachments) {
										String type = firstTarget.getProperty("object_type");
										if (type.compareToIgnoreCase("Engineering Change Notice Revision - Escooter") == 0) {
											dmService.getProperties(new TCComponent[] { firstTarget }, new String[] { "vf4_model", "object_desc", "vf4_module_group", "vf4_impact_module", "vf4_classification", "vf4_priority", "vf4_exchangeability_newpart", "vf4_exchangeability_oldpart", "vf4_disposal_code", "vf4_change_reason", "CMHasProblemItem", "CMHasSolutionItem", "creation_date", "vf4_process_desc", "vf4_is_big_cost_impact" });
											model = firstTarget.getPropertyDisplayableValue("vf4_model");
											description = firstTarget.getProperty("object_desc");
											moduleGroup = firstTarget.getProperty("vf4_module_group");
											impactModule = firstTarget.getProperty("vf4_impact_module");
											classification = firstTarget.getProperty("vf4_classification");
											priority = firstTarget.getProperty("vf4_priority");
											exchangeNewPart = firstTarget.getProperty("vf4_exchangeability_newpart");
											exchangeOldPart = firstTarget.getProperty("vf4_exchangeability_oldpart");
											disposalCode = firstTarget.getProperty("vf4_disposal_code");
											changeReason = firstTarget.getProperty("vf4_change_reason");
											problems = firstTarget.getProperty("CMHasProblemItem");
											solutions = firstTarget.getProperty("CMHasSolutionItem");
											creationDate = firstTarget.getProperty("creation_date");
											processDesc = firstTarget.getProperty("vf4_process_desc");
											bigCostImpact = firstTarget.getProperty("vf4_is_big_cost_impact");
										} else if (type.compareTo("Spec Document Revision") == 0 || type.compareTo("EFMEA Document Revision") == 0) {
											TCComponentItem firstTartgetItem = ((TCComponentItemRevision) firstTarget).getItem();
											dmService.getProperties(new TCComponent[] { firstTarget }, new String[] { "object_desc", "creation_date" });
											dmService.getProperties(new TCComponent[] { firstTartgetItem }, new String[] { "vf3_model_code", "vf3_module_name", "vf3_doc_type" });

											String modelCode = TCExtension.GetPropertyRealValue(firstTartgetItem, "vf3_model_code");
											model = modelMapping.getOrDefault(modelCode, "");
											moduleGroup = firstTartgetItem.getPropertyDisplayableValue("vf3_module_name");
											documentType = firstTartgetItem.getPropertyDisplayableValue("vf3_doc_type");

											description = firstTarget.getProperty("object_desc");
											creationDate = firstTarget.getProperty("creation_date");
										} else if (type.compareToIgnoreCase("VF Design Revision") == 0 || type.compareToIgnoreCase("EBUS Design Revision") == 0 || type.compareToIgnoreCase("Car Line Itm Revision") == 0 || type.compareToIgnoreCase("Engineering Change Notice Revision") == 0 || type.compareToIgnoreCase("Engineering Change Request Revision") == 0 || type.compareToIgnoreCase("ERN Revision") == 0 || type.compareToIgnoreCase("VF BP Design Revision") == 0
												|| type.compareToIgnoreCase("Battery Line Revision") == 0) {
											dmService.getProperties(new TCComponent[] { firstTarget }, new String[] { "object_string" });
											targetList.add(firstTarget.getProperty("object_string"));
										} else if (type.compareTo("VF D Standard Revision") == 0) {
											dmService.getProperties(new TCComponent[] { firstTarget }, new String[] { "creation_date", "vf9_module_lev1", "object_type", "object_desc" });
											moduleGroup = firstTarget.getProperty("vf9_module_lev1");
											description = firstTarget.getProperty("object_desc");
											creationDate = firstTarget.getProperty("creation_date");
											documentType = firstTarget.getProperty("object_type");
										}
									}
								}

								TCComponent[] referenceAttachments = rootTask.getRelatedComponents("root_reference_attachments");
								if (referenceAttachments != null) {
									for (TCComponent firstRefer : referenceAttachments) {
										String type = firstRefer.getProperty("object_type");
										if (type.compareToIgnoreCase("Engineering Change Notice Revision - Escooter") == 0) {
											dmService.getProperties(new TCComponent[] { firstRefer }, new String[] { "vf4_model", "object_desc", "vf4_module_group", "vf4_impact_module", "vf4_classification", "vf4_priority", "vf4_exchangeability_newpart", "vf4_exchangeability_oldpart", "vf4_disposal_code", "vf4_change_reason", "CMHasProblemItem", "CMHasSolutionItem", "creation_date", "vf4_process_desc", "vf4_is_big_cost_impact" });
											model = firstRefer.getProperty("vf4_model");
											description = firstRefer.getProperty("object_desc");
											moduleGroup = firstRefer.getProperty("vf4_module_group");
											impactModule = firstRefer.getProperty("vf4_impact_module");
											classification = firstRefer.getProperty("vf4_classification");
											priority = firstRefer.getProperty("vf4_priority");
											exchangeNewPart = firstRefer.getProperty("vf4_exchangeability_newpart");
											exchangeOldPart = firstRefer.getProperty("vf4_exchangeability_oldpart");
											disposalCode = firstRefer.getProperty("vf4_disposal_code");
											changeReason = firstRefer.getProperty("vf4_change_reason");
											problems = firstRefer.getProperty("CMHasProblemItem");
											solutions = firstRefer.getProperty("CMHasSolutionItem");
											creationDate = firstRefer.getProperty("creation_date");
											processDesc = firstRefer.getProperty("vf4_process_desc");
											bigCostImpact = firstRefer.getProperty("vf4_is_big_cost_impact");
										} else if (type.compareTo("Spec Document Revision") == 0 || type.compareTo("EFMEA Document Revision") == 0) {
											TCComponentItem firstReferItem = ((TCComponentItemRevision) firstRefer).getItem();
											dmService.getProperties(new TCComponent[] { firstRefer }, new String[] { "object_desc", "creation_date" });
											dmService.getProperties(new TCComponent[] { firstReferItem }, new String[] { "vf3_model_code", "vf3_module_name", "vf3_doc_type" });

											String modelCode = TCExtension.GetPropertyRealValue(firstReferItem, "vf3_model_code");
											model = modelMapping.getOrDefault(modelCode, "");
											moduleGroup = firstReferItem.getPropertyDisplayableValue("vf3_module_name");
											documentType = firstReferItem.getPropertyDisplayableValue("vf3_doc_type");

											description = firstRefer.getProperty("object_desc");
											creationDate = firstRefer.getProperty("creation_date");
										} else if (type.compareToIgnoreCase("VF Design Revision") == 0 || type.compareToIgnoreCase("EBUS Design Revision") == 0 || type.compareToIgnoreCase("Car Line Itm Revision") == 0 || type.compareToIgnoreCase("Engineering Change Notice Revision") == 0 || type.compareToIgnoreCase("Engineering Change Request Revision") == 0 || type.compareToIgnoreCase("ERN Revision") == 0 || type.compareToIgnoreCase("VF BP Design Revision") == 0
												|| type.compareToIgnoreCase("Battery Line Revision") == 0) {
											dmService.getProperties(new TCComponent[] { firstRefer }, new String[] { "object_string" });
											targetList.add(firstRefer.getProperty("object_string"));
										} else if (type.compareTo("VF D Standard Revision") == 0) {
											dmService.getProperties(new TCComponent[] { firstRefer }, new String[] { "creation_date", "vf9_module_lev1", "object_type", "object_desc" });
											moduleGroup = firstRefer.getProperty("vf9_module_lev1");
											description = firstRefer.getProperty("object_desc");
											creationDate = firstRefer.getProperty("creation_date");
											documentType = firstRefer.getProperty("object_type");
										}
									}
								}

								/* set job name to target if target blank */
								if (targetList.size() == 0) {
									targetList.add(processName);
								}

								targets = String.join("\n", targetList);

								if (currentStatus.compareToIgnoreCase("Completed") == 0) {
									if (releaseStatus.contains("Rejected")) {
										processStatus = REJECTED_STATUS;
									} else {
										processStatus = APPROVED_STATUS;
									}
									completedDate = rootTask.getProperty("fnd0EndDate");
								} else if (currentStatus.compareToIgnoreCase("Aborted") == 0) {
									processStatus = ABORTED_STATUS;
									completedDate = rootTask.getProperty("fnd0EndDate");
								} else {
									processStatus = PENDING_STATUS;
									pendingTasks = new HashSet<String>(Arrays.asList(rootTask.getProperty("fnd0StartedTasks").split(", ")));// removeDupicateWord(rootTask.getProperty("fnd0StartedTasks"), TOKEN_2);
									Set<String> pendingUserList = new HashSet<String>();
									List<TCComponentSignoff> requiredSignOff = new LinkedList<TCComponentSignoff>();
									boolean isAllRequiredDecised = true;
									if (pendingTasks != null) {
										for (String pendingTask : pendingTasks) {
											TCComponentTask subTask = rootTask.getSubtask(pendingTask);
											if (subTask == null) {
												processStatus = STUCK_STATUS;
												continue;
											}

											if (subTask.getDueDate() != null)
												dueDate = df.format(subTask.getDueDate());

											if (subTask.getTaskType().compareTo("EPMReviewTask") == 0 || subTask.getTaskType().compareTo("EPMAcknowledgeTask") == 0) {
												TCComponentTask[] childTasks = subTask.getSubtasks();
												for (int k = 0; k < childTasks.length; k++) {
													if (childTasks[k].getTaskType().compareToIgnoreCase("EPMPerformSignoffTask") == 0) {
														for (int h = 0; h < childTasks[k].getRelated("signoff_attachments").length; h++) {
															TCComponentSignoff signoff = (TCComponentSignoff) childTasks[k].getRelatedComponents("signoff_attachments")[h];
															if (signoff.isDecisionRequired()) {
																requiredSignOff.add(signoff);
															}
															if (signoff.getDecisionDate() == null) {
																String[] temp = signoff.toString().split(TOKEN_1);
																pendingUserList.add(temp[0]);
															}
														}
													}
												}
											} else if (subTask.getTaskType().compareTo("EPMTask") == 0) {
												processStatus = STUCK_STATUS;
											} else {
												if (subTask.getProperty("fnd0Performer") != null)
													pendingUserList.add(subTask.getProperty("fnd0Performer"));
											}
										}
									}

									if (requiredSignOff.size() > 0) {
										for (TCComponentSignoff signOff : requiredSignOff) {
											if (signOff.getDecisionDate() == null)
												isAllRequiredDecised = false;
										}
										if (isAllRequiredDecised == true)
											processStatus = STUCK_STATUS;
									}
									pendingUser = String.join(", ", pendingUserList);
								}

								aData.setItemID(itemID);
								aData.setItemRevision(itemRevision);
								aData.setItemName(itemName);
								aData.setDescription(description);
								aData.setModuleGroup(moduleGroup);
								aData.setImpactModule(impactModule);
								aData.setClassification(classification);
								aData.setPriority(priority);
								aData.setExchangeNewPart(exchangeNewPart);
								aData.setExchangeOldPart(exchangeOldPart);
								aData.setDisposalCode(disposalCode);
								aData.setChangeReason(changeReason);
								aData.setProblems(problems);
								aData.setSolutions(solutions);
								aData.setWfTemplate(wfTemplate);
								aData.setProcessDesc(processDesc);
								aData.setBigCostImpact(bigCostImpact);
								aData.setPendingTask(pendingTasks == null ? "" : String.join(", ", pendingTasks));
								aData.setPendingUser(pendingUser);
								aData.setTargets(targets);
								aData.setProcessStatus(processStatus);
								aData.setCreationDate(creationDate);
								aData.setStartDate(startDate);
								aData.setDueDate(dueDate);
								aData.setCompletedDate(completedDate);
								aData.setOwningUser(owningUser);
								aData.setOwningGroup(owningGroup);
								aData.setDocumentType(documentType);

								if (validModel(model))
									dataToExcel.put(itemID, aData);
							} catch (TCException | NotLoadedException e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		publishReport();
	}

	private String removeDupicateWord(String input, String token) {
		final String[] strWords = input.split(", ");
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

	private void publishReport() {
		try {
			String processTemplate = dlg.cbProcess.getText();
			String fileTemplateName = mapProcess2ReportTemplate.get(processTemplate);

			File templateFile = downloadTemplateFile(processTemplate + "_" + StringExtension.getTimeStamp(), fileTemplateName);
			if (templateFile == null) {
				MessageBox.post("Cannot get Report Template file. Please contact your administrator for further instructions with below messages.", "Export ECR Information failed", MessageBox.ERROR);
				return;
			}

			InputStream outputFile = new FileInputStream(templateFile);
			XSSFWorkbook workbook = new XSSFWorkbook(outputFile);
			templateFile.setWritable(true);

			XSSFSheet spreadsheet = workbook.getSheet("Report");

			CellStyle defaultCellStyle = workbook.createCellStyle();
			defaultCellStyle.setAlignment(HorizontalAlignment.LEFT);
			defaultCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			defaultCellStyle.setBorderTop(BorderStyle.THIN);
			defaultCellStyle.setBorderBottom(BorderStyle.THIN);
			defaultCellStyle.setBorderLeft(BorderStyle.THIN);
			defaultCellStyle.setBorderRight(BorderStyle.THIN);

			if (fileTemplateName.compareTo("Scooter_ECN_Process_Status_Report_Template") == 0) {
				ecnProcessTemplate(defaultCellStyle, spreadsheet);
			} else if (fileTemplateName.compareTo("EScooter_SOR_Release_Process_Status_Report_Template") == 0) {
				sorProcessTemplate(defaultCellStyle, spreadsheet);
			} else if (fileTemplateName.compareTo("EScooter_Release_Process_Status_Report_Template") == 0) {
				releaseProcessTemplate(defaultCellStyle, spreadsheet);
			}

			for (int kz = 0; kz < 18; kz++) {
				spreadsheet.autoSizeColumn(kz);
			}

			outputFile.close();

			FileOutputStream outputStream = new FileOutputStream(templateFile);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();

			Desktop.getDesktop().open(templateFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean validModel(String models) {
		if (modelFilters == null)
			return true;

		for (String model : models.split(",")) {
			if (modelFilters.contains(model))
				return true;
		}

		return false;
	}

	private void removeModel() {
		int index = dlg.lstModel.getSelectionIndex();
		if (index != -1) {
			dlg.lstModel.remove(index);
		}
	}

	private void addModel() {
		String model = dlg.cbModel.getText();
		if (!model.isEmpty()) {
			for (String value : dlg.lstModel.getItems()) {
				if (value.compareTo(model) == 0)
					return;
			}

			dlg.lstModel.add(model);
		}
	}

	private File downloadTemplateFile(String fileName, String templateName) {
		LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
		parameter.put("Name", templateName);
		parameter.put("Dataset Type", "MS ExcelX");
		parameter.put("Owning Group", "dba");

		TCComponent[] item_list = Query.queryItem(session, parameter, "Dataset...");
		if (item_list != null && item_list.length > 0)
			return TCExtension.downloadDataset(System.getProperty("java.io.tmpdir"), (TCComponentDataset) item_list[0], "MSExcelX", fileName + ".xlsm", session);

		return null;
	}

	private void releaseProcessTemplate(CellStyle defaultCellStyle, XSSFSheet spreadsheet) {
		int rownum = 1;
		for (Map.Entry<String, EScooterProcessStatusModel> entry : dataToExcel.entrySet()) {
			EScooterProcessStatusModel obj = entry.getValue();
			XSSFRow row = spreadsheet.createRow(rownum);

			XSSFCell noCell = row.createCell(0);
			noCell.setCellValue(rownum);
			noCell.setCellStyle(defaultCellStyle);

			XSSFCell ownerCell = row.createCell(1);
			ownerCell.setCellValue(obj.getOwningUser());
			ownerCell.setCellStyle(defaultCellStyle);

			XSSFCell owningGroupCell = row.createCell(2);
			owningGroupCell.setCellValue(obj.getOwningGroup());
			owningGroupCell.setCellStyle(defaultCellStyle);

			XSSFCell cel9 = row.createCell(3);
			cel9.setCellValue(obj.getWfTemplate());
			cel9.setCellStyle(defaultCellStyle);

			XSSFCell pendingTaskCell = row.createCell(4);
			pendingTaskCell.setCellValue(obj.getPendingTask());
			pendingTaskCell.setCellStyle(defaultCellStyle);

			XSSFCell pendingUserCell = row.createCell(5);
			pendingUserCell.setCellValue(obj.getPendingUser());
			pendingUserCell.setCellStyle(defaultCellStyle);

			XSSFCell targetCell = row.createCell(6);
			targetCell.setCellValue(obj.getTargets());
			targetCell.setCellStyle(defaultCellStyle);

			XSSFCell processStatusCell = row.createCell(7);
			processStatusCell.setCellValue(obj.getProcessStatus());
			processStatusCell.setCellStyle(defaultCellStyle);

			XSSFCell createDateCell = row.createCell(8);
			createDateCell.setCellValue(obj.getCreationDate());
			createDateCell.setCellStyle(defaultCellStyle);

			XSSFCell startDateCell = row.createCell(9);
			startDateCell.setCellValue(obj.getStartDate());
			startDateCell.setCellStyle(defaultCellStyle);

			XSSFCell dueDateCell = row.createCell(10);
			dueDateCell.setCellValue(obj.getDueDate());
			dueDateCell.setCellStyle(defaultCellStyle);

			XSSFCell completeDateCell = row.createCell(11);
			completeDateCell.setCellValue(obj.getCompletedDate());
			completeDateCell.setCellStyle(defaultCellStyle);

			XSSFCell currentDateCell = row.createCell(12);
			currentDateCell.setCellValue(obj.getCurrentDate());
			currentDateCell.setCellStyle(defaultCellStyle);

			XSSFCell overDueDateCell = row.createCell(13);
			overDueDateCell.setCellValue(obj.getOverDueDate());
			overDueDateCell.setCellStyle(defaultCellStyle);

			rownum++;
		}
	}

	private void sorProcessTemplate(CellStyle defaultCellStyle, XSSFSheet spreadsheet) {
		int rownum = 1;
		for (Map.Entry<String, EScooterProcessStatusModel> entry : dataToExcel.entrySet()) {
			EScooterProcessStatusModel obj = entry.getValue();
			XSSFRow row = spreadsheet.createRow(rownum);

			XSSFCell numCell = row.createCell(0);
			numCell.setCellValue(rownum);
			numCell.setCellStyle(defaultCellStyle);

			XSSFCell cel1 = row.createCell(1);
			cel1.setCellValue(obj.getItemID());
			cel1.setCellStyle(defaultCellStyle);

			XSSFCell cel2 = row.createCell(2);
			cel2.setCellValue(obj.getItemRevision());
			cel2.setCellStyle(defaultCellStyle);

			XSSFCell cel3 = row.createCell(3);
			cel3.setCellValue(obj.getItemName());
			cel3.setCellStyle(defaultCellStyle);

			XSSFCell cel4 = row.createCell(4);
			cel4.setCellValue(obj.getDescription());
			cel4.setCellStyle(defaultCellStyle);

			XSSFCell cel5 = row.createCell(5);
			cel5.setCellValue(obj.getDocumentType());
			cel5.setCellStyle(defaultCellStyle);

			XSSFCell docTypeCell = row.createCell(6);
			docTypeCell.setCellValue(obj.getOwningUser());
			docTypeCell.setCellStyle(defaultCellStyle);

			XSSFCell cel6 = row.createCell(7);
			cel6.setCellValue(obj.getOwningGroup());
			cel6.setCellStyle(defaultCellStyle);

			XSSFCell cel7 = row.createCell(8);
			cel7.setCellValue(obj.getModuleGroup());
			cel7.setCellStyle(defaultCellStyle);

			XSSFCell cel8 = row.createCell(9);
			cel8.setCellValue(obj.getCreationDate());
			cel8.setCellStyle(defaultCellStyle);

			XSSFCell cel9 = row.createCell(10);
			cel9.setCellValue(obj.getWfTemplate());
			cel9.setCellStyle(defaultCellStyle);

			XSSFCell cel10 = row.createCell(11);
			cel10.setCellValue(obj.getPendingTask());
			cel10.setCellStyle(defaultCellStyle);

			XSSFCell cel11 = row.createCell(12);
			cel11.setCellValue(obj.getProcessStatus());
			cel11.setCellStyle(defaultCellStyle);

			XSSFCell cel12 = row.createCell(13);
			cel12.setCellValue(obj.getResParty());
			cel12.setCellStyle(defaultCellStyle);

			XSSFCell cel13 = row.createCell(14);
			cel13.setCellValue(obj.getReviewer());
			cel13.setCellStyle(defaultCellStyle);

			XSSFCell cel14 = row.createCell(15);
			cel14.setCellValue(obj.getPendingUser());
			cel14.setCellStyle(defaultCellStyle);

			XSSFCell cel15 = row.createCell(16);
			cel15.setCellValue(obj.getReviewerComment());
			cel15.setCellStyle(defaultCellStyle);

			XSSFCell cel16 = row.createCell(17);
			cel16.setCellValue(obj.getStartDate());
			cel16.setCellStyle(defaultCellStyle);

			XSSFCell cel17 = row.createCell(18);
			cel17.setCellValue(obj.getDueDate());
			cel17.setCellStyle(defaultCellStyle);

			XSSFCell cel18 = row.createCell(19);
			cel18.setCellValue(obj.getCompletedDate());
			cel18.setCellStyle(defaultCellStyle);

			XSSFCell cel19 = row.createCell(20);
			cel19.setCellValue(obj.getCurrentDate());
			cel19.setCellStyle(defaultCellStyle);

			XSSFCell cel20 = row.createCell(21);
			cel20.setCellValue(obj.getOverDueDate());
			cel20.setCellStyle(defaultCellStyle);

			rownum++;
		}
	}

	private void ecnProcessTemplate(CellStyle defaultCellStyle, XSSFSheet spreadsheet) {
		int rownum = 1;
		for (Map.Entry<String, EScooterProcessStatusModel> entry : dataToExcel.entrySet()) {
			EScooterProcessStatusModel obj = entry.getValue();
			XSSFRow row = spreadsheet.createRow(rownum);

			XSSFCell numCell = row.createCell(0);
			numCell.setCellValue(rownum);
			numCell.setCellStyle(defaultCellStyle);

			XSSFCell cel1 = row.createCell(1);
			cel1.setCellValue(obj.getItemID());
			cel1.setCellStyle(defaultCellStyle);

			XSSFCell cel2 = row.createCell(2);
			cel2.setCellValue(obj.getItemRevision());
			cel2.setCellStyle(defaultCellStyle);

			XSSFCell cel3 = row.createCell(3);
			cel3.setCellValue(obj.getItemName());
			cel3.setCellStyle(defaultCellStyle);

			XSSFCell cel4 = row.createCell(4);
			cel4.setCellValue(obj.getDescription());
			cel4.setCellStyle(defaultCellStyle);

			XSSFCell cel5 = row.createCell(5);
			cel5.setCellValue(obj.getModuleGroup());
			cel5.setCellStyle(defaultCellStyle);

			XSSFCell cel6 = row.createCell(6);
			cel6.setCellValue(obj.getImpactModule());
			cel6.setCellStyle(defaultCellStyle);

			XSSFCell cel7 = row.createCell(7);
			cel7.setCellValue(obj.getClassification());
			cel7.setCellStyle(defaultCellStyle);

			XSSFCell cel8 = row.createCell(8);
			cel8.setCellValue(obj.getPriority());
			cel8.setCellStyle(defaultCellStyle);

			XSSFCell cel9 = row.createCell(9);
			cel9.setCellValue(obj.getExchangeNewPart());
			cel9.setCellStyle(defaultCellStyle);

			XSSFCell cel10 = row.createCell(10);
			cel10.setCellValue(obj.getExchangeOldPart());
			cel10.setCellStyle(defaultCellStyle);

			XSSFCell cel11 = row.createCell(11);
			cel11.setCellValue(obj.getDisposalCode());
			cel11.setCellStyle(defaultCellStyle);

			XSSFCell cel12 = row.createCell(12);
			cel12.setCellValue(obj.getChangeReason());
			cel12.setCellStyle(defaultCellStyle);

			XSSFCell cel13 = row.createCell(13);
			cel13.setCellValue(obj.getProblems());
			cel13.setCellStyle(defaultCellStyle);

			XSSFCell cel14 = row.createCell(14);
			cel14.setCellValue(obj.getSolutions());
			cel14.setCellStyle(defaultCellStyle);

			XSSFCell cel15 = row.createCell(15);
			cel15.setCellValue(obj.getWfTemplate());
			cel15.setCellStyle(defaultCellStyle);

			XSSFCell cel18 = row.createCell(16);
			cel18.setCellValue(obj.getProcessDesc());
			cel18.setCellStyle(defaultCellStyle);

			XSSFCell cel19 = row.createCell(17);
			cel19.setCellValue(obj.getBigCostImpact());
			cel19.setCellStyle(defaultCellStyle);

			XSSFCell cel20 = row.createCell(18);
			cel20.setCellValue(obj.getPendingTask());
			cel20.setCellStyle(defaultCellStyle);

			XSSFCell cel21 = row.createCell(19);
			cel21.setCellValue(obj.getPendingUser());
			cel21.setCellStyle(defaultCellStyle);

			XSSFCell cel22 = row.createCell(20);
			cel22.setCellValue(obj.getTargets());
			cel22.setCellStyle(defaultCellStyle);

			XSSFCell statusCell = row.createCell(21);
			statusCell.setCellValue(obj.getProcessStatus());
			statusCell.setCellStyle(defaultCellStyle);

			XSSFCell creationDateCell = row.createCell(22);
			creationDateCell.setCellValue(obj.getCreationDate());
			creationDateCell.setCellStyle(defaultCellStyle);

			XSSFCell starDateCell = row.createCell(23);
			starDateCell.setCellValue(obj.getStartDate());
			starDateCell.setCellStyle(defaultCellStyle);

			XSSFCell completeDateCell = row.createCell(24);
			completeDateCell.setCellValue(obj.getCompletedDate());
			completeDateCell.setCellStyle(defaultCellStyle);

			XSSFCell ownerCell = row.createCell(25);
			ownerCell.setCellValue(obj.getOwningUser());
			ownerCell.setCellStyle(defaultCellStyle);

			XSSFCell owningGroupCell = row.createCell(26);
			owningGroupCell.setCellValue(obj.getOwningGroup());
			owningGroupCell.setCellStyle(defaultCellStyle);

			rownum++;
		}
	}
}
