package com.teamcenter.vinfast.handlers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.tcservices.TcBOMService;
import com.teamcenter.rac.kernel.tcservices.TcResponseHelper;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.dialog.ModuleGroupEnglishDialog;

public class ModuleGroupInputAutomation extends AbstractHandler {

	private static final Logger logger = Logger.getLogger(ModuleGroupInputAutomation.class);

	DataManagementService dataMangementService = null;
	TCSession session = null;
	
	public ModuleGroupInputAutomation() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		dataMangementService = DataManagementService.getService(session);
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();
		TCComponentBOMLine[] bomlines = Arrays.asList(appComps).toArray(new TCComponentBOMLine[0]);
		InterfaceAIFComponent parentLine = app.getTargetComponent();
//		List<TCComponentBOMLine> bomlines = new LinkedList<TCComponentBOMLine>(); 
//		getAllValidChildrenLines((TCComponentBOMLine)parentLine, bomlines);
		
		try {
			ModuleGroupEnglishDialog dlg = new ModuleGroupEnglishDialog((TCComponentBOMLine)parentLine, bomlines, session);
			dlg.setAlwaysOnTop(true);
			dlg.createAndShowGUI();
		} catch (Exception e) {
			logger.error(e);
		}

		return null;
	}
	
	private void getAllValidChildrenLines(TCComponentBOMLine parentLine, List<TCComponentBOMLine> bomlines) {
		try {
			TcResponseHelper response = TcBOMService.expand(session, parentLine);
			TCComponentBOMLine[] childrenLines =  Arrays.asList(response.getReturnedObjects()).toArray(new TCComponentBOMLine[0]);
			//if (isValid(parentLine)) {
				bomlines.add(parentLine);				
			//}
			//System.out.println("##### " + parentLine.getProperty("VL5_module_group"));
			if(childrenLines != null ) {
				for(TCComponentBOMLine childLine : childrenLines) {
					//System.out.println("##### " + childLine.getProperty("VL5_module_group"));
					//if (isValid(childLine)) {
						
					//}
					
					if(childLine.getChildrenCount() > 0) {
						getAllValidChildrenLines(childLine, bomlines);
					} else {
						bomlines.add(childLine);
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

//	private boolean isValid(TCComponentBOMLine parentLine) throws TCException {
//		String value = parentLine.getProperty("VL5_module_group");
//		return value == null || value.length() <= 2;
//	}

}
