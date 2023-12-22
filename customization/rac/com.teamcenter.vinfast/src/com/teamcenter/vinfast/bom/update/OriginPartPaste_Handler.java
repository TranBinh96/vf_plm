package com.teamcenter.vinfast.bom.update;

import java.util.Vector;
import javax.swing.SwingUtilities;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.MessageBox;
import com.vinfast.sc.utilities.Utilities;

public class OriginPartPaste_Handler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				AIFClipboard clipBoard = AIFPortal.getClipboard();
				if(!clipBoard.isEmpty()) {
					Vector<TCComponent> clipboardComponents = clipBoard.toVector();
					String err = "";
					if(clipboardComponents.size() != 1) {
						err = "Please copy only 01 BOMLine (Flex Part BOMLine)";
						throwError(err);
						return;
					}else {
						TCComponent flexLine  = clipboardComponents.elementAt(0);
						//validate flex part
						err = OriginPart_Handler.isValidFlexPart(flexLine);
						if(!err.isEmpty()) {
							throwError(err);
							return;
						}
						
						//validate subgroup
						TCComponent subGroup = Utilities.getComponent();
						err = OriginPart_Handler.isValidSubgroup(subGroup);
						if(!err.isEmpty()) {
							throwError(err);
							return;
						}
						
						//patse origin part to subgroup
						err = OriginPart_Handler.addBOMline(flexLine, subGroup);
						if(!err.isEmpty()) {
							throwError(err);
							return;
						}else {
							MessageBox.post("Copy Origin Part to MBOM successfully", "Info" , MessageBox.INFORMATION);
						}
						
					}
				}else {
					String err = "Nothing in clipboard! Please copy 01 BOMLine (Flex Part BOMLine)";
					throwError(err);
				}
			}
		});
		return null;
	}
	
	private void throwError(String err) {
		if(!err.isEmpty()) {
			MessageBox.post(err, "Error", MessageBox.ERROR);
		}
	}
}
