package com.vinfast.sap.bomtracking;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class BomTrackingTranferDialog extends TitleAreaDialog{
	public Combo comboServer;
	public Button btnSave;
	private Text textPlant;
	private Text textMCN;
	private ProgressBar progressBar;
	TCSession session;
	private Table tblItem;
	private TableColumn tblclmnNewColumn;
	private TableColumn tblclmnNewColumn_1;
	private TableColumn tblclmnNewColumn_2;
	private TableColumn tblclmnNewColumn_3;
	private TableColumn tblclmnNewColumn_4;
	private TableColumn tblclmnNewColumn_5;
	private TableColumn tblclmnNewColumn_6;
	
	public BomTrackingTranferDialog(Shell parent) {
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
		
		Label lblMCN = new Label(container, SWT.NONE);
		lblMCN.setText("MCN:");
		
		textMCN = new Text(container, SWT.BORDER);
		textMCN.setEditable(false);
		textMCN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblServer = new Label(container, SWT.NONE);
		lblServer.setText("SERVER:");
		
		comboServer = new Combo(container, SWT.READ_ONLY);
		comboServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboServer.setItems(new String[] {"PRODUCTION","QA","DEV"});
		
		Label lblShop = new Label(container, SWT.NONE);
		lblShop.setText("PLANT:");
		
		textPlant = new Text(container, SWT.BORDER);
		textPlant.setEditable(false);
		textPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		progressBar = new ProgressBar(container, SWT.NONE);
		progressBar.setSelection(1);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		progressBar.setState(SWT.PAUSED);
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
		
		tblItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				if( event.detail == SWT.CHECK ) {
					for (int i = 0; i < tblItem.getItemCount(); i++) {
						TableItem item = tblItem.getItem(i);
						System.out.println(item.getText(2) + "   " + item.getChecked());
					}
//					System.out.println( "You checked " + event.item );
//					if( tblItem.indexOf( ( TableItem )event.item ) == tblItem.getSelectionIndex() ) {
//				        TableItem ti = ( TableItem )event.item;
//				        ti.setChecked( !ti.getChecked() );
//				    }
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
		return new Point(550, 360);
	}
	
	@Override
	public void setTitle(String newTitle) {
		super.setTitle(newTitle);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
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
	
	public void setTableItem(TCComponent[] objectChildComponents) {
		for (TCComponent tcComponent : objectChildComponents) {
			try {
				TableItem item1 = new TableItem(tblItem, SWT.NONE);
			    item1.setText(new String[] { "",
			    							 tcComponent.getPropertyDisplayableValue("vf6_action"),
			    							 tcComponent.getPropertyDisplayableValue("vf6_main_group_str"), 
								    		 tcComponent.getPropertyDisplayableValue("vf6_sub_group_str"),
								    		 tcComponent.getPropertyDisplayableValue("vf6_bomline_id"),
								    		 tcComponent.getPropertyDisplayableValue("vf6_part_number"),
								    		 tcComponent.getPropertyDisplayableValue("vf6_quantity")});
			    String check = tcComponent.getPropertyDisplayableValue("vf6_is_transferred");
			    item1.setChecked(check.compareToIgnoreCase("true") != 0);
			} catch (NotLoadedException e) {
				
			}
		}
	}
	
	public Table getTableItem() {
		return this.tblItem;
	}
	
	public Button getOkButton() {
		
		return this.btnSave;
	}
	
	public void setProgressStatus(int percentage) {
//		this.progressBar.setState(SWT.NORMAL);
		this.progressBar.setSelection(percentage);
		this.progressBar.redraw();
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}
	
	public void setProgressVisible(boolean status) {
		if(status) {
			this.progressBar.setState(SWT.NORMAL);
		}
		else {
			this.progressBar.setState(SWT.PAUSED);
		}
		this.progressBar.redraw();
	}
	
	@Override
	protected void okPressed() {
		//super.okPressed();
	}

}
