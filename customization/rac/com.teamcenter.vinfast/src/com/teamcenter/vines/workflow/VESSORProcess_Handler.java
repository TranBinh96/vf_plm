package com.teamcenter.vines.workflow;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
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
import org.eclipse.wb.swt.SWTResourceManager;

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

import vfplm.soa.common.define.Constant;

public class VESSORProcess_Handler extends AbstractHandler {
	private TCSession session = null;
	private DataManagementService dmService = null;
	private VESSORProcess_Dialog dlg = null;
	private LinkedList<TCComponent> targetObjects = null;
	private TCComponent sorDocument = null;
	private boolean isValidate = true;
	private LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ALModel>>> workprocessMapping;
	private ALModel assignmentListSelected = null;
	private LinkedHashMap<String, String> programValidateMapping;
	private LinkedHashMap<String, String> programDataForm;
	private static String CL_GATE = "Purchase Review SOR Document";
//	private final static String PURCHASE_GROUP = "3_IN_1_PDU_DCDC_OBC.Direct Pur.Purchase.VINFAST;BATTERY_PACK.Direct Pur.Purchase.VINFAST;BIW-DC.Direct Pur.Purchase.VINFAST;EE.Direct Pur.Purchase.VINFAST;EXT-AFS.Direct Pur.Purchase.VINFAST;INT.Direct Pur.Purchase.VINFAST;PWT-CHS.Direct Pur.Purchase.VINFAST;SEATS.Direct Pur.Purchase.VINFAST;SEATS_AND_RESTRAINTS.Direct Pur.Purchase.VINFAST;STANDARD.Direct Pur.Purchase.VINFAST;Pack Pin.Purchase.VINES;Purchase.VINES";
	private final static String[] CELL_ALLOWED_OBJ_TYPES = { "VF4_Cell_DesignRevision" };
	private final static String[] BP_ALLOWED_OBJ_TYPES = { "VF4_BP_DesignRevision" };
	private final static String[] ME_ALLOWED_OBJ_TYPES = { "VF4_VES_ME_PartRevision" };
	private List<String> allowedObjectType = null;
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

	private ProgressMonitorDialog progressMonitorDialog = null;
	private String triggerMessage = "";
	private TCComponentGroupMember clUser;

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

			String[] workprocessDataForm = new String[] { "VINES_SOR_RELEASE_PROCESS" };
			workprocessMapping = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ALModel>>>();
			targetObjects = new LinkedList<>();
			LinkedHashMap<String, String> sourceProgram = TCExtension.GetLovValueAndDisplay("VF4_Sourcing_Car_Program");
			programDataForm = new LinkedHashMap<String, String>();

			String userGroup = session.getCurrentGroup().toString();
			String[] preValues = TCExtension.GetPreferenceValues("VES_SOR_PROCESS_SETTING", session);
			if (preValues != null && preValues.length > 0) {
				programValidateMapping = new LinkedHashMap<String, String>();
				for (String value : preValues) {
					String[] str = value.split("==");
					if (str.length >= 3) {
						if (Arrays.asList(str[2].split(";")).contains(userGroup)) {
							programValidateMapping.put(str[0], str[1]);
							if (sourceProgram.containsKey(str[0]))
								programDataForm.put(str[0], sourceProgram.get(str[0]));
						}
					}
				}
			}

			TCExtension.SortingLOV(programDataForm);

			dlg = new VESSORProcess_Dialog(new Shell());
			dlg.create();

			dlg.txtID.setText(sorDocument.toString());

			dlg.cbWorkflow.setItems(workprocessDataForm);
			dlg.cbWorkflow.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					removeAllBomline();
					dlg.cbProgram.removeAll();
					String workprocess = dlg.cbWorkflow.getText();
					loadALByWorkflow(workprocess);

					StringExtension.UpdateValueTextCombobox(dlg.cbProgram, programDataForm);

