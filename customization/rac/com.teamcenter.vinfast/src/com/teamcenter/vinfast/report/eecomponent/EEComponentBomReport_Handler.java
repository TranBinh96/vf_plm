package com.teamcenter.vinfast.report.eecomponent;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vf.utils.Query;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class EEComponentBomReport_Handler extends AbstractHandler {
	private int[] colorArrayR = { 189, 252, 255, 180, 198 };
	private int[] colorArrayG = { 215, 228, 230, 198, 224 };
	private int[] colorArrayB = { 238, 214, 153, 231, 180 };

	private EEComponentBomReport_Dialog frame = null;
	private TCSession session = null;
	private DataManagementService dmService = null;
	private static long startTime = 0;

	private final String REPORT_PREFIX = "EEBOM_";

	private String TopBom = "";
	private int lastKey = 0;

	private static String TEMP_DIR;

	private List<String> selectedProps = new ArrayList<String>();
	private LinkedHashMap<String, LinkedHashMap<String, String>> mapMasterBOMLineInfo;
	private LinkedHashMap<String, String> mapAvaPropPreference = new LinkedHashMap<String, String>();
	private Set<String> propertyList = new HashSet<String>();

	private int sourcingNumberCol = 0;
	private XSSFWorkbook wb;

	private boolean isExpand = true;

	public EEComponentBomReport_Handler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] targetComponents = app.getTargetComponents();
		if (targetComponents != null && targetComponents.length == 0) {
			MessageBox.post("Please Select One Bom Line.", "Error", MessageBox.ERROR);
			return null;
		}

		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		dmService = DataManagementService.getService(session);
		TEMP_DIR = System.getenv("tmp");

		List<String> listValueBOMExportPropPre = Arrays.asList(TCExtension.GetPreferenceValues("VINFAST_EEBOM_VIEW_CONFIG", session));
		for (String singleValue : listValueBOMExportPropPre) {
			String[] strSplit = singleValue.split(",");
			if (strSplit.length != 3) {
				mapAvaPropPreference.put(strSplit[1], "");
				selectedProps.add(strSplit[1]);
			} else {
				mapAvaPropPreference.put(strSplit[1], strSplit[2]);
				selectedProps.add(strSplit[1]);
				propertyList.add(strSplit[2]);
			}
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mapMasterBOMLineInfo = new LinkedHashMap<String, LinkedHashMap<String, String>>();

				createDialog(targetComponents);
			}
		});

		return null;
	}

	public void createDialog(final InterfaceAIFComponent[] targetComponents) {
		frame = new EEComponentBomReport_Dialog();
		frame.setTitle("EEComponent BOM Report");
		frame.setMinimumSize(new Dimension(500, 200));
		frame.title.setText("<html>The report is based on selected EEBOM structure. This action will take few minutes.</html>");

		frame.btnCreate.addActionListener(new ActionListener() {
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

						frame.progressBar.setIndeterminate(true);
						frame.btnCreate.setEnabled(false);
						startTime = System.currentTimeMillis();
						TEMP_DIR = System.getenv("tmp") + "/" + startTime;
						File theDir = new File(TEMP_DIR);
						theDir.mkdirs();

						int i = 0;
						if (targetComponents.length > 1) {
							for (InterfaceAIFComponent oneLine : targetComponents) {
								collectData(i, (TCComponentBOMLine) oneLine);
								i++;
							}
						} else {
							LinkedList<TCComponentBOMLine> allBOMLineInWindow = TCExtension.expandAllBOMLines((TCComponentBOMLine) targetComponents[0], session);
							try {
								LinkedList<TCComponentBOMLine> arrangementBomLine = TCExtension.arrangementBomLine(allBOMLineInWindow);
								Set<String> propertyAdding = new HashSet<>(Arrays.asList(new String[] { "bl_parent", "bl_item_item_id", "bl_level_starting_0", "bl_sequence_no", "bl_quantity", "VF4_ECPartDesignator" }));
								propertyList.addAll(propertyAdding);
								dmService.getProperties(arrangementBomLine.toArray(new TCComponentBOMLine[0]), propertyList.toArray(new String[0]));
								for (TCComponentBOMLine bomLine : arrangementBomLine) {
									collectData(i, bomLine);
									i++;
								}
							} catch (Exception e2) {
								e2.printStackTrace();
							}
						}

						excelPublishReport();

						frame.dispose();
						return status;
					}
				};
				session.queueOperation(job);
			}
		});

		frame.rbtNoExpand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isExpand = false;
				updateUI();
			}
		});

		frame.rbtExpand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isExpand = true;
				updateUI();
			}
		});

		frame.btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		updateUI();
	}

	private void updateUI() {
		frame.rbtExpand.setSelected(isExpand);
		frame.rbtNoExpand.setSelected(!isExpand);
	}

	private void collectData(int key, TCComponentBOMLine bomline) {
		try {
			TCComponent parent = bomline.getReferenceProperty("bl_parent");
			TCComponentBOMLine bom_line_parent = (TCComponentBOMLine) parent;

			String masterID = bomline.getProperty("bl_item_item_id") + "-" + bomline.getPropertyDisplayableValue("bl_level_starting_0") + "-" + bomline.getPropertyDisplayableValue("bl_sequence_no") + "-" + (bom_line_parent == null ? "" : bom_line_parent.getProperty("bl_item_item_id"));

			LinkedHashMap<String, String> lineItem = mapMasterBOMLineInfo.get(masterID + "-" + String.valueOf(lastKey));

			if (lineItem != null) {
				String quantity = lineItem.get("Quantity");
				if (quantity.isEmpty()) {
					lineItem.put("Quantity", bomline.getPropertyDisplayableValue("bl_quantity"));
				} else {
					try {
						lineItem.put("Quantity", String.valueOf(Double.parseDouble(quantity) + Double.parseDouble(bomline.getPropertyDisplayableValue("bl_quantity"))));
					} catch (Exception e) {
					}
				}
				//
				String refMPN = lineItem.get("Reference");
				if (refMPN.isEmpty()) {
					lineItem.put("Reference", bomline.getPropertyDisplayableValue("VF4_ECPartDesignator"));
				} else {
					try {
						lineItem.put("Reference", refMPN + ";" + bomline.getPropertyDisplayableValue("VF4_ECPartDesignator"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				lastKey = key;
				String check = "";
				String objectType = bomline.getItemRevision().getType();
//				if (objectType.compareToIgnoreCase("VF4_Compo_DesignRevision") != 0) {
//					return;
//				}

				/* default add Item ID to report */
				if (!selectedProps.contains("Item ID")) {
					selectedProps.add("Item ID");
				}

				LinkedHashMap<String, String> BOMLineInfo = new LinkedHashMap<String, String>();
				for (Map.Entry<String, String> entry : mapAvaPropPreference.entrySet()) {
					String value = "";
					if (!entry.getValue().isEmpty()) {
						value = bomline.getProperty(entry.getValue());
					}
					BOMLineInfo.put(entry.getKey(), value);
				}

				//
				TCComponentBOMLine[] substituteList = bomline.listSubstitutes();
				if (substituteList.length > 0) {
					Set<String> subList = new HashSet<String>();
					Set<String> subMPNList = new HashSet<String>();
					for (TCComponentBOMLine substitute : substituteList) {
						String subID = substitute.getPropertyDisplayableValue("bl_item_item_id");
						if (subID.contains("HOU") || subID.contains("EEP")) {
							String subName = substitute.getPropertyDisplayableValue("bl_item_object_name");
							subList.add(subID + "-" + subName);
						} else {
							subList.add(subID);
						}

						subMPNList.add(getSubMPNs(substitute.getItemRevision()));
					}

					BOMLineInfo.put("Substitute", String.join(",", subList));
					BOMLineInfo.put("Substitute MPNs", String.join(",", subMPNList));
				}
				//
				TCComponent[] objectChildComponents = bomline.getItemRevision().getRelatedComponents("VF4_EC_Supplier_Relation");

				String sourcingInfo = "";
				int _sourcingNumber = 0;

				for (TCComponent tcComponent : objectChildComponents) {
					if (tcComponent.getType().compareToIgnoreCase("VF4_EC_Supp_PartRevision") == 0) {
						_sourcingNumber++;
						if (!sourcingInfo.isEmpty()) {
							sourcingInfo += ";";
						}
						sourcingInfo += tcComponent.getPropertyDisplayableValue("object_name") + ";" + tcComponent.getPropertyDisplayableValue("vf4_manufacturer") + ";" + tcComponent.getPropertyDisplayableValue("vf4_supplier_name");
					}
				}

				if (_sourcingNumber > sourcingNumberCol)
					sourcingNumberCol = _sourcingNumber;

				BOMLineInfo.put("Sourcing List", sourcingInfo);
				BOMLineInfo.put("Check", check);

				mapMasterBOMLineInfo.put(masterID + "-" + String.valueOf(key), BOMLineInfo);
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void excelPublishReport() {
		try {
			wb = new XSSFWorkbook();
			XSSFSheet spreadsheet = wb.createSheet("BOM");

			// Top Header
			XSSFCellStyle topHeaderCellStyle = wb.createCellStyle();
			topHeaderCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			topHeaderCellStyle.setBorderTop(BorderStyle.THIN);
			topHeaderCellStyle.setBorderBottom(BorderStyle.THIN);
			topHeaderCellStyle.setBorderLeft(BorderStyle.THIN);
			topHeaderCellStyle.setBorderRight(BorderStyle.THIN);
			topHeaderCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(146, 208, 80), new DefaultIndexedColorMap()));

			XSSFRow topHeaderRow = spreadsheet.createRow(0);
			int cellCounter = 0;
			setCell(1, topHeaderRow, "BOM", topHeaderCellStyle);
			spreadsheet.addMergedRegion(new CellRangeAddress(0, 0, 1, selectedProps.size() - 2));
			for (int i = 0; i < sourcingNumberCol; i++) {
				XSSFCellStyle secondHeaderCellStyle = wb.createCellStyle();
				secondHeaderCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				secondHeaderCellStyle.setBorderTop(BorderStyle.THIN);
				secondHeaderCellStyle.setBorderBottom(BorderStyle.THIN);
				secondHeaderCellStyle.setBorderLeft(BorderStyle.THIN);
				secondHeaderCellStyle.setBorderRight(BorderStyle.THIN);
				secondHeaderCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(colorArrayR[i % 5], colorArrayG[i % 5], colorArrayB[i % 5]), new DefaultIndexedColorMap()));

				setCell(selectedProps.size() - 1 + i * 3, topHeaderRow, "SOURCING " + (i + 1), secondHeaderCellStyle);
				spreadsheet.addMergedRegion(new CellRangeAddress(0, 0, selectedProps.size() - 1 + i * 3, selectedProps.size() - 1 + i * 3 + 2));
			}

			// Header
			CellStyle headerCellStyle = wb.createCellStyle();
			headerCellStyle.setBorderTop(BorderStyle.THIN);
			headerCellStyle.setBorderBottom(BorderStyle.THIN);
			headerCellStyle.setBorderLeft(BorderStyle.THIN);
			headerCellStyle.setBorderRight(BorderStyle.THIN);

			XSSFRow headerRow = spreadsheet.createRow(1);
			cellCounter = 0;
			for (String value : selectedProps) {
				if (value.compareToIgnoreCase("Sourcing List") == 0) {
					for (int i = 0; i < sourcingNumberCol; i++) {
						setCell(cellCounter, headerRow, "MPN", headerCellStyle);
						cellCounter++;
						setCell(cellCounter, headerRow, "Manufacturer", headerCellStyle);
						cellCounter++;
						setCell(cellCounter, headerRow, "Supplier Name", headerCellStyle);
						cellCounter++;
					}
				} else {
					setCell(cellCounter, headerRow, value, headerCellStyle);
					cellCounter++;
				}
			}

			// Body
			XSSFCellStyle cellStyleOrange = wb.createCellStyle();
			cellStyleOrange.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			cellStyleOrange.setBorderTop(BorderStyle.THIN);
			cellStyleOrange.setBorderBottom(BorderStyle.THIN);
			cellStyleOrange.setBorderLeft(BorderStyle.THIN);
			cellStyleOrange.setBorderRight(BorderStyle.THIN);
			cellStyleOrange.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 192, 0), new DefaultIndexedColorMap()));

			CellStyle bodyCellStyle = wb.createCellStyle();
			bodyCellStyle.setBorderTop(BorderStyle.THIN);
			bodyCellStyle.setBorderBottom(BorderStyle.THIN);
			bodyCellStyle.setBorderLeft(BorderStyle.THIN);
			bodyCellStyle.setBorderRight(BorderStyle.THIN);

			int rowCounter = 2;
			for (Entry<String, LinkedHashMap<String, String>> entry : mapMasterBOMLineInfo.entrySet()) {
				LinkedHashMap<String, String> BOMLineInfo = entry.getValue();

				String preference = BOMLineInfo.get("Reference");
				String quantity = BOMLineInfo.get("Quantity");
				LinkedHashMap<String, String> preExpand = new LinkedHashMap<String, String>();
				if (isExpand) {
					preExpand = expandRow(preference, quantity);
				} else {
					preExpand.put(preference, quantity);
				}
//				LinkedHashMap<String, String> preExpand = expandRow(preference, quantity);

				for (Map.Entry<String, String> row : preExpand.entrySet()) {
					cellCounter = 0;
					XSSFRow bodyRow = spreadsheet.createRow(rowCounter);
					for (String col : selectedProps) {
						String dispItm = BOMLineInfo.get(col);
						if (col.compareToIgnoreCase("Sourcing List") == 0) {
							if (dispItm == null) {
								for (int i = 0; i < sourcingNumberCol; i++) {
									setCell(cellCounter, bodyRow, "", bodyCellStyle);
									cellCounter++;
									setCell(cellCounter, bodyRow, "", bodyCellStyle);
									cellCounter++;
									setCell(cellCounter, bodyRow, "", bodyCellStyle);
									cellCounter++;
								}
							} else {
								String[] str = dispItm.split(";");
								String value = "";
								for (int i = 0; i < sourcingNumberCol; i++) {
									value = "";
									try {
										value = str[0 + i * 3];
									} catch (Exception e) {
									}
									setCell(cellCounter, bodyRow, value, bodyCellStyle);
									cellCounter++;
									value = "";
									try {
										value = str[1 + i * 3];
									} catch (Exception e) {
									}
									setCell(cellCounter, bodyRow, value, bodyCellStyle);
									cellCounter++;
									value = "";
									try {
										value = str[2 + i * 3];
									} catch (Exception e) {
									}
									setCell(cellCounter, bodyRow, value, bodyCellStyle);
									cellCounter++;
								}
							}
						} else if (col.compareToIgnoreCase("Reference") == 0) {
							setCell(cellCounter, bodyRow, row.getKey(), bodyCellStyle);
							cellCounter++;
						} else if (col.compareToIgnoreCase("Quantity") == 0) {
							setCell(cellCounter, bodyRow, row.getValue(), bodyCellStyle);
							cellCounter++;
						} else {
							if (dispItm == null) {
								dispItm = "";
							}
							boolean errorCell = false;
							if (col.compareToIgnoreCase("Ref MPN") == 0) {
								if (BOMLineInfo.get("Check").compareToIgnoreCase("0") == 0) {
									errorCell = true;
								}
							}
							if (errorCell)
								setCell(cellCounter, bodyRow, dispItm, cellStyleOrange);
							else
								setCell(cellCounter, bodyRow, dispItm, bodyCellStyle);
							cellCounter++;
						}
					}
					rowCounter++;
				}
			}

			for (int kz = 0; kz < selectedProps.size() + sourcingNumberCol * 3; kz++) {
				spreadsheet.autoSizeColumn(kz);
			}

			String fileName = REPORT_PREFIX + TopBom + "_" + StringExtension.getTimeStamp() + ".xlsx";
			File file = new File(TEMP_DIR + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			fos.close();
			frame.progressBar.setIndeterminate(false);
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private LinkedHashMap<String, String> expandRow(String preference, String quantity) {
		LinkedHashMap<String, String> preExpand = new LinkedHashMap<String, String>();
		if (preference == null || preference.isEmpty()) {
			preExpand.put("", quantity);
		} else {
			if (preference.contains(";")) {
				String[] groupECUs = preference.split(";");
				for (String groupECU : groupECUs) {
					String[] ecus = groupECU.split("-");
					if (ecus != null && ecus.length == 2) {
						String fromChar = getCharFromString(ecus[0]);
						int fromNum = getIntegerFromString(ecus[0]);
						String toChar = getCharFromString(ecus[1]);
						int toNum = getIntegerFromString(ecus[1]);

						if (fromChar.compareToIgnoreCase(toChar) == 0) {
							if (toNum > fromNum) {
								for (int i = fromNum; i <= toNum; i++) {
									preExpand.put(fromChar + String.valueOf(i), "1.000");
								}
							} else {
								// error
								preExpand.put(groupECU, "1.000");
							}
						} else {
							// error
							preExpand.put(groupECU, "1.000");
						}
					} else {
						preExpand.put(groupECU, "1.000");
					}
				}
			} else {
				preExpand.put(preference, quantity);
			}
		}

		return preExpand;
	}

	private void setCell(int index, XSSFRow headerRow, String displayValue, CellStyle headerCellStyle) {
		XSSFCell cell = headerRow.createCell(index);
		cell.setCellValue(displayValue);
		cell.setCellStyle(headerCellStyle);
	}

	private String getCharFromString(String input) {
		String[] part = input.split("(?<=\\D)(?=\\d)");
		if (part != null && part.length > 0)
			return part[0];
		return "";
	}

	private int getIntegerFromString(String input) {
		int output = -1;
		String[] part = input.split("(?<=\\D)(?=\\d)");
		if (part != null && part.length > 1)
			try {
				output = Integer.parseInt(part[1]);
			} catch (Exception e) {
				output = -1;
			}
		return output;
	}

	private String currentTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
		Date date = new Date(System.currentTimeMillis());
		return formatter.format(date);
	}

	private String getSubMPNs(Set<String> subList) {
		LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
		inputQuery.put("Item ID", String.join(";", subList));

		TCComponent[] item_search = Query.queryItem(session, inputQuery, "__Latest Item Revision");
		if (item_search.length > 0) {
			Set<String> output = new HashSet<String>();
			for (TCComponent tcComponent : item_search) {
				if (tcComponent instanceof TCComponentItemRevision) {
					try {
						TCComponent[] objectChildComponents = tcComponent.getRelatedComponents("VF4_EC_Supplier_Relation");
						String subItem = tcComponent.getPropertyDisplayableValue("item_id") + " (";
						for (TCComponent tcComp : objectChildComponents) {
							if (tcComp.getType().compareToIgnoreCase("VF4_EC_Supp_PartRevision") == 0) {
								subItem += tcComp.getPropertyDisplayableValue("object_name") + " / " + tcComp.getPropertyDisplayableValue("vf4_manufacturer");
							}
						}
						subItem += ")";
						output.add(subItem);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return String.join(",", output);
		}
		return "";
	}

	private String getSubMPNs(TCComponentItemRevision itemRev) {
		try {
			TCComponent[] objectChildComponents = itemRev.getRelatedComponents("VF4_EC_Supplier_Relation");
			String subItem = itemRev.getPropertyDisplayableValue("item_id") + " (";
			for (TCComponent tcComp : objectChildComponents) {
				if (tcComp.getType().compareToIgnoreCase("VF4_EC_Supp_PartRevision") == 0) {
					subItem += tcComp.getPropertyDisplayableValue("object_name") + " / " + tcComp.getPropertyDisplayableValue("vf4_manufacturer");
				}
			}
			subItem += ")";
			return subItem;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
