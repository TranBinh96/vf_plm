package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ECRStatusModel extends BaseModel {
    private String ecrNumber;
    private String status;
}