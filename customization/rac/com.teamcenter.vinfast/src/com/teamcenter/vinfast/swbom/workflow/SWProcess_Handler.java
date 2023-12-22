package com.teamcenter.vinfast.swbom.workflow;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

public class SWProcess_Handler extends AbstractHandler {
	private TCSession session;
	private SWProcess_Dialog dlg;
	private TCComponentItemRevision selectedObject = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private boolean isFRS = true;
	private LinkedHashMap<String, String> programMappingProcess;
	private LinkedHashMap<TCComponent, List<ValidationResult>> partsAndValidationResults = null;
	private String triggerMessage = "";
	private boolean isValidate = true;
	private LinkedHashMap<String, TCComponent[]> alList;
	private String workflowName = "";
	private TCComponentBOMLine topBom = null;
	private static String[] partReleaseStatus = new String[] { "PR", "PPR" };
	private String namingConventionInfo = "<br> Format: [TC SW Part number]-[TC revision]_[Encryption Status].[File type] <br> or [TC SW Part number]-[TC revision]_[SW block]_[Encryption Status].[File type]";

	public SWProcess_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (validObjectSelect(targetComp)) {
				MessageBox.post("Please select one VF FRS Revision/ECU Revision.", "Error", MessageBox.ERROR);
				return null;
			}

			// Init data
			alList = new LinkedHashMap<>();
			programMappingProcess = new LinkedHashMap<>();
			String[] preferenceValues = TCExtension.GetPreferenceValues("VINF_SWBOM_PROCESS_BY_PROGRAM", session);
			for (String value : preferenceValues) {
				String[] str = value.split("==");
				if (str.length > 2) {
					String workflowName = "";
					if (isFRS) {
						workflowName = str[1];
					} else {
						workflowName = str[2];
					}
					programMappingProcess.put(str[0], workflowName);
					if (!alList.containsKey(workflowName)) {
						alList.put(workflowName, TCExtension.getALByWorkflow(workflowName, session));
					}
				}
			}

			LinkedHashMap<String, String> programDataForm = TCExtension.GetLovValueAndDisplay("vf4_program", "VF3_FRSRevision", session);
			Set<String> removeKey = new HashSet<>();
			for (Map.Entry<String, String> programData : programDataForm.entrySet()) {
				if (!programMappingProcess.keySet().contains(programData.getKey()))
					removeKey.add(programData.getKey());

			}

			for (String key : removeKey) {
				programDataForm.remove(key);
			}

			String program = selectedObject.getPropertyDisplayableValue("vf4_program");
			// Init UI
			dlg = new SWProcess_Dialog(new Shell());
			dlg.create();

			dlg.txtID.setText(selectedObject.getPropertyDisplayableValue("object_string"));

