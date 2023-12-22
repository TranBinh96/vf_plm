package com.teamcenter.vinfast.bom.update;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.MessageBox;
import com.vinfast.sc.utilities.Utilities;

public class PartSubstitutesRemove_Handler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String err = "";
				TCComponent part = Utilities.getComponent();
				// validate part having substitutes
				err = PartSubstitutes_Handler.isValidSubstitutePart(part);
				if (!err.isEmpty()) {
					throwError(err);
					return;
				}

				// remove all part having same group id with selected part
				err = PartSubstitutes_Handler.removeAllBOMLineInGroup(part);
				if (!err.isEmpty()) {
					throwError(err);
					return;
				} else {
					MessageBox.post("Remove parts from MBOM successfully", "Info", MessageBox.INFORMATION);
				}
			}
		});
		return null;
	}

	private void throwError(String err) {
		if (!err.isEmpty()) {
			MessageBox.post(err, "Error", MessageBox.ERROR);
		}
	}
}
