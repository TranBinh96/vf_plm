package com.vinfast.sap.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.vinfast.sap.configurator.MaterialPlatformCode;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class UnitTransferDialog extends TitleAreaDialog {
	static int total = 0;
	static int counter = 1;
	private static Text txtModel;
	private static Text txtYear;
	private static Text txtMCN;
	private static Text txtPlant;
	private static Text txtShop;
	private static Combo cbServer;
	private static ProgressBar progressBar;
	private Button btnTransfer;
	private Button btnValidate;
	private static TCComponentChangeItemRevision changeRevision = null;
	private TCSession session = null;
	
	public UnitTransferDialog(Shell parent) {
		super(parent);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	public UnitTransferDialog(TCSession connection, TCComponentChangeItemRevision MCNObject) {
		super(new Shell());
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		changeRevision = MCNObject;
		session = connection;
	}

	@Override
	public void setMessage(String newMessage, int newType) {
		// TODO Auto-generated method stub
		super.setMessage(newMessage, newType);
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Click \"Prepare Data\" to generate data report", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		try {
			
			Composite container = new Composite(area, SWT.NONE);
			container.setLayout(new GridLayout(4, false));
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			gd_container.grabExcessVerticalSpace = false;
			container.setLayoutData(gd_container);
			
			TCComponent impactedShop = changeRevision.getContents(PropertyDefines.REL_IMPACT_SHOP)[0];
			TCComponentItemRevision shopsParent = UIGetValuesUtility.getTopLevelItemRevision(session, (TCComponentItemRevision)impactedShop, PropertyDefines.REVISION_RULE_WORKING);
			MaterialPlatformCode platformCode = UIGetValuesUtility.getPlatformCode(shopsParent);
			
			Label lblModel = new Label(container, SWT.NONE);
			lblModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblModel.setText("MODEL:");

			txtModel = new Text(container, SWT.BORDER);
			txtModel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtModel.setEditable(false);
			txtModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtModel.setText(platformCode.getPlatformCode());
			txtModel.setData(shopsParent);
			
			Label lblYear = new Label(container, SWT.NONE);
			lblYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblYear.setText("YEAR:");

			txtYear = new Text(container, SWT.BORDER);
			txtYear.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtYear.setEditable(false);
			txtYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtYear.setText(platformCode.getModelYear());

			Label lblPlant = new Label(container, SWT.NONE);
			lblPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblPlant.setText("PLANT:");

			txtPlant = new Text(container, SWT.BORDER);
			txtPlant.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtPlant.setEditable(false);
			txtPlant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtPlant.setText(changeRevision.getProperty(PropertyDefines.ECM_PLANT));

			Label lblShop = new Label(container, SWT.NONE);
			lblShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblShop.setText("SHOP:");

			txtShop = new Text(container, SWT.BORDER);
			txtShop.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtShop.setEditable(false);
			txtShop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtShop.setText(impactedShop.getProperty(PropertyDefines.ITEM_CURRENT_NAME));
			txtShop.setData(impactedShop);
			
			Label lblMcn = new Label(container, SWT.NONE);
			lblMcn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblMcn.setText("MCN:");

			txtMCN = new Text(container, SWT.BORDER);
			txtMCN.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			txtMCN.setEditable(false);
			txtMCN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtMCN.setText(changeRevision.getProperty(PropertyDefines.ITEM_ID).substring(3));
			txtMCN.setData(changeRevision);

			Label lblServer = new Label(container, SWT.NONE);
			lblServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblServer.setText("SERVER:");

			cbServer = new Combo(container, SWT.READ_ONLY);
			cbServer.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			cbServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			cbServer.setItems(new String[] { "PRODUCTION", "QA", "DEV" });
			cbServer.setText("PRODUCTION");
			cbServer.setToolTipText("Select \"SAP\" server to transfer");

			progressBar = new ProgressBar(container, SWT.NONE);
			progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			progressBar.setVisible(false);
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SAP Interface...");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 280);
	}

	@Override
	public void setTitle(String newTitle) {
		super.setTitle(newTitle);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnTransfer = createButton(parent, IDialogConstants.CLOSE_ID, "Transfer", false);
		btnTransfer.setEnabled(false);
		btnValidate = createButton(parent, IDialogConstants.DETAILS_ID, "Prepare Data", true);
		btnValidate.setToolTipText("Click to generate transfer data");
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public static String getModel() {
		return txtModel.getText();
	}
	public static String getYear() {
		return txtYear.getText();
	}
	public static String getMCNID() {
		return txtMCN.getText();
	}
	public static TCComponentChangeItemRevision getChangeRevision() {
		return (TCComponentChangeItemRevision)txtMCN.getData();
	}
	public static TCComponentItemRevision getShopRevision() {
		return (TCComponentItemRevision)txtShop.getData();
	}
	public static TCComponentItemRevision getShopParentRevision() {
		return (TCComponentItemRevision)txtModel.getData();
	}
	public static String getPlant() {
		return txtPlant.getText();
	}
	public static String getShop() {
		return txtShop.getText();
	}
	public static String getServer() {
		return cbServer.getText();
	}
	public Button getTransferButton() {
		return btnTransfer;
	}
	public Button getValidateButton() {
		return btnValidate;
	}
	public static void setProgressStatus(int percentage) {
		progressBar.setSelection(percentage);
		progressBar.redraw();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static void updateprogressBar() {
		float value = (float) counter / (float) total;
		progressBar.setSelection((int) (value * 100));
		progressBar.redraw();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		counter++;
	}
	public static void setVisibleProgressBar(boolean value) {
		progressBar.setVisible(value);
	}
}
