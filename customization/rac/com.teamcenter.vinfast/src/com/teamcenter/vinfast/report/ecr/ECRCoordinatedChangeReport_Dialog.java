package com.teamcenter.vinfast.report.ecr;

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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.util.MessageBox;

public class ECRCoordinatedChangeReport_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;

	public List lstModule;
	public Button btnModuleAdd;
	public Button btnModuleRemove;
	public Combo cbModule;

	public List lstProgram;
	public Button btnProgramAdd;
	public Button btnProgramRemove;
	public Combo cbProgram;

	public ECRCoordinatedChangeReport_Dialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);

		try {
			setTitle("ECR Coordinated Change");

			container = new Composite(area, SWT.BORDER);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblPartNumber = new Label(container, SWT.NONE);
			lblPartNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblPartNumber.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
			lblPartNumber.setText("Program:");

			lstProgram = new List(container, SWT.BORDER);
			GridData gd_lstProgram = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_lstProgram.heightHint = 100;
			lstProgram.setLayoutData(gd_lstProgram);

			btnProgramRemove = new Button(container, SWT.NONE);
			btnProgramRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnProgramRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			new Label(container, SWT.NONE);

			cbProgram = new Combo(container, SWT.READ_ONLY);
			cbProgram.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
			cbProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			btnProgramAdd = new Button(container, SWT.NONE);
			btnProgramAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));

			Label lblRevisionRule = new Label(container, SWT.NONE);
			lblRevisionRule.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblRevisionRule.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblRevisionRule.setText("Module:");

			lstModule = new List(container, SWT.BORDER);
			GridData gd_lstModule = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_lstModule.heightHint = 100;
			lstModule.setLayoutData(gd_lstModule);

			btnModuleRemove = new Button(container, SWT.NONE);
			btnModuleRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnModuleRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			new Label(container, SWT.NONE);

			cbModule = new Combo(container, SWT.READ_ONLY);
			cbModule.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
			cbModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			btnModuleAdd = new Button(container, SWT.NONE);
			btnModuleAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));

			Label label = new Label(container, SWT.HORIZONTAL);
			GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
			gd_label.widthHint = 431;
			label.setLayoutData(gd_label);
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Report");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(470, 482);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Export", true);
	}
}
