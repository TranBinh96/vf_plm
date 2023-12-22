package com.teamcenter.vinfast.change;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.vf.utils.TCExtension;

public class ECRInputManagement {

	private static final String VF_END_META_ATTR_CELL_VALUE = "VF_END_META_ATTR";
	private static final String VF_END_BOM_ATTR_CELL_VALUE = "VF_END_BOM_ATTR";
	private static final String ECR_REV_AND_EXCEL_DS_RELATION = "CMReferences";
	private static final String ECR_FORM_AND_EXCEL_DS_RELATION = "IMAN_external_object_link";
	public static final String ECR_FORM_TYPE = "Vf6_ecr_information";
	public static final String ECR_FORM_RELATION = "Vf6_ecr_info_relation";
	private static final String ECR_META_DATA_SHEET_NAME = "VF_SYSTEM_SUMMARY";
	private static final String ECR_BOM_DATA_SHEET_NAME = "VF_SYSTEM_BOM";
	private static final int ECR_META_DATA_START_ROW_INDEX = 1;
	private static final int ECR_BOM_DATA_START_COL_INDEX = 0;
	private static final String WRONG_TEMPLATE_ERROR_MSG = "Uploaded Excel file is not a valid one. Please download the valid one from the Download button.";

	private static TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	private File excelTemplate;
	private ECRInput ecrInput;
	private TCComponent ecrRev;
	private TCComponent ecrItem;
	private TCComponent ecrForm;
	private Set<String> ecrMetadataHeaders;
	private Set<String> ecrBOMHeaders;
	private TCComponentDataset excelDS;

	DataManagementService dms;

	public ECRInputManagement(TCComponent ecrItem, TCComponent ecrRev) throws TCException, Exception {
		dms = DataManagementService.getService(session);
		this.ecrItem = ecrItem;
		this.ecrRev = ecrRev;
		this.ecrInput = ECRInput.createEmptyInput(ecrItem, ecrRev);
		this.ecrMetadataHeaders = new LinkedHashSet<String>();
		this.ecrBOMHeaders = new LinkedHashSet<String>();
	}

	public ECRInput extractDataFromTemplate(File excelTemplate) throws Exception {
		this.excelTemplate = excelTemplate;

		validateExcelTemplate();
		Map<String, String> attributeNamesAndValues = extractMetaDataFromTemplate();
		List<Map<String, String>> bomAttributesAndValues = extractBOMDataFromTemplate();

		this.ecrInput.setAttributeNamesAndValues(attributeNamesAndValues);
		this.ecrInput.setBomAttributesAndValues(bomAttributesAndValues);
		ECRInput result = new ECRInput(attributeNamesAndValues, bomAttributesAndValues, ecrItem, ecrRev);

		return result;
	}

	private void validateExcelTemplate() throws Exception {
		try (InputStream excelFile = new FileInputStream(excelTemplate);
				XSSFWorkbook wb = new XSSFWorkbook(excelFile);) {
			XSSFSheet bomdataSheet = wb.getSheet(ECR_BOM_DATA_SHEET_NAME);
			XSSFSheet metadataSheet = wb.getSheet(ECR_META_DATA_SHEET_NAME);
			
			boolean []hasEndCellForBOMData = new boolean[] { false };
			boolean []hasEndCellForMetaData = new boolean[] { false };
			
			if (bomdataSheet == null || metadataSheet == null) {
				throw new Exception(WRONG_TEMPLATE_ERROR_MSG);
			}
			
			bomdataSheet.forEach(row -> {
				int lastCellNum = row.getLastCellNum();
				for (int cellIndex = row.getFirstCellNum(); cellIndex < lastCellNum; cellIndex++) {
					Cell cell = row.getCell(cellIndex);
					if (cell != null) {
						String cellVal = getCellStringValue(cell);
						if (cellVal.compareToIgnoreCase(VF_END_BOM_ATTR_CELL_VALUE) == 0) {
							hasEndCellForBOMData[0] = true;
							break;
						}
					}
				}
			});
			
			metadataSheet.forEach(row -> {
				int lastCellNum = row.getLastCellNum();
				for (int cellIndex = row.getFirstCellNum(); cellIndex < lastCellNum; cellIndex++) {
					Cell cell = row.getCell(cellIndex);
					if (cell != null) {
						String cellVal = getCellStringValue(cell);
						if (cellVal.compareToIgnoreCase(VF_END_META_ATTR_CELL_VALUE) == 0) {
							hasEndCellForMetaData[0] = true;
							break;
						}
					}
				}
			});
			
			if (hasEndCellForBOMData[0] == false || hasEndCellForMetaData[0] == false) {
				throw new Exception(WRONG_TEMPLATE_ERROR_MSG);
			}
		}
	}

