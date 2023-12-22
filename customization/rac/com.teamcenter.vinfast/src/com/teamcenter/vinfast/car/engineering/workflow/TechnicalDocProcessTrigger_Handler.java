package com.teamcenter.vinfast.car.engineering.workflow;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.vinfast.admin.pal.ALAssistant_Constant;
import com.teamcenter.vinfast.escooter.sorprocess.model.TaskListModel;
import com.teamcenter.vinfast.model.ALModel;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

public class TechnicalDocProcessTrigger_Handler extends AbstractHandler {
	private TCSession session;
	private TechnicalDocProcessTrigger_Dialog dlg;
	private TCComponentItemRevision selectedObject = null;
	private static String WORKPROCESS_NAME = "VF_Technical_Doc_Release_Process";
	private final String MANUFACTURING_REVIEW_TASK = "VF Manufacturing";

	private ALModel assignmentListSelected = null;
	private LinkedHashMap<TCComponent, List<ValidationResult>> partsAndValidationResults = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private boolean isValidate = true;
	private LinkedList<TCComponentItemRevision> vfteRevisions = null;
	private LinkedHashMap<String, ALModel> alMapping = new LinkedHashMap<String, ALModel>();
	private LinkedHashMap<String, Set<TCComponentGroupMember>> shopMapping = null;

	private String docTypeValue = "";
	private String programValue = "";
	private String moduleValue = "";

	public TechnicalDocProcessTrigger_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

		if (validObjectSelect(targetComponents)) {
			MessageBox.post("Please Select 1 Document Revision.", "Warning", MessageBox.WARNING);
			return null;
		}

		vfteRevisions = new LinkedList<TCComponentItemRevision>();

		try {
			String objectType = selectedObject.getType();
			docTypeValue = objectType.compareTo("VF4_VFFS_docRevision") == 0 ? "VFFS" : TCExtension.GetPropertyRealValue(selectedObject.getItem(), "vf3_doc_type");
			if (docTypeValue.compareTo("FMEA") == 0) {
				if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
					MessageBox.post("You don't have write access to trigger process.", "Warning", MessageBox.WARNING);
					return null;
				}
			}

			programValue = TCExtension.GetPropertyRealValue(selectedObject.getItem(), "vf3_model_code");
			moduleValue = TCExtension.GetPropertyRealValue(selectedObject.getItem(), "vf3_module_name");

			dlg = new TechnicalDocProcessTrigger_Dialog(new Shell());
			dlg.create();
			dlg.txtDocType.setText(docTypeValue);
			dlg.txtProgram.setText(programValue);
			dlg.txtModule.setText(moduleValue);

