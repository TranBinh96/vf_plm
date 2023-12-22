package com.vf.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.PropertyDefines;

import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.widgets.ProgressBar;


public class VFJESSOSDialog extends TitleAreaDialog {

	private static String LOV_ChangeRequest = "Vf6_source_trigger_MCN_lov";
	public Combo icbChangeRequestType = null;
	public Text itxtECRMCRID = null;
	public Text itxtReason = null;
	public Text itxtDCRNumber = null;
	private Button ibtnTrigger = null;
	private TCSession session = null;
	private TCComponentItem basePartItem = null;
	private Button btnSearch = null;
	ControlDecoration decECRMCRID = null;
	ControlDecoration decDCRNumber = null;
	private ProgressBar progressBar = null;
	public Text itxtStation = null;
	
	public VFJESSOSDialog(Shell parentShell, TCSession _session) {
		super(parentShell);
		session = _session;
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		setTitle("Trigger JES/SOS");
		setMessage("Please fill values to trigger JES/SOS", IMessageProvider.INFORMATION);
		Composite area = (Composite) super.createDialogArea(parent);

		try {

			//init object UI
			Composite container = new Composite(area, SWT.NONE);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			GridData gd_container = new GridData(GridData.FILL_BOTH);
			gd_container.heightHint = 105;
			container.setLayoutData(gd_container);

			// Change Request Type
			Label lblChangeRequestType = new Label(container, SWT.NONE);
			lblChangeRequestType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblChangeRequestType.setText("Change Request Type:");

			
			icbChangeRequestType = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
			addRequiredDecoration(icbChangeRequestType);
			icbChangeRequestType.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			icbChangeRequestType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			icbChangeRequestType.setItems(TCExtension.GetLovValues(LOV_ChangeRequest));

			icbChangeRequestType.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					
					String selectedValue = icbChangeRequestType.getText();
					if(selectedValue.equals("DCR") || selectedValue.equals("Other")) {
						decDCRNumber.show();
						decECRMCRID.hide();
						btnSearch.setEnabled(false);
						itxtDCRNumber.setEnabled(true);
						itxtECRMCRID.setText("");
						getShell().redraw();
					}else {
						decDCRNumber.hide();
						decECRMCRID.show();
						itxtDCRNumber.setText("");
						itxtDCRNumber.setEnabled(false);
						btnSearch.setEnabled(true);
						getShell().redraw();
					}
					enableTrigger();
				}
			});

			// ECR/MCN ID
			Label lblECRMCRID = new Label(container, SWT.NONE);
			lblECRMCRID.setText("ECR/MCR ID:");
			
			itxtECRMCRID = new Text(container, SWT.READ_ONLY | SWT.BORDER);
			decECRMCRID = addRequiredDecoration(itxtECRMCRID);
			decECRMCRID.hide();
			itxtECRMCRID.setEditable(false);
			itxtECRMCRID.setEnabled(false);
			itxtECRMCRID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			itxtECRMCRID.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					enableTrigger();
				}
			});
			
			//Search Button
			btnSearch =  new Button(container, SWT.PUSH);
			btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/search.png"));
			btnSearch.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					SearchItemDialog searchDlg = new SearchItemDialog(new Shell(), session, "__colorPartCreate_GetBasePart");
					searchDlg.create();
					searchDlg.tblItem.addListener(SWT.DefaultSelection, new Listener() {
						public void handleEvent(Event e) {
							TableItem[] selection = searchDlg.tblItem.getSelection();
							if (selection.length > 0) {
								basePartItem = (TCComponentItem) selection[0].getData();
								itxtECRMCRID.setData(basePartItem);
								try {
									itxtECRMCRID.setText(basePartItem.getProperty(PropertyDefines.ITEM_ID));
								} catch (TCException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							searchDlg.close();
						}
					});

					searchDlg.tblItem.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							TableItem[] selection = searchDlg.tblItem.getSelection();
							if (selection.length > 0) {
								TCComponentItem basePartItem = (TCComponentItem) selection[0].getData();
								itxtECRMCRID.setData(basePartItem);
								try {
									itxtECRMCRID.setText(basePartItem.getProperty(PropertyDefines.ITEM_ID));
								} catch (TCException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					});

					searchDlg.btnAccept.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							if(basePartItem != null) {
								itxtECRMCRID.setData(basePartItem);
								try {
									itxtECRMCRID.setText(basePartItem.getProperty(PropertyDefines.ITEM_ID));
								} catch (TCException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					});
					searchDlg.open();
				}
			});

			// DCR Number

			Label lblDCRNumber = new Label(container, SWT.NONE);
			lblDCRNumber.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblDCRNumber.setText("Released DCR No./Others:");

			itxtDCRNumber = new Text(container, SWT.BORDER);
			decDCRNumber = addRequiredDecoration(itxtDCRNumber);
			decDCRNumber.hide();
			itxtDCRNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			Label lblStation = new Label(container, SWT.NONE);
			lblStation.setText("Station");
			
			itxtStation = new Text(container, SWT.BORDER);
			itxtStation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			itxtStation.setEditable(false);
			itxtStation.setEnabled(false);
			
			Label lblReason = new Label(container, SWT.NONE);
			lblReason.setText("Description");
			
			itxtReason = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			GridData gd_itxtReason = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gd_itxtReason.heightHint = 45;
			itxtReason.setLayoutData(gd_itxtReason);
			addRequiredDecoration(itxtReason);
			
			progressBar = new ProgressBar(container, SWT.NONE);
			progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
			progressBar.setVisible(false);
			
			itxtDCRNumber.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					enableTrigger();
				}
			});
			itxtReason.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					enableTrigger();
				}
			});

		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return area;
	}

	public Button getSearchButton() {
		return btnSearch;
	}

	private void enableTrigger() {
		
		String text;
		if(itxtDCRNumber.isEnabled()) {
			text = itxtDCRNumber.getText();
		}else {
			text = itxtECRMCRID.getText();
		}
		if(icbChangeRequestType.getText().isEmpty() || text.isEmpty() || itxtReason.getText().isEmpty()) {
			
			ibtnTrigger.setEnabled(false);
		}else {
			ibtnTrigger.setEnabled(true);
		}
	}
	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//		setSaveButton(createButton(parent, IDialogConstants.OK_ID, "Save", true));
		ibtnTrigger = createButton(parent, IDialogConstants.OK_ID, "Trigger", true);
		ibtnTrigger.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/newprocess_16.png"));
		ibtnTrigger.setEnabled(false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("JES/SOS Dialog");
	}

	@Override
	protected void okPressed() {
		// super.okPressed();
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 350);
	}

	public Button getTriggerButton() {
		return ibtnTrigger;
	}

	private ControlDecoration addRequiredDecoration(Control control) {
		ControlDecoration controlDecoration = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
		controlDecoration.setDescriptionText("Field cannot be empty");
		controlDecoration.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/Mandatory.png"));
		return controlDecoration;
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}
}

