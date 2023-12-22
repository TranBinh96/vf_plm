package com.vinfast.sap.bomtracking;


import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.kernel.TCSession;
import com.vinfast.car.sap.superbom.BomTrackingModel;


public class BomTrackingImportDialog extends TitleAreaDialog{
	public Button btnSave;
	public Button btnMerge;
	public Button getBtnMerge() {
		return btnMerge;
	}





	TCSession session;
	private Table tblItem;
	private TableColumn tblclmnNewColumn;
	private TableColumn tblclmnNewColumn_1;
	private TableColumn tblclmnNewColumn_2;
	private TableColumn tblclmnNewColumn_3;
	private TableColumn tblclmnNewColumn_4;
	private TableColumn tblclmnNewColumn_5;
	private TableColumn tblclmnNewColumn_6;
	private TableColumn tblclmnNewColumn_7;
	private HashSet<String> setSelectedLine = new HashSet<String>();
	
	public HashSet<String> getSetSelectedLine() {
		return setSelectedLine;
	}

	public BomTrackingImportDialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setHelpAvailable(false);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Dry run to see error report and transfer",IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
//		progressBar.setState(SWT.NORMAL);
		
		tblItem = new Table(container, SWT.BORDER | SWT.CHECK);
		tblItem.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		tblItem.setHeaderVisible(true);
		tblItem.setLinesVisible(true);
		
		CheckboxTableViewer tableViewer = new CheckboxTableViewer(tblItem);
		tableViewer.setAllGrayed(true);
		tableViewer.setAllChecked(true);
		
		tblclmnNewColumn_5 = new TableColumn(tblItem, SWT.NONE);
		tblclmnNewColumn_5.setWidth(25);
		
		tblclmnNewColumn_6 = new TableColumn(tblItem, SWT.NONE);
		tblclmnNewColumn_6.setWidth(60);
		tblclmnNewColumn_6.setText("Action");
		
		tblclmnNewColumn = new TableColumn(tblItem, SWT.NONE);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("Main Group");
		
		tblclmnNewColumn_1 = new TableColumn(tblItem, SWT.NONE);
		tblclmnNewColumn_1.setWidth(100);
		tblclmnNewColumn_1.setText("Sub Group");
		
		tblclmnNewColumn_2 = new TableColumn(tblItem, SWT.NONE);
		tblclmnNewColumn_2.setWidth(100);
		tblclmnNewColumn_2.setText("Bomline ID");
		
		tblclmnNewColumn_3 = new TableColumn(tblItem, SWT.NONE);
		tblclmnNewColumn_3.setWidth(100);
		tblclmnNewColumn_3.setText("Part Number");
		
		tblclmnNewColumn_4 = new TableColumn(tblItem, SWT.NONE);
		tblclmnNewColumn_4.setWidth(100);
		tblclmnNewColumn_4.setText("Quantity");
		
		tblclmnNewColumn_7 = new TableColumn(tblItem, SWT.NONE);
		tblclmnNewColumn_7.setWidth(100);
		tblclmnNewColumn_7.setText("New Part");
		
		tblclmnNewColumn_4 = new TableColumn(tblItem, SWT.NONE);
		tblclmnNewColumn_4.setWidth(100);
		tblclmnNewColumn_4.setText("Replaced Part");
		
		tblItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				if( event.detail == SWT.CHECK ) {
					for (int i = 0; i < tblItem.getItemCount(); i++) {
						TableItem item = tblItem.getItem(i);
						if(item.getChecked()) {
							setSelectedLine.add(String.format("%s~%s",item.getText(3), item.getText(4)));
						}else {
							setSelectedLine.remove(String.format("%s~%s",item.getText(3), item.getText(4)));
						}
						System.out.println(item.getText(5) + "   " + item.getChecked());
					}
			    }
			}
		});
		
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
		return new Point(773, 506);
	}
	
	@Override
	public void setTitle(String newTitle) {
		super.setTitle(newTitle);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSave = createButton(parent, IDialogConstants.OK_ID, "Import", true);
		btnMerge = createButton(parent, IDialogConstants.OK_ID, "Merge", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	public TCSession getSession() {
		return session;
	}

	public void setSession(TCSession session) {
		this.session = session;
	}
	
	
	public void setTableItem(ArrayList<BomTrackingModel> list) {
		for(TableItem item : tblItem.getItems()) {
			item.dispose();
		}
		
		for (BomTrackingModel model : list) {
			try {
				TableItem item1 = new TableItem(tblItem, SWT.NONE);
			    item1.setText(new String[] { "",
			    							 model.getActionType(),
			    							 model.getMainGroup(), 
			    							 model.getSubGroup(),
			    							 model.getBomlineID(),
			    							 model.getPartNumber(),
			    							 model.getQuantity(),
			    							 model.getNewPartNumber(),
			    							 model.getReplacedPartNumber()});
			    String check = "false";
			    item1.setChecked(check.compareToIgnoreCase("true") == 0);
			} catch (Exception e) {
				
			}
		}
	}
	public Table getTableItem() {
		return this.tblItem;
	}
	
	public Button getSaveButton() {
		
		return this.btnSave;
	}
	
	@Override
	protected void okPressed() {
		//super.okPressed();
	}

}
