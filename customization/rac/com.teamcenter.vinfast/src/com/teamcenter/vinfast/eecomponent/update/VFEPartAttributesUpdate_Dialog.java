package com.teamcenter.vinfast.eecomponent.update;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class VFEPartAttributesUpdate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	public Button ckbIsAfterSale;
	public Label lblDonorVehicle;
	public Label lblSupplierType;
	public Label lblLongOrShort;
	public Label lblPartCategory;
	public Label lblPartTraceabilityIndicator;
	public Combo cbDonorProgram;
	public Combo cbSupplierType;
	public Combo cbLongOrShortlead;
	public Combo cbPartCategory;
	public Combo cbPartTraceability;
	public Button btnInfoPartTraceability;
	public Button btnInfoIsAfterSale;
	public Button btnInfoDonorVehicle;
	public Button btnInfoSupplierType;
	public Button btnInfoLongShort;
	public Button btnInfoPartCategory;
	
	public VFEPartAttributesUpdate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			setTitle("Component Attributes Update");
			setMessage("Define business object create information");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Label lblVehicleCategory = new Label(container, SWT.NONE);
			lblVehicleCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVehicleCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblVehicleCategory.setText("Is After Sale Relevant:");
			{
				ckbIsAfterSale = new Button(container, SWT.CHECK);
				ckbIsAfterSale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnInfoIsAfterSale = new Button(container, SWT.NONE);
				btnInfoIsAfterSale.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/info_16.png"));
			}
			{
				lblDonorVehicle = new Label(container, SWT.NONE);
				lblDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblDonorVehicle.setText("Donor Program:");
				lblDonorVehicle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				cbDonorProgram = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbDonorProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbDonorProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnInfoDonorVehicle = new Button(container, SWT.NONE);
				btnInfoDonorVehicle.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/info_16.png"));
			}
			{
				lblSupplierType = new Label(container, SWT.NONE);
				lblSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblSupplierType.setText("Supplier Type:");
				lblSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				cbSupplierType = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbSupplierType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbSupplierType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnInfoSupplierType = new Button(container, SWT.NONE);
				btnInfoSupplierType.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/info_16.png"));
			}
			{
				lblLongOrShort = new Label(container, SWT.NONE);
				lblLongOrShort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblLongOrShort.setText("Long Or Short lead:");
				lblLongOrShort.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				cbLongOrShortlead = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbLongOrShortlead.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbLongOrShortlead.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnInfoLongShort = new Button(container, SWT.NONE);
				btnInfoLongShort.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/info_16.png"));
			}
			{
				lblPartCategory = new Label(container, SWT.NONE);
				lblPartCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblPartCategory.setText("Part Category:");
				lblPartCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				cbPartCategory = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbPartCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbPartCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnInfoPartCategory = new Button(container, SWT.NONE);
				btnInfoPartCategory.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/info_16.png"));
			}
			{
				lblPartTraceabilityIndicator = new Label(container, SWT.NONE);
				lblPartTraceabilityIndicator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblPartTraceabilityIndicator.setText("Part Traceability Indicator:");
				lblPartTraceabilityIndicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				cbPartTraceability = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				btnInfoPartTraceability = new Button(container, SWT.NONE);
				btnInfoPartTraceability.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/info_16.png"));	
			}
		}
		catch(Exception ex ) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(480, 430);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Component Attributes");
	}
}
