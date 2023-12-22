package com.teamcenter.vines.ecr;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.wb.swt.ResourceManager;

import com.teamcenter.rac.kernel.TCException;

public class VESECRBomInfoUpdate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnUp;
	public Button btnDown;
	public Button btnAddBomline;
	public Button btnRemoveBomline;
	public Button btnChangeTypeUpdateAll;
	public Button btnSteeringUpdateAll;
	public Button btnExchangeabilityUpdateAll;
	public Button btnAddRemovePart;
	public Button btnMaturityLevel;
	public Button btnRecover;
	public Table tblBom;
	public Button ckbCheckAll;

	public Button btnCreate;

	public VESECRBomInfoUpdate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.ON_TOP);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		setTitle("Update Bom Information");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(1, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Composite composite = new Composite(container, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

			ckbCheckAll = new Button(composite, SWT.CHECK);
			ckbCheckAll.setBounds(10, 5, 13, 16);

			btnAddBomline = new Button(composite, SWT.NONE);
			btnAddBomline.setBounds(110, 0, 100, 25);
			btnAddBomline.setText("Add");
			btnAddBomline.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));

			btnRemoveBomline = new Button(composite, SWT.NONE);
			btnRemoveBomline.setBounds(215, 0, 100, 25);
			btnRemoveBomline.setText("Remove");
			btnRemoveBomline.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));

			btnChangeTypeUpdateAll = new Button(composite, SWT.NONE);
			btnChangeTypeUpdateAll.setBounds(320, 0, 100, 25);
			btnChangeTypeUpdateAll.setText("Change Type");

			btnSteeringUpdateAll = new Button(composite, SWT.NONE);
			btnSteeringUpdateAll.setBounds(425, 0, 100, 25);
			btnSteeringUpdateAll.setText("Steering");

			btnExchangeabilityUpdateAll = new Button(composite, SWT.NONE);
			btnExchangeabilityUpdateAll.setBounds(530, 0, 100, 25);
			btnExchangeabilityUpdateAll.setText("Exchangeability");

			btnAddRemovePart = new Button(composite, SWT.NONE);
			btnAddRemovePart.setBounds(740, 0, 100, 25);
			btnAddRemovePart.setText("Add Line");

			btnMaturityLevel = new Button(composite, SWT.NONE);
			btnMaturityLevel.setText("Maturity Level");
			btnMaturityLevel.setBounds(635, 0, 100, 25);

			btnUp = new Button(composite, SWT.NONE);
			btnUp.setBounds(30, 0, 35, 25);
			btnUp.setText("Up");

			btnDown = new Button(composite, SWT.NONE);
			btnDown.setText("Down");
			btnDown.setBounds(70, 0, 35, 25);

			btnRecover = new Button(composite, SWT.NONE);
			btnRecover.setText("Recover");
			btnRecover.setBounds(846, 1, 100, 25);

			CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.MULTI);
			checkboxTableViewer.setAllChecked(false);
			checkboxTableViewer.setAllGrayed(false);
			tblBom = checkboxTableViewer.getTable();
			tblBom.setTouchEnabled(true);
			tblBom.setLinesVisible(true);
			tblBom.setHeaderVisible(true);
			tblBom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

			TableColumn tblclmnNewColumn_1 = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewColumn_1.setWidth(30);

			TableColumn tblclmnChangeType = new TableColumn(tblBom, SWT.NONE);
			tblclmnChangeType.setWidth(80);
			tblclmnChangeType.setText("Change Type");

			TableColumn tblclmnPosid = new TableColumn(tblBom, SWT.NONE);
			tblclmnPosid.setWidth(69);
			tblclmnPosid.setText("POSID");

			TableColumn tblclmnDonorVehicle = new TableColumn(tblBom, SWT.NONE);
			tblclmnDonorVehicle.setWidth(100);
			tblclmnDonorVehicle.setText("Donor Vehicle");

			TableColumn tblclmnLevel = new TableColumn(tblBom, SWT.NONE);
			tblclmnLevel.setWidth(40);
			tblclmnLevel.setText("Level");

			TableColumn tblclmnSteering = new TableColumn(tblBom, SWT.NONE);
			tblclmnSteering.setWidth(70);
			tblclmnSteering.setText("Steering");

			TableColumn tblclmnQuantity = new TableColumn(tblBom, SWT.NONE);
			tblclmnQuantity.setWidth(70);
			tblclmnQuantity.setText("Quantity");

			TableColumn tblclmnMatunityLevel = new TableColumn(tblBom, SWT.NONE);
			tblclmnMatunityLevel.setWidth(80);
			tblclmnMatunityLevel.setText("Maturity Level");

			TableColumn tblclmnPurchaseLevel = new TableColumn(tblBom, SWT.NONE);
			tblclmnPurchaseLevel.setWidth(80);
			tblclmnPurchaseLevel.setText("Purchase Level");

			TableColumn tblclmnNewColumn = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewColumn.setWidth(100);
			tblclmnNewColumn.setText("Part No");

			TableColumn tblclmnOldRevision = new TableColumn(tblBom, SWT.NONE);
			tblclmnOldRevision.setWidth(55);
			tblclmnOldRevision.setText("Old Rev");

			TableColumn tblclmnFrozenRevison = new TableColumn(tblBom, SWT.NONE);
			tblclmnFrozenRevison.setWidth(55);
			tblclmnFrozenRevison.setText("Frozen Rev");

			TableColumn tblclmnNewRevision = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewRevision.setWidth(55);
			tblclmnNewRevision.setText("New Rev");

			TableColumn tblclmnPartName = new TableColumn(tblBom, SWT.NONE);
			tblclmnPartName.setWidth(100);
			tblclmnPartName.setText("Part Name");

			TableColumn tblclmnOriginalPart = new TableColumn(tblBom, SWT.NONE);
			tblclmnOriginalPart.setWidth(86);
			tblclmnOriginalPart.setText("Original Part");

			TableColumn tblclmnVariantFormula = new TableColumn(tblBom, SWT.NONE);
			tblclmnVariantFormula.setWidth(100);
			tblclmnVariantFormula.setText("Variant Formula");

			TableColumn tblclmnTorqueInfo = new TableColumn(tblBom, SWT.NONE);
			tblclmnTorqueInfo.setWidth(100);
			tblclmnTorqueInfo.setText("Torque Info");

			TableColumn tblclmnWeightg = new TableColumn(tblBom, SWT.NONE);
			tblclmnWeightg.setWidth(100);
			tblclmnWeightg.setText("Weight (g)");

			TableColumn tblclmnChangeDesc = new TableColumn(tblBom, SWT.NONE);
			tblclmnChangeDesc.setWidth(150);
			tblclmnChangeDesc.setText("Change Description");

			TableColumn tblclmndDataAffected = new TableColumn(tblBom, SWT.NONE);
			tblclmndDataAffected.setWidth(60);
			tblclmndDataAffected.setText("3D Data");

			TableColumn tblclmnMaterial = new TableColumn(tblBom, SWT.NONE);
			tblclmnMaterial.setWidth(100);
			tblclmnMaterial.setText("Material");

			TableColumn tblclmnCoating = new TableColumn(tblBom, SWT.NONE);
			tblclmnCoating.setWidth(100);
			tblclmnCoating.setText("Coating");

			TableColumn tblclmnSpecbook = new TableColumn(tblBom, SWT.NONE);
			tblclmnSpecbook.setWidth(100);
			tblclmnSpecbook.setText("Specbook");

			TableColumn tblclmnSupplier = new TableColumn(tblBom, SWT.NONE);
			tblclmnSupplier.setWidth(100);
			tblclmnSupplier.setText("Supplier");

			TableColumn tblclmnSupplierContact = new TableColumn(tblBom, SWT.NONE);
			tblclmnSupplierContact.setWidth(100);
			tblclmnSupplierContact.setText("Supplier Contact");

			TableColumn tblclmnAftersaleRelevant = new TableColumn(tblBom, SWT.NONE);
			tblclmnAftersaleRelevant.setWidth(100);
			tblclmnAftersaleRelevant.setText("Is Aftersale Relevant");

			TableColumn tblclmnExchangeability = new TableColumn(tblBom, SWT.NONE);
			tblclmnExchangeability.setWidth(180);
			tblclmnExchangeability.setText("Exchangeability");

			TableColumn tblclmnPartTraceability = new TableColumn(tblBom, SWT.NONE);
			tblclmnPartTraceability.setWidth(100);
			tblclmnPartTraceability.setText("Part Traceability");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1038, 1000);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Engineering Change Request...");
	}
}
