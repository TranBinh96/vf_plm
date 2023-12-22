package com.teamcenter.vinfast.qcheck;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.GetNextIdsResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.InfoForNextId;
import com.vf.utils.Query;
import com.vf.utils.TCExtension;

public class QCheckRequest_Handler extends AbstractHandler {
	private TCSession session;
	QCheckRequest_Dialog dlg;
	public QCheckRequest_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			
			dlg = new QCheckRequest_Dialog(new Shell(), session);
			dlg.create();
			
			//Init Data
			String[] revisionRuleDataForm = TCExtension.GetRevisionRules(session);
			//Init UI
			dlg.cbRevisionRule.setItems(revisionRuleDataForm);
			dlg.cbRevisionRule.setText("VINFAST_WORKING_RULE");
			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createNewItem();
				}
			});
			
			dlg.open();
		} 
		catch (Exception e) {
			
		}
		return null;
	}
	
	private void createNewItem() {
		if(!validateRequired()) {
			dlg.setMessage("Please input all required information", IMessageProvider.WARNING);
			return;
		}
		
		TableItem[] parts = dlg.tblParts.getItems();
		List<TCComponent> partsList = new LinkedList<TCComponent>();
		if(parts.length != 0) {
			for(int pCount = 0; pCount < parts.length ; pCount++) {
				String partName = parts[pCount].getText(0);
				String partType = parts[pCount].getText(1);

				String[] split = partName.split("-", 2);
				String[] splitID = split[0].split("/");
				
				LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
				inputQuery.put("Item ID", splitID[0].trim());
				inputQuery.put("Revision", splitID[1].trim());
				inputQuery.put("Type", partType);
				TCComponent[] item_search = Query.queryItem(session, inputQuery, "Item Revision...");

				if (item_search != null) {
					partsList.add(item_search[0]);	
				}
			}
		}
		
		try {
			DataManagementService dms = DataManagementService.getService(session) ;
			InfoForNextId nextID = new InfoForNextId();
			nextID.propName = "item_id";
			nextID.typeName = "VF4_QCA_Request";
			nextID.pattern = "NNNNNNN";

			GetNextIdsResponse IDReponse = dms.getNextIds(new InfoForNextId[] {nextID});
			String[] ids = IDReponse.nextIds;
			
			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = "VF4_QCA_Request"; 
			itemDef.data.stringProps.put("item_id", ids[0]);
			itemDef.data.stringProps.put("vf4_status", "REQUEST");
			itemDef.data.stringProps.put("object_desc", dlg.txtDescription.getText());
			itemDef.data.stringProps.put("vf4_revision_rule", dlg.cbRevisionRule.getText());
			
			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });
			if (response.serviceData.sizeOfPartialErrors() == 0) {
				TCComponent cfgContext = response.output[0].objects[0];
				cfgContext.setRelated("IMAN_requirement", partsList.toArray(new TCComponent[0]));
				try {
					session.getUser().getNewStuffFolder().add("contents", cfgContext);
				} 
				catch (TCException e1) {
					MessageBox.post("Exception: " + e1, "ERROR", MessageBox.ERROR);
				}
				resetDialog();
				dlg.setMessage("Created successfully, new item ( " + ids[0] + " ) has been copied to your Newstuff folder", IMessageProvider.INFORMATION);
			}
			else {
				ServiceData serviceData = response.serviceData;
				for (int i = 0; i < serviceData.sizeOfPartialErrors(); i++) {
					for (String msg : serviceData.getPartialError(i).getMessages()) {
						MessageBox.post("Exception: " + msg, "ERROR", MessageBox.ERROR);
					}
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Boolean validateRequired() {
		if(dlg.tblParts.getItemCount() <= 0) return false;
		return true;
	}
	
	private void resetDialog() {
		dlg.txtDescription.setText("");
		dlg.tblParts.removeAll();
	}
}
