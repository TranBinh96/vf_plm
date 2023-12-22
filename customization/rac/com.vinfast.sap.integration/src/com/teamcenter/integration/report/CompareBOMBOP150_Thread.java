package com.teamcenter.integration.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sap.util.UIGetValuesUtility;

public class CompareBOMBOP150_Thread {
	private String folderPath = "C:\\temp\\";
	private TCSession session = null;
	private DataManagementService dmService = null;
	private TCComponentBOMLine topBOPLine = null;
	private TCComponentBOMLine topBOMLine = null;
	private ShopModel shop = null;

	public CompareBOMBOP150_Thread(ShopModel shop, TCComponentBOMLine topBOMLine, TCComponentBOMLine topBOPLine, TCSession session) {
		this.shop = shop;
		this.topBOMLine = topBOMLine;
		this.topBOPLine = topBOPLine;
		this.session = session;
		this.dmService = DataManagementService.getService(session);
	}

	public void run() {
		try {
			LinkedHashMap<String, Integer> bomLines = expandBom(topBOMLine);
			LinkedHashMap<String, Integer> bopLines = expandBom(topBOPLine);

			print2Text(bomLines, bopLines, "CompareBOMBOP_" + shop.getProgram() + "_" + shop.getShopName() + "_" + shop.getShopID() + "_" + TCExtension.getCurrentTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private LinkedHashMap<String, Integer> expandBom(TCComponentBOMLine selectedObject) {
		LinkedHashMap<String, Integer> output = new LinkedHashMap<String, Integer>();
		try {
			String beforeAction = TCExtension.getCurrentTime();
			LinkedList<TCComponentBOMLine> bomlines = TCExtension.ExpandAllBOMLines(selectedObject, session);
			String afterAction = TCExtension.getCurrentTime();
			System.out.print("Open Bom " + beforeAction + " => " + afterAction);
			if (bomlines != null) {
				String[] bomProperties = new String[] { "awb0BomLineItemId", "bl_rev_item_revision_id", "bl_item_object_name", "VF4_bomline_id" };
				dmService.getProperties(bomlines.toArray(new TCComponentBOMLine[0]), bomProperties);
				beforeAction = TCExtension.getCurrentTime();
				for (TCComponentBOMLine bomline : bomlines) {
					if (validateBomLine(bomline)) {
						String bomlineID = bomline.getPropertyDisplayableValue("awb0BomLineItemId") + "/" + bomline.getPropertyDisplayableValue("bl_rev_item_revision_id") + " - " + bomline.getPropertyDisplayableValue("bl_item_object_name");
						if (output.containsKey(bomlineID)) {
							int count = output.get(bomlineID);
							output.put(bomlineID, ++count);
						} else {
							output.put(bomlineID, 1);
						}
					}
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
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void print2Text(LinkedHashMap<String, Integer> bomList, LinkedHashMap<String, Integer> bopList, String fileName) {
		UIGetValuesUtility.createFolder(folderPath);
		File file = new File(folderPath + fileName + ".csv");
		BufferedWriter writer = null;
		try {
			String[] header = new String[] { "BOM Line", "MBOM", "BOP" };
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			writer.write(String.join(",", header));
			writer.newLine();

			LinkedHashMap<String, Integer> bopListRemove = bopList;

			for (Map.Entry<String, Integer> bomline : bomList.entrySet()) {
				LinkedList<String> lineValue = new LinkedList<String>();
				lineValue.add(bomline.getKey());
				lineValue.add(Integer.toString(bomline.getValue()));
				if (bopList.containsKey(bomline.getKey())) {
					lineValue.add(Integer.toString(bopList.get(bomline.getKey())));
					bopListRemove.remove(bomline.getKey());
				} else
					lineValue.add("");

				writer.write(String.join(",", lineValue));
				writer.newLine();
			}
			for (Map.Entry<String, Integer> bomline : bopListRemove.entrySet()) {
				LinkedList<String> lineValue = new LinkedList<String>();
				lineValue.add(bomline.getKey());
				lineValue.add("");
				lineValue.add(Integer.toString(bomline.getValue()));

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
