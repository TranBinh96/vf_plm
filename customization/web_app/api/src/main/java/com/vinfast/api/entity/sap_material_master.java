package com.vinfast.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "sap_material_master")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class sap_material_master implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_to", length = 16, nullable = false)
    private String transfer_to;

    @Column(name = "part_number", length = 16, nullable = false)
    private String part_number;

    @Column(name = "part_name", length = 128, nullable = false)
    private String part_name;

    @Column(name = "vn_description", length = 128)
    private String vn_description;

    @Column(name = "revision", length = 3, nullable = false)
    private String revision;

    @Column(name = "ecr_number", length = 128)
    private String ecr_number;

    @Column(name = "change_number", length = 32)
    private String change_number;

    @Column(name = "old_partnumber", length = 128)
    private String old_partnumber;

    @Column(name = "uom", length = 8, nullable = false)
    private String uom;

    @Column(name = "net_weight", length = 16)
    private String net_weight;

    @Column(name = "gross_weight", length = 16)
    private String gross_weight;

    @Column(name = "material_type", length = 4, nullable = false)
    private String material_type;

    @Column(name = "functional_class", length = 8)
    private String functional_class;

    @Column(name = "approval_code", length = 8)
    private String approval_code;

    @Column(name = "tracable_part", length = 128, nullable = false)
    private String tracable_part;

    @Column(name = "gm_part_number", length = 16)
    private String gm_part_number;

    @Column(name = "broadcast_code", length = 8)
    private String broadcast_code;

    @Column(name = "context", length = 24)
    private String context;

    @Column(name = "creation_date", nullable = false)
    private Timestamp creation_date;

    @Column(name = "last_update_on")
    private Timestamp last_update_on;

    @Column(name = "status", length = 10, nullable = false)
    private String status;

    @Column(name = "failed_counter", length = 128, nullable = false)
    private int failed_counter;

    @Column(name = "sync_status", length = 8)
    private String sync_status;

    @Column(name = "error_log", length = 256)
    private String error_log;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        sap_material_master that = (sap_material_master) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
