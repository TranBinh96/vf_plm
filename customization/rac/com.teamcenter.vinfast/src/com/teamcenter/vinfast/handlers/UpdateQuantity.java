package com.teamcenter.vinfast.handlers;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.dialog.UpdateQuantityDialog;

public class UpdateQuantity extends AbstractHandler{
	private static final Logger logger = Logger.getLogger(UpdateQuantity.class);
	
	public UpdateQuantity() {
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
			UpdateQuantityDialog dlg = new UpdateQuantityDialog(new Shell(), bomlines, session);
			dlg.create();
			dlg.setTitle("");
			dlg.open();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return null;
	}
}
