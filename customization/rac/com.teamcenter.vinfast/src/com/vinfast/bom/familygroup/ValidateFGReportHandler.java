package com.vinfast.bom.familygroup;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.utils.Utils;
import com.vf4.services.rac.custom.ReportDataSourceService;
import com.vf4.services.rac.custom._2020_10.ReportDataSource;
import com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationInput;
import com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationOutput;
import com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationPartInfo;
import com.vf4.services.rac.custom._2020_10.ReportDataSource.FGValidationResult;

import vfplm.soa.common.service.VFUtility;

public class ValidateFGReportHandler extends AbstractHandler{
	private TCSession session;
	private static final Logger logger = Logger.getLogger(ValidateFGReportHandler.class);
	private LinkedHashMap<String, LinkedList<String>> prg2Variant;
	private LinkedHashMap<String, LinkedList<String>> prg2TopBOM; 
	private LinkedHashMap<String, String> donorVehDisVal2RealVal;
	public ValidateFGReportHandler() {
		super();
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		this.prg2Variant = new LinkedHashMap<String, LinkedList<String>>();
		this.prg2TopBOM = new LinkedHashMap<String, LinkedList<String>>();
		FamilyGroupRuleCreationHandler.getPre_PROGRAM_2_CONTEXT(session, prg2Variant);
		getPre_PROGRAM_2_TOPBOM(session, prg2TopBOM);
		try {
			donorVehDisVal2RealVal = Utils.getLovDetailInfo(session, "vf4_donor_vehicle", "Design");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
//		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
//		InterfaceAIFComponent[] appComps = app.getTargetComponents();
//		if(appComps.length > 1) {
//			MessageBox.post("This feature is applicable to single part only.", "Information", MessageBox.INFORMATION);
//			return null;
//		}
		
		try {
//			if(((TCComponent) appComps[0]).isTypeOf("BOMLine")){
//				TCComponentItemRevision partRev = ((TCComponentBOMLine) appComps[0]).getItemRevision();
				
				ValidateFGReportDialog dlg = new ValidateFGReportDialog(new Shell());
				dlg.create();
				dlg.setTitle("Famiy Group Report");
				dlg.setMessage("Define input information",IMessageProvider.INFORMATION);
				dlg.getCbPrg().addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent arg0) {
						String currenctTxt = dlg.getCbPrg().getText();
						if(prg2Variant.containsKey(currenctTxt)) {
							String[] variantVal = (String[]) prg2Variant.get(currenctTxt).toArray(new String[0]);
							dlg.getCbVariant().setItems(variantVal);
						}
						if(prg2TopBOM.containsKey(currenctTxt)) {
							String[] topBOMs = (String[]) prg2TopBOM.get(currenctTxt).toArray(new String[0]);
							dlg.getCbTopPart().setItems(topBOMs);
						}
					}
				});
				dlg.getCbPrg().setItems(this.prg2Variant.keySet().stream().toArray(String[]::new));
				dlg.getCbRevisionRule().setItems(Utils.getRevisionRule(session));
				dlg.getBtnOk().addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if(dlg.getCbPrg().getText().isEmpty() || dlg.getCbVariant().getText().isEmpty() 
								|| dlg.getCbTopPart().getText().isEmpty() || dlg.getCbRevisionRule().getText().isEmpty()) {
							dlg.setMessage("Please input all required information",IMessageProvider.WARNING);
							return; 
						}else {
							ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(dlg.getShell());
							progressDialog.open();
							IProgressMonitor progressMonitor = progressDialog.getProgressMonitor();
							int workLoad = 1;
							progressMonitor.setTaskName("Validating...");
							progressMonitor.beginTask("Waiting server response...", workLoad);
							progressMonitor.worked(1);
							progressMonitor.subTask("Server executing...");
							if (progressMonitor.isCanceled()) {
								return;
							}
//							dlg.showProgressBar();
							
							//TODO call soa server api
							FGValidationInput fgIn = new FGValidationInput();
							fgIn.itemId = dlg.getCbTopPart().getText();
							if (donorVehDisVal2RealVal.containsValue(dlg.getCbPrg().getText())) {
							    for (final Object entry : donorVehDisVal2RealVal.keySet()) {
							        if (donorVehDisVal2RealVal.get(entry).compareToIgnoreCase(dlg.getCbPrg().getText()) == 0) {
							        	fgIn.program = entry.toString();
							        	break;
							        }
							    }
							}
							fgIn.revId = "01";
							fgIn.itemType = "VF4_Design";
							fgIn.revisionRule = dlg.getCbRevisionRule().getText();
							fgIn.savedVariant2LoadBom = dlg.getCbVariant().getText();
							fgIn.variant2Validate = dlg.getCbVariant().getText();
							
							FGValidationOutput fgOut = ReportDataSourceService.getService(session).validateFamilyGroup(fgIn);
							if(fgOut.serviceData.sizeOfPartialErrors() > 0) {
								MessageBox.post("Exception: " + VFUtility.HanlderServiceData(fgOut.serviceData), "ERROR", MessageBox.ERROR);
								return;
							}
							
							//DUMMY data
//							FGValidationOutput fgOut = new FGValidationOutput();
//							fgOut.validationResults = new FGValidationResult[4];
//							fgOut.finalValidationResult = "Pass";
//							for(int i = 0; i< 4; i++) {
//								FGValidationResult fgRes = new FGValidationResult();
//								fgRes.fgCode = "FG" + i;
//								fgRes.fgDescription = "Dummy";
//								fgRes.minQuality = 2;
//								fgRes.maxQuality = 2;
//								if(i == 0) {
//									fgRes.validationResult = "Pass";
//								}
//								if(i == 1) {
//									fgRes.validationResult = "Failed";
//								}
//								if(i == 2) {
//									fgRes.validationResult = "Warning";
//								}
//								if(i == 3) {
//									fgRes.validationResult = "Pass";
//								}
//								fgRes.relevantPartsInfo = new FGValidationPartInfo[4];
//								//create dummy data for testing
//								for(int j = 0; j < 4; j++) {
//									FGValidationPartInfo partInfo = new FGValidationPartInfo();
//									TCComponent part = new TCComponent();
//									partInfo.partRev = part;
//									partInfo.quantity = 2;
//									fgRes.relevantPartsInfo[j] = partInfo;
//								}
//								fgOut.validationResults[i] = fgRes;
//							}
							//DUMMY data
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							progressMonitor.done();
							progressDialog.close();
							
							
							dlg.showValidationResult(generateResultInHTML(fgOut));
							dlg.getShell().setSize(606, 600);
						}
					}
					
				});
				dlg.open();
