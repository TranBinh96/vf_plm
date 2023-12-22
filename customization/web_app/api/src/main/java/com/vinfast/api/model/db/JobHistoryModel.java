package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qtsc_job_history;
import com.vinfast.api.entity.Qview_tsc_job_history;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class JobHistoryModel extends BaseModel {
    private int rowIndex;
    private Long id;
    private String job_uid;
    private String job_name;
    private String job_category_name;
    private String task_name;
    private String status;
    private String statusColor;
    private String message;
    private String remark;
    private Timestamp start_time;
    private Timestamp end_time;

    public boolean validData() {
        return true;
    }
    private static final Qtsc_job_history objectTable = Qtsc_job_history.tsc_job_history;
    public static final Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("job_uid", objectTable.job_uid);
        put("status", objectTable.status);
        put("message", objectTable.message);
        put("remark", objectTable.remark);
        put("start_time", objectTable.start_time);
        put("end_time", objectTable.end_time);
    }};
    private static final Qview_tsc_job_history objectView = Qview_tsc_job_history.view_tsc_job_history;
    public static final Map<String, ComparableExpressionBase<?>> sortPropertiesView = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectView.id);
        put("job_uid", objectView.job_uid);
        put("job_name", objectView.job_name);
        put("job_category_name", objectView.job_category_name);
        put("task_name", objectView.task_name);
        put("status", objectView.status);
        put("message", objectView.message);
        put("remark", objectView.remark);
        put("start_time", objectView.start_time);
        put("end_time", objectView.end_time);
    }};
}

