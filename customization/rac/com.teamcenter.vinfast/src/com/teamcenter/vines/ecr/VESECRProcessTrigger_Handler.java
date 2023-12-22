package com.teamcenter.vines.ecr;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

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

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

public class VESECRProcessTrigger_Handler extends AbstractHandler {
	private TCSession session;
	private VESECRProcessTrigger_Dialog dlg;
	private TCComponent selectedObject = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private String WORKPROCESS_TEMPLACE = "";
	private final static String ECR_WORKFLOW_NAME = "VINES_ECR_RELEASE_PROCESS";
	private final static String ECN_WORKFLOW_NAME = "VINES_ECN_RELEASE_PROCESS";
	private final static String ECR_OBJECT_TYPE = "VF4_VinES_ECR";
	private final static String ECN_OBJECT_TYPE = "VF4_VinES_ECN";
	private String AL_PREFIX = "";
	private final static String ECR_AL_PREFIX = "VINES_ECR_";
	private final static String ECN_AL_PREFIX = "VINES_ECN_";
	private LinkedHashMap<String, LinkedHashMap<String, String>> programMapping = null;
	private boolean firstUpdate = false;
	private boolean isValidate = true;
	private boolean isPermission = true;
	private boolean byPass = false;
	private LinkedHashMap<TCComponent, List<ValidationResult>> partsAndValidationResults = null;
	private LinkedHashMap<String, Boolean> validateSettingList = null;
	private LinkedList<String> objectTypeValidate = null;
	private String triggerMessage = "";

	public VESECRProcessTrigger_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			firstUpdate = true;
			isPermission = true;
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (targetComp.length != 1) {
				MessageBox.post("Please select only 1 VINES ECR/ECN", "Warning", MessageBox.WARNING);
				return null;
			}

			selectedObject = (TCComponent) targetComp[0];
			isPermission = TCExtension.checkPermissionAccess(selectedObject, "WRITE", session);

			// Init data
			String ecrCategoryLevel2 = selectedObject.getPropertyDisplayableValue("vf4_ecr_category2");
			String[] combineReleaseDataForm = new String[] { "", "P Release", "I Release", "PR Release" };

			objectTypeValidate = new LinkedList<String>();
			validateSettingList = new LinkedHashMap<String, Boolean>() {
				{
					put("ECR Category", true);
					put("Implement Date", true);
					put("Module Group", true);
					put("Change Specialist I", true);
					put("Analyst", true);
					put("In Workflow", true);
					put("Is After Sale", true);
					put("Is Modified Part", true);
					put("Child Release", true);
					put("Baseline Check", true);
					put("Release Combine", true);
					put("COP", true);
					put("ECR Document", true);
				}
			};

			String[] settingPreference = TCExtension.GetPreferenceValues("VINES_ECR_Validation_Setting", session);
			if (settingPreference != null && settingPreference.length > 0) {
				for (int i = 0; i < settingPreference.length; i++) {
					if (i == 0) {
						byPass = (settingPreference[i].contains(selectedObject.getPropertyDisplayableValue("item_id")));
					} else if (i == 1) {
						if (settingPreference[i].contains(";")) {
							String[] objectTypes = settingPreference[i].split(";");
							for (String objectType : objectTypes) {
								objectTypeValidate.add(objectType);
							}
						} else {
							objectTypeValidate.add(settingPreference[i]);
						}
					} else {
						if (settingPreference[i].contains("==")) {
							String[] str = settingPreference[i].split("==");
							validateSettingList.put(str[0], str[1].compareToIgnoreCase("true") == 0);
						}
					}
				}
			}
			if (selectedObject.getType().compareToIgnoreCase(ECR_OBJECT_TYPE + "Revision") == 0) {
				WORKPROCESS_TEMPLACE = ECR_WORKFLOW_NAME;
				AL_PREFIX = ECR_AL_PREFIX;
			} else {
				WORKPROCESS_TEMPLACE = ECN_WORKFLOW_NAME;
				AL_PREFIX = ECN_AL_PREFIX;
				byPass = true;
			}

			// Init UI
			dlg = new VESECRProcessTrigger_Dialog(new Shell());
			dlg.create();

			dlg.txtWorkflow.setText(WORKPROCESS_TEMPLACE);
			dlg.txtID.setText(selectedObject.getPropertyDisplayableValue("object_string"));

