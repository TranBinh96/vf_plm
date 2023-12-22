package com.vinfast.api.model.db;

import com.vinfast.api.model.common.BasePagingModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class JobHistoryPagingModel extends BasePagingModel<JobHistoryModel> {
    private String jobName;
    private Integer jobCategoryID;
    private String status;
    private Timestamp startFromDate = null;
    private Timestamp startToDate = null;
}