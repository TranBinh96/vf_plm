package com.teamcenter.integration.arch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.SwingUtilities;

import com.teamcenter.integration.model.MCNInformation;
import com.teamcenter.rac.util.StringViewerDialog;
import com.vinfast.integration.model.ActionCommand;
import com.vinfast.integration.model.ProcessStatus;
import com.vinfast.integration.model.ProcessStatus.ProcessUpdateType;
import com.vinfast.integration.model.ReportMessage;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class ReportAbstract extends ProcessorAbstract{
	File logFile = null; 
	Logger log 	=  new Logger();
	StringBuilder strBuilder =  new StringBuilder();
	MCNInformation mcn = null;
	String reportName = "";
	boolean isInited = false;
	boolean isPopup = false;
	boolean isUpdateHeader = false;
	
	public ReportAbstract(String processName, String reportName) {
		super(processName, 1000);
		this.reportName = reportName;
	}

	@Override
	public void onMessage(ModelAbstract msg) {
		switch(msg.getModelType()) {
			case REPORT_MESSAGE:
				onReportUpdate((ReportMessage)msg);
				break;
			case MCN_INFORMATION:
				onMCNInformation((MCNInformation)msg);
				break;
			case ACTION_COMMAND:
				onCommand((ActionCommand)msg);
				break;
			default:
				break;
		}
	}
	
	private void onCommand(ActionCommand cmd) {
		switch(cmd.getCommand()) {
			case COMMAND_INIT_REPORT:
				initReport();
				break;
			case COMMAND_POPUP_REPORT:
				popUpReport();
				break;
			default:
				break;
		}
	}
	
	private void onReportUpdate(ReportMessage msg){
		switch(msg.getType()) {
			case UPDATE_HEADER:
				updateHeader(msg.getData());
				break;
			case UPDATE_BODY_INFO:
				if(!isUpdateHeader) {
					initHeader(msg.getHeader());
				}
				updateBody(msg.getData(), true);
				break;
			case UPDATE_BODY_ERROR:
				if(!isUpdateHeader) {
					initHeader(msg.getHeader());
				}
				updateBody(msg.getData(), false);
				break;
			default:
				break;
		}
	}
	
	private void onMCNInformation(MCNInformation info) {
		mcn = info;
	}
	
	private void initReport() {

		strBuilder = new StringBuilder();
		//init details
		strBuilder.append("<html><body>");
		String[] printValues = new String[] { "User :" + TCHelper.getInstance().session.getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
		Logger.bufferResponse("DETAILS", printValues, strBuilder);
		isInited = true;
		isPopup = false;
	}
	
	private void popUpReport() {
		strBuilder.append("</table>");
		strBuilder.append("</body></html>");
		String logFolder = UIGetValuesUtility.createLogFolder("MCN"+mcn.getMcnID());
		logFile = Logger.writeBufferResponse(strBuilder.toString(), logFolder, reportName);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					StringViewerDialog viewdialog = new StringViewerDialog(logFile);
					viewdialog.setTitle("Transfer Status");
					viewdialog.setSize(600, 400);
					viewdialog.setLocationRelativeTo(null);
					viewdialog.setVisible(true);
					isInited = false;
					isPopup = true;
					isUpdateHeader = false;
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		});
	}
	
	void updateDetails(String[] values) {
		strBuilder.append("<table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">");

		strBuilder.append("<tr>");
		for(String header : values) {

			strBuilder.append("<th><b>"+header+"</b></th>");
		}

		strBuilder.append("</tr></table>");
	}
	
	void updateHeader(String[] values) {
		if(!isInited) {
			initReport();
		}
		isUpdateHeader = true;
		strBuilder.append("<table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border= \"1\" style=\"width:100%\"><tr style=\"background-color:#ccffff;\">");

		for(String header : values) {

			strBuilder.append("<th><b>"+header+"</b></th>");
		}

		strBuilder.append("</tr>");
	}
	
	void updateBody(String[] values,boolean isGood) {
		if(!isInited) {
			initReport();
		}

		if(!isGood){

			strBuilder.append("<tr>");

			for(String header : values) {

				strBuilder.append("<td align=\"center\"><font color=\"red\">"+header+"</font></td>");
			}
			strBuilder.append("</tr>");
		}else {
			strBuilder.append("<tr>");

			for(String header : values) {

				strBuilder.append("<td align=\"center\">"+header+"</td>");
			}
			strBuilder.append("</tr>");
		}
	}
	
	void initHeader(String[] header) {
		updateHeader(header);
	}
	
	File writeBufferToFile(String msg, String logPath) {
		File logFile = null;
		BufferedWriter bw = null;
		try {
			String current_date = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
			logFile = new File(logPath+"//VF_"+reportName+"_"+current_date+".html");

			if(logFile.exists() && !logFile.isDirectory()) {

				bw = new BufferedWriter(new FileWriter(logFile, false));
				bw.write(msg);
				
			}else{
				logFile.createNewFile();
				bw = new BufferedWriter(new FileWriter(logFile, false));
				bw.write(msg);
			}

			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return logFile;
	}
	
	@Override
	protected void onBusiness() {
		publish(PropertyDefines.UI_STORE, new ProcessStatus(ProcessUpdateType.UPDATE_RUNNING_INDICATOR));
	}
	
}
