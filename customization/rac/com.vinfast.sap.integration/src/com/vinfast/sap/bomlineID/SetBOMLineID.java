package com.vinfast.sap.bomlineID;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SetBOMLineID extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub

		TCComponentBOMLine[] selectedLines = null;

		ISelection selection = HandlerUtil.getCurrentSelection( event );
		
		InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents(selection);

		try {
			
			if(selectedObjects.length != 0) {

				selectedLines = new TCComponentBOMLine[selectedObjects.length];
				
				int count = 0;

				for(InterfaceAIFComponent line : selectedObjects) {

					selectedLines[count] = (TCComponentBOMLine)line;
					count++;
				}

				if(selectedLines != null) {

					TCComponentBOMLine parentLine = selectedLines[0].getCachedParent();

					TCComponentItem parentItem =  parentLine.getItem();
					
					TCSession session = parentItem.getSession();
					
					DataManagementService dmService = DataManagementService.getService(session);

					dmService.getProperties(new TCComponent[] {parentItem}, new String[] {"item_id","IMAN_master_form"});

					TCComponent form = parentItem.getRelatedComponent("IMAN_master_form");

					TCComponentGroupMember groupMember = UIGetValuesUtility.getAccessor(session);

					boolean result = UIGetValuesUtility.checkAccess(session, groupMember, form);

					if(result == false) {

						MessageBox.post("No \"Write\" access on parent", "No \"Write\" access on parent master form. Get Ownership or check login group"
								+ " on the parent part and retry.", "Error", MessageBox.ERROR);
						return null;
					}

					String currentBOMLineID = getCurrentBOMLineID(dmService, parentLine, form);

					if(currentBOMLineID.equals("") ==  false) {

						for(TCComponentBOMLine traverseLine : selectedLines){

							String manufCode = traverseLine.getProperty("VF4_manuf_code");

							if(manufCode.equals("Output") == false) {

								String type = traverseLine.getItem().getDisplayType();

								if((type.equals("VF Item") || type.equals("Item") || type.equals("Structure Part")) == false) {

									int value = Integer.parseInt(currentBOMLineID);

									value++;

									String latestID = Integer.toString(value);

									for(int i=latestID.length(); i<4 ;i++){

										latestID = "0"+latestID;
									}

									boolean code = UIGetValuesUtility.setProperty(dmService, traverseLine, "VF4_bomline_id", latestID);

									if(code == true) {

										currentBOMLineID = latestID;
									}
								}
							}

						}

						boolean retCode = UIGetValuesUtility.setProperty(dmService, form, "serial_number", currentBOMLineID);

						if(retCode == true) {

							parentLine.window().save();
						}
					}
				}
				
			}else {
				MessageBox.post("No object is selected.", "Please select one BOMLine and set BOMline ID.", "Set BOMLine...", MessageBox.INFORMATION);
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public String getCurrentBOMLineID(DataManagementService dmService, TCComponentBOMLine parentLine, TCComponent form) {

		String currentValue = "";

		try {

			dmService.getProperties(new TCComponent[] {form}, new String[] {"serial_number"});
			
			currentValue = form.getProperty("serial_number");

			if(currentValue.trim().equals("")) {

				if(parentLine.getChildrenCount() != 0) {

					AIFComponentContext[] childLines = parentLine.getChildren();
					
					TCComponent[] childComponents = new TCComponent[childLines.length];

					for(int i=0 ; i<childLines.length ; i++) {
						
						childComponents[i] = (TCComponent)childLines[i].getComponent();
					}

					if(childComponents != null) {

						dmService.getProperties(childComponents, new String[] {"VF4_bomline_id","VF4_manuf_code"});
						
						ArrayList<Integer> collectionID = new ArrayList<Integer>();

						for(TCComponent child : childComponents) {

							String lineID = child.getProperty("VF4_bomline_id");
							String manufCode = child.getProperty("VF4_manuf_code");
							if(lineID.trim().length()!=0 && (!manufCode.equals("Output"))) {
								collectionID.add(Integer.parseInt(lineID));
							}
						}

						if(collectionID.isEmpty() == false) {

							Collections.sort(collectionID);
							int value = collectionID.get(collectionID.size()-1);
							currentValue =  Integer.toString(value);
						}else {
							currentValue = "0";
						}
					}
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return currentValue;
	}


}
