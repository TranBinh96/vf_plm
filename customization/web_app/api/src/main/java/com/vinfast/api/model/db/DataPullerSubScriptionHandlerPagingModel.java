package com.vinfast.api.model.db;

import com.vinfast.api.model.common.BasePagingModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataPullerSubScriptionHandlerPagingModel extends BasePagingModel<DataPullerSubScriptionHandlerModel> {
    private String tc_event_type = "";
    private String object_type = "";
    private Integer event_id = null;
}