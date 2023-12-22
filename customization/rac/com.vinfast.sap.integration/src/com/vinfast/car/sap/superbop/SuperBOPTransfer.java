package com.vinfast.car.sap.superbop;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.services.ReadXMLInputFile;
import com.vinfast.sap.util.UIGetValuesUtility;
import com.vinfast.url.SAPURL;

public class SuperBOPTransfer extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	public TCComponentBOMLine transferBOPItem(TCSession session,TCComponentBOMLine child_BOM, TCComponentBOMLine child_BOP, HashMap<String, String> bomLineValues,int count, String logFolder){

		new Logger();
		ReadXMLInputFile readFile = new ReadXMLInputFile() ;
		TCComponentBOMLine operation = null;

		boolean toProcess = true;

		try {

			TCComponentItemRevision itemRevision = child_BOP.getItemRevision();

			operation = getOperation(child_BOP);

			if(operation == null){
				return operation;
			}

			String fileName = readFile.getXML(itemRevision);

			if(fileName == null){

				MessageBox.post("Parser file name unknown. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
				return null;

			}else {

				InputStream traverse_File = readFile.loadXML(fileName);

				if(traverse_File == null) {

					MessageBox.post("Property file parser error. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
					return null;

				}

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(traverse_File);
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName(SAPURL.SUP_BOP_HEADER_TAG);

				for (int temp = 0; temp < nList.getLength(); temp++) 
				{
					Node nNode = nList.item(temp);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) 
					{
						Element tagElement = (Element) nNode;
						NodeList dataList = tagElement.getChildNodes() ;

						for (int jnx = 0 ; jnx < dataList.getLength() ; jnx ++)
						{
							Node dataNode = dataList.item(jnx);

							if (dataNode.getNodeType() == Node.ELEMENT_NODE) 
							{
								Element dataElement = (Element) dataNode;

								NodeList attributes = dataElement.getChildNodes() ;

								for (int knx = 0 ; knx < attributes.getLength() ; knx ++){

									Node attribute = attributes.item(knx);

									if (attribute.getNodeType() == Node.ELEMENT_NODE)
									{
										Element attributeElement = (Element)attribute ;

										String sap_tag = attributeElement.getAttribute("erp_tag");
										String plm_tag = attributeElement.getAttribute("plm_tag");
										String type_tag = attributeElement.getAttribute("objectType");
										String relation = attributeElement.getAttribute("relation");
										String is_mandatory = attributeElement.getAttribute("mandatory");

										String propTagValue = "";
										if(bomLineValues.containsKey(sap_tag) == false){
											if(relation.equalsIgnoreCase("HASHMAP")){

												propTagValue = bomLineValues.get(plm_tag);

											}else if(relation.equalsIgnoreCase("PARENT")){

												if(sap_tag.equals("WORKSTATION")){
													propTagValue = getWorkStationID(operation, plm_tag);


												}else{
													TCComponentBOMLine parentBOMLine = (TCComponentBOMLine) operation.getReferenceProperty("bl_parent");

													if(parentBOMLine.getType().equalsIgnoreCase(type_tag)){

														propTagValue = parentBOMLine.getProperty(plm_tag).trim();
													}
												}

											}else if(relation.equalsIgnoreCase("BOMLINE")){

												if(type_tag.equals("Operation Revision")){
													propTagValue = operation.getProperty(plm_tag).trim();

													if(sap_tag.equals("MESBOPINDICATOR")){

														if (propTagValue.equals("") || propTagValue.equalsIgnoreCase("NA"))
														{
															propTagValue =  "N";
														}else{
															propTagValue =  "Y";
														}

													}

													if(sap_tag.equals("LINESUPPLYMETHOD") && propTagValue.equals("")){

														propTagValue =  "JIS";
													}

													if(sap_tag.equals("SELECTIONSTRING")){

														if(propTagValue.equals("") ==  false){

															propTagValue = propTagValue.substring(0, propTagValue.length());

															Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(propTagValue);
															while(m.find()) {
																propTagValue = propTagValue.replace(m.group(1),bomLineValues.get("PLATFORM")+"_"+bomLineValues.get("MODELYEAR"));
															}
														}
													}
												}
												else{
													propTagValue = child_BOM.getProperty(plm_tag).trim();
												}
											}else{

												propTagValue = "";
											}

											bomLineValues.put(sap_tag, propTagValue.toUpperCase()) ;

											if(is_mandatory.equals("Yes") && propTagValue.equals(""))
											{
												Logger.writeResponse(new String[] {Integer.toString(count),bomLineValues.get("PARTNO"),bomLineValues.get("BOMLINEID"),sap_tag+" value is empty in mandatory field.","-","Error"},logFolder,"BOM");
												count++;
												toProcess = false;

											}
										}

									}
								}
							}
						}
					}
				}
			}
			if(toProcess ==  false){
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return operation;
	}

	public boolean transferOperationItem(TCSession session, TCComponentBOMLine operation, HashMap<String, String> bomLineValues, String logFolder){

		new Logger();
		ReadXMLInputFile readFile = new ReadXMLInputFile() ;
		boolean toProcess = true;

		try {

			TCComponentItemRevision itemRevision = operation.getItemRevision();
			String fileName = readFile.getXML(itemRevision);

			if(fileName == null){

				MessageBox.post("Parser file name unknown. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
				return false;

			}else {

				InputStream traverse_File = readFile.loadXML(fileName);

				if(traverse_File == null) {

					MessageBox.post("Property file parser error. Please contact Teamcenter Admin", "Error", MessageBox.ERROR);
					return false;

				}
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(traverse_File);
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName(SAPURL.SUP_BOP_HEADER_TAG);

				for (int temp = 0; temp < nList.getLength(); temp++) 
				{
					Node nNode = nList.item(temp);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) 
					{
						Element tagElement = (Element) nNode;
						NodeList dataList = tagElement.getChildNodes() ;

						for (int jnx = 0 ; jnx < dataList.getLength() ; jnx ++)
						{
							Node dataNode = dataList.item(jnx);

							if (dataNode.getNodeType() == Node.ELEMENT_NODE) 
							{
								Element dataElement = (Element) dataNode;

								NodeList attributes = dataElement.getChildNodes() ;

								for (int knx = 0 ; knx < attributes.getLength() ; knx ++){

									Node attribute = attributes.item(knx);

									if (attribute.getNodeType() == Node.ELEMENT_NODE)
									{
										Element attributeElement = (Element)attribute ;

										String sap_tag = attributeElement.getAttribute("erp_tag");
										String plm_tag = attributeElement.getAttribute("plm_tag");
										String type_tag = attributeElement.getAttribute("objectType");
										String relation = attributeElement.getAttribute("relation");
										String is_mandatory = attributeElement.getAttribute("mandatory");

										String propTagValue = "";

										if(relation.equalsIgnoreCase("HASHMAP")){

											propTagValue = bomLineValues.get(plm_tag);

										}else if(relation.equalsIgnoreCase("PARENT")){

											String action = bomLineValues.get("ACTION");
											if(sap_tag.equals("WORKSTATION")){
												if(action.equals("A")) {
													propTagValue = getWorkStationID(operation, plm_tag);
												}else {
													propTagValue = getDeletedWorkStation(operation.getItemRevision());
												}

											}else{
												TCComponentBOMLine parentBOMLine = (TCComponentBOMLine) operation.getReferenceProperty("bl_parent");
												if(parentBOMLine == null) {
													propTagValue = "";
												}else {
													if(parentBOMLine.getType().equalsIgnoreCase(type_tag)){

														propTagValue = parentBOMLine.getProperty(plm_tag).trim();
													}
												}
											}

										}else if(relation.equalsIgnoreCase("BOMLINE")){

											if(type_tag.equals("Operation Revision")){
												propTagValue = operation.getProperty(plm_tag).trim();

												if(sap_tag.equals("MESBOPINDICATOR")){

													if (propTagValue.equals("") || propTagValue.equalsIgnoreCase("NA"))
													{
														propTagValue =  "N";
													}else{
														propTagValue =  "Y";
													}

												}

												if(sap_tag.equals("LINESUPPLYMETHOD") && propTagValue.equals("")){

													propTagValue =  "JIS";
												}

												if(sap_tag.equals("SELECTIONSTRING")){

													if(propTagValue.equals("") ==  false){

														propTagValue = propTagValue.substring(0, propTagValue.length());

														Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(propTagValue);
														while(m.find()) {
															propTagValue = propTagValue.replace(m.group(1),bomLineValues.get("PLATFORM")+"_"+bomLineValues.get("MODELYEAR"));
														}
													}
												}
											}
											else{
												propTagValue = operation.getProperty(plm_tag).trim();
											}
										}else{

											if(sap_tag.equals("SEQUENCE")){
												new UIGetValuesUtility();
												propTagValue = UIGetValuesUtility.getSequenceID();
											}else {
												propTagValue = "";
											}
										}

										if(bomLineValues.containsKey(sap_tag) == false){

											bomLineValues.put(sap_tag, propTagValue.toUpperCase()) ;
										}
										if(is_mandatory.equals("Yes") && sap_tag.equals("WORKSTATION") && propTagValue.equals(""))
										{

											String[] response = new String[]{bomLineValues.get("PARTNO"),"WORKSTATION is missing","Error"};
											Logger.writeResponse(response, logFolder, "OWPTRANSFER");
											toProcess = false;
											break;

										}
									}
								}
							}
						}
					}
				}
			}
			if(toProcess ==  false){
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return toProcess;
	}

	public TCComponentBOMLine getOperation(TCComponentBOMLine bopChild){

		TCComponentBOMLine operationList = null;
		try{

			TCComponentBOMLine operation = (TCComponentBOMLine)bopChild.getReferenceProperty("bl_parent");

			if(operation !=null && operation.getItemRevision().getType().equals("MEOPRevision")){
				return operation;
			}
		}catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return operationList;
	}

	public String getWorkStationID(TCComponentBOMLine operation, String plm_tag) {
		// TODO Auto-generated method stub
		String workstationID = "" ;
		TCComponentBOMLine procLine = null;
		TCComponentBOMLine shopLine = null;
		TCComponentBOMLine topLine = null;
		try {

			TCComponentItemRevision operationRevision = operation.getItemRevision();

			if(operationRevision.getType().equals("MEOPRevision")){

				TCComponentBOMLine workstation = (TCComponentBOMLine) operation.getReferenceProperty("bl_parent");
				TCComponentItemRevision workstationRevision = workstation.getItemRevision();

				if(workstationRevision.getType().equals("Mfg0MEProcStatnRevision")){

					String workStationName = workstation.getProperty(plm_tag);//PT-01LH
					String split_StationName[] = workStationName.split("-");
					String processLine = split_StationName[0].trim();//PT
					String workStatName = split_StationName[1].trim();//01LH

					procLine = (TCComponentBOMLine)workstation.getReferenceProperty("bl_parent");
					String procLineName = procLine.getProperty(plm_tag).substring(0, 2).trim();//PT

					if(procLineName.equals(processLine)){

						shopLine = (TCComponentBOMLine)procLine.getReferenceProperty("bl_parent");//PS
						String shopName = shopLine.getProperty(plm_tag).substring(0, 2).trim();

						topLine = (TCComponentBOMLine)shopLine.getReferenceProperty("bl_parent");
						String plantName = topLine.getProperty(plm_tag).substring(0, 4).trim();
						workstationID = plantName+"_"+shopName+procLineName+workStatName;
					}
				}

			}

		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return workstationID;
	}

	private String getDeletedWorkStation(TCComponentItemRevision operationRev) {
		// TODO Auto-generated method stub
		String workstationID = "";

		try {

			workstationID = operationRev.getProperty("vf3_transfer_to_sap");

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return workstationID;
	}

	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, LinkedHashMap<String,String>> changeInWorkStation(LinkedHashMap<String,LinkedHashMap<String,String>> old_items, LinkedHashMap<String,LinkedHashMap<String,String>> new_items){

		LinkedHashMap<String,LinkedHashMap<String,String>> oldBOP = (LinkedHashMap<String,LinkedHashMap<String,String>>) old_items.clone();
		LinkedHashMap<String,LinkedHashMap<String,String>> newBOP = (LinkedHashMap<String,LinkedHashMap<String,String>>) new_items.clone();
		LinkedHashMap<String,LinkedHashMap<String,String>> differentWorkStation = new LinkedHashMap<String,LinkedHashMap<String,String>>();

		for ( String key : newBOP.keySet()) {

			try {

				LinkedHashMap<String, String> newLine = newBOP.get(key);
				LinkedHashMap<String, String> oldLine = oldBOP.get(key);

				if(oldLine != null){
					
					String newLineOperation = newLine.get("BOPID");
					String oldLineOperation = oldLine.get("BOPID");
					
					String newLineWS = newLine.get("WORKSTATION");
					String oldLineWS = oldLine.get("WORKSTATION");

					if((newLineOperation.equals(oldLineOperation)) == false || (newLineWS.equals(oldLineWS))== false){
						differentWorkStation.put(key, newLine);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return differentWorkStation;
	}

	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, LinkedHashMap<String,String>> changeInOperationRevision(LinkedHashMap<String,LinkedHashMap<String,String>> old_items, LinkedHashMap<String,LinkedHashMap<String,String>> new_items){

		LinkedHashMap<String,LinkedHashMap<String,String>> oldBOP = (LinkedHashMap<String,LinkedHashMap<String,String>>) old_items.clone();
		LinkedHashMap<String,LinkedHashMap<String,String>> newBOP = (LinkedHashMap<String,LinkedHashMap<String,String>>) new_items.clone();
		LinkedHashMap<String,LinkedHashMap<String,String>> differentOperationRevision = new LinkedHashMap<String,LinkedHashMap<String,String>>();

		for ( String key : newBOP.keySet()) {
			try {

				LinkedHashMap<String, String> newLine = newBOP.get(key);
				LinkedHashMap<String, String> oldLine = oldBOP.get(key);

				if(oldLine != null){
					
					String newLineOperation = newLine.get("BOPID");
					String oldLineOperation = oldLine.get("BOPID");
					
					String newLineWS = newLine.get("WORKSTATION");
					String oldLineWS = oldLine.get("WORKSTATION");
					
					String newRevision = newLine.get("REVISION");
					String oldRevision = oldLine.get("REVISION");

					if((newLineOperation.equals(oldLineOperation)==true) && (newLineWS.equals(oldLineWS)==true) && (newRevision.equals(oldRevision) == false)){
						differentOperationRevision.put(key, newLine);
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return differentOperationRevision;
	}

}