	private List<Map<String, String>> extractBOMDataFromTemplate() throws Exception {

		List<Map<String, String>> bomAttributesAndValues = new LinkedList<Map<String, String>>();
		try {
			FileInputStream fileStream = new FileInputStream(excelTemplate);
			XSSFWorkbook wb = new XSSFWorkbook(fileStream);
			XSSFSheet sheet = wb.getSheet(ECR_BOM_DATA_SHEET_NAME);
			int[] bomAttributeRowIndex = new int[] { -1 };
			int[] bomAttributeLastColIndex = new int[] { -1 };
			sheet.forEach(row -> {
				int lastCellNum = row.getLastCellNum();
				for (int cellIndex = row.getFirstCellNum(); cellIndex < lastCellNum; cellIndex++) {
					Cell cell = row.getCell(cellIndex);
					if (cell != null) {
						String cellVal = getCellStringValue(cell);
						if (cellVal.compareToIgnoreCase(VF_END_BOM_ATTR_CELL_VALUE) == 0) {
							bomAttributeLastColIndex[0] = cell.getColumnIndex();
							bomAttributeRowIndex[0] = cell.getRowIndex();
							break;
						}
					}
				}
			});

			boolean isAllRowValsEmpty = false;
			XSSFRow bomAttributeRow = sheet.getRow(bomAttributeRowIndex[0]);
			for (int attributeColIndex = ECR_BOM_DATA_START_COL_INDEX; attributeColIndex < bomAttributeLastColIndex[0]; attributeColIndex++) {
				XSSFCell bomAttributeNameCell = bomAttributeRow.getCell(attributeColIndex);
				String bomAttributeName = getCellStringValue(bomAttributeNameCell);
				ecrBOMHeaders.add(bomAttributeName);
			}

			for (int myRowIndex = bomAttributeRowIndex[0] + 1; isAllRowValsEmpty == false; myRowIndex++) {
				XSSFRow bomValuesRow = sheet.getRow(myRowIndex);
				Map<String, String> bomAttributesAndVals = new HashMap<String, String>();
				isAllRowValsEmpty = true;
				for (int myColIndex = ECR_BOM_DATA_START_COL_INDEX; myColIndex < bomAttributeLastColIndex[0]; myColIndex++) {
					XSSFCell bomValCell = bomValuesRow.getCell(myColIndex);
					if (bomValCell != null) {
						// System.out.println(myRowIndex + " - " + myColIndex);
						String bomVal = getCellStringValue(bomValCell);
						Cell bomAttributeCell = bomAttributeRow.getCell(myColIndex);
						String bomAttribute = getCellStringValue(bomAttributeCell);
						if (bomVal.compareTo("VF_EMPTY") != 0) {
							isAllRowValsEmpty = false;
							bomAttributesAndVals.put(bomAttribute, bomVal);
						}
					}
				}

				if (isAllRowValsEmpty == false) {
					bomAttributesAndValues.add(bomAttributesAndVals);
				} else {
					break;
				}
			}

			wb.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bomAttributesAndValues;
	}

	private String getCellStringValue(Cell cell) {
		String cellVal = "";

		if (cell.getCellType() == CellType.FORMULA) {
			switch (cell.getCachedFormulaResultType()) {
			case BOOLEAN:
				boolean booleanVal = cell.getBooleanCellValue();
				cellVal = String.valueOf(booleanVal);
				break;
			case NUMERIC:
				double doubleVal = cell.getNumericCellValue();
				cellVal = String.valueOf(doubleVal);

				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					Date dateVal = cell.getDateCellValue();
					SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
					cellVal = df.format(dateVal);
				}

				break;
			case STRING:
				cellVal = cell.getRichStringCellValue().getString();
				break;
			default:
				break;
			}
		} else {
			cellVal = cell.toString();
		}

		return cellVal;
	}

