package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;

@Getter
@Setter
public class BaseFileModel extends BaseModel{
    private ByteArrayOutputStream file;
}
