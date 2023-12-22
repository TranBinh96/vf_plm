package com.teamcenter.vinfast.utils;

import java.net.URL;
import java.util.List;
import java.util.Map.Entry;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;

public class ValidationResult {
	private String validationName;
	private String resultMessage;
	private boolean isPassed;
	private boolean isShown;

	public ValidationResult(String validationName, TCComponent validateObject, String validateAttribute) {
		try {
			this.validationName = validationName;
			if (validateObject.getPropertyDisplayableValue(validateAttribute).isEmpty()) {
				this.resultMessage = "Fill attribute " + validationName;
				this.isPassed = false;
			} else {
				this.isPassed = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.setShown(true);
	}

	public ValidationResult(String validationName, boolean isPassed, String resultMessage) {
		this.validationName = validationName;
		this.resultMessage = resultMessage;
		this.isPassed = isPassed;
		this.setShown(true);
	}

	public ValidationResult() {
		this.validationName = "";
		this.resultMessage = "";
		this.isPassed = true;
		this.setShown(true);
	}

	public String getValidationName() {
		return validationName;
	}

	public void setValidatingAttribute(String validationName) {
		this.validationName = validationName;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}

	public boolean isPassed() {
		return isPassed;
	}

	public void setPassed(boolean isPassed) {
		this.isPassed = isPassed;
	}

	public static String createHtmlText(Entry<TCComponent, List<ValidationResult>> partAndValidationResults) throws TCException {
		StringBuffer sb = new StringBuffer();
		String partString = partAndValidationResults.getKey().getProperty("object_string");
		sb.append("<p><b>" + partString + "</b></br>");
		sb.append("<table border=\"1\" style=\"width:460px\" ><tr><td style=\"width:30%;\"><b>Attribute</b></td><td style=\"width:70%\"><b>Results/Remarks</b></td></tr>");
		for (ValidationResult vr : partAndValidationResults.getValue()) {
			if (vr.isShown()) {
				String colorStyle = "";
				if (vr.isPassed) {
					colorStyle = " style=\"color:green\"";
				} else {
					colorStyle = " style=\"color:red\"";
				}
				sb.append("<tr><td" + colorStyle + ">" + vr.getValidationName() + "</td><td>" + vr.getResultMessage() + "</td></tr>");
			}
		}
		sb.append("</table></p>");
		return sb.toString();
	}

	public static String createHtmlValidation(Entry<TCComponent, List<ValidationResult>> partAndValidationResults) throws TCException {
		String partString = partAndValidationResults.getKey().getProperty("object_string");
		StringBuffer sb = new StringBuffer();

		sb.append("<p><b>" + partString + "</b></p>");
		sb.append("<table>");
		for (ValidationResult vr : partAndValidationResults.getValue()) {
			if (vr.isShown()) {
				String colorStyle = "";
				if (vr.isPassed) {
					colorStyle = " style=\"color:#28D094\"";
					sb.append("<tr><td><p" + colorStyle + ">" + vr.getValidationName() + "</p></td><td>" + passImage + "</td></tr>");
				} else {
					colorStyle = " style=\"color:#E91E63\"";
					sb.append("<tr><td><p" + colorStyle + ">" + vr.getValidationName() + "</p></td>");
					sb.append("<td><p>" + vr.getResultMessage() + "</p></td></tr>");
				}
			}
		}
		sb.append("</table>");
		return sb.toString();
	}

	public static boolean checkValidationResults(List<ValidationResult> validateResults) {
		boolean allPassed = true;
		for (ValidationResult validateResult : validateResults) {
			if (validateResult.isPassed() == false) {
				allPassed = false;
				break;
			}
		}
		return allPassed;
	}

	public static String getValidationPassImage() {
		String pathToImage = "/icons/validation_passed_16.png";
		URL path = ValidationResult.class.getClassLoader().getResource(pathToImage);
		String passImage = "<img src=\"" + path.toString() + "\" alt=\"Passed\"/>";
		return passImage;
	}

	public static String passImage = "<img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAAsTAAALEwEAmpwYAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAAB6JQAAgIMAAPn/AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAABtklEQVR42mL8//8/AyWAiYFCwEKKYsadbPwMDAwBDAwMG/67//pIkgGMuznqg8WDC29/u8Vv8dBsGwMDgzdRXmDcycZvddrpfKxMfMOeTwf4L325xCAlKCl9+/ZtbgwDGHey2aNrTpBKOMDKyWOw+M0Khu8/vv3x+uX50FHS4aWAgIAkigGMO9nk+WW0Dqgc0syHibkIuy64x/La4NDnYwxCH3m/lL3Nv1jGW9wjxi4WJCoqeoeBgYGB4f///wz///9nYDgpsZ/hvsH/2HsF/5nmsFkw7GC1j72d+5/hlMR/wS3Sn7tndh9dv369B0w9DCMC8ffvBwx//zJcZH3M4KLgtVVRTPbqim97GVi///+Tdj/mmqSgZHNAQMAOnOngv/WbRIaHr+5d+nWDQVxDXegbL4vtxz8fGazv6j5U51ZfHB0dvYNwOvj9zoj5PvP1xTLrJfn/cTLwfGL5Ef03/CavAO98olLif/dfH//+e+3J/On7j4//PjEYv1F/KSoquj0kJOQr0Un5v/uvi3yPfzgyfPnCEMrq/1JUVHQH3oSCHqowLLxNwv3EiRMTccnDMOOA50bAANjR3EnfxqyYAAAAAElFTkSuQmCC\"/>";

	public boolean isShown() {
		return isShown;
	}

	public void setShown(boolean isHide) {
		this.isShown = isHide;
	}
}
