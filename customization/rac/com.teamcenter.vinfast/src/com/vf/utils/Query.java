package com.vf.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.model.ModelObject;

public class Query {
	public final static String QUERY_JES_DATASET = "Dataset Name";//Nguyen create query to search dataset created by dba group only
	public final static String QUERY_JES_ENTRY_DATASET_NAME = "Dataset Name";
	
	public static File downloadFirstNameRefOfDatasetWithPrefix(TCSession session, String queryName, String datasetEntryName, String datasetName, String tempDir, String downloadingFileNamePrefix) throws ServiceException, TCException{
		// NGUYEN 
		TCComponentDataset templateDataset = null;
		SavedQueryService QRservices = SavedQueryService.getService(session);

		//System.out.println("#################################got session and service################################################");
		FindSavedQueriesCriteriaInput qry[]=new FindSavedQueriesCriteriaInput[1];
		FindSavedQueriesCriteriaInput qurey=new  FindSavedQueriesCriteriaInput();
		System.out.println("gor finder query");
		String  name[]={queryName};
		String desc[]={""};
		qurey.queryNames=name;
		qurey.queryDescs=desc;
		qurey.queryType=0;

		qry[0]=qurey;

		//System.out.println("got find qryyyyyyyy session and service################################################");

		FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
		ModelObject[]result=responce1.savedQueries;

		SavedQueryInput qc=new SavedQueryInput();
		SavedQueryInput qc_v[]=new SavedQueryInput[1];

		qc.query=(TCComponentQuery) result[0];
		qc.entries= new String[] {datasetEntryName};
		
		qc.values=new String[] {datasetName};
		qc_v[0]=qc;

		//System.out.println("########################################got find qryyyyyyyy ################################################");
		ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

		SavedQueryResults[] results=responce.arrayOfResults;
		
		if(results[0].numOfObjects!=0){
			for (int i = 0; i < results[0].objects.length; i++) {
				if (results[0].objects[i].getClass().getName().equals(TCComponentDataset.class.getName())) {
					templateDataset = (TCComponentDataset) results[0].objects[i];
				}
			}
		}
		else
		{
			System.out.println("NO Object FOUND");
		}
		//System.out.println("##############result"+folder[0].toString());
	
	
		File excelFile = null;
		TCComponentDataset excel = templateDataset;
		
		if(excel!=null){
			TCComponent[] namedRef = excel.getNamedReferences();
			TCComponentTcFile file = (TCComponentTcFile)namedRef[0];
			// NGUYEN create temp path
			String fileName = file.toString();
			String fileExtension = fileName.substring(fileName.lastIndexOf("."));
			String reportFileName = generateReportFilePath(downloadingFileNamePrefix);
			excelFile = file.getFile(tempDir, reportFileName  + fileExtension);
			//excelFile = file.getFmsFile();
		}

		return excelFile;
	}
	
	private static String generateReportFilePath(String prefix) {
		return prefix + System.currentTimeMillis();
	}
	
	public static File downloadFirstNameRefOfDataset(TCSession session, String queryName, String datasetEntryName, String datasetName, String fullFilePathWithoutExtension) throws ServiceException, TCException{
		// NGUYEN 
		TCComponentDataset templateDataset = null;
		SavedQueryService QRservices = SavedQueryService.getService(session);

		//System.out.println("#################################got session and service################################################");
		FindSavedQueriesCriteriaInput qry[]=new FindSavedQueriesCriteriaInput[1];
		FindSavedQueriesCriteriaInput qurey=new  FindSavedQueriesCriteriaInput();
		System.out.println("gor finder query");
		String  name[]={queryName};
		String desc[]={""};
		qurey.queryNames=name;
		qurey.queryDescs=desc;
		qurey.queryType=0;

		qry[0]=qurey;

		//System.out.println("got find qryyyyyyyy session and service################################################");

		FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
		ModelObject[]result=responce1.savedQueries;

		SavedQueryInput qc=new SavedQueryInput();
		SavedQueryInput qc_v[]=new SavedQueryInput[1];

		qc.query=(TCComponentQuery) result[0];
		qc.entries= new String[] {datasetEntryName};
		
		qc.values=new String[] {datasetName};
		qc_v[0]=qc;

		//System.out.println("########################################got find qryyyyyyyy ################################################");
		ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

		SavedQueryResults[] results=responce.arrayOfResults;
		
		if(results[0].numOfObjects!=0){
			for (int i = 0; i < results[0].objects.length; i++) {
				if (results[0].objects[i].getClass().getName().equals(TCComponentDataset.class.getName())) {
					templateDataset = (TCComponentDataset) results[0].objects[i];
				}
			}
		}
		else
		{
			System.out.println("NO Object FOUND");
		}
		//System.out.println("##############result"+folder[0].toString());
	
	
		File excelFile = null;
		TCComponentDataset excel = templateDataset;
		
		if(excel!=null){
			TCComponent[] namedRef = excel.getNamedReferences();
			TCComponentTcFile file = (TCComponentTcFile)namedRef[0];
			String tcFileName = file.toString();
			String fileExtension = tcFileName.substring(tcFileName.lastIndexOf("."));
			String fullFilePath = fullFilePathWithoutExtension+fileExtension;
			String fileName = fullFilePath.substring(fullFilePath.lastIndexOf("\\")+1);
			String folderPath = fullFilePath.substring(0,fullFilePath.lastIndexOf("\\"));
			excelFile = file.getFile(folderPath, fileName);
			//excelFile = file.getFmsFile();
		}

		return excelFile;
	}
	
