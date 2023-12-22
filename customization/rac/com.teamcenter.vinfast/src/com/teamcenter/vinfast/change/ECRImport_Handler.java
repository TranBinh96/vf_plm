package com.teamcenter.vinfast.change;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ClientMetaModel;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.teamcenter.soa.client.model.Type;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.utils.Query;
import com.vf.utils.TCExtension;

public class ECRImport_Handler extends AbstractHandler {
	private static final String VF_SHOW_ECR_TEMPLATE = "VF_SHOW_ECR_TEMPLATE";
	private TCSession session;
	private ECRImport_Dialog dlg;
	private TCComponent selectedObject;
	private ECRInputManagement ecrInputManagement;
	private String ecrSummaryTemplate = "";
	private LinkedHashMap<String, String> attributeMap;
	private String[] attributeMandatory;
	private String formIcon = "<img src=\"data:image/png;base64,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\" style=\"position: absolute;margin: auto;top: 0;left: 0;right: 0;bottom: 0;\" width=\"100\" height=\"100\"/>";

	public ECRImport_Handler() {
		attributeMap = null;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		selectedObject = (TCComponent) targetComp[0];
		try {
			ecrInputManagement = new ECRInputManagement(((TCComponentItemRevision) selectedObject).getItem(), (TCComponentItemRevision) selectedObject);
			getAttributeMap();

			try {
				attributeMandatory = TCExtension.GetPreferenceValues("VF_UPDATE_ECR_ATTRIBUTE_MANDATORY", session);
			} catch (Exception e) {
				e.printStackTrace();
			}

			dlg = new ECRImport_Dialog(new Shell());
			dlg.create();

			dlg.browser.setText("<html style=\"padding: 0px;border-width: 1px;border-color: black;border-style: solid;\"><body style=\"margin: 0px; text-align: center;\"><div style=\"height: 100%\">" + formIcon + "</div></body></html>");

			dlg.txtECR.setText(selectedObject.toString());
			dlg.btnUpload.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						FileDialog fileDialog = new FileDialog(new Shell(), SWT.OPEN);
						fileDialog.setText("Open");
						fileDialog.setFilterPath("C:/");
						String[] filterExt = { "*.xlsm" };
						fileDialog.setFilterExtensions(filterExt);
						String selectedFile = fileDialog.open();
						if (selectedFile != null && selectedFile.length() > 2) {
							dlg.getShell().setMaximized(true);
							dlg.btnCreate.setEnabled(false);
							readExcelFile(selectedFile);
						}
					} catch (Exception ex) {
						MessageBox.post("Some errors occured, please contact your administrator for further instructions.\n" + ex.getMessage(), "Upload ECR Information failed", MessageBox.ERROR);
						ex.printStackTrace();
					}
				}
			});

			dlg.btnDownload.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						downloadTemplateFile();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						handleImportClicked();
					} catch (Exception ex) {
						MessageBox.post("Some errors occured, please contact your administrator for further instructions with below messages.\n" + ex.getMessage(), "Upload ECR Information failed", MessageBox.ERROR);
						ex.printStackTrace();
					}
				}
			});

			dlg.btnCancel.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						handleCancelClicked();
					} catch (Exception ex) {
						MessageBox.post("Some errors occured, please contact your administrator for further instructions.\n" + ex.getMessage(), "Upload ECR Information failed", MessageBox.ERROR);
						ex.printStackTrace();
					}
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void downloadTemplateFile() {
		LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
		parameter.put("Name", "VF_ECRImport_Template");
		parameter.put("Dataset Type", "MS ExcelX");

		TCComponent[] item_list = Query.queryItem(session, parameter, "Dataset...");
		if (item_list != null && item_list.length > 0) {
			File templateFile = TCExtension.downloadDataset(System.getProperty("java.io.tmpdir"), (TCComponentDataset) item_list[0], "MSExcelX", "", session);
			try {
				String templateFileNameWithExt = templateFile.getName();
				String randName = "_" + String.valueOf(new Date().getTime());
				String fileExt = templateFileNameWithExt.substring(templateFileNameWithExt.lastIndexOf("."));
				String templateFileName = templateFileNameWithExt.replace(fileExt, "");

				File newFile = new File(templateFile.getParent(), templateFileName + randName + fileExt);
				Files.move(templateFile.toPath(), newFile.toPath());
				if (templateFile != null)
					Desktop.getDesktop().open(newFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleCancelClicked() throws Exception {
		dlg.close();
	}

	private void handleImportClicked() throws Exception {
		ecrInputManagement.importDataToTC();
		dlg.close();
		MessageBox.post("ECR information extracted from Excel template and saved into Teacenter successfully.", "Information", MessageBox.INFORMATION);
	}

	private void readExcelFile(String filePath) throws Exception, TCException {
		if (ecrSummaryTemplate.isEmpty())
			getECRHTMLTemplate();
		File ecrExcel = new File(filePath);
		ECRInput ecrInput = ecrInputManagement.extractDataFromTemplate(ecrExcel);
		showAndValidateECRInput(ecrInput);
	}

	private void showAndValidateECRInput(ECRInput ecrInput) throws Exception {
		boolean isValidUpdate = true;
		boolean isValidECRNUmber = false; // ECR item ID should be always mandatory
		String htmReport = ecrSummaryTemplate;
		StringBuilder bomInformation = new StringBuilder();
		String selectedECRNumber = ecrInput.getEcrItem().getProperty("item_id");
		String excelECRNumber = "";
		if (ecrInput != null) {
			Map<String, String> attributeMap = ecrInput.getAttributeNamesAndValues();
			if (attributeMap != null) {
				Set<String> ecrHeaderSet = ecrInputManagement.getMetadataHeaders();
				for (String header : ecrHeaderSet) {
					if (header.compareToIgnoreCase("item_id") == 0) {
						excelECRNumber = attributeMap.get(header);
						if (excelECRNumber != null) {
							if (excelECRNumber.compareToIgnoreCase(selectedECRNumber) == 0) {
								isValidECRNUmber = true;
							}
						}
					}

					if (header.compareTo("vf6cp_market") == 0) {
						if (attributeMap.containsKey(header)) {
							String market = attributeMap.get(header);
							if (checkExist(header)) {
								if (market.isEmpty())
									htmReport = htmReport.replace("Market:", "<span class=\"badge bg-danger\">Market:</span>");
								htmReport = htmReport.replace("{" + header + "_VN}", market.contains("VN") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_US}", market.contains("US") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_EU}", market.contains("EU") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_CA}", market.contains("CA") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_THA}", market.contains("THA") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_SG}", market.contains("SG") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_UK}", market.contains("UK") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_AU}", market.contains("AU") ? "v" : "");
							} else {
								htmReport = htmReport.replace("{" + header + "_VN}", market.contains("VN") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_US}", market.contains("US") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_EU}", market.contains("EU") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_CA}", market.contains("CA") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_THA}", market.contains("THA") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_SG}", market.contains("SG") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_UK}", market.contains("UK") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_AU}", market.contains("AU") ? "v" : "");
							}
						} else {
							if (checkExist(header))
								htmReport = htmReport.replace("Market:", "<span class=\"badge bg-danger\">Market:</span>");
							htmReport = htmReport.replace("{" + header + "_VN}", "");
							htmReport = htmReport.replace("{" + header + "_US}", "");
							htmReport = htmReport.replace("{" + header + "_EU}", "");
							htmReport = htmReport.replace("{" + header + "_CA}", "");
							htmReport = htmReport.replace("{" + header + "_THA}", "");
							htmReport = htmReport.replace("{" + header + "_SG}", "");
							htmReport = htmReport.replace("{" + header + "_UK}", "");
							htmReport = htmReport.replace("{" + header + "_AU}", "");
						}
					} else if (header.compareTo("vf6cp_lhd_rhd") == 0) {
						if (attributeMap.containsKey(header)) {
							String vehicle = attributeMap.get(header);
							if (checkExist(header)) {
								if (vehicle.isEmpty())
									htmReport = htmReport.replace("Driving position:", "<span class=\"badge bg-danger\">Driving position:</span>");
								htmReport = htmReport.replace("{" + header + "_LHD}", vehicle.contains("LHD") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_RHD}", vehicle.contains("RHD") ? "v" : "");
							} else {
								htmReport = htmReport.replace("{" + header + "_LHD}", vehicle.contains("LHD") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_RHD}", vehicle.contains("RHD") ? "v" : "");
							}
						} else {
							if (checkExist(header))
								htmReport = htmReport.replace("Driving position:", "<span class=\"badge bg-danger\">Driving position:</span>");
							htmReport = htmReport.replace("{" + header + "_LHD}", "");
							htmReport = htmReport.replace("{" + header + "_RHD}", "");
						}
					} else if (header.compareTo("vf6cp_seat_configuration") == 0) {
						if (attributeMap.containsKey(header)) {
							String seat = attributeMap.get(header);
							if (checkExist(header)) {
								if (seat.isEmpty())
									htmReport = htmReport.replace("Seat configuration:", "<span class=\"badge bg-danger\">Seat configuration:</span>");
								htmReport = htmReport.replace("{" + header + "_5}", seat.contains("5") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_6}", seat.contains("6") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_7}", seat.contains("7") ? "v" : "");
							} else {
								htmReport = htmReport.replace("{" + header + "_5}", seat.contains("5") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_6}", seat.contains("6") ? "v" : "");
								htmReport = htmReport.replace("{" + header + "_7}", seat.contains("7") ? "v" : "");
							}
						} else {
							if (checkExist(header))
								htmReport = htmReport.replace("Seat configuration:", "<span class=\"badge bg-danger\">Seat configuration:</span>");
							htmReport = htmReport.replace("{" + header + "_5}", "");
							htmReport = htmReport.replace("{" + header + "_6}", "");
							htmReport = htmReport.replace("{" + header + "_7}", "");
						}
					} else if (header.compareTo("vf6_impacted_module_comp") == 0) {
						if (attributeMap.containsKey(header)) {
							String module = attributeMap.get(header);
							if (checkExist(header)) {
								if (module.isEmpty())
									htmReport = htmReport.replace("Module:", "<span class=\"badge bg-danger\">Module:</span>");
								htmReport = htmReport.replace("{" + header + "_BIW}", module.contains("BODY IN WHITE") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_D&D}", module.contains("DOOR & CLOSURES") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_INT}", module.contains("INTERIOR") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_EXT}", module.contains("EXTERIOR") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_EE}", module.contains("ELECTRIC ELECTRONICS") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_CHA}", module.contains("CHASSIS") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_PWT}", module.contains("POWER TRAIN") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_BAP}", module.contains("BATTERY PACK") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_EMO}", module.contains("E-MOTOR") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_BMW}", module.contains("BMW ENGINE") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_EDG}", module.contains("EDAG ENGINEER") ? "v" : "x");
							} else {
								htmReport = htmReport.replace("{" + header + "_BIW}", module.contains("BODY IN WHITE") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_D&D}", module.contains("DOOR & CLOSURES") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_INT}", module.contains("INTERIOR") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_EXT}", module.contains("EXTERIOR") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_EE}", module.contains("ELECTRIC ELECTRONICS") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_CHA}", module.contains("CHASSIS") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_PWT}", module.contains("POWER TRAIN") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_BAP}", module.contains("BATTERY PACK") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_EMO}", module.contains("E-MOTOR") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_BMW}", module.contains("BMW ENGINE") ? "v" : "x");
								htmReport = htmReport.replace("{" + header + "_EDG}", module.contains("EDAG ENGINEER") ? "v" : "x");
							}
						} else {
							if (checkExist(header))
								htmReport = htmReport.replace("Module:", "<span class=\"badge bg-danger\">Module:</span>");
							htmReport = htmReport.replace("{" + header + "_BIW}", "x");
							htmReport = htmReport.replace("{" + header + "_D&D}", "x");
							htmReport = htmReport.replace("{" + header + "_INT}", "x");
							htmReport = htmReport.replace("{" + header + "_EXT}", "x");
							htmReport = htmReport.replace("{" + header + "_EE}", "x");
							htmReport = htmReport.replace("{" + header + "_CHA}", "x");
							htmReport = htmReport.replace("{" + header + "_PWT}", "x");
							htmReport = htmReport.replace("{" + header + "_BAP}", "x");
							htmReport = htmReport.replace("{" + header + "_EMO}", "x");
							htmReport = htmReport.replace("{" + header + "_BMW}", "x");
							htmReport = htmReport.replace("{" + header + "_EDG}", "x");
						}
					} else {
						if (attributeMap.containsKey(header)) {
							String value = attributeMap.get(header);
							if (checkExist(header)) {
								if (value.isEmpty()) {
									htmReport = htmReport.replace("{" + header + "}", "<span class=\"badge bg-danger\">Must be filled!</span>");
									isValidUpdate = false;
								} else
									htmReport = htmReport.replace("{" + header + "}", getCheckText(value));
							} else {
								htmReport = htmReport.replace("{" + header + "}", getCheckText(value));
							}
						} else {
							if (checkExist(header)) {
								htmReport = htmReport.replace("{" + header + "}", "<span class=\"badge bg-danger\">Must be filled!</span>");
								isValidUpdate = false;
							} else {
								htmReport = htmReport.replace("{" + header + "}", "");
							}
						}
					}
				}
			}
			Set<String> bomHeaderSet = ecrInputManagement.getBOMHeaders();
			bomInformation.append("<tr>");
			for (String header : bomHeaderSet) {
				bomInformation.append("<td>" + getDisplayNameOfAttribute(header) + "</td>");
			}
			bomInformation.append("</tr>");

			List<Map<String, String>> bomMap = ecrInput.getBomAttributesAndValues();
			if (bomMap != null) {
				for (Map<String, String> attributes : bomMap) {
					bomInformation.append("<tr>");
					for (String header : bomHeaderSet) {
						if (attributes.containsKey(header)) {
							String value = attributes.get(header);
							if (checkExist("Bomline;" + header)) {
								if (value.isEmpty()) {
									bomInformation.append("<td>" + "<span class=\"badge bg-danger\">Must be filled!</span>" + "</td>");
									isValidUpdate = false;
								} else {
									bomInformation.append("<td>" + value + "</td>");
								}
							} else {
								bomInformation.append("<td>" + value + "</td>");
							}
						} else {
							if (checkExist("Bomline;" + header)) {
								bomInformation.append("<td>" + "<span class=\"badge bg-danger\">Must be filled!</span>" + "</td>");
								isValidUpdate = false;
							} else {
								bomInformation.append("<td></td>");
							}
						}
					}
					bomInformation.append("</tr>");
				}
			}
			htmReport = htmReport.replace("{bomtable}", bomInformation.toString());
		}
		dlg.browser.setText(htmReport);

		dlg.btnCreate.setEnabled(isValidUpdate);

		StringBuffer sb = new StringBuffer();
		sb.append("The ECR content is not valid. Please follow steps below to correct it.\n");
		if (!isValidECRNUmber) {
			sb.append("- Please recheck the selected ECR as ").append("the selected ECR number (").append(selectedECRNumber).append(") is different from the Excel ECR number (").append(excelECRNumber).append(").\n");
		}
		if (!isValidUpdate) {
			// isValidECRNUmber
			sb.append("- ").append("Please input all required information. The missing attributes are highlighted in the dialog.\n");
		}
		if (!isValidECRNUmber || !isValidUpdate) {
			dlg.btnCreate.setEnabled(false);
			MessageBox.post(sb.toString(), "ERROR", MessageBox.ERROR);
		} else {
			dlg.btnCreate.setEnabled(true);
		}
	}

	private boolean checkExist(String target) {
		if (attributeMandatory != null && attributeMandatory.length > 0) {
			for (String str : attributeMandatory) {
				if (target.compareToIgnoreCase(str) == 0)
					return true;
			}
		}

		return false;
	}

	private String getDisplayNameOfAttribute(String realValue) {
		try {
			if (realValue.isEmpty())
				return "";
			if (attributeMap == null)
				return "";
			return attributeMap.get(realValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private void getECRHTMLTemplate() {
		LinkedHashMap<String, String> parameter = new LinkedHashMap<String, String>();
		parameter.put("Name", VF_SHOW_ECR_TEMPLATE);
		parameter.put("Dataset Type", "HTML");

		TCComponent[] item_list = Query.queryItem(session, parameter, "Dataset...");
		if (item_list != null && item_list.length > 0) {
			File templateFile = TCExtension.downloadDataset(System.getProperty("java.io.tmpdir"), (TCComponentDataset) item_list[0], "HTML", "", session);
			try {
				byte[] encoded = Files.readAllBytes(Paths.get(templateFile.getPath()));
				ecrSummaryTemplate = new String(encoded, Charset.defaultCharset());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void getAttributeMap() throws TCException, NotLoadedException {
		if (attributeMap == null)
			attributeMap = new LinkedHashMap<String, String>();

		Connection connection = session.getSoaConnection();
		ClientMetaModel metaModel = connection.getClientMetaModel();
		List<Type> types = metaModel.getTypes(new String[] { "Vf6_bom_infomation" }, connection);

		for (Type type : types) {
			Hashtable<String, PropertyDescription> propDescs = type.getPropDescs();
			for (String propName : new TreeSet<String>(propDescs.keySet())) {
				PropertyDescription propDesc = propDescs.get(propName);
				attributeMap.put(propName, propDesc.getUiName());
			}
		}
	}

	private String getCheckText(String input) {
		if (input.compareToIgnoreCase("True") == 0 || input.compareToIgnoreCase("Yes") == 0)
			return "True";
		if (input.compareToIgnoreCase("False") == 0 || input.compareToIgnoreCase("No") == 0)
			return "False";
		return input;
	}
}
