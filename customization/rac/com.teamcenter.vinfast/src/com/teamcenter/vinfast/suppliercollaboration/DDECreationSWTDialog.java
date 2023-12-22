package com.teamcenter.vinfast.suppliercollaboration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.collections4.map.HashedMap;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.GetNextIdsResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.InfoForNextId;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.vinfast.subdialog.SearchPartRev_Dialog;
import com.teamcenter.vinfast.subdialog.SearchCompanyContact_Dialog;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.Utilities;
import org.eclipse.wb.swt.ResourceManager;

public class DDECreationSWTDialog extends TitleAreaDialog{

	private Composite area;
	private Composite container;
	
	protected Object result;
	protected Table tblPart;
	protected Table tblVendor;
	protected Text txtAttachment;
	protected Text txtECNNumber;
	protected Text txtDescription;
	protected TCSession session = null;
	protected TableItem[] partItems;
	protected TableItem[] vendorItems;
	protected Combo cbRule;
	protected Button btnCreate;
	protected Label lblUseVscf;
	protected Button ckbVendorUpload;
	protected Button ckbVSCF;
	Button btnAddDDU;
	Button btnRemoveDDU;
	Button btnPartSearch;
	Button btnPartRemove;
	Button btnPartPaste;
	Button btnAttachment;
	Button ckbSendData;
	
	private Boolean isShowVSCF = false;
	private Boolean isDefaultVSCF = false;
	private Table tblDDU;

	public DDECreationSWTDialog(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
//		setBlockOnOpen(false);
		setHelpAvailable(false);
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		String[] settings = TCExtension.GetPreferenceValues("VINF_DDE_SETTING", session);
		if(settings != null) {
			String[] str = settings[0].split(",");
			if(str.length > 1) {
				if(str[0].compareToIgnoreCase("1") == 0) isShowVSCF = true;
				if(str[1].compareToIgnoreCase("1") == 0) isDefaultVSCF  = true;
			}
		}
		
		Utilities util = new Utilities();
		area = (Composite) super.createDialogArea(parent);
		
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		
		try {
			String[] fileFormat = TCExtension.GetLovValues("vf4_expected_format", "VF4_DDU_EventRevision", session);
			setMessage("Define business object create information");
			parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			setTitle("Create Spec Document");

			container = new Composite(area, SWT.BORDER);
			GridLayout gl_container = new GridLayout(3, false);
			container.setLayout(gl_container);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			ckbSendData = new Button(container, SWT.CHECK);
			ckbSendData.setSelection(true);
			ckbSendData.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			ckbSendData.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
			ckbSendData.setText("Send Data");
			ckbSendData.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					if(!ckbSendData.getSelection()) {
						ckbVendorUpload.setSelection(true);
						updateVendorUpdateUI();
					}
					updateSendDataUI();
				}
			});
			
			Label lblPartNumber = new Label(container, SWT.NONE);
			lblPartNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblPartNumber.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 3));
			lblPartNumber.setText("Part Number:");
	
			tblPart = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_tblPart = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
			gd_tblPart.widthHint = 150;
			gd_tblPart.heightHint = 150;
			tblPart.setLayoutData(gd_tblPart);
			tblPart.setLinesVisible(true);
			tblPart.setToolTipText("Search and choose Part Numbers (Mandatory");
			TableColumn partDetails = new TableColumn(tblPart, SWT.FILL);
			partDetails.setResizable(true);
			partDetails.setWidth(145);
			partDetails.setText("Part Number(s)");
			partDetails.setToolTipText("Search and choose Part Numbers (Mandatory");
			TableColumn partType = new TableColumn(tblPart, SWT.FILL);
			partType.setResizable(true);
			partType.setWidth(135);
			partType.setText("Object Type");
			partType.setToolTipText("Search and choose Part Numbers (Mandatory");
			
			btnPartSearch = new Button(container, SWT.FLAT);
			btnPartSearch.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnPartSearch.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnPartSearch.setImage(new Image(null, DDECreationSWTDialog.class.getClassLoader().getResourceAsStream("icons/search.png")));
			btnPartSearch.setToolTipText("Open Part Search dialog");
			btnPartSearch.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					SearchPartRev_Dialog container = new SearchPartRev_Dialog(parent.getShell());
					container.open();
					util.centerToScreen(container.getShell());
					Button ok = container.getOKButton();
	
					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
