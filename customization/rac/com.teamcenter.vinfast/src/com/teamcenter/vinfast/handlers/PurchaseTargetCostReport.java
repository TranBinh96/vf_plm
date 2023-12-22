package com.teamcenter.vinfast.handlers;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.teamcenter.rac.aif.kernel.AIFSession;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.SearchService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;

import com.teamcenter.services.rac.core._2007_12.Session;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.vf.dialog.FrameWithOneComboBox;

public class PurchaseTargetCostReport extends AbstractHandler {

	private final String[] VINFAST_COSTING_CAR_PROGRAM_VALUE;
	private final String VINFAST_COSTING_CAR_PROGRAM_PREFERENCE = "VF_Costing_Car_Program";
	private final String TOKEN_STRING                           = ";"; 
	private final String TARGET_COST_NAME_SUFFIX				= "_TargetCost";
	private final String PIECE_COST_NAME_SUFFIX					= "_PieceCost";
	
	private static HashMap<String, CostObject> mappingCostObj;
	private static FrameWithOneComboBox frame = null;
	private static TCSession session = null;
	private static TCComponentGroup group = null;
	private static TCComponentRole role = null;
	private static TCComponentUser user = null;
	private static String selectedCarPrg = null;
	private static InterfaceAIFComponent[] targetComp = null;

	private final String PART_NUMBER_HEADER			= "Part Number";
	private final String PART_NAME_HEADER			= "Part Name";
	private final String PROCUREMENT_LEVEL_HEADER	= "Vinfast - Procurement Level";
	private final String DESIGN_ASSUMPTION_HEADER	= "Design Assumption";
	private final String SOR_NUM_HEADER				= "SOR Number";
	private final String SOR_NAME_HEADER			= "SOR Name";
	private final String MODULE_HEADER				= "Module Group";
	private final String CL_HEADER					= "CL";
	private final String BUYER_HEADER				= "Buyer";
	private final String PIECE_COST_TARGET_HEADER	= "Piece Cost Target";
	private final String PIECE_COST_TARGET_CURR_HEADER = "Piece Cost Target Currency";
	private final String EDD_COST_TARGET_HEADR		= "EDD Cost Target";
	private final String EDD_COST_TARGET_CURR_HEADR	= "EDD Cost Target Currency";
	private final String TOOLING_COST_TARGET_HEADER	= "Tooling Cost Target";
	private final String TOOLING_COST_TARGET_CURR_HEADER = "Tooling Cost Target Currency";
	
	private final String TARGET_COST_SHEET = "Target Cost";
	private static String TEMP_DIR;
	private static XSSFWorkbook wb;
	private static LinkedList<String> lsHeader;
	
	public LinkedList<String> getLsHeader() {
		return lsHeader;
	}
	public XSSFWorkbook getWb() {
		return wb;
	}
	
	public PurchaseTargetCostReport() {
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		TCPreferenceService preferenceService = session.getPreferenceService();
		VINFAST_COSTING_CAR_PROGRAM_VALUE = preferenceService.getStringValues(VINFAST_COSTING_CAR_PROGRAM_PREFERENCE);
		lsHeader = new LinkedList<String>();
		lsHeader.add(PART_NUMBER_HEADER);
		lsHeader.add(PART_NAME_HEADER);
		lsHeader.add(PROCUREMENT_LEVEL_HEADER);
		lsHeader.add(DESIGN_ASSUMPTION_HEADER);
		lsHeader.add(SOR_NUM_HEADER);
		lsHeader.add(SOR_NAME_HEADER);
		lsHeader.add(MODULE_HEADER);
		lsHeader.add(CL_HEADER);
		lsHeader.add(BUYER_HEADER);
		lsHeader.add(PIECE_COST_TARGET_HEADER);
		lsHeader.add(PIECE_COST_TARGET_CURR_HEADER);
		lsHeader.add(EDD_COST_TARGET_HEADR);
		lsHeader.add(EDD_COST_TARGET_CURR_HEADR);
		lsHeader.add(TOOLING_COST_TARGET_HEADER);
		lsHeader.add(TOOLING_COST_TARGET_CURR_HEADER);
		
		TEMP_DIR = System.getenv("tmp");
	}
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mappingCostObj = new HashMap<String, CostObject>();
				wb = new XSSFWorkbook();
				
