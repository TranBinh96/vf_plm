package com.teamcenter.vinfast.handlers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.SelectionHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.tcservices.TcBOMService;
import com.teamcenter.rac.kernel.tcservices.TcResponseHelper;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2007_01.DataManagement.GetItemFromIdPref;
import com.teamcenter.services.rac.core._2009_10.DataManagement;
import com.teamcenter.services.rac.core._2009_10.DataManagement.GetItemFromAttributeInfo;
import com.teamcenter.services.rac.core._2009_10.DataManagement.GetItemFromAttributeResponse;
import com.teamcenter.soaictstubs.ICCTAccessControlService;
import com.teamcenter.soaictstubs.ICCTReservationService;
import com.teamcenter.soaictstubs.booleanSeq_tHolder;

public class POSIDGenerator extends AbstractHandler {

	private static String REL_MASTER_FORM = "IMAN_master_form";
	private static String PROP_ITEM_ID = "bl_item_item_id";
	private static String PROP_POSID = "VL5_pos_id";
	private static String PROP_SERIAL_POSID = "object_desc";
	private static String COUNTER_GENERATOR = "POSID_COUNTER";
	private static String PREVIOUS_COUNTER = "previous_item_id";
	ICCTAccessControlService acl = null;
	String current_BomlineId = "";
	boolean accessError = false;
	
	StringBuilder errorBuilder = null;
	int counter = 0;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection		= HandlerUtil.getCurrentSelection( event );
		InterfaceAIFComponent[] selectedObjects = SelectionHelper.getTargetComponents( selection );
		
		if (selectedObjects.length > 1) {
			MessageBox.post("This feature is supported only for a signle assembly.", "Error", MessageBox.ERROR);
			return null;
		}
		
		TCComponentBOMLine selected_bomline	 = (TCComponentBOMLine) selectedObjects[0];
		TCSession session			= selected_bomline.getSession();
		counter = 1;
		accessError = false;
		boolean isCheckIn = true;
		
