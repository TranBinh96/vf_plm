package com.teamcenter.vinfast.aftersale.update;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.open.OpenFormDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.utils.CannotCreateCostException;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.utils.TCExtension;

public class ServiceChapterUpdate_Handler extends AbstractHandler {
	private TCSession session;

	private static String OBJECT_TYPE = "VF4_SBOM_Change_Note";
	private static String OBJECT_RELATION = "VF4_SBOM_ChangeNoteRelation";
	private static String SEARCH_TYPE = "SBOM Change Note";
	private static String SUFFIX = "_SBOMChangeNote";
	private static String PREFERENCE_GROUP_CHANGENOTE = "VINF_CHANGE_NOTE_GROUP_PER";

	public ServiceChapterUpdate_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();

		if (appComps != null && appComps.length > 1) {
			MessageBox.post("This feature is applicable to single part only.", "Warning", MessageBox.WARNING);
			return null;
		}

		InterfaceAIFComponent appComp = app.getTargetComponent();
		if (appComp == null) {
			return null;
		}

		try {
			TCComponentGroupMember groupMember = TCExtension.getCurrentGroupMember(session);
			TCComponentItemRevision partRev = null;

			if (appComp instanceof TCComponentBOMLine) {
				TCComponentBOMLine bom = (TCComponentBOMLine) app.getTargetComponent();
				partRev = bom.getItemRevision();
			} else {
				partRev = (TCComponentItemRevision) appComp;
			}

			TCComponentGroup group = groupMember.getGroup();
			String[] groupTarget = TCExtension.GetPreferenceValues(PREFERENCE_GROUP_CHANGENOTE, session);//"Service Technic.After sale"
			if (groupTarget != null && groupTarget.length > 0) {
				if (group.toString().compareToIgnoreCase(groupTarget[0]) != 0) {
					MessageBox.post("You are not authorized to Create/Update SBOM Change Note.",
							"Please change to group: " + groupTarget[0] + " and try again.", "Access",
							MessageBox.ERROR);
					return null;
				}
			}

			TCComponent formItem = getAndRelateForm(partRev);
			if (formItem != null) {
				OpenFormDialog pfepDialog = new OpenFormDialog((TCComponentForm) formItem);
				pfepDialog.setVisible(true);
			} else {
				MessageBox.post(
						"Cannot find Change Notices form of the selected part." + "\nPlease contact your IT Helpdesk.",
						"Error", MessageBox.ERROR);
			}
		} catch (CannotCreateCostException ex) {
			MessageBox.post(ex.getMessage(), "Information", MessageBox.INFORMATION);
		} catch (Exception ex) {
			MessageBox.post("There are some issues occurred. Please contact your IT Service Desk.\n" + ex.getMessage(),
					"Error", MessageBox.ERROR);
			ex.printStackTrace();
		}

		return null;
	}

	private TCComponent getAndRelateForm(TCComponentItemRevision partRev)
			throws TCException, NotLoadedException, Exception {
		AIFComponentContext[] formItemAifs = partRev.getItem().getRelated(OBJECT_RELATION);
		TCComponent formItem = null;
		TCComponent[] formItems = new TCComponent[formItemAifs != null ? formItemAifs.length : 0];
		for (int i = 0; i < formItems.length; i++)
			formItems[i] = (TCComponent) formItemAifs[i].getComponent();

		if (formItems == null || formItems.length == 0) {
			String formName = getFormName(partRev);
			formItems = Utils.executeSavedQuery("General...", new String[] { formName, SEARCH_TYPE },
					new String[] { "Name", "Type" });

			if (formItems.length == 0) {
				TCComponent form = createForm(partRev);
				formItems = new TCComponent[1];
				formItems[0] = form;
			}
		}

		if (formItems != null && formItems.length > 0) {
			for (TCComponent comp : formItems) {
				if (comp.getType().contains(OBJECT_TYPE)) {
					formItem = comp;
					formItem.refresh();
					partRev.setRelated(OBJECT_RELATION, new TCComponent[] { formItem });
					break;
				}
			}
		}

		return formItem;
	}

	private String getFormName(TCComponentItemRevision partRev) throws NotLoadedException {
		return partRev.getPropertyDisplayableValue("item_id") + SUFFIX;
	}

	private TCComponent createForm(ModelObject partRev) throws Exception {
		// create form
		String formName = partRev.getPropertyDisplayableValue("item_id") + SUFFIX;
		DataManagementService dms = DataManagementService.getService(session);
		CreateIn in = new CreateIn();
		in.data.boName = OBJECT_TYPE;
		in.data.stringProps.put("object_name", formName);
		CreateIn[] ins = new CreateIn[] { in };
		CreateResponse res = dms.createObjects(ins);
		if (res.output.length > 0 && res.output[0].objects.length > 0) {
			// change ownerships
			TCComponent formItem = res.output[0].objects[0];
//			TCComponentGroup partRevOwningGroup = (TCComponentGroup) (((TCComponentItemRevision) partRev).getReferenceProperty("owning_group"));
//			TCComponentUser partRevOwningUser = (TCComponentUser) (((TCComponentItemRevision) partRev).getReferenceProperty("owning_user"));
//			formItem.changeOwner(partRevOwningUser, partRevOwningGroup);
			return formItem;
		} else {
			String errorMessage = Utils.getErrorMessagesFromSOA(res.serviceData);
			throw new Exception(errorMessage);
		}
	}
}
