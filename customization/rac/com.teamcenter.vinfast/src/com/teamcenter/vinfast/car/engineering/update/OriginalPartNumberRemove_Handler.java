package com.teamcenter.vinfast.car.engineering.update;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PostEventObjectProperties;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PostEventResponse;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.vf.utils.GrandWriterAccessService;
import com.vf.utils.TCExtension;

public class OriginalPartNumberRemove_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponentItem selectedObject = null;
	private OriginalPartNumberRemove_Dialog dlg;
	private String[] ROLE_ACCESS = new String[] { "ECM", "DBA" };

	public OriginalPartNumberRemove_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();

		if (!validObjectSelect(app.getTargetComponents())) {
			MessageBox.post("Please select one VF Design, VF BP Design, Bomline.", "Information", MessageBox.WARNING);
			return null;
		}

		if (!TCExtension.checkUserHasRole(ROLE_ACCESS, session)) {
			MessageBox.post("You do not have permission to access on item.", "Access Denied", MessageBox.WARNING);
			return null;
		}

		try {
			String oldPartNumber = selectedObject.getPropertyDisplayableValue("vf4_orginal_part_number");
			if (oldPartNumber.isEmpty()) {
				MessageBox.post("Original Part Number is empty. No need to remove.", "Information", MessageBox.WARNING);
				return null;
			}

			dlg = new OriginalPartNumberRemove_Dialog(new Shell());
			dlg.create();

			dlg.txtPartItem.setText(selectedObject.toString());
			dlg.txtOldPartNumber.setText(oldPartNumber);

			dlg.btnAccept.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					removeOriginalPart();
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void removeOriginalPart() {
		try {
			LinkedList<TCComponent> formList = new LinkedList<TCComponent>();
			LinkedHashMap<String, String> queryInput = new LinkedHashMap<String, String>();
			queryInput.put("Item ID", selectedObject.getPropertyDisplayableValue("item_id"));

			TCComponent costRevision = null;
			TCComponent[] costRevisions = TCExtension.queryItem(session, queryInput, "__TNH_CostRev_VFWorkingRule");
			if (costRevisions != null && costRevisions.length > 0)
				costRevision = costRevisions[0];

			if (costRevision != null) {
				TCComponent[] sapCostForms = costRevision.getRelatedComponents("VF4_SAPCostRelation");
				if (sapCostForms != null) {
					for (TCComponent sapCostForm : sapCostForms) {
						formList.add(sapCostForm);
					}
				}

				TCComponent[] sourcingCostForms = costRevision.getReferenceListProperty("VF4_SourcingCostFormRela");
				if (sourcingCostForms != null) {
					for (TCComponent sourcingCostForm : sourcingCostForms) {
						formList.add(sourcingCostForm);
					}
				}
			}

			TCComponent[] supplierInformationForms = selectedObject.getRelatedComponents("VF4_Supplier_Info_Relation");
			if (supplierInformationForms != null) {
				for (TCComponent supplierInformationForm : supplierInformationForms) {
					formList.add(supplierInformationForm);
				}
			}

			TCComponent[] plantInformationForms = selectedObject.getRelatedComponents("VF4_plant_form_relation");
			if (plantInformationForms != null) {
				for (TCComponent plantInformationForm : plantInformationForms) {
					formList.add(plantInformationForm);
				}
			}

			TCComponentItemRevision itemRev = TCExtension.getLatestItemRevision(selectedObject);

			LinkedList<TCComponent> accessList = new LinkedList<TCComponent>();
			accessList.addAll(formList);
			accessList.add(selectedObject);
			accessList.add(itemRev);

			GrandWriterAccessService grandAccess = new GrandWriterAccessService(accessList.toArray(new TCComponent[0]), session);
			grandAccess.triggerProcess("Remove Older Part Number - " + selectedObject.getPropertyDisplayableValue("item_id"));

			removeProperty(selectedObject, new String[] { "vf3_old_part_number", "vf4_orginal_part_number", "vf4_vf_code", "vl5_color_code", "vf4_supplier_type", "vf4_long_short_lead", "vf4_item_is_traceable" });
			removeProperty(itemRev, new String[] { "vf4_q_checker", "vl5_specbook_num", "vf4_sor_number_rev", "vf4_sor_name_rev", "vf4_sor_release_date_rev", "vf4_forecast_date_SOB" });

			for (TCComponent form : formList) {
				removeFormProperty(form);
			}

			grandAccess.completeTask();

			selectedObject.refresh();
			dlg.setMessage("Update successfully.", IMessageProvider.INFORMATION);
		} catch (Exception e) {
			e.printStackTrace();
			dlg.setMessage("Update unsuccessfully.", IMessageProvider.ERROR);
		}
	}

	private void removeFormProperty(TCComponent form) {
		HashMap<String, VecStruct> nameValMap = new HashMap<>();
		Hashtable<String, PropertyDescription> propertyDescriptionMap = form.getTypeObject().getPropDescs();
		for (Map.Entry<String, PropertyDescription> propDescEntry : propertyDescriptionMap.entrySet()) {
			String propName = propDescEntry.getKey();
			PropertyDescription propDesc = propDescEntry.getValue();
			if (!propDesc.isModifiable())
				continue;

			int propType = propDesc.getType();
			if (propName.startsWith("vf4")) {
				if (propType == PropertyDescription.CLIENT_PROP_TYPE_string || propType == PropertyDescription.CLIENT_PROP_TYPE_date || propType == PropertyDescription.CLIENT_PROP_TYPE_double) {
					VecStruct valVec = new VecStruct();
					valVec.stringVec = new String[] { "" };
					nameValMap.put(propName, valVec);
				}
			}
		}
		if (nameValMap.size() > 0) {
			try {
				DataManagementService.getService(session).setProperties(new TCComponent[] { form }, nameValMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void removeProperty(TCComponent object, String[] properties) {
		try {
			for (String property : properties) {
				object.setProperty(property, "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		try {
			if (targetComponents == null)
				return true;

			if (targetComponents.length > 1)
				return true;

			if (targetComponents[0] instanceof TCComponentBOMLine) {
				selectedObject = ((TCComponentBOMLine) targetComponents[0]).getItem();
			} else if (targetComponents[0] instanceof TCComponentItem) {
				selectedObject = (TCComponentItem) targetComponents[0];
			}

			if (selectedObject == null)
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
