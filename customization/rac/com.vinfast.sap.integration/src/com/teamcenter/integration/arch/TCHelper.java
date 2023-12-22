package com.teamcenter.integration.arch;

import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.sap.bom.BOMManager;

public class TCHelper {
	private static final TCHelper INSTANCE = new TCHelper();

	private TCHelper() {

	}

	public TCSession session = null;
	public TCComponentItemRevision changeObject = null;
	public DataManagementService dmService = null;
	public BOMManager bomManager = new BOMManager();

	public static TCHelper getInstance() {
		return INSTANCE;
	}
}
