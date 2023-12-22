package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qsys_data_puller_subscription_handler;
import com.vinfast.api.entity.Qview_sys_data_puller_subscription_handler;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DataPullerSubScriptionHandlerModel extends BaseModel {
    private int rowIndex;
    private Long id;
    private String tc_event_type;
    private String object_type;
    private Integer event_id;
    private String event_name;

    public boolean validData() {
        if (tc_event_type.isEmpty()) {
            setMessage("TC Event Type must not null");
            return false;
        }

        if (object_type.isEmpty()) {
            setMessage("Object Type must not null");
            return false;
        }

        if (event_id == null) {
            setMessage("Event must not null");
            return false;
        }

        return true;
    }

    private static final Qsys_data_puller_subscription_handler objectTable = Qsys_data_puller_subscription_handler.sys_data_puller_subscription_handler;
    public static final Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<>() {{
        put("id", objectTable.id);
        put("tc_event_type", objectTable.tc_event_type);
        put("object_type", objectTable.object_type);
        put("event_id", objectTable.event_id);
    }};

    private static final Qview_sys_data_puller_subscription_handler objectView = Qview_sys_data_puller_subscription_handler.view_sys_data_puller_subscription_handler;
    public static final Map<String, ComparableExpressionBase<?>> sortPropertiesView = new HashMap<>() {{
        put("id", objectView.id);
        put("tc_event_type", objectView.tc_event_type);
        put("object_type", objectView.object_type);
        put("event_id", objectView.event_id);
        put("event_name", objectView.event_name);
    }};
}