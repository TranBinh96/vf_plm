package com.vinfast.api.service.implement;

import com.querydsl.jpa.impl.JPAQuery;
import com.vinfast.api.common.constants.ErrorCodeConst;
import com.vinfast.api.entity.Qsap_material_master;
import com.vinfast.api.entity.sap_material_master;
import com.vinfast.api.model.common.BasePagingModel;
import com.vinfast.api.model.common.SelectItem;
import com.vinfast.api.model.dataForm.DataFormSapMaterialMasterList;
import com.vinfast.api.model.db.SapMaterialMasterModel;
import com.vinfast.api.model.db.SapMaterialMasterPagingModel;
import com.vinfast.api.service.interfaces.ISAPService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
public class SAPService implements ISAPService {
    private static final Logger LOGGER = LogManager.getLogger(com.vinfast.api.service.implement.DatabaseService.class);
    @PersistenceContext
    private EntityManager em;

    private static final Qsap_material_master sapMaterialMasterTable = Qsap_material_master.sap_material_master;
    public DataFormSapMaterialMasterList sapMaterialMasterList_GetDataForm() {
        DataFormSapMaterialMasterList modelReturn = new DataFormSapMaterialMasterList();
        modelReturn.statusList.add(new SelectItem("FAILED", "FAILED"));
        modelReturn.statusList.add(new SelectItem("PROCESSED", "PROCESSED"));
        modelReturn.statusList.add(new SelectItem("REJECTED", "REJECTED"));
        modelReturn.statusList.add(new SelectItem("QUEUED", "QUEUED"));
        return modelReturn;
    }

    public BasePagingModel<SapMaterialMasterModel> sapMaterialMaster_GetAll(SapMaterialMasterPagingModel listModel) {
        try {
            JPAQuery<sap_material_master> jpaQuery = new JPAQuery<>(em);
            jpaQuery.from(sapMaterialMasterTable);
            if(!listModel.getMaterialType().isEmpty())
                jpaQuery.where(sapMaterialMasterTable.material_type.contains(listModel.getMaterialType()));
            if(!listModel.getStatus().isEmpty())
                jpaQuery.where(sapMaterialMasterTable.status.contains(listModel.getStatus()));
            if (!listModel.getPartNumber().isEmpty())
                jpaQuery.where(sapMaterialMasterTable.part_number.contains(listModel.getPartNumber()));
            if (!listModel.getPartName().isEmpty())
                jpaQuery.where(sapMaterialMasterTable.part_name.contains(listModel.getPartName()));
            if (listModel.getCreateFromDate() != null)
                jpaQuery.where(sapMaterialMasterTable.creation_date.after(listModel.getCreateFromDate()));
            if (listModel.getCreateToDate() != null)
                jpaQuery.where(sapMaterialMasterTable.creation_date.before(listModel.getCreateToDate()));
            if (listModel.getUpdateFromDate() != null)
                jpaQuery.where(sapMaterialMasterTable.last_update_on.after(listModel.getCreateFromDate()));
            if (listModel.getUpdateToDate() != null)
                jpaQuery.where(sapMaterialMasterTable.last_update_on.before(listModel.getUpdateToDate()));
            if (!listModel.getSortColumn().isEmpty() && !listModel.getSortColumnDir().isEmpty()) {
                if (listModel.isDesc()) {
                    jpaQuery.orderBy(SapMaterialMasterModel.sortProperties.get(listModel.getSortColumn()).desc());
                } else {
                    jpaQuery.orderBy(SapMaterialMasterModel.sortProperties.get(listModel.getSortColumn()).asc());
                }
            } else {
                jpaQuery.orderBy(SapMaterialMasterModel.sortProperties.get("id").desc());
            }
            listModel.setTotalItems(jpaQuery.stream().count());
            jpaQuery.offset(listModel.getOffset()).limit(listModel.itemsPerPage);
            int index = 0;
            for (sap_material_master item : jpaQuery.fetch()) {
                SapMaterialMasterModel model = new SapMaterialMasterModel();
                model.setRowIndex(++index);
                model.setId(item.getId());
                model.setTransfer_to(item.getTransfer_to());
                model.setPart_name(item.getPart_name());
                model.setPart_number(item.getPart_number());
                model.setVn_description(item.getVn_description());
                model.setRevision(item.getRevision());
                model.setOld_partnumber(item.getOld_partnumber());
                model.setUom(item.getUom());
                model.setNet_weight(item.getNet_weight());
                model.setGross_weight(item.getGross_weight());
                model.setMaterial_type(item.getMaterial_type());
                model.setFunctional_class(item.getFunctional_class());
                model.setApproval_code(item.getApproval_code());
                model.setTracable_part(item.getTracable_part());
                model.setGm_part_number(item.getGm_part_number());
                model.setBroadcast_code(item.getBroadcast_code());
                model.setContext(item.getContext());
                model.setCreation_date(item.getCreation_date());
                model.setLast_update_on(item.getLast_update_on());
                model.setStatus(item.getStatus());
                model.setFailed_counter(item.getFailed_counter());
                model.setSync_status(item.getSync_status());
                model.setError_log(item.getError_log());
                listModel.dataList.add(model);
            }
        } catch (Exception ex) {
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }
        return listModel;
    }

    public SapMaterialMasterModel sapMaterialMaster_GetByID(long id) {
        SapMaterialMasterModel model = new SapMaterialMasterModel();
        try {
            JPAQuery<sap_material_master> jpaQuery = new JPAQuery<>(em);
            sap_material_master item = jpaQuery.from(sapMaterialMasterTable).where(sapMaterialMasterTable.id.eq(id)).fetchOne();
            if (item == null)
                throw new Exception("Item not exist");
            model.setId(item.getId());
            model.setTransfer_to(item.getTransfer_to());
            model.setPart_name(item.getPart_name());
            model.setPart_number(item.getPart_number());
            model.setVn_description(item.getVn_description());
            model.setRevision(item.getRevision());
            model.setOld_partnumber(item.getOld_partnumber());
            model.setUom(item.getUom());
            model.setNet_weight(item.getNet_weight());
            model.setGross_weight(item.getGross_weight());
            model.setMaterial_type(item.getMaterial_type());
            model.setFunctional_class(item.getFunctional_class());
            model.setApproval_code(item.getApproval_code());
            model.setTracable_part(item.getTracable_part());
            model.setGm_part_number(item.getGm_part_number());
            model.setBroadcast_code(item.getBroadcast_code());
            model.setContext(item.getContext());
            model.setCreation_date(item.getCreation_date());
            model.setLast_update_on(item.getLast_update_on());
            model.setStatus(item.getStatus());
            model.setFailed_counter(item.getFailed_counter());
            model.setSync_status(item.getSync_status());
            model.setError_log(item.getError_log());
        } catch (Exception ex) {
            model.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            model.setMessage(ex.toString());
        }
        return model;
    }
}
