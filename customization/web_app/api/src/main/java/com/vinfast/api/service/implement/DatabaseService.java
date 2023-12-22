package com.vinfast.api.service.implement;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.vinfast.api.common.constants.CommonConst;
import com.vinfast.api.common.constants.ErrorCodeConst;
import com.vinfast.api.entity.*;
import com.vinfast.api.model.common.BaseModel;
import com.vinfast.api.model.common.BasePagingModel;
import com.vinfast.api.model.common.SelectItem;
import com.vinfast.api.model.dataForm.*;
import com.vinfast.api.model.db.*;
import com.vinfast.api.service.interfaces.IDatabaseService;

import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseService implements IDatabaseService {
    private static final Logger LOGGER = LogManager.getLogger(com.vinfast.api.service.implement.DatabaseService.class);
    @PersistenceContext
    private EntityManager em;
    private static final Qtc_data_puller_wip dataPullerTable = Qtc_data_puller_wip.tc_data_puller_wip;
    private static final Qplm_bom_verify_master bomVerifyMasterTable = Qplm_bom_verify_master.plm_bom_verify_master;
    private static final Qplm_revision_rule_master revisionRuleMasterTable = Qplm_revision_rule_master.plm_revision_rule_master;
    private static final Qemail_sender emailSenderTable = Qemail_sender.email_sender;
    private static final Qsys_data_puller_event_master dataPullerEventMasterTable = Qsys_data_puller_event_master.sys_data_puller_event_master;
    private static final Qsys_data_puller_subscription_handler dataPullerSubscriptionTable = Qsys_data_puller_subscription_handler.sys_data_puller_subscription_handler;
    private static final Qview_sys_data_puller_subscription_handler dataPullerSubscriptionView = Qview_sys_data_puller_subscription_handler.view_sys_data_puller_subscription_handler;
    private static final Qast_technician_map technicalMapTable = Qast_technician_map.ast_technician_map;
    private static final Qview_ast_technician_map technicalMapView = Qview_ast_technician_map.view_ast_technician_map;
    private static final Qsys_employee employeeTable = Qsys_employee.sys_employee;

    public DataFormTCPullerList dataPullerList_GetDataForm() {
        DataFormTCPullerList modelReturn = new DataFormTCPullerList();
        try {
            JPAQuery<sys_data_puller_event_master> eventQuery = new JPAQuery(this.em);
            eventQuery.from(dataPullerEventMasterTable);
            eventQuery.orderBy(DataPullerEventMasterModel.sortProperties.get("name").asc());
            for (sys_data_puller_event_master item : eventQuery.fetch())
                modelReturn.typeList.add(new SelectItem(item.getName(), item.getName()));

            modelReturn.statusList.add(new SelectItem("FAILED", "FAILED"));
            modelReturn.statusList.add(new SelectItem("PROCESSED", "PROCESSED"));
            modelReturn.statusList.add(new SelectItem("REJECTED", "REJECTED"));
            modelReturn.statusList.add(new SelectItem("QUEUED", "QUEUED"));
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public BasePagingModel<TCDataPullerWipModel> dataPuller_GetAll(TCDataPullerWipPagingModel listModel) {
        try {
            JPAQuery<tc_data_puller_wip> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(dataPullerTable);
            if (!listModel.getType().isEmpty())
                jpaQuery.where(dataPullerTable.type.eq(listModel.getType()));
            if (!listModel.getUid().isEmpty())
                jpaQuery.where(dataPullerTable.uid.contains(listModel.getUid()));
            if (!listModel.getStatus().isEmpty())
                jpaQuery.where(dataPullerTable.status.eq(listModel.getStatus()));
            if (!listModel.getError().isEmpty())
                jpaQuery.where(dataPullerTable.error_log.contains(listModel.getError()));
            if (listModel.getCreateFromDate() != null)
                jpaQuery.where(dataPullerTable.created_on.after(listModel.getCreateFromDate()));
            if (listModel.getCreateToDate() != null)
                jpaQuery.where(dataPullerTable.created_on.before(listModel.getCreateToDate()));
            if (listModel.getUpdateFromDate() != null)
                jpaQuery.where(dataPullerTable.last_update.after(listModel.getCreateFromDate()));
            if (listModel.getUpdateToDate() != null)
                jpaQuery.where(dataPullerTable.last_update.before(listModel.getUpdateToDate()));
            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(TCDataPullerWipModel.sortProperties.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(TCDataPullerWipModel.sortProperties.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(TCDataPullerWipModel.sortProperties.get("id").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (tc_data_puller_wip item : jpaQuery.fetch()) {
                TCDataPullerWipModel model = new TCDataPullerWipModel();
                model.setRowIndex(++index);
                model.setId(item.getId());
                model.setType(item.getType());
                model.setUid(item.getUid());
                model.setCreated_on(item.getCreated_on());
                model.setFailed_counter(item.getFailed_counter());
                model.setError_log(item.getError_log());
                model.setLast_update(item.getLast_update());
                model.setStatus(item.getStatus());
                if (item.getStatus().compareTo("QUEUED") == 0) {
                    model.setStatusColor(CommonConst.COLOR_STATUS_INFO);
                } else if (item.getStatus().compareTo("FAILED") == 0) {
                    model.setStatusColor(CommonConst.COLOR_STATUS_DANGER);
                } else if (item.getStatus().compareTo("REJECTED") == 0) {
                    model.setStatusColor(CommonConst.COLOR_STATUS_SECONDARY);
                } else if (item.getStatus().compareTo("PROCESSED") == 0) {
                    model.setStatusColor(CommonConst.COLOR_STATUS_SUCCESS);
                }
                listModel.dataList.add(model);
            }
        } catch (Exception ex) {
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }
        return listModel;
    }

    public TCDataPullerWipModel dataPuller_GetByID(long id) {
        TCDataPullerWipModel modelReturn = new TCDataPullerWipModel();
        try {
            JPAQuery<tc_data_puller_wip> jpaQuery = new JPAQuery<>(em);
            tc_data_puller_wip item = jpaQuery.from(dataPullerTable).where(dataPullerTable.id.eq(id)).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            modelReturn.setId(item.getId());
            modelReturn.setType(item.getType());
            modelReturn.setUid(item.getUid());
            modelReturn.setFailed_counter(item.getFailed_counter());
            modelReturn.setError_log(item.getError_log());
            modelReturn.setStatus(item.getStatus());
            modelReturn.setCreated_on(item.getCreated_on());
            modelReturn.setLast_update(item.getLast_update());
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel dataPuller_Create(TCDataPullerWipModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            this.em.createNativeQuery("INSERT INTO tc_data_puller_wip (uid, type, status) VALUES ('" + model.getUid() + "','" + model.getType() + "','" + model.getStatus() + "')")
                    .executeUpdate();
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel dataPuller_Update(TCDataPullerWipModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            JPAQuery<tc_data_puller_wip> jpaQuery = new JPAQuery<>(em);
            tc_data_puller_wip item = jpaQuery.from(dataPullerTable).where(dataPullerTable.id.eq(model.getId())).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            JPAUpdateClause updateClause = new JPAQueryFactory(em).update(dataPullerTable);
            updateClause.where(dataPullerTable.id.eq(model.getId()));
            updateClause.set(dataPullerTable.type, model.getType());
            updateClause.set(dataPullerTable.uid, model.getUid());
            updateClause.set(dataPullerTable.failed_counter, model.getFailed_counter());
            updateClause.set(dataPullerTable.error_log, model.getError_log());
            updateClause.set(dataPullerTable.status, model.getStatus());
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

    @Transactional
    public BaseModel dataPuller_MultiUpdate(TCDataPullerWipMultiUpdateModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());

            for (long id : model.getIdList()) {
                JPAQuery<tc_data_puller_wip> jpaQuery = new JPAQuery<>(em);
                tc_data_puller_wip item = jpaQuery.from(dataPullerTable).where(dataPullerTable.id.eq(id)).fetchOne();
                if (item != null) {
                    JPAUpdateClause updateClause = (new JPAQueryFactory(this.em)).update(dataPullerTable);
                    updateClause.where(dataPullerTable.id.eq(id));
                    updateClause.set(dataPullerTable.status, model.getStatus());
                    updateClause.execute();
                }
            }
            modelReturn.setMessage("Update success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public BasePagingModel<BomVerifyMasterModel> bomVerifyMaster_GetAll(BomVerifyMasterPagingModel listModel) {
        try {
            JPAQuery<plm_bom_verify_master> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(bomVerifyMasterTable);
            if (!listModel.getBomType().isEmpty())
                jpaQuery.where(bomVerifyMasterTable.bom_type.eq(listModel.getBomType()));
            if (!listModel.getProgramName().isEmpty())
                jpaQuery.where(bomVerifyMasterTable.program_name.eq(listModel.getProgramName()));
            if (!listModel.getModuleName().isEmpty())
                jpaQuery.where(bomVerifyMasterTable.module_name.eq(listModel.getModuleName()));
            if (!listModel.getRevisionRule().isEmpty())
                jpaQuery.where(bomVerifyMasterTable.revision_rule.eq(listModel.getRevisionRule()));
            if (!listModel.getNotifier().isEmpty())
                jpaQuery.where(bomVerifyMasterTable.notifiers.contains(listModel.getNotifier()));
            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(BomVerifyMasterModel.sortProperties.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(BomVerifyMasterModel.sortProperties.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(BomVerifyMasterModel.sortProperties.get("id").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (plm_bom_verify_master item : jpaQuery.fetch()) {
                BomVerifyMasterModel model = new BomVerifyMasterModel();
                model.setRowIndex(++index);
                model.setId(item.getId());
                model.setBom_type(item.getBom_type());
                model.setProgram_name(item.getProgram_name());
                model.setModule_name(item.getModule_name());
                model.setPart_number(item.getPart_number());
                model.setRevision_rule(item.getRevision_rule());
                model.setNotifiers(item.getNotifiers());
                model.setIs_active(item.getIs_active());
                listModel.dataList.add(model);
            }
            return listModel;
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
            return listModel;
        }
    }

    public BomVerifyMasterModel bomVerifyMaster_GetByID(long id) {
        BomVerifyMasterModel modelReturn = new BomVerifyMasterModel();
        try {
            JPAQuery<plm_bom_verify_master> jpaQuery = new JPAQuery<>(em);
            plm_bom_verify_master item = jpaQuery.from(bomVerifyMasterTable).where(bomVerifyMasterTable.id.eq(id)).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            modelReturn.setId(item.getId());
            modelReturn.setProgram_name(item.getProgram_name());
            modelReturn.setModule_name(item.getModule_name());
            modelReturn.setBom_type(item.getBom_type());
            modelReturn.setPart_number(item.getPart_number());
            modelReturn.setRevision_rule(item.getRevision_rule());
            modelReturn.setNotifiers(item.getNotifiers());
            modelReturn.setIs_active(item.getIs_active());
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel bomVerifyMaster_Create(BomVerifyMasterModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());

            this.em.createNativeQuery("INSERT INTO plm_bom_verify_master (bom_type, program_name, module_name, part_number, revision_rule, notifiers, is_active) VALUES ('" + model
                            .getBom_type() + "','" + model
                            .getProgram_name() + "','" + model
                            .getModule_name() + "','" + model
                            .getPart_number() + "','" + model
                            .getRevision_rule() + "','" + model
                            .getNotifiers() + "','" + model
                            .getIs_active() + "')")
                    .executeUpdate();
            modelReturn.setMessage("Create New Success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel bomVerifyMaster_Update(BomVerifyMasterModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());

            JPAQuery<plm_bom_verify_master> jpaQuery = new JPAQuery<>(em);
            plm_bom_verify_master item = jpaQuery.from(bomVerifyMasterTable).where(bomVerifyMasterTable.id.eq(model.getId())).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");

            JPAUpdateClause updateClause = new JPAQueryFactory(em).update(bomVerifyMasterTable);
            updateClause.where(bomVerifyMasterTable.id.eq(model.getId()));
            updateClause.set(bomVerifyMasterTable.bom_type, model.getBom_type());
            updateClause.set(bomVerifyMasterTable.program_name, model.getProgram_name());
            updateClause.set(bomVerifyMasterTable.module_name, model.getModule_name());
            updateClause.set(bomVerifyMasterTable.part_number, model.getPart_number());
            updateClause.set(bomVerifyMasterTable.revision_rule, model.getRevision_rule());
            updateClause.set(bomVerifyMasterTable.notifiers, model.getNotifiers());
            updateClause.set(bomVerifyMasterTable.is_active, model.getIs_active());
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

    @Transactional
    public BaseModel bomVerifyMaster_MultiUpdate(BomVerifyMasterMultiUpdateModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());

            for (long id : model.getIdList()) {
                JPAQuery<plm_bom_verify_master> jpaQuery = new JPAQuery<>(em);
                plm_bom_verify_master item = jpaQuery.from(bomVerifyMasterTable).where(bomVerifyMasterTable.id.eq(id)).fetchOne();
                if (item != null) {
                    JPAUpdateClause updateClause = (new JPAQueryFactory(this.em)).update(bomVerifyMasterTable);
                    updateClause.where(bomVerifyMasterTable.id.eq(id));
                    updateClause.set(bomVerifyMasterTable.notifiers, updateEmailList(item.getNotifiers(), model.getEmailList(), model.getTypeUpdate()));
                    updateClause.execute();
                }
            }
            modelReturn.setMessage("Update success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    private String updateEmailList(String currentList, List<String> emailList, String typeUpdate) {
        Set<String> output = new HashSet<>();
        if (currentList.contains(";")) {
            for (String str : currentList.split(";")) {
                if (!str.isEmpty())
                    output.add(str);
            }
        } else if (!currentList.isEmpty()) {
            output.add(currentList);
        }
        if (typeUpdate.compareTo("ADD") == 0) {
            output.addAll(emailList);
        } else {
            for (String str : emailList)
                output.remove(str);
        }
        return String.join(";", output);
    }

    public DataFormBomVerifyMasterList bomVerifyMasterList_GetDataForm() {
        DataFormBomVerifyMasterList modelReturn = new DataFormBomVerifyMasterList();
        String[] bomType = {"EBOM", "SBOM"};
        try {
            JPAQuery<plm_bom_verify_master> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(bomVerifyMasterTable);
            Set<String> programList = new HashSet<>();
            Set<String> moduleList = new HashSet<>();
            for (plm_bom_verify_master item : jpaQuery.fetch()) {
                programList.add(item.getProgram_name());
                moduleList.add(item.getModule_name());
            }
            for (String item : bomType)
                modelReturn.bomTypeList.add(new SelectItem(item, item));
            for (String program : programList) {
                if (program.isEmpty())
                    continue;
                modelReturn.programList.add(new SelectItem(program, program));
            }
            for (String module : moduleList) {
                if (module.isEmpty())
                    continue;
                modelReturn.moduleList.add(new SelectItem(module, module));
            }
            JPAQuery<plm_revision_rule_master> revisionRuleQuery = new JPAQuery(this.em);
            revisionRuleQuery.from(revisionRuleMasterTable);
            for (plm_revision_rule_master item : revisionRuleQuery.fetch())
                modelReturn.revisionRuleList.add(new SelectItem(item.getName(), item.getName()));
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public DataFormBomVerifyMasterUpdate bomVerifyMasterUpdate_GetDataForm() {
        DataFormBomVerifyMasterUpdate modelReturn = new DataFormBomVerifyMasterUpdate();
        String[] bomType = {"EBOM", "SBOM"};
        try {
            JPAQuery<plm_revision_rule_master> revisionRuleQuery = new JPAQuery<>(em);
            revisionRuleQuery.from(revisionRuleMasterTable);
            for (plm_revision_rule_master item : revisionRuleQuery.fetch())
                modelReturn.revisionRuleList.add(new SelectItem(item.getName(), item.getName()));
            for (String item : bomType)
                modelReturn.bomTypeList.add(new SelectItem(item, item));
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public BasePagingModel<EmailSenderModel> emailSender_GetAll(EmailSenderPagingModel listModel) {
        try {
            JPAQuery<email_sender> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(emailSenderTable);
            if (!listModel.getEmailTo().isEmpty())
                jpaQuery.where(emailSenderTable.email_to.contains(listModel.getEmailTo().trim()));
            if (!listModel.getSubject().isEmpty())
                jpaQuery.where(emailSenderTable.subject.contains(listModel.getSubject()));
            if (!listModel.getStatus().isEmpty())
                jpaQuery.where(emailSenderTable.status.eq(listModel.getStatus()));
            if (listModel.getCreateFromDate() != null)
                jpaQuery.where(dataPullerTable.created_on.after(listModel.getCreateFromDate()));
            if (listModel.getCreateToDate() != null)
                jpaQuery.where(dataPullerTable.created_on.before(listModel.getCreateToDate()));
            if (listModel.getUpdateFromDate() != null)
                jpaQuery.where(dataPullerTable.last_update.after(listModel.getCreateFromDate()));
            if (listModel.getUpdateToDate() != null)
                jpaQuery.where(dataPullerTable.last_update.before(listModel.getUpdateToDate()));
            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(EmailSenderModel.sortProperties.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(EmailSenderModel.sortProperties.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(EmailSenderModel.sortProperties.get("id").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (email_sender item : jpaQuery.fetch()) {
                EmailSenderModel model = new EmailSenderModel();
                model.setRowIndex(++index);
                model.setId(item.getId());
                model.setEmail_to(item.getEmail_to());
                model.setSubject(item.getSubject());
                model.setPriority(item.getPriority());
                model.setStatus(item.getStatus());
                model.setCreated_on(item.getCreated_on());
                model.setUpdated_on(item.getUpdated_on());
                listModel.dataList.add(model);
            }
            return listModel;
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
            return listModel;
        }
    }

    public EmailSenderModel emailSender_GetByID(long id) {
        EmailSenderModel modelReturn = new EmailSenderModel();
        try {
            JPAQuery<email_sender> jpaQuery = new JPAQuery<>(em);
            email_sender item = jpaQuery.from(emailSenderTable).where(emailSenderTable.id.eq(id)).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            modelReturn.setId(item.getId());
            modelReturn.setEmail_to(item.getEmail_to());
            modelReturn.setSubject(item.getSubject());
            modelReturn.setPriority(item.getPriority());
            modelReturn.setStatus(item.getStatus());
            modelReturn.setCreated_on(item.getCreated_on());
            modelReturn.setUpdated_on(item.getUpdated_on());
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel emailSender_Update(EmailSenderModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());

            JPAQuery<email_sender> jpaQuery = new JPAQuery<>(em);
            email_sender item = jpaQuery.from(emailSenderTable).where(emailSenderTable.id.eq(model.getId())).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            JPAUpdateClause updateClause = (new JPAQueryFactory(this.em)).update(emailSenderTable);
            updateClause.where(emailSenderTable.id.eq(model.getId()));
            updateClause.set(emailSenderTable.email_to, model.getEmail_to());
            updateClause.set(emailSenderTable.status, model.getStatus());
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

    public DataFormEmailSenderList emailSenderList_GetDataForm() {
        DataFormEmailSenderList modelReturn = new DataFormEmailSenderList();
        String[] statusList = {"QUEUE", "SENT", "FAILED", "PARKED", "INVALID_EMAIL"};
        try {
            for (String status : statusList)
                modelReturn.statusList.add(new SelectItem(status, status));
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public BasePagingModel<RevisionRuleMasterModel> revisionRuleMaster_GetAll(RevisionRuleMasterPagingModel listModel) {
        try {
            JPAQuery<plm_revision_rule_master> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(revisionRuleMasterTable);
            if (!listModel.getName().isEmpty())
                jpaQuery.where(revisionRuleMasterTable.name.eq(listModel.getName()));
            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(RevisionRuleMasterModel.sortProperties.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(RevisionRuleMasterModel.sortProperties.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(RevisionRuleMasterModel.sortProperties.get("id").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (plm_revision_rule_master item : jpaQuery.fetch()) {
                RevisionRuleMasterModel model = new RevisionRuleMasterModel();
                model.setRowIndex(++index);
                model.setId(item.getId());
                model.setName(item.getName());
                listModel.dataList.add(model);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }
        return listModel;
    }

    public RevisionRuleMasterModel revisionRuleMaster_GetByID(long id) {
        RevisionRuleMasterModel modelReturn = new RevisionRuleMasterModel();
        try {
            JPAQuery<plm_revision_rule_master> jpaQuery = new JPAQuery<>(em);
            plm_revision_rule_master item = jpaQuery.from(revisionRuleMasterTable).where(revisionRuleMasterTable.id.eq(id)).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            modelReturn.setId(item.getId());
            modelReturn.setName(item.getName());
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel revisionRuleMaster_Create(RevisionRuleMasterModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            this.em.createNativeQuery("INSERT INTO plm_revision_rule_master (name) VALUES ('" + model.getName() + "')")
                    .executeUpdate();
            modelReturn.setMessage("Create New Success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel revisionRuleMaster_Update(RevisionRuleMasterModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            JPAQuery<plm_revision_rule_master> jpaQuery = new JPAQuery<>(em);
            plm_revision_rule_master item = jpaQuery.from(revisionRuleMasterTable).where(revisionRuleMasterTable.id.eq(model.getId())).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");

            JPAUpdateClause updateClause = new JPAQueryFactory(this.em).update(revisionRuleMasterTable);
            updateClause.where(revisionRuleMasterTable.id.eq(model.getId()));
            updateClause.set(revisionRuleMasterTable.name, model.getName());
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

    public BasePagingModel<DataPullerEventMasterModel> dataPullerEventMaster_GetAll(DataPullerEventMasterPagingModel listModel) {
        try {
            JPAQuery<sys_data_puller_event_master> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(dataPullerEventMasterTable);
            if (!listModel.getName().isEmpty())
                jpaQuery.where(dataPullerEventMasterTable.name.eq(listModel.getName()));
            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(DataPullerEventMasterModel.sortProperties.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(DataPullerEventMasterModel.sortProperties.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(DataPullerEventMasterModel.sortProperties.get("id").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (sys_data_puller_event_master item : jpaQuery.fetch()) {
                DataPullerEventMasterModel model = new DataPullerEventMasterModel();
                model.setRowIndex(++index);
                model.setId(item.getId());
                model.setName(item.getName());
                listModel.dataList.add(model);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }
        return listModel;
    }

    public DataPullerEventMasterModel dataPullerEventMaster_GetByID(long id) {
        DataPullerEventMasterModel modelReturn = new DataPullerEventMasterModel();
        try {
            JPAQuery<sys_data_puller_event_master> jpaQuery = new JPAQuery<>(em);
            sys_data_puller_event_master item = jpaQuery.from(dataPullerEventMasterTable).where(dataPullerEventMasterTable.id.eq(id)).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            modelReturn.setId(item.getId());
            modelReturn.setName(item.getName());
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel dataPullerEventMaster_Create(DataPullerEventMasterModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            this.em.createNativeQuery("INSERT INTO sys_data_puller_event_master (name) VALUES ('" + model.getName() + "')")
                    .executeUpdate();
            modelReturn.setMessage("Create New Success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel dataPullerEventMaster_Update(DataPullerEventMasterModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            JPAQuery<sys_data_puller_event_master> jpaQuery = new JPAQuery<>(em);
            sys_data_puller_event_master item = jpaQuery.from(dataPullerEventMasterTable).where(dataPullerEventMasterTable.id.eq(model.getId())).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");

            JPAUpdateClause updateClause = new JPAQueryFactory(this.em).update(dataPullerEventMasterTable);
            updateClause.where(dataPullerEventMasterTable.id.eq(model.getId()));
            updateClause.set(dataPullerEventMasterTable.name, model.getName());
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

    public BasePagingModel<DataPullerSubScriptionHandlerModel> dataPullerSubscriptionHandler_GetAll(DataPullerSubScriptionHandlerPagingModel listModel) {
        try {
            JPAQuery<view_sys_data_puller_subscription_handler> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(dataPullerSubscriptionView);
            if (!listModel.getTc_event_type().isEmpty())
                jpaQuery.where(dataPullerSubscriptionView.tc_event_type.eq(listModel.getTc_event_type()));
            if (!listModel.getObject_type().isEmpty())
                jpaQuery.where(dataPullerSubscriptionView.object_type.eq(listModel.getObject_type()));
            if (listModel.getEvent_id() != null)
                jpaQuery.where(dataPullerSubscriptionView.event_id.eq(listModel.getEvent_id()));

            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(DataPullerSubScriptionHandlerModel.sortPropertiesView.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(DataPullerSubScriptionHandlerModel.sortPropertiesView.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(DataPullerSubScriptionHandlerModel.sortPropertiesView.get("id").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (view_sys_data_puller_subscription_handler item : jpaQuery.fetch()) {
                DataPullerSubScriptionHandlerModel model = new DataPullerSubScriptionHandlerModel();
                model.setRowIndex(++index);
                model.setId(item.getId());
                model.setTc_event_type(item.getTc_event_type());
                model.setObject_type(item.getObject_type());
                model.setEvent_name(item.getEvent_name());
                listModel.dataList.add(model);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }
        return listModel;
    }

    public DataPullerSubScriptionHandlerModel dataPullerSubscriptionHandler_GetByID(long id) {
        DataPullerSubScriptionHandlerModel modelReturn = new DataPullerSubScriptionHandlerModel();
        try {
            JPAQuery<sys_data_puller_subscription_handler> jpaQuery = new JPAQuery<>(em);
            sys_data_puller_subscription_handler item = jpaQuery.from(dataPullerSubscriptionTable).where(dataPullerSubscriptionTable.id.eq(id)).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            modelReturn.setId(item.getId());
            modelReturn.setTc_event_type(item.getTc_event_type());
            modelReturn.setObject_type(item.getObject_type());
            modelReturn.setEvent_id(item.getEvent_id());
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel dataPullerSubscriptionHandler_Create(DataPullerSubScriptionHandlerModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            this.em.createNativeQuery("INSERT INTO sys_data_puller_subscription_handler (tc_event_type, object_type, event_id) VALUES ('"
                            + model.getTc_event_type() + "','"
                            + model.getObject_type() + "',"
                            + model.getEvent_id()
                            + ")")
                    .executeUpdate();
            modelReturn.setMessage("Create New Success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel dataPullerSubscriptionHandler_Update(DataPullerSubScriptionHandlerModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            JPAQuery<sys_data_puller_subscription_handler> jpaQuery = new JPAQuery<>(em);
            sys_data_puller_subscription_handler item = jpaQuery.from(dataPullerSubscriptionTable).where(dataPullerSubscriptionTable.id.eq(model.getId())).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");

            JPAUpdateClause updateClause = new JPAQueryFactory(this.em).update(dataPullerSubscriptionTable);
            updateClause.where(dataPullerSubscriptionTable.id.eq(model.getId()));
            updateClause.set(dataPullerSubscriptionTable.tc_event_type, model.getTc_event_type());
            updateClause.set(dataPullerSubscriptionTable.object_type, model.getObject_type());
            updateClause.set(dataPullerSubscriptionTable.event_id, model.getEvent_id());
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

    public DataFormDataPullerSubscriptionList dataPullerSubscriptionList_GetDataForm() {
        DataFormDataPullerSubscriptionList modelReturn = new DataFormDataPullerSubscriptionList();
        try {
            JPAQuery<sys_data_puller_event_master> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(dataPullerEventMasterTable);
            for (sys_data_puller_event_master item : jpaQuery.fetch()) {
                modelReturn.eventList.add(new SelectItem(item.getId().toString(), item.getName()));
            }
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public BasePagingModel<TechnicianMapModel> technicianMap_GetAll(TechnicianMapPagingModel listModel) {
        try {
            JPAQuery<view_ast_technician_map> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(technicalMapView);

            if (!listModel.getEmployee_code().isEmpty())
                jpaQuery.where(technicalMapView.employee_code.eq(listModel.getEmployee_code()));

            if (!listModel.getEmployee_name().isEmpty())
                jpaQuery.where(technicalMapView.employee_name.contains(listModel.getEmployee_name()));

            if (!listModel.getKey_word().isEmpty())
                jpaQuery.where(technicalMapView.key_word.contains(listModel.getKey_word()));

            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(TechnicianMapModel.sortPropertiesView.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(TechnicianMapModel.sortPropertiesView.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(TechnicianMapModel.sortPropertiesView.get("id").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (view_ast_technician_map item : jpaQuery.fetch()) {
                TechnicianMapModel model = new TechnicianMapModel();
                model.setRowIndex(++index);
                model.setId(item.getId());
                model.setEmployee_code(item.getEmployee_code());
                model.setEmployee_name(item.getEmployee_name());
                model.setService_desk_id(item.getService_desk_id());
                model.setService_desk_name(item.getService_desk_name());
                model.setIs_active(item.getIs_active());
                model.setKey_word(item.getKey_word());
                listModel.dataList.add(model);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.toString());
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }
        return listModel;
    }

    public TechnicianMapModel technicianMap_GetByID(long id) {
        TechnicianMapModel modelReturn = new TechnicianMapModel();
        try {
            JPAQuery<ast_technician_map> jpaQuery = new JPAQuery<>(em);
            ast_technician_map item = jpaQuery.from(technicalMapTable).where(technicalMapTable.id.eq(id)).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            modelReturn.setId(item.getId());
            modelReturn.setEmployee_code(item.getEmployee_code());
            modelReturn.setIs_active(item.getIs_active());
            modelReturn.setKey_word(item.getKey_word());
            //   (?i).*?(\bAL\b|\bCM AL\b|\bECR\b).*?
            modelReturn.setKeywordListFromKeyword();
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel technicianMap_Create(TechnicianMapModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            this.em.createNativeQuery("INSERT INTO ast_technician_map (employee_code, is_active, key_word) VALUES ('" +
                            model.getEmployee_code() + "'," +
                            model.getIs_active() + ",'" +
                            model.getKeywordFromKeywordList() + "')")
                    .executeUpdate();
            modelReturn.setMessage("Create New Success");
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    @Transactional
    public BaseModel technicianMap_Update(TechnicianMapModel model) {
        BaseModel modelReturn = new BaseModel();
        try {
            if (model == null)
                throw new Exception("Model null");
            if (!model.validData())
                throw new Exception(model.getMessage());
            JPAQuery<ast_technician_map> jpaQuery = new JPAQuery<>(em);
            ast_technician_map item = jpaQuery.from(technicalMapTable).where(technicalMapTable.id.eq(model.getId())).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");

            JPAUpdateClause updateClause = new JPAQueryFactory(this.em).update(technicalMapTable);
            updateClause.where(technicalMapTable.id.eq(model.getId()));
            updateClause.set(technicalMapTable.is_active, model.getIs_active());
            updateClause.set(technicalMapTable.key_word, model.getKeywordFromKeywordList());
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

    public DataFormTechnicianMapUpdate technicianMapUpdate_GetDataForm() {
        DataFormTechnicianMapUpdate modelReturn = new DataFormTechnicianMapUpdate();
        try {
            JPAQuery<sys_employee> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(employeeTable);
            for (sys_employee item : jpaQuery.fetch()) {
                modelReturn.employeeList.add(new SelectItem(item.getEmployee_code(), item.getEmployee_name()));
            }
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }
}
