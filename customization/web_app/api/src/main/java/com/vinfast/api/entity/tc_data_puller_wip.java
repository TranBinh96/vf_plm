package com.vinfast.api.entity;

import lombok.*;
import org.hibernate.Hibernate;
import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "tc_data_puller_wip")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class tc_data_puller_wip implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String uid;
    private Timestamp created_on;
    private int failed_counter;
    private String error_log;
    private Timestamp last_update;
    private String status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        tc_data_puller_wip that = (tc_data_puller_wip) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
