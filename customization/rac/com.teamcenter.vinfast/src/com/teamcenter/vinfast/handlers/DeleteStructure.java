package com.teamcenter.vinfast.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.delete.DeleteCommand;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class DeleteStructure extends AbstractHandler { 

	public static final Logger logger = Logger.getLogger(DeleteStructure.class);
	private final TCSession session;
	
	public DeleteStructure() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {			
			InterfaceAIFComponent[] selectedComps = AIFUtility.getCurrentApplication().getTargetComponents();
			StringBuffer errBuff = new StringBuffer();
			
			if (selectedComps.length != 1) {
				errBuff.append("This feature is allowed for single selection only.\n");
			}
			
			TCComponent selectedComp = (TCComponent) selectedComps[0];
			
			// { 
				// String compOwner = selectedComp.getReferenceProperty("owning_user").getProperty("user_id");
				// String currentUser = AIFUtility.getCurrentApplication().getSession().getUserName();
				// System.out.println(currentUser);
				// if (compOwner.equals(currentUser) == false) {
					// errBuff.append("This feature is allowed to owner of the selected part only.\n");
				// }
			// }
			
			{
				TCComponent[] children = selectedComp.getReferenceListProperty("bvr_occurrences");
				if (children.length != 0) {
					errBuff.append("This feature is allowed to empty structure only.");
				}
			}
			
			String err = errBuff.toString();
			if (err.isEmpty()) {
				boolean deleteRight = session.getTCAccessControlService().checkPrivilege(selectedComp, "DELETE");
				if (deleteRight) {
					AIFComponentContext[] aif = ((TCComponent)selectedComp).whereReferenced();
					for (int f = 0; f < aif.length; f++) {
						try {
							TCComponent priObj = (TCComponent) aif[f].getComponent();
							if(priObj.getType().compareToIgnoreCase("VisStructureContext") == 0){
								priObj.remove("IMAN_StructureContent", ((TCComponent)selectedComp));
							}
						} catch (Exception e) {
							MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
							continue;
						}
					}
				}
				DeleteCommand delCmd = new DeleteCommand(AIFUtility.getCurrentApplication().getTargetContexts());
				delCmd.executeModal();
			} else {
				MessageBox.post(err, "Invalid Selection", MessageBox.WARNING);
			}
		} catch (Exception e1) {
			logger.error(e1);
			e1.printStackTrace();
		}

		return null;
	}


}
