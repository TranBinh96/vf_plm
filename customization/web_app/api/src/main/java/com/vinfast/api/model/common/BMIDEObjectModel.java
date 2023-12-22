package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BMIDEObjectModel extends BaseModel {
    private int rowIndex;
    private String realValue;
    private String displayValue;
    private String fieldType;
    private String serverType;
    private String serverPropertyType;
    private String clientType;
    private String compoundType;
    private Boolean isArray;
    private Boolean isDisplayable;
    private Boolean isEnabled;
    private Boolean isModifiable;
    private String lov;
}