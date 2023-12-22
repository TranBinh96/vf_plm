package com.vinfast.api.service.implement;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.vinfast.api.common.constants.ErrorCodeConst;
import com.vinfast.api.entity.Qsys_employee;
import com.vinfast.api.entity.Qsys_login;
import com.vinfast.api.entity.sys_employee;
import com.vinfast.api.entity.sys_login;
import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.model.common.BasePagingModel;
import com.vinfast.api.model.db.ChangePasswordModel;
import com.vinfast.api.model.db.DataPullerEventMasterModel;
import com.vinfast.api.model.db.EmployeeModel;
import com.vinfast.api.model.db.EmployeePagingModel;
import com.vinfast.api.service.interfaces.ISystemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Component
public class SystemService implements ISystemService {
    private static final Logger LOGGER = LogManager.getLogger(SystemService.class);

    @PersistenceContext
    private EntityManager em;

    private final static Qsys_login loginTable = Qsys_login.sys_login;
    private final static Qsys_employee employeeTable = Qsys_employee.sys_employee;

    public BaseModel login(String userName, String password) {
        BaseModel modelReturn = new BaseModel();
        try {
            JPAQuery<sys_login> dbContext = new JPAQuery<>(em);
            sys_login dataReturn = dbContext.from(loginTable).where(loginTable.username.eq(userName)).fetchOne();
            if (dataReturn == null)
                throw new Exception("Username not exist");

            if (dataReturn.getPassword().compareTo(password) != 0)
                throw new Exception("Password not correct");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel changePassword(ChangePasswordModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (!model.validData())
                throw new Exception(model.getMessage());

            JPAQuery<sys_login> dbContext = new JPAQuery<>(em);
            sys_login dataReturn = dbContext.from(loginTable).where(loginTable.username.eq(model.getLoginName())).fetchOne();
            if (dataReturn == null)
                throw new Exception("Username not exist");

            JPAUpdateClause updateClause = new JPAQueryFactory(em).update(loginTable);
            updateClause.where(loginTable.username.eq(model.getLoginName()));
            updateClause.set(loginTable.password, model.getNewPass());
            long result = updateClause.execute();
            if (result == 0)
                throw new Exception("Update not success");
            modelReturn.setMessage("Update success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public EmployeeModel employee_GetByCode(String employeeCode) {
        EmployeeModel modelReturn = new EmployeeModel();
        try {
            JPAQuery<sys_employee> dbContext = new JPAQuery<>(em);
            List<sys_employee> emList = dbContext.from(employeeTable).fetch();
            sys_employee item = dbContext.from(employeeTable).where(employeeTable.employee_code.eq(employeeCode)).fetchOne();
            if (item == null)
                throw new Exception("Employee not exist");

            modelReturn.setEmployee_code(item.getEmployee_code());
            modelReturn.setEmployee_name(item.getEmployee_name());
            modelReturn.setEmail(item.getEmail());
            modelReturn.setPhone_number(item.getPhone_number());
            modelReturn.setRemark(item.getRemark());
            modelReturn.setService_desk_id(item.getService_desk_id());
            modelReturn.setService_desk_name(item.getService_desk_name());
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public BasePagingModel<EmployeeModel> employee_GetAll(EmployeePagingModel listModel) {
        try {
            JPAQuery<sys_employee> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(employeeTable);
            if (!listModel.getEmployeeCode().isEmpty())
                jpaQuery.where(employeeTable.employee_code.eq(listModel.getEmployeeCode()));
            if (!listModel.getEmployeeName().isEmpty())
                jpaQuery.where(employeeTable.employee_name.eq(listModel.getEmployeeName()));

            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(EmployeeModel.sortProperties.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(EmployeeModel.sortProperties.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(EmployeeModel.sortProperties.get("employee_code").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (sys_employee item : jpaQuery.fetch()) {
                EmployeeModel model = new EmployeeModel();
                model.setRowIndex(++index);
                model.setEmployee_code(item.getEmployee_code());
                model.setEmployee_name(item.getEmployee_name());
                model.setEmail(item.getEmail());
                model.setPhone_number(item.getPhone_number());
                model.setService_desk_id(item.getService_desk_id());
                model.setService_desk_name(item.getService_desk_name());
                model.setRemark(item.getRemark());

                listModel.dataList.add(model);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }
        return listModel;
    }

    @Transactional
    public BaseModel employee_Create(EmployeeModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            this.em.createNativeQuery("INSERT INTO sys_employee (employee_code, employee_name, email, phone_number, service_desk_id, service_desk_name, remark) VALUES ('"
                            + model.getEmployee_code() + "','"
                            + model.getEmployee_name() + "','"
                            + model.getEmail() + "','"
                            + model.getPhone_number() + "','"
                            + model.getService_desk_id() + "','"
                            + model.getService_desk_name() + "','"
                            + model.getRemark() + "')")
                    .executeUpdate();
            modelReturn.setMessage("Create New Success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel employee_Update(EmployeeModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            JPAQuery<sys_employee> jpaQuery = new JPAQuery<>(em);
            sys_employee item = jpaQuery.from(employeeTable).where(employeeTable.employee_code.eq(model.getEmployee_code())).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");

            JPAUpdateClause updateClause = new JPAQueryFactory(this.em).update(employeeTable);
            updateClause.where(employeeTable.employee_code.eq(model.getEmployee_code()));
            updateClause.set(employeeTable.employee_name, model.getEmployee_name());
            updateClause.set(employeeTable.email, model.getEmail());
            updateClause.set(employeeTable.phone_number, model.getPhone_number());
            updateClause.set(employeeTable.service_desk_id, model.getService_desk_id());
            updateClause.set(employeeTable.service_desk_name, model.getService_desk_name());
            updateClause.set(employeeTable.remark, model.getRemark());
            long result = updateClause.execute();
            if (result == 0L)
                throw new Exception("Update not success");
            modelReturn.setMessage("Update success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }
}
