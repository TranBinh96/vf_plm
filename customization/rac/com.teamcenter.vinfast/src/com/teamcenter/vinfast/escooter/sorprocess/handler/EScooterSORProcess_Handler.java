package com.teamcenter.vinfast.escooter.sorprocess.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.event.MenuEvent;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.common.actions.CheckoutEditAction;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2010_09.DataManagement;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.services.rac.workflow._2014_06.Workflow;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffInfo;
import com.teamcenter.services.rac.workflow._2015_07.Workflow.CreateSignoffs;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.escooter.sorprocess.model.AssigmentListModel;
import com.teamcenter.vinfast.escooter.sorprocess.model.ResourcesModel;
import com.teamcenter.vinfast.escooter.sorprocess.model.TaskListModel;
import com.teamcenter.vinfast.escooter.sorprocess.model.WorkflowModel;
import com.teamcenter.vinfast.escooter.sorprocess.view.GroupMemberSearchDialog;
import com.teamcenter.vinfast.escooter.sorprocess.view.NewSORProcessDialog;
import com.teamcenter.vinfast.escooter.sorprocess.view.SORSearchDialog;
import com.teamcenter.vinfast.utils.IValidator;
import com.teamcenter.vinfast.utils.Utils;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.teamcenter.vinfast.utils.ValidationRouter;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.PropertyDefines;

import vfplm.soa.common.define.Constant;
import vfplm.soa.common.define.Constant.VFItem;

public class EScooterSORProcess_Handler extends AbstractHandler {
	private TCSession session = null;
	private DataManagementService dmService = null;
	private NewSORProcessDialog dlg = null;

	private Utils utility = null;
	private TCComponent wf = null;
	private TCComponent specbook = null;
	private String[] program = null;
	private WorkflowModel wfModel = null;
	private TCPreferenceService preService = null;
	private TCComponent[] targetItemRevs = null;
	private TCComponentBOMLine[] targetBomLines = null;
	private LinkedHashMap<String, String> mapGrouptoPrg = null;
	private LinkedHashMap<String, TCComponent> mapPrg2Wf = null;
	private static boolean partValidationRes = true;
	private static String prgRealValue = "";

	private LinkedHashMap<String, TCComponent> prg2ChiefEngineer = null;
	private LinkedHashMap<String, String> lovVF4SourcingCarPrg = new LinkedHashMap<String, String>();
	private final static String[] ALLOWED_OBJ_TYPES = { "VF3_Scooter_partRevision", "VF4_DesignRevision", "VF4_BP_DesignRevision" };
	private LinkedHashMap<String, String> validateMapping = null;

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		EScooterSORProcess_Handler hd = new EScooterSORProcess_Handler();
		try {
			// initialize
			int res = hd.init();
			if (res != 0)
				return null;
		} catch (TCException | NotLoadedException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
		}

		hd.getNewProcessDlg().create();
		hd.getNewProcessDlg().getTxtWf().setEditable(false);
		hd.getNewProcessDlg().getTxtSOR().setEditable(false);
		hd.getNewProcessDlg().getCbPrg().setItems(hd.getProgram());

		hd.getNewProcessDlg().getCbPrg().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				hd.clearCache();
				hd.getNewProcessDlg().resetUI();

				String selectedPrg = hd.getNewProcessDlg().getCbPrg().getItem(hd.getNewProcessDlg().getCbPrg().getSelectionIndex());
				hd.setWf(selectedPrg);

				if (hd.getLovVF4SourcingCarPrg().containsValue(hd.getNewProcessDlg().getCbPrg().getText())) {
					for (final Object entry : hd.getLovVF4SourcingCarPrg().keySet()) {
						if (hd.getLovVF4SourcingCarPrg().get(entry).compareToIgnoreCase(hd.getNewProcessDlg().getCbPrg().getText()) == 0) {
							EScooterSORProcess_Handler.prgRealValue = entry.toString();
							break;
						}
					}
				} else {
					MessageBox.post("Program " + hd.getNewProcessDlg().getCbPrg().getText() + " is not valid in LOV VF4_Sourcing_Car_Program", "ERROR", MessageBox.ERROR);
					return;
				}

				if (hd.getWf() != null) {
					hd.getNewProcessDlg().getTxtWf().setText(hd.getWf().toString());
					WorkflowModel wfModel = new WorkflowModel((TCComponentTaskTemplate) hd.getWf());
					hd.setWfModel(wfModel);
					if (wfModel.getAssignmentListDispNameByProgram(selectedPrg) != null) {
						LinkedHashMap<String, String> moduleMapping = wfModel.getAssignmentListDispNameByProgram(selectedPrg);
						StringExtension.UpdateValueTextCombobox(hd.getNewProcessDlg().getCbModule(), moduleMapping);
					}
				}

