package com.teamcenter.vinfast.utils;

public class VFNotSupportException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public VFNotSupportException(String message, Exception ex) {
		super(message, ex);
	}

	public VFNotSupportException(String message) {
		super(message);
	}

}
