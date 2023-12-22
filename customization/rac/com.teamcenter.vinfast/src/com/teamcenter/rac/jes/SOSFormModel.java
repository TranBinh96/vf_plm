package com.teamcenter.rac.jes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;

import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.FileUtility;
import com.vf.utils.ExcelExtension;
import com.vf.utils.StringExtension;

public class SOSFormModel {
	private String createBy = "";
	private String revisionID = "";
	private String vehicleType = "";
	private String shopName = "";
	private String stageName = "";
	private String locationName = "";
	private String version = "";
	private LocalDateTime createDate;
	private LocalDateTime updateDate;
	private LinkedList<ActivityModel> activityList = new LinkedList<ActivityModel>();
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private TCSession session;
	private File sosExcelFile = null;
	private String  personal_protective_equipment;
	
									
	
	public SOSFormModel(TCSession session) {
		this.session = session;
		createDate = LocalDateTime.now();
		updateDate = LocalDateTime.now();
	}
	
	public void addNewActivity(String name, String id, double workingTime, double walkingTime, String version) {
		ActivityModel newItem = new ActivityModel();
		newItem.setActivityID(id);
		newItem.setActivityName(name);
		newItem.setWorkingTime(workingTime);
		newItem.setWalkingTime(walkingTime);
		newItem.setVersion(version);
		
		activityList.add(newItem);
		
		if(!id.isEmpty()) {
			if (id.substring(0, 1).equals("S"))
				vehicleType = "SEDAN";
			else if (id.substring(0, 1).equals("U"))
				vehicleType = "SUV";	
		}
	}
	
	
	
	
	public void addNewActivity(String name, String id, double workingTime, String version) {
		addNewActivity(name, id, workingTime, ActivityModel.NULL_TIME, version);
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCreateDateString() {
		return dtf.format(createDate);
	}
	
	public String getUpdateDateString() {
		return dtf.format(updateDate);
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getRevisionID() {
		return revisionID;
	}

	public void setRevisionID(String revisionID) {
		this.revisionID = revisionID;
	}

	public String getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public String getStageName() {
		return stageName;
	}

	public void setStageName(String stageName) {
		this.stageName = stageName;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	public LinkedList<ActivityModel> getActivityList() {
		return activityList;
	}

	public void setActivityList(LinkedList<ActivityModel> activityList) {
		this.activityList = activityList;
	}
	
	public DateTimeFormatter getDtf() {
		return dtf;
	}

	public void setDtf(DateTimeFormatter dtf) {
		this.dtf = dtf;
	}

	

	public void updateOrCreateSOSExcelToStation(File outputFile, TCComponentItemRevision stationRev) throws Exception {
		boolean isCreate = false;
		boolean isRequiredChangeTemplate = checkRequiredChangeTemplate(outputFile);
		if (outputFile == null || isRequiredChangeTemplate) {
			isCreate = true;
			String filePath = "";
			String originalFile = "";
			String outPutFilePath = "";

			try {
				filePath = setOutputFilePath();
				if (getActivityList().size() < 9) {
					originalFile = filePath + "\\JES\\JES_Input_file\\SOS_FINAL.xlsx";
				} else {
					originalFile = filePath + "\\JES\\JES_Input_file\\SOS_Extend.xlsx";
				}
				outPutFilePath = filePath + "\\JES\\" + stationRev.getProperty("object_name") + ".xlsx";

				// DUPLICATE A FILE
				FileSystem system = FileSystems.getDefault();
				Path original = system.getPath(originalFile);
				Path target = system.getPath(outPutFilePath);

				// Throws an exception if the original file is not found.
				Files.copy(original, target, StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new TCException(ex.toString());
			}
			outputFile = new File(outPutFilePath);
		}

		InputStream fileOut = null;
		XSSFWorkbook workbook = null;
		try {
			fileOut = new FileInputStream(outputFile);
			workbook = new XSSFWorkbook(fileOut);
			XSSFSheet worksheet = workbook.getSheet("SOS");
			worksheet = worksheet == null ? workbook.getSheetAt(0) : worksheet;
			outputFile.setWritable(true);

			if (isCreate) {
				// Date Prepared
				XSSFCell datePreparedCell = ExcelExtension.getCellByAddress("R2", worksheet);
				datePreparedCell.setCellValue(getCreateDateString());
				
			
				File footRotectionImage = new File("icons\\footRotection.png");
	            if (footRotectionImage != null) {
	                importImage(workbook, "C", 5, "D", 7, footRotectionImage);
	            }

	            File handProtectionImage = new File("icons\\handProtection.png");
	            if (handProtectionImage != null) {
	                importImage(workbook, "D", 5, "E", 7, handProtectionImage);
	            }

	            File faceShieldImage = new File("icons\\faceShield.png");
	            if (faceShieldImage != null) {
	                importImage(workbook, "E", 5, "F", 7, faceShieldImage);
	            }
	            File dustMarkImage = new File("icons\\dustMark.png");
	            if (dustMarkImage != null) {
	                importImage(workbook, "F", 5, "G", 7, dustMarkImage);
	            }
	            File earProtectionImage = new File("icons\\earProtectionOn.png");
	            if (earProtectionImage != null) {
	                importImage(workbook, "G", 5, "H", 7, earProtectionImage);
	            }
	            File safetyGlassesImage = new File("icons\\safetyGlasses.png");
	            if (safetyGlassesImage != null) {
	                importImage(workbook, "H", 5, "I", 7, safetyGlassesImage);
	            }
	            File bumpCapImage = new File("icons\\bumpCap.png");
	            if (bumpCapImage != null) {
	                importImage(workbook, "I", 5, "J", 7, bumpCapImage);
	            }
	            File safetyHelmetsImage = new File("icons\\safetyHelmets.png");
	            if (safetyHelmetsImage != null) {
	                importImage(workbook, "J", 5, "K", 7, safetyHelmetsImage);
	            }

				// Prepared By
				XSSFCell preparedByCell = ExcelExtension.getCellByAddress("R3", worksheet);
				preparedByCell.setCellValue(getCreateBy());
			} else {
				// Date Updated
				XSSFCell dateUpdatedCell = ExcelExtension.getCellByAddress("R4", worksheet);
				dateUpdatedCell.setCellValue(getUpdateDateString());
			}

			// REV
			XSSFCell revCell = ExcelExtension.getCellByAddress("M3", worksheet);
			revCell.setCellValue((getVersion().isBlank() || getVersion().contentEquals("01")) ? getRevisionID() : getVersion());

			// VEHICLE TYPE
			XSSFCell vehicleTypeCell = ExcelExtension.getCellByAddress("H3", worksheet);
			vehicleTypeCell.setCellValue(getVehicleType());

			// SHOP
			XSSFCell shopCell = ExcelExtension.getCellByAddress("H4", worksheet);
			shopCell.setCellValue(getShopName());

			// STAGE NAME
//			XSSFCell stageCell = ExcelExtension.getCellByAddress("C9", worksheet);
//			stageCell.setCellValue(sosForm.getStageName());

			// LOCATION
			XSSFCell locationCell = ExcelExtension.getCellByAddress("H9", worksheet);
			locationCell.setCellValue(getLocationName());

			// JOB ELEMENT LIST
			if (getActivityList().size() > 0) {
				int i = 0;
				int row = 15;
				for (ActivityModel activity : getActivityList()) {
					if (i == 8) {
						row += 5;
					}
					if (i > 20)
						continue;
					row++;
					XSSFCell jesNameCell = ExcelExtension.getCellByAddress("C" + row, worksheet);
					jesNameCell.setCellValue(activity.getActivityName().toUpperCase());
					
					XSSFCell jesRefCell = ExcelExtension.getCellByAddress("N" + row, worksheet);
					jesRefCell.setCellValue(activity.getActivityID());
					
					XSSFCell jesWorkCell = ExcelExtension.getCellByAddress("R" + row, worksheet);
					if (activity.getWorkingTime() != ActivityModel.NULL_TIME) {
						jesWorkCell.setCellValue(activity.getWorkingTime());
					} else {
						jesWorkCell.setCellValue("");
					}

					if (activity.getWalkingTime() != ActivityModel.NULL_TIME) {
						XSSFCell jesWalkCell = ExcelExtension.getCellByAddress("S" + row, worksheet);
						jesWalkCell.setCellValue(activity.getWalkingTime());	
					}
					
					i++;
				}
			}

			fileOut.close();
			FileOutputStream output_file = new FileOutputStream(outputFile);
			workbook.write(output_file);
			workbook.close();
			output_file.close();

			if (isCreate) {
				createDataset(outputFile.getPath(), stationRev);
				String path = outputFile.getPath().replace("C:\\Siemens\\TR13\\JES", "C:\\Temp");
				sosExcelFile = new File(path);
				FileUtility.copyFile(outputFile, sosExcelFile);
				outputFile.delete();
			}else {
				sosExcelFile = outputFile;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new TCException(ex.toString());
		}
	}

	private boolean checkRequiredChangeTemplate(File outputFile) throws FileNotFoundException, IOException {
		boolean isRequiredChangeTemplate = false;
		if (outputFile != null) {
			FileInputStream fileOutTemp = new FileInputStream(outputFile);
			XSSFWorkbook workbook = new XSSFWorkbook(fileOutTemp);
			XSSFSheet worksheet = workbook.getSheet("SOS");
			boolean isUsingOnePageTemplate = worksheet.getRow(40) == null;
			boolean isUsingOldTwoPageTemplate = true;
			try {
				isUsingOldTwoPageTemplate = ExcelExtension.getCellByAddress("A1", worksheet).getStringCellValue().trim().isEmpty();
			} catch(Exception ex) {
				// NOTHING to do
			}
			isRequiredChangeTemplate = getActivityList().size() >= 9 && (isUsingOnePageTemplate || isUsingOldTwoPageTemplate);
			workbook.close();
		}
		
		return isRequiredChangeTemplate;
	}
	
	private String setOutputFilePath() {
		try {
			String env1 = StringExtension.getFMSHome();
			int ocr = env1.lastIndexOf("\\");
			return env1.substring(0, ocr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return "";
	}
	

	
	private void createDataset(String fileName, TCComponentItemRevision stationRev) {
		String datasetType = "MSExcelX";

		NamedReferenceContext namedReferenceContext[] = null;
		String namedRef = null;

		try {
			TCComponentDatasetDefinitionType tcDatasetDefinitionType = (TCComponentDatasetDefinitionType) session.getTypeComponent("DatasetType");
			TCComponentDatasetDefinition datasetDefinition = tcDatasetDefinitionType.find(datasetType);
			namedReferenceContext = datasetDefinition.getNamedReferenceContexts();
			namedRef = namedReferenceContext[0].getNamedReference();
			String[] type = { namedRef };
			TCComponentDatasetType datasetTypeComponent = (TCComponentDatasetType) session.getTypeComponent("Dataset");
			// Create the Dataset with given name and description. create( datasetName,
			// datasetDesc, dataset_id, dataset_rev, datasetType, toolUsed );
			TCComponentDataset newDataset = datasetTypeComponent.create(stationRev.getProperty("object_name"), stationRev.getProperty("item_id"), datasetDefinition.toString());
			newDataset.refresh();
			// import selected file on newly created dataset with the Specification relation
			newDataset.setFiles(new String[] { fileName }, type);
			// attach dataset to target element with VF4_SOS_Relation */
			stationRev.setRelated("VF4_SOS_Relation", new TCComponent[] { newDataset });
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public File getSOSExcelFile() {
		return sosExcelFile;
	}
	public  void createImage( Workbook workbook, Sheet sheet,String nameImage, String row) throws IOException {
        InputStream inputStream = new FileInputStream(nameImage);
        CellReference cr = new CellReference(row);
        byte[] bytes = IOUtils.toByteArray(inputStream);
        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
        inputStream.close();
        CreationHelper helper = workbook.getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(cr.getRow()); //Column B
        anchor.setRow1(2); //Row 3
        anchor.setCol2(cr.getRow()+1); //Column C
        anchor.setRow2(3); //Row 4
        drawing.createPicture(anchor, pictureIdx);

    }
	
	
	private static void importImage(XSSFWorkbook workbook, String col1, int row1, String col2, int row2, File pict) {
        try {
            InputStream InputPic = new FileInputStream(pict);
            byte[] bytes = IOUtils.toByteArray(InputPic);
            int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG | Workbook.PICTURE_TYPE_PNG);
            InputPic.close();
            CreationHelper helper = workbook.getCreationHelper();
            Drawing<?> drawing = workbook.getSheet("SOS").getDrawingPatriarch();
            if (drawing == null)
                drawing = workbook.getSheet("SOS").createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(CellReference.convertColStringToIndex(col1));
            anchor.setRow1(row1);
            anchor.setCol2(CellReference.convertColStringToIndex(col2));
            anchor.setRow2(row2);
            Picture pic = drawing.createPicture(anchor, pictureIdx);
            pic.resize(0.99);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
