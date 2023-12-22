package com.teamcenter.rac.jes;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.vf.utils.TCExtension;

public class JESMigration_Handler extends AbstractHandler {
	private TCSession session;
	private JESMigration_Dialog dlg;
	private LinkedHashMap<String, LinkedList<ShopModel>> shopItemMapping = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private static String[] GROUP_PERMISSION = { "dba" };

	public JESMigration_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!TCExtension.checkPermission(GROUP_PERMISSION, session)) {
				MessageBox.post("You are not authorized.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			getListShop();
			String[] revRulesDataForm = TCExtension.GetRevisionRules(session);

			dlg = new JESMigration_Dialog(new Shell());
			dlg.create();

			dlg.cbProgram.setItems(shopItemMapping.keySet().toArray(new String[0]));
			dlg.cbProgram.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					updateShopTable();
				}
			});

			dlg.cbRevisionRule.setItems(revRulesDataForm);

			dlg.ckbCheckAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectAll();
				}
			});

			dlg.btnAccept.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					migration();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void selectAll() {
		for (TableItem item : dlg.tblShop.getItems()) {
			item.setChecked(dlg.ckbCheckAll.getSelection());
		}
	}

	private void getListShop() {
		shopItemMapping = new LinkedHashMap<String, LinkedList<ShopModel>>();
		String[] preValues = TCExtension.GetPreferenceValues("VF_MBOM_BOM/BOP_REPORT", session);
		if (preValues != null) {
			Set<String> idList = new HashSet<String>();
			Set<String> bopIDList = new HashSet<String>();
			for (String preValue : preValues) {
				String[] value = preValue.split("==");
				LinkedList<String> shopIDList = new LinkedList<String>();
				if (value[2].contains(";")) {
					for (String id : value[2].split(";")) {
						shopIDList.add(id);
					}
				} else {
					shopIDList.add(value[2]);
				}

				for (String id : shopIDList) {
					ShopModel newItem = new ShopModel();
					newItem.setProgram(value[0]);
					if (value.length == 4) {
						newItem.setShopName(value[1]);
						newItem.setShopID(id);
						newItem.setBom150(value[3].compareTo("150") == 0);
						idList.add(id);
					} else if (value.length == 5) {
						newItem.setShopName(value[1]);
						newItem.setShopID(id);
						newItem.setBom150(value[3].compareTo("150") == 0);
						newItem.setTopID(value[4]);
						idList.add(id);
						idList.add(value[4]);
					} else if (value.length == 6) {
						newItem.setShopName(value[1]);
						newItem.setShopID(id);
						newItem.setBom150(value[3].compareTo("150") == 0);
						newItem.setTopID(value[4]);
						newItem.setShopBopID(value[5]);
						idList.add(id);
						idList.add(value[4]);
						bopIDList.add(value[5]);
					} else if (value.length == 7) {
						newItem.setShopName(value[1]);
						newItem.setShopID(id);
						newItem.setBom150(value[3].compareTo("150") == 0);
						newItem.setTopID(value[4]);
						newItem.setShopBopID(value[5]);
						idList.add(id);
						idList.add(value[4]);
					}

					if (shopItemMapping.containsKey(value[0])) {
						shopItemMapping.get(value[0]).add(newItem);
					} else {
						LinkedList<ShopModel> shopList = new LinkedList<ShopModel>();
						shopList.add(newItem);
						shopItemMapping.put(value[0], shopList);
					}
				}
			}

			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", String.join(";", idList));
			inputQuery.put("Type", "VF Item;Item;Structure Part");

			TCComponent[] item_search = TCExtension.queryItem(session, inputQuery, "Item...");
			if (item_search != null && item_search.length > 0) {
				for (TCComponent item : item_search) {
					try {
						for (Map.Entry<String, LinkedList<ShopModel>> tcComponent : shopItemMapping.entrySet()) {
							for (ShopModel shopItem : tcComponent.getValue()) {
								if (shopItem.getShopID().compareTo(item.getPropertyDisplayableValue("item_id")) == 0) {
									shopItem.setShopObject(item);
								}
								if (shopItem.getTopID().compareTo(item.getPropertyDisplayableValue("item_id")) == 0) {
									shopItem.setTopObject(item);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", String.join(";", bopIDList));
			inputQuery.put("Type", "Process Area");

			item_search = TCExtension.queryItem(session, inputQuery, "Item...");
			if (item_search != null && item_search.length > 0) {
				for (TCComponent item : item_search) {
					try {
						for (Map.Entry<String, LinkedList<ShopModel>> tcComponent : shopItemMapping.entrySet()) {
							for (ShopModel shopItem : tcComponent.getValue()) {
								if (shopItem.getShopBopID().compareTo(item.getPropertyDisplayableValue("item_id")) == 0) {
									shopItem.setShopBopObject(item);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void updateShopTable() {
		dlg.tblShop.removeAll();
		String programData = dlg.cbProgram.getText();
		LinkedList<ShopModel> shopList = shopItemMapping.get(programData);

		for (ShopModel child : shopList) {
			TableItem item = new TableItem(dlg.tblShop, SWT.NONE);
			try {
				item.setText(new String[] { "", child.getShopName(), child.getShopID(), child.getShopDesc() });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void migration() {
		try {
			String revisionRule = dlg.cbRevisionRule.getText();
			String programData = dlg.cbProgram.getText();
			LinkedList<ShopModel> shopList = shopItemMapping.get(programData);

			int i = 0;
			LinkedHashMap<TCComponent, LinkedList<ShopModel>> topIDList = new LinkedHashMap<TCComponent, LinkedList<ShopModel>>();
			for (TableItem tableItem : dlg.tblShop.getItems()) {
				if (tableItem.getChecked()) {
					ShopModel shopModel = shopList.get(i);
					if (topIDList.containsKey(shopModel.getTopObject())) {
						topIDList.get(shopModel.getTopObject()).add(shopModel);
					} else {
						LinkedList<ShopModel> shopModelList = new LinkedList<ShopModel>();
						shopModelList.add(shopModel);
						topIDList.put(shopModel.getTopObject(), shopModelList);
					}
				}
				i++;
			}

			i = 0;

			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Export Report...", IProgressMonitor.UNKNOWN);
					try {
						int i = 0;
						for (Map.Entry<TCComponent, LinkedList<ShopModel>> topID : topIDList.entrySet()) {
							TCComponentBOMLine topBOMLine = null;
							TCComponentBOMLine topBOPLine = null;
							OpenContextInfo[] createdBOMViews = TCExtension.createContextViews(session, topID.getKey(), revisionRule);
							for (OpenContextInfo views : createdBOMViews) {
								if (views.context.getType().equals("BOMLine"))
									topBOMLine = (TCComponentBOMLine) views.context;
								else if (views.context.getType().equals("Mfg0BvrPlantBOP"))
									topBOPLine = (TCComponentBOMLine) views.context;
								else
									TCExtension.closeContext(session, (TCComponentBOMLine) views.context);
							}

							for (ShopModel shopModel : topID.getValue()) {
								TCComponent[] shopBopline = TCExtension.searchStruture(session, shopModel.getShopBopID(), topBOPLine);
								if (shopBopline != null) {
									monitor.subTask("Report processed: " + i++ + "/" + topIDList.size() * topID.getValue().size());
									JESMigration_Thread trigger = new JESMigration_Thread(shopModel.getShopName(), (TCComponentBOMLine) shopBopline[0], session);
									trigger.run();
								}
							}

							if (topBOMLine != null)
								TCExtension.closeContext(session, topBOMLine);
							if (topBOPLine != null)
								TCExtension.closeContext(session, topBOPLine);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
			MessageBox.post("The report is exported successfully to folder C:\\temp.", "Information", MessageBox.INFORMATION);
		} catch (

		Exception e) {
			e.printStackTrace();
		}
	}
}
