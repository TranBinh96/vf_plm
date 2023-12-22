package com.vinfast.api.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class AutoBaselineEvent extends ApplicationEvent {
    private String topBOMUID;
    private int numberEachPackage = 10;
    public AutoBaselineEvent(Object source, String uid){
        super(source);
        this.topBOMUID = uid;
    }
}
