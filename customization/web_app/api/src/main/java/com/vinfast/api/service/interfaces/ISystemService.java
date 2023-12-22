package com.vinfast.api.service.interfaces;

import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.model.common.BasePagingModel;
import com.vinfast.api.model.db.ChangePasswordModel;
import com.vinfast.api.model.db.EmployeeModel;
import com.vinfast.api.model.db.EmployeePagingModel;
import org.springframework.stereotype.Service;

@Service
public interface ISystemService {
    BaseModel login(String userName, String password);
    BaseModel changePassword(ChangePasswordModel model);
    EmployeeModel employee_GetByCode(String employeeCode);
    BasePagingModel<EmployeeModel> employee_GetAll(EmployeePagingModel listModel);
    BaseModel employee_Create(EmployeeModel model);
    BaseModel employee_Update(EmployeeModel model);
}
