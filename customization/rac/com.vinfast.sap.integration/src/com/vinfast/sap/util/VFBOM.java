/************************************************************************************************************************
 * Class : VFBOM			Date:14-07-2023			Description: All function related to Structure Management
 * 
 * 
 ************************************************************************************************************************/
package com.vinfast.sap.util;

import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CloseBOMWindowsResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RevisionRuleConfigInfo;
import com.teamcenter.services.rac.cad._2019_06.StructureManagement.CreateWindowsInfo3;

public class VFBOM {
	
	public static CreateBOMWindowsOutput[] openBOM(StructureManagementService SMService, TCComponent[] itemRevisions,TCComponentRevisionRule revisionRule) {

		CreateBOMWindowsOutput[] BOMOutput = null;
		try {

			RevisionRuleConfigInfo revRuleConfig = new RevisionRuleConfigInfo();
			revRuleConfig.revRule = revisionRule;
			
			CreateWindowsInfo3[] BOMWindowInfo = new CreateWindowsInfo3[itemRevisions.length];
			for (int i = 0; i < itemRevisions.length; i++) {
				TCComponentItemRevision itemRevision = (TCComponentItemRevision) itemRevisions[i];
				BOMWindowInfo[i] = new CreateWindowsInfo3();
				BOMWindowInfo[i].clientId = itemRevision.getProperty(PropertyDefines.ITEM_ID);
				BOMWindowInfo[i].itemRev = itemRevision;
				BOMWindowInfo[i].item = itemRevision.getItem();
				if(revisionRule != null) {
					BOMWindowInfo[i].revRuleConfigInfo = revRuleConfig;
				}
			}

			CreateBOMWindowsResponse BOMWindows = SMService.createOrReConfigureBOMWindows(BOMWindowInfo);
			ServiceData serviceDate = BOMWindows.serviceData;
			if (serviceDate.sizeOfPartialErrors() > 0) {
				MessageBox.post(SoaUtil.buildErrorMessage(serviceDate), "Error", MessageBox.ERROR);
			} else {
				BOMOutput = BOMWindows.output;
			}

		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return BOMOutput;
	}
	
	public static void closeBOM(StructureManagementService SMService, CreateBOMWindowsOutput[] bomWindowOutputs) {
		
		if(bomWindowOutputs != null) {
			TCComponentBOMWindow[] bomWindows = new TCComponentBOMWindow[bomWindowOutputs.length];
			for(int i=0; i<bomWindowOutputs.length ; i++) {
				bomWindows[i] = bomWindowOutputs[i].bomWindow;
			}
			CloseBOMWindowsResponse bomWindowResponse = SMService.closeBOMWindows(bomWindows);
			ServiceData serviceResult = bomWindowResponse.serviceData;
			if(serviceResult.sizeOfPartialErrors() > 0) {
				MessageBox.post(SoaUtil.buildErrorMessage(serviceResult), "Error", MessageBox.ERROR);
			}
		}
	}

}
