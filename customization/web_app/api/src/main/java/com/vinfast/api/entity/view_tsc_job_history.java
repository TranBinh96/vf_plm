package com.vinfast.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "view_tsc_job_history")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Immutable
public class view_tsc_job_history implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String job_uid;
    private String job_name;
    private String task_name;
    private Integer job_category_id;
    private String job_category_name;
    private String status;
    private String message;
    private String remark;
    private Timestamp start_time;
    private Timestamp end_time;
}
