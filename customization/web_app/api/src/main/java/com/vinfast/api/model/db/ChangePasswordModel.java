package com.vinfast.api.model.db;


import com.vinfast.api.model.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordModel extends BaseModel {
    private String loginName;
    private String currentPass;
    private String newPass;
    private String reNewPass;

    public boolean validData() {
        if (loginName.isEmpty()) {
            setMessage("Login name is empty");
            return false;
        }
        if (currentPass.isEmpty()) {
            setMessage("Current password is empty");
            return false;
        }
        if (newPass.isEmpty()) {
            setMessage("New password is empty");
            return false;
        }
        if (newPass.compareTo(reNewPass) != 0) {
            setMessage("Confirm new pass not correct");
            return false;
        }
        if (newPass.length() < 6 || newPass.length() > 20) {
            setMessage("Length of password from 6 to 20 letter");
            return false;
        }
        if (newPass.contains(" ")) {
            setMessage("Password have space letter");
            return false;
        }
        return true;
    }
}
