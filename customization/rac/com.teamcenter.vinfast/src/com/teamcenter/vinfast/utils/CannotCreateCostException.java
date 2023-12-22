package com.teamcenter.vinfast.utils;

public class CannotCreateCostException extends Exception {
	private static final long serialVersionUID = 1L;

	public CannotCreateCostException(String message, Exception ex) {
		super(message, ex);
	}

	public CannotCreateCostException(String message) {
		super(message);
	}
}
