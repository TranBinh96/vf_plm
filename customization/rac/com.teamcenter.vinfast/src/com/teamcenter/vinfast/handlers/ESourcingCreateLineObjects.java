package com.teamcenter.vinfast.handlers;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentFolderType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentUnitOfMeasure;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOut;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.query.SavedQueryService;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.rac.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.rac.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.model.ModelObject;

public class ESourcingCreateLineObjects {

	TCSession session = null;
	AIFDesktop desktop = null;
	String itemType = null;
	String itemRevisionType = null;
	String itemName = null;
	String itemDesc = null;
	String itemId = null;
	String revID = null;
	TCComponentForm form = null;

	TCComponentItem newItem = null;
	TCComponentItem newDocumentItem = null;
	TCComponentItemRevision newItemRev = null;
	Vector<Hashtable<String, String>> ValueVector = null;
	String classID = "";
	Hashtable<String, String> itemPropertyMap = null;
	Hashtable<String, Date> datePropertyMap = null;
	StringBuffer outPutMessage = null;
	Vector<Hashtable<String, String>> inspectionDocumentMap = null ;
	String documentProductCode = "" ;
	TCComponent[] uom_value_tags = null;

	ESourcingCreateLineObjects(TCSession theSession, Hashtable<String, String> data_table , Hashtable<String, Date> date_table, TCComponent[] uom_tags)
	{
		this.session = theSession;
		itemPropertyMap = data_table ;
		datePropertyMap = date_table ;
		uom_value_tags = uom_tags;

		createGenericOperation();
	}


	private void createGenericOperation()
	{
		TCComponent folder = null;
		TCComponent clFolder = null ;
		TCComponentUser user = null;
		TCComponentGroup group = null;
		TCComponent uomvalue = null;
	
		try
		{
			// POPULATE UOM MAP
			/*
			 *  UOM Properties
			 */
			String uom =  itemPropertyMap.get("uom_tag");
			
			itemPropertyMap.remove("uom_tag");
			
			if(!uom.equals("")&& uom_value_tags.length != 0){
				
				for(int i=0; i<uom_value_tags.length ;i++){
					TCComponentUnitOfMeasure unit = (TCComponentUnitOfMeasure)uom_value_tags[i];
					if(unit.getProperty("symbol").equals(uom)){
						uomvalue = uom_value_tags[i];
					}
				}
			}
			
			String name = itemPropertyMap.get("object_name");

			DataManagementService dms = DataManagementService.getService(session) ;
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = "VF4_line_item"; //Item BO
			itemDef.data.stringProps.put("object_name", name); //Custom Property
			itemDef.data.tagProps.put("uom_tag", uomvalue);

			//Item revision
			CreateInput itemRevisionDef = new CreateInput();
			itemRevisionDef.boName = "VF4_line_itemRevision"; //Item rev BO
			itemRevisionDef.stringProps.put("item_revision_id", "01");

			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] {
					itemRevisionDef
			});
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			CreateOut[] createOutResp = response.output ;
			TCComponent[] compOnent = createOutResp[0].objects ;
			TCComponentItemRevision targetRev = null ;
			try
			{
				for (int i = 0; i < compOnent.length ; i ++)
				{
					if (compOnent[i] instanceof TCComponentItemRevision )
					{
						targetRev = (TCComponentItemRevision)compOnent[i] ;
					}
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			// Create Items in Cl home Folder
			String clName = itemPropertyMap.get("vf4_cl");
			if(clName!=null){
				clFolder = getBuyerSrcFolder(clName);
				if(clFolder == null){
					user = getUser(clName);
					group = user.getLoginGroup();
					clFolder = createSrcFolder(user,group);
				}

			}else{
				user = session.getUser();
				clFolder = getBuyerSrcFolder(clName);
			}

			TCComponentFolder clSourcingFolder = (TCComponentFolder)clFolder;
			clSourcingFolder.add("contents", targetRev);


			String userName = itemPropertyMap.get("vf4_buyer");
			if(!clName.equals(userName)){
				if(userName!=null){
					folder = getBuyerSrcFolder(userName);
					if(folder == null){
						user = getUser(userName);
						group = user.getLoginGroup();
						folder = createSrcFolder(user,group);
					}
	
				}else{
					user = session.getUser();
					folder = getBuyerSrcFolder(userName);
				}
	
				TCComponentFolder sourcingFolder = (TCComponentFolder)folder;
				sourcingFolder.add("contents", targetRev);
			}
			new ESourcingSaveProperties(itemPropertyMap, datePropertyMap , session, targetRev) ;

		}
		catch (Exception ex)
		{
			System.out.println("\n PASTE OPERATION FAILED = " + ex);
			this.outPutMessage.append("\nPaste Operation failed, Please contact system administrator");
		}
	}

	public TCComponentUser getUser(String userName){

		TCComponentUser user = null;
		String userID = userName.substring(userName.indexOf("(")+1,userName.indexOf(")"));
		try {
			TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");
			user = userType.find(userID);
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}

	public TCComponentFolder createSrcFolder(TCComponentUser user, TCComponentGroup group){

		TCComponentFolder sourcingFolder = null;
		try{
			TCComponentFolderType folderType = (TCComponentFolderType)session.getTypeComponent("Folder");
			sourcingFolder = folderType.create("SOURCING", "", "Folder");
			sourcingFolder.changeOwner(user, group);
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TCComponentFolder home  = user.getHomeFolder();
		//home.add("contents", sourcingFolder);
		//sourcingFolder.add("contents", targetRev);
		return sourcingFolder;
	}

	public TCComponent getBuyerSrcFolder(String userName){

		TCComponent src_folder = null;
		try
		{

			SavedQueryService QRservices = SavedQueryService.getService(session);

			FindSavedQueriesCriteriaInput qry[]=new FindSavedQueriesCriteriaInput[1];
			FindSavedQueriesCriteriaInput qurey=new  FindSavedQueriesCriteriaInput();
			String  name[]={"General..."};
			String desc[]={""};
			qurey.queryNames=name;
			qurey.queryDescs=desc;
			qurey.queryType=0;

			qry[0]=qurey;

			FindSavedQueriesResponse responce1 = QRservices.findSavedQueries(qry);
			ModelObject[]result=responce1.savedQueries;

			SavedQueryInput qc=new SavedQueryInput();
			SavedQueryInput qc_v[]=new SavedQueryInput[1];

			qc.query=(TCComponentQuery) result[0];
			qc.entries= new String[] {"Name","Type","Owning User"};
			qc.values=new String[] {"SOURCING","FOLDER",userName};
			qc_v[0]=qc;

			System.out.println("########################################got find qryyyyyyyy ################################################");
			ExecuteSavedQueriesResponse responce = QRservices.executeSavedQueries(qc_v);

			SavedQueryResults[] results=responce.arrayOfResults;

			TCComponent[] folder =  results[0].objects;
			if(folder.length!=0){

				src_folder =  folder[0];
			}
		}
		catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return src_folder;
	}
}