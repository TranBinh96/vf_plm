package com.teamcenter.vinfast.admin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.Query;

public class UpdateCostItemOwningGroupHandler extends AbstractHandler {
	private TCSession session;

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		if(session.getGroup().toString().compareToIgnoreCase("dba") != 0) {
			MessageBox.post("You don't have permission to use this command", "WARNING", MessageBox.WARNING);
			return null;
		}
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		for(InterfaceAIFComponent selected : targetComp) {
			TCComponentItemRevision costRev = (TCComponentItemRevision) selected;
			String itemId = "";
			try {
				itemId = costRev.getPropertyDisplayableValue("item_id");
			} catch (NotLoadedException e1) {
				e1.printStackTrace();
			}
			try {
				LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
				parameter.put("Item ID", itemId);
//				parameter.put("Type", "VF4_DesignRevision;VF4_BP_DesignRevision;VF3_car_partRevision_VF3_manuf_partRevision");
				TCComponent[] designRev = Query.queryItem(session, parameter, "Latest Design Revision");// getItemList(session, newIDValue);
				if(designRev != null && designRev.length > 0) {
					TCComponentGroup designRevOwningGroup = (TCComponentGroup) designRev[0].getReferenceProperty("owning_group");
					TCComponentUser designRevOwningUser = (TCComponentUser) designRev[0].getReferenceProperty("owning_user");
					AIFComponentContext[] costFormAifs = costRev.getRelated("VF4_SourcingCostFormRela");
					for (AIFComponentContext costFormAif : costFormAifs) {
						((TCComponent)costFormAif.getComponent()).changeOwner(designRevOwningUser, designRevOwningGroup);
					}
					costRev.changeOwner(designRevOwningUser, designRevOwningGroup);
					costRev.getItem().changeOwner(designRevOwningUser, designRevOwningGroup);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		MessageBox.post("Finished", "INFORMATION", MessageBox.INFORMATION);
		return null;
	}
}
