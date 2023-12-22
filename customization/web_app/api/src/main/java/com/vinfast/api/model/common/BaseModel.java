package com.vinfast.api.model.common;

import com.vinfast.api.common.constants.ErrorCodeConst;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BaseModel {
    private String errorCode = ErrorCodeConst.SUCCESS;
    private String message = "";
}
