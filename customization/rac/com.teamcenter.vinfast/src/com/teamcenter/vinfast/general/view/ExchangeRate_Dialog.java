package com.teamcenter.vinfast.general.view;

import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ExchangeRate_Dialog extends Dialog {
	private Composite area;
	private Composite container;
	
	public Text txtAmount;
	public Combo cbCurrencyFrom;
	public Button btnCalculate;
	public Table tblRate;
	private TableColumn tblclmnCurrencyName;
	private TableColumn tblclmnCurrencyCode;
	private TableColumn tblclmnRate;
	private Label lblNewLabel_1;
	private Label label;
	public Text txtResult;
	public Button btnSwitch;
	public Combo cbCurrencyTo;
	private Label lblTo;
	
	public ExchangeRate_Dialog(Shell parentShell) throws TCException {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		area.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 5;
		try {
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			container = new Composite(area, SWT.NONE);
			container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridLayout gl_container = new GridLayout(6, false);
			gl_container.horizontalSpacing = 4;
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			lblNewLabel_1 = new Label(container, SWT.NONE);
			lblNewLabel_1.setAlignment(SWT.CENTER);
			lblNewLabel_1.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
			lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));
			lblNewLabel_1.setText("Base currency is USD");
			
			label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1));
			
			Label lblVehicleCategory = new Label(container, SWT.NONE);
			lblVehicleCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVehicleCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblVehicleCategory.setText("Amount");
			
			Label lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblNewLabel.setText("From");
			new Label(container, SWT.NONE);
			
			lblTo = new Label(container, SWT.NONE);
			lblTo.setText("To");
			lblTo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
			Label lblDescription = new Label(container, SWT.NONE);
			lblDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			lblDescription.setText("Result");
			new Label(container, SWT.NONE);
			
			txtAmount = new Text(container, SWT.BORDER);
			txtAmount.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
			GridData gd_txtAmount = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			gd_txtAmount.heightHint = 6;
			gd_txtAmount.widthHint = 5;
			txtAmount.setLayoutData(gd_txtAmount);
			
			cbCurrencyFrom = new Combo(container, SWT.READ_ONLY);
			cbCurrencyFrom.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
			cbCurrencyFrom.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			GridData gd_cbCurrencyFrom = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			gd_cbCurrencyFrom.widthHint = 16;
			gd_cbCurrencyFrom.heightHint = 35;
			cbCurrencyFrom.setLayoutData(gd_cbCurrencyFrom);
			
			btnSwitch = new Button(container, SWT.FLAT);
			GridData gd_btnSwitch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_btnSwitch.heightHint = 29;
			btnSwitch.setLayoutData(gd_btnSwitch);
			btnSwitch.setText("Switch");
			
			cbCurrencyTo = new Combo(container, SWT.READ_ONLY);
			cbCurrencyTo.setEnabled(false);
			cbCurrencyTo.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
			cbCurrencyTo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbCurrencyTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			txtResult = new Text(container, SWT.BORDER);
			txtResult.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
			GridData gd_txtResult = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			gd_txtResult.widthHint = 18;
			txtResult.setLayoutData(gd_txtResult);
			
			btnCalculate = new Button(container, SWT.FLAT);
			GridData gd_btnCalculate = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_btnCalculate.heightHint = 27;
			btnCalculate.setLayoutData(gd_btnCalculate);
			btnCalculate.setText("Convert");
			
			tblRate = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
			tblRate.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
			tblRate.setHeaderForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			tblRate.setHeaderBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN));
			GridData gd_tblRate = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1);
			gd_tblRate.widthHint = 179;
			tblRate.setLayoutData(gd_tblRate);
			tblRate.setHeaderVisible(true);
			tblRate.setLinesVisible(true);
			
			tblclmnCurrencyName = new TableColumn(tblRate, SWT.LEFT);
			tblclmnCurrencyName.setWidth(250);
			tblclmnCurrencyName.setText("Currency Name");
			
			tblclmnCurrencyCode = new TableColumn(tblRate, SWT.CENTER);
			tblclmnCurrencyCode.setWidth(100);
			tblclmnCurrencyCode.setText("Currency Code");
			
			tblclmnRate = new TableColumn(tblRate, SWT.RIGHT);
			tblclmnRate.setWidth(100);
			tblclmnRate.setText("Rate");
		}
		catch(Exception ex ) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(742, 430);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(385, 400);
		super.configureShell(newShell);
		newShell.setText("Exchange Rate");
	}
}
