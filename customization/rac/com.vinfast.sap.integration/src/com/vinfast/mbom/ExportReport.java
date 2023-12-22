package com.vinfast.mbom;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.internal.rac.core.ThumbnailService;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.SessionService;
import com.teamcenter.services.rac.manufacturing.StructureSearchService;
import com.teamcenter.soa.common.ObjectPropertyPolicy;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class ExportReport extends AbstractHandler {

	private DataManagementService coreDMservice = null;
	private StructureSearchService strutService = null;
	private StructureManagementService CADSMService = null;
	private com.teamcenter.services.rac.bom.StructureManagementService BOMSMService = null;
	private com.teamcenter.services.rac.manufacturing.DataManagementService manufDMService = null;
	private ThumbnailService tService = null;
	TCSession session = null;
	private TCComponentBOMLine searchScope = null;
	private static String STR_TEMPLATE = "VF_RECIPE_MBOM_SHOPS";
	UpdateBaseMBOM updateMBOM = null;
	String shopName;
	DataManagementService coreDMService = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		try {

			IParameter cmdParamIsMEPart = event.getCommand().getParameter("com.teamcenter.vinfast.commands.manufacturing.report");
			String commandName = (cmdParamIsMEPart != null && cmdParamIsMEPart.getName() != null) ? cmdParamIsMEPart.getName() : "";
			ISelection selection = HandlerUtil.getCurrentSelection( event );
			InterfaceAIFComponent[] objects = SelectionHelper.getTargetComponents(selection);
			searchScope = (TCComponentBOMLine)objects[0];
			session = searchScope.getSession();
			setObjectPolicy();
			shopName = commandName;
			if((searchScope.parent() == null && searchScope.getProperty(PropertyDefines.BOM_OBJECT_TYPE).equals(PropertyDefines.TYPE_DESIGN_REVISION)) == false) {
				MessageBox.post("Please select EBOM TopLine to to generate report", "Error", MessageBox.ERROR);
				return null;
			}
			session.queueOperation( new AbstractAIFOperation( "Exporting..." )
			{
				@Override
				public void executeOperation() throws Exception
				{
					System.out.println(Calendar.getInstance().getTime());
					File templateFile = MBOMUtil.getRecipeTemplate(session, STR_TEMPLATE);
					if(templateFile != null) {
						FileDataCollector templateData = new FileDataCollector(templateFile, shopName);
						if(templateData.isLoaded == true) {
							GenerateExcelReport excelReport = new GenerateExcelReport();
							excelReport.printHeaderData(new String[] {"SHOP","LINE","SUB GROUP","PART NUMBER","THUMBNAIL", "MAIN MODULE GROUP","MAIN MODULE","MODULE NAME"});
							updateMBOM(excelReport, templateData);
							excelReport.autoFitColumnText(new int[] {0,1,2,3,4,5,6,7});
							excelReport.printToExcel("C:/Temp/"+shopName+".xlsx");
							MessageBox.post("Export successful \"C:/Temp/"+shopName+".xlsx\"", "Success", MessageBox.INFORMATION);
						}
					}
					System.out.println(Calendar.getInstance().getTime());
				}
			} );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		return null;
	}

	public void updateMBOM(GenerateExcelReport excelReport, FileDataCollector fileData) throws TCException {
		//Lines created
		for(String line : fileData.getShopLines()) {
			fileData.setCurrentLine(line);
			HashMap<String, ArrayList<DataMap>> subGroups = fileData.get(line);
			for(String subGroup : subGroups.keySet()) {
				fileData.setCurrentGroup(subGroup);
				ArrayList<String> consumablesIDs = fileData.getSubGroupConsumables(line, subGroup);
				if(consumablesIDs != null) {
					String queryValue = MBOMUtil.converArrayToString(consumablesIDs);
					TCComponent[] items = UIGetValuesUtility.quickSearch(session, new String[] {"Item ID","Type"}, new String[] {queryValue,"VF3_manuf_part"}, "Item...");
					if(items != null) {
						for(TCComponent item : items) {
							try {
								String ID = item.getPropertyDisplayableValue(IPropertyName.ITEM_ID);
								String imageID = fileData.getThumbnail(ID);
								if(imageID == null) {
									imageID = MBOMUtil.loadThumbnail(getThumbnailService(), item);
									fileData.setThumbnail(ID, imageID.equals("")?"No Preview":imageID);
								}
								excelReport.printRowData(new String[] {shopName,line,subGroup,ID,imageID,"NA","NA","NA"});
							} catch (NotLoadedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}else {
						excelReport.printRowData(new String[] {shopName,line,subGroup,"NA","No Preview","NA","NA","NA"});
					}
				}
				if(subGroups.get(subGroup) != null) {
					updateSubGroupRelated(subGroups,fileData,excelReport);//Update Map
				}
			}
		}

	}

	public void updateSubGroupRelated(HashMap<String, ArrayList<DataMap>> subGroups,FileDataCollector dataCollector, GenerateExcelReport excelReport) {

		ArrayList<DataMap> dataMap = subGroups.get(dataCollector.getCurrentGroup());
		for(DataMap map : dataMap) {
			String moduleName = map.getModuleName();
			if(map.getID().isEmpty() == true && moduleName.isEmpty() == false) {
				TCComponent[] recipeLines = MBOMUtil.SearchStruture(getStuctureSearchService(), map, searchScope);
				if(recipeLines != null) {
					for(TCComponent resp : recipeLines) {
						try {
							TCComponentBOMLine BL =  (TCComponentBOMLine)resp;
							String ID = BL.getPropertyDisplayableValue(PropertyDefines.BOM_ITEM_ID);
							String MG = resp.getPropertyDisplayableValue("VL5_module_group");
							String MM = resp.getPropertyDisplayableValue("VL5_main_module");
							String MN = resp.getPropertyDisplayableValue("VL5_module_name");
							String imageID = dataCollector.getThumbnail(ID);
							if(imageID == null) {
								imageID = MBOMUtil.loadThumbnail(getThumbnailService(), BL.getItem());
								dataCollector.setThumbnail(ID, imageID.equals("")?"No Preview":imageID);
							}
							excelReport.printRowData(new String[] {shopName,dataCollector.getCurrentLine(),dataCollector.getCurrentGroup(),ID,imageID,MG,MM,MN});
						} catch (NotLoadedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (TCException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else {
					excelReport.printRowData(new String[] {shopName,dataCollector.getCurrentLine(),dataCollector.getCurrentGroup(),"NA","No Preview","NA","NA","NA"});
				}
			}
		}
	}

	public ThumbnailService getThumbnailService() {

		if(tService == null) {
			tService = ThumbnailService.getService(session);
			return tService;
		}else {
			return tService;
		}
	}

	public DataManagementService getCoreDataMangement() {

		if(coreDMservice == null) {
			coreDMservice = DataManagementService.getService(session);
			return coreDMservice;
		}else {
			return coreDMservice;
		}
	}

	public StructureSearchService getStuctureSearchService() {

		if(strutService == null) {
			strutService = StructureSearchService.getService(session);
			return strutService;
		}else {
			return strutService;
		}
	}

	public StructureManagementService getCADStructureManagementService() {

		if(CADSMService == null) {
			CADSMService = StructureManagementService.getService(session);
			return CADSMService;
		}else {
			return CADSMService;
		}
	}

	public com.teamcenter.services.rac.bom.StructureManagementService getBOMStructureManagementService() {

		if(BOMSMService == null) {
			BOMSMService = com.teamcenter.services.rac.bom.StructureManagementService.getService(session);
			return BOMSMService;
		}else {
			return BOMSMService;
		}
	}

	public com.teamcenter.services.rac.manufacturing.DataManagementService getMFGDataManagementService() {

		if(manufDMService == null) {
			manufDMService = com.teamcenter.services.rac.manufacturing.DataManagementService.getService(session);
			return manufDMService;
		}else {
			return manufDMService;
		}
	}

	protected void setObjectPolicy() {

		SessionService sessionService = SessionService.getService(session);
		ObjectPropertyPolicy policy = new ObjectPropertyPolicy();
		policy.addType("BOMLine", new String[] {"bl_item_item_id","VL5_module_group","VL5_main_module","VL5_module_name"});
		policy.addType("VF3_vfitemRevision", new String[] {"items_tag","bom_view_tags","view"});
		policy.addType("VF3_manuf_part", new String[] {"item_id"});
		policy.addType("RevisionRule", new String[] {"current_name"});
		sessionService.setObjectPropertyPolicy(policy); 
	}
}
