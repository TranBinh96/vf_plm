package com.vinfast.api.service.implement;

import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.Type;
import com.teamcenter.soa.client.model.strong.*;
import com.vinfast.api.common.config.FRSIntegrationConfig;
import com.vinfast.api.common.constants.ErrorCodeConst;
import com.vinfast.api.common.constants.TeamcenterConst;
import com.vinfast.api.common.extensions.BOMExtension;
import com.vinfast.api.common.extensions.StringExtension;
import com.vinfast.api.common.extensions.TCCommonExtension;
import com.vinfast.api.model.common.*;
import com.vinfast.api.service.interfaces.IFRSIntegrationService;
import com.vinfast.connect.client.AppXSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

@Component
public class FRSIntegrationService implements IFRSIntegrationService {
    private static final Logger LOGGER = LogManager.getLogger(FRSIntegrationService.class);

    @Override
    public BaseModel frsSOTATransfer(String topBOMUID) {
        BaseModel modelReturn = new BaseModel();
        try {
            ArrayList<FeatureIntakeRequest> sotaList = new ArrayList<>();

            FileInputStream fis = new FileInputStream("D:\\Temp\\SOTA-20012023 - Copy.xlsx");
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sheet = wb.getSheetAt(0);
            int cellCount = 0;
            int rowCount = 0;

            for (Row row : sheet) {
                rowCount++;
                cellCount = 0;
                if (rowCount > 1) {
                    FeatureIntakeRequest newItem = new FeatureIntakeRequest();
                    for (Cell cell : row) {
                        cellCount++;
                        String value = cell.getStringCellValue();
                        if (cellCount == 1) {
                            newItem.setFeatureKey(value);
                        } else if (cellCount == 2) {
                            newItem.setEcuID(Integer.parseInt(value));
                        } else if (cellCount == 3) {
                            newItem.setName(value);
                        } else if (cellCount == 4) {
                            newItem.setPlatform(value);
                        } else if (cellCount == 5) {
                            newItem.setModel(value);
                        } else if (cellCount == 6) {
                            newItem.setMarket(value);
                        } else if (cellCount == 7) {
                            newItem.setDomains(new String[]{value});
                        } else if (cellCount == 8) {
                            newItem.setDescription(value);
                        } else if (cellCount == 9) {
                            newItem.setVehicleVersion(value);
                        }
                    }
                    newItem.setSubModels(new String[]{""});
                    newItem.setSubDomains(new String[]{""});
                    sotaList.add(newItem);
                }
            }

            sotaExcelforeTransfer(sotaList);
            modelReturn.setMessage("Transfer success");
            return modelReturn;
        } catch (Exception e) {
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(e.toString());
            return modelReturn;
        }
    }

