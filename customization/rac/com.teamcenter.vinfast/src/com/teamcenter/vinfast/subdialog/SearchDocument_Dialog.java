package com.teamcenter.vinfast.subdialog;

import java.util.LinkedHashMap;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.UserList;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class SearchDocument_Dialog extends Dialog {
	TCSession session;
	private Table tblSearch = null;
	private Button ok = null;

	private Label lblSORNumber;
	private Label lblSORName;
	private Text txtSORNumber;
	private Text txtSORName;
	private Combo cbOwner;
	private Combo cbModelCode;

	private TableItem[] searchItems = null;
	private String DocumentType = "";
	private LinkedHashMap<String, TCComponent> mapSORCompo = null;
	private Text txtRevision;

	public SearchDocument_Dialog(Shell parent, int style, String docType) {
		super(parent);
		setBlockOnOpen(false);
		DocumentType = docType;
		mapSORCompo = new LinkedHashMap<String, TCComponent>();
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();
		LinkedHashMap<String, String> listUserNameOS = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> modelCodeValue = new LinkedHashMap<String, String>();
		int defaultVal = 0;
		String[] modelCodeDataForm = null;
		try {
			defaultVal = getAllUser(session, listUserNameOS);
			modelCodeDataForm = TCExtension.GetPreferenceValues("VINF_SPECBOOK_MODEL_CODE", session);
			modelCodeValue = StringExtension.GetComboboxValue(modelCodeDataForm, "AUTOMOBILE");
		} catch (TCException e) {
			e.printStackTrace();
		}

		GridLayout gl_shell = new GridLayout(3, false);
		container.setLayout(gl_shell);

		lblSORNumber = new Label(container, SWT.NONE);
		lblSORNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblSORNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblSORNumber.setText(DocumentType + " Number:");

		txtSORNumber = new Text(container, SWT.BORDER);
		txtSORNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtSORNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSORNumber.setFocus();

		Button btnSearch = new Button(container, SWT.NONE);
		btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
		btnSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnSearch.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				searchDoc();
			}
		});

		Label lblRevision = new Label(container, SWT.NONE);
		lblRevision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblRevision.setText(DocumentType + " Revision:");
		lblRevision.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		txtRevision = new Text(container, SWT.BORDER);
		txtRevision.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtRevision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		lblSORName = new Label(container, SWT.NONE);
		lblSORName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblSORName.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblSORName.setText(DocumentType + " Name:");

		txtSORName = new Text(container, SWT.BORDER);
		txtSORName.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtSORName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSORName.addListener(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					searchDoc();
				}
			}
		});

		new Label(container, SWT.NONE);

		Label lblOwner = new Label(container, SWT.NONE);
		lblOwner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblOwner.setText("Owner:");
		lblOwner.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbOwner = new Combo(container, SWT.NONE | SWT.READ_ONLY);
		cbOwner.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		StringExtension.UpdateValueTextCombobox(cbOwner, listUserNameOS);
//		cbOwner.setItems(listUserNameOS.toArray(new String[listUserNameOS.size()]));
		cbOwner.select(defaultVal);
		cbOwner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbOwner.addListener(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					searchDoc();
				}
			}
		});

		Button btnOwnerClear = new Button(container, SWT.NONE);
		btnOwnerClear.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
		btnOwnerClear.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				cbOwner.deselectAll();
			}
		});

		Label lblModelCode = new Label(container, SWT.NONE);
		lblModelCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblModelCode.setText("Model Code:");
		lblModelCode.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));

		cbModelCode = new Combo(container, SWT.NONE | SWT.READ_ONLY);
		cbModelCode.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbModelCode.setItems(new String[] {});
		cbModelCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbModelCode.select(0);
		StringExtension.UpdateValueTextCombobox(cbModelCode, modelCodeValue);

		Button btnModelCodeClear = new Button(container, SWT.NONE);
		btnModelCodeClear.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
		btnModelCodeClear.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				cbModelCode.deselectAll();
			}
		});

		tblSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		tblSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		tblSearch.setHeaderVisible(true);
		tblSearch.setLinesVisible(true);

		TableColumn sorName = new TableColumn(tblSearch, SWT.NONE);
		sorName.setResizable(true);
		sorName.setWidth(200);
		sorName.setText("Name");

		TableColumn owner = new TableColumn(tblSearch, SWT.NONE);
		owner.setResizable(true);
		owner.setWidth(200);
		owner.setText("Owner");

		TableColumn type = new TableColumn(tblSearch, SWT.NONE);
		type.setResizable(true);
		type.setWidth(200);
		type.setText("Type");

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

		txtSORNumber.addListener(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					searchDoc();
				}
			}
		});

		return container;
	}

	private int getAllUser(TCSession session, LinkedHashMap<String, String> output) throws TCException {
		int defaultIndex = 0;
		TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");
		UserList allUser;
		allUser = userType.getUserListByUser("*");
		if (allUser != null && allUser.getUserIds().length > 0) {
			for (int i = 0; i < allUser.getUserIds().length; i++) {
				String userName = allUser.getUserNames()[i];
				String userID = allUser.getUserIds()[i];
				String OSName = userName + " (" + userID + ")";
				output.put(userID, OSName);
				if (session.getUser().toString().compareToIgnoreCase(OSName) == 0) {
					defaultIndex = i;
				}
			}
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
		newShell.setText("Document Search Dialog");
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

	public LinkedHashMap<String, TCComponent> getMapSORCompo() {
		return mapSORCompo;
	}

	private void searchDoc() {
		TCComponent[] objects = null;
		String id = txtSORNumber.getText();
		String rev = txtRevision.getText();
		String name = txtSORName.getText();
		String owningUser = (String) cbOwner.getData(cbOwner.getText());
		String modelCode = (String) cbModelCode.getData(cbModelCode.getText());
		if (owningUser == null)
			owningUser = "";
		if (id.isEmpty() && name.isEmpty() && owningUser.isEmpty() && modelCode.isEmpty()) {
			MessageBox.post("Search fields cannot be empty.", "ERROR", MessageBox.ERROR);
		} else {
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			if (!id.isEmpty())
				queryInput.put("ID", id);
			if (!rev.isEmpty())
				queryInput.put("Revision", rev);
			if (!name.isEmpty())
				queryInput.put("Name", name);
			if (owningUser != null && !owningUser.isEmpty())
				queryInput.put("Owning User", owningUser);
			if (modelCode != null && !modelCode.isEmpty())
				queryInput.put("Model Code", modelCode);
			queryInput.put("Vehicle Category", "AUTOMOBILE");
			queryInput.put("Document Type", DocumentType);
			if (DocumentType.compareToIgnoreCase("DVPR") == 0)
				objects = Query.queryItem(session, queryInput, "__Automobile Document Revision");
			else
				objects = Query.queryItem(session, queryInput, "__Specification Document Item");

			if (objects != null) {
				tblSearch.removeAll();
				DataManagementService dmService = DataManagementService.getService(session);
				dmService.getProperties(objects, new String[] { "object_string", "owning_user", "object_type" });

				for (TCComponent obj : objects) {
					try {
						String[] propValues = obj.getProperties(new String[] { "object_string", "owning_user", "object_type" });
						mapSORCompo.put(propValues[0], obj);
						setTableRow(propValues);
					} catch (TCException e) {
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
