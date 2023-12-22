package com.vinfast.api.model.common;

import java.util.Date;

public class MissingFileInAWSListModel extends BaseListModel<MissingFileInAWSModel> {
    private Date dateRequest;

    public void setDateRequest(Date dateRequest) {
        this.dateRequest = dateRequest;
    }

    public Date getDateRequest() {
        return this.dateRequest;
    }
}