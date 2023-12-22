package com.teamcenter.integration.ulti;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfg0ProductItem;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.configurator.MaterialPlatformCode;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class BOMManagement {
	int serialNumber = 1;
	boolean isWindowOpen;

	private String shopName;
	private String model;
	private String year;
	private String plant;
	private String shop;
	private String MCN = "";
	private String materialcode;
	private StringBuilder strBom = new StringBuilder();
	private StringBuilder strBop = new StringBuilder();

	private TCComponentItemRevision topLevelItemRevision;
	private TCComponent shopItem;
	private TCComponent mbomTOPLine;
	private TCComponent mbomTraverseLine;
	private TCComponent bopTOPLine;
	private TCComponent bopTraverseLine;

	OpenContextInfo[] contextView;
	private TCComponent[] impactedItems;
	private TCComponent[] problemItems;
	private TCComponent[] solutionItems;
	private Set<String> setItemInReleaseFolder = new HashSet<String>();

	private TCComponentItemRevision changeObject = null;
	private DataManagementService dmService = null;
	private TCSession session = null;
	ArrayList<TCComponentBOMLine> operationNoPart = new ArrayList<TCComponentBOMLine>();

	public BOMManagement(TCSession _session, TCComponentItemRevision _changeObject, String company) {
		try {
			changeObject = _changeObject;
			session = _session;
			dmService = DataManagementService.getService(session);
			TCComponent[] impactedShop = UIGetValuesUtility.getRelatedComponents(dmService, changeObject, new String[] {}, PropertyDefines.REL_IMPACT_SHOP);
			if (impactedShop != null && impactedShop.length == 1) {
				shopItem = impactedShop[0];
				if (company.compareTo(PropertyDefines.VIN_FAST_ELECTRIC) == 0) {
					topLevelItemRevision = (TCComponentItemRevision) shopItem;
				} else {
					if (shopItem instanceof TCComponentItemRevision)
						topLevelItemRevision = UIGetValuesUtility.getTopLevelItemRevision(session, (TCComponentItemRevision) shopItem, PropertyDefines.REVISION_RULE_WORKING);
				}
				MaterialPlatformCode plaformCode = null;
				if (topLevelItemRevision != null) {
					plaformCode = UIGetValuesUtility.getPlatformCode(dmService, topLevelItemRevision);
				} else {
					if (shopItem instanceof TCComponentCfg0ProductItem)
						plaformCode = UIGetValuesUtility.getPlatformCode(dmService, (TCComponentCfg0ProductItem) shopItem);
				}

				if (plaformCode == null || plaformCode.getPlatformCode().isEmpty()) {
					String objectName = topLevelItemRevision.getProperty(PropertyDefines.ITEM_NAME);
					if (objectName.contains("_")) {
						String[] name = objectName.split("_");
						model = name[0];
					}
					year = "";
					shopName = "";
					materialcode = plaformCode.getMaterialCode();
				} else {
					model = plaformCode.getPlatformCode();
					year = plaformCode.getModelYear();
					shopName = impactedShop[0].getProperty("object_name").replace(" ", "_");
					materialcode = plaformCode.getMaterialCode();
				}

				plant = changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT);
				shop = changeObject.getItem().getPropertyDisplayableValue("vf6_shop");
				String MCN_SAPID = changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				if (MCN_SAPID != null && MCN_SAPID.length() > 8)
					MCN = MCN_SAPID.substring(MCN_SAPID.length() - 8);

				impactedItems = UIGetValuesUtility.getRelatedComponents(dmService, changeObject, new String[] {}, PropertyDefines.REL_IMPACT_ITEMS);

				problemItems = UIGetValuesUtility.getRelatedComponents(dmService, changeObject, new String[] {}, PropertyDefines.REL_PRB_ITEMS);

				solutionItems = UIGetValuesUtility.getRelatedComponents(dmService, changeObject, new String[] {}, PropertyDefines.REL_SOL_ITEMS);

				TCComponent[] itemForRelease = UIGetValuesUtility.getRelatedComponents(dmService, changeObject, new String[] {}, PropertyDefines.REL_SOL_ITEMS);
				if (itemForRelease != null) {
					for (int i = 0; i < itemForRelease.length; i++) {
						try {
							String stationName = itemForRelease[i].getProperty(PropertyDefines.ITEM_ID);
							setItemInReleaseFolder.add(stationName);
						} catch (TCException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (TCException e1) {
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
	}

	public boolean createMBOMBOPWindow() {
		try {
			OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(session, topLevelItemRevision);
			if (createdBOMViews != null) {
				// open bom with revision rule Working Any Status
				setContextView(createdBOMViews);
				isWindowOpen = true;
				for (OpenContextInfo views : createdBOMViews) {
					if (views.context.getType().equals("BOMLine")) {
						// handle case top topLevelItemRevision is MBOM
						String shopID = getShopItem().getProperty("item_id");
						TCComponentBOMLine topBOMLine = (TCComponentBOMLine) views.context;
						setMBOMTOPLine(views.context);
						setMBOMTraverseLine(views.context);

						AIFComponentContext[] childerns = topBOMLine.getChildren();
						for (AIFComponentContext child : childerns) {
							TCComponent childLine = (TCComponent) child.getComponent();
							if (childLine.getProperty("bl_rev_item_id").equals(shopID)) {
								// The impacted shop line
								setMBOMTraverseLine(childLine);
								break;
							}
						}
					}
					if (views.context.getType().equals("Mfg0BvrPlantBOP")) {
						// handle case top topLevelItemRevision is BOP
						operationNoPart = new ArrayList<TCComponentBOMLine>();
						TCComponentBOMLine topBOPLine = (TCComponentBOMLine) views.context;
						TCComponentItem topLevelItem = topLevelItemRevision.getItem();
						TCComponentForm topLevelItemMasterForm = (TCComponentForm) topLevelItem.getRelatedComponent("IMAN_master_form");
						String bopTOPID = topLevelItemMasterForm.getProperty("user_data_2");
						if (bopTOPID.isEmpty()) {
							setBOPTOPLine(views.context);
						} else {
							if (topBOPLine.getProperty("bl_rev_item_id").equals(bopTOPID)) {
								setBOPTOPLine(views.context);
							} else {
								TCComponentItem plantModel = UIGetValuesUtility.findItem(session, bopTOPID);
								OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(session, plantModel);
								topBOPLine = (TCComponentBOMLine) createdBOPView[0].context;
								setBOPTOPLine(createdBOPView[0].context);
							}
						}

						bopTraverseLine = topBOPLine;
						TCComponentItemRevision shopItemRevision = (TCComponentItemRevision) getShopItem();
						TCComponentItem shopItem = shopItemRevision.getItem();
						TCComponentForm shopItemMasterForm = (TCComponentForm) shopItem.getRelatedComponent("IMAN_master_form");
						String bopTraversalID = shopItemMasterForm.getProperty("user_data_2");

						if (!bopTraversalID.isEmpty()) {
							boolean BOPFound = false;
							AIFComponentContext[] childerns = topBOPLine.getChildren();
							for (AIFComponentContext child : childerns) {
								TCComponent childLine = (TCComponent) child.getComponent();
								if (childLine.getProperty("bl_rev_item_id").equals(bopTraversalID)) {
									bopTraverseLine = childLine;
									BOPFound = true;
									break;
								}
							}

							if (BOPFound == false) {
								TCComponentItem plantModel = UIGetValuesUtility.findItem(session, bopTraversalID);
								OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(session, plantModel);
								topBOPLine = (TCComponentBOMLine) createdBOPView[0].context;
								setBOPTOPLine(createdBOPView[0].context);
								bopTraverseLine = createdBOPView[0].context;
							}
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return true;
	}

	public boolean createMBOMBOPWindowWithRule(String rule) {
		try {
			TCComponentItemRevision topLevelItemRevision = getTopLevelItemRevision();
			OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(session, topLevelItemRevision, rule);
			if (createdBOMViews != null) {
				// open bom with revision rule Working Any Status
				setContextView(createdBOMViews);
				isWindowOpen = true;
				for (OpenContextInfo views : createdBOMViews) {
					if (views.context.getType().equals("BOMLine")) {
						// handle case top topLevelItemRevision is MBOM
						String shopID = getShopItem().getProperty("item_id");
						TCComponentBOMLine topBOMLine = (TCComponentBOMLine) views.context;
						setMBOMTOPLine(views.context);
						setMBOMTraverseLine(views.context);

						AIFComponentContext[] childerns = topBOMLine.getChildren();
						for (AIFComponentContext child : childerns) {
							TCComponent childLine = (TCComponent) child.getComponent();
							if (childLine.getProperty("bl_rev_item_id").equals(shopID)) {
								// The impacted shop line
								setMBOMTraverseLine(childLine);
								break;
							}
						}
					}
					if (views.context.getType().equals("Mfg0BvrPlantBOP")) {
						// handle case top topLevelItemRevision is BOP
						operationNoPart = new ArrayList<TCComponentBOMLine>();
						TCComponentBOMLine topBOPLine = (TCComponentBOMLine) views.context;
						TCComponentItem topLevelItem = topLevelItemRevision.getItem();
						TCComponentForm topLevelItemMasterForm = (TCComponentForm) topLevelItem.getRelatedComponent("IMAN_master_form");
						String bopTOPID = topLevelItemMasterForm.getProperty("user_data_2");
						if (bopTOPID.isEmpty()) {
							setBOPTOPLine(views.context);
						} else {
							if (topBOPLine.getProperty("bl_rev_item_id").equals(bopTOPID)) {
								setBOPTOPLine(views.context);
							} else {
								TCComponentItem plantModel = UIGetValuesUtility.findItem(session, bopTOPID);
								OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(session, plantModel, rule);
								topBOPLine = (TCComponentBOMLine) createdBOPView[0].context;
								setBOPTOPLine(createdBOPView[0].context);
							}
						}

						bopTraverseLine = topBOPLine;
						TCComponentItemRevision shopItemRevision = (TCComponentItemRevision) getShopItem();
						TCComponentItem shopItem = shopItemRevision.getItem();
						TCComponentForm shopItemMasterForm = (TCComponentForm) shopItem.getRelatedComponent("IMAN_master_form");
						String bopTraversalID = shopItemMasterForm.getProperty("user_data_2");

						if (!bopTraversalID.isEmpty()) {
							boolean BOPFound = false;
							AIFComponentContext[] childerns = topBOPLine.getChildren();
							for (AIFComponentContext child : childerns) {
								TCComponent childLine = (TCComponent) child.getComponent();
								if (childLine.getProperty("bl_rev_item_id").equals(bopTraversalID)) {
									bopTraverseLine = childLine;
									BOPFound = true;
									break;
								}
							}

							if (BOPFound == false) {
								TCComponentItem plantModel = UIGetValuesUtility.findItem(session, bopTraversalID);
								OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(session, plantModel, rule);
								topBOPLine = (TCComponentBOMLine) createdBOPView[0].context;
								setBOPTOPLine(createdBOPView[0].context);
								bopTraverseLine = createdBOPView[0].context;
							}
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return true;
	}

	public ArrayList<TCComponentBOMLine> getOperationNoPart() {
		return operationNoPart;
	}

	public TCComponent[] getImpactedItems() {
		return this.impactedItems;
	}

	public TCComponent[] getProblemItems(String type) {
		if (type.isEmpty())
			return this.problemItems;
		List<TCComponent> output = new LinkedList<TCComponent>();
		if (this.solutionItems != null && this.solutionItems.length > 0) {
			for (TCComponent item : problemItems) {
				if (item.getType().compareTo(type) == 0)
					output.add(item);
			}
		}
		return output.toArray(new TCComponent[0]);
	}

	public TCComponent[] getSolutionItems(String type) {
		if (type.isEmpty())
			return this.solutionItems;
		List<TCComponent> output = new LinkedList<TCComponent>();
		if (this.solutionItems != null && this.solutionItems.length > 0) {
			for (TCComponent item : solutionItems) {
				if (item.getType().compareTo(type) == 0)
					output.add(item);
			}
		}
		return output.toArray(new TCComponent[0]);
	}

	public TCComponent getShopItem() {
		return shopItem;
	}

	public TCComponentItemRevision getTopLevelItemRevision() {
		return topLevelItemRevision;
	}

	public String getModel() {
		return model;
	}

	public String getYear() {
		return year;
	}

	public String getPlant() {
		return plant;
	}

	public String getShop() {
		return shop;
	}

	public String getMCN() {
		return MCN;
	}

	public String getShopName() {
		return shopName;
	}

	public String getMaterialCode() {
		return materialcode;
	}

	public void setMaterialCode(String matCode) {
		this.materialcode = matCode;
	}

	public StringBuilder readReport() {
		return strBom;
	}

	public StringBuilder readReport2() {
		return strBop;
	}

	public int getSerialNo() {
		return serialNumber++;
	}

	public TCComponent getMBOMTOPLine() {
		return mbomTOPLine;
	}

	public void setMBOMTOPLine(TCComponent mBOMTOPLine) {
		mbomTOPLine = mBOMTOPLine;
	}

	public TCComponent getMBOMTraverseLine() {
		return mbomTraverseLine;
	}

	public void setMBOMTraverseLine(TCComponent mBOMTraverseLine) {
		mbomTraverseLine = mBOMTraverseLine;
	}

	public TCComponent getBOPTOPLine() {
		return bopTOPLine;
	}

	public void setBOPTOPLine(TCComponent bOPTOPLine) {
		bopTOPLine = bOPTOPLine;
	}

	public TCComponent getBOPTraverseLine() {
		return bopTraverseLine;
	}

	public boolean isWindowOpen() {
		return isWindowOpen;
	}

	public OpenContextInfo[] getContextView() {
		return contextView;
	}

	public void setContextView(OpenContextInfo[] contextView) {
		this.contextView = contextView;
	}

	public void closeMBOMBOPWindows() {
		UIGetValuesUtility.closeAllContext(session, getContextView());
		TCComponentBOMLine BOPLine = (TCComponentBOMLine) getBOPTOPLine();
		try {
			if (BOPLine != null && !BOPLine.window().isWindowClosed())
				UIGetValuesUtility.closeContext(session, getBOPTOPLine());
		} catch (TCException e) {
			e.printStackTrace();
		}
		isWindowOpen = false;

		operationNoPart.clear();
	}

	public File popupReport(String fileName) {
		File attachFile = Logger.writeBufferResponse(readReport().toString(), UIGetValuesUtility.createLogFolder("MCN" + getMCN()), fileName);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					StringViewerDialog viewdialog = new StringViewerDialog(attachFile);
					viewdialog.setTitle("Transfer Status");
					viewdialog.setSize(600, 400);
					viewdialog.setLocationRelativeTo(null);
					viewdialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return attachFile;
	}
	
	public File generateReport(String fileName) {
		return Logger.writeBufferResponse(readReport().toString(), UIGetValuesUtility.createLogFolder("MCN" + getMCN()), fileName);
	}

	public File popupReport2(String fileName) {
		File attachFile = Logger.writeBufferResponse(readReport2().toString(), UIGetValuesUtility.createLogFolder("MCN" + getMCN()), fileName);
		return attachFile;
	}

	public void initReport() {
		this.strBom = new StringBuilder();
		this.printReport("APPEND", new String[] { "<html>", "<body>" });
		String[] printValues = new String[] { "Model :" + this.getModel() + "_" + this.getYear(), "User :" + this.session.getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
		this.printReport("DETAILS", printValues);
		printValues = new String[] { "S.No BOM", "Sub-group", "Part Number", "BOM ID", "Message", "Action", "Result" };
		this.printReport("HEADER", printValues);
	}

	public void initReport(String[] val) {
		this.strBom = new StringBuilder();
		this.printReport("APPEND", new String[] { "<html>", "<body>" });
		String[] printValues = new String[] { "Model :" + this.getModel() + "_" + this.getYear(), "User :" + this.session.getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
		this.printReport("DETAILS", printValues);
		printValues = val;
		this.printReport("HEADER", printValues);
	}

	public void initReport(String[] val, String subgroup, String swBOM) {
		this.strBom = new StringBuilder();
		this.printReport("APPEND", new String[] { "<html>", "<body>" });
		String[] printValues = new String[] { "SubGroup: " + subgroup + " & " + "SWBOM: " + swBOM, "User :" + this.session.getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
		this.printReport("DETAILS", printValues);
		printValues = val;
		this.printReport("HEADER", printValues);
	}

	public void initReport2() {
		this.strBop = new StringBuilder();
		this.printReport2("APPEND", new String[] { "<html>", "<body>" });
		String[] printValues = new String[] { "S.No BOP", "Sub-group", "BOM ID", "Work Station", "BOP ID", "Revision", "Message", "Action", "Result" };
		this.printReport2("HEADER", printValues);
	}

	public void finishReport(String sameDayReport) {
		this.printReport("APPEND", new String[] { "</table>" });
		if (!sameDayReport.equals("")) {
			this.printReport("APPEND", new String[] { "<br>" });
			this.printReport("APPEND", new String[] { sameDayReport });
		}

		this.printReport("APPEND", new String[] { "</body>", "</html>" });
	}

	public void finishReport2(String sameDayReport) {
		this.printReport2("APPEND", new String[] { "</table>" });
		if (!sameDayReport.equals("")) {
			this.printReport2("APPEND", new String[] { "<br>" });
			this.printReport2("APPEND", new String[] { sameDayReport });
		}

		this.printReport2("APPEND", new String[] { "</body>", "</html>" });
	}

	public Set<String> getSetItemInReleaseFolder() {
		return setItemInReleaseFolder;
	}

	public void setSetItemInReleaseFolder(Set<String> setItemInReleaseFolder) {
		this.setItemInReleaseFolder = setItemInReleaseFolder;
	}

	public TCComponentItemRevision getChangeObject() {
		return changeObject;
	}

	public void printReport(String argument, String[] values) {
		switch (argument) {
		case "APPEND":
			for (String info : values) {
				strBom.append(info);
			}
			break;
		case "DETAILS":
			strBom.append("<table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">");
			strBom.append("<tr>");
			for (String details : values) {
				strBom.append("<th><b>" + details + "</b></th>");
			}
			strBom.append("</tr></table>");
			break;
		case "HEADER":
			strBom.append("<table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border= \"1\" style=\"width:100%\"><tr style=\"background-color:#ccffff;\">");
			for (String header : values) {
				strBom.append("<th><b>" + header + "</b></th>");
			}
			strBom.append("</tr>");
			break;
		case "PRINT":
			if (values[values.length - 1].equalsIgnoreCase("Error") || values[values.length - 1].equalsIgnoreCase("NOT MATCH")) {
				strBom.append("<tr>");
				for (String header : values) {
					strBom.append("<td align=\"center\"><font color=\"red\">" + header + "</font></td>");
				}
				strBom.append("</tr>");
			} else {
				strBom.append("<tr>");
				for (String header : values) {
					strBom.append("<td align=\"center\">" + header + "</td>");
				}
				strBom.append("</tr>");
			}
			break;
		}
	}

	public void printReport2(String argument, String[] values) {
		switch (argument) {
		case "APPEND":
			for (String info : values) {
				strBop.append(info);
			}
			break;
		case "DETAILS":
			strBop.append("<table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">");
			strBop.append("<tr>");
			for (String details : values) {
				strBop.append("<th><b>" + details + "</b></th>");
			}
			strBop.append("</tr></table>");
			break;
		case "HEADER":
			strBop.append("<table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border= \"1\" style=\"width:100%\"><tr style=\"background-color:#ccffff;\">");
			for (String header : values) {
				strBop.append("<th><b>" + header + "</b></th>");
			}
			strBop.append("</tr>");
			break;
		case "PRINT":
			if (values[values.length - 1].equalsIgnoreCase("Error")) {
				strBop.append("<tr>");
				for (String header : values) {
					strBop.append("<td align=\"center\"><font color=\"red\">" + header + "</font></td>");
				}
				strBop.append("</tr>");
			} else {
				strBop.append("<tr>");
				for (String header : values) {
					strBop.append("<td align=\"center\">" + header + "</td>");
				}
				strBop.append("</tr>");
			}
			break;
		}
	}
}
