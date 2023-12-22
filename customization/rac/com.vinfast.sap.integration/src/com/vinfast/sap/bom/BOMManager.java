package com.vinfast.sap.bom;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.ExecutionEvent;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2009_10.DataManagement.CreateInput;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.ContextGroup;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInput;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextsResponse;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.configurator.MaterialPlatformCode;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class BOMManager {

	private int serialNumber = 1;
	private int total = 0;
	private int impactedItemsCount;
	private int problemItemsCount;
	private int solutionItemsCount;
	private boolean isWindowOpen = false;
	private boolean hasError = false;

	private String shopName = "";
	private String model = "";
	private String year = "";
	private String plant = "";
	private String MCN = "";
	private String materialcode = null;
	private StringBuilder strBom = new StringBuilder();
	private StringBuilder strBop = new StringBuilder();
	private String errorMessage = "";

	private TCComponent shopItem = null;
	private TCSession session = null;
	private TCComponentItemRevision topLevelItemRevision = null;
	private TCComponent MBOMTOPLine = null;
	private TCComponent MBOMTraverseLine = null;
	private TCComponent BOPTOPLine = null;
	private TCComponent BOPTraverseLine = null;

	private OpenContextInfo[] contextView;
	private TCComponent[] impactedItems;
	private TCComponent[] problemItems;
	private TCComponent[] solutionItems;
	private Set<String> setItemInReleaseFolder = new HashSet<String>();
	private String company = "";

	private ArrayList<String> operationIDs = new ArrayList<String>();

	public BOMManager() {
		
	}
	
	public BOMManager(String company) {
		this.company = company;
	}
	
	public Set<String> getSetItemInReleaseFolder() {
		return setItemInReleaseFolder;
	}

	private void setSetItemInReleaseFolder(Set<String> setItemInReleaseFolder) {
		this.setItemInReleaseFolder = setItemInReleaseFolder;
	}

	TCComponentItemRevision changeObject;
	DataManagementService dataManagementService;
	ArrayList<TCComponentBOMLine> operationNoPart = new ArrayList<TCComponentBOMLine>();

	public BOMManager loadChangeAttachments(TCSession session, TCComponentItemRevision changeObject) {
		BOMManager BOMUtilities = null;
		try {
			DataManagementService dmCoreService = DataManagementService.getService(session);
			TCComponent[] impactedShop = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] {}, PropertyDefines.REL_IMPACT_SHOP);
			if (impactedShop != null && impactedShop.length == 1) {
				TCComponentItemRevision topLevelItemRevision = null;
				if (company.compareTo(PropertyDefines.VIN_FAST_ELECTRIC) == 0) {
					topLevelItemRevision = (TCComponentItemRevision) impactedShop[0];
				} else {
					if (impactedShop[0] instanceof TCComponentItemRevision)
						topLevelItemRevision = UIGetValuesUtility.getTopLevelItemRevision(session, (TCComponentItemRevision) impactedShop[0], PropertyDefines.REVISION_RULE_WORKING);
				}
				
				MaterialPlatformCode plaformCode = UIGetValuesUtility.getPlatformCode(dmCoreService, topLevelItemRevision);
				if (plaformCode.getPlatformCode().length() == 0) {
					String[] name = topLevelItemRevision.getProperty(PropertyDefines.ITEM_NAME).split("_");
					BOMUtilities = new BOMManager();
					BOMUtilities.setModel(name[0]);
					BOMUtilities.setYear("");
					BOMUtilities.setShopName("");
					BOMUtilities.setMaterialCode(plaformCode.getMaterialCode());
				} else {
					BOMUtilities = new BOMManager();
					BOMUtilities.setModel(plaformCode.getPlatformCode());
					BOMUtilities.setYear(plaformCode.getModelYear());
					BOMUtilities.setShopName(impactedShop[0].getProperty(PropertyDefines.ITEM_NAME).replace(" ", "_"));
					BOMUtilities.setMaterialCode(plaformCode.getMaterialCode());
				}

				BOMUtilities.setChangeObject(changeObject);
				BOMUtilities.setSession(session);
				BOMUtilities.setDataManagementService(dmCoreService);
				BOMUtilities.setPlant(changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT));
				String MCN_SAPID = changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				BOMUtilities.setMCN(MCN_SAPID.substring(MCN_SAPID.length() - 8));
				BOMUtilities.setShopItem(impactedShop[0]);
				BOMUtilities.setTopLevelItemRevision(topLevelItemRevision);

				TCComponent[] impacted = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] {}, PropertyDefines.REL_IMPACT_ITEMS);
				BOMUtilities.setImpactedItems(impacted);
				if (impacted == null) {
					BOMUtilities.setImpactedItemsCount(0);
				} else {
					BOMUtilities.setImpactedItemsCount(impacted.length);
				}

				TCComponent[] problem = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_PRB_ITEMS);
				BOMUtilities.setProblemItems(problem);
				if (problem == null) {
					BOMUtilities.setProblemItemsCount(0);
				} else {
					BOMUtilities.setProblemItemsCount(problem.length);
				}

				TCComponent[] solution = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] { PropertyDefines.TYPE_OPERATION_REVISION }, PropertyDefines.REL_SOL_ITEMS);
				BOMUtilities.setSolutionItems(solution);
				if (solution == null) {
					BOMUtilities.setSolutionItemsCount(0);
				} else {
					for(TCComponent operation : solution) {
						operationIDs.add(operation.getProperty(PropertyDefines.ITEM_ID));
					}
					BOMUtilities.setSolutionItemsCount(solution.length);
				}

				TCComponent[] itemForRelease = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] {}, PropertyDefines.REL_SOL_ITEMS);
				Set<String> setItemInReleaseFolder = new HashSet<String>();
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
				BOMUtilities.setSetItemInReleaseFolder(setItemInReleaseFolder);
				BOMUtilities.setTotal(BOMUtilities.getImpactedItemsCount() + BOMUtilities.getSolutionItemsCount() + BOMUtilities.getProblemItemsCount());
			}
		} catch (TCException e1) {
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

		return BOMUtilities;
	}

	public boolean hasError() {
		return hasError;
	}

	public void setError(boolean hasError) {
		this.hasError = hasError;
	}

	public TCComponentItemRevision getChangeObject() {
		return changeObject;
	}

	public void setChangeObject(TCComponentItemRevision changeObject) {
		this.changeObject = changeObject;
	}

	public int getImpactedItemsCount() {
		return impactedItemsCount;
	}

	public void setImpactedItemsCount(int impactedItemsCount) {
		this.impactedItemsCount = impactedItemsCount;
	}

	public int getProblemItemsCount() {
		return problemItemsCount;
	}

	public void setProblemItemsCount(int problemItemsCount) {
		this.problemItemsCount = problemItemsCount;
	}

	public int getSolutionItemsCount() {
		return solutionItemsCount;
	}

	public void setSolutionItemsCount(int solutionItemsCount) {
		this.solutionItemsCount = solutionItemsCount;
	}

	public boolean createMBOMBOPWindow(String rule) {
		TCSession sessionID = getSession();
		setError(false);
		setErrorMessage("");
		try {
			com.teamcenter.services.rac.manufacturing._2011_06.DataManagement dmService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);
			TCComponentRevisionRule RevisionRule = UIGetValuesUtility.getRevisionRule(rule, session);
			CreateInput input = new CreateInput();
			input.tagProps.put("RevisionRule", RevisionRule);

			OpenContextInput contextInput = new OpenContextInput();
			contextInput.object = getTopLevelItemRevision();
			contextInput.openAssociatedContexts = true;
			contextInput.openViews = true;
			contextInput.contextSettings = input;

			OpenContextsResponse response = dmService.openContexts(new OpenContextInput[] { contextInput });
			ServiceData sd = response.serviceData;
			if (sd.sizeOfPartialErrors() > 0) {
				setErrorMessage(SoaUtil.buildErrorMessage(sd));
				return false;
			}else {
				ContextGroup[] groups = response.output;
				setContextView(groups[0].contexts);
				setWindowOpen(true);
				for (OpenContextInfo views : getContextView()) {
					if (views.context.getType().equals(PropertyDefines.TYPE_BOMLINE)) {

						String shopID = getShopItem().getProperty(PropertyDefines.ITEM_ID);
						TCComponentBOMLine topBOMLine = (TCComponentBOMLine) views.context;
						setMBOMTOPLine(views.context);
						setMBOMTraverseLine(views.context);

						AIFComponentContext[] childerns = topBOMLine.getChildren();
						for (AIFComponentContext child : childerns) {
							TCComponent childLine = (TCComponent) child.getComponent();
							if (childLine.getProperty(PropertyDefines.BOM_ITEM_ID).equals(shopID)) {
								// The impacted shop line
								setMBOMTraverseLine(childLine);
								break;
							}
						}
					}
					if (views.context.getType().equals(PropertyDefines.TYPE_BOPLINE)) {
						// handle case top topLevelItemRevision is BOP
						operationNoPart = new ArrayList<TCComponentBOMLine>();
						TCComponentBOMLine topBOPLine = (TCComponentBOMLine) views.context;
						TCComponentItem topLevelItem = topLevelItemRevision.getItem();
						TCComponentForm topLevelItemMasterForm = (TCComponentForm) topLevelItem.getRelatedComponent("IMAN_master_form");
						String bopTOPID = topLevelItemMasterForm.getProperty("user_data_2");
						if (bopTOPID.isEmpty()) {
							setBOPTOPLine(views.context);
						} else {
							if (topBOPLine.getProperty(PropertyDefines.BOM_ITEM_ID).equals(bopTOPID)) {
								setBOPTOPLine(views.context);
							} else {
								TCComponentItem plantModel = UIGetValuesUtility.findItem(sessionID, bopTOPID);
								OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(sessionID, plantModel, rule);
								topBOPLine = (TCComponentBOMLine) createdBOPView[0].context;
								setBOPTOPLine(createdBOPView[0].context);
							}
						}

						setBOPTraverseLine(topBOPLine);

						TCComponentItemRevision shopItemRevision = (TCComponentItemRevision) getShopItem();
						TCComponentItem shopItem = shopItemRevision.getItem();
						TCComponentForm shopItemMasterForm = (TCComponentForm) shopItem.getRelatedComponent("IMAN_master_form");
						String bopTraversalID = shopItemMasterForm.getProperty("user_data_2");

						if (!bopTraversalID.isEmpty()) {
							boolean BOPFound = false;
							AIFComponentContext[] childerns = topBOPLine.getChildren();
							for (AIFComponentContext child : childerns) {
								TCComponent childLine = (TCComponent) child.getComponent();
								if (childLine.getProperty(PropertyDefines.BOM_ITEM_ID).equals(bopTraversalID)) {
									setBOPTraverseLine(childLine);
									BOPFound = true;
									break;
								}
							}

							if (BOPFound == false) {
								TCComponentItem plantModel = UIGetValuesUtility.findItem(sessionID, bopTraversalID);
								OpenContextInfo[] createdBOPView = UIGetValuesUtility.createContextViews(sessionID, plantModel, rule);
								topBOPLine = (TCComponentBOMLine) createdBOPView[0].context;
								setBOPTOPLine(createdBOPView[0].context);
								setBOPTraverseLine(createdBOPView[0].context);
							}
						}
					}
				}
				if (getMBOMTOPLine() == null || getBOPTOPLine() == null) {
					setErrorMessage("MBOM and BOP linking error. NO BOP is linked to MBOM. Please link MBOM and BOP");
					setError(true);
					return false;
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

	public void setOperationNoPart(TCComponentBOMLine operationNoPart) {
		this.operationNoPart.add(operationNoPart);
	}

	public void setImpactedItems(TCComponent[] impactedItems) {
		this.impactedItems = impactedItems;
	}

	public void setProblemItems(TCComponent[] problemItems) {
		this.problemItems = problemItems;
	}

	public void setSolutionItems(TCComponent[] solutionItems) {
		this.solutionItems = solutionItems;
	}

	public TCComponent[] getImpactedItems() {
		return this.impactedItems;
	}

	public TCComponent[] getProblemItems() {
		return this.problemItems;
	}

	public TCComponent[] getSolutionItems() {
		return this.solutionItems;
	}

	public TCComponent getShopItem() {
		return shopItem;
	}

	public void setShopItem(TCComponent shopItem) {
		this.shopItem = shopItem;
	}

	public TCComponentItemRevision getTopLevelItemRevision() {
		return topLevelItemRevision;
	}

	public void setTopLevelItemRevision(TCComponentItemRevision topLevelItemRevision) {
		this.topLevelItemRevision = topLevelItemRevision;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getPlant() {
		return plant;
	}

	public void setPlant(String plant) {
		this.plant = plant;
	}

	public String getMCN() {
		return MCN;
	}

	public void setMCN(String mCN) {
		MCN = mCN;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public DataManagementService getDataManagementService() {
		return dataManagementService;
	}

	public void setDataManagementService(DataManagementService dmCoreService) {
		this.dataManagementService = dmCoreService;
	}

	public TCSession getSession() {
		return session;
	}

	public void setSession(TCSession session) {
		this.session = session;
	}

	public String getMaterialCode() {
		return materialcode;
	}

	public void setMaterialCode(String matCode) {
		this.materialcode = matCode;
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
		case "WARN":
			if (values[values.length - 1].equalsIgnoreCase("Error") || values[values.length - 1].equalsIgnoreCase("NOT MATCH")) {
				strBom.append("<tr>");
				for (String header : values) {
					strBom.append("<td align=\"center\"><font color=\"Maroon\">" + header + "</font></td>");
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
		case "WARN":
			if (values[values.length - 1].equalsIgnoreCase("Error") || values[values.length - 1].equalsIgnoreCase("NOT MATCH")) {
				strBop.append("<tr>");
				for (String header : values) {
					strBop.append("<td align=\"center\"><font color=\"Maroon\">" + header + "</font></td>");
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

	public StringBuilder readReport() {
		return strBom;
	}

	public StringBuilder readReport2() {
		return strBop;
	}

	public int getSerialNo() {
		return serialNumber;
	}

	public void incrementSerialNo() {
		this.serialNumber = this.serialNumber+1;
	}

	public TCComponent getMBOMTOPLine() {
		return MBOMTOPLine;
	}

	private void setMBOMTOPLine(TCComponent mBOMTOPLine) {
		MBOMTOPLine = mBOMTOPLine;
	}

	public TCComponent getMBOMTraverseLine() {
		return MBOMTraverseLine;
	}

	private void setMBOMTraverseLine(TCComponent mBOMTraverseLine) {
		MBOMTraverseLine = mBOMTraverseLine;
	}

	public TCComponent getBOPTOPLine() {
		return BOPTOPLine;
	}

	private void setBOPTOPLine(TCComponent bOPTOPLine) {
		BOPTOPLine = bOPTOPLine;
	}

	public TCComponent getBOPTraverseLine() {
		return BOPTraverseLine;
	}

	private void setBOPTraverseLine(TCComponent bOPTraverseLine) {
		BOPTraverseLine = bOPTraverseLine;
	}

	public boolean isWindowOpen() {
		return isWindowOpen;
	}

	private void setWindowOpen(boolean isWindowOpen) {
		this.isWindowOpen = isWindowOpen;
	}

	public OpenContextInfo[] getContextView() {
		return contextView;
	}

	private void setContextView(OpenContextInfo[] contextView) {
		this.contextView = contextView;
	}

	public int getTotal() {
		return total;
	}

	private void setTotal(int total) {
		this.total = total;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	private void setErrorMessage(String message) {
		this.errorMessage = message;
	}

	public void closeMBOMBOPWindows() {
		UIGetValuesUtility.closeAllContext(getSession(), getContextView());
		TCComponentBOMLine BOPLine = (TCComponentBOMLine) getBOPTOPLine();
		try {
			if (BOPLine != null && !BOPLine.window().isWindowClosed())
				UIGetValuesUtility.closeContext(getSession(), getBOPTOPLine());
		} catch (TCException e) {
			e.printStackTrace();
		}
		setWindowOpen(false);

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

	public File popupReport2(String fileName) {
		File attachFile = Logger.writeBufferResponse(readReport2().toString(), UIGetValuesUtility.createLogFolder("MCN" + getMCN()), fileName);
		return attachFile;
	}

	public void initReport() {
		String[] printValues;
		this.strBom = new StringBuilder();
		this.printReport("APPEND", new String[] { "<html>", "<body>" });
		if(this.getModel().isEmpty() || this.model.equals("NA")) {
			printValues = new String[] { "Shop :" + this.getShopName(), "MCN :" + getMCN(), "User :" + this.getSession().getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };

		}else {
			printValues = new String[] { "Model :" + this.getModel() + "_" + this.getYear(), "MCN :" + getMCN(), "User :" + this.getSession().getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };

		}
		this.printReport("DETAILS", printValues);
		printValues = new String[] { "S.NO", "SUB GROUP", "RECORD", "BOMLINE ID", "MESSAGE", "ACTION", "RESULT" };
		this.printReport("HEADER", printValues);
	}

	public void initReport(String[] val) {
		String[] printValues;
		this.strBom = new StringBuilder();
		this.printReport("APPEND", new String[] { "<html>", "<body>" });
		if(this.getModel().isEmpty() || this.model.equals("NA")) {
			printValues = new String[] { "Shop :" + this.getShopName(),"MCN :" + getMCN(), "User :" + this.getSession().getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };

		}else {
			printValues = new String[] { "Model :" + this.getModel() + "_" + this.getYear(),"MCN :" + getMCN(), "User :" + this.getSession().getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };

		}this.printReport("DETAILS", printValues);
		printValues = val;
		this.printReport("HEADER", printValues);
	}

	public void initReport(String[] val, String subgroup, String swBOM) {
		this.strBom = new StringBuilder();
		this.printReport("APPEND", new String[] { "<html>", "<body>" });
		String[] printValues = new String[] { "SubGroup: " + subgroup + " & " + "SWBOM: " + swBOM,"MCN :" + getMCN(), "User :" + this.getSession().getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
		this.printReport("DETAILS", printValues);
		printValues = val;
		this.printReport("HEADER", printValues);
	}

	public void initReport2() {
		this.strBop = new StringBuilder();
		this.printReport2("APPEND", new String[] { "<html>", "<body>" });
		String[] printValues = new String[] { "S.NO", "SUB GROUP", "BOM ID", "WORKSTATION", "BOP ID", "REVISION", "MESSAGE", "ACTION", "RESULT" };
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

	public void resetSerialNo() {
		this.serialNumber = 1;
	}

	public boolean isOperationInMCN(String operationID) {

		if(this.operationIDs.contains(operationID)) {
			return true;
		}else {
			return false;
		}
	}

	public static String getCompanyFromEvent(ExecutionEvent event) {
		String company = PropertyDefines.VIN_FAST;
		String command = event.getCommand().toString();
		if (command.contains(PropertyDefines.VIN_FAST_ELECTRIC)) {
			company = PropertyDefines.VIN_FAST_ELECTRIC;
		} else if (command.contains(PropertyDefines.VIN_ES)) {
			company = PropertyDefines.VIN_ES;
		}
		
		return company;
	}
}
