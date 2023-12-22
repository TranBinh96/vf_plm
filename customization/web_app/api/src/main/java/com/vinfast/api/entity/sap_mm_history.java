package com.vinfast.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "sap_mm_history")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class sap_mm_history {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        sap_mm_history that = (sap_mm_history) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
