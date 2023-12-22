package com.teamcenter.vinfast.doc.esom.view;

import java.util.LinkedHashMap;

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
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sc.utilities.Utilities;

public class ECRItemSearchDialog extends AbstractSWTDialog{
	private Table tablePartNumber;
	private Button ok;
	public TableItem[] searchItems;
	private LinkedHashMap<String, TCComponent> mapTcCompo = null;
	
	public ECRItemSearchDialog(Shell parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
		setBlockOnOpen(false);
		mapTcCompo = new LinkedHashMap<String, TCComponent>();
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

		Label lblPartNumber = new Label(container, SWT.NONE);
		lblPartNumber.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPartNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		lblPartNumber.setText("ECR Number:");

		Text textID = new Text(container, SWT.BORDER);
		textID.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
		textID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textID.setFocus();

		tablePartNumber = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tablePartNumber.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		tablePartNumber.setHeaderVisible(true);
		tablePartNumber.setLinesVisible(true);
		TableColumn partDetails = new TableColumn(tablePartNumber, SWT.NONE);
		partDetails.setResizable(true);
		partDetails.setWidth(250);
		partDetails.setText("ECR Number");
		
		TableColumn partType = new TableColumn(tablePartNumber, SWT.NONE);
		partType.setResizable(true);
		partType.setWidth(150);
		partType.setText("Object Type");
		
		textID.addListener(SWT.KeyUp, new Listener() {

			public void handleEvent(Event evt) {
				tablePartNumber.deselectAll();
				if(evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
					//do something here....
					String ID = textID.getText();
					loadObjects(session, ID);
				}
				 ok.setEnabled(false);
			}
		});
		
		tablePartNumber.addListener(SWT.MouseUp, new Listener() {
		      public void handleEvent(Event e) {
		    	  int count = tablePartNumber.getSelectionCount();
		    	  if(count == 0) {
		    		  ok.setEnabled(false);
		    	  }
		    	  else {
		    		  ok.setEnabled(true);
		    	  }
		    	  tablePartNumber.forceFocus();
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
		newShell.setText("Search Dialog...");
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
		return this.tablePartNumber;
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
	}
	
	public TableItem[] getSelectedTableItems() {
		
		return this.searchItems;
	}
	
	public void loadObjects(TCSession session, String ID) {
		TCComponent[] objects = null;

		if(ID.equals("")) {
			MessageBox.post("Search criteria is empty.", "Please input the search value and re-try", "Query...", MessageBox.ERROR);
			return; 
		}
		else {
			if(ID.equals("")) {
				ID = "*";
			}

			String[] entries = new String[] {"Item ID", "Type"};
			String[] values = new String[] {ID,"Vf6_ECR"};
			objects = new Utilities().searchObjects(session, entries, values, "Item...");

			if(objects != null) {
				
				tablePartNumber.removeAll();
				DataManagementService dmService = DataManagementService.getService(session);
				dmService.getProperties(objects, new String[] {"object_string","object_type"});
				
				for(TCComponent obj : objects) {
					
					try {
						String[] propValues = obj.getProperties(new String[] {"object_string","object_type"});
						mapTcCompo.put(propValues[0], obj);
						setTableRow(propValues);
					} catch (TCException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				tablePartNumber.redraw();
			}
			else {
				MessageBox.post("No objects found with search criteria..", "Please input valid ID and re-try", "Query...", MessageBox.ERROR);
				return; 
			}
		}
	}
	
	private void setTableRow(String[] values) {
		TableItem row = new TableItem(this.tablePartNumber, SWT.NONE);
		row.setText(values);
	}
	
	public LinkedHashMap<String, TCComponent> getMapTcCompo() {
		return mapTcCompo;
	}
}
