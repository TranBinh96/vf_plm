package com.vinfast.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;

@Entity
@Table(name = "tsc_job_master")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class tsc_job_master implements Serializable {
    @Id
    private String uid;
    private String job_name;
    private int job_category_id;
    private String task_name;
    private String status;
    private String schedule_type;
    private Time start_time;
    private String schedule_days;
    private String schedule_months;
    private String repeat_every;
    private String remark;
    private Timestamp create_date;
    private String create_by;
    private Timestamp update_date;
    private String update_by;
}

