package com.teamcenter.vinfast.subdialog;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.Query;
import org.eclipse.wb.swt.ResourceManager;

public class SearchCompanyContact_Dialog extends Dialog {
	private TCSession session = null;
	public Table tblSearch;
	private Button ok;

	private Text txtName;
	private Text txtEmail;

	public LinkedList<TCComponent> itemSearch = null;

	public SearchCompanyContact_Dialog(Shell parent) {
		super(parent);
		setBlockOnOpen(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		itemSearch = new LinkedList<TCComponent>();
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();

		GridLayout gl_shell = new GridLayout(3, false);
		container.setLayout(gl_shell);

		Label lblPartNumber = new Label(container, SWT.NONE);
		lblPartNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPartNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblPartNumber.setText("Vendor Name:");

		txtName = new Text(container, SWT.BORDER);
		txtName.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtName.setFocus();

		Button btnSearch = new Button(container, SWT.NONE);
		btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
		btnSearch.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				loadObjects();
			}
		});

		Label lblRevision = new Label(container, SWT.NONE);
		GridData gd_lblRevision = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblRevision.widthHint = 50;
		lblRevision.setLayoutData(gd_lblRevision);
		lblRevision.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblRevision.setText("Email:");

		txtEmail = new Text(container, SWT.BORDER);
		txtEmail.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		GridData gd_txtEmail = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtEmail.widthHint = 50;
		txtEmail.setLayoutData(gd_txtEmail);
		txtEmail.addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event evt) {
				tblSearch.deselectAll();
				if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					loadObjects();
				}
				ok.setEnabled(false);
			}
		});
		new Label(container, SWT.NONE);

		tblSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tblSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		tblSearch.setHeaderVisible(true);
		tblSearch.setLinesVisible(true);
		TableColumn partDetails = new TableColumn(tblSearch, SWT.NONE);
		partDetails.setResizable(true);
		partDetails.setWidth(100);
		partDetails.setText("Vendor Code");

		TableColumn tblclmnVendorName = new TableColumn(tblSearch, SWT.NONE);
		tblclmnVendorName.setWidth(180);
		tblclmnVendorName.setText("Vendor Name");
		tblclmnVendorName.setResizable(true);

		TableColumn tblclmnVendorContact = new TableColumn(tblSearch, SWT.NONE);
		tblclmnVendorContact.setWidth(180);
		tblclmnVendorContact.setText("Vendor Contact");

		TableColumn partType = new TableColumn(tblSearch, SWT.NONE);
		partType.setResizable(true);
		partType.setWidth(200);
		partType.setText("Vendor Email");

		tblSearch.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event e) {
				int count = tblSearch.getSelectionCount();
				if (count == 0) {
					ok.setEnabled(false);
				} else {
					ok.setEnabled(true);
				}
				tblSearch.forceFocus();
			}
		});

		txtName.addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event evt) {
				tblSearch.deselectAll();
				if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					loadObjects();
				}
				ok.setEnabled(false);
			}
		});

		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		ok = createButton(parent, IDialogConstants.OK_ID, "Accept", false);
		ok.setEnabled(false);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Search Dialog...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(700, 400);
	}

	public Button getOKButton() {
		return getButton(IDialogConstants.OK_ID);
	}

	@Override
	protected void okPressed() {
	}

	public void loadObjects() {
		String name = txtName.getText();
		String email = txtEmail.getText();
		TCComponent[] objects = null;
		if (name.isEmpty() && email.isEmpty()) {
			MessageBox.post("Search criteria is empty.", "Please input the search value and re-try", "Query...", MessageBox.ERROR);
			return;
		} else {
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			if (!name.isEmpty())
				queryInput.put("Name", name);
			if (!email.isEmpty())
				queryInput.put("Email", email);

			objects = Query.queryItem(session, queryInput, "Vendor Contact Search");

			if (objects != null) {
				tblSearch.removeAll();
				itemSearch = new LinkedList<TCComponent>();
				for (TCComponent obj : objects) {
					try {
						itemSearch.add(obj);
						String vendorContact = obj.getPropertyDisplayableValue("object_name");
						String vendorEmail = obj.getPropertyDisplayableValue("email_address");
						String company = obj.getPropertyDisplayableValue("sc0VendorObject");
						String vendorCode = "";
						String vendorName = "";
						if (company.contains("-")) {
							String[] str = company.split("-");
							vendorCode = str[0];
							vendorName = str[1];
						}
						setTableRow(new String[] { vendorCode, vendorName, vendorContact, vendorEmail });
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				tblSearch.redraw();
			} else {
				MessageBox.post("No objects found with search criteria..", "Please input valid ID and re-try", "Query...", MessageBox.ERROR);
				return;
			}
		}
	}

	private void setTableRow(String[] values) {
		TableItem row = new TableItem(this.tblSearch, SWT.NONE);
		row.setText(values);
	}
}