			createProgramMapping();
			dlg.cbProgram.setItems(programMapping.keySet().toArray(new String[0]));
			dlg.cbProgram.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.btnAccept.setEnabled(false);
					dlg.btnValidate.setEnabled(true);
					if (!firstUpdate) {
						dlg.cbModule.removeAll();
						String program = dlg.cbProgram.getText();
						LinkedHashMap<String, String> moduleMaping = programMapping.get(program);
						if (moduleMaping != null) {
							StringExtension.UpdateValueTextCombobox(dlg.cbModule, moduleMaping);
						}
					}
				}
			});

			dlg.cbModule.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.btnAccept.setEnabled(false);
					dlg.btnValidate.setEnabled(true);
				}
			});

			dlg.cbCombine.setItems(combineReleaseDataForm);
			if (combineReleaseDataForm.equals(ecrCategoryLevel2)) {
				dlg.cbCombine.setText(ecrCategoryLevel2);
			}
			dlg.cbCombine.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.btnAccept.setEnabled(false);
					dlg.btnValidate.setEnabled(true);
				}
			});

			dlg.btnValidate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String validateMess = submitValidation();
					if (validateMess.isEmpty()) {
						dlg.setMessage("Validate success. You can trigger process.", false);
						dlg.btnAccept.setEnabled(true);
						dlg.btnValidate.setEnabled(false);
					} else {
						dlg.setMessage(validateMess, true);
						dlg.btnAccept.setEnabled(false);
						dlg.btnValidate.setEnabled(true);
					}
				}
			});

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dlg.btnValidate.setEnabled(true);
					dlg.btnAccept.setEnabled(false);
					submitWorkflow();
				}
			});

			dlg.btnAccept.setEnabled(false);
			fillDefaultData();
			updateUI();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void updateUI() {
		boolean check = WORKPROCESS_TEMPLACE.compareToIgnoreCase(ECR_WORKFLOW_NAME) == 0;
		dlg.lblCombine.setVisible(check);
		dlg.cbCombine.setVisible(check);
	}

	private void fillDefaultData() throws Exception {
		firstUpdate = false;
		String program = selectedObject.getPropertyDisplayableValue("vf4_vehicle_group");
		String module = TCExtension.GetPropertyRealValue(selectedObject, "vf4_module_group");
		String category = selectedObject.getPropertyDisplayableValue("vf4_ecr_category2");
		if (Arrays.asList(dlg.cbProgram.getItems()).contains(program)) {
			dlg.cbProgram.setText(program);

			if (Arrays.asList(dlg.cbModule.getItems()).contains(module)) {
				dlg.cbModule.setText(module);
			}
		}
		if (Arrays.asList(dlg.cbCombine.getItems()).contains(category)) {
			dlg.cbCombine.setText(category);
		}
	}

	private String submitValidation() {
		if (dlg.txtWorkflow.getText().isEmpty())
			return "Please input all required information";
		if (dlg.cbProgram.getText().isEmpty())
			return "Please input all required information";
		if (dlg.cbModule.getText().isEmpty())
			return "Please input all required information";
		if (!dlg.cbCombine.getText().isEmpty()) {
			if (!isPermission)
				return "You are not authorized to combine ECR";
		}
		validateObject();
		if (!isValidate)
			return "Data not valid";

		return "";
	}

	private void submitWorkflow() {
		try {
			triggerMessage = "";
			String program = dlg.cbProgram.getText();
			String module = dlg.cbModule.getText();
			String processTemplate = dlg.txtWorkflow.getText();
			String alName = (String) dlg.cbModule.getData(module);
			String processName = program + " - " + module + " - " + selectedObject.toString();
			String combineStatus = dlg.cbCombine.getText();
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Trigger processing...", IProgressMonitor.UNKNOWN);

					try {
						// check Premium
						String PREMIUM_STR = "premium";
						if (alName.toLowerCase().contains(PREMIUM_STR)) {
							try {
								String modelStr = selectedObject.getProperty("vf6_model_es");
								if (modelStr.toLowerCase().contains(PREMIUM_STR) == false) {
									selectedObject.setProperty("vf6_model_es", modelStr + " PREMIUM");
								}
							} catch (TCException e) {
								e.printStackTrace();
							}
						}
						// trigger validate process template
						if (!byPass) {
							monitor.subTask("Trigger validate process...");
							String validationProcessTemplate = "";
							if (combineStatus.compareToIgnoreCase("P Release") == 0)
								validationProcessTemplate = "VinES Validation P Release";
							else if (combineStatus.compareToIgnoreCase("I Release") == 0)
								validationProcessTemplate = "VinES Validation I Release";
							else if (combineStatus.compareToIgnoreCase("PR Release") == 0)
								validationProcessTemplate = "VinES Validation PR Release";
							else
								validationProcessTemplate = "VinES Validation ECR Not Combine Release";

							TriggerProcess trigger = new TriggerProcess(session);
							trigger.setWorkProcessTemplace(validationProcessTemplate);
							trigger.setWorkProcessName(processName);
							trigger.setWorkProcessDesc("VF System Process");
							trigger.setWorkProcessAttachment(new String[] { selectedObject.getUid() });
							triggerMessage = trigger.run();

							if (!triggerMessage.isEmpty()) {
								throw new Exception(triggerMessage);
							}
						}
						// check combine status
						try {
							selectedObject.setProperty("vf4_remark", combineStatus);
						} catch (Exception e) {
							e.printStackTrace();
						}
						String processDesc = combineStatus.isEmpty() ? "" : "CHANGE APPROVAL COMBINED WITH " + combineStatus.toUpperCase();
						markTarget("_");

						// trigger ecr process tempalte
						monitor.subTask("Trigger ECR process...");
						TriggerProcess trigger = new TriggerProcess(session);
						trigger.setWorkProcessTemplace(processTemplate);
						trigger.setWorkProcessName(processName);
						trigger.setWorkProcessDesc(processDesc);
						trigger.setAlName(alName);
						trigger.setWorkProcessAttachment(new String[] { selectedObject.getUid() });
						triggerMessage = trigger.run();

						TCComponent item = selectedObject.getReferenceProperty("items_tag");
						item.setProperty("vf4_module_group_vi", module);

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
				MessageBox.post("Process initiated.", "Success", MessageBox.INFORMATION);
			} else {
				MessageBox.post(triggerMessage, "Error", MessageBox.ERROR);
			}
		} catch (Exception e) {
			MessageBox.post("Exception: " + e.toString(), "Error", MessageBox.ERROR);
			e.printStackTrace();
		}
		try {
			selectedObject.refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}

		dlg.close();
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

	private void createProgramMapping() {
		programMapping = new LinkedHashMap<>();
		TCComponent[] alList = TCExtension.getALByWorkflow(WORKPROCESS_TEMPLACE, session);
		if (alList != null) {
			for (TCComponent alItem : alList) {
				try {
					String alName = alItem.getProperty("list_name");
					String alDesc = alItem.getProperty("list_desc");
					if (alName.contains(AL_PREFIX)) {
						String[] str = alName.replace(AL_PREFIX, "").split("_");
						if (str.length >= 2) {
							String program = str[0];
							String module = str[1];

							if (programMapping.containsKey(program)) {
								programMapping.get(program).put(alName, module);
							} else {
								LinkedHashMap<String, String> moduleMapping = new LinkedHashMap<>();
								moduleMapping.put(alName, module);

								programMapping.put(program, moduleMapping);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void validateObject() {
		if (byPass)
			return;
		try {
			StringBuffer validationResultText = new StringBuffer();
			String combineStatus = dlg.cbCombine.getText();
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Validating data...", IProgressMonitor.UNKNOWN);

					try {
						isValidate = true;
						validationResultText.append("<html style=\"padding: 0px;\">");
						validationResultText.append("<style> table, tr, td {border: 1px solid;}table {width: 100%;border-collapse: collapse;table-layout: fixed;}td {height: 20px;word-wrap:break-word;}</style>");

						partsAndValidationResults = new LinkedHashMap<TCComponent, List<ValidationResult>>();
						validateECRRevision();
						validatePartList(combineStatus);

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

	private void validateECRRevision() throws Exception {
		List<ValidationResult> validateList = new LinkedList<ValidationResult>();

		if (validateSettingList.get("ECR Category"))
			validateList.add(validateObjectNull(selectedObject, "ECR Category", "vf4_ecr_category"));
		if (validateSettingList.get("Implement Date"))
			validateList.add(validateObjectNull(selectedObject, "Implement Date", "vf4_implementation_date_arr"));
		if (validateSettingList.get("Module Group"))
			validateList.add(validateObjectNull(selectedObject, "Module Group", "vf4_module_group_comp"));
		if (validateSettingList.get("Change Specialist I"))
			validateList.add(validateObjectNull(selectedObject, "Change Specialist I", "ChangeSpecialist1"));
		if (validateSettingList.get("Analyst"))
			validateList.add(validateObjectNull(selectedObject, "Analyst", "Analyst"));
		validateList.add(validateFolderNotHaveBaselineRevision(selectedObject.getRelatedComponents("CMHasImpactedItem"), "Impacted Folder"));
		validateList.add(validateFolderNotHaveBaselineRevision(selectedObject.getRelatedComponents("CMHasProblemItem"), "Problem Folder"));
		validateList.add(validateFolderNotHaveBaselineRevision(selectedObject.getRelatedComponents("Cm0HasProposal"), "Proposal Folder"));

		partsAndValidationResults.put(selectedObject, validateList);
	}

	private void validatePartList(String combineStatus) throws Exception {
		TCComponent[] partList = selectedObject.getRelatedComponents("Cm0HasProposal");
		if (partList != null && partList.length > 0) {
			String baselineStatus = "";
			String releaseStatus = "";
			if (!combineStatus.isEmpty()) {
				baselineStatus = combineStatus.replace("Release", "Check");
				releaseStatus = combineStatus.replace("Release", "");
			}
			for (TCComponent part : partList) {
				if (!objectTypeValidate.contains(part.getType()))
					continue;

				List<ValidationResult> validateList = new LinkedList<ValidationResult>();

				if (validateSettingList.get("In Workflow")) {
					ValidationResult inWorkflowValidate = validateObjectInWorkflow(part);
					if (inWorkflowValidate != null)
						validateList.add(inWorkflowValidate);
				}

				if (validateSettingList.get("Is After Sale")) {
					ValidationResult isAfterSaleValidate = validateObjectIsAfterSale(part);
					if (isAfterSaleValidate != null)
						validateList.add(isAfterSaleValidate);
				}

				if (validateSettingList.get("Is Modified Part")) {
					ValidationResult isModifiedPartValidate = validateObjectIsModifiedPart(part);
					if (isModifiedPartValidate != null)
						validateList.add(isModifiedPartValidate);
				}

				if (!combineStatus.isEmpty()) {
					if (validateSettingList.get("Child Release")) {
						ValidationResult childReleaseValidate = validateObjectChildRelease(part, releaseStatus.trim());
						if (childReleaseValidate != null)
							validateList.add(childReleaseValidate);
					}

					if (validateSettingList.get("Baseline Check")) {
						ValidationResult baselineCheckValidate = validateObjectBaselineCheck(part, baselineStatus);
						if (baselineCheckValidate != null)
							validateList.add(baselineCheckValidate);
					}

					if (validateSettingList.get("Release Combine")) {
						ValidationResult releaseCombineValidate = validateObjectReleaseCombineStatus(part, releaseStatus.trim());
						if (releaseCombineValidate != null)
							validateList.add(releaseCombineValidate);
					}
				}

				partsAndValidationResults.put(part, validateList);
			}
		}
	}

	private ValidationResult validateObjectNull(TCComponent validateObject, String validateName, String validateAttribute) throws Exception {
		ValidationResult itemValidate = null;
		if (validateObject.getPropertyDisplayableValue(validateAttribute).isEmpty()) {
			itemValidate = new ValidationResult(validateName, false, "Fill attribute " + validateName);
			isValidate = false;
		} else {
			itemValidate = new ValidationResult(validateName, true, ValidationResult.passImage);
		}

		return itemValidate;
	}

	private ValidationResult validateObjectInWorkflow(TCComponent part) throws Exception {
		boolean inWorkflow = false;
		TCComponent[] startedWorkflowTask = part.getRelatedComponents("fnd0StartedWorkflowTasks");
		for (TCComponent startedWorkflow : startedWorkflowTask) {
			if (startedWorkflow.toString().compareToIgnoreCase(WORKPROCESS_TEMPLACE) == 0) {
				inWorkflow = true;
				break;
			}
		}
		if (!inWorkflow) {
			TCComponent[] allWorkflowTask = part.getRelatedComponents("fnd0AllWorkflows");
			for (TCComponent allWorkflow : allWorkflowTask) {
				if (allWorkflow.toString().compareToIgnoreCase(WORKPROCESS_TEMPLACE) == 0) {
					inWorkflow = true;
					break;
				}
			}
		}
		ValidationResult itemValidate = null;
		if (inWorkflow) {
			itemValidate = new ValidationResult("Not in ECR/ECN Process", false, "Remove to proposal folder");
			isValidate = false;
		} else {
			itemValidate = new ValidationResult("Not in ECR/ECN Process", true, ValidationResult.passImage);
		}
		return itemValidate;
	}

	private ValidationResult validateObjectIsAfterSale(TCComponent part) throws Exception {
		if (part instanceof TCComponentItemRevision) {
			TCComponentItem partItem = ((TCComponentItemRevision) part).getItem();
			TCComponentItemRevision[] partRevisionList = partItem.getReleasedItemRevisions();
			if (partRevisionList.length > 0) {
				boolean check = false;
				for (TCComponentItemRevision partRevision : partRevisionList) {
					String statusList = partRevision.getPropertyDisplayableValue("release_status_list");
					if (!statusList.isEmpty()) {
						if (Arrays.stream(TCExtension.releaseStatusList).anyMatch(statusList::equals)) {
							check = true;
							break;
						}
					}
				}
				if (check) {
					return validateObjectNull(((TCComponentItemRevision) part).getItem(), "Is After Sale", "vf4_itm_after_sale_relevant");
				}
			}
		}
		return null;
	}

	private ValidationResult validateObjectReleaseCombineStatus(TCComponent part, String status) throws Exception {
		if (part instanceof TCComponentItemRevision) {
			TCComponentItem partItem = ((TCComponentItemRevision) part).getItem();
			boolean check = true;
			int statusIndex = TCExtension.getReleaseStatusIndex(status);
			TCComponentItemRevision[] partRevisionList = partItem.getReleasedItemRevisions();
			if (partRevisionList != null && partRevisionList.length > 0) {
				for (TCComponentItemRevision partRevision : partRevisionList) {
					String statusList = partRevision.getPropertyDisplayableValue("release_status_list");
					int index = TCExtension.getReleaseStatusIndex(statusList);
					if (index >= statusIndex) {
						check = false;
						break;
					}
				}
			}
			ValidationResult itemValidate = null;
			if (!check) {
				itemValidate = new ValidationResult("Part Release", false, "Part already release");
				isValidate = false;
			} else {
				itemValidate = new ValidationResult("Part Release", true, ValidationResult.passImage);
			}
			return itemValidate;
		}
		return null;
	}

	private ValidationResult validateObjectBaselineCheck(TCComponent part, String status) throws Exception {
		if (part instanceof TCComponentItemRevision) {
			boolean check = false;
			TCComponentItemRevision[] baselineRevList = ((TCComponentItemRevision) part).listBaselineRevs("");
			if (baselineRevList != null && baselineRevList.length > 0) {
				for (TCComponentItemRevision baselineRev : baselineRevList) {
					String statusList = baselineRev.getPropertyDisplayableValue("release_status_list");
					if (statusList.compareTo(status) == 0) {
						check = true;
						break;
					}
				}
			}
			ValidationResult itemValidate = null;
			if (!check) {
				itemValidate = new ValidationResult("Baseline Check", false, "Baseline part with " + status + " status");
				isValidate = false;
			} else {
				itemValidate = new ValidationResult("Baseline Check", true, ValidationResult.passImage);
			}
			return itemValidate;
		}
		return null;
	}

	private ValidationResult validateObjectChildRelease(TCComponent part, String status) throws Exception {
		if (part instanceof TCComponentItemRevision) {
			boolean check = true;
			int indexStatus = TCExtension.getReleaseStatusIndex(status);
			AIFComponentContext[] childPartList = ((TCComponentItemRevision) part).getChildren("PR4D_cad");
			if (childPartList != null && childPartList.length > 0) {
				for (AIFComponentContext childPart : childPartList) {
					if (childPart.getComponent() instanceof TCComponentItemRevision) {
						TCComponentItemRevision child = (TCComponentItemRevision) childPart.getComponent();
						String statusList = child.getPropertyDisplayableValue("release_status_list");
						int index = TCExtension.getReleaseStatusIndex(statusList);
						if (index < indexStatus) {
							check = false;
							break;
						}
					}
				}
			}
			ValidationResult itemValidate = null;
			if (!check) {
				itemValidate = new ValidationResult("Child Part Release", false, "Release child part with " + status + " status");
				isValidate = false;
			} else {
				itemValidate = new ValidationResult("Child Part Release", true, ValidationResult.passImage);
			}
			return itemValidate;
		}
		return null;
	}

	private ValidationResult validateObjectIsModifiedPart(TCComponent part) throws Exception {
		if (part instanceof TCComponentItemRevision) {
			String isModified = ((TCComponentItemRevision) part).getItem().getPropertyDisplayableValue("vf4_is_modified_part");
			if (isModified.compareTo("Yes") == 0) {
				return validateObjectNull(((TCComponentItemRevision) part).getItem(), "Old Part Number", "vf4_orginal_part_number");
			}
		}
		return null;
	}

	private ValidationResult validateFolderNotHaveBaselineRevision(TCComponent[] partList, String folderName) throws Exception {
		if (partList != null && partList.length > 0) {
			for (TCComponent part : partList) {
				String revID = part.getPropertyDisplayableValue("item_revision_id");
				if (revID.contains("."))
					return new ValidationResult(folderName, false, "Have baseline revision");
			}
		}
		return new ValidationResult(folderName, true, ValidationResult.passImage);
	}
}
