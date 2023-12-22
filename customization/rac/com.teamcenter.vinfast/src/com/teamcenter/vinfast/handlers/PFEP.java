package com.teamcenter.vinfast.handlers;

import org.apache.log4j.Logger;
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
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
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

public class PFEP extends AbstractHandler {

	private static final Logger logger = Logger.getLogger(PFEP.class);
	private final TCSession session;

	public PFEP() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();

		if (appComps != null && appComps.length > 1) {
			MessageBox.post("This feature is applicable to single part only.", "Information", MessageBox.INFORMATION);
			return null;
		}

		InterfaceAIFComponent appComp = app.getTargetComponent();
		if (appComp == null) {
			return null;
		}

		try {
			TCComponentItemRevision partRev = null;
			if (appComp instanceof TCComponentBOMLine) {
				TCComponentBOMLine bom = (TCComponentBOMLine) app.getTargetComponent();
				partRev = bom.getItemRevision();
			} else {
				partRev = (TCComponentItemRevision) appComp;
			}

			TCComponent pfepForm = getAndRelatePFEPForm(partRev);
			
			if (pfepForm != null) {
				OpenFormDialog pfepDialog = new OpenFormDialog((TCComponentForm) pfepForm);
				pfepDialog.setVisible(true);
			} else {
				MessageBox.post("Cannot find PFEP form of the selected part." + "\nPlease contact your IT Helpdesk.",
						"Error", MessageBox.ERROR);
			}
		} catch (CannotCreateCostException ex) {
			logger.error(ex);
			MessageBox.post(ex.getMessage(), "Information", MessageBox.INFORMATION);
		} catch (Exception ex) {
			logger.error(ex);
			MessageBox.post("There are some issues occurred. Please contact your IT Service Desk.\n" + ex.getMessage(),
					"Error", MessageBox.ERROR);
			ex.printStackTrace();
		}

		return null;
	}

	private TCComponent getAndRelatePFEPForm(TCComponentItemRevision partRev) throws TCException, NotLoadedException, Exception {
		AIFComponentContext[] pfepFormAifs = partRev.getItem().getRelated("VF4_PFEP_Relation");
		TCComponent pfepForm = null;
		TCComponent[] pfepForms = new TCComponent[pfepFormAifs != null ? pfepFormAifs.length : 0];
		for (int i = 0; i < pfepForms.length; i++)
			pfepForms[i] = (TCComponent) pfepFormAifs[i].getComponent();

		if (pfepForms == null || pfepForms.length == 0) {
			String formName = getFormName(partRev);
			pfepForms = Utils.executeSavedQuery("General...",
					new String[] { formName, "PFEP Form" }, new String[] { "Name", "Type" });

			if (pfepForms.length == 0) {
				TCComponent form = createPFEPForm(partRev);
				pfepForms = new TCComponent[1];
				pfepForms[0] = form;
			}
		}
		
		if (pfepForms != null && pfepForms.length > 0) {
			for (TCComponent comp : pfepForms) {
				if (comp.getType().contains("VF4_PFEP_Form")) {
					pfepForm = comp;
					pfepForm.refresh();
					((TCComponentItem) ((TCComponentItemRevision) partRev).getItem()).setRelated("VF4_PFEP_Relation",
							new TCComponent[] { pfepForm });
					break;
				}
			}
		}
		
		return pfepForm;
	}

	private String getFormName(TCComponentItemRevision partRev) throws NotLoadedException {
		return partRev.getPropertyDisplayableValue("item_id") + "_PFEP";
	}

	@SuppressWarnings("unchecked")
	private TCComponent createPFEPForm(ModelObject partRev) throws Exception {
		// create form
		String formName = partRev.getPropertyDisplayableValue("item_id") + "_PFEP";
		DataManagementService dms = DataManagementService.getService(session);
		CreateIn in = new CreateIn();
		in.data.boName = "VF4_PFEP_Form";
		in.data.stringProps.put("object_name", formName);
		CreateIn[] ins = new CreateIn[] { in };
		CreateResponse res = dms.createObjects(ins);
		if (res.output.length > 0 && res.output[0].objects.length > 0) {
			// change ownerships
			TCComponent form = res.output[0].objects[0];
			TCComponentGroup partRevOwningGroup = (TCComponentGroup) (((TCComponentItemRevision) partRev)
					.getReferenceProperty("owning_group"));
			TCComponentUser partRevOwningUser = (TCComponentUser) (((TCComponentItemRevision) partRev)
					.getReferenceProperty("owning_user"));
			form.changeOwner(partRevOwningUser, partRevOwningGroup);
			return form;
		} else {
			String errorMessage = Utils.getErrorMessagesFromSOA(res.serviceData);
			throw new Exception(errorMessage);
		}
	}
}
