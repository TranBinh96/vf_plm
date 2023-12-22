package com.vf.dialog;

import java.util.LinkedHashMap;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.Query;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class SearchItemDialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	private TCSession session;
	private Text txtInput;
	public Table tblItem;
	public Button btnAccept;
	private String queryName;
	
	public SearchItemDialog(Shell parentShell, TCSession _session, String _query) {
		super(parentShell);
		session = _session;
		queryName = _query;
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("");
		setMessage("Please fill values to search.", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		TCComponentItemRevision selectedObject = null;

		try {
			//init object UI
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Label lblTitle = new Label(container, SWT.NONE);
			lblTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblTitle.setText("Part Number");
			
			txtInput = new Text(container, SWT.BORDER);
			txtInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtInput.addTraverseListener(new org.eclipse.swt.events.TraverseListener() {
				public void keyTraversed(TraverseEvent event) {
					if(event.detail == SWT.TRAVERSE_RETURN) {
						searchAction();
					}
				}
			});
			
			Button btnSearch = new Button(container, SWT.NONE);
			btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			btnSearch.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					searchAction();
				}
			});
			
			tblItem = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblItem.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			tblItem.setHeaderVisible(true);
			tblItem.setLinesVisible(true);
			
			TableColumn tblclmnObject = new TableColumn(tblItem, SWT.CENTER);
			tblclmnObject.setWidth(200);
			tblclmnObject.setText("Object");
			
			TableColumn tblclmnType = new TableColumn(tblItem, SWT.CENTER);
			tblclmnType.setWidth(200);
			tblclmnType.setText("Type");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnAccept = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		btnAccept.setText("Accept");
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Search Dialog");
	}
	
	private void searchAction() {
		TCComponent[] objects = null;
		String partNumber = txtInput.getText();

		if(partNumber.isEmpty()) {
			setMessage("Search fields cannot be empty.", IMessageProvider.WARNING);
		}
		else {
			try {
				LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
				inputQuery.put("Item ID", partNumber + "*");
				objects = Query.queryItem(session, inputQuery, queryName);
				if(objects != null) {
					setMessage("Please fill values to search.", IMessageProvider.INFORMATION);
					tblItem.removeAll();
					tblItem.redraw();
					
					for(TCComponent line : objects) {
						TableItem item = new TableItem(tblItem, SWT.NONE);
						item.setText(new String[] { line.getPropertyDisplayableValue("object_string"), 
													line.getPropertyDisplayableValue("object_type")});
						item.setData(line);
					}
				}
				else {
					setMessage("No objects found with search criteria.", IMessageProvider.WARNING);
				}	
			} 
			catch (NotLoadedException e) {
				
			}
		}
	}
}
