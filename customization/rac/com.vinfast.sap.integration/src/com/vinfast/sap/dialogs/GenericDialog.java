package com.vinfast.sap.dialogs;

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
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCSession;

public class GenericDialog extends TitleAreaDialog {
	public Combo comboServer;
	public Button btnSave;
	private Text textPlant;
	private Text textMCN;
	private ProgressBar progressBar;
	TCSession session;

	public GenericDialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Dry run to see error report and transfer", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		GridData gd_container = new GridData(GridData.FILL_BOTH);
		gd_container.verticalIndent = 5;
		container.setLayoutData(gd_container);

		Label lblMCN = new Label(container, SWT.NONE);
		GridData gd_textMCN = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_textMCN.verticalIndent = 10;
		gd_textMCN.horizontalIndent = 5;
		lblMCN.setLayoutData(gd_textMCN);
		lblMCN.setText("MCN:");

		textMCN = new Text(container, SWT.BORDER);
		textMCN.setEditable(false);
		GridData gd_text_1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text_1.verticalIndent = 10;
		gd_text_1.horizontalIndent = 5;
		textMCN.setLayoutData(gd_text_1);

		Label lblServer = new Label(container, SWT.NONE);
		GridData gd_lblServer = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblServer.verticalIndent = 10;
		gd_lblServer.horizontalIndent = 5;
		lblServer.setLayoutData(gd_lblServer);
		lblServer.setText("SERVER:");

		comboServer = new Combo(container, SWT.READ_ONLY);
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_combo.verticalIndent = 10;
		gd_combo.horizontalIndent = 5;
		comboServer.setLayoutData(gd_combo);
		comboServer.setItems(new String[] { "PRODUCTION", "QA", "DEV" });

		Label lblShop = new Label(container, SWT.NONE);
		GridData gd_lblShop = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblShop.verticalIndent = 10;
		gd_lblShop.horizontalIndent = 5;
		lblShop.setLayoutData(gd_lblShop);
		lblShop.setText("PLANT:");

		textPlant = new Text(container, SWT.BORDER);
		GridData gd_text_2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text_2.verticalIndent = 10;
		gd_text_2.horizontalIndent = 5;
		textPlant.setLayoutData(gd_text_2);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		progressBar = new ProgressBar(container, SWT.NONE);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
		gd_progressBar.verticalIndent = 10;
		progressBar.setLayoutData(gd_progressBar);

		return area;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SAP MES Interface...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 300);
	}

	@Override
	public void setTitle(String newTitle) {
		// TODO Auto-generated method stub
		super.setTitle(newTitle);
	}

	public void setProgressVisible(boolean status) {

		progressBar.setVisible(true);
		progressBar.getShell().redraw();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Auto-generated method stub
		btnSave = createButton(parent, IDialogConstants.OK_ID, "Transfer", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public TCSession getSession() {
		return session;
	}

	public void setSession(TCSession session) {
		this.session = session;
	}

	public void setMCN(String MCN) {

		this.textMCN.setText(MCN);
	}

	public void setPlant(String plant) {

		this.textPlant.setText(plant);
	}

	public void setServer(String server) {

		this.comboServer.setText(server);
	}

	public String getMCN() {

		return this.textMCN.getText();
	}

	public String getPlant() {

		return this.textPlant.getText();
	}

	public String getServer() {

		return this.comboServer.getText();
	}

	public Button getOkButton() {

		return this.btnSave;
	}

	public void setProgressStatus(int percentage) {

		this.progressBar.setSelection(percentage);
		this.progressBar.redraw();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		// super.okPressed();
	}

}
