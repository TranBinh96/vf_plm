package com.teamcenter.rac.jes;

import java.io.File;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class SOSFormGenerate {
	
	private TCSession session = null;
	private TCComponentBOPLine selectedObject = null;
	private TCComponentItemRevision stationRev = null;
	private SOSFormModel sosForm = null;
	
	private File outputFile = null;

	public SOSFormGenerate(TCSession _session, TCComponentBOPLine _selectedObject, TCComponentItemRevision _stationRev, File file) {
		session = _session;
		selectedObject = _selectedObject;
		stationRev = _stationRev;
		sosForm = new SOSFormModel(session);
		outputFile = file;
	}

	public String generateSOS() {
		try {
			generateData();
			sosForm.updateOrCreateSOSExcelToStation(outputFile, stationRev);
			selectedObject.refresh();
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
		return "";
	}

	private void generateData() throws Exception {
		sosForm = new SOSFormModel(session);
		sosForm.setCreateBy(session.getUserName());
		TCComponentItemRevision station = selectedObject.getItemRevision();
		sosForm.setLocationName(station.getProperty("object_name"));
		String stationRevID = station.getProperty("item_revision_id");
		AIFComponentContext[] releasedSOS = station.getRelated("VF4_sos_soft_copy");
		String sosVersion = (releasedSOS != null) ? String.valueOf(releasedSOS.length + 1) : "";
		if (sosVersion.length() == 1) {
			sosVersion = "0" + sosVersion;
		}
		sosForm.setVersion(sosVersion);
		sosForm.setRevisionID(stationRevID);
		
//		String stageName = selectedObject.getItemRevision().getProperty("object_desc");
//		if (!selectedObject.getItemRevision().getProperty("vf5_viet_description").isEmpty())
//			stageName = selectedObject.getItemRevision().getProperty("vf5_viet_description");
//		sosForm.setStageName(stageName);

		TCComponentBOMLine topBomLine = TCExtension.getTopBom(selectedObject);
		if (topBomLine != null)
			sosForm.setVehicleType(topBomLine.getProperty("bl_item_object_name"));

		TCComponentBOMLine shopBomline = TCExtension.getParentBom(selectedObject, 1);
		if (shopBomline != null)
			sosForm.setShopName(shopBomline.getProperty("bl_item_object_name"));

		AIFComponentContext[] childComp = selectedObject.getChildren();
		if (childComp != null && childComp.length > 0) {
			for (int i = 0; i < childComp.length; i++) {
				if (childComp[i].getComponent().getType().equals("Mfg0BvrOperation")) {
					try {
						TCComponentBOPLine opLine = (TCComponentBOPLine) childComp[i].getComponent();
						TCComponentItemRevision operationRevision = opLine.getItemRevision();
						String opeID = operationRevision.getProperty("item_id");
						String opRevID = operationRevision.getProperty("item_revision_id");
						
						String opeName = "";
						TCComponent[] history = operationRevision.getRelatedComponents("VF4_history_jes");
						boolean hasPDF = history != null && history.length > 0;
						String jesVersion = hasPDF ? String.format("%02d", history.length) : opRevID;
						
						DataManagementService dm = DataManagementService.getService(session);
						dm.getProperties(new TCComponent[] { opLine }, new String[] { "bl_me_activity_lines" });
						AIFComponentContext[] context = opLine.getRelated("bl_me_activity_lines");
						TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) context[0].getComponent();
						
						AIFComponentContext[] rootActivity = activityLine.getRelated("me_cl_source");
						TCComponentMEActivity rootActivityComp = (TCComponentMEActivity) rootActivity[0].getComponent();
						String rootActivityVietDesc = rootActivityComp.getProperty("vf4_detail_step_desc");
						String operationVNDesc = operationRevision.getProperty("vf5_viet_description");
						if (rootActivityVietDesc != null && rootActivityVietDesc.length() > 1) {
							opeName = rootActivityVietDesc;
						} else if (operationVNDesc != null && operationVNDesc.length() > 1){
							opeName = operationVNDesc;
						}					
						
						if (opeName.isBlank() == false) {
							double workingTime = 0;
							dm.getProperties(new TCComponent[] { activityLine }, new String[] { "al_activity_duration_time" });
							String time = activityLine.getProperty("al_activity_duration_time");
							if (!time.isEmpty() && StringExtension.isDouble(time)) {
								workingTime = Double.parseDouble(time);
							}

//							if (hasPDF) {
//								opeID += "/" + jesVersion;
//							}
							
							sosForm.addNewActivity(opeName, opeID, workingTime, jesVersion);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public SOSFormModel getSOSFormModel() {
		return sosForm;
	}
	
	public File getSOSExcelFile() {
		File sosExcelFile = null;
		if (sosForm != null) {
			sosExcelFile = sosForm.getSOSExcelFile();
		}
		
		return sosExcelFile;
	}
	
	public static File getSOSExcelFile(TCComponentItemRevision revObj) {
		File excelFile = null;
		TCComponentDataset excel = null;
		try {
			TCComponent[] dataset = revObj.getRelatedComponents("VF4_SOS_Relation");
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