				try {
					Map<TCComponent, List<ValidationResult>> bomlineAndValidationResult = new HashMap<TCComponent, List<ValidationResult>>();
					for (TCComponentBOMLine bomline : hd.getTargetBomLines()) {
						IValidator validator = ValidationRouter.getValidator(selectedPrg, hd.getValidateMapping(), false);
						List<ValidationResult> validateResults = validator.validate(bomline);
						bomlineAndValidationResult.put(bomline, validateResults);
					}
					String result = getValidationResult(bomlineAndValidationResult);
					hd.getNewProcessDlg().getBrwValidateRes().setText(result);
				} catch (Exception e3) {
					MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
				}

			}
		});

		// handle module selection
		hd.getNewProcessDlg().getCbModule().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// TODO set assignment list
				String selModule = (String) hd.getNewProcessDlg().getCbModule().getData(hd.getNewProcessDlg().getCbModule().getText()); // hd.getNewProcessDlg().getCbModule().getItem(hd.getNewProcessDlg().getCbModule().getSelectionIndex());
				String selPrg = hd.getNewProcessDlg().getCbPrg().getItem(hd.getNewProcessDlg().getCbPrg().getSelectionIndex());
				AssigmentListModel assList = hd.getWfModel().getAssignmentListByName(selModule);

				// TODO set resource model for task Chief Engineer base on Program
				ResourcesModel chiefRm = assList.getResourceByTaskName("Chief Engineer Review");
				if (chiefRm != null) {
					for (Map.Entry<String, TCComponent> entry : hd.getPrg2ChiefEngineer().entrySet()) {
						String prg = entry.getKey();
						try {
							if (prg.compareToIgnoreCase(selPrg) == 0) {
								TCComponentGroupMember[] gmComp = new TCComponentGroupMember[] { (TCComponentGroupMember) entry.getValue() };
								chiefRm.setGroupMember(gmComp);
								chiefRm.setPerformer(new String[] { gmComp[0].getUser().toString() });
								chiefRm.setUser(new TCComponentUser[] { gmComp[0].getUser() });
								assList.setResourceByTaskName("Chief Engineer Review", chiefRm);
								break;
							}
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
				}

				ResourcesModel[] res = assList.getResources();
				TaskListModel[] taskList = assList.getTaskList();
				hd.getNewProcessDlg().getTreeAssignList().removeAll();
				if (res != null && taskList != null) {
					for (int i = 0; i < taskList.length; i++) {
						TreeItem task = new TreeItem(hd.getNewProcessDlg().getTreeAssignList(), SWT.NONE);
						task.setText(taskList[i].getName());

						for (int j = 0; j < res[i].getUser().length; j++) {
							TreeItem performer = new TreeItem(task, SWT.NONE);
							performer.setText(res[i].getUser()[j].toString());
						}
					}
					for (final TreeItem item : hd.getNewProcessDlg().getTreeAssignList().getItems()) {
						item.setExpanded(true);
					}

					final Menu menu = new Menu(hd.getNewProcessDlg().getTreeAssignList());
					hd.getNewProcessDlg().getTreeAssignList().setMenu(menu);
					menu.addMenuListener(new MenuAdapter() {
						public void menuShown(MenuEvent e) {
							MenuItem[] items = menu.getItems();
							for (int i = 0; i < items.length; i++) {
								items[i].dispose();
							}
							MenuItem newItem = new MenuItem(menu, SWT.NONE);
							newItem.setText("Menu for " + hd.getNewProcessDlg().getTreeAssignList().getSelection()[0].getText());
						}
					});
				}
			}
		});

		// handle add sor event
		hd.getNewProcessDlg().getBtnAddSOR().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					SORSearchDialog searchDlg = new SORSearchDialog(hd.getNewProcessDlg().getShell(), hd.getNewProcessDlg().getShell().getStyle(), "VF3_spec_docRevision;VF4_SOR_docRevision");
					searchDlg.open();
					Button btnOK = searchDlg.getOKButton();

					btnOK.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							Table partTable = searchDlg.getSearchTable();
							TableItem[] items = partTable.getSelection();
							if (searchDlg.getMapSORCompo().containsKey(items[0].getText())) {
								hd.setSpecbook(searchDlg.getMapSORCompo().get(items[0].getText()));
								if (hd.addPart2Specbook()) {
									hd.getNewProcessDlg().getTxtSOR().setText(items[0].getText());
								}

							}
							searchDlg.getShell().dispose();
						}
					});

				} catch (Exception e1) {
					MessageBox.post("Exception: " + e1.toString(), "ERROR", MessageBox.ERROR);
				}
			}
		});

		// handle tree double-click event
		hd.getNewProcessDlg().getTreeAssignList().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				// TODO Auto-generated method stub
				final TreeItem treeSelected = hd.getNewProcessDlg().getTreeAssignList().getSelection()[0];
				if (treeSelected.getParentItem() == null) {
					return;
				}
				int itemCount = treeSelected.getParentItem().indexOf(treeSelected);
				GroupMemberSearchDialog searchDlg = new GroupMemberSearchDialog(hd.getNewProcessDlg().getShell(), hd.getNewProcessDlg().getShell().getStyle());
				searchDlg.open();
				Button btnOK = searchDlg.getOKButton();
				btnOK.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Table partTable = searchDlg.getSearchTable();
						TableItem[] items = partTable.getSelection();
						final TreeItem treeSelected = hd.getNewProcessDlg().getTreeAssignList().getSelection()[0];
						treeSelected.setText(items[0].getText());

						// update resource model
						String targetTask = treeSelected.getParentItem().getText();
						ResourcesModel resModel = hd.getWfModel().getAssignmentListByDesc(hd.getNewProcessDlg().getCbModule().getText()).getResourceByTaskName(targetTask);
						TCComponent grpMemberCompo = searchDlg.getMapGrpMember().get(items[0].getText(3));
						if (grpMemberCompo != null) {
							try {
								resModel.setGroupMemberAt(itemCount, (TCComponentGroupMember) grpMemberCompo);
								TCComponentUser user = ((TCComponentGroupMember) grpMemberCompo).getUser();
								resModel.setUserAt(itemCount, user);
								resModel.setPerformerAt(itemCount, user.toString());
							} catch (TCException e1) {
								e1.printStackTrace();
							}

						}

						searchDlg.getShell().dispose();
					}
				});
			}
		});

		// handle event btn Start
		hd.getNewProcessDlg().getBtnStart().addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (EScooterSORProcess_Handler.partValidationRes == false) {
					MessageBox.post("Part missing required information, please update data...", "ERROR", MessageBox.ERROR);
					return;
				}
				if (hd.getNewProcessDlg().getTxtSOR().getText().isEmpty() || hd.getNewProcessDlg().getTxtWf().getText().isEmpty() || hd.getNewProcessDlg().getCbModule().getText().isEmpty() || hd.getNewProcessDlg().getCbPrg().getText().isEmpty()) {
					MessageBox.post("Program/Workflow/Module/SOR are required information, please input...", "ERROR", MessageBox.ERROR);
					return;
				}
				hd.createNewSORProcess();
			}
		});

		hd.getNewProcessDlg().getShell().pack();
		hd.getNewProcessDlg().open();

		return null;
	}

	private int setMapGrouptoPrg() {
		int res = 0;
		String[] preVal = preService.getStringValues("VF_SOURCING_PROGRAM_2_USER_GROUP");
		if (preVal != null) {
			this.mapGrouptoPrg = new LinkedHashMap<>();
			for (int i = 0; i < preVal.length; i++) {
				this.mapGrouptoPrg.put(preVal[i].split("=")[0], preVal[i].split("=")[1]);
			}
			return res;
		} else {
			System.out.println("[setMapGrouptoPrg] Invalid preference VF_SOURCING_PROGRAM_2_USER_GROUP");
			return res = 1;
		}
	}

	private int init() throws TCException, NotLoadedException {
		int res = 0;
		this.session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		this.preService = session.getPreferenceService();
		this.utility = new Utils();
		this.dlg = new NewSORProcessDialog(new Shell());
		this.prg2ChiefEngineer = new LinkedHashMap<String, TCComponent>();
//		this.lovVF4SourcingCarPrg = Utils.getLovDetailInfo(session, "vf4_car_program", "VF3_spec_docRevision");

		this.validateMapping = ValidationRouter.getProgramValidateMapping(session);
		String[] lovVF4SourcingCarPrg = Utils.getPreferenceValues(session, "LOV_VF4_SOURCING_CAR_PROGRAM");
		if (lovVF4SourcingCarPrg != null) {
			for (int i = 0; i < lovVF4SourcingCarPrg.length; i++) {
				String realName = lovVF4SourcingCarPrg[i].split(";")[0];
				String disName = lovVF4SourcingCarPrg[i].split(";")[1];
				this.lovVF4SourcingCarPrg.put(realName, disName);
			}
		}

		if (this.lovVF4SourcingCarPrg == null || this.lovVF4SourcingCarPrg.size() == 0) {
			return 1;
		}
		res = setMapGrouptoPrg();
		if (res != 0)
			return res;
		// validate group
		if (!isValidGroup(session.getCurrentGroup().toString())) {
			MessageBox.post("Access Denied! Allowed groups are " + Arrays.toString(Arrays.asList(mapGrouptoPrg.keySet().toArray()).toArray(new String[mapGrouptoPrg.values().size()])) + ".\n", "", MessageBox.WARNING);
			return 1;
		} else {
			setProgram(session.getCurrentGroup().toString());
		}
		res = setMapPrg2Wf();
		if (res != 0)
			return res;
		res = setTargetItemRevs();

		// prevent Make and Buy in same SOR
		if (!isSamePartType())
			return 1;

		// get preference VF_Process_Scooter_Program_Assignment
		String[] lstChiefEngId = Utils.getPreferenceValues(session, "VF_Process_Scooter_Program_Assignment");
		for (String line : lstChiefEngId) {
			String prg = line.split("==")[0];
			String grpRoleUserId = line.split("=")[3];
			String group = grpRoleUserId.split("/")[0];
			String role = grpRoleUserId.split("/")[1];
			String userId = grpRoleUserId.split("/")[2];

			TCComponent[] objects = null;
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			queryInput.put("Id", userId);
			queryInput.put("Group", group);
			queryInput.put("Role", role);
			queryInput.put("Group Member Status", "FALSE");
			objects = Query.queryItem(session, queryInput, "__TNH_FindGroupMem");
			if (objects == null || objects.length == 0) {
				MessageBox.post("Cannot find Chief Engineer: " + userId, "ERROR", MessageBox.ERROR);
				return 1;
			}
			this.prg2ChiefEngineer.put(prg, objects[0]);
		}

		if (res != 0)
			return res;
		return res;
	}

	private int setTargetItemRevs() throws TCException, NotLoadedException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();
		int res = 0;
		int counter = 0;
		if (appComps != null && appComps.length > 0) {
			ArrayList<String> uniqueID = new ArrayList<String>();
			this.targetItemRevs = new TCComponent[appComps.length];
			this.targetBomLines = new TCComponentBOMLine[appComps.length];
			List<String> validObjType = Arrays.asList(EScooterSORProcess_Handler.ALLOWED_OBJ_TYPES);
			for (InterfaceAIFComponent appComp : appComps) {
				String partID = "";
				if (appComp == null) {
					MessageBox.post("Invalid target object", "ERROR", MessageBox.ERROR);
					return 1;
				}
				if (appComp instanceof TCComponentBOMLine) {
					this.targetBomLines[counter] = (TCComponentBOMLine) appComp;
					this.targetItemRevs[counter] = this.targetBomLines[counter].getItemRevision();
					partID = this.targetBomLines[counter].getPropertyDisplayableValue("bl_item_item_id");
					if (uniqueID.contains(partID)) {
						MessageBox.post("Duplicated part: " + partID, "ERROR", MessageBox.ERROR);
						return 1;
					}
					if (!validObjType.contains(this.targetItemRevs[counter].getType())) {
						MessageBox.post("This feature is not applicable for part \"" + partID + "\" of type \"" + this.targetItemRevs[counter].getType() + "\"", "Info", MessageBox.INFORMATION);
						return 1;
					}

					counter++;
				} else if (appComp instanceof TCComponentItemRevision) {
					this.targetItemRevs[counter] = (TCComponentItemRevision) appComp;
					partID = this.targetItemRevs[counter].getProperty(Constant.VFWorkspaceObject.ID);
					if (uniqueID.contains(partID)) {
						MessageBox.post("Duplicated part: " + partID, "ERROR", MessageBox.ERROR);
						return 1;
					}
					if (!validObjType.contains(this.targetItemRevs[counter].getType())) {
						MessageBox.post("This feature is not applicable for part \"" + partID + "\" of type \"" + this.targetItemRevs[counter].getType() + "\"", "Info", MessageBox.INFORMATION);
						return 1;
					}
					counter++;
				}
			}
			return res;
		} else {
			System.out.println("[setTargetItemRevs] Invalid target object");
			return 1;
		}
	}

	private boolean isValidGroup(String userGroup) {
		boolean isValidGroup = utility.checkValidGroup(Arrays.asList(mapGrouptoPrg.keySet().toArray()).toArray(new String[mapGrouptoPrg.values().size()]), userGroup);
		if (isValidGroup == false) {
			MessageBox.post("Access Denied! Allowed groups are " + Arrays.toString(Arrays.asList(mapGrouptoPrg.keySet().toArray()).toArray(new String[mapGrouptoPrg.values().size()])) + ".\n", "", MessageBox.WARNING);
			return false;
		}
		return true;
	}

	private void setProgram(String currentGrp) {
		for (String key : mapGrouptoPrg.keySet()) {
			if (currentGrp.contains(key) || currentGrp.compareToIgnoreCase(key) == 0) {
				this.program = mapGrouptoPrg.get(key).split(";");
				break;
			}
		}
	}

	public String[] getProgram() {
		return program;
	}

	private int setMapPrg2Wf() {
		String[] preVal = preService.getStringValues("VF_SOURCING_PROGRAM_2_WF");
		if (preVal == null || preVal.length <= 0) {
			return 1;
		} else {
			Map<String, TCComponent> wfNameAndComps = utility.getAllWorkflowTemplates();
			this.mapPrg2Wf = new LinkedHashMap<String, TCComponent>();
			for (String prWf : preVal) {
				if (prWf.split(";").length != 2) {
					MessageBox.post("Value(s) in preference VF_SOURCING_PROGRAM_2_WF is invalid", "ERROR", MessageBox.ERROR);
					return 1;
				}
				String wf = prWf.split(";")[1].trim();
				String pr = prWf.split(";")[0].trim();

				if (wfNameAndComps.containsKey(wf)) {
					this.mapPrg2Wf.put(pr, wfNameAndComps.get(wf));
				} else {
					System.out.println("[setMapPrg2Wf] Workflow not existed: " + wf);
				}
			}
		}
		return 0;
	}

	public TCComponent getWf() {
		return wf;
	}

	public void setWf(String prg) {
		if (this.mapPrg2Wf.containsKey(prg)) {
			this.wf = this.mapPrg2Wf.get(prg);
		} else {
			this.wf = null;
		}
	}

	public void clearCache() {
		this.program = null;
		this.wf = null;
		this.wfModel = null;
	}

	private String getValidationResult(final Map<TCComponent, List<ValidationResult>> partsAndValidationResults) throws TCException {
		partValidationRes = true;
		String validationResultText = "";
		for (Entry<TCComponent, List<ValidationResult>> partsAndValidationResult : partsAndValidationResults.entrySet()) {
			List<ValidationResult> validationResults = partsAndValidationResult.getValue();
			validationResultText += ValidationResult.createHtmlText(partsAndValidationResult) + "</br></br>";
			boolean checkPassed = ValidationResult.checkValidationResults(validationResults);
			if (checkPassed == false) {
				partValidationRes = false;
			}
		}
		return validationResultText;
	}

	public WorkflowModel getWfModel() {
		return wfModel;
	}

	public void setWfModel(WorkflowModel wfModel) {
		this.wfModel = wfModel;
	}

	public TCSession getSession() {
		return session;
	}

	public TCComponent getSpecbook() {
		return specbook;
	}

	public void setSpecbook(TCComponent specbook) {
		this.specbook = specbook;
	}

	private boolean addPart2Specbook() {
		try {
			TCComponent[] currSolutionItems = this.specbook.getRelatedComponents("EC_solution_item_rel");
			/* step1: remove all current parts */
			if (currSolutionItems != null && currSolutionItems.length > 0) {
				this.specbook.remove("EC_solution_item_rel", currSolutionItems);
			}
			/* step2: add new part */
			if (this.targetItemRevs != null && this.targetItemRevs.length > 0) {
				ArrayList<TCComponent> arrPartRev = new ArrayList<>();
				ArrayList<String> uniqueID = new ArrayList<>();
				for (int i = 0; i < this.targetItemRevs.length; i++) {
					TCComponentItemRevision partRev = null;
					String partID = "";
					if (this.targetItemRevs[i] instanceof TCComponentBOMLine) {
						TCComponentBOMLine aBOMLine = (TCComponentBOMLine) this.targetItemRevs[i];
						partRev = aBOMLine.getItemRevision();
						partID = aBOMLine.getPropertyDisplayableValue("bl_item_item_id");
					} else {
						partID = ((TCComponentItemRevision) this.targetItemRevs[i]).getProperty(Constant.VFWorkspaceObject.ID);
						partRev = (TCComponentItemRevision) this.targetItemRevs[i];
					}

					if (!uniqueID.contains(partID)) {
						arrPartRev.add(partRev);
						uniqueID.add(partID);
					}
				}
				this.specbook.setRelated("EC_solution_item_rel", arrPartRev.toArray(new TCComponent[arrPartRev.size()]));
				this.specbook.setProperty("vf4_car_program", EScooterSORProcess_Handler.prgRealValue);
				return true;
			}
		} catch (TCException | NotLoadedException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
		}
		return false;
	}

	public NewSORProcessDialog getNewProcessDlg() {
		return dlg;
	}

	public void setNewProcessDlg(NewSORProcessDialog newProcessDlg) {
		this.dlg = newProcessDlg;
	}

	public TCComponent[] getTargetItemRevs() {
		return targetItemRevs;
	}

	public TCComponentBOMLine[] getTargetBomLines() {
		return targetBomLines;
	}

	private IStatus startProcess(final ContextData inputData) {
		boolean isCreatedSORTable = isCreatedSORTable(session, specbook, targetBomLines, targetItemRevs);
		if (isCreatedSORTable == false) {
			MessageBox.post("There is an issue while initialing workflow process, please contact to admin.", "ERROR", MessageBox.ERROR);
			return Status.CANCEL_STATUS;
		}
		String programValue = this.dlg.getCbPrg().getText().toString();
		InstanceInfo intInfo = WorkflowService.getService(session).createInstance(true, null, programValue + " - " + this.specbook.toString(), null, programValue + " - " + this.specbook.toString(), inputData);
		ServiceData output = intInfo.serviceData;

		if (output.sizeOfPartialErrors() > 0) {
			MessageBox.post(Utils.HanlderServiceData(output), "ERROR", MessageBox.ERROR);
			return Status.CANCEL_STATUS;
		} else {
			if (output.getCreatedObject(0) != null) {
				try {
					if (!assignPerformer((TCComponentProcess) output.getCreatedObject(0))) {
						try {
							output.getCreatedObject(0).delete();
						} catch (TCException e1) {
							e1.printStackTrace();
						}
						MessageBox.post("There is an issue while assigning assignment list, please re-trigger process", "ERROR", MessageBox.ERROR);
						return Status.CANCEL_STATUS;
					}
					System.out.println("[createWorkflowProcess] New SOR process has been triggered");
				} catch (Exception e) {
					e.printStackTrace();
					try {
						output.getCreatedObject(0).delete();
					} catch (TCException e1) {
						e1.printStackTrace();
					}

					MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
					MessageBox.post("Cannot trigger new process, please try again", "ERROR", MessageBox.ERROR);
					return Status.CANCEL_STATUS;
				}
			}
			return Status.OK_STATUS;
		}
	}

	private boolean assignPerformer(TCComponentProcess process) throws TCException, ServiceException {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String selModule = (String) this.dlg.getCbModule().getData(this.dlg.getCbModule().getText());
		AssigmentListModel asgList = this.wfModel.getAssignmentListByName(selModule);
		TCComponentTask rootTask = ((TCComponentProcess) process).getRootTask();
		TCComponentTask[] subtasks = rootTask.getSubtasks();
		for (int i = 0; i < subtasks.length; i++) {
			if (subtasks[i].getTaskType().compareToIgnoreCase("EPMDoTask") == 0 && asgList.getResourceByTaskName(subtasks[i].getName()) != null) {
				if (asgList.getResourceByTaskName(subtasks[i].getName()).getUser().length > 1) {
					MessageBox.post("Cannot assign 2 performer for Do Task: " + subtasks[i].getName(), "ERROR", MessageBox.ERROR);
				} else {
					if (this.session.getUser().toString().compareToIgnoreCase(asgList.getResourceByTaskName(subtasks[i].getName()).getUser()[0].toString()) != 0) {
						subtasks[i].setResponsibleParty(asgList.getResourceByTaskName(subtasks[i].getName()).getUser()[0]);
					}
				}
			}
			if (subtasks[i].getTaskType().compareToIgnoreCase("EPMReviewTask") == 0 && asgList.getResourceByTaskName(subtasks[i].getName()) != null) {
				TCComponentTask selectSignOffTask = subtasks[i].getSubtasks()[0];
				if (selectSignOffTask.getTaskType().compareToIgnoreCase("EPMSelectSignoffTask") != 0) {
					selectSignOffTask = subtasks[i].getSubtasks()[1];
				}
				System.out.println("[assignPerformer] Processing: " + selectSignOffTask.getName());

				CreateSignoffs createSignoff = new CreateSignoffs();
				createSignoff.task = selectSignOffTask;
				createSignoff.signoffInfo = new CreateSignoffInfo[asgList.getResourceByTaskName(subtasks[i].getName()).getGroupMember().length];
				for (int x = 0; x < asgList.getResourceByTaskName(subtasks[i].getName()).getGroupMember().length; x++) {
					createSignoff.signoffInfo[x] = new CreateSignoffInfo();
					createSignoff.signoffInfo[x].originType = "SOA_EPM_ORIGIN_UNDEFINED";
					createSignoff.signoffInfo[x].signoffAction = "SOA_EPM_Review";
					createSignoff.signoffInfo[x].signoffMember = asgList.getResourceByTaskName(subtasks[i].getName()).getGroupMember()[x];
				}
				ServiceData addSignoffsResponse = WorkflowService.getService(session).addSignoffs(new CreateSignoffs[] { createSignoff });
				if (addSignoffsResponse.sizeOfPartialErrors() > 0) {
					System.out.println("[assignPerformer] Exception: " + Utils.HanlderServiceData(addSignoffsResponse));
					return false;
				}

				// Set adhoc done
				DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
				propInfo.object = selectSignOffTask;
				propInfo.timestamp = Calendar.getInstance();
				propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
				propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
				propInfo.vecNameVal[0].name = "task_result";
				propInfo.vecNameVal[0].values = new String[] { "Completed" };
				DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[] { propInfo }, new String[0]);
				if (setPropertyResponse.data.sizeOfPartialErrors() > 0) {
					System.out.println("[assignPerformer]: " + Utils.HanlderServiceData(setPropertyResponse.data));
					return false;
				}

				// Complete selection signoff task if it started
				if (selectSignOffTask.getTaskState().compareToIgnoreCase("Started") == 0) {
					try {
						Thread.sleep(1000);
						process.refresh();
						subtasks[i].refresh();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Workflow.PerformActionInputInfo paii = new Workflow.PerformActionInputInfo();
					paii.clientId = "complete" + selectSignOffTask.getUid();
					paii.action = "SOA_EPM_complete_action";
					paii.actionableObject = selectSignOffTask;
					paii.propertyNameValues.put("comments", new String[] { "Auto Completed" });
					paii.supportingValue = "SOA_EPM_completed";

					ServiceData sd = WorkflowService.getService(session).performAction3(new Workflow.PerformActionInputInfo[] { paii });
					if (sd.sizeOfPartialErrors() > 0) {
						System.out.println("[assignPerformer]: " + Utils.HanlderServiceData(sd));
						return false;
					}
				}
			}
		}

		return true;
	}

	private void createNewSORProcess() {
		System.out.println(this.dlg.getTxtWf().getText());

		String[] keys = new String[this.targetItemRevs.length + 1];

		keys[0] = this.specbook.getUid();
		for (int count = 0; count < this.targetItemRevs.length; count++) {
			try {
				keys[count + 1] = (this.targetItemRevs[count] instanceof TCComponentBOMLine) ? ((TCComponentBOMLine) this.targetItemRevs[count]).getItemRevision().getUid() : this.targetItemRevs[count].getUid();
			} catch (TCException e) {
				MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
				return;
			}
		}

		int[] attachmentTypes = new int[keys.length];
		Arrays.fill(attachmentTypes, 1);
		final com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData data = new com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData();
		data.attachmentCount = this.targetItemRevs.length;
		data.attachments = keys;
		data.processTemplate = this.dlg.getTxtWf().getText();
		data.attachmentTypes = attachmentTypes;

		IStatus status = startProcess(data);
		if (status == Status.OK_STATUS) {
			getNewProcessDlg().getShell().dispose();
			MessageBox.post("SOR process has been triggererd successfully", "INFO", MessageBox.INFORMATION);
			return;
		}
	}

	public static boolean isCreatedSORTable(final TCSession session, final TCComponent sorItemRev, TCComponentBOMLine[] targetBomLines, TCComponent[] targetItemRevs) {
		// check access write
		TCAccessControlService acl = session.getTCAccessControlService();
		boolean hasWriteAccess = false;
		try {
			hasWriteAccess = acl.checkPrivilege(sorItemRev, "WRITE");
			if (!hasWriteAccess) {
				MessageBox.post("You don't have access write on this SOR item", "WARNING", MessageBox.WARNING);
				return false;
			}

			LinkedList<CreateIn> createInputs = new LinkedList<CreateIn>();
			if (targetBomLines != null && targetBomLines.length > 0 && targetBomLines[0] != null) {
				for (TCComponent compo : targetBomLines) {
					CreateIn createInput = new CreateIn();
					createInput.data.boName = "VF4_SOR_part_info";

					String uomValue = ((TCComponentBOMLine) compo).getItemRevision().getItem().getPropertyDisplayableValue("uom_tag");
					TCComponent uomObj = TCExtension.GetUOMItem(uomValue);

					createInput.data.tagProps.put("vf4_design_part", ((TCComponentBOMLine) compo).getItemRevision());
					createInput.data.tagProps.put("vf4_uom", (TCComponent) uomObj);
					createInput.data.stringProps.put("vf4_main_module", compo.getProperty(Constant.VFBOMLINE.MAIN_MODULE_ENGLISH));
					createInput.data.stringProps.put("vf4_module_group", compo.getProperty(Constant.VFBOMLINE.MODULE_GROUP_ENGLISH));
					createInput.data.stringProps.put("vf4_module_name", compo.getProperty(Constant.VFBOMLINE.MODULE_ENGLISH));
					createInput.data.stringProps.put("vf4_purchase_level", compo.getProperty(Constant.VFBOMLINE.PUR_LEVEL_VF));

					createInputs.add(createInput);
				}
			} else {
				for (TCComponent compo : targetItemRevs) {
					CreateIn createInput = new CreateIn();
					createInput.data.boName = "VF4_SOR_part_info";
					String uomValue = ((TCComponentItemRevision) compo).getItem().getPropertyDisplayableValue("uom_tag");
					TCComponent uomObj = TCExtension.GetUOMItem(uomValue);
					createInput.data.tagProps.put("vf4_design_part", compo);
					createInput.data.tagProps.put("vf4_uom", uomObj);
					createInput.data.stringProps.put("vf4_main_module", "");
					createInput.data.stringProps.put("vf4_module_group", "");
					createInput.data.stringProps.put("vf4_module_name", "");
					createInput.data.stringProps.put("vf4_purchase_level", PropertyDefines.PUR_LEVEL_P);

					createInputs.add(createInput);
				}

			}

			// create table row
			DataManagementService dms = DataManagementService.getService(session);
			CreateResponse response = dms.createObjects(createInputs.toArray(new CreateIn[createInputs.size()]));
			List<TCComponent> newSORTable = new LinkedList<TCComponent>();
			if (response.serviceData.sizeOfPartialErrors() == 0 && response.output.length > 0) {
				for (CreateOut output : response.output) {
					newSORTable.addAll(Arrays.asList(output.objects));
				}
			} else {
				MessageBox.post("There are some errors while processing, please contact to your IT Service Desk.", "Error", MessageBox.ERROR);
				return false;
			}

			// add to sor revision
			sorItemRev.setRelated("vf4_SOR_part_info", newSORTable.toArray(new TCComponent[0]));
			sorItemRev.refresh();
			if (sorItemRev.isCheckedOut() == false && sorItemRev.okToCheckout()) {
				CheckoutEditAction checkOutAction = new CheckoutEditAction();
				checkOutAction.run();
			}
		} catch (TCException | ServiceException | NotLoadedException e) {
			e.printStackTrace();
			MessageBox.post("Exception" + e.toString(), "ERROR", MessageBox.ERROR);
			return false;
		}
		return true;
	}

	public LinkedHashMap<String, String> getLovVF4SourcingCarPrg() {
		return lovVF4SourcingCarPrg;
	}

	private boolean isSamePartType() {
		String makeOrBuy = "";
		for (TCComponent itemRev : this.targetItemRevs) {
			try {
				TCComponentItem item = ((TCComponentItemRevision) itemRev).getItem();
				String partMakeBuy = item.getProperty(VFItem.ATTR_MAKE_BUY);
				if (makeOrBuy.isEmpty()) {
					makeOrBuy = partMakeBuy;
				} else {
					if (makeOrBuy.compareToIgnoreCase(partMakeBuy) != 0) {
						MessageBox.post("Cannot add Make part and Buy part in same SOR process", "WARN", MessageBox.WARNING);
						return false;
					}
				}
			} catch (TCException e) {
				System.out.println("Exception: " + e.toString());
			}
		}
		return true;
	}

	public LinkedHashMap<String, TCComponent> getPrg2ChiefEngineer() {
		return prg2ChiefEngineer;
	}

	public void setPrg2ChiefEngineer(LinkedHashMap<String, TCComponent> prg2ChiefEngineer) {
		this.prg2ChiefEngineer = prg2ChiefEngineer;
	}

	public LinkedHashMap<String, String> getValidateMapping() {
		return validateMapping;
	}
}
