package com.vinfast.api.model.db;


import com.vinfast.api.model.common.BasePagingModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobMasterPagingModel extends BasePagingModel<JobMasterModel> {
    private String jobName;
    private Integer jobCategoryID;
}