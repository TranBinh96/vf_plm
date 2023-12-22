package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qast_technician_map;
import com.vinfast.api.entity.Qtsc_job_category;
import com.vinfast.api.entity.Qview_ast_technician_map;
import com.vinfast.api.entity.Qview_tsc_job_history;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.*;

@Getter
@Setter
public class TechnicianMapModel extends BaseModel {
    private int rowIndex;
    private Long id;
    private String employee_code;
    private String employee_name;
    private String service_desk_id;
    private String service_desk_name;
    private Boolean is_active;
    private String key_word;
    private List<String> key_word_list;

    public void setKeywordListFromKeyword() {
        key_word_list = new LinkedList<>();
        if (!key_word.isEmpty()) {
            String keyWord = key_word.replace("(?i).*?(\\b", "").replace("\\b).*?", "").replace("\\b", "a");
            Collections.addAll(key_word_list, keyWord.split("a\\|a"));
        }
    }

    public String getKeywordFromKeywordList() {
        if (key_word_list != null && key_word_list.size() > 0) {
            String keyword = "(?i).*?(\\b" + String.join("\\b|\\b", key_word_list) + "\\b).*?";
            return keyword;
        }

        return "";
    }

    public boolean validData() {
        if (this.employee_code.isEmpty()) {
            setMessage("Employee Code name must not null");
            return false;
        }
        return true;
    }

    private static final Qast_technician_map objectTable = Qast_technician_map.ast_technician_map;
    public static final Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("employee_code", objectTable.employee_code);
        put("is_active", objectTable.is_active);
        put("key_word", objectTable.key_word);
    }};

    private static final Qview_ast_technician_map objectView = Qview_ast_technician_map.view_ast_technician_map;
    public static final Map<String, ComparableExpressionBase<?>> sortPropertiesView = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectView.id);
        put("employee_code", objectView.employee_code);
        put("employee_name", objectView.employee_name);
        put("service_desk_id", objectView.service_desk_id);
        put("service_desk_name", objectView.service_desk_name);
        put("is_active", objectView.is_active);
        put("key_word", objectView.key_word);
    }};
}
