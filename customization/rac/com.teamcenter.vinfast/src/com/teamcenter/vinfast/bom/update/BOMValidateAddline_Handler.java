package com.teamcenter.vinfast.bom.update;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class BOMValidateAddline_Handler extends AbstractHandler {
	private TCSession session;
	private BOMValidateAddline_Dialog dlg;
	private LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>> moduleValidate = null;
	private LinkedHashMap<String, LinkedList<String>> purchaseLevelValidate = null;
	private TCComponentItemRevision newRev = null;
	private LinkedList<TCComponent> itemSearch = null;
	private TCComponentBOMLine selectedObject = null;

	public BOMValidateAddline_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			itemSearch = new LinkedList<TCComponent>();
			// Init data
			updateValidate();
			// Init UI
			dlg = new BOMValidateAddline_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Add new Bomline");
			dlg.setMessage("Define business object create information");

			//
			dlg.txtPartNumber.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event evt) {
					dlg.tblSearch.deselectAll();
					if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
						searchItem();
					}
				}
			});

			dlg.txtRevision.addListener(SWT.KeyUp, new Listener() {
				public void handleEvent(Event evt) {
					dlg.tblSearch.deselectAll();
					if (evt.keyCode == SWT.CR || evt.keyCode == SWT.KEYPAD_CR) {
						searchItem();
					}
				}
			});

			dlg.btnSearch.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					searchItem();
				}
			});

			dlg.tblSearch.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					try {
						int index = dlg.tblSearch.getSelectionIndex();
						if (itemSearch.get(index) instanceof TCComponentItemRevision) {
							newRev = (TCComponentItemRevision) itemSearch.get(index);
							fillDataItem();
						}
					} catch (Exception e2) {

					}
				}
			});

			if (moduleValidate != null)
				dlg.cbBOMModuleGroupEnglish.setItems(moduleValidate.keySet().toArray(new String[0]));
			dlg.cbBOMModuleGroupEnglish.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					updateModuleLevel2();
				}
			});

			dlg.cbBOMMainModuleEnglish.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					updateModuleLevel3();
				}
			});

			dlg.btnSelectParent.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						selectParent();
					} catch (Exception e2) {

					}
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					addToBOMLine();
				}
			});

			selectParent();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void searchItem() {
		String partNumber = dlg.txtPartNumber.getText();
		String revision = dlg.txtRevision.getText();
		TCComponent[] objects = null;
		if (partNumber.isEmpty() && revision.isEmpty()) {
			MessageBox.post("Search criteria is empty.", "Please input the search value and re-try", "Query...",
					MessageBox.ERROR);
			return;
		}
		LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
		if (!partNumber.isEmpty())
			queryInput.put("Item ID", partNumber);
		if (!revision.isEmpty())
			queryInput.put("Revision", revision);

		objects = Query.queryItem(session, queryInput, "Item Revision...");

		if (objects != null) {
			dlg.tblSearch.removeAll();
			DataManagementService dmService = DataManagementService.getService(session);
			dmService.getProperties(objects, new String[] { "object_string", "object_type" });
			for (TCComponent obj : objects) {
				try {
					String[] propValues = obj.getProperties(new String[] { "object_string", "object_type" });
					TableItem row = new TableItem(dlg.tblSearch, SWT.NONE);
					itemSearch.add(obj);
					row.setText(propValues);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			dlg.tblSearch.redraw();
		} else {
			MessageBox.post("No objects found with search criteria..", "Please input valid ID and re-try", "Query...",
					MessageBox.ERROR);
			return;
		}
	}

	private void fillDataItem() throws NotLoadedException, TCException {
		setDefaultDialog();
		dlg.txtPart.setText(newRev.getPropertyDisplayableValue("object_string"));
		String partMakeBuy = newRev.getItem().getPropertyDisplayableValue("vf4_item_make_buy");
		dlg.txtMakeBuy.setText(partMakeBuy);
		if (!partMakeBuy.isEmpty()) {
			LinkedList<String> purchaseLevel = purchaseLevelValidate.get(partMakeBuy);
			dlg.cbBOMPurchaseLevel.setItems(purchaseLevel.toArray(new String[0]));
		}
	}

	private boolean checkWriteAccess(TCComponentBOMLine selectedObject) throws TCException {
		TCAccessControlService aclSrv = session.getTCAccessControlService();
		TCComponent parentBVR = selectedObject.getBOMViewRevision();
		if (parentBVR == null)
			return false;
		boolean check = aclSrv.checkPrivilege(parentBVR, "WRITE");
		return check;
	}

	private void updateValidate() {
		String[] values = TCExtension.GetPreferenceValues("VF_PART_CREATE_MODULE_VALIDATE", session);
		if (values != null && values.length > 0) {
			moduleValidate = new LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>>();
			for (String value : values) {
				String[] str = value.split(";");
				if (str.length > 2) {
					String module1 = str[0];
					String module2 = str[1];
					String module3 = str[2];

					if (moduleValidate.containsKey(module1)) {
						LinkedHashMap<String, LinkedList<String>> oldModule1 = moduleValidate.get(module1);
						if (oldModule1.containsKey(module2)) {
							LinkedList<String> oldModule2 = oldModule1.get(module2);
							oldModule2.add(module3);
						} else {
							LinkedList<String> newModule3 = new LinkedList<String>();
							newModule3.add(module3);
							oldModule1.put(module2, newModule3);
						}
					} else {
						LinkedList<String> newModule3 = new LinkedList<String>();
						newModule3.add(module3);
						LinkedHashMap<String, LinkedList<String>> newModule2 = new LinkedHashMap<String, LinkedList<String>>();
						newModule2.put(module2, newModule3);
						moduleValidate.put(module1, newModule2);
					}
				}
			}
		}

		String[] valuePurchases = TCExtension.GetPreferenceValues("VF_PART_CREATE_PURCHASELEVEL_VALIDATE", session);
		if (valuePurchases != null && valuePurchases.length > 0) {
			if (valuePurchases.length > 0) {
				purchaseLevelValidate = new LinkedHashMap<String, LinkedList<String>>();
				for (String purchase : valuePurchases) {
					if (purchase.contains(";")) {
						String[] strArray = purchase.split(";");
						String partMakeBuy = strArray[0];
						LinkedList<String> purchaseList = new LinkedList<String>();
						if (strArray[1].contains(",")) {
							String[] strArray2 = strArray[1].split(",");
							for (String value : strArray2) {
								purchaseList.add(value);
							}
						} else {
							purchaseList.add(strArray[1]);
						}
						purchaseLevelValidate.put(partMakeBuy, purchaseList);
					}
				}
			}
		}
	}

	private void updateModuleLevel2() {
		String moduleLevel1 = dlg.cbBOMModuleGroupEnglish.getText();
		LinkedHashMap<String, LinkedList<String>> module2 = moduleValidate.get(moduleLevel1);
		if (module2 != null) {
			dlg.cbBOMMainModuleEnglish.setItems(module2.keySet().toArray(new String[0]));
			dlg.cbBOMModuleName.deselectAll();
		}
	}

	private void updateModuleLevel3() {
		String moduleLevel1 = dlg.cbBOMModuleGroupEnglish.getText();
		LinkedHashMap<String, LinkedList<String>> module2 = moduleValidate.get(moduleLevel1);
		if (module2 != null) {
			String moduleLevel2 = dlg.cbBOMMainModuleEnglish.getText();
			LinkedList<String> module3 = module2.get(moduleLevel2);
			if (module3 != null) {
				dlg.cbBOMModuleName.setItems(module3.toArray(new String[0]));
			}
		}
	}

	private Boolean checkRequired() {
		if (dlg.cbBOMPurchaseLevel.getText().isEmpty())
			return false;
		if (dlg.cbBOMMainModuleEnglish.getText().isEmpty())
			return false;
		if (dlg.cbBOMModuleGroupEnglish.getText().isEmpty())
			return false;
		if (dlg.cbBOMModuleName.getText().isEmpty())
			return false;

		return true;
	}

	private void addToBOMLine() {
		if (!checkRequired()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}
		try {
			String purchaseLevel = dlg.cbBOMPurchaseLevel.getText();
			String mainModuleEnglish = dlg.cbBOMMainModuleEnglish.getText();
			String moduleGroupEnglish = dlg.cbBOMModuleGroupEnglish.getText();
			String moduleName = dlg.cbBOMModuleName.getText();
			String quantity = "1";
			int occurence = 1;

			if (StringExtension.isInteger(dlg.txtQuantity.getText(), 10)) {
				quantity = dlg.txtQuantity.getText();
			}

			if (StringExtension.isInteger(dlg.txtOccurence.getText(), 10)) {
				occurence = Integer.parseInt(dlg.txtOccurence.getText());
			}

			for (int i = 0; i < occurence; i++) {
				TCComponentBOMLine child = selectedObject.add(newRev.getItem(), newRev, null, false);
				child.setProperty("bl_quantity", quantity);
				try {
					child.setProperty("VL5_module_group", moduleGroupEnglish);
				} catch (Exception e) {
				}
				try {
					child.setProperty("VL5_main_module", mainModuleEnglish);
				} catch (Exception e) {
				}
				try {
					child.setProperty("VL5_module_name", moduleName);
				} catch (Exception e) {
				}
				try {
					child.setProperty("VL5_purchase_lvl_vf", purchaseLevel);
				} catch (Exception e) {
				}
			}
			dlg.setMessage("Add new Bomline scuccess.", IMessageProvider.INFORMATION);
			setDefaultDialog();
		} catch (TCException e) {
			e.printStackTrace();
			dlg.setMessage(e.toString(), IMessageProvider.ERROR);
		}
	}

	private void selectParent() throws NotLoadedException {
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComp.length > 0) {
			if (targetComp[0] instanceof TCComponentBOMLine) {
				selectedObject = (TCComponentBOMLine) targetComp[0];
				dlg.txtParentBomline.setText(selectedObject.getPropertyDisplayableValue("object_string"));
			}
		}
	}

	private void setDefaultDialog() {
		dlg.txtPart.setText("");
		dlg.txtMakeBuy.setText("");
		dlg.txtOccurence.setText("1");
		dlg.txtQuantity.setText("1");
		dlg.cbBOMModuleGroupEnglish.deselectAll();
		dlg.cbBOMMainModuleEnglish.removeAll();
		dlg.cbBOMModuleName.removeAll();
	}
}
