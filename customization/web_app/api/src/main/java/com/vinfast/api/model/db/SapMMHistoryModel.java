package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qsap_material_master;
import com.vinfast.api.entity.Qsap_mm_history;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SapMMHistoryModel extends BaseModel {
    private Long id;
    private String part_number;
    private String part_name;
    private String vn_description;
    private String revision;
    private String ecr_number;
    private String change_number;
    private String old_partnumber;
    private String uom;
    private String plants;
    private String net_weight;
    private String gross_weight;
    private String material_type;
    private String functional_class;
    private String approval_code;
    private String tracable_part;
    private String gm_part_number;
    private String broadcast_code;
    private String context;
    private Timestamp last_update_on;
    public boolean validData() {

        return true;
    }

    private final static Qsap_mm_history objectTable = Qsap_mm_history.sap_mm_history;
    public final static Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("transfer_to", objectTable.part_number);
        put("part_number", objectTable.part_name);
        put("part_name", objectTable.part_name);
        put("vn_description", objectTable.vn_description);
        put("revision", objectTable.revision);
        put("ecr_number", objectTable.ecr_number);
        put("change_number", objectTable.change_number);
        put("old_partnumber", objectTable.old_partnumber);
        put("uom", objectTable.uom);
        put("net_weight", objectTable.net_weight);
        put("gross_weight", objectTable.gross_weight);
        put("material_type", objectTable.material_type);
        put("functional_class", objectTable.functional_class);
        put("approval_code", objectTable.approval_code);
        put("tracable_part", objectTable.tracable_part);
        put("gm_part_number", objectTable.gm_part_number);
        put("broadcast_code", objectTable.broadcast_code);
        put("context", objectTable.context);
        put("last_update_on", objectTable.last_update_on);
    }};
}
