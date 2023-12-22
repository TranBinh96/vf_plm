package com.teamcenter.vines.workflow;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;

public class VESCell_ECMProcessApproval_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;

	private Label lblNewLabel;
	private Label lblProgram;
	public Combo cbProgram;
	private Label lblModule;
	public Label lblCombine;
	public Combo cbCombine;
	public Text txtID;
	public Combo cbModule;
	public Text txtMessage;
	private Label label;

	public VESCell_ECMProcessApproval_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
//		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.verticalSpacing = 2;
		gridLayout.marginWidth = 5;
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(4, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				txtMessage = new Text(container, SWT.WRAP | SWT.MULTI);
				txtMessage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtMessage.setEditable(false);
				GridData gd_txtMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
				gd_txtMessage.heightHint = 65;
				txtMessage.setLayoutData(gd_txtMessage);
			}
			{
				label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
			}
			{
				lblProgram = new Label(container, SWT.NONE);
				lblProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblProgram.setText("Program: (*)");
			}
			{
				cbProgram = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbProgram.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}
			{
				lblModule = new Label(container, SWT.NONE);
				lblModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblModule.setText("Module: (*)");
			}
			{
				cbModule = new Combo(container, SWT.READ_ONLY);
				cbModule.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbModule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			{
				lblCombine = new Label(container, SWT.NONE);
				lblCombine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblCombine.setText("Combine with:");
			}
			{
				cbCombine = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbCombine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbCombine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			}
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("ECM:");
			}
			{
				txtID = new Text(container, SWT.BORDER);
				txtID.setEnabled(false);
				txtID.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtID.setEditable(false);
				txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Trigger", true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 342);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("VES ECM Process");
	}

	public void setMessage(String value, Boolean error) {
		txtMessage.setForeground(SWTResourceManager.getColor(error ? SWT.COLOR_RED : SWT.COLOR_BLACK));
		txtMessage.setText(value);
	}
}
