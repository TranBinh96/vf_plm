package com.teamcenter.vinfast.handlers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.workflow.commands.newprocess.NewProcessOperation;
import com.teamcenter.vinfast.utils.CostUtils;

public class FreezeCost extends AbstractHandler {

	private static final String WF_RELASE_TARGET_COST = "Release_Cost";
	private final TCSession session;
	private final static Logger logger = Logger.getLogger(FreezeCost.class);;
	
	public FreezeCost() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		final InterfaceAIFComponent[] appComps = app.getTargetComponents();
		final Set<TCComponent> selectedCompsList = new HashSet<TCComponent>(); 
		
		
		session.queueOperationAndWait(new AbstractAIFOperation("Collecting un-frozen costs...") {
			
			@Override
			public void executeOperation() throws Exception {
				for (int i = 0; i < appComps.length; i++) {
					if (appComps[i] instanceof TCComponentBOMLine) {
						try {
							selectedCompsList.add((TCComponent)((TCComponentBOMLine) appComps[i]).getItemRevision());
							addChildrens(selectedCompsList, (TCComponentBOMLine) appComps[i]);
						} catch (TCException e) {
							e.printStackTrace();
						}
					} else {
						selectedCompsList.add((TCComponent)appComps[i]);				
					}
				}
			}
		});
		session.setReadyStatus();

		
		try {
			final TCComponent[] selectedComps = getOnlyNotFrozenComps(selectedCompsList);
			if (selectedComps.length == 0) {
				MessageBox.post("All selected part(s)' costs are frozen already.", "Info",
						MessageBox.INFORMATION);
			} else {
				int decision = ConfirmDialog.prompt(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
		                 "Confirmation", "Are you sure to freeze costs of all selected part(s)'s and child-part(s) in all-level?", "SearchTrimLeadingTrailingBlankSpace");
				if (decision == 2) {
					session.queueOperationAndWait(new AbstractAIFOperation("Freezing costs...") {
						@Override
						public void executeOperation() throws Exception {
							try {
								int[] selectedCompsTargetType = new int[selectedComps.length];
								for (int j = 0; j < selectedCompsTargetType.length; j++) {
									selectedCompsTargetType[j] = 1;
									CostUtils costUtil = new CostUtils(session, (TCComponentItemRevision)selectedComps[j]);
									costUtil.searchCostRevAndRelateToPart();
								}
								
								TCComponentTaskTemplateType tCComponentTaskTemplateType = (TCComponentTaskTemplateType)(session.getTypeComponent("EPMTaskTemplate"));
								boolean getFiltered = false;
								boolean includeNotPublished = false;
								TCComponentTaskTemplate[] arrayOfTCComponentTaskTemplate = tCComponentTaskTemplateType.getProcessTemplates(includeNotPublished , getFiltered, null, null, null);
								boolean isFoundFrozenProcess = false;
								for (TCComponentTaskTemplate processTemplate : arrayOfTCComponentTaskTemplate) {
									if (processTemplate.getName().equals(WF_RELASE_TARGET_COST) == true) {
										isFoundFrozenProcess = true;
										NewProcessOperation qq = new NewProcessOperation(session, AIFDesktop.getActiveDesktop(), "Process Name", "Process Desc", processTemplate, selectedComps, selectedCompsTargetType);		
										qq.executeOperation();
										//session.queueOperationAndWait(qq);
										break;
									}
								}
								
								if (isFoundFrozenProcess == false) {
									logger.error("Cannot find process template " + WF_RELASE_TARGET_COST);
								}
							
							} catch (TCException e) {
								logger.error(e);
								e.printStackTrace();
							}
						}
					});
					
					session.setReadyStatus();
				}	
			}
		} catch (TCException e1) {
			logger.error(e1);
			e1.printStackTrace();
		}
		
		return null;
	}

	private TCComponent[] getOnlyNotFrozenComps(Set<TCComponent> selectedCompsList) throws TCException {
		List<TCComponent> fileteredList = new LinkedList<TCComponent>();
		for (TCComponent partRev : selectedCompsList) {
			CostUtils costUtil = new CostUtils(session, (TCComponentItemRevision) partRev);
			TCComponentItemRevision costRev = costUtil.getOrSearchCostAndRemoveWrongCosts();
			if (costRev != null) {
				String releaseStatusList = costRev.getProperty("release_status_list");
				if (releaseStatusList.contains("TCM Released") == false && releaseStatusList.contains("VF4_Sourcing") == false) {
					fileteredList.add(partRev);
				}
			} else {
				logger.error("WARNING: Cannot find valid cost!");
			}
		}
		
		return fileteredList.toArray(new TCComponent[] {});
	}

	private void addChildrens(Set<TCComponent> selectedCompsList, TCComponentBOMLine tcComponentBOMLine) throws TCException {
		AIFComponentContext[] children = tcComponentBOMLine.getChildren();
		for (AIFComponentContext child : children) {
			TCComponentBOMLine childLine = (TCComponentBOMLine)child.getComponent();
			addChildrens(selectedCompsList, childLine);
		}
		selectedCompsList.add((TCComponent)tcComponentBOMLine.getItemRevision());
	}

}
