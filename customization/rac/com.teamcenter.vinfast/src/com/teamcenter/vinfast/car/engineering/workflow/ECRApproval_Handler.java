package com.teamcenter.vinfast.car.engineering.workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONObject;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.change.ECRInputManagement;
import com.teamcenter.vinfast.model.ALPreferenceModel;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.vf.utils.ExcelExtension;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

public class ECRApproval_Handler extends AbstractHandler {
	private TCSession session;
	private ECRApproval_Dialog dlg;
	private TCComponent selectedObject = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private String WORKPROCESS_TEMPLACE = "";
	private final static String ECR_WORKFLOW_NAME = "VinFast ECR";
	private final static String ECN_WORKFLOW_NAME = "VinFast ECN";
	private static final String[] GROUP_PERMISSION = new String[] { "dba", "Adhoc EBUS.HEXAGON.ERN.Manufacturing" };
	private LinkedHashMap<String, LinkedHashMap<String, Map<String, String>>> programMapping = null;
	private LinkedHashMap<String, String> programMappingProcess = null;
	private boolean firstUpdate = false;
	private boolean isValidate = true;
	private boolean isPermission = true;
	private boolean byPass = false;
	private LinkedHashMap<TCComponent, List<ValidationResult>> partsAndValidationResults = null;
	private LinkedHashMap<String, Set<TCComponentGroupMember>> copAssignmentList = null;
	private LinkedHashMap<String, Boolean> validateSettingList = null;
	private LinkedList<String> objectTypeValidate = null;
	private boolean isAdhocEBUS = false;
	private LinkedHashMap<String, String> donorVehicleMapping = null;
	private boolean isHaveCOP = false;
	private boolean isCheckCOP = false;
	private int maxPartInCall = 20;
	private String triggerMessage = "";
	private LinkedHashMap<String, String[]> ecrCheckListMapping;

	public ECRApproval_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		isAdhocEBUS = checkIsAdhocECRTransfer(arg0);
		if (isAdhocEBUS) {
			if (!TCExtension.checkUserHasGroup(GROUP_PERMISSION, session)) {
				MessageBox.post("Your group is not authorized for this feature.\nOnly users belonging to group \"" + String.join(";", GROUP_PERMISSION) + "\" have access-right to this feature.", "Accesss Denial", MessageBox.ERROR);
				return null;
			}
		}

