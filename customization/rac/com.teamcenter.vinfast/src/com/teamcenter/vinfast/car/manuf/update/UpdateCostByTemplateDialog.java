package com.teamcenter.vinfast.car.manuf.update;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import org.eclipse.swt.widgets.Label;

public class UpdateCostByTemplateDialog extends TitleAreaDialog{

	private Composite area;
	private Composite container;
	
	public Text textAttachment;
	public TCSession session = null;
	public Button btnOK;
	public Button btnDownloadTemplate;
	public Button btnAttachment;
	public Button btnCompleteTsk;
	
	public UpdateCostByTemplateDialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		area = (Composite) super.createDialogArea(parent);
		try {
			setMessage("");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			setTitle("Please upload CSV file");

			container = new Composite(area, SWT.BORDER);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
			gd_table.widthHint = 150;
			gd_table.heightHint = 150;
	
			textAttachment = new Text(container, SWT.BORDER);
			textAttachment.setEnabled(false);
			textAttachment.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			textAttachment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			textAttachment.setToolTipText("Choose file type *.csv (Optiona)");
	
			btnAttachment = new Button(container, SWT.NONE);
			btnAttachment.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnAttachment.setImage(new Image(parent.getDisplay(), UpdateCostByTemplateDialog.class.getClassLoader().getResourceAsStream("icons/importobjects_16.png")));
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
			
			btnCompleteTsk = new Button(container, SWT.CHECK);
			btnCompleteTsk.setText("Complete Task");
			new Label(container, SWT.NONE);
		} 
		catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Update Cost by Template");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(570, 299);
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		return;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Auto-generated method stub
		super.createButtonsForButtonBar(parent);
		btnOK = getButton(IDialogConstants.OK_ID);
		btnOK.setEnabled(false);
		btnOK.setText("OK");
		btnOK.setToolTipText("Click to upload data");
		
		btnDownloadTemplate = getButton(CANCEL);
		btnDownloadTemplate.setText("Download File");
		btnDownloadTemplate.setToolTipText("Click to download template file");
		btnDownloadTemplate.setSize(400, 250);
	}

}
