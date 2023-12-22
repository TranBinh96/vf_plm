package com.teamcenter.rac.jes;

public class ActivityModel {
	private String activityName = "";
	private String activityID = "";
	private double workingTime = NULL_TIME;
	private double walkingTime = NULL_TIME;
	private String version = "";
	public static double NULL_TIME = -1;
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getActivityName() {
		return activityName;
	}
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
	public String getActivityID() {
		return activityID;
	}
	public void setActivityID(String activityID) {
		this.activityID = activityID;
	}
	public double getWorkingTime() {
		return workingTime;
	}
	public void setWorkingTime(double workingTime) {
		if (workingTime > NULL_TIME) {
			this.workingTime = workingTime;
		}
	}
	public double getWalkingTime() {
		return walkingTime;
	}
	public void setWalkingTime(double walkingTime) {
		if (walkingTime > NULL_TIME) {
			this.walkingTime = walkingTime;
		}
	}
}
