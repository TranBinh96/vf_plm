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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.teamcenter.integration.ulti.StringExtension;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.vinfast.sap.util.UIGetValuesUtility;

public class UnitBOMBOPReport_Thread implements Runnable {
	private String folderPath = "C:\\temp\\";
	private TCSession session = null;
	private DataManagementService dmService = null;
	private TCComponentBOMLine topBOPLine = null;
	private TCComponentBOMLine topBOMLine = null;
	private ShopModel shop = null;

	public UnitBOMBOPReport_Thread(ShopModel _shop, TCSession _session) {
		session = _session;
		shop = _shop;
		dmService = DataManagementService.getService(session);
	}

	@Override
	public void run() {
		try {
			OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(session, shop.getShopObject());
			for (OpenContextInfo views : createdBOMViews) {
				if (views.context.getType().equals("BOMLine"))
					topBOMLine = (TCComponentBOMLine) views.context;
				else if (views.context.getType().equals("Mfg0BvrPlantBOP"))
					topBOPLine = (TCComponentBOMLine) views.context;
				else
					UIGetValuesUtility.closeContext(session, (TCComponentBOMLine) views.context);
			}

			LinkedHashMap<TCComponentBOMLine, HashMap<String, LinkedList<TCComponentBOMLine>>> processData = expandBom(topBOMLine);
			if (processData != null && processData.size() > 0) {
				ArrayList<HashMap<String, String>> valuesMap = new ArrayList<HashMap<String, String>>();
				for (Map.Entry<TCComponentBOMLine, HashMap<String, LinkedList<TCComponentBOMLine>>> process : processData.entrySet()) {
					LinkedList<TCComponentBOMLine> bomLinesInput = process.getValue().get("Input");
					if (bomLinesInput != null && bomLinesInput.size() > 0) {
						LinkedList<TCComponentBOMLine> bomLinesOutput = process.getValue().get("Output");
						if (topBOPLine == null) {
							for (TCComponentBOMLine input : bomLinesInput) {
								String parentPartNo = input.getPropertyDisplayableValue("bl_item_item_id");
								String bomlineID = input.getPropertyDisplayableValue("VF4_bomline_id");
								if (StringExtension.isInteger(bomlineID, 10)) {
									bomlineID = StringExtension.ConvertNumberToString(Integer.parseInt(bomlineID), 4);
								}
								String qty = input.getPropertyDisplayableValue("bl_quantity");
								if (qty.isEmpty())
									qty = "1.000";

								String torque = input.getPropertyDisplayableValue("VL5_torque_inf");

								if (bomLinesOutput != null && bomLinesOutput.size() > 0) {
									for (TCComponentBOMLine output : bomLinesOutput) {
										HashMap<String, String> map = new HashMap<String, String>();
										map.put("Material", output.getPropertyDisplayableValue("bl_item_item_id"));
										map.put("Component", parentPartNo);
										map.put("Bomline ID", bomlineID);
										map.put("Quantity", qty);
										map.put("Torque", torque);
										map.put("BOP", "");
										map.put("Workstation", "");
										valuesMap.add(map);
									}
								}
							}
						} else {
							NodeInfo[] nodes = TCExtension.findBOMLineInBOP(topBOPLine, bomLinesInput.toArray(new TCComponentBOMLine[0]), session);
							if (nodes != null) {
								try {
									for (NodeInfo node : nodes) {
										TCComponent BOM = node.originalNode;
										TCComponent[] BOP = node.foundNodes;
										TCComponentBOMLine orginalLine = (TCComponentBOMLine) BOM;

										String parentPartNo = orginalLine.getPropertyDisplayableValue("bl_item_item_id");
										String bomlineID = orginalLine.getPropertyDisplayableValue("VF4_bomline_id");
										if (StringExtension.isInteger(bomlineID, 10)) {
											bomlineID = StringExtension.ConvertNumberToString(Integer.parseInt(bomlineID), 4);
										}
										String qty = orginalLine.getPropertyDisplayableValue("bl_quantity");
										if (qty.isEmpty())
											qty = "1.000";

										String torque = orginalLine.getPropertyDisplayableValue("VL5_torque_inf");

										String bopNo = "";
										String workStation = "";

										if (BOP.length == 1) {
											TCComponentBOMLine foundLine = (TCComponentBOMLine) BOP[0];
											TCComponentBOMLine operation = foundLine.parent();
											bopNo = operation.getPropertyDisplayableValue("bl_item_item_id");
											workStation = UIGetValuesUtility.getWorkStationID(operation, "bl_rev_object_name");
										}

										if (bomLinesOutput != null && bomLinesOutput.size() > 0) {
											for (TCComponentBOMLine output : bomLinesOutput) {
												HashMap<String, String> map = new HashMap<String, String>();
												map.put("Material", output.getPropertyDisplayableValue("bl_item_item_id"));
												map.put("Component", parentPartNo);
												map.put("Bomline ID", bomlineID);
												map.put("Quantity", qty);
												map.put("Torque", torque);
												map.put("BOP", bopNo);
												map.put("Workstation", workStation);
												valuesMap.add(map);
											}
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
				LocalDateTime now = LocalDateTime.now();
				print2Text(valuesMap, "UnitBOMBOP_" + shop.getProgram() + "_" + shop.getShopName() + "_" + shop.getShopID() + "_" + dtf.format(now));
			}

			if (topBOMLine != null)
				UIGetValuesUtility.closeContext(session, topBOMLine);
			if (topBOPLine != null)
				UIGetValuesUtility.closeContext(session, topBOPLine);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private LinkedHashMap<TCComponentBOMLine, HashMap<String, LinkedList<TCComponentBOMLine>>> expandBom(TCComponentBOMLine selectedObject) {
		LinkedHashMap<TCComponentBOMLine, HashMap<String, LinkedList<TCComponentBOMLine>>> outputData = new LinkedHashMap<TCComponentBOMLine, HashMap<String, LinkedList<TCComponentBOMLine>>>();
		try {
			LinkedList<TCComponentBOMLine> bomlines = TCExtension.ExpandAllBOMLines(selectedObject, session);
			if (bomlines != null) {
				String[] property = new String[] { "VF4_bomline_id", "VF4_manuf_code", "VF3_purchase_lvl_vf", "VL5_purchase_lvl_vf" };
				dmService.getProperties(bomlines.toArray(new TCComponent[0]), property);
				for (TCComponentBOMLine bomline : bomlines) {
					String manuType = validateBomLine(bomline);
					if (!manuType.isEmpty()) {
						TCComponentBOMLine bomParent = bomline.parent();
						if (outputData.containsKey(bomParent)) {
							if (manuType.compareTo("Input") == 0) {
								if (outputData.get(bomParent).containsKey("Input")) {
									outputData.get(bomParent).get("Input").add(bomline);
								} else {
									LinkedList<TCComponentBOMLine> newBom = new LinkedList<TCComponentBOMLine>();
									newBom.add(bomline);
									outputData.get(bomParent).put("Input", newBom);
								}
							} else {
								if (outputData.get(bomParent).containsKey("Output")) {
									outputData.get(bomParent).get("Output").add(bomline);
								} else {
									LinkedList<TCComponentBOMLine> newBom = new LinkedList<TCComponentBOMLine>();
									newBom.add(bomline);
									outputData.get(bomParent).put("Output", newBom);
								}
							}
						} else {
							HashMap<String, LinkedList<TCComponentBOMLine>> newInOut = new LinkedHashMap<String, LinkedList<TCComponentBOMLine>>();
							LinkedList<TCComponentBOMLine> newBom = new LinkedList<TCComponentBOMLine>();
							newBom.add(bomline);
							if (manuType.compareTo("Input") == 0) {
								newInOut.put("Input", newBom);
							} else {
								newInOut.put("Output", newBom);
							}
							outputData.put(bomParent, newInOut);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outputData;
	}

	private String validateBomLine(TCComponentBOMLine bomLine) {
		try {
//			String bomlineID = bomLine.getPropertyDisplayableValue("VF4_bomline_id");
//			if (bomlineID.isEmpty())
//				return "";

//			String purLevel = bomLine.getPropertyDisplayableValue("VF3_purchase_lvl_vf");
//			if (purLevel.isEmpty()) {
//				purLevel = bomLine.getPropertyDisplayableValue("VL5_purchase_lvl_vf");
//			}
//			if (purLevel.compareTo("P") != 0 && purLevel.compareTo("IB") != 0 && purLevel.compareTo("H") != 0)
//				return "";

			String manufCode = bomLine.getPropertyDisplayableValue("VF4_manuf_code");
			if (!manufCode.equalsIgnoreCase("Input") && !manufCode.equalsIgnoreCase("Output"))
				return "";

			if (manufCode.equalsIgnoreCase("Input"))
				return "Input";
			else
				return "Output";
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private void print2Text(ArrayList<HashMap<String, String>> valuesMap, String fileName) {
		UIGetValuesUtility.createFolder(folderPath);
		String[] header = new String[] { "Material", "Component", "Bomline ID", "Quantity", "Torque", "BOP", "Workstation" };
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
