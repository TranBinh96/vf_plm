package com.vinfast.api.model.dataForm;

import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.model.common.SelectItem;

import java.util.LinkedList;
import java.util.List;

public class DataFormJobMasterList extends BaseModel {
    public List<SelectItem> jobCategoryList = new LinkedList<>();
}