package com.teamcenter.vinfast.report.bomcomparision;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.dialog.SimpleFrame;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class SWBOM_Handler extends AbstractHandler {
	private TCComponentItemRevision selectObject = null;
	private static SimpleFrame frame = null;
	private static TCSession session = null;
	private DataManagementService dmService = null;

	LinkedHashMap<String, String> mapBOMProperties = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("Level", "bl_level_starting_0");
			put("Item Name", "bl_item_object_name");
			put("Item ID", "awb0BomLineItemId");
			put("Revision ID", "bl_rev_item_revision_id");
			put("ECU", "VF4_ecu");
			put("Part Type", "vf4_software_part_type");
			put("Region", "VF4_region");
			put("Variant Formula", "bl_formula");
			put("FuSA Level", "VF4_fusa_level");
			put("Model Year", "bl_rev_vl5_model_year");
			put("File Type", "");
			put("Lifecycle", "");
		}
	};

	public SWBOM_Handler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		dmService = DataManagementService.getService(session);
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] targetComponents = app.getTargetComponents();

		if (validObjectSelect(targetComponents)) {
			MessageBox.post("Please select one FRS Item.", "Error", MessageBox.ERROR);
			return null;
		}

		try {
			ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/KIT.png"));
			Icon ok_Icon = new ImageIcon(getClass().getResource("/icons/ok.png"));
			Icon cancel_Icon = new ImageIcon(getClass().getResource("/icons/cancel_16.png"));

			frame = new SimpleFrame();
			frame.setTitle("Export service chapter");
			frame.setIconImage(frame_Icon.getImage());
			frame.setMinimumSize(new Dimension(500, 200));
			frame.label1.setText("<html>The report is based on selected SBOM structure.<br/>This action will take few minutes.</html>");
			frame.label1.setHorizontalAlignment(SwingConstants.CENTER);
			frame.btnLeft.setIcon(ok_Icon);
			frame.btnLeft.setText("Continue");

			frame.btnRight.setIcon(cancel_Icon);
			frame.btnRight.setText("Cancel");

			frame.btnLeft.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Job job = new Job("Creating Report") {
						@Override
						protected IStatus run(IProgressMonitor arg0) {
							IStatus status = new IStatus() {
								@Override
								public boolean matches(int arg0) {
									return false;
								}

								@Override
								public boolean isOK() {
									return false;
								}

								@Override
								public boolean isMultiStatus() {
									return false;
								}

								@Override
								public int getSeverity() {
									return 0;
								}

								@Override
								public String getPlugin() {
									return null;
								}

								@Override
								public String getMessage() {
									return null;
								}

								@Override
								public Throwable getException() {
									return null;
								}

								@Override
								public int getCode() {
									return 0;
								}

								@Override
								public IStatus[] getChildren() {
									return null;
								}
							};

							frame.btnLeft.setEnabled(false);

							try {
								TCComponentRevisionRule revisionRule = TCExtension.getRevisionRule("Precise Only", session);
								TCComponentBOMLine topBOM = TCExtension.openBOMStructure(selectObject, revisionRule, session);
								if (topBOM == null) {
									frame.dispose();
									return status;
								}

								LinkedList<TCComponentBOMLine> allBOMLine = expandBOMLines(topBOM);
								excelPublishReport(allBOMLine);
							} catch (Exception e2) {
								e2.printStackTrace();
							}

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
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void excelPublishReport(LinkedList<TCComponentBOMLine> allBOMLine) {
		try {
			@SuppressWarnings("resource")
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet spreadsheet = wb.createSheet("SBOM");

			// Header
			CellStyle headerCellStyle = wb.createCellStyle();

			HSSFRow headerRow = spreadsheet.createRow(0);
			int cellCounter = 0;
			Set<String> bomProperties = new HashSet<String>();
			for (Map.Entry<String, String> entry : mapBOMProperties.entrySet()) {
				setCell(cellCounter, headerRow, entry.getKey(), headerCellStyle);
				bomProperties.add(entry.getValue());
				cellCounter++;
			}
			// Body
			CellStyle bodyCellStyle = wb.createCellStyle();

			int rowCounter = 1;
			dmService.getProperties(allBOMLine.toArray(new TCComponentBOMLine[0]), bomProperties.toArray(new String[0]));
			for (TCComponentBOMLine bomline : allBOMLine) {
				cellCounter = 0;
				HSSFRow bodyRow = spreadsheet.createRow(rowCounter);
				for (Map.Entry<String, String> entry : mapBOMProperties.entrySet()) {
					String dispItm = "";
					if (entry.getValue().isEmpty()) {

					} else {
						try {
							dispItm = bomline.getPropertyDisplayableValue(entry.getValue());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					setCell(cellCounter, bodyRow, dispItm, bodyCellStyle);
					cellCounter++;
				}
				rowCounter++;
			}

			String TEMP_DIR = System.getenv("tmp");
			String fileName = "FRS_COMPATIBILITY_" + selectObject.getPropertyDisplayableValue("item_id") + "_" + StringExtension.getTimeStamp() + ".xls";
			File file = new File(TEMP_DIR + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			MessageBox.post("Export report successfully.", "Information", MessageBox.INFORMATION);
			Desktop.getDesktop().open(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setCell(int index, HSSFRow headerRow, String displayValue, CellStyle headerCellStyle) {
		HSSFCell cell = headerRow.createCell(index);
		cell.setCellValue(displayValue);
		cell.setCellStyle(headerCellStyle);
	}

	private LinkedList<TCComponentBOMLine> expandBOMLines(TCComponentBOMLine rootLine) {
		LinkedList<TCComponentBOMLine> outBomLines = new LinkedList<TCComponentBOMLine>();
		try {
			outBomLines.add(rootLine);

			for (AIFComponentContext children : rootLine.getChildren()) {
				TCComponentBOMLine level1BomLine = (TCComponentBOMLine) children.getComponent();
				outBomLines.add(level1BomLine);
				for (AIFComponentContext children1 : level1BomLine.getChildren()) {
					TCComponentBOMLine level2BomLine = (TCComponentBOMLine) children1.getComponent();
					outBomLines.add(level2BomLine);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outBomLines;
	}

	private Boolean validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null || targetComponents.length != 1)
			return true;

		if (targetComponents[0] instanceof TCComponentItemRevision) {
			selectObject = (TCComponentItemRevision) targetComponents[0];
			if (selectObject.getType().compareToIgnoreCase("VF3_FRSRevision") == 0)
				return false;
		}

		return true;
	}
}
