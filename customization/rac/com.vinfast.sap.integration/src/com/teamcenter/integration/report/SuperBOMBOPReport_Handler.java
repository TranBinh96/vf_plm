package com.teamcenter.integration.report;

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

import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentSavedVariantRule;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SuperBOMBOPReport_Handler extends AbstractHandler {
	private TCSession session;
	private SuperBOMBOPReport_Dialog dlg;
	private LinkedHashMap<String, LinkedList<ShopModel>> shopItemMapping = null;
	private LinkedHashMap<String, LinkedList<TCComponentSavedVariantRule>> variantMapping = null;
	private ProgressMonitorDialog progressMonitorDialog = null;

	public SuperBOMBOPReport_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();

			getListShop();

			dlg = new SuperBOMBOPReport_Dialog(new Shell());
			dlg.create();

			dlg.cbProgram.setItems(shopItemMapping.keySet().toArray(new String[0]));
			dlg.cbProgram.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					updateShopTable();
					updateVariantTable();
				}
			});

			dlg.ckbCheckAll.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectAll();
				}
			});

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					exportReport();
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
		variantMapping = new LinkedHashMap<String, LinkedList<TCComponentSavedVariantRule>>();
		String[] preValues = TCExtension.GetPreferenceValues("VF_MBOM_BOM/BOP_REPORT", session);
		if (preValues != null) {
			Set<String> idList = new HashSet<String>();
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

				if (value[3].compareTo("100") == 0)
					continue;

				for (String id : shopIDList) {
					ShopModel newItem = new ShopModel();
					newItem.setProgram(value[0]);
					if (value.length == 5) {
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
									updateVariantList(item, tcComponent.getKey());
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

	private void updateVariantList(TCComponent topItem, String program) {
		try {
			TCComponent contextConfig = topItem.getRelatedComponent("Smc0HasVariantConfigContext");
			if (contextConfig != null) {
				TCComponent[] variants = contextConfig.getRelatedComponents("IMAN_reference");
				if (variants != null) {
					LinkedList<TCComponentSavedVariantRule> variantList = new LinkedList<TCComponentSavedVariantRule>();
					for (TCComponent variant : variants) {
						if (variant instanceof TCComponentSavedVariantRule)
							variantList.add((TCComponentSavedVariantRule) variant);
					}
					variantMapping.put(program, variantList);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	private void updateVariantTable() {
		dlg.tblVariant.removeAll();
		String programData = dlg.cbProgram.getText();
		LinkedList<TCComponentSavedVariantRule> variantList = variantMapping.get(programData);

		for (TCComponentSavedVariantRule child : variantList) {
			TableItem item = new TableItem(dlg.tblVariant, SWT.NONE);
			try {
				item.setText(new String[] { "", child.getPropertyDisplayableValue("object_name") });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void exportReport() {
		try {
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
			LinkedList<TCComponentSavedVariantRule> variantList = variantMapping.get(programData);
			LinkedList<TCComponentSavedVariantRule> variantChosse = new LinkedList<TCComponentSavedVariantRule>();
			for (TableItem variant : dlg.tblVariant.getItems()) {
				if (variant.getChecked()) {
					variantChosse.add(variantList.get(i));
				}
				i++;
			}

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
							OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(session, topID.getKey());
							for (OpenContextInfo views : createdBOMViews) {
								if (views.context.getType().equals("BOMLine"))
									topBOMLine = (TCComponentBOMLine) views.context;
								else if (views.context.getType().equals("Mfg0BvrPlantBOP"))
									topBOPLine = (TCComponentBOMLine) views.context;
								else
									UIGetValuesUtility.closeContext(session, (TCComponentBOMLine) views.context);
							}

							String contextID = topID.getKey().getPropertyDisplayableValue("Smc0HasVariantConfigContext");
							String modelAndYear = contextID.split("-").length > 0 ? contextID.split("-")[0] : "";
							String[] modelAndYearWords = modelAndYear.split("_");
							String platformCode = modelAndYearWords.length == 2 ? modelAndYearWords[0] : "";
							String modelYear = modelAndYearWords.length == 2 ? modelAndYearWords[1] : "";

							for (TCComponentSavedVariantRule variantRule : variantChosse) {
								for (ShopModel shopModel : topID.getValue()) {
									TCComponent[] shopBomline = UIGetValuesUtility.searchStruture(session, shopModel.getShopID(), topBOMLine);
									if (shopBomline != null) {
										monitor.subTask("Report processed: " + i++ + "/" + topIDList.size() * topID.getValue().size() * variantChosse.size());
										TCExtension.applyVariantRule(variantRule, topBOMLine.getCachedWindow(), session);
										SuperBOMBOPReport_Thread trigger = new SuperBOMBOPReport_Thread(shopModel, (TCComponentBOMLine) shopBomline[0], topBOMLine, variantRule.getPropertyDisplayableValue("object_name"), platformCode, modelYear, session);
										trigger.run();
									}
								}
							}

							if (topBOMLine != null)
								UIGetValuesUtility.closeContext(session, topBOMLine);
							if (topBOPLine != null)
								UIGetValuesUtility.closeContext(session, topBOPLine);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
