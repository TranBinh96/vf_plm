package com.teamcenter.vinfast.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.FileManagementService;
import com.teamcenter.services.rac.core._2006_03.FileManagement.FileTicketsResponse;
import com.teamcenter.soa.client.FileManagementUtility;


/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ExportDataset extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public ExportDataset() {
	}
	String path = "C:\\Temp\\";
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractAIFUIApplication application= AIFUtility.getCurrentApplication();
		TCSession session=(TCSession) application.getSession();
		TCComponent target_comp=(TCComponent) application.getTargetComponent();

		AIFComponentContext[] datasets;
		try {
			datasets = target_comp.getRelated("IMAN_specification");
			if(datasets.length>0)
			{
				for(AIFComponentContext dataset : datasets){
					TCComponentDataset rapidDataset = (TCComponentDataset)dataset.getComponent();
					System.out.println(rapidDataset.getType());
					if(rapidDataset.getType().equals("VMBFILE")){
						downloadDataset(session, rapidDataset);
						MessageBox.post(" Exported Successfully \n Location : "+path, "Information",MessageBox.INFORMATION);
					}
				}
			}else
			{
				MessageBox.post(" No Dataset Found", "Error",MessageBox.ERROR);
				return  null;
			}
		}catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public File downloadDataset(TCSession session, TCComponentDataset dataset){

		File downloadFile = null;
		try
		{
			SimpleDateFormat formatter  =  new SimpleDateFormat("dd-MM-yyyy_hh_mm_ss");
			String format_date  =  formatter.format(new Date()).replace("-", "_");
			
			FileManagementService fmService = FileManagementService.getService(session);
			FileManagementUtility fileUtility = new FileManagementUtility(session.getSoaConnection());


			TCComponentTcFile[] files = dataset.getTcFiles();
			FileTicketsResponse ticketResp = fmService.getFileReadTickets(files);

			Map<TCComponentTcFile, String> map = ticketResp.tickets;

			for(TCComponentTcFile tcFile : map.keySet())
			{
				String fileName = tcFile.getProperty("original_file_name");		    
				String ticket = map.get(tcFile);

				downloadFile = fileUtility.getTransientFile(ticket, path + format_date +fileName);

			}
		}
		catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return downloadFile;
	}
}
