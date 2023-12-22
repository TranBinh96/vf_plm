package com.vinfast.api.model.dataForm;

import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.model.common.SelectItem;

import java.util.LinkedList;
import java.util.List;

public class DataFormDataPullerSubscriptionList extends BaseModel {
    public List<SelectItem> eventList = new LinkedList<>();
    public List<SelectItem> tcEventTypeList = new LinkedList<>();
}
