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
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.utils.CannotCreateCostException;
import com.teamcenter.vinfast.utils.CostUtils;

public class PieceCost extends AbstractHandler {

	private final TCSession session;
	private static final Logger logger = Logger.getLogger(PieceCost.class);

	public PieceCost() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();

		TCComponentGroup currentUserGroup = session.getGroup();
		boolean isDBA = currentUserGroup.toString().equals("dba");

		if (appComps != null && appComps.length > 1 && isDBA == false) {
			MessageBox.post("This feature is applicable to single part only.", "Information", MessageBox.INFORMATION);
			return null;
		} else if (appComps != null && appComps.length > 1 && isDBA == true) {
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

					costUtil.getOrCreateOrSearchAndRelatePieceCost(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
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

			CostUtils costUtil = new CostUtils(session, partRev);

			boolean isValid = costUtil.validatePartType();

			if (isValid == false) {
				MessageBox.post("This feature is not applicable for this part.", "Information", MessageBox.INFORMATION);
				return null;
			}

			boolean isCreateCost = session.getGroup().toString().contains("Product Finance") ? true : false;
			TCComponent pieceCost = costUtil.getOrCreateOrSearchAndRelatePieceCost(isCreateCost);
			if (pieceCost != null) {
				OpenFormDialog pieceCostDialog = new OpenFormDialog((TCComponentForm) pieceCost);
				pieceCostDialog.setVisible(true);
			} else {
				MessageBox.post((isCreateCost ? "Cost forms of part \"" + partRev.toString() + "\" are invalid."
						: "The login group is not athorized to create cost forms!")
						+ "\nPlease contact your IT Helpdesk.", "Error", MessageBox.ERROR);
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
