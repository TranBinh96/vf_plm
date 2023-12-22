package com.teamcenter.vinfast.escooter.engineering.workflow;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.ResourceManager;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.common.actions.CheckoutEditAction;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.admin.pal.ALAssistant_Constant;
import com.teamcenter.vinfast.escooter.sorprocess.model.TaskListModel;
import com.teamcenter.vinfast.model.ALModel;
import com.teamcenter.vinfast.subdialog.SearchGroupMember_Dialog;
import com.teamcenter.vinfast.utils.IValidator;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.teamcenter.vinfast.utils.ValidationRouter;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;
import com.vinfast.sc.utilities.PropertyDefines;

import vfplm.soa.common.define.Constant;

public class EScooterSORProcessTrigger_Handler extends AbstractHandler {
	private TCSession session = null;
	private DataManagementService dmService = null;
	private EScooterSORProcessTrigger_Dialog dlg = null;
	private LinkedList<TCComponentBOMLine> targetObjects = null;
	private TCComponent sorDocument = null;
	private boolean isValidate = true;
	private LinkedHashMap<String, String> programValidateMapping = null;
	private LinkedHashMap<String, String> programDataForm = null;
	private LinkedHashMap<String, LinkedHashMap<String, ALModel>> programMapping = null;
	private ALModel assignmentListSelected = null;
	private final static String[] ALLOWED_OBJ_TYPES = { "VF3_Scooter_partRevision" };
	private static String WORKPROCESS_TEMPLACE = "EScooter_SOR_Release_Process";
	private LinkedList<String> bomInfoTemplate = new LinkedList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("");
			add("VL5_pos_id");
			add("awb0BomLineItemId");
			add("bl_item_object_name");
			add("bl_uom");
			add("bl_item_vf4_item_make_buy");
			add("VL5_purchase_lvl_vf");
			add("VL5_module_group");
			add("VL5_main_module");
			add("VL5_module_name");
		}
	};

	private ProgressMonitorDialog progressMonitorDialog = null;
	private String triggerMessage = "";

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			sorDocument = (TCComponent) targetComp[0];

			String docType = TCExtension.GetPropertyRealValue(sorDocument, "vf3_doc_type");
			if (docType.compareTo("ESOR") != 0) {
				MessageBox.post("Please Select ESOR Document Revision.", "Error", MessageBox.ERROR);
				return null;
			}

			if (!TCExtension.checkPermissionAccess(sorDocument, "WRITE", session)) {
				MessageBox.post("You don't have permission access to this SOR.", "Warning", MessageBox.WARNING);
				return null;
			}

			targetObjects = new LinkedList<TCComponentBOMLine>();
			LinkedHashMap<String, String> sourceProgram = TCExtension.GetLovValueAndDisplay("VF4_Sourcing_Car_Program");
			programValidateMapping = new LinkedHashMap<String, String>();
			programDataForm = new LinkedHashMap<String, String>();

			String userGroup = session.getCurrentGroup().toString();
			String[] preValues = TCExtension.GetPreferenceValues("VF_ESCOOTER_SOR_PROCESS_SETTING", session);
			if (preValues != null && preValues.length > 0) {
				for (String value : preValues) {
					String[] str = value.split("\\=");
					if (str.length >= 3) {
						if (Arrays.asList(str[2].split(";")).contains(userGroup)) {
							programValidateMapping.put(str[0], str[1]);
							if (sourceProgram.containsKey(str[0]))
								programDataForm.put(str[0], sourceProgram.get(str[0]));
						}
					}
				}
			}

			programMapping = TCExtension.getScooterProgramMapping(WORKPROCESS_TEMPLACE, programDataForm.keySet(), true, session);

			dlg = new EScooterSORProcessTrigger_Dialog(new Shell());
			dlg.create();

			dlg.txtWorkflow.setText(WORKPROCESS_TEMPLACE);
			dlg.txtID.setText(sorDocument.toString());

			dlg.lblAllowedObjectType.setText("Allowed Object Type: " + String.join(",", Arrays.asList(EScooterSORProcessTrigger_Handler.ALLOWED_OBJ_TYPES)));
			StringExtension.UpdateValueTextCombobox(dlg.cbProgram, TCExtension.SortingLOV(programDataForm));
			dlg.cbProgram.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					resetAction();
					dlg.cbModule.removeAll();
					String program = (String) dlg.cbProgram.getData(dlg.cbProgram.getText());
					LinkedHashMap<String, ALModel> moduleMaping = programMapping.get(program);
					if (moduleMaping != null)
						dlg.cbModule.setItems(moduleMaping.keySet().toArray(new String[0]));
				}
			});

			dlg.cbModule.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					resetAction();
					String program = (String) dlg.cbProgram.getData(dlg.cbProgram.getText());
					String module = dlg.cbModule.getText();
					assignmentListSelected = programMapping.get(program).getOrDefault(module, null);

					dlg.treeAssignList.removeAll();
					if (assignmentListSelected != null) {
						for (Map.Entry<TaskListModel, Set<TCComponentGroupMember>> gateMappingEntrySet : assignmentListSelected.getReviewGateList().entrySet()) {
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
								}
							}
						}

						for (final TreeItem item : dlg.treeAssignList.getItems()) {
							item.setExpanded(true);
						}
					}
				}
			});

			dlg.treeAssignList.addListener(SWT.MouseDoubleClick, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					final TreeItem treeSelected = dlg.treeAssignList.getSelection()[0];
					if (treeSelected.getParentItem() == null) {
						return;
					}

					String gate = treeSelected.getParentItem().getText();
					TCComponentGroupMember reviewerSelected = (TCComponentGroupMember) treeSelected.getData();

					SearchGroupMember_Dialog searchDlg = new SearchGroupMember_Dialog(dlg.getShell(), "", "", null);
					searchDlg.open();
					Button btnOK = searchDlg.getOKButton();
					btnOK.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							int index = searchDlg.tblSearch.getSelectionIndex();
							TCComponent newMember = searchDlg.itemSearch.get(index);
							if (newMember instanceof TCComponentGroupMember) {
								final TreeItem treeSelected = dlg.treeAssignList.getSelection()[0];
								treeSelected.setText(newMember.toString());
								treeSelected.setData(newMember);

								assignmentListSelected.replaceReviewer(gate, reviewerSelected, (TCComponentGroupMember) newMember);
							}
							searchDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnAddBomInfo.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					resetAction();
					try {
						addBomline();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			dlg.btnRemoveBomInfo.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					resetAction();
					removeBomline();
				}
			});

			dlg.btnUpdateBomInfo.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					resetAction();
					updateBomline();
				}
			});

			dlg.ckbCheckAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					for (TableItem item : dlg.tblBom.getItems()) {
						item.setChecked(dlg.ckbCheckAll.getSelection());
					}
				}
			});

			dlg.btnValidate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					updateBomline();

					String validateMess = submitValidation();
					if (validateMess.isEmpty()) {
						dlg.tabFolder.setSelection(2);
						dlg.setMessage("Validate success. You can trigger process.", false);
					} else {
						dlg.setMessage(validateMess, true);
					}
					dlg.btnAccept.setEnabled(validateMess.isEmpty());
				}
			});

			dlg.btnAccept.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					startProcess();
				}
			});

			dlg.btnAccept.setEnabled(false);

			dlg.open();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void resetAction() {
		dlg.btnAccept.setEnabled(false);
		dlg.btnValidate.setEnabled(true);
	}

	private void updateBomline() {
		try {
			dlg.tblBom.removeAll();
			for (TCComponent bomline : targetObjects) {
				LinkedList<String> tableValue = new LinkedList<>();

				for (String property : bomInfoTemplate) {
					if (!property.isEmpty()) {
						tableValue.add(bomline.getPropertyDisplayableValue(property));
					} else {
						tableValue.add("");
					}
				}
				TableItem item = new TableItem(dlg.tblBom, SWT.NONE);
				item.setText(tableValue.toArray(new String[0]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addBomline() throws Exception {
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComp.length > 0) {
			for (InterfaceAIFComponent comp : targetComp) {
				if (comp instanceof TCComponentBOMLine) {
					TCComponentBOMLine bomline = (TCComponentBOMLine) comp;

					String type = bomline.getItemRevision().getType();
					if (!Arrays.asList(EScooterSORProcessTrigger_Handler.ALLOWED_OBJ_TYPES).contains(type))
						continue;

					String partNo = bomline.getPropertyDisplayableValue("bl_item_item_id");
					boolean check = false;
					for (TCComponent targetBomline : targetObjects) {
						String partNumber = targetBomline.getPropertyDisplayableValue("bl_item_item_id");
						if (partNumber.compareTo(partNo) == 0)
							check = true;
					}

					if (check)
						continue;

					LinkedList<String> tableValue = new LinkedList<>();
					for (String property : bomInfoTemplate) {
						if (!property.isEmpty()) {
							tableValue.add(bomline.getPropertyDisplayableValue(property));
						} else {
							tableValue.add("");
						}
					}
					TableItem item = new TableItem(dlg.tblBom, SWT.NONE);
					item.setText(tableValue.toArray(new String[0]));
					targetObjects.add(bomline);
				}
			}
		}
	}

	private void removeBomline() {
		int totalDel = 0;
		for (TableItem tableItem : dlg.tblBom.getItems()) {
			if (tableItem.getChecked()) {
				totalDel++;
			}
		}
		for (int j = 0; j < totalDel; j++) {
			int i = 0;
			for (TableItem tableItem : dlg.tblBom.getItems()) {
				if (tableItem.getChecked()) {
					totalDel++;
					dlg.tblBom.remove(i);
					targetObjects.remove(i);
					break;
				}
				i++;
			}
		}

		dlg.tblBom.setFocus();
		dlg.tblBom.selectAll();
	}

	private String submitValidation() {
		if (dlg.txtID.getText().isEmpty() || dlg.txtWorkflow.getText().isEmpty() || dlg.cbModule.getText().isEmpty() || dlg.cbProgram.getText().isEmpty())
			return "Please input all required information.";

		if (targetObjects == null || targetObjects.size() == 0) {
			dlg.tabFolder.setSelection(0);
			return "Please select bomline to trigger.";
		}

		validateObjects();
		if (!isValidate) {
			dlg.tabFolder.setSelection(1);
			return "Data not valid. Please view Validate tab.";
		}

		return "";
	}

	private void validateObjects() {
		try {
			StringBuffer validationResultText = new StringBuffer();
			String program = (String) dlg.cbProgram.getData(dlg.cbProgram.getText());
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Validating data...", IProgressMonitor.UNKNOWN);

					try {
						isValidate = true;
						Map<TCComponent, List<ValidationResult>> bomlineAndValidationResult = new LinkedHashMap<TCComponent, List<ValidationResult>>();
						List<ValidationResult> summaryValidate = new LinkedList<ValidationResult>();
						summaryValidate.add(validateSummaryMakeBuy());
						summaryValidate.add(validateSummaryPurchaseLevel());
						bomlineAndValidationResult.put(sorDocument, summaryValidate);

						for (TCComponent partRevision : targetObjects) {
							IValidator validator = ValidationRouter.getValidator(program, programValidateMapping, false);
							List<ValidationResult> validateResults = validator.validate(partRevision);
							bomlineAndValidationResult.put(partRevision, validateResults);
						}

						validationResultText.append("<html style=\"padding: 0px;\">");
						validationResultText.append("<style> table, tr, td {border: 1px solid;}table {width: 100%;border-collapse: collapse;table-layout: fixed;}td {height: 20px;word-wrap:break-word;}</style>");
						for (Entry<TCComponent, List<ValidationResult>> partsAndValidationResult : bomlineAndValidationResult.entrySet()) {
							List<ValidationResult> validationResults = partsAndValidationResult.getValue();
							validationResultText.append(ValidationResult.createHtmlValidation(partsAndValidationResult) + "</br>");
							boolean checkPassed = ValidationResult.checkValidationResults(validationResults);
							if (checkPassed == false) {
								isValidate = false;
							}
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

	private void startProcess() {
		try {
			addPart2SOR();
			createdSORTable();

			triggerMessage = "";
			String program = dlg.cbProgram.getText();
			String module = dlg.cbModule.getText();
			String processName = program + " - " + module + " - " + sorDocument.toString();
			String processDesc = program + " - " + module + " - " + sorDocument.toString();

			LinkedList<String> keys = new LinkedList<>();
			keys.add(sorDocument.getUid());
			TCComponent[] currSolutionItems = sorDocument.getRelatedComponents("EC_solution_item_rel");
			if (currSolutionItems != null) {
				for (TCComponent solutionItem : currSolutionItems) {
					keys.add(solutionItem.getUid());
				}
			}

			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Trigger processing...", IProgressMonitor.UNKNOWN);

					try {
						monitor.subTask("Trigger SOR Process...");
						TriggerProcess trigger = new TriggerProcess(session);
						trigger.setWorkProcessTemplace(WORKPROCESS_TEMPLACE);
						trigger.setWorkProcessName(processName);
						trigger.setWorkProcessDesc(processDesc);
						if (assignmentListSelected != null)
							trigger.setAssignmentList(assignmentListSelected.getGateMapping());
						trigger.setWorkProcessAttachment(keys.toArray(new String[0]));
						triggerMessage = trigger.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});

			if (!triggerMessage.isEmpty())
				throw new Exception(triggerMessage);

			dlg.setMessage("Process trigger success.", false);
			dlg.btnAccept.setEnabled(false);
			dlg.btnValidate.setEnabled(false);
		} catch (Exception e) {
			dlg.setMessage(e.getMessage(), true);
		}
	}

	private boolean addPart2SOR() {
		try {
			TCComponent[] currSolutionItems = sorDocument.getRelatedComponents("EC_solution_item_rel");
			if (currSolutionItems != null && currSolutionItems.length > 0) {
				sorDocument.remove("EC_solution_item_rel", currSolutionItems);
			}

			LinkedList<TCComponent> partRevList = new LinkedList<>();
			for (TCComponent targetItem : targetObjects) {
				if (targetItem instanceof TCComponentBOMLine)
					partRevList.add(((TCComponentBOMLine) targetItem).getItemRevision());
				else
					partRevList.add(targetItem);
			}

			sorDocument.setRelated("EC_solution_item_rel", partRevList.toArray(new TCComponent[0]));
			sorDocument.setProperty("vf4_car_program", (String) dlg.cbProgram.getData(dlg.cbProgram.getText()));
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
		}
		return false;
	}

	private void createdSORTable() {
		try {
			LinkedList<CreateIn> createInputs = new LinkedList<CreateIn>();
			for (TCComponent compo : targetObjects) {
				CreateIn createInput = new CreateIn();
				createInput.data.boName = "VF4_SOR_part_info";

				TCComponentItemRevision partRevision = null;
				String uomValue = "";
				String mainModule = "";
				String moduleGroup = "";
				String moduleName = "";
				String purchaseLevel = "";
				if (compo instanceof TCComponentBOMLine) {
					uomValue = ((TCComponentBOMLine) compo).getItem().getPropertyDisplayableValue("uom_tag");
					partRevision = ((TCComponentBOMLine) compo).getItemRevision();
					mainModule = compo.getProperty(Constant.VFBOMLINE.MAIN_MODULE_ENGLISH);
					moduleGroup = compo.getProperty(Constant.VFBOMLINE.MODULE_GROUP_ENGLISH);
					moduleName = compo.getProperty(Constant.VFBOMLINE.MODULE_ENGLISH);
					purchaseLevel = compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF);
				} else {
					uomValue = ((TCComponentItemRevision) compo).getItem().getPropertyDisplayableValue("uom_tag");
					partRevision = (TCComponentItemRevision) compo;
					purchaseLevel = "P";
				}
				TCComponent uomObj = TCExtension.GetUOMItem(uomValue);

				createInput.data.tagProps.put("vf4_design_part", partRevision);
				createInput.data.tagProps.put("vf4_uom", (TCComponent) uomObj);
				createInput.data.stringProps.put("vf4_main_module", mainModule);
				createInput.data.stringProps.put("vf4_module_group", moduleGroup);
				createInput.data.stringProps.put("vf4_module_name", moduleName);
				createInput.data.stringProps.put("vf4_purchase_level", purchaseLevel);

				createInputs.add(createInput);
			}

			CreateResponse response = dmService.createObjects(createInputs.toArray(new CreateIn[createInputs.size()]));
			if (response.serviceData.sizeOfPartialErrors() > 0)
				throw new Exception("There are some errors while processing, please contact to your IT Service Desk. " + TCExtension.hanlderServiceData(response.serviceData));

			List<TCComponent> newSORTable = new LinkedList<TCComponent>();
			for (CreateOut output : response.output) {
				newSORTable.addAll(Arrays.asList(output.objects));
			}

			sorDocument.setRelated("vf4_SOR_part_info", newSORTable.toArray(new TCComponent[0]));
			sorDocument.refresh();
			if (sorDocument.isCheckedOut() == false && sorDocument.okToCheckout()) {
				CheckoutEditAction checkOutAction = new CheckoutEditAction();
				checkOutAction.run();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ValidationResult validateSummaryMakeBuy() {
		ValidationResult itemValidate = null;

		String currentMakeBuy = "";
		try {
			for (TCComponentBOMLine bomLine : targetObjects) {
				String makeBuy = bomLine.getPropertyDisplayableValue("bl_item_vf4_item_make_buy");
				if (currentMakeBuy.isEmpty())
					currentMakeBuy = makeBuy;
				else {
					if (currentMakeBuy.compareTo(makeBuy) != 0)
						itemValidate = new ValidationResult("Make/Buy", false, "Cannot add MAKE part and BUY part in same SOR process.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (itemValidate == null)
			itemValidate = new ValidationResult("Part Make/Buy", true, ValidationResult.passImage);

		return itemValidate;
	}

	private ValidationResult validateSummaryPurchaseLevel() {
		ValidationResult itemValidate = null;

		Set<String> purchaseLevelList = new HashSet<String>();

		try {
			for (TCComponentBOMLine bomLine : targetObjects) {
				purchaseLevelList.add(bomLine.getPropertyDisplayableValue("VL5_purchase_lvl_vf"));
			}

			if (purchaseLevelList.contains(PropertyDefines.PUR_LEVEL_H)) {
				if (purchaseLevelList.size() > 1)
					itemValidate = new ValidationResult("Purchase Level", false, "Cannot add H and other purchase level in same SOR process.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (itemValidate == null)
			itemValidate = new ValidationResult("Purchase Level", true, ValidationResult.passImage);

		return itemValidate;
	}
}
