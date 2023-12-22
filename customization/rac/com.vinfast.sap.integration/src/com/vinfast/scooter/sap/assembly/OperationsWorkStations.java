package com.vinfast.scooter.sap.assembly;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class OperationsWorkStations {

	LinkedHashMap<String, ArrayList<String>> workstations = null;
	
	public OperationsWorkStations() {
		// TODO Auto-generated constructor stub
		workstations = new LinkedHashMap<String, ArrayList<String>>();
	}
	
	public String generateWorkStationID(TCComponentBOMLine operation, String plm_tag) {
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
					String workStatName = "";
					if(workStationName.contains("-")) {

						String split_StationName[] = workStationName.split("-");
						workStatName = split_StationName[1].trim();//01LH
					}else {
						workStatName = workStationName;
					}
					procLine = (TCComponentBOMLine)workstation.getReferenceProperty("bl_parent");
					String procLineName = procLine.getProperty(plm_tag).substring(0, 2).trim();//PT
					
					String shopName;
					String plantName;
					shopLine = (TCComponentBOMLine)procLine.getReferenceProperty("bl_parent");//PS
					
					if(shopLine.getItemRevision().getType().equals("Mfg0MEPlantBOPRevision")){
						
						shopName = procLineName;
						plantName = shopLine.getProperty(plm_tag).substring(0, 4).trim();
					}else {
						
						shopName = shopLine.getProperty(plm_tag).substring(0, 2).trim();
						topLine = (TCComponentBOMLine)shopLine.getReferenceProperty("bl_parent");
						plantName = topLine.getProperty(plm_tag).substring(0, 4).trim();
					}

					workstationID = plantName+"_"+shopName+procLineName+workStatName;
				}

			}

		} catch (TCException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return workstationID;
	}
	
	public void setWorkStationsID(String operation, String workStation) {
		
		if(workstations.containsKey(operation)) {
			
			ArrayList<String> ws = workstations.get(operation);
			if(!ws.contains(workStation)) {
				
				ws.add(workStation);
			}
			
		}else {
			
			ArrayList<String> workStations =  new ArrayList<String>();
			workStations.add(workStation);
			workstations.put(operation, workStations);
		}
	}
	
	public ArrayList<String> getWorkStationsID(String operation){
		
		return workstations.get(operation);
	}
}
