package com.teamcenter.vinfast.escooter.engineering.create;

import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
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
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ScooterColorPartCreate_Handler extends AbstractHandler {
	private TCSession session;
	private ScooterColorPartCreate_Dialog dlg;
	private TCComponentBOMLine selectedObject;
	private LinkedHashMap<String, String> materialGroupDataForm;
	private InterfaceAIFComponent[] targetComp =null;
	private TCComponent selectedObjectItem = null;


	public ScooterColorPartCreate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (!validObjectSelect(targetComp)) {
				MessageBox.post("Selected BOMLine is not Scooter Part/ME Scooter Part.", "Error", MessageBox.ERROR);
				return null;
			}
			LinkedHashMap<String, String> objectTypeDataForm = new LinkedHashMap<String, String>() {
				{
					put("VF3_Scooter_part", "Scooter Part");
					put("VF3_me_scooter", "ME Scooter Part");
				}
			};

			// init data
			LinkedHashMap<String, String> vehicleLineDataForm = TCExtension.GetLovValueAndDisplay("vf4_es_model_veh_line", "VF3_Scooter_part", session);
			materialGroupDataForm = TCExtension.GetLovValueAndDescription("vf3_mat_group", "VF3_Scooter_part", session);

			String[] baseColorDataForm = TCExtension.GetPreferenceValues("Vinfast_Scooter_Color_Code", session);
			String[] subColorDataForm = TCExtension.GetPreferenceValues("VF_ESCOOTER_SUB_COLOR_CODE", session);
			String[] baseMaterialDataForm = TCExtension.GetPreferenceValues("VINF_SCOOTERCOLORPART_BASEMATERIAL", session);
			String[] colorMakingMethodDataForm = TCExtension.GetPreferenceValues("VINF_SCOOTERCOLORPART_COLORMAKINGMETHOD", session);
			String[] characteristicDataForm = TCExtension.GetPreferenceValues("VINF_SCOOTERCOLORPART_CHARACTERISTIC", session);
			String[] subColorNameDataForm = TCExtension.GetPreferenceValues("VINF_SCOOTERCOLORPART_SUBCOLORNAME", session);
			String[] lovUOM = TCExtension.GetUOMList(session);

			dlg = new ScooterColorPartCreate_Dialog(new Shell());
			dlg.create();

			StringExtension.UpdateValueTextCombobox(dlg.cbItemType, objectTypeDataForm);
			dlg.cbItemType.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateColorCode();
					dlg.lblVehicleLine.setVisible(dlg.cbItemType.getText().compareToIgnoreCase("Scooter Part") == 0);
					dlg.cbVehicleLine.setVisible(dlg.cbItemType.getText().compareToIgnoreCase("Scooter Part") == 0);
					dlg.lblMaterialGroup.setVisible(dlg.cbItemType.getText().compareToIgnoreCase("Scooter Part") == 0);
					dlg.cbMaterialGroup.setVisible(dlg.cbItemType.getText().compareToIgnoreCase("Scooter Part") == 0);
				}
			});

			dlg.cbBaseColor.setItems(baseColorDataForm);
			dlg.cbBaseColor.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateColorCode();
				}
			});

			dlg.cbSubColor.setItems(subColorDataForm);
			dlg.cbSubColor.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event arg0) {
					updateColorCode();
				}
			});

			dlg.cbBaseMaterial.setItems(baseMaterialDataForm);

			dlg.cbColorMakingMethod.setItems(colorMakingMethodDataForm);

			dlg.cbCharacteristic.setItems(characteristicDataForm);

			dlg.cbSubColorName.setItems(subColorNameDataForm);

			dlg.cbUOM.setItems(lovUOM);

			StringExtension.UpdateValueTextCombobox(dlg.cbVehicleLine, vehicleLineDataForm);

			StringExtension.UpdateValueTextCombobox(dlg.cbMaterialGroup, materialGroupDataForm);

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					createNewPart();
				}
			});

			updateSelectedBasePart();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void updateSelectedBasePart() throws NotLoadedException, TCException {
	
		TCComponentItem item = null;
		String id, name, uom, materialGroup, vietDesc = "";
		try {
			if (targetComp[0] instanceof TCComponentBOMLine) {
				item = selectedObject.getItem();
				TCComponentItemRevision itemRevision = selectedObject.getItemRevision();
				id = itemRevision.getPropertyDisplayableValue("item_id");
				name = itemRevision.getProperty("object_name");
				uom = item.getProperty("uom_tag");
				vietDesc = itemRevision.getPropertyDisplayableValue("vf3_viet_desciption");				
			}else {				
				selectedObjectItem = (TCComponentItemRevision) targetComp[0];
				item = ((TCComponentItemRevision) selectedObjectItem).getItem();
				id = selectedObjectItem.getPropertyDisplayableValue("item_id");
				name = selectedObjectItem.getProperty("object_name");
				uom = ((TCComponentItemRevision) selectedObjectItem).getItem().getProperty("uom_tag");
				vietDesc = selectedObjectItem.getPropertyDisplayableValue("vf3_viet_desciption");	
			}
			materialGroup = TCExtension.GetPropertyRealValue(item, "vf3_mat_group");
			if (materialGroupDataForm.get(materialGroup) != null) {
				dlg.txtID.setText(id);
				dlg.txtName.setText(name);
				dlg.cbUOM.setText(uom);
				dlg.cbMaterialGroup.setText(materialGroupDataForm.get(materialGroup));
				dlg.txtVietnameseDescription.setText(vietDesc);
			}else
				dlg.setMessage("Please update Material Group. ", IMessageProvider.WARNING);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private boolean checkRequired() {
		boolean check = true;

		if (dlg.cbItemType.getText().isEmpty()) {
			warningLabel(dlg.lblItemType, true);
			check = false;
		} else {
			warningLabel(dlg.lblItemType, false);
		}
		if (dlg.cbBaseColor.getText().isEmpty()) {
			warningLabel(dlg.lblBaseColor, true);
			check = false;
		} else {
			warningLabel(dlg.lblBaseColor, false);
		}
		if (dlg.cbSubColor.getText().isEmpty()) {
			warningLabel(dlg.lblSubColor, true);
			check = false;
		} else {
			warningLabel(dlg.lblSubColor, false);
		}
		if (dlg.txtID.getText().isEmpty() || dlg.txtColorCode.getText().isEmpty()) {
			warningLabel(dlg.lblID, true);
			check = false;
		} else {
			warningLabel(dlg.lblID, false);
		}
		if (dlg.txtName.getText().isEmpty()) {
			warningLabel(dlg.lblName, true);
			check = false;
		} else {
			warningLabel(dlg.lblName, false);
		}
		if (dlg.cbUOM.getText().isEmpty()) {
			warningLabel(dlg.lblUom, true);
			check = false;
		} else {
			warningLabel(dlg.lblUom, false);
		}

		if (dlg.cbItemType.getText().compareToIgnoreCase("Scooter Part") == 0) {
			if (dlg.cbVehicleLine.getText().isEmpty()) {
				warningLabel(dlg.lblVehicleLine, true);
				check = false;
			} else {
				warningLabel(dlg.lblVehicleLine, false);
			}
		}

		return check;
	}

	private void createNewPart() {
		if (!checkRequired()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}

		DataManagementService dmService = DataManagementService.getService(session);

		String objectType = (String) dlg.cbItemType.getData(dlg.cbItemType.getText());
		String id = dlg.txtID.getText() + dlg.txtColorCode.getText();
		String name = dlg.txtName.getText();
		String vietDescription = dlg.txtVietnameseDescription.getText();
		String vehicleLine = (String) dlg.cbVehicleLine.getData(dlg.cbVehicleLine.getText());
		String colorCode = dlg.txtColorCode.getText();
		String baseMaterial = dlg.cbBaseMaterial.getText();
		String colorMethod = dlg.cbColorMakingMethod.getText();
		String characteristic = dlg.cbCharacteristic.getText();
		String subColorName = dlg.cbSubColorName.getText();
		String subColor = dlg.cbSubColor.getText();
		String uom = dlg.cbUOM.getText();
		String materialGroup = (String) dlg.cbMaterialGroup.getData(dlg.cbMaterialGroup.getText());

		TCComponent UOMTag = null;

		try {
			UOMTag = TCExtension.GetUOMItem(uom);

			// Item
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = objectType;
			itemDef.data.stringProps.put("item_id", id);
			itemDef.data.stringProps.put("object_name", name);
			itemDef.data.stringProps.put("vf3_color_code", colorCode);
			itemDef.data.stringProps.put("vf3_base_material", baseMaterial);
			itemDef.data.stringProps.put("vf3_color_making_method", colorMethod);
			itemDef.data.stringProps.put("vf3_characteristic", characteristic);
			itemDef.data.stringProps.put("vf3_sub_color", subColor);
			itemDef.data.stringProps.put("vf3_sub_color_name", subColorName);
			itemDef.data.tagProps.put("uom_tag", UOMTag);
			String infoItem = dlg.txtID.getText() + dlg.txtColorCode.getText();

			if (objectType.compareToIgnoreCase("VF3_Scooter_part") == 0) {
				itemDef.data.stringProps.put("vf4_es_model_veh_line", vehicleLine);
				itemDef.data.stringProps.put("vf3_mat_group", materialGroup);
			}

			// Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = objectType + "Revision";
			itemRevisionDef.stringProps.put("item_revision_id", "01");
			itemRevisionDef.stringProps.put("vf3_viet_desciption", vietDescription);

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { itemRevisionDef });
			CreateResponse response = dmService.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() > 0) {
				dlg.setMessage(TCExtension.hanlderServiceData(response.serviceData), IMessageProvider.ERROR);
				return;
			}

			TCComponentItemRevision cfgContext = null;
			for (TCComponent rev : response.output[0].objects) {
				if (rev instanceof TCComponentItemRevision)
					cfgContext = (TCComponentItemRevision) rev;
			}
			if (cfgContext != null) {
				if (selectedObject != null ||  selectedObjectItem != null){
					if (targetComp[0] instanceof TCComponentBOMLine) {
						addToBOMLine(cfgContext);
						dlg.setMessage("Created successfully, new item "+infoItem+" has been add to BOM struct. ", IMessageProvider.INFORMATION);
					}
					else {
						try {
							session.getUser().getNewStuffFolder().add("contents", cfgContext);
							openOnCreate(cfgContext);
							dlg.setMessage("Created successfully, new item "+infoItem+" has been copied to your Newstuff folder", IMessageProvider.INFORMATION);

						} catch (TCException e1) {
							e1.printStackTrace();
						}
					}
				}
				resetDialog();
			} else {
				dlg.setMessage("Create unsuccessfully, please contact with administrator.", IMessageProvider.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addToBOMLine(TCComponentItemRevision newRev) {
		try {
			TCComponentBOMLine bomline = (TCComponentBOMLine) selectedObject.getReferenceProperty("bl_parent");
			if (bomline != null) {
				TCComponentBOMLine child = bomline.add(newRev.getItem(), newRev, null, false);
				child.setProperty("bl_quantity", "1");
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	

	private void resetDialog() {
		dlg.cbItemType.deselectAll();
		dlg.cbBaseColor.deselectAll();
		dlg.cbSubColor.deselectAll();
		dlg.txtColorCode.setText("");
		dlg.txtName.setText("");
		dlg.txtVietnameseDescription.setText("");
		dlg.cbBaseMaterial.deselectAll();
		dlg.cbColorMakingMethod.deselectAll();
		dlg.cbCharacteristic.deselectAll();
		dlg.cbSubColorName.deselectAll();
		dlg.cbVehicleLine.deselectAll();
		dlg.cbMaterialGroup.deselectAll();
		dlg.btnCreate.setEnabled(false);
	}

	private void updateColorCode() {
		String colorCode = "";
		if (!dlg.cbBaseColor.getText().isEmpty()) {
			String[] strSplit = dlg.cbBaseColor.getText().split("-");
			if (strSplit.length > 1) {
				colorCode += strSplit[1];
			}
		}
		dlg.txtColorCode.setText(colorCode + dlg.cbSubColor.getText());
	}

	private void warningLabel(Label target, boolean warning) {
		if (warning)
			target.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		else
			target.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
	}

	public boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return true;

		if (targetComponents.length > 1)
			return true;

		if (targetComponents[0] instanceof TCComponentBOMLine)
			selectedObject = (TCComponentBOMLine) targetComponents[0];
		else
			selectedObjectItem = (TCComponentItemRevision) targetComponents[0];


		if (selectedObject != null || selectedObjectItem != null) {
			try {
				String objectType = selectedObject.getItem().getType();
				if (objectType.compareTo("VF3_Scooter_partRevision") == 0 || objectType.compareTo("VF3_me_scooterRevision") == 0) {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		

		return true;
	}

	protected void openOnCreate(TCComponent object) {
		try {
			TCExtension.openComponent(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
