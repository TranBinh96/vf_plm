package com.teamcenter.vinfast.handlers;

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
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.ui.handlers.AbstractOpenViewHandler;
import com.teamcenter.rac.ui.views.ActiveWorkspaceView;
import com.teamcenter.rac.ui.views.BrowserSummaryViewPart;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.vns.services.IViewLayoutService;
import com.teamcenter.rac.vns.services.IViewOpenService;
import com.teamcenter.rac.vns.services.IViewSelectionService;

import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

public class PlantFormOpen extends AbstractOpenViewHandler {

	private String plantFormRel = ""; 
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
					MessageBox.post("Selected a wrong object to open Plant Info!\nPlease select an part.", "Error", MessageBox.ERROR);
					return null;
				}
				
				plantFormRel = ((TCComponentBOMLine)(aifComp[0])).getItem().getType().equals("VF3_manuf_part") ? "VF3_plant_rel" : "VF4_plant_form_relation";
				
				TCComponentBOMLine partLine = (TCComponentBOMLine) aifComp[0];
				TCComponent plantForm = (TCComponent) (partLine.getItem().getRelatedComponent(plantFormRel));
				
				copyPosIdToClipboard(partLine);
				
				showJESView(iWorkbenchSite, iViewPart, plantForm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private void copyPosIdToClipboard(TCComponentBOMLine partLine) throws TCException {
		
		String myString = partLine.getProperty("VL5_pos_id");
		StringSelection stringSelection = new StringSelection(myString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private boolean validateSelection(InterfaceAIFComponent[] aifComp) throws TCException {
		if (aifComp.length != 1) {
			return false;
		}

		if (!(aifComp[0] instanceof TCComponentBOMLine)) {
			return false;
		}
		
		if ((((TCComponentBOMLine)(aifComp[0])).getItem().getType().equals("VF3_manuf_part") == false) && (((TCComponentBOMLine)(aifComp[0])).getItem().getType().equals("VF4_Design") == false)) {
			return false;
		}


		return true;
	}

	private void showJESView(IWorkbenchSite iWorkbenchSite, IViewPart iViewPart, TCComponent plantForm) throws PartInitException {
		StructuredSelection newSelection = new StructuredSelection(plantForm);
		final IWorkbenchPage iWorkbenchPage = iWorkbenchSite.getPage();

		final String viewId = "com.teamcenter.rac.ui.views.ActiveWorkspaceView";
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
							ActiveWorkspaceView view = (ActiveWorkspaceView) v.getView(false);
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