			StringExtension.UpdateValueTextCombobox(dlg.cbProgram, programDataForm);
			if (programDataForm.values().contains(program))
				dlg.cbProgram.setText(program);
			dlg.cbProgram.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					updateProgram();
				}
			});

			dlg.btnValidate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (dlg.txtWorkflow.getText().isEmpty() || dlg.cbModule.getText().isEmpty()) {
						dlg.setMessage("Please input all required information.", true);
						return;
					}

					workflowName = dlg.txtWorkflow.getText();
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
//			updateProgram();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void updateProgram() {
		String program = (String) dlg.cbProgram.getData(dlg.cbProgram.getText());
		try {
			String workflow = programMappingProcess.get(program);
			dlg.txtWorkflow.setText(workflow);

			LinkedHashMap<String, String> moduleDataForm = new LinkedHashMap<>();
			for (TCComponent al : alList.get(workflow)) {
				String alName = ((TCComponentAssignmentList) al).getName();
				String alDesc = al.getProperty("list_desc");

				String[] str = alDesc.split(";");
				if (str.length > 1) {
					if (str[0].compareTo(program) == 0)
						moduleDataForm.put(alName, str[1]);
				}
			}

			StringExtension.UpdateValueTextCombobox(dlg.cbModule, moduleDataForm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String submitValidation() {
		if (dlg.txtWorkflow.getText().isEmpty() || dlg.cbModule.getText().isEmpty())
			return "Please input all required information";

		validateObject();

		if (!isValidate)
			return "Data not valid";

		return "";
	}

	private void submitWorkflow() {
		try {
			triggerMessage = "";
			String frsVersion = "";
			String model = "";
			String market = "";
			if (topBom != null) {
				try {
					TCComponentItemRevision frsItem = topBom.getItemRevision();
					frsVersion = frsItem.getPropertyDisplayableValue("object_name");
					model = frsItem.getPropertyDisplayableValue("vf4_model");
					market = frsItem.getPropertyDisplayableValue("vf4_market_arr");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String program = dlg.cbProgram.getText();
			String processTemplate = dlg.txtWorkflow.getText();
			String processName = processTemplate + " - " + frsVersion + " - " + program + " - " + model + " - " + market + " - " + selectedObject.toString();
			String alName = (String) dlg.cbModule.getData(dlg.cbModule.getText());
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Trigger processing...", IProgressMonitor.UNKNOWN);

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
						isValidate = true;
						validationResultText.append("<html style=\"padding: 0px;\">");
						validationResultText.append("<style> table, tr, td {border: 1px solid;}table {width: 100%;border-collapse: collapse;table-layout: fixed;}td {height: 20px;word-wrap:break-word;}</style>");

						partsAndValidationResults = new LinkedHashMap<TCComponent, List<ValidationResult>>();
						if (isFRS) {
							validateFRSRevision();
						} else {
							validateECURevision();
						}

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

	private void validateECURevision() throws Exception {
		List<ValidationResult> validateList = new LinkedList<ValidationResult>();
		validateList.add(validateObjectNull(((TCComponentItemRevision) selectedObject).getItem(), "ECU Type", "vf4_ECU_type"));

		ValidationResult inWorkflowValidate = validateObjectInWorkflow(selectedObject);
		if (inWorkflowValidate != null)
			validateList.add(inWorkflowValidate);

		ValidationResult binaryFileNameValidate = validateObjectBinaryFileName(selectedObject);
		if (binaryFileNameValidate != null)
			validateList.add(binaryFileNameValidate);

		ValidationResult swPartTypeValidate = validateObjectSWPartType(selectedObject);
		if (swPartTypeValidate != null)
			validateList.add(swPartTypeValidate);

		Set<String> partNotRelease = new HashSet<>();
		TCComponent[] partList = selectedObject.getRelatedComponents("PR4D_cad");
		if (partList != null) {
			for (TCComponent part : partList) {
				String objectType = part.getPropertyDisplayableValue("object_type");
				if (objectType.compareTo("ECU Revision") == 0) {
					List<ValidationResult> validateCompList = new LinkedList<ValidationResult>();
					validateCompList.add(validateObjectNull(((TCComponentItemRevision) selectedObject).getItem(), "ECU Type", "vf4_ECU_type"));

					ValidationResult inWorkflowCompValidate = validateObjectInWorkflow(selectedObject);
					if (inWorkflowValidate != null)
						validateCompList.add(inWorkflowCompValidate);

					ValidationResult binaryFileNameCompValidate = validateObjectBinaryFileName(selectedObject);
					if (binaryFileNameValidate != null)
						validateCompList.add(binaryFileNameCompValidate);

					ValidationResult swPartTypeCompValidate = validateObjectSWPartType(selectedObject);
					if (swPartTypeValidate != null)
						validateCompList.add(swPartTypeCompValidate);

					partsAndValidationResults.put(part, validateCompList);
				} else {
//					try {
//						String releaseStatus = part.getPropertyDisplayableValue("release_status_list");
//						if (releaseStatus.isEmpty())
//							partNotRelease.add(part.getPropertyDisplayableValue("item_id"));
//						else {
//							if (!Arrays.stream(partReleaseStatus).anyMatch(releaseStatus::equals))
//								partNotRelease.add(part.getPropertyDisplayableValue("item_id"));
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
				}
			}
		}
		ValidationResult partReleaseValidate = null;
		if (partNotRelease.size() == 0)
			partReleaseValidate = new ValidationResult("Child Parts Not Release", true, ValidationResult.passImage);
		else {
			partReleaseValidate = new ValidationResult("Child Parts Not Release", false, "Release Part list: <br> - " + String.join("<br>- ", partNotRelease));
			isValidate = false;
		}

		validateList.add(partReleaseValidate);
		partsAndValidationResults.put(selectedObject, validateList);
	}

	private void validateFRSRevision() throws Exception {
		List<ValidationResult> validateList = new LinkedList<ValidationResult>();

		validateList.add(validateObjectNull(selectedObject, "Program", "vf4_program"));

		ValidationResult inWorkflowValidate = validateObjectInWorkflow(selectedObject);
		if (inWorkflowValidate != null)
			validateList.add(inWorkflowValidate);

		ValidationResult haveConfigurationContext = validateObjectHaveConfigurationContext(selectedObject.getItem());
		if (haveConfigurationContext != null)
			validateList.add(haveConfigurationContext);

		ValidationResult haveECURelease = validateObjectHaveECURelease(selectedObject);
		if (haveECURelease != null)
			validateList.add(haveECURelease);

		partsAndValidationResults.put(selectedObject, validateList);
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
			if (startedWorkflow.toString().compareToIgnoreCase(workflowName) == 0) {
				inWorkflow = true;
				break;
			}
		}
		if (!inWorkflow) {
			TCComponent[] allWorkflowTask = part.getRelatedComponents("fnd0AllWorkflows");
			for (TCComponent allWorkflow : allWorkflowTask) {
				if (allWorkflow.toString().compareToIgnoreCase(workflowName) == 0) {
					inWorkflow = true;
					break;
				}
			}
		}
		ValidationResult itemValidate = null;
		if (inWorkflow) {
			itemValidate = new ValidationResult("Not in " + workflowName, false, "Can not trigger.");
			isValidate = false;
		} else {
			itemValidate = new ValidationResult("Not in " + workflowName, true, ValidationResult.passImage);
		}
		return itemValidate;
	}

	private ValidationResult validateObjectHaveConfigurationContext(TCComponent part) throws Exception {
		ValidationResult itemValidate = null;
		TCComponent[] configutionContext = part.getRelatedComponents("Smc0HasVariantConfigContext");
		if (configutionContext.length > 0) {
			itemValidate = new ValidationResult("Configuration Context", true, ValidationResult.passImage);
		} else {
			itemValidate = new ValidationResult("Configuration Context", false, "Copy Configuration Context to FRS.");
			isValidate = false;
		}
		return itemValidate;
	}

	private ValidationResult validateObjectHaveECURelease(TCComponent part) throws Exception {
		ValidationResult itemValidate = null;
		TCComponent[] ecuList = part.getRelatedComponents("PR4D_cad");
		String mess = "ECU Release";
		if (ecuList.length > 0) {
			Set<String> ecuNotRelease = new HashSet<String>();
			for (TCComponent ecu : ecuList) {
				try {
					String releaseStatus = ecu.getPropertyDisplayableValue("release_status_list");
					if (releaseStatus.isEmpty()) {
						boolean isInWorkflowTask = false;
						TCComponent[] workflowTask = ecu.getRelatedComponents("fnd0StartedWorkflowTasks");
						for (TCComponent tcComponent : workflowTask) {
							if (tcComponent.toString().compareToIgnoreCase("VF SINGLE ECU SIGNOFF") == 0) {
								isInWorkflowTask = true;
								break;
							}
						}
						if (!isInWorkflowTask)
							ecuNotRelease.add(ecu.getPropertyDisplayableValue("item_id"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (ecuNotRelease.size() == 0)
				itemValidate = new ValidationResult(mess, true, ValidationResult.passImage);
			else {
				itemValidate = new ValidationResult(mess, false, "Trigger ECU list: <br> - " + String.join("<br>- ", ecuNotRelease));
				isValidate = false;
			}
		} else {
			itemValidate = new ValidationResult(mess, false, "Copy ECU to FRS.");
			isValidate = false;
		}
		return itemValidate;
	}

	private ValidationResult validateObjectBinaryFileName(TCComponent part) throws Exception {
		ValidationResult itemValidate = null;
		TCComponent[] datasetList = part.getRelatedComponents("IMAN_specification");
		String mess = "Binary file name";
		if (datasetList.length > 0) {
			Set<String> fileNameNotValid = new HashSet<String>();
			for (TCComponent dataset : datasetList) {
				try {
					String datasetType = dataset.getType();
					if (datasetType.compareTo("ACD2_ACADEPRJ") == 0) {
						String fileName = dataset.getPropertyDisplayableValue("object_name");
						if (!checkNameConvention(fileName))
							fileNameNotValid.add(fileName);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (fileNameNotValid.size() == 0)
				itemValidate = new ValidationResult(mess, true, ValidationResult.passImage);
			else {
				itemValidate = new ValidationResult(mess, false, "Follow naming convention to update file name: <br>- " + String.join("<br>- ", fileNameNotValid) + namingConventionInfo);
				isValidate = false;
			}
		} else {
			itemValidate = new ValidationResult(mess, true, ValidationResult.passImage);
		}
		return itemValidate;
	}

	private ValidationResult validateObjectSWPartType(TCComponent part) throws Exception {
		ValidationResult itemValidate = null;
		TCComponent[] partList = part.getRelatedComponents("PR4D_cad");
		String mess = "Software Part Type";
		if (partList.length > 0) {
			Set<String> partNotSWPartType = new HashSet<String>();
			for (TCComponent item : partList) {
				try {
					if (item instanceof TCComponentItemRevision) {
						String objectType = item.getPropertyDisplayableValue("object_type");
						if (objectType.compareTo("ECU Revision") != 0) {
							if (((TCComponentItemRevision) item).getItem().getPropertyDisplayableValue("vf4_software_part_type").isEmpty())
								partNotSWPartType.add(item.getPropertyDisplayableValue("item_id"));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (partNotSWPartType.size() == 0)
				itemValidate = new ValidationResult(mess, true, ValidationResult.passImage);
			else {
				itemValidate = new ValidationResult(mess, false, "Input SW Part Type property for Part list: <br>- " + String.join("<br>- ", partNotSWPartType));
				isValidate = false;
			}
		} else {
			itemValidate = new ValidationResult(mess, true, ValidationResult.passImage);
		}
		return itemValidate;
	}

	private boolean checkNameConvention(String input) {
		if (!input.contains("_"))
			return false;

		if (!input.contains("."))
			return false;

		String[] str1 = input.split("[.]");
		if (str1.length != 2)
			return false;

		String fileName = str1[0];
		String fileExtension = str1[1];

		if (fileExtension.compareTo(fileExtension.toLowerCase()) != 0)
			return false;

		String[] str = fileName.split("_");
		if (str[str.length - 1].compareTo("UN") != 0)
			return false;

		return true;
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;
		if (targetComponents.length > 1)
			return true;

		if (targetComponents[0] instanceof TCComponentBOMLine) {
			try {
				selectedObject = ((TCComponentBOMLine) targetComponents[0]).getItemRevision();
				getParent((TCComponentBOMLine) targetComponents[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (selectedObject != null) {
			if (selectedObject.getType().compareToIgnoreCase("VF3_FRSRevision") != 0 && selectedObject.getType().compareToIgnoreCase("VF3_ECURevision") != 0)
				return true;
		}

		isFRS = selectedObject.getType().compareToIgnoreCase("VF3_FRSRevision") == 0;
		return false;
	}

	private void getParent(TCComponentBOMLine bom_line_parent2) {
		try {
			String level = bom_line_parent2.getProperty("bl_level_starting_0");
			if (level.compareTo("0") == 0) {
				topBom = bom_line_parent2;
				return;
			}

			TCComponent parent = bom_line_parent2.getReferenceProperty("bl_parent");
			TCComponentBOMLine bom_line_parent = (TCComponentBOMLine) parent;
			if (bom_line_parent != null) {
				getParent(bom_line_parent);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
}
