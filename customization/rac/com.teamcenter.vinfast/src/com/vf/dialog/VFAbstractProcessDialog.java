package com.vf.dialog;

import java.util.List;
import java.util.Map;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.vinfast.utils.VFProcessDialogProcessInput;
import com.teamcenter.vinfast.utils.ValidationResult;

public abstract class VFAbstractProcessDialog extends AbstractAIFDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public abstract void createAndShowGUI(final Map<String, Map<String, VFProcessDialogProcessInput>> input,
			final TCComponent[] targets, final Map<TCComponent, List<ValidationResult>> partsAndValidationResults,
			final String dialogTitle) throws Exception;

	public interface PostAction {
		public void execute(TCComponent createdProcess) throws Exception;
	}
}
