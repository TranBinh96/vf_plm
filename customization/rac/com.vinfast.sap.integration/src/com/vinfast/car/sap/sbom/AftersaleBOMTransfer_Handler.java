package com.vinfast.car.sap.sbom;

import java.io.StringReader;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.teamcenter.integration.model.AftersaleBOMTransferModel;
import com.teamcenter.integration.model.BaseModel;
import com.teamcenter.integration.model.SAPAPIModel;
import com.teamcenter.integration.ulti.StringExtension;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;

public class AftersaleBOMTransfer_Handler extends AbstractHandler {
	private TCSession session;
	private DataManagementService dmService = null;
	private AftersaleBOMTransfer_Dialog dlg;
	private LinkedList<AftersaleBOMTransferModel> aftersaleBomTransferList;
	private String modelCode = "";
	private String modelYear = "";
	private LinkedHashMap<String, SAPAPIModel> sapServerList = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			dmService = DataManagementService.getService(session);
			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();

			if (targetComp == null || targetComp.length == 0) {
				MessageBox.post("Please select Bomline to transfer.", "Error", MessageBox.ERROR);
				return null;
			}

			sapServerList = new LinkedHashMap<>();
			String[] preValue = TCExtension.GetPreferenceValues("VINF_SAP_TRANSFER_IP", session);
			for (String value : preValue) {
				if (value.contains("==")) {
					String[] str = value.split("==");
					if (str.length > 3) {
						SAPAPIModel newItem = new SAPAPIModel();
						newItem.setApiUrl(str[1]);
						newItem.setUsername(str[2]);
						newItem.setPassword(str[3]);
						sapServerList.put(str[0], newItem);
					}
				}
			}

			getModel((TCComponentBOMLine) targetComp[0]);
			aftersaleBomTransferList = new LinkedList<AftersaleBOMTransferModel>();
			LinkedHashMap<String, String> superSessionLOV = TCExtension.GetLovValueAndDisplay("VF4_supersession_code");
			LinkedHashMap<String, String> superSessionCodeList = new LinkedHashMap<String, String>();
			for (Map.Entry<String, String> entry : superSessionLOV.entrySet()) {
				superSessionCodeList.put(entry.getValue(), entry.getKey());
			}

			for (InterfaceAIFComponent target : targetComp) {
				if (target instanceof TCComponentBOMLine) {
					TCComponentBOMLine bomLine = (TCComponentBOMLine) target;
					AftersaleBOMTransferModel newItem = new AftersaleBOMTransferModel(bomLine, superSessionCodeList, dmService);
					newItem.setYear(modelYear);
					newItem.setMaterialClass(modelCode);
					aftersaleBomTransferList.add(newItem);
				}
			}

			dlg = new AftersaleBOMTransfer_Dialog(new Shell());
			dlg.create();

			dlg.cbServer.setItems(sapServerList.keySet().toArray(new String[0]));
			dlg.setTitle("Aftersale BOM Transfer");
//			dlg.setMessage("Please Prepare Data before Transfer.", IMessageProvider.INFORMATION);