	private Map<String, String> extractMetaDataFromTemplate() throws Exception {
		Map<String, String> attributeNamesAndValues = new HashMap<String, String>();

		if (excelTemplate != null) {
			try (InputStream excelFile = new FileInputStream(excelTemplate);
					XSSFWorkbook wb = new XSSFWorkbook(excelFile);) {
				XSSFSheet summarySheet = wb.getSheet(ECR_META_DATA_SHEET_NAME);
				int[] attributeColIndex = new int[] { -1 };
				int[] valueColIndex = new int[] { -1 };
				int[] endRowIndex = new int[] { -1 };
				if (summarySheet != null) {
					summarySheet.forEach(row -> {
						int lastCellNum = row.getLastCellNum();
						for (int cellIndex = row.getFirstCellNum(); cellIndex < lastCellNum; cellIndex++) {
							Cell cell = row.getCell(cellIndex);
							if (cell != null) {
								String cellVal = getCellStringValue(cell);
								if (cellVal.compareToIgnoreCase(VF_END_META_ATTR_CELL_VALUE) == 0) {
									attributeColIndex[0] = cell.getColumnIndex();
									valueColIndex[0] = attributeColIndex[0] + 1;
									endRowIndex[0] = cell.getRowIndex();
									break;
								}
							}
						}
					});

					for (int rowInx = ECR_META_DATA_START_ROW_INDEX; rowInx < endRowIndex[0]; rowInx++) {
						XSSFRow row = summarySheet.getRow(rowInx);
						if (row != null) {
							XSSFCell attributeNameCell = row.getCell(attributeColIndex[0]);
							XSSFCell valCell = row.getCell(valueColIndex[0]);
							if (attributeNameCell != null) {
								String attributeName = getCellStringValue(attributeNameCell);
								String valToSet = "";
								ecrMetadataHeaders.add(attributeName);
								if (valCell != null) {
									String val = getCellStringValue(valCell);
									if (attributeName != null && attributeName.length() > 0 && val != null
											&& val.isEmpty() == false) {
										valToSet = val;
									}
								}
								
								attributeNamesAndValues.put(attributeName, valToSet);
							}
						}
					}

					wb.close();
				} else {
					throw new Exception("Cannot find sheet \"" + ECR_META_DATA_SHEET_NAME + "\"");
				}
			}
		}

		return attributeNamesAndValues;
	}

	private void saveSummaryData() throws TCException {
		Map<String, String> attributeNamesAndVals = ecrInput.getAttributeNamesAndValues();
		List<TCComponent> listComp = new LinkedList<TCComponent>();
		listComp.add(ecrInput.getEcrRev());
		TCProperty[][] propsToSet = TCComponentType.getTCPropertiesSet(listComp,
				attributeNamesAndVals.keySet().toArray(new String[0]));
		for (TCProperty prop : propsToSet[0]) {
			TCPropertyDescriptor propDesc = prop.getDescriptor();
			int serverPropType = propDesc.getType();
			String attributeName = propDesc.getName();
			if (propDesc.isArray()) {
				if (serverPropType == TCPropertyDescriptor.CLIENT_PROP_TYPE_string
						|| serverPropType == TCPropertyDescriptor.CLIENT_PROP_TYPE_char) {
					System.out.println("SERVER_PROP_string array");

				}
			} else {
				System.out.println(propDesc.getServerPropertyType());
				if (serverPropType == TCPropertyDescriptor.SERVER_PROP_int) {
					System.out.println("SERVER_PROP_int");

					String intStr = attributeNamesAndVals.get(attributeName);
					if (!intStr.isEmpty()) {
						int intVal = -1;
						try {
							intVal = Integer.parseInt(intStr);
						} catch (NumberFormatException ex) {
							ex.printStackTrace();
							intVal = (int) Double.parseDouble(intStr);
						}

						attributeNamesAndVals.put(attributeName, String.valueOf(intVal));
					}
				} else if (serverPropType == TCPropertyDescriptor.CLIENT_PROP_TYPE_double
						|| serverPropType == TCPropertyDescriptor.CLIENT_PROP_TYPE_float) {
					System.out.println("SERVER_PROP_double");

					String doubleStr = attributeNamesAndVals.get(attributeName);
					if (!doubleStr.isEmpty()) {
						double floatVal = Double.parseDouble(doubleStr);
						attributeNamesAndVals.put(attributeName, String.valueOf(floatVal));
					}
				} else if (serverPropType == TCPropertyDescriptor.SERVER_PROP_date) {
					System.out.println("SERVER_PROP_date");

				} else if (serverPropType == TCPropertyDescriptor.CLIENT_PROP_TYPE_string
						|| serverPropType == TCPropertyDescriptor.CLIENT_PROP_TYPE_char) {
					System.out.println("SERVER_PROP_string");

				}
			}

		}

		Map<String, String> mapToSet = new HashMap<String, String>();
		mapToSet.putAll(ecrInput.getAttributeNamesAndValues());
		if (mapToSet.get("item_id") != null) {
			mapToSet.remove("item_id");
		} else if (mapToSet.get("object_name") != null) {
			mapToSet.remove("object_name");
		}
		
		String vehicleGroup = mapToSet.get("vf6_vehicle_group");
		if (vehicleGroup != null) {
			if (vehicleGroup.equalsIgnoreCase("VFe34"))	{
				vehicleGroup = "SCP";
			} else if(vehicleGroup.equalsIgnoreCase("VFe35_Premium"))	{
				vehicleGroup = "VFe35";
			} else if(vehicleGroup.equalsIgnoreCase("VFe36_Premium") || vehicleGroup.equalsIgnoreCase("VFe36_Sedan"))	{
				vehicleGroup = "VFe36";
			} else if(vehicleGroup.equalsIgnoreCase("VFe34NG"))	{
				vehicleGroup = "VFe34 NG";
			}
			mapToSet.put("vf6_vehicle_group", vehicleGroup);
		}
		ecrRev.setProperties(mapToSet);
	}

