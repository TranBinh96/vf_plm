package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class BaseListModel<T> extends BaseModel {
//    public int totalItems = 0;
//    public int limitItems = 60;
//    public int offset = 0;
    public ArrayList<T> dataList = new ArrayList<>();
}
