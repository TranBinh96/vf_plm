package com.teamcenter.vinfast.car.engineering.view;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.logging.log4j.core.util.FileUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import java.io.File;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RelationAndTypesFilter;
import com.vf.utils.TCExtension;

//import jcifs.smb.NtlmPasswordAuthentication;
//import jcifs.smb.SmbFile;
//import jcifs.smb.SmbFileInputStream;
//import jcifs.smb.SmbFileOutputStream;

public class EmotorFlashingSW_Handler extends AbstractHandler {
	private TCSession session;
	private EmotorFlashingSW_Dialog dlg;
	private TCComponentBOMLine selectedObject = null;
	private LinkedHashMap<String, LinkedList<String>> finalMaterialFolder = null;

	private static String[] GROUP_PERMISSION = { "dba" };
	private static String SHARER_FOLDER = "\\\\10.128.211.42\\f$\\EOLDATA\\EMOTOR Flashing\\";

	private File fAppFile = null;
	private File fBootloaderFile = null;
	private File fFlashDriverFile = null;
	private File rAppFile = null;
	private File rBootloaderFile = null;
	private File rFlashDriverFile = null;

	public EmotorFlashingSW_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (targetComp.length != 1) {
				return null;
			}

			if (targetComp[0] instanceof TCComponentBOMLine)
				selectedObject = (TCComponentBOMLine) targetComp[0];
			else
				return null;

