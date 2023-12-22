package com.teamcenter.integration.arch;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.kernel.TCSession;

public abstract class DialogAbstract extends TitleAreaDialog{

	public DialogAbstract(Shell parentShell) {
		super(parentShell);
	}
	
	protected abstract void setClientSession(TCSession session);
	
	protected abstract Button getBtnTransfer();

	protected abstract Button getBtnPrepare();
	
	public abstract void setTotal(int total);
	
	public abstract void updateprogressBar();
	
	public abstract String getServer();

	public abstract String getServerIP();
	
	public abstract Combo getComboServer();
	
//	public abstract Combo getComboIP();

	public abstract Label getLbProcessStatus();
	
	public abstract void setLabelProcessStatus(String text);
	
	public abstract void setProcessPercent(int percent);
}
