package com.vinfast.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "tsc_file_master")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class tsc_file_master {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String job_uid;

    private Integer bytes;

    private Integer rows;

    private String remark;

    private Timestamp create_date;

    private String create_by;

    private Timestamp update_date;

    private String update_by;
}
