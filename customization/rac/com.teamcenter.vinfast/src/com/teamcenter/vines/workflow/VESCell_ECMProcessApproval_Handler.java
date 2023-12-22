package com.teamcenter.vines.workflow;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

public class VESCell_ECMProcessApproval_Handler extends AbstractHandler {
	private TCSession session;
	private VESCell_ECMProcessApproval_Dialog dlg;
	private TCComponent selectedObject = null;

	private static String PREFERENCE_NAME = "VES_ECM_APPROVAL";
	private static String WORKPROCESS_TEMPLACE = "VES_ECM_Process";
	private static String OBJECT_TYPE = "VF4_VES_ECMRevision";

	private LinkedHashMap<String, Set<String>> conditionList;
	private LinkedHashMap<String, String> al;

	public VESCell_ECMProcessApproval_Handler() {
		super();
	}

	@SuppressWarnings("null")
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();

		if (!validObjectSelect(targetComp)) {
			MessageBox.post("Please select 1 ECM Revision.", "Error", MessageBox.ERROR);
			return null;
		}

		try {
			// Init data
			selectedObject = (TCComponent) targetComp[0];
			conditionList = new LinkedHashMap<String, Set<String>>();
			al = new LinkedHashMap<String, String>();

			String[] programsTypesWorkflows = TCExtension.GetPreferenceValues(PREFERENCE_NAME, session);
			if (programsTypesWorkflows != null || programsTypesWorkflows.length > 0) {
				for (String programsTypeWorkflow : programsTypesWorkflows) {
					if (programsTypeWorkflow.contains(";")) {
						String[] str = programsTypeWorkflow.split(";");
						if (str.length > 1) {
							String program = str[0];
							String module = "";
							if (str[1].contains("=")) {
								String[] str2 = str[1].split("=");
								module = str2[0];
							} else {
								module = str[1];
							}

							if (conditionList.containsKey(program)) {
								conditionList.get(program).add(module);
							} else {
								Set<String> newModule = new HashSet<String>();
								newModule.add(module);
								conditionList.put(program, newModule);
							}
						}
					}
					if (programsTypeWorkflow.contains("=")) {
						String[] str = programsTypeWorkflow.split("=");
						if (str.length > 1) {
							al.put(str[0], str[1]);
						}
					}
				}
			}

			String errorMes = validateItem(selectedObject);
			// Init UI
			dlg = new VESCell_ECMProcessApproval_Dialog(new Shell());
			dlg.create();

			if (!errorMes.isEmpty()) {
				dlg.setMessage(errorMes, true);
				for (Control ctrl : dlg.getShell().getChildren()) {
					ctrl.setEnabled(false);
				}
			} else {
				dlg.setMessage("Trigger ECM Process", false);
			}

			dlg.txtID.setText(selectedObject.getPropertyDisplayableValue("revision_list"));

			dlg.cbProgram.setItems(conditionList.keySet().toArray(new String[0]));
			dlg.cbProgram.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					try {
						dlg.cbModule.removeAll();
						dlg.cbModule.setItems(conditionList.get(dlg.cbProgram.getText()).toArray(new String[0]));
					} catch (Exception e) {
					}
				}
			});

			dlg.cbModule.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.btnCreate.setEnabled(submitValidation());
				}
			});

			dlg.cbCombine.setItems(new String[] { "", "I Release", "P Release", "PR Release" });

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					submitWorkflow();
				}
			});
			dlg.btnCreate.setEnabled(submitValidation());
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String validateItem(TCComponent itemRevision) {
		try {
			// check in process
			TCComponent[] workflowTask = itemRevision.getRelatedComponents("fnd0StartedWorkflowTasks");
			for (TCComponent tcComponent : workflowTask) {
				if (tcComponent.toString().compareToIgnoreCase(WORKPROCESS_TEMPLACE) == 0) {
					return "The part revision is already triggered.";
				}
			}

			// check approvaled
			TCComponent[] workflowTaskApprovaled = itemRevision.getRelatedComponents("fnd0AllWorkflows");
			for (TCComponent tcComponent : workflowTaskApprovaled) {
				if (tcComponent.toString().compareToIgnoreCase(WORKPROCESS_TEMPLACE) == 0) {
					return "The part revision is already approved.";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null || targetComponents.length != 1)
			return false;

		if (targetComponents[0] instanceof TCComponentItemRevision) {
			TCComponentItemRevision objectTarget = (TCComponentItemRevision) targetComponents[0];
			if (objectTarget.getType().compareToIgnoreCase(OBJECT_TYPE) != 0)
				return false;
		}

		return true;
	}

	private boolean submitValidation() {
		if (dlg.cbProgram.getText().isEmpty())
			return false;
		if (dlg.cbModule.getText().isEmpty())
			return false;
		return true;
	}

	private void submitWorkflow() {
		String program = dlg.cbProgram.getText();
		String module = dlg.cbModule.getText();
		String alName = al.get(program + ";" + module);
		if (alName == null || alName.isEmpty()) {
			dlg.setMessage("Not have assingment list. Please contact with administrator.", true);
			return;
		}

		LinkedList<String> attachments = new LinkedList<String>();
		attachments.add(selectedObject.getUid());

		String processDesc = checkCombineWith();

		TriggerProcess trigger = new TriggerProcess(session);
		trigger.setWorkProcessTemplace(WORKPROCESS_TEMPLACE);
		trigger.setWorkProcessName(program + " - " + module + " - " + selectedObject.toString());
		trigger.setWorkProcessDesc(processDesc);
		trigger.setAlName(alName);
		trigger.setWorkProcessAttachment(attachments.toArray(new String[0]));

		String mess = trigger.run();
		if (mess.isEmpty()) {
			MessageBox.post("Process initiated.", "Success", MessageBox.INFORMATION);
			dlg.close();
		} else {
			dlg.setMessage(mess, true);
		}
	}

	private String checkCombineWith() {
		String combine = dlg.cbCombine.getText();
		try {
			TCComponentItemRevision target = (TCComponentItemRevision) selectedObject;
			target.setProperty("vf4_combine_release_status", combine);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		if (combine.isEmpty())
			return "";
		return "CHANGE APPROVAL COMBINED WITH " + combine.toUpperCase();
	}
}
