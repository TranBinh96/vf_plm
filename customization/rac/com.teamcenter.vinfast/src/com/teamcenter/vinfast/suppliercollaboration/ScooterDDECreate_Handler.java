package com.teamcenter.vinfast.suppliercollaboration;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
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
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.vinfast.subdialog.SearchPartRev_Dialog;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.Utilities;

public class ScooterDDECreate_Handler extends AbstractHandler {
	private TCSession session;
	private ScooterDDECreate_Dialog dlg;
	private TCComponent selectedObject;

	private ProgressMonitorDialog progressMonitorDialog = null;
	private LinkedList<TCComponent> partLists = null;
	private String[] GROUP_PERMISSION = { "Vendor Contact.Supplier Collaboration.VINFAST", "dba" };
	private boolean isECNSelected = false;

	public ScooterDDECreate_Handler() {
		super();
	}

	public class DDEPack {
		public LinkedList<TCComponent> partList = new LinkedList<TCComponent>();
		public LinkedList<TCComponentDataset> fileAttach = new LinkedList<TCComponentDataset>();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			String objectType = selectedObject.getType();
			if (objectType.compareTo("VF4_ChangeReq_ESRevision") == 0)
				isECNSelected = true;

			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized to create DDE.", "Please change to group: " + String.join(" or ", GROUP_PERMISSION) + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			partLists = new LinkedList<TCComponent>();

			dlg = new ScooterDDECreate_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);

