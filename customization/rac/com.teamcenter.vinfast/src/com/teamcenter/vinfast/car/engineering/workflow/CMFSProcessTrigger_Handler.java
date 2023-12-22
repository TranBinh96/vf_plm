package com.teamcenter.vinfast.car.engineering.workflow;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.admin.pal.ALAssistant_Constant;
import com.teamcenter.vinfast.escooter.sorprocess.model.TaskListModel;
import com.teamcenter.vinfast.model.ALModel;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

public class CMFSProcessTrigger_Handler extends AbstractHandler {
	private TCSession session;
	private CMFSProcessTrigger_Dialog dlg;
	private TCComponentItemRevision selectedObject = null;
	private static String WORKPROCESS_NAME = "VF_CMFS_Release_Process";
	private static String OBJECT_TYPE = "VF3_CMFS_docRevision";
	private LinkedHashMap<TCComponent, List<ValidationResult>> partsAndValidationResults = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private boolean isValidate = true;
	private LinkedHashMap<String, ALModel> alMapping = new LinkedHashMap<String, ALModel>();
	private ALModel assignmentListSelected = null;

	public CMFSProcessTrigger_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

		if (validObjectSelect(targetComponents)) {
			MessageBox.post("Please Select One CMF Document Revision.", "Error", MessageBox.ERROR);
			return null;
		}

