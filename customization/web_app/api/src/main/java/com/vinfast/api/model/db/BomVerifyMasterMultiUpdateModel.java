package com.vinfast.api.model.db;

import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BomVerifyMasterMultiUpdateModel extends BaseModel {
    private List<Long> idList = null;
    private List<String> emailList = null;
    private String typeUpdate = "";

    public boolean validData() {
        if (this.typeUpdate.isEmpty()) {
            setMessage("No type update.");
            return false;
        }
        if (this.idList.size() == 0) {
            setMessage("No list to update.");
            return false;
        }
        if (this.emailList.size() == 0) {
            setMessage("No email to update.");
            return false;
        }

        return true;
    }
}