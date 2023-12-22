package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ImpactedProgramModel extends BaseModel {
    private String partNo;
    private Set<String> programList = new HashSet<>();

    public void addProgram(String program) {
        programList.add(program);
    }
}