		try {
			dlg = new CMFSProcessTrigger_Dialog(new Shell());
			dlg.create();
			dlg.txtProgram.setText(TCExtension.GetPropertyRealValue(selectedObject.getItem(), "vf3_model_code"));
			dlg.txtModule.setText(TCExtension.GetPropertyRealValue(selectedObject.getItem(), "vf3_module_name"));

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					submitToWorkflow();
				}
			});
			submitValidation();
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
			dlg.btnCreate.setEnabled(false);
			dlg.setMessage("Data not valid. Please view Validate tab for detail.", true);
			dlg.tabFolder.setSelection(0);
			return;
		}

		updateReviewerTable();
		if (!isValidate) {
			dlg.btnCreate.setEnabled(false);
			dlg.setMessage("Reviewer not exits. Please view Review tab for detail.", true);
			dlg.tabFolder.setSelection(1);
			return;
		}

		dlg.btnCreate.setEnabled(true);
		dlg.tabFolder.setSelection(1);
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

						for (Entry<TCComponent, List<ValidationResult>> partsAndValidationResult : partsAndValidationResults.entrySet()) {
							validationResultText.append(ValidationResult.createHtmlValidation(partsAndValidationResult) + "</br>");
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

	private void validateDocumentRevision() throws Exception {
		List<ValidationResult> validateList = new LinkedList<ValidationResult>();
		TCComponentItemRevision itemRevision = (TCComponentItemRevision) selectedObject;
		TCComponentItem item = itemRevision.getItem();

		validateList.add(new ValidationResult("Model Code", item, "vf3_model_code"));
		validateList.add(new ValidationResult("Module Name", item, "vf3_module_name"));

		boolean isInWorkflowTask = false;
		TCComponent[] workflowTask = itemRevision.getRelatedComponents("fnd0StartedWorkflowTasks");
		for (TCComponent tcComponent : workflowTask) {
			if (tcComponent.toString().compareToIgnoreCase(WORKPROCESS_NAME) == 0) {
				isInWorkflowTask = true;
			}
		}
		if (isInWorkflowTask) {
			validateList.add(new ValidationResult("Document not in process", false, "Contact to PLM admin."));
			isValidate = false;
		} else
			validateList.add(new ValidationResult("Document not in process", true, ValidationResult.passImage));

		boolean isWorkflowTaskApprovaled = false;
		TCComponent[] workflowTaskApprovaled = itemRevision.getRelatedComponents("fnd0AllWorkflows");
		for (TCComponent tcComponent : workflowTaskApprovaled) {
			if (tcComponent.toString().compareToIgnoreCase(WORKPROCESS_NAME) == 0) {
				if (tcComponent.getPropertyDisplayableValue("task_state").compareTo("Aborted") != 0)
					isWorkflowTaskApprovaled = true;
			}
		}
		if (isWorkflowTaskApprovaled) {
			validateList.add(new ValidationResult("Document not yet approved", false, "Contact to PLM admin."));
			isValidate = false;
		} else
			validateList.add(new ValidationResult("Document not yet approved", true, ValidationResult.passImage));

		Boolean checkFile = false;
		TCComponent[] fileComponent = itemRevision.getRelatedComponents("IMAN_specification");
		for (TCComponent tcComponent : fileComponent) {
			if (tcComponent.getType().compareToIgnoreCase("PDF") == 0)
				checkFile = true;
		}
		if (!checkFile) {
			validateList.add(new ValidationResult("PDF attachment", false, "Please attach PDF file."));
			isValidate = false;
		} else
			validateList.add(new ValidationResult("PDF attachment", true, ValidationResult.passImage));

		partsAndValidationResults.put(selectedObject, validateList);
	}

	private void updateReviewerTable() {
		dlg.treeAssignList.removeAll();
		try {
			TCComponent[] alItems = TCExtension.getALByWorkflow(WORKPROCESS_NAME, session);
			if (alItems != null) {
				Set<TCComponent> subTaskList = TCExtension.getSubTaskList(WORKPROCESS_NAME, session);
				for (TCComponent alItem : alItems) {
					String alName = alItem.getProperty("object_string");
					String alNameProcess = alName.replace(WORKPROCESS_NAME + "_", "");
					String alDesc = alItem.getProperty("list_desc");
					String[] str = alNameProcess.split("_");
					if (str.length >= 2) {
						String program = str[0];
						String module = str[1];

						if (module.compareTo("Common") == 0) {
							if (!alDesc.isEmpty()) {
								String[] moduleList = alDesc.split(";");
								for (String moduleTemp : moduleList) {
									ALModel newALitem = new ALModel((TCComponentAssignmentList) alItem, subTaskList);
									alMapping.put(WORKPROCESS_NAME + "_" + program + "_" + moduleTemp, newALitem);
								}
							}
						} else {
							ALModel newALitem = new ALModel((TCComponentAssignmentList) alItem, subTaskList);
							alMapping.put(alName, newALitem);
						}
					}
				}

				if (alItems.length > 0) {
					String alNameTemp = WORKPROCESS_NAME + "_" + dlg.txtProgram.getText() + "_" + dlg.txtModule.getText();
					if (alMapping.containsKey(alNameTemp)) {
						assignmentListSelected = alMapping.get(alNameTemp);
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

						for (final TreeItem item : dlg.treeAssignList.getItems()) {
							item.setExpanded(true);
						}
					} else {
						isValidate = false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;
		if (targetComponents.length > 1)
			return true;
		selectedObject = (TCComponentItemRevision) targetComponents[0];
		if (selectedObject.getType().compareToIgnoreCase(OBJECT_TYPE) != 0)
			return true;
		return false;
	}

	private void submitToWorkflow() {
		String program = dlg.txtProgram.getText();
		String module = dlg.txtModule.getText();
		LinkedList<String> attachments = new LinkedList<String>();
		attachments.add(selectedObject.getUid());

		TriggerProcess trigger = new TriggerProcess(session);
		trigger.setWorkProcessTemplace(WORKPROCESS_NAME);
		trigger.setWorkProcessName(program + " - " + module + " - " + selectedObject.toString());
		trigger.setAlName(assignmentListSelected.getALName());
		trigger.setWorkProcessAttachment(attachments.toArray(new String[0]));

		String mess = trigger.run();
		if (mess.isEmpty()) {
			dlg.setMessage("Trigger Process success.", false);
			dlg.btnCreate.setEnabled(false);
		} else {
			dlg.setMessage(mess, true);
		}
	}
}
