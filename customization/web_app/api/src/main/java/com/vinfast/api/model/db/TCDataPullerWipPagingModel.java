package com.vinfast.api.model.db;

import com.vinfast.api.model.common.BasePagingModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class TCDataPullerWipPagingModel extends BasePagingModel<TCDataPullerWipModel> {
    private String type = "";
    private String status = "";
    private String uid = "";
    private String error = "";
    private Timestamp createFromDate = null;
    private Timestamp createToDate = null;
    private Timestamp updateFromDate = null;
    private Timestamp updateToDate = null;
}
