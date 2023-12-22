package com.teamcenter.vinfast.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.common.NotDefinedException;

import com.teamcenter.rac.util.MessageBox;

public class VersionInfo extends AbstractHandler {

	private final Logger logger;

	public VersionInfo() {
		super();
		logger = Logger.getLogger(this.getClass());
	}

	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		
		try {
			IParameter cmdVersion = evt.getCommand().getParameter("com.teamcenter.vinfast.commands.VersionInfo");
			String versionInfo = (String) cmdVersion.getName();
			if (versionInfo.length() > 0) {
				MessageBox.post("VinFast Teamcenter Version: " + versionInfo, "Info", MessageBox.INFORMATION);
			}
		} catch (NotDefinedException e) {
			MessageBox.post("Cannot detect Vinfast Teamcenter version, please contact IT for support.", "Warning",
					MessageBox.WARNING);
		}		

		return null;
	}
}