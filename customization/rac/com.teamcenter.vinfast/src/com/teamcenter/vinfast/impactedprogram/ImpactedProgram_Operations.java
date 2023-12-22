package com.teamcenter.vinfast.impactedprogram;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.loose.core._2006_03.FileManagement.DatasetFileInfo;
import com.teamcenter.services.loose.core._2006_03.FileManagement.GetDatasetWriteTicketsInputData;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateDatasetsResponse;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.vinfast.impactedprogram.bean.ImpactedPartBean;
import com.teamcenter.vinfast.impactedprogram.bean.ImpactedProgramBean;
import com.vf4.services.rac.cm.ChangeManagementService;
import com.vf4.services.rac.cm._2020_12.ChangeManagement;
import com.vf4.services.rac.cm._2020_12.ChangeManagement.ImpactedProgramInput;
import com.vf4.services.rac.cm._2020_12.ChangeManagement.ImpactedProgramResponse;

public class ImpactedProgram_Operations {
	private TCSession tcSession = null;
	private DataManagementService dmService = null;
	private ArrayList<TCComponent> tcComponentList;
	private String revisionRule;
	private SXSSFWorkbook workbook = null;

	public ImpactedProgram_Operations(TCSession session, ArrayList<TCComponent> tcComponentArray, String revisionRule) {
		this.tcSession = session;
		this.tcComponentList = tcComponentArray;
		this.revisionRule = revisionRule;
		this.dmService = DataManagementService.getService(session);
	}

	public Map<TCComponent, TCComponent[]> getImpactedProgramList() {
		Map<TCComponent, TCComponent[]> impactedProgramMap;
		try {
			if (tcComponentList.size() > 0) {
				// call SOA Service
				String[] topNodeTypes = new String[1];
				TCComponent[] imParts = new TCComponent[tcComponentList.size()];
				topNodeTypes[0] = "VF3_vehicle";

				for (int ii = 0; ii < tcComponentList.size(); ii++) {
					TCComponent tcComponent = (TCComponent) tcComponentList.get(ii);

					if (tcComponent instanceof TCComponentBOMLine) {
						System.out.println("Component is a BOMLine...");
						TCComponentBOMLine tcBOMLine = (TCComponentBOMLine) tcComponent;
						tcComponent = (TCComponent) tcBOMLine.getItem();
					}

					// System.out.println("UID : " + tcComponent.getUid());
					ServiceData sd = dmService.loadObjects(new String[] { tcComponent.getUid() });
					TCComponent modelObj = (TCComponent) sd.getPlainObject(0);
					imParts[ii] = modelObj;
				}

				ImpactedProgramInput taskInput = new ImpactedProgramInput();
				taskInput.topNodeType = topNodeTypes;
				taskInput.impactedParts = imParts;
				taskInput.revisionRule = revisionRule;

				ChangeManagement cm = ChangeManagementService.getService(tcSession);
				ImpactedProgramResponse response = cm.getImpactedPrograms(taskInput);
				impactedProgramMap = response.outputs;

				return impactedProgramMap;
			} else {
				return null;
			}
		} catch (Exception ex) {
			return null;
		}
	}

