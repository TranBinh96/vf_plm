package com.teamcenter.vinfast.car.engineering.view;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.util.MessageBox;

public class RawMaterialCost_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	public Label lblTotalResult;
	public Table tblMaster;
	private TableColumn tblclmnNo;
	private TableColumn tblclmnHpartNumber;
	private TableColumn tblclmnHpartName;
	private TableColumn tblclmnBlankNumber;
	private TableColumn tblclmnBlankName;
	private TableColumn tblclmnBlankMaterial;
	private TableColumn tblclmnBlankCoating;
	private TableColumn tblclmnBlankThickness;
	private TableColumn tblclmnBlankWidth;
	private TableColumn tblclmnBlankLength;
	private TableColumn tblclmnCoilNumber;
	private TableColumn tblclmnCoilName;
	private TableColumn tblclmnCoilMaterial;
	private TableColumn tblclmnCoilCoating;
	private TableColumn tblclmnCoilThickness;
	private TableColumn tblclmnCoilWidth;
	private TableColumn tblclmnCoil2Number;
	private TableColumn tblclmnCoil2Name;
	private TableColumn tblclmnCoil2Material;
	private TableColumn tblclmnCoil2Coating;
	private TableColumn tblclmnCoil2Thickness;
	private TableColumn tblclmnCoil2Width;
	private TableColumn tblclmnCost;
	private TableColumn tblclmnNewColumn;
	private TableColumn tblclmnNewColumn_1;
	
	public RawMaterialCost_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.ON_TOP);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(1, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			tblMaster = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblMaster.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			tblMaster.setHeaderVisible(true);
			tblMaster.setLinesVisible(true);
			
			tblclmnNo = new TableColumn(tblMaster, SWT.NONE);
			tblclmnNo.setWidth(40);
			tblclmnNo.setText("No");
			
			tblclmnHpartNumber = new TableColumn(tblMaster, SWT.NONE);
			tblclmnHpartNumber.setWidth(120);
			tblclmnHpartNumber.setText("H-Part Number");
			
			tblclmnHpartName = new TableColumn(tblMaster, SWT.NONE);
			tblclmnHpartName.setWidth(160);
			tblclmnHpartName.setText("H-Part Name");
			
			tblclmnBlankNumber = new TableColumn(tblMaster, SWT.NONE);
			tblclmnBlankNumber.setWidth(120);
			tblclmnBlankNumber.setText("Blank Number");
			
			tblclmnBlankName = new TableColumn(tblMaster, SWT.NONE);
			tblclmnBlankName.setWidth(160);
			tblclmnBlankName.setText("Blank Name");
			
			tblclmnBlankMaterial = new TableColumn(tblMaster, SWT.NONE);
			tblclmnBlankMaterial.setWidth(120);
			tblclmnBlankMaterial.setText("Blank Material");
			
			tblclmnBlankCoating = new TableColumn(tblMaster, SWT.NONE);
			tblclmnBlankCoating.setWidth(120);
			tblclmnBlankCoating.setText("Blank Coating");
			
			tblclmnBlankThickness = new TableColumn(tblMaster, SWT.NONE);
			tblclmnBlankThickness.setWidth(120);
			tblclmnBlankThickness.setText("Blank Thickness");
			
			tblclmnBlankWidth = new TableColumn(tblMaster, SWT.NONE);
			tblclmnBlankWidth.setWidth(120);
			tblclmnBlankWidth.setText("Blank Width");
			
			tblclmnBlankLength = new TableColumn(tblMaster, SWT.NONE);
			tblclmnBlankLength.setWidth(120);
			tblclmnBlankLength.setText("Blank Length");
			
			tblclmnCoilNumber = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoilNumber.setWidth(120);
			tblclmnCoilNumber.setText("Coil Number");
			
			tblclmnCoilName = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoilName.setWidth(160);
			tblclmnCoilName.setText("Coil Name");
			
			tblclmnCoilMaterial = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoilMaterial.setWidth(120);
			tblclmnCoilMaterial.setText("Coil Material");
			
			tblclmnCoilCoating = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoilCoating.setWidth(120);
			tblclmnCoilCoating.setText("Coil Coating");
			
			tblclmnCoilThickness = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoilThickness.setWidth(120);
			tblclmnCoilThickness.setText("Coil Thickness");
			
			tblclmnCoilWidth = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoilWidth.setWidth(120);
			tblclmnCoilWidth.setText("Coil Width");
			
			tblclmnCoil2Number = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoil2Number.setWidth(120);
			tblclmnCoil2Number.setText("Coil2 Number");
			
			tblclmnCoil2Name = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoil2Name.setWidth(160);
			tblclmnCoil2Name.setText("Coil2 Name");
			
			tblclmnCoil2Material = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoil2Material.setWidth(120);
			tblclmnCoil2Material.setText("Coil2 Material");
			
			tblclmnCoil2Coating = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoil2Coating.setWidth(120);
			tblclmnCoil2Coating.setText("Coil2 Coating");
			
			tblclmnCoil2Thickness = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoil2Thickness.setWidth(120);
			tblclmnCoil2Thickness.setText("Coil2 Thickness");
			
			tblclmnCoil2Width = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCoil2Width.setWidth(120);
			tblclmnCoil2Width.setText("Coil2 Width");
			
			tblclmnCost = new TableColumn(tblMaster, SWT.NONE);
			tblclmnCost.setWidth(80);
			tblclmnCost.setText("Piece Cost");
			
			tblclmnNewColumn = new TableColumn(tblMaster, SWT.NONE);
			tblclmnNewColumn.setWidth(80);
			tblclmnNewColumn.setText("Tooling Cost");
			
			tblclmnNewColumn_1 = new TableColumn(tblMaster, SWT.NONE);
			tblclmnNewColumn_1.setWidth(80);
			tblclmnNewColumn_1.setText("Ednd Cost");
			
			lblTotalResult = new Label(container, SWT.NONE);
			lblTotalResult.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblTotalResult.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
			lblTotalResult.setText("0 item(s)");
		} 
		catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Raw Material Cost");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 600);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	}
}
