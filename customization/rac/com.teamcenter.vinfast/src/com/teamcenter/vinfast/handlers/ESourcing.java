package com.teamcenter.vinfast.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ESourcing extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public ESourcing() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		TCSession session = (TCSession)AIFUtility.getCurrentApplication().getSession();
		String group = session.getCurrentGroup().toString();
		String role = session.getCurrentRole().toString();
		
		if (targetComp == null)
		{
		       System.out.println("Return");
		}
		if(((group.equals("Purchase") && role.equals("CL")) || (group.equals("dba") && role.equals("DBA"))||
				(group.equals("Purchase") && role.equals("Purchasing Coordinator")) || group.equals("Executive"))){
			ESourcingDialog eSrc = new ESourcingDialog(session , targetComp) ;
			eSrc.createDialog() ;
		}else{
			
			MessageBox.post("You are not authorized to perform this operation. Please contact System Admin.", "Error", MessageBox.ERROR);
			return null;
		}
		
		return null;
	}
}
