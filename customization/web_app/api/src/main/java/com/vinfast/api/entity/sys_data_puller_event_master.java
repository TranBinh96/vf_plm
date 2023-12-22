package com.vinfast.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "sys_data_puller_event_master")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class sys_data_puller_event_master {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        sys_data_puller_event_master that = (sys_data_puller_event_master) o;
        return (this.id != null && Objects.equals(this.id, that.id));
    }

    public int hashCode() {
        return getClass().hashCode();
    }
}
