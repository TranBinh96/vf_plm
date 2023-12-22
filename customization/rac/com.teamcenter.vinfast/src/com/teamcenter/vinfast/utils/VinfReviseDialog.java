package com.teamcenter.vinfast.utils;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.client.model.LovValue;

public class VinfReviseDialog extends Dialog {

	protected Object result;
	protected Shell shlVinfReviseDialog;
	private Text text;
	private Button btnOk;
	private Combo cbvQualityOfFinance;
	public static int DECISION_OK = 1;
	public static int DECISION_CANCEL = 2;
	private int decision;
	private String reviseReason = "";
	private TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	private String qualityOfFinance;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public VinfReviseDialog(Shell parent, int style) {
		super(parent, style);
		setText("Revise Dialog");
		decision = DECISION_CANCEL;
		qualityOfFinance = "";
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 * @throws TCException 
	 */
	public Object open() throws TCException {
		createContents();

		Rectangle parentSize = Display.getCurrent().getBounds();
		Rectangle mySize = shlVinfReviseDialog.getBounds();

		int locationX, locationY;
		locationX = (parentSize.width - mySize.width) / 2 + parentSize.x;
		locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;

		shlVinfReviseDialog.setLocation(new Point(locationX, locationY));

		shlVinfReviseDialog.open();
		shlVinfReviseDialog.layout();
		Display display = getParent().getDisplay();

		while (!shlVinfReviseDialog.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 * @throws TCException 
	 */
	private void createContents() throws TCException {
		//this.parent.setSize(450,300);
		shlVinfReviseDialog = new Shell(getParent(), getStyle());
		shlVinfReviseDialog.setSize(450, 335);
		shlVinfReviseDialog.setText("Revise Dialog");

		text = new Text(shlVinfReviseDialog, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnOk.setEnabled(text.getText().isEmpty() == false && cbvQualityOfFinance.getSelectionIndex() >= 0);
			}
		});
		text.setBounds(10, 65, 424, 189);

		Label lblNewLabel = new Label(shlVinfReviseDialog, SWT.NONE);
		lblNewLabel.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblNewLabel.setBounds(10, 39, 103, 20);
		lblNewLabel.setText("Revise Reason");

		btnOk = new Button(shlVinfReviseDialog, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				decision = DECISION_OK;
				if (text != null) {
					reviseReason = text.getText();
				}
				shlVinfReviseDialog.close();
			}
		});
		btnOk.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnOk.setBounds(188, 260, 113, 30);
		btnOk.setText("OK");
		btnOk.setEnabled(false);

		Button btnCancel = new Button(shlVinfReviseDialog, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				decision = DECISION_CANCEL;
				shlVinfReviseDialog.close();
			}
		});
		btnCancel.setText("Cancel");
		btnCancel.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		btnCancel.setBounds(321, 260, 113, 30);

		Label lblQoF = new Label(shlVinfReviseDialog, SWT.NONE);
		lblQoF.setText("Quality of Finance");
		lblQoF.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblQoF.setBounds(10, 13, 134, 20);
		
		cbvQualityOfFinance = new Combo(shlVinfReviseDialog, SWT.NONE);
		cbvQualityOfFinance.setBounds(150, 10, 284, 28);
		cbvQualityOfFinance.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (cbvQualityOfFinance.getSelectionIndex() >= 0) {
					qualityOfFinance = (String) cbvQualityOfFinance
							.getData(String.valueOf(cbvQualityOfFinance.getSelectionIndex()));
					
					btnOk.setEnabled(text.getText().isEmpty() == false && cbvQualityOfFinance.getSelectionIndex() >= 0);
				} else {
					btnOk.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		TCComponentType costType;
		costType = session.getTypeComponent("VF4_CostRevision");
		TCComponentListOfValues lov = costType.getPropertyDescriptor("vf4_quality_of_finance").getLOV();
		String[] lovDispValues = lov.getListOfValues().getLOVDisplayValues();
		List<LovValue> lovVals = lov.getListOfValues().getValues();
		cbvQualityOfFinance.setItems(lovDispValues);
		for (int i = 0; i < lovVals.size(); i++) {
			String value = (String) lovVals.get(i).getValue();
			cbvQualityOfFinance.setData(String.valueOf(i), value);
		}
	}

	public int getDecision() {
		return decision;
	}

	public String getUserInputText() throws Exception {
		return reviseReason;
	}

	public String getSelectedQualityOfFinance() {
		return qualityOfFinance;
	}
}
