package com.teamcenter.vinfast.model;

public class DVPRDocumentTestReportModel {
	private String vfte = "";
	private String testName = "";
	private String progress = "";
	private String testResult = "";
	private String comment = "";

	public String getVfte() {
		return vfte;
	}

	public void setVfte(String vfte) {
		this.vfte = vfte;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = String.valueOf(progress) + "%";
	}

	public void setProgress(double progress) {
		this.progress = String.valueOf((int) (progress * 100)) + "%";
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public boolean isProgressValid() {
		return progress.compareTo("0%") == 0 || progress.compareTo("50%") == 0 || progress.compareTo("90%") == 0 || progress.compareTo("100%") == 0;
	}

	public String getTestResult() {
		return testResult;
	}

	public void setTestResult(String testResult) {
		if (testResult.compareToIgnoreCase("Green") == 0)
			testResult = "Green";
		if (testResult.compareToIgnoreCase("Yellow") == 0)
			testResult = "Yellow";
		if (testResult.compareToIgnoreCase("Red") == 0)
			testResult = "Red";

		this.testResult = testResult;
	}

	public boolean isTestResultValid() {
		return (testResult.compareTo("Green") == 0 || testResult.compareTo("Yellow") == 0 || testResult.compareTo("Red") == 0);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
