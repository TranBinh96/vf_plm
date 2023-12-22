package com.vinfast.api.model.db;

import com.vinfast.api.model.common.BasePagingModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class SapMaterialMasterPagingModel extends BasePagingModel<SapMaterialMasterModel> {
    private String partNumber;
    private String partName;
    private String materialType;
    private String status;
    private Timestamp createFromDate = null;
    private Timestamp createToDate = null;
    private Timestamp updateFromDate = null;
    private Timestamp updateToDate = null;
}
