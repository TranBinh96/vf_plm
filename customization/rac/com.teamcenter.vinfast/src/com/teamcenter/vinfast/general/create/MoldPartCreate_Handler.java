package com.teamcenter.vinfast.general.create;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class MoldPartCreate_Handler extends AbstractHandler {
	private TCSession session;

	public MoldPartCreate_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		HandlerUtil.getActiveWorkbenchWindowChecked(event);

		try {
			MoldPartCreate_Dialog dialog = new MoldPartCreate_Dialog(new Shell(), session);
			dialog.create();
			dialog.open();
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}
}
