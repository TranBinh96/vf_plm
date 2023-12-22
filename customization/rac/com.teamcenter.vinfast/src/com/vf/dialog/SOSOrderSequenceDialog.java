package com.vf.dialog;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.util.ArrayUtil;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.teamcenter.rac.kernel.TCSession;
import org.eclipse.wb.swt.ResourceManager;


public class SOSOrderSequenceDialog extends TitleAreaDialog {

	private Button btn_Save = null;
	private Button btn_Up = null;
	private Button btn_Down = null;
	private TCSession session = null;
	private Table table = null;

	public SOSOrderSequenceDialog(Shell parentShell, TCSession _session) {
		super(parentShell);
		session = _session;
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		setTitle("Sequence Order");
		setMessage("Please arrange in order to show in SOS", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);

		try {

			//init object UI
			Composite container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(1, false);
			container.setLayout(gl_container);
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			gd_container.heightHint = 105;
			container.setLayoutData(gd_container);

			table = new Table(container, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd.heightHint = 160;
			table.setLayoutData(gd);
			table.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					enableButtons();
				}
			});

			TableColumn bomLine = new TableColumn(table, SWT.NONE);
			bomLine.setText("BOMLine");
			bomLine.setWidth(280);
			TableColumn findNo = new TableColumn(table, SWT.CENTER);

			findNo.setText("Find No");
			findNo.setWidth(70);

		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//		setSaveButton(createButton(parent, IDialogConstants.OK_ID, "Save", true));
		btn_Up = createButton(parent, IDialogConstants.OK_ID, "Up", true);
		btn_Up.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/up_16.png"));
		btn_Up.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected (SelectionEvent e){
				orderSequence(-1);
			}
		});
		btn_Up.setEnabled(false);
		btn_Down = createButton(parent, IDialogConstants.OK_ID, "Down", true);
		btn_Down.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/down_16.png"));
		btn_Down.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected (SelectionEvent e){
				orderSequence(1);
			}
		});
		btn_Down.setEnabled(false);
		btn_Save = createButton(parent, IDialogConstants.OK_ID, "Save", true);
		btn_Save.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/save_16.png"));
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SOS Sequence Dialog");
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
		return new Point(400, 350);
	}

	public Button getSaveButton() {
		return btn_Save;
	}

	public Table getTable() {
		return table;
	}

	private void orderSequence(int order) {
		int[] selectedIndices = table.getSelectionIndices();
		int[] swappedRows = new int[selectedIndices.length];
		for(int i=0;i<selectedIndices.length;i++) {
			//Previous Row
			TableItem swapRowItem = table.getItem(selectedIndices[i]+order);
			String value = swapRowItem.getText(0);
			Object data = swapRowItem.getData();
			//Selected Row
			TableItem RowItem = table.getItem(selectedIndices[i]);
			String selectedValue = RowItem.getText(0);
			Object selectedData = RowItem.getData();
			//Set in Previous Row
			swapRowItem.setText(0, selectedValue);
			swapRowItem.setData(selectedData);
			//Set in current row
			RowItem.setText(0, value);
			RowItem.setData(data);
			swappedRows[i] = selectedIndices[i]+order;
		}
		table.setSelection(swappedRows);
		table.redraw();
		enableButtons();
	}
	
	private void enableButtons() {
		int[] indices = table.getSelectionIndices();
		for(int i = 0; i<indices.length; i++) {
			if(indices[i] == 0) {
				btn_Up.setEnabled(false);
				break;
			}else {
				btn_Up.setEnabled(true);
			}
		}
		
		for(int i = 0; i<indices.length; i++) {
			if(indices[i] == table.getItemCount()-1) {
				btn_Down.setEnabled(false);
				break;
			}else {
				btn_Down.setEnabled(true);
			}
		}
	}
}

