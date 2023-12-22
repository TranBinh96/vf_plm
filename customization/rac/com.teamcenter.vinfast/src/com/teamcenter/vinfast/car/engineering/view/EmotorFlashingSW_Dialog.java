package com.teamcenter.vinfast.car.engineering.view;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.util.MessageBox;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class EmotorFlashingSW_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Text txtFAppFile;
	public Text txtFBootLoaderFile;
	public Text txtFFlashDriverFile;
	public Text txtRAppFile;
	public Text txtRBootLoaderFile;
	public Text txtRFlashDriverFile;
	public Button btnGetFile;
	public Button btnTransferFile;
	private Group grpEdsf_1;
	private Label lblNewLabel_2;
	public Text txtProgram;
	private Label lblBootLoader_2;
	private Label lblBootLoader_3;
	public Text txtFFinalMaterial;
	public Text txtFInverterID;
	private Label lblBootLoader_4;
	private Label lblBootLoader_5;
	public Text txtRInverterID;
	public Text txtRFinalMaterial;

	public EmotorFlashingSW_Dialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL | SWT.ON_TOP);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Emotor Flashing SW");
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;

		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(1, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			grpEdsf_1 = new Group(container, SWT.NONE);
			grpEdsf_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			grpEdsf_1.setText("FRS Info");
			grpEdsf_1.setLayout(new GridLayout(2, false));
			
			lblNewLabel_2 = new Label(grpEdsf_1, SWT.NONE);
			lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_2.setText("Program:");
			
			txtProgram = new Text(grpEdsf_1, SWT.BORDER | SWT.READ_ONLY);
			txtProgram.setEditable(false);
			txtProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Group grpEdsf = new Group(container, SWT.NONE);
			grpEdsf.setText("EDS_F");
			grpEdsf.setLayout(new GridLayout(2, false));
			grpEdsf.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblBootLoader_2 = new Label(grpEdsf, SWT.NONE);
			lblBootLoader_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblBootLoader_2.setText("Inverter ID:");
			
			txtFInverterID = new Text(grpEdsf, SWT.BORDER | SWT.READ_ONLY);
			txtFInverterID.setEditable(false);
			txtFInverterID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtFInverterID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblBootLoader_3 = new Label(grpEdsf, SWT.NONE);
			lblBootLoader_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblBootLoader_3.setText("Final Material:");
			
			txtFFinalMaterial = new Text(grpEdsf, SWT.BORDER | SWT.READ_ONLY);
			txtFFinalMaterial.setEditable(false);
			txtFFinalMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtFFinalMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblNewLabel = new Label(grpEdsf, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setText("App:");

			txtFAppFile = new Text(grpEdsf, SWT.BORDER | SWT.READ_ONLY);
			txtFAppFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtFAppFile.setEditable(false);
			txtFAppFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblBootLoader = new Label(grpEdsf, SWT.NONE);
			lblBootLoader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblBootLoader.setText("Boot Loader:");

			txtFBootLoaderFile = new Text(grpEdsf, SWT.BORDER | SWT.READ_ONLY);
			txtFBootLoaderFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtFBootLoaderFile.setEditable(false);
			txtFBootLoaderFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblNewLabel_1_1 = new Label(grpEdsf, SWT.NONE);
			lblNewLabel_1_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_1_1.setText("Flash Driver:");

			txtFFlashDriverFile = new Text(grpEdsf, SWT.BORDER | SWT.READ_ONLY);
			txtFFlashDriverFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtFFlashDriverFile.setEditable(false);
			txtFFlashDriverFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Group grpEdsr = new Group(container, SWT.NONE);
			grpEdsr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			grpEdsr.setText("EDS_R");
			grpEdsr.setLayout(new GridLayout(2, false));
			
			lblBootLoader_4 = new Label(grpEdsr, SWT.NONE);
			lblBootLoader_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblBootLoader_4.setText("Inverter ID:");
			
			txtRInverterID = new Text(grpEdsr, SWT.BORDER | SWT.READ_ONLY);
			txtRInverterID.setEditable(false);
			txtRInverterID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtRInverterID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblBootLoader_5 = new Label(grpEdsr, SWT.NONE);
			lblBootLoader_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblBootLoader_5.setText("Final Material:");
			
			txtRFinalMaterial = new Text(grpEdsr, SWT.BORDER | SWT.READ_ONLY);
			txtRFinalMaterial.setEditable(false);
			txtRFinalMaterial.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtRFinalMaterial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblNewLabel_1 = new Label(grpEdsr, SWT.NONE);
			lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_1.setText("App:");

			txtRAppFile = new Text(grpEdsr, SWT.BORDER | SWT.READ_ONLY);
			txtRAppFile.setEditable(false);
			txtRAppFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtRAppFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblBootLoader_1 = new Label(grpEdsr, SWT.NONE);
			lblBootLoader_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblBootLoader_1.setText("Boot Loader:");

			txtRBootLoaderFile = new Text(grpEdsr, SWT.BORDER | SWT.READ_ONLY);
			txtRBootLoaderFile.setEditable(false);
			txtRBootLoaderFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtRBootLoaderFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Label lblNewLabel_1_1_1 = new Label(grpEdsr, SWT.NONE);
			lblNewLabel_1_1_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_1_1_1.setText("Flash Driver:");

			txtRFlashDriverFile = new Text(grpEdsr, SWT.BORDER | SWT.READ_ONLY);
			txtRFlashDriverFile.setEditable(false);
			txtRFlashDriverFile.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtRFlashDriverFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Emotor Flashing SW");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnTransferFile = createButton(parent, IDialogConstants.CLOSE_ID, "Transfer File", false);
		btnGetFile = createButton(parent, IDialogConstants.CLOSE_ID, "Get File", false);
	}
}
