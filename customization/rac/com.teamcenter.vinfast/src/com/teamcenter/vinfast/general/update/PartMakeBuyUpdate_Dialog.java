package com.teamcenter.vinfast.general.update;

import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.wb.swt.SWTResourceManager;
import com.teamcenter.rac.kernel.TCException;
import org.eclipse.swt.browser.Browser;

public class PartMakeBuyUpdate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	private Label lblNewLabel;
	public Combo cbPartMakeBuy;
	
	public Table tblItem;
	private TableColumn tblclmnNewColumn_1;
	private TableColumn tblclmnNewColumn_2;
	private TableColumn tblclmnNewColumn_3;
	private TableColumn tblclmnNewColumn_4;
	private TableColumn tblclmnNewColumn_6;
	private TableColumn tblclmnNewColumn_7;
	public Browser brwItem;
	
	public PartMakeBuyUpdate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		try {
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			{
				lblNewLabel = new Label(container, SWT.NONE);
				lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				lblNewLabel.setText("Part Make/Buy: (*)");
			}
			{
				cbPartMakeBuy = new Combo(container, SWT.NONE | SWT.READ_ONLY);
				cbPartMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				cbPartMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			
			brwItem = new Browser(container, SWT.BORDER);
			brwItem.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			
//			tblItem = new Table(container, SWT.BORDER);
//			tblItem.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
//			tblItem.setHeaderVisible(true);
//			tblItem.setLinesVisible(true);
//			
//			CheckboxTableViewer tableViewer = new CheckboxTableViewer(tblItem);
//			tableViewer.setAllGrayed(true);
//			tableViewer.setAllChecked(true);
//			
//			tblclmnNewColumn_1 = new TableColumn(tblItem, SWT.NONE);
//			tblclmnNewColumn_1.setWidth(40);
//			tblclmnNewColumn_1.setText("No");
//			
//			tblclmnNewColumn_2 = new TableColumn(tblItem, SWT.NONE);
//			tblclmnNewColumn_2.setWidth(100);
//			tblclmnNewColumn_2.setText("Part Number");
//			
//			tblclmnNewColumn_3 = new TableColumn(tblItem, SWT.NONE);
//			tblclmnNewColumn_3.setWidth(100);
//			tblclmnNewColumn_3.setText("Release Status");
//			
//			tblclmnNewColumn_4 = new TableColumn(tblItem, SWT.NONE);
//			tblclmnNewColumn_4.setWidth(150);
//			tblclmnNewColumn_4.setText("Part Name");
//			
//			tblclmnNewColumn_6 = new TableColumn(tblItem, SWT.NONE);
//			tblclmnNewColumn_6.setWidth(70);
//			tblclmnNewColumn_6.setText("Make/Buy");
//			
//			tblclmnNewColumn_7 = new TableColumn(tblItem, SWT.NONE);
//			tblclmnNewColumn_7.setWidth(200);
//			tblclmnNewColumn_7.setText("Status");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Update", true);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(900, 479);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(400, 400);
		super.configureShell(newShell);
		newShell.setText("Part Make/Buy");
	}
}
