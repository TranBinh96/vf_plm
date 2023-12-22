package com.teamcenter.vinfast.general.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.services.rac.core.LOVService;
import com.teamcenter.services.rac.core._2013_05.LOV.InitialLovData;
import com.teamcenter.services.rac.core._2013_05.LOV.LOVInput;
import com.teamcenter.services.rac.core._2013_05.LOV.LOVSearchResults;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.rac.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.subdialog.RequestTranferMMSearchDialg;
import com.teamcenter.vinfast.utils.Utils;

public class RequestTranferMM_Dialog extends AbstractSWTDialog {

	protected TCSession session;
	protected Object result;
	private Table table;
	private Combo programsCbx;
	private Button btnAdd;
	private Button btnRemove;
	private Button btnRequestTransfer;
	boolean isAccess = false;
	boolean isReadOnly = false;
	boolean isFilled = false;
	private Map<String, String> programsDisplayValsAndVals;
	List<TransferingTableItem> transferingTableItems;
	private Label instructionLbl;
	private Set<TCComponent> selectedParts;

	public RequestTranferMM_Dialog(Shell parent, int style, Set<TCComponent> selectedParts) throws Exception {
		super(parent, style);
		setBlockOnOpen(false);

		this.selectedParts = selectedParts;
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();

		programsDisplayValsAndVals = new HashMap<String, String>();

		transferingTableItems = new LinkedList<TransferingTableItem>();
		List<TransferingTableItem> selectedTransferingTableItems = createTableItems(selectedParts);
		transferingTableItems.addAll(selectedTransferingTableItems);
	}

