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
@Table(name = "plm_revision_rule_master")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class plm_revision_rule_master implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        plm_revision_rule_master that = (plm_revision_rule_master) o;
        return (this.id != null && Objects.equals(this.id, that.id));
    }

    public int hashCode() {
        return getClass().hashCode();
    }
}