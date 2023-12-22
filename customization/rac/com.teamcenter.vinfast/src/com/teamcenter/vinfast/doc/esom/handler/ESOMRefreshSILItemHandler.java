package com.teamcenter.vinfast.doc.esom.handler;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.common.actions.CheckoutEditAction;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.vinfast.utils.VFNotSupportException;

public class ESOMRefreshSILItemHandler extends AbstractHandler {
	
	public class SILItem {
		String silNumber;
		String silName;
		String severity;
		String status;
		String validation;
		String riskAssess;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		//assume this is ESOM Revision
		//check to make sure one 
		InterfaceAIFComponent[] selectedObjects = AIFUtility.getCurrentApplication().getTargetComponents();
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		
		try {
			if (selectedObjects.length == 1) {
				
				TCComponent esomRev = (TCComponent)selectedObjects[0];
				
				//check access write
				TCAccessControlService acl = session.getTCAccessControlService();
				boolean hasWriteAccess = acl.checkPrivilege(esomRev, "WRITE");
				if(!hasWriteAccess) {
					MessageBox.post("You don't have access write on this ESOM item", "WARNING", MessageBox.WARNING);
					return null;
				}
				
				//get all ecr item in relation
				AIFComponentContext[] silFiles = esomRev.getRelated("VF4_SIL_Items");
				
				if(silFiles.length == 0) {
					return null;
				}
				
				if(silFiles.length > 1) {
					MessageBox.post("Not allow multiple files in SIL Items folder", "INFORMATION", MessageBox.INFORMATION);
					return null;
				}
				
				File excelFile = null;
				TCComponentDataset excel = (TCComponentDataset)silFiles[0].getComponent();
				if(excel!=null){
					TCComponent[] namedRef = excel.getNamedReferences();
					TCComponentTcFile file = (TCComponentTcFile)namedRef[0];
					String fileName = file.toString();
					String fileExtension = fileName.substring(fileName.lastIndexOf("."));
					
					String reportFileName = "SIL_items_" + System.currentTimeMillis();
					excelFile = file.getFile(System.getenv("tmp"), reportFileName  + fileExtension);
					//excelFile = file.getFmsFile();
				}
				if(excelFile == null) {
					return null;
				}
				
				FileInputStream fis = new FileInputStream(excelFile);
	            Workbook workbook = new XSSFWorkbook(fis);
	            Sheet datatypeSheet = workbook.getSheetAt(0);
	            Iterator<Row> iterator = datatypeSheet.iterator();
	            int rowCounter = 1;
	            int silNumIndex = -1;
	            int silNameIndex = -1;
	            int silSeverityIndex = -1;
	            int silStatusIndex = -1;
	            int silValidationIndex = -1;
	            List<SILItem> sils =  new LinkedList<SILItem>();
	            while (iterator.hasNext()) {
	                Row currentRow = iterator.next();
	                Iterator<Cell> cellIterator = currentRow.iterator();
	                int cellCounter = 1;
	                if(rowCounter == 1) {
	                	//get index for each column
	                	while (cellIterator.hasNext()) {
	                		Cell currentCell = cellIterator.next();
	                		if (currentCell.getCellType() == CellType.STRING) {
	                			if(currentCell.getStringCellValue().compareToIgnoreCase("SIL Number") == 0) {
	                				silNumIndex = currentCell.getColumnIndex();
	                			}else if(currentCell.getStringCellValue().compareToIgnoreCase("SIL Name") == 0) {
	                				silNameIndex = currentCell.getColumnIndex();
	                			}else if(currentCell.getStringCellValue().compareToIgnoreCase("Severity") == 0) {
	                				silSeverityIndex = currentCell.getColumnIndex();
	                			}else if(currentCell.getStringCellValue().compareToIgnoreCase("Status") == 0) {
	                				silStatusIndex = currentCell.getColumnIndex();
	                			}else if(currentCell.getStringCellValue().compareToIgnoreCase("Validation") == 0) {
	                				silValidationIndex = currentCell.getColumnIndex();
	                			}
	                		}
	                		cellCounter++;
	                	}
	                	if(silNumIndex == -1 || silNameIndex == -1 || silSeverityIndex == -1 || silStatusIndex == -1 || silValidationIndex == -1) {
	                		MessageBox.post("SIL Items file is incorrect format", "INFORMATION", MessageBox.INFORMATION);
	                		return null;
	                	}
	                }else {
	                	SILItem item = new SILItem();
	                	item.silNumber = currentRow.getCell(silNumIndex).getStringCellValue().trim();
	                	item.silName = currentRow.getCell(silNameIndex).getStringCellValue().trim();
	                	item.severity = currentRow.getCell(silSeverityIndex).getStringCellValue().trim();
	                	item.validation = currentRow.getCell(silValidationIndex).getStringCellValue().trim();
	                	item.status = currentRow.getCell(silStatusIndex).getStringCellValue().trim();
	                	if(item.silNumber.isEmpty() || item.silName.isEmpty() || item.severity.isEmpty() || item.validation.isEmpty() || item.status.isEmpty()) {
	                		MessageBox.post("All information SIL Number, SIL Name, Severity, Validation, Status are mandantory", "INFORMATION", MessageBox.INFORMATION);
	                		return null;
	                	}
	                	//set value risk assessment
	                	if(item.severity.compareToIgnoreCase("B") == 0 || 
	                			item.severity.compareToIgnoreCase("C") == 0 || 
	                			item.severity.compareToIgnoreCase("D") == 0 ||
	                			item.validation.compareToIgnoreCase("Vehicle or Lab Test") == 0) {
	                		item.riskAssess = "MINOR";
	                	}else if(item.validation.compareToIgnoreCase("Virtual Assessment with validated model") == 0) {
	                		item.riskAssess = "MODERATE";
	                	}else {
	                		item.riskAssess = "CRITICAL";
	                	}
	                	sils.add(item);
	                }
	                
	                rowCounter++;
	            }
	            
	            //prepare data to insert to table
	            List<TCComponent> newRows = new LinkedList<TCComponent>();
				if (sils.size() > 0) {
					CreateIn[] createInputs = new CreateIn[sils.size()];
					for (int i = 0; i < sils.size(); i++) {
						SILItem silItem = sils.get(i);
						CreateIn createInput = new CreateIn();
						createInput.data.boName = "VF3_ESOM_SIL";
						createInput.data.stringProps.put("vf3_sil_number", silItem.silNumber);
						createInput.data.stringProps.put("vf3_sil_name", silItem.silName);
						createInput.data.stringProps.put("vf3_status", silItem.status);
						createInput.data.stringProps.put("vf3_validation", silItem.validation);
						createInput.data.stringProps.put("vf3_severity", silItem.severity);
						if(!silItem.riskAssess.isEmpty()) {
							createInput.data.stringProps.put("vf3_risk_assessment", silItem.riskAssess);
						}
						createInputs[i] = createInput;
					}
					DataManagementService dms = DataManagementService.getService(session);
					CreateResponse response = dms.createObjects(createInputs);
					if (response.serviceData.sizeOfPartialErrors() == 0 && response.output.length > 0) {
						newRows = getNewRows(response);
					}
					
				}
				//add to esom
				esomRev.setRelated("vf3_sil_info", newRows.toArray(new TCComponent[0]));
				esomRev.refresh();
				if (esomRev.isCheckedOut() == false && esomRev.okToCheckout()) {
					CheckoutEditAction checkOutAction = new CheckoutEditAction();
					checkOutAction.run();
				}
	            
			} else {
				throw new VFNotSupportException("This feature only supports single part selection!");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			MessageBox.post("There are some errors while processing, please contact to your IT Service Desk.", "Error", MessageBox.WARNING);
		}
		
		return null;
	}

	private List<TCComponent> getNewRows(CreateResponse response) {
		List<TCComponent> newRows = new LinkedList<TCComponent>();
		for (CreateOut output : response.output) {
			newRows.addAll(Arrays.asList(output.objects));
		}
		return newRows;
	}

}