	private void saveBOMData() throws Exception {
		List<Map<String, String>> bomAttributesAndValsList = ecrInput.getBomAttributesAndValues();
		if (bomAttributesAndValsList.size() > 0) {
			CreateIn[] createInputs = new CreateIn[bomAttributesAndValsList.size()];
			for (int bomListIndex = 0; bomListIndex < bomAttributesAndValsList.size(); bomListIndex++) {
				Map<String, String> bomAttributesAndVals = bomAttributesAndValsList.get(bomListIndex);
				CreateIn createInput = new CreateIn();
				createInput.data.boName = "Vf6_bom_infomation";
				
				String donorVal = bomAttributesAndVals.get("vf6_donor_vehicle");
				String purchaseLevel = bomAttributesAndVals.get("vf6_purchase_level");
				String maturity = bomAttributesAndVals.get("vf6_maturity_level");
				createInput.data.stringProps.put("vf6_donor_vehicle", donorVal);
				createInput.data.stringProps.put("vf6_purchase_level", purchaseLevel);
				createInput.data.stringProps.put("vf6_maturity_level", maturity);
				
				//createInput.data.stringProps.putAll(bomAttributesAndVals);
				
				createInputs[bomListIndex] = createInput;
			}
			DataManagementService dms = DataManagementService.getService(session);
			CreateResponse response = dms.createObjects(createInputs);

			if (response.serviceData.sizeOfPartialErrors() == 0 && response.output.length > 0) {
				List<TCComponent> rows = new LinkedList<TCComponent>();
				for (CreateOut output : response.output) {
					rows.addAll(Arrays.asList(output.objects));
				}
				
				int bomListIndex = 0;
				for (TCComponent row : rows) {
					Map<String, String> bomAttributesAndVals = bomAttributesAndValsList.get(bomListIndex++);
					
					try {
						row.setProperties(bomAttributesAndVals);
					} catch (TCException ex) {
						try {
							if (ex.getErrorCode() == 38009) {
								String quantityStr = bomAttributesAndVals.get("vf6_quantity");
								double quantityDouble = Double.parseDouble(quantityStr);
								int quantity = (int) Math.floor(quantityDouble);
								quantityStr = Integer.toString(quantity);
								bomAttributesAndVals.put("vf6_quantity", quantityStr);
								row.setProperties(bomAttributesAndVals);
							}	
						} catch (Exception e) {
							e.printStackTrace();
							throw ex;
						}
					}
				}
				ecrForm.setRelated("vf6_bom_information", rows.toArray(new TCComponent[0]));
				ecrForm.refresh();

			} else {
				String errorMsg = getServiceDataErrorMessages(response.serviceData);
				errorMsg = "Cannot create ECR BOM data as errors below:\n" + errorMsg;
				throw new Exception(errorMsg);
			}
		} else {
			ecrForm.setRelated("vf6_bom_information", new TCComponent[0]);
		}
	}

	private static String getServiceDataErrorMessages(ServiceData sd) {
		StringBuffer sb = new StringBuffer();
		if (sd.sizeOfPartialErrors() > 0) {
			ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
			ErrorValue[] errorValue = errorStack.getErrorValues();
			for (int inx = 0; inx < errorValue.length; inx++) {
				sb.append(errorValue[inx].getMessage()).append("\n");
			}
		}
		return sb.toString();
	}

