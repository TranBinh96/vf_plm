package com.teamcenter.rac.jes.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;

public class DownloadJESAndSOS extends AbstractHandler {
	
	private static final String TYPE = "JES-SOS"; 
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		
		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
		TCSession session = (TCSession)application.getSession();
		InterfaceAIFComponent[] selectedAif_ProcessLine = (InterfaceAIFComponent[])application.getTargetComponents();
		TCComponent[] selected_ProcessLine = new TCComponent[selectedAif_ProcessLine.length];
		for (int i = 0; i < selectedAif_ProcessLine.length; i++) {
			selected_ProcessLine[i] = (TCComponent)selectedAif_ProcessLine[i];
		}
		//System.out.println("[DownloadAction] Selected item: " + selected_ProcessLine + ", Type: " + selected_ProcessLine.getType());
		@SuppressWarnings("unused")
		DownloadDialog downloadClass =  new DownloadDialog(session, selected_ProcessLine, DownloadJESAndSOS.TYPE);
//		if(selected_ProcessLine.getType().equals("Mfg0BvrProcessLine")){
//			
//			@SuppressWarnings("unused")
//			DownloadDialog downloadClass =  new DownloadDialog(session, selected_ProcessLine, DownloadJESAndSOS.TYPE);
//			
//		}else{
//			MessageBox.post("Not a Valid Selection. Please select Process Line object.", "Error", MessageBox.ERROR);
//			return null;
//		}
		
		return null;
	}
}


