package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LOVValueListModel extends BaseListModel<LOVValueModel> {
    private String objectName;
    private String propertyName;
}
