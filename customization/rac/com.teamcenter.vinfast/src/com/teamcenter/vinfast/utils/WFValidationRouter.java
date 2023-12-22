package com.teamcenter.vinfast.utils;

public class WFValidationRouter {

	public static IValidator getValidator(String program, String workflowName) {

		IValidator validator = null;
		if (program.toLowerCase().contains("s&s") || program.toLowerCase().contains("sns") || program.toLowerCase().contains("sedan") || program.toLowerCase().contains("suv")) {
			validator = new SnSValidator();
		} else {
			validator = new ECRValidator();
		}

		return validator;
	}
}
