package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qtc_data_puller_wip;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class TCDataPullerWipModel extends BaseModel {
    private int rowIndex;
    private Long id;
    private String type;
    private String uid;
    private Timestamp created_on;
    private int failed_counter;
    private String error_log;
    private Timestamp last_update;
    private String status = "";
    private String statusColor;

    public boolean validData() {
        if (this.type.isEmpty()) {
            setMessage("Type must not null");
            return false;
        }
        if (this.status.isEmpty()) {
            setMessage("Status must not null");
            return false;
        }
        return true;
    }

    private static final Qtc_data_puller_wip objectTable = Qtc_data_puller_wip.tc_data_puller_wip;
    public static final Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("type", objectTable.type);
        put("uid", objectTable.uid);
        put("created_on", objectTable.created_on);
        put("failed_counter", objectTable.failed_counter);
        put("error_log", objectTable.error_log);
        put("last_update", objectTable.last_update);
        put("status", objectTable.status);
    }};
}