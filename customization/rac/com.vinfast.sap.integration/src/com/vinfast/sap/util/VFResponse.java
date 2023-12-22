package com.vinfast.sap.util;

import com.teamcenter.rac.kernel.TCComponent;

public class VFResponse {
	
	private boolean isError = false;
	private String errorMessage = "";
	private TCComponent[] outputObjects = null;

	public void setErrorMessage(String errorMsg) {
		errorMessage = errorMsg;
	}
	public void setOutput(TCComponent[] objects) {
		outputObjects = objects;
	}
	public void setError(boolean error) {
		isError = error;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public TCComponent[] getOutput() {
		return outputObjects;
	}
	public boolean hasError() {
		return isError;
	}
}
