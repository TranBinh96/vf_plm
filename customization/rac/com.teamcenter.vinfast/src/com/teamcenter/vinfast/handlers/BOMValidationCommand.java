package com.teamcenter.vinfast.handlers;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.AbstractPSEApplication;
import com.teamcenter.vinfast.handlers.BOMValidationOperation;

public class BOMValidationCommand implements Runnable {
	@Override
	public void run() {
		try {
			TCSession tcSession = (TCSession) AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
			final AbstractPSEApplication PSEApplication = (AbstractPSEApplication)AIFUtility.getCurrentApplication();
			TCComponentBOMLine topBOMLine = (TCComponentBOMLine) PSEApplication.getTopBOMLine();
			if (topBOMLine != null) {
				try {
					//System.out.println("Saving BOMWindow...");
					//System.out.println("Top BOMLine - " + topBOMLine.getProperty("bl_line_name"));
					TCComponentBOMWindow tcWindow = (TCComponentBOMWindow)topBOMLine.getCachedWindow();
					BOMValidationOperation bomvalidationOperation = new BOMValidationOperation(tcSession);
					bomvalidationOperation.saveWindow(tcWindow);
					//System.out.println("BOM Window is Saved...");
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
