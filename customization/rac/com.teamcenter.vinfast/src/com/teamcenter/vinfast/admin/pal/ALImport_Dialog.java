package com.teamcenter.vinfast.admin.pal;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;

import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentAssignmentListType;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ALImport_Dialog extends TitleAreaDialog {
	private Composite area;
	private Composite container;

	private TCSession session;
	private LinkedHashMap<String, TCComponentTaskTemplate> subTaskList = null;
	private TCComponentTaskTemplate workflowTemplate;
	private ProgressMonitorDialog progressMonitorDialog = null;
	private Button btnUpdate;
	private Browser brwTable;
	private Label lblNewLabel;
	private Text txtFile;
	private Button btnOpenFile;

	private LinkedList<ALAssistant_Model> alMapping;
	private LinkedList<String> headerList;
	private String importMessage = "";

	public ALImport_Dialog(Shell parentShell, TCSession session, LinkedHashMap<String, TCComponentTaskTemplate> subTaskList, TCComponentTaskTemplate workflowSelected) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setBlockOnOpen(false);
		this.session = session;
		this.subTaskList = subTaskList;
		this.workflowTemplate = workflowSelected;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		parent.setFocus();

		try {
			container = new Composite(area, SWT.NONE);
			container.setLayout(new GridLayout(3, false));
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			setTitle("Import AL");
			setMessage("Select file to import.", IMessageProvider.INFORMATION);

			lblNewLabel = new Label(container, SWT.NONE);
			lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblNewLabel.setText("Excel file:");

			txtFile = new Text(container, SWT.BORDER);
			txtFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			btnOpenFile = new Button(container, SWT.NONE);
			btnOpenFile.setImage(ResourceManager.getPluginImage("com.teamcenter.vinfast", "icons/importobjects_16.png"));
			btnOpenFile.addListener(SWT.Selection, new Listener() {
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

			brwTable = new Browser(container, SWT.NONE);
			brwTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", MessageBox.ERROR);
		}

		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Assignment List");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 800);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnUpdate = createButton(parent, IDialogConstants.CLOSE_ID, "Import", false);
		btnUpdate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				org.eclipse.swt.widgets.MessageBox messageBox = new org.eclipse.swt.widgets.MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage("Do you really want to IMPORT new item?");
				messageBox.setText("Assignment List");
				int response = messageBox.open();
				if (response == SWT.YES) {
					importALs();
				}
			}
		});
	}

	private void readExcelFile(String path) {
		LinkedList<String> alNameNotCorrect = new LinkedList<>();
		alMapping = new LinkedList<>();
		headerList = new LinkedList<>();
		txtFile.setText(path);
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
					if (rowCount == 1) {
						for (Cell cell : row) {
							cellCount++;
							String alName = cell.getStringCellValue();
							headerList.add(alName);
//							if (!subTaskList.keySet().contains(alName) && cellCount > 1)
//								alNameNotCorrect.add(alName);
						}
					} else {
						ALAssistant_Model newAl = new ALAssistant_Model(session, subTaskList);
						for (Cell cell : row) {
							cellCount++;
							if (cellCount == 1) {
								newAl.setALName(cell.getStringCellValue());
							} else if (cellCount == 2) {
								newAl.setALDesc(cell.getStringCellValue());
							} else {
								String resource = cell.getStringCellValue();
								newAl.setTaskObject(headerList.get(cellCount - 1), resource);
							}
						}
						for (int i = cellCount; i <= headerList.size(); i++) {
							newAl.setTaskObject(headerList.get(i - 1), "");
						}
						alMapping.add(newAl);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (alNameNotCorrect.size() > 0) {
			setMessage("Task Name not correct: " + String.join(", ", alNameNotCorrect), IMessageProvider.WARNING);
			return;
		}
		refreshReport();
	}

	private void refreshReport() {
		boolean check = true;
		StringBuffer validationResultText = new StringBuffer();
		validationResultText.append("<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
		validationResultText.append(StringExtension.htmlTableCss);
		validationResultText.append("<body style=\"margin: 0px;\">");
		validationResultText.append("<table>");
		LinkedHashMap<String, String> header = new LinkedHashMap<>();
		for (String headerName : headerList) {
			header.put(headerName, String.valueOf(100 / headerList.size()));
		}

		validationResultText.append(StringExtension.genTableHeader(header));
		for (ALAssistant_Model alMap : alMapping) {
			if (!alMap.getUpdateValid())
				check = false;

			validationResultText.append("<tr>");

			validationResultText.append("<td>");
			validationResultText.append(alMap.getALNameHtml());
			validationResultText.append("</td>");

			validationResultText.append("<td>");
			validationResultText.append(alMap.getALDesc());
			validationResultText.append("</td>");

			for (String taskMap : alMap.getTaskDetail()) {
				validationResultText.append("<td>");
				validationResultText.append(taskMap);
				validationResultText.append("</td>");
			}
			validationResultText.append("</tr>");
		}
		validationResultText.append("</table>");
		validationResultText.append("</body></html>");

		brwTable.setText(validationResultText.toString());

		if (check)
			setMessage("File OK.", IMessageProvider.INFORMATION);
		else
			setMessage("User not correct.", IMessageProvider.WARNING);

		btnUpdate.setEnabled(check);
	}

	private void importALs() {
		try {
			importMessage = "";
			if (progressMonitorDialog == null)
				progressMonitorDialog = new ProgressMonitorDialog(getShell());

			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Import processing...", IProgressMonitor.UNKNOWN);
					try {
						int totalNumber = alMapping.size();
						int processedNumber = 0;
						for (ALAssistant_Model alModel : alMapping) {
							monitor.subTask("AL Processed: " + processedNumber + "/" + totalNumber);
							String alName = alModel.getALName();
							String[] alDesc = new String[] { alModel.getALDesc() };
							try {
								TCComponentAssignmentList alItem = alModel.getALObject();
								if (alItem != null)
									alModel.getALObject().modify(alName, alDesc, workflowTemplate, alModel.getResourceList().toArray(new ResourceMember[0]), true);
								else {
									TCComponentAssignmentListType alType = (TCComponentAssignmentListType) session.getTypeComponent("EPMAssignmentList");
									alType.create(alName, alDesc, workflowTemplate, true, alModel.getResourceList().toArray(new ResourceMember[0]));
								}
							} catch (Exception e) {
								importMessage += String.valueOf(processedNumber + 1) + ". " + e.getMessage() + "/n";
								e.printStackTrace();
							}
							processedNumber++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					monitor.done();
					if (monitor.isCanceled())
						throw new InterruptedException("The long running operation was cancelled");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		MessageBox.post("Import success.", "Success", MessageBox.INFORMATION);
		this.close();
	}
}