//							Table partTable = container.getSearchTable();
//							TableItem[] items = partTable.getSelection();
//							for(int row = 0; row < items.length; row++) {
//								TableItem setItem = new TableItem(tblPart, SWT.NONE);
//								for(int col = 0; col < partTable.getColumnCount(); col++) {
//									setItem.setText(col, items[row].getText(col));
//								}
//							}
	
							container.getShell().dispose();
						}
					});
				}
			});
	
			btnPartRemove = new Button(container, SWT.FLAT);
			btnPartRemove.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnPartRemove.setImage(new Image(parent.getDisplay(), DDECreationSWTDialog.class.getClassLoader().getResourceAsStream("icons/remove_16.png")));
			btnPartRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnPartRemove.setToolTipText("Remove selected parts");
			btnPartRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					tblPart.remove(tblPart.getSelectionIndices());
				}
			});
			
			btnPartPaste = new Button(container, SWT.FLAT);
			btnPartPaste.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnPartPaste.setImage(new Image(parent.getDisplay(), DDECreationSWTDialog.class.getClassLoader().getResourceAsStream("icons/paste_16.png")));
			btnPartPaste.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnPartPaste.setToolTipText("Paste copied parts");
			btnPartPaste.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					AIFClipboard clipBoard = AIFPortal.getClipboard();
					if(!clipBoard.isEmpty()) {
						Vector<TCComponent> clipboardComponents = clipBoard.toVector();
						for(TCComponent objectRevision : clipboardComponents) {
							try {
								String className = objectRevision.getClass().getName();
								String typeName = className.substring(className.lastIndexOf(".")+1, className.length());
								if(typeName.equals("TCComponentItemRevision") ||
									typeName.equals("TCComponentBOMLine")) {
									String[] propValues = objectRevision.getProperties(new String[] {"object_string","object_type"});
									TableItem row = new TableItem(tblPart, SWT.NONE);
									row.setText(propValues);
								}
							} 
							catch (TCException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			});
	
			Label lblVendor = new Label(container, SWT.NONE);
			lblVendor.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblVendor.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 2));
			lblVendor.setText("Vendor Contact:");
	
			tblVendor = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			GridData gd_tblVendor = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
			gd_tblVendor.heightHint = 150;
			tblVendor.setLayoutData(gd_tblVendor);
			tblVendor.setLinesVisible(true);
			tblVendor.setToolTipText("Search and choose Vendors (Mandatory");
			TableColumn VendorDetails = new TableColumn(tblVendor, SWT.FILL);
			VendorDetails.setResizable(true);
			VendorDetails.setWidth(145);
	
			TableColumn emailID = new TableColumn(tblVendor, SWT.FILL);
			emailID.setResizable(true);
			emailID.setWidth(135);
	
			Button btnSearch = new Button(container, SWT.NONE);
			btnSearch.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnSearch.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnSearch.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/vendor_16.png"));
			btnSearch.setToolTipText("Open Vendor Search dialog");
			btnSearch.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					SearchCompanyContact_Dialog container = new SearchCompanyContact_Dialog(parent.getShell());
					container.open();
					util.centerToScreen(container.getShell());
					Button ok = container.getOKButton();
	
					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
