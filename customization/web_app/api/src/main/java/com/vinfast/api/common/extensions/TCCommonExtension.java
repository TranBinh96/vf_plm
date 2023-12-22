package com.vinfast.api.common.extensions;

import com.teamcenter.services.strong.administration.PreferenceManagementService;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core.ReservationService;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.services.strong.core._2007_01.DataManagement;
import com.teamcenter.services.strong.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.strong.core._2007_12.Session;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.services.strong.query._2006_03.SavedQuery;
import com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.GetFileResponse;
import com.teamcenter.soa.client.model.*;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ImanQuery;
import com.teamcenter.soa.common.ObjectPropertyPolicy;
import com.vinfast.api.common.constants.CommonConst;
import com.teamcenter.soa.client.Connection;
import com.vinfast.connect.client.AppXSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class TCCommonExtension {
    private static final Logger LOGGER = LogManager.getLogger(TCCommonExtension.class);

    public static AppXSession loginTC(String user, String pass) throws Exception {
        AppXSession session = new AppXSession(CommonConst.PRODUCTION_SERVER, false);
        Connection tcConnect = AppXSession.getConnection();
        session.login(user, pass, "vfplm-soa");

        Session.StateNameValue properties = new Session.StateNameValue();
        properties.name = "bypassFlag";
        properties.value = Property.toBooleanString(true);
        SessionService.getService(tcConnect).setUserSessionState(new Session.StateNameValue[]{properties});

        PreferenceManagement.SetPreferences2In PSEShowUnconfigdVarPrefIn = new PreferenceManagement.SetPreferences2In();
        PSEShowUnconfigdVarPrefIn.preferenceName = "PSEShowUnconfigdVarPref";
        PSEShowUnconfigdVarPrefIn.values = new String[]{"0"};

        PreferenceManagement.PreferenceLocation preLocation = new PreferenceManagement.PreferenceLocation();
        preLocation.location = "User";

        PreferenceManagement.SetPreferencesAtLocationsIn setPSEShowUnconfigdVarPref = new PreferenceManagement.SetPreferencesAtLocationsIn();
        setPSEShowUnconfigdVarPref.location = preLocation;
        setPSEShowUnconfigdVarPref.preferenceInputs = PSEShowUnconfigdVarPrefIn;

        PreferenceManagement.SetPreferences2In PSEShowSuppressedOccsPrefIn = new PreferenceManagement.SetPreferences2In();
        PSEShowSuppressedOccsPrefIn.preferenceName = "PSEShowSuppressedOccsPref";
        PSEShowSuppressedOccsPrefIn.values = new String[]{"1"};

        PreferenceManagement.SetPreferencesAtLocationsIn setPSEShowSuppressedOccsPref = new PreferenceManagement.SetPreferencesAtLocationsIn();
        setPSEShowSuppressedOccsPref.location = preLocation;
        setPSEShowSuppressedOccsPref.preferenceInputs = PSEShowSuppressedOccsPrefIn;

        ServiceData serviceData = PreferenceManagementService.getService(tcConnect).setPreferencesAtLocations(new PreferenceManagement.SetPreferencesAtLocationsIn[]{setPSEShowUnconfigdVarPref, setPSEShowSuppressedOccsPref});
        if (serviceData.sizeOfPartialErrors() > 0) {
            System.out.println("[main] Can not update PSEShowUnconfigdVarPref");
        }
        LOGGER.info("Login Teamcenter success...");
        return session;
    }

    public static void logoutTC(AppXSession session, Connection tcConnect) {
        if (session != null && tcConnect != null) {
            LOGGER.info("Logout Teamcenter...");
            tcConnect.release();
            session.logout();
        }
    }

    public static FileManagementUtility getFileManagementUtility() throws Exception {
        String[] bootStrapURL = getPreference("Fms_BootStrap_Urls");
        return new FileManagementUtility(AppXSession.getConnection(), null, null, bootStrapURL, "");
    }

    public static String[] getPreference(String prefName) {
        String[] IP_values = null;
        PreferenceManagementService preferenceService = PreferenceManagementService.getService(AppXSession.getConnection());
        PreferenceManagement.GetPreferencesResponse preferencesResponse = preferenceService.refreshPreferences2(new String[]{prefName}, false);
        PreferenceManagement.CompletePreference[] pref = preferencesResponse.response;

        if (pref.length != 0) {
            PreferenceManagement.PreferenceValue values = pref[0].values;
            IP_values = values.values;
        }
        return IP_values;
    }

    public static String hanlderServiceData(ServiceData sd) {
        StringBuilder strOut = new StringBuilder();
        if (sd.sizeOfPartialErrors() > 0) {
            ErrorStack errorStack = sd.getPartialError(sd.sizeOfPartialErrors() - 1);
            ErrorValue[] errorValue = errorStack.getErrorValues();
            for (ErrorValue er : errorValue) {
                if (strOut.length() > 0)
                    strOut.append(", ");
                strOut.append(er.getMessage());
            }
        }
        return strOut.toString();
    }

    public static void loadObjectPolicy(LinkedHashMap<String, String[]> type, Connection tcConnect) {
        ObjectPropertyPolicy policy = new ObjectPropertyPolicy();
        for (Map.Entry<String, String[]> entry : type.entrySet()) {
            policy.addType(entry.getKey(), entry.getValue());
        }

        SessionService sessionService = SessionService.getService(tcConnect);
        sessionService.setObjectPropertyPolicy(policy);
    }

    public static ModelObject[] executeQueryBuilder(String queryName, LinkedHashMap<String, String> queryInput, Connection connection) {
        ImanQuery query = null;
        ModelObject object;

        try {
            SavedQueryService queryService = SavedQueryService.getService(connection);
            DataManagementService dmService = DataManagementService.getService(connection);
            SavedQuery.GetSavedQueriesResponse savedQueries = queryService.getSavedQueries();

            if (savedQueries.queries.length == 0) {
                LOGGER.error("[executeQueryBuilder] Exception: There are no saved queries in the system.");
                return null;
            }

            for (int i = 0; i < savedQueries.queries.length; i++) {
                if (savedQueries.queries[i].name.equals(queryName)) {
                    query = savedQueries.queries[i].query;
                    break;
                }
            }

            if (query == null) {
                LOGGER.error("[executeQueryBuilder] Exception: Could not find query.");
                return null;
            }

            com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput[] savedQueryInput = new com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput[1];
            savedQueryInput[0] = new com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput();
            savedQueryInput[0].query = query;
            savedQueryInput[0].entries = new String[queryInput.size()];
            savedQueryInput[0].values = new String[queryInput.size()];

            int i = 0;
            for (Map.Entry<String, String> pair : queryInput.entrySet()) {
                savedQueryInput[0].entries[i] = pair.getKey();
                savedQueryInput[0].values[i] = pair.getValue();
                i++;
            }

            com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse savedQueryResult = queryService.executeSavedQueries(savedQueryInput);
            com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults found = savedQueryResult.arrayOfResults[0];
            ModelObject[] listModelObj = new ModelObject[found.objectUIDS.length];
            if (found.objectUIDS.length > 0) {
                ServiceData sd = dmService.loadObjects(found.objectUIDS);
                for (int j = 0; j < found.objectUIDS.length; ++j) {
                    if (sd.sizeOfPlainObjects() > 0) {
                        object = sd.getPlainObject(j);
                        listModelObj[j] = object;
                    }
                }
                return listModelObj;
            }
        } catch (Exception e) {
            LOGGER.error(e);

        }

        return null;
    }

    public static File downloadFile(FileManagementUtility fileUtility, ImanFile tcFile, String path) {
        File fileDestination = new File(path);
        if (fileDestination.exists())
            return fileDestination;
        GetFileResponse fileResp = fileUtility.getFiles(new ModelObject[]{tcFile});
        File[] nameReffiles = fileResp.getFiles();
        for (int j = 0; j < nameReffiles.length; j++) {
            for (File singlerefFile : nameReffiles) {
                File fTmpDest = new File(path);
                singlerefFile.renameTo(fTmpDest);
                return fTmpDest;
            }
        }
        return null;
    }

    public static void loadObject(String[] uid, Connection conn) {
        DataManagementService dmService = DataManagementService.getService(conn);
        ServiceData sd = dmService.loadObjects(uid);
        if (sd.sizeOfPartialErrors() > 0) {
            LOGGER.error("[TCCommonExtension] vfLoadObject: " + TCCommonExtension.hanlderServiceData(sd));
        }
    }

    public static String getPropertyRealValue(ModelObject item, String propertyName) {
        try {
            Property moduleGroupProperty = item.getPropertyObject(propertyName);
            if (moduleGroupProperty != null) {
                return moduleGroupProperty.getStringValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String[] getPropertyRealValues(ModelObject item, String propertyName) {
        try {
            Property moduleGroupProperty = item.getPropertyObject(propertyName);
            if (moduleGroupProperty != null) {
                return moduleGroupProperty.getStringArrayValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ModelObject getModelObjectsFromUID(String uid) {
        DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
        ServiceData data = dmService.loadObjects(new String[]{uid});
        if(data.sizeOfPartialErrors() > 0) {
            LOGGER.error(TCCommonExtension.hanlderServiceData(data));
            return null;
        }

        return data.getPlainObject(0);
    }

    public static ModelObject[] getModelObjectsFromUIDs(String[] arrayOfUIDs) {
        List<ModelObject> objectList = new LinkedList<>();

        if (arrayOfUIDs.length > 0) {
            DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
            ServiceData data = dmService.loadObjects(arrayOfUIDs);
            if(data.sizeOfPartialErrors() > 0) {
                LOGGER.error(TCCommonExtension.hanlderServiceData(data));
                return null;
            }

            for (int i = 0; i < data.sizeOfPlainObjects(); i++) {
                objectList.add(data.getPlainObject(i));
            }
        }

        return objectList.toArray(new ModelObject[0]);
    }

    public static void checkInObject(ModelObject[] modelObjects) throws Exception {
        ReservationService resServ = ReservationService.getService(AppXSession.getConnection());
        ServiceData data = resServ.checkin(modelObjects);
        if (data.sizeOfPartialErrors() > 0) {
            LOGGER.error(TCCommonExtension.hanlderServiceData(data));
            throw new Exception(TCCommonExtension.hanlderServiceData(data));
        }
    }

    public static void checkOutObject(ModelObject[] modelObjects) throws Exception {
        ReservationService resServ = ReservationService.getService(AppXSession.getConnection());
        ServiceData data = resServ.checkout(modelObjects, "", "");
        if (data.sizeOfPartialErrors() > 0) {
            LOGGER.error(TCCommonExtension.hanlderServiceData(data));
            throw new Exception(TCCommonExtension.hanlderServiceData(data));
        }
    }

    public static void refreshObject(ModelObject[] modelObjects) throws Exception {
        DataManagementService dataManagementService = DataManagementService.getService(AppXSession.getConnection());
        ServiceData data = dataManagementService.refreshObjects(modelObjects);
        if(data.sizeOfPartialErrors() > 0){
            LOGGER.error(TCCommonExtension.hanlderServiceData(data));
            throw new Exception(TCCommonExtension.hanlderServiceData(data));
        }
    }
    public static void setPropertiesObject(ModelObject[] modelObjects, HashMap<String, VecStruct> mapAttributes) throws Exception {
        DataManagementService dataManagementService = DataManagementService.getService(AppXSession.getConnection());
        ServiceData data = dataManagementService.setProperties(modelObjects, mapAttributes);
        if(data.sizeOfPartialErrors() > 0){
            LOGGER.error(TCCommonExtension.hanlderServiceData(data));
            throw new Exception(TCCommonExtension.hanlderServiceData(data));
        }
    }

    public static String safeGetLOVName(Lov lovReference) {
        if (lovReference == null)
            return "";
        LovInfo lovInfo = lovReference.getLovInfo();
        if (lovInfo == null)
            return "nullLovInfo";
        return lovInfo.getName();
    }

    public static String serverTypeToString(int serverPropertyType) {
        String type = "*UNKNOWN*";
        switch (serverPropertyType) {
            case 1:
                type = "char";
                break;
            case 2:
                type = "date";
                break;
            case 3:
                type = "double";
                break;
            case 11:
                type = "ExternalReference";
                break;
            case 4:
                type = "float";
                break;
            case 5:
                type = "int";
                break;
            case 6:
                type = "logical";
                break;
            case 12:
                type = "note";
                break;
            case 7:
                type = "short";
                break;
            case 8:
                type = "string";
                break;
            case 9:
                type = "TypedReference";
                break;
            case 13:
                type = "TypedRelation";
                break;
            case 0:
                type = "untyped";
                break;
            case 10:
                type = "UntypedReference";
                break;
            case 14:
                type = "UntypedRelation";
                break;
        }
        return type;
    }

    public static String serverPropertyTypeToString(int serverPropType) {
        String type = "*UNKNOWN*";
        switch (serverPropType) {
            case 0:
                type = "Unknown";
                break;
            case 1:
                type = "POM_Attribute";
                break;
            case 2:
                type = "POM_Reference";
                break;
            case 3:
                type = "IMAN_Relation";
                break;
            case 4:
                type = "PropFromOtherType";
                break;
            case 5:
                type = "Computed";
                break;
            case 6:
                type = "SourceProperty";
                break;
        }
        return type;
    }

    public static String fieldTypeToString(int fieldType) {
        String type = "*UNKNOWN*";
        switch (fieldType) {
            case 0:
                type = "Unknown";
                break;
            case 1:
                type = "Simple";
                break;
            case 2:
                type = "CompoundObject";
                break;
        }
        return type;
    }

    public static String clientPropTypeToString(int fieldType) {
        String type = "*UNKNOWN*";
        switch (fieldType) {
            case 1:
                type = "char";
                break;
            case 2:
                type = "date";
                break;
            case 3:
                type = "double";
                break;
            case 4:
                type = "float";
                break;
            case 5:
                type = "int";
                break;
            case 6:
                type = "bool";
                break;
            case 7:
                type = "short";
                break;
            case 8:
                type = "string";
                break;
            case 9:
                type = "ModelObject";
                break;
        }
        return type;
    }
}
