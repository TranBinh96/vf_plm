package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qsap_material_master;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SapMaterialMasterModel extends BaseModel {
    private int rowIndex;
    private Long id;
    private String transfer_to;
    private String part_number;
    private String part_name;
    private String vn_description;
    private String revision;
    private String ecr_number;
    private String change_number;
    private String old_partnumber;
    private String uom;
    private String net_weight;
    private String gross_weight;
    private String material_type;
    private String functional_class;
    private String approval_code;
    private String tracable_part;
    private String gm_part_number;
    private String broadcast_code;
    private String context;
    private Timestamp creation_date;
    private Timestamp last_update_on;
    private String status;
    private int failed_counter;
    private String sync_status;
    private String error_log;
    public boolean validData() {
        return true;
    }

    private final static Qsap_material_master objectTable = Qsap_material_master.sap_material_master;
    public final static Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", objectTable.id);
        put("transfer_to", objectTable.transfer_to);
        put("part_number", objectTable.part_number);
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
        put("creation_date", objectTable.creation_date);
        put("last_update_on", objectTable.last_update_on);
        put("status", objectTable.status);
        put("failed_counter", objectTable.failed_counter);
        put("sync_status", objectTable.sync_status);
        put("error_log", objectTable.error_log);
    }};
}
