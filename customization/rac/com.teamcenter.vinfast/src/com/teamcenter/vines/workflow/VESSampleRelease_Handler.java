package com.teamcenter.vines.workflow;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

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
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

public class VESSampleRelease_Handler extends AbstractHandler {
	private TCSession session;
	private VESSampleRelease_Dialog dlg;
	private TCComponent selectedObject = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private String triggerMessage = "";
	private String WORKFLOW_NAME = "VINES_SAMPLE_RELEASE_PROCESS";

	public VESSampleRelease_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (!validObjectSelect(targetComp)) {
				MessageBox.post("Please select one snapshoot.", "Warning", MessageBox.WARNING);
				return null;
			}

			if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
				MessageBox.post("You don't have write access to update information: " + selectedObject.getPropertyDisplayableValue("item_id"), "Warning", MessageBox.WARNING);
				return null;
			}

			String[] releaseSampleDataForm = new String[] { "Sample A", "Sample B", "Sample C" };
			Set<String> alDataForm = new HashSet<>();
			TCComponent[] alList = TCExtension.getALByWorkflow(WORKFLOW_NAME, session);
			for (TCComponent alItem : alList) {
				if (alItem instanceof TCComponentAssignmentList)
					alDataForm.add(((TCComponentAssignmentList) alItem).getName());
			}

			dlg = new VESSampleRelease_Dialog(new Shell());
			dlg.create();

			dlg.txtID.setText(selectedObject.getPropertyDisplayableValue("object_string"));
			dlg.txtWorkflow.setText(WORKFLOW_NAME);
			dlg.cbReleaseSample.setItems(releaseSampleDataForm);
			dlg.cbAL.setItems(alDataForm.toArray(new String[0]));

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

	private void submitWorkflow() {
		try {
			triggerMessage = "";
			String sampleRelease = dlg.cbReleaseSample.getText();
			String processTemplate = dlg.txtWorkflow.getText();
			String releaseSample = dlg.cbReleaseSample.getText();
			String processName = processTemplate + " - " + releaseSample + " - " + selectedObject.toString();
			String alName = dlg.cbAL.getText();
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Trigger processing...", IProgressMonitor.UNKNOWN);

					try {
						selectedObject.setProperty("vf4_sample_type", sampleRelease);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						String processDesc = "";
						monitor.subTask("Trigger ECR process...");
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
			return false;
		if (targetComponents.length > 1)
			return false;

		selectedObject = (TCComponent) targetComponents[0];
		if (selectedObject.getType().compareToIgnoreCase("Snapshot") != 0)
			return false;

		return true;
	}
}