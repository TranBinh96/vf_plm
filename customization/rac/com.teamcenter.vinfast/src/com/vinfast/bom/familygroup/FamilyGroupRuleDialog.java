package com.vinfast.bom.familygroup;


import org.apache.log4j.Logger;
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
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class FamilyGroupRuleDialog extends TitleAreaDialog {

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;

	private Button btnSave;
	private static final Logger logger = Logger.getLogger(FamilyGroupRuleDialog.class);

	protected Object result;
	private Label lblVariant;
	private Combo cbVariant;
	private Label lblFgCode;
	private Combo cbFGCode;
	private Label lblFgDescription;
	private Text txtFGDes;
	private Label lblValidationType;
	private Combo cbValiType;
	private Label lblMinQty;
	private Text txtMinQty;
	private Label lblMaxQty;
	private Text txtMaxQty;
	private final String placeholder = "Required";
	private Table table;
	private Combo cbPrg;
	
	public FamilyGroupRuleDialog(Shell parentShell, TCComponentItemRevision partRev, Table table,TCSession session) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		this.table = table;
//		getPrgValueAndVariantValue();
	}
	
	
	
	public Button getBtnSave() {
		return btnSave;
	}



	public void setBtnSave(Button btnSave) {
		this.btnSave = btnSave;
	}



	public Label getLblVariant() {
		return lblVariant;
	}



	public void setLblVariant(Label lblVariant) {
		this.lblVariant = lblVariant;
	}



	public Combo getCbVariant() {
		return cbVariant;
	}



	public void setCbVariant(Combo cbVariant) {
		this.cbVariant = cbVariant;
	}



	public Label getLblFgCode() {
		return lblFgCode;
	}



	public void setLblFgCode(Label lblFgCode) {
		this.lblFgCode = lblFgCode;
	}



	public Combo getCbFGCode() {
		return cbFGCode;
	}



	public void setCbFGCode(Combo cbFGCode) {
		this.cbFGCode = cbFGCode;
	}



	public Label getLblFgDescription() {
		return lblFgDescription;
	}



	public void setLblFgDescription(Label lblFgDescription) {
		this.lblFgDescription = lblFgDescription;
	}



	public Text getTxtFGDes() {
		return txtFGDes;
	}



	public void setTxtFGDes(Text txtFGDes) {
		this.txtFGDes = txtFGDes;
	}



	public Label getLblValidationType() {
		return lblValidationType;
	}



	public void setLblValidationType(Label lblValidationType) {
		this.lblValidationType = lblValidationType;
	}



	public Combo getCbValiType() {
		return cbValiType;
	}



	public void setCbValiType(Combo cbValiType) {
		this.cbValiType = cbValiType;
	}



	public Label getLblMinQty() {
		return lblMinQty;
	}



	public void setLblMinQty(Label lblMinQuality) {
		this.lblMinQty = lblMinQuality;
	}



	public Text getTxtMinQty() {
		return txtMinQty;
	}



	public void setTxtMinQty(Text txtMinQty) {
		this.txtMinQty = txtMinQty;
	}



	public Label getLblMaxQty() {
		return lblMaxQty;
	}



	public void setLblMaxQty(Label lblMaxQuality) {
		this.lblMaxQty = lblMaxQuality;
	}



	public Text getTxtMaxQty() {
		return txtMaxQty;
	}



	public void setTxtMaxQty(Text txtMaxQty) {
		this.txtMaxQty = txtMaxQty;
	}



	public Combo getCbPrg() {
		return cbPrg;
	}



	public void setCbPrg(Combo cbPrg) {
		this.cbPrg = cbPrg;
	}


	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		try {
			Composite container = new Composite(area, SWT.NONE);
			GridLayout glContainer = new GridLayout(2, true);
			glContainer.marginRight = 10;
			glContainer.marginLeft = 10;
			container.setLayout(glContainer);
			GridData gdContainer = new GridData(GridData.FILL_BOTH);
			gdContainer.widthHint = 680;
			container.setLayoutData(gdContainer);
			
			
			Label lblPrg = new Label(container, SWT.NONE);
			lblPrg.setText("Program(*): ");
			
			cbPrg = new Combo(container, SWT.READ_ONLY);
			cbPrg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			lblVariant = new Label(container, SWT.NONE);
			lblVariant.setText("Variant: ");
			
			cbVariant = new Combo(container, SWT.NONE);
			cbVariant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblFgCode = new Label(container, SWT.NONE);
			lblFgCode.setText("FG Code(*):");
			
			cbFGCode = new Combo(container, SWT.READ_ONLY);
			cbFGCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblFgDescription = new Label(container, SWT.NONE);
			lblFgDescription.setText("FG Description:");
			
			txtFGDes = new Text(container, SWT.BORDER | SWT.READ_ONLY);
			txtFGDes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblValidationType = new Label(container, SWT.NONE);
			lblValidationType.setText("Validation Type(*):");
			
			cbValiType = new Combo(container, SWT.READ_ONLY);
			cbValiType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			lblMinQty = new Label(container, SWT.NONE);
			lblMinQty.setText("Min Quantity:");
			
			txtMinQty = new Text(container, SWT.BORDER);
			txtMinQty.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			
			lblMaxQty = new Label(container, SWT.NONE);
			lblMaxQty.setText("Max Quantity:");
			
			txtMaxQty = new Text(container, SWT.BORDER);
			txtMaxQty.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		catch(Exception ex ) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSave = createButton(parent, IDialogConstants.OK_ID, "Save", true);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Program and Family Group");
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		//super.okPressed();
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(627, 453);
	}
	
}
