package com.teamcenter.vinfast.admin;

import java.util.LinkedHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.Query;

public class ReassignTaskDialog extends AbstractSWTDialog{

	private Table tblSearch;
	private Button ok;
	public TableItem[] searchItems;
	public Text txtReassignFrom;
	private LinkedHashMap<String, TCComponent> mapGrpMember = null;
	
	public ReassignTaskDialog(Shell parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
		setBlockOnOpen(false);
		mapGrpMember = new LinkedHashMap<String, TCComponent>();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setFocus();

		GridLayout gl_shell = new GridLayout(5, false);
		gl_shell.horizontalSpacing = 7;
		gl_shell.verticalSpacing = 7;
		gl_shell.marginRight = 10;
		gl_shell.marginLeft = 10;
		gl_shell.marginBottom = 5;
		gl_shell.marginHeight = 10;
		container.setLayout(gl_shell);

		Label lblReassignTo = new Label(container, SWT.NONE);
		lblReassignTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblReassignTo.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblReassignTo.setText("Re-assign To:");

		Text txtReassignTo = new Text(container, SWT.BORDER);
		txtReassignTo.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtReassignTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtReassignTo.setFocus();
		
		Label lblReassignFrom = new Label(container, SWT.NONE);
		lblReassignFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblReassignFrom.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblReassignFrom.setText("Re-assign From:");
		
		txtReassignFrom = new Text(container, SWT.BORDER);
		txtReassignFrom.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		txtReassignFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtReassignFrom.setFocus();

		Button btnSearch = new Button(container, SWT.NONE);
		btnSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnSearch.setText("Search");
		new Label(container, SWT.NONE);

		tblSearch = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tblSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
		tblSearch.setHeaderVisible(true);
		tblSearch.setLinesVisible(true);

		TableColumn user = new TableColumn(tblSearch, SWT.NONE);
		user.setResizable(true);
		user.setWidth(200);
		user.setText("User");

		TableColumn group = new TableColumn(tblSearch, SWT.NONE);
		group.setResizable(true);
		group.setWidth(200);
		group.setText("Group");

		TableColumn role = new TableColumn(tblSearch, SWT.NONE);
		role.setResizable(true);
		role.setWidth(200);
		role.setText("Role");
		
		TableColumn name = new TableColumn(tblSearch, SWT.NONE);
		name.setResizable(true);
		name.setWidth(200);
		name.setText("Name");
		
		TableColumn status = new TableColumn(tblSearch, SWT.NONE);
		status.setResizable(true);
		status.setWidth(200);
		status.setText("Status");
		
		txtReassignTo.addListener(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event evt) {
				if(evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					getGrpMember(session, txtReassignTo);
				}
			}
		});
		
		btnSearch.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				getGrpMember(session, txtReassignTo);
			}
		});
		
		tblSearch.addListener(SWT.MouseUp, new Listener() {
		      public void handleEvent(Event e) {
		    	  int count = tblSearch.getSelectionCount();
		    	  if(count == 0) {
		    		  ok.setEnabled(false);
		    	  }else {
					TableItem[] items = tblSearch.getSelection();
					if (items[0].getText(4).compareToIgnoreCase("False") == 0) {
						ok.setEnabled(true);
					} else {
						ok.setEnabled(false);
					}
		    	  }
		    	  tblSearch.forceFocus();
		      }
		});
		
		tblSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = tblSearch.getSelectionCount();
				if (count == 0) {
					ok.setEnabled(false);
				} else {
					TableItem[] items = tblSearch.getSelection();
					if (items[0].getText(4).compareToIgnoreCase("False") == 0) {
						ok.setEnabled(true);
					} else {
						ok.setEnabled(false);
					}
				}
			}
		});
		
		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		ok = getButton(IDialogConstants.OK_ID);
		ok.setEnabled(false);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Re-assign Task");
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
		if(values[4].compareToIgnoreCase("True") == 0) {
			TableEditor editor = new TableEditor(this.tblSearch);
		    editor.grabHorizontal = editor.grabVertical = true;
		    editor.setEditor(new Label(this.tblSearch, SWT.NONE), row, 1);
		    row.setForeground(this.getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
		}
		
	}
	
	public LinkedHashMap<String, TCComponent> getMapGrpMember() {
		return mapGrpMember;
	}

	private void getGrpMember(TCSession session, Text txtUserId) {
		TCComponent[] objects = null;
		String id = txtUserId.getText();
		if (id.equals("")) {
			MessageBox.post("Search fields cannot be empty.","ERROR", MessageBox.ERROR);
		} else {
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
			queryInput.put("Id", id);
			objects = Query.queryItem(session, queryInput, "Admin - User Memberships");

			if (objects != null) {

				tblSearch.removeAll();
				DataManagementService dmService = DataManagementService.getService(session);
				dmService.getProperties(objects, new String[] { "user", "group", "role", "object_string", "status"});

				for (TCComponent obj : objects) {
					try {
						String[] propValues = obj.getProperties(new String[] { "user", "group", "role", "object_string","status" });
						mapGrpMember.put(propValues[3], obj);
						setTableRow(propValues);
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						MessageBox.post("Exception: " + e.toString(),"ERROR", MessageBox.ERROR);
					}
				}
				tblSearch.redraw();
			} else {
				MessageBox.post("No objects found with search criteria..", "Please input valid ID and re-try",
						"Query...", MessageBox.ERROR);
				return;
			}
		}
	}
	
}
