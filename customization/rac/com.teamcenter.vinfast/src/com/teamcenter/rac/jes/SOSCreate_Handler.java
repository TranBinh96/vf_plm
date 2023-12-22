package com.teamcenter.rac.jes;

import java.io.File;

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

//-----------------------
// Plant BOP Revision / Mfg0MEPlantBOPRevision
// |
// |__Process Area Revision / Mfg0MEProcAreaRevision
//    |
//    |__Process Line Revision / Mfg0MEProcLineRevision
//       |
//       |__Process Station Revision / Mfg0MEProcLineRevision
//          |
//          |__Station Revision / MEWorkareaRevision
//          |__Operation Revision / MEOPRevision

public class SOSCreate_Handler extends AbstractHandler {
	private TCSession session = null;
	private TCComponentBOPLine selectedObject = null;
	private TCComponentItemRevision processTag = null;

	public SOSCreate_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		InterfaceAIFComponent[] aifComp = AIFUtility.getCurrentApplication().getTargetComponents();
		try {
			if (!validateSelection(aifComp)) {
				return null;
			}

			processTag = selectedObject.getItemRevision();

			File excelFile = getSOSExcelFile(processTag);
			if (excelFile != null) {
				MessageBox.post("SOS file already created.", "Error", MessageBox.ERROR);
				return null;
			}
			
			SOSFormGenerate sosFormGenerate = new SOSFormGenerate(session, selectedObject, processTag, null);
			String errorMess = sosFormGenerate.generateSOS();
			if(!errorMess.isEmpty()) {
				MessageBox.post("SOS generate unsuccess. Please contact with admin.", errorMess, "Error", MessageBox.ERROR);
			} else {
				MessageBox.post("SOS created successfully", "Success", MessageBox.INFORMATION);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private boolean validateSelection(InterfaceAIFComponent[] aifComp) throws Exception {
		if(aifComp == null || aifComp.length != 1) {
			MessageBox.post("Please select a Station.", "Warning", MessageBox.WARNING);
			return false;
		}
		
		if (!(aifComp[0] instanceof TCComponentBOPLine)) {
			MessageBox.post("Please select a Station.", "Warning", MessageBox.WARNING);
			return false;
		}
		
		selectedObject = (TCComponentBOPLine) aifComp[0];
		if (!(selectedObject.getType().equals("Mfg0BvrProcessStation"))) {
			MessageBox.post("Please select a Station.", "Warning", MessageBox.WARNING);
			return false;
		}

//		String release = selectedObject.getPropertyDisplayableValue("awb0RevisionRelStatusList");
//		if (!release.isEmpty()) {
//			MessageBox.post("Station already release.", "Warning", MessageBox.WARNING);
//			return false;
//		}

		return true;
	}

	private File getSOSExcelFile(TCComponentItemRevision revObj) {
		File excelFile = null;
		TCComponentDataset excel = null;
		try {
			TCComponent[] dataset = revObj.getRelatedComponents("VF4_SOS_Relation");
			if (dataset == null || dataset.length == 0) dataset = revObj.getRelatedComponents("IMAN_specification");
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