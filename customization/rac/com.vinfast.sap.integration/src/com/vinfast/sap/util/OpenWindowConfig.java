package com.vinfast.sap.util;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.CreateInput;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.ContextGroup;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInput;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextsResponse;

public class OpenWindowConfig {

	public TCComponentBOMLine MBOMTopLine = null;
	public TCComponentBOMLine BOPTopLine = null;
	public TCComponentBOMLine SearchIn = null;
	public TCComponentBOMLine FindIn = null;
	private OpenContextInfo[] createdBOMViews = null;
	private TCSession session = null;
	private String company = "";
	public OpenWindowConfig(TCSession connection, TCComponentItemRevision shopRevision) {
		// TODO Auto-generated constructor stub
		session = connection;
		createContextViews(shopRevision);
	}
	public OpenWindowConfig(TCSession connection, TCComponentItemRevision shopRevision, String company) {
		// TODO Auto-generated constructor stub
		session = connection;
		this.company = company;
		createContextViews(shopRevision);
	}
	private void createContextViews(TCComponentItemRevision shopRevision) {

		try {
			TCComponentItemRevision topParentLine = (company.contentEquals(PropertyDefines.VIN_FAST_ELECTRIC)) ? shopRevision : UIGetValuesUtility.getTopLevelItemRevision(session, shopRevision, PropertyDefines.REVISION_RULE_WORKING);
			com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);
			TCComponentRevisionRule RevisionRule = UIGetValuesUtility.getRevisionRule(PropertyDefines.REVISION_RULE_WORKING, session);
			CreateInput input = new CreateInput();
			input.tagProps.put("RevisionRule", RevisionRule);

			OpenContextInput contextInput = new OpenContextInput();
			contextInput.object = topParentLine;
			contextInput.openAssociatedContexts = true;
			contextInput.openViews = true;
			contextInput.contextSettings = input;

			OpenContextsResponse response = dmService.openContexts(new OpenContextInput[] { contextInput });
			ServiceData sd = response.serviceData;
			if (sd.sizeOfPartialErrors() > 0) {
				MessageBox.post("Error opening MBOM in MPP", SoaUtil.buildErrorMessage(sd), "Access", MessageBox.ERROR);
				return;
			} else {
				ContextGroup[] groups = response.output;
				createdBOMViews = groups[0].contexts;
				for (OpenContextInfo views : createdBOMViews) {
					if (views.context.getType().equals(PropertyDefines.TYPE_BOMLINE)) {
						MBOMTopLine = (TCComponentBOMLine) views.context;
					}
					if (views.context.getType().equals(PropertyDefines.TYPE_BOPLINE)) {
						BOPTopLine = (TCComponentBOMLine) views.context;
					}
				}
				if (MBOMTopLine == null || BOPTopLine == null) {
					MessageBox.post("BOM and BOP linked error.", "Please check BOM and BOP is linked or Contact Teamcenter Admin.", "Link...", MessageBox.ERROR);
					return;
				}
				//Find shop line to Search impacted items in MBOM 
				String shopID = shopRevision.getProperty(PropertyDefines.ITEM_ID);
				if(MBOMTopLine.getProperty(PropertyDefines.BOM_ITEM_ID).equals(shopID) == false) {
					AIFComponentContext[] childLines = MBOMTopLine.getChildren();
					for(AIFComponentContext child : childLines) {
						TCComponent bomline = (TCComponent)child.getComponent();
						if(bomline.getProperty(PropertyDefines.BOM_ITEM_ID).equals(shopID)) {
							SearchIn = (TCComponentBOMLine) bomline;
							break;
						}
					}
				}else {
					SearchIn = MBOMTopLine;
				}
				//Find shop related BOP line to find subgroup parts in BOP
				TCComponentForm topLineMasterForm = (TCComponentForm) shopRevision.getItem().getRelatedComponent(PropertyDefines.TYPE_IMAN_MASTER_FORM);
				String searchPlantModel = topLineMasterForm.getProperty("user_data_2");
				if (searchPlantModel.isEmpty() == false && BOPTopLine.getProperty(PropertyDefines.BOM_ITEM_ID).equals(searchPlantModel) == false) {
					AIFComponentContext[] childLines = BOPTopLine.getChildren();
					for(AIFComponentContext child : childLines) {
						TCComponent bomline = (TCComponent)child.getComponent();
						if(bomline.getProperty(PropertyDefines.BOM_ITEM_ID).equals(searchPlantModel)) {
							FindIn = (TCComponentBOMLine) bomline;
							break;
						}
					}
				}else {
					FindIn = BOPTopLine;
				}
				if (searchPlantModel.isEmpty() == false && FindIn == null) {
					TCComponentItem plantModel = UIGetValuesUtility.findItem(session, searchPlantModel);
					OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(session, plantModel);
					BOPTopLine = (TCComponentBOMLine) createdBOPView[0].context;
					FindIn = BOPTopLine;
				}
				UIGetValuesUtility.setViewReference(session, MBOMTopLine, BOPTopLine);
				
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isLoaded() {
		if (MBOMTopLine == null || BOPTopLine == null || SearchIn == null || FindIn == null) {
			return false;
		}else {
			return true;
		}
	}
	
	public void closeWindowConfig() {
		UIGetValuesUtility.closeAllContext(session, createdBOMViews);
	}
}
