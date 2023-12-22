package com.teamcenter.vinfast.car.engineering.specdoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.doc.esom.view.UserSearchDialog;
import com.teamcenter.vinfast.doc.esom.view.DVPRItemSearchDialog;
import com.teamcenter.vinfast.doc.esom.view.ECRItemSearchDialog;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ESOMDocumentCreate_Handler extends AbstractHandler {
	private TCSession session;
	private ESOMDocumentCreate_Dialog dlg;
	private TCComponent selectedObject = null;
	private TCComponent espMgl = null;
	private TCComponent vfMgl = null;
	private LinkedHashMap<String, TCComponent> ecrItems = new LinkedHashMap<String, TCComponent>();
	private LinkedHashMap<String, TCComponent> dvprItems = new LinkedHashMap<String, TCComponent>();

	public ESOMDocumentCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			// ---------------------------- Init -------------------------------------
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();

			if (validObjectSelect(targetComp)) {
				MessageBox.post("Please Select One VF Design Part Revision.", "Warning", MessageBox.WARNING);
				return null;
			}

			String[] rootIdList = TCExtension.GetPreferenceValues("VINF_VEHICLE_ID", session);
			boolean isLvl1 = isLvlOneAssy(selectedObject, "VF4_DesignRevision", new ArrayList<String>(Arrays.asList(rootIdList)));
			if (!isLvl1) {
				MessageBox.post("This function only apply for Assembly level one" + "", "Warning", MessageBox.WARNING);
				return null;
			}

			String[] modeCodeDataForm = TCExtension.GetPreferenceValues("VINF_SPECBOOK_MODEL_CODE", session);
			String[] moduleNameDataForm = TCExtension.GetPreferenceValues("VINF_SPECBOOK_MODULE_NAME", session);
			LinkedHashMap<String, String> modelCodeValue = StringExtension.GetComboboxValue(modeCodeDataForm, "AUTOMOBILE");
			LinkedHashMap<String, String> moduleNameValue = StringExtension.GetComboboxValue(moduleNameDataForm, "AUTOMOBILE");
			LinkedHashMap<String, String> saleableLov = TCExtension.GetLovValueAndDisplay("vf3_saleable", "VF3_AT_ESOM_DocRevision", session);

			dlg = new ESOMDocumentCreate_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Define business object create information", IMessageProvider.INFORMATION);

			StringExtension.UpdateValueTextCombobox(dlg.cbModelCode, modelCodeValue);
			StringExtension.UpdateValueTextCombobox(dlg.cbModuleName, moduleNameValue);
			StringExtension.UpdateValueTextCombobox(dlg.cbSaleable, saleableLov);

			dlg.cbModelCode.setEnabled(false);
			dlg.cbModuleName.setEnabled(false);
			dlg.txtName.setEnabled(false);

			setName(selectedObject);
			setModelCode();
			setModule();
			setDesignPart(selectedObject);
			checkComboboxToGenID();

			dlg.btnAddEspMgl.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					UserSearchDialog container = new UserSearchDialog(dlg.getShell(), dlg.getShell().getStyle());
					container.open();
					Button ok = container.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							Table partTable = container.getSearchTable();
							TableItem[] items = partTable.getSelection();
							espMgl = container.getMapTcCompo().get(items[0].getText(0));
							if (espMgl != null) {
								try {
									dlg.txtEspMgl.setText(espMgl.getPropertyDisplayableValue("object_string"));
								} catch (NotLoadedException e1) {
									e1.printStackTrace();
								}
							}
							container.getShell().dispose();
						}
					});
				}
			});

			dlg.btnRemoveEspMgl.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.txtEspMgl.setText("");
					espMgl = null;
				}
			});

			dlg.btnAddVfMgl.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					UserSearchDialog container = new UserSearchDialog(dlg.getShell(), dlg.getShell().getStyle());
					container.open();
					Button ok = container.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							Table partTable = container.getSearchTable();
							TableItem[] items = partTable.getSelection();
							vfMgl = container.getMapTcCompo().get(items[0].getText(0));
							if (vfMgl != null) {
								try {
									dlg.txtVfMgl.setText(vfMgl.getPropertyDisplayableValue("object_string"));
								} catch (NotLoadedException e1) {
									e1.printStackTrace();
								}
							}
							container.getShell().dispose();
						}
					});
				}
			});

			dlg.btnRemoveVfMgl.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					dlg.txtVfMgl.setText("");
					vfMgl = null;
				}
			});

			dlg.btnAddECR.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					ECRItemSearchDialog container = new ECRItemSearchDialog(dlg.getShell(), dlg.getShell().getStyle());
					container.open();
					Button ok = container.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							Table partTable = container.getSearchTable();
							TableItem[] items = partTable.getSelection();
							for (int row = 0; row < items.length; row++) {
								TableItem setItem = new TableItem(dlg.tableECRItem, SWT.NONE);
								for (int col = 0; col < partTable.getColumnCount(); col++) {
									setItem.setText(col, items[row].getText(col));
									if (container.getMapTcCompo().containsKey(items[row].getText(col))) {
										ecrItems.put(items[row].getText(col), container.getMapTcCompo().get(items[row].getText(col)));
									}
								}
							}
							container.getShell().dispose();
						}
					});

				}
			});

			dlg.btnRemoveECR.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					for (TableItem item : dlg.tableECRItem.getSelection()) {
						ecrItems.remove(item.getText(0));
					}
					dlg.tableECRItem.remove(dlg.tableECRItem.getSelectionIndices());

				}
			});

			dlg.btnAddDVPR.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					DVPRItemSearchDialog container = new DVPRItemSearchDialog(dlg.getShell(), dlg.getShell().getStyle());
					container.open();
					Button ok = container.getOKButton();

					ok.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							Table partTable = container.getSearchTable();
							TableItem[] items = partTable.getSelection();
							for (int row = 0; row < items.length; row++) {
								TableItem setItem = new TableItem(dlg.tableDVPRItem, SWT.NONE);
								for (int col = 0; col < partTable.getColumnCount(); col++) {
									setItem.setText(col, items[row].getText(col));
									if (container.getMapTcCompo().containsKey(items[row].getText(col))) {
										dvprItems.put(items[row].getText(col), container.getMapTcCompo().get(items[row].getText(col)));
									}
								}
							}
							container.getShell().dispose();
						}
					});

				}
			});

			dlg.btnRemoveDVPR.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					for (TableItem item : dlg.tableDVPRItem.getSelection()) {
						dvprItems.remove(item.getText(0));
					}
					dlg.tableDVPRItem.remove(dlg.tableDVPRItem.getSelectionIndices());
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!validationCreate()) {
						dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
						return;
					}
					createNewItem();
					;
				}
			});

			dlg.open();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void generateNextID(String vehicleCategory, String prefixName, String modelCode, String moduleName) {
		try {
			String search_Items = prefixName + modelCode + moduleName;

			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", search_Items + "*");
			inputQuery.put("Type", "VF3_AT_ESOM_Doc");

			TCComponent[] item_search = TCExtension.queryItem(session, inputQuery, "Latest Part ID");

			if (item_search == null || item_search.length == 0) {
				if (vehicleCategory.compareTo("AUTOMOBILE") == 0) {
					newIDValue = search_Items + "0001";
				} else {
					newIDValue = search_Items + "001";
				}
			} else {
				int id = 0;
				String split = "";
				if (vehicleCategory.compareTo("AUTOMOBILE") == 0) {
					split = item_search[0].toString().substring(9, 13);
				} else {
					split = item_search[0].toString().substring(10, 13);
				}
				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);

				if (vehicleCategory.compareTo("AUTOMOBILE") == 0) {
					newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 4);
				} else {
					newIDValue = search_Items + StringExtension.ConvertNumberToString(id + 1, 3);
				}
			}

			dlg.txtID.setText(newIDValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Boolean checkIDExist(String newIDValue) {
		LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
		parameter.put("Item ID", newIDValue);
		parameter.put("Type", "VF3_AT_ESOM_Doc");

		TCComponent[] item_list = TCExtension.queryItem(session, parameter, "Item...");
		if (item_list == null || item_list.length == 0) {
			return false;
		}
		return true;
	}

	private int getModule(TCComponent designPart, Combo cbModule) {
		String idPrefix;
		try {
			idPrefix = designPart.getPropertyDisplayableValue("object_string").substring(0, 3);
		} catch (NotLoadedException e) {
			e.printStackTrace();
			return -1;
		}
		for (int i = 0; i < cbModule.getItems().length; i++) {
			if (cbModule.getItem(i).toString().contains(idPrefix)) {
				return i;
			}
		}
		return -1;
	}

	private int getModelCode(TCComponent designPart, Combo cbModel) {
		try {
			String donorVeh = ((TCComponentItemRevision) designPart).getItem().getPropertyDisplayableValue("vf4_donor_vehicle");
			if (donorVeh.contains("/")) {
				donorVeh = donorVeh.split("/")[0];
			}
			if (donorVeh.isEmpty()) {
				return -1;
			}
			for (int i = 0; i < cbModel.getItems().length; i++) {
				if (cbModel.getItem(i).toString().contains(donorVeh)) {
					return i;
				}
			}
		} catch (NotLoadedException | TCException e) {
			e.printStackTrace();
			return -1;
		}
		return -1;
	}

	private void setModelCode() {
		if (getModelCode(selectedObject, dlg.cbModelCode) != -1) {
			dlg.cbModelCode.select(getModelCode(selectedObject, dlg.cbModelCode));
		}
	}

	private void setModule() {
		if (getModule(selectedObject, dlg.cbModuleName) != -1) {
			dlg.cbModuleName.select(getModule(selectedObject, dlg.cbModuleName));
		}
	}

	private void setDesignPart(TCComponent compo) {
		try {
			dlg.txtDesignPart.setText(compo.getPropertyDisplayableValue("object_string"));
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
	}

	private void setName(TCComponent compo) {
		try {
			dlg.txtName.setText(compo.getPropertyDisplayableValue("object_name") + " Engineering Sign Off");
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
	}

	private void checkComboboxToGenID() {
		dlg.txtID.setText("");
		if (!dlg.cbModuleName.getText().isEmpty() && !dlg.cbModelCode.getText().isEmpty()) {

			String modelCode = (String) dlg.cbModelCode.getData(dlg.cbModelCode.getText());
			String moduleName = (String) dlg.cbModuleName.getData(dlg.cbModuleName.getText());

			if (!modelCode.isEmpty() && !moduleName.isEmpty()) {
				generateNextID("AUTOMOBILE", "ESOM", modelCode, moduleName);
			}
		}
	}

	private void createNewItem() {
		try {
			if (checkIDExist(dlg.txtID.getText())) {
				dlg.setMessage("ID exists in Teamcenter", IMessageProvider.WARNING);
			} else {
				DataManagementService dms = DataManagementService.getService(session);
				String objectType = "VF3_AT_ESOM_Doc";
				String id = dlg.txtID.getText();
				String name = dlg.txtName.getText();

				String modelCode = (String) dlg.cbModelCode.getData(dlg.cbModelCode.getText());
				String moduleName = (String) dlg.cbModuleName.getData(dlg.cbModuleName.getText());
				String saleable = dlg.cbSaleable.getText();
				Calendar targetReleaseDate = StringExtension.getDatetimeFromWidget(dlg.datTargetReleaseDate);

				CreateIn itemDef = new CreateIn();
				itemDef.clientId = "1";
				itemDef.data.boName = objectType;
				itemDef.data.stringProps.put("item_id", id);
				itemDef.data.stringProps.put("object_name", name);
				itemDef.data.stringProps.put("vf3_model_code", modelCode);
				itemDef.data.stringProps.put("vf3_module_name", moduleName);

				CreateInput revDef = new CreateInput();
				revDef.boName = objectType + "Revision";
				revDef.stringProps.put("vf3_saleable", saleable);
				revDef.dateProps.put("vf4_target_release_date", targetReleaseDate);

				itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

				CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

				if (response.serviceData.sizeOfPartialErrors() > 0) {
					MessageBox.post("Created unsuccessfully. Exception: " + TCExtension.hanlderServiceData(response.serviceData), "ERROR", MessageBox.ERROR);
					return;
				}

				TCComponent cfgContext = response.output[0].objects[0];
				TCComponent esomRev = cfgContext.getRelatedComponents("revision_list")[0];
				// Add ecr items and dvpr items to esom
				if (ecrItems != null && ecrItems.size() > 0) {
					esomRev.setRelated("VF4_ECR_Items", ecrItems.values().toArray(new TCComponent[ecrItems.values().size()]));
				}
				if (dvprItems != null && dvprItems.size() > 0) {
					esomRev.setRelated("VF4_DVPItems", dvprItems.values().toArray(new TCComponent[dvprItems.values().size()]));
				}
				if (vfMgl != null) {
					esomRev.setReferenceProperty("vf3_vf_mgl", vfMgl);
				}
				if (espMgl != null) {
					esomRev.setReferenceProperty("vf3_esp_mgl", espMgl);
				}
				if (selectedObject != null) {
					esomRev.setReferenceProperty("vf3_design_part", selectedObject);
				}

				Boolean addToFolder = false;
				if (selectedObject != null) {
					String type = selectedObject.getProperty("object_type");
					if (type.compareToIgnoreCase("Folder") == 0) {
						try {
							selectedObject.add("contents", cfgContext);
							addToFolder = true;
							dlg.setMessage("Created successfully, new item has been copied to " + selectedObject.getProperty("object_name") + " folder", IMessageProvider.INFORMATION);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
				}
				if (!addToFolder) {
					try {
						session.getUser().getNewStuffFolder().add("contents", cfgContext);
					} catch (TCException e1) {
						MessageBox.post("Exception: " + e1, "ERROR", MessageBox.ERROR);
					}
					String msg = "Created successfully," + id + "new item has been copied to your Newstuff folder";
					dlg.setMessage(msg, IMessageProvider.INFORMATION);
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private boolean validationCreate() {
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.txtDesignPart.getText().isEmpty())
			return false;
		if (dlg.txtID.getText().isEmpty())
			return false;
		if (dlg.cbModelCode.getText().isEmpty())
			return false;
		if (dlg.cbModuleName.getText().isEmpty())
			return false;
		if (dlg.txtEspMgl.getText().isEmpty())
			return false;
		if (dlg.txtVfMgl.getText().isEmpty())
			return false;
		if (dlg.cbSaleable.getText().isEmpty())
			return false;

		return true;
	}

	private boolean isLvlOneAssy(TCComponent itemRev, String Returnobjectype, ArrayList<String> lstRootItemId) {
		TCComponent[] usedParts = null;
		try {
			usedParts = itemRev.whereUsed(TCComponent.WHERE_USED_ALL);
			for (int i = 0; i < usedParts.length; i++) {
				if (usedParts[i].getTypeComponent().getType().equals(Returnobjectype)) {
					String rootId = usedParts[i].getPropertyDisplayableValue("item_id");
					String revNum = usedParts[i].getPropertyDisplayableValue("item_revision_id");
					if (lstRootItemId.contains(rootId) && revNum.compareToIgnoreCase("01") == 0) {
						return true;
					}
				}
			}
		} catch (TCException | NotLoadedException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;

		if (targetComponents.length > 1)
			return true;

		if (targetComponents[0] instanceof TCComponentBOMLine) {
			try {
				selectedObject = ((TCComponentBOMLine) targetComponents[0]).getItemRevision();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (targetComponents[0] instanceof TCComponentItemRevision) {
			selectedObject = (TCComponent) targetComponents[0];
		}

		if (selectedObject == null)
			return true;

		if (selectedObject.getType().compareTo("VF4_DesignRevision") != 0)
			return true;

		return false;
	}
}