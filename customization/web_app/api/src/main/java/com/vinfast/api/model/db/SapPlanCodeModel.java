package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qsap_material_master;
import com.vinfast.api.entity.Qsap_plant_code;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SapPlanCodeModel extends BaseModel {
    private Long id;
    private String plant_code;
    private String make_buy;
    public boolean validData() {

        return true;
    }

    private final static Qsap_plant_code objectTable = Qsap_plant_code.sap_plant_code;
    public final static Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("plant_code", objectTable.plant_code);
        put("make_buy", objectTable.make_buy);
    }};
}