		try {
			
			acl = new ICCTAccessControlService(session.getSoaConnection());
			
			ICCTReservationService reserveService = new ICCTReservationService(session.getSoaConnection());
			
			TCComponentBOMWindow window =  selected_bomline.window();

			TCComponentItem counterItem = findItem(session, COUNTER_GENERATOR);
			
			if(counterItem == null) {

				MessageBox.post("POS ID service has issue in Teamcenter. Contact Teamcenter Administrator.", "Error", MessageBox.ERROR);
				
			}else {
				
				booleanSeq_tHolder accessHolder = new booleanSeq_tHolder();
				
				acl.checkPrivileges(counterItem.getUid(), new String[] {"WRITE"}, accessHolder);
				
				boolean values[] = accessHolder.value;
				
				if(values[0] == false) {
					
					MessageBox.post("You dont have access to POS ID service. Contact Teamcenter Administrator to get access.", "Error", MessageBox.ERROR);
					
				}else {
					
					if(counterItem.isCheckedOut() == true) {
						
						MessageBox.post("POS ID generating service is busy. Please try again in a few minutes.", "Error", MessageBox.ERROR);
						
					}else {
						TCComponent master_form = counterItem.getRelatedComponent(REL_MASTER_FORM);

						DataManagementService dataMangementService =  DataManagementService.getService(session);
						
						errorBuilder =  new StringBuilder();

						String currentPOSID = counterItem.getProperty(PROP_SERIAL_POSID);

						if(currentPOSID.equals("") == false){
							
							
							bufferResponse("APPEND", new String[] {"<html>","<body>"}, errorBuilder);
							
							bufferResponse("DETAILS", new String[] {"You do not have \"WRITE\" access to modify below parts."}, errorBuilder);
							
							bufferResponse("HEADER", new String[] {"S.No","Parent","Part Number"}, errorBuilder);

							if(selected_bomline.hasChildren() == true){

								Set<TCComponent> lines = new LinkedHashSet<TCComponent>();
								
								traverseBOMStructure(session, dataMangementService, selected_bomline, lines);
								
								isCheckIn = handleSetPosID(reserveService, counterItem, master_form, lines);
								
								bufferResponse("APPEND", new String[] {"</table>","</body>","</html>"},errorBuilder);

								if(window.isModified() == true) {

									window.save();
									
									if(accessError ==  true) {
										
										SwingUtilities.invokeLater(new Runnable() {

											public void run() {

												try {

													StringViewerDialog viewdialog = new StringViewerDialog(new String[] {errorBuilder.toString()}, false);
													viewdialog.setTitle("Warning");
													viewdialog.setSize(600, 400);
													viewdialog.setLocationRelativeTo(null);
													viewdialog.setVisible(true);

												} catch (Exception e) {

													e.printStackTrace();
												}
											}
										});
										
									}else {
										
										MessageBox.post("POS ID(s) set successfully.", "Success", MessageBox.INFORMATION);
									}
									
									
								}else {
									
									if(accessError ==  true) {
										
										SwingUtilities.invokeLater(new Runnable() {

											public void run() {

												try {

													StringViewerDialog viewdialog = new StringViewerDialog(new String[] {errorBuilder.toString()}, false);
													viewdialog.setTitle("Error");
													viewdialog.setSize(600, 400);
													viewdialog.setLocationRelativeTo(null);
													viewdialog.setVisible(true);

												} catch (Exception e) {

													e.printStackTrace();
												}
											}
										});
									}else {
										MessageBox.post("BOMLine with empty POS ID(s) are filled.", "Information", MessageBox.INFORMATION);
									}
								}

							}else {
								if (!isCheckIn) {reserveService.unReserve(new String[] {counterItem.getUid()}, false); isCheckIn = true;}
								MessageBox.post("Selected BOMLine doesnot have child Parts. Please select part having child(s)", "Error", MessageBox.ERROR);
								return null;
							}

						}else{
							if (!isCheckIn) {reserveService.unReserve(new String[] {counterItem.getUid()}, false); isCheckIn = true;}
							MessageBox.post("Current POS ID is empty on POSID Generator. Please contact Teamcenter Administrator.", "Error", MessageBox.ERROR);
							return null;
						}
						
					}
				}
				
				if (!isCheckIn) {reserveService.unReserve(new String[] {counterItem.getUid()}, false); isCheckIn = true;}
			}
		}	
		catch (Exception e) {
			MessageBox.post("Errors below occured when generating POSIS. Please contact Teamcenter Administrator.\n" + e.getMessage(), "Error", MessageBox.ERROR);
			e.printStackTrace();
		}

