package com.teamcenter.vinfast.sor;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

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
import com.teamcenter.vinfast.subdialog.SearchGroupMember_Dialog;
import com.teamcenter.vinfast.utils.IValidator;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.teamcenter.vinfast.utils.ValidationRouter;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

import vfplm.soa.common.define.Constant;

public class SORProcessTrigger_Handler_Backup extends AbstractHandler {
	private TCSession session = null;
	private DataManagementService dmService = null;
	private SORProcessTrigger_Dialog_Backup dlg = null;
	private LinkedList<TCComponent> targetObjects = null;
	private TCComponent sorDocument = null;
	private boolean isValidate = true;
	private LinkedHashMap<String, String> programValidateMapping = null;
	private final static String[] ALLOWED_OBJ_TYPES = { "VF4_DesignRevision", "VF4_Cell_DesignRevision", "VF4_BP_DesignRevision", "VF4_VES_ME_PartRevision" };
	private final static String PURCHASE_GROUP = "3_IN_1_PDU_DCDC_OBC.Direct Pur.Purchase.VINFAST;BATTERY_PACK.Direct Pur.Purchase.VINFAST;BIW-DC.Direct Pur.Purchase.VINFAST;EE.Direct Pur.Purchase.VINFAST;EXT-AFS.Direct Pur.Purchase.VINFAST;INT.Direct Pur.Purchase.VINFAST;PWT-CHS.Direct Pur.Purchase.VINFAST;SEATS.Direct Pur.Purchase.VINFAST;SEATS_AND_RESTRAINTS.Direct Pur.Purchase.VINFAST;STANDARD.Direct Pur.Purchase.VINFAST";
	private static String WORKPROCESS_TEMPLACE = "VINFAST_SOR_RELEASE_PROCESS";
	private LinkedList<String> bomInfoTemplate = new LinkedList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("");
			add("VL5_pos_id");
			add("awb0BomLineItemId");
			add("bl_item_object_name");
			add("bl_uom");
			add("VL5_purchase_lvl_vf");
			add("VL5_module_group");
			add("VL5_main_module");
			add("VL5_module_name");
		}
	};

	private LinkedList<String> bomInfoTemplate1 = new LinkedList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("");
			add("");
			add("item_id");
			add("object_name");
			add("item.uom_tag");
			add("");
			add("");
			add("");
			add("");
		}
	};

	private LinkedHashMap<String, LinkedHashMap<String, String>> programMapping = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private String triggerMessage = "";
	private TCComponentGroupMember clUSer;
	private TCComponentGroupMember buyerUser;

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			sorDocument = (TCComponent) targetComp[0];

			if (!TCExtension.checkPermissionAccess(sorDocument, "WRITE", session)) {
				MessageBox.post("You don't have permission access to this SOR.", "Warning", MessageBox.WARNING);
				return null;
			}

			targetObjects = new LinkedList<>();
			LinkedHashMap<String, String> sourceProgram = TCExtension.GetLovValueAndDisplay("VF4_Sourcing_Car_Program");
			LinkedHashMap<String, String> sourceProgramDataForm = null;

			String userGroup = session.getCurrentGroup().toString();
			String[] preValues = TCExtension.GetPreferenceValues("VINF_SOR_PROCESS_SETTING", session);
			if (preValues != null && preValues.length > 0) {
				programValidateMapping = new LinkedHashMap<>();
				sourceProgramDataForm = new LinkedHashMap<>();
				for (String value : preValues) {
					String[] str = value.split("==");
					if (str.length >= 3) {
						if (Arrays.asList(str[2].split(";")).contains(userGroup)) {
							programValidateMapping.put(str[0], str[1]);
							if (sourceProgram.containsKey(str[0]))
								sourceProgramDataForm.put(str[0], sourceProgram.get(str[0]));
						}
					}
				}
			}

			programMapping = new LinkedHashMap<>();
			TCComponent[] alList = TCExtension.getALByWorkflow(WORKPROCESS_TEMPLACE, session);
			if (alList != null) {
				for (TCComponent alItem : alList) {
					try {
						String alName = alItem.getProperty("list_name");
						String alDesc = alItem.getProperty("list_desc");
						if (alName.contains("VINFAST_SOR_RELEASE_")) {
							String[] str = alName.replace("VINFAST_SOR_RELEASE_", "").split("_");
							if (str.length >= 2) {
								String program = str[0];
								String module = str[1];
								if (sourceProgramDataForm.keySet().contains(program)) {
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

			if (sourceProgramDataForm == null) {
				MessageBox.post("Access Denied.", "", MessageBox.WARNING);
				return null;
			}

			try {
				buyerUser = null;
				TCComponent buyerProperty = sorDocument.getReferenceProperty("vf4_commodity_buyer1");
				if (buyerProperty instanceof TCComponentGroupMember)
					buyerUser = (TCComponentGroupMember) buyerProperty;

				clUSer = null;
				TCComponent clProperty = sorDocument.getReferenceProperty("vf4_pur_commod_manager1");
				if (clProperty instanceof TCComponentGroupMember)
					clUSer = (TCComponentGroupMember) clProperty;
			} catch (Exception e) {
				e.printStackTrace();
			}

			dlg = new SORProcessTrigger_Dialog_Backup(new Shell());
			dlg.create();

			dlg.txtWorkflow.setText(WORKPROCESS_TEMPLACE);
			dlg.txtID.setText(sorDocument.toString());
			if (clUSer != null)
				dlg.txtCL.setText(clUSer.getUser().toString());
			if (buyerUser != null)
				dlg.txtBuyer.setText(buyerUser.getUser().toString());

			StringExtension.UpdateValueTextCombobox(dlg.cbProgram, TCExtension.SortingLOV(sourceProgramDataForm));
			dlg.cbProgram.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					resetAction();
					String program = (String) dlg.cbProgram.getData(dlg.cbProgram.getText());
					LinkedHashMap<String, String> moduleMaping = programMapping.get(program);
					if (moduleMaping != null)
						StringExtension.UpdateValueTextCombobox(dlg.cbModule, moduleMaping);
				}
			});

			dlg.cbModule.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					resetAction();
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
					try {
						removeBomline();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			dlg.btnUpdateBomInfo.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					resetAction();
					try {
						updateBomline();
					} catch (Exception e) {
						e.printStackTrace();
					}
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

			dlg.ckbRawMaterial.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					removeAllBomline();
				}
			});

			dlg.btnSelectCL.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					SearchGroupMember_Dialog searchDlg = new SearchGroupMember_Dialog(dlg.getShell(), PURCHASE_GROUP, "CL", false);
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							dlg.txtCL.setText("");

							int index = searchDlg.tblSearch.getSelectionIndex();
							if (searchDlg.itemSearch.get(index) instanceof TCComponentGroupMember) {
								clUSer = (TCComponentGroupMember) searchDlg.itemSearch.get(index);

								try {
									TCComponent user = TCExtension.getPersonFromUserID(clUSer.getUserId(), session);
									dlg.txtCL.setText(user.getPropertyDisplayableValue("user_name"));
								} catch (Exception e2) {
									e2.toString();
								}
							}

							searchDlg.getShell().dispose();

							dlg.txtBuyer.setText("");
							buyerUser = null;
						}
					});
				}
			});

			dlg.btnSelectBuyer.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					if (clUSer == null) {
						MessageBox.post("Please select Purchasing Commodity Manager first.", "", MessageBox.WARNING);
						return;
					}

					String group = "";
					try {
						group = clUSer.getGroup().toString();
					} catch (Exception e) {
						e.printStackTrace();
					}

					SearchGroupMember_Dialog searchDlg = new SearchGroupMember_Dialog(dlg.getShell(), group.isEmpty() ? PURCHASE_GROUP : group, "Buyer", false);
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							dlg.txtBuyer.setText("");

							int index = searchDlg.tblSearch.getSelectionIndex();
							if (searchDlg.itemSearch.get(index) instanceof TCComponentGroupMember) {
								buyerUser = (TCComponentGroupMember) searchDlg.itemSearch.get(index);

								try {
									TCComponent person = TCExtension.getPersonFromUserID(buyerUser.getUserId(), session);
									dlg.txtBuyer.setText(person.getPropertyDisplayableValue("user_name"));
								} catch (Exception e2) {
									e2.toString();
								}
							}

							searchDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnValidate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					try {
						updateBomline();
					} catch (Exception e) {
						e.printStackTrace();
					}

					String validateMess = submitValidation();
					if (validateMess.isEmpty()) {
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

	private void updateBomline() throws Exception {
		dlg.tblBom.removeAll();
		for (TCComponent bomline : targetObjects) {
			LinkedList<String> tableValue = new LinkedList<>();
			LinkedList<String> propertyList = null;
			if (dlg.ckbRawMaterial.getSelection())
				propertyList = bomInfoTemplate1;
			else
				propertyList = bomInfoTemplate;

			for (String property : propertyList) {
				if (!property.isEmpty()) {
					tableValue.add(bomline.getPropertyDisplayableValue(property));
				} else {
					tableValue.add("");
				}
			}
			TableItem item = new TableItem(dlg.tblBom, SWT.NONE);
			item.setText(tableValue.toArray(new String[0]));
		}
	}

	private void addBomline() throws Exception {
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComp.length > 0) {
			for (InterfaceAIFComponent comp : targetComp) {
				if (comp instanceof TCComponentBOMLine) {
					if (dlg.ckbRawMaterial.getSelection())
						return;

					TCComponentBOMLine bomline = (TCComponentBOMLine) comp;

					String type = bomline.getItemRevision().getType();
					if (!Arrays.asList(SORProcessTrigger_Handler_Backup.ALLOWED_OBJ_TYPES).contains(type))
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

				if (comp instanceof TCComponentItemRevision) {
					if (!dlg.ckbRawMaterial.getSelection())
						return;

					TCComponentItemRevision partRevision = (TCComponentItemRevision) comp;

					String type = partRevision.getType();
					if (!Arrays.asList(SORProcessTrigger_Handler_Backup.ALLOWED_OBJ_TYPES).contains(type))
						continue;

					String partNo = partRevision.getPropertyDisplayableValue("item_id");
					boolean check = false;
					for (TCComponent targetPartRevision : targetObjects) {
						String partNumber = targetPartRevision.getPropertyDisplayableValue("item_id");
						if (partNumber.compareTo(partNo) == 0)
							check = true;
					}

					if (check)
						continue;

					LinkedList<String> tableValue = new LinkedList<>();
					for (String property : bomInfoTemplate1) {
						if (!property.isEmpty()) {
							if (property.contains(".")) {
								String[] str = property.split("\\.");
								tableValue.add(((TCComponentItemRevision) partRevision).getItem().getPropertyDisplayableValue(str[1]));
							} else {
								tableValue.add(partRevision.getPropertyDisplayableValue(property));
							}
						} else {
							tableValue.add("");
						}
					}
					TableItem item = new TableItem(dlg.tblBom, SWT.NONE);
					item.setText(tableValue.toArray(new String[0]));
					targetObjects.add(partRevision);
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

	private void removeAllBomline() {
		targetObjects.clear();
		dlg.tblBom.removeAll();

		dlg.tblBom.setFocus();
		dlg.tblBom.selectAll();
	}

	private String submitValidation() {
		if (dlg.txtID.getText().isEmpty() || dlg.txtWorkflow.getText().isEmpty() || dlg.cbModule.getText().isEmpty() || dlg.cbProgram.getText().isEmpty() || clUSer == null || buyerUser == null)
			return "Please input all required information.";

		if (targetObjects == null || targetObjects.size() == 0)
			return "Please select bomline to trigger.";

		validateObjects();
		if (!isValidate)
			return "Data not valid. Please view Validate tab.";

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
						for (TCComponent bomline : targetObjects) {
							IValidator validator = ValidationRouter.getValidator(program, programValidateMapping, false);
							List<ValidationResult> validateResults = validator.validate(bomline);
							bomlineAndValidationResult.put(bomline, validateResults);
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
			updateUserSOR();

			triggerMessage = "";
			String program = dlg.cbProgram.getText();
			String module = dlg.cbModule.getText();
			String alName = (String) dlg.cbModule.getData(dlg.cbModule.getText());
			String processName = program + " - " + module + " - " + sorDocument.toString();
			String processDesc = program + " - " + module + " - " + sorDocument.toString();

			LinkedHashMap<String, Set<TCComponentGroupMember>> alList = new LinkedHashMap<String, Set<TCComponentGroupMember>>();
			alList.put("Buyers Review", new HashSet<TCComponentGroupMember>() {
				private static final long serialVersionUID = 1L;
				{
					add(buyerUser);
				}
			});
			alList.put("Commodity Leaders Review", new HashSet<TCComponentGroupMember>() {
				private static final long serialVersionUID = 1L;
				{
					add(clUSer);
				}
			});

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
						trigger.setAlName(alName);
						trigger.setAssignmentList(alList);
						trigger.setWorkProcessAttachment(keys.toArray(new String[0]));
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

			if (!triggerMessage.isEmpty())
				throw new Exception(triggerMessage);

			MessageBox.post("Process initiated.", "Success", MessageBox.INFORMATION);
			dlg.close();
		} catch (Exception e) {
			dlg.setMessage(e.getMessage(), true);
		}
	}

	private void updateUserSOR() {
		try {
			TCComponentGroupMember originalBuyerUser = null;
			TCComponent buyerProperty = sorDocument.getReferenceProperty("vf4_commodity_buyer1");
			if (buyerProperty instanceof TCComponentGroupMember)
				originalBuyerUser = (TCComponentGroupMember) buyerProperty;

			TCComponentGroupMember originalCLUser = null;
			TCComponent clProperty = sorDocument.getReferenceProperty("vf4_pur_commod_manager1");
			if (clProperty instanceof TCComponentGroupMember)
				originalCLUser = (TCComponentGroupMember) clProperty;

			if (originalBuyerUser != buyerUser)
				sorDocument.setReferenceProperty("vf4_commodity_buyer1", buyerUser);

			if (originalCLUser != clUSer)
				sorDocument.setReferenceProperty("vf4_pur_commod_manager1", clUSer);
		} catch (Exception e) {
			e.printStackTrace();
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
}
