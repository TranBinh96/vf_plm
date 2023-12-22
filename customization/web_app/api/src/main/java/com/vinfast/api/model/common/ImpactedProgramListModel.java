package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImpactedProgramListModel extends BaseListModel<ImpactedProgramModel> {
    private String[] uid = null;
}
