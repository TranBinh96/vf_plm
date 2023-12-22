package com.teamcenter.vinfast.handlers;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ESourcingEditAction extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public ESourcingEditAction() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
				TCSession session = (TCSession)AIFUtility.getCurrentApplication().getSession();
				String group = session.getCurrentGroup().toString();
				String role = session.getCurrentRole().toString();
				if(role.contains("EDAG")){
					
				}
				if(((group.equals("Purchase") && role.equals("CL")) || (group.equals("dba") && role.equals("DBA") ||
						(group.equals("Purchase") && role.equals("Buyer"))||(group.equals("Purchase") && role.equals("Senior Buyer")) || group.equals("Executive"))) == false){
					MessageBox.post("You are not authorized to perform this operation. Please contact System Administrator.", "Error", MessageBox.ERROR);
					return;
				}

				if (targetComp != null && targetComp.length !=0 )
				{
					if(validateCheckoutObjects(targetComp) == true){

						int userConf = JOptionPane.showConfirmDialog(null, "Some of the selected objects are Checked-Out changes cannot be saved/modified. Do you want to continue?", 
								"Warning",  JOptionPane.YES_NO_OPTION,	JOptionPane.WARNING_MESSAGE);
						if (userConf == JOptionPane.NO_OPTION)
						{
							return;
						}
					}
				}
				if (targetComp != null && targetComp.length !=0 )
				{

					ESourcingEditDialog eSrcEdit = new ESourcingEditDialog(session , targetComp) ;
					eSrcEdit.createDialog() ;
				}
				else
				{
					MessageBox.post("Please select \"SOURCING PARTS\" and edit.", "Error", MessageBox.ERROR);
					return;
				}
			}
		});
		return null;
	}

	public boolean validateCheckoutObjects(InterfaceAIFComponent[] targetComp){

		for(int i=0; i<targetComp.length; i++ ){

			TCComponentItemRevision targetRev = (TCComponentItemRevision) targetComp[i] ;
			if(targetRev.isCheckedOut() ==  true){
				return true;
			}
		}

		return false;
	}
}
