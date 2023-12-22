package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qtsc_job_category;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class JobCategoryModel extends BaseModel {
    private int rowIndex;
    private Long id;
    private String job_category_name;
    private String remark;
    private Timestamp create_date;
    private String create_by;
    private Timestamp update_date;
    private String update_by;

    public boolean validData() {
        if (this.job_category_name.isEmpty()) {
            setMessage("Job category name must not null");
            return false;
        }
        return true;
    }

    private static final Qtsc_job_category objectTable = Qtsc_job_category.tsc_job_category;
    public static final Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("job_category_name", objectTable.job_category_name);
        put("remark", objectTable.remark);
        put("create_by", objectTable.create_by);
        put("create_date", objectTable.create_date);
        put("update_by", objectTable.update_by);
        put("update_date", objectTable.update_date);
    }};
}