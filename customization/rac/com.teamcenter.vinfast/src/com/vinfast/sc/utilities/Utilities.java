package com.vinfast.sc.utilities;

import java.io.File;
import java.util.HashMap;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.GetRevisionRulesResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RevisionRuleInfo;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.FileManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateDatasetsOutput;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateDatasetsResponse;
import com.teamcenter.services.rac.core._2006_03.FileManagement.CommitDatasetFileInfo;
import com.teamcenter.services.rac.core._2006_03.FileManagement.DatasetFileInfo;
import com.teamcenter.services.rac.core._2006_03.FileManagement.DatasetFileTicketInfo;
import com.teamcenter.services.rac.core._2006_03.FileManagement.GetDatasetWriteTicketsInputData;
import com.teamcenter.services.rac.core._2006_03.FileManagement.GetDatasetWriteTicketsResponse;
import com.teamcenter.services.rac.core._2007_06.DataManagement.RelationAndTypesFilter;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsData2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsOutput2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsPref2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationsResponse2;
import com.teamcenter.services.rac.core._2007_09.DataManagement.ExpandGRMRelationship;
import com.teamcenter.services.rac.core._2008_06.DataManagement.DatasetProperties2;
import com.teamcenter.services.rac.core._2010_09.DataManagement.NameValueStruct1;
import com.teamcenter.services.rac.core._2010_09.DataManagement.PropInfo;
import com.teamcenter.services.rac.core._2010_09.DataManagement.SetPropertyResponse;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;
import com.teamcenter.soa.client.model.ModelObject;


public class Utilities {

	public static TCComponent findSavedQuery(TCSession session, String queryName) {
		TCComponent[] result = null;
		try
		{
			SavedQueryService QRservices = SavedQueryService.getService(session);
			FindSavedQueriesCriteriaInput findQuery = new  FindSavedQueriesCriteriaInput();
			findQuery.queryNames= new String[] {queryName};
			findQuery.queryDescs= new String[] {""};
			findQuery.queryType=0;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(new FindSavedQueriesCriteriaInput[] {findQuery});
			result = responce1.savedQueries;
		}
		catch (ServiceException e) {
			// TODO Auto-generated catch block
		}
		return result[0] ;
	}
	public TCComponent[] searchObjects(TCSession session, String[] entires,String[] values, String query_name )
	{
		TCComponent[] objects = null ;
		try
		{
			SavedQueryService QRservices = SavedQueryService.getService(session);
			
			FindSavedQueriesCriteriaInput findQuery = new  FindSavedQueriesCriteriaInput();
			findQuery.queryNames= new String[] {query_name};
			findQuery.queryDescs= new String[] {""};
			findQuery.queryType=0;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(new FindSavedQueriesCriteriaInput[] {findQuery});
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput savedQuery = new SavedQueryInput();

			savedQuery.query = (TCComponentQuery) result[0];
			savedQuery.entries = entires;
			savedQuery.values = values;
			savedQuery.limitListCount = 100;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(new SavedQueryInput[] {savedQuery});

			SavedQueryResults[] results = responce.arrayOfResults;

			if(results[0].numOfObjects!=0){

				objects = results[0].objects ;
			}
		}
		catch (ServiceException e) {
			// TODO Auto-generated catch block
		}
		return objects ;
	}

	public String[] getRevisionRule(TCSession session){

		String[] revRule = {};

		try{
			DataManagementService dmService = DataManagementService.getService(session);
			StructureManagementService structureService =  StructureManagementService.getService(session);
			GetRevisionRulesResponse revisionRules =  structureService.getRevisionRules();
			RevisionRuleInfo[] RuleInfo = revisionRules.output;

			if(RuleInfo != null) {

				TCComponentRevisionRule[] ruleObj = new TCComponentRevisionRule[RuleInfo.length];
				int count = 0;
				for(RevisionRuleInfo rule : RuleInfo){

					ruleObj[count] = rule.revRule;
					count++;
				}

				if(ruleObj.length !=0) {

					revRule =  new String[ruleObj.length];
					dmService.getProperties(ruleObj, new String[] {"object_name"});
					int i=0;
					for(TCComponentRevisionRule rule : ruleObj){

						revRule[i] = rule.getProperty("object_name");
						i++;
					}
				}

			}

		}
		catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return revRule;

	}
	
