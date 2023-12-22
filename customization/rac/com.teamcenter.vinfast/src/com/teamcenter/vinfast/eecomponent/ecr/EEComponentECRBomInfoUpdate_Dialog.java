package com.teamcenter.vinfast.eecomponent.ecr;

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
import org.eclipse.swt.widgets.Label;

public class EEComponentECRBomInfoUpdate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnUp;
	public Button btnDown;
	public Button btnAddOldBomline;
	public Button btnRemoveOldBomline;
	public Button btnChangeTypeUpdateAll;
	public Button btnDisposalMaterial;
	public Button btnRecover;
	public Table tblBom;
	public Button ckbCheckAll;
	public Button btnAddNewBomline;
	public Button btnRemoveNewBomline;
	public Button btnRemoveLine;

	public Button btnCreate;

	public EEComponentECRBomInfoUpdate_Dialog(Shell parentShell) throws TCException {
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
			composite.setLayout(new GridLayout(11, false));
			composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

			ckbCheckAll = new Button(composite, SWT.CHECK);

			btnUp = new Button(composite, SWT.NONE);
			btnUp.setText("Up");

			btnDown = new Button(composite, SWT.NONE);
			btnDown.setText("Down");

			btnAddOldBomline = new Button(composite, SWT.NONE);
			btnAddOldBomline.setText("Add Old Part");
			btnAddOldBomline.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));

			btnRemoveOldBomline = new Button(composite, SWT.NONE);
			btnRemoveOldBomline.setText("Remove Old Part");
			btnRemoveOldBomline.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			
			btnAddNewBomline = new Button(composite, SWT.NONE);
			btnAddNewBomline.setText("Add New Part");
			btnAddNewBomline.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
			
			btnRemoveNewBomline = new Button(composite, SWT.NONE);
			btnRemoveNewBomline.setText("Remove New Part");
			btnRemoveNewBomline.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			
			btnRemoveLine = new Button(composite, SWT.NONE);
			btnRemoveLine.setText("Remove Line");
			btnRemoveLine.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));

			btnChangeTypeUpdateAll = new Button(composite, SWT.NONE);
			btnChangeTypeUpdateAll.setText("Change Type");

			btnDisposalMaterial = new Button(composite, SWT.NONE);
			btnDisposalMaterial.setText("Disposal Material");

			btnRecover = new Button(composite, SWT.NONE);
			btnRecover.setText("Recover");

			CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
			checkboxTableViewer.setAllChecked(false);
			checkboxTableViewer.setAllGrayed(false);
			tblBom = checkboxTableViewer.getTable();
			tblBom.setTouchEnabled(true);
			tblBom.setLinesVisible(true);
			tblBom.setHeaderVisible(true);
			tblBom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

			TableColumn tblclmnNewColumn_1 = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewColumn_1.setWidth(30);
			
			TableColumn tblclmnTopPartNumber = new TableColumn(tblBom, SWT.NONE);
			tblclmnTopPartNumber.setWidth(80);
			tblclmnTopPartNumber.setText("Top Part Number");

			TableColumn tblclmnChangeType = new TableColumn(tblBom, SWT.NONE);
			tblclmnChangeType.setWidth(80);
			tblclmnChangeType.setText("Change Type");

			TableColumn tblclmnChangePoint = new TableColumn(tblBom, SWT.NONE);
			tblclmnChangePoint.setWidth(109);
			tblclmnChangePoint.setText("Change Point");

			TableColumn tblclmnDisposalMaterials = new TableColumn(tblBom, SWT.NONE);
			tblclmnDisposalMaterials.setWidth(109);
			tblclmnDisposalMaterials.setText("Disposal materials");

			TableColumn tblclmnNewColumn = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewColumn.setWidth(100);
			tblclmnNewColumn.setText("Old Part Number");

			TableColumn tblclmnPartName = new TableColumn(tblBom, SWT.NONE);
			tblclmnPartName.setWidth(100);
			tblclmnPartName.setText("Old Part Name");

			TableColumn tblclmnOldRevision = new TableColumn(tblBom, SWT.NONE);
			tblclmnOldRevision.setWidth(55);
			tblclmnOldRevision.setText("Old Part Rev");

			TableColumn tblclmnQuantity = new TableColumn(tblBom, SWT.NONE);
			tblclmnQuantity.setWidth(70);
			tblclmnQuantity.setText("Old Part Quantity");

			TableColumn tblclmnMpn = new TableColumn(tblBom, SWT.NONE);
			tblclmnMpn.setWidth(70);
			tblclmnMpn.setText("Old Part MPN");

			TableColumn tblclmnSubstitute = new TableColumn(tblBom, SWT.NONE);
			tblclmnSubstitute.setWidth(70);
			tblclmnSubstitute.setText("Old Part Substitute");

			TableColumn tblclmnDesignator = new TableColumn(tblBom, SWT.NONE);
			tblclmnDesignator.setWidth(70);
			tblclmnDesignator.setText("Old Part Designator");

			TableColumn tblclmnNewPartNumber = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewPartNumber.setWidth(100);
			tblclmnNewPartNumber.setText("Part Number");

			TableColumn tblclmnNewPartName = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewPartName.setWidth(100);
			tblclmnNewPartName.setText("Part Name");

			TableColumn tblclmnNewPartRev = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewPartRev.setWidth(55);
			tblclmnNewPartRev.setText("Part Rev");

			TableColumn tblclmnNewPartQuantity = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewPartQuantity.setWidth(70);
			tblclmnNewPartQuantity.setText("Quantity");

			TableColumn tblclmnNewPartMpn = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewPartMpn.setWidth(70);
			tblclmnNewPartMpn.setText("MPN");

			TableColumn tblclmnNewPartSubstitute = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewPartSubstitute.setWidth(70);
			tblclmnNewPartSubstitute.setText("Substitute");

			TableColumn tblclmnNewPartDesignator = new TableColumn(tblBom, SWT.NONE);
			tblclmnNewPartDesignator.setWidth(70);
			tblclmnNewPartDesignator.setText("Designator");

			TableColumn tblclmnPartTraceability = new TableColumn(tblBom, SWT.NONE);
			tblclmnPartTraceability.setWidth(70);
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
