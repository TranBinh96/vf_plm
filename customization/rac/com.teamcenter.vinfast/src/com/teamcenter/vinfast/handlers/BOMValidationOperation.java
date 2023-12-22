package com.teamcenter.vinfast.handlers;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.loose.core.SessionService;
import com.teamcenter.vinfast.bom.update.BOMValidateSave_Dialog;
import com.teamcenter.vinfast.model.ErrorBeanModel;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class BOMValidationOperation {
	TCSession tcSession = null;
	Display display = null;
	private BOMValidateSave_Dialog dlg;
	private String tcUserName;
	private String tcSyslog;
	TCComponentDataset tcFile;

	private LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>> moduleValidate = null;
	private LinkedHashMap<String, LinkedList<String>> purchaseLevelValidate = null;
	private TableEditor[] bomEditors;
	private TableEditor[] bomEditors1;
	private TableEditor[] bomEditors2;
	private TableEditor[] bomEditors3;

	public BOMValidationOperation(TCSession session) {
		try {
			this.tcSession = session;
			this.display = AIFDesktop.getActiveDesktop().getShell().getDisplay();
			tcUserName = tcSession.getUserName();
			// System.out.println("Current User - " + tcUserName);

			tcSyslog = SessionService.getService(tcSession.getSoaConnection()).getTCSessionInfo().extraInfo
					.get("syslogFile");
			// System.out.println("Syslog Name - " + tcSyslog);
			String sysToken = tcSyslog.replace("tcserver.exe", "").replace(".syslog", "");

			createSessionFile(tcUserName, sysToken);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private void createSessionFile(String userName, String sysToken) {
		try {
			String datasetName = "BOMValidation_" + userName + "_" + sysToken;
			// System.out.println("DataSet Name - " + datasetName);

			tcFile = (TCComponentDataset) findExistingDataset(datasetName);
			if (tcFile == null) {
				TCComponentDatasetType tcDSType = (TCComponentDatasetType) tcSession.getTypeComponent("Text");
				tcFile = tcDSType.create(datasetName, "Test Dataset", "Text");
				tcFile.fireComponentSaveEvent();
				tcFile.refresh();
			} else {
				// System.out.println("Existing Dataset found...");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private TCComponentDataset findExistingDataset(String dsName) {
		try {
			TCComponentDataset tcFile = null;

			AIFComponentContext[] aifcomponentcontext = null;
			TCComponentQueryType tcQueryType = null;
			TCComponentQuery tcQuery = null;
			String propNames[] = null;
			String propValues[] = null;

			if (dsName != null) {
				propNames = new String[] { "Name", "Dataset Type" };
				propValues = new String[] { dsName, "Text" };

				tcQueryType = (TCComponentQueryType) tcSession.getTypeComponent("ImanQuery");
				tcQuery = (TCComponentQuery) tcQueryType.find("Dataset...");
				aifcomponentcontext = tcQuery.getExecuteResults(propNames, propValues);

				if (aifcomponentcontext.length > 0)
					tcFile = (TCComponentDataset) aifcomponentcontext[0].getComponent();
			}

			return tcFile;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void saveWindow(TCComponentBOMWindow tcWindow) {
		try {
			tcWindow.save();

//			try {
//				tcFile.delete();
//			} catch (TCException ex) {
//				ex.printStackTrace();
//			}
		} catch (TCException tcexception) {

//			try {
//				tcFile.delete();
//			} catch (TCException ex) {
//				ex.printStackTrace();
//			}

			String[] errorStrings = tcexception.errorStrings;
			List<String> otherErrorList = new ArrayList<String>();
			List<ErrorBeanModel> errorList = new ArrayList<ErrorBeanModel>();
			ErrorBeanModel headerBean = new ErrorBeanModel();
			boolean headerflag = true;
			for (String error : errorStrings) {
				if (error.contains("error_950003,")) {
					String token[] = error.split(",");
					if (headerflag) {
						headerBean = new ErrorBeanModel();
						headerBean.setParent(token[1].replaceAll("\"", "").trim());
						headerBean.setChild(token[2].replaceAll("\"", "").trim());
						headerBean.setFindno(token[3].replaceAll("\"", "").trim());
						headerBean.setErrorCodes(token[4].replaceAll("\"", "").trim());
						headerflag = false;
					} else {
						ErrorBeanModel eb = new ErrorBeanModel();
						eb.setParent(token[1].replaceAll("\"", "").trim());
						eb.setChild(token[2].replaceAll("\"", "").trim());
						eb.setFindno(token[3].replaceAll("\"", "").trim());
						eb.setErrorCodes(token[4].replaceAll("\"", "").trim());
						errorList.add(eb);
					}
				} else {
					otherErrorList.add(error);
				}
			}
			errorList.sort((o1, o2) -> o1.getFindno().compareTo(o2.getFindno()));
			// System.out.println("Size of Error List <ErrorBean> : " + errorList.size());

			StringBuilder htmlErrorMessage = new StringBuilder();
			htmlErrorMessage.append(
					"<html style=\"padding: 0px; border-width: 1px; border-color: black; border-style:solid\">");
			htmlErrorMessage.append(StringExtension.htmlTableCss);
			htmlErrorMessage.append("<body style=\"margin: 0px;\">");
			htmlErrorMessage.append("<table>");
			htmlErrorMessage.append("<tr style=\"background-color: #147391; color: white;\">");
			htmlErrorMessage.append("<td style=\"width:30%;\"><p>" + headerBean.getParent() + "</p></td>");
			htmlErrorMessage.append("<td style=\"width:30%;\"><p>" + headerBean.getChild() + "</p></td>");
			htmlErrorMessage.append("<td style=\"width:10%;\"><p>" + headerBean.getFindno() + "</p></td>");
			htmlErrorMessage.append("<td style=\"width:30%;\"><p>" + headerBean.getErrorCodes() + "</p></td>");
			htmlErrorMessage.append("</tr>");

			for (ErrorBeanModel eb : errorList) {
				htmlErrorMessage.append("<tr>");
				htmlErrorMessage.append("<td><p>" + eb.getParent() + "</p></td>");
				htmlErrorMessage.append("<td><p>" + eb.getChild() + "</p></td>");
				htmlErrorMessage.append("<td><p>" + eb.getFindno() + "</p></td>");
				htmlErrorMessage.append("<td><p style=\"color: red;\">" + eb.getErrorCodes().replace(" ; ", "<br>") + "</p></td>");
				htmlErrorMessage.append("</tr>");
			}
			htmlErrorMessage.append(" </table> ");

			if (otherErrorList.size() > 0) {
				if (otherErrorList.size() == 1) {
					htmlErrorMessage.append("<br><br>");
					htmlErrorMessage.append("<h2>Additional Error Message requiring your attention:</h2>");
					htmlErrorMessage.append("&nbsp;&nbsp; - &nbsp;" + otherErrorList.get(0));
				} else {
					htmlErrorMessage.append("<br><br>");
					htmlErrorMessage.append("<h2>Additional Error Messages requiring your attention:</h2>");
					for (String errorMesg : otherErrorList) {
						htmlErrorMessage.append("&nbsp;&nbsp; - &nbsp;" + errorMesg + "<br>");
					}
				}
			}

			htmlErrorMessage.append(" </body> </html>");

			Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
			double width = screensize.getWidth();
			double height = screensize.getHeight();
			Point point = display.getDPI();
			float resizeFactor = (float) ((point.x * 1.042) / 100);

//			updateValidate();
			
			dlg = new BOMValidateSave_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("BOM Validate");
			dlg.btnOK.setVisible(false);
			dlg.setMessage("", IMessageProvider.INFORMATION);
			dlg.browserReport.setText(htmlErrorMessage.toString());
			dlg.getShell().setSize((int) (Math.min(width, 1024) * resizeFactor), (int) (Math.min(height, 650) * resizeFactor));
//			updateUI(errorList);
			dlg.open();
		} catch (Exception ex) {
			System.out.println("Exception during BOMSave Window - " + ex.getMessage());
			MessageDialog.openError(display.getActiveShell(), "BOM Validation Error", ex.getMessage());
		}
	}

	//
	private void updateUI(List<ErrorBeanModel> errorList) {
		bomEditors = new TableEditor[errorList.size()];
		bomEditors1 = new TableEditor[errorList.size()];
		bomEditors2 = new TableEditor[errorList.size()];
		bomEditors3 = new TableEditor[errorList.size()];
		for (int i = 0; i < errorList.size(); i++) {
			updateTableItem(i, errorList.get(i).getChild(), "Make", "", "", "", "");
		}
	}

	private void updateTableItem(int i, String bomline, String partMakeBuy, String purchaseLevel, String moduleL1,
			String moduleL2, String moduleL3) {
		final TableItem item = new TableItem(dlg.tblUpdate, SWT.NONE);
		item.setText(new String[] { bomline, partMakeBuy });
		// Create the editor and purchase level combobox
		bomEditors[i] = new TableEditor(dlg.tblUpdate);
		Combo cbBOMPurchaseLevel = new Combo(dlg.tblUpdate, SWT.READ_ONLY);
		cbBOMPurchaseLevel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbBOMPurchaseLevel.setText("Color...");
		cbBOMPurchaseLevel.computeSize(SWT.DEFAULT, dlg.tblUpdate.getItemHeight());
		bomEditors[i].grabHorizontal = true;
		bomEditors[i].minimumHeight = cbBOMPurchaseLevel.getSize().y;
		bomEditors[i].minimumWidth = cbBOMPurchaseLevel.getSize().x;
		LinkedList<String> purchaseLevelList = purchaseLevelValidate.get(partMakeBuy);
		cbBOMPurchaseLevel.setItems(purchaseLevelList.toArray(new String[0]));
		bomEditors[i].setEditor(cbBOMPurchaseLevel, item, 1);

		// Create the editor and module 1 combobox
		bomEditors1[i] = new TableEditor(dlg.tblUpdate);
		Combo cbBOMModuleGroupEnglish = new Combo(dlg.tblUpdate, SWT.READ_ONLY);
		cbBOMModuleGroupEnglish.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbBOMModuleGroupEnglish.setText("Color...");
		cbBOMModuleGroupEnglish.computeSize(SWT.DEFAULT, dlg.tblUpdate.getItemHeight());
		bomEditors1[i].grabHorizontal = true;
		bomEditors1[i].minimumHeight = cbBOMModuleGroupEnglish.getSize().y;
		bomEditors1[i].minimumWidth = cbBOMModuleGroupEnglish.getSize().x;
		cbBOMModuleGroupEnglish.setItems(moduleValidate.keySet().toArray(new String[0]));
		bomEditors1[i].setEditor(cbBOMModuleGroupEnglish, item, 2);

		// Create the editor and module 2 combobox
		bomEditors2[i] = new TableEditor(dlg.tblUpdate);
		Combo cbBOMMainModuleEnglish = new Combo(dlg.tblUpdate, SWT.READ_ONLY);
		cbBOMMainModuleEnglish.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbBOMMainModuleEnglish.setText("Color...");
		cbBOMMainModuleEnglish.computeSize(SWT.DEFAULT, dlg.tblUpdate.getItemHeight());
		bomEditors2[i].grabHorizontal = true;
		bomEditors2[i].minimumHeight = cbBOMMainModuleEnglish.getSize().y;
		bomEditors2[i].minimumWidth = cbBOMMainModuleEnglish.getSize().x;
		bomEditors2[i].setEditor(cbBOMMainModuleEnglish, item, 3);

		// Create the editor and module 3 combobox
		bomEditors3[i] = new TableEditor(dlg.tblUpdate);
		Combo cbBOMModuleName = new Combo(dlg.tblUpdate, SWT.READ_ONLY);
		cbBOMModuleName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		cbBOMModuleName.setText("Color...");
		cbBOMModuleName.computeSize(SWT.DEFAULT, dlg.tblUpdate.getItemHeight());
		bomEditors3[i].grabHorizontal = true;
		bomEditors3[i].minimumHeight = cbBOMModuleName.getSize().y;
		bomEditors3[i].minimumWidth = cbBOMModuleName.getSize().x;
		bomEditors3[i].setEditor(cbBOMModuleName, item, 4);

		cbBOMModuleGroupEnglish.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				Combo cbModuleL1 = (Combo) bomEditors1[i].getEditor();
				Combo cbModuleL2 = (Combo) bomEditors2[i].getEditor();
				Combo cbModuleL3 = (Combo) bomEditors3[i].getEditor();

				updateModuleLevel2(cbModuleL1, cbModuleL2, cbModuleL3);
			}
		});

		cbBOMMainModuleEnglish.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				Combo cbModuleL1 = (Combo) bomEditors1[i].getEditor();
				Combo cbModuleL2 = (Combo) bomEditors2[i].getEditor();
				Combo cbModuleL3 = (Combo) bomEditors3[i].getEditor();

				updateModuleLevel3(cbModuleL1, cbModuleL2, cbModuleL3);
			}
		});
	}

	private void updateModuleLevel2(Combo cbModuleL1, Combo cbModuleL2, Combo cbModuleL3) {
		String moduleLevel1 = cbModuleL1.getText();
		LinkedHashMap<String, LinkedList<String>> module2 = moduleValidate.get(moduleLevel1);
		if (module2 != null) {
			cbModuleL2.setItems(module2.keySet().toArray(new String[0]));
			cbModuleL3.deselectAll();
		}
	}

	private void updateModuleLevel3(Combo cbModuleL1, Combo cbModuleL2, Combo cbModuleL3) {
		String moduleLevel1 = cbModuleL1.getText();
		LinkedHashMap<String, LinkedList<String>> module2 = moduleValidate.get(moduleLevel1);
		if (module2 != null) {
			String moduleLevel2 = cbModuleL2.getText();
			LinkedList<String> module3 = module2.get(moduleLevel2);
			if (module3 != null) {
				cbModuleL3.setItems(module3.toArray(new String[0]));
			}
		}
	}

	private void updateValidate() {
		String[] values = TCExtension.GetPreferenceValues("VF_PART_CREATE_MODULE_VALIDATE", tcSession);
		if (values != null && values.length > 0) {
			moduleValidate = new LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>>();
			for (String value : values) {
				String[] str = value.split(";");
				if (str.length > 2) {
					String module1 = str[0];
					String module2 = str[1];
					String module3 = str[2];

					if (moduleValidate.containsKey(module1)) {
						LinkedHashMap<String, LinkedList<String>> oldModule1 = moduleValidate.get(module1);
						if (oldModule1.containsKey(module2)) {
							LinkedList<String> oldModule2 = oldModule1.get(module2);
							oldModule2.add(module3);
						} else {
							LinkedList<String> newModule3 = new LinkedList<String>();
							newModule3.add(module3);
							oldModule1.put(module2, newModule3);
						}
					} else {
						LinkedList<String> newModule3 = new LinkedList<String>();
						newModule3.add(module3);
						LinkedHashMap<String, LinkedList<String>> newModule2 = new LinkedHashMap<String, LinkedList<String>>();
						newModule2.put(module2, newModule3);
						moduleValidate.put(module1, newModule2);
					}
				}
			}
		}

		String[] valuePurchases = TCExtension.GetPreferenceValues("VF_PART_CREATE_PURCHASELEVEL_VALIDATE", tcSession);
		if (valuePurchases != null && valuePurchases.length > 0) {
			if (valuePurchases.length > 0) {
				purchaseLevelValidate = new LinkedHashMap<String, LinkedList<String>>();
				for (String purchase : valuePurchases) {
					if (purchase.contains(";")) {
						String[] strArray = purchase.split(";");
						String partMakeBuy = strArray[0];
						LinkedList<String> purchaseList = new LinkedList<String>();
						if (strArray[1].contains(",")) {
							String[] strArray2 = strArray[1].split(",");
							for (String value : strArray2) {
								purchaseList.add(value);
							}
						} else {
							purchaseList.add(strArray[1]);
						}
						purchaseLevelValidate.put(partMakeBuy, purchaseList);
					}
				}
			}
		}
	}
}
