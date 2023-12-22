package com.teamcenter.vinfast.aftersale.workflow;

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

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.admin.pal.ALAssistant_Constant;
import com.teamcenter.vinfast.escooter.sorprocess.model.TaskListModel;
import com.teamcenter.vinfast.model.ALModel;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

public class SCRProcessTrigger_Handler extends AbstractHandler {
	private TCSession session;
	private SCRProcessTrigger_Dialog dlg;
	private TCComponent selectedObject = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private LinkedHashMap<String, ALModel> programMapping = null;
	private final static String WORKPROCESS_TEMPLACE = "VINFAST_SCR_RELEASE_PROCESS";
	private String triggerMessage = "";

	public SCRProcessTrigger_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			programMapping = new LinkedHashMap<String, ALModel>();
			TCComponent[] alList = TCExtension.getALByWorkflow(WORKPROCESS_TEMPLACE, session);
			Set<TCComponent> subTaskList = TCExtension.getSubTaskList(WORKPROCESS_TEMPLACE, session);
			if (alList != null) {
				for (TCComponent alItem : alList) {
					try {
						String alName = alItem.getProperty("list_name");
						if (alName.contains(WORKPROCESS_TEMPLACE + "_")) {
							String program = alName.replace(WORKPROCESS_TEMPLACE + "_", "");
							programMapping.put(program, new ALModel((TCComponentAssignmentList) alItem, subTaskList));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			dlg = new SCRProcessTrigger_Dialog(new Shell());
			dlg.create();

			dlg.txtWorkflow.setText(WORKPROCESS_TEMPLACE);
			dlg.txtID.setText(selectedObject.getPropertyDisplayableValue("object_string"));

			dlg.cbProgram.setItems(programMapping.keySet().toArray(new String[0]));
			dlg.cbProgram.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					String program = dlg.cbProgram.getText();
					dlg.treeAssignList.removeAll();
					if (programMapping.containsKey(program)) {
						for (Map.Entry<TaskListModel, Set<TCComponentGroupMember>> gateMappingEntrySet : programMapping.get(program).getReviewGateList().entrySet()) {
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
					}
				}
			});

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					submitWorkflow();
				}
			});

			fillDefaultData();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void fillDefaultData() throws Exception {
		String program = selectedObject.getPropertyDisplayableValue("vf4_veh_program");
		if (Arrays.asList(dlg.cbProgram.getItems()).contains(program)) {
			dlg.cbProgram.setText(program);
		}
	}

	private void submitWorkflow() {
		if (dlg.cbProgram.getText().isEmpty()) {
			dlg.setMessage("Please input all required information", true);
			return;
		}
		try {
			triggerMessage = "";
			String program = dlg.cbProgram.getText();
			String processTemplate = dlg.txtWorkflow.getText();
			String alName = programMapping.get(program).getALName();
			String processName = program + " - " + selectedObject.toString();
			String processDesc = "";
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Trigger processing...", IProgressMonitor.UNKNOWN);

					try {
						monitor.subTask("Trigger SCR process...");
						TriggerProcess trigger = new TriggerProcess(session);
						trigger.setWorkProcessTemplace(processTemplate);
						trigger.setWorkProcessName(processName);
						trigger.setWorkProcessDesc(processDesc);
						trigger.setAlName(alName);
						trigger.setWorkProcessAttachment(new String[] { selectedObject.getUid() });
						triggerMessage = trigger.run();

						if (!triggerMessage.isEmpty()) {
							throw new Exception(triggerMessage);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});

			if (triggerMessage.isEmpty()) {
				selectedObject.refresh();
				dlg.setMessage("Process trigger successfully.", false);
			} else {
				dlg.setMessage(triggerMessage, true);
			}
		} catch (Exception e) {
			dlg.setMessage("Exception: " + e.getMessage(), true);
			e.printStackTrace();
		}
	}
}
