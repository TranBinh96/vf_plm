package com.vinfast.api.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
@Getter
@Setter
public class AfterSaleServiceBomUpdateEvent extends ApplicationEvent {
    private String topBOMUID;
    public AfterSaleServiceBomUpdateEvent(Object source, String uid){
        super(source);
        this.topBOMUID = uid;
    }
}
