package com.vinfast.api.model.db;

import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TCDataPullerWipMultiUpdateModel extends BaseModel {
    private List<Long> idList = null;
    private String status;

    public boolean validData() {
        if (this.idList.size() == 0) {
            setMessage("No list to update.");
            return false;
        }
        if (this.status.isEmpty()) {
            setMessage("No Status.");
            return false;
        }

        return true;
    }
}