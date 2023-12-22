package com.teamcenter.vinfast.model;

public class DVPRDocumentVFTESummaryModel {
	private String vfte;
	private int greenNumber = 0;
	private int yellowNumber = 0;
	private int redNumber = 0;
	private String targetReleaseDate = "";

	public String getVfte() {
		return vfte;
	}

	public void setVfte(String vfte) {
		this.vfte = vfte;
	}

	public int getGreenNumber() {
		return greenNumber;
	}

	public int getYellowNumber() {
		return yellowNumber;
	}

	public int getRedNumber() {
		return redNumber;
	}

	public void setTestResult(String testResult) {
		switch (testResult) {
		case "Green":
			greenNumber++;
			break;
		case "Yellow":
			yellowNumber++;
			break;
		case "Red":
			redNumber++;
			break;
		default:
			break;
		}
	}

	public String getNeedRevision() {
		return targetReleaseDate.isEmpty() ? "NO" : "YES";
	}

	public String getTargetReleaseDate() {
		return targetReleaseDate;
	}

	public void setTargetReleaseDate(String targetReleaseDate) {
		this.targetReleaseDate = targetReleaseDate;
	}

}
