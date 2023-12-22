package com.teamcenter.vines.workflow;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

public class VESBPIApproval_Handler extends AbstractHandler {
	private TCSession session;
	private VESBPIApproval_Dialog dlg;
	private TCComponentItemRevision objectTarget = null;
	private String alName = "";
	private String documentType = "";
	private String modelCode = "";
	private String WORKPROCESS_NAME = "BPI_SpecDoc_Release";
	private String PREFERENCE_NAME = "VES_BPI_SPECDOC_APPROVAL";
	private LinkedHashMap<String, TCComponentAssignmentList> alList = null;

	public VESBPIApproval_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

		if (validObjectSelect(targetComponents)) {
			MessageBox.post("Please Select One Document Revision.", "Error", MessageBox.ERROR);
			return null;
		}

//		String messeage = validAfterStartDialog();
		createDialog("");

		return null;
	}

	private String validAfterStartDialog() {
		try {
			TCComponentItemRevision itemRevision = (TCComponentItemRevision) objectTarget;
			TCComponentItem item = itemRevision.getItem();

			// check in process
			TCComponent[] workflowTask = itemRevision.getRelatedComponents("fnd0StartedWorkflowTasks");
			for (TCComponent tcComponent : workflowTask) {
				if (tcComponent.toString().compareToIgnoreCase(WORKPROCESS_NAME) == 0) {
					return "Error: ESOM is already triggered to ESOM Workflow. If you wish to abort the work, then please send email to - itservicedesk@vinfast.vn.";
				}
			}

			// check approvaled
			TCComponent[] workflowTaskApprovaled = itemRevision.getRelatedComponents("fnd0AllWorkflows");
			for (TCComponent tcComponent : workflowTaskApprovaled) {
				if (tcComponent.toString().compareToIgnoreCase(WORKPROCESS_NAME) == 0) {
					return "Error: The selected ESOM document is already approved.";
				}
			}

			// check have PDF
			Boolean checkPDF = false;
			TCComponent[] excelComponent = itemRevision.getRelatedComponents("IMAN_specification");
			for (TCComponent tcComponent : excelComponent) {
				if (tcComponent.getType().compareToIgnoreCase("PDF") == 0)
					checkPDF = true;
			}
			if (!checkPDF) {
				return "Error: The selected ESOM document does not have any PDF attachment. Please attach ESOM template (PDF) and try again.";
			}
		} catch (Exception e) {
		}

		return "";
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;
		if (targetComponents.length > 1)
			return true;

		if (targetComponents[0] instanceof TCComponentItemRevision) {
			objectTarget = (TCComponentItemRevision) targetComponents[0];
		}

		if (objectTarget == null)
			return true;

		return false;
	}

	private void getALLogic() {
		alList = new LinkedHashMap<String, TCComponentAssignmentList>();
		TCComponent[] alItems = TCExtension.getALByWorkflow(WORKPROCESS_NAME, session);
		if (alItems != null) {
			for (TCComponent alItem : alItems) {
				if (alItem instanceof TCComponentAssignmentList) {
					String alDesc = "";
					try {
						alDesc = alItem.getProperty("list_desc");
						if (alDesc.contains(";")) {
							String[] str1 = alDesc.split(";");
							if (!str1[1].isEmpty()) {
								Set<String> docType = new HashSet<String>();
								if (str1[1].contains(",")) {
									for (String str : str1[1].split(",")) {
										if (!str.isEmpty())
											docType.add(str);
									}
								} else {
									docType.add(str1[1]);
								}

								for (String doc : docType) {
									alList.put(str1[0] + ";" + doc, (TCComponentAssignmentList) alItem);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void createDialog(String messageError) {
		StringBuffer validationResultText = new StringBuffer();
		validationResultText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		validationResultText.append(StringExtension.htmlTableCss);
		validationResultText.append("<body style=\"margin: 0px;\">");
		validationResultText.append("<table>");
		validationResultText.append(StringExtension.genTableHeader(new String[] { "Task Name", "Reviewer" }));
		validationResultText.append("</table>");
		validationResultText.append("</body></html>");

		try {
			// ---------------------------- Init Data -----------------------------
			getALLogic();
			documentType = objectTarget.getType();
			modelCode = objectTarget.getItem().getPropertyDisplayableValue("vf3_model_code");

			dlg = new VESBPIApproval_Dialog(new Shell());
			dlg.create();
			dlg.brwReviewer.setText(validationResultText.toString());
			// ---------------------------- Init Message -----------------------------
			dlg.txtDoctype.setText(documentType);
			dlg.txtModelCode.setText(modelCode);
			if (!messageError.isEmpty()) {
				dlg.setMessage(messageError, true);
				for (Control ctrl : dlg.getShell().getChildren()) {
					ctrl.setEnabled(false);
				}
			} else {
				updateReviewerTable();
			}
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
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateReviewerTable() throws Exception {
		StringBuffer validationResultText = new StringBuffer();
		validationResultText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		validationResultText.append(StringExtension.htmlTableCss);
		validationResultText.append("<body style=\"margin: 0px;\">");
		validationResultText.append("<table>");
		validationResultText.append(StringExtension.genTableHeader(new String[] { "Task Name", "Reviewer" }));
		//
		if (modelCode.compareTo("E1") != 0 && modelCode.compareTo("XN") != 0 && modelCode.compareTo("AC") != 0)
			modelCode = "OTHER";
		if (alList.containsKey(modelCode + ";" + documentType)) {
			TCComponentAssignmentList rawModel = alList.get(modelCode + ";" + documentType);
			alName = rawModel.getProperty("list_name");
			TCComponent[] resRaw = rawModel.getRelatedComponents("resources");
			TCComponent[] taskRaw = rawModel.getRelatedComponents("task_templates");

			for (int i = 0; i < taskRaw.length; i++) {
				validationResultText.append("<tr>");
				validationResultText.append("<td><p>" + taskRaw[i] + "</p></td>");
				TCComponent[] items = resRaw[i].getRelatedComponents("resources");
				if (items != null) {
					String reviewerStr = "";
					boolean first = true;
					for (TCComponent reviewer : items) {
						if (first) {
							first = false;
						} else {
							reviewerStr += "<br>";
						}
						reviewerStr += reviewer.toString();
					}
					validationResultText.append("<td><p>" + reviewerStr + "</p></td>");
				} else {
					validationResultText.append("<td>" + StringExtension.genBadgetFail("Missing") + "</td>");
				}
				validationResultText.append("</tr>");
			}
		}
		//
		validationResultText.append("</table>");
		validationResultText.append("</body></html>");

		dlg.brwReviewer.setText(validationResultText.toString());
		dlg.btnCreate.setEnabled(!alName.isEmpty());
	}

	private void submitToWorkflow() throws TCException {
		LinkedList<String> attachments = new LinkedList<String>();
		attachments.add(objectTarget.getUid());

		TriggerProcess trigger = new TriggerProcess(session);
		trigger.setWorkProcessTemplace(WORKPROCESS_NAME);
		trigger.setWorkProcessName(documentType + " - " + modelCode + " - " + objectTarget.toString());
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
}
