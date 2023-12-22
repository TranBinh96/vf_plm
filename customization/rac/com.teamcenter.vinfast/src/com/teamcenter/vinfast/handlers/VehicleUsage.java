package com.teamcenter.vinfast.handlers;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.open.OpenFormDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.utils.VehicleUsageUtils;

public class VehicleUsage extends AbstractHandler {

	private static final Logger logger = Logger.getLogger(VehicleUsage.class);
	private final TCSession session;
	private boolean CREATE_VEHICLE_USAGE_FORM;

	public VehicleUsage() {
		session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		CREATE_VEHICLE_USAGE_FORM = true;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent[] appComps = app.getTargetComponents();
		TCComponentGroup currentUserGroup = session.getGroup();
		boolean isDBA = currentUserGroup.toString().equals("dba");
		CREATE_VEHICLE_USAGE_FORM = true;
		
		if (appComps != null && appComps.length > 1 && isDBA == false) {
			MessageBox.post("This feature is applicable to single part only.", "Info", MessageBox.INFORMATION);
			return null;
		} else if (appComps != null && appComps.length > 1 && isDBA == true) {
			try {
				// dba support correction
				CREATE_VEHICLE_USAGE_FORM = false;
				handleCorrection(app, appComps);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		InterfaceAIFComponent appComp = app.getTargetComponent();
		if (appComp == null) {
			return null;
		}
		
		TCComponentItemRevision partRev = null;
		TCComponentItem part = null;
		try {
			if (appComp instanceof TCComponentBOMLine) {
				TCComponentBOMLine bom = (TCComponentBOMLine) app.getTargetComponent();
				partRev = bom.getItemRevision();
				part = partRev.getItem();
			} else if (appComp instanceof TCComponentItemRevision) {
				partRev = (TCComponentItemRevision) appComp;
				part = partRev.getItem();
			} else if (appComp instanceof TCComponentItem) {
				part = (TCComponentItem) appComp;
				partRev = part.getLatestItemRevision(); 
			} else {
				MessageBox.post("This feature is not applicable for selected object.", "Info",
						MessageBox.INFORMATION);
				return null;
			}
			
			VehicleUsageUtils vehicleUsageUtils = new VehicleUsageUtils(part, partRev, CREATE_VEHICLE_USAGE_FORM, session);
			final TCComponent vehicleUsage = vehicleUsageUtils.getVehicleUsage();
			if (vehicleUsage != null) {
				vehicleUsage.refresh();
				OpenFormDialog vehicleUsageDialog = new OpenFormDialog((TCComponentForm) vehicleUsage);
				vehicleUsageDialog.setVisible(true);
			} else {
				MessageBox.post("Cannot find vehicle form of \"" + part.getProperty("object_string") + "\".",
						"Error", MessageBox.ERROR);
			}
		} catch (Exception ex) {
			if (partRev != null && ex.getMessage() != null && ex.getMessage().equals("NO_ACCESS_RIGHT")) {
				try {
					MessageBox.post("Please login with group \""+ partRev.getProperty("owning_group").toString() + "\" to create Vehicle Usage form on your selected part.",
							"Warning", MessageBox.WARNING);
				} catch (TCException e) {
					logger.error(ex);
					e.printStackTrace();
				}
			}
			
			logger.error(ex);
			ex.printStackTrace();
		}

		return null;
	}

	private void handleCorrection(AbstractAIFUIApplication app, InterfaceAIFComponent[] appComps)
			throws TCException, Exception {
		List<TCComponentItem> parts = new LinkedList<TCComponentItem>();
		for (InterfaceAIFComponent appComp : appComps) {
			TCComponentItemRevision partRev = null;
			TCComponentItem part = null;
			if (appComp instanceof TCComponentBOMLine) {
				TCComponentBOMLine bom = (TCComponentBOMLine) app.getTargetComponent();
				
				partRev = bom.getItemRevision();
				part = partRev.getItem();
			} else if (appComp instanceof TCComponentItemRevision) {
				partRev = (TCComponentItemRevision) appComp;
				part = partRev.getItem();
			} else if (appComp instanceof TCComponentItem) {
				part = (TCComponentItem) appComp;
				partRev = part.getLatestItemRevision(); 
			} else {
				MessageBox.post("One of selected parts is not bomline nor item nor itemrevision.", "Info",
						MessageBox.INFORMATION);
				break;
			}
			
			VehicleUsageUtils vehicleUsageUtils = new VehicleUsageUtils(part, partRev, CREATE_VEHICLE_USAGE_FORM, session);			
			vehicleUsageUtils.getVehicleUsage();
			parts.add(part);
		}
		TCComponentType.refreshThese(parts.toArray(new TCComponentItem[] {}));
	}

}