//			}
		} catch (TCException e) {
			MessageBox.post("Exception: " + e.toString(), "ERROR", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	public static void getPre_PROGRAM_2_TOPBOM(TCSession session, LinkedHashMap<String, LinkedList<String>> output) {
		String[]tmp = session.getPreferenceService().getStringValues("VF_PROGRAM_2_TOPBOM");
		if(tmp != null && tmp.length > 0) {
			for(int i = 0; i < tmp.length; i++) {
				if(tmp[i].contains("=")) {
					LinkedList<String> topBOMs = new LinkedList<String>();
					String prg = tmp[i].split("=")[0];
					String topBOM = tmp[i].split("=")[1];
					if(topBOM.contains(";")) {
						topBOMs.addAll(Arrays.asList(topBOM.split(";")));
					}else {
						topBOMs.add(topBOM);
					}
					output.put(prg, topBOMs);
				}
			}
		}
	}
	
	private String generateResultInHTML(FGValidationOutput rawData) {
		StringBuilder sb = new StringBuilder();
		String finalStatus = rawData.finalValidationResult;
		sb.append("<p style=\"text-align:center;\"><b>"+"Validation Status: " + "<span" + getFinalStatusStyle(finalStatus) +">" +finalStatus+"</span>" +"</b></br>");
		sb.append("<table border=\"1\" style=\"width:650px;background-color:#f0f0f0;margin-left:auto;margin-right:auto\" ><tr><td style=\"width:40%;\"><b>FG Code</b></td><td style=\"width:30%;\"><b>Decription</b></td><td style=\"width:10%;\"><b>Validation Type</b></td><td style=\"width:10%\"><b>Status</b></td><td style=\"width:10%\"><b>Quantity</b></td></tr>");
		for(FGValidationResult detailRes : rawData.validationResults) {
			Double totalPartInGroup = detailRes.fgCountInBom;
			String fgCode = detailRes.fgCode;
			String status = detailRes.validationResult;
			String desc = detailRes.fgDescription;
			String valiType = detailRes.fgValidationType;
			double minQty = detailRes.minQuantity;
			double maxQty = detailRes.maxQuantity;
			sb.append("<tr"+ getStatusStyle(status)+"><td>"+fgCode +"(" +minQty +" - "+maxQty+")"+"</td><td>"+desc+"</td><td>"+valiType+"</td><td>"+status+"</td><td>"+totalPartInGroup+"</td></tr>");
			for(FGValidationPartInfo partInfo : detailRes.relevantPartsInfo) {
				TCComponent partObj = partInfo.partRev;
				Double qty = partInfo.quantity;
				String partId = "";
				try {
					partId = partObj.getProperty("item_id");
				} catch (TCException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sb.append("<tr><td></td><td></td><td></td><td>"+partId+"</td><td>"+qty+"</td></tr>");
			}
		}
		return sb.toString();
	}
	private String getFinalStatusStyle(String finalStatus) {
		String color = "";
		if(finalStatus.compareToIgnoreCase("PASSED") ==0) {
			color = "color:#00e640";
		}else if(finalStatus.compareToIgnoreCase("FAILED") ==0) {
			color = "color:red";
		}else if(finalStatus.compareToIgnoreCase("WARNING") ==0) {
			color = "color:yellow";
		}
		return " style=\"text-transform:uppercase;" + color +"\"";
	}
	
	private String getStatusStyle(String fgStatus) {
		String style = "";
		if(fgStatus.compareToIgnoreCase("PASSED") ==0) {
			style = " style=\"background-color:#00e640\"";
		}else if(fgStatus.compareToIgnoreCase("FAILED") ==0) {
			style = " style=\"background-color:red\"";
		}else if(fgStatus.compareToIgnoreCase("WARNING") ==0) {
			style = " style=\"background-color:yellow\"";
		}
		return style;
	}
	
}