		return null;
	
	}

	private boolean handleSetPosID(ICCTReservationService reserveService, TCComponentItem counterItem,
			TCComponent master_form, Set<TCComponent> lines) throws TCException, Exception {
		
		TCComponentType.refresh(Arrays.asList(new TCComponentItem[] { counterItem }));
		reserveService.reserve(new String[] {counterItem.getUid()}, "", "", (short)2);
		boolean isCheckIn = false;
		
		current_BomlineId = counterItem.getProperty(PROP_SERIAL_POSID);
		String previous_Counter = current_BomlineId;
		master_form.setProperty(PREVIOUS_COUNTER, previous_Counter);
		counterItem.setProperty(PROP_SERIAL_POSID, current_BomlineId);
		generateNextPOSID(lines.size());
		counterItem.setProperty(PROP_SERIAL_POSID, current_BomlineId);
		generateNextPOSID(-lines.size());
		reserveService.unReserve(new String[] {counterItem.getUid()}, false);
		isCheckIn = true;
		
		TCComponentType.refresh(Arrays.asList(lines.toArray(new TCComponent[0])));
		for (TCComponent line : lines) {
			//TCComponent line = lines.get(i);
			String propValueVL5 = line.getPropertyDisplayableValue(PROP_POSID).trim();
			String propValueVF3 = line.getPropertyDisplayableValue("VF3_pos_ID").trim();

			if(propValueVL5.length() <= 2){
				try {
					String genPOS = generateNextPOSID(1);
					line.setProperty(PROP_POSID, genPOS );
					
					if (propValueVF3.length() > 2) {
						line.setProperty("VF3_pos_ID", "");
					}
				}catch (Exception e) {
					accessError = true;
					generateNextPOSID(-1);
					bufferResponse("PRINT", new String[] {Integer.toString(getCounter()),line.toString(),line.toString()}, errorBuilder);
				}
			}
		}

		return isCheckIn;
	}

	private TCComponentItem findItem(TCSession session, String ID) {

		TCComponentItem item = null;
		DataManagementService dmService = DataManagementService.getService(session);

		Map<String, String> itemMap = new HashMap<String, String>();
		itemMap.put("item_id", ID);

		GetItemFromAttributeInfo attrInfo = new GetItemFromAttributeInfo();
		attrInfo.itemAttributes = itemMap;

		GetItemFromAttributeResponse response = dmService.getItemFromAttribute(new GetItemFromAttributeInfo[] {attrInfo}, 0, new GetItemFromIdPref());
		DataManagement.GetItemFromAttributeItemOutput[] itemOutput = response.output;
		item = itemOutput[0].item;

		return item;
	}

	private void traverseBOMStructure(TCSession session,DataManagementService dataMangementService, TCComponent BOMLine, Set<TCComponent> lines) throws Exception {
		TCComponent[] returnLines = null;
		TcResponseHelper response = TcBOMService.expand(session, BOMLine);

		returnLines =  response.getReturnedObjects();

		if(returnLines != null ) {
			
			dataMangementService.getProperties(returnLines, new String[] {PROP_ITEM_ID, PROP_POSID, "VF3_pos_ID"});
			TCComponent parentBVR = ((TCComponentBOMLine)BOMLine).getBOMViewRevision();
			for(TCComponent childLine : returnLines) {
				System.out.println(childLine.toString());
				boolean modifyRight = session.getTCAccessControlService().checkPrivilege(parentBVR, "WRITE");
				if (modifyRight) {
					String propValueVL5 = childLine.getPropertyDisplayableValue(PROP_POSID).trim();
					//String propValueVF3 = childLine.getPropertyDisplayableValue("VF3_pos_ID").trim();

					//if(propValueVL5.length() <= 2 || propValueVF3.length() <= 2){
					if(propValueVL5.length() <= 2){
							lines.add(childLine);
					}
				}

				if(childLine.getChildrenCount() > 0) {
					traverseBOMStructure(session, dataMangementService, childLine, lines);
				}
			}
		}
	}

	private String generateNextPOSID(int number) {

		String prefix = current_BomlineId.substring(0,3);
		String previousCounter = current_BomlineId.substring(3,current_BomlineId.length());
		String latestCounter =  String.valueOf(Integer.parseInt(previousCounter)+number);

		for(int i = latestCounter.length(); i<previousCounter.length(); i++) {

			latestCounter = "0"+latestCounter;

		}

		current_BomlineId = prefix+latestCounter;
		
		return current_BomlineId;
	}
	
	private void bufferResponse(String argument, String[] values, StringBuilder strBuilder){

		switch(argument) {
		
		case "APPEND":
			
			for(String info : values) {

				strBuilder.append(info);
			}

			break;
		
		case "DETAILS":
		

			for(String header : values) {

				strBuilder.append("<h2 style=\"color:red\">"+header+"</h2>");
			}

			break;
			
		case "HEADER":	
		
			strBuilder.append("<table align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border= \"1\" style=\"width:100%\"><tr style=\"background-color:#ccffff;\">");

			for(String header : values) {

				strBuilder.append("<th>"+header+"</th>");
			}

			strBuilder.append("</tr>");
			break;
			
		case "PRINT":
			
			strBuilder.append("<tr>");

			strBuilder.append("<td align=\"center\">"+values[0]+"</td>");
			strBuilder.append("<td>"+values[1]+"</td>");
			strBuilder.append("<td>"+values[2]+"</td>");
			strBuilder.append("</tr>");
			
			break;
		}
	}
	
	private int getCounter() {
		return counter++;
	}
}
