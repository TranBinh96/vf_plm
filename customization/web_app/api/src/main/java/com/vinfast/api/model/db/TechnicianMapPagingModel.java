package com.vinfast.api.model.db;

import com.vinfast.api.model.common.BasePagingModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TechnicianMapPagingModel extends BasePagingModel<TechnicianMapModel> {
    private String employee_code = "";
    private String employee_name = "";
    private String key_word = "";
}
