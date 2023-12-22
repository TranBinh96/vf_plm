package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qplm_revision_rule_master;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class RevisionRuleMasterModel extends BaseModel {
    private int rowIndex;
    private Long id;
    private String name;

    public boolean validData() {
        return true;
    }
    private static final Qplm_revision_rule_master objectTable = Qplm_revision_rule_master.plm_revision_rule_master;
    public static final Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("name", objectTable.name);
    }};
}