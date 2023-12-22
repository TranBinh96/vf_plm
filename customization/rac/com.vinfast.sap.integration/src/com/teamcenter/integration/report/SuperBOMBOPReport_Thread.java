package com.teamcenter.integration.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.teamcenter.integration.ulti.StringExtension;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SuperBOMBOPReport_Thread {
	private String folderPath = "C:\\temp\\";
	private TCSession session = null;
	private TCComponentBOMLine topBOPLine = null;
	private TCComponentBOMLine topBOMLine = null;
	private String variantRule = "";
	private String platformCode = "";
	private String modelYear = "";
	private ShopModel shop = null;

	public SuperBOMBOPReport_Thread(ShopModel shop, TCComponentBOMLine topBOPLine, TCComponentBOMLine topBOMLine, String variantRule, String platformCode, String modelYear, TCSession session) {
		this.shop = shop;
		this.topBOMLine = topBOMLine;
		this.topBOPLine = topBOPLine;
		this.variantRule = variantRule;
		this.platformCode = platformCode;
		this.modelYear = modelYear;
		this.session = session;
	}

	public void run() {
		try {
			LinkedList<TCComponentBOMLine> bomLines = expandBom(topBOMLine);
			if (bomLines != null) {
				String beforeAction = TCExtension.getCurrentTime();
				NodeInfo[] nodes = TCExtension.findBOMLineInBOP(topBOPLine, bomLines.toArray(new TCComponentBOMLine[0]), session);
				String afterAction = TCExtension.getCurrentTime();
				System.out.print("Find Node " + beforeAction + " => " + afterAction);
				if (nodes != null) {
					beforeAction = TCExtension.getCurrentTime();
					ArrayList<HashMap<String, String>> valuesMap = new ArrayList<HashMap<String, String>>();
					for (NodeInfo node : nodes) {
						HashMap<String, String> map = new HashMap<String, String>();
						TCComponent BOM = node.originalNode;
						TCComponentBOMLine orginalLine = (TCComponentBOMLine) BOM;
//						TCComponentBOMLine parentLine = orginalLine.parent();
//						map.put("Sub Group", parentLine.getPropertyDisplayableValue("bl_item_item_id"));
						map.put("Part No", orginalLine.getPropertyDisplayableValue("bl_item_item_id"));
						map.put("Part Name", orginalLine.getItemRevision().getPropertyDisplayableValue("object_name"));
						String posID = orginalLine.getPropertyDisplayableValue("VF3_pos_ID");
						if (posID.isEmpty())
							posID = orginalLine.getPropertyDisplayableValue("VL5_pos_id");
						map.put("POS ID", posID.replace("\n", ""));

						String bomlineID = orginalLine.getPropertyDisplayableValue("VF4_bomline_id");
						if (StringExtension.isInteger(bomlineID, 10)) {
							bomlineID = StringExtension.ConvertNumberToString(Integer.parseInt(bomlineID), 4);
						}
						map.put("Bomline", bomlineID.replace("\n", ""));

						String qty = orginalLine.getPropertyDisplayableValue("bl_quantity");
						if (qty.isEmpty())
							qty = "1.000";
						map.put("Quantity", qty);

						String variant = orginalLine.getPropertyDisplayableValue("bl_formula");
						if (!variant.isEmpty()) {
							variant = variant.replaceAll("\\&", "AND");
							variant = variant.replaceAll("\\|", "OR");
							//variant = variant.replaceAll("!=", "<>");
							Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(variant);
							while (m.find()) {
								variant = variant.replace(m.group(1), platformCode + "_" + modelYear);
							}
						}
						map.put("Formula", variant);

						String torque = orginalLine.getPropertyDisplayableValue("VL5_torque_inf");
						map.put("Torque", torque.replace("\n", ""));

						map.put("Platform", platformCode);
						map.put("Year", modelYear);

						TCComponent[] BOP = node.foundNodes;
						if (BOP.length == 1) {
							TCComponentBOMLine foundLine = (TCComponentBOMLine) BOP[0];
							TCComponentBOMLine operation = foundLine.parent();

							map.put("BOP", operation.getPropertyDisplayableValue("bl_item_item_id"));
							map.put("BOP Revision", operation.getPropertyDisplayableValue("bl_rev_item_revision_id"));
							String WS = UIGetValuesUtility.getWorkStationID(operation, "bl_rev_object_name");// getWorkStationID(operation, "object_name");
							map.put("Workstation", WS);

							String type = operation.getPropertyDisplayableValue("vf4_operation_type");
							if (type.equals("NA") || type.equals("Automatic Consumption"))
								type = "N";
							else
								type = "Y";
							map.put("MES", type);
						} else {
							map.put("BOP", "NO BOP");
							map.put("BOP Revision", "");
							map.put("Workstation", "");
							map.put("MES", "");
						}

						valuesMap.add(map);
					}
					afterAction = TCExtension.getCurrentTime();
					System.out.print("Find BOM/BOP " + beforeAction + " => " + afterAction);
					
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
					LocalDateTime now = LocalDateTime.now();
					print2Text(valuesMap, "SuperBOMBOP_" + shop.getProgram() + "_" + shop.getShopName() + "_" + shop.getShopObject().getPropertyDisplayableValue("bl_item_item_id") + "_" + variantRule + "_" + dtf.format(now));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private LinkedList<TCComponentBOMLine> expandBom(TCComponentBOMLine selectedObject) {
		LinkedList<TCComponentBOMLine> output = new LinkedList<TCComponentBOMLine>();
		try {
			String beforeAction = TCExtension.getCurrentTime();
			LinkedList<TCComponentBOMLine> bomlines = TCExtension.ExpandAllBOMLines(selectedObject, session);
			String afterAction = TCExtension.getCurrentTime();
			System.out.print("Open Bom " + beforeAction + " => " + afterAction);
			if (bomlines != null) {
				beforeAction = TCExtension.getCurrentTime();
				for (TCComponentBOMLine bomline : bomlines) {
					if (validateBomLine(bomline))
						output.add(bomline);
				}
				afterAction = TCExtension.getCurrentTime();
				System.out.print("Process logic " + beforeAction + " => " + afterAction);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return output;
	}

	private boolean validateBomLine(TCComponentBOMLine bomLine) {
		try {
			String bomlineID = bomLine.getPropertyDisplayableValue("VF4_bomline_id");
			if (bomlineID.isEmpty())
				return false;

//			String type = bomLine.getPropertyDisplayableValue("fnd0bl_line_object_type");
//			if (type.compareTo("VF3_vfitemRevision") == 0)
//				return false;
//
//			if (type.compareTo("VF4_CostRevision") == 0)
//				return false;
//
//			if (type.compareTo("VF4_str_partRevision") == 0)
//				return false;
//
//			if (type.compareTo("ItemRevision") == 0)
//				return false;
//
//			String purLevel = bomLine.getPropertyDisplayableValue("VF3_purchase_lvl_vf");
//			if (purLevel.isEmpty()) {
//				purLevel = bomLine.getPropertyDisplayableValue("VL5_purchase_lvl_vf");
//			}
//			if (purLevel.compareTo("P") != 0 && purLevel.compareTo("IB") != 0 && purLevel.compareTo("K") != 0)
//				return false;
//
//			String manufCode = bomLine.getPropertyDisplayableValue("VF4_manuf_code");
//			if (manufCode.equalsIgnoreCase("Assy"))
//				return false;
//
//			TCComponentBOMLine bomParent = bomLine.parent();
//			if (bomParent != null) {
//				String bomParentType = bomParent.getPropertyDisplayableValue("fnd0bl_line_object_type");
//				if ((bomParentType.compareTo("VF3_vfitemRevision") != 0 && bomParentType.compareTo("VF4_CostRevision") != 0 && bomParentType.compareTo("VF4_str_partRevision") != 0 && bomParentType.compareTo("ItemRevision") != 0))
//					return false;
//			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void print2Text(ArrayList<HashMap<String, String>> valuesMap, String fileName) {
		UIGetValuesUtility.createFolder(folderPath);
		String[] header = new String[] { "Main Group", "Sub Group", "Part No", "Part Name", "POS ID", "Bomline", "Quantity", "Formula", "Torque", "Platform", "Year", "BOP", "BOP Revision", "Workstation", "MES" };
		File file = new File(folderPath + fileName + ".csv");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			writer.write(String.join(",", header));
			writer.newLine();

			for (HashMap<String, String> map : valuesMap) {
				LinkedList<String> lineValue = new LinkedList<String>();
				for (String head : header) {
					if (map.containsKey(head)) {
						lineValue.add(map.get(head));
					} else {
						lineValue.add("");
					}
				}
				writer.write(String.join(",", lineValue));
				writer.newLine();
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
