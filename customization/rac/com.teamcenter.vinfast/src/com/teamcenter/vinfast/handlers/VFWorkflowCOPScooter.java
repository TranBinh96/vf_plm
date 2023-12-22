package com.teamcenter.vinfast.handlers;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.vinfast.utils.Utils;
import com.teamcenter.vinfast.utils.VFProcessDialogProcessInput;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.vf.dialog.VFProcessDialogScooter;

public class VFWorkflowCOPScooter extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(VFWorkflowCOPScooter.class);
	
	private Utils utility = new Utils();
	private TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();
		TCComponent[] parts = new TCComponent[appComps.length];
		int i = 0;
		for (InterfaceAIFComponent appComp : appComps) {
			parts[i++] = (TCComponent)appComp;
		}

		VFProcessDialogScooter vd = new VFProcessDialogScooter(session, true, null);
		vd.setAlwaysOnTop(true);
		TCComponent rootObj = null;
		
		Map<TCComponent, List<ValidationResult>> partsAndValidationResults = new LinkedHashMap<TCComponent, List<ValidationResult>>();
		Map<String, Map<String, VFProcessDialogProcessInput>> input = new LinkedHashMap<String, Map<String,VFProcessDialogProcessInput>>();
		try {
			String[] prgm_values = new String[] { "Scooter" };
			String[] programsTypesWorkflows = Utils.getPreferenceValues(session, "VF_Program_Type_Workflow");
			
			utility = new Utils();
			Map<String, TCComponent> wfNameAndComps = utility.getAllWorkflowTemplates();
			for (String prgm_value : prgm_values) {
				Map<String, VFProcessDialogProcessInput> workflowAndInput = input.get(prgm_value);
				if (workflowAndInput == null) {
					workflowAndInput = new HashMap<String, VFProcessDialogProcessInput>();
					input.put(prgm_value, workflowAndInput);
				}
				
				int index = 0;
				for (String programsTypeWorkflow : programsTypesWorkflows) {
					String prefix = prgm_value + "~" + parts[0].getType();
					String pref = programsTypesWorkflows[index++];
					if (pref.contains(prefix)) {
						String workflowName = programsTypeWorkflow.split("~")[2];
						List<TCComponent> assignmentList = new LinkedList<TCComponent>();
						TCComponent wf = wfNameAndComps.get(workflowName);
						if (wf != null) {
							TCComponent[] assListComps = wf.getRelatedComponents("assignment_lists");
							for (TCComponent assListComp : assListComps) {
								assignmentList.add(assListComp);
							}
							VFProcessDialogProcessInput processInput = new VFProcessDialogProcessInput(assignmentList);
							workflowAndInput.put(workflowName, processInput);
							rootObj = parts[0];
						} 
						else {
							logger.error("[VF] WARNING: Cannot find workflow \"" + workflowName + "\"");
							//throw new Exception("Cannot find workflow " + workflowName);
						}
					}
				}
			}
			
			ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/process_cop_16.png"));
			vd.setIconImage(frame_Icon.getImage());
			vd.setRootTarget(rootObj);
			vd.createAndShowGUI(input, parts, partsAndValidationResults, "Process Dialog");
		} 
		catch (TCException e1) {
			logger.error(e1);
			e1.printStackTrace();
		} 
		catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
			
		return null;
	}
	
}
