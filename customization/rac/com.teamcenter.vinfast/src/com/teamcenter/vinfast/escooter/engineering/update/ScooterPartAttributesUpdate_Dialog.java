package com.teamcenter.vinfast.escooter.engineering.update;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class ScooterPartAttributesUpdate_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;
	public Button btnSave;
	public Table table;
	private Label lblNewLabel;
	public Button ckbIsAfterSale;
	private Label lblPartMakebuy;
	public Combo cbMakeBuy;
	private Label lblNewLabel_1;
	public Combo cbVehicleLine;
	public Button btnAdd;
	public Button btnRemove;
	public Combo cbPartTraceability;
	public Button btnInfoPartTraceability;
	private Label lblNewLabel_2;

	public ScooterPartAttributesUpdate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		setTitle("EScooter Part Attributes Update");
		setMessage("Define business object create information");
		try {
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setText("Is After Sale Relevant:");

			ckbIsAfterSale = new Button(container, SWT.CHECK);
			new Label(container, SWT.NONE);

			lblPartMakebuy = new Label(container, SWT.NONE);
			lblPartMakebuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPartMakebuy.setText("Part Make/Buy:");

			cbMakeBuy = new Combo(container, SWT.READ_ONLY);
			cbMakeBuy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbMakeBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			new Label(container, SWT.NONE);

			lblNewLabel_1 = new Label(container, SWT.NONE);
			lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_1.setText("EScooter Model/ Vehicle Line:");

			cbVehicleLine = new Combo(container, SWT.READ_ONLY);
			cbVehicleLine.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbVehicleLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			new Label(container, SWT.NONE);

			lblNewLabel_2 = new Label(container, SWT.NONE);
			lblNewLabel_2.setText("Part Traceability Indicator:");
			lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			cbPartTraceability = new Combo(container, SWT.READ_ONLY);
			cbPartTraceability.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbPartTraceability.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			btnInfoPartTraceability = new Button(container, SWT.NONE);
			btnInfoPartTraceability.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/info_16.png"));

			table = new Table(container, SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			TableColumn column = new TableColumn(table, SWT.CENTER);
			column.setResizable(true);
			column.setWidth(400);
			column.setText("Part Number(s)");

			btnAdd = new Button(container, SWT.PUSH);
			btnAdd.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			btnAdd.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			btnAdd.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

			btnRemove = new Button(container, SWT.PUSH);
			btnRemove.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			btnRemove.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			btnRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
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
		newShell.setText("EScooter Part Attributes");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(520, 400);
	}
}
