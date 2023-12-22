package com.teamcenter.vinfast.eecomponent.workflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

public class EEComponent_ECNApproval_Handler extends AbstractHandler {
	private TCSession session;
	private EEComponent_ECNApproval_Dialog dlg;
	private TCComponent selectedObject = null;
	private String PROGRAMTYPE_PREF = "VINF_EE_ECN_PROCESS_APPROVAL";
	private String BIG_COST_IMPACT = "Big cost impact";

	private LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> categoryMapping = null;

	public EEComponent_ECNApproval_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();

			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			// Init data
			categoryMapping = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>();
			String[] programsTypesWorkflows = TCExtension.GetPreferenceValues(PROGRAMTYPE_PREF, session);
			if (programsTypesWorkflows != null && programsTypesWorkflows.length > 0) {
				for (String programsTypeWorkflow : programsTypesWorkflows) {
					String[] str = programsTypeWorkflow.split("=");
					if (str.length >= 4) {
						String vehicleCategory = str[0];
						String module = str[1];
						String workflow = str[2];
						String al = str[3];

						LinkedHashMap<String, LinkedHashMap<String, String>> moduleMapping = null;
						if (categoryMapping.containsKey(vehicleCategory))
							moduleMapping = categoryMapping.get(vehicleCategory);
						else {
							moduleMapping = new LinkedHashMap<String, LinkedHashMap<String, String>>();
							categoryMapping.put(vehicleCategory, moduleMapping);
						}

						LinkedHashMap<String, String> workflowMapping = null;
						if (moduleMapping.containsKey(module))
							workflowMapping = moduleMapping.get(module);
						else {
							workflowMapping = new LinkedHashMap<String, String>();
							moduleMapping.put(module, workflowMapping);
						}

						workflowMapping.put(workflow, al);
					}
				}
			}

			// Init UI
			dlg = new EEComponent_ECNApproval_Dialog(new Shell());
			dlg.create();

			dlg.txtID.setText(selectedObject.getPropertyDisplayableValue("revision_list"));

			if (categoryMapping != null)
				dlg.cbVehicleCategory.setItems(new ArrayList<String>(categoryMapping.keySet()).toArray(new String[0]));

			dlg.cbVehicleCategory.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.cbModule.removeAll();
					if (dlg.cbVehicleCategory.getText().isEmpty())
						return;

					LinkedHashMap<String, LinkedHashMap<String, String>> moduleMapping = categoryMapping.get(dlg.cbVehicleCategory.getText());
					if (moduleMapping != null)
						dlg.cbModule.setItems(new ArrayList<String>(moduleMapping.keySet()).toArray(new String[0]));
				}
			});

			dlg.cbModule.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.cbWorkflow.removeAll();
					if (dlg.cbModule.getText().isEmpty())
						return;

					LinkedHashMap<String, LinkedHashMap<String, String>> moduleMapping = categoryMapping.get(dlg.cbVehicleCategory.getText());
					if (moduleMapping != null) {
						LinkedHashMap<String, String> workflowMapping = moduleMapping.get(dlg.cbModule.getText());
						if (workflowMapping != null) {
							dlg.cbWorkflow.setItems(new ArrayList<String>(workflowMapping.keySet()).toArray(new String[0]));
						}
					}
				}
			});

			dlg.cbWorkflow.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.cbCombine.setEnabled(CheckCombineable());
				}
			});

			dlg.cbCostImpact.setItems(new String[] { BIG_COST_IMPACT, "Non/ Low cost impact" });

			dlg.cbCombine.setItems(new String[] { "I Release", "PR Release" });

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					SubmitWorkflow();
				}
			});
			dlg.cbCombine.setEnabled(false);
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean CheckCombineable() {
		return (dlg.cbWorkflow.getText().contains("AfterSOB") || dlg.cbWorkflow.getText().contains("BeforeSOB"));
	}

	private boolean validationSubmit() {
		if (dlg.cbVehicleCategory.getText().isEmpty())
			return false;
		if (dlg.cbModule.getText().isEmpty())
			return false;
		if (dlg.cbWorkflow.getText().isEmpty())
			return false;
		if (dlg.cbCostImpact.getText().isEmpty())
			return false;

		return true;
	}

	private void SubmitWorkflow() {
		if (!validationSubmit()) {
			dlg.setMessage("Please input all required information.", true);
			return;
		}

		String category = dlg.cbVehicleCategory.getText();
		String module = dlg.cbModule.getText();
		String wfName = dlg.cbWorkflow.getText();

		String processDesc = checkCombineWith();
		checkBigCost();

		LinkedList<String> attachments = new LinkedList<String>();
		attachments.add(selectedObject.getUid());

		TriggerProcess trigger = new TriggerProcess(session);
		trigger.setWorkProcessTemplace(wfName);
		trigger.setWorkProcessName(category + " - " + module + " - " + selectedObject.toString());
		trigger.setWorkProcessDesc(processDesc);
		trigger.setAlName(getAL(category, module, wfName));
		trigger.setWorkProcessAttachment(attachments.toArray(new String[0]));

		String mess = trigger.run();
		if (mess.isEmpty()) {
			MessageBox.post("Process initiated.", "Success", MessageBox.INFORMATION);
			dlg.close();
		} else {
			dlg.setMessage(mess, true);
		}
	}

	private String getAL(String category, String module, String workflow) {
		if (categoryMapping.containsKey(category)) {
			LinkedHashMap<String, LinkedHashMap<String, String>> moduleMapping = categoryMapping.get(category);
			if (moduleMapping.containsKey(module)) {
				LinkedHashMap<String, String> workflowMapping = moduleMapping.get(module);
				if (workflowMapping.containsKey(workflow))
					return workflowMapping.get(workflow);
			}
		}

		return "";
	}

	private void checkBigCost() {
		String bigCost = dlg.cbCostImpact.getText();
		boolean check = false;
		if (bigCost.compareToIgnoreCase(BIG_COST_IMPACT) == 0) {
			check = true;
		}

		try {
			TCComponentItemRevision target = (TCComponentItemRevision) selectedObject;
			if (check) {
				target.setLogicalProperty("vf4_is_big_cost_impact", check);
			} else {
				target.setProperty("vf4_is_big_cost_impact", "0");
			}
		} catch (Exception e) {
			System.out.println(e.toString());
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