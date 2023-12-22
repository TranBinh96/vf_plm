package com.vinfast.api.common.extensions;

import com.teamcenter.services.strong.cad._2007_01.StructureManagement;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.cad.StructureManagementService;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateOrUpdateRelativeStructureResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.ExpandPSAllLevelsInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.ExpandPSAllLevelsPref;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.ExpandPSAllLevelsResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.ExpandPSAllLevelsOutput;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.ExpandPSOneLevelResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.GetRevisionRulesResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.RevisionRuleInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.RelationAndTypesFilter;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.RevisionRuleConfigInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CloseBOMWindowsResponse;
import com.teamcenter.services.strong.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructureInfo2;
import com.teamcenter.services.strong.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructurePref2;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.SaveBOMWindowsResponse;
import com.teamcenter.services.strong.cad._2019_06.StructureManagement.CreateWindowsInfo3;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.*;
import com.vinfast.connect.client.AppXSession;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

public class BOMExtension {
    public static RevisionRule getRevisionRule(String ruleName) throws Exception {
        StructureManagementService sm = StructureManagementService.getService(AppXSession.getConnection());
        GetRevisionRulesResponse response = sm.getRevisionRules();

        for (RevisionRuleInfo ruleInfo : response.output) {
            TCCommonExtension.loadObject(new String[]{ruleInfo.revRule.getUid()}, AppXSession.getConnection());
            if (ruleInfo.revRule.get_object_name().equals(ruleName)) {
                return ruleInfo.revRule;
            }
        }
        return null;
    }

    public static LinkedList<BOMLine> expandBOM150(String UID, RevisionRule revisionRule, boolean isOneLevel, int levelBreak) throws Exception {
        Item item = null;
        ModelObject itemRevs = null;
        ModelObject[] bomViews = null;
        ServiceData serviceData = DataManagementService.getService(AppXSession.getConnection()).loadObjects(new String[]{UID});
        if (serviceData.getPlainObject(0) != null) {
            item = (Item) serviceData.getPlainObject(0);
        }

        if (item == null)
            throw new Exception("BOM.traverseBOM item is null.");

        itemRevs = ItemRevisionExtension.getLatestRevision(item, AppXSession.getConnection());
        bomViews = item.get_bom_view_tags();

        CreateWindowsInfo3 bomWinInfo150 = new CreateWindowsInfo3();
        bomWinInfo150.item = item;
        bomWinInfo150.itemRev = (ItemRevision) itemRevs;
        bomWinInfo150.bomView = (PSBOMView) bomViews[0];
        bomWinInfo150.revRuleConfigInfo = new RevisionRuleConfigInfo();
        bomWinInfo150.revRuleConfigInfo.revRule = revisionRule;

        CreateBOMWindowsResponse createBOMWindowsResponse = StructureManagementService.getService(AppXSession.getConnection()).createOrReConfigureBOMWindows(new CreateWindowsInfo3[]{bomWinInfo150});
        if (createBOMWindowsResponse.serviceData.sizeOfPartialErrors() > 0)
            throw new Exception("BOM.traverseBOM returned a partial error. " + TCCommonExtension.hanlderServiceData(createBOMWindowsResponse.serviceData));

        com.teamcenter.services.strong.cad._2007_01.StructureManagement.ExpandPSAllLevelsInfo exp_all = new ExpandPSAllLevelsInfo();
        exp_all.parentBomLines = new BOMLine[]{createBOMWindowsResponse.output[0].bomLine};
        exp_all.excludeFilter = "None";

        ExpandPSAllLevelsPref exp_pref = new ExpandPSAllLevelsPref();
        exp_pref.expItemRev = false;
        exp_pref.info = new RelationAndTypesFilter[0];

        ExpandPSAllLevelsResponse expandPSAllLevelsResponse = StructureManagementService.getService(AppXSession.getConnection()).expandPSAllLevels(exp_all, exp_pref);
        if (expandPSAllLevelsResponse.serviceData.sizeOfPartialErrors() > 0) {
            throw new Exception("BOM.traverseBOM returned a partial error. " + TCCommonExtension.hanlderServiceData(expandPSAllLevelsResponse.serviceData));
        }

        ExpandPSAllLevelsOutput[] bomOutput = expandPSAllLevelsResponse.output;

        LinkedList<BOMLine> allBOMLineInWindow = new LinkedList<>();
        if(isOneLevel)
            expandBOMLinesOneLevel(allBOMLineInWindow, (BOMLine) bomOutput[bomOutput.length - 1].parent.bomLine, levelBreak);

        return allBOMLineInWindow;
    }

