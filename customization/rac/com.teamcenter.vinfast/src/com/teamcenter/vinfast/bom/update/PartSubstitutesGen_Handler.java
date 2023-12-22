package com.teamcenter.vinfast.bom.update;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.tcservices.TcBOMService;
import com.teamcenter.rac.kernel.tcservices.TcResponseHelper;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.model.PartSubstitutesPasteModel;
import com.vinfast.sc.utilities.PropertyDefines;

import antlr.collections.List;

public class PartSubstitutesGen_Handler extends AbstractHandler {

    private PartSubstitutesPaste_Dialog dlg = null;

    public static final String FIND_NO= "bl_sequence_no";
    public static final String SUBSTITUTE_GROUP_ID = "VF4_sub_group_id";
    public static final String SUBSTITUTE_LIST = "bl_substitute_list";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // TODO Auto-generated method stub
        ISelection selection		= HandlerUtil.getCurrentSelection( event );
        InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents( selection );

        try {
            HashSet<TCComponent> lines = traverseBOMStructure(selectedObjects);
            if(lines.size() != 0) {
                TCComponentBOMLine parentLine = (TCComponentBOMLine) selectedObjects[0];
                TCComponentBOMLine parent = (TCComponentBOMLine) parentLine.getReferenceProperty(PropertyDefines.BOM_PARENT);
                String groupID = generateGroupID(parent);
                for (TCComponent line : lines) {
                    line.setProperty(SUBSTITUTE_GROUP_ID, groupID);
                }
                MessageBox.post("Group ID(s) set successfully.", "Success", MessageBox.INFORMATION);
            }
        } catch (Exception e) {
            MessageBox.post("Errors below occured when generating Group ID.Please contact Teamcenter Administrator.\n" + e.getMessage(), "Error", MessageBox.ERROR);
            e.printStackTrace();
        }
        return null;
    }
    private HashSet<TCComponent> traverseBOMStructure(InterfaceAIFComponent[] BOMLine) throws Exception {
        String propFindID ="";
        int checkSubtitutes = 0;
        HashSet<TCComponent> lines = new HashSet<TCComponent>();
        for (int iLine = 0; iLine < BOMLine.length; iLine++) {
            TCComponent line = (TCComponent) BOMLine[iLine];
            String groupID = line.getPropertyDisplayableValue(SUBSTITUTE_GROUP_ID).trim();
            String subtitute_List =  line.getPropertyDisplayableValue(SUBSTITUTE_LIST).trim();
            String propFindIDNew= line.getPropertyDisplayableValue(FIND_NO).trim();

            propFindID = propFindID.isEmpty() ? propFindIDNew :  propFindID;
            if(propFindID.compareTo(propFindIDNew) == 0 && groupID.isEmpty())
            {
                if(subtitute_List.length() > 0)
                    checkSubtitutes++;
                lines.add(line);
            }else
                checkSubtitutes = 2;
        }
        if(checkSubtitutes > 1) {
            lines.clear();
            MessageBox.post("Find ID must be the same and Substitute Group ID is empty.", "Error", MessageBox.ERROR);
        }
        return lines;
    }

    private void throwError(String err) {
        if (!err.isEmpty()) {
            MessageBox.post(err, "Error", MessageBox.ERROR);
        }
    }

    private static String generateGroupID(TCComponentBOMLine subgroup) {
        String retId = "";
        TcResponseHelper responseHelper;
        try {
            responseHelper = TcBOMService.expand(subgroup.getSession(), subgroup);
            TCComponent[] childrens = responseHelper.getReturnedObjects();
            HashSet<String> existedID = new HashSet<String>();
            for (TCComponent children : childrens) {
                existedID.add(children.getProperty(PropertyDefines.BOM_PART_GROUPID));
            }

            Random random = new Random();
            String generatedString = "";
            int leftLimit = 48; // numeral '0'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 2;
            do {
                generatedString = random.ints(leftLimit, rightLimit + 1)
                        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
            } while (existedID.contains(generatedString));
            retId = generatedString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retId;
    }


}