		try {
			firstUpdate = true;
			isPermission = true;
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (targetComp.length != 1) {
				MessageBox.post("Please select only 1 ECR/ECN", "Warning", MessageBox.WARNING);
				return null;
			}

			selectedObject = (TCComponent) targetComp[0];
			isPermission = TCExtension.checkPermissionAccess(selectedObject, "WRITE", session);

			// Init data
			String ecrCategoryLevel2 = selectedObject.getPropertyDisplayableValue("vf6_ecr_category_lv2");
			LinkedHashMap<String, String> combineReleaseDataForm = TCExtension.GetLovValueAndDisplayInterdependent("vf6_ecr_category", "Vf6_ECRRevision", new String[] { "Gate release" }, session);
			combineReleaseDataForm.remove("PPR Release");
			combineReleaseDataForm.remove("P Release & I Release");
			combineReleaseDataForm.remove("I Release & PR Release");
			combineReleaseDataForm.remove("P Release & I Release & PR Release & PPR Release");
			combineReleaseDataForm.remove("Combine HTK01");
			combineReleaseDataForm.remove("Combine HTK02");
			combineReleaseDataForm.put("", "");

			donorVehicleMapping = TCExtension.GetLovValueAndDisplay("vf4_donor_vehicle", "VF4_Design", session);
			if (selectedObject.getType().compareToIgnoreCase("Vf6_ECRRevision") == 0)
				WORKPROCESS_TEMPLACE = ECR_WORKFLOW_NAME;
			else
				WORKPROCESS_TEMPLACE = ECN_WORKFLOW_NAME;

			objectTypeValidate = new LinkedList<String>();
			validateSettingList = new LinkedHashMap<String, Boolean>();

			validateSettingList.put("Implement Date", true);
			validateSettingList.put("Module Group", true);
			validateSettingList.put("Change Specialist I", true);
			validateSettingList.put("Analyst", true);
			validateSettingList.put("In Workflow", true);
			validateSettingList.put("Is After Sale", true);
			validateSettingList.put("Is Modified Part", true);
			validateSettingList.put("Child Release", true);
			validateSettingList.put("Baseline Check", true);
			validateSettingList.put("Release Combine", true);
			validateSettingList.put("COP", true);
			validateSettingList.put("ECR Document", true);
			validateSettingList.put("ECR Category", true);
			validateSettingList.put("ECR Check List", true);
			String[] settingPreference = TCExtension.GetPreferenceValues("VF_ECR_Validation_Setting", session);
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
					} else if (i == 2) {
						if (settingPreference[i].contains("==")) {
							String[] str = settingPreference[i].split("==");
							isCheckCOP = (str[1].compareToIgnoreCase("true") == 0);
						}
					} else {
						if (settingPreference[i].contains("==")) {
							String[] str = settingPreference[i].split("==");
							validateSettingList.put(str[0], str[1].compareToIgnoreCase("true") == 0);
						}
					}
				}
			}
			if (selectedObject.getType().compareToIgnoreCase("Vf6_ECNRevision") == 0)
				byPass = true;

			if (isAdhocEBUS)
				validateSettingList.put("In Workflow", false);

			ecrCheckListMapping = new LinkedHashMap<String, String[]>() {
				{
					put("vf6_1_consist_betw_cas_part", new String[] { "P", "I", "PR" });
					put("vf6_2_patents_check_all_mar", new String[] { "P", "I", "PR" });
					put("vf6_2_regulation_check_all", new String[] { "P", "I", "PR" });
					put("vf6_3_ee_related_sys_sor", new String[] { "P" });
					put("vf6_3_ee_related_fusa", new String[] { "P" });
					put("vf6_3_ee_related_sup_safety", new String[] { "P", "I" });
					put("vf6_3_list_of_safety", new String[] { "P", "I" });
					put("vf6_4_commercial_confirm", new String[] {});
					put("vf6_5_dvp_for_single_part", new String[] { "P", "I", "PR" });
					put("vf6_5_simulation_progress", new String[] { "P", "I", "PR" });
					put("vf6_5_strength", new String[] { "P", "I", "PR" });
					put("vf6_5_durability", new String[] { "P", "I", "PR" });
					put("vf6_5_hard_points_c_suv", new String[] {});
					put("vf6_5_hard_points_c_plat", new String[] {});
					put("vf6_5_model_analisys", new String[] { "P", "I", "PR" });
					put("vf6_5_nvh", new String[] { "P", "I", "PR" });
					put("vf6_5_operating_loads", new String[] { "P", "I", "PR" });
					put("vf6_5_crash", new String[] { "P", "I", "PR" });
					put("vf6_5_corrosion", new String[] { "P", "I", "PR" });
					put("vf6_5_heat_protection", new String[] { "P", "I", "PR" });
					put("vf6_6_part_match_bom", new String[] { "P", "I", "PR" });
					put("vf6_6_cad_pro_match_bom", new String[] { "P", "I", "PR" });
					put("vf6_6_stard_part_match_bom", new String[] { "P", "I", "PR" });
					put("vf6_6_q_checker", new String[] { "P", "I", "PR" });
					put("vf6_7_format", new String[] { "P", "I", "PR" });
					put("vf6_7_color_define", new String[] { "P", "I", "PR" });
					put("vf6_7_material_thickness", new String[] { "P", "I", "PR" });
					put("vf6_7_weight_desity", new String[] { "P", "I", "PR" });
					put("vf6_7_rps_desc", new String[] { "P", "I", "PR" });
					put("vf6_7_part_marking_3d", new String[] { "P", "I", "PR" });
					put("vf6_7_assy_parts", new String[] { "P", "I", "PR" });
					put("vf6_7_toleran_special_hole", new String[] { "P", "I", "PR" });
					put("vf6_7_cutting_tolerance", new String[] { "P", "I", "PR" });
					put("vf6_8_title_block", new String[] { "I", "PR" });
					put("vf6_8_represent_grid_lines", new String[] { "I", "PR" });
					put("vf6_8_edge", new String[] { "I", "PR" });
					put("vf6_8_symetric_parts", new String[] { "I", "PR" });
					put("vf6_8_represent_stand_parts", new String[] { "I", "PR" });
					put("vf6_8_coordinate_system", new String[] { "I", "PR" });
					put("vf6_8_represent_parts_assem", new String[] { "I", "PR" });
					put("vf6_8_material_specifi", new String[] { "I", "PR" });
					put("vf6_8_tolerance_specifi", new String[] { "I", "PR" });
					put("vf6_8_part_label_mark_sys", new String[] { "I", "PR" });
					put("vf6_8_supplier_DWG", new String[] { "I", "PR" });
					put("vf6_8_have_view_view", new String[] { "I", "PR" });
					put("vf6_8_mating_surface", new String[] { "I", "PR" });
					put("vf6_8_product_charac", new String[] { "I", "PR" });
					put("vf6_8_joining", new String[] { "I", "PR" });
					put("vf6_8_surface_treatment", new String[] { "I", "PR" });
					put("vf6_8_measurement_point", new String[] { "I", "PR" });
					put("vf6_8_rps_table", new String[] { "I", "PR" });
					put("vf6_8_gdnt", new String[] { "I", "PR" });
					put("vf6_9_part_feasibility", new String[] { "P", "I", "PR" });
					put("vf6_9_assembly_ability", new String[] { "P", "I", "PR" });
					put("vf6_9_painting_e_coating", new String[] { "P", "I", "PR" });
					put("vf6_9_assembly", new String[] { "P", "I", "PR" });
					put("vf6_9_logistic", new String[] { "P", "I", "PR" });
					put("vf6_9_after_Sales", new String[] {});
					put("vf6_10_interface_component", new String[] { "P", "I", "PR" });
					put("vf6_10_clearance_check", new String[] { "P", "I", "PR" });
					put("vf6_11_can_matrix", new String[] { "P", "I", "PR" });
					put("vf6_11_cds", new String[] { "P", "I", "PR" });
					put("vf6_11_basic_functions", new String[] { "P", "I", "PR" });
					put("vf6_11_adas_system", new String[] { "P", "I", "PR" });
					put("vf6_12_nvh", new String[] { "P", "I", "PR" });
					put("vf6_12_dynamics", new String[] { "P", "I", "PR" });
					put("vf6_12_weig_meet_target", new String[] { "P", "I", "PR" });
					put("vf6_12_durab_meet_target", new String[] { "P", "I", "PR" });
					put("vf6_12_power_consumtion", new String[] { "P", "I", "PR" });
				}
			};

			// Init UI
			dlg = new ECRApproval_Dialog(new Shell());
			dlg.create();

			dlg.txtWorkflow.setText(WORKPROCESS_TEMPLACE);
			dlg.txtID.setText(selectedObject.getPropertyDisplayableValue("object_string"));

			createProgramMapping();
			String[] programDataForm = new String[programMapping.size()];
			int i = 0;
			for (Map.Entry<String, LinkedHashMap<String, Map<String, String>>> entryItem : programMapping.entrySet()) {
				programDataForm[i++] = entryItem.getKey();
			}

			Arrays.sort(programDataForm);
			dlg.cbProgram.setItems(programDataForm);
			dlg.cbProgram.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.btnAccept.setEnabled(false);
					dlg.btnValidate.setEnabled(true);
					if (!firstUpdate) {
						dlg.cbModule.removeAll();
						String program = dlg.cbProgram.getText();
						if (!program.isEmpty()) {
							updateModule(program, "");
						}
						updateProcessName();
					}
					String program = dlg.cbProgram.getText();
					if (programMappingProcess != null && !program.isEmpty()) {
						dlg.txtWorkflow.setText(programMappingProcess.get(program));
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

			StringExtension.UpdateValueTextCombobox(dlg.cbCombine, combineReleaseDataForm);
			if (combineReleaseDataForm.values().contains(ecrCategoryLevel2)) {
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
						if (isCheckCOP)
							copPartList();
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

	private void updateModule(String program, String module) {
		try {
			List<String> moduleDataForm = new LinkedList<String>();
			LinkedHashMap<String, Map<String, String>> moduleMapping = programMapping.get(program);
			if (moduleMapping != null) {
				for (Map.Entry<String, Map<String, String>> entryItem : moduleMapping.entrySet()) {
					moduleDataForm.add(entryItem.getKey());
				}
			}
			dlg.cbModule.setItems(moduleDataForm.toArray(new String[0]));
			if (!module.isEmpty()) {
				if (moduleDataForm.contains(module)) {
					dlg.cbModule.setText(module);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateProcessName() {
		String program = dlg.cbProgram.getText();
		String module = dlg.cbModule.getText();
		if (!program.isEmpty()) {
			LinkedHashMap<String, Map<String, String>> moduleMapping = programMapping.get(program);
			if (moduleMapping != null) {
				Map<String, String> moduleItem = moduleMapping.get(module);
				if (moduleItem == null && module.isEmpty() && !moduleMapping.values().isEmpty()) {
					moduleItem = moduleMapping.values().iterator().next();
				}

				if (moduleItem != null) {
					dlg.txtWorkflow.setText(moduleItem.get("processName"));
				}
			}
		}
	}

	private boolean checkIsAdhocECRTransfer(ExecutionEvent evt) {
		boolean isAdhocECRTransfer = false;
		IParameter cmdParam;
		try {
			cmdParam = evt.getCommand().getParameter("com.teamcenter.vinfast.commands.admin.adhocECRTrigger");
			if (cmdParam != null) {
				String paramVal = (String) cmdParam.getName();
				if (paramVal.isEmpty() == false) {
					isAdhocECRTransfer = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isAdhocECRTransfer;
	}

	private void updateUI() {
		boolean check = WORKPROCESS_TEMPLACE.compareToIgnoreCase(ECR_WORKFLOW_NAME) == 0;
		dlg.lblCombine.setVisible(check);
		dlg.cbCombine.setVisible(check);
//		if (!byPass)
//			validateObject();
	}

	private void fillDefaultData() throws Exception {
		firstUpdate = false;
		String programValue = selectedObject.getPropertyDisplayableValue("vf6_vehicle_group");
		String module = TCExtension.GetPropertyRealValue(selectedObject, "vf6_module_group_comp");
		if (module.compareToIgnoreCase("Battery Pack") != 0 && module.compareToIgnoreCase("E-MOTOR") != 0) {
			String program = compareProgram(programValue);
			boolean checkExist = false;
			for (String programItem : dlg.cbProgram.getItems()) {
				if (program.compareToIgnoreCase(programItem) == 0) {
					checkExist = true;
					break;
				}
			}
			if (checkExist) {
				dlg.cbProgram.setText(program);
				updateModule(program, module);
			}
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
			String processName = program + " - " + module + " - " + selectedObject.toString();
			String combineStatus = dlg.cbCombine.getText();
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Trigger processing...", IProgressMonitor.UNKNOWN);

					try {
						// get Assignment List Name
						String alName = "";
						LinkedHashMap<String, Map<String, String>> moduleList = programMapping.get(program);
						if (moduleList != null) {
							Map<String, String> moduleItem = moduleList.get(module);
							if (moduleItem != null) {
								alName = moduleItem.get("alName");
							}
						}
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
						// check AdHoc EBUS
						if (isAdhocEBUS) {
							try {
								AIFComponentContext[] ecrForms = selectedObject.getRelated(ECRInputManagement.ECR_FORM_RELATION);
								if (ecrForms.length == 0) {
									createAndAttachECRForm();
								}
								selectedObject.setProperty("vf6cp_system_1", "_");
							} catch (TCException e) {
								e.printStackTrace();
							} catch (ServiceException e) {
								// skip checking part already in another ECR
								e.printStackTrace();
							}
						}
						// trigger validate process template
						if (!byPass) {
							monitor.subTask("Trigger validate process...");
							String validationProcessTemplate = "";
							if (combineStatus.compareToIgnoreCase("PR Release") == 0)
								validationProcessTemplate = "Vinfast Validation PR Release";
							else if (combineStatus.compareToIgnoreCase("I Release") == 0)
								validationProcessTemplate = "Vinfast Validation I Release";
							else if (combineStatus.compareToIgnoreCase("P Release") == 0)
								validationProcessTemplate = "Vinfast Validation P Release";
							else
								validationProcessTemplate = "Vinfast Validation ECR Not Cobine Release";

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
							if (combineStatus.trim().length() > 2) {
								selectedObject.setProperty("vf6_ecr_status", combineStatus);
							}
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

						// trigger ECR COP Notice
						if (isHaveCOP) {
							monitor.subTask("Trigger ECR COP Notice process...");
							TriggerProcess triggerCOP = new TriggerProcess(session);
							triggerCOP.setWorkProcessTemplace("VinFast-ECR-COP-notice");
							triggerCOP.setWorkProcessName(processName);
							triggerCOP.setWorkProcessDesc(processDesc);
							triggerCOP.setAssignmentList(copAssignmentList);
							triggerCOP.setWorkProcessAttachment(new String[] { selectedObject.getUid() });
							triggerCOP.run();
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

	private void createAndAttachECRForm() throws TCException, ServiceException {
		String ecrName = selectedObject.getProperty("object_name");
		DataManagementService dms = DataManagementService.getService(session);

		CreateIn ecrFormInput = new CreateIn();
		ecrFormInput.data.boName = ECRInputManagement.ECR_FORM_TYPE;
		ecrFormInput.data.stringProps.put("object_name", ecrName);
		CreateResponse response = dms.createObjects(new CreateIn[] { ecrFormInput });
		if (response.output.length > 0 && response.output[0].objects.length > 0) {
			selectedObject.setRelated(ECRInputManagement.ECR_FORM_RELATION, new TCComponent[] { response.output[0].objects[0] });
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

	private void createProgramMapping() {
		String type = selectedObject.getType();
		programMapping = new LinkedHashMap<String, LinkedHashMap<String, Map<String, String>>>();
		String[] programs = TCExtension.GetPreferenceValues("VF_Program_Type_Workflow", session);
		if (programs.length > 0) {
			for (String program : programs) {
				if (program.contains("~")) {
					String[] str = program.split("~");
					if (str.length > 2) {
						String programName = str[0];
						String objectType = str[1];
						String processName = str[2];
						if (type.compareToIgnoreCase(objectType) == 0) {
							LinkedHashMap<String, Map<String, String>> newItem = null;
							if (programMapping.containsKey(programName)) {
								newItem = programMapping.get(programName);
							} else {
								newItem = new LinkedHashMap<String, Map<String, String>>();
								programMapping.put(programName, newItem);
							}

							try {
								TCComponent[] als = TCExtension.getALByWorkflow(processName, session);
								if (als != null && als.length > 0) {
									for (TCComponent al : als) {

										String alName = al.getProperty("list_name");
										String alDesc = al.getProperty("list_desc");
										if (alName.contains("_" + programName + "_")) {
											newItem.put(alDesc, new HashMap<String, String>() {
												private static final long serialVersionUID = 1L;
												{
													put("alName", alName);
													put("processName", processName);
												}
											});
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
		}
	}

	private String compareProgram(String program) {
		if (program.compareToIgnoreCase("VF35") == 0 || program.compareToIgnoreCase("VF35/VFe35") == 0 || program.compareToIgnoreCase("VFe35") == 0) {
			return "VF35/VFe35";
		} else if (program.compareToIgnoreCase("VF36") == 0 || program.compareToIgnoreCase("VF36/VFe36") == 0 || program.compareToIgnoreCase("VFe36") == 0) {
			return "VF36/VFe36";
		} else if (program.compareToIgnoreCase("VFe34") == 0) {
			return "VF34";
		}

		return program;
	}

	private void copPartList() {
		isHaveCOP = false;
		copAssignmentList = new LinkedHashMap<String, Set<TCComponentGroupMember>>();
		Set<String> programStr = new HashSet<String>();
		StringBuilder copResult = new StringBuilder();
		copResult.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		copResult.append(StringExtension.htmlTableCss);
		copResult.append("<body style=\"margin: 0px;\">");

		LinkedHashMap<String, String> header = new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("Part", "40");
				put("Impacted Programs", "60");
			}
		};

		try {
			TCComponent[] partList = selectedObject.getRelatedComponents("CMHasProblemItem");
			if (partList != null && partList.length > 0) {
				List<TCComponent> partListOwn = new LinkedList<TCComponent>();
				for (TCComponent item : partList) {
					TCComponentItem part = null;
					if (item instanceof TCComponentItem)
						part = (TCComponentItem) item;
					else if (item instanceof TCComponentItemRevision)
						part = ((TCComponentItemRevision) item).getItem();

					if (part != null) {
						String donorVehicle = part.getPropertyDisplayableValue("vf4_donor_vehicle");
						if (!donorVehicle.isEmpty()) {
							if (compareProgramAndDonorVehicle(donorVehicle))
								partListOwn.add(part);
						}
					}
				}

				if (partListOwn != null && partListOwn.size() > 0) {
					copResult.append("<table>");
					copResult.append(StringExtension.genTableHeader(header));
					Map<String, Set<String>> impactedProgramMap = getImpactedProgramListByAPI(partListOwn.toArray(new TCComponent[0]));
					for (TCComponent part : partListOwn) {
						if (impactedProgramMap != null && part instanceof TCComponentItem) {
							Set<String> programImpacted = new HashSet<String>();
							Set<String> programList = impactedProgramMap.get(part.getPropertyDisplayableValue("item_id"));
							if (programList != null && programList.size() > 0) {
								for (String programValue : programList) {
									if (!donorVehicleMapping.containsKey(programValue))
										continue;

									String program = donorVehicleMapping.get(programValue);
									programStr.add(program);
									programImpacted.add(program);
								}
							}
							copResult.append("<tr>");
							copResult.append("<td>" + part.getProperty("item_id") + "-" + part.getProperty("object_name") + "</td>");
							copResult.append("<td>" + String.join(", ", programImpacted) + "</td>");
							copResult.append("</tr>");
						}
					}
					copResult.append("</table>");
					copResult.append("<br><br>");
				}
			}
			if (programStr == null || programStr.size() == 0) {
				copResult.append("<span>Not have impacted program</span>");
			} else {
				ALPreferenceModel alModel = new ALPreferenceModel(session, "VF_COP_ASSIGNMENT_LIST", false);
				copResult.append("<table>");
				LinkedList<String> headerTable2 = new LinkedList<String>();
				headerTable2.add("Program");
				headerTable2.addAll(alModel.getGateList());
				copResult.append(StringExtension.genTableHeader(headerTable2.toArray(new String[0])));

				String module = selectedObject.getPropertyDisplayableValue("vf6_module_group_comp");
				for (String program : programStr) {
					if (compareProgramAndDonorVehicle(program))
						continue;

					isHaveCOP = true;
					copResult.append("<tr>");
					copResult.append("<td>" + program + "</td>");
					LinkedHashMap<String, Set<TCComponentGroupMember>> als = alModel.GetAssignmentListByCondition(program + ";" + module);
					if (als != null) {
						for (Map.Entry<String, Set<TCComponentGroupMember>> alItem : als.entrySet()) {
							Set<TCComponentGroupMember> reviewerList = null;
							if (copAssignmentList.containsKey(alItem.getKey())) {
								reviewerList = alItem.getValue();
							} else {
								reviewerList = new HashSet<TCComponentGroupMember>();
								copAssignmentList.put(alItem.getKey(), reviewerList);
							}
							copResult.append("<td>");
							Set<String> reviewers = new HashSet<String>();
							if (alItem.getValue() != null) {
								for (TCComponentGroupMember userReview : alItem.getValue()) {
									reviewers.add(userReview.getUser().toString());
									reviewerList.add(userReview);
								}
							}
							copResult.append("<p>" + String.join("<br>", reviewers) + "</p>");
							copResult.append("</td>");
						}
					}
					copResult.append("</tr>");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		copResult.append("</table>");
		dlg.brwCOP.setText(copResult.toString());
		copResult.append("</body></html>");
	}

	private boolean compareProgramAndDonorVehicle(String donor) {
		if (donor == null)
			return true;

		String program = dlg.cbProgram.getText();
		if (program.compareToIgnoreCase("EDS") == 0)
			program = "EDS Platform";
		else if (program.compareToIgnoreCase("SnS") == 0)
			program = "S&S";
		else if (program.compareToIgnoreCase("VF34") == 0)
			program = "VFe34";
		else if (program.compareToIgnoreCase("VF35/VFe35") == 0)
			program = "VFe35/VF35";
		else if (program.compareToIgnoreCase("VF35/VFe35 Premium") == 0)
			program = "VFe35/VF35";
		else if (program.compareToIgnoreCase("VF34/VFe34") == 0)
			program = "VFe34/VF34";
		else if (program.compareToIgnoreCase("VF34/VFe34 Premium") == 0)
			program = "VFe34/VF34";

		if (program.contains(donor))
			return true;

		return false;
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

		if (validateSettingList.get("ECR Document")) {
			if (validateECRDocument())
				validateList.add(new ValidationResult("ECR Document", true, ValidationResult.passImage));
			else {
				validateList.add(new ValidationResult("ECR Document", false, "ECR Document not yet update. Please Generate ECR Document."));
				isValidate = false;
			}
		}

		if (validateSettingList.get("ECR Category"))
			validateList.add(validateECRCategory(selectedObject));
		if (validateSettingList.get("Implement Date"))
			validateList.add(validateObjectNull(selectedObject, "Implement Date", "vf6_implementation_date"));
		if (validateSettingList.get("Module Group"))
			validateList.add(validateObjectNull(selectedObject, "Module Group", "vf6_module_group_comp"));
		if (validateSettingList.get("Change Specialist I"))
			validateList.add(validateObjectNull(selectedObject, "Change Specialist I", "ChangeSpecialist1"));
		if (validateSettingList.get("Analyst"))
			validateList.add(validateObjectNull(selectedObject, "Analyst", "Analyst"));
		validateList.add(validateFolderNotHaveBaselineRevision(selectedObject.getRelatedComponents("CMHasImpactedItem"), "Impacted Folder"));
		validateList.add(validateFolderNotHaveBaselineRevision(selectedObject.getRelatedComponents("CMHasProblemItem"), "Problem Folder"));
		validateList.add(validateFolderNotHaveBaselineRevision(selectedObject.getRelatedComponents("Cm0HasProposal"), "Proposal Folder"));

		String status = selectedObject.getPropertyDisplayableValue("vf6_ecr_status");
		if (validateSettingList.get("ECR Check List")) {
			if (!status.isEmpty())
				status = status.replace(" Release", "");

			TCComponentType propDesc = session.getTypeService().getTypeComponent("Vf6_ECRRevision");
			for (Map.Entry<String, String[]> entrySet : ecrCheckListMapping.entrySet()) {
				String property = entrySet.getKey();
				List<String> statusCheckList = Arrays.asList(entrySet.getValue());
				if (statusCheckList.contains(status)) {
					validateList.add(validateECRCheckItem(selectedObject, propDesc.getPropDesc(property).getUiName(), property));
				}
			}
		}

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

	private ValidationResult validateECRCheckItem(TCComponent validateObject, String validateName, String validateAttribute) throws Exception {
		ValidationResult itemValidate = null;
		String propertyValue = validateObject.getPropertyDisplayableValue(validateAttribute);
		if (propertyValue.isEmpty()) {
			itemValidate = new ValidationResult(validateName, false, "Fill property.");
			isValidate = false;
		} else {
			if (propertyValue.compareTo("3") == 0) {
				itemValidate = new ValidationResult(validateName, false, "Risk Releasing: 3.Not conform/item not taken in to account.");
				isValidate = false;
			} else {
				itemValidate = new ValidationResult(validateName, true, ValidationResult.passImage);
			}
		}

		return itemValidate;
	}

	private ValidationResult validateECRCategory(TCComponent validateObject) throws Exception {
		ValidationResult itemValidate = null;
		String ecrCategory = validateObject.getPropertyDisplayableValue("vf6_ecr_category");
		if (ecrCategory.isEmpty()) {
			itemValidate = new ValidationResult("ECR Category", false, "Fill attribute ECR Category");
			isValidate = false;
		} else {
			String[] ecrCategoryLOV = TCExtension.GetLovValues("vf6_ecr_category", "Vf6_ECRRevision", session);
			if (Arrays.toString(ecrCategoryLOV).contains(ecrCategory))
				itemValidate = new ValidationResult("ECR Category", true, ValidationResult.passImage);
			else
				itemValidate = new ValidationResult("ECR Category", false, "Current value of ECR Category field is invalid, please clean and select another value then.");
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

	private boolean validateECRDocument() {
		File templateFile = getECRExcelFile();
		if (templateFile == null)
			return false;

		try {
			InputStream outputFile = new FileInputStream(templateFile);
			XSSFWorkbook workbook = new XSSFWorkbook(outputFile);

			XSSFSheet worksheet = workbook.getSheet("ECR Summary");
			if (worksheet == null)
				return false;

			if (!compareExcelValueAndSummaryValue(worksheet, "H3", selectedObject.getPropertyDisplayableValue("item_id") + " - " + selectedObject.getPropertyDisplayableValue("object_name")))
				return false;
			// -------------------------------- Change Description
			// Current Weight
			if (!compareExcelValueAndSummaryValue(worksheet, "E7", getDecimal(selectedObject.getPropertyDisplayableValue("vf6cp_current_weight"))))
				return false;
			// Current Material
			if (!compareExcelValueAndSummaryValue(worksheet, "J7", selectedObject.getPropertyDisplayableValue("vf6cp_current_material")))
				return false;
			// New Weight
			if (!compareExcelValueAndSummaryValue(worksheet, "Q7", getDecimal(selectedObject.getPropertyDisplayableValue("vf6cp_new_weight"))))
				return false;
			// New Material
			if (!compareExcelValueAndSummaryValue(worksheet, "U7", selectedObject.getPropertyDisplayableValue("vf6cp_new_material")))
				return false;
			// Supplier
			if (!compareExcelValueAndSummaryValue(worksheet, "AD6", selectedObject.getPropertyDisplayableValue("vf6cp_supplier")))
				return false;
			// Vehicle cost delta
			if (!compareExcelValueAndSummaryValue(worksheet, "AD8", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_init_veh_cost_delta"))))
				return false;
			// Piece cost delta
			if (!compareExcelValueAndSummaryValue(worksheet, "AD9", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_delta_piece_cost"))))
				return false;
			// Tooling cost delta
			if (!compareExcelValueAndSummaryValue(worksheet, "AD10", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_delta_tooling_cost"))))
				return false;
			// ED&D cost delta
			if (!compareExcelValueAndSummaryValue(worksheet, "AD11", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_delta_edd_cost"))))
				return false;
			// Other cost
			if (!compareExcelValueAndSummaryValue(worksheet, "AD12", getDecimal(selectedObject.getPropertyDisplayableValue("vf6_other_cost"))))
				return false;
			// Leadtime impact
			if (!compareExcelValueAndSummaryValue(worksheet, "AD13", getDecimal(selectedObject.getPropertyDisplayableValue("vf6cp_leadtime_impact"))))
				return false;
			// PTO/SOP Time impact
			if (!compareExcelValueAndSummaryValue(worksheet, "AD14", getDecimal(selectedObject.getPropertyDisplayableValue("vf6cp_pto_sop_timeimpact"))))
				return false;
			// Hard TKO-01/Order material
			if (!compareExcelValueAndSummaryValue(worksheet, "AD15", selectedObject.getPropertyDisplayableValue("vf6_is_htko1_required")))
				return false;
			// Hard TKO-02/Cut steel
			if (!compareExcelValueAndSummaryValue(worksheet, "AD16", selectedObject.getPropertyDisplayableValue("vf6_is_htko2_required")))
				return false;
			// Problem
			if (!compareExcelValueAndSummaryValue(worksheet, "Z18", selectedObject.getPropertyDisplayableValue("vf6cp_problem")))
				return false;
			// Root cause
			if (!compareExcelValueAndSummaryValue(worksheet, "Z25", selectedObject.getPropertyDisplayableValue("vf6cp_root_cause")))
				return false;
			// Solution
			if (!compareExcelValueAndSummaryValue(worksheet, "Z34", selectedObject.getPropertyDisplayableValue("vf6cp_solution")))
				return false;
			// -------------------------------- Precab Information
			// Project/Program
			if (!compareExcelValueAndSummaryValue(worksheet, "AS6", selectedObject.getPropertyDisplayableValue("vf6_vehicle_group")))
				return false;
			// Market
			String market = selectedObject.getPropertyDisplayableValue("vf6cp_market");
			if (!compareExcelValueAndSummaryValue(worksheet, "AS7", market.contains("Vietnam") ? "R" : "£"))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "AT7", market.contains("USA&CAN") ? "R" : "£"))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "AU7", market.contains("Europe") ? "R" : "£"))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "AV7", market.contains("USA&CAN") ? "R" : "£"))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "AW7", market.contains("Thailand") ? "R" : "£"))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "AX7", market.contains("Singapore") ? "R" : "£"))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "AY7", market.contains("United_Kingdom") ? "R" : "£"))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "AZ7", market.contains("Australia") ? "R" : "£"))
				return false;
			// Driving Position
			String lhdrhd = selectedObject.getPropertyDisplayableValue("vf6cp_lhd_rhd");
			if (!compareExcelValueAndSummaryValue(worksheet, "AS14", lhdrhd.contains("LHD") ? "R" : "£"))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "AW14", lhdrhd.contains("RHD") ? "R" : "£"))
				return false;
			if (lhdrhd.compareToIgnoreCase("All") == 0) {
				if (!compareExcelValueAndSummaryValue(worksheet, "AS14", "R"))
					return false;
				if (!compareExcelValueAndSummaryValue(worksheet, "AW14", "R"))
					return false;
			}
			// Variant
			String variant = selectedObject.getPropertyDisplayableValue("vf6cp_variant");
			if (!compareExcelValueAndSummaryValue(worksheet, "AS16", variant.isEmpty() ? "" : variant.replace(",", "\n")))
				return false;
			// Seat
			String seatConfi = selectedObject.getPropertyDisplayableValue("vf6cp_seat_configuration");
			if (!compareExcelValueAndSummaryValue(worksheet, "AS20", seatConfi.contains("5") ? "R" : "£"))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "AU20", seatConfi.contains("6") ? "R" : "£"))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "AW20", seatConfi.contains("7") ? "R" : "£"))
				return false;
			// Pre-cab decision approval
			if (!compareExcelValueAndSummaryValue(worksheet, "AN23", selectedObject.getPropertyDisplayableValue("vf6cp_pre_decision_approval")))
				return false;
			// Pre-cab decision comments
			if (!compareExcelValueAndSummaryValue(worksheet, "AS23", selectedObject.getPropertyDisplayableValue("vf6cp_precab_dec_comment")))
				return false;
			// Pre-cab Approval date
			if (!compareExcelValueAndSummaryValue(worksheet, "AS28", selectedObject.getPropertyDisplayableValue("vf6cp_pre_approval_date")))
				return false;
			// -------------------------------- ECR Information
			// ECR No
			if (!compareExcelValueAndSummaryValue(worksheet, "AS30", selectedObject.getPropertyDisplayableValue("item_id")))
				return false;
			// ECR Trigger date
//			if (!compareExcelValueAndSummaryValue(worksheet, "AS31", "");
			// ECR contact email
			if (!compareExcelValueAndSummaryValue(worksheet, "AS32", selectedObject.getPropertyDisplayableValue("vf6cp_ecr_contact_email")))
				return false;
			// Module Group
			if (!compareExcelValueAndSummaryValue(worksheet, "AS33", selectedObject.getPropertyDisplayableValue("vf6_module_group_comp")))
				return false;
			// SIL No
			if (!compareExcelValueAndSummaryValue(worksheet, "AS34", selectedObject.getPropertyDisplayableValue("vf6cp_sil_no")))
				return false;
			// DCR No
			if (!compareExcelValueAndSummaryValue(worksheet, "AS35", selectedObject.getPropertyDisplayableValue("vf6cp_dcr_no")))
				return false;
			// Effective Progposal
			if (!compareExcelValueAndSummaryValue(worksheet, "AS36", selectedObject.getPropertyDisplayableValue("vf6_effective_proposal")))
				return false;
			// Coordinated change
			if (!compareExcelValueAndSummaryValue(worksheet, "AS37", selectedObject.getPropertyDisplayableValue("vf6_coordinated_change")))
				return false;
			// -------------------------------- Affection
			// Impacted Module Group
			String impactedModule = selectedObject.getPropertyDisplayableValue("vf6_impacted_module_comp");
			if (!compareExcelValueAndSummaryValue(worksheet, "BH7", impactedModule.contains("Chasis") ? "ü" : ""))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "BH8", impactedModule.contains("Doors Closures") ? "ü" : ""))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "BH9", impactedModule.contains("Electrics/Electronics") ? "ü" : ""))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "BH10", impactedModule.contains("Interior") ? "ü" : ""))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "BH11", impactedModule.contains("Powertrain") ? "ü" : ""))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "BH12", impactedModule.contains("Vehicle Complete") ? "ü" : ""))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "BH13", impactedModule.contains("Exterior") ? "ü" : ""))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "BH14", impactedModule.contains("Body in White") ? "ü" : ""))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "BH15", impactedModule.contains("Seats and Restraints") ? "ü" : ""))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "BH16", impactedModule.contains("Battery Pack") ? "ü" : ""))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "BH17", impactedModule.contains("E-MOTOR") ? "ü" : ""))
				return false;
			if (!compareExcelValueAndSummaryValue(worksheet, "BH18", impactedModule.contains("3_IN_1_PDU_DCDC_OBC") ? "ü" : ""))
				return false;
			// Function
			// Is Purchase Review REQ
			if (!compareExcelValueAndSummaryValue(worksheet, "BH33", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_purchase_review_req")) ? "ü" : ""))
				return false;
			// Is Homo REQ
			if (!compareExcelValueAndSummaryValue(worksheet, "BH31", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_homo_required")) ? "ü" : ""))
				return false;
			// Is After sale Review REQ
			if (!compareExcelValueAndSummaryValue(worksheet, "BH32", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_aftersale_required")) ? "ü" : ""))
				return false;
			// Is Styling Review REQ
			if (!compareExcelValueAndSummaryValue(worksheet, "BH28", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_color_trim_required")) ? "ü" : ""))
				return false;
			// Is Functional Safety Review REQ
			if (!compareExcelValueAndSummaryValue(worksheet, "BH33", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_functional_safety")) ? "ü" : ""))
				return false;
			// Is Standard part Review REQ
			if (!compareExcelValueAndSummaryValue(worksheet, "BH34", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_standard_part")) ? "ü" : ""))
				return false;
			// Is indirect purchase required
			if (!compareExcelValueAndSummaryValue(worksheet, "BH39", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_is_indirectPur_required")) ? "ü" : ""))
				return false;

			// Documentation
			// Have SOR
			if (!compareExcelValueAndSummaryValue(worksheet, "BQ7", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_have_SOR")) ? "ü" : ""))
				return false;
			// Have Spec Book
			if (!compareExcelValueAndSummaryValue(worksheet, "BQ8", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_have_spec_book")) ? "ü" : ""))
				return false;
			// Have DFMEA
			if (!compareExcelValueAndSummaryValue(worksheet, "BQ9", checkBooleanValue(selectedObject.getPropertyDisplayableValue("vf6_have_DFMEA")) ? "ü" : ""))
				return false;

			// Bom Information
			LinkedList<LinkedHashMap<String, String>> bomInfoInECRDocument = new LinkedList<LinkedHashMap<String, String>>();
			int i = 0;
			for (Row row : worksheet) {
				i++;
				if (i > 45) {
					if (getCellValue(worksheet, "B" + String.valueOf(row.getRowNum())).isEmpty())
						break;
					LinkedHashMap<String, String> bomInfoItem = new LinkedHashMap<String, String>() {
						private static final long serialVersionUID = 1L;
						{
							put("vf6_posid", getCellValue(worksheet, "B" + String.valueOf(row.getRowNum())));
							put("vf6_donor_vehicle", getCellValue(worksheet, "E" + String.valueOf((row.getRowNum()))));
							put("vf6_structure_level", getCellValue(worksheet, "G" + String.valueOf(row.getRowNum())));
							put("vf6_steering", getCellValue(worksheet, "H" + String.valueOf(row.getRowNum())));
							put("vf6_quantity2", getCellValue(worksheet, "I" + String.valueOf(row.getRowNum())));
							put("vf6_maturity_level", getCellValue(worksheet, "J" + String.valueOf(row.getRowNum())));
							put("vf6_purchase_level", getCellValue(worksheet, "L" + String.valueOf(row.getRowNum())));
							put("vf6_change_type", getCellValue(worksheet, "M" + String.valueOf(row.getRowNum())));
							put("vf6_part_number", getCellValue(worksheet, "O" + String.valueOf(row.getRowNum())));
							put("vf6_old_version", getCellValue(worksheet, "R" + String.valueOf(row.getRowNum())));
							put("vf6_frozen_revision", getCellValue(worksheet, "S" + String.valueOf(row.getRowNum())));
							put("vf6_new_revision", getCellValue(worksheet, "T" + String.valueOf(row.getRowNum())));
							put("vf6_part_name", getCellValue(worksheet, "U" + String.valueOf(row.getRowNum())));
							put("vf6_original_base_part", getCellValue(worksheet, "AC" + String.valueOf(row.getRowNum())));
							put("vf6_variant_formula", getCellValue(worksheet, "AG" + String.valueOf(row.getRowNum())));
							put("vf6_torque_information", getCellValue(worksheet, "AL" + String.valueOf(row.getRowNum())));
							put("vf6_weight", getCellValue(worksheet, "AQ" + String.valueOf(row.getRowNum())));
							put("vf6_3d_data_affected", getCellValue(worksheet, "BD" + String.valueOf(row.getRowNum())));
							put("vf6_material", getCellValue(worksheet, "BE" + String.valueOf(row.getRowNum())));
							put("vf6_cad_coating", getCellValue(worksheet, "BH" + String.valueOf(row.getRowNum())));
							put("vf6_specbook", getCellValue(worksheet, "BM" + String.valueOf(row.getRowNum())));
							put("vf6_is_aftersale_relevaant", getCellValue(worksheet, "BY" + String.valueOf(row.getRowNum())));
							put("vf6_exchangeability", getCellValue(worksheet, "CA" + String.valueOf(row.getRowNum())));
							put("vf6_part_tracebility", getCellValue(worksheet, "CE" + String.valueOf(row.getRowNum())));
						}
					};
					bomInfoInECRDocument.add(bomInfoItem);
				}
			}
			TCComponent[] forms = selectedObject.getRelatedComponents("Vf6_ecr_info_relation");
			if (forms.length > 0) {
				TCComponent[] bomInfo = forms[0].getRelatedComponents("vf6_bom_information");

				if (bomInfo.length != bomInfoInECRDocument.size())
					return false;

				i = 0;
				for (TCComponent bomline : bomInfo) {
					if (bomline.getPropertyDisplayableValue("vf6_posid").compareTo(bomInfoInECRDocument.get(i).get("vf6_posid")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_donor_vehicle").compareTo(bomInfoInECRDocument.get(i).get("vf6_donor_vehicle")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_structure_level").compareTo(bomInfoInECRDocument.get(i).get("vf6_structure_level")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_steering").compareTo(bomInfoInECRDocument.get(i).get("vf6_steering")) != 0)
						return false;
//					if (bomline.getPropertyDisplayableValue("vf6_quantity2").compareTo(bomInfoInECRDocument.get(i).get("vf6_quantity2")) != 0)
//						return false;
					if (bomline.getPropertyDisplayableValue("vf6_maturity_level").compareTo(bomInfoInECRDocument.get(i).get("vf6_maturity_level")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_purchase_level").compareTo(bomInfoInECRDocument.get(i).get("vf6_purchase_level")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_change_type").compareTo(bomInfoInECRDocument.get(i).get("vf6_change_type")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_part_number").compareTo(bomInfoInECRDocument.get(i).get("vf6_part_number")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_old_version").compareTo(bomInfoInECRDocument.get(i).get("vf6_old_version")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_frozen_revision").compareTo(bomInfoInECRDocument.get(i).get("vf6_frozen_revision")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_new_revision").compareTo(bomInfoInECRDocument.get(i).get("vf6_new_revision")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_part_name").compareTo(bomInfoInECRDocument.get(i).get("vf6_part_name")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_original_base_part").compareTo(bomInfoInECRDocument.get(i).get("vf6_original_base_part")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_variant_formula").compareTo(bomInfoInECRDocument.get(i).get("vf6_variant_formula")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_torque_information").compareTo(bomInfoInECRDocument.get(i).get("vf6_torque_information")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_weight").compareTo(bomInfoInECRDocument.get(i).get("vf6_weight")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_3d_data_affected").compareTo(bomInfoInECRDocument.get(i).get("vf6_3d_data_affected")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_material").compareTo(bomInfoInECRDocument.get(i).get("vf6_material")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_cad_coating").compareTo(bomInfoInECRDocument.get(i).get("vf6_cad_coating")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_specbook").compareTo(bomInfoInECRDocument.get(i).get("vf6_specbook")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_is_aftersale_relevaant").compareTo(bomInfoInECRDocument.get(i).get("vf6_is_aftersale_relevaant")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_exchangeability").compareTo(bomInfoInECRDocument.get(i).get("vf6_exchangeability")) != 0)
						return false;
					if (bomline.getPropertyDisplayableValue("vf6_part_tracebility").compareTo(bomInfoInECRDocument.get(i).get("vf6_part_tracebility")) != 0)
						return false;
					i++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private boolean compareExcelValueAndSummaryValue(XSSFSheet worksheet, String cellAddress, String summaryValue) {
		XSSFCell cell = ExcelExtension.getCellByAddress(cellAddress, worksheet);
		String cellValue = ExcelExtension.getCellStringValue(cell);
		return summaryValue.compareTo(cellValue) == 0;
	}

	private boolean compareExcelValueAndSummaryValue(XSSFSheet worksheet, String cellAddress, Double summaryValue) {
		XSSFCell cell = ExcelExtension.getCellByAddress(cellAddress, worksheet);
		String cellValue = ExcelExtension.getCellStringValue(cell);
		if (StringExtension.isDouble(cellValue))
			return (Double.parseDouble(cellValue) == summaryValue);
		else
			return summaryValue == null;
	}

	private String getCellValue(XSSFSheet worksheet, String cellAddress) {
		XSSFCell cell = ExcelExtension.getCellByAddress(cellAddress, worksheet);
		return ExcelExtension.getCellStringValue(cell);
	}

	private Map<String, Set<String>> getImpactedProgramListByAPI(TCComponent[] tcComponentList) {
		Map<String, Set<String>> returnData = new HashMap<String, Set<String>>();
		try {
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Getting impacted program...", IProgressMonitor.UNKNOWN);

					Set<String> uidList = new HashSet<String>();
					int i = 0;
					int total = 0;
					monitor.subTask("Processed: " + total + "/" + tcComponentList.length + " parts");
					for (TCComponent part : tcComponentList) {
						i++;
						if (i % maxPartInCall == 0 || i == tcComponentList.length) {
							total += uidList.size();
							try {
								callAPI(returnData, uidList);
							} catch (Exception e) {
								e.printStackTrace();
							}
							monitor.subTask("Processed: " + total + "/" + tcComponentList.length + " parts");
							i = 0;
							uidList = new HashSet<String>();
						}
						uidList.add(part.getUid());
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnData;
	}

	private void callAPI(Map<String, Set<String>> returnData, Set<String> uidList) throws Exception {
		URL url = new URL("http://10.128.11.181:8080/hphongapi/engineering/impactedProgram_Get?" + "uid=" + String.join(",", uidList));
//		URL url = new URL("http://localhost:9669/engineering/impactedProgram_Get?" + "uid=" + String.join(",", uidList));
		HttpURLConnection httpConn = null;
		httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setRequestMethod("GET");
		httpConn.setRequestProperty("Accept", "application/json");
		httpConn.setConnectTimeout(60000);
		if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new RuntimeException("Failed : HTTP Error code : " + httpConn.getResponseCode());
		}
		InputStreamReader in = new InputStreamReader(httpConn.getInputStream());
		BufferedReader br = new BufferedReader(in);

		StringBuilder sbt = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
			sbt.append(output);
		}
		JSONObject json = new JSONObject(sbt.toString());
		String errorCode = json.getString("errorCode");
		if (errorCode.compareToIgnoreCase("00") == 0) {
			JSONArray dataListJson = json.getJSONArray("dataList");
			if (dataListJson != null) {
				for (Object object : dataListJson) {
					if (object instanceof JSONObject) {
						String partNo = ((JSONObject) object).getString("partNo");
						Set<String> programList = new HashSet<String>();
						JSONArray programListJSON = ((JSONObject) object).getJSONArray("programList");
						for (Object program : programListJSON) {
							programList.add(program.toString());
						}
						returnData.put(partNo, programList);
					}
				}
			}
		}
	}

	private File getECRExcelFile() {
		File excelFile = null;
		TCComponentDataset excel = null;
		try {
			String ecrDocumentName = getNameECRDocument();
			TCComponent[] datasets = selectedObject.getRelatedComponents("CMReferences");
			for (TCComponent dataset : datasets) {
				if (dataset.getType().equals("MSExcelX") && ecrDocumentName.compareTo(dataset.toString()) == 0) {
					excel = (TCComponentDataset) dataset;
					break;
				}
			}

			if (excel != null) {
				TCComponent[] namedRef = excel.getNamedReferences();
				TCComponentTcFile file = (TCComponentTcFile) namedRef[0];
				excelFile = file.getFmsFile();
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return excelFile;
	}

	private String getNameECRDocument() {
		String output = "ECR_Document_";
		try {
			output += selectedObject.getProperty("item_id") + "_" + selectedObject.getProperty("item_revision_id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	private Double getDecimal(String input) {
		if (StringExtension.isDouble(input))
			return Double.parseDouble(input);
		return null;
	}

	private boolean checkBooleanValue(String input) {
		if (input.compareToIgnoreCase("True") == 0 || input.compareToIgnoreCase("1") == 0 || input.compareToIgnoreCase("Yes") == 0)
			return true;
		return false;
	}
}
