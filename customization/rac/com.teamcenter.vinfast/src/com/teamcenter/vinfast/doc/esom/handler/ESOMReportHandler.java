package com.teamcenter.vinfast.doc.esom.handler;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.doc.esom.view.ESOMReportDialog;
import com.teamcenter.vinfast.handlers.ProcessReportObject;
import com.teamcenter.vinfast.handlers.ProcessStatusReport;
import com.vf.utils.Query;

public class ESOMReportHandler extends AbstractHandler {
	private static XSSFWorkbook wb;
	private static String selectedPrg = null;
	private ArrayList<String> programList = new ArrayList<String>();
	private final String DEFAULT_DATASET_TEMPLATE_NAME = "VF_ESOM_Report_Template";
	private final String DEFAULT_DATASET_TEMPLATE_NAME_TEST = "VF_ESOM_Report_Template_TEST";
	private static String REPORT_PREFIX = "VF_ESOM_Report_Report_";
	private static String TEMP_DIR;
	private static long startTime = 0;
	private static TCSession session = null;
	private static ESOMReportDialog frame = null;
	private LinkedList<ECRInfo> lstEcrInfo;
	private LinkedList<DVPRInfo> lstDvprInfo;
	private LinkedList<SILInfo> lstSilInfo;

	public ESOMReportHandler() {
		wb = new XSSFWorkbook();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		lstEcrInfo = new LinkedList<ESOMReportHandler.ECRInfo>();
		lstDvprInfo = new LinkedList<ESOMReportHandler.DVPRInfo>();
		lstSilInfo = new LinkedList<ESOMReportHandler.SILInfo>();
		TCPreferenceService preferenceService = session.getPreferenceService();
		String[] vfESOMPrgPre = preferenceService.getStringValues("VF_ESOM_Program");
		for (String aPre : vfESOMPrgPre) {
			programList.add(aPre.split("-")[0].trim());
		}

	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createDialog();

			}
		});
		return null;
	}

	public class ECRInfo {
		public String ECRNumber;
		public String riskAssessment;
		public String comment;
		public String moduleGrp;
		public String project;
		public String esomNumber;
	}

	public class DVPRInfo {
		public String DVPRNumber;
		public String targetMet;
		public String ptgCompleted;
		public String riskAssessment;
		public String comment;
		public String moduleGrp;
		public String project;
		public String esomNumber;
	}

	public class SILInfo {
		public String SILNumber;
		public String SILName;
		public String severity;
		public String status;
		public String validation;
		public String riskAssessment;
		public String moduleGrp;
		public String project;
		public String esomNumber;
	}

	@SuppressWarnings("unchecked")
	public void createDialog() {

		ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/KIT.png"));
		Icon ok_Icon = new ImageIcon(getClass().getResource("/icons/ok.png"));
		Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));

		frame = new ESOMReportDialog();
		frame.setTitle("ESOM Report");
		frame.setIconImage(frame_Icon.getImage());
		frame.setMinimumSize(new Dimension(400, 110));

		frame.btnLeft.setIcon(ok_Icon);
		frame.btnRight.setIcon(cancel_Icon);

		for (String key : programList) {
			frame.cbProgram.addItem(key);
		}

		frame.btnLeft.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectedPrg = frame.cbProgram.getSelectedItem().toString();
				if (selectedPrg == null) {
					MessageBox.post("Please select a program.", "INFO", MessageBox.INFORMATION);
					return;
				}
