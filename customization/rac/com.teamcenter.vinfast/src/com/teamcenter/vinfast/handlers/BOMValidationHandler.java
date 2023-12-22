package com.teamcenter.vinfast.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.IEclipseContext;

import com.teamcenter.vinfast.handlers.BOMValidationCommand;

public class BOMValidationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			boolean isSaveVariant = true;
			Object appContext = event.getApplicationContext();
			if(appContext instanceof ExpressionContext) {
				IEclipseContext eclip = ((ExpressionContext) appContext).eclipseContext;
				String view = eclip.getLocal("org.eclipse.e4.ui.model.application.ui.basic.MWindowElement").toString();
				isSaveVariant = view.contains("PSEVariantFormulaExpressionEditorView");
			}
			
			if(isSaveVariant) {
		        ICommandService iCommandService = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);  
		        Command command = iCommandService.getCommand("org.eclipse.ui.file.save");
		        command.executeWithChecks(event);
			} else {
				BOMValidationCommand bomvalidationCommand = new BOMValidationCommand();
				bomvalidationCommand.run();	
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
