package com.vinfast.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "plm_bom_verify_master")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class plm_bom_verify_master implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bom_type;
    private String program_name;
    private String module_name;
    private String part_number;
    private String revision_rule;
    private String notifiers;
    private Boolean is_active;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        plm_bom_verify_master that = (plm_bom_verify_master) o;
        return (this.id != null && Objects.equals(this.id, that.id));
    }

    public int hashCode() {
        return getClass().hashCode();
    }
}