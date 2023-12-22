package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qtsc_file_master;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class FileMasterModel extends BaseModel {
    private int rowIndex;
    private Long id;
    private String name;
    private String job_uid;
    private String job_name;
    private Integer bytes;
    private Integer rows;
    private String remark;
    private Timestamp create_date;
    private String create_by;
    private Timestamp update_date;
    private String update_by;

    public boolean validData() {
        return true;
    }

    private static final Qtsc_file_master objectTable = Qtsc_file_master.tsc_file_master;
    public static final Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("name", objectTable.name);
        put("job_uid", objectTable.job_uid);
        put("bytes", objectTable.bytes);
        put("rows", objectTable.rows);
        put("remark", objectTable.remark);
        put("create_date", objectTable.create_date);
        put("create_by", objectTable.create_by);
        put("update_date", objectTable.update_date);
        put("update_by", objectTable.update_by);
    }};
}