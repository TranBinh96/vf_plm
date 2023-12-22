package com.vf.dialog;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.jface.fieldassist.ControlDecoration;

public class VFFileChooserDialog extends TitleAreaDialog {

	private Text itxtFilePath = null;
	private Button btnMigrate = null;
			
	public VFFileChooserDialog(Shell parentShell, String name) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setTitle(name);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		setMessage("Please choose file listing Operations ID", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);

		try {

			//init object UI
			Composite container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			gd_container.heightHint = 105;
			container.setLayoutData(gd_container);

			// ECR/MCN ID
			Label lableFilePath = new Label(container, SWT.NONE);
			lableFilePath.setText("Selected File:");
			
			itxtFilePath = new Text(container, SWT.READ_ONLY | SWT.BORDER);
			addRequiredDecoration(itxtFilePath);
			itxtFilePath.setEditable(false);
			itxtFilePath.setEnabled(false);
			GridData gd_itxtFilePath = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_itxtFilePath.widthHint = 200;
			itxtFilePath.setLayoutData(gd_itxtFilePath);
							
			Button btnNewButton = new Button(container, SWT.NONE);
			btnNewButton.setText("Choose");
			btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			btnNewButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog fileDialog =  new FileDialog(getParentShell(), SWT.OPEN | SWT.SINGLE);
					fileDialog.setFilterExtensions(new String[] {"*.TXT","*.txt"});
					fileDialog.open();
					itxtFilePath.setText(fileDialog.getFileName());
					itxtFilePath.setData(fileDialog.getFilterPath());
				}
			});
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return area;
	}

	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//		setSaveButton(createButton(parent, IDialogConstants.OK_ID, "Save", true));
		btnMigrate = createButton(parent, IDialogConstants.OK_ID, "Migrate", true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("File Choose Dialog");
	}

	@Override
	protected void okPressed() {
		// super.okPressed();
	}
	
	public File getFile() {
		return new File(itxtFilePath.getData().toString(),itxtFilePath.getText());
	}
	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 250);
	}

	public Button getButton() {
		return btnMigrate;
	}

	private ControlDecoration addRequiredDecoration(Control control) {
		ControlDecoration controlDecoration = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
		controlDecoration.setDescriptionText("Field cannot be empty");
		controlDecoration.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/Mandatory.png"));
		return controlDecoration;
	}
	
}