	public static TCComponent[] queryItem(TCSession session, LinkedHashMap<String, String> inputQuery, String queryName) {
		TCComponent[] objects = null;
		try {
			SavedQueryService QRservices = SavedQueryService.getService(session);
			FindSavedQueriesCriteriaInput qry[] = new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey = new FindSavedQueriesCriteriaInput();
			String name[] = { queryName };
			String desc[] = { "" };
			qurey.queryNames = name;
			qurey.queryDescs = desc;
			qurey.queryType = 0;

			qry[0] = qurey;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[] result = responce1.savedQueries;

			SavedQueryInput qc = new SavedQueryInput();
			SavedQueryInput qc_v[] = new SavedQueryInput[1];

			qc.query = (TCComponentQuery) result[0];
			qc.entries = new String[inputQuery.size()];
			qc.values = new String[inputQuery.size()];
			int i = 0;
			for(Map.Entry<String, String> entry : inputQuery.entrySet()) {
				qc.entries[i]	= entry.getKey();
				qc.values[i] 	= entry.getValue();
				i++;
			}
			qc_v[0] = qc;

			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

			SavedQueryResults[] results = responce.arrayOfResults;

			if (results[0].numOfObjects != 0) {
				objects = results[0].objects;
			} else {
				System.out.println("[WARNING] NO OBJECT FOUND");
			}
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objects;
	}
	
	public static File downloadFirstNameRefOfDataset(TCSession session, String queryName, String datasetEntryName, String datasetName, String tempDir, String downloadingFileNamePrefix) throws ServiceException, TCException{
		// NGUYEN 
		TCComponentDataset templateDataset = null;
		SavedQueryService QRservices = SavedQueryService.getService(session);

		//System.out.println("#################################got session and service################################################");
		FindSavedQueriesCriteriaInput qry[]=new FindSavedQueriesCriteriaInput[1];
		FindSavedQueriesCriteriaInput qurey=new  FindSavedQueriesCriteriaInput();
		System.out.println("gor finder query");
		String  name[]={queryName};
		String desc[]={""};
		qurey.queryNames=name;
		qurey.queryDescs=desc;
		qurey.queryType=0;

		qry[0]=qurey;

		//System.out.println("got find qryyyyyyyy session and service################################################");

		FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
		ModelObject[]result=responce1.savedQueries;

		SavedQueryInput qc=new SavedQueryInput();
		SavedQueryInput qc_v[]=new SavedQueryInput[1];

		qc.query=(TCComponentQuery) result[0];
		qc.entries= new String[] {datasetEntryName};
		
		qc.values=new String[] {datasetName};
		qc_v[0]=qc;

		//System.out.println("########################################got find qryyyyyyyy ################################################");
		ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

		SavedQueryResults[] results=responce.arrayOfResults;
		
		if(results[0].numOfObjects!=0){
			for (int i = 0; i < results[0].objects.length; i++) {
				if (results[0].objects[i].getClass().getName().equals(TCComponentDataset.class.getName())) {
					templateDataset = (TCComponentDataset) results[0].objects[i];
				}
			}
		}
		else
		{
			System.out.println("NO Object FOUND");
		}
		//System.out.println("##############result"+folder[0].toString());
	
	
		File excelFile = null;
		TCComponentDataset excel = templateDataset;
		
		if(excel!=null){
			TCComponent[] namedRef = excel.getNamedReferences();
			TCComponentTcFile file = (TCComponentTcFile)namedRef[0];
			// NGUYEN create temp path
			String fileName = file.toString();
			String fileExtension = fileName.substring(fileName.lastIndexOf("."));
			String reportFileName = generateReportFilePath(downloadingFileNamePrefix);
			excelFile = file.getFile(tempDir, reportFileName  + fileExtension);
			//excelFile = file.getFmsFile();
		}

		return excelFile;
	}
	
	
	public static File downloadFirstNameRefOfDataset(TCSession session, String queryName, String datasetEntryName, String datasetName, String tempDir, String downloadingFileNamePrefix, long timestamp) throws ServiceException, TCException{
		// NGUYEN 
		TCComponentDataset templateDataset = null;
		SavedQueryService QRservices = SavedQueryService.getService(session);

		//System.out.println("#################################got session and service################################################");
		FindSavedQueriesCriteriaInput qry[]=new FindSavedQueriesCriteriaInput[1];
		FindSavedQueriesCriteriaInput qurey=new  FindSavedQueriesCriteriaInput();
		System.out.println("gor finder query");
		String  name[]={queryName};
		String desc[]={""};
		qurey.queryNames=name;
		qurey.queryDescs=desc;
		qurey.queryType=0;

		qry[0]=qurey;

		//System.out.println("got find qryyyyyyyy session and service################################################");

		FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
		ModelObject[]result=responce1.savedQueries;

		SavedQueryInput qc=new SavedQueryInput();
		SavedQueryInput qc_v[]=new SavedQueryInput[1];

		qc.query=(TCComponentQuery) result[0];
		qc.entries= new String[] {datasetEntryName};
		
		qc.values=new String[] {datasetName};
		qc_v[0]=qc;

		//System.out.println("########################################got find qryyyyyyyy ################################################");
		ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

		SavedQueryResults[] results=responce.arrayOfResults;
		
		if(results[0].numOfObjects!=0){
			for (int i = 0; i < results[0].objects.length; i++) {
				if (results[0].objects[i].getClass().getName().equals(TCComponentDataset.class.getName())) {
					templateDataset = (TCComponentDataset) results[0].objects[i];
				}
			}
		}
		else
		{
			System.out.println("NO Object FOUND");
		}
		//System.out.println("##############result"+folder[0].toString());
	
	
		File excelFile = null;
		TCComponentDataset excel = templateDataset;
		
		if(excel!=null){
			TCComponent[] namedRef = excel.getNamedReferences();
			TCComponentTcFile file = (TCComponentTcFile)namedRef[0];
			// NGUYEN create temp path
			String fileName = file.toString();
			String fileExtension = fileName.substring(fileName.lastIndexOf("."));
			
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timestamp);
			DateFormat df = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss-SSS");
			
			String reportFileName = downloadingFileNamePrefix + "_" + System.currentTimeMillis() + "_" + df.format(cal.getTime());
			excelFile = file.getFile(tempDir, reportFileName  + fileExtension);
			//excelFile = file.getFmsFile();
		}

		return excelFile;
	}
	
