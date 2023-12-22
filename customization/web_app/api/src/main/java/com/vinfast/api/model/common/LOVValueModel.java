package com.vinfast.api.model.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class LOVValueModel extends BaseModel {
    private String value;
    private String display;
    private String description;
}
