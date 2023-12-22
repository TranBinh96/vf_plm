package com.teamcenter.vinfast.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.utils.CannotCreateCostException;
import com.teamcenter.vinfast.utils.CostUtils;
import com.teamcenter.vinfast.utils.VinfReviseDialog;

public class ReviseCost extends AbstractHandler {

	private final TCSession session;
	private static final Logger logger = Logger.getLogger(ReviseCost.class);

	public ReviseCost() {
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

		try {
			TCComponentItemRevision partRev = null;
			if (appComps[0] instanceof TCComponentItemRevision) {
				partRev = (TCComponentItemRevision) appComps[0];
			} else if (appComps[0] instanceof TCComponentBOMLine) {
				partRev = (TCComponentItemRevision) ((TCComponentBOMLine) appComps[0]).getItemRevision();
			}

//			TCComponentItemRevision[] workingItemRevisions = partRev.getItem().getWorkingItemRevisions();
//			if (workingItemRevisions.length == 1) {
//				partRev = workingItemRevisions[0];
//			} else if (workingItemRevisions.length > 1) {
//				logger.error("WARNING: There are more than one working revisions on object " + partRev.getItem().toString());
//				partRev = workingItemRevisions[workingItemRevisions.length-1];
//			} else {
//				logger.error("WARNING: There are no working revisions on object " + partRev.getItem().toString());
//			}
			final CostUtils costUtil = new CostUtils(session, partRev);
			boolean hasReviseRight = validateOwnerShip(partRev);
			boolean isAllCostRevReleased = validateAllCostRevReleased(partRev, costUtil);
			if (isAllCostRevReleased) {
				if (hasReviseRight) {
					// try getting cost form
					costUtil.getOrCreateOrSearchAndRelateTargetCost(false);
					
					final VinfReviseDialog dlg = new VinfReviseDialog(AIFUtility.getActiveDesktop().getShell(),
							SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.CENTER);
					dlg.open();

					if (dlg.getDecision() == VinfReviseDialog.DECISION_OK) {
						session.queueOperationAndWait(new AbstractAIFOperation("Revising cost...") {
							
							@Override
							public void executeOperation() throws Exception {
								String reviseReason = dlg.getUserInputText();
								costUtil.reviseCost(reviseReason);
								costUtil.searchCostRevAndRelateToPart();
								TCComponent revisedCostRev = costUtil.searchCost();
								if (revisedCostRev != null) {
									revisedCostRev.setStringProperty("vf4_quality_of_finance", dlg.getSelectedQualityOfFinance());
								}
							}
						});
					}
				} else {
					MessageBox.post("Please change your current group into \"" + partRev.getProperty("owning_group").toString()
							+ "\" to revise cost.", "Information", MessageBox.INFORMATION);
				}
			} else {
				MessageBox.post("The selected part's cost is not released. Revising cost is not necessary.", "Information",
						MessageBox.INFORMATION);
			}
		} catch (TCException e1) {
			logger.error(e1);
			e1.printStackTrace();
		} catch (CannotCreateCostException e1) {
			MessageBox.post(e1.getMessage(), "Information", MessageBox.INFORMATION);
			logger.error(e1);
			e1.printStackTrace();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return null;
	}

	private boolean validateOwnerShip(TCComponentItemRevision partRev) throws TCException {
		TCComponentGroup revOwningGroup = (TCComponentGroup) partRev.getReferenceProperty("owning_group");
		TCComponentGroup currentUserGroup = session.getGroup();
		TCComponentUser currentUser = session.getUser();
		TCComponentUser revOwningUser = (TCComponentUser) partRev.getReferenceProperty("owning_user");
		 
		return (currentUserGroup.toString().equals(revOwningGroup.toString()) == true)
				|| (currentUser.toString().equals(revOwningUser.toString()) == true);
	}

	private boolean validateAllCostRevReleased(TCComponentItemRevision partRev, CostUtils costUtil) throws Exception {
		TCComponentItemRevision costRev = costUtil.getOrSearchCostAndRemoveWrongCosts();
		boolean isReleaseAll = true;
		if (costRev != null) {
			TCComponentItem costItem = costRev.getItem();
			TCComponent[] revList = costItem.getReferenceListProperty("revision_list");
			for (TCComponent rev : revList) {
				String releaseStatusList = rev.getProperty("release_status_list");
				if (releaseStatusList.isEmpty()) {
					logger.error("Cannot revise the part's cost \"" + costItem.getProperty("object_string")
							+ "\" as cost-revision \"" + costRev.getProperty("object_string") + "\" is not released.");
					isReleaseAll = false;
					break;
				}
			}
		} else {
			throw new Exception("WARNING: Cannot find valid cost!");
		}

		return isReleaseAll;
	}

}
