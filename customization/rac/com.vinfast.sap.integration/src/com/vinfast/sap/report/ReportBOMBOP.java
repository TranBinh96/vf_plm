package com.vinfast.sap.report;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2008_06.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.cad._2008_06.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.rac.cad._2008_06.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.rac.cad._2008_06.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.rac.cad._2008_06.StructureManagement.ExpandPSOneLevelResponse2;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing.CoreService;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FindNodeInContextResponse;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FoundNodesInfo;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.services.rac.manufacturing._2013_05.Core.FindNodeInContextInputInfo;
import com.vinfast.sap.configurator.MaterialPlatformCode;
import com.vinfast.sap.util.UIGetValuesUtility;

public class ReportBOMBOP extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
			AIFComponentContext selectedLines = application.getTargetContext();
			DataManagementService DMService = DataManagementService.getService(session);
			TCComponent mainGroup = getMainGroupName(DMService, (TCComponent) selectedLines.getComponent());
			TCComponentBOMLine t_MainGroup = (TCComponentBOMLine) mainGroup;
			String strMainGroup = t_MainGroup.getItemRevision().getProperty("object_name");
			if (selectedLines != null && !strMainGroup.equals("")) {
				TCComponentBOMLine selectedLine = (TCComponentBOMLine) selectedLines.getComponent();
				MaterialPlatformCode platformCode = UIGetValuesUtility.getPlatformCode(DMService, t_MainGroup.getItemRevision());
				String subGroup = selectedLine.getItem().getProperty("item_id");
				TCComponentBOMWindow window = selectedLine.window();
				TCComponentBOMLine topBOMLine = window.getTopBOMLine();
				TCComponentItem topLineItem = topBOMLine.getItem();
				DMService.getProperties(new TCComponent[] { topLineItem }, new String[] { "IMAN_master_form" });
				TCComponentForm shopMasterForm = (TCComponentForm) topLineItem.getRelatedComponent("IMAN_master_form");
				String shopPlantBOMID = shopMasterForm.getProperty("user_data_2");
				TCComponentItem bopTopItem = UIGetValuesUtility.findItem(session, shopPlantBOMID);
				OpenContextInfo[] contextInfo = UIGetValuesUtility.createContextViews(session, bopTopItem);
				TCComponentBOMLine bopTOPLine = (TCComponentBOMLine) contextInfo[0].context;
				UIGetValuesUtility.setViewReference(session, topBOMLine, bopTOPLine);
				TCComponent[] validBOMLines = traverseBOM(session, DMService, new TCComponentBOMLine[] { selectedLine });
				File printFile = new File("C:\\Temp\\" + subGroup + ".html");
				if (validBOMLines != null) {
					StringBuffer print = new StringBuffer();
					print.append("<html><body><table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border= \"1\" style=\"width:100%\">" + "<tr style=\"background-color:#ccffff;\"><th><b>S.NO</b></th><th><b>PLATFORM</b></th><th><b>MODEL YEAR</b></th><th><b>MAIN GROUP</b></th><th><b>SUB GROUP</b></th>" + "<th><b>PART NUMBER</b></th><th><b>BOMLINE ID</b></th><th><b>FORMULA</b></th><th><b>BOP</b></th><th><b>REVISION</b></th><th><b>WORKSTATION</b></th></tr>");
					NodeInfo[] linkedBOP = findBOMLineInBOP(session, validBOMLines, bopTOPLine);
					int count = 1;
					for (NodeInfo childBOMLine : linkedBOP) {
						TCComponentBOMLine bomLine = (TCComponentBOMLine) childBOMLine.originalNode;
						TCComponentBOMLine bopLine = (TCComponentBOMLine) childBOMLine.foundNodes[0];

						print.append("<tr>");
						print.append("<td align=\"center\">" + count + "</td>");
						print.append("<td align=\"center\">" + platformCode.getPlatformCode() + "</td>");
						print.append("<td align=\"center\">" + platformCode.getModelYear() + "</td>");
						print.append("<td align=\"center\">" + strMainGroup + "</td>");
						print.append("<td align=\"center\">" + subGroup + "</td>");
						getBOMValues(DMService, bomLine, print, platformCode.getPlatformCode() + "_" + platformCode.getModelYear());
						getBOPValues(DMService, bopLine, print);
						print.append("</tr>");
						count++;
					}
					print.append("</table></body></html>");
					BufferedWriter bwr = new BufferedWriter(new FileWriter(printFile));
					bwr.write(print.toString());
					bwr.flush();
					bwr.close();
					Desktop.getDesktop().browse(printFile.toURI());
				}

				UIGetValuesUtility.closeContext(session, bopTOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public TCComponent getMainGroupName(DataManagementService DMService, TCComponent selectedLines) {
		TCComponent mainGroup = null;
		try {
			DMService.getProperties(new TCComponent[] { selectedLines }, new String[] { "bl_rev_object_name", "bl_parent", "bl_level_starting_0" });
			String level = selectedLines.getProperty("bl_level_starting_0");
			if (level.equals("1")) {
				mainGroup = selectedLines;
			} else {
				TCComponent part = selectedLines.getReferenceProperty("bl_parent");
				mainGroup = getMainGroupName(DMService, part);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return mainGroup;
	}

	public void getBOMValues(DataManagementService DMService, TCComponentBOMLine bomLine, StringBuffer print, String platformCode) {
		try {
			String[] bomProp = { "bl_item_item_id", "VF4_bomline_id", "bl_quantity", "VF3_purchase_lvl_vf", "bl_formula" };
			DMService.getProperties(new TCComponent[] { bomLine }, bomProp);

			String variant = bomLine.getProperty("bl_formula");
			if (variant.equals("") == false) {
				variant = variant.substring(0, variant.length());
				Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(variant);
				while (m.find()) {
					variant = variant.replace(m.group(1), platformCode);
				}
			}

			print.append("<td align=\"center\">" + bomLine.getProperty("bl_item_item_id") + "</td>");
			print.append("<td align=\"center\">" + bomLine.getProperty("VF4_bomline_id") + "</td>");
			print.append("<td>" + variant + "</td>");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public void getBOPValues(DataManagementService DMService, TCComponentBOMLine bopLine, StringBuffer print) {
		TCComponentBOMLine operation = null;
		try {
			String[] bomProp = { "bl_parent" };
			DMService.getProperties(new TCComponent[] { bopLine }, bomProp);
			operation = (TCComponentBOMLine) bopLine.getReferenceProperty("bl_parent");

			if (operation != null && operation.getItemRevision().getType().equals("MEOPRevision")) {
				String BOPID = operation.getProperty("bl_item_item_id");
				print.append("<td align=\"center\">" + BOPID + "</td>");
				String BOPIDRev = operation.getProperty("bl_rev_item_revision_id");
				print.append("<td align=\"center\">" + BOPIDRev + "</td>");
				String workStation = getWorkStationID(operation, "bl_rev_object_name");
				print.append("<td align=\"center\">" + workStation + "</td>");
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public String getWorkStationID(TCComponentBOMLine operation, String plm_tag) {
		String workstationID = "";
		TCComponentBOMLine procLine = null;
		TCComponentBOMLine shopLine = null;
		TCComponentBOMLine topLine = null;
		try {
			TCComponentItemRevision operationRevision = operation.getItemRevision();
			if (operationRevision.getType().equals("MEOPRevision")) {
				TCComponentBOMLine workstation = (TCComponentBOMLine) operation.getReferenceProperty("bl_parent");
				TCComponentItemRevision workstationRevision = workstation.getItemRevision();

				if (workstationRevision.getType().equals("Mfg0MEProcStatnRevision")) {
					String workStationName = workstation.getProperty(plm_tag);// PT-01LH
					String split_StationName[] = workStationName.split("-");
					String processLine = split_StationName[0].trim();// PT
					String workStatName = split_StationName[1].trim();// 01LH

					procLine = (TCComponentBOMLine) workstation.getReferenceProperty("bl_parent");
					String procLineName = procLine.getProperty(plm_tag).substring(0, 2).trim();// PT

					if (procLineName.equals(processLine)) {
						shopLine = (TCComponentBOMLine) procLine.getReferenceProperty("bl_parent");// PS
						String shopName = shopLine.getProperty(plm_tag).substring(0, 2).trim();

						topLine = (TCComponentBOMLine) shopLine.getReferenceProperty("bl_parent");
						String plantName = topLine.getProperty(plm_tag).substring(0, 4).trim();
						workstationID = plantName + "_" + shopName + procLineName + workStatName;
					}
				}
			}

		} catch (TCException e1) {
			e1.printStackTrace();
		}

		return workstationID;
	}

	public NodeInfo[] findBOMLineInBOP(TCSession session, TCComponent[] selected_Objects, TCComponentBOMLine targetBomLine) {
		CoreService structService = CoreService.getService(session);
		NodeInfo[] bomNodeBop = null;
		FindNodeInContextInputInfo[] InContextInputInfo = new FindNodeInContextInputInfo[1];
		InContextInputInfo[0] = new FindNodeInContextInputInfo();
		InContextInputInfo[0].context = targetBomLine;
		InContextInputInfo[0].nodes = selected_Objects;
		InContextInputInfo[0].allContexts = false;
		InContextInputInfo[0].byIdOnly = true;
		InContextInputInfo[0].relationDepth = 0;
		InContextInputInfo[0].relationDirection = 1;
		InContextInputInfo[0].relationTypes = new String[] { "FND_TraceLink" };

		FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(InContextInputInfo);
		FoundNodesInfo[] nodeInfo = InContextInputResponse.resultInfo;

		bomNodeBop = nodeInfo[0].resultNodes;

		return bomNodeBop;
	}

	public TCComponent[] traverseBOM(TCSession session, DataManagementService DMService, TCComponentBOMLine[] bomLines) {
		TCComponent[] validLines = null;
		ArrayList<TCComponentBOMLine> bomBOPValues = new ArrayList<TCComponentBOMLine>();
		String[] prop_Values = { "bl_rev_item_id", "bl_rev_object_type", "VF4_bomline_id", "VF3_purchase_lvl_vf", "VF4_manuf_code" };

		StructureManagementService SMService = StructureManagementService.getService(session);
		try {

			ExpandPSOneLevelInfo oneLevelInfo = new ExpandPSOneLevelInfo();
			oneLevelInfo.parentBomLines = bomLines;
			oneLevelInfo.excludeFilter = "None2";

			ExpandPSOneLevelPref oneLevelPref = new ExpandPSOneLevelPref();
			oneLevelPref.expItemRev = false;

			ExpandPSOneLevelResponse2 expandOutput = SMService.expandPSOneLevel(oneLevelInfo, oneLevelPref);
			ExpandPSOneLevelOutput output[] = expandOutput.output;
			ExpandPSData[] bomDataLines = output[0].children;

			for (ExpandPSData bomLineIterator : bomDataLines) {
				TCComponentBOMLine traverseLine = bomLineIterator.bomLine;
				TCComponent traverseObject = bomLineIterator.objectOfBOMLine;

				if ((traverseObject.getDisplayType().equals("VF Item Revision") || traverseObject.getDisplayType().equals("Item Revision")) == false) {
					DMService.getProperties(new TCComponent[] { traverseLine }, prop_Values);
					String[] bomlinePropValues = traverseLine.getProperties(prop_Values);
					if (UIGetValuesUtility.isBOMLinePurchase(bomlinePropValues[3].trim()) == true && UIGetValuesUtility.isToProcessAssembly(bomlinePropValues[2].trim(), bomlinePropValues[4].trim()) == true) {
						bomBOPValues.add(traverseLine);
					}
				}
			}

			if (bomBOPValues.size() != 0) {
				validLines = new TCComponentBOMLine[bomBOPValues.size()];
				int count = 0;

				for (TCComponentBOMLine line : bomBOPValues) {
					validLines[count] = line;
					count++;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return validLines;
	}
}