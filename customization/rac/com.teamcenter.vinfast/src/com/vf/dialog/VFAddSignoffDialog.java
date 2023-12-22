package com.vf.dialog;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;

import com.teamcenter.rac.kernel.TCSession;

public class VFAddSignoffDialog extends TitleAreaDialog {

	private TCSession session = null;
	private Button btnAssign = null;
	private Table table = null;
	private List listLine = null;
	private Text textShop = null;

	public VFAddSignoffDialog(Shell parentShell, TCSession _session) {
		super(parentShell);
		// TODO Auto-generated constructor stub
		session = _session;
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		setTitle("Assign Signoff Profile");
		setMessage("Please fill values to show Signoff Users", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		try {

			Composite container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			container.setLayoutData(gd_container);
			//Shop
			Label lblShop = new Label(container, SWT.NONE);
			lblShop.setText("Shop:");

			textShop = new Text(container, SWT.BORDER);
			textShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			addRequiredDecoration(textShop);
			textShop.setEditable(false);
			//Line
			Label lblLine = new Label(container, SWT.NONE);
			lblLine.setText("Line:");

			listLine = new List(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_list_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_list_1.heightHint = 60;
			listLine.setLayoutData(gd_list_1);
			addRequiredDecoration(listLine);

			table = new Table(container,SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			gd_table.heightHint = 100;
			table.setLayoutData(gd_table);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			TableColumn tblclmnNewColumn = new TableColumn(table, SWT.LEFT);
			tblclmnNewColumn.setWidth(40);
			tblclmnNewColumn.setText("S.No");

			TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.LEFT);
			tblclmnNewColumn_1.setMoveable(false);
			tblclmnNewColumn_1.setWidth(210);
			tblclmnNewColumn_1.setText("Profile(s)");

			table.addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent arg0) {
					// TODO Auto-generated method stub
					if(((Table)arg0.widget).getItemCount() > 0){
						btnAssign.setEnabled(true);
					}else {
						btnAssign.setEnabled(false);
					}
				}
			});

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return parent;

	}
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//		setSaveButton(createButton(parent, IDialogConstants.OK_ID, "Save", true));
		btnAssign = createButton(parent, IDialogConstants.OK_ID, "Assign", true);
		btnAssign.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
		btnAssign.setEnabled(false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Assign Signoff Dialog");
	}

	@Override
	protected void okPressed() {
		// super.okPressed();
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(300, 400);
	}

	private ControlDecoration addRequiredDecoration(Control control) {
		ControlDecoration controlDecoration = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
		controlDecoration.setDescriptionText("Field cannot be empty");
		controlDecoration.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/Mandatory.png"));
		return controlDecoration;
	}
	
	public Button getAssignButton() {
		return btnAssign;
	}

	public List getListControl() {
		return listLine;
	}

	public void setLineValues(String[] values) {
		for(String value: values) {
			listLine.add(value);
		}
	}
	public void setTableValues(String[] values) {
		for(int i=0; i< values.length; i++) {
			TableItem item = new TableItem(table, SWT.None);
			item.setText(0, "");
			item.setChecked(true);
			item.setText(1,values[i]);
		}
	}
	public void removeAllLines() {
		table.removeAll();
	}

	public void setShop(String shop) {
		textShop.setText(shop);
	}
	
	public ArrayList<String> getTableItemsValue() {
		ArrayList<String> values = null;
		TableItem[] items = table.getItems();
		if(table.getItemCount() > 0) {
			values =  new ArrayList<String>();
			for(TableItem item : items) {
				if(item.getChecked() == true) {
					values.add(item.getText(1));
				}
			}
		}
		return values;
	}
}
