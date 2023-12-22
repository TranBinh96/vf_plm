package com.teamcenter.vinfast.car.manuf.workflow;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

public class MCNProcess_Handler extends AbstractHandler {
	private TCSession session;
	private MCNProcess_Dialog dlg;
	private TCComponentItemRevision selectedObject = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private String WORKPROCESS_TEMPLACE = "MCN_APPROVAL_TRANSFER_SAP";

	private String triggerMessage = "";

	public MCNProcess_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (validObjectSelect(targetComp)) {
				MessageBox.post("Please select one MCN Revision.", "Error", MessageBox.ERROR);
				return null;
			}

			// Init data
			LinkedHashMap<String, String> moduleDataForm = new LinkedHashMap<>();
			String changeRequestType = selectedObject.getPropertyDisplayableValue("vf6CP_change_request_type");

			TCComponent[] alList = TCExtension.getALByWorkflow(WORKPROCESS_TEMPLACE, session);
			if (alList != null) {
				for (TCComponent alItem : alList) {
					if (alItem instanceof TCComponentAssignmentList) {
						String name = ((TCComponentAssignmentList) alItem).getName();
						String desc = ((TCComponentAssignmentList) alItem).getDescription();

						if (!changeRequestType.isEmpty()) {
							if (changeRequestType.compareTo("ECR/ECN") == 0) {
								if (desc.compareTo("NO ECR") == 0)
									continue;
							} else {
								if (desc.compareTo("NO ECR") != 0)
									continue;
							}
						}

						moduleDataForm.put(name, desc);
					}
				}
			}

			// Init UI
			dlg = new MCNProcess_Dialog(new Shell());
			dlg.create();

			dlg.txtWorkflow.setText(WORKPROCESS_TEMPLACE);
			StringExtension.UpdateValueTextCombobox(dlg.cbModule, moduleDataForm);

			String validateMessage = validAfterStartDialog();
			if (!validateMessage.isEmpty()) {
				dlg.btnAccept.setEnabled(false);
				dlg.setMessage(validateMessage, true);
			}

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

	private String validAfterStartDialog() {
		try {
			// check in process
			TCComponent[] workflowTask = selectedObject.getRelatedComponents("fnd0StartedWorkflowTasks");
			for (TCComponent tcComponent : workflowTask) {
				if (tcComponent.toString().compareToIgnoreCase(WORKPROCESS_TEMPLACE) == 0) {
					return "MCN Revision is already triggered to " + WORKPROCESS_TEMPLACE + ".";
				}
			}
			// check approvaled
			TCComponent[] workflowTaskApprovaled = selectedObject.getRelatedComponents("fnd0AllWorkflows");
			for (TCComponent tcComponent : workflowTaskApprovaled) {
				if (tcComponent.toString().compareToIgnoreCase(WORKPROCESS_TEMPLACE) == 0) {
					return "The selected MCN Revision is already approved.";
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return "";
	}

	private boolean checkRequired() {
		if (dlg.cbModule.getText().isEmpty())
			return false;

		return true;
	}

	private void submitWorkflow() {
		if (!checkRequired()) {
			dlg.setMessage("Please input all required information", false);
			return;
		}

		try {
			triggerMessage = "";
			String module = dlg.cbModule.getText();
			String processTemplate = dlg.txtWorkflow.getText();
			String processName = processTemplate + " - " + module + " - " + selectedObject.toString();
			String processDesc = processTemplate + " - " + module + " - " + selectedObject.toString();
			String alName = (String) dlg.cbModule.getData(module);

			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Trigger processing...", IProgressMonitor.UNKNOWN);

					try {
						monitor.subTask("Trigger process...");
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
				dlg.close();
				MessageBox.post("Process initiated.", "Success", MessageBox.INFORMATION);
			} else {
				dlg.setMessage(triggerMessage, true);
			}
		} catch (Exception e) {
			dlg.setMessage(e.toString(), true);
			e.printStackTrace();
		}
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;
		if (targetComponents.length > 1)
			return true;

		if (targetComponents[0] instanceof TCComponentItemRevision) {
			selectedObject = (TCComponentItemRevision) targetComponents[0];
		}

		if (selectedObject != null) {
			if (selectedObject.getType().compareToIgnoreCase("Vf6_MCNRevision") != 0)
				return true;
		}

		return false;
	}
}
