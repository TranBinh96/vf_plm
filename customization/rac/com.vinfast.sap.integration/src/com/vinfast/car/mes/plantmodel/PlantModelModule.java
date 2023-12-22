package com.vinfast.car.mes.plantmodel;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.integration.arch.IntegrationModuleAbstract;
import com.teamcenter.integration.arch.ModelAbstract;
import com.teamcenter.integration.arch.ReportAbstract;
import com.teamcenter.integration.model.MCNInformation;
import com.vinfast.sap.util.PropertyDefines;

public class PlantModelModule extends IntegrationModuleAbstract {
	private static final Logger logger = Logger.getLogger(PlantModelModule.class);

	public PlantModelModule() {
		super();
	}

	PlantModelDialog dialog = null;

	@Override
	protected void initModule() {
		logger.info("PlantModelModule.initModule()");
		
		dialog = new PlantModelDialog(new Shell());
		new PlantModelBusiness(PropertyDefines.BOM_PROCESSOR_NAME);
		new PlantModelConnector(PropertyDefines.CONNECTION_PROCESSOR_NAME);
		new ReportAbstract(PropertyDefines.REPORT_PROCESSOR_NAME, "PLANT_MODEL");
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
		dialog.setTitle("Plant Model(s) Transfer");
		dialog.setPlant(mcn.getPlant());
		dialog.setMCN(mcn.getMcnID());
	}
}
