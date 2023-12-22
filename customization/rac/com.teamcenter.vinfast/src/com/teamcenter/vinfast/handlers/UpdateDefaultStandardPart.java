package com.teamcenter.vinfast.handlers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;

public class UpdateDefaultStandardPart extends AbstractHandler {
	
	private static final Logger logger = Logger.getLogger(UpdateDefaultStandardPart.class);

	List<String> errorMessages;
	
	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		//TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		
		errorMessages = new LinkedList<String>();
		
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();

		TCComponentBOMLine[] bomlines = Arrays.asList(appComps).toArray(new TCComponentBOMLine[0]);
		
		try {
			for (TCComponentBOMLine bomline : bomlines) {
				String itemID = bomline.getItem().getProperty("item_id");
				if (itemID.startsWith("STD")) {
					try {
						Map<String, String> propsAndValsForStandardParts = new HashMap<String, String>();
						if (bomline.getProperty("VL5_main_module") == null || bomline.getProperty("VL5_main_module").isEmpty())
							propsAndValsForStandardParts.put("VL5_main_module", "STANDARD");
						
						if (bomline.getProperty("VL5_module_group") == null || bomline.getProperty("VL5_module_group").isEmpty())
							propsAndValsForStandardParts.put("VL5_module_group", "STANDARD");
						
						if (bomline.getProperty("VL5_module_name") == null || bomline.getProperty("VL5_module_name").isEmpty())
							propsAndValsForStandardParts.put("VL5_module_name", "STANDARD");
						
						if (bomline.getProperty("VL5_change_index") == null || bomline.getProperty("VL5_change_index").isEmpty())
							propsAndValsForStandardParts.put("VL5_change_index", "COP");
						
						if (propsAndValsForStandardParts.isEmpty() == false)
							bomline.setProperties(propsAndValsForStandardParts);
					} catch (TCException ex) {
						errorMessages.add(generatedErrorMessageLine(bomline));
					}
				}
			}
			
			if (errorMessages.isEmpty() == false) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						StringBuffer html = new StringBuffer();
						html.append("<html><body><table border=\"1\" style=\"width:100%\" >");
						html.append("<tr><td><b>Part Info</b></td><td><b>Level</b></td><td><b>Note</b></td></tr>");
						for (String errMsg : errorMessages) {
							String level = errMsg.split("\\|")[1];
							String line = errMsg.split("\\|")[0];
							html.append("<tr><td>").append(line).append("</td>").append("<td>").append(level).append("</td>")
							.append("<td>No Access</td>").append("</tr>");
						}
						html.append("</table></body></html>");
						StringViewerDialog dlg = new StringViewerDialog(new String[] {html.toString()});
					
						dlg.setTitle("Not Updated Lines");
						dlg.setVisible(true);
					}
				});
			} else {
				MessageBox.post("Please verify the updated content and Click on “Save” button to commit the values.", "Information", MessageBox.INFORMATION);
			}
		} catch (Exception e) {
			logger.error(e);
			MessageBox.post(e);
		}

		return null;
	}
	
	private String generatedErrorMessageLine(TCComponentBOMLine bomline) throws TCException {
		return bomline.getProperty("object_string") + "|" + bomline.getProperty("bl_level_starting_0");
	}
}
