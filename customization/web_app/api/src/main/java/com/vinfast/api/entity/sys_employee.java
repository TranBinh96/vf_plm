package com.vinfast.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "sys_employee")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class sys_employee implements Serializable {
    @Id
    private String employee_code;

    private String employee_name;
    private String email;
    private String phone_number;
    private Integer avatarID;
    private String service_desk_id;
    private String service_desk_name;
    private String remark;
    private Timestamp create_date;
    private String create_by;
    private Timestamp update_date;
    private String update_by;
}