			dlg.txtModel.setText(modelCode);
			dlg.txtYear.setText(modelYear);
			dlg.btnAccept.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					transferToSAP();
				}
			});

			dlg.btnAccept.setEnabled(true);
			refreshReport();
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	

	private void transferToSAP() {
		try {
			if (dlg.cbServer.getText().isEmpty()) {
				dlg.setMessage("Server not yet select.", IMessageProvider.WARNING);
				return;
			}
			SAPAPIModel server = sapServerList.get(dlg.cbServer.getText());
			if (server == null) {
				dlg.setMessage("Transfer unsuccess.", IMessageProvider.ERROR);
				return;
			}

			String API_URL = server.getApiUrl();
			String API_USERNAME = server.getUsername();
			String API_PASSWORD = server.getPassword();
			StringBuilder transferData = new StringBuilder();
			transferData.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://plm.com/ServiceBOM\">");
			transferData.append("<soapenv:Header/>");
			transferData.append("<soapenv:Body>");
			transferData.append("<ser:MT_ServiceBOM_REQ>");

			for (AftersaleBOMTransferModel transferItem : aftersaleBomTransferList) {
				transferData.append(transferItem.getTransferData());
			}

			transferData.append("</ser:MT_ServiceBOM_REQ>");
			transferData.append("</soapenv:Body>");
			transferData.append("</soapenv:Envelope>");

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL)).header("Authorization", "Basic " + Base64.getEncoder().encodeToString((API_USERNAME + ":" + API_PASSWORD).getBytes())).header("SOAPAction", "http://sap.com/xi/WebService/soap1.1")
					.header("Content-Type", "text/xml; charset=utf-8").method("POST", HttpRequest.BodyPublishers.ofString(transferData.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", ""))).build();

			HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).proxy(ProxySelector.getDefault()).build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			BaseModel returnModel = getResponseMessage(response.body());
			if (returnModel.getErrorCode().compareTo("00") == 0) {
				dlg.setMessage("Transfer success.", IMessageProvider.INFORMATION);
				dlg.btnAccept.setEnabled(false);
			} else {
				dlg.setMessage("Transfer unsuccess. " + returnModel.getMessage(), IMessageProvider.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BaseModel getResponseMessage(String xml) {
		BaseModel returnModel = new BaseModel();
		returnModel.setErrorCode("01");
		returnModel.setMessage("Response message not correct format.");
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));

			for (int i = 0; i < doc.getElementsByTagName("SOAP:Envelope").getLength(); i++) {
				Node nNode = doc.getElementsByTagName("SOAP:Body").item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					NodeList node = eElement.getElementsByTagName("STATUS");
					if (node.getLength() == 0) {
						node = eElement.getElementsByTagName("STATUS");
					}

					String status = node.item(0).getTextContent();
					if (status.compareTo("OK") == 0)
						returnModel.setErrorCode("00");
					returnModel.setMessage(eElement.getElementsByTagName("MESSAGE").item(0).getTextContent());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnModel;
	}

	private void refreshReport() {
		StringBuffer validationResultText = new StringBuffer();
		validationResultText.append("<html>");
		validationResultText.append("<head> <style> table, th, td { border: 1px solid black; border-collapse: collapse; } tbody { display: block; width: 100%; overflow: auto; } </style> </head>");
		validationResultText.append("<body>");
		validationResultText.append("<table>"); 
		validationResultText.append("<tbody>"); 
		LinkedHashMap<String, String> header = new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("No", "5");
				put("ItemID", "10");
				put("Part Name", "35");
				put("Find No.", "5");
				put("EPC Variant", "10");
				put("Qty", "5");
				put("Suppression", "20");
				put("Effective Date", "10");
				put("ValidTill Date", "10");
				put("Purchase Level Vinfast", "20");
				put("Is After Sale Relevant", "20");
				put("Aftersales Critical", "20");
				put("AFS Part Volume", "20");
				put("Market", "20");
				put("Release Statuses", "20");
				put("Module Group English", "20");
				put("Main Module English", "35");
				put("Module Name", "20");
				put("Supplier Name", "35");
				put("SOR Name", "20");
				put("SOR Number", "20");
				put("Part Make/Buy", "20");
				put("Material Type", "20");
				put("POS ID", "20");
				put("Color Code", "20");
				put("Colored Part", "20");
				put("Torque Information", "20");
			}
		};
		validationResultText.append(StringExtension.genTableHeader(header));		
		//
		int i = 0;
		for (AftersaleBOMTransferModel transferItem : aftersaleBomTransferList) {
			validationResultText.append("<tr style='font-size: 12px; text-align: center'>");
			validationResultText.append("<td>" + String.valueOf(++i) + "</td>");
			validationResultText.append("<td>" + transferItem.getItemID() + "/" + transferItem.getRevNumber() + "</td>");
			validationResultText.append("<td>" + transferItem.getPartNameENG() + "</td>");
			validationResultText.append("<td>" + transferItem.getSeqNumber() + "</td>");
			validationResultText.append("<td>" + transferItem.getEpcVariant() + "</td>");
			validationResultText.append("<td>" + transferItem.getQty() + "</td>");
			validationResultText.append("<td>" + transferItem.getSuppressionCode() + " - " + transferItem.getSuppressionDesc() + "</td>");
			validationResultText.append("<td>" + transferItem.getEffectiveDate() + "</td>");
			validationResultText.append("<td>" + transferItem.getValidTillDate() + "</td>");			
			validationResultText.append("<td>" + transferItem.getPurchaseLvlVin()+"</td>");
			validationResultText.append("<td>" + transferItem.getAfterSaleRelevant()+"</td>");
			validationResultText.append("<td>" + transferItem.getAftersalesCritical()+"</td>");			
			validationResultText.append("<td>" + transferItem.getAfsPartVolume()+"</td>");
			validationResultText.append("<td>" + transferItem.getMarket()+"</td>");
			validationResultText.append("<td>" + transferItem.getReleaseStatuses()+"</td>");
			validationResultText.append("<td>" + transferItem.getModuleGroupEnglish()+"</td>");
			validationResultText.append("<td>" + transferItem.getMainModuleEnglish()+"</td>");
			validationResultText.append("<td>" + transferItem.getModuleName()+"</td>");
			validationResultText.append("<td>" + transferItem.getSupplierName()+"</td>");
			validationResultText.append("<td>" + transferItem.getSorName()+"</td>");
			validationResultText.append("<td>" + transferItem.getSorNumber()+"</td>");
			validationResultText.append("<td>" + transferItem.getPartMakeBuy()+"</td>");
			validationResultText.append("<td>" + transferItem.getMaterialType()+"</td>");
			validationResultText.append("<td>" + transferItem.getPosID()+"</td>");
			validationResultText.append("<td>" + transferItem.getColorCode()+"</td>");
			validationResultText.append("<td>" + transferItem.getColoredPart() +"</td>");
			validationResultText.append("<td>" + transferItem.getTorqueInformation() +"</td>");
//			if (transferItem.isNoNeedTransfer()) {
//				validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetDefault(transferItem.getMessage()) + "</td>");
//			} else {
//				if (transferItem.isValidate())
//					validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetSuccess("Ready to transfer") + "</td>");
//				else
//					validationResultText.append("<td style='text-align: left'>" + StringExtension.genBadgetFail(transferItem.getMessage()) + "</td>");
//			}
			validationResultText.append("</tr>");
		}
		//
		validationResultText.append("</tbody >");
		validationResultText.append("</table>");
		validationResultText.append("</body></html>");

		dlg.brwReport.setText(validationResultText.toString());
	}

	private void getModel(TCComponent bom_line_parent2) {
		try {
			TCComponent parent = bom_line_parent2.getReferenceProperty("bl_parent");
			if (parent != null) {
				if (parent.getProperty("bl_level_starting_0").equals("0")) {
					TCComponentItem item = ((TCComponentBOMLine) parent).getItem();
					try {
						String configuratorContext = item.getPropertyDisplayableValue("Smc0HasVariantConfigContext");
						if (configuratorContext.contains("-")) {
							String[] str = configuratorContext.split("-");
							if (str[0].contains("_")) {
								String[] str1 = str[0].split("_");
								modelCode = str1[0];
								modelYear = str1[1];
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				} else {
					getModel(parent);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
}
