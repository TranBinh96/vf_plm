package com.vinfast.bom.familygroup;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;


public class FamilyGroupRuleDeleteHandler extends AbstractHandler {

	private static final Logger logger = Logger.getLogger(FamilyGroupRuleDeleteHandler.class);
	private TCSession session = null;
	public FamilyGroupRuleDeleteHandler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();
		if(appComps.length > 0) {
			for(InterfaceAIFComponent comps : appComps) {
				try {
					if(((TCComponent) comps).isTypeOf("VF4_FG_Rule")) {
						AIFComponentContext[] aif = ((TCComponent)comps).whereReferenced();
						for (int f = 0; f < aif.length; f++) {
							try {
								TCComponent priObj = (TCComponent) aif[f].getComponent();
								if(priObj.getType().compareToIgnoreCase("Folder") == 0) {
									priObj.remove("contents", ((TCComponent)comps));
								}else {
									priObj.remove(aif[f].getContext().toString(), ((TCComponent)comps));
								}
							} catch (Exception e) {
								MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
								continue;
							}
						}
						((TCComponent)comps).delete();
					}
				} catch (TCException e) {
					MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
					return null;
				}
			}
		}
		return null;
	}

}
