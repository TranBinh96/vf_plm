package com.teamcenter.vinfast.general.workflow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.MessageBox;

public class RequestTranferMM_Handler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		System.out.println("");

		Shell shell = new Shell();
		
		RequestTranferMM_Dialog dialog;
		try {
			
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			
			Set<TCComponent> selectedParts = new HashSet<TCComponent>();
			selectedParts.addAll(Arrays.asList(Arrays.asList(targetComp).toArray(new TCComponent[0])));
			dialog = new RequestTranferMM_Dialog(shell, SWT.SHELL_TRIM, selectedParts );
			
			centerToScreen(shell);
			dialog.open();
			
		} catch (Exception e) {
			MessageBox.post(e);
			e.printStackTrace();
		}
		return null;
	}
	
	public void centerToScreen(Shell shell) {

		Monitor primary = Display.getCurrent().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		shell.setLocation(x, y);
	}

}