//				// TODO call function query data
				AbstractAIFOperation op = new AbstractAIFOperation() {

					@Override
					public void executeOperation() throws Exception {
						// TODO Auto-generated method stub
						frame.btnLeft.setEnabled(false);
						startTime = System.currentTimeMillis();

						LinkedHashMap<String, String> queryCriteria = new LinkedHashMap<String, String>();
						queryCriteria.put("Model Code", "*" + selectedPrg + "*");

						TCComponent[] queryResult = Query.queryItem(session, queryCriteria, "__TNH_FindESOM_byModel");
						if (queryResult == null || queryResult.length == 0) {
							MessageBox.post("There are not object has been found", "INFO", MessageBox.INFORMATION);
						} else {
							extractData(queryResult);
							publishReport();
						}

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

	public void extractData(TCComponent[] comps) throws TCException {
		for (TCComponent esomRev : comps) {
			String module = "";
			String model = "";
			String esomId = "";
			try {
				module = esomRev.getPropertyDisplayableValue("vf3_module_name");
				model = esomRev.getPropertyDisplayableValue("vf3_model_code");
				esomId = esomRev.getPropertyDisplayableValue("item_id");
				System.out.println("[extractData] Processing: " + esomId);
				if (esomId.compareTo("ESOMXXEEP0005") == 0) {
					System.out.println(esomId);
				}
			} catch (NotLoadedException e1) {
				e1.printStackTrace();
				continue;
			}
			// TODO extract ecr information
			List<TCComponent> currEcrTable = new LinkedList<TCComponent>(Arrays.asList(esomRev.getReferenceListProperty("vf3_ecr_info")));
			for (int i = 0; i < currEcrTable.size(); i++) {
				try {
					ECRInfo ecrInfo = new ECRInfo();
					TCComponent ecrDetailInfo = currEcrTable.get(i);
					TCComponent ecrItem = ecrDetailInfo.getReferenceProperty("vf3_ecr_item");
					if (ecrItem != null)
						ecrInfo.ECRNumber = ecrItem.getPropertyDisplayableValue("item_id");
					ecrInfo.riskAssessment = ecrDetailInfo.getPropertyDisplayableValue("vf3_risk_assessment");
					ecrInfo.comment = ecrDetailInfo.getPropertyDisplayableValue("vf3_comment");
					ecrInfo.moduleGrp = module;
					ecrInfo.project = model;
					ecrInfo.esomNumber = esomId;
					this.lstEcrInfo.add(ecrInfo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// TODO extract dvpr information
			List<TCComponent> currDVPRTable = new LinkedList<TCComponent>(Arrays.asList(esomRev.getReferenceListProperty("vf3_dvpr_info")));
			for (int i = 0; i < currDVPRTable.size(); i++) {
				try {
					DVPRInfo dvprInfo = new DVPRInfo();
					TCComponent dvprDetailInfo = currDVPRTable.get(i);
					TCComponent dvprItem = dvprDetailInfo.getReferenceProperty("vf3_dvpr_item");
					if (dvprItem != null) {
						dvprInfo.DVPRNumber = dvprItem.getPropertyDisplayableValue("item_id");
					}
					dvprInfo.targetMet = dvprDetailInfo.getPropertyDisplayableValue("vf3_target_met");
					dvprInfo.ptgCompleted = dvprDetailInfo.getPropertyDisplayableValue("vf3_percentage_completed");
					dvprInfo.riskAssessment = dvprDetailInfo.getPropertyDisplayableValue("vf3_risk_assessment");
					dvprInfo.comment = dvprDetailInfo.getPropertyDisplayableValue("vf3_comment");
					dvprInfo.moduleGrp = module;
					dvprInfo.project = model;
					dvprInfo.esomNumber = esomId;
					this.lstDvprInfo.add(dvprInfo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// TODO extract sil information
			List<TCComponent> currSILTable = new LinkedList<TCComponent>(Arrays.asList(esomRev.getReferenceListProperty("vf3_sil_info")));
			for (int i = 0; i < currSILTable.size(); i++) {
				try {
					SILInfo silInfo = new SILInfo();
					TCComponent silDetailInfo = currSILTable.get(i);
					silInfo.SILNumber = silDetailInfo.getPropertyDisplayableValue("vf3_sil_number");
					silInfo.SILName = silDetailInfo.getPropertyDisplayableValue("vf3_sil_name");
					silInfo.severity = silDetailInfo.getPropertyDisplayableValue("vf3_severity");
					silInfo.riskAssessment = silDetailInfo.getPropertyDisplayableValue("vf3_risk_assessment");
					silInfo.status = silDetailInfo.getPropertyDisplayableValue("vf3_status");
					silInfo.validation = silDetailInfo.getPropertyDisplayableValue("vf3_validation");
					silInfo.moduleGrp = module;
					silInfo.project = model;
					silInfo.esomNumber = esomId;
					this.lstSilInfo.add(silInfo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void publishReport() {
		try {
			REPORT_PREFIX = ESOMReportHandler.selectedPrg;
			File report = Query.downloadFirstNameRefOfDataset(session, Query.QUERY_JES_DATASET, Query.QUERY_JES_ENTRY_DATASET_NAME, DEFAULT_DATASET_TEMPLATE_NAME, TEMP_DIR, REPORT_PREFIX, startTime);

			InputStream fileIn = new FileInputStream(report);
			ESOMReportHandler.wb = new XSSFWorkbook(fileIn);

			writeARowReportDefault();
			fileIn.close();
			FileOutputStream fos = new FileOutputStream(report);
			ESOMReportHandler.wb.write(fos);
			fos.close();

			report.setWritable(false);
			Desktop.getDesktop().open(report);

		} catch (IOException | TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public void writeARowReportDefault() {

		// TODO loop propmap and write file
		XSSFSheet ecrSheet = wb.getSheet("ECR");
		XSSFSheet dvprSheet = wb.getSheet("DVPR");
		XSSFSheet silSheet = wb.getSheet("SIL");
		int rownum = 1;
		XSSFWorkbook wb = ecrSheet.getWorkbook();

		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);

		for (ECRInfo ecrInfo : this.lstEcrInfo) {
			XSSFRow row = ecrSheet.createRow(rownum);

			XSSFCell ecrCell = row.createCell(0);
			ecrCell.setCellValue(ecrInfo.ECRNumber);
			ecrCell.setCellStyle(cellStyle);

			XSSFCell riskAsseCell = row.createCell(1);
			riskAsseCell.setCellValue(ecrInfo.riskAssessment);
			riskAsseCell.setCellStyle(cellStyle);

			XSSFCell cmtCell = row.createCell(2);
			cmtCell.setCellValue(ecrInfo.comment);
			cmtCell.setCellStyle(cellStyle);

			XSSFCell moduleCell = row.createCell(3);
			moduleCell.setCellValue(ecrInfo.moduleGrp);
			moduleCell.setCellStyle(cellStyle);

			XSSFCell prjCell = row.createCell(4);
			prjCell.setCellValue(ecrInfo.project);
			prjCell.setCellStyle(cellStyle);

			XSSFCell esomIdCell = row.createCell(5);
			esomIdCell.setCellValue(ecrInfo.esomNumber);
			esomIdCell.setCellStyle(cellStyle);

			rownum++;
		}

		rownum = 1;
		for (DVPRInfo dvpr : this.lstDvprInfo) {
			XSSFRow row = dvprSheet.createRow(rownum);

			XSSFCell dvprCell = row.createCell(0);
			dvprCell.setCellValue(dvpr.DVPRNumber);
			dvprCell.setCellStyle(cellStyle);

			XSSFCell targetMetCell = row.createCell(1);
			targetMetCell.setCellValue(dvpr.targetMet);
			targetMetCell.setCellStyle(cellStyle);

			XSSFCell prtCompletedCell = row.createCell(2);
			prtCompletedCell.setCellValue(dvpr.ptgCompleted);
			prtCompletedCell.setCellStyle(cellStyle);

			XSSFCell riskAsseCell = row.createCell(3);
			riskAsseCell.setCellValue(dvpr.riskAssessment);
			riskAsseCell.setCellStyle(cellStyle);

			XSSFCell cmtCell = row.createCell(4);
			cmtCell.setCellValue(dvpr.comment);
			cmtCell.setCellStyle(cellStyle);

			XSSFCell moduleCell = row.createCell(5);
			moduleCell.setCellValue(dvpr.moduleGrp);
			moduleCell.setCellStyle(cellStyle);

			XSSFCell prjCell = row.createCell(6);
			prjCell.setCellValue(dvpr.project);
			prjCell.setCellStyle(cellStyle);

			XSSFCell esomIdCell = row.createCell(7);
			esomIdCell.setCellValue(dvpr.esomNumber);
			esomIdCell.setCellStyle(cellStyle);

			rownum++;
		}
		rownum = 1;
		for (SILInfo silInfo : this.lstSilInfo) {
			XSSFRow row = silSheet.createRow(rownum);

			XSSFCell silCell = row.createCell(0);
			silCell.setCellValue(silInfo.SILNumber);
			silCell.setCellStyle(cellStyle);

			XSSFCell silNameCell = row.createCell(1);
			silNameCell.setCellValue(silInfo.SILName);
			silNameCell.setCellStyle(cellStyle);

			XSSFCell severityCell = row.createCell(2);
			severityCell.setCellValue(silInfo.severity);
			severityCell.setCellStyle(cellStyle);

			XSSFCell statusCell = row.createCell(3);
			statusCell.setCellValue(silInfo.status);
			statusCell.setCellStyle(cellStyle);

			XSSFCell validationCell = row.createCell(4);
			validationCell.setCellValue(silInfo.validation);
			validationCell.setCellStyle(cellStyle);

			XSSFCell riskAsseCell = row.createCell(5);
			riskAsseCell.setCellValue(silInfo.riskAssessment);
			riskAsseCell.setCellStyle(cellStyle);

			XSSFCell moduleCell = row.createCell(6);
			moduleCell.setCellValue(silInfo.moduleGrp);
			moduleCell.setCellStyle(cellStyle);

			XSSFCell prjCell = row.createCell(7);
			prjCell.setCellValue(silInfo.project);
			prjCell.setCellStyle(cellStyle);

			XSSFCell esomIdCell = row.createCell(8);
			esomIdCell.setCellValue(silInfo.esomNumber);
			esomIdCell.setCellStyle(cellStyle);

			rownum++;
		}
	}
}
