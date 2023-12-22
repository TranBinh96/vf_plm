package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ECRStatusListModel extends BaseListModel<ECRStatusModel> {
    private String[] ecrList;
}