//							Table partTable = container.getSearchTable();
//							TableItem[] items = partTable.getSelection();
//							for(int row = 0; row < items.length; row++) {
//								TableItem setItem = new TableItem(tblVendor, SWT.NONE);
//								for(int col = 0; col < partTable.getColumnCount(); col++) {
//									setItem.setText(col, items[row].getText(col));
//								}
//							}
	
							container.getShell().dispose();
						}
					});
				}
			});
	
			Button btnRemove = new Button(container, SWT.NONE);
			btnRemove.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnRemove.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			btnRemove.setImage(new Image(parent.getDisplay(), DDECreationSWTDialog.class.getClassLoader().getResourceAsStream("icons/remove_16.png")));
			btnRemove.setToolTipText("Remove selected parts");
			btnRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					List<Integer> delList = new LinkedList<Integer>();
					TableItem[] dduItems = tblDDU.getItems();
					if(dduItems.length > 0) {
						int i = 0;
						for (TableItem dduItem : dduItems) {
							for (TableItem vendorItem : tblVendor.getSelection()) {
								if(dduItem.getText(0).compareToIgnoreCase(vendorItem.getText(0)) == 0) {
									delList.add(i);
								}
							}
							i++;
						}	
					}
					tblVendor.remove(tblVendor.getSelectionIndices());
					for (int delIndex : delList) {
						tblDDU.remove(delIndex);
					}
				}
			});
	
			Label lblRevisionRule = new Label(container, SWT.NONE);
			lblRevisionRule.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblRevisionRule.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblRevisionRule.setText("Revision Rule:");
	
			String[] revRules = util.getRevisionRule(session);
			cbRule = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
			cbRule.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			cbRule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			cbRule.setItems(revRules);
			cbRule.setText("VINFAST_WORKING_RULE");
			new Label(container, SWT.NONE);
			
			Label lblAttachments = new Label(container, SWT.NONE);
			lblAttachments.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblAttachments.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblAttachments.setText("Attachments:");
	
			txtAttachment = new Text(container, SWT.BORDER);
			txtAttachment.setEnabled(false);
			txtAttachment.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			txtAttachment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtAttachment.setToolTipText("Choose file type *.zip/*.pdf (Optiona)");
	
			btnAttachment = new Button(container, SWT.NONE);
			btnAttachment.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnAttachment.setImage(new Image(parent.getDisplay(), DDECreationSWTDialog.class.getClassLoader().getResourceAsStream("icons/importobjects_16.png")));
			btnAttachment.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {

					FileDialog fd = new FileDialog(getShell(), SWT.SELECTED);
					fd.setFilterPath("C:/");
					String[] filterExt = { "*.zip", "*.pdf"};
					fd.setFilterExtensions(filterExt);
					String selected = fd.open();
					if(selected != null)
					txtAttachment.setText(selected);
				}
			});
			Label lblEcnNumber = new Label(container, SWT.NONE);
			lblEcnNumber.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblEcnNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblEcnNumber.setText("ECN Number:");
			
			txtECNNumber = new Text(container, SWT.BORDER);
			txtECNNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtECNNumber.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			txtECNNumber.setToolTipText("Enter ECN Number (Optional)");
			new Label(container, SWT.NONE);
			
			Label lblDescription = new Label(container, SWT.NONE);
			lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
			lblDescription.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			lblDescription.setText("Description:");
	
			txtDescription = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_txtDescription.heightHint = 72;
			txtDescription.setLayoutData(gd_txtDescription);
			txtDescription.setToolTipText("Enter Description (Optional)");
			new Label(container, SWT.NONE);
			
			ckbVendorUpload = new Button(container, SWT.CHECK);
			ckbVendorUpload.setSelection(true);
			ckbVendorUpload.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
			ckbVendorUpload.setText("Allow Vendor Upload");
			ckbVendorUpload.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			ckbVendorUpload.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					if(!ckbVendorUpload.getSelection()) {
						ckbSendData.setSelection(true);
						updateSendDataUI();
					}
					updateVendorUpdateUI();
				}
			});
			
			Label lblEcnNumber_1 = new Label(container, SWT.NONE);
			lblEcnNumber_1.setText("Edit Exchange Lines:");
			lblEcnNumber_1.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			
			tblDDU = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			tblDDU.setToolTipText("Search and choose Vendors (Mandatory");
			tblDDU.setLinesVisible(true);
			tblDDU.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
			
			TableColumn tcVendor = new TableColumn(tblDDU, 0);
			tcVendor.setWidth(145);
			tcVendor.setResizable(true);
			
			TableColumn tcDes = new TableColumn(tblDDU, 0);
			tcDes.setWidth(135);
			tcDes.setResizable(true);
			
			TableColumn tcDueDate = new TableColumn(tblDDU, 0);
			tcDueDate.setWidth(135);
			tcDueDate.setResizable(true);
			
			TableColumn tcExpectedFormat = new TableColumn(tblDDU, 0);
			tcExpectedFormat.setWidth(135);
			tcExpectedFormat.setResizable(true);
			
			TableColumn tcRemark = new TableColumn(tblDDU, 0);
			tcRemark.setWidth(135);
			tcRemark.setResizable(true);
			
			btnAddDDU = new Button(container, SWT.NONE);
			btnAddDDU.setToolTipText("Open Vendor Search dialog");
			btnAddDDU.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/add16.png"));
			btnAddDDU.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnAddDDU.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					DDUCreate_Dialog dduDlg = new DDUCreate_Dialog(parent.getShell());
					dduDlg.open();
					util.centerToScreen(container.getShell());
					
					dduDlg.cbVendor.setItems(getVendorList());
					dduDlg.cbFormat.setItems(fileFormat);
					Button ok = dduDlg.getOKButton();
					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
