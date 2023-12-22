package com.teamcenter.vinfast.general.create.mepart;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class MEPart_Handler extends AbstractHandler {
	private TCSession session;
	private MEPart_Dialog dlg;

	private TCComponent selectedObject = null;

	private LinkedHashMap<String, String> modelAndPrefixNumber;
	private LinkedHashMap<String, String> modelAndDisplayName;
	private LinkedHashMap<String, Boolean> isAfterSaleSetting;
	private LinkedHashMap<String, String> partTraceabilitySetting;
	private LinkedHashMap<String, List<String>> uomSetting;
	private LinkedHashMap<String, List<String>> partMakeBuySetting;
	
	private TCComponentItemRevision parentPart;

	private static String PREFERENCE_MATERIAL_LIST = "VF_MEPART_COIL_MATERIALS";
	private static String PREFERENCE_COATING_LIST = "VF_MEPART_COIL_COATINGS";

	private MEPartCreateAbstract meCom = null;

	public MEPart_Handler() {
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
			uomSetting = new LinkedHashMap<String, List<String>>();
			partMakeBuySetting = new LinkedHashMap<String, List<String>>();

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
					isAfterSaleSetting.put(str[0], null);
					partTraceabilitySetting.put(str[0], "");
					uomSetting.put(str[0], null);
					partMakeBuySetting.put(str[0], null);
					if (str.length > 2) {
						if (str[2].compareTo("1") == 0)
							isAfterSaleSetting.put(str[0], true);
						else if (str[2].compareTo("0") == 0)
							isAfterSaleSetting.put(str[0], false);
					}
					if (str.length > 3) {
						if (str[3].compareTo("1") == 0)
							partTraceabilitySetting.put(str[0], "Yes");
						else
							partTraceabilitySetting.put(str[0], "No");
					}
					if (str.length > 4) {
						if (!str[4].isEmpty()) {
							List<String> uomList = new LinkedList<String>();
							if (str[4].contains(",")) {
								String[] str1 = str[4].split(",");
								for (String value : str1) {
									uomList.add(value);
								}
							} else {
								uomList.add(str[4]);
							}
							uomSetting.put(str[0], uomList);
						}
					}
					if (str.length > 5) {
						if (!str[5].isEmpty()) {
							List<String> makeBuyList = new LinkedList<String>();
							if (str[5].contains(",")) {
								String[] str1 = str[5].split(",");
								for (String value : str1) {
									makeBuyList.add(value);
								}
							} else {
								makeBuyList.add(str[5]);
							}
							partMakeBuySetting.put(str[0], makeBuyList);
						}
					}
				}
			}
			// for scooter
			LinkedHashMap<String, String> vehicleLineDataForm = TCExtension.GetLovValueAndDisplay("vf4_es_model_veh_line", "VF3_me_scooter", session);
			// for coil
			String[] materialDataForm = TCExtension.GetPreferenceValues(PREFERENCE_MATERIAL_LIST, session);
			String[] coatingPreference = TCExtension.GetPreferenceValues(PREFERENCE_COATING_LIST, session);
			LinkedList<String> coatingDataForm = new LinkedList<String>();
			coatingDataForm.add("");
			for (String value : coatingPreference) {
				coatingDataForm.add(value);
			}

			// Init UI
			dlg = new MEPart_Dialog(new Shell());
			dlg.create();

			dlg.cbType.setItems(new String[] { "Car", "Scooter" });

			Listener listener = new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					if (meCom != null) {
						meCom.dispose();
					}
					String subCategory = "";
					if (!dlg.cbCategory.getText().isEmpty())
						subCategory = (String) dlg.cbCategory.getData(dlg.cbCategory.getText());
					
					if(meCom == null || meCom.isDisposed()) {
						meCom = MePartCreateFactory.generateComposite(dlg.container, dlg.cbType.getText(), subCategory);	
						meCom.setMakeBuyDataForm(makeBuyDataForm);
						meCom.setUomDataForm(uomDataForm);
						meCom.setPartTraceabilityDataForm(partTraceabilityDataForm);
						meCom.setPartCategoryDataForm(partCategoryDataForm);
						meCom.setSupplierTypeDataForm(supplierTypeDataForm);
						meCom.setVehicleLineDataForm(vehicleLineDataForm);
						meCom.setMaterialDataForm(materialDataForm);
						meCom.setCoatingDataForm(coatingDataForm);
						meCom.setModelAndDisplayName(modelAndDisplayName);
						meCom.setModelAndPrefixNumber(modelAndPrefixNumber);
						meCom.setIsAfterSaleSetting(isAfterSaleSetting);
						meCom.setPartTraceabilitySetting(partTraceabilitySetting);
						meCom.setUomSetting(uomSetting);
						meCom.setPartMakeBuySetting(partMakeBuySetting);
						meCom.setParentPart(parentPart);
						meCom.setSession(session);
						meCom.initData(subCategory);
					}
					meCom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
					resizeUI();
				}
			};

			dlg.cbType.addListener(SWT.Modify, listener);
			dlg.cbType.select(0);

			StringExtension.UpdateValueTextCombobox(dlg.cbCategory, categoryTypeCar);
			dlg.cbCategory.addListener(SWT.Modify, listener);
			dlg.cbCategory.select(0);

			dlg.btnCreate.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					if (!meCom.checkRequired()) {
						dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
						return;
					}
					String mess = meCom.createNewItem(selectedObject, dlg.ckbOpenOnCreate.getSelection());
					if (!mess.isEmpty())
						dlg.setMessage(mess);
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void resizeUI() {
		Point currentSize = dlg.getShell().getSize();
		dlg.getShell().setSize(currentSize.x, ++currentSize.y);
	}
}
