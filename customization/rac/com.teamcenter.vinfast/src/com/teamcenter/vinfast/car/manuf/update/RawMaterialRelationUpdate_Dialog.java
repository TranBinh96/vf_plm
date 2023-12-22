package com.teamcenter.vinfast.car.manuf.update;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import org.eclipse.swt.widgets.Text;

public class RawMaterialRelationUpdate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnSave;
	public Table tblLink;
	private Label lblNewLabel;
	public Button btnAdd;
	public Button btnRemove;
	public Text txtPart;
	private TableColumn tblclmnName;
	private TableColumn tblclmnType;

	public RawMaterialRelationUpdate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		setTitle("Raw Material Link");
		setMessage("Define business object create information");
		try {
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setText("Part:");
			
			txtPart = new Text(container, SWT.BORDER);
			txtPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			tblLink = new Table(container, SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
			tblLink.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));
			tblLink.setHeaderVisible(true);
			tblLink.setLinesVisible(true);

			TableColumn column = new TableColumn(tblLink, SWT.CENTER);
			column.setResizable(true);
			column.setWidth(120);
			column.setText("Part Number");
			
			tblclmnName = new TableColumn(tblLink, SWT.LEFT);
			tblclmnName.setWidth(200);
			tblclmnName.setText("Name");
			tblclmnName.setResizable(true);
			
			tblclmnType = new TableColumn(tblLink, SWT.LEFT);
			tblclmnType.setWidth(150);
			tblclmnType.setText("Type");
			tblclmnType.setResizable(true);
			
			btnAdd = new Button(container, SWT.PUSH);
			btnAdd.setText("Add");
			btnAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			btnAdd.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			btnAdd.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
			
			btnRemove = new Button(container, SWT.PUSH);
			btnRemove.setText("Remove");
			btnRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			btnRemove.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			btnRemove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSave = createButton(parent, IDialogConstants.CLOSE_ID, "Update", false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Raw Material");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(520, 400);
	}
}
