package com.teamcenter.rac.jes.handlers;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.handlers.AbstractOpenViewHandler;
import com.teamcenter.rac.ui.views.BrowserSummaryViewPart;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.vns.services.IViewLayoutService;
import com.teamcenter.rac.vns.services.IViewOpenService;
import com.teamcenter.rac.vns.services.IViewSelectionService;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.JES_EScooter_Logic;

public class JESOpen extends AbstractOpenViewHandler {

	private static TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		IWorkbenchSite iWorkbenchSite = HandlerUtil.getActiveSite(arg0);
		IWorkbenchPart iWorkbenchPart = HandlerUtil.getActivePart(arg0);
		if (iWorkbenchPart instanceof IViewPart) {
			IViewPart iViewPart = (IViewPart) iWorkbenchPart;
			try {
				InterfaceAIFComponent[] aifComp = AIFUtility.getCurrentApplication().getTargetComponents();
				
				boolean is_valid = validateSelection(aifComp) ;
				if (!is_valid)
				{
					MessageBox.post("Selected a wrong object to open JES!\nPlease select an Operation.", "Error", MessageBox.ERROR);
					return null;
				}
				
				TCComponentBOMLine opRevLine = (TCComponentBOMLine) aifComp[0];
				TCComponent jesRev = (TCComponent) (opRevLine.getItemRevision()
						.getRelatedComponent(JES_EScooter_Logic.OPREV_JESREV_RELATION));
				if (jesRev == null) {
					jesRev = createJES(opRevLine);
				}

				showJESView(iWorkbenchSite, iViewPart, jesRev);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private boolean validateSelection(InterfaceAIFComponent[] aifComp) throws TCException {
		if (aifComp.length != 1) {
			return false;
		}

		if (!(aifComp[0] instanceof TCComponentBOMLine)) {
			return false;
		}
		
		if (((TCComponentBOMLine)(aifComp[0])).getItem().getType().equals("MEOP") == false) {
			return false;
		}


		return true;
	}

	private TCComponent createJES(TCComponentBOMLine opRevLine) throws TCException, NotLoadedException {
		TCComponentItemType jesType = (TCComponentItemType) session.getTypeComponent(JES_EScooter_Logic.JESREV_TYPE);
		TCComponent opRev = opRevLine.getItemRevision();
		String opName = opRev.getPropertyDisplayableValue("object_name");
		String opID = opRev.getPropertyDisplayableValue("item_id");

		TCComponentItem jes = jesType.create(opID, "01", JES_EScooter_Logic.JESREV_TYPE, opName, "", null);
		TCComponentItemRevision jesRev = jes.getLatestItemRevision();
		opRev.setRelated(JES_EScooter_Logic.OPREV_JESREV_RELATION, new TCComponent[] { jesRev });
		return jesRev;
	}

	private void showJESView(IWorkbenchSite iWorkbenchSite, IViewPart iViewPart, TCComponent jesRev) throws PartInitException {
		StructuredSelection newSelection = new StructuredSelection(jesRev);
		final IWorkbenchPage iWorkbenchPage = iWorkbenchSite.getPage();

		final String viewId = "com.teamcenter.rac.ui.views.SummaryView";
		IViewOpenService iViewOpenService = getViewOpenService();
		IViewLayoutService iViewLayoutService = getViewLayoutService();
		String str = iViewOpenService.allocateViewSecondaryID(iWorkbenchPage, viewId);
		iViewLayoutService.addSecondaryPlaceHolder(iViewPart, viewId, str);
		IViewPart newSummaryView = iWorkbenchPage.showView(viewId, str, 2);
		boolean bool = getViewDefinitionService().isSecondaryView(newSummaryView);
		if (bool && true && newSummaryView instanceof ISelectionListener) {
			((ISelectionListener) newSummaryView).selectionChanged((IWorkbenchPart) iViewPart,
					(ISelection) newSelection);

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// PROBPLEMS: cannot separate view open by this class with user view =>lock all
					// Summary View
					// TODO: lock only summary view open by this class
					for (IViewReference v : iWorkbenchPage.getViewReferences()) {
						if (v.getId() != null && v.getId().equals(viewId)) {
							BrowserSummaryViewPart view = (BrowserSummaryViewPart) v.getView(false);
							IViewSelectionService selectionService = (view).getVNSSupport()
									.getViewSelectionService();
							selectionService.removeSelectionLockEnabled(view);
							selectionService.setSelectionLockEnabled(view, true);
							view.alignStateOfWidgetsUsedForSelectionLock();
						}
					}

				}
			});
		}
	}
}
