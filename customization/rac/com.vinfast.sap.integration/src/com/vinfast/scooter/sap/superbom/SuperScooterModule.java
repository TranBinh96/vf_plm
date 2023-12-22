package com.vinfast.scooter.sap.superbom;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.integration.arch.IntegrationModuleAbstract;
import com.teamcenter.integration.arch.ModelAbstract;
import com.teamcenter.integration.arch.ReportAbstract;
import com.teamcenter.integration.model.MCNInformation;
import com.vinfast.sap.util.PropertyDefines;

public class SuperScooterModule extends IntegrationModuleAbstract{

	
	SuperScooterDialog dialog = null;
	@Override
	protected void initModule() {
		System.out.println("SuperScooterBomBop");
		//initiate
		dialog = new SuperScooterDialog(new Shell());
		new SuperScooterBusiness(PropertyDefines.BOM_PROCESSOR_NAME);
		new SuperScooterConnector(PropertyDefines.CONNECTION_PROCESSOR_NAME);
		new ReportAbstract(PropertyDefines.REPORT_PROCESSOR_NAME, "SCOOTER_SUPER_BOMBOP");
		setDialog(dialog);
	}

	@Override
	protected void onUpdateUiEvent(ModelAbstract event) {
		super.onUpdateUiEvent(event);
	}
	
	@Override
	protected boolean validate() {
		return true;
	}
	
	@Override
	protected void onUpdateMCN(MCNInformation mcn) {
		System.out.println("onUpdateMCN");
		dialog.setTitle("Scooter Super Bom/Bop Transfer");
		dialog.setPlant(mcn.getPlant());
		dialog.setMCN(mcn.getMcnID());
		dialog.setYear(mcn.getModelYear());
		dialog.setShop(mcn.getMainGroup());
		dialog.setModel(mcn.getPlatForm());
	}
}
