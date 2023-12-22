package com.teamcenter.vinfast.car.engineering.create;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.UserList;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.Query;

public class SearchDesignDialog extends AbstractSWTDialog {

	private Table tblSearch = null;
	private Button ok = null;
	private TableItem[] searchItems = null;
	private LinkedHashMap<String, TCComponent> mapDesignCompo = null;
	private String typeSearch = "";

	public SearchDesignDialog(Shell parent, int style, String typeSearch) {
		super(parent, style);
		setBlockOnOpen(false);
		mapDesignCompo = new LinkedHashMap<String, TCComponent>();
		this.typeSearch = typeSearch;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
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

		Label lblSORNumber = new Label(container, SWT.NONE);
		lblSORNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblSORNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblSORNumber.setText("ID");

		Text txtSORNumber = new Text(container, SWT.BORDER);
		txtSORNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtSORNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSORNumber.setFocus();

		Button btnSearch = new Button(container, SWT.NONE);
		btnSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnSearch.setText("Search");
		new Label(container, SWT.NONE);

		Label lblSORName = new Label(container, SWT.NONE);
		GridData gd_lblSORName = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblSORName.widthHint = 50;
		lblSORName.setLayoutData(gd_lblSORName);
		lblSORName.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblSORName.setText("Name");

		Text txtSORName = new Text(container, SWT.BORDER);
		txtSORName.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		GridData gd_txtSORName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtSORName.widthHint = 50;
		txtSORName.setLayoutData(gd_txtSORName);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		Label lblOwner = new Label(container, SWT.NONE);
		lblOwner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblOwner.setText("Owner");
		lblOwner.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		Combo cbOwner = new Combo(container, SWT.NONE);
		ArrayList<String> listUserNameOS = new ArrayList<String>();
		int defaultVal = getAllUser(session, listUserNameOS);
		cbOwner.setItems(listUserNameOS.toArray(new String[listUserNameOS.size()]));
		cbOwner.select(defaultVal);
		cbOwner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		tblSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		tblSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		tblSearch.setHeaderVisible(true);
		tblSearch.setLinesVisible(true);

		TableColumn tblDesign = new TableColumn(tblSearch, SWT.NONE);
		tblDesign.setResizable(true);
		tblDesign.setWidth(200);
		tblDesign.setText("Name");

		TableColumn owner = new TableColumn(tblSearch, SWT.NONE);
		owner.setResizable(true);
		owner.setWidth(200);
		owner.setText("Owner");

		TableColumn type = new TableColumn(tblSearch, SWT.NONE);
		type.setResizable(true);
		type.setWidth(200);
		type.setText("Type");

		txtSORNumber.addListener(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					queryDesignRevision(session, txtSORNumber, txtSORName, cbOwner);
				}
			}
		});

		txtSORName.addListener(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					queryDesignRevision(session, txtSORNumber, txtSORName, cbOwner);
				}
			}
		});

		cbOwner.addListener(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					queryDesignRevision(session, txtSORNumber, txtSORName, cbOwner);
				}
			}

		});

		btnSearch.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				queryDesignRevision(session, txtSORNumber, txtSORName, cbOwner);
			}
		});

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

		return container;
	}

	private int getAllUser(TCSession session, ArrayList<String> output) {
		int defaultIndex = 0;
		try {
			TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");
			UserList allUser;
			allUser = userType.getUserListByUser("*");
			if (allUser != null && allUser.getUserIds().length > 0) {
				for (int i = 0; i < allUser.getUserIds().length; i++) {
					String userName = allUser.getUserNames()[i];
					String userID = allUser.getUserIds()[i];
					String OSName = userName + " (" + userID + ")";
					output.add(OSName);
					if (session.getUser().toString().compareToIgnoreCase(OSName) == 0) {
						defaultIndex = i;
					}
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return defaultIndex;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		ok = getButton(IDialogConstants.OK_ID);
		ok.setEnabled(false);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Search Old Part");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 400);
	}

	public Button getOKButton() {
		return getButton(IDialogConstants.OK_ID);
	}

	public void setSelectedTableItems(TableItem[] items) {
		this.searchItems = items;
	}

	public Table getSearchTable() {
		return this.tblSearch;
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
	}

	public TableItem[] getSelectedTableItems() {

		return this.searchItems;
	}

	private void setTableRow(String[] values) {
		TableItem row = new TableItem(this.tblSearch, SWT.NONE);
		row.setText(values);
	}

	public LinkedHashMap<String, TCComponent> getMapDesignCompo() {
		return mapDesignCompo;
	}

	private void queryDesignRevision(TCSession session, Text itemID, Text itemName, Combo cbOwner) {
		TCComponent[] objects = null;
		String id = itemID.getText();
		String name = itemName.getText();
		String owningUser = "";
		if (!cbOwner.getText().isEmpty())
			owningUser = cbOwner.getItem(cbOwner.getSelectionIndex());
		if (id.equals("") && name.equals("") && owningUser.equals("")) {
			MessageBox.post("Search fields cannot be empty.", "ERROR", MessageBox.ERROR);
		} else {
			if (id.equals("")) {
				id = "*";
			}
			if (name.equals("")) {
				name = "*";
			}
			if (owningUser.equals("")) {
				owningUser = "*";
			}
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			queryInput.put("Item ID", id);
			queryInput.put("Name", name);
			queryInput.put("Owning User", owningUser);
			queryInput.put("Type", typeSearch);
			objects = Query.queryItem(session, queryInput, "Item...");

			if (objects != null) {

				tblSearch.removeAll();
				DataManagementService dmService = DataManagementService.getService(session);
				dmService.getProperties(objects, new String[] { "object_string", "owning_user", "object_type" });

				for (TCComponent obj : objects) {
					try {
						String[] propValues = obj.getProperties(new String[] { "object_string", "owning_user", "object_type" });
						mapDesignCompo.put(propValues[0], obj);
						setTableRow(propValues);
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
					}
				}
				tblSearch.redraw();
			} else {
				MessageBox.post("No objects found with search criteria..", "Please input valid ID and re-try", "Query...", MessageBox.ERROR);
				return;
			}
		}
	}

}