	public TCComponentDataset createDataset(TCSession session,String filePath, String datasetType, String namedRef) {

		TCComponentDataset dataset = null;
		if(filePath.length()!=0) {

			DataManagementService dmService = DataManagementService.getService(session);
			FileManagementService fileService = FileManagementService.getService(session);
			FileManagementUtility fileUtility = new FileManagementUtility(session.getSoaConnection());
			File fileObject = new File(filePath);

			DatasetProperties2 props = new DatasetProperties2();
			props.clientId = fileObject.getName();
			props.type = datasetType;
			props.name = fileObject.getName();
			props.description = fileObject.getName();
			DatasetProperties2[] currProps = { props };

			CreateDatasetsResponse resp =  dmService.createDatasets2(currProps);
			CreateDatasetsOutput[] dataOutput = resp.output;
			dataset = dataOutput[0].dataset;
			// Assume this file is in current dir

			// Create a file to associate with dataset
			DatasetFileInfo fileInfo = new DatasetFileInfo();
			fileInfo.clientId            = fileObject.getName();
			fileInfo.fileName            = fileObject.getName();
			fileInfo.namedReferencedName = namedRef;
			fileInfo.isText              = false;
			fileInfo.allowReplace        = false;
			DatasetFileInfo[] fileInfos = { fileInfo };

			GetDatasetWriteTicketsInputData inputData = new GetDatasetWriteTicketsInputData();
			inputData.createNewVersion = true;
			inputData.dataset = dataset;
			inputData.datasetFileInfos = fileInfos;

			GetDatasetWriteTicketsResponse  response = fileService.getDatasetWriteTickets(new GetDatasetWriteTicketsInputData[] {inputData});
			CommitDatasetFileInfo[] commitFileInfo = response.commitInfo;
			DatasetFileTicketInfo FileTicketInfo[] = commitFileInfo[0].datasetFileTicketInfos;
			String ticketValue = FileTicketInfo[0].ticket;

			DatasetFileTicketInfo ticketInfo = new DatasetFileTicketInfo();
			ticketInfo.datasetFileInfo = fileInfo;
			ticketInfo.ticket = ticketValue;

			fileUtility.putFileViaTicket(ticketValue, fileObject);

			CommitDatasetFileInfo commitInfo = new CommitDatasetFileInfo();
			commitInfo.createNewVersion = false;
			commitInfo.dataset = dataset;
			commitInfo.datasetFileTicketInfos = new DatasetFileTicketInfo[] {ticketInfo};

			fileService.commitDatasetFiles(new CommitDatasetFileInfo[] {commitInfo});

			fileUtility.term();

		}
		return dataset;
	}
	
	public void centerToScreen(Shell shell) {

		Monitor primary = Display.getCurrent().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		shell.setLocation(x, y);
	}
	public static HashMap<TCComponent, TCComponent[]> expandBOPOneLevel(TCSession session, TCComponent[] parentLines){

		HashMap<TCComponent, TCComponent[]> expandLines = new HashMap<TCComponent, TCComponent[]>();

		DataManagementService dataManagementService = DataManagementService.getService(session);
		dataManagementService.getProperties(parentLines, new String[] {PropertyDefines.BOM_ALL_MATERIAL});

		try {

			for(TCComponent parent : parentLines) {

				TCComponent[] returnedObjects = parent.getRelatedComponents(PropertyDefines.BOM_ALL_MATERIAL);

				expandLines.put(parent, returnedObjects);
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return expandLines;
	}

	public static TCComponent getComponent() {
		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		TCComponent tcComponent = (TCComponent)targetComponents[0];
		return tcComponent;
	}
	
	public static TCComponent[] getRelatedComponents(DataManagementService dmService, TCComponent primaryObject, String[] objectType, String relation) {

		TCComponent[] relatedObj = null;

		RelationAndTypesFilter filter = new RelationAndTypesFilter();
		filter.otherSideObjectTypes = objectType;
		filter.relationTypeName = relation;

		ExpandGRMRelationsPref2 relationPref =  new ExpandGRMRelationsPref2();
		relationPref.expItemRev = false;
		relationPref.returnRelations = false;
		relationPref.info = new RelationAndTypesFilter[] {filter};

		ExpandGRMRelationsResponse2 response = dmService.expandGRMRelationsForPrimary(new TCComponent[] {primaryObject}, relationPref);
		ExpandGRMRelationsOutput2[] output = response.output;
		ExpandGRMRelationsData2[] data =  output[0].relationshipData;
		ExpandGRMRelationship[] relations = data[0].relationshipObjects;

		if(relations.length != 0) {

			relatedObj = new TCComponent[relations.length];
			int i = 0;

			for(ExpandGRMRelationship relationObj : relations) {
				relatedObj[i] = relationObj.otherSideObject;
				i++;
			}
		}
		return relatedObj;
	}
	
	public static boolean isReleasedItem(TCComponentItemRevision rev) {
		String statusList;
		try {
			statusList = rev.getProperty(PropertyDefines.ITEM_RELEASE_STATUS_LIST);
			return statusList.length() > 0; 
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	public static TCComponentBOMLine findTopMBOMFromSubgroup(TCComponentBOMLine subgroup) {
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
	
	public static boolean setProperty(DataManagementService dmService,TCComponent object,String property, String value) {
		boolean result = false;
		NameValueStruct1 tStruct = new NameValueStruct1();
		tStruct.name = property;
		tStruct.values =  new String[] {value};

		PropInfo propertInfo = new PropInfo();
		propertInfo.object = object;
		propertInfo.vecNameVal = new NameValueStruct1[] {tStruct};

		SetPropertyResponse response = dmService.setProperties(new PropInfo[] {propertInfo}, new String[] {});
		ServiceData sd = response.data;

		if (sd.sizeOfPartialErrors() > 0)
		{
			ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors()-1) ;
			ErrorValue[] errorValue = errorStack.getErrorValues() ;
			for (int inx = 0 ; inx < errorValue.length ; inx++)
			{
				System.out.println(errorValue[inx].getMessage());
			}

		}else {
			return true;
		}
		return result;
	}
}
