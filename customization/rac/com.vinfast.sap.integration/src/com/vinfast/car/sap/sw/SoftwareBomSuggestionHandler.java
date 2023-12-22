package com.vinfast.car.sap.sw;

import java.io.File;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.tcservices.TcBOMService;
import com.teamcenter.rac.kernel.tcservices.TcResponseHelper;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RelationAndTypesFilter;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.manufacturing.CoreService;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FindNodeInContextResponse;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FoundNodesInfo;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.services.rac.manufacturing._2013_05.Core.FindNodeInContextInputInfo;
import com.vinfast.car.sap.sw.SoftwarePartLine.ACTION;
import com.vinfast.sap.bom.BOMManager;
import com.vinfast.sap.dialogs.UpdateSoftwareSuggestionFrame;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SoftwareBomSuggestionHandler extends Thread{
	
	private TCSession session;
	private UpdateSoftwareSuggestionFrame fr;
	private BOMManager BOMManager = new BOMManager();
	private LinkedList<SoftwarePartLine> listPartAdd;
	private LinkedList<SoftwarePartLine> listPartDelete;
	private LinkedList<SoftwarePartLine> listPartUpdate;
	private LinkedList<SoftwarePartLine> listPartCommon;
	private LinkedList<String[]> reportRaw;
	private Set<String> operationNotSetPrecise;
	private String problem = "";
	private File reportFile = null;
	private int matchingRate = 0;

	private final String COMPARE_NOTMATCH = "NOT MATCH";
	private final String COMPARE_MATCH = "MATCH";
	private final String ACTION_ADD = "A";
	private final String ACTION_DELETE = "D";
	private final String ACTION_UPDATE = "C";
	private final String ACTION_NONE = "";
	private final int PERFECT_MATCH = 100;
	public void setFr(UpdateSoftwareSuggestionFrame fr) {
		this.fr = fr;
	}
	public String getProblem() {
		return problem;
	}
	public LinkedList<String[]> getReport() {
		return reportRaw;
	}
	public LinkedList<SoftwarePartLine> getListPartAdd() {
		return listPartAdd;
	}

	public LinkedList<SoftwarePartLine> getListPartDelete() {
		return listPartDelete;
	}

	public LinkedList<SoftwarePartLine> getListPartUpdate() {
		return listPartUpdate;
	}

	public Set<String> getOperationNotSetPrecise() {
		return operationNotSetPrecise;
	}
	
	@Override
	public void run() {
		BOMManager = new BOMManager();
		boolean isExecOke = exec();
		if(!isExecOke) {
			MessageBox.post(this.getProblem(), "Error", MessageBox.ERROR);
		}else {
			BOMManager.setSession(session);
			BOMManager.initReport(new String[] {"Part ID", "Part Revision", "ECU" , "Required Action on MBOM", "Info", "Compare Status"}, fr.getTxtSUbGroupID().getText(), fr.getTxtSWBOMID().getText());
			for(String[] row : this.getReport()) {
				BOMManager.printReport("PRINT", row);
			}
			String fileName = String.format("SW_COMPARE_%s_%s", "M"+fr.getTxtSUbGroupID().getText(), "S"+fr.getTxtSWBOMID().getText());
			new Logger();
			String data = Logger.previousTransaction(UIGetValuesUtility.createLogFolder("MCN"+BOMManager.getMCN()), fileName);
			BOMManager.finishReport(data);
			reportFile = BOMManager.popupReport(String.format(fileName));
			
			//update report
			if(matchingRate > PERFECT_MATCH - 5) {
				try {
					TCComponentBOMLine subGroupBl = (TCComponentBOMLine)fr.getSubGroup();
					TCComponentDataset newDataset = UIGetValuesUtility.createDataset(DataManagementService.getService(session), subGroupBl.getItemRevision(), "IMAN_specification", fileName, "Update SWMBOM Validation", "HTML", "IExplore");

					if(newDataset != null) {

						UIGetValuesUtility.uploadNamedReference(session, newDataset, reportFile, "HTML", true, true);
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}	
	}
	
	private void updateProcess(int percent, String detail) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                fr.setProcessPercent(percent, detail);
            }
        });
	}


	
	public boolean exec() {
		updateProcess(0, "Starting...");	
		session = fr.getSWBOM().getSession();
		reportRaw = new LinkedList<String[]>();
		matchingRate = 0;
		//1. expand data
		TCComponentBOMLine subGroupBl = (TCComponentBOMLine)fr.getSubGroup();
		TCComponentBOMLine swBOMBl = (TCComponentBOMLine)fr.getSWBOM();
		LinkedList<SoftwarePartLine> listMBOMPart = getCurrentListMBOMPart(subGroupBl);
		updateProcess(60, "Open Software BOM...");
		LinkedList<SoftwarePartLine> listSWBOMPart = getCurrentListSwBOMPart(swBOMBl);
		updateProcess(95, "Gen report...");
		
		if(operationNotSetPrecise != null && operationNotSetPrecise.isEmpty()) {
			matchingRate = compare(listMBOMPart, listSWBOMPart);
			updateProcess(100, String.format("Matching:%s", Integer.toString(matchingRate)+"%"));
			return true;
		}else {
			String join = String.join(";", operationNotSetPrecise);
			problem = String.format("ECU operations below should be precise %s", join);
			updateProcess(100, "Fail");
			return false;
		}
		
	}
	
	private int compare(LinkedList<SoftwarePartLine> listMBOMPart, LinkedList<SoftwarePartLine> listSWBOMPart) {
		HashMap<String, SoftwarePartLine> mapMBOMPart = new HashMap<String, SoftwarePartLine>();
		for(SoftwarePartLine part : listMBOMPart) {
			mapMBOMPart.put(part.getKey(), part);
		}
		
		HashMap<String, SoftwarePartLine> mapSwBOMPart = new HashMap<String, SoftwarePartLine>();
		for(SoftwarePartLine part : listSWBOMPart) {
			mapSwBOMPart.put(part.getKey(), part);
		}
		
		findListAddPart(mapMBOMPart, mapSwBOMPart);
		findListDeletePart(mapMBOMPart, mapSwBOMPart);
		findListUpdatePart(mapMBOMPart, mapSwBOMPart);
		
		//export to report
		Collections.sort(listPartAdd, new Comparator<SoftwarePartLine>() {
			@Override
			public int compare(SoftwarePartLine arg0, SoftwarePartLine arg1) {
				return Collator.getInstance().compare(arg0.getEcu(), arg1.getEcu());
			}
		});	
		addToReport(listPartAdd, reportRaw);
		Collections.sort(listPartDelete, new Comparator<SoftwarePartLine>() {
			@Override
			public int compare(SoftwarePartLine arg0, SoftwarePartLine arg1) {
				return Collator.getInstance().compare(arg0.getEcu(), arg1.getEcu());
			}
		});
		addToReport(listPartDelete, reportRaw);
		Collections.sort(listPartUpdate, new Comparator<SoftwarePartLine>() {
			@Override
			public int compare(SoftwarePartLine arg0, SoftwarePartLine arg1) {
				return Collator.getInstance().compare(arg0.getEcu(), arg1.getEcu());
			}
		});
		addToReport(listPartUpdate, reportRaw);
		Collections.sort(listPartCommon, new Comparator<SoftwarePartLine>() {
			@Override
			public int compare(SoftwarePartLine arg0, SoftwarePartLine arg1) {
				return Collator.getInstance().compare(arg0.getEcu(), arg1.getEcu());
			}
		});
		addToReport(listPartCommon, reportRaw);
		
		if(listPartAdd.isEmpty() && listPartUpdate.isEmpty() && listPartDelete.isEmpty() && !listPartCommon.isEmpty()) {
			return 100;
		}
		
		return ((listPartCommon.size()*100)/(listPartAdd.size()+listPartUpdate.size()+listPartDelete.size()+listPartCommon.size()));
	}
	
	private void addToReport(LinkedList<SoftwarePartLine> list, LinkedList<String[]> report) {
		for(SoftwarePartLine part : list) {
	    	switch(part.getAction()) {
	    	case ACTION_ADD:
	    		report.addFirst(new String[] {part.getId(), part.getRevID(), part.getEcu(),  ACTION_ADD, part.getInformation(), COMPARE_NOTMATCH});
	    		break;
	    	case ACTION_DELETE:
	    		report.addFirst(new String[] {part.getId(), part.getRevID(), "" ,  ACTION_DELETE, part.getInformation(), COMPARE_NOTMATCH});
	    		break;
	    	case ACTION_UPDATE_REVISION:
	    		report.addFirst(new String[] {part.getId(), part.getRevID(), part.getEcu(),  ACTION_UPDATE, part.getInformation(), COMPARE_NOTMATCH});
	    		break;
	    	case ACTION_UPDATE_VARIANT:
	    		report.addFirst(new String[] {part.getId(), part.getRevID(), part.getEcu(),  ACTION_UPDATE, part.getInformation(), COMPARE_NOTMATCH});
	    		break;
	    	case ACTION_NONE:
	    		report.addLast(new String[] {part.getId(), part.getRevID(), part.getEcu(),  ACTION_NONE, part.getInformation(), COMPARE_MATCH});
	    		break;
	    	}	
		}
	}
	
	private void findListAddPart(HashMap<String, SoftwarePartLine> mapMBOMPart, HashMap<String, SoftwarePartLine> mapSwBOMPart){
		@SuppressWarnings("unchecked")
		HashMap<String,SoftwarePartLine> swBOM = (HashMap<String,SoftwarePartLine>)mapSwBOMPart.clone();
		mapMBOMPart.clone();
		Set<String> keyMBOM = mapMBOMPart.keySet();
		swBOM.keySet().removeAll(keyMBOM);
		for(HashMap.Entry<String, SoftwarePartLine> entry : swBOM.entrySet()) {
			entry.getValue().setAction(ACTION.ACTION_ADD);
		}
		listPartAdd =  new LinkedList<SoftwarePartLine>(swBOM.values());
	}
	
	private void findListDeletePart(HashMap<String, SoftwarePartLine> mapMBOMPart, HashMap<String, SoftwarePartLine> mapSwBOMPart){
		mapSwBOMPart.clone();
		@SuppressWarnings("unchecked")
		HashMap<String,SoftwarePartLine>  mBOM = (HashMap<String,SoftwarePartLine>)mapMBOMPart.clone();
		Set<String> keySWBOM = mapSwBOMPart.keySet();
		mBOM.keySet().removeAll(keySWBOM);
		for(HashMap.Entry<String, SoftwarePartLine> entry : mBOM.entrySet()) {
			entry.getValue().setAction(ACTION.ACTION_DELETE);
		}
		listPartDelete = new LinkedList<SoftwarePartLine>(mBOM.values());
	}
	
	private void findListUpdatePart(HashMap<String, SoftwarePartLine> mapMBOMPart, HashMap<String, SoftwarePartLine> mapSwBOMPart){
		HashMap<String,SoftwarePartLine> update_Parts = new HashMap<String,SoftwarePartLine>();
		HashMap<String,SoftwarePartLine> common_Parts = new HashMap<String,SoftwarePartLine>();
		for (String key : mapMBOMPart.keySet()) {
			try {
				SoftwarePartLine swItem = mapSwBOMPart.get(key);
				SoftwarePartLine mItem = mapMBOMPart.get(key);
				
				if(swItem != null) {
					String swRev = swItem.getRevID();
					String mRev = mItem.getRevID();

					String swFormula = swItem.getVariantFormula();
					String mFormula = mItem.getVariantFormula();

					if((swRev.equals(mRev) && swFormula.equals(mFormula)) == false){
						if(!swRev.equals(mRev)) {
							swItem.setAction(ACTION.ACTION_UPDATE_REVISION);
							swItem.setInformation(String.format("Part updated revision (%s -> %s ) in SW BOM.\n", mRev,swRev ));
						}
						if(!swFormula.equals(mFormula)) {
							swItem.setAction(ACTION.ACTION_UPDATE_VARIANT);
						}
						update_Parts.put(swItem.getKey(), swItem);
						
					}else {
						swItem.setAction(ACTION.ACTION_NONE);
						common_Parts.put(swItem.getKey(), swItem);	
						
					}	
				}
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		listPartUpdate =  new LinkedList<SoftwarePartLine>(update_Parts.values());
		listPartCommon =  new LinkedList<SoftwarePartLine>(common_Parts.values());
	}
	
	
	private LinkedList<SoftwarePartLine> getCurrentListMBOMPart(TCComponentBOMLine subgroup){
		LinkedList<SoftwarePartLine> res = new LinkedList<SoftwarePartLine>();
		try {
			//1. get top mbom
			subgroup.refresh();
			TCComponentBOMLine topMBOM = findTopMBOMFromSubgroup(subgroup);
			System.out.println(topMBOM.getProperty(PropertyDefines.BOM_ITEM_ID));
			TCComponentItemRevision topMBOMRevision = topMBOM.getItemRevision();
			
			new LinkedList<TCComponentBOMLine>();
			res = getSoftwarePartInMBOM(topMBOMRevision, subgroup.getProperty(PropertyDefines.BOM_ITEM_ID));	
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	private LinkedList<SoftwarePartLine> getSoftwarePartInMBOM(TCComponentItemRevision topMBOM, String subGroupID){
		updateProcess(10, "Finding Subgroup...");
		LinkedList<SoftwarePartLine> res = new LinkedList<SoftwarePartLine>();
		//1. open mbom with working precise rule
		try {
			OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(session, topMBOM, PropertyDefines.REVISION_RULE_WORKING_PRECISE);
			TCComponentBOMLine topMBOMLine = null;
			TCComponentBOMLine topBOPLine = null;
			for(OpenContextInfo views : createdBOMViews) {
				if(views.context.getType().equals("BOMLine")) {
					topMBOMLine = (TCComponentBOMLine) views.context;
				}
				if(views.context.getType().equals("Mfg0BvrPlantBOP")) {
					topBOPLine = (TCComponentBOMLine) views.context;
				}
			}
			
			//2. find back subgroup again
			TCComponent[] components = UIGetValuesUtility.searchStruture(session, subGroupID, topMBOMLine);
			TCComponentBOMLine subgroup = (TCComponentBOMLine) components[0];
			updateProcess(40, "Expand MBOM & Get Data...");
			//3. expand subgroup
			LinkedList<TCComponentBOMLine> list = new LinkedList<TCComponentBOMLine>();
			TcResponseHelper responseHelper = TcBOMService.expand(session, subgroup);
			TCComponent[] returnedObjects = responseHelper.getReturnedObjects();
			for(TCComponent ret : returnedObjects) {
				list.add((TCComponentBOMLine)ret);
			}
			updateProcess(45, "Validate Operation");
			
			//4. find part in BOP & check if operation is set precise or not?
			operationNotSetPrecise = getListOperationNotPrecise(returnedObjects, topBOPLine);
			updateProcess(55, "Get Part Info in MBOM");
			
			//5. get sw part information in MBOM
			res = getPartInformation(list);

			//6. close all context
			UIGetValuesUtility.closeAllContext(session, createdBOMViews);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;	
	}
	
	private LinkedList<SoftwarePartLine> getCurrentListSwBOMPart(TCComponentBOMLine swBOMBl){
		LinkedList<SoftwarePartLine> res = new LinkedList<SoftwarePartLine>();
		try {	
			//1. open swbom in precise view
			swBOMBl.refresh();
			TCComponentItemRevision swBOMRevision = swBOMBl.getItemRevision();
			OpenContextInfo[] createdBOMViews = UIGetValuesUtility.createContextViews(session, swBOMRevision, PropertyDefines.REVISION_PRECISE_ONLY);
			TCComponentBOMLine swTopBOMLine = (TCComponentBOMLine) createdBOMViews[0].context;
			updateProcess(70, "Expand SW BOM");
			//2. get all level 2 part
			LinkedList<TCComponentBOMLine> listLevel2 = new LinkedList<TCComponentBOMLine>();
			expandBOMLines(listLevel2, swTopBOMLine, session);
			updateProcess(90, "Get Part Info in SW BOM");
			
			//3. get part information
			res = getPartInformation(listLevel2);
			
			UIGetValuesUtility.closeAllContext(session, createdBOMViews);
			
		} catch (TCException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	private LinkedList<SoftwarePartLine> getPartInformation(LinkedList<TCComponentBOMLine> bls) {
		LinkedList<SoftwarePartLine> res = new LinkedList<SoftwarePartLine>();
		try {
			 
			DataManagementService dmCore = DataManagementService.getService(session);
			String[] partProperties = new String[4];
			partProperties[0] = PropertyDefines.BOM_ITEM_ID;
			partProperties[1] = PropertyDefines.BOM_ITEM_REV_ID;
			partProperties[2] = PropertyDefines.BOM_FORMULA;
			partProperties[3] = PropertyDefines.ITEM_SW_PART_TYPE;
			
			for(TCComponentBOMLine partLine: bls) {
				dmCore.getProperties(new TCComponent[] {partLine}, partProperties);
				SoftwarePartLine part = new SoftwarePartLine();
				part.setId(partLine.getProperty(PropertyDefines.BOM_ITEM_ID));
				part.setRevID(partLine.getProperty(PropertyDefines.BOM_ITEM_REV_ID));
				part.setVariantFormula(partLine.getProperty(PropertyDefines.BOM_FORMULA));
				part.setSwPartType(partLine.getProperty(PropertyDefines.ITEM_SW_PART_TYPE));
				TCComponentBOMLine ecu = (TCComponentBOMLine) partLine.getReferenceProperty(PropertyDefines.BOM_PARENT);
				part.setEcu(ecu.getItemRevision().getProperty(PropertyDefines.ITEM_CURRENT_NAME));
				if(part.isValidSwPartLine()) {
					res.add(part);
//					System.out.println(part.getId() + "/" + part.getRevID());
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	private void expandBOMLines(LinkedList<TCComponentBOMLine> outBomLines, final TCComponentBOMLine rootLine, TCSession conn) {
		
		try {
			int level = Integer.valueOf(rootLine.getProperty("bl_level_starting_0"));
			//we only need expand level 0 & level 1
			if(level == 2) {
				outBomLines.add(rootLine);
				return;
			}else if(level > 2){
				return;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		} catch (TCException e) {
			e.printStackTrace();
			return;
		}
		StructureManagementService structureService = StructureManagementService.getService(conn);
		ExpandPSOneLevelInfo levelInfo = new ExpandPSOneLevelInfo();
		ExpandPSOneLevelPref levelPref = new ExpandPSOneLevelPref();
		levelInfo.parentBomLines = new TCComponentBOMLine[] { rootLine };
		levelInfo.excludeFilter = "None";
		levelPref.expItemRev = false;
		levelPref.info = new RelationAndTypesFilter[0];
		ExpandPSOneLevelResponse levelResp = structureService.expandPSOneLevel(levelInfo, levelPref);

		if (levelResp.output.length > 0) {
			for (ExpandPSOneLevelOutput levelOut : levelResp.output) {
				for (ExpandPSData psData : levelOut.children) {
					expandBOMLines(outBomLines, psData.bomLine, conn);
				}
			}
		}
	}
	
	private TCComponentBOMLine findTopMBOMFromSubgroup(TCComponentBOMLine subgroup) {
		TCComponentBOMLine parent = subgroup;
		TCComponentBOMLine topbom = parent;
		// finding topline
		while(parent != null) {
			try {
				topbom = parent;
				parent = (TCComponentBOMLine) parent.getReferenceProperty("bl_parent");
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		return topbom;
	}
	
	private Set<String> getListOperationNotPrecise(TCComponent[] listSwPart, TCComponentBOMLine topBOP) {
		DataManagementService dmCoreService = DataManagementService.getService(session);
		Set<String> res = new HashSet<String>();
		try {
			CoreService structService = CoreService.getService(session);
			FindNodeInContextInputInfo[] findNodeInputInfo	= new FindNodeInContextInputInfo[1];
			findNodeInputInfo[0] = new FindNodeInContextInputInfo();
			findNodeInputInfo[0].clientID = "findInBOP";
			findNodeInputInfo[0].context = topBOP;
			findNodeInputInfo[0].nodes = listSwPart;
			findNodeInputInfo[0].allContexts = false;
			findNodeInputInfo[0].byIdOnly = true;
			findNodeInputInfo[0].relationDepth = 0;
			findNodeInputInfo[0].relationDirection = 1;
			findNodeInputInfo[0].relationTypes = new String[] {"FND_TraceLink"};
			
			FindNodeInContextResponse InContextInputResponse = structService.findNodeInContext(findNodeInputInfo);

			ServiceData serviceData = InContextInputResponse.serviceData;

			if(serviceData.sizeOfPartialErrors() > 0) {
				MessageBox.post(SoaUtil.buildErrorMessage(serviceData), "Error", MessageBox.ERROR);
			}else {
				FoundNodesInfo[] foundLinkedLines = InContextInputResponse.resultInfo;
				if(foundLinkedLines.length != 0) {
					for(FoundNodesInfo foundParentLines : foundLinkedLines) {
						NodeInfo[] childLinkedNodes =  foundParentLines.resultNodes;
						if(childLinkedNodes.length != 0) {
							for(NodeInfo childNode : childLinkedNodes) {
								TCComponentBOMLine partInBOP = 	(TCComponentBOMLine)childNode.foundNodes[0];	
								TCComponentBOMLine operationLine = (TCComponentBOMLine) partInBOP.getReferenceProperty(PropertyDefines.BOM_PARENT);
								dmCoreService.getProperties(new TCComponent[] {operationLine}, new String[] {PropertyDefines.BOM_IS_PRECISE, PropertyDefines.BOM_ITEM_ID});
								if(!operationLine.getProperty(PropertyDefines.BOM_IS_PRECISE).equals("True")) {
									res.add(operationLine.getProperty(PropertyDefines.BOM_ITEM_ID));
//									System.out.println(String.format("%s/%s is precise: %s", 
//											operationLine.getProperty(PropertyDefines.BOM_ITEM_ID), 
//											operationLine.getProperty(PropertyDefines.BOM_ITEM_REV_ID), 
//											operationLine.getProperty(PropertyDefines.BOM_IS_PRECISE)));
								}else {
								}
							}
						}else {}
					}
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
		
	}
}