    private static void expandBOMLinesOneLevel(LinkedList<BOMLine> outBomLines, final BOMLine rootLine, int levelBreak) {
        outBomLines.add(rootLine);
        if(levelBreak != 0){
            try {
                String level = rootLine.getPropertyDisplayableValue("bl_level_starting_0");
                if(StringExtension.isInteger(level)) {
                    if(Integer.parseInt(level) == levelBreak) {
                        return;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        StructureManagementService structureService = StructureManagementService.getService(AppXSession.getConnection());

        ExpandPSOneLevelInfo levelInfo = new ExpandPSOneLevelInfo();
        levelInfo.parentBomLines = new BOMLine[]{rootLine};
        levelInfo.excludeFilter = "None";

        ExpandPSOneLevelPref levelPref = new ExpandPSOneLevelPref();
        levelPref.expItemRev = false;
        levelPref.info = new RelationAndTypesFilter[0];

        ExpandPSOneLevelResponse levelResp = structureService.expandPSOneLevel(levelInfo, levelPref);

        if (levelResp.output.length > 0) {
            for (ExpandPSOneLevelOutput levelOut : levelResp.output) {
                for (ExpandPSData psData : levelOut.children) {
                    expandBOMLinesOneLevel(outBomLines, psData.bomLine, levelBreak);
                }
            }
        }
    }
    public static ArrayList openBomWindow(ItemRevision parentItemRev) throws Exception {
        ArrayList bomWindowAndParentLine = new ArrayList(2);
        CreateWindowsInfo3[] createBOMWindowsInfo = new CreateWindowsInfo3[1];
        createBOMWindowsInfo[0] = new CreateWindowsInfo3();
        createBOMWindowsInfo[0].itemRev = parentItemRev;
        StructureManagementService cadSMService = StructureManagementService.getService(AppXSession.getConnection());

        CreateBOMWindowsResponse response = cadSMService.createOrReConfigureBOMWindows(createBOMWindowsInfo);
        if(response.serviceData.sizeOfPartialErrors() > 0){
            throw new Exception("[openBomWindow]: " + TCCommonExtension.hanlderServiceData(response.serviceData));
        }
        bomWindowAndParentLine.add(response.output[0].bomWindow);
        bomWindowAndParentLine.add(response.output[0].bomLine);

        return bomWindowAndParentLine;
    }
    public static void saveBomWindow(BOMWindow bomWindow) throws Exception{
        StructureManagementService structureManagementService = StructureManagementService.getService(AppXSession.getConnection());
        SaveBOMWindowsResponse saveBOMWindowsResponse = structureManagementService.saveBOMWindows(new BOMWindow[]{bomWindow});
        if(saveBOMWindowsResponse.serviceData.sizeOfPartialErrors() > 0){
            throw new Exception("[saveBomWindow]: " + TCCommonExtension.hanlderServiceData(saveBOMWindowsResponse.serviceData));
        }
    }
    public static void closeBomWindow(BOMWindow bomWindow) throws Exception{
        StructureManagementService structureManagementService = StructureManagementService.getService(AppXSession.getConnection());
        CloseBOMWindowsResponse closeBOMWindowsResponse = structureManagementService.closeBOMWindows(new BOMWindow[]{bomWindow});
        if(closeBOMWindowsResponse.serviceData.sizeOfPartialErrors() > 0){
            throw new Exception("[closeBomWindow]: " + TCCommonExtension.hanlderServiceData(closeBOMWindowsResponse.serviceData));
        }
    }
    public static void createBomViewRevision(ItemRevision revision, Boolean precise) throws Exception {
        CreateOrUpdateRelativeStructureInfo2[] info = new CreateOrUpdateRelativeStructureInfo2[1];
        info[0] = new CreateOrUpdateRelativeStructureInfo2();
        info[0].lastModifiedOfBVR = Calendar.getInstance();
        info[0].parent = revision;
        info[0].precise = precise;

        CreateOrUpdateRelativeStructurePref2 prefs = new CreateOrUpdateRelativeStructurePref2();
        prefs.cadOccIdAttrName = null;
        prefs.overwriteForLastModDate = true;

        StructureManagementService smService = StructureManagementService.getService(AppXSession.getConnection());
        CreateOrUpdateRelativeStructureResponse createOrUpdateRelativeStructureResponse = smService.createOrUpdateRelativeStructure(info, "PR4D_cad", true, prefs);
        if (createOrUpdateRelativeStructureResponse.serviceData.sizeOfPartialErrors() > 0) {
            throw new Exception("[createBomViewRevision]: " + TCCommonExtension.hanlderServiceData(createOrUpdateRelativeStructureResponse.serviceData));
        }
    }

    public static StructureManagement.CreateBOMWindowsResponse openNewBomWindow(Item item, ItemRevision itemRevision, RevisionRule revisionRule) {
        StructureManagement.CreateBOMWindowsInfo bomWindownInfo = new StructureManagement.CreateBOMWindowsInfo();
        if (item != null) {
            bomWindownInfo.item = item;
        } else {
            bomWindownInfo.itemRev = itemRevision;
        }
        bomWindownInfo.revRuleConfigInfo = new StructureManagement.RevisionRuleConfigInfo();
        bomWindownInfo.revRuleConfigInfo.revRule = revisionRule;
        StructureManagement.CreateBOMWindowsResponse resCreateBOM = StructureManagementService.getService(AppXSession.getConnection()).createBOMWindows(new StructureManagement.CreateBOMWindowsInfo[] { bomWindownInfo });
        if (resCreateBOM.serviceData.sizeOfPartialErrors() > 0) {
            TCCommonExtension.hanlderServiceData(resCreateBOM.serviceData);
            return null;
        }
        return resCreateBOM;
    }

    public static ItemRevision getItemRevisionByRule(Item item, RevisionRule revisionRule) {
        try {
            StructureManagement.CreateBOMWindowsResponse response = openNewBomWindow(item, null, revisionRule);
            if (response != null) {
                BOMWindow bomWindow = (response.output[0]).bomWindow;
                BOMLine bomline = (response.output[0]).bomLine;
                StructureManagementService.getService(AppXSession.getConnection()).closeBOMWindows(new BOMWindow[] { bomWindow });
                return (ItemRevision)bomline.get_bl_revision();
            }
            return (ItemRevision)item.get_item_revision();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
