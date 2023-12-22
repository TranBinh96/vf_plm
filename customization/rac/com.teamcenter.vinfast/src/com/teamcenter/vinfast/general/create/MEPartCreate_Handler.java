package com.teamcenter.vinfast.general.create;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.workflow.WorkflowService;
import com.teamcenter.services.rac.workflow._2014_06.Workflow;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;
import com.vf.utils.TriggerProcess;

import vfplm.soa.common.define.Constant;

public class MEPartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private MEPartCreate_Dialog dlg;

	private TCComponent selectedObject = null;
	private TCComponentItemRevision parentPart = null;
	private String parentType = "";
	private boolean isCarPart = true;
	private boolean isCOILPart = false;
	private boolean isBLNPart = false;
	private LinkedHashMap<String, String> modelAndPrefixNumber;
	private LinkedHashMap<String, String> modelAndDisplayName;
	private LinkedHashMap<String, Boolean> isAfterSaleSetting;
	private LinkedHashMap<String, String> partTraceabilitySetting;

	private static String BLANK_TAB = "Blank";
	private static String COIL_TAB = "Coil";
	private static String SCOOTER_TAB = "Scooter";
	private static String PREFERENCE_MATERIAL_LIST = "VF_MEPART_COIL_MATERIALS";
	private static String PREFERENCE_COATING_LIST = "VF_MEPART_COIL_COATINGS";
	private static String WORKPROCESS_TEMPLACE = "RawMaterial_SOR_Release_Process";

	private Listener coilGenNameListener = null;
	private Listener blankGenNameListener = null;

	public MEPartCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			parentPart = null;

			if (targetComp[0] instanceof TCComponentBOMLine) {
				parentPart = ((TCComponentBOMLine) targetComp[0]).getItemRevision();
			} else if (targetComp[0] instanceof TCComponentItemRevision) {
				parentPart = (TCComponentItemRevision) targetComp[0];
			}
			// Load data form
			modelAndPrefixNumber = new LinkedHashMap<String, String>();
			modelAndDisplayName = new LinkedHashMap<String, String>();
			isAfterSaleSetting = new LinkedHashMap<String, Boolean>();
			partTraceabilitySetting = new LinkedHashMap<String, String>();
			getModelAndPrefixNumber();
			String[] makeBuyDataForm = TCExtension.GetLovValues("vf4_item_make_buy", "Item", session);
			String[] uomDataForm = TCExtension.GetUOMList(session);
			LinkedHashMap<String, String> partTraceabilityDataForm = TCExtension.GetLovValueAndDisplay("vf4_item_is_traceable", "VF3_manuf_part", session);
			String[] partCategoryDataForm = TCExtension.GetLovValues("vf4_part_category", "VF3_me_scooter", session);
			// for car
			String[] supplierTypeDataForm = TCExtension.GetLovValues("vf4_supplier_type", "VF3_manuf_part", session);
			String[] subCategoryPre = TCExtension.GetPreferenceValues("VF_MBOM_ME_TYPES", session);
			LinkedHashMap<String, String> categoryTypeCar = new LinkedHashMap<String, String>();
			for (String category : subCategoryPre) {
				if (category.contains("=")) {
					String[] str = category.split("=");
					categoryTypeCar.put(str[0], str[0] + " - " + str[1]);
					if (str.length > 3) {
						if (str[2].compareTo("1") == 0) {
							isAfterSaleSetting.put(str[0], true);
						} else {
							isAfterSaleSetting.put(str[0], false);
						}

						if (str[3].compareTo("1") == 0) {
							partTraceabilitySetting.put(str[0], "Yes");
						} else {
							partTraceabilitySetting.put(str[0], "No");
						}
					} else if (str.length > 2) {
						if (str[2].compareTo("1") == 0) {
							isAfterSaleSetting.put(str[0], true);
						} else {
							isAfterSaleSetting.put(str[0], false);
						}

						partTraceabilitySetting.put(str[0], "");
					} else {
						isAfterSaleSetting.put(str[0], null);
						partTraceabilitySetting.put(str[0], "");
					}
				}
			}
			// for scooter
			LinkedHashMap<String, String> vehicleLineDataForm = TCExtension.GetLovValueAndDisplay("vf4_es_model_veh_line", "VF3_me_scooter", session);
			// for coil
			String[] materialDataForm = TCExtension.GetPreferenceValues(PREFERENCE_MATERIAL_LIST, session);
			String[] coatingPreference = TCExtension.GetPreferenceValues(PREFERENCE_COATING_LIST, session);
			List<String> coatingDataForm = new LinkedList<String>();
			coatingDataForm.add("");
			for (String value : coatingPreference) {
				coatingDataForm.add(value);
			}

			// Init UI
			dlg = new MEPartCreate_Dialog(new Shell());
			dlg.create();

			StringExtension.UpdateValueTextCombobox(dlg.cbModel, modelAndDisplayName);
			dlg.cbModel.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateUI();
					onEventChange_Combobox();
				}
			});

			StringExtension.UpdateValueTextCombobox(dlg.cbCategory, categoryTypeCar);
			dlg.cbCategory.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateUI();
					onEventChange_Combobox();
				}
			});

			dlg.cbPartMakeBuy.setItems(makeBuyDataForm);
			dlg.cbPartMakeBuy.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					updateUI();
				}
			});

			dlg.cbUOM.setItems(uomDataForm);

			StringExtension.UpdateValueTextCombobox(dlg.cbPartTraceability, partTraceabilityDataForm);

			StringExtension.UpdateValueTextCombobox(dlg.cbVehicleLine, vehicleLineDataForm);

			dlg.cbSupplierType.setItems(supplierTypeDataForm);

			dlg.cbPartCategory.setItems(partCategoryDataForm);
			dlg.cbPartCategory.setText("NONE");

			coilGenNameListener = new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					generateCOILName();
				}
			};
			dlg.cbCoilMaterial.setItems(materialDataForm);
			dlg.cbCoilMaterial.addListener(SWT.Modify, coilGenNameListener);
			dlg.cbCoilCoating.setItems(coatingDataForm.toArray(new String[0]));
			dlg.cbCoilCoating.addListener(SWT.Modify, coilGenNameListener);
			dlg.txtCoilThickness.addListener(SWT.Modify, coilGenNameListener);
			dlg.txtCoilWidth.addListener(SWT.Modify, coilGenNameListener);

			blankGenNameListener = new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					generateBLANKName();
				}
			};
			dlg.cbBlankMaterial.setItems(materialDataForm);
			dlg.cbBlankMaterial.addListener(SWT.Modify, blankGenNameListener);
			dlg.cbBlankCoating.setItems(coatingDataForm.toArray(new String[0]));
			dlg.cbBlankCoating.addListener(SWT.Modify, blankGenNameListener);
			dlg.txtBlankThickness.addListener(SWT.Modify, blankGenNameListener);
			dlg.txtBlankWidth.addListener(SWT.Modify, blankGenNameListener);
			dlg.txtBlankLength.addListener(SWT.Modify, blankGenNameListener);

			dlg.btnAutoFill.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					if (isCOILPart)
						coilValidate();
					else if (isBLNPart) {
						blankValidate();
					}
				}
			});

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					createNewItem();
				}
			});
			updateUI();
			fillRawMaterialAttribute();
			dlg.open();
		} catch (TCException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void fillRawMaterialAttribute() {
		if (parentPart != null) {
			try {
				parentType = parentPart.getPropertyDisplayableValue("object_type");
				String objectItem = parentPart.getPropertyDisplayableValue("object_string");
				dlg.txtBlackLinkPart.setText(objectItem);
				dlg.txtCoilLinkPart.setText(objectItem);
				if (parentType.compareTo("VF Design Revision") == 0) {
					String material = parentPart.getPropertyDisplayableValue("vf4_catia_material");
					String coating = parentPart.getPropertyDisplayableValue("vf4_cad_coating");
					String thickness = parentPart.getPropertyDisplayableValue("vf4_cad_thickness");

					dlg.cbCoilMaterial.setText(material);
					dlg.cbBlankMaterial.setText(material);
					dlg.cbCoilCoating.setText(coating);
					dlg.cbBlankCoating.setText(coating);
					if (StringExtension.isDouble(thickness)) {
						int thicknessConvert = (int) (Double.parseDouble(thickness) * 100);
						dlg.txtCoilThickness.setSelection(thicknessConvert);
						dlg.txtBlankThickness.setSelection(thicknessConvert);
					}
				} else if (parentType.compareTo("ME Part Revision") == 0) {
					String material = parentPart.getItem().getPropertyDisplayableValue("vf4_COI_material");
					String coating = parentPart.getItem().getPropertyDisplayableValue("vf4_coating");
					String thickness = parentPart.getItem().getPropertyDisplayableValue("vf4_thickness");
					String width = parentPart.getItem().getPropertyDisplayableValue("vf4_width");
					String length = parentPart.getItem().getPropertyDisplayableValue("vf4_length");

					dlg.cbCoilMaterial.setText(material);
					dlg.cbBlankMaterial.setText(material);
					dlg.cbCoilCoating.setText(coating);
					dlg.cbBlankCoating.setText(coating);
					if (StringExtension.isDouble(thickness)) {
						dlg.txtCoilThickness.setSelection((int) Double.parseDouble(thickness));
						dlg.txtBlankThickness.setSelection((int) Double.parseDouble(thickness));
					}
					if (StringExtension.isDouble(width)) {
						dlg.txtCoilWidth.setSelection((int) Double.parseDouble(width));
						dlg.txtBlankWidth.setSelection((int) Double.parseDouble(width));
					}
					if (StringExtension.isDouble(length)) {
						dlg.txtBlankLength.setSelection((int) Double.parseDouble(length));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void updateUI() {
		String category = (String) dlg.cbCategory.getData(dlg.cbCategory.getText());
		Boolean isAfterSale = isAfterSaleSetting.get(category);
		String makeBuy = dlg.cbPartMakeBuy.getText();
		if (makeBuy.compareTo("Information") == 0) {
			updateSupplierType(true);
			dlg.cbSupplierType.setEnabled(false);
			dlg.cbSupplierType.setText("NONE");

			dlg.rbtIsAfterSaleFalse.setSelection(true);
			dlg.rbtIsAfterSaleTrue.setSelection(false);

			dlg.cbPartTraceability.setEnabled(false);
			dlg.cbPartTraceability.setText("No");
		} else {
			if (makeBuy.compareTo("Make") == 0) {
				updateSupplierType(true);
				dlg.cbSupplierType.setEnabled(false);
				dlg.cbSupplierType.setText("NONE");
			} else {
				updateSupplierType(false);
				dlg.cbSupplierType.setEnabled(true);
				dlg.cbSupplierType.setText("");
			}

			if (isAfterSale == null) {
				dlg.rbtIsAfterSaleFalse.setSelection(false);
				dlg.rbtIsAfterSaleTrue.setSelection(false);
			} else {
				dlg.rbtIsAfterSaleFalse.setSelection(!isAfterSale);
				dlg.rbtIsAfterSaleTrue.setSelection(isAfterSale);
			}

			String partTrace = partTraceabilitySetting.get(category);
			if (partTrace == null || partTrace.isEmpty()) {
				dlg.cbPartTraceability.setEnabled(true);
			} else {
				dlg.cbPartTraceability.setEnabled(false);
				dlg.cbPartTraceability.setText(partTrace);
			}
		}

		String model = dlg.cbModel.getText();
		isCarPart = !(model.compareToIgnoreCase("Scooter") == 0);
		dlg.lblSupplierType.setVisible(isCarPart);
		dlg.cbSupplierType.setVisible(isCarPart);
		dlg.tabFolder.setVisible(false);
		isCOILPart = false;
		isBLNPart = false;
		if (isCarPart) {
			if (!dlg.cbCategory.getText().isEmpty()) {
				String subCategory = (String) dlg.cbCategory.getData(dlg.cbCategory.getText());
				if (subCategory.compareToIgnoreCase("COI") == 0) {
					dlg.tabFolder.setVisible(true);
					updateTab(COIL_TAB);
					isCOILPart = true;
				} else if (subCategory.compareToIgnoreCase("BLN") == 0) {
					dlg.tabFolder.setVisible(true);
					updateTab(BLANK_TAB);
					isBLNPart = true;
				}
			}
		} else {
			dlg.tabFolder.setVisible(true);
			updateTab(SCOOTER_TAB);
		}
		dlg.btnAutoFill.setEnabled(isCOILPart || isBLNPart);
	}

	private void updateTab(String tabItemName) {
		for (CTabItem tabItem : dlg.tabFolder.getItems()) {
			if (tabItem.getText().compareToIgnoreCase(tabItemName) != 0) {
				if (!tabItem.isDisposed())
					tabItem.dispose();
			}
		}
		if (tabItemName.compareToIgnoreCase(SCOOTER_TAB) == 0) {
			if (dlg.tbtmScooter.isDisposed()) {
				dlg.tbtmScooter = new CTabItem(dlg.tabFolder, SWT.NONE);
				dlg.tbtmScooter.setControl(dlg.comScooter);
				dlg.tbtmScooter.setText(SCOOTER_TAB);
			}
		} else if (tabItemName.compareToIgnoreCase(COIL_TAB) == 0) {
			if (dlg.tbtmCoil.isDisposed()) {
				dlg.tbtmCoil = new CTabItem(dlg.tabFolder, SWT.NONE);
				dlg.tbtmCoil.setControl(dlg.comCoil);
				dlg.tbtmCoil.setText(COIL_TAB);
			}
		} else if (tabItemName.compareToIgnoreCase(BLANK_TAB) == 0) {
			if (dlg.tbtmBlank.isDisposed()) {
				dlg.tbtmBlank = new CTabItem(dlg.tabFolder, SWT.NONE);
				dlg.tbtmBlank.setControl(dlg.comBlank);
				dlg.tbtmBlank.setText(BLANK_TAB);
			}
		}
		try {
			dlg.tabFolder.setSelection(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void onEventChange_Combobox() {
		if (!dlg.cbModel.getText().isEmpty() && !dlg.cbCategory.getText().isEmpty()) {
			String inputString = "";
			String type = "";

			if (!isCarPart) {
				type = "ME Scooter";
			} else {
				type = "ME Part";
			}
			inputString = (String) dlg.cbCategory.getData(dlg.cbCategory.getText());
			inputString += modelAndPrefixNumber.get(dlg.cbModel.getData(dlg.cbModel.getText()));
			dlg.txtID.setText(generateNextID(inputString, type));
		} else {
			dlg.txtID.setText("");
		}
	}

	private void coilValidate() {
		boolean isFormatValid = true;
		String name = dlg.txtName.getText();
		if (name.contains("_")) {
			String[] str = name.split("_");
			if (str[0].compareToIgnoreCase("COIL") != 0) {
				dlg.lblNameWarning.setText("Prefix name only available for COIL.");
			}
			if (str.length == 5) {
				// material
				if (checkExistInLOV(str[1], dlg.cbCoilMaterial.getItems())) {
					dlg.cbCoilMaterial.setText(str[1]);
				} else {
					dlg.cbCoilMaterial.deselectAll();
					isFormatValid = false;
				}
				// coating
				if (checkExistInLOV(str[2], dlg.cbCoilCoating.getItems())) {
					dlg.cbCoilCoating.setText(str[2]);
				} else {
					dlg.cbCoilCoating.deselectAll();
					isFormatValid = false;
				}
				// thickness
				if (StringExtension.isInteger(str[3], 10)) {
					dlg.txtCoilThickness.setSelection(Integer.parseInt(str[3]));
				} else {
					dlg.txtCoilThickness.setSelection(0);
				}
				// width
				if (StringExtension.isInteger(str[4], 10)) {
					dlg.txtCoilWidth.setSelection(Integer.parseInt(str[4]));
				} else {
					dlg.txtCoilWidth.setSelection(0);
				}
			} else if (str.length == 4) {
				// material
				if (checkExistInLOV(str[1], dlg.cbCoilMaterial.getItems())) {
					dlg.cbCoilMaterial.setText(str[1]);
				} else {
					dlg.cbCoilMaterial.deselectAll();
					isFormatValid = false;
				}
				// thickness
				if (StringExtension.isInteger(str[2], 10)) {
					dlg.txtCoilThickness.setSelection(Integer.parseInt(str[2]));
				} else {
					dlg.txtCoilThickness.setSelection(0);
				}
				// width
				if (StringExtension.isInteger(str[3], 10)) {
					dlg.txtCoilWidth.setSelection(Integer.parseInt(str[3]));
				} else {
					dlg.txtCoilWidth.setSelection(0);
				}
				dlg.cbCoilCoating.deselectAll();
			} else {
				isFormatValid = false;
			}
			dlg.txtName.setText(name);
		} else {
			isFormatValid = false;
		}
		if (isFormatValid)
			dlg.lblNameWarning.setText("");
		else {
			dlg.lblNameWarning.setText("Format name not correct.");
		}
	}

	private void blankValidate() {
		boolean isFormatValid = true;
		String name = dlg.txtName.getText();
		if (name.contains("_")) {
			String[] str = name.split("_");
			if (str[0].compareToIgnoreCase("BLANK") != 0) {
				dlg.lblNameWarning.setText("Prefix name only available for BLANK.");
			}
			if (str.length == 6) {
				// material
				if (checkExistInLOV(str[1], dlg.cbBlankMaterial.getItems())) {
					dlg.cbBlankMaterial.setText(str[1]);
				} else {
					dlg.cbBlankMaterial.deselectAll();
					isFormatValid = false;
				}
				// coating
				if (checkExistInLOV(str[2], dlg.cbBlankCoating.getItems())) {
					dlg.cbBlankCoating.setText(str[2]);
				} else {
					dlg.cbBlankCoating.deselectAll();
					isFormatValid = false;
				}
				// thickness
				if (StringExtension.isInteger(str[3], 10)) {
					dlg.txtBlankThickness.setSelection(Integer.parseInt(str[3]));
				} else {
					dlg.txtBlankThickness.setSelection(0);
				}
				// width
				if (StringExtension.isInteger(str[4], 10)) {
					dlg.txtBlankWidth.setSelection(Integer.parseInt(str[4]));
				} else {
					dlg.txtBlankWidth.setSelection(0);
				}
				// length
				if (StringExtension.isInteger(str[5], 10)) {
					dlg.txtBlankLength.setSelection(Integer.parseInt(str[5]));
				} else {
					dlg.txtBlankLength.setSelection(0);
				}
			} else if (str.length == 5) {
				// material
				if (checkExistInLOV(str[1], dlg.cbBlankMaterial.getItems())) {
					dlg.cbBlankMaterial.setText(str[1]);
				} else {
					dlg.cbBlankMaterial.deselectAll();
					isFormatValid = false;
				}
				// thickness
				if (StringExtension.isInteger(str[2], 10)) {
					dlg.txtBlankThickness.setSelection(Integer.parseInt(str[2]));
				} else {
					dlg.txtBlankThickness.setSelection(0);
				}
				// width
				if (StringExtension.isInteger(str[3], 10)) {
					dlg.txtBlankWidth.setSelection(Integer.parseInt(str[3]));
				} else {
					dlg.txtBlankWidth.setSelection(0);
				}
				// length
				if (StringExtension.isInteger(str[4], 10)) {
					dlg.txtBlankLength.setSelection(Integer.parseInt(str[4]));
				} else {
					dlg.txtBlankLength.setSelection(0);
				}
				dlg.cbBlankCoating.deselectAll();
			} else {
				isFormatValid = false;
			}
			dlg.txtName.setText(name);
		} else {
			isFormatValid = false;
		}
		if (isFormatValid)
			dlg.lblNameWarning.setText("");
		else {
			dlg.lblNameWarning.setText("Format name not correct.");
		}
	}

	private void resetDialog() {
//		dlg.cbModel.deselectAll();
		dlg.cbCategory.deselectAll();
		dlg.txtID.setText("");
		dlg.txtName.setText("");
		dlg.txtDesc.setText("");
//		dlg.cbPartMakeBuy.deselectAll();
//		dlg.cbUOM.deselectAll();
		dlg.cbPartTraceability.deselectAll();
		// scooter
		dlg.cbVehicleLine.deselectAll();
		dlg.txtPartNameVietnamese.setText("");
		dlg.txtPartReference.setText("");
		// coil
		dlg.cbCoilMaterial.deselectAll();
		dlg.cbCoilCoating.deselectAll();
		dlg.txtCoilThickness.setSelection(0);
		dlg.txtCoilWidth.setSelection(0);
		dlg.ckbIsCoilPurchase.setSelection(false);
		// blank
		dlg.cbBlankMaterial.deselectAll();
		dlg.cbBlankCoating.deselectAll();
		dlg.txtBlankThickness.setSelection(0);
		dlg.txtBlankWidth.setSelection(0);
		dlg.txtBlankLength.setSelection(0);
		dlg.ckbIsBlankPurchase.setSelection(false);
	}

	private Boolean checkRequired() {
		if (dlg.cbModel.getText().isEmpty())
			return false;
		if (dlg.cbCategory.getText().isEmpty())
			return false;
		if (dlg.txtID.getText().isEmpty())
			return false;
		if (dlg.txtName.getText().isEmpty())
			return false;
		if (dlg.txtDesc.getText().isEmpty())
			return false;
		if (dlg.cbPartMakeBuy.getText().isEmpty())
			return false;
		if (dlg.cbUOM.getText().isEmpty())
			return false;
		if (dlg.cbPartCategory.getText().isEmpty())
			return false;
		if (!isCarPart) {
			if (dlg.cbVehicleLine.getText().isEmpty())
				return false;
		} else {
			if (isCOILPart) {
				String name = dlg.txtName.getText();
				coilValidate();
				dlg.txtName.setText(name);
				dlg.lblNameWarning.setText("");
				if (dlg.cbCoilMaterial.getText().isEmpty())
					return false;
				if (dlg.txtCoilThickness.getText().isEmpty() || dlg.txtCoilThickness.getText().compareToIgnoreCase("0.00") == 0)
					return false;
				if (dlg.txtCoilWidth.getText().isEmpty() || dlg.txtCoilWidth.getText().compareToIgnoreCase("0") == 0)
					return false;
			} else if (isBLNPart) {
				String name = dlg.txtName.getText();
				blankValidate();
				dlg.txtName.setText(name);
				dlg.lblNameWarning.setText("");
				if (dlg.cbBlankMaterial.getText().isEmpty())
					return false;
				if (dlg.txtBlankThickness.getText().isEmpty() || dlg.txtBlankThickness.getText().compareToIgnoreCase("0.00") == 0)
					return false;
				if (dlg.txtBlankWidth.getText().isEmpty() || dlg.txtBlankWidth.getText().compareToIgnoreCase("0") == 0)
					return false;
				if (dlg.txtBlankLength.getText().isEmpty() || dlg.txtBlankLength.getText().compareToIgnoreCase("0") == 0)
					return false;
			}
		}

		return true;
	}

	private String generateNextID(String inputString, String type) {
		try {
			String newIDValue = "";
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", inputString + "*");
			inputQuery.put("Type", type);
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "Latest Part ID");
			int remainNo = 11 - inputString.length();
			if (item_search == null || item_search.length == 0) {
				newIDValue = inputString + StringExtension.ConvertNumberToString(1, remainNo);
			} else {
				int id = 0;

				String split = item_search[0].toString().substring(inputString.length(), 11);
				if (id < Integer.parseInt(split))
					id = Integer.parseInt(split);

				newIDValue = inputString + StringExtension.ConvertNumberToString(id + 1, remainNo);
			}
			return newIDValue;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private void generateCOILName() {
		dlg.lblNameWarning.setText("");
		if (!isCOILPart)
			return;
		String subCategory = (String) dlg.cbCategory.getData(dlg.cbCategory.getText());
		if (subCategory.compareTo("COI") == 0)
			subCategory = "COIL";
		String material = dlg.cbCoilMaterial.getText();
		String coating = dlg.cbCoilCoating.getText();
		String width = dlg.txtCoilWidth.getText();
		String thickness = dlg.txtCoilThickness.getText();

		int thicknessValue = 0;
		if (StringExtension.isDouble(thickness)) {
			Double thicknessValueDouble = (Double.parseDouble(thickness) * 100);
			thicknessValue = thicknessValueDouble.intValue();
		}
		if (String.valueOf(thicknessValue).length() < 3)
			thickness = "0" + String.valueOf(thicknessValue);
		else
			thickness = String.valueOf(thicknessValue);

		dlg.txtName.setText(subCategory + "_" + material + (coating.isEmpty() ? "" : "_" + coating) + "_" + thickness + "_" + width);
	}

	private void generateBLANKName() {
		dlg.lblNameWarning.setText("");
		if (!isBLNPart)
			return;
		String subCategory = (String) dlg.cbCategory.getData(dlg.cbCategory.getText());
		if (subCategory.compareTo("BLN") == 0)
			subCategory = "BLANK";
		String material = dlg.cbBlankMaterial.getText();
		String coating = dlg.cbBlankCoating.getText();
		String thickness = dlg.txtBlankThickness.getText();
		String width = dlg.txtBlankWidth.getText();
		String length = dlg.txtBlankLength.getText();

		int thicknessValue = 0;
		if (StringExtension.isDouble(thickness)) {
			Double thicknessValueDouble = (Double.parseDouble(thickness) * 100);
			thicknessValue = thicknessValueDouble.intValue();
		}
		if (String.valueOf(thicknessValue).length() < 3)
			thickness = "0" + String.valueOf(thicknessValue);
		else
			thickness = String.valueOf(thicknessValue);

		dlg.txtName.setText(subCategory + "_" + material + (coating.isEmpty() ? "" : "_" + coating) + "_" + thickness + "_" + width + "_" + length);
	}

	private boolean checkExistInLOV(String input, String[] lov) {
		for (String value : lov) {
			if (value.compareToIgnoreCase(input) == 0)
				return true;
		}

		return false;
	}

	private void getModelAndPrefixNumber() {
		String[] preference = TCExtension.GetPreferenceValues("VF_MODEL_AND_PREFIX_NUMBER", session);
		for (String row : preference) {
			String[] str = row.split("=");
			if (str.length > 2) {
				String value = str[0];
				String display = str[1];
				String prefixNumber = str[2];
				modelAndDisplayName.put(value, display);
				modelAndPrefixNumber.put(value, prefixNumber);
			}
		}
		modelAndDisplayName = TCExtension.SortingLOV(modelAndDisplayName);
	}

	private void createNewItem() {
		if (!checkRequired()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}
		dlg.lblNameWarning.setText("");
		DataManagementService dms = DataManagementService.getService(session);
		String objectType = isCarPart ? "VF3_manuf_part" : "VF3_me_scooter";
		String id = dlg.txtID.getText();
		String name = dlg.txtName.getText();
		String model = (String) dlg.cbModel.getData(dlg.cbModel.getText());
		String partMakeBuy = dlg.cbPartMakeBuy.getText();
		String description = dlg.txtDesc.getText();
		String uom = dlg.cbUOM.getText();
		String subCategory = (String) dlg.cbCategory.getData(dlg.cbCategory.getText());
		String partTraceability = "";
		if (!dlg.cbPartTraceability.getText().isEmpty())
			partTraceability = (String) dlg.cbPartTraceability.getData(dlg.cbPartTraceability.getText());

		String partCategory = dlg.cbPartCategory.getText();
		Boolean isAfterSale = dlg.rbtIsAfterSaleTrue.getSelection();
		String supplierType = dlg.cbSupplierType.getText();
		// for Scooter
		String vehicleLine = (String) dlg.cbVehicleLine.getData(dlg.cbVehicleLine.getText());
		String partRef = dlg.txtPartReference.getText();
		String partNameViet = dlg.txtPartNameVietnamese.getText();

		// for COIL Part
		String coilMaterial = dlg.cbCoilMaterial.getText();
		String coilCoating = dlg.cbCoilCoating.getText();
		double coilThickness = 0;
		try {
			coilThickness = Double.parseDouble(dlg.txtCoilThickness.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
		double coilWidth = 0;
		try {
			coilWidth = Double.parseDouble(dlg.txtCoilWidth.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean isCoilPurchase = dlg.ckbIsCoilPurchase.getSelection();
		boolean isBlankPurchase = dlg.ckbIsBlankPurchase.getSelection();

		// for BLANK Part
		String blankMaterial = dlg.cbBlankMaterial.getText();
		String blankCoating = dlg.cbBlankCoating.getText();
		double blankThickness = 0;
		try {
			blankThickness = Double.parseDouble(dlg.txtBlankThickness.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
		double blankWidth = 0;
		try {
			blankWidth = Double.parseDouble(dlg.txtBlankWidth.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
		double blankLength = 0;
		try {
			blankLength = Double.parseDouble(dlg.txtBlankLength.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}

		TCComponent UOMTag = null;
		try {
			UOMTag = TCExtension.GetUOMItem(uom);

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = objectType;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("object_desc", description);
			itemDef.data.stringProps.put("vf4_item_make_buy", partMakeBuy);
			itemDef.data.tagProps.put("uom_tag", UOMTag);
			itemDef.data.boolProps.put("vf4_itm_after_sale_relevant", isAfterSale);
			itemDef.data.stringProps.put("vf4_item_is_traceable", partTraceability);
			itemDef.data.stringProps.put("vf4_part_category", partCategory);
			// for Scooter
			if (!isCarPart) {
				itemDef.data.stringProps.put("vf4_es_model_veh_line", vehicleLine);
				itemDef.data.stringProps.put("vf4_part_reference", partRef);
			} else {
				itemDef.data.stringProps.put("vf4_donor_veh", model);
				itemDef.data.stringProps.put("vf4_me_part_type", subCategory);
				itemDef.data.stringProps.put("vf4_supplier_type", supplierType);
				if (isCOILPart) {
					itemDef.data.stringProps.put("vf4_COI_material", coilMaterial);
					itemDef.data.stringProps.put("vf4_coating", coilCoating);
					itemDef.data.doubleProps.put("vf4_thickness", coilThickness);
					itemDef.data.doubleProps.put("vf4_width", coilWidth);
					itemDef.data.boolProps.put("vf4_is_purchase_ME_part", isCoilPurchase);
				} else if (isBLNPart) {
					itemDef.data.stringProps.put("vf4_COI_material", blankMaterial);
					itemDef.data.stringProps.put("vf4_coating", blankCoating);
					itemDef.data.doubleProps.put("vf4_thickness", blankThickness);
					itemDef.data.doubleProps.put("vf4_width", blankWidth);
					itemDef.data.doubleProps.put("vf4_length", blankLength);
					itemDef.data.boolProps.put("vf4_is_purchase_ME_part", isBlankPurchase);
				}
			}
			// -------------------------

			// Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = objectType + "Revision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("object_desc", description);
			// for Scooter
			if (!isCarPart) {
				itemRevisionDef.stringProps.put("vf3_viet_desciption", partNameViet);
			}
			// -------------------------
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponent cfgContext = response.output[0].objects[0];
				TCComponentItemRevision itemRev = (TCComponentItemRevision) response.output[0].objects[2];
				createRelation(itemRev);
				if (cfgContext != null) {
					Boolean addToFolder = false;
					if (selectedObject != null) {
						String type = selectedObject.getProperty("object_type");
						if (type.compareToIgnoreCase("Folder") == 0) {
							try {
								selectedObject.add("contents", cfgContext);
								addToFolder = true;
								dlg.setMessage("Created successfully, new item has been copied to " + selectedObject.getProperty("object_name") + " folder.", IMessageProvider.INFORMATION);
								openOnCreate(cfgContext);
							} catch (TCException e1) {
								e1.printStackTrace();
							}
						}
					}
					if (!addToFolder) {
						try {
							session.getUser().getNewStuffFolder().add("contents", cfgContext);
							dlg.setMessage("Created successfully, new item has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
							openOnCreate(cfgContext);
						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
					if ((isCOILPart && isCoilPurchase) || (isBLNPart && isBlankPurchase))
						submitToWorkflow(itemRev);
					resetDialog();
				} else {
					dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
				}
			} else {
				ServiceData serviceData = response.serviceData;
				for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
					for (String msg : serviceData.getPartialError(i).getMessages()) {
						MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createRelation(TCComponentItemRevision itemRev) {
		try {
			if (isCOILPart) {
				if (parentPart != null) {
					if (parentType.compareTo("ME Part Revision") == 0) {
						itemRev.setRelated("VF4_blank_part_relation", new TCComponent[] { parentPart });
					}
				}
			} else if (isBLNPart) {
				if (parentPart != null) {
					if (parentType.compareTo("ME Part Revision") != 0) {
						itemRev.setRelated("VF4_H_Part_Relation", new TCComponent[] { parentPart });
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openOnCreate(TCComponent object) {
		try {
			if (dlg.ckbOpenOnCreate.getSelection())
				TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateSupplierType(boolean addNone) {
		boolean haveNone = false;
		for (String value : dlg.cbSupplierType.getItems()) {
			if (value.compareTo("NONE") == 0) {
				haveNone = true;
				break;
			}
		}
		if (addNone) {
			if (!haveNone)
				dlg.cbSupplierType.add("NONE");
		} else {
			if (haveNone)
				dlg.cbSupplierType.remove("NONE");
		}
	}

	private void submitToWorkflow(TCComponent newPart) {
		TriggerProcess process = new TriggerProcess(session);
		process.setWorkProcessTemplace(WORKPROCESS_TEMPLACE);
		process.setWorkProcessName("Raw Material SOR Process");
		process.setAlName("RawMaterial_SOR_Process");
		process.setWorkProcessAttachment(new String[] { newPart.getUid() });

		TCComponent currentProcess = process.trigger();
		if (currentProcess != null) {
			try {
				createDataset(currentProcess, newPart);

				TCComponentTask rootTask = ((TCComponentProcess) currentProcess).getRootTask();
				TCComponentTask[] subtasks = rootTask.getSubtasks();
				for (TCComponentTask subtask : subtasks) {
					String taskType = subtask.getTaskType();
					String taskName = subtask.getProperty("object_name");
					if (taskType.compareToIgnoreCase("EPMTask") == 0 && taskName.compareToIgnoreCase("Waiting Dataset") == 0) {

						if (subtask.getTaskState().compareToIgnoreCase("Started") == 0) {
							try {
								Thread.sleep(1000);
								currentProcess.refresh();
								subtask.refresh();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							Workflow.PerformActionInputInfo paii = new Workflow.PerformActionInputInfo();
							paii.clientId = "complete" + subtask.getUid();
							paii.action = "SOA_EPM_complete_action";
							paii.actionableObject = subtask;
							paii.propertyNameValues.put("comments", new String[] { "Auto Completed" });
							paii.supportingValue = "SOA_EPM_completed";

							ServiceData sd = WorkflowService.getService(session).performAction3(new Workflow.PerformActionInputInfo[] { paii });
							if (sd.sizeOfPartialErrors() > 0) {
								System.out.println("[assignPerformer]: " + Utils.HanlderServiceData(sd));
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void createDataset(TCComponent createdProcess, TCComponent part) throws TCException, IOException {
		String rootTaskUid = createdProcess.getReferenceProperty("root_task").getUid();
		if (createdProcess != null) {
			System.out.println("Creating dataset for " + rootTaskUid);
			File tempParamFile = new File(System.getenv("tmp") + "\\" + String.valueOf(System.currentTimeMillis()) + ".txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempParamFile));

			StringBuffer line = new StringBuffer();
			line.append("UID=");
			line.append(part.getUid());
			line.append("~~");
			line.append(Constant.VFBOMLINE.CHANGE_INDEX).append("=").append("NEW");
			line.append("~~");
			line.append(Constant.VFBOMLINE.PUR_LEVEL_VF).append("=").append("P");
			line.append("~~");
			line.append(Constant.VFBOMLINE.MODULE_GROUP_ENGLISH).append("=").append("");
			line.append("~~");
			line.append(Constant.VFBOMLINE.MODULE_ENGLISH).append("=").append("");
			line.append("~~");
			line.append(Constant.VFBOMLINE.MAIN_MODULE_ENGLISH).append("=").append("");
			line.append("~~");
			line.append(Constant.VFBOMLINE.UOM).append("=").append(dlg.cbUOM.getText());
			writer.write(line.toString() + "\n");

			writer.close();
			Utils.createDataset(session, rootTaskUid, "Text", "DBA_Created", tempParamFile.getAbsolutePath());
		}
	}
}
