package com.vinfast.mbom;

import java.io.File;
import java.util.ArrayList;

import com.teamcenter.rac.kernel.IRelationName;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.internal.rac.core.ThumbnailService;
import com.teamcenter.services.internal.rac.core._2009_10.Thumbnail.SearchOrders;
import com.teamcenter.services.internal.rac.core._2009_10.Thumbnail.ThumbnailFileTicketsResponse;
import com.teamcenter.services.rac.manufacturing.StructureSearchService;
import com.teamcenter.services.rac.manufacturing._2009_10.StructureSearch.MFGSearchCriteria;
import com.teamcenter.services.rac.manufacturing._2009_10.StructureSearch.OccurrenceNoteExpression;
import com.teamcenter.services.rac.manufacturing._2009_10.StructureSearch.SearchExpressionSet;
import com.teamcenter.services.rac.manufacturing._2009_10.StructureSearch.StructureSearchResultResponse;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.model.ModelObject;


public class MBOMUtil {


	public static TCComponent[] SearchStruture(StructureSearchService strutService, DataMap dataMap, TCComponent searchScope) {

		TCComponent[] foundObjects = null;

		try {
			String mainModuleGroup = dataMap.getModuleGroup();
			String mainModule = dataMap.getMainModule();
			String moduleName = dataMap.getModuleName();

			OccurrenceNoteExpression[] noteExp = new OccurrenceNoteExpression[4];
			noteExp[0] =  new OccurrenceNoteExpression();
			noteExp[0].noteType = "VL5_module_group";
			noteExp[0].queryOperator = "Equal";
			noteExp[0].values = new String[] {mainModuleGroup};

			noteExp[1] =  new OccurrenceNoteExpression();
			noteExp[1].noteType = "VL5_main_module";
			noteExp[1].queryOperator = "Equal";
			noteExp[1].values = new String[] {mainModule};

			noteExp[2] =  new OccurrenceNoteExpression();
			noteExp[2].noteType = "VL5_module_name";
			noteExp[2].queryOperator = "Equal";
			noteExp[2].values = new String[] {moduleName};

			noteExp[3] =  new OccurrenceNoteExpression();
			noteExp[3].noteType = "VL5_purchase_lvl_vf";
			noteExp[3].queryOperator = "Equal";
			noteExp[3].values = new String[] {"P"};

			SearchExpressionSet searchSet =  new SearchExpressionSet();
			//searchSet.itemAndRevisionAttributeExpressions = new AttributeExpression[] {};
			searchSet.doTrushapeRefinement = false;
			searchSet.returnScopedSubTreesHit = false;
			//searchSet.savedQueryExpressions =  new SavedQueryExpression[] {};
			searchSet.occurrenceNoteExpressions = noteExp;

			MFGSearchCriteria mfgCriteria = new MFGSearchCriteria();

			ArrayList<TCComponent> searchParts = new ArrayList<TCComponent>();

			StructureSearchResultResponse startResponse = strutService.startSearch(new TCComponent[] {searchScope}, searchSet, mfgCriteria);

			TCComponent cursor = startResponse.searchCursor;

			cursor = continueSearch(strutService, cursor, searchParts);

			strutService.stopSearch(cursor);

			if(!searchParts.isEmpty()) {

				foundObjects = new TCComponent[searchParts.size()]; 
				foundObjects = searchParts.toArray(foundObjects); 
			}

		} catch (ServiceException e1) {
			//TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return foundObjects;

	}
	
	public static TCComponent continueSearch(StructureSearchService strutService, TCComponent cursor, ArrayList<TCComponent> searchParts) {

		StructureSearchResultResponse nextResponse;
		TCComponent nextcursor = null;
		try {
			nextResponse = strutService.nextSearch(cursor);
			nextcursor = nextResponse.searchCursor;
			TCComponent[] objects = nextResponse.objects;
			if (nextResponse.finished == false) {
				if (objects.length > 0) {
					for (TCComponent object : objects) {
						searchParts.add(object);
					}
				}
				continueSearch(strutService, nextcursor, searchParts);
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nextcursor;
	}
	
	/**
	 * @apiNote Return Named Reference file from Excel Template Dataset
	 * @param session
	 * @param datasetName
	 * @return File
	 */
	public static File getRecipeTemplate(TCSession session, String datasetName) {

		File template = null;
		SavedQueryService QRservices = SavedQueryService.getService(session);
		try {
			FindSavedQueriesCriteriaInput findQuery = new FindSavedQueriesCriteriaInput();
			findQuery.queryNames = new String[] { "Dataset Name" };
			findQuery.queryDescs = new String[0];
			findQuery.queryType = 0;
			FindSavedQueriesResponse SQresponse = QRservices.findSavedQueries(new FindSavedQueriesCriteriaInput[] { findQuery });
			if(SQresponse.serviceData.sizeOfPartialErrors() > 0) {
				MessageBox.post(SoaUtil.buildErrorMessage(SQresponse.serviceData), "Error", MessageBox.ERROR);
			}else {
				ModelObject result = SQresponse.savedQueries[0];
				SavedQueryInput savedQuery = new SavedQueryInput();
				savedQuery.query = (TCComponentQuery) result;
				savedQuery.entries = new String[] {"Dataset Name"};
				savedQuery.values = new String[] {datasetName};
				savedQuery.limitListCount = 1;
				ExecuteSavedQueriesResponse response = QRservices.executeSavedQueries(new SavedQueryInput[] { savedQuery });
				if(response.serviceData.sizeOfPartialErrors() > 0) {
					MessageBox.post(SoaUtil.buildErrorMessage(response.serviceData), "Error", MessageBox.ERROR);
				}else {
					SavedQueryResults[] results = response.arrayOfResults;
					if (results[0].numOfObjects != 0) {
						TCComponentDataset datasets = (TCComponentDataset)results[0].objects[0];
						TCComponentTcFile[] namedRefs = datasets.getTcFiles();
						if(namedRefs != null) {
							template = namedRefs[0].getFmsFile();
						}else {
							MessageBox.post("Error or corrupt loading template. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
						}
					}
				}
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return template;
	}
	public static String loadThumbnail(ThumbnailService service,TCComponent object) {
		SearchOrders order = new SearchOrders();
		order.datasetTypeSearchOrder = new String[] {"BMP"};
		order.relationSearchOrder = new String[] {IRelationName.IMAN_specification};
		ThumbnailFileTicketsResponse response = service.getThumbnailFileTickets(new TCComponent[] {object}, order);
		String[] images = response.thumbnailFileTickets;
		return images[0];
	}
	public static String converArrayToString(ArrayList<String> arrayList) {
		String text = "";
		if(arrayList != null) {
			for(String str : arrayList) {
				if(text.equals("")) {
					text = str;
				}else {
					text = text+";"+str;
				}
			}
		}
		return text;
	}
}