			// get data
			finalMaterialFolder = new LinkedHashMap<>();
			String[] preValues = TCExtension.GetPreferenceValues("VF_EMOTOR_FLASHING_FOLDER_MAPPING", session);
			if (preValues != null) {
				for (String value : preValues) {
					if (value.contains("=")) {
						String[] item = value.split("=");
						if (item.length >= 2) {
							if (finalMaterialFolder.containsKey(item[0])) {
								finalMaterialFolder.get(item[0]).add(item[1]);
							} else {
								LinkedList<String> folderList = new LinkedList<String>();
								folderList.add(item[1]);
								finalMaterialFolder.put(item[0], folderList);
							}
						}
					}
				}
			}
			String contextID = selectedObject.getItem().getPropertyDisplayableValue("Smc0HasVariantConfigContext");
			// Init UI
			dlg = new EmotorFlashingSW_Dialog(new Shell());
			dlg.create();
			dlg.setMessage("Please Get File before Transfer.", IMessageProvider.INFORMATION);
			// -----------
			if (contextID.length() > 4) {
				dlg.txtProgram.setText(contextID.substring(0, 4));
			}
			dlg.btnGetFile.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					onGetFile();
				}
			});

			dlg.btnTransferFile.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					onTransferFile();
				}
			});
			dlg.btnTransferFile.setEnabled(false);
			// -----------
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void onGetFile() {
		fAppFile = null;
		fBootloaderFile = null;
		fFlashDriverFile = null;
		rAppFile = null;
		rBootloaderFile = null;
		rFlashDriverFile = null;

		LinkedList<TCComponentBOMLine> ecuList = expandBOMLines(selectedObject);
		for (TCComponentBOMLine ecuBomline : ecuList) {
			try {
				TCComponentItem itemObj = ecuBomline.getItem();
				TCComponentItemRevision revObj = ecuBomline.getItemRevision();
				String ecuType = itemObj.getPropertyDisplayableValue("vf4_ECU_type");
				if (ecuType.compareTo("EDS_F") == 0 || ecuType.compareTo("EDS_R") == 0) {
					getFileToSend(ecuBomline, ecuType, dlg.txtProgram.getText());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dlg.btnTransferFile.setEnabled(true);
	}

	private void onTransferFile() {
		String finalMaterialEDS_F = dlg.txtFFinalMaterial.getText();
		if (!finalMaterialEDS_F.isEmpty()) {
			if (finalMaterialEDS_F.contains(";")) {
				for (String finalMaterial : finalMaterialEDS_F.split(";")) {
					String folderName = SHARER_FOLDER + finalMaterial + "\\";
					if (fAppFile != null)
						copyFile(fAppFile, dlg.txtFAppFile.getText(), folderName + "App\\");
					if (fBootloaderFile != null)
						copyFile(fBootloaderFile, dlg.txtFBootLoaderFile.getText(), folderName + "BootLoader\\");
					if (fFlashDriverFile != null)
						copyFile(fFlashDriverFile, dlg.txtFFlashDriverFile.getText(), folderName + "FlashDriver\\");
				}
			}
		}

		String finalMaterialEDS_R = dlg.txtRFinalMaterial.getText();
		if (!finalMaterialEDS_R.isEmpty()) {
			if (finalMaterialEDS_R.contains(";")) {
				for (String finalMaterial : finalMaterialEDS_R.split(";")) {
					String folderName = SHARER_FOLDER + finalMaterial + "\\";
					if (rAppFile != null)
						copyFile(rAppFile, dlg.txtRAppFile.getText(), folderName + "App\\");
					if (rBootloaderFile != null)
						copyFile(rBootloaderFile, dlg.txtRBootLoaderFile.getText(), folderName + "BootLoader\\");
					if (rFlashDriverFile != null)
						copyFile(rFlashDriverFile, dlg.txtRFlashDriverFile.getText(), folderName + "FlashDriver\\");
				}
			}
		}

		dlg.setMessage("Transfer file success.", IMessageProvider.INFORMATION);
	}

	private void copyFile(File file, String fileName, String folderPath) {
		try {
			File tempDirectory = new File(folderPath);
//			try {
//				NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("VINGROUP", "administrator", "ABCabc123");
//				SmbFile sFile = new SmbFile("smb://10.128.211.42/f$/EOLDATA/EMOTOR Flashing/PWT30211500/App/", auth);
//				sFile.canWrite();
//				Files.copy(file.toPath(), (new File(folderPath + fileName)).toPath(), StandardCopyOption.REPLACE_EXISTING);
//			} catch (Exception e2) {
//				e2.printStackTrace();
//			}
			if (tempDirectory.exists()) {
				for (File fileExist : tempDirectory.listFiles()) {
					fileExist.delete();
				}
				Files.copy(file.toPath(), (new File(folderPath + fileName)).toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private LinkedList<TCComponentBOMLine> expandBOMLines(TCComponentBOMLine rootLine) {
		LinkedList<TCComponentBOMLine> output = new LinkedList<TCComponentBOMLine>();
		StructureManagementService structureService = StructureManagementService.getService(session);
		ExpandPSOneLevelInfo levelInfo = new ExpandPSOneLevelInfo();
		ExpandPSOneLevelPref levelPref = new ExpandPSOneLevelPref();
		levelInfo.parentBomLines = new TCComponentBOMLine[] { rootLine };
		levelInfo.excludeFilter = "None";
		levelPref.expItemRev = false;
		levelPref.info = new RelationAndTypesFilter[0];
		ExpandPSOneLevelResponse levelResp = structureService.expandPSOneLevel(levelInfo, levelPref);

		if (levelResp.output.length > 0) {
			for (ExpandPSOneLevelOutput levelOut : levelResp.output) {
				for (ExpandPSData psData : levelOut.children) {
					output.add(psData.bomLine);
				}
			}
		}
		return output;
	}

	private void getFileToSend(TCComponentBOMLine tcBomline, String ecuType, String modelName) {
		try {
			String hwPartID = "";
			LinkedList<TCComponentBOMLine> childs = expandBOMLines(tcBomline);
			for (TCComponentBOMLine line : childs) {
				String partType = line.getPropertyDisplayableValue("vf4_software_part_type");
				if (partType.compareTo("SW") == 0) {
					TCComponentItemRevision itemRev = line.getItemRevision();
					TCComponent[] spec = itemRev.getRelatedComponents("IMAN_specification");
					for (TCComponent tcComponent : spec) {
						if (tcComponent instanceof TCComponentDataset) {
							String fileName = tcComponent.getPropertyDisplayableValue("ref_list");
							TCComponent[] namedRef = ((TCComponentDataset) tcComponent).getNamedReferences();
							TCComponentTcFile tcfile = (TCComponentTcFile) namedRef[0];

							if (fileName.contains("EMT_BL_")) {
								if (ecuType.compareTo("EDS_F") == 0) {
									dlg.txtFBootLoaderFile.setText(fileName);
									fBootloaderFile = tcfile.getFmsFile();
								} else {
									dlg.txtRBootLoaderFile.setText(fileName);
									rBootloaderFile = tcfile.getFmsFile();
								}
							} else if (fileName.contains("EMT_AP_")) {
								if (ecuType.compareTo("EDS_F") == 0) {
									dlg.txtFAppFile.setText(fileName);
									fAppFile = tcfile.getFmsFile();
								} else {
									dlg.txtRAppFile.setText(fileName);
									rAppFile = tcfile.getFmsFile();
								}
							}
						}
					}
				} else if (partType.compareTo("HW") == 0) {
					hwPartID = line.getPropertyDisplayableValue("bl_item_item_id");
				} else if (partType.compareTo("FDRV") == 0) {
					TCComponentItemRevision itemRev = line.getItemRevision();
					TCComponent[] spec = itemRev.getRelatedComponents("IMAN_specification");
					for (TCComponent tcComponent : spec) {
						if (tcComponent instanceof TCComponentDataset) {
							String fileName = tcComponent.getPropertyDisplayableValue("ref_list");
							TCComponent[] namedRef = ((TCComponentDataset) tcComponent).getNamedReferences();
							TCComponentTcFile tcfile = (TCComponentTcFile) namedRef[0];

							if (fileName.contains("EMT_FL_")) {
								if (ecuType.compareTo("EDS_F") == 0) {
									dlg.txtFFlashDriverFile.setText(fileName);
									fFlashDriverFile = tcfile.getFmsFile();
								} else {
									dlg.txtRFlashDriverFile.setText(fileName);
									rFlashDriverFile = tcfile.getFmsFile();
								}
							}
						}
					}
				}
			}

			String keyName = modelName + "_" + ecuType + "_" + hwPartID;
			LinkedList<String> finalMaterial = null;
			if (finalMaterialFolder.containsKey(keyName))
				finalMaterial = finalMaterialFolder.get(keyName);
			if (ecuType.compareTo("EDS_F") == 0) {
				dlg.txtFInverterID.setText(hwPartID);
				if (finalMaterial != null)
					dlg.txtFFinalMaterial.setText(String.join(";", finalMaterial));
			} else {
				dlg.txtRInverterID.setText(hwPartID);
				if (finalMaterial != null)
					dlg.txtRFinalMaterial.setText(String.join(";", finalMaterial));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