					updateAllowedObjectType();
				}
			});

			dlg.cbProgram.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					resetAction();
					dlg.cbModule.removeAll();
					String workprocess = dlg.cbWorkflow.getText();
					if (workprocessMapping.containsKey(workprocess)) {
						LinkedHashMap<String, LinkedHashMap<String, ALModel>> programMapping = workprocessMapping.get(workprocess);
						String program = (String) dlg.cbProgram.getData(dlg.cbProgram.getText());
						if (programMapping.containsKey(program)) {
							String[] moduleDataForm = programMapping.get(program).keySet().toArray(new String[0]);
							Arrays.sort(moduleDataForm);
							dlg.cbModule.setItems(moduleDataForm);
						}
					}
				}
			});

			dlg.cbModule.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					resetAction();

					String workflow = dlg.cbWorkflow.getText();
					String program = (String) dlg.cbProgram.getData(dlg.cbProgram.getText());
					String module = dlg.cbModule.getText();
					assignmentListSelected = workprocessMapping.get(workflow).get(program).getOrDefault(module, null);

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
									int action = -1;// ALAssistant_Extension.getActionOfMember(resourcesTask, reviewer);
									if (action == ALAssistant_Constant.AL_ACTION_REVIEW_REQUIRED || action == ALAssistant_Constant.AL_ACTION_ACKNOW_REQUIRED)
										task.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));

									if (task.getText().compareTo(CL_GATE) == 0) {
										clUser = reviewer;
										dlg.txtCL.setText(reviewer.toString());
									}
								}
							}
						}

						for (final TreeItem item : dlg.treeAssignList.getItems()) {
							item.setExpanded(true);
						}
					}
				}
			});

			dlg.btnSelectCL.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					SearchGroupMember_Dialog searchDlg = new SearchGroupMember_Dialog(dlg.getShell(), "*", "CL", false);
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							dlg.txtCL.setText("");

							int index = searchDlg.tblSearch.getSelectionIndex();
							if (searchDlg.itemSearch.get(index) instanceof TCComponentGroupMember) {
								clUser = (TCComponentGroupMember) searchDlg.itemSearch.get(index);

								try {
									dlg.txtCL.setText(clUser.toString());

									for (TreeItem treeItem : dlg.treeAssignList.getItems()) {
										String gate = treeItem.getText();
										if (gate.compareTo(CL_GATE) == 0) {
											if (treeItem.getItemCount() == 0) {
												TreeItem performer = new TreeItem(treeItem, SWT.NONE);
												performer.setData(clUser);
												performer.setText(clUser.toString());
												performer.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/user_16.png"));
											} else {
												TCComponentGroupMember reviewerSelected = (TCComponentGroupMember) treeItem.getItem(0).getData();

												treeItem.getItem(0).setText(clUser.toString());
												treeItem.getItem(0).setData(clUser);

												if (assignmentListSelected != null)
													assignmentListSelected.replaceReviewer(gate, reviewerSelected, clUser);
											}
										}
									}
								} catch (Exception e2) {
									e2.toString();
								}
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
					updateAllowedObjectType();
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

	private void updateAllowedObjectType() {
		String documentType = sorDocument.getType();
		if (dlg.ckbRawMaterial.getSelection()) {
			allowedObjectType = Arrays.asList(ME_ALLOWED_OBJ_TYPES);
		} else {
			if (documentType.compareTo("VF3_VES_CSOR_docRevision") == 0) {
				allowedObjectType = Arrays.asList(CELL_ALLOWED_OBJ_TYPES);
			} else {
				allowedObjectType = Arrays.asList(BP_ALLOWED_OBJ_TYPES);
			}
		}

		dlg.lblAllowedObjectType.setText("Allowed Object Type: " + String.join(",", allowedObjectType));
	}

	private void loadALByWorkflow(String workprocessName) {
		if (workprocessName.isEmpty() || workprocessMapping.containsKey(workprocessName))
			return;

		try {
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Program loading...", IProgressMonitor.UNKNOWN);
					try {
						workprocessMapping.put(workprocessName, TCExtension.getProgramMapping(workprocessName, programDataForm.keySet(), session));
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
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
					if (!allowedObjectType.contains(type))
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
				} else if (comp instanceof TCComponentItemRevision) {
					if (!dlg.ckbRawMaterial.getSelection())
						return;

					TCComponentItemRevision partRevision = (TCComponentItemRevision) comp;

					String type = partRevision.getType();
					if (!allowedObjectType.contains(type))
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
		if (dlg.txtID.getText().isEmpty() || dlg.cbWorkflow.getText().isEmpty() || dlg.cbModule.getText().isEmpty() || dlg.cbProgram.getText().isEmpty() || dlg.txtCL.getText().isEmpty())
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
			boolean isMePart = dlg.ckbRawMaterial.getSelection();
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
						for (TCComponent partRevision : targetObjects) {
							IValidator validator = ValidationRouter.getValidator(program, programValidateMapping, isMePart);
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
			String workflowName = dlg.cbWorkflow.getText();
			String program = (String) dlg.cbProgram.getData(dlg.cbProgram.getText());
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
						trigger.setWorkProcessTemplace(workflowName);
						trigger.setWorkProcessName(processName);
						trigger.setWorkProcessDesc(processDesc);
						if (assignmentListSelected != null) {
							trigger.setAssignmentList(assignmentListSelected.getGateMapping());
						}
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
}
