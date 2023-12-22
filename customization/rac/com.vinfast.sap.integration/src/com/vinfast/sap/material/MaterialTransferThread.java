package com.vinfast.sap.material;

import com.teamcenter.integration.model.MaterialTransferModel;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.vinfast.integration.model.OrganizationInformationAbstract;
import com.vinfast.sap.services.CreateSoapHttpRequest;
import com.vinfast.sap.services.Logger;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.url.SAPURL;

public class MaterialTransferThread implements Runnable {
	private OrganizationInformationAbstract serverInfo = null;
	private StringBuilder strBuilder = null;
	private MaterialTransferModel transferModel = null;
	private String logFolder = "";
	private SAPURL SAPConnect = null;
	private int count = 0;

	public void setServerInfo(OrganizationInformationAbstract serverInfo) {
		this.serverInfo = serverInfo;
	}

	public void setStrBuilder(StringBuilder strBuilder) {
		this.strBuilder = strBuilder;
	}

	public void setLogFolder(String logFolder) {
		this.logFolder = logFolder;
	}

	public void setSAPConnect(SAPURL sAPConnect) {
		SAPConnect = sAPConnect;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setTransferModel(MaterialTransferModel transferModel) {
		this.transferModel = transferModel;
	}

	public MaterialTransferThread() {

	}

	@Override
	public void run() {
		String revID = transferModel.getMaterialNumber();
		String revRevID = transferModel.getRevisionNumber();
		String[] printValues = new String[] { "", "", "", "" };
		String serverIP = serverInfo.getServerIP();
		try {
			String message[] = CreateSoapHttpRequest.sendRequest(SAPConnect.materialWebserviceURL(serverIP), transferModel.getTransferData(), SAPURL.MAT_HEADER, SAPURL.MAT_TAG, SAPURL.MAT_NAMESPACE, "I_MAT_" + count + "_" + revID + "_" + revRevID, logFolder, serverInfo.getAuth());
			if (message != null && message[0].equals("E")) {
				printValues = new String[] { Integer.toString(count), revID + "/" + revRevID, message[1], "Error" };
				Logger.bufferResponse("PRINT", printValues, strBuilder);

			} else if (message != null) {
				printValues = new String[] { Integer.toString(count), revID + "/" + revRevID, message[1], "Success" };
				Logger.bufferResponse("PRINT", printValues, strBuilder);
			} else {
				printValues = new String[] { Integer.toString(count), revID + "/" + revRevID, "No response from SAP. Need to transfer again", "Error" };
				Logger.bufferResponse("PRINT", printValues, strBuilder);
			}
			setTransferToSAP(transferModel.getMaterialItemObject());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setTransferToSAP(TCComponent item) {
		if (item == null)
			return;
		try {
			String transferStatus = item.getProperty(PropertyDefines.ITEM_IS_IN_SAP).trim();
			if (transferStatus.isEmpty())
				item.setProperty(PropertyDefines.ITEM_IS_IN_SAP, "True");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
}
