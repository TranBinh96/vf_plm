package com.vinfast.api.model.dataForm;

import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.model.common.SelectItem;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class DataFormBomVerifyMasterUpdate extends BaseModel {
    public List<SelectItem> revisionRuleList = new LinkedList<>();

    public List<SelectItem> isActiveList = new LinkedList<>();

    public List<SelectItem> bomTypeList = new LinkedList<>();
}