package com.vinfast.sap.util;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.common.NotDefinedException;

import com.teamcenter.rac.util.MessageBox;

public class VersionInfo extends AbstractHandler {

	public VersionInfo() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		
		try {
			IParameter cmdVersion = evt.getCommand().getParameter("com.teamcenter.vinfast.commands.VersionInfo");
			String versionInfo = cmdVersion.getName();
			if (versionInfo.length() > 0) {
				MessageBox.post("VinFast Teamcenter ERP Version: " + versionInfo, "Info", MessageBox.INFORMATION);
			}
		} catch (NotDefinedException e) {
			MessageBox.post("Cannot detect Vinfast Teamcenter ERP Transfer version, please contact IT for support.", "Warning",
					MessageBox.WARNING);
		}		

		return null;
	}

}