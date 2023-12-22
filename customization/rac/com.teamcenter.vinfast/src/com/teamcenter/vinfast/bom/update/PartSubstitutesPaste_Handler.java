package com.teamcenter.vinfast.bom.update;

import java.util.LinkedList;
import java.util.Vector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.model.PartSubstitutesPasteModel;

public class PartSubstitutesPaste_Handler extends AbstractHandler {
	private PartSubstitutesPaste_Dialog dlg = null;
	
	public PartSubstitutesPaste_Handler() {
		super();
	}
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		AIFClipboard clipBoard = AIFPortal.getClipboard();
		if (clipBoard == null) {
			String err = "Nothing in clipboard! Please copy bomline";
			throwError(err);
			return null;
		}

		String err = "";
		LinkedList<PartSubstitutesPasteModel> partSubstituesList = new LinkedList<PartSubstitutesPasteModel>();
		@SuppressWarnings("unchecked")
		Vector<TCComponent> clipboardComponents = clipBoard.toVector();
		for (TCComponent item : clipboardComponents) {
			// validate part with substitutes
			err = PartSubstitutes_Handler.isValidSubstitutePart(item);
			partSubstituesList.add(new PartSubstitutesPasteModel((TCComponentBOMLine) item, err));
		}

		// validate subgroup
		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComponents == null || targetComponents.length == 0) {
			throwError("Please select subgroup");
			return null;
		}

		TCComponent subGroup = (TCComponent) targetComponents[0];
		err = PartSubstitutes_Handler.isValidSubgroup(subGroup);
		if (!err.isEmpty()) {
			throwError(err);
			return null;
		}

		// paste origin part to subgroup
		for (PartSubstitutesPasteModel item : partSubstituesList) {
			if (item.getStatus().isEmpty()) {
				err = PartSubstitutes_Handler.addMainPartAndSubstitutes(item.getPart(), subGroup);
				if (!err.isEmpty()) {
					item.setStatus(err);
				} else {
					item.setStatus("Success");
				}
			}
		}
		
		dlg = new PartSubstitutesPaste_Dialog(new Shell());
		dlg.create();
		updateUI(partSubstituesList);
		dlg.open();
		return null;
	}

	private void throwError(String err) {
		if (!err.isEmpty()) {
			MessageBox.post(err, "Error", MessageBox.ERROR);
		}
	}
	
	private void updateUI(LinkedList<PartSubstitutesPasteModel> partSubstituesList) {
		int i = 0;
		for (PartSubstitutesPasteModel item : partSubstituesList) {
			i++;
			TableItem row = new TableItem(dlg.tblUpdate, SWT.NONE);
			row.setText(new String[] { String.valueOf(i), item.getBomline(), item.getSubstitues(), item.getStatus() });
			if (item.getStatus().compareToIgnoreCase("Success") == 0)
				row.setBackground(3, SWTResourceManager.getColor(SWT.COLOR_GREEN));
		}
	}
}
