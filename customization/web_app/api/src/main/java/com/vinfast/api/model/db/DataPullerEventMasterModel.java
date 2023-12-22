package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qsys_data_puller_event_master;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DataPullerEventMasterModel extends BaseModel {
    private int rowIndex;
    private Long id;
    private String name;

    public boolean validData() {
        return true;
    }
    private static final Qsys_data_puller_event_master objectTable = Qsys_data_puller_event_master.sys_data_puller_event_master;
    public static final Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("name", objectTable.name);
    }};
}
