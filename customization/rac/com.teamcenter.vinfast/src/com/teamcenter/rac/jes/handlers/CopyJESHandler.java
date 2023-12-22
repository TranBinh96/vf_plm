package com.teamcenter.rac.jes.handlers;

import java.awt.datatransfer.ClipboardOwner;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.spire.ms.System.Collections.Generic.List;
import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.AIFTransferable;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class CopyJESHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		try {
			InterfaceAIFComponent aifComp = AIFUtility.getCurrentApplication().getTargetComponent();
			TCComponentBOMLine bomline =  (TCComponentBOMLine)aifComp;
			AIFComponentContext[] images = bomline.getItemRevision().getRelated("TC_Attaches");
			TCComponent activity = bomline.getRelatedComponent("bl_me_activity_lines");
			if(images.length == 0 && activity == null) {
				MessageBox.post("No Image and activities to copy on selected Item.", "Copy...", MessageBox.WARNING);
			}else {
				List<TCComponent> list = new List<TCComponent>();
				for(AIFComponentContext image : images) {
					if(image.getComponent().getType().equals("Image")) {
						list.add((TCComponent) image.getComponent());
						break;
					}
				}
				if(activity != null) {
					list.add(activity);
				}
				AIFClipboard clipBoard = AIFPortal.getClipboard();
				clipBoard.clearClipboard();
				ClipboardOwner owner = clipBoard.getOwner();
				AIFTransferable transfer = new AIFTransferable(list);
				clipBoard.setContents(transfer, owner);
				MessageBox.post("Successfully copied JES related to Clipboard", "Copy...", MessageBox.INFORMATION);
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
