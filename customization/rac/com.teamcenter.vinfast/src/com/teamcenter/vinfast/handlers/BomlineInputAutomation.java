package com.teamcenter.vinfast.handlers;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.vf.dialog.BomlineSingleLovAutomationDialog;

public class BomlineInputAutomation extends AbstractHandler {

	private static final Logger logger = Logger.getLogger(BomlineInputAutomation.class);

	public BomlineInputAutomation() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();

		TCComponentBOMLine[] bomlines = Arrays.asList(appComps).toArray(new TCComponentBOMLine[0]);
		
		for(TCComponentBOMLine bomline : bomlines) {
			System.out.println("bomline " + bomline.toString());
		}
		
		try {
			IParameter cmdParamBomlineAttrName = evt.getCommand().getParameter("com.teamcenter.vinfast.commands.BomlineInputAutomation.bomlineAttrName");
			IParameter cmdParamIconPath = evt.getCommand().getParameter("com.teamcenter.vinfast.commands.BomlineInputAutomation.icon");
			String iconPath = cmdParamIconPath != null ? cmdParamIconPath.getName() : "";
			String bomlineAttrName = (String) cmdParamBomlineAttrName.getName();
			String title = "Update " + session.getTypeComponent("BOMLine").getPropertyDescriptor(bomlineAttrName).getDisplayName();
			BomlineSingleLovAutomationDialog dlg = new BomlineSingleLovAutomationDialog(title, bomlineAttrName, iconPath, bomlines, session);
			dlg.setAlwaysOnTop(true);
			dlg.createAndShowGUI();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return null;
	}

}