	protected Control createDialogArea(Composite parent) {
		List<String> idList = new ArrayList<String>();

		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gl_shlUpdatePartNumber = new GridLayout(4, false);
		gl_shlUpdatePartNumber.marginHeight = 10;
		gl_shlUpdatePartNumber.marginLeft = 10;
		gl_shlUpdatePartNumber.marginBottom = 10;
		container.setLayout(gl_shlUpdatePartNumber);

		Label partsLbl = new Label(container, SWT.NONE);
		partsLbl.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		// partsLbl.setBackground( SWTResourceManager.getColor( SWT.COLOR_WHITE ) );
		partsLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 2));
		partsLbl.setText("Parts: ");

		createTableColumns(container);

		btnAdd = new Button(container, SWT.PUSH);
		btnAdd.setImage(new Image(null,
				RequestTranferMM_Dialog.class.getClassLoader().getResourceAsStream("icons/edit_16.png")));
		btnAdd.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		btnAdd.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		new Label(container, SWT.NONE);

		btnAdd.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {

				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						buttonAddHandler(idList);
					}

				});
			}
		});
		btnAdd.setVisible(false);

		btnRemove = new Button(container, SWT.PUSH);
		btnRemove.setImage(new Image(null,
				RequestTranferMM_Dialog.class.getClassLoader().getResourceAsStream("icons/remove_16.png")));
		btnRemove.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		btnRemove.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		new Label(container, SWT.NONE);

		btnRemove.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				for (int selectedIndex : table.getSelectionIndices()) {
					TableItem tableItem = table.getItem(selectedIndex);
					TransferingTableItem transferingItem = (TransferingTableItem) tableItem.getData();
					transferingTableItems.remove(transferingItem);
				}
				table.remove(table.getSelectionIndices());
				handleTransferButton();
			}
		});

		Label programsLbl = new Label(container, SWT.NONE);
		programsLbl.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		// programsLbl.setBackground( SWTResourceManager.getColor( SWT.COLOR_WHITE ) );
		programsLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		programsLbl.setText("Programs: ");

		programsCbx = new Combo(container, SWT.NONE);
		programsCbx.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		programsCbx.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		programsCbx.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		List<String> programs;
		try {
			programs = getPrograms();
			programsCbx.setItems(programs.toArray(new String[0]));
			programsCbx.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					validateCarAndScooter();
					handleTransferButton();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Label dummyLbl = new Label(container, SWT.NONE);
		dummyLbl.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.ITALIC));
		dummyLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		dummyLbl.setVisible(false);

		dummyLbl = new Label(container, SWT.NONE);
		dummyLbl.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.ITALIC));
		dummyLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		dummyLbl.setVisible(false);

		instructionLbl = new Label(container, SWT.NONE);
		instructionLbl.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.ITALIC));
		// partsLbl.setBackground( SWTResourceManager.getColor( SWT.COLOR_WHITE ) );
		instructionLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		instructionLbl.setText(
				"NOTE: Please make sure \"Programs\" is selected and all \"Parts\" are green before requesting transfering.");

		return container;
	}

	private boolean checkAllIsEScooterPart() {
		boolean isAllScooter = true;

		for (TCComponent selectedPart : selectedParts) {
			if (!selectedPart.getType().toLowerCase().contains("scooter")) {
				isAllScooter = false;
				break;
			}
		}

		return isAllScooter;
	}

	private void createTableColumns(Composite container) {
		table = new Table(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn column1 = new TableColumn(table, SWT.LEFT);
		column1.setResizable(true);
		column1.setWidth(120);
		column1.setText("Part Number(s)");

		TableColumn column5 = new TableColumn(table, SWT.LEFT);
		column5.setResizable(true);
		column5.setWidth(200);
		column5.setText("Validation Result");

		TableColumn column2 = new TableColumn(table, SWT.LEFT);
		column2.setResizable(true);
		column2.setWidth(150);
		column2.setText("Already Transferred?");

		TableColumn column4 = new TableColumn(table, SWT.LEFT);
		column4.setResizable(true);
		column4.setWidth(120);
		column4.setText("Plant Forms");

		TableColumn column6 = new TableColumn(table, SWT.LEFT);
		column6.setResizable(true);
		column6.setWidth(170);
		column6.setText("Required Fields Filled?");

		TableColumn column3 = new TableColumn(table, SWT.LEFT);
		column3.setResizable(true);
		column3.setWidth(120);
		column3.setText("Maturity Level");

	}

	private String[] createTableRowData(TransferingTableItem transferingTableItem) {
		// part number | Already Transferred | maturity level | plant forms | Is Valid
		// to Transfer
		return new String[] { transferingTableItem.itemID, transferingTableItem.getValidToTrasnferText,
				transferingTableItem.isTransferredText, transferingTableItem.getPlantFormText,
				transferingTableItem.isRequiredFieldsFilledText, transferingTableItem.maturityLevelText };
	}

	private void setTableItems() {
		for (TransferingTableItem transferingTableItem : this.transferingTableItems) {
			TableItem tableItem = new TableItem(table, SWT.NONE);
			String[] rowData = createTableRowData(transferingTableItem);
			tableItem.setText(rowData);
			tableItem.setData(transferingTableItem);

			Display display = Display.getDefault();
			Color red = new Color(display, new RGB(255, 0, 0));
			Color green = new Color(display, new RGB(63, 127, 95));
			if (transferingTableItem.isPassedValidation == false) {
				// tableItem.setForeground(red);
				tableItem.setForeground(0, red);
				tableItem.setForeground(1, red);
			} else {
				tableItem.setForeground(0, green);
				tableItem.setForeground(1, green);
			}

			final Font originalFont = Display.getCurrent().getSystemFont();
			FontData[] boldValues = originalFont.getFontData();
			FontData[] italicValues = originalFont.getFontData();
			for (int i = 0; i < boldValues.length; i++) {
				boldValues[i].setStyle(boldValues[i].getStyle() | SWT.BOLD);
				italicValues[i].setStyle(italicValues[i].getStyle() | SWT.ITALIC);
			}

			Font fontBold = new Font(Display.getCurrent(), boldValues);
			Font fontItalic = new Font(Display.getCurrent(), italicValues);
			if (!transferingTableItem.isRequiredFieldsFilled)
				tableItem.setFont(5, fontBold);
			if (!transferingTableItem.isPartMaturedToTransfer)
				tableItem.setFont(4, fontBold);
			if (!transferingTableItem.isAllPlantFormFilled)
				tableItem.setFont(3, fontItalic);
			if (transferingTableItem.isTransferredText.toLowerCase().equals("true"))
				tableItem.setFont(2, fontItalic);
		}
	}

	private List<String> getPrograms() throws TCException {
		List<String> programs = new LinkedList<String>();

		LOVService lovService = LOVService.getService(session);

		InitialLovData lovInitInput = new InitialLovData();
		LOVInput lovInput = new LOVInput();
		Map<String, String[]> propMap = new HashMap<String, String[]>();

		lovInitInput.propertyName = "vf4_platform_module";
		lovInitInput.lovInput = lovInput;

		lovInput.boName = "VF4_line_itemRevision";
		lovInput.operationName = "Create";
		lovInput.propertyValues = propMap;
		propMap.put("vf4_platform_module", new String[0]);

		LOVSearchResults response = lovService.getInitialLOVValues(lovInitInput);
		TCComponentListOfValues lovs = (TCComponentListOfValues) response.lovData.lovs[0];
		String[] displayVals = lovs.getListOfValues().getLOVDisplayValues();
		Object[] vals = lovs.getListOfValues().getListOfValues();
		for (int i = 0; i < vals.length; i++) {
			String displayVal = displayVals[i];
			String val = vals[i].toString();
			programsDisplayValsAndVals.put(displayVal, val);
			programs.add(displayVal);
		}

		return programs;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Request Transfer Material Dialog");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 330);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		btnRequestTransfer = getButton(IDialogConstants.OK_ID);
//		btnRequestTransfer.setImage(new Image(null,
//				RequestTranferMMDialog.class.getClassLoader().getResourceAsStream("icons/save_16.png")));
		btnRequestTransfer.setText("Request Transfer");
		btnRequestTransfer.setEnabled(false);

		try {
			handlePostAddingPartToTable();
		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void okPressed() {
//		If all passed, trigger transferred with all parts in table
//		Get latest released part revision list
//		Trigger process VF_Request_Transfer_Master_Material_To_SAP

		List<String> validPartRevs = new LinkedList<String>();
		int[] attachmentTypes = new int[transferingTableItems.size()];
		for (TransferingTableItem transferingTableItem : transferingTableItems) {
			String uid = transferingTableItem.latestRev.getUid();
			validPartRevs.add(uid);
		}
		Arrays.fill(attachmentTypes, 1);

		WorkflowService wfsrv = WorkflowService.getService(session);
		ContextData triggerProcessData = new ContextData();
		triggerProcessData.attachmentTypes = attachmentTypes;
		triggerProcessData.attachments = validPartRevs.toArray(new String[0]);
		triggerProcessData.processTemplate = programsCbx.getText().toLowerCase().contains("scooter") ? "RTMS_S"
				: "RTMS";
		triggerProcessData.processAssignmentList = programsCbx.getText().toLowerCase().contains("scooter") ? ""
				: getAssignmentList();
		InstanceInfo copyingPartProcess = wfsrv.createInstance(true, null, "VF System Call", null, "VF System Call",
				triggerProcessData);
		StringBuffer errMsg = new StringBuffer();

		if (copyingPartProcess.serviceData.sizeOfCreatedObjects() > 0
				&& copyingPartProcess.serviceData.sizeOfPartialErrors() == 0) {
			System.out.println("[vf] trigger OKAY");
		} else {
			String soaErr = Utils.getErrorMessagesFromSOA(copyingPartProcess.serviceData);
			errMsg.append(soaErr);
		}

		boolean isTriggeredSuccessful = errMsg.length() <= 2;
		MessageBox mb = new MessageBox(getShell(), isTriggeredSuccessful ? SWT.ICON_INFORMATION : SWT.ICON_WARNING);
		mb.setText(isTriggeredSuccessful ? "Info" : "Warning");
		String msg = isTriggeredSuccessful ? "Transfering Request triggerred successfully."
				: "Transfering Request triggerred FAIL with below errors. Please contact your IT Service Desk.\n"
						+ errMsg.toString();
		mb.setMessage(msg);
		mb.open();

		super.okPressed();
	}

	private String getAssignmentList() {
		String selectedProgramText = programsCbx.getText();
		String assignmentListName = "";
		if (selectedProgramText != null && !selectedProgramText.isEmpty()) {
			String selectedProgram = this.programsDisplayValsAndVals.get(selectedProgramText);
			if (selectedProgram != null) {
				String lowerSelectedProgram = selectedProgram.toLowerCase();
				if (lowerSelectedProgram.contains("ebus")) {
					assignmentListName = "RTMS_EBUS";
				} else if (lowerSelectedProgram.contains("vfe32")) {
					assignmentListName = "RTMS_VFe32";
				} else if (lowerSelectedProgram.contains("vfe33")) {
					assignmentListName = "RTMS_VFe33";
				} else if (lowerSelectedProgram.contains("vfe34ng")) {
					assignmentListName = "RTMS_VFe34NG";
				} else if (lowerSelectedProgram.contains("vfe3n1")) {
					assignmentListName = "RTMS_VFe3N1";
				} else if (lowerSelectedProgram.contains("vf") && lowerSelectedProgram.contains("31")) {
					assignmentListName = "RTMS_SCP";
				} else if (lowerSelectedProgram.contains("vf") && lowerSelectedProgram.contains("32")) {
					assignmentListName = "RTMS_CSUV";
				} else if (lowerSelectedProgram.contains("vf") && lowerSelectedProgram.contains("33")) {
					assignmentListName = "RTMS_DSUV";
				} else {
					assignmentListName = "RTMS_MBOM";
				}
			}
		}

		System.out.println(assignmentListName);

		return assignmentListName;
	}

	private void buttonAddHandler(List<String> idList) {
		try {
			RequestTranferMMSearchDialg mmDialog = new RequestTranferMMSearchDialg();
			Set<TCComponent> selectedItems = new HashSet<TCComponent>();
			mmDialog.createAndShowGUI(session, selectedItems, new Runnable() {

				@Override
				public void run() {
					try {
						transferingTableItems.addAll(createTableItems(selectedItems));
						handlePostAddingPartToTable();
					} catch (NotLoadedException e) {
						e.printStackTrace();
					} catch (TCException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handlePostAddingPartToTable() throws NotLoadedException, TCException {
		setTableItems();
		handleTransferButton();
	}

	private void handleTransferButton() {
		boolean isAllValid = true;
		for (TransferingTableItem transferingTableItem : this.transferingTableItems) {
			if (transferingTableItem.isPassedValidation == false || (transferingTableItem.isAllPlantFormFilled == false
					&& programsCbx.getText().toLowerCase().contains("scooter"))) {
				isAllValid = false;
				break;
			}
		}

		boolean isEnable = (this.transferingTableItems.size() > 0 && isAllValid
				&& programsCbx.getSelectionIndex() > -1);
		if (instructionLbl != null)
			instructionLbl.setVisible(!isEnable);
		btnRequestTransfer.setEnabled(isEnable);
	}

	private List<TransferingTableItem> createTableItems(Set<TCComponent> selectedItems) throws Exception {
		List<TransferingTableItem> tableItems = new LinkedList<TransferingTableItem>();

		for (TCComponent selectedItem : selectedItems) {
			TransferingTableItem transferingItem = new TransferingTableItem(selectedItem);
			tableItems.add(transferingItem);
		}

		return tableItems;
	}

	class TransferingTableItem {
		public String isTransferredText;
		public String getValidToTrasnferText;
		public String getPlantFormText;
		public String maturityLevelText;
		public String isRequiredFieldsFilledText;
		public String itemID;
		public String validationText;
		public TCComponent item;
		public TCComponent latestRev;
		public boolean isPassedValidation;

		public boolean isPartMaturedToTransfer;
		public boolean isAllPlantFormFilled;
		public boolean isRequiredFieldsFilled;
		public TCComponent revRule;

		public TransferingTableItem(TCComponent item) throws Exception {
			this.itemID = item.getPropertyDisplayableValue("item_id");
			this.item = item;
			this.isPassedValidation = false;
			this.isAllPlantFormFilled = false;
			this.isPartMaturedToTransfer = false;
			this.isRequiredFieldsFilled = false;

			revRule = session.getComponentManager().getTCComponent("dHFAgTh647MsRA");// vf rls
			TCComponentItemRevision latestRev = ((TCComponentItem) item).getConfiguredItemRevision(revRule);
			if (latestRev == null) {
				revRule = session.getComponentManager().getTCComponent("QIOAAAAY47MsRA");// any st wk
				latestRev = ((TCComponentItem) item).getConfiguredItemRevision(revRule);
				if (latestRev == null)
					throw new Exception("Cannot get latest released revision of part \""
							+ item.getProperty("object_string") + "\"");
			}

			this.latestRev = latestRev;
			this.isTransferredText = item.getPropertyDisplayableValue("vf4_is_transferred_erp");
			if (isTransferredText.isEmpty())
				isTransferredText = "False";
			this.maturityLevelText = validateMaturity();
			this.getPlantFormText = validatePlantForm();
			this.isRequiredFieldsFilledText = validateRequiredFields();
			this.getValidToTrasnferText = validate();
		}

		private String validatePlantForm() throws TCException, NotLoadedException {
			this.isAllPlantFormFilled = true;

			String[] plantParms = new String[] { "VF4_plant_form_relation", "VF4_plant_form", "vf4_plant",
					"vf4_make_buy" };

			AIFComponentContext[] plantFormsContext = this.item.getRelated(plantParms[0]);

			if (plantFormsContext.length == 0) {
				plantParms = new String[] { "VF3_plant_rel", "VF3_plant_form", "vf3_plant", "vf3_make_buy" };
				plantFormsContext = this.item.getRelated(plantParms[0]);
			}

			if (plantFormsContext.length == 0) {
				plantParms = new String[] { "Vf8_PlantFormRel", "Vf8_PlantForm", "vf8_Plantcode", "vf8_Make_Buy" };
				plantFormsContext = this.item.getRelated(plantParms[0]);
			}

			for (AIFComponentContext plantFormContext : plantFormsContext) {
				TCComponent plantForm = (TCComponent) plantFormContext.getComponent();
				String plantCode = plantForm.getPropertyDisplayableValue(plantParms[2]);
				String procurementType = plantForm.getPropertyDisplayableValue(plantParms[3]);
				String type = plantForm.getTypeComponent().getName();

				if (plantCode.isEmpty() || procurementType.isEmpty() || type.compareTo(plantParms[1]) != 0) {
					this.isAllPlantFormFilled = false;
					break;
				}
			}

			if (plantFormsContext.length == 0)
				this.isAllPlantFormFilled = false;

			String selectedProgram = programsCbx != null ? programsCbx.getText()
					: checkAllIsEScooterPart() ? "EScooter" : "";
			String plantFormText = this.isAllPlantFormFilled ? "Valid to transfer"
					: selectedProgram.toLowerCase().contains("scooter") ? "Please fill plant information."
							: "Missing plant info! Require MBOM Team to evaluate.";
			return plantFormText;
		}

		private String validateMaturity() throws TCException {
			String latestStatus = "";
			try {
				latestStatus = latestRev.getPropertyDisplayableValue("release_status_list");
				if (latestStatus.isEmpty()) {
					latestStatus = "Part's NOT matured to transfer!";
				} else {
					isPartMaturedToTransfer = true;
				}
			} catch (NotLoadedException e) {
				e.printStackTrace();
			}

			return latestStatus;
		}

		private String validate() throws NotLoadedException {
			StringBuilder sb = new StringBuilder();

			if (latestRev != null) {
				String selectedProgram = programsCbx != null ? programsCbx.getText()
						: checkAllIsEScooterPart() ? "EScooter" : "";
				if ((selectedProgram.toLowerCase().contains("scooter") && this.isPartMaturedToTransfer
						&& isRequiredFieldsFilled && isAllPlantFormFilled)
						|| (!selectedProgram.toLowerCase().contains("scooter") && this.isPartMaturedToTransfer
								&& isRequiredFieldsFilled)) {
					sb.append("Valid to transfer");
					isPassedValidation = true;
				} else {
					isPassedValidation = false;
					sb.append("NOT valid for SAP transfer! ");
				}
			}

			return sb.toString();
		}

		private String validateRequiredFields() throws NotLoadedException {
			boolean isPartMakeBuyFilled = !item.getPropertyDisplayableValue("vf4_item_make_buy").trim().isEmpty();
			boolean isMaterialTypeFilled = !item.getPropertyDisplayableValue("vf4_itm_material_type").trim().isEmpty();
			this.isRequiredFieldsFilled = isPartMakeBuyFilled && isMaterialTypeFilled;

			return isRequiredFieldsFilled ? "Valid to transfer" : "Part Make Buy or Material Type is not filled!";
		}
	}

	private void validateCarAndScooter() {
		if (programsCbx.getText().toLowerCase().isEmpty())
			return;

		// if scooter selected, all should be scooter
		if (programsCbx.getText().toLowerCase().contains("scooter")) {
			for (TransferingTableItem transferingTableItem : transferingTableItems) {
				if (transferingTableItem.item.getType().toLowerCase().contains("scooter") == false) {
					com.teamcenter.rac.util.MessageBox.post(
							"Cannot select Escooter program as there are Car's part(s).\nPlease select only EScooter's parts.",
							"Warning", com.teamcenter.rac.util.MessageBox.WARNING);
					programsCbx.setText("");
					break;
				}
			}
		}

		// if not scooter selected, should not contains any scooter
		if (!programsCbx.getText().toLowerCase().contains("scooter")) {
			for (TransferingTableItem transferingTableItem : transferingTableItems) {
				if (transferingTableItem.item.getType().toLowerCase().contains("scooter")) {
					com.teamcenter.rac.util.MessageBox.post(
							"Selected parts are Escooter.\nPlease select Escooter's program.", "Warning",
							com.teamcenter.rac.util.MessageBox.WARNING);
					programsCbx.setText("");
					break;
				}
			}
		}
	}
}
