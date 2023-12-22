package com.teamcenter.vinfast.change;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.Query;
import com.vf.utils.TCExtension;

public class ECRImportDownloadTemplate_Handler extends AbstractHandler {
	private TCSession session;

	public ECRImportDownloadTemplate_Handler() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			downloadTemplateFile();
		} catch (Exception e) {
			MessageBox.post("There are some issues occured while downloading or openning the ECR Excel. Please contact your system administrator for support.", "Error Dialog", MessageBox.ERROR);
			e.printStackTrace();
		}

		return null;
	}

	private void downloadTemplateFile() {
		LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
		parameter.put("Name", "VF_ECRImport_Template");
		parameter.put("Dataset Type", "MS ExcelX");

		TCComponent[] item_list = Query.queryItem(session, parameter, "Dataset...");
		if (item_list != null && item_list.length > 0) {
			File templateFile = TCExtension.downloadDataset(System.getProperty("java.io.tmpdir"), (TCComponentDataset) item_list[0], "MSExcelX", "", session);

			try {
				String templateFileNameWithExt = templateFile.getName();
				String randName = "_" + String.valueOf(new Date().getTime());
				String fileExt = templateFileNameWithExt.substring(templateFileNameWithExt.lastIndexOf("."));
				String templateFileName = templateFileNameWithExt.replace(fileExt, "");

				File newFile = new File(templateFile.getParent(), templateFileName + randName + fileExt);
				Files.move(templateFile.toPath(), newFile.toPath());
				if (templateFile != null)
					Desktop.getDesktop().open(newFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
