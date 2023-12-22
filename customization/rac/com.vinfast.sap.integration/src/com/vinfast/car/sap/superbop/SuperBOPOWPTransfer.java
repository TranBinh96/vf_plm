package com.vinfast.car.sap.superbop;

import java.util.HashMap;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vinfast.sap.dialogs.BOMBOPDialog;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SuperBOPOWPTransfer {

	TCSession session;
	public SuperBOPOWPTransfer(TCSession connection) {
		// TODO Auto-generated constructor stub
		session = connection;
	}

	public HashMap<String, String> transferProblemItems(BOMBOPDialog transferDlg, TCComponent MEOPRevision) {

		HashMap<String, String> BOP_Values = null;

		try {
			
			String OperationType = MEOPRevision.getPropertyDisplayableValue("vf4_operation_type") ;
			
			if(((OperationType.equals("") || OperationType.equals("NA")) == false) && hasChildItems(MEOPRevision) == false){

			BOP_Values = new HashMap<String, String>();
			BOP_Values.put("MCN", transferDlg.getMCN());
			BOP_Values.put("MAINGROUP", "");
			BOP_Values.put("SUBGROUP", "");
			BOP_Values.put("PLATFORM", transferDlg.getModel());
			BOP_Values.put("MODELYEAR", transferDlg.getYear());
			BOP_Values.put("SAPPLANT", transferDlg.getPlant());
			BOP_Values.put("GROUPDESCRIPTION","");
			BOP_Values.put("BOMLINEID","");
			BOP_Values.put("ACTION", "D");
			BOP_Values.put("MESBOPINDICATOR", "Y");
			BOP_Values.put("WORKSTATION", MEOPRevision.getPropertyDisplayableValue("vf3_transfer_to_sap"));
			BOP_Values.put("LINESUPPLYMETHOD", "JIS");
			BOP_Values.put("BOPID", MEOPRevision.getPropertyDisplayableValue("item_id"));
			BOP_Values.put("OPTION", "");
			BOP_Values.put("FAMILY_ADDR", "");
			BOP_Values.put("L_R_HAND", "");
			BOP_Values.put("REVISION", MEOPRevision.getPropertyDisplayableValue("item_revision_id"));
			new UIGetValuesUtility();
			BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());
			
			}
		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BOP_Values;
	}

	public HashMap<String, String> transferSolutionItems(BOMBOPDialog transferDlg, TCComponent MEOPRevision) {

		HashMap<String, String> BOP_Values = null;

		try {


			BOP_Values = new HashMap<String, String>();
			BOP_Values.put("MCN", transferDlg.getMCN());
			BOP_Values.put("MAINGROUP", "");
			BOP_Values.put("SUBGROUP", "");
			BOP_Values.put("PLATFORM", transferDlg.getModel());
			BOP_Values.put("MODELYEAR", transferDlg.getYear());
			BOP_Values.put("SAPPLANT", transferDlg.getPlant());
			BOP_Values.put("GROUPDESCRIPTION","");
			BOP_Values.put("BOMLINEID","");
			BOP_Values.put("ACTION", "A");
			BOP_Values.put("MESBOPINDICATOR", "Y");
			BOP_Values.put("WORKSTATION", getWorkStationID((TCComponentBOMLine)MEOPRevision, "bl_rev_object_name"));
			BOP_Values.put("LINESUPPLYMETHOD", "JIS");
			BOP_Values.put("BOPID", MEOPRevision.getPropertyDisplayableValue("bl_item_item_id"));
			BOP_Values.put("OPTION", "");
			BOP_Values.put("FAMILY_ADDR", "");
			BOP_Values.put("L_R_HAND", "");
			BOP_Values.put("REVISION", MEOPRevision.getPropertyDisplayableValue("bl_rev_item_revision_id"));
			new UIGetValuesUtility();
			BOP_Values.put("SEQUENCE", UIGetValuesUtility.getSequenceID());


		} catch (NotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BOP_Values;
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

	public boolean hasChildItems(TCComponent operation)
	{
		boolean hasItems = false; 
		
		try
		{
			AIFComponentContext[] operationChildren = operation.getRelated("PR4D_cad");
			
			if(operationChildren != null) {

				for (AIFComponentContext childObject : operationChildren) {
	
					String object_Type = childObject.getComponent().getType();
	
					if (object_Type.equals("Mfg0MENCToolRevision") == false) {
	
						return true;
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return hasItems ;
	}
}
