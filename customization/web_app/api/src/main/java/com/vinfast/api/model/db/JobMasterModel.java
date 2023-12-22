package com.vinfast.api.model.db;


import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qtsc_job_master;
import com.vinfast.api.entity.Qview_tsc_job_master;
import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.model.db.FileMasterModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class JobMasterModel extends BaseModel {
    private int rowIndex;
    private String uid;
    private String job_name;
    private int job_category_id;
    private String job_category_name;
    private String task_name;
    private String status;
    private String schedule_type;
    private Time start_time;
    private String schedule_days;
    private String schedule_months;
    private String repeat_every;
    private String remark;
    private Timestamp create_date;
    private String create_by;
    private Timestamp update_date;
    private String update_by;
    private List<FileMasterModel> fileList;

    public boolean validData() {
        if (this.uid.isEmpty()) {
            setMessage("Uid must not null");
            return false;
        }
        if (this.job_name.isEmpty()) {
            setMessage("Job Name must not null");
            return false;
        }
        return true;
    }

    private static final Qtsc_job_master objectTable = Qtsc_job_master.tsc_job_master;
    public static final Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("uid", objectTable.uid);
        put("job_name", objectTable.job_name);
        put("job_category_id", objectTable.job_category_id);
        put("task_name", objectTable.task_name);
        put("status", objectTable.status);
        put("schedule_type", objectTable.schedule_type);
        put("start_time", objectTable.start_time);
        put("schedule_days", objectTable.schedule_days);
        put("schedule_months", objectTable.schedule_months);
        put("repeat_every", objectTable.repeat_every);
        put("remark", objectTable.remark);
        put("create_by", objectTable.create_by);
        put("create_date", objectTable.create_date);
        put("update_by", objectTable.update_by);
        put("update_date", objectTable.update_date);
    }};

    private static final Qview_tsc_job_master objectView = Qview_tsc_job_master.view_tsc_job_master;
    public static final Map<String, ComparableExpressionBase<?>> sortPropertiesView = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("uid", objectView.uid);
        put("job_name", objectView.job_name);
        put("job_category_id", objectView.job_category_id);
        put("job_category_name", objectView.job_category_name);
        put("task_name", objectView.task_name);
        put("status", objectView.status);
        put("schedule_type", objectView.schedule_type);
        put("start_time", objectView.start_time);
        put("schedule_days", objectView.schedule_days);
        put("schedule_months", objectView.schedule_months);
        put("repeat_every", objectTable.repeat_every);
        put("remark", objectView.remark);
        put("create_by", objectView.create_by);
        put("create_date", objectView.create_date);
        put("update_by", objectView.update_by);
        put("update_date", objectView.update_date);
    }};
}