	private TCComponent creatEcrFormAndAttachToECR(TCComponent ecrRev) throws Exception {
		Map<String, String> stringProps = new HashMap<String, String>();
		
		AIFComponentContext[] obsoleteEcrForms = ecrRev.getRelated(ECRInputManagement.ECR_FORM_RELATION);
		
		String ecrRevName = ecrRev.getProperty("object_name");
		stringProps.put("object_name", ecrRevName);
		CreateIn input = new CreateIn();
		input.data.boName = ECRInputManagement.ECR_FORM_TYPE;
		input.data.stringProps = stringProps;
		
		Map<String, String> attributeNamesAndVals = ecrInput.getAttributeNamesAndValues();
		if (attributeNamesAndVals.get("vf6cp_market") != null) {
			input.data.stringArrayProps.put("vf6_market", attributeNamesAndVals.get("vf6cp_market").split(","));
		}
		
		stringProps.put("vf6_problem", attributeNamesAndVals.get("vf6cp_problem"));
		stringProps.put("vf6_root_cause", attributeNamesAndVals.get("vf6cp_root_cause"));
		stringProps.put("vf6_solution", attributeNamesAndVals.get("vf6cp_solution"));
		stringProps.put("vf6_vehicle_type", attributeNamesAndVals.get("vf6cp_vehicle_type"));

		CreateResponse res = dms.createObjects(new CreateIn[] { input });

		TCComponent newECRForm = null;
		if (res.output.length > 0 && res.output[0].objects.length > 0) {
			newECRForm = res.output[0].objects[0];
			AIFComponentContext[] oldEcrForm = ecrRev.getRelated(ECRInputManagement.ECR_FORM_RELATION);
			ecrRev.setRelated(ECRInputManagement.ECR_FORM_RELATION, new TCComponent[] { newECRForm });
			
			if (obsoleteEcrForms.length > 0) {
				TCComponent obsoleteForm = (TCComponent) obsoleteEcrForms[0].getComponent();
				String obsoleteFormName = obsoleteForm.getProperty("object_name");
				obsoleteForm.setProperty("object_name", obsoleteFormName + "_VF_OBSOLETE");
			}
			
			cleanOldECRForm(oldEcrForm);
		} else {
			String errorMsg = getServiceDataErrorMessages(res.serviceData);
			throw new Exception("Cannot create ECR form with below errors.\n" + errorMsg);
		}

		return newECRForm;
	}

	private void cleanOldECRForm(AIFComponentContext[] oldEcrForm) {
		if (oldEcrForm.length > 0) {
			try {
				TCComponent oldForm = (TCComponent) oldEcrForm[0].getComponent();
				AIFComponentContext[] oldRows = oldForm.getRelated("vf6_bom_information");
				
				oldForm.setRelated("vf6_bom_information", new TCComponent[] {});
				if (oldRows.length > 0) {
					//DeleteCommand delCmd = new DeleteCommand(oldRows);
					//delCmd.executeModeless();	
				}
				
				oldForm.setProperty("object_name", "VF_ECR_FORM_OBSOLETED");
//				DeleteCommand delCmd = new DeleteCommand(oldEcrForm);
//				delCmd.executeModal();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

//	private TCComponent createEcrForm(TCComponent ecrRev) throws TCException, Exception {
//		AIFComponentContext[] ecrForms = ecrRev.getRelated(ECRInputManagement.ECR_FORM_RELATION);
//		TCComponent ecrForm = null;
//		if (ecrForms.length > 0) {
//			ecrForm = (TCComponent) ecrForms[0].getComponent();
//		} else {
//			ecrForm = creatEcrForm(ecrRev);
//		}
//
//		return ecrForm;
//	}

	public void importDataToTC() throws Exception {
		if (ecrInput.isEmpty() == false) {
			ecrForm = creatEcrFormAndAttachToECR(ecrRev);
			
			saveBOMData();
			saveSummaryData();

			String datasetType = "MSExcelX";
			TCComponentDataset ecrExcel = TCExtension.createDataset(excelTemplate.getName(), datasetType, excelTemplate,
					session);
			excelDS = ecrExcel;
			ecrForm.setRelated(ECR_FORM_AND_EXCEL_DS_RELATION, new TCComponent[] { excelDS });
			ecrRev.add(ECR_REV_AND_EXCEL_DS_RELATION, excelDS);
		}
	}

	public Set<String> getBOMHeaders() {
		return ecrBOMHeaders;
	}

	public Set<String> getMetadataHeaders() {
		return ecrMetadataHeaders;
	}

}