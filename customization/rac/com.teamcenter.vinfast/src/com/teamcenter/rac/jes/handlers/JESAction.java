package com.teamcenter.rac.jes.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.model.ModelObject;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class JESAction extends AbstractHandler {
	/**
	 * The constructor.
	 */
	TCSession session = null ;
	BufferedWriter output = null;
	FileReader fr = null;

	TCComponentItemRevision operationRev = null ;
	public JESAction() {
	}


	public boolean validateSelection(InterfaceAIFComponent[] aifComp)
	{
		if (aifComp.length != 1)
		{
			return false ;
		}

		if (!(aifComp[0] instanceof TCComponentBOPLine))
		{
			return false ;
		}

		if (!(aifComp[0].getType().equals("Mfg0BvrOperation"))) {
			return false ;
		}

		return true ;
	}
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */

	public ArrayList<TCComponent> whereUsedMEOP(TCComponent itemRev , String Returnobjectype){
		ArrayList<TCComponent> usedMEOP = new ArrayList<TCComponent>();
		TCComponent[]  usedParts = null;
		try {
			usedParts = itemRev.whereUsed(TCComponent.WHERE_USED_ALL);
			for(int i=0;i<usedParts.length;i++){

				if(usedParts[i].getTypeComponent().getType().equals(Returnobjectype)){
					usedMEOP.add(usedParts[i]);
				}
			}
		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return usedMEOP;
	}

	TCComponentItem searchWS(String itemID )
	{
		TCComponentItem item = null ;
		try
		{
			SavedQueryService QRservices = SavedQueryService.getService(session);

			//System.out.println("#################################got session and service################################################");
			FindSavedQueriesCriteriaInput qry[]=new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey=new  FindSavedQueriesCriteriaInput();
			System.out.println("gor finder query");
			String  name[]={"Item..."};
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
			qc.entries= new String[] {"Item ID"};
			qc.values=new String[] {itemID};
			qc_v[0]=qc;

			//System.out.println("########################################got find qryyyyyyyy ################################################");
			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

			SavedQueryResults[] results=responce.arrayOfResults;

			if(results[0].numOfObjects!=0){

				//isPartSourceable =  false ;
				TCComponent[] tcComp = results[0].objects ;
				item = (TCComponentItem)tcComp[0] ;
			}
			else
			{
				System.out.println("NO Object FOUND");
			}
			//System.out.println("##############result"+folder[0].toString());
		}
		catch (ServiceException e) {
			// TODO Auto-generated catch block
		}
		return item ;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		Hashtable<String, String> OperationList = new Hashtable<String, String>() ;
		Hashtable<String, String> equipmentList = new Hashtable<String, String>() ;
		Vector<Hashtable<String, String>> equipmentVec = new Vector<Hashtable<String, String>>() ;
		Hashtable<String, String> materialList = new Hashtable<String, String>() ;
		Vector<Hashtable<String, String>> materialVec = new Vector<Hashtable<String, String>>() ;

		session = (TCSession) AIFUtility.getCurrentApplication().getSession() ;
		InterfaceAIFComponent[] aifComp = AIFUtility.getCurrentApplication().getTargetComponents() ;


		boolean is_valid = validateSelection(aifComp) ;
		if (!is_valid)
		{
			MessageBox.post("Not a Valid Selection. Please select valid object.", "Error", MessageBox.ERROR);
			return null;
		}

		//GET OPERATION PROPERTIES
		TCComponentBOPLine operationLine = (TCComponentBOPLine) aifComp[0] ;

		try
		{
			operationRev = operationLine.getItemRevision() ;
			String operationRevNumber = operationRev.getProperty("item_revision_id");
			String OperationName = operationLine.getProperty("bl_item_object_name") + " / " + operationRev.getProperty("vf5_viet_description");
			String OperationID = operationLine.getProperty("bl_rev_awp0Item_item_id");

			AIFComponentContext[] context = operationLine.getRelated("bl_me_activity_lines") ;
			TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine)context[0].getComponent() ;
			String Time = activityLine.getProperty("al_activity_duration_time") ;
			double d = Double.parseDouble(Time) ;
			DecimalFormat df = new DecimalFormat("#.##");
			String analysisTime = df.format(d) ;

			String stationObjectStr = operationLine.getProperty("bl_formatted_parent_name") ; 

			String Station = "" ;
			String StationName = "" ;
			try
			{
				String[] tempStr = stationObjectStr.split("/") ;
				Station = tempStr[0] ;
			}
			catch(Exception e)
			{
				Station = stationObjectStr ;
			}

			ArrayList<TCComponent> targetWorkstations = whereUsedMEOP(operationRev , "Mfg0MEProcStatnRevision");

			if (targetWorkstations.size() != 0)
			{
				for(TCComponent targetWorkstation:targetWorkstations){

					if (targetWorkstation.getProperty("item_id").equals(Station))
					{
						StationName = targetWorkstation.getProperty("object_name") ;
						break ;
					}
				}
			}

			OperationList.put("ID", OperationID) ;
			OperationList.put("Name", OperationName) ;
			OperationList.put("Station", StationName) ;
			OperationList.put("Time", analysisTime) ;
			OperationList.put("RevisionID", operationRevNumber);

			System.out.println();
			TCComponent[] allMaterials = operationLine.getRelatedComponents("Mfg0all_material") ;
			TCComponent[] allEquipments = operationLine.getRelatedComponents("Mfg0used_equipment") ;

			for (int inx = 0 ; inx < allMaterials.length ; inx ++){

				materialList = new Hashtable<String, String>() ;

				if (allMaterials[inx].getProperty("bl_quantity").equals(""))
				{
					materialList.put("Quantity","1") ;
				}
				else
				{
					materialList.put("Quantity",allMaterials[inx].getProperty("bl_quantity")) ;

				}
				materialList.put("ID",allMaterials[inx].getProperty("bl_item_item_id")) ;
				materialList.put("Name",allMaterials[inx].getProperty("bl_item_object_name")) ;
				String torque = allMaterials[inx].getProperty("VF3_torque_info") ;
				materialList.put("Torque", torque) ;

				materialVec.add(materialList) ;
			}

			for (int inx = 0 ; inx < allEquipments.length ; inx ++){

				equipmentList = new Hashtable<String, String>() ;
				equipmentList.put("Name",allEquipments[inx].getProperty("bl_item_object_name")) ;
				equipmentList.put("Bit",allEquipments[inx].getProperty("VF4_bit")) ;
				equipmentList.put("Socket",allEquipments[inx].getProperty("VF4_socket")) ;
				equipmentList.put("Extension",allEquipments[inx].getProperty("VF4_extension")) ;
				equipmentVec.add(equipmentList) ;
			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		if(materialVec.size() < 43){

			populateReport(OperationList , materialVec , equipmentVec , operationRev) ;

		}else{

			MessageBox.post("Material count must be less than templete rows (i.e 40). Please contact system admin.", "Error", MessageBox.ERROR);
			return null;
		}
		return null;
	}

	public String setOutputFilePath()
	{
		String filePath = null ;
		try
		{
			String env1 = getfmshome();
			int ocr = env1.lastIndexOf("\\");
			String env2 = env1.substring(0, ocr);
			filePath = env2  ;
		}
		catch(Exception ex)
		{
			return null ;
		}

		return filePath ;
	}

	public String getfmshome()
	{
		String fmshome = new String();

		try
		{
			Process p = null;
			Runtime r = Runtime.getRuntime();
			String OS = System.getProperty("os.name").toLowerCase();

			if ( (OS.indexOf("nt") > -1) || (OS.indexOf("windows") > -1 ) )
			{
				p = r.exec( "cmd.exe /c set FMS_HOME" );
			}

			BufferedReader br = new BufferedReader ( new InputStreamReader( p.getInputStream() ) );
			String line;
			while( (line = br.readLine()) != null ) 
			{
				int idx = line.indexOf( '=' );
				fmshome = line.substring( idx+1 );
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return fmshome;

	}

	private void populateReport(Hashtable<String, String> operationList,
			Vector<Hashtable<String, String>> materialVec,
			Vector<Hashtable<String, String>> equipmentVec , TCComponentItemRevision revObj) {
		// TODO Auto-generated method stub

		String filePath = null ;
		String originalFile = null ;
		String outPutFile = null ;

		try {

			filePath = setOutputFilePath() ;
			
			if (materialVec.size() < 5 && equipmentVec.size() < 5)
			{
				originalFile = filePath + "\\JES\\JES_Input_file\\JES.xlsx" ;
			}
			else
			{
				originalFile = filePath + "\\JES\\JES_Input_file\\JES_Extend.xlsx" ;
			}
			outPutFile = filePath + "\\JES\\" + revObj.getProperty("object_name") + ".xlsx" ;

			//DUPLICATE A FILE
			FileSystem system = FileSystems.getDefault();
			Path original = system.getPath(originalFile);
			Path target = system.getPath(outPutFile);

			// Throws an exception if the original file is not found.
			Files.copy(original, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException | TCException ex) {
			ex.printStackTrace();
			System.out.println("ERROR");
		}

		File temp = new File(outPutFile) ;
		if (!temp.exists())
		{
			return ;
		}
		InputStream fileOut = null ;
		XSSFWorkbook workbook = null ;

		try{

			fileOut = new FileInputStream(new File(outPutFile));
			workbook = new XSSFWorkbook(fileOut) ;
			XSSFSheet worksheet = workbook.getSheet("JES");

			//HEADER ROW
			XSSFRow headerRow = null ;
			if (worksheet.getRow(1) == null)
			{
				headerRow = worksheet.createRow(1) ;
			}
			else
			{
				headerRow = worksheet.getRow(1);
			}

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");  
			LocalDateTime now = LocalDateTime.now();  

			XSSFCell dateCell = null ;
			dateCell = headerRow.getCell(CellReference.convertColStringToIndex("S")) ;
			if (dateCell == null)
			{
				dateCell = headerRow.createCell(CellReference.convertColStringToIndex("S")) ;
			}
			dateCell.setCellValue(dtf.format(now));

			//Person ROW
			XSSFRow personRow = null ;
			if (worksheet.getRow(2) == null)
			{
				personRow = worksheet.createRow(2) ;
			}
			else
			{
				personRow = worksheet.getRow(2);
			}
			XSSFCell personCell = null ;
			personCell = personRow.getCell(CellReference.convertColStringToIndex("S")) ;
			if (personCell == null)
			{
				personCell = personRow.createCell(CellReference.convertColStringToIndex("S")) ;
			}
			personCell.setCellValue(session.getUserName());

			//thanhpn6 add revision number
			XSSFRow revRow = null;
			if (worksheet.getRow(2) == null)
			{
				revRow = worksheet.createRow(2) ;
			}
			else
			{
				revRow = worksheet.getRow(2);
			}
			XSSFCell revCell = null;
			revCell = revRow.getCell(CellReference.convertColStringToIndex("N")) ;
			if (revCell == null)
			{
				revCell = revRow.createCell(CellReference.convertColStringToIndex("N")) ;
			}
			revCell.setCellValue(operationList.get("RevisionID"));
			//thanhpn6 finish add revision number
			

			//MODEL ROW

			XSSFCell modelCell = null ;
			modelCell = personRow.getCell(CellReference.convertColStringToIndex("I")) ;
			if (modelCell == null)
			{
				modelCell = personRow.createCell(CellReference.convertColStringToIndex("I")) ;
			}

			if(operationRev.getProperty("item_id").substring(0,1).equals("S"))
			{
				modelCell.setCellValue("SEDAN");
			}else if (operationRev.getProperty("item_id").substring(0,1).equals("U"))
			{
				modelCell.setCellValue("SUV");
			}

			//Operation ROW
			XSSFRow operationRow = null ;
			if (worksheet.getRow(10) == null)
			{
				operationRow = worksheet.createRow(10) ;
			}
			else
			{
				operationRow = worksheet.getRow(10);
			}

			XSSFCell nameCell = operationRow.getCell(CellReference.convertColStringToIndex("A")) ;
			nameCell.setCellValue(operationList.get("Name").toUpperCase());
			//nameCell.setCellStyle(style);

			XSSFCell IDCell = operationRow.getCell(CellReference.convertColStringToIndex("G")) ;
			IDCell.setCellValue(operationList.get("ID").toUpperCase());
			//IDCell.setCellStyle(style);

			XSSFCell timeCell = operationRow.getCell(CellReference.convertColStringToIndex("I")) ;
			timeCell.setCellValue(operationList.get("Time").toUpperCase());
			//timeCell.setCellStyle(style);

			XSSFCell stationCell = operationRow.getCell(CellReference.convertColStringToIndex("J")) ;
			stationCell.setCellValue(operationList.get("Station").toUpperCase());
			//stationCell.setCellStyle(style);

			//MATERIAL LIST
			for (int i = 0 ; i < materialVec.size() ; i ++){

				Hashtable<String, String> materialTable = materialVec.get(i) ;
				XSSFRow materialRow = null ;
				if (i < 6)
				{
					materialRow = worksheet.getRow(24 + i);
				}
				else
				{
					materialRow = worksheet.getRow(24 + 3 + i);
				}

				XSSFCell serialCell = materialRow.getCell(CellReference.convertColStringToIndex("A")) ;
				serialCell.setCellValue( i + 1);

				XSSFCell mNameCell = materialRow.getCell(CellReference.convertColStringToIndex("B")) ;
				mNameCell.setCellValue(materialTable.get("ID").toUpperCase());
				//mNameCell.setCellStyle(style);

				XSSFCell mIDCell = materialRow.getCell(CellReference.convertColStringToIndex("E")) ;
				mIDCell.setCellValue(materialTable.get("Name").toUpperCase());
				//mIDCell.setCellStyle(style);

				XSSFCell mTimeCell = materialRow.getCell(CellReference.convertColStringToIndex("J")) ;
				mTimeCell.setCellValue(materialTable.get("Quantity").toUpperCase());
				//mTimeCell.setCellStyle(style);

				XSSFCell mTorqueCell = materialRow.getCell(CellReference.convertColStringToIndex("K")) ;
				mTorqueCell.setCellValue(materialTable.get("Torque").toUpperCase());

			}

			//EQUIPMENT LIST
			for (int i = 0 ; i < equipmentVec.size() ; i ++){

				Hashtable<String, String> equipmentTable = equipmentVec.get(i) ;
				XSSFRow equipmentRow = null;

				if (i < 6)
				{
					equipmentRow = worksheet.getRow(24 + i);
				}
				else
				{
					equipmentRow = worksheet.getRow(24 + 3 + i);
				}

				String Name = equipmentTable.get("Name") ;
				String Bit = equipmentTable.get("Bit") ;
				String Socket = equipmentTable.get("Socket") ;
				String Extension = equipmentTable.get("Extension") ;
				
				String finalString = "" ;
				finalString = Name;
				if ( !Bit.equals("") )
				{
					finalString = finalString + " | B-" + Bit;
				}
				if ( !Socket.equals(""))
				{
					finalString = finalString + " | S- " + Socket ;
				}
				if ( !Extension.equals(""))
				{
					finalString = finalString + " | Ex- " + Extension ;
				}

				//finalString = Name + " | B-" + Bit + " | S-" + Socket  + " |Ex-" + Extension ;  

				XSSFCell eIDCell = equipmentRow.getCell(CellReference.convertColStringToIndex("O")) ;
				eIDCell.setCellValue(finalString.toUpperCase());

			}

			fileOut.close();
			FileOutputStream output_file = new FileOutputStream(new File(outPutFile)) ;
			workbook.write(output_file);
			output_file.close();
			createDataset(outPutFile , operationRev) ;
			File file = new File(outPutFile) ;
			file.delete() ;
			
			MessageBox.post("Created Successfully", "Success", MessageBox.INFORMATION);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			MessageBox.post("ERROR", "Please contact Admin", MessageBox.ERROR);
		}finally{
			try
			{
				fileOut.close();
				File file = new File(outPutFile) ;
				file.delete() ;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public TCComponentDataset getLogo()
	{
		return null ;
	}

	public TCComponentDataset getImageDataset(TCComponentItemRevision selected_rev){
		TCComponentDataset image =  null;
		try {
			AIFComponentContext[] datasets = selected_rev.getRelated("IMAN_specification");

			for(int i =0 ;i<datasets.length; i++){

				if(datasets[i].getComponent().getType().equalsIgnoreCase("JPEG") || datasets[i].getComponent().getType().equalsIgnoreCase("JPG")){
					image = (TCComponentDataset)datasets[i].getComponent();
					break;
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return image;
	}

	public List <String> exportDatasetsToDirectory(TCComponentDataset dscp, String exportDir) throws Exception
	{
		List<String> exportedFiles = new ArrayList<String>();

		TCComponentTcFile[] files = dscp.getTcFiles();
		for(int l = 0; l < files.length; l++)
		{

			TCComponentTcFile tcf = (TCComponentTcFile) files[l];
			File f = tcf.getFmsFile();
			InputStream in = new FileInputStream(f);

			String exportfilename = exportDir + "\\" + tcf;
			OutputStream out = new FileOutputStream(exportfilename);
			exportedFiles.add(exportfilename);

			byte[] buffer = new byte[1024];
			int bytesRead;

			while ((bytesRead = in.read(buffer)) >= 0) {
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			in.close();
		}
		return exportedFiles;
	}

	private void createDataset( String fileName  , TCComponentItemRevision operationRev) {

		System.out.println( "filename :- " + fileName );
		String [] result = fileName.split( "\\." );
		// Last str = file extension
		@SuppressWarnings("unused")
		String fileExtn = result [ result.length -1 ];

		String datasetType = "MSExcelX";

		System.out.println( "datasetType :- " + datasetType);

		NamedReferenceContext namedReferenceContext[] = null;
		String namedRef = null;	  

		try 
		{

			TCComponentDatasetDefinitionType tcDatasetDefinitionType = ( TCComponentDatasetDefinitionType )session.getTypeComponent( "DatasetType" );
			TCComponentDatasetDefinition datasetDefinition = tcDatasetDefinitionType.find( datasetType );

			namedReferenceContext = datasetDefinition.getNamedReferenceContexts();
			namedRef = namedReferenceContext[0].getNamedReference();

			String [] type = { namedRef };

			TCComponentDatasetType datasetTypeComponent = ( TCComponentDatasetType ) session.getTypeComponent( "Dataset" );

			/*
			 * Create the Dataset with given name and description. 
			 * create( datasetName, datasetDesc, dataset_id, dataset_rev, datasetType, toolUsed );
			 */
			TCComponentDataset newDataset = datasetTypeComponent.create( operationRev.getProperty("object_name") , operationRev.getProperty("item_id"), datasetDefinition.toString() );

			newDataset.refresh();

			/* import selected file on newly created dataset with the Specification relation */
			newDataset.setFiles( new String [] { fileName} ,type );

			/* attach dataset to target element with IMAN_specification */
			operationRev.add ( "IMAN_specification", newDataset ); 


		} catch (TCException e) 
		{

			e.printStackTrace();
		}


	}
}
