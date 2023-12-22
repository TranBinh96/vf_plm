package com.teamcenter.vinfast.model;

public class ErrorBeanModel {
	private String parent;
	private String child;
	private String findno;
	private String errorCodes;
	
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public String getChild() {
		return child;
	}
	public void setChild(String child) {
		this.child = child;
	}
	public String getFindno() {
		return findno;
	}
	public void setFindno(String findno) {
		this.findno = findno;
	}
	public String getErrorCodes() {
		return errorCodes;
	}
	public void setErrorCodes(String errorCodes) {
		this.errorCodes = errorCodes;
	}
}