    private void sotaExcelforeTransfer(ArrayList<FeatureIntakeRequest> sotaList) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        for (FeatureIntakeRequest model : sotaList) {
            try {
                HttpEntity<FeatureIntakeRequest> entity = new HttpEntity<>(model, headers);
                ResponseEntity<String> response = restTemplate.exchange(FRSIntegrationConfig.STG_FEATURE_INTAKE_URL, HttpMethod.PUT, entity, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    LOGGER.info(response.getBody());
                } else {
                    LOGGER.info(response.getBody());
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.toString());
            }
        }
    }

    @Override
    public BaseFileModel frsInternalDownload(String topBOMUID) {
        BaseFileModel modelReturn = new BaseFileModel();
        try {
            ModelObject topBom = TCCommonExtension.getModelObjectsFromUID(topBOMUID);
            if (topBom == null)
                throw new Exception("Can't find TOP BOM Item.");
            RevisionRule revRule = BOMExtension.getRevisionRule(TeamcenterConst.REVISION_RULE_PRECISE_ONLY);
            LinkedList<BOMLine> bomlineList = BOMExtension.expandBOM150(topBOMUID, revRule, true, 2);
            if (bomlineList.size() > 0) {
                HashMap<String, ImanFile> namesAndTcFiles = new HashMap<>();
                DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                Document document = documentBuilder.newDocument();

                //export FRS
                frsInternalExport(bomlineList, namesAndTcFiles, document);
                //download XML file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource domSource = new DOMSource(document);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                StreamResult streamResult = new StreamResult(bout);
                transformer.transform(domSource, streamResult);
                modelReturn.setFile(bout);
            }
            modelReturn.setMessage("Download success.");
        } catch (Exception e) {
            LOGGER.error(e.toString());
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(e.toString());
        }
        return modelReturn;
    }

    @Override
    public BaseModel frsInternalTransfer(String topBOMUID) {
        BaseModel modelReturn = new BaseModel();
        try {
            ModelObject topBom = TCCommonExtension.getModelObjectsFromUID(topBOMUID);
            if (topBom == null)
                throw new Exception("Can't find TOP BOM Item.");
            RevisionRule revRule = BOMExtension.getRevisionRule(TeamcenterConst.REVISION_RULE_PRECISE_ONLY);
            LinkedList<BOMLine> bomlineList = BOMExtension.expandBOM150(topBOMUID, revRule, true, 2);
            if (bomlineList.size() > 0) {
                String selectedFolder = FRSIntegrationConfig.FRS_FOLDER;
                HashMap<String, ImanFile> namesAndTcFiles = new HashMap<>();
                DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                Document document = documentBuilder.newDocument();

                //export FRS
                frsInternalExport(bomlineList, namesAndTcFiles, document);
                //download XML file
                String xmlFileName = "frs_" + topBom.getPropertyDisplayableValue("item_id") + "_" + java.util.UUID.randomUUID() + ".xml";
                String xmlFilePath = selectedFolder + xmlFileName;
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource domSource = new DOMSource(document);
                StreamResult streamResult = new StreamResult(new File(xmlFilePath));
                transformer.transform(domSource, streamResult);
                //create artifact
                ArtifactoryModel artifactoryModel = new ArtifactoryModel(FRSIntegrationConfig.STG_ARTIFACTORY_USER, FRSIntegrationConfig.STG_ARTIFACTORY_PASS, FRSIntegrationConfig.STG_ARTIFACTORY_URL, FRSIntegrationConfig.STG_ARTIFACTORY_REPO, null);
                artifactoryModel.createArtifactory();
                //upload file
                uploadBinaryFile(namesAndTcFiles, selectedFolder, artifactoryModel);

                File folder = new File(selectedFolder);
                FileUtils.cleanDirectory(folder);
            }
            modelReturn.setMessage("Transfer success.");
            return modelReturn;
        } catch (Exception e) {
            LOGGER.error(e.toString());
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(e.toString());
            return modelReturn;
        }
    }

    @Override
    public BaseFileModel frsDSADownload(String topBOMUID) {
        BaseFileModel modelReturn = new BaseFileModel();
        try {
            ModelObject topBom = TCCommonExtension.getModelObjectsFromUID(topBOMUID);
            if (topBom == null)
                throw new Exception("Can't find TOP BOM Item.");
            RevisionRule revRule = BOMExtension.getRevisionRule(TeamcenterConst.REVISION_RULE_PRECISE_ONLY);
            LinkedList<BOMLine> bomlineList = BOMExtension.expandBOM150(topBOMUID, revRule, true, 2);
            if (bomlineList.size() > 0) {
                HashMap<String, ImanFile> namesAndTcFiles = new HashMap<>();
                DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                Document document = documentBuilder.newDocument();

                //export FRS
                frsDSAExport(bomlineList, namesAndTcFiles, document);
                //download XML file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource domSource = new DOMSource(document);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                StreamResult streamResult = new StreamResult(bout);
                transformer.transform(domSource, streamResult);
                modelReturn.setFile(bout);
            }
            modelReturn.setMessage("Download success.");
        } catch (Exception e) {
            LOGGER.error(e.toString());
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(e.toString());
        }
        return modelReturn;
    }

    @Override
    public BaseModel frsDSATransfer(String topBOMUID) {
        BaseModel modelReturn = new BaseModel();
        try {
            ModelObject topBom = TCCommonExtension.getModelObjectsFromUID(topBOMUID);
            if (topBom == null)
                throw new Exception("Can't find TOP BOM Item.");
            RevisionRule revRule = BOMExtension.getRevisionRule(TeamcenterConst.REVISION_RULE_PRECISE_ONLY);
            LinkedList<BOMLine> bomlineList = BOMExtension.expandBOM150(topBOMUID, revRule, true, 2);
            if (bomlineList.size() > 0) {
                String selectedFolder = FRSIntegrationConfig.FRS_FOLDER;
                HashMap<String, ImanFile> namesAndTcFiles = new HashMap<>();
                DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                Document document = documentBuilder.newDocument();

                //export FRS
                frsDSAExport(bomlineList, namesAndTcFiles, document);
                //download XML file
                String xmlFileName = "frs_" + topBom.getPropertyDisplayableValue("item_id") + "_" + java.util.UUID.randomUUID() + ".xml";
                String xmlFilePath = selectedFolder + xmlFileName;
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource domSource = new DOMSource(document);
                StreamResult streamResult = new StreamResult(new File(xmlFilePath));
                transformer.transform(domSource, streamResult);
                //create artifact
                ArtifactoryModel artifactoryModel = new ArtifactoryModel(FRSIntegrationConfig.STG_ARTIFACTORY_USER, FRSIntegrationConfig.STG_ARTIFACTORY_PASS, FRSIntegrationConfig.STG_ARTIFACTORY_URL, FRSIntegrationConfig.STG_ARTIFACTORY_REPO, null);
                artifactoryModel.createArtifactory();
                //upload file
//                uploadBinaryFile(namesAndTcFiles, selectedFolder, artifactoryModel);
                //upload xml to FOTA
                uploadXMLToFOTA(xmlFilePath);

//                File folder = new File(selectedFolder);
//                FileUtils.cleanDirectory(folder);
            }
            modelReturn.setMessage("Transfer success.");
            return modelReturn;
        } catch (Exception e) {
            LOGGER.error(e.toString());
            modelReturn.setErrorCode(ErrorCodeConst.ERROR_NORMAL);
            modelReturn.setMessage(e.toString());
            return modelReturn;
        }
    }

    private void frsInternalExport(LinkedList<BOMLine> allBOMLineInWindow, HashMap<String, ImanFile> namesAndTcFiles, Document document) throws Exception {
        BOMLine rootLine = allBOMLineInWindow.get(0);
        Item objectItem = (Item) rootLine.get_bl_item();
        ItemRevision objectItemRev = (ItemRevision) rootLine.get_bl_revision();
        String contextID = objectItem.getPropertyDisplayableValue("Smc0HasVariantConfigContext");
        String modelAndYear = contextID.split("-").length > 0 ? contextID.split("-")[0] : "";
        String[] modelAndYearWords = modelAndYear.split("_");
        if (modelAndYearWords.length < 2) {
            LOGGER.error("Configuration Context not exists.");
            throw new Exception("Configuration Context not exists.");
        }

        String frsNumber = rootLine.getPropertyDisplayableValue("awb0BomLineItemId");
        String frsName = rootLine.getPropertyDisplayableValue("bl_item_object_name");
        String model = modelAndYearWords[0];
        String year = modelAndYearWords[1];
        String[] region = null;
        String frsVersion = "";
        if (((ItemRevision) rootLine.get_bl_revision()).get_object_type().compareToIgnoreCase("VF3_FRSRevision") == 0) {
            region = TCCommonExtension.getPropertyRealValues(objectItemRev, "vf4_market_arr");
            String version = objectItemRev.getPropertyDisplayableValue("vf4_version");
            String majorVersion = objectItemRev.getPropertyDisplayableValue("vf4_major_version");
            String minorVersion = objectItemRev.getPropertyDisplayableValue("vf4_minor_version");
            String hotfix = objectItemRev.getPropertyDisplayableValue("vf3_hotfix_number");
            frsVersion = String.format("%,.0f", Double.parseDouble(version)) + "." + majorVersion + "." + minorVersion + "." + hotfix;
        } else {
            frsVersion = rootLine.get_bl_revision().getPropertyDisplayableValue("object_desc");
        }

        if (model.isEmpty() || year.isEmpty() || region == null) {
            LOGGER.error("Model, Year, Market must not null.");
            throw new Exception("Model, Year, Market must not null.");
        }

        Calendar frsDateReleasedDate = rootLine.getPropertyObject("awb0RevisionReleaseDate").getCalendarValue();
        Calendar today = Calendar.getInstance();
        String releaseDate = frsDateReleasedDate != null ? StringExtension.getUnixTimestamp(frsDateReleasedDate) : StringExtension.getUnixTimestamp(today);
        String lifeCycle = rootLine.get_bl_revision().getPropertyDisplayableValue("vf4_lifecycle");
        String technicianInfo = "";
        String customerInfo = "";

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

        // root element
        Element frsRootEle = document.createElement("frs");
        document.appendChild(frsRootEle);

        Element frsNumberEle = document.createElement("frsNumber");
        frsRootEle.appendChild(frsNumberEle);
        Text frsNumberTextNode = document.createTextNode("v" + frsVersion);
        frsNumberEle.appendChild(frsNumberTextNode);

        Element frsNameEle = document.createElement("frsTCID");
        frsRootEle.appendChild(frsNameEle);
        Text frsNameEleTextNode = document.createTextNode(frsNumber);
        frsNameEle.appendChild(frsNameEleTextNode);

        Element modelEle = document.createElement("model");
        frsRootEle.appendChild(modelEle);
        Text modelTextNode = document.createTextNode(model);
        modelEle.appendChild(modelTextNode);

        Element yearEle = document.createElement("year");
        frsRootEle.appendChild(yearEle);
        Text yearTextNode = document.createTextNode(year);
        yearEle.appendChild(yearTextNode);

        Element marketEle = document.createElement("market");
        frsRootEle.appendChild(marketEle);
        Text marketTextNode = document.createTextNode(region[0]);
        marketEle.appendChild(marketTextNode);

        Element lifeCycleEle = document.createElement("lifecycle");
        frsRootEle.appendChild(lifeCycleEle);
        Text lifeCycleTextNode = document.createTextNode(lifeCycle);
        lifeCycleEle.appendChild(lifeCycleTextNode);

        Element techInfoEle = document.createElement("technicianInfo");
        frsRootEle.appendChild(techInfoEle);
        Text techInfoTextNode = document.createTextNode(technicianInfo);
        techInfoEle.appendChild(techInfoTextNode);

        Element cusInfoEle = document.createElement("customerInfo");
        frsRootEle.appendChild(cusInfoEle);
        Text cusInfoTextNode = document.createTextNode(customerInfo);
        cusInfoEle.appendChild(cusInfoTextNode);

        Element releaseDateEle = document.createElement("releaseDate");
        frsRootEle.appendChild(releaseDateEle);
        Text releaseDateTextNode = document.createTextNode(releaseDate);
        releaseDateEle.appendChild(releaseDateTextNode);

        Element ecuListEle = document.createElement("ecuList");
        frsRootEle.appendChild(ecuListEle);
        Element currentEcuPartListEle = null;

        HashMap<String, LinkedList<ImanFile>> filesMap = new HashMap<String, LinkedList<ImanFile>>();

        String ECU_ID = "";
        String Part_Type = "";
        for (BOMLine line : allBOMLineInWindow) {
            int level = Integer.parseInt(line.getPropertyDisplayableValue("bl_level_starting_0"));
            if (level == 1) {
                Item item = (Item) line.get_bl_item();
                TCCommonExtension.loadObject(new String[]{item.getUid()}, AppXSession.getConnection());
                ItemRevision itemRev = (ItemRevision) line.get_bl_revision();

                Element ecuEle = document.createElement("ecu");
                ecuListEle.appendChild(ecuEle);

                String ecuId = "";
                try {
                    ecuId = item.getPropertyDisplayableValue("vf4_ECU_type");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (ecuId.isEmpty())
                    ecuId = line.getPropertyDisplayableValue("bl_rev_object_name");

                Element ecuIdEle = document.createElement("ecuId");
                ecuEle.appendChild(ecuIdEle);
                Text ecuIdTextNode = document.createTextNode(ecuId);
                ecuIdEle.appendChild(ecuIdTextNode);
                ECU_ID = ecuId;

                String orderNumber = line.getPropertyDisplayableValue("bl_sequence_no");
                Element orderNumberEle = document.createElement("orderNumber");
                ecuEle.appendChild(orderNumberEle);
                Text orderNumberTextNode = document.createTextNode(orderNumber);
                orderNumberEle.appendChild(orderNumberTextNode);

                Element ecuPartListEle = document.createElement("ecuPartList");
                ecuEle.appendChild(ecuPartListEle);
                currentEcuPartListEle = ecuPartListEle;


                ModelObject[] spec = itemRev.get_IMAN_specification();
                if (spec.length > 0) {
//                    GetProperties(spec);
                    for (ModelObject comp : spec) {
                        Type type = comp.getTypeObject();
                        if (type.getClassName().compareToIgnoreCase("Dataset") == 0) {
                            Property refListProperty = comp.getPropertyObject("ref_list");
                            ModelObject[] refObjects = refListProperty.getModelObjectArrayValue();
                            for (ModelObject nameRef : refObjects) {
                                if (nameRef instanceof ImanFile) {
                                    String fileName = ((ImanFile) nameRef).get_original_file_name();
                                    if (!fileName.contains("-")) {
                                        continue;
                                    } else {
                                        fileName = fileName.split("-")[0];
                                    }

                                    if (filesMap.containsKey(fileName)) {
                                        filesMap.get(fileName).add((ImanFile) nameRef);
                                    } else {
                                        LinkedList<ImanFile> list = new LinkedList<ImanFile>();
                                        list.add((ImanFile) nameRef);
                                        filesMap.put(fileName, list);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (level == 2) {
                Element ecuPartEle = document.createElement("ecuPart");
                currentEcuPartListEle.appendChild(ecuPartEle);

                String partType = line.getPropertyDisplayableValue("vf4_software_part_type");
                String variant = line.getPropertyDisplayableValue("bl_formula");
                String traceability = "";
                String partNumber = line.getPropertyDisplayableValue("bl_item_item_id");

                Element partTypeEle = document.createElement("partType");
                Text partTypeTextNode = document.createTextNode(partType);
                partTypeEle.appendChild(partTypeTextNode);
                ecuPartEle.appendChild(partTypeEle);
                Part_Type = partType;

                Element variantEle = document.createElement("variant");
                Text variantTextNode = document.createTextNode(variant);
                variantEle.appendChild(variantTextNode);
                ecuPartEle.appendChild(variantEle);

                Element traceabilityEle = document.createElement("traceability");
                Text traceabilityTextNode = document.createTextNode(traceability);
                traceabilityEle.appendChild(traceabilityTextNode);
                ecuPartEle.appendChild(traceabilityEle);

                Element partNumberEle = document.createElement("partNumber");
                Text partNumberTextNode = document.createTextNode(partNumber);
                partNumberEle.appendChild(partNumberTextNode);
                ecuPartEle.appendChild(partNumberEle);

                Element filesEle = document.createElement("files");
                ecuPartEle.appendChild(filesEle);
                String revision = line.getPropertyDisplayableValue("bl_rev_item_revision_id");
                if (filesMap.size() > 0 && filesMap.containsKey(partNumber)) {
                    LinkedList<ImanFile> datasets = filesMap.get(partNumber);
                    for (ImanFile nameref : datasets) {
                        String fileName = nameref.getPropertyDisplayableValue("original_file_name");
                        String fullPathAndName = String.format("%s~%s~%s", fileName, ECU_ID, Part_Type);
                        namesAndTcFiles.put(fullPathAndName, (ImanFile) nameref);
                        if (fileName.trim().length() > 0) {
                            Element fileEle = document.createElement("file");
                            filesEle.appendChild(fileEle);

                            Element revisionEle = document.createElement("revision");
                            Text revisionTextNode = document.createTextNode(revision);
                            revisionEle.appendChild(revisionTextNode);
                            fileEle.appendChild(revisionEle);

                            Element fileNameEle = document.createElement("fileName");
                            Text fileNameTextNode = document.createTextNode(fileName);
                            fileNameEle.appendChild(fileNameTextNode);
                            fileEle.appendChild(fileNameEle);
                        }
                    }
                } else {
//                    Element fileEle = document.createElement("file");
//                    filesEle.appendChild(fileEle);
//
//                    Element revisionEle = document.createElement("revision");
//                    Text revisionTextNode = document.createTextNode(revision);
//                    revisionEle.appendChild(revisionTextNode);
//                    fileEle.appendChild(revisionEle);
//
//                    Element fileNameEle = document.createElement("fileName");
//                    Text fileNameTextNode = document.createTextNode("");
//                    fileNameEle.appendChild(fileNameTextNode);
//                    fileEle.appendChild(fileNameEle);
                }
            }
        }
    }

    private void frsDSAExport(LinkedList<BOMLine> allBOMLineInWindow, HashMap<String, ImanFile> namesAndTcFiles, Document document) throws Exception {
        LinkedHashMap<String, LinkedHashMap<String, String>> ecuMapping = new LinkedHashMap<>();
        String[] preValues = TCCommonExtension.getPreference("VF_ECU_DID_REV_DID_SCHEME_MAPPING");
        if (preValues != null) {
            for (String value : preValues) {
                if (value.contains(";")) {
                    String[] item = value.split(";");
                    if (item.length > 2) {
                        LinkedHashMap<String, String> subItem = new LinkedHashMap<>();
                        subItem.put("DID", item[1]);
                        subItem.put("REV_DID", item[2]);
                        ecuMapping.put(item[0], subItem);
                    }
                }
            }
        }

        BOMLine rootLine = allBOMLineInWindow.get(0);
        Item objectItem = (Item) rootLine.get_bl_item();
        ItemRevision objectItemRev = (ItemRevision) rootLine.get_bl_revision();
        TCCommonExtension.refreshObject(new ModelObject[]{objectItemRev});
        String contextID = objectItem.getPropertyDisplayableValue("Smc0HasVariantConfigContext");
        String modelAndYear = contextID.split("-").length > 0 ? contextID.split("-")[0] : "";
        String[] modelAndYearWords = modelAndYear.split("_");

        // Export XML format
        String frsNumber = rootLine.getPropertyDisplayableValue("awb0BomLineItemId");
        String platform = modelAndYearWords.length == 2 ? modelAndYearWords[0] : "";
        String year = modelAndYearWords.length == 2 ? modelAndYearWords[1] : "";
        String[] region = null;
        List<String> modelValue;
        String frsVersion = "";
        if (objectItemRev.get_object_type().compareToIgnoreCase("VF3_FRSRevision") == 0) {
            region = TCCommonExtension.getPropertyRealValues(objectItemRev, "vf4_market_arr");
            String version = objectItemRev.getPropertyDisplayableValue("vf4_version");
            String majorversion = objectItemRev.getPropertyDisplayableValue("vf4_major_version");
            String minorversion = objectItemRev.getPropertyDisplayableValue("vf4_minor_version");
            if (StringExtension.isDouble(version)) {
                frsVersion = String.format("%,.0f", Double.parseDouble(version)) + "." + majorversion + "." + minorversion;
            }
        } else {
            frsVersion = objectItemRev.getPropertyDisplayableValue("object_desc");
        }

        Calendar frsDateReleasedDate = rootLine.getPropertyObject("awb0RevisionReleaseDate").getCalendarValue();
        String releaseDate = frsDateReleasedDate != null ? Long.toString(frsDateReleasedDate.getTimeInMillis()) : "";

        // root element
        Element frsRootEle = document.createElement("frs");
        document.appendChild(frsRootEle);

        Element frsNumberEle = document.createElement("frsNumber");
        frsRootEle.appendChild(frsNumberEle);
        Text frsNumberTextNode = document.createTextNode(frsVersion);
        frsNumberEle.appendChild(frsNumberTextNode);

        Element frsNameEle = document.createElement("frsTCID");
        frsRootEle.appendChild(frsNameEle);
        Text frsNameEleTextNode = document.createTextNode(frsNumber);
        frsNameEle.appendChild(frsNameEleTextNode);

        Element platformEle = document.createElement("vehiclePlatform");
        frsRootEle.appendChild(platformEle);
        Text platformTextNode = document.createTextNode(platform);
        platformEle.appendChild(platformTextNode);

        Element yearEle = document.createElement("year");
        frsRootEle.appendChild(yearEle);
        Text yearTextNode = document.createTextNode(year);
        yearEle.appendChild(yearTextNode);

        Element modelEle = document.createElement("model");
        frsRootEle.appendChild(modelEle);
        Text modelTextNode = document.createTextNode(region != null ? String.join(",", region) : "");
        modelEle.appendChild(modelTextNode);

        Element regionEle = document.createElement("region");
        frsRootEle.appendChild(regionEle);
        Text regionTextNode = document.createTextNode(region != null ? String.join(",", region) : "");
        regionEle.appendChild(regionTextNode);

        Element makeEle = document.createElement("make");
        frsRootEle.appendChild(makeEle);
        Text makeTextNode = document.createTextNode("H");
        makeEle.appendChild(makeTextNode);

        Element releaseDateEle = document.createElement("releaseDate");
        frsRootEle.appendChild(releaseDateEle);
        Text releaseDateTextNode = document.createTextNode(releaseDate);
        releaseDateEle.appendChild(releaseDateTextNode);

        Element responsEle = document.createElement("repository");
        frsRootEle.appendChild(responsEle);
        Text responsTextNode = document.createTextNode("https://artifactory.vingroup.net/artifactory/webapp/#/artifacts/browse/tree/General/" + FRSIntegrationConfig.PRO_ARTIFACTORY_REPO);
        responsEle.appendChild(responsTextNode);

        Element ecuListEle = document.createElement("ecuList");
        frsRootEle.appendChild(ecuListEle);
        Element currentEcuPartListEle = null;

        HashMap<String, LinkedList<ImanFile>> filesMap = new HashMap<>();

        String ECU_ID = "";
        String Part_Type;
        for (BOMLine line : allBOMLineInWindow) {
            int level = Integer.parseInt(line.getPropertyDisplayableValue("bl_level_starting_0"));
            if (level == 1) {
                Element ecuEle = document.createElement("ecu");
                ecuListEle.appendChild(ecuEle);

                String ecuId = line.getPropertyDisplayableValue("bl_rev_object_name");
                Element ecuIdEle = document.createElement("ecuId");
                ecuEle.appendChild(ecuIdEle);
                Text ecuIdTextNode = document.createTextNode(ecuId);
                ecuIdEle.appendChild(ecuIdTextNode);
                ECU_ID = ecuId;

                String orderNumber = line.getPropertyDisplayableValue("bl_sequence_no");
                Element orderNumberEle = document.createElement("orderNumber");
                ecuEle.appendChild(orderNumberEle);
                Text orderNumberTextNode = document.createTextNode(orderNumber);
                orderNumberEle.appendChild(orderNumberTextNode);

                Element ecuPartListEle = document.createElement("ecuPartList");
                ecuEle.appendChild(ecuPartListEle);
                currentEcuPartListEle = ecuPartListEle;

                ItemRevision itemRev = (ItemRevision) line.get_bl_revision();
                ModelObject[] spec = null;
                if (itemRev != null)
                    spec = itemRev.get_IMAN_specification();
                if (spec != null && spec.length > 0) {
                    for (ModelObject comp : spec) {
                        TCCommonExtension.loadObject(new String[]{comp.getUid()}, AppXSession.getConnection());
                        Type type = comp.getTypeObject();
                        if (type.getClassName().compareToIgnoreCase("Dataset") == 0) {
                            Property refListProperty = comp.getPropertyObject("ref_list");
                            ModelObject[] refObjs = refListProperty.getModelObjectArrayValue();
                            for (ModelObject nameref : refObjs) {
                                if (nameref instanceof ImanFile) {
                                    TCCommonExtension.loadObject(new String[]{nameref.getUid()}, AppXSession.getConnection());
                                    String fileName = ((ImanFile) nameref).get_original_file_name();
                                    if (!fileName.contains("-")) {
                                        continue;
                                    } else {
                                        fileName = fileName.split("-")[0];
                                    }

                                    if (filesMap.containsKey(fileName)) {
                                        filesMap.get(fileName).add((ImanFile) nameref);
                                    } else {
                                        LinkedList<ImanFile> list = new LinkedList<>();
                                        list.add((ImanFile) nameref);
                                        filesMap.put(fileName, list);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (level == 2) {
                Element ecuPartEle = document.createElement("ecuPart");
                assert currentEcuPartListEle != null;
                currentEcuPartListEle.appendChild(ecuPartEle);

                String partType = line.getPropertyDisplayableValue("vf4_software_part_type");
                String didValue = "";
                String revDidValue = "";
                if (ecuMapping.containsKey(partType)) {
                    LinkedHashMap<String, String> ecuLogic = ecuMapping.get(partType);
                    didValue = ecuLogic.get("DID");
                    revDidValue = ecuLogic.get("REV_DID");
                }
                String variant = line.getPropertyDisplayableValue("bl_formula");
                String partNumber = line.getPropertyDisplayableValue("bl_item_item_id");
                String revision = line.getPropertyDisplayableValue("bl_rev_item_revision_id");

                Element partTypeEle = document.createElement("partType");
                Text partTypeTextNode = document.createTextNode(partType);
                partTypeEle.appendChild(partTypeTextNode);
                ecuPartEle.appendChild(partTypeEle);
                Part_Type = partType;

                Element didEle = document.createElement("did");
                Text didTextNode = document.createTextNode(didValue);
                didEle.appendChild(didTextNode);
                ecuPartEle.appendChild(didEle);

                Element revDidEle = document.createElement("revdid");
                Text revDidTextNode = document.createTextNode(revDidValue);
                revDidEle.appendChild(revDidTextNode);
                ecuPartEle.appendChild(revDidEle);

                Element variantEle = document.createElement("variant");
                Text variantTextNode = document.createTextNode(getVariant(variant));
                variantEle.appendChild(variantTextNode);
                ecuPartEle.appendChild(variantEle);

                Element partNumberEle = document.createElement("partNumber");
                Text partNumberTextNode = document.createTextNode(partNumber);
                partNumberEle.appendChild(partNumberTextNode);
                ecuPartEle.appendChild(partNumberEle);

                Element revisionEle = document.createElement("revision");
                Text revisionTextNode = document.createTextNode(revision);
                revisionEle.appendChild(revisionTextNode);
                ecuPartEle.appendChild(revisionEle);

                Element filesEle = document.createElement("files");
                ecuPartEle.appendChild(filesEle);
                if (filesMap.size() > 0 && filesMap.containsKey(partNumber)) {
                    LinkedList<ImanFile> datasets = filesMap.get(partNumber);
                    for (ImanFile nameref : datasets) {
                        String fileName = nameref.getPropertyDisplayableValue("original_file_name");
                        String fullPathAndName = String.format("%s~%s~%s", fileName, ECU_ID, Part_Type);
                        namesAndTcFiles.put(fullPathAndName, nameref);
                        if (fileName.trim().length() > 0) {
                            Element fileEle = document.createElement("file");
                            filesEle.appendChild(fileEle);

                            Element fileNameEle = document.createElement("fileName");
                            Text fileNameTextNode = document.createTextNode(fileName);
                            fileNameEle.appendChild(fileNameTextNode);
                            fileEle.appendChild(fileNameEle);
                        }
                    }
                }
            }
        }
    }

    private ArrayList<FeatureIntakeRequest> frsSOTAExport(LinkedList<BOMLine> allBOMLineInWindow) throws Exception {
        BOMLine rootLine = allBOMLineInWindow.get(0);
        Item objectItem = (Item) rootLine.get_bl_item();
        ItemRevision objectItemRev = (ItemRevision) rootLine.get_bl_revision();
        TCCommonExtension.refreshObject(new ModelObject[]{objectItemRev});
        String contextID = objectItem.getPropertyDisplayableValue("Smc0HasVariantConfigContext");
        String modelAndYear = contextID.split("-").length > 0 ? contextID.split("-")[0] : "";
        String[] modelAndYearWords = modelAndYear.split("_");

        // Export XML format
        String frsNumber = rootLine.getPropertyDisplayableValue("awb0BomLineItemId");
        String platform = modelAndYearWords.length == 2 ? modelAndYearWords[0] : "";
        String year = modelAndYearWords.length == 2 ? modelAndYearWords[1] : "";

        ArrayList<FeatureIntakeRequest> sotaList = new ArrayList<>();
        for (BOMLine line : allBOMLineInWindow) {
            int level = Integer.parseInt(line.getPropertyDisplayableValue("bl_level_starting_0"));
            if (level == 1) {
                ItemRevision itemRev = (ItemRevision) line.get_bl_revision();
                TCCommonExtension.loadObject(new String[]{itemRev.getUid()}, AppXSession.getConnection());
                String objectType = itemRev.get_object_type();
                if (objectType.compareToIgnoreCase("VF3_ECURevision") == 0) {
                    Property featureProperty = itemRev.getPropertyObject("vf3_ecr_release_features");
                    ModelObject[] featureList = featureProperty.getModelObjectArrayValue();
                    for (ModelObject feature : featureList) {
                        if (feature != null) {
                            TCCommonExtension.loadObject(new String[]{feature.getUid()}, AppXSession.getConnection());
                            FeatureIntakeRequest newFeature = new FeatureIntakeRequest();
//                            newFeature.setEcuID(objectItem.getPropertyDisplayableValue("item_id"));
                            newFeature.setPlatform(platform);
                            newFeature.setFeatureKey(feature.getPropertyDisplayableValue("vf3_key"));
                            newFeature.setName(feature.getPropertyDisplayableValue("vf3_name"));
                            newFeature.setMarket(feature.getPropertyDisplayableValue("vf4_market_arr"));
                            newFeature.setModel(feature.getPropertyDisplayableValue("vf4_model_arr"));
                            newFeature.setVehicleVersion(feature.getPropertyDisplayableValue("vf4_minimum_vehicle_version"));
                            newFeature.setOptions(feature.getPropertyDisplayableValue("vf4_OptionsDependExpression"));
                            sotaList.add(newFeature);
                        }
                    }
                }
            }
        }
        return sotaList;
    }

    private void uploadBinaryFile(HashMap<String, ImanFile> namesAndTcFiles, String selectedFolder, ArtifactoryModel artifactoryModel) throws Exception {
        boolean checkValid = true;
        String BOOTSTRAP_URL_PREF = "Fms_BootStrap_Urls";
        String[] bootStrapURL = TCCommonExtension.getPreference(BOOTSTRAP_URL_PREF);
        FileManagementUtility fileUtility = new FileManagementUtility(AppXSession.getConnection(), null, null, bootStrapURL, selectedFolder);
        for (String fullPathAndName : namesAndTcFiles.keySet()) {
            ImanFile tcFile = namesAndTcFiles.get(fullPathAndName);
            String ecuPart;
            String partType;
            String fileName;
            String[] str = fullPathAndName.split("~");
            if (str.length >= 3) {
                fileName = str[0];
                ecuPart = str[1];
                partType = str[2];
                if (!artifactoryModel.checkExistInRepo(ecuPart, partType, fileName)) {
                    File fileInTC = TCCommonExtension.downloadFile(fileUtility, tcFile, selectedFolder + File.separator + fileName);
                    if (fileInTC != null) {
                        try {
                            InputStream file = Files.newInputStream(fileInTC.toPath());
                            String filePath = String.format("%s/%s/%s", ecuPart, partType, fileName);
                            artifactoryModel.uploadFile(filePath, file);
                        } catch (Exception e) {
                            LOGGER.info(e.toString());
                            checkValid = false;
                        }
                    } else {
                        LOGGER.info(fileName + " can't download file from TC");
                        checkValid = false;
                    }
                } else {
                    LOGGER.info(fileName + " exist in Repo");
                }
            }
        }
        if (!checkValid) throw new Exception("Have error in upload file process. Please contact with Admin.");
    }

    private void uploadXMLToFOTA(String xmlFilePath) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBasicAuth(FRSIntegrationConfig.STG_MULESOFT_USER, FRSIntegrationConfig.STG_MULESOFT_PASS);

        File xmlFile = new File(xmlFilePath);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", xmlFile);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(FRSIntegrationConfig.STG_API_IMPORT_XML, requestEntity, String.class);
        System.out.println(response);
    }

    private String getVariant(String input) {
        if (input.isEmpty()) return "";
        Set<String> output = new HashSet<>();
        try {
            if (input.contains("AND")) {
                String[] split = input.split("AND");
                if (split.length > 1) {
                    if (split[0].contains("=") && split[1].contains("=")) {
                        String[] market = split[0].split("=");
                        String[] model = split[1].split("=");
                        if (FRSIntegrationConfig.MODEL_MAPPING.containsKey(model[1].trim())) {
                            output.add(FRSIntegrationConfig.MODEL_MAPPING.get(model[1].trim()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info(e.toString());
        }
        return String.join("_", output);
    }
}
