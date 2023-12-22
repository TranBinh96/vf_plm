package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissingFileInAWSModel extends BaseModel {
    private int rowIndex;
    private String fullName;
    private String bomName;

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    private String bomType;
    private String program;
    private String releaseRule;
    private String fileName;

    public void setFullName(String fullName) {
        this.fullName = fullName;
        if (fullName.contains("/")) {
            String[] parts = fullName.split("/");
            if (parts.length > 2) {
                String top = parts[0];
                this.bomType = parts[1];
                this.fileName = parts[2];
                if (top.contains("-")) {
                    String[] part1 = top.split("-");
                    if (part1.length > 2) {
                        this.bomName = part1[0];
                        this.program = part1[1];
                        this.releaseRule = part1[2];
                    }
                }
            }
        }
    }
}