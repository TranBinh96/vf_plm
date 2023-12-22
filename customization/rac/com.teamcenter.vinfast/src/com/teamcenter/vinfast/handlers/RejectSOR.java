package com.teamcenter.vinfast.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.vf.dialog.FrameWithOneComboBox;
import com.vf.utils.Query;

import vfplm.soa.common.define.Constant;

public class RejectSOR extends AbstractHandler{
	private final Logger LOGGER;
	private static FrameWithOneComboBox frame 	= null;
	private static TCSession session 				= null;
	private static TCComponentGroup group 			= null;
	private static TCComponentRole role 			= null;
	private static TCComponentUser user 			= null;
	private static String TEMP_DIR;
	private static long startTime = 0;
	private ArrayList<String> processNames;
	private static LinkedHashMap<String, String> exchangerate = new LinkedHashMap<>();
	public RejectSOR() {
		TEMP_DIR = System.getenv("tmp");
		this.LOGGER = Logger.getLogger(this.getClass());
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		TCPreferenceService preferenceService = session.getPreferenceService();
		processNames = new ArrayList<>(Arrays.asList(preferenceService.getStringValues("VF_SOURCING_PROGRAM_2_WF")));
	}
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				group = session.getCurrentGroup();
				role = session.getCurrentRole();
				user = session.getUser();
				if(group.toString().compareToIgnoreCase("dba") != 0 && role.toString().compareToIgnoreCase("DBA") != 0) {
					MessageBox.post("ACCESS DENIED", "Error", MessageBox.ERROR);
					return;
				}
				InterfaceAIFComponent[] aifCompo = AIFUtility.getCurrentApplication().getTargetComponents();
				try {
					if (aifCompo != null && aifCompo.length > 0) {
						for (int i = 0; i < aifCompo.length; i++) {
							TCComponentItemRevision sorRev = (TCComponentItemRevision) aifCompo[i];
							removeSORProcess(sorRev);
							String donorVeh = sorRev.getPropertyDisplayableValue("vf4_car_program");
							StringBuilder listPartID = new StringBuilder();
							/*remove sourcing item*/
							TCComponent[] partRevs = sorRev.getRelatedComponents("EC_solution_item_rel");
							for(int j = 0; j < partRevs.length; j++) {
								TCComponent partRev = partRevs[j];
								String partID = partRev.getPropertyDisplayableValue("item_id");
								if(listPartID.length() > 0) listPartID.append(";");
								listPartID.append(partID);
							}
							removeST(listPartID.toString(), donorVeh);
						}
					} else {
						MessageBox.post("Please Select Design Part.", "Error", MessageBox.ERROR);
					}
				} catch (Exception e) {
					MessageBox.post("Exception: " + e.toString(), "Error", MessageBox.ERROR);
				}
			}
		});
		return null;
	}
	
	private void removeST(String listPartID, String stPrg) {
		LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
		queryInput.put("VF Part Number", listPartID);
		queryInput.put("Sourcing Program", stPrg);
		try {
			TCComponent[] stParts = Query.queryItem(session, queryInput, Constant.Query.SOURCE_PART_QUERY);
			if(stParts != null && stParts.length > 0) {
				for (int i = 0; i < stParts.length; i++) {
					AIFComponentContext[] aif = stParts[i].whereReferenced();
					for (int f = 0; f < aif.length; f++) {
						try {
							TCComponent priObj = (TCComponent) aif[f].getComponent();
							priObj.remove(aif[f].getContext().toString(), stParts[i]);
						} catch (Exception e) {
							continue;
						}
					}
					TCComponentItem stPartItm = ((TCComponentItemRevision)stParts[i]).getItem();
					stPartItm.delete();
				}
			}
			
		} catch (Exception e) {
			MessageBox.post("Exception: " + e.toString(), "Error", MessageBox.ERROR);
		}
	}
	
	private void removeSORProcess(TCComponent sorItemRev) {
		try {
			AIFComponentContext[] aif = sorItemRev.whereReferenced();
			if(aif != null && aif.length > 0) {
				for(int i = 0; i < aif.length; i++) {
					String relType = aif[i].getContext().toString();
					String priType = aif[i].getComponent().getType();
					if(priType.compareToIgnoreCase("EPMTask") == 0) {
						TCComponentTask oldProcess = (TCComponentTask)aif[i].getComponent();
						String rootTmp = oldProcess.getName();
						String relStatus = oldProcess.getPropertyDisplayableValue("release_status_attachments");
						if(processNames.contains(rootTmp) && relStatus.contains("VF Released")) {
							TCComponent[] targetObjs = oldProcess.getRelatedComponents("Fnd0EPMTarget");
							oldProcess.remove("Fnd0EPMTarget", targetObjs);
						}
					}
				}
			}
		} catch (TCException | NotLoadedException e) {
			MessageBox.post("Exception: " + e.toString(), "Error", MessageBox.ERROR);
		}
	}
}
