package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Getter
@Setter
public class TaskScheduleModel extends BaseModel {
    private int rowIndex;
    private String folder;
    private String hostName;
    private String taskName;
    private String fullName;
    private Timestamp nextRunTime;
    private String status;
    private String logonMode;
    private Timestamp lastRunTime;
    private String lastResult;
    private String author;
    private String taskToRun;
    private String startIn;
    private String comment;
    private String scheduledTaskState;
    private String idleTime;
    private String powerManagement;
    private String runAsUser;
    private String deleteTaskIfNotRescheduled;
    private String stopTaskIfRunsXHoursAndXMins;
    private String schedule;
    private String scheduleType;
    private Time startTime;
    private String startTimeString;
    private Date startDate;
    private Date endDate;
    private String days;
    private String months;
    private String repeatEvery;
    private String repeatUntilTime;
    private String repeatUntilDuration;
    private String repeatStopIfStillRunning;

}