	public ArrayList<ImpactedPartBean> getImpactedPartList(Map<TCComponent, TCComponent[]> impactedProgramMap) {
		ArrayList<ImpactedPartBean> _impactedPartList = new ArrayList<ImpactedPartBean>();

		try {
			for (Map.Entry<TCComponent, TCComponent[]> mapElement : impactedProgramMap.entrySet()) {
				TCComponent tcComponent = (TCComponent) mapElement.getKey();
				String _selectedPart = (String) (tcComponent.getProperty("item_id") + "-"
						+ tcComponent.getProperty("object_name"));
				TCComponent[] myList = (TCComponent[]) mapElement.getValue();
				// System.out.println("Item - " + _selectedPart + "\tNum of Impacted Programs -
				// " + myList.length);

				if (myList.length == 0) {
					ImpactedPartBean _impactedPartBean = new ImpactedPartBean();
					_impactedPartBean.setStrProgramList(Constants.getNoImpactedProgram());
					_impactedPartBean.setStrPartName(_selectedPart);
					_impactedPartList.add(_impactedPartBean);
				} else {
					ArrayList<TCComponent> _programList = new ArrayList<TCComponent>();
					StringBuilder sb = new StringBuilder();
					boolean bFlag = false;
					for (TCComponent impactedProgram : myList) {
						if (bFlag)
							sb.append(", ");
						_programList.add(impactedProgram);
						String programName = (String) (impactedProgram.getProperty("item_id") + "-"
								+ impactedProgram.getProperty("object_name"));
						sb.append(programName);
						bFlag = true;
					}

					ImpactedPartBean _impactedPartBean = new ImpactedPartBean();
					_impactedPartBean.setTcComponent(tcComponent);
					_impactedPartBean.setStrPartName(_selectedPart);
					_impactedPartBean.setStrProgramList(sb.toString());
					_impactedPartBean.setProgramList(_programList);
					_impactedPartList.add(_impactedPartBean);
				}
			}

			_impactedPartList.sort((o1, o2) -> o1.getStrPartName().compareTo(o2.getStrPartName()));

			return _impactedPartList;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public ArrayList<ImpactedProgramBean> getImpactedProgramList(Map<TCComponent, TCComponent[]> impactedProgramMap) {
		ArrayList<ImpactedProgramBean> _impactedProgramList = new ArrayList<ImpactedProgramBean>();

		try {
			for (Map.Entry<TCComponent, TCComponent[]> mapElement : impactedProgramMap.entrySet()) {
				TCComponent tcComponent = (TCComponent) mapElement.getKey();
				TCComponent[] myList = (TCComponent[]) mapElement.getValue();

				if (myList.length == 0) {
					boolean bFlag = false;
					for (ImpactedProgramBean impBean : _impactedProgramList) {
						if (impBean.getStrProgramName().equals(Constants.getNoImpactedProgram())) {
							bFlag = true;
							impBean.getImpactedPartList().add(tcComponent);
							break;
						}
					}
					if (!bFlag) {
						ArrayList<TCComponent> _programList = new ArrayList<TCComponent>();
						_programList.add(tcComponent);
						ImpactedProgramBean _impactedProgramBean = new ImpactedProgramBean();
						_impactedProgramBean.setStrProgramName(Constants.getNoImpactedProgram());
						_impactedProgramBean.setImpactedPartList(_programList);
						_impactedProgramList.add(_impactedProgramBean);
					}
				} else {
					for (TCComponent impactedProgram : myList) {
						boolean bFlag = false;
						String programName = (String) (impactedProgram.getProperty("item_id") + "-"
								+ impactedProgram.getProperty("object_name"));
						for (ImpactedProgramBean impBean : _impactedProgramList) {
							if (impBean.getStrProgramName().equals(programName)) {
								bFlag = true;
								impBean.getImpactedPartList().add(tcComponent);
								break;
							}
						}
						if (!bFlag) {
							ArrayList<TCComponent> _partList = new ArrayList<TCComponent>();
							_partList.add(tcComponent);
							ImpactedProgramBean _impactedProgramBean = new ImpactedProgramBean();
							_impactedProgramBean.setStrProgramName(programName);
							_impactedProgramBean.setImpactedPartList(_partList);
							_impactedProgramList.add(_impactedProgramBean);
						}
					}
				}
			}

			for (ImpactedProgramBean improgBean : _impactedProgramList) {
				StringBuilder sb = new StringBuilder();
				boolean bFlag = false;
				for (TCComponent tcComp : improgBean.getImpactedPartList()) {
					if (bFlag)
						sb.append(", ");
					String partName = (String) (tcComp.getProperty("item_id") + "-"
							+ tcComp.getProperty("object_name"));
					sb.append(partName);
					bFlag = true;
				}
				improgBean.setStrPartList(sb.toString());
			}

			_impactedProgramList.sort((o1, o2) -> o1.getStrProgramName().compareTo(o2.getStrProgramName()));

			return _impactedProgramList;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void createAndAttachReportDataset(TCComponentItemRevision tcECRRevision, File namedRefFile) {
		try {
			TCComponentDataset dataset = null;

			DataManagementService.DatasetProperties2[] dataProps = new DataManagementService.DatasetProperties2[] {
					new DataManagementService.DatasetProperties2() };

			dataProps[0].clientId = "MyID123456789";
			dataProps[0].name = namedRefFile.getName();
			dataProps[0].description = "Impacted Programs Report";
			dataProps[0].relationType = "CMReferences";
			dataProps[0].type = "MSExcelX";
			dataProps[0].container = tcECRRevision;

			CreateDatasetsResponse dataResp = dmService.createDatasets2(dataProps);
			if (dataResp.output.length > 0)
				dataset = dataResp.output[0].dataset;

			FileManagementUtility fMSFileManagement = new FileManagementUtility(tcSession.getSoaConnection());
			GetDatasetWriteTicketsInputData[] inputs = {
					getGetDatasetWriteTicketsInputData(dmService, namedRefFile, dataset, "excel") };
			fMSFileManagement.putFiles(inputs);
			fMSFileManagement.term();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private GetDatasetWriteTicketsInputData getGetDatasetWriteTicketsInputData(final DataManagementService dmService,
			File file, TCComponentDataset Dataset, String datasetRef) {
		try {
			File file1 = file; // Create a file to associate with
			DatasetFileInfo fileInfo = new DatasetFileInfo();

			if (datasetRef.equalsIgnoreCase("Text")) {
				fileInfo.isText = true;
			} else {
				fileInfo.isText = false;
			}

			fileInfo.clientId = "12345";
			fileInfo.fileName = file1.getAbsolutePath();
			fileInfo.namedReferencedName = datasetRef;
			// fileInfo.isText = true;
			fileInfo.allowReplace = false;
			DatasetFileInfo[] fileInfos = { fileInfo };
			GetDatasetWriteTicketsInputData inputData = new GetDatasetWriteTicketsInputData();
			// inputData.dataset = resp.output[0].dataset;

			inputData.dataset = Dataset;
			inputData.createNewVersion = true;
			inputData.datasetFileInfos = fileInfos;

			return inputData;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void writeToExcel(String filePath, String strECRName, String reportDate,
			ArrayList<ImpactedProgramBean> programList, ArrayList<ImpactedPartBean> partList) {
		try {
			File reportFile = new File(filePath);
			if (!reportFile.exists()) {
				workbook = new SXSSFWorkbook();
			}

			workbook.setCompressTempFiles(true);

			String sheetProgramView = Constants.getSheetProgramView();
			SXSSFSheet sheetProgram = workbook.createSheet(sheetProgramView);
			sheetProgram.setRandomAccessWindowSize(100);
			sheetProgram.setColumnWidth(0, 15000);
			sheetProgram.setColumnWidth(1, 15000);
			writeHeader(sheetProgram, strECRName, reportDate, Constants.getSheetProgramView());
			if (strECRName.equals(""))
				populateProgramSheetData(sheetProgram, programList, 3);
			else
				populateProgramSheetData(sheetProgram, programList, 4);

			String sheetPartView = Constants.getSheetPartView();
			SXSSFSheet sheetPart = workbook.createSheet(sheetPartView);
			sheetPart.setRandomAccessWindowSize(100);
			sheetPart.setColumnWidth(0, 15000);
			sheetPart.setColumnWidth(1, 15000);
			writeHeader(sheetPart, strECRName, reportDate, Constants.getSheetPartView());

			if (strECRName.equals(""))
				populatePartSheetData(sheetPart, partList, 3);
			else
				populatePartSheetData(sheetPart, partList, 4);

			FileOutputStream output = new FileOutputStream(reportFile);
			workbook.write(output);
			output.flush();
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void populateProgramSheetData(SXSSFSheet sheet, ArrayList<ImpactedProgramBean> programList, int num) {
		try {
			CellStyle defaultStyle = getDefaultStyle();
			int iRowCount = num;

			for (ImpactedProgramBean programBean : programList) {
				int startMerge = iRowCount;
				SXSSFRow row = sheet.createRow((short) iRowCount);
				iRowCount++;

				SXSSFCell cell0 = row.createCell(0);
				cell0.setCellValue(programBean.getStrProgramName());
				cell0.setCellStyle(defaultStyle);

				if (programBean.getImpactedPartList().size() == 1) {
					SXSSFCell cell1 = row.createCell(1);
					cell1.setCellValue(programBean.getStrPartList());
					cell1.setCellStyle(defaultStyle);
				} else if (programBean.getImpactedPartList().size() > 1) {
					boolean bFlag = false;
					for (TCComponent tcComponent : programBean.getImpactedPartList()) {
						if (bFlag) {
							row = sheet.createRow((short) iRowCount);
							SXSSFCell cellx = row.createCell(0);
							cellx.setCellValue(programBean.getStrProgramName());
							cellx.setCellStyle(defaultStyle);
							iRowCount++;
						}

						SXSSFCell cell1 = row.createCell(1);
						String _selectedPart = (String) (tcComponent.getProperty("item_id") + "-"
								+ tcComponent.getProperty("object_name"));
						cell1.setCellValue(_selectedPart);
						cell1.setCellStyle(defaultStyle);
						bFlag = true;
					}

					sheet.addMergedRegion(new CellRangeAddress(startMerge, iRowCount - 1, 0, 0));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void populatePartSheetData(SXSSFSheet sheet, ArrayList<ImpactedPartBean> partList, int num) {
		try {
			CellStyle defaultStyle = getDefaultStyle();
			int iRowCount = num;

			for (ImpactedPartBean partBean : partList) {
				int startMerge = iRowCount;
				SXSSFRow row = sheet.createRow((short) iRowCount);
				iRowCount++;

				SXSSFCell cell0 = row.createCell(0);
				cell0.setCellValue(partBean.getStrPartName());
				cell0.setCellStyle(defaultStyle);

				if (partBean.getProgramList() == null) {
					SXSSFCell cell1 = row.createCell(1);
					cell1.setCellValue(Constants.getNoImpactedProgram());
					cell1.setCellStyle(defaultStyle);
				} else {
					if (partBean.getProgramList().size() == 1) {
						SXSSFCell cell1 = row.createCell(1);
						cell1.setCellValue(partBean.getStrProgramList());
						cell1.setCellStyle(defaultStyle);
					} else if (partBean.getProgramList().size() > 1) {
						boolean bFlag = false;
						for (TCComponent tcComponent : partBean.getProgramList()) {
							if (bFlag) {
								row = sheet.createRow((short) iRowCount);
								SXSSFCell cellx = row.createCell(0);
								cellx.setCellValue(partBean.getStrPartName());
								cellx.setCellStyle(defaultStyle);
								iRowCount++;
							}

							SXSSFCell cell1 = row.createCell(1);
							String _selectedProgram = (String) (tcComponent.getProperty("item_id") + "-"
									+ tcComponent.getProperty("object_name"));
							cell1.setCellValue(_selectedProgram);
							cell1.setCellStyle(defaultStyle);
							bFlag = true;
						}

						sheet.addMergedRegion(new CellRangeAddress(startMerge, iRowCount - 1, 0, 0));
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeHeader(SXSSFSheet sheet, String strECRName, String reportDate, String sheetType) {
		try {
			String[] headers = new String[2];
			String[] revRule = new String[2];
			String[] repDate = new String[2];
			sheet.setDisplayZeros(true);

			revRule[0] = "Revision Rule";
			revRule[1] = revisionRule;

			repDate[0] = "Report Date";
			repDate[1] = reportDate;

			CellStyle defaultStyle = getDefaultStyle();
			CellStyle headerStyle = getStyleHeader();
			CellStyle defaultBoldStyle = getDefaultBoldStyle();

			if (sheetType.equalsIgnoreCase(Constants.getSheetProgramView())) {
				headerStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
				sheet.setTabColor(IndexedColors.LIGHT_ORANGE.getIndex());
				headers[0] = Constants.getTblHdrImpactedPrograms();
				headers[1] = Constants.getTblHdrSelectedParts();
			} else if (sheetType.equalsIgnoreCase(Constants.getSheetPartView())) {
				headerStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
				sheet.setTabColor(IndexedColors.SKY_BLUE.getIndex());
				headers = new String[] { Constants.getTblHdrSelectedParts(), Constants.getTblHdrImpactedPrograms() };
				headers[0] = Constants.getTblHdrSelectedParts();
				headers[1] = Constants.getTblHdrImpactedPrograms();
			}

			int size = headers.length;
			int iRowCount = 0;
			SXSSFRow row = null;

			if (strECRName.equals("")) {
				// write RevisionRule
				row = sheet.createRow((short) iRowCount);
				for (int ii = 0; ii < 2; ii++) {
					SXSSFCell cell = row.createCell(ii);
					cell.setCellValue(revRule[ii]);
					if (ii == 0)
						cell.setCellStyle(defaultBoldStyle);
					else
						cell.setCellStyle(defaultStyle);
				}
			} else {
				String[] ecr = new String[2];
				ecr[0] = "ECR Revision";
				ecr[1] = strECRName;

				row = sheet.createRow((short) iRowCount);
				for (int ii = 0; ii < 2; ii++) {
					SXSSFCell cell = row.createCell(ii);
					cell.setCellValue(ecr[ii]);
					if (ii == 0)
						cell.setCellStyle(defaultBoldStyle);
					else
						cell.setCellStyle(defaultStyle);
				}

				// write RevisionRule
				iRowCount++;
				row = sheet.createRow((short) iRowCount);
				for (int ii = 0; ii < 2; ii++) {
					SXSSFCell cell = row.createCell(ii);
					cell.setCellValue(revRule[ii]);
					if (ii == 0)
						cell.setCellStyle(defaultBoldStyle);
					else
						cell.setCellStyle(defaultStyle);
				}
			}

			// write date
			iRowCount++;
			row = sheet.createRow((short) iRowCount);
			for (int ii = 0; ii < 2; ii++) {
				SXSSFCell cell = row.createCell(ii);
				cell.setCellValue(repDate[ii]);
				if (ii == 0)
					cell.setCellStyle(defaultBoldStyle);
				else
					cell.setCellStyle(defaultStyle);
			}

			// write header
			iRowCount++;
			row = sheet.createRow((short) iRowCount);
			for (int ii = 0; ii < size; ii++) {
				SXSSFCell cell = row.createCell(ii);
				cell.setCellValue(headers[ii]);
				cell.setCellStyle(headerStyle);
			}

			if (strECRName.equals("")) {
				sheet.setAutoFilter(new CellRangeAddress(2, 2, 0, 1));
				sheet.createFreezePane(0, 3, 0, 3);
			} else {
				sheet.setAutoFilter(new CellRangeAddress(3, 3, 0, 1));
				sheet.createFreezePane(0, 4, 0, 4);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public CellStyle getDefaultStyle() {
		try {
			CellStyle style = workbook.createCellStyle();

			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.TOP);

			style.setBorderBottom(BorderStyle.THIN);
			style.setBorderTop(BorderStyle.THIN);
			style.setBorderRight(BorderStyle.THIN);
			style.setBorderLeft(BorderStyle.THIN);

			return style;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public CellStyle getDefaultBoldStyle() {
		try {
			CellStyle style = workbook.createCellStyle();

			Font xssffont = workbook.createFont();
			xssffont.setBold(true);
			style.setFont(xssffont);

			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.TOP);

			style.setBorderBottom(BorderStyle.THIN);
			style.setBorderTop(BorderStyle.THIN);
			style.setBorderRight(BorderStyle.THIN);
			style.setBorderLeft(BorderStyle.THIN);

			return style;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public CellStyle getStyleHeader() {
		try {
			CellStyle style = workbook.createCellStyle();

			Font xssffont = workbook.createFont();
			xssffont.setBold(true);
			style.setFont(xssffont);

			xssffont.setFontHeightInPoints((short) 12);

			style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			style.setAlignment(HorizontalAlignment.CENTER);
			style.setVerticalAlignment(VerticalAlignment.CENTER);

			style.setBorderBottom(BorderStyle.THIN);
			style.setBorderTop(BorderStyle.THIN);
			style.setBorderRight(BorderStyle.THIN);
			style.setBorderLeft(BorderStyle.THIN);

			return style;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
