package com.teamcenter.rac.jes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.spire.xls.FileFormat;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateRelationsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.Relationship;
import com.teamcenter.services.rac.core._2007_01.DataManagement.CreateFormsOutput;
import com.teamcenter.services.rac.core._2007_01.DataManagement.CreateOrUpdateFormsResponse;
import com.teamcenter.services.rac.core._2007_01.DataManagement.FormInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.vf.utils.ImageValidator;
import com.vf.utils.JES_Logic;
import com.vf.utils.JES_SymbolGetter;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class JESMigration_Thread {
	private String folderPath = "C:\\temp\\";
	private TCSession session = null;
	private DataManagementService dmService = null;
	private TCComponentBOMLine topBOPLine = null;
	private String shopName = "";

	private TCComponentDataset Imagedataset = null;
	private ArrayList<TCComponentMEActivity> activitiesList = null;
	private String TEMP_DIR;
	private JES_Logic logic = new JES_Logic();
	private HashMap<String, String> parentMap;
	private Set<TCComponentBOPLine> stationList = null;

	public JESMigration_Thread(String shopName, TCComponentBOMLine topBOPLine, TCSession session) {
		this.shopName = shopName;
		this.topBOPLine = topBOPLine;
		this.session = session;
		this.dmService = DataManagementService.getService(session);
	}

	public void run() {
		try {
			TEMP_DIR = System.getenv("tmp");
			LinkedList<String[]> valuesMap = new LinkedList<String[]>();
			LinkedList<TCComponentBOPLine> bomLines = expandBom(topBOPLine);
			stationList = new HashSet<TCComponentBOPLine>();
			System.out.print("-----------------------------------------------------------" + bomLines.size());
			for (TCComponentBOPLine bomLine : bomLines) {
				if (!checkHaveActivities(bomLine))
					continue;

				TCComponentItemRevision opeRevision = bomLine.getItemRevision();
				TCComponent[] pdfList = opeRevision.getReferenceListProperty("VF4_jes_soft_copy");
				TCComponent[] jesHitoryForms = opeRevision.getReferenceListProperty("VF4_history_jes");
				boolean hasPDF = pdfList != null && pdfList.length > 0;
				boolean hasJESHitoryForms = jesHitoryForms != null && jesHitoryForms.length > 0;
				
				TCComponent station = bomLine.getReferenceProperty("bl_parent");
				if (station != null && station instanceof TCComponentBOPLine)
					stationList.add((TCComponentBOPLine) station);
				
				//hasPDF = hasJESHitoryForms = false;
				if ( hasPDF && hasJESHitoryForms)
					continue;

				boolean haveExcel = false;
				boolean havePDF = false;
				boolean haveForm = false;
				
				String ID = opeRevision.getProperty("item_id");
				String Rev = opeRevision.getProperty("item_revision_id");
				parentMap = getShopParents(bomLine);
				if (parentMap != null) {
					HashMap<String, String> OperationList = loadOperationDetails(bomLine);
					if (OperationList != null) {
						OperationList.put("RQT_TYPE", "");
						OperationList.put("RQT_ID", "");
						OperationList.put("RQT_NAME", "");
						OperationList.put("RQT_DESC", "");

						try {
							Vector<HashMap<String, String>> equipmentList = loadEquipmentDetails(bomLine);
							Vector<HashMap<String, String>> materialList = loadMaterialDetails(bomLine);

							String operationName = OperationList.get("OP_NAME");
							String jesNameRef = operationName.replace(":", "_").replace("/", "_").replace("\\", "_").replace("*", "_").replace("?", "_").replace("\"", "_").replace("<", "_").replace(">", "_").replace("|", "_");
							File excelFile = populateReport(OperationList, materialList, equipmentList, bomLine.getItemRevision(), activitiesList, TEMP_DIR + "\\" + jesNameRef);
							if (excelFile != null) {
								haveExcel = true;
								String JESName = operationName + " (Ver: " + OperationList.get("OP_HISTORY") + ")";
								String pdfFilePath = createPDF(excelFile);
								TCComponentDataset newDataset = hasPDF ? (TCComponentDataset)pdfList[0] : createDataset(JESName, pdfFilePath);
								if (newDataset != null) {
									TCComponent newForm = hasJESHitoryForms ? jesHitoryForms[0] : createForm(operationName, OperationList, materialList, equipmentList);
									if (newForm == null) {
										newDataset.delete();
									} else {
										String errorMessage = hasPDF ? "" : createRelation(opeRevision, newDataset, "VF4_jes_soft_copy");
										if (errorMessage.isEmpty()) {
											havePDF = true;
											errorMessage = hasJESHitoryForms ? "" : createRelation(opeRevision, newForm, "VF4_history_jes");
											if (!errorMessage.isEmpty()) {
												haveForm = true;
												newDataset.delete();
												newForm.delete();
											}
										} else {
											newDataset.delete();
											newForm.delete();
										}
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				valuesMap.add(new String[] { "", ID, Rev, haveExcel ? "v" : "x", haveForm ? "v" : "x", havePDF ? "v" : "x", "" });
			}

			// update SOS for Station
			boolean isRequiredPDF = true;
			for (TCComponentBOPLine station : stationList) {
								
				TCComponent[] dataset = station.getItemRevision().getRelatedComponents("VF4_sos_soft_copy");
				
				if (dataset != null && dataset.length > 0) {
					for (TCComponent ds : dataset) {
						if (ds.getType().contains("PDF")) {
							isRequiredPDF = false;
						}
					}
				}
				
				isRequiredPDF = true;
				if (isRequiredPDF) {
					String errorMes = updateSOS(station);
					String IDStation = station.getProperty("item_id");
					String RevStation = station.getProperty("item_revision_id");
					valuesMap.add(new String[] { "", IDStation, RevStation, "", "", "", errorMes.isEmpty() ? "v" : errorMes });
				}
				
			}
			print2Text(valuesMap, "JESMigration_" + shopName + "_" + StringExtension.getTimeStamp());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String updateSOS(TCComponentBOPLine station) {
		String errorMess = "";
		try {
			TCComponentItemRevision stationRevision = station.getItemRevision();
			TCComponent[] released = stationRevision.getRelatedComponents("VF4_sos_soft_copy");

			File excelFile = getSOSExcelFile(stationRevision);
			SOSFormGenerate sosFormGenerate = new SOSFormGenerate(session, station, stationRevision, excelFile);
			String errorMessage = sosFormGenerate.generateSOS();
			if (!errorMessage.isEmpty()) {
				errorMess = "Failed to Create/Update SOS. Please contact with admin.";
			} else {
				SOSFormModel sosForm = sosFormGenerate.getSOSFormModel();
				File sosExcelFile = sosFormGenerate.getSOSExcelFile();
				if (sosExcelFile != null) {
					String fileName = sosForm.getLocationName() + " (Ver: " + String.format("%02d", released.length + 1) + ")";
					String SOSFilePath = createPDF(sosExcelFile);
					TCComponentDataset SOSDataset = createDataset(fileName, SOSFilePath);
					if (SOSDataset != null) {
						if (!errorMessage.isEmpty()) {
							SOSDataset.delete();
							errorMess = "Failed to Create/Update SOS. Please contact with admin.";
						} else {
							stationRevision.setRelated("VF4_sos_soft_copy", new TCComponent[] { SOSDataset });
							sosExcelFile.delete();
							new File(SOSFilePath).delete();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return errorMess;
	}

	private LinkedList<TCComponentBOPLine> expandBom(TCComponentBOMLine selectedObject) {
		LinkedList<TCComponentBOPLine> output = new LinkedList<TCComponentBOPLine>();
		try {
			LinkedList<TCComponentBOMLine> bomlines = TCExtension.expandAllBOMLines(selectedObject, session);
			if (bomlines != null) {
				String[] bomProperties = new String[] { "bl_item_object_type", "bl_rev_awp0Item_item_id", "awb0BomLineRevId", "bl_parent" };
				dmService.getProperties(bomlines.toArray(new TCComponentBOMLine[0]), bomProperties);
				for (TCComponentBOMLine bomline : bomlines) {
					String objectType = "";
					try {
						objectType = bomline.getPropertyDisplayableValue("bl_item_object_type");
					} catch (Exception e) {
					}
					if (objectType.compareTo("Operation") == 0 && bomline instanceof TCComponentBOPLine) {
						output.add((TCComponentBOPLine) bomline);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return output;
	}

	private void print2Text(LinkedList<String[]> valuesMap, String fileName) {
		TCExtension.createFolder(folderPath);
		String[] header = new String[] { "Parent", "Operation", "Rev", "Have Excel", "Have Form", "Have PDF" };
		File file = new File(folderPath + fileName + ".csv");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			writer.write(String.join(",", header));
			writer.newLine();

			for (String[] map : valuesMap) {
				writer.write(String.join(",", map));
				writer.newLine();
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkHaveActivities(TCComponentBOMLine operation) {
		try {
			AIFComponentContext[] context = operation.getRelated("bl_me_activity_lines");
			if (context != null && context.length > 0) {
				TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) context[0].getComponent();
				// test case
				AIFComponentContext[] rootActivity = activityLine.getRelated("me_cl_source");
				TCComponentMEActivity rootActivityComp = (TCComponentMEActivity) rootActivity[0].getComponent();
				AIFComponentContext[] contents = rootActivityComp.getRelated("contents");

				for (AIFComponentContext content : contents) {
					if (content.getComponent().getClass().getSimpleName().equals(TCComponentMEActivity.class.getSimpleName())) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private HashMap<String, String> loadOperationDetails(TCComponentBOPLine operationLine) {
		HashMap<String, String> operationDetails = new HashMap<String, String>();
		TCComponent lastModified = null;
		DecimalFormat timeFormat = new DecimalFormat("#.##");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		try {
			TCComponentItemRevision operationRevision = operationLine.getItemRevision();
			TCComponent[] history = operationRevision.getRelatedComponents("VF4_history_jes");
			operationDetails.put("OP_HISTORY", String.format("%02d", history.length + 1));
			String operationRevisionID = operationRevision.getProperty("item_id");
			operationDetails.put("OP_ID", operationRevisionID);
			String operationRevisionRevision = operationRevision.getProperty("item_revision_id");
			operationDetails.put("OP_REVID", operationRevisionRevision);
			String operationRevisionName = operationRevision.getProperty("object_name");
			String operationRevisionVNDesc = operationRevision.getProperty("vf5_viet_description");
			if (operationRevisionVNDesc != null && operationRevisionVNDesc.length() > 1) {
				operationRevisionName = operationRevisionVNDesc;
			}
			operationDetails.put("OP_NAME", operationRevisionName);
			Date operationRevisionCreatedDate = operationRevision.getDateProperty("creation_date");
			operationDetails.put("OP_CREATIONDATE", dateFormat.format(operationRevisionCreatedDate));
			operationDetails.put("OP_WORKSTATION", parentMap.get("Station"));
			DataManagementService dm = DataManagementService.getService(session);
			dm.getProperties(new TCComponent[] { operationLine }, new String[] { "bl_me_activity_lines" });
			TCComponent activities = operationLine.getRelatedComponent("bl_me_activity_lines");
			if (activities != null) {
				TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) activities;
				String activityName = activityLine.getProperty("al_activity_vf4_detail_step_desc");
				if (activityName.isEmpty() == false) {
					operationDetails.put("OP_NAME", activityName);
				}
				String Time = activityLine.getProperty("al_activity_work_time");
				double workTime = Double.parseDouble(Time);
				if (workTime == 0) {
					Time = activityLine.getProperty("al_activity_time_system_unit_time");
					if (Time != null && Time.length() > 0)
						workTime = Double.parseDouble(Time);
				}
				String activityTime = timeFormat.format(workTime);
				operationDetails.put("OP_WORKTIME", activityTime);
				AIFComponentContext[] rootActivity = activityLine.getRelated("me_cl_source");
				if (rootActivity != null) {
					activitiesList = new ArrayList<TCComponentMEActivity>();
					TCComponentMEActivity rootActivityLine = (TCComponentMEActivity) rootActivity[0].getComponent();
					AIFComponentContext[] rootActivityChilds = rootActivityLine.getRelated("contents");
					for (AIFComponentContext content : rootActivityChilds) {
						if (content.getComponent().getClass().getSimpleName().equals(TCComponentMEActivity.class.getSimpleName())) {
							activitiesList.add((TCComponentMEActivity) content.getComponent());
						}
					}
					lastModified = getLastModifiedComponent(operationRevision, rootActivityLine, activitiesList);
				}
			} else {
				operationDetails.put("OP_WORKTIME", "");
			}

			if (lastModified != null) {
				Date lastModifiedDate = lastModified.getDateProperty("last_mod_date");
				operationDetails.put("OP_LASTMODIFIEDDATE", dateFormat.format(lastModifiedDate));
				String lastModifiedUser = lastModified.getProperty("last_mod_user");
				operationDetails.put("OP_LASTMODIFIEDUSER", lastModifiedUser);
			} else {
				Date lastModifiedDate = operationRevision.getDateProperty("last_mod_date");
				operationDetails.put("OP_LASTMODIFIEDDATE", dateFormat.format(lastModifiedDate));
				String lastModifiedUser = operationRevision.getProperty("last_mod_user");
				operationDetails.put("OP_LASTMODIFIEDUSER", lastModifiedUser);
			}

		} catch (TCException e) {
			e.printStackTrace();
			return null;
		}

		return operationDetails;
	}

	private TCComponent getLastModifiedComponent(TCComponentItemRevision operationRevision, TCComponentMEActivity rootActivityLine, ArrayList<TCComponentMEActivity> activitiesList) {
		TCComponent lastModified = null;
		try {
			Date operationRevisionModifiedDate = operationRevision.getDateProperty("last_mod_date");
			Date rootActivityLastModifiedDate = rootActivityLine.getDateProperty("last_mod_date");
			for (TCComponentMEActivity activityChild : activitiesList) {

				Date activityDate = activityChild.getDateProperty("last_mod_date");
				if (rootActivityLastModifiedDate.compareTo(activityDate) < 0) {
					rootActivityLastModifiedDate = activityDate;
					lastModified = activityChild;
				}
			}
			if (rootActivityLastModifiedDate.compareTo(operationRevisionModifiedDate) > 0) {
				return lastModified;
			} else {
				lastModified = operationRevision;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return lastModified;
	}

	private Vector<HashMap<String, String>> loadEquipmentDetails(TCComponentBOPLine operationLine) {
		Vector<HashMap<String, String>> equipmentDetails = new Vector<HashMap<String, String>>();
		try {
			TCComponent[] allEquipments = operationLine.getRelatedComponents("Mfg0used_equipment");
			for (int inx = 0; inx < allEquipments.length; inx++) {
				HashMap<String, String> equipmentList = new HashMap<String, String>();
				equipmentList.put("Name", allEquipments[inx].getProperty("bl_item_object_name"));
				equipmentList.put("Bit", allEquipments[inx].getProperty("VF4_bit"));
				equipmentList.put("Socket", allEquipments[inx].getProperty("VF4_socket"));
				equipmentList.put("Extension", allEquipments[inx].getProperty("VF4_extension"));

				String backupTools = allEquipments[inx].getProperty("VF3_note");
				equipmentList.put("BackupTools", backupTools);

				String substituteList = allEquipments[inx].getProperty("bl_substitute_list");
				equipmentList.put("SubstitueList", substituteList);

				equipmentDetails.add(equipmentList);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return equipmentDetails;
	}

	private Vector<HashMap<String, String>> loadMaterialDetails(TCComponentBOPLine operationLine) {
		Vector<HashMap<String, String>> materialDetails = new Vector<HashMap<String, String>>();
		try {
			TCComponent[] allMaterials = operationLine.getRelatedComponents("Mfg0all_material");
			List<AIFComponentContext> consumableParts = Arrays.asList(operationLine.getChildren("Mfg0consumed_material"));

			for (int inx = 0; inx < allMaterials.length; inx++) {
				HashMap<String, String> materialList = new HashMap<String, String>();
				TCComponentItemRevision partRev = ((TCComponentBOMLine) allMaterials[inx]).getItemRevision();
				if (allMaterials[inx].getProperty("bl_quantity").equals("")) {
					materialList.put("Quantity", "1");
				} else {
					materialList.put("Quantity", allMaterials[inx].getProperty("bl_quantity"));
				}

				String materialType = consumableParts.toString().contains(partRev.toString()) ? "" : "";

				materialList.put("ID", allMaterials[inx].getProperty("bl_item_item_id"));
				String objectType = allMaterials[inx].getProperty("fnd0bl_line_object_type");
				if (objectType.compareToIgnoreCase("VF3_Scooter_partRevision") == 0) {
					materialList.put("Name", allMaterials[inx].getProperty("vf3_viet_desc"));
				} else {
					materialList.put("Name", allMaterials[inx].getProperty("bl_item_object_name"));
				}
				String torque = allMaterials[inx].getProperty("VL5_torque_inf");
				if (torque.trim().length() <= 2) {
					torque = allMaterials[inx].getProperty("VF3_torque_info");
					if (torque.trim().length() <= 2) {
						torque = allMaterials[inx].getProperty("VF3_torque_info");
					}
				}
				materialList.put("Torque", torque);
				materialList.put("MaterialType", materialType);
				materialDetails.add(materialList);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return materialDetails;
	}

	private File populateReport(HashMap<String, String> operationList, Vector<HashMap<String, String>> materialVec, Vector<HashMap<String, String>> equipmentVec, TCComponentItemRevision operationRev, List<TCComponentMEActivity> activityComps, String outputFilePathWithoutExtension) {
		File updatefile = null;
		try {
			updatefile = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET, Query.QUERY_JES_ENTRY_DATASET_NAME, logic.JES_DATASET_TEMPLATE, outputFilePathWithoutExtension);
			if (updatefile == null) {
				return null;
			}
			InputStream fileOut = new FileInputStream(updatefile);
			XSSFWorkbook workbook = new XSSFWorkbook(fileOut);
			XSSFSheet worksheet = workbook.getSheetAt(0);

			// HEADER ROW
			updatefile.setWritable(true);
			if (worksheet.getRow(1) == null) {
			} else {
			}

			// ----Created Time Row
			XSSFRow createdTimeRow = null;
			if (worksheet.getRow(1) == null) {
				createdTimeRow = worksheet.createRow(1);
			} else {
				createdTimeRow = worksheet.getRow(1);
			}

			XSSFCell createdTimeCell = null;
			createdTimeCell = createdTimeRow.getCell(CellReference.convertColStringToIndex("S"));
			if (createdTimeCell == null) {
				createdTimeCell = createdTimeRow.createCell(CellReference.convertColStringToIndex("S"));
			}
			createdTimeCell.setCellValue(operationList.get("OP_CREATIONDATE"));

			// -------Modified Date ROW
			XSSFRow modifyDateRow = null;
			if (worksheet.getRow(3) == null) {
				modifyDateRow = worksheet.createRow(3);
			} else {
				modifyDateRow = worksheet.getRow(3);
			}

			XSSFCell modifyDateCell = null;
			modifyDateCell = modifyDateRow.getCell(CellReference.convertColStringToIndex("S"));
			if (modifyDateCell == null) {
				modifyDateCell = modifyDateRow.createCell(CellReference.convertColStringToIndex("S"));
			}
			modifyDateCell.setCellValue(operationList.get("OP_LASTMODIFIEDDATE"));

			// ----Modified By Row
			XSSFRow modifiedByRow = null;
			if (worksheet.getRow(2) == null) {
				modifiedByRow = worksheet.createRow(2);
			} else {
				modifiedByRow = worksheet.getRow(2);
			}

			XSSFCell modifiedByCell = null;
			modifiedByCell = modifiedByRow.getCell(CellReference.convertColStringToIndex("S"));
			if (modifiedByCell == null) {
				modifiedByCell = modifiedByRow.createCell(CellReference.convertColStringToIndex("S"));
			}
			modifiedByCell.setCellValue(operationList.get("OP_LASTMODIFIEDUSER"));

			updateRevNumber(workbook, worksheet, operationList.get("OP_HISTORY"));

			try {
				File operationImage = getOperationImage(operationRev);
				if (operationImage != null) {
					drawPicture(workbook, logic.PICTURE_COL_TOP_INDEX, logic.PICTURE_ROW_TOP_INDEX, logic.PICTURE_COL_BOTTOM_INDEX, logic.PICTURE_ROW_BOTTOM_INDEX, operationImage);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			fillDetailSteps(activityComps, worksheet);

			// Operation ROW
			XSSFRow operationRow = null;
			if (worksheet.getRow(10) == null) {
				operationRow = worksheet.createRow(10);
			} else {
				operationRow = worksheet.getRow(10);
			}

			XSSFRow jobElementRow = null;
			if (worksheet.getRow(7) == null) {
				jobElementRow = worksheet.createRow(7);
			} else {
				jobElementRow = worksheet.getRow(7);
			}

			XSSFRow shopRow = worksheet.getRow(3);
			XSSFCell shopCell = shopRow.getCell(CellReference.convertColStringToIndex("I"));
			shopCell.setCellValue(parentMap.get("Shop"));

			XSSFRow modelRow = worksheet.getRow(2);
			XSSFCell modelCell = modelRow.getCell(CellReference.convertColStringToIndex("I"));
			modelCell.setCellValue(parentMap.get("Program"));

			XSSFCell nameCell = jobElementRow.getCell(CellReference.convertColStringToIndex("A"));
			nameCell.setCellValue(operationList.get("OP_NAME").toUpperCase());

			XSSFCell IDCell = operationRow.getCell(CellReference.convertColStringToIndex("G"));
			IDCell.setCellValue(operationList.get("OP_ID").toUpperCase());

			XSSFCell timeCell = operationRow.getCell(CellReference.convertColStringToIndex("I"));
			timeCell.setCellValue(operationList.get("OP_WORKTIME").toUpperCase());

			XSSFCell stationCell = operationRow.getCell(CellReference.convertColStringToIndex("J"));
			stationCell.setCellValue(operationList.get("OP_WORKSTATION").toUpperCase());

			writeHeader(workbook, worksheet, logic.JES_TEMPLATE_PART_DATA_INDEX_HEADER_ROW);

			int rowCount = worksheet.getLastRowNum();
			for (int count = logic.JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW; count <= rowCount; count++) {
				if (count == 32)
					continue;
				XSSFRow row = worksheet.getRow(count);
				if (row == null)
					continue;

				XSSFCell A = row.getCell(CellReference.convertColStringToIndex("A"));
				if (A == null)
					continue;
				A.setCellValue("");

				XSSFCell B = row.getCell(CellReference.convertColStringToIndex("B"));
				if (B == null)
					continue;
				B.setCellValue("");

				XSSFCell E = row.getCell(CellReference.convertColStringToIndex("E"));
				if (E == null)
					continue;
				E.setCellValue("");

				XSSFCell J = row.getCell(CellReference.convertColStringToIndex("J"));
				if (J == null)
					continue;
				J.setCellValue("");

				XSSFCell K = row.getCell(CellReference.convertColStringToIndex("K"));
				if (K == null)
					continue;
				K.setCellValue("");
			}

			XSSFCellStyle partDataCellStyle = logic.createCellStyleForPartData(workbook);
			// MATERIAL LIST
			boolean hasSecondPage = false;
			for (int i = 0; i < materialVec.size(); i++) {
				HashMap<String, String> materialTable = materialVec.get(i);
				XSSFRow materialRow = null;
				int rowIndex;
				if (i < logic.JES_TEMPLATE_MAX_PART_DATA_ROWS_IN_FIRST_PAGE) {
					rowIndex = logic.JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW + i;
					materialRow = worksheet.createRow(rowIndex);
				} else {
					hasSecondPage = true;
					rowIndex = logic.JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW + 1 + i;
					materialRow = worksheet.createRow(rowIndex);
				}

				XSSFCellStyle serialCellStyle = createStepCellStyle(workbook);

				XSSFCell serialCell = materialRow.createCell(CellReference.convertColStringToIndex("A"));
				serialCell.setCellValue(i + 1);
				serialCell.setCellStyle(serialCellStyle);

				XSSFCell mNameCell = materialRow.createCell(CellReference.convertColStringToIndex("B"));
				for (int j = 1; j <= 3; j++) {
					mNameCell = materialRow.createCell(j, CellType.STRING);
					mNameCell.setCellType(CellType.STRING);
					mNameCell.setCellStyle(partDataCellStyle);
					mNameCell.setCellValue(materialTable.get("ID").toUpperCase());
				}
				worksheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 3));

				XSSFCell mIDCell = materialRow.createCell(CellReference.convertColStringToIndex("E"));
				for (int j = 4; j <= 8; j++) {
					mIDCell = materialRow.createCell(j, CellType.STRING);
					mIDCell.setCellType(CellType.STRING);
					mIDCell.setCellStyle(partDataCellStyle);
					mIDCell.setCellValue(materialTable.get("Name").toUpperCase());
				}
				worksheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 8));

				XSSFCell mTimeCell = materialRow.createCell(CellReference.convertColStringToIndex("J"));
				mTimeCell.setCellType(CellType.NUMERIC);
				mTimeCell.setCellStyle(partDataCellStyle);
				String qutyStr = materialTable.get("Quantity");
				Double quty = Double.valueOf(qutyStr);
				mTimeCell.setCellValue(quty);

				XSSFCell mTorqueCell = materialRow.createCell(CellReference.convertColStringToIndex("K"));
				for (int j = 10; j <= 13; j++) {
					mTorqueCell = materialRow.createCell(j, CellType.STRING);
					mTorqueCell.setCellType(CellType.STRING);
					mTorqueCell.setCellStyle(partDataCellStyle);
					mTorqueCell.setCellValue(materialTable.get("Torque").toUpperCase());
				}
				worksheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 10, 13));

				for (int j = 14; j <= 18; j++) {
					XSSFCell cell = materialRow.createCell(j, CellType.STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellStyle(partDataCellStyle);
					cell.setCellValue(materialTable.get("MaterialType"));
				}
				worksheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 14, 18));

				// Backup tool
				for (int j = 19; j <= 22; j++) {
					XSSFCell cell = materialRow.createCell(j, CellType.STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellStyle(partDataCellStyle);
				}
				worksheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 19, 22));
			}

			// EQUIPMENT LIST
			for (int i = 0; i < equipmentVec.size(); i++) {

				HashMap<String, String> equipmentTable = equipmentVec.get(i);
				int rowIndex = -1;
				XSSFRow equipmentRow = null;
				if (i < logic.JES_TEMPLATE_MAX_PART_DATA_ROWS_IN_FIRST_PAGE) {
					rowIndex = logic.JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW + i;
					equipmentRow = worksheet.getRow(rowIndex) != null ? worksheet.getRow(rowIndex) : worksheet.createRow(rowIndex);
				} else {
					hasSecondPage = true;
					rowIndex = logic.JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW + 1 + i;
					equipmentRow = worksheet.getRow(logic.JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW + 1 + i) != null ? worksheet.getRow(logic.JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW + 1 + i) : worksheet.createRow(logic.JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW + 1 + i);
				}

				boolean isNoMaterialRow = (i >= materialVec.size());
				if (isNoMaterialRow) {
					XSSFCellStyle serialCellStyle = createStepCellStyle(workbook);
					XSSFCell serialCell = equipmentRow.createCell(CellReference.convertColStringToIndex("A"));
					serialCell.setCellValue(i + 1);
					serialCell.setCellStyle(serialCellStyle);

					XSSFCell mNameCell = equipmentRow.createCell(CellReference.convertColStringToIndex("B"));
					for (int j = 1; j <= 3; j++) {
						mNameCell = equipmentRow.createCell(j, CellType.STRING);
						mNameCell.setCellType(CellType.STRING);
						mNameCell.setCellStyle(partDataCellStyle);
					}
					worksheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 3));

					XSSFCell mIDCell = equipmentRow.createCell(CellReference.convertColStringToIndex("E"));
					for (int j = 4; j <= 8; j++) {
						mIDCell = equipmentRow.createCell(j, CellType.STRING);
						mIDCell.setCellType(CellType.STRING);
						mIDCell.setCellStyle(partDataCellStyle);
					}
					worksheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 8));

					XSSFCell mTimeCell = equipmentRow.createCell(CellReference.convertColStringToIndex("J"));
					mTimeCell.setCellType(CellType.NUMERIC);
					mTimeCell.setCellStyle(partDataCellStyle);

					XSSFCell mTorqueCell = equipmentRow.createCell(CellReference.convertColStringToIndex("K"));
					for (int j = 10; j <= 13; j++) {
						mTorqueCell = equipmentRow.createCell(j, CellType.STRING);
						mTorqueCell.setCellType(CellType.STRING);
						mTorqueCell.setCellStyle(partDataCellStyle);
					}
					worksheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 10, 13));

					for (int j = 14; j <= 18; j++) {
						XSSFCell cell = equipmentRow.createCell(j, CellType.STRING);
						cell.setCellType(CellType.STRING);
						cell.setCellStyle(partDataCellStyle);
					}
					worksheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 14, 18));

					// Backup tool
					for (int j = 19; j <= 22; j++) {
						XSSFCell cell = equipmentRow.createCell(j, CellType.STRING);
						cell.setCellType(CellType.STRING);
						cell.setCellStyle(partDataCellStyle);
					}
					worksheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 19, 22));
				}

				String Name = equipmentTable.get("Name");
				String Bit = equipmentTable.get("Bit");
				String Socket = equipmentTable.get("Socket");
				String Extension = equipmentTable.get("Extension");
				String backupTools = equipmentTable.get("BackupTools");

				String finalString = "";
				finalString = Name;
				if (!Bit.equals("")) {
					finalString = finalString + " | B-" + Bit;
				}
				if (!Socket.equals("")) {
					finalString = finalString + " | S- " + Socket;
				}
				if (!Extension.equals("")) {
					finalString = finalString + " | Ex- " + Extension;
				}

				for (int j = CellReference.convertColStringToIndex("O"); j <= CellReference.convertColStringToIndex("S"); j++) {
					XSSFCell cell = equipmentRow.createCell(j, CellType.STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellStyle(partDataCellStyle);
					cell.setCellValue(finalString.toUpperCase());
				}
				for (int j = CellReference.convertColStringToIndex("T"); j <= CellReference.convertColStringToIndex("W"); j++) {
					XSSFCell cell = equipmentRow.createCell(j, CellType.STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellStyle(partDataCellStyle);
					cell.setCellValue(backupTools);
				}
			}

			if (hasSecondPage) {
				worksheet.setAutobreaks(false);
				int secondHeaderRowIndex = logic.JES_TEMPLATE_PART_DATA_INDEX_DATA_ROW + logic.JES_TEMPLATE_MAX_PART_DATA_ROWS_IN_FIRST_PAGE;
				worksheet.setRowBreak(secondHeaderRowIndex - 1);
				writeHeader(workbook, worksheet, secondHeaderRowIndex);
			}

			fileOut.close();
			FileOutputStream output_file = new FileOutputStream(updatefile);
			workbook.write(output_file);
			output_file.close();
			updatefile.setWritable(false);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return updatefile;
	}

	private String createPDF(File Flile) {
		String pdfFile = Flile.getAbsolutePath().replace("xlsx", "pdf");
		com.spire.xls.Workbook workbook = new com.spire.xls.Workbook();
		workbook.loadFromFile(Flile.getAbsolutePath());
		// workbook.getConverterSetting().setSheetFitToPage(true);
		workbook.saveToFile(pdfFile, FileFormat.PDF);
		return pdfFile;
	}

	private TCComponentDataset createDataset(String datasetName, String namedReferenceFile) {
		TCComponentDataset newDataset = null;
		try {
			TCComponentDatasetDefinitionType tcDatasetDefinitionType = (TCComponentDatasetDefinitionType) session.getTypeComponent("DatasetType");
			TCComponentDatasetDefinition datasetDefinition = tcDatasetDefinitionType.find("PDF");
			NamedReferenceContext[] namedReferenceContext = datasetDefinition.getNamedReferenceContexts();
			String namedRef = namedReferenceContext[0].getNamedReference();
			String[] type = { namedRef };
			TCComponentDatasetType datasetTypeComponent = (TCComponentDatasetType) session.getTypeComponent("Dataset");
			newDataset = datasetTypeComponent.create(datasetName, datasetName, datasetDefinition.toString());
			newDataset.setFiles(new String[] { namedReferenceFile }, type);
		} catch (TCException e) {
			e.printStackTrace();
		}
		return newDataset;
	}

	private TCComponent createForm(String formName, HashMap<String, String> operationList,Vector<HashMap<String, String>> materialList,Vector<HashMap<String, String>> equipmentList) {

		TCComponentForm formTag = null;
		String operationString = operationList.get("OP_NAME");
		HashMap<String, String[]> propMap = new HashMap<String, String[]>();
		propMap.put("vf4_operation_id", new String[] {operationList.get("OP_ID")});
		propMap.put("object_name", new String[] {operationString});
		propMap.put("vf4_location", new String[] {operationList.get("OP_WORKSTATION")});
		propMap.put("vf4_vehicle_type", new String[] {parentMap.get("Program")});
		propMap.put("vf4_shop", new String[] {parentMap.get("Shop")});
		propMap.put("vf4_name_operation", new String[] {operationString});
		propMap.put("vf4_model", new String[] {parentMap.get("Program")});
		propMap.put("vf4_prepared_by", new String[] {operationList.get("ModifiedBy")});
		propMap.put("vf4_jes_version", new String[] {operationList.get("OP_HISTORY")});
		propMap.put("vf4_change_request_desc", new String[] {operationList.get("RQT_NAME")});
		propMap.put("vf4_change_request_item", new String[] {operationList.get("RQT_ID")});
		propMap.put("vf4_change_request_type", new String[] {operationList.get("RQT_TYPE")});
		propMap.put("object_desc", new String[] {operationList.get("RQT_DESC")});
		propMap.put("vf4_image", new String[] {Imagedataset.getUid()});
		CreateResponse response = savePartDetails(dmService, materialList, equipmentList);
		if(response.serviceData.sizeOfPartialErrors() > 0) {
			SoaUtil.buildErrorMessage(response.serviceData);
			return formTag;
		}else {
			CreateOut[] output = response.output;
			String[] partInfo = new String[output.length];
			for(int i=0;i<output.length; i++) {
				CreateOut createdObject = response.output[i];
				partInfo[i] = createdObject.objects[0].getUid();
			}
			propMap.put("vf4_jes_parts_list", partInfo);

		}
		response = saveStepDetails(dmService);
		if(response.serviceData.sizeOfPartialErrors() > 0) {
			SoaUtil.buildErrorMessage(response.serviceData);
			return formTag;
		}else {
			CreateOut[] output = response.output;
			String[] detailsInfo = new String[output.length];
			for(int i=0;i<output.length; i++) {
				CreateOut createdObject = response.output[i];
				detailsInfo[i] = createdObject.objects[0].getUid();
			}
			propMap.put("vf4_jes_detail_steps_table", detailsInfo);
		}

		FormInfo formInfo =  new FormInfo();
		formInfo.name = operationList.get("OP_ID")+"/"+operationList.get("OP_REVID");
		formInfo.formType = "VF4_jes_history_form";
		formInfo.saveDB = true;
		formInfo.attributesMap = propMap;

		CreateOrUpdateFormsResponse updateResponse = dmService.createOrUpdateForms(new FormInfo[] {formInfo});
		if(updateResponse.serviceData.sizeOfPartialErrors() > 0) {
			SoaUtil.buildErrorMessage(response.serviceData);
		}else {
			if (updateResponse.serviceData.sizeOfCreatedObjects() > 0) {
				CreateFormsOutput[] outputs = updateResponse.outputs;
				formTag = (TCComponentForm) outputs[0].form;
			}
		}
		return formTag;
	}

	private CreateResponse savePartDetails(DataManagementService dmService, Vector<HashMap<String, String>> materialList,Vector<HashMap<String, String>> equipmentList) {

		CreateResponse response = null;
		try {
			if(materialList.size() >= equipmentList.size()) {
				CreateIn[] input = new CreateIn[materialList.size()];
				for(int i = 0; i<materialList.size(); i++) {
					HashMap<String, String> materialMap = materialList.get(i);
					Map<String, String> propMaps = new HashMap<String, String>();
					if(i<equipmentList.size() && equipmentList.size() != 0) {
						HashMap<String, String> equipmentMap = equipmentList.get(i);
						propMaps.put("vf4_tools_comsumables", equipmentMap.get("SubstitueList"));
						propMaps.put("vf4_backup_tool", equipmentMap.get("BackupTools"));
						propMaps.put("vf4_step_id", String.valueOf(i));
						propMaps.put("vf4_part_name", materialMap.get("Name"));
						propMaps.put("vf4_torque", materialMap.get("Torque"));
						propMaps.put("vf4_quantity", materialMap.get("Quantity"));
						propMaps.put("vf4_part_number", materialMap.get("ID"));
					}else {
						propMaps.put("vf4_tools_comsumables", "");
						propMaps.put("vf4_backup_tool", "");
						propMaps.put("vf4_step_id", String.valueOf(i));
						propMaps.put("vf4_part_name", materialMap.get("Name"));
						propMaps.put("vf4_torque", materialMap.get("Torque"));
						propMaps.put("vf4_quantity", materialMap.get("Quantity"));
						propMaps.put("vf4_part_number", materialMap.get("ID"));
					}

					CreateInput data = new CreateInput();
					data.boName = "VF4_jes_parts_list";
					data.stringProps = propMaps;
					input[i] = new CreateIn();
					input[i].data = data;
				}
				return dmService.createObjects(input);
			}else {
				CreateIn[] input = new CreateIn[equipmentList.size()];
				for(int i = 0; i<equipmentList.size(); i++) {

					HashMap<String, String> equipmentMap = equipmentList.get(i);
					Map<String, String> propMaps = new HashMap<String, String>();
					if(i<materialList.size() && materialList.size() != 0) {
						HashMap<String, String> materialMap = materialList.get(i);
						propMaps.put("vf4_tools_comsumables", equipmentMap.get("SubstitueList"));
						propMaps.put("vf4_backup_tool", equipmentMap.get("BackupTools"));
						propMaps.put("vf4_step_id", String.valueOf(i));
						propMaps.put("vf4_part_name", materialMap.get("Name"));
						propMaps.put("vf4_torque", materialMap.get("Torque"));
						propMaps.put("vf4_quantity", materialMap.get("Quantity"));
						propMaps.put("vf4_part_number", materialMap.get("ID"));
					}else {
						propMaps.put("vf4_tools_comsumables", equipmentMap.get("SubstitueList"));
						propMaps.put("vf4_backup_tool", equipmentMap.get("BackupTools"));
						propMaps.put("vf4_step_id", String.valueOf(i));
						propMaps.put("vf4_part_name", "");
						propMaps.put("vf4_torque", "");
						propMaps.put("vf4_quantity","");
						propMaps.put("vf4_part_number", "");
					}

					CreateInput data = new CreateInput();
					data.boName = "VF4_jes_parts_list";
					data.stringProps = propMaps;
					input[i] = new CreateIn();
					input[i].data = data;
				}
				return dmService.createObjects(input);
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	private CreateResponse saveStepDetails(DataManagementService dmService) {
		CreateResponse response = null;
		try {
			if(activitiesList != null) {
				CreateIn[] input = new CreateIn[activitiesList.size()];
				for(int i = 0; i<activitiesList.size(); i++) {

					TCComponentMEActivity activityComp = activitiesList.get(i);
					String stepDesc = activityComp.getProperty(logic.DETAIL_STEP_PROP_NAME_DESC);
					if (stepDesc.contains("\\n")) {
						String[] str = stepDesc.split("\\\\n+");
						stepDesc = "";
						boolean first = true;
						for (int j = 0; j < str.length; j++) {
							if (first) {
								stepDesc += str[j];
								first = false;
							} else {
								stepDesc += "\n" + str[j];
							}
						}
					}

					String stepReference = activityComp.getProperty(logic.DETAIL_STEP_REFERENCE);
					if (stepReference != null) {
						stepReference = stepReference.trim();
					}
					if (stepDesc.length() > 2 && stepReference.length() > 2) {
						stepDesc += "\nRefer: " + activityComp.getProperty(logic.DETAIL_STEP_REFERENCE);
					}
					String stepSymbol = activityComp.getProperty(logic.DETAIL_PROP_NAME_STEP_SYMBOL);

					HashMap<String, String> propMaps = new HashMap<String, String>();
					propMaps.put("vf4_step_id", String.valueOf(i+1));
					propMaps.put("vf4_symbol", stepSymbol);
					propMaps.put("vf4_description", stepDesc);

					CreateInput data = new CreateInput();
					data.boName = "VF4_jes_detail_steps_table";
					data.stringProps = propMaps;

					input[i] = new CreateIn();
					input[i].data = data;
				}
				return dmService.createObjects(input);
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	// --------------------------------------------
	private File getOperationImage(TCComponentItemRevision operationRev) {
		File operationImage = null;
		try {
			AIFComponentContext[] attachedObjects = operationRev.getRelated(logic.OPERATION_PICTURE_RELATION);
			ImageValidator imgValidator = new ImageValidator();
			for (AIFComponentContext attachedObject : attachedObjects) {
				TCComponent comp = (TCComponent) attachedObject.getComponent();
				if (comp.getTypeComponent().isTypeOf("Dataset")) {
					Imagedataset = (TCComponentDataset) comp;
					TCComponent[] namedRefs = Imagedataset.getNamedReferences();
					for (int i = 0; i < Imagedataset.getNamedReferences().length; i++) {
						TCComponentTcFile tcFile = (TCComponentTcFile) namedRefs[i];
						if (imgValidator.validate(tcFile.toString())) {
							try {
								operationImage = tcFile.getFile(TEMP_DIR);
							} catch (Exception e) {
								e.printStackTrace();
							}

							return operationImage;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void writeHeader(XSSFWorkbook workbook, XSSFSheet sheet, int headerRowIndex) {
		XSSFRow row = sheet.createRow(headerRowIndex);
		// Create Style border
		XSSFCellStyle Header = logic.createCellStyleForPartDataHeader(workbook);
		// Step
		XSSFCell cell = row.createCell(0, CellType.STRING);
		cell.setCellStyle(Header);
		cell.setCellValue("STEP");
		// Part number
		for (int i = 1; i <= 3; i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(Header);
			cell.setCellValue("PART N#");
		}
		sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex, 1, 3));
		// Partname
		for (int i = 4; i <= 8; i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(Header);
			cell.setCellValue("PART NAME");
		}
		sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex, 4, 8));
		// Qty
		cell = row.createCell(9, CellType.NUMERIC);
		cell.setCellType(CellType.NUMERIC);
		cell.setCellStyle(Header);
		cell.setCellValue("QTY");
		// TORQUE
		for (int i = 10; i <= 13; i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(Header);
			cell.setCellValue("TORQUE");
		}
		sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex, 10, 13));
		// Production tool
		for (int i = 14; i <= 18; i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(Header);
			cell.setCellValue("TOOLS/CONSUMABLES");
		}
		sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex, 14, 18));

		// Backup tool
		for (int i = 19; i <= 22; i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(Header);
			cell.setCellValue("BACKUP TOOL");
		}
		sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex, 19, 22));

	}

	private XSSFCellStyle createStepCellStyle(XSSFWorkbook workbook) {
		XSSFCellStyle border = workbook.createCellStyle();
		border.setBorderBottom(BorderStyle.THIN);
		border.setBorderLeft(BorderStyle.THIN);
		border.setBorderRight(BorderStyle.THIN);
		border.setBorderTop(BorderStyle.THIN);
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		cellStyle.cloneStyleFrom(border);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		// font.setBold(true);
		cellStyle.setFont(font);
		return cellStyle;
	}

	private void updateRevNumber(XSSFWorkbook workbook, XSSFSheet worksheet, String revNum) {
		if (!(isMergedCell(worksheet, 2, 3, CellReference.convertColStringToIndex("N"), CellReference.convertColStringToIndex("N")))) {
			// set border first time
			String setBorderRange = "$N$2:$N$4";
			setBordersToMergedCells(worksheet, CellRangeAddress.valueOf(setBorderRange));

			setMerge(worksheet, 2, 3, CellReference.convertColStringToIndex("N"), CellReference.convertColStringToIndex("N"));
			XSSFRow revRow = null;
			XSSFRow titleRow = null;
			XSSFCellStyle cellStyle = createCellStyle(workbook);
			if (worksheet.getRow(1) == null) {
				titleRow = worksheet.createRow(1);
			} else {
				titleRow = worksheet.getRow(1);
			}
			XSSFCell revTitleCell = null;
			revTitleCell = titleRow.getCell(CellReference.convertColStringToIndex("N"));
			revTitleCell.setCellValue("REV");
			revTitleCell.setCellStyle(cellStyle);
			if (worksheet.getRow(2) == null) {
				revRow = worksheet.createRow(2);
			} else {
				revRow = worksheet.getRow(2);
			}
			XSSFCell revValueCell = null;
			revValueCell = revRow.getCell(CellReference.convertColStringToIndex("N"));
			if (revValueCell == null) {
				revValueCell = revRow.createCell(CellReference.convertColStringToIndex("N"));
			}
			revValueCell.setCellValue(revNum);
			revValueCell.setCellStyle(cellStyle);
		} else {
			XSSFRow revRow = null;
			if (worksheet.getRow(2) == null) {
				revRow = worksheet.createRow(2);
			} else {
				revRow = worksheet.getRow(2);
			}
			XSSFCell revCell = null;
			revCell = revRow.getCell(CellReference.convertColStringToIndex("N"));
			if (revCell == null) {
				revCell = revRow.createCell(CellReference.convertColStringToIndex("N"));
			}
			revCell.setCellValue(revNum);
		}
	}

	private boolean isMergedCell(XSSFSheet worksheet, int numRow, int untilRow, int numCol, int untilCol) {
		System.out.println("num merged reagons: " + worksheet.getNumMergedRegions());
		for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
			CellRangeAddress merge = worksheet.getMergedRegion(i);
			System.out.println("Number:" + i + " " + merge.getFirstRow() + " " + merge.getLastRow() + " " + merge.getFirstColumn() + " " + merge.getLastColumn());
			if (numRow == merge.getFirstRow() && untilRow == merge.getLastRow() && numCol == merge.getFirstColumn() && untilCol == merge.getLastColumn()) {
				return true;
			}
		}
		return false;
	}

	protected void setMerge(XSSFSheet sheet, int numRow, int untilRow, int numCol, int untilCol) {
		CellRangeAddress cellMerge = new CellRangeAddress(numRow, untilRow, numCol, untilCol);
		sheet.addMergedRegion(cellMerge);
	}

	protected void setBordersToMergedCells(XSSFSheet sheet, CellRangeAddress rangeAddress) {
		RegionUtil.setBorderTop(BorderStyle.MEDIUM, rangeAddress, sheet);
		RegionUtil.setBorderLeft(BorderStyle.MEDIUM, rangeAddress, sheet);
		RegionUtil.setBorderRight(BorderStyle.MEDIUM, rangeAddress, sheet);
		RegionUtil.setBorderBottom(BorderStyle.THIN, rangeAddress, sheet);
	}

	private XSSFCellStyle createCellStyle(XSSFWorkbook workbook) {
		XSSFCellStyle cellStyle = workbook.createCellStyle();

		XSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setFontName("Calibri");
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBold(true);
		font.setItalic(false);

		// need set border twice time to make sure all borders were set
		cellStyle.setBorderTop(BorderStyle.MEDIUM);
		cellStyle.setBorderRight(BorderStyle.MEDIUM);
		cellStyle.setBorderBottom(BorderStyle.MEDIUM);
		cellStyle.setBorderLeft(BorderStyle.MEDIUM);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setFont(font);
		return cellStyle;
	}

	private void fillDetailSteps(List<TCComponentMEActivity> activityComps, XSSFSheet worksheet) throws Exception {
		int detailStepRowIndex = logic.DETAIL_STEPS_ROW_INDEX;
		int i = 1;
		for (TCComponentMEActivity activityComp : activityComps) {
			XSSFRow detailStepsRow = null;
			if (worksheet.getRow(detailStepRowIndex) == null) {
				detailStepsRow = worksheet.createRow(detailStepRowIndex);
			} else {
				detailStepsRow = worksheet.getRow(detailStepRowIndex);
			}

			XSSFCell detailStepsCell_No = detailStepsRow.getCell(CellReference.convertColStringToIndex(logic.DETAIL_STEPS_COL_INDEX_NO));
			XSSFCell detailStepsCell_Desc = detailStepsRow.getCell(CellReference.convertColStringToIndex(logic.DETAIL_STEPS_COL_INDEX_DESC));

			// String stepNo = activityComp.getProperty(logic.DETAIL_PROP_NAME_STEP_NO);
			String stepNo = String.valueOf(i);
			String stepDesc = activityComp.getProperty(logic.DETAIL_STEP_PROP_NAME_DESC);
			if (stepDesc.contains("\\n")) {
				String[] str = stepDesc.split("\\\\n+");
				stepDesc = "";
				boolean first = true;
				for (int j = 0; j < str.length; j++) {
					if (first) {
						stepDesc += str[j];
						first = false;
					} else {
						stepDesc += "\n" + str[j];
					}
				}
			}
			// stepDesc += "\n" +
			// activityComp.getProperty(logic.DETAIL_STEP_PROP_NAME_DESC_EN);
			String stepReference = activityComp.getProperty(logic.DETAIL_STEP_REFERENCE);
			if (stepReference != null) {
				stepReference = stepReference.trim();
			}
			if (stepDesc.length() > 2 && stepReference.length() > 2) {
				stepDesc += "\nRefer: " + activityComp.getProperty(logic.DETAIL_STEP_REFERENCE);
			}
			String stepSymbol = activityComp.getProperty(logic.DETAIL_PROP_NAME_STEP_SYMBOL);
			System.out.println("");
			detailStepsCell_No.setCellValue(stepNo);
			detailStepsCell_Desc.setCellValue(stepDesc);
			JES_SymbolGetter symbolGetter = new JES_SymbolGetter();
			File symbolImage = symbolGetter.getImage(session, stepSymbol);
			if (symbolImage != null) {
				drawPicture(worksheet.getWorkbook(), logic.DETAIL_STEPS_COL_INDEX_SYMBOL_START, detailStepRowIndex, logic.DETAIL_STEPS_COL_INDEX_SYMBOL_END, detailStepRowIndex + 1, symbolImage);
			} else {
				// nguyen_log "CANNOT find symbol images or it's empty.
			}

			detailStepRowIndex++;
			i++;
		}
	}

	private void drawPicture(XSSFWorkbook workbook, String col1, int row1, String col2, int row2, File pict) throws FileNotFoundException, IOException {
		InputStream InputPic = new FileInputStream(pict);
		byte[] bytes = IOUtils.toByteArray(InputPic);
		int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG | Workbook.PICTURE_TYPE_PNG);
		InputPic.close();
		CreationHelper helper = workbook.getCreationHelper();
		Drawing<?> drawing = workbook.getSheetAt(0).createDrawingPatriarch();
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setCol1(CellReference.convertColStringToIndex(col1));
		anchor.setRow1(row1);
		anchor.setCol2(CellReference.convertColStringToIndex(col2));
		anchor.setRow2(row2);
		Picture pic = drawing.createPicture(anchor, pictureIdx);
		pic.resize(0.99);
	}

	private HashMap<String, String> getShopParents(TCComponentBOMLine bopLine) {
		HashMap<String, String> parentMap = new HashMap<String, String>();
		parentMap.put("Station", "");
		parentMap.put("Line", "");
		parentMap.put("Shop", "");
		parentMap.put("Program", "");
		try {
			TCComponent station = bopLine.getReferenceProperty("bl_parent");
			TCComponentBOMLine stationLine = (TCComponentBOMLine) station;
			if (stationLine != null && stationLine.getType().equals("Mfg0BvrProcessStation")) {
				String stationName = stationLine.getProperty("bl_item_object_name");
				stationName = stationName.substring(stationName.length() - 4, stationName.length());
				TCComponent line = stationLine.getReferenceProperty("bl_parent");
				TCComponentBOMLine lineLine = (TCComponentBOMLine) line;
				if (lineLine != null && lineLine.getType().equals("Mfg0BvrProcessLine")) {
					String lineName = lineLine.getProperty("bl_item_object_name");
					lineName = lineName.substring(0, 2);
					parentMap.put("Line", lineName);
					parentMap.put("Station", lineName + "-" + stationName);
					TCComponent shop = lineLine.getReferenceProperty("bl_parent");
					TCComponentBOMLine shopLine = (TCComponentBOMLine) shop;
					if (shopLine != null && (shopLine.getType().equals("Mfg0BvrProcessArea") || shopLine.getType().equals("Mfg0BvrProcessLine"))) {
						TCComponentItem shopItem = shopLine.getItem();
						String shopName = shopItem.getProperty("vf4_shop");
						if (shopName.equals("")) {
							shopName = shopLine.getProperty("bl_item_object_name");
						}
						parentMap.put("Shop", shopName);
						TCComponent program = shopLine.getReferenceProperty("bl_parent");
						TCComponentBOMLine programLine = (TCComponentBOMLine) program;
						TCComponentItem programItem = programLine.getItem();
						String programName = programItem.getProperty("vf4_program_model_name");
						if (programName.equals("")) {
							programName = programLine.getProperty("bl_item_object_name");
						}
						parentMap.put("Program", programName);
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		if (parentMap.get("Station").isEmpty() || parentMap.get("Shop").isEmpty() || parentMap.get("Program").isEmpty()) {
			return null;
		}
		return parentMap;
	}

	private String createRelation(TCComponent primary, TCComponent secondary, String relation) {
		String hasError = "";

		Relationship relationShip = new Relationship();
		relationShip.primaryObject = primary;
		relationShip.secondaryObject = secondary;
		relationShip.relationType = relation;

		CreateRelationsResponse response = dmService.createRelations(new Relationship[] { relationShip });
		if (response.serviceData.sizeOfPartialErrors() > 0) {
			hasError = SoaUtil.buildErrorMessage(response.serviceData);
		}
		return hasError;
	}

	private File getSOSExcelFile(TCComponentItemRevision revObj) {
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
