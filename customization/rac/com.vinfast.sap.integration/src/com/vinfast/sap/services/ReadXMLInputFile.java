package com.vinfast.sap.services;

import java.io.File;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class ReadXMLInputFile {

	public String getXML(TCComponent tcComp)
	{
		String traverse_file = null;
		
		try{
			
			ResourceBundle bundle = ResourceBundle.getBundle("Transfer");
			String compType = tcComp.getTypeComponent().getType() ;
			traverse_file = bundle.getString(compType + ".FILE");
			
		}catch(MissingResourceException me){
			
			me.printStackTrace();
			return null;
		}
		return traverse_file ;

	}
	
	public InputStream loadXML(String fileName){
		
		InputStream file = this.getClass().getClassLoader().getResourceAsStream("/xml/"+fileName);
		
		return file;
	}
	
	public String getInputFileName (TCComponent tcComp)
	{
		String traverse_file = null;
		
		try{
			
			ResourceBundle bundle = ResourceBundle.getBundle("Transfer");
			String compType = tcComp.getTypeComponent().getType() ;
			traverse_file = bundle.getString(compType + ".FILE");
			
		}catch(MissingResourceException me){
			
			me.printStackTrace();
			return null;
		}
		return traverse_file ;

	}
	
	
	
	public File getParserFileName(TCSession session, TCComponentItemRevision itemRevision, ResourceBundle bundle)
	{
		String traverse_file = null;
		File defaultFile = null;
		
		try{

			String itemType = itemRevision.getType() ;
			traverse_file = bundle.getString(itemType+".FILE");

			if(traverse_file != null){

				String datasetname[] = {traverse_file};
				TCComponentQueryType tcQueryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
				TCComponentQuery tcdatasetQuery =(TCComponentQuery)tcQueryType.find("Dataset Name");
				String datasetnamelabel[]={"Dataset Name"};
				TCComponent[] datasetObj = tcdatasetQuery.execute(datasetnamelabel, datasetname);
				for(TCComponent progressReportDataset : datasetObj)
				{
					if(progressReportDataset instanceof TCComponentDataset)
					{
						TCComponentDataset progress_report = (TCComponentDataset) progressReportDataset;
						TCComponentTcFile[] relatedTcFiles = progress_report.getTcFiles();

						for (int iFileCnt = 0; iFileCnt < relatedTcFiles.length;iFileCnt++)
						{     
							String fileName = relatedTcFiles[iFileCnt].toString ();
							int extensionIndex = fileName.lastIndexOf ( '.' );

							if (fileName.substring(extensionIndex).equalsIgnoreCase(".xml")) 
							{

								TCComponentTcFile progressReportFile = relatedTcFiles[iFileCnt];
								defaultFile = progressReportFile.getFmsFile();
								break;
								
							}	
						}
					}
				}
			}

		}catch(MissingResourceException me){

			return null;
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return defaultFile ;

	}
	
	public File getParserFileName(TCSession session, TCComponent itemRevision, ResourceBundle bundle)
	{
		String traverse_file = null;
		File defaultFile = null;
		
		try{

			String itemType = itemRevision.getType() ;
			traverse_file = bundle.getString(itemType+".FILE");

			if(traverse_file != null){

				String datasetname[] = {traverse_file};
				TCComponentQueryType tcQueryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
				TCComponentQuery tcdatasetQuery =(TCComponentQuery)tcQueryType.find("Dataset Name");
				String datasetnamelabel[]={"Dataset Name"};
				TCComponent[] datasetObj = tcdatasetQuery.execute(datasetnamelabel, datasetname);
				for(TCComponent progressReportDataset : datasetObj)
				{
					if(progressReportDataset instanceof TCComponentDataset)
					{
						TCComponentDataset progress_report = (TCComponentDataset) progressReportDataset;
						TCComponentTcFile[] relatedTcFiles = progress_report.getTcFiles();

						for (int iFileCnt = 0; iFileCnt < relatedTcFiles.length;iFileCnt++)
						{     
							String fileName = relatedTcFiles[iFileCnt].toString ();
							int extensionIndex = fileName.lastIndexOf ( '.' );

							if (fileName.substring(extensionIndex).equalsIgnoreCase(".xml")) 
							{

								TCComponentTcFile progressReportFile = relatedTcFiles[iFileCnt];
								defaultFile = progressReportFile.getFmsFile();
								break;
								
							}	
						}
					}
				}
			}

		}catch(MissingResourceException me){

			return null;
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return defaultFile ;

	}
	
	public String getBOMLineFileName (TCComponentBOMLine tcComp)
	{
		String traverse_file = null;
		String compType = null;
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("Transfer");
			compType = tcComp.getItemRevision().getType();
			traverse_file = bundle.getString(compType + ".FILE");
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(MissingResourceException me){
			//log.writeLog("INFO:| Input File Type not found");
			return null;
		}
		//log.writeLog("INFO:| Input File:-"+traverse_file);
		return traverse_file ;
	
	}

	
	public File readXML(TCSession session , String inputFileName){
		File defaultFile = null;
		try
		{
			String datasetname[] = {inputFileName};
			TCComponentQueryType tcQueryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
			TCComponentQuery tcdatasetQuery =(TCComponentQuery)tcQueryType.find("Dataset Name");
			String datasetnamelabel[]={"Dataset Name"};
			TCComponent[] datasetObj = tcdatasetQuery.execute(datasetnamelabel, datasetname);
			for(TCComponent progressReportDataset : datasetObj)
			{
				if(progressReportDataset instanceof TCComponentDataset)
				{
					TCComponentDataset progress_report = (TCComponentDataset) progressReportDataset;
					TCComponentTcFile[] relatedTcFiles = progress_report.getTcFiles();

					for (int iFileCnt = 0; iFileCnt < relatedTcFiles.length;iFileCnt++)
					{     
						String fileName = relatedTcFiles[iFileCnt].toString ();
						int extensionIndex = fileName.lastIndexOf ( '.' );

						if (fileName.substring(extensionIndex).equals(".xml") || fileName.substring(extensionIndex).equals(".XML")) 
						{

							TCComponentTcFile progressReportFile = relatedTcFiles[iFileCnt];
							defaultFile = progressReportFile.getFmsFile();
							break;
						}	
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return defaultFile;
	}
	
}
