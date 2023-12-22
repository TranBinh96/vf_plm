package com.vinfast.api.model.dataForm;

import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.model.common.SelectItem;

import java.util.LinkedList;
import java.util.List;

public class DataFormBomVerifyMasterList extends BaseModel {
    public List<SelectItem> bomTypeList = new LinkedList<>();
    public List<SelectItem> programList = new LinkedList<>();
    public List<SelectItem> moduleList = new LinkedList<>();
    public List<SelectItem> revisionRuleList = new LinkedList<>();
}