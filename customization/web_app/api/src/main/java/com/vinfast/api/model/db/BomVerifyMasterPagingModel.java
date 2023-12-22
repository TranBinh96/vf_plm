package com.vinfast.api.model.db;

import com.vinfast.api.model.common.BasePagingModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BomVerifyMasterPagingModel extends BasePagingModel<BomVerifyMasterModel> {
    private String bomType = "";
    private String programName = "";
    private String moduleName = "";
    private String revisionRule = "";
    private String notifier = "";
    private Boolean isActive = null;
}