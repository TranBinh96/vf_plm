package com.vinfast.api.model.common;


public class TaskSchedulePagingModel extends BasePagingModel<TaskScheduleModel> {
    private String status;

    public void setStatus(String status) {
        this.status = status;
    }

    private String taskName;

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getStatus() {
        return this.status;
    }

    public String getTaskName() {
        return this.taskName;
    }
}