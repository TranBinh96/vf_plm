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
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.vf.dialog.ChangePropertyObjectsDialog;

public class SetManufacturingComponent extends AbstractHandler {

	private static final Logger logger = Logger.getLogger(SetManufacturingComponent.class);

	public SetManufacturingComponent() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();

		TCComponentBOMLine[] bomlines = Arrays.asList(appComps).toArray(new TCComponentBOMLine[0]);
		
		try {
			String type = "VF4_Design";
			String attributeName = "vf4_manu_component";
			String title = "Update " + session.getTypeComponent(type).getPropertyDescriptor(attributeName).getDisplayName();
			
			List<TCComponent> objects = new LinkedList<TCComponent>();
			for (TCComponentBOMLine bomline : bomlines) {
				objects.add(bomline.getItem());
			}
			
			ChangePropertyObjectsDialog dlg = new ChangePropertyObjectsDialog(title, bomlines, objects, type, attributeName, session);
			dlg.setAlwaysOnTop(true);
			dlg.createAndShowGUI();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return null;
	}

}
