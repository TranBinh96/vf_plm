package com.vinfast.api.model.db;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.vinfast.api.entity.Qsys_login;
import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class LoginModel extends BaseModel {
    private int rowIndex;
    private int id;

    private String employee_code;
    private String username;
    private String password;
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
        if(username.isEmpty()){
            setMessage("Username must not null");
            return false;
        }
        if(password.isEmpty()){
            setMessage("Password must not null");
            return false;
        }
        return true;
    }
    private final static Qsys_login dataPuller = Qsys_login.sys_login;
    public final static Map<String, ComparableExpressionBase<?>> sortProperties = new HashMap<String, ComparableExpressionBase<?>>() {{
        put("id", dataPuller.id);
        put("employee_code", dataPuller.employee_code);
        put("username", dataPuller.username);
        put("password", dataPuller.password);
        put("remark", dataPuller.remark);
        put("create_date", dataPuller.create_date);
        put("create_by", dataPuller.create_by);
        put("update_date", dataPuller.update_date);
        put("update_by", dataPuller.update_by);
    }};
}
