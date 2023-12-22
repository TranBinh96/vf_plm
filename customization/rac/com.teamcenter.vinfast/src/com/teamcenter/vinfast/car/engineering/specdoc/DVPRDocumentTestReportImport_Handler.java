package com.teamcenter.vinfast.car.engineering.specdoc;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.model.DVPRDocumentTestReportModel;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class DVPRDocumentTestReportImport_Handler extends AbstractHandler {
	private TCSession session;
	private TCComponent selectedObject = null;
	private DVPRDocumentTestReportImport_Dialog dlg;

	private ProgressMonitorDialog progressMonitorDialog = null;
	private LinkedList<DVPRDocumentTestReportModel> testReportList;
	private String importMessage = "";

	public DVPRDocumentTestReportImport_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];

			if (selectedObject.getType().compareTo("VF3_AT_DVPR_docRevision") != 0) {
				MessageBox.post("Please select DVPR Document Revision.", "Access", MessageBox.WARNING);
				return null;
			}

			if (!TCExtension.checkPermissionAccess(selectedObject, "WRITE", session)) {
				MessageBox.post("Current logined user does not have access on the selected object.", "Warning", MessageBox.WARNING);
				return null;
			}

			dlg = new DVPRDocumentTestReportImport_Dialog(new Shell());
			dlg.create();

			dlg.btnOpenFile.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					FileDialog fileDialog = new FileDialog(new Shell(), SWT.OPEN);
					fileDialog.setText("Open");
					fileDialog.setFilterPath("C:/");
					String[] filterExt = { "*.xlsx", "*.xls" };
					fileDialog.setFilterExtensions(filterExt);
					readExcelFile(fileDialog.open());
				}
			});

			dlg.btnUpdate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					org.eclipse.swt.widgets.MessageBox messageBox = new org.eclipse.swt.widgets.MessageBox(dlg.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setMessage("Do you really want to IMPORT new item?");
					messageBox.setText("DVPR Document Test Report");
					int response = messageBox.open();
					if (response == SWT.YES) {
						importTestReport();
					}
				}
			});

			dlg.open();
		} catch (Exception e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	private void readExcelFile(String path) {
		testReportList = new LinkedList<>();
		dlg.txtFile.setText(path);
		if (!path.isEmpty()) {
			try {
				FileInputStream fis = new FileInputStream(path);
				@SuppressWarnings("resource")
				XSSFWorkbook wb = new XSSFWorkbook(fis);
				XSSFSheet sheet = wb.getSheetAt(0);
				int cellCount = 0;
				int rowCount = 0;

				for (Row row : sheet) {
					rowCount++;
					cellCount = 0;
					if (rowCount > 1) {
						DVPRDocumentTestReportModel testReportItem = new DVPRDocumentTestReportModel();
						for (Cell cell : row) {
							cellCount++;
							if (cellCount == 1) {
								testReportItem.setVfte(cell.getStringCellValue());
							} else if (cellCount == 2) {
								testReportItem.setTestName(cell.getStringCellValue());
							} else if (cellCount == 3) {
								Double doubleValue = null;
								try {
									doubleValue = cell.getNumericCellValue();
								} catch (Exception e) {
									e.printStackTrace();
								}
								if (doubleValue != null)
									testReportItem.setProgress(doubleValue);
								else
									testReportItem.setProgress(cell.getStringCellValue());
							} else if (cellCount == 4) {
								testReportItem.setTestResult(cell.getStringCellValue());
							} else {
								testReportItem.setComment(cell.getStringCellValue());
							}
						}
						testReportList.add(testReportItem);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		refreshReport();
	}

	private void refreshReport() {
		String[] headerList = new String[] { "VFTE", "Test Name", "Progress", "Test Result", "Comment" };
		boolean check = true;
		StringBuffer validationResultText = new StringBuffer();
		validationResultText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		validationResultText.append(StringExtension.htmlTableCss);
		validationResultText.append("<body style=\"margin: 0px;\">");
		validationResultText.append("<table>");
		LinkedHashMap<String, String> header = new LinkedHashMap<>();
		for (String headerName : headerList) {
			header.put(headerName, String.valueOf(100 / headerList.length));
		}

		validationResultText.append(StringExtension.genTableHeader(header));
		for (DVPRDocumentTestReportModel testReportItem : testReportList) {
			validationResultText.append("<tr>");
			validationResultText.append("<td>" + testReportItem.getVfte() + "</td>");
			validationResultText.append("<td>" + testReportItem.getTestName() + "</td>");
			if (testReportItem.isProgressValid()) {
				validationResultText.append("<td>" + testReportItem.getProgress() + "</td>");
			} else {
				check = false;
				validationResultText.append("<td>" + StringExtension.genBadgetFail("Available value: 0%/50%/90%/100%") + "</td>");
			}
			if (testReportItem.isTestResultValid()) {
				validationResultText.append("<td>" + testReportItem.getTestResult() + "</td>");
			} else {
				check = false;
				validationResultText.append("<td>" + StringExtension.genBadgetFail("Available value: Green/Yellow/Red") + "</td>");
			}
			validationResultText.append("<td>" + testReportItem.getComment() + "</td>");
			validationResultText.append("</tr>");
		}
		validationResultText.append("</table>");
		validationResultText.append("</body></html>");

		dlg.brwTable.setText(validationResultText.toString());

		if (check)
			dlg.setMessage("Validate successfully.", IMessageProvider.INFORMATION);
		else
			dlg.setMessage("Validate unsuccessfully.", IMessageProvider.WARNING);

		dlg.btnUpdate.setEnabled(check);
	}

	private void importTestReport() {

		try {
			importMessage = "";
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(dlg.getShell());

			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Import processing...", IProgressMonitor.UNKNOWN);
					try {
						int totalNumber = testReportList.size();
						int processedNumber = 0;
						TCComponent[] testReportRows = selectedObject.getRelatedComponents(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.RELATION_NAME);
						for (DVPRDocumentTestReportModel item : testReportList) {
							monitor.subTask("Test Report Processed: " + processedNumber + "/" + totalNumber);

							for (TCComponent testReportRow : testReportRows) {
								String vfte = testReportRow.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_VFTE_ITEM);
								String testName = testReportRow.getPropertyDisplayableValue(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_NAME);

								if (vfte.compareTo(item.getVfte()) == 0 && testName.compareTo(item.getTestName()) == 0) {
									testReportRow.setStringProperty(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_PROGRESS, item.getProgress());
									testReportRow.setStringProperty(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_TEST_RESULT, item.getTestResult());
									testReportRow.setStringProperty(SpecDocNameDefine.DVPR_TEST_REPORT_TABLE.PROPERTY_COMMENT, item.getComment());
									break;
								}
							}

							processedNumber++;
						}
					} catch (Exception e) {
						e.printStackTrace();
						importMessage = e.getMessage();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (importMessage.isEmpty()) {
			MessageBox.post("Import successful.", "Success", MessageBox.INFORMATION);
		} else {
			MessageBox.post("Import unsuccessful. Exception: " + importMessage, "Error", MessageBox.ERROR);
		}

		dlg.close();
	}
}