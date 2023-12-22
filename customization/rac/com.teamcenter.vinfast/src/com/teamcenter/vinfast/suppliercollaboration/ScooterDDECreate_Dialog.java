package com.teamcenter.vinfast.suppliercollaboration;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import org.eclipse.wb.swt.ResourceManager;

public class ScooterDDECreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;

	public Table tblParts;
	public Text txtECNNumber;
	public Text txtDesc;
	public Button btnAttachmentRemove;
	public Button btnAttachmentAdd;
	private TableColumn tblclmnSupplierCode;
	private TableColumn tblclmnAttachment;
	private TableColumn tblclmnSupplierName;
	private TableColumn tblclmnRevision;
	public Button btnPartSearch;
	public Button btnPartRemove;
	public Button btnPartPaste;

	public ScooterDDECreate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);

		try {
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			setTitle("Send data to supplier");
			setMessage("Add information to send data to supplier", IMessageProvider.INFORMATION);

			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label lblEcnNumber = new Label(container, SWT.NONE);
			lblEcnNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblEcnNumber.setText("ECN Number:");

			txtECNNumber = new Text(container, SWT.BORDER | SWT.READ_ONLY);
			txtECNNumber.setEnabled(false);
			txtECNNumber.setEditable(false);
			txtECNNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtECNNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			txtECNNumber.setToolTipText("Enter ECN Number (Optional)");
			new Label(container, SWT.NONE);

			Label lblDescription = new Label(container, SWT.NONE);
			lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblDescription.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblDescription.setText("Description:");

			txtDesc = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_txtDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtDesc.heightHint = 50;
			txtDesc.setLayoutData(gd_txtDesc);
			txtDesc.setToolTipText("Enter Description (Optional)");
			new Label(container, SWT.NONE);

			Label lblPartNumber = new Label(container, SWT.NONE);
			lblPartNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblPartNumber.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 6));
			lblPartNumber.setText("Part Number:");

			tblParts = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblParts.setHeaderVisible(true);
			GridData gd_tblParts = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 6);
			gd_tblParts.widthHint = 150;
			gd_tblParts.heightHint = 150;
			tblParts.setLayoutData(gd_tblParts);
			tblParts.setLinesVisible(true);
			tblParts.setToolTipText("Search and choose Part Numbers (Mandatory");
			TableColumn partDetails = new TableColumn(tblParts, SWT.FILL);
			partDetails.setWidth(100);
			partDetails.setText("Part Number");
			partDetails.setToolTipText("Search and choose Part Numbers (Mandatory");
			
			tblclmnRevision = new TableColumn(tblParts, 0);
			tblclmnRevision.setWidth(60);
			tblclmnRevision.setToolTipText("Search and choose Part Numbers (Mandatory");
			tblclmnRevision.setText("Revision");
			TableColumn partType = new TableColumn(tblParts, SWT.FILL);
			partType.setWidth(120);
			partType.setText("Object Type");
			partType.setToolTipText("Search and choose Part Numbers (Mandatory");

			tblclmnSupplierCode = new TableColumn(tblParts, SWT.NONE);
			tblclmnSupplierCode.setWidth(90);
			tblclmnSupplierCode.setText("Supplier Code");
			
			tblclmnSupplierName = new TableColumn(tblParts, SWT.NONE);
			tblclmnSupplierName.setWidth(100);
			tblclmnSupplierName.setText("Contact Email");

			tblclmnAttachment = new TableColumn(tblParts, SWT.NONE);
			tblclmnAttachment.setWidth(170);
			tblclmnAttachment.setText("Attachment");

			btnAttachmentAdd = new Button(container, SWT.FLAT);
			btnAttachmentAdd.setToolTipText("Attach File");
			btnAttachmentAdd.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnAttachmentAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/file_import_16.png"));

			btnAttachmentRemove = new Button(container, SWT.FLAT);
			btnAttachmentRemove.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnAttachmentRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/file_export_16.png"));
			btnAttachmentRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnAttachmentRemove.setToolTipText("Remove File");
			
			btnPartSearch = new Button(container, SWT.FLAT);
			btnPartSearch.setToolTipText("Search Part");
			btnPartSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/part.png"));
			btnPartSearch.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			
			btnPartRemove = new Button(container, SWT.FLAT);
			btnPartRemove.setToolTipText("Remove Part");
			btnPartRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			btnPartRemove.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			
			btnPartPaste = new Button(container, SWT.FLAT);
			btnPartPaste.setToolTipText("Paste Part");
			btnPartPaste.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/paste_16.png"));
			btnPartPaste.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			new Label(container, SWT.NONE);
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("EScooter DDE");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 498);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}
}
