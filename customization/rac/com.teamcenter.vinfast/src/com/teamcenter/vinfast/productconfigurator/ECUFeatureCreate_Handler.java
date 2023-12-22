package com.teamcenter.vinfast.productconfigurator;

import java.util.Arrays;
import java.util.LinkedHashMap;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ECUFeatureCreate_Handler extends AbstractHandler {
	private TCSession session;
	private ECUFeatureCreate_Dialog dlg;
	private TCComponent selectedObject = null;

	private LinkedHashMap<String, TCComponent> featureList = null;
	private LinkedHashMap<String, TCComponent> groupList = null;

	public ECUFeatureCreate_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			
			TCComponent[] objectChildComponents = selectedObject.getRelatedComponents("vf3_ecr_release_features");
			
			if(!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
				MessageBox.post("You don't have write access to update information: " + selectedObject.getPropertyDisplayableValue("item_id"), "ERROR", MessageBox.ERROR);
				return null;
			}

			// Init data
			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Context ID", "*");
			TCComponent[] item_search = Query.queryItem(session, inputQuery, "__Cfg0GetAllFamilyGroupForContext");

			LinkedHashMap<String, String> familyGroupList = new LinkedHashMap<String, String>();
			LinkedHashMap<String, LinkedHashMap<String, String>> familyList = new LinkedHashMap<String, LinkedHashMap<String, String>>();
			featureList = new LinkedHashMap<String, TCComponent>();
			groupList = new LinkedHashMap<String, TCComponent>();

			if (item_search != null && item_search.length > 0) {
				for (TCComponent tcComponent : item_search) {
					String familyGroupID = tcComponent.getPropertyDisplayableValue("cfg0ObjectId");
					String familyGroupDesc = tcComponent.getPropertyDisplayableValue("current_desc");
					familyGroupList.put(familyGroupID, familyGroupID + " - " + familyGroupDesc);
					groupList.put(familyGroupID, tcComponent);
				}
			}
			
			String[] marketDataForm = TCExtension.GetLovValues("VF4_Market");
			String[] modelDataForm = TCExtension.GetLovValues("VF4_model_FRS_LOV");

			// Init UI
			dlg = new ECUFeatureCreate_Dialog(new Shell());
			dlg.create();

			StringExtension.UpdateValueTextCombobox(dlg.cbFamilyGroup, TCExtension.SortingLOV(familyGroupList));

			dlg.cbFamilyGroup.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					String group = (String) dlg.cbFamilyGroup.getData(dlg.cbFamilyGroup.getText());
					dlg.cbFamily.removeAll();
					if (!group.isEmpty()) {
						LinkedHashMap<String, String> fam = familyList.get(group);
						if (fam == null) {
							try {
								TCComponent groupObject = groupList.get(group);
								TCComponent[] families = groupObject.getRelatedComponents("cfg0Families");
								if (families != null && families.length > 0) {
									LinkedHashMap<String, String> fa = new LinkedHashMap<String, String>();
									for (TCComponent family : families) {
										String familyID = family.getPropertyDisplayableValue("cfg0ObjectId");
										String familyDesc = family.getPropertyDisplayableValue("object_desc");
										fa.put(familyID, familyID + " - " + familyDesc);
										featureList.put(familyID, family);
									}
									familyList.put(group, TCExtension.SortingLOV(fa));
									fam = TCExtension.SortingLOV(fa);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if (fam != null)
							StringExtension.UpdateValueTextCombobox(dlg.cbFamily, fam);
					}
				}
			});
			
			dlg.cbMarket.setItems(marketDataForm);
			dlg.btnAddMarket.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					addValueToList(dlg.cbMarket.getText(), dlg.lstMarket);
				}
			});
			
			dlg.btnRemoveMarket.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int index = dlg.lstMarket.getSelectionIndex();
					if (index != -1) {
						dlg.lstMarket.remove(index);
					}
				}
			});
			
			dlg.cbModel.setItems(modelDataForm);
			dlg.btnAddModel.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					addValueToList(dlg.cbModel.getText(), dlg.lstModel);
				}
			});
			
			dlg.btnRemoveModel.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int index = dlg.lstModel.getSelectionIndex();
					if (index != -1) {
						dlg.lstModel.remove(index);
					}
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					addFeature();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean submitValidation() {
		if (dlg.cbFamily.getText().isEmpty())
			return false;

		return true;
	}

	private void addFeature() {
		try {
			if (!submitValidation()) {
				dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
				return;
			}

			String featureSelectedID = (String) dlg.cbFamily.getData(dlg.cbFamily.getText());
			TCComponent featureSelected = featureList.get(featureSelectedID);

			if (featureSelected != null) {
				DataManagementService dms = DataManagementService.getService(session);
				String objectType = "VF3_ECU_ReleaseFeatures";
				String key = featureSelected.getPropertyDisplayableValue("cfg0ObjectId");
				String name = featureSelected.getPropertyDisplayableValue("object_desc");
				String market = getStringFromArray(dlg.lstMarket.getItems());
				String model = getStringFromArray(dlg.lstModel.getItems());
				double vehicleVersion = -1;
				if(StringExtension.isDouble(dlg.txtVehicleVersion.getText())) {
					vehicleVersion = Double.parseDouble(dlg.txtVehicleVersion.getText());
				}
				String optionDepend = dlg.txtOptionDependency.getText();

				CreateIn itemDef = new CreateIn();
				itemDef.clientId = "1";
				itemDef.data.boName = objectType;
				itemDef.data.stringProps.put("vf3_key", key);
				itemDef.data.stringProps.put("vf3_name", name);
				itemDef.data.stringProps.put("vf4_market_arr", market);
				itemDef.data.stringProps.put("vf4_model_arr", model);
				if(vehicleVersion >= 0)
					itemDef.data.doubleProps.put("vf4_minimum_vehicle_version", vehicleVersion);
				itemDef.data.stringProps.put("vf4_OptionsDependExpression", optionDepend);
				itemDef.data.tagProps.put("vf3_object_reference", featureSelected);
				
				CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

				if (response.serviceData.sizeOfPartialErrors() == 0) {
					TCComponent cfgContext = response.output[0].objects[0];
					selectedObject.add("vf3_ecr_release_features", new TCComponent[] { cfgContext });
					dlg.setMessage("Add feature successfully.", IMessageProvider.INFORMATION);
					resetUI();
				} else {
					ServiceData serviceData = response.serviceData;
					for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
						for (String msg : serviceData.getPartialError(i).getMessages()) {
							MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
						}
					}
				}
			}
		} catch (Exception exp) {
			dlg.setMessage(exp.toString(), IMessageProvider.ERROR);
			exp.printStackTrace();
		}
	}

	private void resetUI() {
		dlg.cbFamilyGroup.deselectAll();
		dlg.cbFamily.removeAll();
	}
	
	private void addValueToList(String newValue, List targetList) {
		if (!newValue.isEmpty()) {
			String[] listItem = targetList.getItems();
			Boolean checkExist = Arrays.stream(listItem).anyMatch(x -> x.compareToIgnoreCase(newValue) == 0);
			if (!checkExist) {
				targetList.add(newValue);
			}
		}
	}
	
	private String getStringFromArray(String[] array) {
		if(array != null && array.length > 0) {
			return String.join(",", array);
		}
			
		return "";	
	}
}
