package com.teamcenter.vinfast.handlers;

import java.util.Arrays;
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
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.common.NotDefinedException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.utils.CannotFoundTargetCostException;
import com.teamcenter.vinfast.utils.IValidator;
import com.teamcenter.vinfast.utils.Utils;
import com.teamcenter.vinfast.utils.VFNotSupportException;
import com.teamcenter.vinfast.utils.VFProcessDialogProcessInput;
import com.teamcenter.vinfast.utils.ValidationResult;
import com.teamcenter.vinfast.utils.ValidationRouter;
import com.vf.dialog.VFAbstractProcessDialog;
import com.vf.dialog.VFScooterProcessDialog;
import com.vf.dialog.VFSourcingProcessDialog;

public class StartSourcing extends AbstractHandler {

	private final String VINF_COST_AVAILABLE_OBJECT_TYPES = "VINF_COST_AVAILABLE_OBJECT_TYPES";
	private final static Logger logger = Logger.getLogger(StartSourcing.class);
	private String[] ALLOWED_OBJ_TYPES = { "VF4_DesignRevision", "VF3_manuf_partRevision", "VF3_car_partRevision", "VF3_Scooter_partRevision", "VF4_VES_ME_PartRevision", "VF4_Service_KitRevision" };
	private String[] SOURCING_PRG_2_ALLOWED_GROUPS;
	private String[] validPrgForCurrentGroup;
	private LinkedHashMap<String, String> mapGrouptoPrg;
	private String[] assignmentListValues = null;
	private TCSession session = null;
	private Utils utility = null;
	private String moduleSeleted = "";

	public static boolean IS_BYPASS_SOURCING_VALIDATION = false;
	private LinkedHashMap<String, String> validateMapping = null;

	public StartSourcing() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		TCPreferenceService preferenceService = session.getPreferenceService();
		SOURCING_PRG_2_ALLOWED_GROUPS = preferenceService.getStringValues(VFSourcingProcessDialog.VF_SOURCING_PRG_2_USER_GROUP);
		mapGrouptoPrg = new LinkedHashMap<>();
		for (int i = 0; i < SOURCING_PRG_2_ALLOWED_GROUPS.length; i++) {
			mapGrouptoPrg.put(SOURCING_PRG_2_ALLOWED_GROUPS[i].split("=")[0], SOURCING_PRG_2_ALLOWED_GROUPS[i].split("=")[1]);
		}
		assignmentListValues = preferenceService.getStringValues("VF_Module_AssignmentList");
		utility = new Utils();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();
		TCComponentBOMLine partBomLine = null;
		String currentGroup = session.getCurrentGroup().toString();
		boolean isMEPart = false;
		try {
			IParameter cmdParamIsMEPart = event.getCommand().getParameter("com.teamcenter.vinfast.commands.StartSourcing.isMEPart");
			String isMEPartStr = (cmdParamIsMEPart != null && cmdParamIsMEPart.getName() != null) ? cmdParamIsMEPart.getName() : "";
			isMEPart = (isMEPartStr.isEmpty() == false);
		} catch (NotDefinedException e) {
			MessageBox.post("Exeption: " + e.toString() + ".\n", "ERROR", MessageBox.ERROR);
			return null;
		}

		Map<TCComponent, List<ValidationResult>> partsAndValidationResults = new HashMap<TCComponent, List<ValidationResult>>();
		Map<String, Map<String, VFProcessDialogProcessInput>> input = new HashMap<String, Map<String, VFProcessDialogProcessInput>>();
		boolean isValidGroup = utility.checkValidGroup(Arrays.asList(mapGrouptoPrg.keySet().toArray()).toArray(new String[mapGrouptoPrg.values().size()]), currentGroup);

		if (isValidGroup == false) {
			MessageBox.post("Access Denied! Allowed groups are " + Arrays.toString(Arrays.asList(mapGrouptoPrg.keySet().toArray()).toArray(new String[mapGrouptoPrg.values().size()])) + ".\n", "", MessageBox.WARNING);
			return null;
		}

		// get default program for current group
		for (String key : mapGrouptoPrg.keySet()) {
			if (currentGroup.contains(key) || currentGroup.compareToIgnoreCase(key) == 0) {
				validPrgForCurrentGroup = mapGrouptoPrg.get(key).split(";");
				break;
			}
		}

