package com.teamcenter.rac.jes;

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class SOSUpdate_Handler extends AbstractHandler {
	private TCSession session = null;
	private TCComponentBOPLine selectedObject = null;

	LinkedList<Hashtable<String, String>> activitiesList = null;
	String stationName = null;
	String stationRevNumber = null;
	TCComponentItemRevision processTag = null;
	String vehicleModel = "";

	public SOSUpdate_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		InterfaceAIFComponent[] aifComp = AIFUtility.getCurrentApplication().getTargetComponents();
		try {
			String errorMessage = validateSelection(aifComp);
			if (!errorMessage.isEmpty()) {
				MessageBox.post(errorMessage, "Error", MessageBox.ERROR);
				return null;
			}

			selectedObject = (TCComponentBOPLine) aifComp[0];
			processTag = selectedObject.getItemRevision();
			stationRevNumber = processTag.getProperty("item_revision_id");
			stationName = selectedObject.getItemRevision().getProperty("object_name");

			File excelFile = getSOSExcelFile(processTag);
			if (excelFile == null) {
				MessageBox.post("SOS file is not created", "Error", MessageBox.ERROR);
				return null;
			}
			
			SOSFormGenerate sosFormGenerate = new SOSFormGenerate(session, selectedObject, processTag, excelFile);
			String errorMess = sosFormGenerate.generateSOS();
			if(!errorMess.isEmpty()) {
				MessageBox.post("SOS update unsuccess. Please contact with admin.", errorMess, "Error", MessageBox.ERROR);
			} else {
				MessageBox.post("SOS updated successfully", "Success", MessageBox.INFORMATION);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private String validateSelection(InterfaceAIFComponent[] aifComp) throws Exception {
		if (aifComp.length == 0) {
			return "No a Valid Selection. Please select valid object.";
		}

		if (aifComp.length > 1) {
			return "No a Valid Selection. Please select valid object.";
		}

		if (!(aifComp[0] instanceof TCComponentBOPLine)) {
			return "No a Valid Selection. Please select valid object.";
		}

		if (!(aifComp[0].getType().equals("Mfg0BvrProcessStation"))) {
			return "No a Valid Selection. Please select valid object.";
		}

//		TCComponentBOPLine bopLine = (TCComponentBOPLine) aifComp[0];
//		String release = bopLine.getPropertyDisplayableValue("awb0RevisionRelStatusList");
//		if (!release.isEmpty()) {
//			return "Station already release.";
//		}

		return "";
	}

	private File getSOSExcelFile(TCComponentItemRevision revObj) {
		File excelFile = null;
		TCComponentDataset excel = null;
		try {
			TCComponent[] dataset = revObj.getRelatedComponents("VF4_SOS_Relation");
			if (dataset == null || dataset.length == 0) {
				dataset = revObj.getRelatedComponents("IMAN_specification");
			}
			
			for (int i = 0; i < dataset.length; i++) {
				if (dataset[i].getType().equals("MSExcelX")) {
					excel = (TCComponentDataset) dataset[i];
					break;
				}
			}
			if (excel != null) {
				TCComponent[] namedRef = excel.getNamedReferences();
				TCComponentTcFile file = (TCComponentTcFile) namedRef[0];
				excelFile = file.getFmsFile();
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return excelFile;
	}
}