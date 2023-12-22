package com.teamcenter.vinfast.mhu;

import java.util.LinkedHashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.Query;

import org.eclipse.swt.widgets.Combo;

public class UpdatePreferredMPNDialog extends AbstractSWTDialog {

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	private Text txtMPNPreferred;
	private Table tblMPN;
	private Button ok;
	public TableItem[] searchItems;

	public UpdatePreferredMPNDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();

		GridLayout gl_shell = new GridLayout(4, false);
		gl_shell.horizontalSpacing = 7;
		gl_shell.verticalSpacing = 7;
		gl_shell.marginRight = 10;
		gl_shell.marginLeft = 10;
		gl_shell.marginBottom = 5;
		gl_shell.marginHeight = 10;
		container.setLayout(gl_shell);

		Label lblMPNPreferred = new Label(container, SWT.NONE);
		lblMPNPreferred.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblMPNPreferred.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblMPNPreferred.setText("MPN Preferred");

		txtMPNPreferred = new Text(container, SWT.BORDER);
		txtMPNPreferred.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtMPNPreferred.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtMPNPreferred.setFocus();
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		tblMPN = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tblMPN.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		tblMPN.setHeaderVisible(true);
		tblMPN.setLinesVisible(true);

		TableColumn sorName = new TableColumn(tblMPN, SWT.NONE);
		sorName.setResizable(true);
		sorName.setWidth(200);
		sorName.setText("MPN");

		TableColumn owner = new TableColumn(tblMPN, SWT.NONE);
		owner.setResizable(true);
		owner.setWidth(200);
		owner.setText("Supplier Name");

		TableColumn type = new TableColumn(tblMPN, SWT.NONE);
		type.setResizable(true);
		type.setWidth(200);
		type.setText("Manufacturer");

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		ok = getButton(IDialogConstants.OK_ID);
	}
	
	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(598, 420);
	}

	public Table getTblMPN() {
		return tblMPN;
	}

	public void setTblMPN(Table tblMPN) {
		this.tblMPN = tblMPN;
	}
	
	public void setTableRow(String[] values) {
		TableItem row = new TableItem(this.tblMPN, SWT.NONE);
		row.setText(values);
	}

	public Text getTxtMPNPreferred() {
		return txtMPNPreferred;
	}

	public void setTxtMPNPreferred(Text txtMPNPreferred) {
		this.txtMPNPreferred = txtMPNPreferred;
	}

	public Button getOk() {
		return ok;
	}

	public void setOk(Button ok) {
		this.ok = ok;
	}
	
}
