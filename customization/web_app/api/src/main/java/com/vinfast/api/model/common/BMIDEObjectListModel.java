package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BMIDEObjectListModel extends BaseListModel<BMIDEObjectModel> {
    private String objectName;
}