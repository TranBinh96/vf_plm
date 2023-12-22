package com.teamcenter.vinfast.handlers;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.dialog.SimpleFrame;
import com.vf.utils.Query;

public class PartMaturityLevelReport extends AbstractHandler {
	private final Logger LOGGER;
	private static LinkedHashMap<String, PartMaturityLevelObject> mapMasterData;
	private static ArrayList<String> listPart;
	private static SimpleFrame frame = null;
	private static TCSession session = null;
	private static long startTime = 0;
	private final String PART_NUMBER_HEADER = "Item Number";
	private final String PART_NAME_HEADER = "Item Name";
	private final String MAKE_OR_BUY_HEADER = "Make/Buy";
	private final String P_ERN_NUMBER_HEADER = "P ERN Number";
	private final String P_ERN_RELEASE_DATE_HEADER = "P ERN Release Date";
	private final String I_ERN_NUMBER_HEADER = "I ERN Number";
	private final String I_ERN_RELEASE_DATE_HEADER = "I ERN Release Date";
	private final String PR_ERN_NUMBER_HEADER = "PR ERN Number";
	private final String PR_ERN_RELEASE_DATE_HEADER = "PR ERN Release Date";

	private final String MATURITY_LEVEL_SHEET = "Sheet1";
	private final String DATASET_TEMPLATE_NAME = "Part_Maturity_Level_Report_Template";
	private final String DATASET_TEMPLATE_NAME_TESTING = "Part_Maturity_Level_Report_Template_Testing";
	private final String REPORT_PREFIX = "Part_Maturity_Level_Report_";

	private static String TEMP_DIR;
	private static SXSSFWorkbook wb;
	private static LinkedList<String> lsHeader;

	public LinkedList<String> getLsHeader() {
		return lsHeader;
	}

	public SXSSFWorkbook getWb() {
		return wb;
	}

