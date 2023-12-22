package com.teamcenter.vines.create;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PostEventObjectProperties;
import com.teamcenter.vinfast.car.engineering.create.SearchDesignDialog;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class CellDesignPartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private CellDesignPartCreate_Dialog dlg;
	private TCComponent selectedObject = null;
	private static String OBJECT_TYPE = "VF4_Cell_Design";
	private static String BASE_NUMBER = "1500";
	private Boolean isAddToBom = false;
	private Boolean isFromBasePart = false;
	private Boolean isFirstCheck = false;
	private LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>> moduleValidate = null;
	private LinkedHashMap<String, LinkedList<String>> purchaseLevelValidate = null;
	private TCComponentItem oldPart = null;

	private LinkedHashMap<String, Map<String, String>> partNumberRuleMapping = null;
	private LinkedHashMap<String, String> partNumberGeneratorMapping = null;

	public CellDesignPartCreate_Handler() {
		super();
	}

	public TCComponentItem getOldPart() {
		return oldPart;
	}

	public void setOldPart(TCComponentItem oldPart) {
		this.oldPart = oldPart;
	}

	public class BasePartInfo {
		public String bPartId;
		public String bGroupName;
		public String bPartName;
		public String bDesc;
		public String bMakeBuy;
		public String bPartCategory;
		public String bDonorVeh;
		public String bPartTracebility;
		public String bUOM;
		public String bLongShortLead;
		public String bSupType;
		public String bManufCompo;
		public String[] bVehicleType;
		public String bIsAfterSaleRelevant;
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			String command = arg0.getCommand().toString();
			isAddToBom = false;
			isFromBasePart = false;
			isFirstCheck = true;
			if (targetComp.length > 1) {
				MessageBox.post("Please select single part", "WARNING", MessageBox.WARNING);
				return null;
			}
			if (command.contains("newCellPartFromBasePart")) {
				isFromBasePart = true;
				if (targetComp[0] instanceof TCComponentBOMLine) {
					TCComponentBOMLine bom = (TCComponentBOMLine) targetComp[0];
					selectedObject = bom.getItemRevision();
					String objectType = selectedObject.getPropertyDisplayableValue("object_type");
					if (objectType.compareToIgnoreCase("VES Cell Design Revision") != 0) {
						MessageBox.post("This function is not applicable for " + objectType, "WARNING", MessageBox.WARNING);
						return null;
					}
				} else {
					selectedObject = (TCComponentItemRevision) targetComp[0];
				}
			} else {
				selectedObject = (TCComponent) targetComp[0];
				if (selectedObject instanceof TCComponentBOMLine) {
					isAddToBom = true;

					updateValidate();
				}
			}

			partNumberRuleMapping = new LinkedHashMap<>();
			partNumberRuleMapping.put("Gen 1/1.5", new HashMap<String, String>() {
				{
					put("Cell Design Part New", "");
					put("Cell Design Part After PR", "A");
				}
			});

			partNumberRuleMapping.put("Gen 1.6", new HashMap<String, String>() {
				{
					put("Cell Design Part New", "");
					put("Cell Design Part After PR", "A");
				}
			});

			partNumberRuleMapping.put("Gen 1.8", new HashMap<String, String>() {
				{
					put("Cell Design Part New", "");
					put("Cell Design Part After PR", "A");
				}
			});

			partNumberRuleMapping.put("Other", new HashMap<String, String>() {
				{
					put("Cell Design Part New", "");
					put("Cell Design Part After PR", "");
				}
			});

			partNumberGeneratorMapping = new LinkedHashMap<>();
			String[] preValues = TCExtension.GetPreferenceValues("VES_CELL_DESIGN_PART_ID_GENERATOR", session);
			if (preValues != null) {
				for (String preValue : preValues) {
					String[] str = preValue.split(";");
					if (str.length >= 2) {
						partNumberGeneratorMapping.put(str[0], str[1]);
					}
				}
			}

			LinkedHashMap<String, String> partTraceabilityDataForm = TCExtension.GetLovValueAndDisplay("vf4_item_is_traceable", OBJECT_TYPE, session);
			String[] partMakeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", OBJECT_TYPE, session);
			String[] partCategoryDataForm = TCExtension.GetLovValues("vf4_part_category", OBJECT_TYPE, session);
			String[] uomDataForm = TCExtension.GetUOMList(session);
			String[] longShortDataForm = TCExtension.GetLovValues("vf4_long_short_lead", OBJECT_TYPE, session);
			String[] supplierTypeDataForm = TCExtension.GetLovValues("vf4_supplier_type", OBJECT_TYPE, session);
			String[] cuvTypeDataForm = TCExtension.GetLovValues("vf4_cuv_veh_type", OBJECT_TYPE + "Revision", session);
			String[] isModifiedPartDataForm = new String[] { "Yes", "No" };
			String[] groupNameDataForm = TCExtension.GetLovValues("VF4_Cell_MatGroup");

			String[] modelDataForm = TCExtension.GetLovValues("VF4_Cell_Model");
			String[] moduleGrpLov = TCExtension.GetLovValues("VL5_module_group", "BOMLine", session);
			// Init UI
			dlg = new CellDesignPartCreate_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Create VINES Cell Design Part");
			dlg.setMessage("Define business object create information");

			dlg.cbPartMakeBuy.setItems(partMakeBuyDataForm);
			dlg.cbPartMakeBuy.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					clearSelectionAndEnable();
					String partMakeBuy = dlg.cbPartMakeBuy.getText();
					if (!partMakeBuy.isEmpty() && isAddToBom) {
						LinkedList<String> purchaseLevel = purchaseLevelValidate.get(partMakeBuy);
						dlg.cbBOMPurchaseLevel.setItems(purchaseLevel.toArray(new String[0]));

						switch (partMakeBuy) {
						case "Make":
							dlg.cbBOMPurchaseLevel.setText("H");
							dlg.cbSupplierType.setText("NONE");
							dlg.cbSupplierType.setEnabled(false);
							break;
						case "Information":
							dlg.cbBOMPurchaseLevel.setText("I");
							dlg.cbBOMPurchaseLevel.setEnabled(false);
							dlg.cbPartCategory.setText("NONE");
							dlg.cbPartCategory.setEnabled(false);
							dlg.cbPartTraceability.setText("No");
							dlg.cbPartTraceability.setEnabled(false);
							dlg.rbtIsAfterSaleFalse.setSelection(true);
							dlg.rbtIsAfterSaleFalse.setEnabled(false);
							dlg.rbtIsAfterSaleTrue.setEnabled(false);
							dlg.cbSupplierType.setText("NONE");
							dlg.cbSupplierType.setEnabled(false);
							dlg.cbUoM.setText("PC");
							dlg.cbUoM.setEnabled(false);
							break;
						case "Sell":
							dlg.cbBOMPurchaseLevel.setText("H");
							dlg.cbBOMPurchaseLevel.setEnabled(false);
							dlg.cbSupplierType.setText("NONE");
							dlg.cbSupplierType.setEnabled(false);
							break;
						}
					}
				}
			});

			dlg.cbPartCategory.setItems(partCategoryDataForm);

			StringExtension.UpdateValueTextCombobox(dlg.cbPartTraceability, partTraceabilityDataForm);
			dlg.cbUoM.setItems(uomDataForm);
			dlg.cbLongShortLead.setItems(longShortDataForm);
			dlg.cbSupplierType.setItems(supplierTypeDataForm);
			dlg.cbVehicleType.setItems(cuvTypeDataForm);

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

			dlg.cbIsModifiedPart.setItems(isModifiedPartDataForm);
			dlg.cbIsModifiedPart.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					if (isFirstCheck)
						return;

					String value = dlg.cbIsModifiedPart.getText();
					if (!value.isEmpty() && value.compareToIgnoreCase("Yes") == 0) {
						SearchDesignDialog searchDlg = new SearchDesignDialog(dlg.getShell(), dlg.getShell().getStyle(), "VF4_BP_Design");
						searchDlg.create();
						searchDlg.getShell().addShellListener(new ShellAdapter() {
							@Override
							public void shellClosed(ShellEvent e) {
								// reset cbIsModifiedPart if Old Part is empty.
								if (dlg.txtOldPartNumber.getText().isEmpty()) {
									dlg.cbIsModifiedPart.deselectAll();
								}
							}
						});

						searchDlg.open();
						Button btnOK = searchDlg.getOKButton();

						btnOK.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								Table partTable = searchDlg.getSearchTable();
								TableItem[] items = partTable.getSelection();
								if (searchDlg.getMapDesignCompo().containsKey(items[0].getText())) {
									dlg.txtOldPartNumber.setText(items[0].getText().split("-")[0]);
									setOldPart((TCComponentItem) searchDlg.getMapDesignCompo().get(items[0].getText()));
									// get info from old part
									try {
										BasePartInfo baseIn = getBasePartInfo(getOldPart().getLatestItemRevision());
										if (baseIn != null) {
											updateInfoFromBasePart(baseIn);
										}
									} catch (Exception e2) {
										e2.toString();
									}
								}
								searchDlg.getShell().dispose();
							}
						});
					} else if (value.compareToIgnoreCase("No") == 0) {
						if (!dlg.txtOldPartNumber.getText().isEmpty()) {
							dlg.txtOldPartNumber.setText("");
						}
					}

					updatePartIDFormat();
				}
			});

			dlg.cbModel.setItems(modelDataForm);
			dlg.cbModel.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					updatePartIDFormat();
				}
			});

			dlg.cbGroupName.setItems(groupNameDataForm);
			dlg.cbGroupName.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					updatePartIDFormat();
				}
			});

			dlg.btnVehicleTypeAdd.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String vehicleType = dlg.cbVehicleType.getText();
					if (!vehicleType.isEmpty()) {
						if (checkExistInList(dlg.lstVehicleType.getItems(), vehicleType))
							dlg.lstVehicleType.add(vehicleType);
					}

					updatePartIDFormat();
				}
			});

			dlg.btnVehicleTypeRemove.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int indexVehicleType = dlg.lstVehicleType.getSelectionIndex();
					if (indexVehicleType >= 0)
						dlg.lstVehicleType.remove(indexVehicleType);

					updatePartIDFormat();
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});

			if (isAddToBom) {
				TCComponentBOMLine selectedBOMLine = (TCComponentBOMLine) selectedObject;
				String moduleGroup = selectedBOMLine.getProperty("VL5_module_group");
				if (!moduleGroup.isEmpty())
					dlg.cbBOMModuleGroupEnglish.setText(moduleGroup);

				String mainModule = selectedBOMLine.getProperty("VL5_main_module");
				if (!mainModule.isEmpty())
					dlg.cbBOMMainModuleEnglish.setText(mainModule);

				String moduleName = selectedBOMLine.getProperty("VL5_module_name");
				if (!moduleName.isEmpty())
					dlg.cbBOMModuleName.setText(moduleName);
			} else {
				dlg.tabFolder.getItems()[1].dispose();
			}

			if (isFromBasePart) {
				BasePartInfo basePartInfo = getBasePartInfo((TCComponentItemRevision) selectedObject);
				if (basePartInfo != null) {
					updateInfoFromBasePart(basePartInfo);
					isFirstCheck = false;
				}
			} else {
				isFirstCheck = false;
			}

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void updateInfoFromBasePart(BasePartInfo baseIn) {
		dlg.cbIsModifiedPart.setText("Yes");
		if (baseIn.bPartId.contains("JF")) {
			dlg.txtID.setText((String) baseIn.bPartId.subSequence(0, 11) + "_JF");
		} else {
			dlg.txtID.setText((String) baseIn.bPartId.subSequence(0, 11));
		}
		dlg.cbGroupName.setText(baseIn.bGroupName);
		dlg.lstVehicleType.removeAll();
		if (baseIn.bVehicleType != null && baseIn.bVehicleType.length > 0)
			dlg.lstVehicleType.setItems(baseIn.bVehicleType);
		dlg.txtName.setText(baseIn.bPartName);
		dlg.txtDescription.setText(baseIn.bDesc);
		dlg.cbPartMakeBuy.setText(baseIn.bMakeBuy);
		dlg.cbPartCategory.setText(baseIn.bPartCategory);
		dlg.cbSupplierType.setText(baseIn.bSupType);
		dlg.cbUoM.setText(baseIn.bUOM);
		dlg.cbLongShortLead.setText(baseIn.bLongShortLead);
		dlg.cbSupplierType.setText(baseIn.bSupType);
		dlg.cbPartTraceability.setText(baseIn.bPartTracebility);
		dlg.txtOldPartNumber.setText(baseIn.bPartId);
		if (baseIn.bIsAfterSaleRelevant.compareToIgnoreCase("True") == 0) {
			dlg.rbtIsAfterSaleTrue.setSelection(true);
		} else {
			dlg.rbtIsAfterSaleFalse.setSelection(true);
		}

		if (baseIn.bManufCompo.compareToIgnoreCase("True") == 0) {
			dlg.rbtManufTrue.setSelection(true);
		} else {
			dlg.rbtManufFalse.setSelection(true);
		}

		updatePartIDFormat();
	}

	private void updatePartIDFormat() {
		dlg.txtID.setText("");
		dlg.txtSuffID.setText("");
		String type = "Cell Design Part";
		String groupName = dlg.cbGroupName.getText();
		String model = dlg.cbModel.getText();

		if (model == null || model.isEmpty())
			return;

		Map<String, String> donorVehicleMapping = null;
		if (partNumberRuleMapping.containsKey(model)) {
			donorVehicleMapping = partNumberRuleMapping.get(model);
		} else {
			donorVehicleMapping = partNumberRuleMapping.get("Other");
		}

		String suffixNewFormat = "";
		if (donorVehicleMapping.containsKey(type + " New"))
			suffixNewFormat = donorVehicleMapping.get(type + " New");

		String suffixAfterPRFormat = "";
		if (donorVehicleMapping.containsKey(type + " After PR"))
			suffixAfterPRFormat = donorVehicleMapping.get(type + " After PR");

		if (dlg.cbIsModifiedPart.getText().compareTo("Yes") == 0) {
			if (!suffixAfterPRFormat.isEmpty()) {
				dlg.txtID.setText(dlg.txtOldPartNumber.getText());
				dlg.txtSuffID.setText(getNewSuffixPartNum(suffixAfterPRFormat, dlg.txtID.getText(), session));
			}
		} else {
			if (!suffixNewFormat.isEmpty())
				dlg.txtSuffID.setText(suffixNewFormat + "A");
		}

		if (dlg.txtID.getText().isEmpty()) {
			if (groupName == null || groupName.isEmpty())
				return;

			dlg.txtID.setText(generateNextID(groupName + BASE_NUMBER + partNumberGeneratorMapping.get(model)));
		}
	}

	public static String getNewSuffixPartNum(String suffix, String currentPartNumber, TCSession session) {
		String id = currentPartNumber.substring(0, 11);
		LinkedHashMap<String, String> queryInput = new LinkedHashMap<String, String>();
		queryInput.put("Item ID", "*" + id + suffix + "*");
		queryInput.put("Type", OBJECT_TYPE);

		TCComponent[] qryOut = Query.queryItem(session, queryInput, "Item...");
		ArrayList<String> lstSuffix = new ArrayList<String>();
		if (qryOut != null) {
			for (TCComponent com : qryOut) {
				try {
					String checkingId = com.getPropertyDisplayableValue("item_id");
					if (!currentPartNumber.contains("JF")) {
						if (checkingId.length() != 13 || checkingId.contains("JF")) {
							continue;
						}
						lstSuffix.add(checkingId.substring(11, 13));
					} else {
						if (checkingId.length() != 16 || !checkingId.contains("JF")) {
							continue;
						}
						lstSuffix.add(checkingId.substring(11, 13));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (lstSuffix.size() >= 1) {
			Collections.sort(lstSuffix);
			Collections.reverse(lstSuffix);
			if (lstSuffix.get(0).substring(1).toUpperCase().compareTo("Z") == 0) {
				int charValue = lstSuffix.get(0).charAt(0);
				String next = String.valueOf((char) (charValue + 1));
				return getNewSuffixPartNum(next, currentPartNumber, session);
			} else {
				String firstChar = lstSuffix.get(0).substring(0, 1);
				int charValue = lstSuffix.get(0).charAt(1);
				String next = String.valueOf((char) (charValue + 1));
				return firstChar + next;
			}
		} else {
			return suffix + "A";
		}
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
		boolean check = true;

		if (dlg.txtID.getText().isEmpty()) {
			if (dlg.cbModel.getText().isEmpty()) {
				warningLabel(dlg.lblModel, true);
				check = false;
			} else {
				warningLabel(dlg.lblModel, false);
			}
			if (dlg.cbGroupName.getText().isEmpty()) {
				warningLabel(dlg.lblGroupName, true);
				check = false;
			} else {
				warningLabel(dlg.lblGroupName, false);
			}
		} else {
			warningLabel(dlg.lblModel, false);
			warningLabel(dlg.lblGroupName, false);
		}
		if (dlg.txtName.getText().isEmpty()) {
			warningLabel(dlg.lblName, true);
			check = false;
		} else {
			warningLabel(dlg.lblName, false);
		}
		if (dlg.cbPartMakeBuy.getText().isEmpty()) {
			warningLabel(dlg.lblPartMakeBuy, true);
			check = false;
		} else {
			warningLabel(dlg.lblPartMakeBuy, false);
		}
		if (dlg.cbPartCategory.getText().isEmpty()) {
			warningLabel(dlg.lblPartCategory, true);
			check = false;
		} else {
			warningLabel(dlg.lblPartCategory, false);
		}
		if (dlg.cbUoM.getText().isEmpty()) {
			warningLabel(dlg.lblUoM, true);
			check = false;
		} else {
			warningLabel(dlg.lblUoM, false);
		}
		if (dlg.cbIsModifiedPart.getText().isEmpty()) {
			warningLabel(dlg.lblIsChangePart, true);
			check = false;
		} else {
			warningLabel(dlg.lblIsChangePart, false);
		}
		// BOM
		if (isAddToBom) {
			if (dlg.cbBOMPurchaseLevel.getText().isEmpty()) {
				warningLabel(dlg.lblPurchaseLevel, true);
				check = false;
			} else {
				warningLabel(dlg.lblPurchaseLevel, false);
			}
			if (dlg.cbBOMMainModuleEnglish.getText().isEmpty()) {
				warningLabel(dlg.lblMainModuleEnglish, true);
				check = false;
			} else {
				warningLabel(dlg.lblMainModuleEnglish, false);
			}
			if (dlg.cbBOMModuleGroupEnglish.getText().isEmpty()) {
				warningLabel(dlg.lblModuleGroupEnglish, true);
				check = false;
			} else {
				warningLabel(dlg.lblModuleGroupEnglish, false);
			}
			if (dlg.cbBOMModuleName.getText().isEmpty()) {
				warningLabel(dlg.lblModuleName, true);
				check = false;
			} else {
				warningLabel(dlg.lblModuleName, false);
			}
		}

		return check;
	}

	private void createNewItem() {
		try {
			if (!checkRequired()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
				return;
			}

			String id = dlg.txtID.getText() + dlg.txtSuffID.getText();
			String name = dlg.txtName.getText();
			String description = dlg.txtDescription.getText();
			String partMakeBuy = dlg.cbPartMakeBuy.getText();
			String partCategory = dlg.cbPartCategory.getText();
			String partTraceability = (String) dlg.cbPartTraceability.getData(dlg.cbPartTraceability.getText());
			boolean isAfterSale = dlg.rbtIsAfterSaleTrue.getSelection();
			String uom = dlg.cbUoM.getText();
			TCComponent UOMTag = TCExtension.GetUOMItem(uom);
			String longShortLead = dlg.cbLongShortLead.getText();
			String supplierType = dlg.cbSupplierType.getText();
			String[] cuvType = dlg.lstVehicleType.getSelection();

			DataManagementService dms = DataManagementService.getService(session);
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = OBJECT_TYPE;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
			itemDef.data.stringProps.put("vf4_part_category", partCategory);
			itemDef.data.stringProps.put("vf4_item_is_traceable", partTraceability);
			itemDef.data.boolProps.put("vf4_itm_after_sale_relevant", isAfterSale);
			itemDef.data.tagProps.put("uom_tag", UOMTag);
			itemDef.data.stringProps.put("vf4_long_short_lead", longShortLead);
			itemDef.data.stringProps.put("vf4_supplier_type", supplierType);
			itemDef.data.boolProps.put("vf4_manu_component", dlg.rbtManufTrue.getSelection());
			itemDef.data.stringProps.put("vf4_orginal_part_number", dlg.txtOldPartNumber.getText());

			CreateInput revDef = new CreateInput();
			revDef.boName = OBJECT_TYPE + "Revision";
			revDef.stringProps.put("object_desc", description);
			revDef.stringArrayProps.put("vf4_cuv_veh_type", cuvType);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponentItemRevision cfgContext = null;
				for (TCComponent rev : response.output[0].objects) {
					if (rev.getType().equals(OBJECT_TYPE + "Revision")) {
						cfgContext = (TCComponentItemRevision) rev;
					}
				}
				if (cfgContext != null) {
					Boolean addToNewstuff = true;
					if (selectedObject != null) {
						if (isAddToBom) {
							addToBOMLine(cfgContext);
							addToNewstuff = false;
							dlg.setMessage("Created successfully, new item has been add to BOM struct.", IMessageProvider.INFORMATION);
						} else {
							String type = selectedObject.getProperty("object_type");
							if (type.compareToIgnoreCase("Folder") == 0) {
								try {
									selectedObject.add("contents", cfgContext.getItem());
									addToNewstuff = false;
									dlg.setMessage("Created successfully, new item has been copied to " + selectedObject.getProperty("object_name") + " folder.", IMessageProvider.INFORMATION);
									openOnCreate(cfgContext);
								} catch (TCException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
					if (addToNewstuff) {
						try {
							session.getUser().getNewStuffFolder().add("contents", cfgContext.getItem());
							dlg.setMessage("Created successfully, new item has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
							openOnCreate(cfgContext);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
					resetDialog();
				} else {
					dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
				}

				if (!dlg.txtOldPartNumber.getText().isEmpty()) {
					PostEventObjectProperties[] postEvtInputs = new PostEventObjectProperties[1];
					postEvtInputs[0] = new PostEventObjectProperties();
					postEvtInputs[0].primaryObject = ((TCComponentItem) response.output[0].objects[0]).getRelatedComponents("revision_list")[0];
					dms.postEvent(postEvtInputs, "Fnd0MultiSite_Unpublish");
				}
			} else {
				ServiceData serviceData = response.serviceData;
				for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
					for (String msg : serviceData.getPartialError(i).getMessages()) {
						MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
					}
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private void addToBOMLine(TCComponentItemRevision newRev) {
		TCComponentBOMLine bomline = (TCComponentBOMLine) selectedObject;
		try {
			String purchaseLevel = dlg.cbBOMPurchaseLevel.getText();
			String mainModuleEnglish = dlg.cbBOMMainModuleEnglish.getText();
			String moduleGroupEnglish = dlg.cbBOMModuleGroupEnglish.getText();
			String moduleName = dlg.cbBOMModuleName.getText();

			TCComponentBOMLine child = bomline.add(newRev.getItem(), newRev, null, false);

			bomline.add(child, true);

			child.setProperty("bl_quantity", "1");
			try {
				child.setProperty("VL5_module_group", moduleGroupEnglish);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				child.setProperty("VL5_main_module", mainModuleEnglish);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				child.setProperty("VL5_module_name", moduleName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				child.setProperty("VL5_purchase_lvl_vf", purchaseLevel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private String generateNextID(String prefix) {
		try {
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", prefix + "*");
			inputQuery.put("Type", OBJECT_TYPE);

			TCComponent[] queryOutput = TCExtension.queryItem(session, inputQuery, "Latest Part ID");

			if (queryOutput == null || queryOutput.length == 0) {
				return prefix;
			}

			int id = 0;
			String split = queryOutput[0].toString().substring(7, 11);

			if (id < Integer.parseInt(split))
				id = Integer.parseInt(split);

			return prefix.substring(0, 7) + StringExtension.ConvertNumberToString(id + 1, 4);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	private void resetDialog() {
		dlg.txtID.setText("");
		dlg.txtSuffID.setText("");
		dlg.txtName.setText("");
		dlg.txtDescription.setText("");
		dlg.cbPartMakeBuy.deselectAll();
		dlg.cbPartCategory.deselectAll();
		dlg.cbPartTraceability.deselectAll();
		dlg.cbUoM.deselectAll();
		dlg.cbLongShortLead.deselectAll();
		dlg.cbSupplierType.deselectAll();
		dlg.lstVehicleType.removeAll();
		// BOM
		dlg.cbBOMPurchaseLevel.deselectAll();
		dlg.cbBOMMainModuleEnglish.deselectAll();
		dlg.cbBOMModuleGroupEnglish.deselectAll();
		dlg.cbBOMModuleName.deselectAll();
	}

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void clearSelectionAndEnable() {
		dlg.cbPartCategory.deselectAll();
		dlg.cbPartTraceability.deselectAll();
		dlg.rbtIsAfterSaleFalse.setSelection(false);
		dlg.rbtIsAfterSaleTrue.setSelection(false);
		dlg.cbSupplierType.deselectAll();
		dlg.cbPartCategory.setEnabled(true);
		dlg.cbPartTraceability.setEnabled(true);
		dlg.rbtIsAfterSaleFalse.setEnabled(true);
		dlg.cbSupplierType.setEnabled(true);
		dlg.rbtIsAfterSaleTrue.setEnabled(true);
		dlg.cbBOMPurchaseLevel.setEnabled(true);
		dlg.cbBOMPurchaseLevel.deselectAll();
		dlg.cbUoM.deselectAll();
		dlg.cbUoM.setEnabled(true);
	}

	public BasePartInfo getBasePartInfo(TCComponentItemRevision basePartCompo) {
		BasePartInfo out = new BasePartInfo();
		try {
			if (basePartCompo != null) {
				out.bPartId = basePartCompo.getPropertyDisplayableValue("item_id");
				String prefix = out.bPartId.substring(0, 3);
				if (Arrays.asList(dlg.cbGroupName.getItems()).contains(prefix))
					out.bGroupName = prefix;
				out.bPartName = basePartCompo.getPropertyDisplayableValue("object_name");
				out.bDesc = basePartCompo.getPropertyDisplayableValue("object_desc");
				out.bMakeBuy = basePartCompo.getItem().getPropertyDisplayableValue("vf4_item_make_buy");
				out.bPartCategory = basePartCompo.getItem().getPropertyDisplayableValue("vf4_part_category");
				out.bDonorVeh = basePartCompo.getItem().getPropertyDisplayableValue("vf4_donor_vehicle");
				out.bPartTracebility = basePartCompo.getItem().getPropertyDisplayableValue("vf4_item_is_traceable");
				out.bUOM = basePartCompo.getItem().getPropertyDisplayableValue("uom_tag");
				out.bLongShortLead = basePartCompo.getItem().getPropertyDisplayableValue("vf4_long_short_lead");
				out.bSupType = basePartCompo.getItem().getPropertyDisplayableValue("vf4_supplier_type");
				out.bManufCompo = basePartCompo.getItem().getPropertyDisplayableValue("vf4_manu_component");
				out.bIsAfterSaleRelevant = basePartCompo.getItem().getPropertyDisplayableValue("vf4_itm_after_sale_relevant");
				out.bVehicleType = basePartCompo.getPropertyDisplayableValue("vf4_cuv_veh_type").split(",");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return out;
	}

	private void warningLabel(Label target, boolean warning) {
		if (warning)
			target.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		else
			target.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
	}

	private boolean checkExistInList(String[] origin, String target) {
		if (origin == null || origin.length == 0)
			return true;

		for (String item : origin) {
			if (item.compareTo(target) == 0)
				return false;
		}
		return true;
	}
}
