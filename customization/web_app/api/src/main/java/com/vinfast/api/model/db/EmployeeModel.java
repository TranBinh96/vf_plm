package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qsys_employee;
import com.vinfast.api.entity.Qsys_login;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class EmployeeModel extends BaseModel {
    private int rowIndex;
    private String employee_code;
    private String employee_name;
    private String email;
    private String phone_number;
    private int avatarID;
    private String service_desk_id;
    private String service_desk_name;
    private String remark;
    private Timestamp create_date;
    private String create_by;
    private Timestamp update_date;
    private String update_by;

    public boolean validData() {
        if (employee_code.isEmpty()) {
            setMessage("Employee Code must not null");
            return false;
        }

        if(employee_name.isEmpty()) {
            setMessage("Employee Name must not null");
            return false;
        }

        return true;
    }

    private final static Qsys_employee dataPuller = Qsys_employee.sys_employee;
    public final static Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("employee_code", dataPuller.employee_code);
        put("employee_name", dataPuller.employee_name);
        put("email", dataPuller.email);
        put("phone_number", dataPuller.phone_number);
        put("avatarID", dataPuller.avatarID);
        put("service_desk_id", dataPuller.service_desk_id);
        put("service_desk_name", dataPuller.service_desk_name);
        put("remark", dataPuller.remark);
        put("create_date", dataPuller.create_date);
        put("create_by", dataPuller.create_by);
        put("update_date", dataPuller.update_date);
        put("update_by", dataPuller.update_by);
    }};
}
