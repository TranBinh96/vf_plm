package com.teamcenter.vinfast.suppliercollaboration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
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
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.subdialog.SearchECNRev_Dialog;
import com.teamcenter.vinfast.subdialog.SearchPartRev_Dialog;
import com.teamcenter.vinfast.subdialog.SearchCompanyContact_Dialog;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.Utilities;

public class DDECreate_Handler extends AbstractHandler {
	private TCSession session;
	private DDECreate_Dialog dlg;

	private Boolean isShowVSCF = false;
	private Boolean isDefaultVSCF = false;

	private LinkedList<TCComponent> partLists = null;
	private LinkedList<TCComponent> vendorLists = null;
	private TCComponent ecrItemRev = null;
	private int defaultDayAfter = 45;
	private String[] GROUP_PERMISSION = { "Vendor Contact.Supplier Collaboration.VINFAST", "dba" };

	public DDECreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized to create DDE.", "Please change to group: " + String.join(" or ", GROUP_PERMISSION) + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			partLists = new LinkedList<TCComponent>();
			vendorLists = new LinkedList<TCComponent>();

			dlg = new DDECreate_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);
			// ---------------------------- Init -------------------------------------
			String[] settings = TCExtension.GetPreferenceValues("VINF_DDE_SETTING", session);
			if (settings != null) {
				String[] str = settings[0].split(",");
				if (str.length > 1) {
					if (str[0].compareToIgnoreCase("1") == 0)
						isShowVSCF = true;
					if (str[1].compareToIgnoreCase("1") == 0)
						isDefaultVSCF = true;
				}
				if (settings.length > 1) {
					if (StringExtension.isInteger(settings[1], 10)) {
						defaultDayAfter = Integer.parseInt(settings[1]);
					}
				}
			}
			String[] revRulesDataForm = TCExtension.GetRevisionRules(session);
			String[] fileFormat = TCExtension.GetLovValues("vf4_expected_format", "VF4_DDU_EventRevision", session);
			String[] exportFormatDataForm = TCExtension.GetLovValues("vf4_export_type", "VF4_SCDDE", session);
			DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
			// -----------------------------------------------------------------------
			dlg.ckbSendData.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					if (!dlg.ckbSendData.getSelection()) {
						dlg.ckbVendorUpload.setSelection(true);
						updateVendorUpdateUI();
					}
					updateSendDataUI();
				}
			});

			dlg.ckbVendorUpload.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					if (!dlg.ckbVendorUpload.getSelection()) {
						dlg.ckbSendData.setSelection(true);
						updateSendDataUI();
					}
					updateVendorUpdateUI();
				}
			});

			dlg.btnPartSearch.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					SearchPartRev_Dialog searchDlg = new SearchPartRev_Dialog(dlg.getShell());
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							int[] items = searchDlg.tblSearch.getSelectionIndices();
							for (int index = 0; index < items.length; index++) {
								TCComponent item = searchDlg.itemSearch.get(items[index]);
								if (!checkDuplicate(item, partLists)) {
									try {
										partLists.add(item);
										TableItem setItem = new TableItem(dlg.tblParts, SWT.NONE);
										String[] value = { item.getPropertyDisplayableValue("object_string"), item.getPropertyDisplayableValue("object_type") };
										setItem.setText(value);
									} catch (Exception e2) {
									}
								}
							}

							searchDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnPartRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					int[] items = dlg.tblParts.getSelectionIndices();
					if (items.length > 0) {
						LinkedList<TCComponent> removePart = new LinkedList<TCComponent>();
						for (int index : items) {
							removePart.add(partLists.get(index));
						}
						for (TCComponent part : removePart) {
							partLists.remove(part);
						}
						dlg.tblParts.remove(items);
					}
				}
			});

			dlg.btnPartPaste.addListener(SWT.Selection, new Listener() {
				@SuppressWarnings("unchecked")
				public void handleEvent(Event e) {
					AIFClipboard clipBoard = AIFPortal.getClipboard();
					if (!clipBoard.isEmpty()) {
						Vector<TCComponent> clipboardComponents = clipBoard.toVector();
						for (TCComponent item : clipboardComponents) {
							try {
								TCComponentItemRevision itemPaste = null;
								if (item instanceof TCComponentItemRevision) {
									itemPaste = (TCComponentItemRevision) item;
								} else if (item instanceof TCComponentBOMLine) {
									itemPaste = ((TCComponentBOMLine) item).getItemRevision();
								}

								if (itemPaste != null) {
									if (!checkDuplicate(itemPaste, partLists)) {
										partLists.add(itemPaste);
										TableItem setItem = new TableItem(dlg.tblParts, SWT.NONE);
										String[] value = { itemPaste.getPropertyDisplayableValue("object_string"), itemPaste.getPropertyDisplayableValue("object_type") };
										setItem.setText(value);
									}
								}
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			});

			dlg.btnVendorSearch.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					SearchCompanyContact_Dialog searchDlg = new SearchCompanyContact_Dialog(dlg.getShell());
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							int[] items = searchDlg.tblSearch.getSelectionIndices();
							for (int index = 0; index < items.length; index++) {
								TCComponent item = searchDlg.itemSearch.get(items[index]);
								if (!checkDuplicate(item, vendorLists)) {
									try {
										vendorLists.add(item);
										TableItem setItem = new TableItem(dlg.tblVendors, SWT.NONE);
										String vendorContact = item.getPropertyDisplayableValue("object_name");
										String vendorEmail = item.getPropertyDisplayableValue("email_address");
										String company = item.getPropertyDisplayableValue("sc0VendorObject");
										String vendorCode = "";
										String vendorName = "";
										if (company.contains("-")) {
											String[] str = company.split("-");
											vendorCode = str[0];
											vendorName = str[1];
										}
										setItem.setText(new String[] { vendorCode, vendorName, vendorContact, vendorEmail });
									} catch (Exception e2) {
									}
								}
							}

							searchDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnVendorRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					int[] items = dlg.tblVendors.getSelectionIndices();
					if (items.length > 0) {
						LinkedList<TCComponent> removeVendor = new LinkedList<TCComponent>();
						for (int index : items) {
							removeVendor.add(vendorLists.get(index));
						}
						for (TCComponent vendor : removeVendor) {
							vendorLists.remove(vendor);
						}
						dlg.tblVendors.remove(items);
					}
				}
			});

			dlg.cbRevisionRule.setItems(revRulesDataForm);
			dlg.cbRevisionRule.setText("VINFAST_WORKING_RULE");

			dlg.cbExportType.setItems(exportFormatDataForm);
			dlg.cbExportType.setText("Default");

			dlg.btnAttachment.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					FileDialog fd = new FileDialog(dlg.getShell(), SWT.SELECTED);
					fd.setFilterPath("C:/");
					String[] filterExt = { "*.zip", "*.pdf" };
					fd.setFilterExtensions(filterExt);
					String selected = fd.open();
					if (selected != null)
						dlg.txtAttachment.setText(selected);
				}
			});

			dlg.btnECNSearch.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					SearchECNRev_Dialog searchDlg = new SearchECNRev_Dialog(dlg.getShell(), "Engineering Change Notice Revision");
					searchDlg.open();
					Button ok = searchDlg.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							int index = searchDlg.tblSearch.getSelectionIndex();
							ecrItemRev = searchDlg.itemSearch.get(index);

							try {
								dlg.txtECNNumber.setText(ecrItemRev.getPropertyDisplayableValue("object_string"));
							} catch (Exception e2) {
							}

							searchDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnDDUAdd.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					DDUCreate_Dialog dduDlg = new DDUCreate_Dialog(dlg.getShell());
					dduDlg.open();
					Button ok = dduDlg.getOKButton();

					dduDlg.cbVendor.setItems(getVendorList());
					dduDlg.cbFormat.setItems(fileFormat);
					dduDlg.cbFormat.setText("Zip");
					Calendar startCal = Calendar.getInstance();
					startCal.add(Calendar.DATE, defaultDayAfter);
					dduDlg.dtDueDate.setDate(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH));

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							TableItem setItem = new TableItem(dlg.tblDDU, SWT.NONE);
							setItem.setText(0, dduDlg.cbVendor.getText());
							setItem.setText(1, dduDlg.txtDescription.getText());

							Calendar cal = Calendar.getInstance();
							cal.set(Calendar.YEAR, dduDlg.dtDueDate.getYear());
							cal.set(Calendar.MONTH, dduDlg.dtDueDate.getMonth());
							cal.set(Calendar.DAY_OF_MONTH, dduDlg.dtDueDate.getDay());
							setItem.setText(2, dateFormat.format(cal.getTime()));

							setItem.setText(3, dduDlg.cbFormat.getText());
							setItem.setText(4, dduDlg.txtRemark.getText());

							dduDlg.getShell().dispose();
						}
					});
				}
			});

			dlg.btnDDURemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					dlg.tblDDU.remove(dlg.tblDDU.getSelectionIndices());
				}
			});

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					createNewItem();
				}
			});
			// -----------------------------------------------------------------------
			updateVSCFUI();
			updateSendDataUI();
			updateVendorUpdateUI();
			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private void updateVSCFUI() {
		dlg.ckbVSCF.setVisible(isShowVSCF);
		dlg.ckbVSCF.setSelection(isDefaultVSCF);
	}

	private void updateVendorUpdateUI() {
		if (!dlg.ckbVendorUpload.getSelection()) {
			dlg.tblDDU.clearAll();
		}
		dlg.btnDDUAdd.setEnabled(dlg.ckbVendorUpload.getSelection());
		dlg.btnDDURemove.setEnabled(dlg.ckbVendorUpload.getSelection());
	}

	private void updateSendDataUI() {
		if (!dlg.ckbSendData.getSelection()) {
			dlg.tblParts.clearAll();
			dlg.txtAttachment.setText("");
			dlg.txtECNNumber.setText("");
			dlg.txtDesc.setText("");
			partLists.clear();
		}
		dlg.btnPartSearch.setEnabled(dlg.ckbSendData.getSelection());
		dlg.btnPartRemove.setEnabled(dlg.ckbSendData.getSelection());
		dlg.btnPartPaste.setEnabled(dlg.ckbSendData.getSelection());
		dlg.btnAttachment.setEnabled(dlg.ckbSendData.getSelection());
		dlg.btnECNSearch.setEnabled(dlg.ckbSendData.getSelection());
	}

	private boolean checkDuplicate(TCComponent item, LinkedList<TCComponent> source) {
		if (source.size() > 0) {
			for (TCComponent part : source) {
				if (part == item)
					return true;
			}
		}

		return false;
	}

	private String[] getVendorList() {
		LinkedList<String> vendorList = new LinkedList<String>();
		TableItem[] itemDDUs = dlg.tblDDU.getItems();
		TableItem[] itemVendors = dlg.tblVendors.getItems();
		if (itemVendors.length > 0) {
			for (TableItem vendor : itemVendors) {
				boolean check = true;
				for (TableItem ddu : itemDDUs) {
					if (vendor.getText(0).compareToIgnoreCase(ddu.getText(0)) == 0) {
						check = false;
					}
				}
				if (check) {
					vendorList.add(vendor.getText(0));
				}
			}
		}
		return vendorList.toArray(new String[0]);
	}

	private TCComponentDataset getDataSet() {
		TCComponentDataset dataset = null;
		String file = dlg.txtAttachment.getText();
		String extension = "";
		int i = file.lastIndexOf('.');
		if (i >= 0) {
			extension = file.substring(i + 1);
		}

		if (extension.equals("") == false) {
			if (extension.equalsIgnoreCase("pdf")) {
				dataset = new Utilities().createDataset(session, file, "PDF", "PDF_Reference");
			} else if (extension.equalsIgnoreCase("zip")) {
				dataset = new Utilities().createDataset(session, file, "Zip", "ZIPFILE");
			}
		}

		return dataset;
	}

	private void createNewItem() {
		if (vendorLists.size() == 0) {
			MessageBox.post("Mandatory fields are not filled.", "Vendor is mandatory to create DDE Object. Please choose and re-try", "Invalid...", MessageBox.ERROR);
			return;
		}

		if (partLists.size() == 0 && dlg.ckbSendData.getSelection()) {
			MessageBox.post("Mandatory fields are not filled.", "Part is mandatory to create DDE Object. Please choose and re-try", "Invalid...", MessageBox.ERROR);
			return;
		}

		if (dlg.cbExportType.getText().isEmpty()) {
			MessageBox.post("Mandatory fields are not filled.", "Export Type is mandatory to create DDE Object. Please choose and re-try", "Invalid...", MessageBox.ERROR);
			return;
		}

		TCComponentDataset dataSet = null;
		if (!dlg.txtAttachment.getText().isEmpty()) {
			dataSet = getDataSet();
			if (dataSet == null) {
				MessageBox.post("Invalid file type.", "Please select ZIP or PDF type only. Please choose and re-try", "Invalid...", MessageBox.ERROR);
				return;
			}
		}

		for (TCComponent vendor : vendorLists) {
			try {
				String email = vendor.getPropertyDisplayableValue("email_address");
				String name = vendor.getPropertyDisplayableValue("sc0VendorObject");
				if (email.compareToIgnoreCase("change_my_mail@dummy.net") == 0) {
					MessageBox.post("Invalid vendor email.", "The supplier - " + name + " email address - change_my_mail@dummy.net is not valid. Please update the email address following HELP002 Document on TeamcCenter.", "Invalid...", MessageBox.ERROR);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// create new DDE object item
		try {
			DataManagementService dms = DataManagementService.getService(session);
			TCComponentItem item = null;
			String name = dlg.txtECNNumber.getText();
			String desc = dlg.txtDesc.getText();
			String exportType = dlg.cbExportType.getText();

			InfoForNextId nextID = new InfoForNextId();
			nextID.propName = "item_id";
			nextID.typeName = "VF4_SCDDE";
			nextID.pattern = "DDENNNNNNNN";

			GetNextIdsResponse IDReponse = dms.getNextIds(new InfoForNextId[] { nextID });
			String[] ids = IDReponse.nextIds;

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = "VF4_SCDDE";
			itemDef.data.stringProps.put("item_id", ids[0]);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", desc);
			itemDef.data.stringProps.put("vf4_product_line", "CAR");
			itemDef.data.stringProps.put("vf4_export_type", exportType);
			// Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = "VF4_SCDDERevision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", desc);
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			ServiceData sd = response.serviceData;
			CreateOut[] createOutResp = response.output;
			TCComponent[] objects = createOutResp[0].objects;

			if (sd.sizeOfPartialErrors() > 0) {
				for (int i = 0; i < sd.sizeOfPartialErrors(); i++) {
					for (String msg : sd.getPartialError(i).getMessages()) {
						MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
					}
				}
			} else {
				item = (TCComponentItem) objects[0];
				session.getUser().getNewStuffFolder().add("contents", item);
				// add part to DDE
				if (partLists.size() > 0) {
					item.setRelated("IMAN_requirement", partLists.toArray(new TCComponent[0]));
				}
				// add dataset to DDE
				if (dataSet != null) {
					item.add("IMAN_requirement", new TCComponent[] { dataSet });
				}
				// add vendor to DDE
				if (vendorLists.size() > 0) {
					item.setRelated("ContactInCompany", vendorLists.toArray(new TCComponent[0]));
				}
				// set status for DDE
				if (!dlg.cbRevisionRule.getText().isEmpty()) {
					try {
						item.setStringProperty("vf4_revision_rule", dlg.cbRevisionRule.getText());
						String status = "REQUEST";
						if (isShowVSCF) {
							if (dlg.ckbVSCF.getSelection()) {
								status = "REQUEST2";
							}
						}
						item.setStringProperty("vf4_status", status);
					} catch (TCException e1) {
						e1.printStackTrace();
					}
				}
				// create DDU
				if (dlg.ckbVendorUpload.getSelection()) {
					LinkedList<TCComponent> dduList = new LinkedList<TCComponent>();
					if (dlg.tblDDU.getItemCount() > 0) {
						for (TableItem rowItem : dlg.tblDDU.getItems()) {
							Calendar dueDate = Calendar.getInstance();
							if (!rowItem.getText(2).isEmpty()) {
								SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
								dueDate.setTime(sdf.parse(rowItem.getText(2)));
							} else {
								Date today = new Date();
								dueDate.setTime(today);
								dueDate.add(Calendar.DATE, defaultDayAfter);
							}
							TCComponent ddu = createDDUObject(rowItem.getText(0), item.getPropertyDisplayableValue("item_id") + "_" + rowItem.getText(0), rowItem.getText(1), dueDate, rowItem.getText(3), rowItem.getText(4), dms);
							if (ddu != null)
								dduList.add(ddu);
						}
					} else {
						for (TableItem rowItem : dlg.tblVendors.getItems()) {
							Calendar dueDate = Calendar.getInstance();
							Date today = new Date();
							dueDate.setTime(today);
							dueDate.add(Calendar.DATE, defaultDayAfter);
							TCComponent ddu = createDDUObject(rowItem.getText(0), item.getPropertyDisplayableValue("item_id") + "_" + rowItem.getText(0), item.getPropertyDisplayableValue("item_id") + "_" + rowItem.getText(0), dueDate, "Zip", "", dms);
							if (ddu != null)
								dduList.add(ddu);
						}
					}
					item.setRelated("VF4_receive_data_relation", dduList.toArray(new TCComponent[0]));
				}
				//
				resetUI();
				MessageBox.post("DDE Object:-" + item.toString() + " created and added into the DDE waiting queue.", item.toString() + " created in your Newstuff folder.", "Success", MessageBox.INFORMATION);
				openOnCreate(item);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private TCComponent createDDUObject(String vendorID, String dduName, String dduDesc, Calendar dduDueDate, String dduFormat, String dduRemark, DataManagementService dms) {
		TCComponent dduItem = null;
		try {
			InfoForNextId nextID = new InfoForNextId();
			nextID.propName = "item_id";
			nextID.typeName = "VF4_DDU_Event";
			nextID.pattern = "DDUENNNNNNNN";

			GetNextIdsResponse IDReponse = dms.getNextIds(new InfoForNextId[] { nextID });
			String[] ids = IDReponse.nextIds;

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = "VF4_DDU_Event";
			itemDef.data.stringProps.put("item_id", ids[0]);
			itemDef.data.stringProps.put("object_name", dduName);
			// Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = "VF4_DDU_EventRevision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", dduDesc);
			itemRevisionDef.dateProps.put("vf4_due_date", dduDueDate);
			itemRevisionDef.stringProps.put("vf4_remarks", dduRemark);
			itemRevisionDef.stringProps.put("vf4_expected_format", dduFormat);
			itemRevisionDef.stringProps.put("vf4_status", "PREPARE");

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			ServiceData sd = response.serviceData;
			CreateOut[] createOutResp = response.output;
			TCComponent[] objects = createOutResp[0].objects;

			if (sd.sizeOfPartialErrors() > 0) {
				ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
				ErrorValue[] errorValue = errorStack.getErrorValues();
				for (int inx = 1; inx < errorValue.length; inx++) {
					System.out.println(errorValue[inx].getMessage());
				}
			} else {
				dduItem = (TCComponentItem) objects[0];
				TCComponentItemRevision dduItemRev = (TCComponentItemRevision) objects[2];
				dduItemRev.setRelated("ContactInCompany", new TCComponent[] { getVendor(vendorID) });
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return dduItem;
	}

	private TCComponent getVendor(String vendorID) throws NotLoadedException {
		for (TCComponent item : vendorLists) {
			String company = item.getPropertyDisplayableValue("sc0VendorObject");
			if (company.contains("-")) {
				String vendorCode = "";
				String[] str = company.split("-");
				vendorCode = str[0];
				if (vendorCode.compareToIgnoreCase(vendorID) == 0)
					return item;
			}
		}

		return null;
	}

	private void resetUI() {
		dlg.tblParts.removeAll();
		dlg.tblVendors.removeAll();
		dlg.tblDDU.removeAll();
		dlg.txtAttachment.setText("");
		dlg.txtECNNumber.setText("");
		dlg.txtDesc.setText("");
		partLists.clear();
		vendorLists.clear();
	}

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
