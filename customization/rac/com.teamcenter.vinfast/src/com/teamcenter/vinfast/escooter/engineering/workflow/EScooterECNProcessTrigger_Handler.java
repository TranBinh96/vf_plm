package com.teamcenter.vinfast.escooter.engineering.workflow;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.admin.pal.ALAssistant_Constant;
import com.teamcenter.vinfast.escooter.sorprocess.model.TaskListModel;
import com.teamcenter.vinfast.model.ALModel;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

public class EScooterECNProcessTrigger_Handler extends AbstractHandler {
	private TCSession session;
	private EScooterECNProcessTrigger_Dialog dlg;
	private TCComponent selectedObject = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ALModel>>> workprocessMapping;
	private ALModel assignmentListSelected = null;
	private String BIG_COST_IMPACT = "Big cost impact";
	private String triggerMessage = "";

	public EScooterECNProcessTrigger_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
				MessageBox.post("You don't have permission access to this ECN.", "Warning", MessageBox.WARNING);
				return null;
			}

			// Init data
			String[] workprocessDataForm = new String[] { "EScooter-ECN-AfterPR-Combined", "EScooter-ECN-AfterSOB-Combined", "EScooter-ECN-BeforeSOB-Combined", "Emotor_ES_ECN_After_PR_Process" };
			String[] costImpactDataForm = new String[] { BIG_COST_IMPACT, "Non/Low cost impact" };
			String[] combineDataForm = new String[] { "", "I Release", "PR Release" };
			workprocessMapping = new LinkedHashMap<>();
			// Init UI
			dlg = new EScooterECNProcessTrigger_Dialog(new Shell());
			dlg.create();

			dlg.txtID.setText(selectedObject.toString());
			dlg.cbWorkflow.setItems(workprocessDataForm);

			dlg.cbWorkflow.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.cbProgram.removeAll();
					String workprocess = dlg.cbWorkflow.getText();
					loadALByWorkflow(workprocess);
					if (workprocessMapping.containsKey(workprocess)) {
						String[] programDataForm = workprocessMapping.get(workprocess).keySet().toArray(new String[0]);
						Arrays.sort(programDataForm);
						dlg.cbProgram.setItems(programDataForm);
					}

					dlg.cbCombine.deselectAll();
					dlg.lblCombine.setVisible(workprocess.contains("AfterSOB") || workprocess.contains("BeforeSOB"));
					dlg.cbCombine.setVisible(workprocess.contains("AfterSOB") || workprocess.contains("BeforeSOB"));
				}
			});

			dlg.cbProgram.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.cbModule.removeAll();
					String workprocess = dlg.cbWorkflow.getText();
					if (workprocessMapping.containsKey(workprocess)) {
						LinkedHashMap<String, LinkedHashMap<String, ALModel>> programMapping = workprocessMapping.get(workprocess);
						String program = dlg.cbProgram.getText();
						if (programMapping.containsKey(program)) {
							String[] moduleDataForm = programMapping.get(program).keySet().toArray(new String[0]);
							Arrays.sort(moduleDataForm);
							dlg.cbModule.setItems(moduleDataForm);
						}
					}
				}
			});

			dlg.cbModule.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					String workflow = dlg.cbWorkflow.getText();
					String program = dlg.cbProgram.getText();
					String module = dlg.cbModule.getText();
					assignmentListSelected = workprocessMapping.get(workflow).get(program).getOrDefault(module, null);

					dlg.treeAssignList.removeAll();
					if (assignmentListSelected != null) {
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

							String taskName = gateMappingEntrySet.getKey().getName();
							task.setText(taskName);
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
					}
				}
			});

			dlg.cbCostImpact.setItems(costImpactDataForm);

			dlg.cbCombine.setItems(combineDataForm);

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					submitWorkflow();
				}
			});
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void loadALByWorkflow(String workprocessName) {
		if (workprocessName.isEmpty() || workprocessMapping.containsKey(workprocessName))
			return;

		try {
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Program loading...", IProgressMonitor.UNKNOWN);
					try {
						workprocessMapping.put(workprocessName, TCExtension.getScooterProgramMapping(workprocessName, null, true, session));
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
	}

	private boolean submitValidation() {
		if (dlg.cbProgram.getText().isEmpty())
			return false;
		if (dlg.cbWorkflow.getText().isEmpty())
			return false;
		if (dlg.cbModule.getText().isEmpty())
			return false;
		if (dlg.cbCostImpact.getText().isEmpty())
			return false;
		return true;
	}

	private void submitWorkflow() {
		if (!submitValidation()) {
			dlg.setMessage("Please input all required information.", true);
			return;
		}

		try {
			triggerMessage = "";
			String processTemplate = dlg.cbWorkflow.getText();
			String program = dlg.cbProgram.getText();
			String module = dlg.cbModule.getText();
			String processName = program + " - " + module + " - " + selectedObject.toString();
			String combineStatus = dlg.cbCombine.getText();
			String bigCost = dlg.cbCostImpact.getText();
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Trigger processing...", IProgressMonitor.UNKNOWN);

					try {
						try {
							selectedObject.setProperty("vf4_combine_release_status", combineStatus);
						} catch (Exception e) {
							e.printStackTrace();
						}
						String processDesc = combineStatus.isEmpty() ? "" : "CHANGE APPROVAL COMBINED WITH " + combineStatus.toUpperCase();
						markTarget("_");

						try {
							selectedObject.setLogicalProperty("vf4_is_big_cost_impact", bigCost.compareToIgnoreCase(BIG_COST_IMPACT) == 0);
						} catch (Exception e) {
							e.printStackTrace();
						}

						monitor.subTask("Trigger ECR process...");
						TriggerProcess trigger = new TriggerProcess(session);
						trigger.setWorkProcessTemplace(processTemplate);
						trigger.setWorkProcessName(processName);
						trigger.setWorkProcessDesc(processDesc);
						if (assignmentListSelected != null) {
							trigger.setAlName(assignmentListSelected.getALName());
							trigger.setAssignmentList(assignmentListSelected.getAddingReviewGateList());
						}
						trigger.setWorkProcessAttachment(new String[] { selectedObject.getUid() });
						triggerMessage = trigger.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});

			if (!triggerMessage.isEmpty())
				throw new Exception(triggerMessage);

			dlg.setMessage("Process trigger success.", false);
			dlg.btnAccept.setEnabled(false);
		} catch (Exception e) {
			dlg.setMessage(e.getMessage(), true);
		}
	}

	private void markTarget(String markerValue) {
		try {
			AIFComponentContext[] forms = selectedObject.getRelated("IMAN_master_form_rev");
			if (forms != null && forms.length > 0) {
				TCComponent form = (TCComponent) forms[0].getComponent();
				form.setProperty("object_desc", markerValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
