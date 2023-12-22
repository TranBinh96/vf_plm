package com.teamcenter.vinfast.mhu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.TCExtension;

public class PreferredMPNOpenBOM_Handler extends AbstractHandler {
	public PreferredMPNOpenBOM_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			boolean haveRef = false;
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (targetComp[0] instanceof TCComponentBOMLine) {
				TCComponentBOMLine bomSelect = (TCComponentBOMLine) targetComp[0];
				TCComponentItemRevision itemRev = bomSelect.getItemRevision();
				TCComponent[] refItems = itemRev.getRelatedComponents("EC_reference_item_rel");
				if (refItems.length > 0) {
					if (refItems[0] instanceof TCComponentItem) {
						TCComponentItem refItem = (TCComponentItem) refItems[0];
						TCComponent[] bomviews = refItem.getRelatedComponents("bom_view_tags");
						if (bomviews.length > 0) {
							haveRef = true;
							TCExtension.openComponent(bomviews[0]);
						}
					}
				}
			}
			if (!haveRef) {
				MessageBox.post("No have BOM Reference", "Warning", MessageBox.WARNING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
