package com.teamcenter.rac.jes.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class DownloadPFMAndPCP extends AbstractHandler {
	private static final String TYPE = "PFM-PCP"; 
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		
		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
		TCSession session = (TCSession)application.getSession();
		TCComponent[] selected_ProcessLine = (TCComponent[]) application.getTargetComponents();
		@SuppressWarnings("unused")
		DownloadDialog downloadClass =  new DownloadDialog(session, selected_ProcessLine, DownloadPFMAndPCP.TYPE);
//		System.out.println("[DownloadAction] Selected item: " + selected_ProcessLine + ", Type: " + selected_ProcessLine.getType());
//		if(selected_ProcessLine.getType().equals("Mfg0BvrProcessLine")){
//			
//			@SuppressWarnings("unused")
//			DownloadDialog downloadClass =  new DownloadDialog(session, selected_ProcessLine, DownloadPFMAndPCP.TYPE);
//			
//		}else{
//			MessageBox.post("Not a Valid Selection. Please select Process Line object.", "Error", MessageBox.ERROR);
//			return null;
//		}
		
		return null;
	}
}


