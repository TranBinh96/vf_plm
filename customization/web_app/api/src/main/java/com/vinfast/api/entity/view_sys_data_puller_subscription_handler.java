package com.vinfast.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Entity
@Table(name = "view_sys_data_puller_subscription_handler")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Immutable
public class view_sys_data_puller_subscription_handler {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tc_event_type;
    private String object_type;
    private int event_id;
    private String event_name;
}