			dlg.cbSubModule.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					if (!dlg.cbSubModule.getText().isEmpty())
						updateReviewerTable();
				}
			});

			dlg.btnValidate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					submitValidation();
				}
			});

			dlg.btnAccept.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					triggerProcess();
				}
			});

			dlg.cbShop.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					updateReviewerTable();
					dlg.tabFolder.setSelection(1);
				}
			});

			dlg.btnAccept.setEnabled(false);

			dlg.ckbRequiredManufacturing.setVisible(docTypeValue.compareTo("FMEA") == 0);
			dlg.cbShop.setVisible(docTypeValue.compareTo("FMEA") == 0);
			dlg.ckbRequiredManufacturing.setSelection(docTypeValue.compareTo("FMEA") == 0);
			if (docTypeValue.compareTo("FMEA") == 0) {
				shopMapping = new LinkedHashMap<String, Set<TCComponentGroupMember>>();
				String[] shopPreference = TCExtension.GetPreferenceValues("VINF_DFMEA_SHOP", session);
				for (String shop : shopPreference) {
					if (shop.contains(";")) {
						String[] splitStr = shop.split(";");
						if (splitStr.length > 2) {
							if (programValue.compareTo(splitStr[0]) == 0) {
								Set<TCComponentGroupMember> memberList = new HashSet<TCComponentGroupMember>();
								if (splitStr[2].contains(",")) {
									String[] splitStr2 = splitStr[2].split(",");
									for (String str : splitStr2) {
										TCComponentGroupMember member = TCExtension.GetGroupMemberByUserID(str, session);
										if (member != null)
											memberList.add(member);
									}
								} else {
									TCComponentGroupMember member = TCExtension.GetGroupMemberByUserID(splitStr[2], session);
									if (member != null)
										memberList.add(member);
								}
								shopMapping.put(splitStr[1], memberList);
							}
						}
					}
				}

				dlg.cbShop.setItems(shopMapping.keySet().toArray(new String[0]));
			}

			dlg.cbSubModule.setVisible(false);
			dlg.lblSubModule.setVisible(false);

			loadALByWorkflow();

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void submitValidation() {
		isValidate = true;

		validateObject();
		if (!isValidate) {
			dlg.btnAccept.setEnabled(false);
			dlg.setMessage("Data not valid. Please view Validate tab for detail.", true);
			dlg.tabFolder.setSelection(0);
			return;
		}

		updateReviewerTable();
		if (!isValidate) {
			dlg.btnAccept.setEnabled(false);
			dlg.setMessage("Reviewer not exits. Please view Review tab for detail.", true);
			dlg.tabFolder.setSelection(1);
			return;
		}

		if (dlg.txtProgram.getText().compareTo("DVPR") == 0) {
			updateVFTEReleaseTable();
			dlg.tabFolder.setSelection(2);
		} else {
			dlg.tabFolder.setSelection(1);
		}

		dlg.btnAccept.setEnabled(true);
		dlg.setMessage("Validate success. You can trigger process.", false);
	}

	private void validateObject() {
		try {
			StringBuffer validationResultText = new StringBuffer();
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Validating data...", IProgressMonitor.UNKNOWN);

					try {
						validationResultText.append("<html style=\"padding: 0px;\">");
						validationResultText.append("<style> table, tr, td {border: 1px solid;}table {width: 100%;border-collapse: collapse;table-layout: fixed;}td {height: 20px;word-wrap:break-word;}</style>");

						partsAndValidationResults = new LinkedHashMap<TCComponent, List<ValidationResult>>();
						validateDocumentRevision();
						validateVFTEReportFile();

						for (Entry<TCComponent, List<ValidationResult>> partsAndValidationResult : partsAndValidationResults.entrySet()) {
							validationResultText.append(ValidationResult.createHtmlValidation(partsAndValidationResult) + "</br>");
							for (ValidationResult validationItem : partsAndValidationResult.getValue()) {
								if (!validationItem.isPassed())
									isValidate = false;
							}
						}
						validationResultText.append("</body></html>");
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
			dlg.brwValidate.setText(validationResultText.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void validateVFTEReportFile() {
		Pattern pattern = Pattern.compile(StringExtension.fileNameAvailableCharacters);
		for (TCComponent vfteRevision : vfteRevisions) {
			List<ValidationResult> validateList = new LinkedList<ValidationResult>();
			try {
				LinkedList<String> fileNameNotValid = new LinkedList<String>();
				TCComponent[] files = vfteRevision.getRelatedComponents("IMAN_specification");
				if (files != null) {
					for (TCComponent pdfFile : files) {
						if (pdfFile instanceof TCComponentDataset && pdfFile.getPropertyDisplayableValue("object_type").compareTo("PDF") == 0) {
							String fileName = pdfFile.getPropertyDisplayableValue("object_name");
							Matcher matcher = pattern.matcher(fileName);
							if (!matcher.matches()) {
								fileNameNotValid.add(fileName);
							}
						}
					}
				}

				if (fileNameNotValid.size() > 0) {
					validateList.add(new ValidationResult("Test Report file name valid. <br>" + StringExtension.fileNameAvailableCharacters, false, String.join("<br>", fileNameNotValid)));
					isValidate = false;
				} else
					validateList.add(new ValidationResult("Test Report file name valid. <br>" + StringExtension.fileNameAvailableCharacters, true, ""));
			} catch (Exception e) {
				e.printStackTrace();
			}

			partsAndValidationResults.put(vfteRevision, validateList);
		}
	}

	private void validateDocumentRevision() throws Exception {
		List<ValidationResult> validateList = new LinkedList<ValidationResult>();

		TCComponentItemRevision itemRevision = (TCComponentItemRevision) selectedObject;
		TCComponentItem item = itemRevision.getItem();

		String objectType = selectedObject.getType();
		String docType = TCExtension.GetPropertyRealValue(item, "vf3_doc_type");

		if (objectType.compareTo("VF4_VFFS_docRevision") == 0) {
			validateList.add(new ValidationResult("Document Type", true, ""));
		} else {
			validateList.add(new ValidationResult("Document Type", item, "vf3_doc_type"));
		}
//		validateList.add(new ValidationResult("Document Type", item, "vf3_doc_type"));
		validateList.add(new ValidationResult("Model Code", item, "vf3_model_code"));
		validateList.add(new ValidationResult("Module Code", item, "vf3_module_name"));

		// -----------------------------------------------------------------------------------------
		boolean isInWorkflowTask = false;
		TCComponent[] workflowTask = itemRevision.getRelatedComponents("fnd0StartedWorkflowTasks");
		for (TCComponent tcComponent : workflowTask) {
			if (tcComponent.toString().compareToIgnoreCase(WORKPROCESS_NAME) == 0)
				isInWorkflowTask = true;
		}
		if (isInWorkflowTask)
			validateList.add(new ValidationResult("Document not in process", false, "Contact to PLM admin."));
		else
			validateList.add(new ValidationResult("Document not in process", true, ValidationResult.passImage));

		// -----------------------------------------------------------------------------------------
		boolean isWorkflowTaskApprovaled = false;
		TCComponent[] workflowTaskApprovaled = itemRevision.getRelatedComponents("fnd0AllWorkflows");
		for (TCComponent tcComponent : workflowTaskApprovaled) {
			if (tcComponent.toString().compareToIgnoreCase(WORKPROCESS_NAME) == 0) {
				if (tcComponent.getPropertyDisplayableValue("task_state").compareTo("Aborted") != 0)
					isWorkflowTaskApprovaled = true;
			}
		}
		if (isWorkflowTaskApprovaled)
			validateList.add(new ValidationResult("Document not yet approved", false, "Contact to PLM admin."));
		else
			validateList.add(new ValidationResult("Document not yet approved", true, ValidationResult.passImage));

		// -----------------------------------------------------------------------------------------
		if (docType.compareTo("ESOM") != 0) {
			Boolean checkFile = false;
			TCComponent[] fileComponent = itemRevision.getRelatedComponents("IMAN_specification");
			for (TCComponent tcComponent : fileComponent) {
				if (tcComponent.getType().compareToIgnoreCase("PDF") == 0 || tcComponent.getType().compareToIgnoreCase("MSExcelX") == 0 || tcComponent.getType().compareToIgnoreCase("MSExcel") == 0)
					checkFile = true;
			}
			if (!checkFile)
				validateList.add(new ValidationResult("PDF/Excel attachment", false, "Please attach PDF/Excel."));
			else
				validateList.add(new ValidationResult("PDF/Excel attachment", true, ValidationResult.passImage));
		}

		// -----------------------------------------------------------------------------------------
		if (docType.compareTo("VFDS") == 0) {
			Boolean checkPartRev = false;
			TCComponent[] objectChildComponents = selectedObject.getRelatedComponents("VF4_SpecItem");
			for (TCComponent tcComponent : objectChildComponents) {
				if (tcComponent.getType().compareToIgnoreCase("VF4_DesignRevision") == 0) {
					checkPartRev = true;
				}
			}
			if (!checkPartRev)
				validateList.add(new ValidationResult("Spec Item folder", false, "Please add part revision in Spec Item folder."));
			else
				validateList.add(new ValidationResult("Spec Item folder", true, ValidationResult.passImage));
		}

		// -----------------------------------------------------------------------------------------
		if (docType.compareTo("DVPR") == 0) {
			TCComponent[] vfteList = itemRevision.getRelatedComponents("VF4_TestReports");
			for (TCComponent tcComponent : vfteList) {
				if (tcComponent.getType().compareToIgnoreCase("VF3_AT_VFTE_Doc") == 0) {
					TCComponentItemRevision lastestRev = TCExtension.getLatestItemRevision((TCComponentItem) tcComponent);
					if (lastestRev != null) {
						String statusList = lastestRev.getPropertyDisplayableValue("release_status_list");
						boolean inWorkflow = false;
						TCComponent[] workflows = lastestRev.getRelatedComponents("fnd0AllWorkflows");
						for (TCComponent workflow : workflows) {
							if (workflow.toString().compareToIgnoreCase(WORKPROCESS_NAME) == 0) {
								if (workflow.getPropertyDisplayableValue("task_state").compareTo("Aborted") != 0)
									inWorkflow = true;
							}
						}
						if (statusList.isEmpty() && !inWorkflow) {
							vfteRevisions.add(lastestRev);
						}
					}
				}
			}

			if (vfteRevisions.size() == 0)
				validateList.add(new ValidationResult("VFTE Revision", false, "Not have VFTE Revision matching with DVPR Revision."));
			else
				validateList.add(new ValidationResult("VFTE Revision", true, ValidationResult.passImage));
		}

		partsAndValidationResults.put(selectedObject, validateList);
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;

		if (targetComponents.length > 1)
			return true;

		if (targetComponents[0] instanceof TCComponentItemRevision)
			selectedObject = (TCComponentItemRevision) targetComponents[0];

		if (selectedObject == null)
			return true;

		return false;
	}

	private void loadALByWorkflow() {
		try {
			if (docTypeValue.isEmpty() || programValue.isEmpty() || moduleValue.isEmpty())
				return;

			Set<String> subModuleDataForm = new HashSet<String>();
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			queryInput.put("Name", WORKPROCESS_NAME + "_*_" + programValue + "_" + moduleValue + "*");

			TCComponent[] alItems = TCExtension.queryItem(session, queryInput, "___Assignment List");
			if (alItems == null || alItems.length == 0)
				return;

			DataManagementService dmsService = DataManagementService.getService(session);
			dmsService.getProperties(alItems, new String[] { "object_string", "list_desc", "resources", "task_templates" });
			Set<TCComponent> subTaskList = TCExtension.getSubTaskList(WORKPROCESS_NAME, session);
			for (TCComponent alItem : alItems) {
				String alName = alItem.getProperty("object_string");
				String alNameProcess = alName.replace(WORKPROCESS_NAME + "_", "");
				String alDesc = alItem.getProperty("list_desc");

				String[] str = alNameProcess.split("_");
				if (str.length >= 3) {
					String docType = str[0];
					String program = str[1];
					String module = str[2];
					String subModule = "";

					if (docType.compareTo("Common") == 0) {
						if (!alDesc.isEmpty()) {
							String[] docTypeList = alDesc.split(";");
							for (String docTypeTemp : docTypeList) {
								if (docTypeTemp.compareTo(docTypeValue) == 0) {
									if (str.length > 3) {
										subModule = str[3];
										subModuleDataForm.add(subModule);
									}
									ALModel newALitem = new ALModel((TCComponentAssignmentList) alItem, subTaskList);
									alMapping.put(WORKPROCESS_NAME + "_" + docTypeTemp + "_" + program + "_" + module + (subModule.isEmpty() ? "" : "_" + subModule), newALitem);
								}
							}
						}
					} else {
						if (docType.compareTo(docTypeValue) == 0) {
							if (str.length > 3) {
								subModule = str[3];
								subModuleDataForm.add(subModule);
							}
							ALModel newALitem = new ALModel((TCComponentAssignmentList) alItem, subTaskList);
							alMapping.put(newALitem.getALName(), newALitem);
						}
					}
				}
			}

			if (subModuleDataForm.size() > 0) {
				dlg.cbSubModule.setItems(subModuleDataForm.toArray(new String[0]));
				dlg.cbSubModule.select(0);
				dlg.cbSubModule.setVisible(true);
				dlg.lblSubModule.setVisible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateReviewerTable() {
		String docType = dlg.txtDocType.getText();
		String program = dlg.txtProgram.getText();
		String module = dlg.txtModule.getText();
		String subModule = dlg.cbSubModule.getText();

		String tempName = WORKPROCESS_NAME + "_" + docType + "_" + program + "_" + module + (subModule.isEmpty() ? "" : "_" + subModule);
		assignmentListSelected = alMapping.getOrDefault(tempName, null);
		dlg.treeAssignList.removeAll();
		if (assignmentListSelected == null) {
			isValidate = false;
			return;
		}
		for (Map.Entry<TaskListModel, Set<TCComponentGroupMember>> gateMappingEntrySet : assignmentListSelected.getReviewGateList().entrySet()) {
			TreeItem task = new TreeItem(dlg.treeAssignList, SWT.NONE);
			String subTaskType = gateMappingEntrySet.getKey().getTaskType();
			if (subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_REVIEW) == 0)
				task.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/EPMReviewTask.png"));
			else if (subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_ACKNOWLEDGE) == 0)
				task.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/EPMAcknowledgeTask.png"));
			else if (subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_DO) == 0)
				task.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/EPMDoTask.png"));
			else if (subTaskType.compareTo(ALAssistant_Constant.TASKTEMPLATE_CONDITION) == 0)
				task.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/EPMConditionTask.png"));

			task.setText(gateMappingEntrySet.getKey().getName());
			if (gateMappingEntrySet.getKey().getName().compareTo(MANUFACTURING_REVIEW_TASK) == 0) {
				if (dlg.ckbRequiredManufacturing.getSelection()) {
					String shop = dlg.cbShop.getText();
					if (shopMapping.containsKey(shop)) {
						if (shopMapping.get(shop).size() > 0) {
							for (TCComponentGroupMember reviewer : shopMapping.get(shop)) {
								TreeItem performer = new TreeItem(task, SWT.NONE);
								performer.setData(reviewer);
								performer.setText(reviewer.toString());
								performer.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/user_16.png"));
								int action = -1;// ALAssistant_Extension.getActionOfMember(resourcesTask, reviewer);
								if (action == ALAssistant_Constant.AL_ACTION_REVIEW_REQUIRED || action == ALAssistant_Constant.AL_ACTION_ACKNOW_REQUIRED)
									task.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
							}
						}
					}
				}
			} else {
				if (gateMappingEntrySet.getValue().size() > 0) {
					for (TCComponentGroupMember reviewer : gateMappingEntrySet.getValue()) {
						TreeItem performer = new TreeItem(task, SWT.NONE);
						performer.setData(reviewer);
						performer.setText(reviewer.toString());
						performer.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/user_16.png"));
						int action = -1;// ALAssistant_Extension.getActionOfMember(resourcesTask, reviewer);
						if (action == ALAssistant_Constant.AL_ACTION_REVIEW_REQUIRED || action == ALAssistant_Constant.AL_ACTION_ACKNOW_REQUIRED)
							task.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
					}
				}
			}
		}

		for (final TreeItem item : dlg.treeAssignList.getItems()) {
			item.setExpanded(true);
		}
	}

	private void updateVFTEReleaseTable() {
		for (TCComponent vfteRevision : vfteRevisions) {
			TableItem tableItem = new TableItem(dlg.tblVFTE, SWT.NONE);
			tableItem.setText(new String[] { vfteRevision.toString() });
		}
	}

	private void triggerProcess() {
		try {
			LinkedHashMap<String, Set<TCComponentGroupMember>> assignmentList = null;
			if (dlg.ckbRequiredManufacturing.getSelection()) {
				String shop = dlg.cbShop.getText();
				if (shop.isEmpty()) {
					dlg.setMessage("Please select manufacturing shop.", true);
					return;
				}

				if (shopMapping.containsKey(shop)) {
					if (shopMapping.get(shop).size() > 0) {
						assignmentList = new LinkedHashMap<String, Set<TCComponentGroupMember>>();
						assignmentList.put(MANUFACTURING_REVIEW_TASK, shopMapping.get(shop));
					}
				}

				selectedObject.setProperty("vl5_weight", "1");
			}

			String program = dlg.txtProgram.getText();
			String module = dlg.txtModule.getText();
			String alName = assignmentListSelected.getALName();
			LinkedList<String> attachments = new LinkedList<String>();
			attachments.add(selectedObject.getUid());

			if (vfteRevisions.size() > 0) {
				for (TCComponentItemRevision vfteRevision : vfteRevisions) {
					attachments.add(vfteRevision.getUid());
				}
			}

			TriggerProcess trigger = new TriggerProcess(session);
			trigger.setWorkProcessTemplace(WORKPROCESS_NAME);
			trigger.setWorkProcessName(program + " - " + module + " - " + selectedObject.toString());
			trigger.setAlName(alName);
			if (assignmentList != null)
				trigger.setAssignmentList(assignmentList);
			trigger.setWorkProcessAttachment(attachments.toArray(new String[0]));

			String mess = trigger.run();
			if (mess.isEmpty()) {
				dlg.setMessage("Trigger process successfully.", false);
				dlg.btnAccept.setEnabled(false);
				dlg.btnValidate.setEnabled(false);
			} else {
				dlg.setMessage(mess, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			dlg.setMessage("Trigger process unsuccessfully. Exception: " + e.getMessage(), true);
		}
	}
}
