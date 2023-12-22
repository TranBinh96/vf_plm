package com.teamcenter.vinfast.handlers;

import java.util.LinkedHashMap;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vf.dialog.FrameWithOneComboBox;
import com.vf.utils.Query;

import vfplm.soa.common.define.Constant.VFItem;
import vfplm.soa.common.define.Constant.VFWorkspaceObject;

public class CalculatePartFinalCost extends AbstractHandler{
	private final Logger LOGGER;
	private static FrameWithOneComboBox frame 	= null;
	private static TCSession session 				= null;
	private static TCComponentGroup group 			= null;
	private static TCComponentRole role 			= null;
	private static TCComponentUser user 			= null;
	private static String TEMP_DIR;
	private static long startTime = 0;
	private static LinkedHashMap<String, String> exchangerate = new LinkedHashMap<>();
	public CalculatePartFinalCost() {
		TEMP_DIR = System.getenv("tmp");
		this.LOGGER = Logger.getLogger(this.getClass());
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		
		exchangerate.put("USD_VND", "23500");
		exchangerate.put("USD_EUR", "0.87");
		exchangerate.put("USD_CNY", "6.927");
		exchangerate.put("USD_RMB", "6.927");
		exchangerate.put("USD_AED", "3.67");
		exchangerate.put("USD_CAD", "1.38");
		exchangerate.put("USD_GBP", "0.765");
		exchangerate.put("USD_INR", "75.038");
		exchangerate.put("USD_SGD", "1.375");
		exchangerate.put("USD_MYR", "4.222");
		exchangerate.put("USD_JPY", "105.957");
		exchangerate.put("USD_BRL", "5.320");
		exchangerate.put("USD_THB", "31.100");
		exchangerate.put("USD_AUD", "1.400");
		exchangerate.put("USD_KRW", "1108.62");
	}
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				group = session.getCurrentGroup();
				role = session.getCurrentRole();
				user = session.getUser();
				
				InterfaceAIFComponent[] aifCompo = AIFUtility.getCurrentApplication().getTargetComponents();
				StringBuilder cmdArgs = new StringBuilder();
				
				try {
					if (aifCompo != null && aifCompo.length == 1) {
						TCComponentItem part = (TCComponentItem) aifCompo[0];
						String partID = part.getPropertyDisplayableValue(VFWorkspaceObject.ID);
						String partMakeBuy = part.getPropertyDisplayableValue(VFItem.ATTR_MAKE_BUY);
						if (partMakeBuy.isEmpty()) {
							MessageBox.post("Please input Part Make/Buy of part " + partID, "Error", MessageBox.ERROR);
							return;
						}
						String originalPartID = part.getPropertyDisplayableValue(VFItem.ATTR_ORIGINAL_PART_NUM).trim();
						if (!originalPartID.isEmpty() && !originalPartID.contains(";")) {
							LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
							queryInput.put("Item ID", originalPartID);
							queryInput.put("Type", "VF4_Design;VF4_BP_Design");
							TCComponent[] comps = Query.queryItem(session, queryInput, vfplm.soa.common.define.Constant.Query.ITEM_QUERY);
							if (comps != null && comps.length > 0) {
								String origlMakeBuy = comps[0].getPropertyDisplayableValue(VFItem.ATTR_MAKE_BUY);
								if (origlMakeBuy.isEmpty()) {
									MessageBox.post("Please input Part Make/Buy of part " + partID, "Error", MessageBox.ERROR);
									return;
								}
								cmdArgs.append(partID.trim()).append("_").append(partMakeBuy.trim()).append("-").append(originalPartID.trim()).append("_").append(origlMakeBuy.trim());
							} else {
								MessageBox.post("Original part not existed " + originalPartID, "Error", MessageBox.ERROR);
								return;
							}
						} else {
							cmdArgs.append(partID.trim()).append("_").append(partMakeBuy.trim());
						}
						if(cmdArgs.length() > 0) {
//							String command = "cmd /c start D:\\Workspace\\VinFastToolsRelease\\trunk\\vf_tools\\vfplm-soa-partcost-export\\vfplm-soa-partcost-export.bat " + cmdArgs.toString();
							String command = "cmd /c start C:\\Siemens\\TR11\\bin\\vfplm-soa-partcost-export\\vfplm-soa-partcost-export.bat " + cmdArgs.toString();
							Runtime.getRuntime().exec(command);
							
//							String line = "java -Dconfig_path=D:\\Workspace\\VinFastToolsRelease\\trunk\\vf_tools\\vfplm-soa-partcost-export\\resources\\config.properties -Dlog4j_path=D:\\Workspace\\VinFastToolsRelease\\trunk\\vf_tools\\vfplm-soa-partcost-export\\resources\\log4j.properties -cp \"D:\\Workspace\\VinFastToolsRelease\\trunk\\vf_tools\\vfplm-soa-partcost-export\\vfplm-soa-partcost-export.jar\" app.App " + cmdArgs.toString();
//							String line = "java -Dconfig_path=C:\\Siemens\\TR11\\bin\\vfplm-soa-partcost-export\\resources\\config.properties -Dlog4j_path=C:\\Siemens\\TR11\\bin\\vfplm-soa-partcost-export\\resources\\log4j.properties -cp \"C:\\Siemens\\TR11\\bin\\vfplm-soa-partcost-export\\vfplm-soa-partcost-export.jar\" app.App " + cmdArgs.toString();
//							CommandLine cmdLine = CommandLine.parse(line);
//							DefaultExecutor executor = new DefaultExecutor();
//							int exitValue = executor.execute(cmdLine);
//							Runtime.getRuntime().exec(line);
						}
					} else {
						MessageBox.post("Cannot apply for multi-part", "Error", MessageBox.ERROR);
					}
				} catch (Exception e) {
					MessageBox.post("Exception: " + e.toString(), "Error", MessageBox.ERROR);
				}
			}
		});
		return null;
	}
}