	public PartMaturityLevelReport() {
		lsHeader = new LinkedList<String>();
		lsHeader.add(PART_NUMBER_HEADER);
		lsHeader.add(PART_NAME_HEADER);
		lsHeader.add(MAKE_OR_BUY_HEADER);
		lsHeader.add(P_ERN_NUMBER_HEADER);
		lsHeader.add(P_ERN_RELEASE_DATE_HEADER);
		lsHeader.add(I_ERN_NUMBER_HEADER);
		lsHeader.add(I_ERN_RELEASE_DATE_HEADER);
		lsHeader.add(PR_ERN_NUMBER_HEADER);
		lsHeader.add(PR_ERN_RELEASE_DATE_HEADER);
		TEMP_DIR = System.getenv("tmp");
		LOGGER = Logger.getLogger(this.getClass());

	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				mapMasterData = new LinkedHashMap<String, PartMaturityLevelObject>();
				// multiMappingCostObj = ArrayListMultimap.create();
				listPart = new ArrayList<String>();

				AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();

				session = (TCSession) app.getSession();

				InterfaceAIFComponent[] bomLine = app.getTargetComponents();

				if (bomLine != null) {
					createDialog(bomLine);
				} else {
					MessageBox.post("Please Select One Bom Line.", "Error", MessageBox.ERROR);
				}

			}
		});
		return null;
	}

	public void createDialog(final InterfaceAIFComponent[] bomLine) {

		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/KIT.png"));
		Icon ok_Icon = new ImageIcon(getClass().getResource("/icons/ok.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));

		frame = new SimpleFrame();
		frame.setTitle("Part Maturity Level Report");
		frame.setIconImage(frame_Icon.getImage());
		frame.setMinimumSize(new Dimension(500, 200));
		frame.label1.setText("<html>The report is based on selected BOM structure.<br/>This action will take few minutes.</html>");
		frame.label1.setHorizontalAlignment(SwingConstants.CENTER);
		frame.btnLeft.setIcon(ok_Icon);
		frame.btnLeft.setText("Continue");

		frame.btnRight.setIcon(cancel_Icon);
		frame.btnRight.setText("Cancel");

		
		frame.btnLeft.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractAIFOperation op = new AbstractAIFOperation() {

					@Override
					public void executeOperation() throws Exception {
						// TODO Auto-generated method stub
						frame.btnLeft.setEnabled(false);
						startTime = System.currentTimeMillis();
						int i = 0;
						for (InterfaceAIFComponent oneLine : bomLine) {
							traverseBOM(i, (TCComponentBOMLine) oneLine);
							i++;
						}
						publishReport();

						frame.dispose();
						return;
					}
				};
				session.queueOperation(op);
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

	@SuppressWarnings("rawtypes")
	public void writeARowReport() {

		// TODO loop propmap and write file
		SXSSFSheet spreadsheet = wb.getSheet(MATURITY_LEVEL_SHEET);
		int rownum = 1;
		SXSSFWorkbook wb = spreadsheet.getWorkbook();

		CellStyle headerCellStyle = wb.createCellStyle();
		headerCellStyle.setAlignment(HorizontalAlignment.LEFT);
		headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerCellStyle.setBorderTop(BorderStyle.THIN);
		headerCellStyle.setBorderBottom(BorderStyle.THIN);
		headerCellStyle.setBorderLeft(BorderStyle.THIN);
		headerCellStyle.setBorderRight(BorderStyle.THIN);

		for (Map.Entry entry : mapMasterData.entrySet()) {
			PartMaturityLevelObject partData = (PartMaturityLevelObject) entry.getValue();
			SXSSFRow row = spreadsheet.createRow(rownum);

			SXSSFCell itemNumCell = row.createCell(0);
			itemNumCell.setCellValue(partData.getPartNumber());
			itemNumCell.setCellStyle(headerCellStyle);

			SXSSFCell itemNameCell = row.createCell(1);
			itemNameCell.setCellValue(partData.getPartName());
			itemNameCell.setCellStyle(headerCellStyle);

			SXSSFCell relStatuses = row.createCell(2);
			relStatuses.setCellValue(partData.getReleaseStatues());
			relStatuses.setCellStyle(headerCellStyle);

			SXSSFCell makeOrBuyCell = row.createCell(3);
			makeOrBuyCell.setCellValue(partData.getMakeOrBuy());
			makeOrBuyCell.setCellStyle(headerCellStyle);

			SXSSFCell PERNNumCell = row.createCell(4);
			PERNNumCell.setCellValue(partData.getP_ERNNumber());
			PERNNumCell.setCellStyle(headerCellStyle);

			SXSSFCell PERNReleaseDateCell = row.createCell(5);
			PERNReleaseDateCell.setCellValue(partData.getP_ERNReleaseDate());
			PERNReleaseDateCell.setCellStyle(headerCellStyle);

			SXSSFCell IERNNumCell = row.createCell(6);
			IERNNumCell.setCellValue(partData.getI_ERNNumber());
			IERNNumCell.setCellStyle(headerCellStyle);

			SXSSFCell IERNReleaseDateCell = row.createCell(7);
			IERNReleaseDateCell.setCellValue(partData.getI_ERNReleaseDate());
			IERNReleaseDateCell.setCellStyle(headerCellStyle);

			SXSSFCell PRERNNumCell = row.createCell(8);
			PRERNNumCell.setCellValue(partData.getPR_ERNNumber());
			PRERNNumCell.setCellStyle(headerCellStyle);

			SXSSFCell PRERNReleaseDateCell = row.createCell(9);
			PRERNReleaseDateCell.setCellValue(partData.getPR_ERNReleaseDate());
			PRERNReleaseDateCell.setCellStyle(headerCellStyle);
			
			SXSSFCell ECRNumberCell = row.createCell(10);
			ECRNumberCell.setCellValue(partData.getPCR_ECRNumber());
			ECRNumberCell.setCellStyle(headerCellStyle);

			SXSSFCell ECRRelDateCell = row.createCell(11);
			ECRRelDateCell.setCellValue(partData.getPCR_ECRReleaseDate());
			ECRRelDateCell.setCellStyle(headerCellStyle);
			
			SXSSFCell ECNNumberCell = row.createCell(12);
			ECNNumberCell.setCellValue(partData.getPCR_ECNNumber());
			ECNNumberCell.setCellStyle(headerCellStyle);
			
			SXSSFCell ECNRelDateCell = row.createCell(13);
			ECNRelDateCell.setCellValue(partData.getPCR_ECNReleaseDate());
			ECNRelDateCell.setCellStyle(headerCellStyle);
			
			SXSSFCell MCNNumberCell = row.createCell(14);
			MCNNumberCell.setCellValue(partData.getMCNNumber());
			MCNNumberCell.setCellStyle(headerCellStyle);
			
			SXSSFCell MCNRelDateCell = row.createCell(15);
			MCNRelDateCell.setCellValue(partData.getMCNReleaseDate());
			MCNRelDateCell.setCellStyle(headerCellStyle);
			
			rownum++;
		}

	}

	private void publishReport() {
		try {
			File report = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET, Query.QUERY_JES_ENTRY_DATASET_NAME, DATASET_TEMPLATE_NAME, TEMP_DIR, REPORT_PREFIX, startTime);

			InputStream fileIn = new FileInputStream(report);
			XSSFWorkbook template = new XSSFWorkbook(fileIn);
			PartMaturityLevelReport.wb = new SXSSFWorkbook(template, 500);

			writeARowReport();
			Runtime.getRuntime().gc();
			FileOutputStream fos = new FileOutputStream(report);
			PartMaturityLevelReport.wb.write(fos);
			fos.close();
			PartMaturityLevelReport.wb.dispose();
			report.setWritable(false);
			Desktop.getDesktop().open(report);

		} catch (IOException | TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void traverseBOM(int key, TCComponentBOMLine bomline) {
		// TODO Auto-generated method stub
		try {
			/* ignore part which has checked */
			String bomlineID = bomline.getProperty("bl_item_item_id");
			if (listPart.contains(bomlineID)) {
				return;
			}
			/* get part information */
			String name = bomline.getProperty("bl_item_object_name");
			String makeOrBuy = bomline.getProperty("bl_item_vf4_item_make_buy");
			String ERNNumber = "";
			String ERNReleaseDate = "";
			StringBuilder ECRNumber = new StringBuilder("");
			StringBuilder ECRReleaseDate = new StringBuilder("");
			StringBuilder ECNNumber = new StringBuilder("");
			StringBuilder ECNReleaseDate = new StringBuilder("");
			StringBuilder MCNNumber = new StringBuilder("");
			StringBuilder MCNReleaseDate = new StringBuilder("");

			StringBuilder relStatuses = new StringBuilder("");
			boolean hasPStatus = false;
			boolean hasIStatus = false;
			boolean hasPRStatus = false;
			boolean hasPCRStatus = false;
			boolean hasICRStatus = false;
			boolean hasPPRStatus = false;
			/* create master data which is input for generate report function */
			PartMaturityLevelObject dataReport = new PartMaturityLevelObject();
			dataReport.setPartNumber(bomlineID);
			dataReport.setPartName(name);
			dataReport.setMakeOrBuy(makeOrBuy);
			mapMasterData.put(bomlineID, dataReport);

			/*
			 * loop all item revision to find latest revision has status P, I,
			 * PR
			 */
			TCComponentItem item = bomline.getItem();
			TCComponent[] itemRevs = item.getRelatedComponents("revision_list");
			for (int i = 0; i < itemRevs.length; ++i) {
				String releaseStatus = itemRevs[i].getProperty("release_status_list");
				String revNumber = itemRevs[i].getProperty("item_revision_id");
				List<String> splitReleaseStatus = Arrays.asList(releaseStatus.split(", "));
				if (!splitReleaseStatus.contains("P") && !splitReleaseStatus.contains("I") && !splitReleaseStatus.contains("PR") && !splitReleaseStatus.contains("PCR")
						&& !splitReleaseStatus.contains("ICR") && !splitReleaseStatus.contains("PPR")) {
					continue;
				} else {
					AIFComponentContext[] aifPrimary = itemRevs[i].getPrimary();
					
					for (int j = 0; j < aifPrimary.length; j++) {
						TCComponent primaryComp = (TCComponent) aifPrimary[j].getComponent();
						if (primaryComp.getType().compareToIgnoreCase("VF4_ERNRevision") == 0) {
							if (splitReleaseStatus.contains("P")) {
								ERNNumber = primaryComp.getProperty("item_id");
								ERNReleaseDate = primaryComp.getProperty("date_released");
								if (Utils.compareDate(mapMasterData.get(bomlineID).getP_ERNReleaseDate(), ERNReleaseDate) >= 0) {
									mapMasterData.get(bomlineID).setP_ERNNumber(ERNNumber);
									mapMasterData.get(bomlineID).setP_ERNReleaseDate(ERNReleaseDate);
								}
								hasPStatus = true;
							} else if (splitReleaseStatus.contains("I")) {
								ERNNumber = primaryComp.getProperty("item_id");
								ERNReleaseDate = primaryComp.getProperty("date_released");
								if (Utils.compareDate(mapMasterData.get(bomlineID).getI_ERNReleaseDate(), ERNReleaseDate) >= 0) {
									mapMasterData.get(bomlineID).setI_ERNNumber(ERNNumber);
									mapMasterData.get(bomlineID).setI_ERNReleaseDate(ERNReleaseDate);
								}
								hasIStatus = true;
							} else if (splitReleaseStatus.contains("PR")) {
								ERNNumber = primaryComp.getProperty("item_id");
								ERNReleaseDate = primaryComp.getProperty("date_released");
								if (Utils.compareDate(mapMasterData.get(bomlineID).getPR_ERNReleaseDate(), ERNReleaseDate) >= 0) {
									mapMasterData.get(bomlineID).setPR_ERNNumber(ERNNumber);
									mapMasterData.get(bomlineID).setPR_ERNReleaseDate(ERNReleaseDate);
								}
								hasPRStatus = true;
							}
						}
						if(splitReleaseStatus.contains("PCR")) {
							hasPCRStatus  = true;
						}
						if(splitReleaseStatus.contains("ICR")) {
							hasICRStatus  = true;
						}
						if(splitReleaseStatus.contains("PPR")) {
							hasPPRStatus  = true;
						}
						if (primaryComp.getType().compareToIgnoreCase("Vf6_ECNRevision") == 0 && !ECNNumber.toString().contains(primaryComp.getProperty("item_id"))) {
							String ecnStatus = primaryComp.getProperty("release_status_list");
							if (!ecnStatus.isEmpty() && ecnStatus.compareToIgnoreCase("Approved") == 0) {
								TCComponent[] solutionItemRev = primaryComp.getRelatedComponents("CMHasSolutionItem");
								for (int y = 0; y < solutionItemRev.length; ++y) {
									if (solutionItemRev[y].getProperty("item_id").compareToIgnoreCase(bomlineID) == 0
											&& solutionItemRev[y].getProperty("item_revision_id").compareToIgnoreCase(revNumber) == 0) {
										
										/*append all ECN - ECR */
										if(ECNNumber.length() > 0) ECNNumber.append(", ");
										ECNNumber.append(primaryComp.getProperty("item_id"));
										if(ECNReleaseDate.length() > 0) ECNReleaseDate.append(", ");
										ECNReleaseDate.append(primaryComp.getProperty("date_released"));
										
										TCComponent ecrRev = primaryComp.getRelatedComponent("CMImplements");
										if(ECRNumber.length() > 0) ECRNumber.append(", ");
										ECRNumber.append(ecrRev.getProperty("item_id"));
										if(ECRReleaseDate.length() > 0) ECRReleaseDate.append(", ");
										ECRReleaseDate.append(ecrRev.getProperty("date_released"));
									}
								}
							}
						}
						if(primaryComp.getType().compareToIgnoreCase("Vf6_MCNRevision") == 0) {
							String mcnStatus = primaryComp.getProperty("release_status_list");
							if(mcnStatus.compareToIgnoreCase("Approved") == 0 || mcnStatus.compareToIgnoreCase("PPR") == 0) {
								TCComponent[] solutionItemRev = primaryComp.getRelatedComponents("EC_solution_item_rel");
								for (int y = 0; y < solutionItemRev.length; ++y) {
									if (solutionItemRev[y].getProperty("item_id").compareToIgnoreCase(bomlineID) == 0
											&& solutionItemRev[y].getProperty("item_revision_id").compareToIgnoreCase(revNumber) == 0) {
										
										/*append all ECN - ECR */
										if(MCNNumber.length() > 0) MCNNumber.append(", ");
										MCNNumber.append(primaryComp.getProperty("item_id"));
										if(MCNReleaseDate.length() > 0) MCNReleaseDate.append(", ");
										MCNReleaseDate.append(primaryComp.getProperty("date_released"));
										
									}
								}
							}
						}
					}
				}
			}

			/* add value for release statuses */
			if (hasPStatus) {
				relStatuses.append("P");
			}
			if (hasIStatus) {
				if (relStatuses.length() > 0)
					relStatuses.append(", ");
				relStatuses.append("I");
			}
			if (hasPRStatus) {
				if (relStatuses.length() > 0)
					relStatuses.append(", ");
				relStatuses.append("PR");
			}
			if(hasPCRStatus) {
				if (relStatuses.length() > 0)
					relStatuses.append(", ");
				relStatuses.append("PCR");
			}
			if(hasICRStatus) {
				if (relStatuses.length() > 0)
					relStatuses.append(", ");
				relStatuses.append("ICR");
			}
			if(hasPPRStatus) {
				if (relStatuses.length() > 0)
					relStatuses.append(", ");
				relStatuses.append("PPR");
			}
			mapMasterData.get(bomlineID).setReleaseStatues(relStatuses.toString());
			mapMasterData.get(bomlineID).setPCR_ECNNumber(ECNNumber.toString());
			mapMasterData.get(bomlineID).setPCR_ECNReleaseDate(ECNReleaseDate.toString());
			mapMasterData.get(bomlineID).setPCR_ECRNumber(ECRNumber.toString());
			mapMasterData.get(bomlineID).setPCR_ECRReleaseDate(ECRReleaseDate.toString());
			mapMasterData.get(bomlineID).setMCNNumber(MCNNumber.toString());
			mapMasterData.get(bomlineID).setMCNReleaseDate(MCNReleaseDate.toString());
			listPart.add(bomlineID);

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
