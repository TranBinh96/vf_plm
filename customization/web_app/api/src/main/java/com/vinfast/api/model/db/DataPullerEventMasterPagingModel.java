package com.vinfast.api.model.db;

import com.vinfast.api.model.common.BasePagingModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataPullerEventMasterPagingModel extends BasePagingModel<DataPullerEventMasterModel> {
    private String name;
}
