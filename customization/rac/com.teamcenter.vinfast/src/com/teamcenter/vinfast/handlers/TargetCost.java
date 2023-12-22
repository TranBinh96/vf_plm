package com.teamcenter.vinfast.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.open.OpenFormDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.utils.CannotCreateCostException;
import com.teamcenter.vinfast.utils.CostUtils;
import com.vf.utils.TCExtension;

public class TargetCost extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(TargetCost.class);
	private TCSession session;

	public TargetCost() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();
		boolean isDBA = TCExtension.checkPermission(new String[] {"dba"}, session);
		
		if(appComps == null || appComps.length == 0) {
			MessageBox.post("Select a part.", "Information", MessageBox.INFORMATION);
			return null;
		}
		
		if(appComps.length > 1) {
			if(!isDBA) {
				MessageBox.post("This feature is applicable to single part only.", "Information", MessageBox.INFORMATION);
				return null;
			} else {
				try {
					for (InterfaceAIFComponent appComp : appComps) {
						TCComponentItemRevision partRev = null;
						if (appComp instanceof TCComponentBOMLine) {
							TCComponentBOMLine bom = (TCComponentBOMLine) app.getTargetComponent();
							partRev = bom.getItemRevision();
						} else {
							partRev = (TCComponentItemRevision) appComp;
						}
						CostUtils costUtil = new CostUtils(session, partRev);
						costUtil.getOrCreateOrSearchAndRelateTargetCost(true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
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

			CostUtils costUtil = new CostUtils(session, partRev);
			boolean isValid = costUtil.validatePartType();

			if (!isValid) {
				MessageBox.post("This feature is not applicable for this part.", "Information", MessageBox.INFORMATION);
				return null;
			}

			boolean isCreateCost = (session.getGroup().toString().contains("Car Program.VINFAST") == false);
			TCComponent targetCost = costUtil.getOrCreateOrSearchAndRelateTargetCost(isCreateCost);
			if (targetCost != null) {
				OpenFormDialog targetCostDialog = new OpenFormDialog((TCComponentForm) targetCost);
				targetCostDialog.setVisible(true);
			} else {
				MessageBox.post((isCreateCost ? "Cost forms of part \"" + partRev.toString() + "\" are invalid." : "The login group is not authorized to create cost forms!") + "\nPlease contact your IT Helpdesk.", "Error", MessageBox.ERROR);
			}
		} catch (CannotCreateCostException ex) {
			logger.error(ex);
			MessageBox.post(ex.getMessage(), "Information", MessageBox.INFORMATION);
		} catch (Exception ex) {
			logger.error(ex);
			MessageBox.post(ex.getMessage(), "Warning", MessageBox.WARNING);
			ex.printStackTrace();
		}
		return null;
	}
}