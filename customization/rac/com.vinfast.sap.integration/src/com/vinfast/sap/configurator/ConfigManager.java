package com.vinfast.sap.configurator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.SwingUtilities;

import org.eclipse.core.commands.ExecutionEvent;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfg0ProductItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class ConfigManager {

	private int serialNumber = 1;
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
	private TCComponentCfg0ProductItem topLevelItemRevision = null;

	private OpenContextInfo[] contextView;
	private TCComponent[] impactedItems;
	private TCComponent[] problemItems;
	private TCComponent[] solutionItems;
	private String company = "";

	public ConfigManager() {
		
	}
	
	public ConfigManager(String company) {
		this.company = company;
	}
	
	TCComponentItemRevision changeObject;
	DataManagementService dataManagementService;
	ArrayList<TCComponentBOMLine> operationNoPart = new ArrayList<TCComponentBOMLine>();

	public ConfigManager loadChangeAttachments(TCSession session, TCComponentItemRevision changeObject) {
		ConfigManager ConfigUtilities = null;
		try {
			DataManagementService dmCoreService = DataManagementService.getService(session);
			TCComponent[] impactedShop = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] {}, PropertyDefines.REL_IMPACT_SHOP);
			if (impactedShop != null && impactedShop.length == 1) {
				TCComponentCfg0ProductItem topLevelItemRevision = null;
				if (company.compareTo(PropertyDefines.VIN_FAST_ELECTRIC) == 0) {
					topLevelItemRevision = (TCComponentCfg0ProductItem) impactedShop[0];
				} else {
					if (impactedShop[0] instanceof TCComponentCfg0ProductItem)
						topLevelItemRevision = (TCComponentCfg0ProductItem) impactedShop[0];
				}
				
				MaterialPlatformCode plaformCode = UIGetValuesUtility.getPlatformCode(dmCoreService, topLevelItemRevision);
				if (plaformCode.getPlatformCode().length() == 0) {
					String[] name = topLevelItemRevision.getProperty(PropertyDefines.ITEM_NAME).split("_");
					ConfigUtilities = new ConfigManager();
					ConfigUtilities.setModel(name[0]);
					ConfigUtilities.setYear("");
					ConfigUtilities.setShopName("");
					ConfigUtilities.setMaterialCode(plaformCode.getMaterialCode());
				} else {
					ConfigUtilities = new ConfigManager();
					ConfigUtilities.setModel(plaformCode.getPlatformCode());
					ConfigUtilities.setYear(plaformCode.getModelYear());
					ConfigUtilities.setShopName(impactedShop[0].getProperty(PropertyDefines.ITEM_NAME).replace(" ", "_"));
					ConfigUtilities.setMaterialCode(plaformCode.getMaterialCode());
				}

				ConfigUtilities.setChangeObject(changeObject);
				ConfigUtilities.setSession(session);
				ConfigUtilities.setDataManagementService(dmCoreService);
				ConfigUtilities.setPlant(changeObject.getPropertyDisplayableValue(PropertyDefines.ECM_PLANT));
				String MCN_SAPID = changeObject.getPropertyDisplayableValue(PropertyDefines.ITEM_ID);
				ConfigUtilities.setMCN(MCN_SAPID.substring(MCN_SAPID.length() - 8));
				ConfigUtilities.setShopItem(impactedShop[0]);
				ConfigUtilities.setTopLevelItemRevision(topLevelItemRevision);

				TCComponent[] problem = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] {}, PropertyDefines.REL_PRB_ITEMS);
				ConfigUtilities.setProblemItems(problem);

				TCComponent[] solution = UIGetValuesUtility.getRelatedComponents(dmCoreService, changeObject, new String[] {}, PropertyDefines.REL_SOL_ITEMS);
				ConfigUtilities.setSolutionItems(solution);

			}
		} catch (TCException e1) {
			e1.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}

		return ConfigUtilities;
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

	public TCComponentCfg0ProductItem getTopLevelItemRevision() {
		return topLevelItemRevision;
	}

	public void setTopLevelItemRevision(TCComponentCfg0ProductItem topLevelItemRevision) {
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

	public OpenContextInfo[] getContextView() {
		return contextView;
	}

	public String getErrorMessage() {
		return errorMessage;
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
			printValues = new String[] { "Shop :" + this.getShopName(), "User :" + this.getSession().getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };

		}else {
			printValues = new String[] { "Model :" + this.getModel() + "_" + this.getYear(), "User :" + this.getSession().getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };

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
			printValues = new String[] { "Shop :" + this.getShopName(), "User :" + this.getSession().getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };

		}else {
			printValues = new String[] { "Model :" + this.getModel() + "_" + this.getYear(), "User :" + this.getSession().getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };

		}this.printReport("DETAILS", printValues);
		printValues = val;
		this.printReport("HEADER", printValues);
	}

	public void initReport(String[] val, String subgroup, String swBOM) {
		this.strBom = new StringBuilder();
		this.printReport("APPEND", new String[] { "<html>", "<body>" });
		String[] printValues = new String[] { "SubGroup: " + subgroup + " & " + "SWBOM: " + swBOM, "User :" + this.getSession().getUserName(), "Time :" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime()) };
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
