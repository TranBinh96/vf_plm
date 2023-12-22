package com.teamcenter.vinfast.productconfigurator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import org.eclipse.wb.swt.SWTResourceManager;

public class CfgContextCreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnOk;
	
	public Combo cbPropulsion;
	public Combo cbSegnSize;
	public Combo cbBodyStyle;
	public Combo cbModelYear;
	public Combo cbVehType;
	public Combo cbVehProgram;
	
	public Text txtVehGen;
	public Text txtAutoID;
	public Text txtName;
	public Text txtDesc;
	
	private Label lblModelYear;
	private Label lblAutoID;
	private Label lblName;
	private Label lblDecription;
	private Label lblVehicleType;
	public Label lblVehicleProgram;
	
	public Label lblPropulsion;
	public Label lblSegSize;
	public Label lblVehGen;
	public Label lblBodyStyle;

	public CfgContextCreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			lblVehicleType = new Label(container, SWT.NONE);
			lblVehicleType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVehicleType.setText("Vehicle Type: (*)");
			
			cbVehType = new Combo(container, SWT.READ_ONLY);
			cbVehType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbVehType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblPropulsion = new Label(container, SWT.NONE);
			lblPropulsion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPropulsion.setText("Propulsion: (*)");
			cbPropulsion = new Combo(container, SWT.READ_ONLY);
			cbPropulsion.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPropulsion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			
			lblSegSize = new Label(container, SWT.NONE);
			lblSegSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblSegSize.setText("Segment/Size: (*)");
			cbSegnSize = new Combo(container, SWT.READ_ONLY);
			cbSegnSize.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbSegnSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblVehGen = new Label(container, SWT.NONE);
			lblVehGen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVehGen.setText("Vehicle Generation:   ");
			txtVehGen = new Text(container, SWT.READ_ONLY | SWT.BORDER);
			txtVehGen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			
			lblBodyStyle = new Label(container, SWT.NONE);
			lblBodyStyle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblBodyStyle.setText("Body Style: (*)");
			cbBodyStyle = new Combo(container, SWT.READ_ONLY);
			cbBodyStyle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbBodyStyle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			
			lblModelYear = new Label(container, SWT.NONE);
			lblModelYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblModelYear.setText("Model Year: (*)");
			
			cbModelYear = new Combo(container, SWT.READ_ONLY);
			cbModelYear.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbModelYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblAutoID = new Label(container, SWT.NONE);
			lblAutoID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblAutoID.setText("ID:");
			
			txtAutoID = new Text(container, SWT.READ_ONLY | SWT.BORDER);
			txtAutoID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtAutoID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblName = new Label(container, SWT.NONE);
			lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblName.setText("Name: (*)");
			
			txtName = new Text(container, SWT.NONE | SWT.BORDER);
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblDecription = new Label(container, SWT.NONE);
			lblDecription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblDecription.setText("Decription:");
			
			txtDesc = new Text(container, SWT.NONE | SWT.BORDER);
			txtDesc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblVehicleProgram = new Label(container, SWT.NONE);
			lblVehicleProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVehicleProgram.setText("EScooter Model/Vehicle Line: (*)");
			
			cbVehProgram = new Combo(container, SWT.READ_ONLY);
			cbVehProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbVehProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		catch(Exception ex ) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnOk = createButton(parent, IDialogConstants.OK_ID, "Create", true);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Create Configurator Context");
	}
	
	@Override
	protected void okPressed() {
		
	}
}
