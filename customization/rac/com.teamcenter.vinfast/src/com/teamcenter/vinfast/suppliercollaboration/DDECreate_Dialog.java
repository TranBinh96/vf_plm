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
import org.eclipse.swt.widgets.Combo;
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

public class DDECreate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	public Button ckbOpenOnCreate;

	public Table tblParts;
	public Table tblVendors;
	public Text txtAttachment;
	public Text txtECNNumber;
	public Text txtDesc;
	public Combo cbRevisionRule;
	public Button ckbVSCF;
	
	public Button btnPartSearch;
	public Button btnPartRemove;
	public Button btnPartPaste;
	public Button btnVendorSearch;
	public Button btnVendorRemove;
	public Button btnAttachment;
	public Button btnECNSearch;
	public Button ckbSendData;
	
	public Button ckbVendorUpload;
	public Label lblDDU;
	public Table tblDDU;
	private TableColumn tcVendor;
	private TableColumn tcDes;
	private TableColumn tcDueDate;
	private TableColumn tcExpectedFormat;
	private TableColumn tcRemark;
	public Button btnDDUAdd;
	public Button btnDDURemove;
	private Label label;
	private Label label_1;
	private TableColumn tblclmnVendorContact;
	private Label lblExportType;
	public Combo cbExportType;

	public DDECreate_Dialog(Shell parentShell) throws TCException {
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
			
			Label lblVendor = new Label(container, SWT.NONE);
			lblVendor.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblVendor.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 2));
			lblVendor.setText("Vendor Contact: (*)");
			
			tblVendors = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			tblVendors.setHeaderVisible(true);
			GridData gd_tblVendors = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2);
			gd_tblVendors.heightHint = 80;
			tblVendors.setLayoutData(gd_tblVendors);
			tblVendors.setLinesVisible(true);
			tblVendors.setToolTipText("Search and choose Vendors (Mandatory");
			TableColumn VendorDetails = new TableColumn(tblVendors, SWT.FILL);
			VendorDetails.setText("Vendor ID");
			VendorDetails.setWidth(100);
			
			TableColumn tblclmnVendorName = new TableColumn(tblVendors, 0);
			tblclmnVendorName.setWidth(170);
			tblclmnVendorName.setText("Vendor Name");
			
			tblclmnVendorContact = new TableColumn(tblVendors, SWT.NONE);
			tblclmnVendorContact.setWidth(170);
			tblclmnVendorContact.setText("Vendor Contact");
			
			TableColumn emailID = new TableColumn(tblVendors, SWT.FILL);
			emailID.setText("Vendor Email");
			emailID.setWidth(135);

			btnVendorSearch = new Button(container, SWT.FLAT);
			btnVendorSearch.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnVendorSearch.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnVendorSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/vendor_16.png"));
			btnVendorSearch.setToolTipText("Open Vendor Search dialog");
			
			btnVendorRemove = new Button(container, SWT.FLAT);
			btnVendorRemove.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnVendorRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnVendorRemove.setImage(new Image(parent.getDisplay(), DDECreate_Dialog.class.getClassLoader().getResourceAsStream("icons/remove_16.png")));
			btnVendorRemove.setToolTipText("Remove selected parts");
			
			ckbSendData = new Button(container, SWT.CHECK);
			ckbSendData.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			ckbSendData.setSelection(true);
			ckbSendData.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			ckbSendData.setText("Send Data");
			
			label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

			Label lblPartNumber = new Label(container, SWT.NONE);
			lblPartNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblPartNumber.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 3));
			lblPartNumber.setText("Part Number:");

			tblParts = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			tblParts.setHeaderVisible(true);
			GridData gd_tblParts = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
			gd_tblParts.widthHint = 150;
			gd_tblParts.heightHint = 150;
			tblParts.setLayoutData(gd_tblParts);
			tblParts.setLinesVisible(true);
			tblParts.setToolTipText("Search and choose Part Numbers (Mandatory");
			TableColumn partDetails = new TableColumn(tblParts, SWT.FILL);
			partDetails.setWidth(145);
			partDetails.setText("Part Number(s)");
			partDetails.setToolTipText("Search and choose Part Numbers (Mandatory");
			TableColumn partType = new TableColumn(tblParts, SWT.FILL);
			partType.setWidth(150);
			partType.setText("Object Type");
			partType.setToolTipText("Search and choose Part Numbers (Mandatory");

			btnPartSearch = new Button(container, SWT.FLAT);
			btnPartSearch.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnPartSearch.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnPartSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/part.png"));
			btnPartSearch.setToolTipText("Open Part Search dialog");

			btnPartRemove = new Button(container, SWT.FLAT);
			btnPartRemove.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnPartRemove.setImage(new Image(parent.getDisplay(), DDECreate_Dialog.class.getClassLoader().getResourceAsStream("icons/remove_16.png")));
			btnPartRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnPartRemove.setToolTipText("Remove selected parts");
			
			btnPartPaste = new Button(container, SWT.FLAT);
			btnPartPaste.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnPartPaste.setImage(new Image(parent.getDisplay(), DDECreate_Dialog.class.getClassLoader().getResourceAsStream("icons/paste_16.png")));
			btnPartPaste.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnPartPaste.setToolTipText("Paste copied parts");
			
			Label lblRevisionRule = new Label(container, SWT.NONE);
			lblRevisionRule.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblRevisionRule.setText("Revision Rule:");

			cbRevisionRule = new Combo(container, SWT.NONE | SWT.READ_ONLY);
			cbRevisionRule.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			cbRevisionRule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			new Label(container, SWT.NONE);
			
			lblExportType = new Label(container, SWT.NONE);
			lblExportType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblExportType.setText("Export Type: (*)");
			lblExportType.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			
			cbExportType = new Combo(container, SWT.READ_ONLY);
			cbExportType.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			cbExportType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			new Label(container, SWT.NONE);

			Label lblAttachments = new Label(container, SWT.NONE);
			lblAttachments.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblAttachments.setText("Attachments:");

			txtAttachment = new Text(container, SWT.BORDER);
			txtAttachment.setEnabled(false);
			txtAttachment.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			txtAttachment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtAttachment.setToolTipText("Choose file type *.zip/*.pdf (Optiona)");

			btnAttachment = new Button(container, SWT.FLAT);
			btnAttachment.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnAttachment.setImage(new Image(parent.getDisplay(), DDECreate_Dialog.class.getClassLoader().getResourceAsStream("icons/importobjects_16.png")));
			
			Label lblEcnNumber = new Label(container, SWT.NONE);
			lblEcnNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblEcnNumber.setText("ECN Number:");

			txtECNNumber = new Text(container, SWT.BORDER);
			txtECNNumber.setEditable(false);
			txtECNNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtECNNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			txtECNNumber.setToolTipText("Enter ECN Number (Optional)");

			btnECNSearch = new Button(container, SWT.FLAT);
			btnECNSearch.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnECNSearch.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnECNSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/ChangeNoticeRevision.png"));
			btnECNSearch.setToolTipText("Open ECR Search dialog");

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
			
			ckbVendorUpload = new Button(container, SWT.CHECK);
			ckbVendorUpload.setText("Vendor Upload");
			ckbVendorUpload.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			
			label_1 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
			label_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
			
			lblDDU = new Label(container, SWT.NONE);
			lblDDU.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 2));
			lblDDU.setText("Edit Exchange Lines:");
			lblDDU.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			
			tblDDU = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			tblDDU.setHeaderVisible(true);
			tblDDU.setToolTipText("Search and choose Vendors (Mandatory");
			tblDDU.setLinesVisible(true);
			GridData gd_tblDDU = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2);
			gd_tblDDU.heightHint = 80;
			tblDDU.setLayoutData(gd_tblDDU);
			
			tcVendor = new TableColumn(tblDDU, SWT.NONE);
			tcVendor.setText("Vendor ID");
			tcVendor.setWidth(100);
			tcVendor.setResizable(true);
			
			tcDes = new TableColumn(tblDDU, SWT.NONE);
			tcDes.setText("Description");
			tcDes.setWidth(135);
			tcDes.setResizable(true);
			
			tcDueDate = new TableColumn(tblDDU, SWT.NONE);
			tcDueDate.setText("Due Date");
			tcDueDate.setWidth(135);
			tcDueDate.setResizable(true);
			
			tcExpectedFormat = new TableColumn(tblDDU, SWT.NONE);
			tcExpectedFormat.setText("Expected Format");
			tcExpectedFormat.setWidth(135);
			tcExpectedFormat.setResizable(true);
			
			tcRemark = new TableColumn(tblDDU, SWT.NONE);
			tcRemark.setText("Remark");
			tcRemark.setWidth(135);
			tcRemark.setResizable(true);
			
			btnDDUAdd = new Button(container, SWT.FLAT);
			btnDDUAdd.setToolTipText("Open Vendor Search dialog");
			btnDDUAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
			btnDDUAdd.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			
			btnDDURemove = new Button(container, SWT.FLAT);
			btnDDURemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnDDURemove.setToolTipText("Open Vendor Search dialog");
			btnDDURemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			btnDDURemove.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			
			ckbVSCF = new Button(container, SWT.CHECK);
			ckbVSCF.setText("Use VSCF");
			new Label(container, SWT.NONE);
			new Label(container, SWT.NONE);
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Send Data to Supplier");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 720);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		ckbOpenOnCreate = new Button(parent, SWT.CHECK);
        ckbOpenOnCreate.setText("Open On Create");
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", true);
	}
}
