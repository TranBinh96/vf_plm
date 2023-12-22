package com.vinfast.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "tsc_job_history")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class tsc_job_history implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String job_uid;
    private String status;
    private String message;
    private String remark;
    private Timestamp start_time;
    private Timestamp end_time;
}