				targetComp = AIFUtility.getCurrentApplication().getTargetComponents();

				session = (TCSession) AIFUtility.getCurrentApplication().getSession();
				group = session.getCurrentGroup();
				role = session.getCurrentRole();
				user = session.getUser();
				
				ArrayList<String> carPrgValid = grantAccessBaseOnGroup(group.toString());
				
				if (carPrgValid.size() <= 0) {
					 MessageBox.post("You do not have permission to perform this operation. Please contact Administrator.", "Not Authorized", MessageBox.ERROR);
					 return;
				} else {
					createDialog(carPrgValid);
				}
				
			}
		});
		return null;
	}

	@SuppressWarnings("unchecked")
	public void createDialog(ArrayList<String> carPrgValid) {

		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/KIT.png"));
		Icon ok_Icon = new ImageIcon(getClass().getResource("/icons/ok.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));

		frame = new FrameWithOneComboBox();
		frame.setTitle("Target Cost Report");
		frame.setIconImage(frame_Icon.getImage());
		frame.setMinimumSize(new Dimension(500, 150));

		frame.label1.setText("CHOOSE CAR PROGRAM:");
		frame.btnLeft.setIcon(ok_Icon);
		frame.btnLeft.setText("OK");
		
		frame.btnRight.setIcon(cancel_Icon);
		frame.btnRight.setText("Cancel");
		
		for (String tu : carPrgValid) {
			frame.comboBox1.addItem(tu);
		}

		frame.btnLeft.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectedCarPrg = frame.comboBox1.getSelectedItem().toString();
				if (selectedCarPrg == null) {
					MessageBox.post("Please select car program.", "Error", MessageBox.ERROR);
					return;
				}
//				// TODO call function query data
				
				Job job = new Job("Creating Report") {
					@Override
					protected IStatus run(IProgressMonitor arg0) {
						// TODO call function query data
						IStatus status = new IStatus() {
							
							@Override
							public boolean matches(int arg0) {
								// TODO Auto-generated method stub
								return false;
							}
							
							@Override
							public boolean isOK() {
								// TODO Auto-generated method stub
								return false;
							}
							
							@Override
							public boolean isMultiStatus() {
								// TODO Auto-generated method stub
								return false;
							}
							
							@Override
							public int getSeverity() {
								// TODO Auto-generated method stub
								return 0;
							}
							
							@Override
							public String getPlugin() {
								// TODO Auto-generated method stub
								return null;
							}
							
							@Override
							public String getMessage() {
								// TODO Auto-generated method stub
								return null;
							}
							
							@Override
							public Throwable getException() {
								// TODO Auto-generated method stub
								return null;
							}
							
							@Override
							public int getCode() {
								// TODO Auto-generated method stub
								return 0;
							}
							
							@Override
							public IStatus[] getChildren() {
								// TODO Auto-generated method stub
								return null;
							}
						};
						frame.btnLeft.setEnabled(false);
						
						ArrayList<String> sourcingList = querySourcingByProgram(selectedCarPrg);
						queryCostingData(sourcingList);
						publishReport();
						
						frame.dispose();
						return status;
					}
				};
				session.queueOperation(job);
			}

		});

		frame.btnRight.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}

		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	ArrayList<String> grantAccessBaseOnGroup(String userGroup) {
		ArrayList<String> listCarPrgAccepted = new ArrayList<String>();
		for(String oneValue : VINFAST_COSTING_CAR_PROGRAM_VALUE) {
			String[] grpAcceptable = oneValue.split(TOKEN_STRING);
			if(Arrays.asList(grpAcceptable).contains(userGroup)) {
				listCarPrgAccepted.add(grpAcceptable[0]);
			}
		}
		return listCarPrgAccepted;
	}
	
	private void queryCostingData(ArrayList<String> sourcingItms) {
		
		StringBuilder targetCostNames = new StringBuilder();
		for(String partNum : sourcingItms) {
			targetCostNames.append(partNum).append(TARGET_COST_NAME_SUFFIX).append(TOKEN_STRING);
		}
		
		TCComponent[] targetComp = queryCostFormByName(targetCostNames.toString(), "Target Cost Form");
//		TCComponent[] pieceComp = queryCostFormByName(pieceCostNames, "Piece Cost Form");
		
		/*parse information of target cost form*/
		if(targetComp != null) {
			for(int i = 0; i < targetComp.length; ++i) {
				TCComponentForm compForm = (TCComponentForm)targetComp[i];
				try {
					String formName = compForm.getProperty("object_name");
					String partNum 			= "";
					String EDDCostTarget 		= "";
					String EDDCostTargetCurr 	= "";
					String toolingCostTarget	= "";
					String toolingCostTargetCurr = "";
					String pieceCostTarget		= "";
					String pieceCostTargetCurr  = "";
					if(formName != null) {
						String[] splitFormName = formName.split("_");
						partNum = splitFormName[0];
						if(compForm.getProperty("vf4_ednd_cost_value_target") != null) {
							EDDCostTarget = compForm.getProperty("vf4_ednd_cost_value_target");
						}
						if(compForm.getProperty("vf4_ednd_cost_curr_target") != null) {
							EDDCostTargetCurr = compForm.getProperty("vf4_ednd_cost_curr_target");
						}
						if(compForm.getProperty("vf4_tooling_invest_target") != null) {
							toolingCostTarget = compForm.getProperty("vf4_tooling_invest_target");
						}
						if(compForm.getProperty("vf4_tooling_invest_curr_tar") != null) {
							toolingCostTargetCurr = compForm.getProperty("vf4_tooling_invest_curr_tar");
						}
						if(compForm.getProperty("vf4_piece_cost_value_target") != null) {
							pieceCostTarget = compForm.getProperty("vf4_piece_cost_value_target");
						}
						if(compForm.getProperty("vf4_piece_cost_curr_target") != null) {
							pieceCostTargetCurr = compForm.getProperty("vf4_piece_cost_curr_target");
						}
						CostObject dataReport = mappingCostObj.get(partNum);
						if(dataReport != null) {
							dataReport.setEDDCostTarget(EDDCostTarget);
							dataReport.setEDDCostTargetCurr("USD");
							dataReport.setToolingCostTarget(toolingCostTarget);
							dataReport.setToolingCostTargetCurr("USD");
							dataReport.setPieceCostTarget(pieceCostTarget);
							dataReport.setPieceCostTargetCurr("USD");
						}
					}
					else
					{
						continue;
					}
					
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	private TCComponent[] queryCostFormByName(String allNamesInOne, String costType) {
		TCComponent[] objects = null;
		try {
			SavedQueryService QRservices = SavedQueryService.getService(session);
			FindSavedQueriesCriteriaInput qry[] = new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey = new FindSavedQueriesCriteriaInput();
			String name[] = { "General..." };
			String desc[] = { "" };
			qurey.queryNames = name;
			qurey.queryDescs = desc;
			qurey.queryType = 0;

			qry[0] = qurey;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput qc = new SavedQueryInput();
			SavedQueryInput qc_v[] = new SavedQueryInput[1];

			qc.query = (TCComponentQuery) result[0];
			qc.entries = new String[] { "Name", "Type" };
			qc.values = new String[] { allNamesInOne, costType};
			qc_v[0] = qc;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

			SavedQueryResults[] results = responce.arrayOfResults;

			if(results[0].numOfObjects!=0){
				objects = results[0].objects;
			}
			else
			{
				System.out.println("NO Cost Object FOUND");
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objects;
	}
	private ArrayList<String> querySourcingByProgram(String carProgram) {
		ArrayList<String> souringPart = new ArrayList<String>();
		TCComponentItemRevision itemRev = null;
		try {

			SavedQueryService QRservices = SavedQueryService.getService(session);
			FindSavedQueriesCriteriaInput qry[] = new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey = new FindSavedQueriesCriteriaInput();
			String name[] = { "Source Part" };
			String desc[] = { "" };
			qurey.queryNames = name;
			qurey.queryDescs = desc;
			qurey.queryType = 0;

			qry[0] = qurey;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput qc = new SavedQueryInput();
			SavedQueryInput qc_v[] = new SavedQueryInput[1];

			qc.query = (TCComponentQuery) result[0];
			qc.entries = new String[] { "Sourcing Program" };
			qc.values = new String[] { carProgram };
			qc_v[0] = qc;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

			SavedQueryResults[] results = responce.arrayOfResults;
			String partNum			= "";
			String partName 		= "";
			String procurementLvl 	= "";
			String changeIndex		= "";
			String sorNum			= "";
			String sorName			= "";
			String moduleName		= "";
			String CL				= "";
			String buyer			= "";
			
			if(results[0].numOfObjects!=0){
				TCComponent[] tcComp = results[0].objects;
				for(int i = 0; i < tcComp.length; ++i) {
					itemRev = (TCComponentItemRevision)tcComp[i];
					if(itemRev != null) {
						if(itemRev.getProperty("vf4_bom_vfPartNumber") != null) {
							partNum = itemRev.getProperty("vf4_bom_vfPartNumber");
							souringPart.add(partNum);
						}else {
							continue;
						}
						if(itemRev.getProperty("object_name") != null) {
							partName = itemRev.getProperty("object_name");
						}
						if(itemRev.getProperty("vf4_purchasing_level") != null) {
							procurementLvl  = itemRev.getProperty("vf4_purchasing_level");
							if(procurementLvl.compareTo("DEL") == 0)
							{
								continue;
							}
						}
						if(itemRev.getProperty("vf4_bom_change_index") != null) {
							changeIndex = itemRev.getProperty("vf4_bom_change_index");
						}
						if(itemRev.getProperty("vf4_cad_sor_number") != null) {
							sorNum = itemRev.getProperty("vf4_cad_sor_number");
						}
						if(itemRev.getProperty("vf4_cad_sor_name") != null) {
							sorName = itemRev.getProperty("vf4_cad_sor_name");
						}
						if(itemRev.getProperty("vf4_module_group_bom") != null) {
							moduleName = itemRev.getProperty("vf4_module_group_bom");
						}
						if(itemRev.getProperty("vf4_cl") != null) {
							CL = itemRev.getProperty("vf4_cl");
						}
						if(itemRev.getProperty("vf4_buyer") != null) {
							buyer = itemRev.getProperty("vf4_buyer");
						}
						CostObject dataReport = new CostObject();
						dataReport.setPartNum(partNum);
						dataReport.setPartName(partName);
						dataReport.setDesignAssumption(changeIndex);
						dataReport.setProcurementLvl(procurementLvl);
						dataReport.setCL(CL);
						dataReport.setBuyer(buyer);
						dataReport.setSorName(sorName);
						dataReport.setSorNum(sorNum);
						dataReport.setModuleName(moduleName);
						mappingCostObj.put(partNum, dataReport);
					}
				}
			}
			else
			{
				System.out.println("NO Sourcing Object FOUND");
			}
		} catch (ServiceException | TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return souringPart;
	}
	
	public void writeHeaderLine(String sheetName, LinkedList<String> stDisplayList)
	{
	   XSSFSheet spreadsheet = wb.createSheet(sheetName);
	   Font headerFont = wb.createFont();
	   //headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
	   headerFont.setBold(true);
	   CellStyle headerCellStyle = wb.createCellStyle();
	   headerCellStyle.setFont(headerFont);
	   //headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
	   headerCellStyle.setAlignment(HorizontalAlignment.CENTER);headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	   headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
	   //headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	   headerCellStyle.setFillBackgroundColor(IndexedColors.PALE_BLUE.getIndex());
	   headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	   XSSFRow  headerRow = spreadsheet.createRow(0);
	   
	   int counter = 0;
	   	   
	   for (String temp : stDisplayList) {
		   getHeaderCell(counter, headerRow, temp, headerCellStyle);
		   counter++;
		   }
	   wb.getSheet(sheetName).createFreezePane(0, 1);
	}
	
	private XSSFCell getHeaderCell(int id, XSSFRow headerRow, String displayValue, CellStyle headerCellStyle)
	{
		 XSSFCell cell = headerRow.createCell(id);
		 cell.setCellValue(displayValue);
		 cell.setCellStyle(headerCellStyle);
		 return cell;
	}
	
	public void writeARowReport() {

		// TODO loop propmap and write file
		XSSFSheet spreadsheet = wb.getSheet(this.TARGET_COST_SHEET);
		int rownum = 1;
		XSSFWorkbook wb = spreadsheet.getWorkbook();
		XSSFCellStyle highlight = wb.createCellStyle();
		highlight.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		highlight.setFillForegroundColor(IndexedColors.RED.getIndex());

		for (Map.Entry entry : mappingCostObj.entrySet()) {
			CostObject obj = (CostObject) entry.getValue();
			XSSFRow row = spreadsheet.createRow(rownum);
			
			XSSFCell partNumCell = row.createCell(0);
			partNumCell.setCellValue(obj.getPartNum());
			
			XSSFCell partNameCell = row.createCell(1);
			partNameCell.setCellValue(obj.getPartName());
			
			XSSFCell procurementCell = row.createCell(2);
			procurementCell.setCellValue(obj.getProcurementLvl());
			
			XSSFCell designAssumCell = row.createCell(3);
			designAssumCell.setCellValue(obj.getDesignAssumption());
			
			XSSFCell sorNumCell = row.createCell(4);
			sorNumCell.setCellValue(obj.getSorNum());
			
			XSSFCell sorNameCell =  row.createCell(5);
			sorNameCell.setCellValue(obj.getSorName());
			
			XSSFCell moduleCell =  row.createCell(6);
			moduleCell.setCellValue(obj.getModuleName());
			
			XSSFCell clCell =  row.createCell(7);
			clCell.setCellValue(obj.getCL());
			
			XSSFCell buyerCell =  row.createCell(8);
			buyerCell.setCellValue(obj.getBuyer());
			
			XSSFCell pieceCostCell = row.createCell(9);
			pieceCostCell.setCellValue(obj.getPieceCostTarget());
			
			XSSFCell pieceCostCurrCell = row.createCell(10);
			pieceCostCurrCell.setCellValue(obj.getPieceCostTargetCurr());
			
			XSSFCell eddCostCell = row.createCell(11);
			eddCostCell.setCellValue(obj.getEDDCostTarget());
			
			XSSFCell eddCostCurrCell = row.createCell(12);
			eddCostCurrCell.setCellValue(obj.getEDDCostTargetCurr());
			
			XSSFCell toolCostCell = row.createCell(13);
			toolCostCell.setCellValue(obj.getToolingCostTarget());
			
			XSSFCell toolCostCurrCell = row.createCell(14);
			toolCostCurrCell.setCellValue(obj.getToolingCostTargetCurr());
			
			rownum++;
		}

		for (int kz = 0; kz < lsHeader.size(); kz++) {
			spreadsheet.autoSizeColumn(kz);
		}
	}
	
	private void publishReport() {
		try {
			String outputFile = PurchaseTargetCostReport.TEMP_DIR + "Target_Cost_" + System.currentTimeMillis() + ".xlsx";
			FileOutputStream fos = new FileOutputStream(outputFile);
			
			writeHeaderLine(TARGET_COST_SHEET, lsHeader);
			writeARowReport();
			PurchaseTargetCostReport.wb.write(fos);
			fos.close();
			
			File report = new File(outputFile);
			report.setWritable(false);
			Desktop.getDesktop().open(report);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