			dlg.btnAttachmentRemove.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					int[] items = dlg.tblParts.getSelectionIndices();
					if (items.length > 0) {
						for (int index : items) {
							dlg.tblParts.getItem(index).setText(5, "");
						}
					}
				}
			});

			dlg.btnAttachmentAdd.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					FileDialog fd = new FileDialog(dlg.getShell(), SWT.SELECTED);
					fd.setFilterPath("C:/");
					String[] filterExt = { "*.zip", "*.pdf" };
					fd.setFilterExtensions(filterExt);
					String selected = fd.open();
					if (selected != null) {
						TableItem[] tableItems = dlg.tblParts.getSelection();
						if (tableItems != null && tableItems.length > 0) {
							tableItems[0].setText(5, selected);
						}
					}
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
										addPartTableRow(itemPaste);
									}
								}
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			});

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					createNewItem();
				}
			});

			if (isECNSelected) {
				getSolutionItem();
				dlg.txtECNNumber.setText(selectedObject.toString());
			}

			dlg.btnPartSearch.setVisible(!isECNSelected);
			dlg.btnPartPaste.setVisible(!isECNSelected);
			dlg.btnPartRemove.setVisible(!isECNSelected);

			dlg.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private void addPartTableRow(TCComponentItemRevision itemRev) {
		try {
			String itemID = itemRev.getPropertyDisplayableValue("item_id");
			String revID = itemRev.getPropertyDisplayableValue("item_revision_id");
			String objectType = itemRev.getPropertyDisplayableValue("object_type");
			String supplierCode = "";
			String supplierName = "";
			String contactEmail = "";

			TCComponentItem item = ((TCComponentItemRevision) itemRev).getItem();
			TCComponent[] forms = item.getRelatedComponents("VF4_Supplier_Info_Relation");
			if (forms != null && forms.length > 0) {
				supplierCode = forms[0].getPropertyDisplayableValue("vf4_itm_supplier_code");
				if (!supplierCode.isEmpty()) {
					TCComponent vendor = getVendor(supplierCode);
					if (vendor != null) {
						contactEmail = vendor.getPropertyDisplayableValue("email_address");
					}
				}
				supplierName = forms[0].getPropertyDisplayableValue("vf4cp_itm_supplier_name");
			}

			partLists.add((TCComponentItemRevision) itemRev);
			TableItem newTableItem = new TableItem(dlg.tblParts, SWT.NONE);
			newTableItem.setText(new String[] { itemID, revID, objectType, supplierCode, contactEmail, "" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getSolutionItem() {
		try {
			TCComponent[] itemList = selectedObject.getRelatedComponents("CMHasSolutionItem");
			if (itemList != null) {
				for (TCComponent itemRev : itemList) {
					if (itemRev instanceof TCComponentItemRevision) {
						addPartTableRow((TCComponentItemRevision) itemRev);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createNewItem() {
		LinkedHashMap<TCComponent, DDEPack> ddeList = new LinkedHashMap<TCComponent, DDEPack>();
		int index = 0;
		for (TableItem tableItem : dlg.tblParts.getItems()) {
			String supCode = tableItem.getText(3);
			TCComponent vendor = getVendor(supCode);
			TCComponentDataset attachDataset = getDataSet(tableItem.getText(5));
			if (vendor != null) {
				if (ddeList.containsKey(vendor)) {
					ddeList.get(vendor).partList.add(partLists.get(index));
					if (attachDataset != null)
						ddeList.get(vendor).fileAttach.add(attachDataset);
				} else {
					DDEPack newItem = new DDEPack();
					newItem.partList.add(partLists.get(index));
					if (attachDataset != null)
						newItem.fileAttach.add(attachDataset);
					ddeList.put(vendor, newItem);
				}
			}
			index++;
		}

		if (ddeList.size() == 0) {
			dlg.setMessage("No have vendor to create DDE.", IMessageProvider.WARNING);
			return;
		}

		String ddeName = dlg.txtECNNumber.getText();
		String ddeDesc = dlg.txtDesc.getText();

		DataManagementService dms = DataManagementService.getService(session);
		HashSet<String> ddeNumbers = new HashSet<String>();
		try {
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Create DDE...", IProgressMonitor.UNKNOWN);
					for (Map.Entry<TCComponent, DDEPack> ddeItem : ddeList.entrySet()) {
						try {
							DDEPack ddePack = ddeItem.getValue();
							TCComponentItem item = null;
							String name = ddeName;
							String desc = ddeDesc;

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
							itemDef.data.stringProps.put("vf4_product_line", "ESCOOTER");
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

							if (sd.sizeOfPartialErrors() == 0) {
								item = (TCComponentItem) objects[0];
								session.getUser().getNewStuffFolder().add("contents", item);
								ddeNumbers.add(item.getPropertyDisplayableValue("item_id"));
								if (ddePack.partList.size() > 0)
									item.setRelated("IMAN_requirement", ddePack.partList.toArray(new TCComponent[0]));

								if (ddePack.fileAttach != null && ddePack.fileAttach.size() > 0)
									item.add("IMAN_requirement", ddePack.fileAttach.toArray(new TCComponentDataset[0]));

								item.setRelated("ContactInCompany", new TCComponent[] { ddeItem.getKey() });
								item.setStringProperty("vf4_status", "REQUEST");
							}
						} catch (Exception exp) {
							exp.printStackTrace();
						}
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		dlg.setMessage("DDE create successfully: " + String.join(", ", ddeNumbers), IMessageProvider.INFORMATION);
		dlg.btnCreate.setEnabled(false);
	}

	private TCComponent getVendor(String vendorCode) {
		if (vendorCode.isEmpty())
			return null;

		LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
		queryInput.put("Vendor Code", vendorCode);

		TCComponent[] queryOutput = TCExtension.queryItem(session, queryInput, "Vendor");
		if (queryOutput == null || queryOutput.length == 0)
			return null;

		try {
			TCComponent[] contacts = queryOutput[0].getReferenceListProperty("ContactInCompany");
			if (contacts == null || contacts.length == 0)
				return null;

			for (TCComponent contact : contacts) {
				String contactName = contact.getPropertyDisplayableValue("object_name");
				String[] str = contactName.split(" ");
				if (str.length >= 2) {
					if (str[0].compareTo("SCF") == 0)
						return contact;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private TCComponentDataset getDataSet(String filePath) {
		if (filePath.isEmpty())
			return null;

		TCComponentDataset dataset = null;
		String extension = "";
		int i = filePath.lastIndexOf('.');
		if (i >= 0) {
			extension = filePath.substring(i + 1);
		}

		if (extension.equals("") == false) {
			if (extension.equalsIgnoreCase("pdf")) {
				dataset = new Utilities().createDataset(session, filePath, "PDF", "PDF_Reference");
			} else if (extension.equalsIgnoreCase("zip")) {
				dataset = new Utilities().createDataset(session, filePath, "Zip", "ZIPFILE");
			}
		}

		return dataset;
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
}