		VFAbstractProcessDialog validationDialog = null;
		try {
			String[] progarmsAndWorkflows = Utils.getPreferenceValues(session, VFSourcingProcessDialog.VF_SOURCING_PRG_2_WF);
			Map<String, TCComponent> wfNameAndComps = utility.getAllWorkflowTemplates();
			validateMapping = ValidationRouter.getProgramValidateMapping(session);
			for (String prWf : progarmsAndWorkflows) {
				if (prWf.split(";").length != 2) {
					// TODO: use new exception
					throw new CannotFoundTargetCostException("Value(s) in preference VF_SOURCING_PROGRAM_2_WF is/are invalid.");
				}
				String wf = prWf.split(";")[1].trim();
				String pr = prWf.split(";")[0].trim();

				// only show valid program by preference VF_SOURCING_PROGRAM_2_WF
				if (!Arrays.asList(validPrgForCurrentGroup).contains(pr)) {
					continue;
				}

				Map<String, VFProcessDialogProcessInput> workflowAndInput = new HashMap<String, VFProcessDialogProcessInput>();
				input.put(pr, workflowAndInput);
				List<TCComponent> assignmentList = new LinkedList<TCComponent>();

				TCComponent wfTemplate = wfNameAndComps.get(wf);
				if (wfTemplate == null) {
					// TODO: use new exception
					continue;
				}
				TCComponent[] wfAsssignmentLists = wfTemplate.getRelatedComponents("assignment_lists");
				assignmentList.addAll(Arrays.asList(wfAsssignmentLists));
				IValidator validator = null;
				try {
					validator = ValidationRouter.getValidator(pr, event, validateMapping);
				} catch (VFNotSupportException ex) {
					ex.printStackTrace();
				}
				String programAndModule = pr + "_" + moduleSeleted;
				String defAssignmentList = getAssignment(assignmentListValues, programAndModule);
				VFProcessDialogProcessInput processInput = new VFProcessDialogProcessInput(assignmentList, validator, defAssignmentList);
				workflowAndInput.put(wf, processInput);
			}

			for (InterfaceAIFComponent appComp : appComps) {
				if (appComp == null) {
					logger.error("\n\nERROR: appComp == NULL!!!!!\n\n");
					return null;
				}

				TCComponentItemRevision partRev = null;
				if (appComp instanceof TCComponentBOMLine) {
					partBomLine = (TCComponentBOMLine) appComp;
					partRev = partBomLine.getItemRevision();
				} else if (appComp instanceof TCComponentItemRevision) {
					partRev = (TCComponentItemRevision) appComp;
				}
				partRev.refresh();

				boolean isValid = false;
				for (String validType : ALLOWED_OBJ_TYPES) {
					if (partRev.getType().equals(validType)) {
						isValid = true;
						break;
					}
				}

				String partRevString = partRev.getProperty("object_string");
				if (isValid == false) {
					MessageBox.post("This feature is not applicable for part \"" + partRevString + "\" of type \"" + partRev.getType() + "\"", "Info", MessageBox.INFORMATION);
					return null;
				}

				IValidator cuvValidator = ValidationRouter.getValidator(validPrgForCurrentGroup[0], event, validateMapping);
				List<ValidationResult> validateResults = null;
				if (isMEPart) {
					validateResults = cuvValidator.validate(partRev);
				} else {
					validateResults = cuvValidator.validate(partBomLine);
				}
				partsAndValidationResults.put((TCComponent) appComp, validateResults);
			}

			if (partsAndValidationResults.size() > 0) {
				VFAbstractProcessDialog.PostAction startSourcingPostAction = new VFAbstractProcessDialog.PostAction() {
					@Override
					public void execute(TCComponent createdProcess) throws Exception {
						// TODO
					}
				};
				if (validPrgForCurrentGroup[0].contains("Scooter")) {
					validationDialog = new VFScooterProcessDialog(session, false, startSourcingPostAction);
				} else {
					validationDialog = new VFSourcingProcessDialog(session, false, startSourcingPostAction);
				}

			} else {
				logger.error("No parts for tringgering sourcing");
			}
			ImageIcon frame_Icon = new ImageIcon(getClass().getResource("/icons/sourcing_16.png"));
			validationDialog.setAlwaysOnTop(true);
			validationDialog.setIconImage(frame_Icon.getImage());
			validationDialog.createAndShowGUI(input, partsAndValidationResults.keySet().toArray(new TCComponent[0]), partsAndValidationResults, "Create Sourcing Process");
		} catch (CannotFoundTargetCostException | VFNotSupportException ex) {
			if (validationDialog != null) {
				validationDialog.setAlwaysOnTop(false);
			}
			MessageBox.post(ex.getMessage(), "ERROR", MessageBox.ERROR);
		} catch (Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
			MessageBox.post(ex.getMessage(), "ERROR", MessageBox.ERROR);
		}

		return null;
	}

	private String getAssignment(String[] assgn_Lists, String module) {
		String assignment_ListValue = "";
		for (String assignment : assgn_Lists) {
			String[] assignmentModule = assignment.split("=");
			if (module.equals(assignmentModule[0])) {
				assignment_ListValue = assignmentModule[1];
			}
		}
		return assignment_ListValue;
	}
}
