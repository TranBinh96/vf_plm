package com.teamcenter.vinfast.bom.update;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;

public class BOMValidateUpdateBOMAttr_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnCreate;
	public Combo cbPurchaseLevel;
	public Table tblUpdate;
	private TableColumn tblclmnNewColumn;
	private TableColumn tblclmnNewColumn_1;
	private TableColumn tblclmnNewColumn_2;
	private TableColumn tblclmnNewColumn_3;
	private TableColumn tblclmnNewColumn_4;
	private TableColumn tblclmnNewColumn_5;
	private Label lblNewLabel;

	public BOMValidateUpdateBOMAttr_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.ON_TOP);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("BOM Attribute Massupdate");
		setMessage("Define business object create information", IMessageProvider.INFORMATION);
		area = (Composite) super.createDialogArea(parent);
		try {
			// init UI
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(2, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setText("Purchase Level");
			new Label(container, SWT.NONE);

			cbPurchaseLevel = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			cbPurchaseLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPurchaseLevel.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			GridData gd_cbPurchaseLevel = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
			gd_cbPurchaseLevel.widthHint = 117;
			cbPurchaseLevel.setLayoutData(gd_cbPurchaseLevel);
			new Label(container, SWT.NONE);

			tblUpdate = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblUpdate.setHeaderVisible(true);
			tblUpdate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

			tblclmnNewColumn = new TableColumn(tblUpdate, SWT.NONE);
			tblclmnNewColumn.setWidth(150);
			tblclmnNewColumn.setText("Bomline");

			tblclmnNewColumn_1 = new TableColumn(tblUpdate, SWT.NONE);
			tblclmnNewColumn_1.setWidth(90);
			tblclmnNewColumn_1.setText("Part Make/Buy");

			tblclmnNewColumn_2 = new TableColumn(tblUpdate, SWT.NONE);
			tblclmnNewColumn_2.setWidth(90);
			tblclmnNewColumn_2.setText("Purchase Level");

			tblclmnNewColumn_3 = new TableColumn(tblUpdate, SWT.NONE);
			tblclmnNewColumn_3.setWidth(172);
			tblclmnNewColumn_3.setText("Module Group English");

			tblclmnNewColumn_4 = new TableColumn(tblUpdate, SWT.NONE);
			tblclmnNewColumn_4.setWidth(187);
			tblclmnNewColumn_4.setText("Main Module English");

			tblclmnNewColumn_5 = new TableColumn(tblUpdate, SWT.NONE);
			tblclmnNewColumn_5.setWidth(152);
			tblclmnNewColumn_5.setText("Module Name");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnCreate = createButton(parent, IDialogConstants.CLOSE_ID, "Create", false);
		btnCreate.setText("Update");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 600);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("ME Part");
	}
}