	public static File downloadFileOfDataset(TCSession session, String queryName, String datasetEntryName, String datasetName, String folderPath, String outputFileName) throws ServiceException, TCException{
		// PHONG 
		TCComponentDataset templateDataset = null;
		SavedQueryService QRservices = SavedQueryService.getService(session);

		//System.out.println("#################################got session and service################################################");
		FindSavedQueriesCriteriaInput qry[]=new FindSavedQueriesCriteriaInput[1];
		FindSavedQueriesCriteriaInput qurey=new  FindSavedQueriesCriteriaInput();
		System.out.println("gor finder query");
		String name[] = {queryName};
		String desc[] = {""};
		qurey.queryNames = name;
		qurey.queryDescs = desc;
		qurey.queryType = 0;

		qry[0] = qurey;

		//System.out.println("got find qryyyyyyyy session and service################################################");
		FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
		ModelObject[]result=responce1.savedQueries;

		SavedQueryInput qc = new SavedQueryInput();
		SavedQueryInput qc_v[] = new SavedQueryInput[1];

		qc.query = (TCComponentQuery) result[0];
		qc.entries = new String[] {datasetEntryName};
		
		qc.values = new String[] {datasetName};
		qc_v[0] = qc;

		//System.out.println("########################################got find qryyyyyyyy ################################################");
		ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

		SavedQueryResults[] results=responce.arrayOfResults;
		
		if(results[0].numOfObjects != 0) {
			for (int i = 0; i < results[0].objects.length; i++) {
				if (results[0].objects[i].getClass().getName().equals(TCComponentDataset.class.getName())) {
					templateDataset = (TCComponentDataset) results[0].objects[i];
				}
			}
		}
		else {
			System.out.println("NO Object FOUND");
		}
	
		File downloadFile = null;
		
		if(templateDataset != null){
			TCComponent[] namedRef = templateDataset.getNamedReferences();
			TCComponentTcFile file = (TCComponentTcFile)namedRef[0];
			
			downloadFile = file.getFile(folderPath, outputFileName);
			downloadFile = file.getFmsFile();
		}

		return downloadFile;
	}
}