//							String abcString = dduDlg.txtDescription.getText();
							TableItem setItem = new TableItem(tblDDU, SWT.NONE);
							setItem.setText(0, dduDlg.cbVendor.getText());
							setItem.setText(1, dduDlg.txtDescription.getText());
							//Due date
							Calendar cal = Calendar.getInstance();
							cal.set(Calendar.YEAR, dduDlg.dtDueDate.getYear());
							cal.set(Calendar.MONTH, dduDlg.dtDueDate.getMonth());
							cal.set(Calendar.DAY_OF_MONTH, dduDlg.dtDueDate.getDay());
							setItem.setText(2, dateFormat.format(cal.getTime()));
							//
							setItem.setText(3, dduDlg.txtRemark.getText());
							setItem.setText(4, dduDlg.cbFormat.getText());
	
							dduDlg.getShell().dispose();
						}
					});
				}
			});
			new Label(container, SWT.NONE);
			
			btnRemoveDDU = new Button(container, SWT.NONE);
			btnRemoveDDU.setToolTipText("Open Vendor Search dialog");
			btnRemoveDDU.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/remove_16.png"));
			btnRemoveDDU.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.NORMAL));
			btnRemoveDDU.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					tblDDU.remove(tblDDU.getSelectionIndices());
				}
			});
			
			lblUseVscf = new Label(container, SWT.NONE);
			lblUseVscf.setText("Use VSCF:");
			lblUseVscf.setFont(SWTResourceManager.getFont("Calibri", 11, SWT.NORMAL));
			
			ckbVSCF = new Button(container, SWT.CHECK);
			new Label(container, SWT.NONE);
	
			Label label = new Label(container, SWT.HORIZONTAL);
			GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
			gd_label.widthHint = 431;
			label.setLayoutData(gd_label);
			
			UpdateUI();
		} 
		catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}
	
	private void updateVendorUpdateUI() {
		if(!ckbVendorUpload.getSelection()) {
			tblDDU.clearAll();
		}
		btnAddDDU.setEnabled(ckbVendorUpload.getSelection());
		btnRemoveDDU.setEnabled(ckbVendorUpload.getSelection());
	}
	
	private void updateSendDataUI() {
		if(!ckbSendData.getSelection()) {
			tblPart.clearAll();
			txtAttachment.setText("");
			txtECNNumber.setText("");
			txtDescription.setText("");
		}
		btnPartSearch.setEnabled(ckbSendData.getSelection());
		btnPartRemove.setEnabled(ckbSendData.getSelection());
		btnPartPaste.setEnabled(ckbSendData.getSelection());
		btnAttachment.setEnabled(ckbSendData.getSelection());
	}
	
	private String[] getVendorList() {
		List<String> vendorList = new LinkedList<String>();
		TableItem[] itemDDUs = tblDDU.getItems();
		TableItem[] itemVendors = tblVendor.getItems(); 
		if(itemVendors.length > 0) {
			for (TableItem vendor : itemVendors) {
				boolean check = true;
				for (TableItem ddu : itemDDUs) {
					if(vendor.getText(0).compareToIgnoreCase(ddu.getText(0)) == 0) {
						check = false;
					}
				}
				if(check) {
					vendorList.add(vendor.getText(0));
				}
			}
		}
		return vendorList.toArray(new String[0]);
	}
	
	private void UpdateUI() {
		ckbVSCF.setVisible(isShowVSCF);
		lblUseVscf.setVisible(true);
		ckbVSCF.setSelection(isDefaultVSCF);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Send Data to Supplier");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(551, 694);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Auto-generated method stub
		super.createButtonsForButtonBar(parent);
		btnCreate = getButton(IDialogConstants.OK_ID);
		btnCreate.setText("Create");
		btnCreate.setToolTipText("Click to Create DDE Object");
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		int partCount = tblPart.getItemCount();
		int vendorCount = tblVendor.getItemCount();
		
		if(ckbSendData.getSelection()) {
			if(partCount == 0 || vendorCount == 0) {
				MessageBox.post("Mandatory fields are not filled.", "Part and Vendor is mandatory to create DDE Object. Please choose and re-try", "Invalid...", MessageBox.ERROR);
				return;
			}	
		}
		else {
			if(vendorCount == 0) {
				MessageBox.post("Mandatory fields are not filled.", "Vendor is mandatory to create DDE Object. Please choose and re-try", "Invalid...", MessageBox.ERROR);
				return;
			}
		}
		
		TCComponent[] partsList = null;
		TCComponent[] vendorList = null;
		TCComponentDataset dataset = null;
		TableItem[] parts = tblPart.getItems();
		TableItem[] vendors = tblVendor.getItems();

		if(parts.length > 0) {
			partsList = new TCComponent[parts.length];
			for(int pCount = 0; pCount < parts.length ; pCount++) {
				String partName = parts[pCount].getText(0);
				String partType = parts[pCount].getText(1);

				String[] split = partName.split("-", 2);
				String[] splitID = split[0].split("/");

				String[] entires = {"Item ID","Revision","Name","Type"};
				String[] values = {splitID[0].trim(),splitID[1].trim(),split[1].trim(),partType};

				TCComponent[] qryObjects = new Utilities().searchObjects(session, entires, values, "Item Revision...");
				System.out.println("");
				partsList[pCount] = qryObjects[0];
			}
		}

		if(vendors.length > 0) {
			vendorList = new TCComponent[vendors.length];
			for(int vCount = 0; vCount < vendors.length ; vCount++) {
				String name = vendors[vCount].getText(0);
				String email = vendors[vCount].getText(1);

				String[] entires = {"Name","Email"};
				String[] values = {name.trim(),email};

				TCComponent[] qryObjects = new Utilities().searchObjects(session, entires, values, "Vendor Contact Search");
				vendorList[vCount] = qryObjects[0];
			}
		}
		
		if(txtAttachment.getText().equals("") == false) {
			String file = txtAttachment.getText();
			String extension = "";
			int i = file.lastIndexOf('.');
			if (i >= 0) {
				extension = file.substring(i+1);
			}

			if(extension.equals("") == false){
				if(extension.equalsIgnoreCase("pdf")) {
					dataset = new Utilities().createDataset(session, file, "PDF", "PDF_Reference");
				}
				else if(extension.equalsIgnoreCase("zip")) {
					dataset = new Utilities().createDataset(session, file, "Zip", "ZIPFILE");
				}
				else {
					MessageBox.post("Invalid file type.", "Please select ZIP or PDF type only. Please choose and re-try", "Invalid...", MessageBox.ERROR);
					return;
				}
			}
		}

		TCComponentItem item = createDDEObject(session, partsList, vendorList, dataset, txtECNNumber.getText(),txtDescription.getText() );
		if(ckbVendorUpload.getSelection()) {
			createDDUObject(session, item);	
		}
		
		if(!cbRule.getText().isEmpty()){
			try {
				item.setStringProperty("vf4_revision_rule", cbRule.getText());
				String status = "REQUEST";
				if(isShowVSCF) {
					if(ckbVSCF.getSelection()) {
						status = "REQUEST2";
					}	
				}
				item.setStringProperty("vf4_status", "REQUEST");
			} 
			catch (TCException e1) {
				e1.printStackTrace();
			}
		}
		MessageBox.post("DDE Object:-" + item.toString() + " created succesfully", "Check in selected folder", "Success", MessageBox.INFORMATION);
		super.okPressed();
	}
	
	private TCComponent getVendor(String vendorName) {
		String email = "";
		
		for (TableItem tableItem : tblVendor.getItems()) {
			if(tableItem.getText(0).compareToIgnoreCase(vendorName) == 0)
				email = tableItem.getText(1);
		}

		String[] entires = {"Name", "Email"};
		String[] values = {vendorName.trim(), email};

		TCComponent[] qryObjects = new Utilities().searchObjects(session, entires, values, "Vendor Contact Search");
		if(qryObjects != null && qryObjects.length > 0) {
			return (TCComponent)qryObjects[0];
		}
		return null;
	}
	
	private void createDDUObject(TCSession session, TCComponentItem ddeItem) {
		DataManagementService dms = DataManagementService.getService(session) ;
		try {
			String ddeId = ddeItem.getPropertyDisplayableValue("item_id");
			TCComponentItemRevision dduItem = null;
			
			if(tblDDU.getItemCount() > 0) {
				TCComponent[] ddus = new TCComponent[tblDDU.getItemCount()];
				int i = 0;
				for (TableItem item : tblDDU.getItems()) {
					InfoForNextId nextID = new InfoForNextId();
					nextID.propName = "item_id";
					nextID.typeName = "VF4_DDU_Event";
					nextID.pattern = "DDUENNNNNNNN";

					GetNextIdsResponse IDReponse = dms.getNextIds(new InfoForNextId[] {nextID});
					String[] ids = IDReponse.nextIds;

					CreateIn itemDef = new CreateIn();
					itemDef.clientId = "1";
					itemDef.data.boName = "VF4_DDU_Event"; 
					itemDef.data.stringProps.put("item_id", ids[0]);
					itemDef.data.stringProps.put("object_name", ddeId + "_" + item.getText(0));
					//Item revision
					CreateInput itemRevisionDef = new CreateInput();
					itemRevisionDef.boName = "VF4_DDU_EventRevision"; //Item rev BO
					itemRevisionDef.stringProps.put("item_revision_id", "01");
					
					itemRevisionDef.stringProps.put("object_desc", item.getText(1));
					if(!item.getText(2).isEmpty()) {
						Calendar cal = Calendar.getInstance();
						SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
						cal.setTime(sdf.parse(item.getText(2)));
						itemRevisionDef.dateProps.put("vf4_due_date", cal);
					}
					itemRevisionDef.stringProps.put("vf4_remarks", item.getText(3));
					itemRevisionDef.stringProps.put("vf4_expected_format", item.getText(4));
					itemRevisionDef.stringProps.put("vf4_status", "PREPARE");
					
					itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
					CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

					ServiceData sd = response.serviceData;
					CreateOut[] createOutResp = response.output ;
					TCComponent[] objects = createOutResp[0].objects;

					if (sd.sizeOfPartialErrors() > 0) {
						ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors()-1) ;
						ErrorValue[] errorValue = errorStack.getErrorValues() ;
						for (int inx = 1 ; inx < errorValue.length ; inx++) {
							System.out.println(errorValue[inx].getMessage());
						}
					}
					else {
						ddus[i] = (TCComponent)objects[0];
						dduItem = (TCComponentItemRevision)objects[2];
						dduItem.setRelated("ContactInCompany", new TCComponent[] { getVendor(item.getText(0)) });
					}
					i++;
				}
				ddeItem.setRelated("VF4_receive_data_relation", ddus);
			}
			else {
				TCComponent[] ddus = new TCComponent[tblVendor.getItemCount()];
				int i = 0;
				for (TableItem item : tblVendor.getItems()) {
					InfoForNextId nextID = new InfoForNextId();
					nextID.propName = "item_id";
					nextID.typeName = "VF4_DDU_Event";
					nextID.pattern = "DDUENNNNNNNN";

					GetNextIdsResponse IDReponse = dms.getNextIds(new InfoForNextId[] {nextID});
					String[] ids = IDReponse.nextIds;

					CreateIn itemDef = new CreateIn();
					itemDef.clientId = "1";
					itemDef.data.boName = "VF4_DDU_Event"; 
					itemDef.data.stringProps.put("item_id", ids[0]);
					itemDef.data.stringProps.put("object_name", ddeId + "_" + item.getText(0));
					//Item revision
					CreateInput itemRevisionDef = new CreateInput();
					itemRevisionDef.boName = "VF4_DDU_EventRevision"; //Item rev BO
					itemRevisionDef.stringProps.put("item_revision_id", "01");
					
					itemRevisionDef.stringProps.put("object_desc", ddeId + "_" + item.getText(0));
					//
					Calendar cal = Calendar.getInstance();
					Date today = new Date();
					cal.setTime(today);
					cal.add(Calendar.DATE, 30);
					itemRevisionDef.dateProps.put("vf4_due_date", cal);
					//
					itemRevisionDef.stringProps.put("vf4_expected_format", "Zip");
					itemRevisionDef.stringProps.put("vf4_status", "PREPARE");
					
					itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
					CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

					ServiceData sd = response.serviceData;
					CreateOut[] createOutResp = response.output ;
					TCComponent[] objects = createOutResp[0].objects;

					if (sd.sizeOfPartialErrors() > 0) {
						ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors()-1) ;
						ErrorValue[] errorValue = errorStack.getErrorValues() ;
						for (int inx = 1 ; inx < errorValue.length ; inx++) {
							System.out.println(errorValue[inx].getMessage());
						}
					}
					else {
						ddus[i] = (TCComponent)objects[0];
						dduItem = (TCComponentItemRevision)objects[2];
						dduItem.setRelated("ContactInCompany", new TCComponent[] { getVendor(item.getText(0)) });
					}
					i++;
				}
				ddeItem.setRelated("VF4_receive_data_relation", ddus);
			}
		}
		catch(Exception exp){
			exp.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public TCComponentItem createDDEObject(TCSession session, TCComponent[] part, TCComponent[] vendor, TCComponentDataset dataset, String name, String desc) {
		DataManagementService dms = DataManagementService.getService(session) ;
		TCComponentItem item = null;

		try {
			InfoForNextId nextID = new InfoForNextId();
			nextID.propName = "item_id";
			nextID.typeName = "VF4_SCDDE";
			nextID.pattern = "DDENNNNNNNN";

			GetNextIdsResponse IDReponse = dms.getNextIds(new InfoForNextId[] {nextID});
			String[] ids = IDReponse.nextIds;

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = "VF4_SCDDE"; 
			itemDef.data.stringProps.put("item_id", ids[0]);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", desc);
//			itemDef.data.stringProps.put("vf4_status", status);
			//Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = "VF4_SCDDERevision"; //Item rev BO
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", desc);
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] {itemRevisionDef	});
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			ServiceData sd = response.serviceData;
			CreateOut[] createOutResp = response.output ;
			TCComponent[] objects = createOutResp[0].objects;

			if (sd.sizeOfPartialErrors() > 0) {
				ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors()-1) ;
				ErrorValue[] errorValue = errorStack.getErrorValues() ;
				for (int inx = 1 ; inx < errorValue.length ; inx++) {
					System.out.println(errorValue[inx].getMessage());
				}

			}
			else {
				item = (TCComponentItem)objects[0];
				item.setRelated("IMAN_requirement",part);
				if(dataset != null) {
					item.add("IMAN_requirement", new TCComponent[] { dataset });
//					item.setRelated("IMAN_requirement",new TCComponent[] { dataset });
				}
				item.setRelated("ContactInCompany", vendor);
			}
		}
		catch(Exception exp){
			exp.printStackTrace();
		}
		return item;
	}
}
