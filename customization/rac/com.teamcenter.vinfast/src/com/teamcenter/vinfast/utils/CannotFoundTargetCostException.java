package com.teamcenter.vinfast.utils;

public class CannotFoundTargetCostException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CannotFoundTargetCostException(String message, Exception ex) {
		super(message, ex);
	}

	public CannotFoundTargetCostException(String message) {
		super(message);
	}

}
