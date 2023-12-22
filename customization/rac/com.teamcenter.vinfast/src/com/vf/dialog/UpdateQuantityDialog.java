package com.vf.dialog;

import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;

public class UpdateQuantityDialog extends TitleAreaDialog{
	private static final Logger logger = Logger.getLogger(UpdateQuantityDialog.class);
	String regex = "[0-9]*\\.?[0-9]*";
	
	TCComponentBOMLine[] bomlines;
	TCSession session;
	TCAccessControlService acl;
	String attributeDisplayName;
	List<String> errorMessages;
	Button btnSave;
	Text txtQuantity;
	
	public UpdateQuantityDialog(Shell parentShell, TCComponentBOMLine []bomlines, TCSession session) throws TCException {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		
		this.bomlines = bomlines;
		this.session = session;
		
		acl = session.getTCAccessControlService();
		errorMessages = new LinkedList<String>();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Update Quantity");
		setMessage("Note: Alphanumeric Characters are not acceptable.",IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);
		try {
			Composite container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			gl_container.horizontalSpacing = 10;
			gl_container.marginRight = 10;
			gl_container.marginLeft = 10;
			container.setLayout(gl_container);
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			gd_container.widthHint = 440;
			container.setLayoutData(gd_container);
			
			Label lblCategory = new Label(container, SWT.NONE);
			lblCategory.setText("Quantity:");
			
			txtQuantity = new Text(container, SWT.BORDER);
			txtQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtQuantity.setText("1");
			txtQuantity.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					Boolean check = (txtQuantity != null && !txtQuantity.getText().isEmpty());
					if(check) {
						String inputString = txtQuantity.getText().trim();
						if(!inputString.isEmpty()) {
							if(!inputString.matches(regex)) {
								check = false;
							}	
						}	
					}
					btnSave.setEnabled(check);
				}
			});
			
		}
		catch(Exception ex ) {
			logger.error(ex);
		}
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSave = createButton(parent, IDialogConstants.OK_ID, "Save", true);
		btnSave.addSelectionListener(new SelectionAdapter() {
	         public void widgetSelected(SelectionEvent event) {
	        	 try {
					fillAttribute();
				} catch (TCException e) {
					logger.error(e);
				}
	         }
	      });
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Update Quantity");
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
		return new Point(400, 220);
	}

	private void showConfirmDialog() {
		//this.setAlwaysOnTop(false);
		if (errorMessages.isEmpty()) {
			MessageBox.post("The update is completed successfully.", "Information", MessageBox.INFORMATION);
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					StringBuffer html = new StringBuffer();
					html.append("<html><body><table border=\"1\" style=\"width:100%\" >");
					html.append("<tr><td><b>NOT Updated Line</b></td><td><b>Level</b></td></tr>");
					for (String errMsg : errorMessages) {
						String level = errMsg.split("\\|")[1];
						String line = errMsg.split("\\|")[0];
						html.append("<tr><td>").append(line).append("</td>").append("<td>").append(level).append("</td>").append("</tr>");
					}
					html.append("</table></body></html>");
					StringViewerDialog dlg = new StringViewerDialog(new String[] {html.toString()});
					dlg.setTitle("Not Updated Lines");
					dlg.setVisible(true);
				}
			});
		}
	}

	private void fillAttribute() throws TCException {
		final String willFillValue = txtQuantity.getText();
		AbstractAIFOperation operation = new AbstractAIFOperation("Auto-filling value") {
			@Override
			public void executeOperation() throws Exception {
				for (TCComponentBOMLine bomline : bomlines) {
					if (bomline.getCachedParent() != null && acl.checkPrivilege(bomline.getCachedParent().getBOMViewRevision(), "WRITE")) {
						try {
							bomline.setStringProperty("bl_quantity", willFillValue);
						} catch (Exception ex) {
							logger.error(ex);
							errorMessages.add(generatedErrorMessageLine(bomline));
						}
					} else {
						errorMessages.add(generatedErrorMessageLine(bomline));
					}
				}
			}
		};
		
		session.queueOperationAndWait(operation);
		showConfirmDialog();
		
		this.close();
	}
	
	private String generatedErrorMessageLine(TCComponentBOMLine bomline) throws TCException {
		return bomline.getProperty("object_string") + "|" + bomline.getProperty("bl_level_starting_0");
	}
}
