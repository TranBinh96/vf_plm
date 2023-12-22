package com.vinfast.api.service.implement;

import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core.LOVService;
import com.teamcenter.services.strong.core._2007_06.LOV.*;
import com.teamcenter.soa.client.model.strong.ListOfValuesString;
import com.teamcenter.soa.client.model.ClientMetaModel;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.teamcenter.soa.client.model.Type;
import com.teamcenter.soa.client.model.strong.*;
import com.vf4.services.loose.cm.ChangeManagementService;
import com.vf4.services.loose.cm._2020_12.ChangeManagement;
import com.vf4.services.loose.cm._2020_12.ChangeManagement.*;
import com.vinfast.api.common.constants.ErrorCodeConst;
import com.vinfast.api.common.constants.TeamcenterConst;
import com.vinfast.api.common.extensions.BOMExtension;
import com.vinfast.api.common.extensions.TCCommonExtension;
import com.vinfast.api.model.common.*;
import com.vinfast.api.model.dataForm.DataFormBMIDEObjectList;
import com.vinfast.api.service.interfaces.IEngineeringService;
import com.vinfast.connect.client.AppXSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EngineeringService implements IEngineeringService {
    private static final Logger LOGGER = LogManager.getLogger(EngineeringService.class);

    public BaseListModel<ECRStatusModel> ecrStatus_Get(ECRStatusListModel listModel) {
        try {
            RevisionRule revisionRule = BOMExtension.getRevisionRule("Any Status; Working");
            LinkedHashMap<String, String> queryInput = new LinkedHashMap<>();
            queryInput.put("Item ID", String.join(";", listModel.getEcrList()));
            queryInput.put("Type", "Engineering Change Request");
            DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
            ModelObject[] queryOutput = TCCommonExtension.executeQueryBuilder("Item...", queryInput, AppXSession.getConnection());
            dmService.refreshObjects(queryOutput);
            for (ModelObject object : queryOutput) {
                ItemRevision itemRevision = BOMExtension.getItemRevisionByRule((Item) object, revisionRule);
                dmService.getProperties(new ModelObject[]{itemRevision}, new String[]{"item_id", "release_status_list", "fnd0AllWorkflows"});
                String ecrNumber = itemRevision.getPropertyDisplayableValue("item_id");
                String status = itemRevision.getPropertyDisplayableValue("release_status_list");
                if (status.isEmpty()) {
                    String wf = itemRevision.getPropertyDisplayableValue("fnd0AllWorkflows");
                    if (wf.contains("VinFast ECR") || wf.contains("VinFast ECR FastTrack")) {
                        status = "In Progress";
                    } else {
                        status = "Not Triggered";
                    }
                }
                ECRStatusModel newItem = new ECRStatusModel();
                newItem.setEcrNumber(ecrNumber);
                newItem.setStatus(status);
                listModel.dataList.add(newItem);
            }
        } catch (Exception e) {
            LOGGER.info("[impactedProgram_Get] " + e.getMessage());
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(e.toString());
        }
        return listModel;
    }

    @Override
    public BaseListModel<ImpactedProgramModel> impactedProgram_Get(ImpactedProgramListModel listModel) {
        try {
            ModelObject[] partList = TCCommonExtension.getModelObjectsFromUIDs(listModel.getUid());

            ImpactedProgramInput taskInput = new ImpactedProgramInput();
            taskInput.topNodeType = new String[]{"VF3_vehicle"};
            taskInput.impactedParts = partList;
            taskInput.revisionRule = TeamcenterConst.REVISION_RULE_VINFAST_RELEASE;

            ChangeManagement cm = ChangeManagementService.getService(AppXSession.getConnection());
            ImpactedProgramResponse response = cm.getImpactedPrograms(taskInput);
            Map<ModelObject, ModelObject[]> impactedProgramTemp = response.outputs;
            if (impactedProgramTemp != null) {
                for (Map.Entry<ModelObject, ModelObject[]> entryItem : impactedProgramTemp.entrySet()) {
                    ModelObject partItem = entryItem.getKey();
                    TCCommonExtension.loadObject(new String[]{partItem.getUid()}, AppXSession.getConnection());
                    String partNo = partItem.getPropertyDisplayableValue("item_id");
                    ImpactedProgramModel newItem = null;
                    boolean check = true;
                    for (ImpactedProgramModel model : listModel.dataList) {
                        if (model.getPartNo().compareToIgnoreCase(partNo) == 0) {
                            newItem = model;
                            check = false;
                            break;
                        }
                    }
                    if (check) {
                        newItem = new ImpactedProgramModel();
                        newItem.setPartNo(partNo);
                    }

                    if (entryItem.getValue() != null) {
                        for (ModelObject program : entryItem.getValue()) {
                            TCCommonExtension.loadObject(new String[]{program.getUid()}, AppXSession.getConnection());
                            newItem.addProgram(program.getPropertyDisplayableValue("item_id"));
                        }
                    }
                    if (check)
                        listModel.dataList.add(newItem);
                }
            }

            taskInput.revisionRule = TeamcenterConst.REVISION_RULE_VINFAST_WORKING;
            response = cm.getImpactedPrograms(taskInput);
            impactedProgramTemp = response.outputs;
            if (impactedProgramTemp != null) {
                for (Map.Entry<ModelObject, ModelObject[]> entryItem : impactedProgramTemp.entrySet()) {
                    ModelObject partItem = entryItem.getKey();
                    TCCommonExtension.loadObject(new String[]{partItem.getUid()}, AppXSession.getConnection());
                    String partNo = partItem.getPropertyDisplayableValue("item_id");
                    ImpactedProgramModel newItem = null;
                    boolean check = true;
                    for (ImpactedProgramModel model : listModel.dataList) {
                        if (model.getPartNo().compareToIgnoreCase(partNo) == 0) {
                            newItem = model;
                            check = false;
                            break;
                        }
                    }
                    if (check) {
                        newItem = new ImpactedProgramModel();
                        newItem.setPartNo(partNo);
                    }

                    if (entryItem.getValue() != null) {
                        for (ModelObject program : entryItem.getValue()) {
                            TCCommonExtension.loadObject(new String[]{program.getUid()}, AppXSession.getConnection());
                            newItem.addProgram(program.getPropertyDisplayableValue("item_id"));
                        }
                    }
                    if (check)
                        listModel.dataList.add(newItem);
                }
            }
        } catch (Exception e) {
            LOGGER.info("[impactedProgram_Get] " + e.getMessage());
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(e.toString());
        }

        return listModel;
    }

    public BaseListModel<BMIDEObjectModel> bmideObject_GetList(BMIDEObjectListModel listModel) {
        try {
            ClientMetaModel metaModel = AppXSession.getConnection().getClientMetaModel();
            List<Type> types = metaModel.getTypes(new String[]{listModel.getObjectName()}, AppXSession.getConnection());
            if (types != null && types.size() > 0) {
                Hashtable<String, PropertyDescription> propDescs = ((Type) types.get(0)).getPropDescs();
                int i = 0;
                for (Object propName : new TreeSet(propDescs.keySet())) {
                    PropertyDescription propDesc = propDescs.get(propName);
                    BMIDEObjectModel newModel = new BMIDEObjectModel();
                    newModel.setRowIndex(++i);
                    newModel.setRealValue(propName.toString());
                    newModel.setDisplayValue(propDesc.getUiName());
                    newModel.setFieldType(TCCommonExtension.fieldTypeToString(propDesc.getFieldType()));
                    newModel.setServerType(TCCommonExtension.serverTypeToString(propDesc.getServerType()));
                    newModel.setServerPropertyType(TCCommonExtension.serverPropertyTypeToString(propDesc.getServerPropertyType()));
                    newModel.setClientType(TCCommonExtension.clientPropTypeToString(propDesc.getType()));
                    newModel.setCompoundType(propDesc.getCompoundObjectType());
                    newModel.setIsArray(Boolean.valueOf(propDesc.isArray()));
                    newModel.setIsDisplayable(Boolean.valueOf(propDesc.isDisplayable()));
                    newModel.setIsEnabled(Boolean.valueOf(propDesc.isEnabled()));
                    newModel.setIsModifiable(Boolean.valueOf(propDesc.isModifiable()));
                    newModel.setLov(TCCommonExtension.safeGetLOVName(propDesc.getLovReference()));
                    listModel.dataList.add(newModel);
                }
            }
        } catch (Exception e) {
            LOGGER.info("[bmideObject_Get] " + e);
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(e.toString());
        }
        return listModel;
    }

    public DataFormBMIDEObjectList bmideObjectList_GetDataForm() {
        DataFormBMIDEObjectList modelReturn = new DataFormBMIDEObjectList();
        String[] clientTypeList = {"ModelObject", "int", "date", "bool", "string", "double"};
        try {
            for (String type : clientTypeList)
                modelReturn.clientTypeList.add(new SelectItem(type, type));
        } catch (Exception ex) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(ex.toString());
        }
        return modelReturn;
    }

    public BaseListModel<LOVValueModel> lovValue_GetList(LOVValueListModel listModel) {
        DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
        LOVService lovService = LOVService.getService(AppXSession.getConnection());
        LOVInfo lovInfo = new LOVInfo();
        lovInfo.typeName = listModel.getObjectName();
        lovInfo.propNames = new String[]{listModel.getPropertyName()};

        try {
            AttachedLOVsResponse loVsResponse = lovService.getAttachedLOVs(new LOVInfo[]{lovInfo});

            if (loVsResponse.serviceData.sizeOfPartialErrors() == 0) {
                ModelObject mObj = loVsResponse.serviceData.getPlainObject(0);
                dmService.getProperties(new ModelObject[]{mObj}, new String[]{"lov_values", "lov_value_descriptions"});
                ListOfValuesString listOfvalues = (ListOfValuesString) mObj;
                String[] valueList = listOfvalues.get_lov_values();
                String[] descList = listOfvalues.get_lov_value_descriptions();
                List<String> displayList = listOfvalues.getPropertyDisplayableValues("lov_values");

                if (valueList.length > 0) {
                    for (int i = 0; i < valueList.length; i++) {
                        LOVValueModel newItem = new LOVValueModel();
                        newItem.setValue(valueList[i]);
                        newItem.setDisplay(displayList.get(i));
                        newItem.setDescription(descList[i]);
                        listModel.dataList.add(newItem);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            listModel.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            listModel.setMessage(ex.toString());
        }

        return listModel;
    }
}
