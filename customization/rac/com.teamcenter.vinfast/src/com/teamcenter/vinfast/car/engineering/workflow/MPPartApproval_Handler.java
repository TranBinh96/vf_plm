package com.teamcenter.vinfast.car.engineering.workflow;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.model.ALPreferenceModel;
import com.vf.utils.StringExtension;
import com.vf.utils.TriggerProcess;

public class MPPartApproval_Handler extends AbstractHandler {
	private TCSession session;
	private MPPartApproval_Dialog dlg;
	private LinkedList<TCComponent> objectTargets = null;
	private LinkedList<TCComponent> validateTargets = null;
	private ALPreferenceModel alModel = null;
	private LinkedHashMap<String, Set<TCComponentGroupMember>> al;
	private LinkedHashMap<String, Set<String>> conditionList;

	private static String WORKPROCESS_TEMPLACE = "VF_MP_Release_Process";
	private static String PREFERENCE_NAME = "VINF_MPPART_APPROVAL";

	public MPPartApproval_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] targetComponents = app.getTargetComponents();
		objectTargets = new LinkedList<TCComponent>();
		validateTargets = new LinkedList<TCComponent>();

		if (validObjectSelect(targetComponents)) {
			MessageBox.post("Please select MP Part Revision.", "Error", MessageBox.ERROR);
			return null;
		}

		try {
			// init Data
			conditionList = new LinkedHashMap<String, Set<String>>();
			alModel = new ALPreferenceModel(session, PREFERENCE_NAME, false);
			for (String conditions : alModel.valueList.keySet()) {
				if (conditions.contains(";")) {
					String[] str = conditions.split(";");
					if (conditionList.containsKey(str[0])) {
						conditionList.get(str[0]).add(str[1]);
					} else {
						Set<String> newModule = new HashSet<String>();
						newModule.add(str[1]);
						conditionList.put(str[0], newModule);
					}
				}
			}
			// init UI
			dlg = new MPPartApproval_Dialog(new Shell());
			dlg.create();

			dlg.setMessage("Trigger MP Part Process", false);
			dlg.cbProgram.setItems(conditionList.keySet().toArray(new String[0]));
			dlg.cbProgram.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					try {
						dlg.cbModule.removeAll();
						dlg.cbModule.setItems(conditionList.get(dlg.cbProgram.getText()).toArray(new String[0]));
						updateALTable();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			dlg.cbModule.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					try {
						updateALTable();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								submitToWorkflow();
							} catch (TCException e2) {
								e2.printStackTrace();
							}
						}
					});
				}
			});
			updatePartsListTable();
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

			// check 3D
			Boolean check3D = false;
			TCComponent[] drawingComponent = itemRevision.getRelatedComponents("IMAN_specification");
			for (TCComponent tcComponent : drawingComponent) {
				if (tcComponent.getTypeComponent().isTypeOf("Dataset") && (tcComponent.getDisplayType().compareToIgnoreCase("CATPart") == 0 || tcComponent.getDisplayType().compareToIgnoreCase("CATProduct") == 0)) {
					check3D = true;
					break;
				}
			}
			if (!check3D) {
				return "The part revision does not have any 3D attachment.";
			}

			// check Excel
			Boolean checkExcel = false;
			TCComponent[] excelComponent = itemRevision.getRelatedComponents("IMAN_specification");
			for (TCComponent tcComponent : excelComponent) {
				if (tcComponent.getType().compareToIgnoreCase("MSExcelX") == 0 || tcComponent.getType().compareToIgnoreCase("MSExcel") == 0) {
					checkExcel = true;
					break;
				}
			}
			if (!checkExcel) {
				return "The part revision does not have any Excel attachment.";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null || targetComponents.length == 0)
			return true;

		for (InterfaceAIFComponent objectTarget : targetComponents) {
			if (objectTarget instanceof TCComponentItemRevision) {
				if (objectTarget.getType().compareToIgnoreCase("VF4_MP_DesignRevision") == 0) {
					objectTargets.add((TCComponent) objectTarget);
				}
			} else if (objectTarget instanceof TCComponentBOMLine) {
				try {
					TCComponent itemRev = ((TCComponentBOMLine) objectTarget).getItemRevision();
					if (itemRev.getType().compareToIgnoreCase("VF4_MP_DesignRevision") == 0) {
						objectTargets.add(itemRev);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return false;
	}

	private void updatePartsListTable() {
		StringBuilder htmlText = new StringBuilder();
		htmlText.append("<html style=\"padding: 0px;\">");
		htmlText.append(StringExtension.htmlTableCss);
		htmlText.append("<body style=\"margin: 0px;\"><table>");
		htmlText.append("<tr style=\"background-color: #147391; color: white;\"><td style=\"width: 40%;\"><p>Part Number</p></td><td style=\"width: 60%;\"><p>Status</p></td></tr>");

		if (objectTargets != null && objectTargets.size() > 0) {
			for (TCComponent objectTarget : objectTargets) {
				try {
					String mess = validateItem(objectTarget);
					if (mess.isEmpty()) {
						htmlText.append("<tr><td><p>" + objectTarget.getPropertyDisplayableValue("object_string") + "</p></td>" + "<td><p style=\"color:#28D094\">" + "Available" + "</p></td>" + "</tr>");
						validateTargets.add(objectTarget);
					} else {
						htmlText.append("<tr><td><p>" + objectTarget.getPropertyDisplayableValue("object_string") + "</p></td>" + "<td><p style=\"color:#E91E63\">" + mess + "</p></td>" + "</tr>");
					}
				} catch (Exception e) {

				}
			}
		}

		htmlText.append("</table></body></html>");
		dlg.brsParts.setText(htmlText.toString());
	}

	private void updateALTable() throws Exception {
		Boolean checkEnable = true;
		String program = dlg.cbProgram.getText();
		String module = dlg.cbModule.getText();
		al = alModel.GetAssignmentListByCondition(program + ";" + module);

		StringBuilder htmlText = new StringBuilder();
		htmlText.append("<html style=\"padding: 0px;\">");
		htmlText.append(StringExtension.htmlTableCss);
		htmlText.append("<body style=\"margin: 0px;\"><table>");
		htmlText.append("<tr style=\"background-color: #147391; color: white;\"><td style=\"width: 50%;\"><p>Task Name</p></td><td style=\"width: 50%;\"><p>Reviewer</p></td></tr>");

		if (al != null) {
			for (Map.Entry<String, Set<TCComponentGroupMember>> entry : al.entrySet()) {
				if (entry.getValue() != null && entry.getValue().size() > 0) {
					String reviewers = "";
					for (TCComponentGroupMember reviewer : entry.getValue()) {
						if (!reviewers.isEmpty())
							reviewers += ", ";
						reviewers += reviewer.getUser();
					}
					htmlText.append("<tr><td><p>" + entry.getKey() + "</p></td><td><p>" + reviewers + "</p></td></tr>");
				} else {
					htmlText.append("<tr><td><p>" + entry.getKey() + "</p></td><td><p style=\"color:#E91E63\">" + "Missing" + "</p></td></tr>");
					checkEnable = false;
				}
			}
		} else {
			checkEnable = false;
		}

		htmlText.append("</table></body></html>");
		dlg.brsALs.setText(htmlText.toString());

		dlg.btnCreate.setEnabled(checkEnable);
	}

	private void submitToWorkflow() throws TCException {
		String program = dlg.cbProgram.getText();
		String module = dlg.cbModule.getText();
		LinkedList<String> attachments = new LinkedList<String>();
		for (TCComponent object : validateTargets) {
			attachments.add(object.getUid());
		}

		TriggerProcess trigger = new TriggerProcess(session);
		trigger.setWorkProcessTemplace(WORKPROCESS_TEMPLACE);
		trigger.setWorkProcessName(program + " - " + module);
		trigger.setAssignmentList(al);
		trigger.setWorkProcessAttachment(attachments.toArray(new String[0]));

		String mess = trigger.run();
		if (mess.isEmpty()) {
			MessageBox.post("Process initiated.", "Success", MessageBox.INFORMATION);
			dlg.close();
		} else {
			dlg.setMessage(mess, true);
		}
	}
}
