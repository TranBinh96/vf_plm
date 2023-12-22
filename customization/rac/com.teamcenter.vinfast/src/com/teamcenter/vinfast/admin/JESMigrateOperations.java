package com.teamcenter.vinfast.admin;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.spire.xls.FileFormat;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
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
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
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
import com.vf.dialog.VFFileChooserDialog;
import com.vf.utils.ImageValidator;
import com.vf.utils.JES_Logic;
import com.vf.utils.JES_SymbolGetter;
import com.vf.utils.Query;
import com.vf.utils.TCExtension;

public class JESMigrateOperations extends AbstractHandler {

	private ArrayList<String> UIDProcess =  null;
	private TCSession session = null;
	private JES_Logic logic = new JES_Logic();
	private HashMap<String, String> parentMap = null;
	private TCComponentBOPLine stationLine = null;
	private String error = null;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private ArrayList<TCComponentMEActivity> activitiesList = null;
	private TCComponentDataset Imagedataset = null;
	private String TEMP_DIR;
	private DataManagementService dmService = null;
	BufferedWriter logger = null;
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		if(targetComp != null) {
			TEMP_DIR = System.getenv("tmp");
			TCComponentBOPLine operationLine = (TCComponentBOPLine) targetComp[0];
			if(operationLine.getTypeComponent().toString().equals("Mfg0BvrPlantBOP")) {
				VFFileChooserDialog fileDialog =  new VFFileChooserDialog(new Shell(), "JES/SOS Migration");
				fileDialog.create();
				fileDialog.getButton().addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {

						migrate(fileDialog, operationLine);
						if(error == null) {
							MessageBox.post("Migration completed Successfully", "Success", MessageBox.INFORMATION);
						}else {
							MessageBox.post(error, "Error", MessageBox.INFORMATION);
						}

						fileDialog.close();
					}
				});
				fileDialog.open();

			}else {
				MessageBox.post("Please select \"Plant BOP\" type Top BOMLine", "Error", MessageBox.ERROR);
			}
		}

		return null;
	}

	private void migrate(VFFileChooserDialog fileDialog, TCComponentBOPLine operationLine) {

		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		dmService = DataManagementService.getService(session);
		File inputFile = fileDialog.getFile();
		if (progressMonitorDialog == null)
			progressMonitorDialog = new ProgressMonitorDialog(fileDialog.getShell());
		try {
			logger = new BufferedWriter(new FileWriter(TEMP_DIR+"\\JES_output.txt"));  
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Migrate JES/SOS...", IProgressMonitor.UNKNOWN);
					try {
						List<String> lines = Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8);
						if(lines.isEmpty() == false) {
							UIDProcess =  new ArrayList<String>(); 
							String queryIds = String.join(";", lines);
							TCComponent[] operations = TCExtension.searchStruture(session, queryIds, operationLine);
							logger.append("OPERATION ID\t\t\tTYPE\t\t\tREVISION\n");
							for(TCComponent operation : operations) {

								TCComponentBOMLine bomline = (TCComponentBOMLine)operation;
								TCComponentItemRevision currentRevision = bomline.getItemRevision();
								String id = currentRevision.getProperty("item_id");
								String rev = currentRevision.getProperty("item_revision_id");
								TCComponent revision =  createNew(bomline.getItem(), currentRevision);

								if(revision == null) {
									logger.append(id+"/"+rev+"\t\t\tNO ACTION\t\t\t"+rev+"\n");
								}else {
									if(revision == currentRevision) {
										parentMap = getShopParents(bomline);
										monitor.subTask("Processing Operation Details...");
										String errorMessage = processOperations((TCComponentBOPLine)operation, false, true);							
										if(!errorMessage.equals("")) {
											error = "Failed to process operations. Please see detail error.";
											return;
										}else {
											logger.append(id+"/"+rev+"\t\t\tNEW\t\t\t"+rev+"\n");
										}

									}else {
										//copy from existing revision
										String oldrev = revision.getProperty("item_revision_id");
										copyToLatest((TCComponentItemRevision)revision, currentRevision);
										logger.append(id+"/"+rev+"\t\t\tCOPY\t\t\t"+oldrev+"==>"+rev+"\n");
									}
								}
							}
						}

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
			logger.close();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/*private void addSignoff(WorkflowService wfService, TCComponentTask selectSignOffTask, TCComponentGroupMember[] performerList) {

		try {
			if (performerList == null || performerList.length == 0) return;

			CreateSignoffs createSignoff = new CreateSignoffs();
			createSignoff.task = selectSignOffTask;
			createSignoff.signoffInfo = new CreateSignoffInfo[performerList.length];
			int j = 0;
			for (TCComponentGroupMember tcGroupMember : performerList) {
				createSignoff.signoffInfo[j] = new CreateSignoffInfo();
				createSignoff.signoffInfo[j].originType = "SOA_EPM_ORIGIN_UNDEFINED";
				createSignoff.signoffInfo[j].signoffAction = "SOA_EPM_Review";
				createSignoff.signoffInfo[j].signoffMember = tcGroupMember;
				j++;
			}
			ServiceData addSignoffsResponse = wfService.addSignoffs(new CreateSignoffs[] { createSignoff });
			addSignoffsResponse.sizeOfPartialErrors();

			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			DataManagement.PropInfo propInfo = new DataManagement.PropInfo();
			propInfo.object = selectSignOffTask;
			propInfo.timestamp = Calendar.getInstance();
			propInfo.vecNameVal = new DataManagement.NameValueStruct1[1];
			propInfo.vecNameVal[0] = new DataManagement.NameValueStruct1();
			propInfo.vecNameVal[0].name = "task_result";
			propInfo.vecNameVal[0].values = new String[]{"Completed"};
			DataManagement.SetPropertyResponse setPropertyResponse = DataManagementService.getService(session).setProperties(new DataManagement.PropInfo[]{propInfo}, new String[0]);
			if(setPropertyResponse.data.sizeOfPartialErrors() > 0) {
				System.out.println("[assignPerformer]: " + Utils.HanlderServiceData(setPropertyResponse.data));

			}
			selectSignOffTask.fireComponentSaveEvent();
			selectSignOffTask.refresh();//selectSignOffTask.getProperty("task_result");
			selectSignOffTask.setProperty("task_result", "Completed");
			selectSignOffTask.fireComponentSaveEvent();
			//Complete selection signoff task if it started
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(selectSignOffTask.getTaskState().compareToIgnoreCase("Started") == 0) {
				PerformActionInputInfo paii = new PerformActionInputInfo();
				paii.clientId = "complete" + selectSignOffTask.getUid();
				paii.action = "SOA_EPM_complete_action";
				paii.actionableObject = selectSignOffTask;
				paii.propertyNameValues.put("comments", new String[]{"Auto Completed"});
				paii.supportingValue = "SOA_EPM_completed";

				ServiceData sd = wfService.performAction3(new PerformActionInputInfo[]{paii});
				if(sd.sizeOfPartialErrors() > 0) {
					System.out.println("[assignPerformer]: " + Utils.HanlderServiceData(sd));

				}
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}*/

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
		propMap.put("vf4_operation_id", new String[] {operationList.get("OP_ID") + "/" + operationList.get("OP_REV_ID")});
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

	public File generateReport(InterfaceAIFComponent aifComp, String outputFilePathWithoutExtension, boolean openAfterDownload) throws Exception {

		File output = null;
		TCComponentBOPLine operationLine = (TCComponentBOPLine) aifComp;
		HashMap<String, String> OperationList = loadOperationDetails(operationLine);
		Vector<HashMap<String, String>> equipmentVec = loadEquipmentDetails(operationLine);
		Vector<HashMap<String, String>> materialVec = loadMaterialDetails(operationLine);

		if (materialVec.size() < logic.JES_TEMPLATE_MAX_MATERIAL_VEC_SIZE) {
			output = populateReport(OperationList, materialVec, equipmentVec, operationLine.getItemRevision(), activitiesList, outputFilePathWithoutExtension);
			if (openAfterDownload) {
				Desktop.getDesktop().open(output);
			}
		} else {
			MessageBox.post("Material count must be less than template rows (i.e 40). Please contact system admin.", "Error", MessageBox.ERROR);
		}
		return output;
	}

	/**
	 * @param operationRevision
	 * @param rootActivityLine
	 * @param activitiesList
	 * @return Compare Operation & Activities and gives latest last modified component
	 */
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lastModified;
	}

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
							operationImage = tcFile.getFile(TEMP_DIR);
							return operationImage;
						}
					}
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			System.out.println("Volume Error: Failed to download NamedReference");
			return null;
		}
		return null;
	}

	private HashMap<String, String> getShopParents(TCComponentBOMLine bopLine) {

		HashMap<String, String> parentMap = new HashMap<String, String>();
		parentMap.put("Station", "");
		parentMap.put("Line", "");
		parentMap.put("Shop", "");
		parentMap.put("Program", "");
		try {
			TCComponent station = bopLine.getReferenceProperty("bl_parent");
			stationLine = (TCComponentBOPLine) station;
			if (stationLine != null && stationLine.getType().equals("Mfg0BvrProcessStation")) {
				String stationName = stationLine.getProperty("bl_item_object_name");
				stationName = stationName.substring(stationName.length()-4, stationName.length());
				TCComponent line = stationLine.getReferenceProperty("bl_parent");
				TCComponentBOMLine lineLine = (TCComponentBOMLine) line;
				if(lineLine != null && lineLine.getType().equals("Mfg0BvrProcessLine")) {
					String lineName = lineLine.getProperty("bl_item_object_name");
					lineName = lineName.substring(0,2);
					parentMap.put("Line", lineName);
					parentMap.put("Station", lineName+"-"+stationName);
					TCComponent shop = lineLine.getReferenceProperty("bl_parent");
					TCComponentBOMLine shopLine = (TCComponentBOMLine) shop;
					if(shopLine != null && (shopLine.getType().equals("Mfg0BvrProcessLine") ||shopLine.getType().equals("Mfg0BvrProcessArea"))) {
						TCComponentItem shopItem = shopLine.getItem();
						String shopName = shopItem.getProperty("vf4_shop");
						if(shopName.equals("")) {
							shopName = shopLine.getProperty("bl_item_object_name");
						}
						parentMap.put("Shop", shopName);
						TCComponent program = shopLine.getReferenceProperty("bl_parent");
						TCComponentBOMLine programLine = (TCComponentBOMLine) program;
						TCComponentItem programItem = programLine.getItem();
						String programName = programItem.getProperty("vf4_program_model_name");
						if(programName.equals("")) {
							programName = programLine.getProperty("bl_item_object_name");
						}
						parentMap.put("Program", programName);
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		if(parentMap.get("Station").isEmpty() || parentMap.get("Shop").isEmpty() || parentMap.get("Program").isEmpty()) {
			return null;
		}
		return parentMap;
	}

	public HashMap<String, TCComponentGroupMember[]> getSignoffUsers(String shop, String line, String station) {
		HashMap<String, TCComponentGroupMember[]> groupMembers = new HashMap<String, TCComponentGroupMember[]>();

		TCPreferenceService prefService = session.getPreferenceService();
		String[] prefME = prefService.getStringValues("VF_JES_Assignment_ME");
		HashMap<String, String[]> MEList = extractSignOffs(prefME);
		String[] MEUserIDs = MEList.get(shop);
		if(MEUserIDs != null) {
			ArrayList<TCComponentGroupMember> userList = new ArrayList<TCComponentGroupMember>();
			for(String MEUserID : MEUserIDs) {
				TCComponentGroupMember member = TCExtension.GetGroupMemberByUserID(MEUserID.toString(), session);
				if(member != null) {
					userList.add(member);
				}
			}
			groupMembers.put("ME", userList.toArray(new TCComponentGroupMember[0]));
		}

		String[] prefGL1 = prefService.getStringValues("VF_JES_Assignment_GL_Shift_1");
		HashMap<String, String[]> GLList1 = extractSignOffs(prefGL1);

		String[] prefGL2 = prefService.getStringValues("VF_JES_Assignment_GL_Shift_2");
		HashMap<String, String[]> GLList2 = extractSignOffs(prefGL2);

		String[] prefGL3 = prefService.getStringValues("VF_JES_Assignment_GL_Shift_3");
		HashMap<String, String[]> GLList3 = extractSignOffs(prefGL3);

		TCComponentGroupMember[] glGroupMembersShift1 = createSignOffGroupMembers(shop, line, GLList1);
		groupMembers.put("GL_Shift_1", glGroupMembersShift1);

		TCComponentGroupMember[] glGroupMembersShift2 = createSignOffGroupMembers(shop, line, GLList2);
		groupMembers.put("GL_Shift_2", glGroupMembersShift2);

		TCComponentGroupMember[] glGroupMembersShift3 = createSignOffGroupMembers(shop, line, GLList3);
		groupMembers.put("GL_Shift_3", glGroupMembersShift3);

		return groupMembers;
	}

	private TCComponentGroupMember[] createSignOffGroupMembers(String shop, String line, HashMap<String, String[]> GLList1) {
		String[] GLUserIDs = GLList1.get(shop+"_"+line);
		TCComponentGroupMember[] ul = null;
		if(GLUserIDs != null) {
			ArrayList<TCComponentGroupMember> userList = new ArrayList<TCComponentGroupMember>();
			for(String GLUserID : GLUserIDs) {
				TCComponentGroupMember member = TCExtension.GetGroupMemberByUserID(GLUserID.toString(), session);
				if(member != null) {
					userList.add(member);
				}
			}
			ul = userList.toArray(new TCComponentGroupMember[0]);
		}
		return ul;
	}

	private HashMap<String, String[]> extractSignOffs(String[] prefGL) {
		HashMap<String, String[]> GLList =  new HashMap<String, String[]>();
		for(String GLValue : prefGL) {
			String[] splitvalue = GLValue.split("=");
			if(splitvalue.length == 2) {
				if(splitvalue[1].contains(";")){
					String[] users = splitvalue[1].split(";");
					GLList.put(splitvalue[0], users);
				}else {
					GLList.put(splitvalue[0], new String[] {splitvalue[1]});
				}
			}
		}

		return GLList;
	}

	private boolean isMergedCell(XSSFSheet worksheet, int numRow, int untilRow, int numCol, int untilCol) {

		for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
			CellRangeAddress merge = worksheet.getMergedRegion(i);
			if (numRow == merge.getFirstRow() && untilRow == merge.getLastRow() && numCol == merge.getFirstColumn() && untilCol == merge.getLastColumn()) {
				return true;
			}
		}
		return false;
	}

	private Vector<HashMap<String, String>> loadEquipmentDetails(TCComponentBOPLine operationLine){
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return equipmentDetails;
	}

	/**
	 * @apiNote Load Operation related Material Information
	 * @param operationLine
	 * @return Array of Map <PropertyKey, PropertyValue>
	 */
	private Vector<HashMap<String, String>> loadMaterialDetails(TCComponentBOPLine operationLine){

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return materialDetails;
	}

	/**
	 * @apiNote Loads operation related information
	 * @param operationLine
	 * @return Map <PropertyKey, PropertyValue>
	 */
	private HashMap<String, String> loadOperationDetails(TCComponentBOPLine operationLine){

		HashMap<String, String> operationDetails = new HashMap<String, String>();
		TCComponent lastModified = null;
		DecimalFormat timeFormat = new DecimalFormat("#.##");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		//parentMap = getShopParents(operationLine);

		try {
			TCComponentItemRevision operationRevision = operationLine.getItemRevision();
			TCComponent[] history = operationRevision.getRelatedComponents("VF4_history_jes");
			operationDetails.put("OP_HISTORY", String.format("%02d", history.length+1));
			String operationRevisionID = operationRevision.getProperty("item_id");
			String opRevID = operationRevision.getProperty("item_revision_id");
			operationDetails.put("OP_ID", operationRevisionID);
			operationDetails.put("OP_REV_ID", opRevID);
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
			if(activities != null) {
				TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) activities;
				String activityName = activityLine.getProperty("al_activity_vf4_detail_step_desc");
				if(activityName.isEmpty() == false) {
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
				if(rootActivity != null) {
					activitiesList =  new ArrayList<TCComponentMEActivity>();
					TCComponentMEActivity rootActivityLine = (TCComponentMEActivity) rootActivity[0].getComponent();
					AIFComponentContext[] rootActivityChilds = rootActivityLine.getRelated("contents");
					for (AIFComponentContext content : rootActivityChilds) {
						if (content.getComponent().getClass().getSimpleName().equals(TCComponentMEActivity.class.getSimpleName())) {
							activitiesList.add((TCComponentMEActivity) content.getComponent());
						}
					}
					lastModified = getLastModifiedComponent(operationRevision, rootActivityLine, activitiesList);
				}
			}else {
				operationDetails.put("OP_WORKTIME", "");
			}

			if(lastModified !=null) {
				Date lastModifiedDate = lastModified.getDateProperty("last_mod_date");
				operationDetails.put("OP_LASTMODIFIEDDATE", dateFormat.format(lastModifiedDate));
				String lastModifiedUser = lastModified.getProperty("last_mod_user");
				operationDetails.put("OP_LASTMODIFIEDUSER", lastModifiedUser);
			}else {
				Date lastModifiedDate = operationRevision.getDateProperty("last_mod_date");
				operationDetails.put("OP_LASTMODIFIEDDATE", dateFormat.format(lastModifiedDate));
				String lastModifiedUser = operationRevision.getProperty("last_mod_user");
				operationDetails.put("OP_LASTMODIFIEDUSER", lastModifiedUser);
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return operationDetails;

	}

	private File populateReport(HashMap<String, String> operationList, Vector<HashMap<String, String>> materialVec, Vector<HashMap<String, String>> equipmentVec, TCComponentItemRevision operationRev, List<TCComponentMEActivity> activityComps, String outputFilePathWithoutExtension) {

		File updatefile = null;
		try {

			updatefile = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET, Query.QUERY_JES_ENTRY_DATASET_NAME, logic.JES_DATASET_TEMPLATE, outputFilePathWithoutExtension);

			if(updatefile == null) {
				MessageBox.post("Error fetching JES template. Please contact Admin.\n", "ERROR", MessageBox.ERROR);
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

			TCComponent[] pdfList = operationRev.getReferenceListProperty("VF4_jes_soft_copy");
			boolean hasPDF = pdfList != null && pdfList.length > 0;
			String jesVersion = (hasPDF) ? operationList.get("OP_HISTORY") : operationList.get("OP_REVID");

			updateRevNumber(workbook, worksheet, jesVersion);

			File operationImage = getOperationImage(operationRev);
			if (operationImage != null) {
				drawPicture(workbook, logic.PICTURE_COL_TOP_INDEX, logic.PICTURE_ROW_TOP_INDEX, logic.PICTURE_COL_BOTTOM_INDEX, logic.PICTURE_ROW_BOTTOM_INDEX, operationImage);
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
			MessageBox.post("Please contact Admin. Detail error message is as below.\n" + ex.getMessage(), "ERROR", MessageBox.ERROR);
		}
		return updatefile;
	}

	/**
	 * @param dialog
	 * @param operationLine
	 * @param openFile
	 * @param createForm
	 * @return Error Message
	 */
	public String processOperations(TCComponentBOPLine operationLine, boolean openFile, boolean createForm) {

		StringBuilder hasError = new StringBuilder();
		HashMap<String, String> OperationList = loadOperationDetails(operationLine);
		if(OperationList == null) {
			hasError.append("Activities has encountered some problem. Please contact Teamcenter Admin");
			hasError.append("\n");
		}else {


			Vector<HashMap<String, String>> equipmentList = loadEquipmentDetails(operationLine);
			Vector<HashMap<String, String>> materialList = loadMaterialDetails(operationLine);
			try {

				TCComponentItemRevision operationRevision = operationLine.getItemRevision();
				String operationName = OperationList.get("OP_NAME");
				if (materialList.size() < logic.JES_TEMPLATE_MAX_MATERIAL_VEC_SIZE) {
					//operationNamexxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
					String jesNameRef = operationName = operationName.replace(":", "_").replace("/", "_").replace("\\", "_").replace("*", "_").replace("?", "_").replace("\"", "_").replace("<", "_").replace(">", "_").replace("|", "_");
					File excelFile = populateReport(OperationList, materialList, equipmentList, operationLine.getItemRevision(), activitiesList, TEMP_DIR + "\\" + jesNameRef);
					if(openFile == true) {
						Desktop.getDesktop().open(excelFile);
					}
					if(createForm == true) {
						String JESName = operationName + " (Ver: "+OperationList.get("OP_HISTORY")+")";
						String pdfFilePath = createPDF(excelFile);
						String operationString = OperationList.get("OP_ID")+"/"+OperationList.get("OP_REVID");
						TCComponentDataset newDataset = createDataset(JESName, pdfFilePath);

						if(newDataset == null) {
							hasError.append(operationString+" - Failed to Create JES File.");
							hasError.append("\n");
						}else {
							newDataset.setProperty("object_desc", operationString);
							TCComponent newForm = createForm(operationName,OperationList,materialList,equipmentList);
							if(newForm == null) {
								newDataset.delete();
								hasError.append(OperationList.get("OP_ID")+"/"+OperationList.get("OP_REVID")+" - Failed to Create JES Form.");
								hasError.append("\n");
							}else {
								String errorMessage = createRelation(operationRevision, newDataset, "VF4_jes_soft_copy");
								if(errorMessage.equals("")) {
									errorMessage = createRelation(operationRevision, newForm, "VF4_history_jes");
									if(!errorMessage.equals("")) {
										newDataset.delete();
										newForm.delete();
										hasError.append(OperationList.get("OP_ID")+"/"+OperationList.get("OP_REVID")+" - Failed to Attach JES Form.");
										hasError.append("\n");
									}else {
										errorMessage = createRelation(newForm, newDataset, "IMAN_specification");
										if(!errorMessage.equals("")) {
											newDataset.delete();
											newForm.delete();
											hasError.append(OperationList.get("OP_ID")+"/"+OperationList.get("OP_REVID")+" - Failed to Attach JES Form.");
											hasError.append("\n");
										}else {
											UIDProcess.add(operationRevision.getUid());
											UIDProcess.add(newDataset.getUid());
											UIDProcess.add(newForm.getUid());
										}
									}
								}else {
									newDataset.delete();
									newForm.delete();
									hasError.append(OperationList.get("OP_ID")+"/"+OperationList.get("OP_REVID")+" - Failed to Attach JES File.");
									hasError.append("\n");
								}
							}
						}
					}
				} else {
					MessageBox.post("Material count must be less than template rows (i.e 40). Please contact system admin.", "Error", MessageBox.ERROR);
				}
			} catch (TCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return hasError.toString();
	}

	private String createRelation(TCComponent primary, TCComponent secondary, String relation) {
		String hasError = "";

		Relationship relationShip =  new Relationship();
		relationShip.primaryObject = primary;
		relationShip.secondaryObject = secondary;
		relationShip.relationType = relation;

		CreateRelationsResponse response = dmService.createRelations(new Relationship[] {relationShip});
		if(response.serviceData.sizeOfPartialErrors() > 0) {
			hasError = SoaUtil.buildErrorMessage(response.serviceData);
		}
		return hasError;
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

	protected void setBordersToMergedCells(XSSFSheet sheet, CellRangeAddress rangeAddress) {
		RegionUtil.setBorderTop(BorderStyle.MEDIUM, rangeAddress, sheet);
		RegionUtil.setBorderLeft(BorderStyle.MEDIUM, rangeAddress, sheet);
		RegionUtil.setBorderRight(BorderStyle.MEDIUM, rangeAddress, sheet);
		RegionUtil.setBorderBottom(BorderStyle.THIN, rangeAddress, sheet);
	}

	protected void setMerge(XSSFSheet sheet, int numRow, int untilRow, int numCol, int untilCol) {
		CellRangeAddress cellMerge = new CellRangeAddress(numRow, untilRow, numCol, untilCol);
		sheet.addMergedRegion(cellMerge);
	}

	/*private void setSignoffProfiles(TCComponentProcess process, HashMap<String, TCComponentGroupMember[]> signoffProfiles) {


		try {
			WorkflowService wfService = WorkflowService.getService(session);
			TCComponentTask rootTask = (process).getRootTask();
			TCComponentTask[] subtasks = rootTask.getSubtasks();
			TCComponentTask autoCompleteTask = null;
			for(int i = 0; i < subtasks.length; i++) {
				if(subtasks[i].getTaskType().equals("EPMTask") && subtasks[i].getName().equals("Auto-Assign-Signoff")) {
					autoCompleteTask = subtasks[i];
				}
				if(subtasks[i].getTaskType().compareToIgnoreCase("EPMReviewTask") == 0 ) {
					TCComponentTask[] selectSignOffTasks = subtasks[i].getSubtasks();
					for(TCComponentTask selectSignOffTask : selectSignOffTasks) {
						if(selectSignOffTask.getTaskType().equals("EPMSelectSignoffTask")  && selectSignOffTask.getName().equals("ME Controller Signoff")) {
							selectSignOffTask = subtasks[i].getSubtasks()[0];
							addSignoff(wfService, selectSignOffTask, signoffProfiles.get("ME"));
						} else if(selectSignOffTask.getTaskType().equals("EPMSelectSignoffTask")  && selectSignOffTask.getName().equals("GL Signoff (Shift 1)")) {
							selectSignOffTask = subtasks[i].getSubtasks()[0];
							addSignoff(wfService, selectSignOffTask, signoffProfiles.get("GL_Shift_1"));
						} else if(selectSignOffTask.getTaskType().equals("EPMSelectSignoffTask")  && selectSignOffTask.getName().equals("GL Signoff (Shift 2)")) {
							selectSignOffTask = subtasks[i].getSubtasks()[0];
							addSignoff(wfService, selectSignOffTask, signoffProfiles.get("GL_Shift_2"));
						} else if(selectSignOffTask.getTaskType().equals("EPMSelectSignoffTask")  && selectSignOffTask.getName().equals("GL Signoff (Shift 3)")) {
							selectSignOffTask = subtasks[i].getSubtasks()[0];
							addSignoff(wfService, selectSignOffTask, signoffProfiles.get("GL_Shift_3"));
						}
					}
				}
			}
			PerformActionInputInfo inputInfo =  new PerformActionInputInfo();
			inputInfo.actionableObject = autoCompleteTask;
			inputInfo.action = "SOA_EPM_complete_action";
			inputInfo.supportingValue = "SOA_EPM_completed";

			ServiceData data = wfService.performAction3(new PerformActionInputInfo[] {inputInfo});
			System.out.println(SoaUtil.buildErrorMessage(data));
			//autoCompleteTask.setProperty("task_result", "Completed");
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

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
	/*private File getSOSExcelFile(TCComponentItemRevision revObj) {
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
	}*/

	private String createPDF(File Flile) {

		String pdfFile = Flile.getAbsolutePath().replace("xlsx", "pdf");
		com.spire.xls.Workbook workbook = new com.spire.xls.Workbook();
		workbook.loadFromFile(Flile.getAbsolutePath());
		//workbook.getConverterSetting().setSheetFitToPage(true);
		workbook.saveToFile(pdfFile, FileFormat.PDF);
		return pdfFile;
	}

	private TCComponent createNew(TCComponentItem item, TCComponentItemRevision revision) {

		TCComponent revTag = revision;
		try {
			int currentRevID;
			String currentRevision = revision.getProperty("item_revision_id");
			currentRevID = Integer.parseInt(currentRevision);
			TCComponent[] revisions = item.getRelatedComponents("revision_list");
			for(int i=revisions.length-1; i>0; i--) {
				TCComponent[] jesPDF = revisions[i].getRelatedComponents("VF4_jes_soft_copy");
				TCComponent[] jesForm = revisions[i].getRelatedComponents("VF4_history_jes");
				String revRevisionID = revisions[i].getProperty("item_revision_id");
				int revRevID = Integer.parseInt(revRevisionID);
				if(jesPDF.length > 0 && jesForm.length > 0 && jesForm.length == jesPDF.length && revRevID == currentRevID) {
					revTag = null;
					break;
				}
				if(jesPDF.length > 0 && revRevID != currentRevID) {
					revTag = revisions[i];
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return revTag;
	}

	private void copyToLatest(TCComponentItemRevision existingRevision, TCComponentItemRevision latestRevision) {

		try {
			latestRevision.setRelated("VF4_jes_soft_copy", existingRevision.getRelatedComponents("VF4_jes_soft_copy"));
			latestRevision.setRelated("VF4_history_jes", existingRevision.getRelatedComponents("VF4_history_jes"));
			TCComponent[] pictureExistingRev= existingRevision.getRelatedComponents("TC_Attaches");
			TCComponent[] pictureLatestRev = latestRevision.getRelatedComponents("TC_Attaches");
			if (pictureExistingRev.length > 0 && pictureLatestRev.length == 0) {
				latestRevision.setRelated("TC_Attaches", pictureExistingRev);
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}