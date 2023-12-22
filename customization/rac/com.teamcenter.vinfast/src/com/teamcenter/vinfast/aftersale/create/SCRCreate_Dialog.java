package com.teamcenter.vinfast.aftersale.create;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class SCRCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	public Button ckbOpenOnCreate;
	public Button btnCreate;
	public Label lblDonorVehicle;
	public Combo cbVehicleProgram;
	public Text txtName;
	public Text txtDescription;
	public Button ckbAFSPartUpdateRequired;
	public Button ckbAFSRelevantUpdateRequired;
	public Button ckbDCRUpdateRequired;
	public Button ckbECPUpdateRequired;
	public Button ckbMarketSpecificPurchaseRequired;
	public Text txtPrefixName;

	public SCRCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Create SCR");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblMaterial = new Label(container, SWT.NONE);
			lblMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblMaterial.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblMaterial.setText("Vehicle Program: (*)");

			cbVehicleProgram = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbVehicleProgram.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			cbVehicleProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_cbVehicleProgram = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gd_cbVehicleProgram.widthHint = 302;
			cbVehicleProgram.setLayoutData(gd_cbVehicleProgram);

			Label lblName = new Label(container, SWT.NONE);
			lblName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblName.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblName.setText("SCR Title: (*)");

			txtPrefixName = new Text(container, SWT.BORDER);
			txtPrefixName.setEditable(false);
			txtPrefixName.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			txtPrefixName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			txtName = new Text(container, SWT.BORDER);
			txtName.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			txtName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblReferenceNumber = new Label(container, SWT.NONE);
			lblReferenceNumber.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblReferenceNumber.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			lblReferenceNumber.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblReferenceNumber.setText("Description: (*)");

			txtDescription = new Text(container, SWT.BORDER);
			txtDescription.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			txtDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gd_txtDescription.heightHint = 47;
			txtDescription.setLayoutData(gd_txtDescription);

			Label lblPartMakeBuy = new Label(container, SWT.NONE);
			lblPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPartMakeBuy.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblPartMakeBuy.setText("AFS Part Update:");

			ckbAFSPartUpdateRequired = new Button(container, SWT.CHECK);
			ckbAFSPartUpdateRequired.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

			Label lblDes = new Label(container, SWT.NONE);
			lblDes.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblDes.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblDes.setText("AFS Relevant Value Update:");

			ckbAFSRelevantUpdateRequired = new Button(container, SWT.CHECK);
			ckbAFSRelevantUpdateRequired.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

			lblDonorVehicle = new Label(container, SWT.NONE);
			lblDonorVehicle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDonorVehicle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblDonorVehicle.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblDonorVehicle.setText("DCR Update:");

			ckbDCRUpdateRequired = new Button(container, SWT.CHECK);
			ckbDCRUpdateRequired.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

			Label lblUoM = new Label(container, SWT.NONE);
			lblUoM.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblUoM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblUoM.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblUoM.setText("EPC Update:");

			ckbECPUpdateRequired = new Button(container, SWT.CHECK);
			ckbECPUpdateRequired.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

			Label lblPurLevel = new Label(container, SWT.NONE);
			lblPurLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblPurLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPurLevel.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
			lblPurLevel.setText("Market Specific:");

			ckbMarketSpecificPurchaseRequired = new Button(container, SWT.CHECK);
			ckbMarketSpecificPurchaseRequired.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		} catch (Exception e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		ckbOpenOnCreate = new Button(parent, SWT.CHECK);
		ckbOpenOnCreate.setText("Open On Create");
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 580);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Aftersale Change Request");
	}
}
