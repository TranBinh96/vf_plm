package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qplm_bom_verify_master;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class BomVerifyMasterModel extends BaseModel {
    private int rowIndex;
    private Long id;
    private String bom_type;
    private String program_name;
    private String module_name;
    private String part_number;
    private String revision_rule;
    private String notifiers;
    private Boolean is_active;
    public boolean validData() {
        return true;
    }
    private static final Qplm_bom_verify_master objectTable = Qplm_bom_verify_master.plm_bom_verify_master;
    public static final Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("bom_type", objectTable.bom_type);
        put("program_name", objectTable.program_name);
        put("module_name", objectTable.module_name);
        put("part_number", objectTable.part_number);
        put("revision_rule", objectTable.revision_rule);
        put("notifiers", objectTable.notifiers);
        put("is_active", objectTable.is_active);
    }};
}