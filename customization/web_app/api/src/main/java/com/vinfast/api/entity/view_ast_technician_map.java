package com.vinfast.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Entity
@Table(name = "view_ast_technician_map")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Immutable
public class view_ast_technician_map {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String employee_code;
    private Boolean is_active;
    private String key_word;
    private String employee_name;
    private String service_desk_id;
    private String service_desk_name;
}
