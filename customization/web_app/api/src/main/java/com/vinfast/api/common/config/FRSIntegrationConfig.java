package com.vinfast.api.common.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;

public class FRSIntegrationConfig {
    public static final Logger LOGGER = LogManager.getLogger(FRSIntegrationConfig.class);
    public static String FRS_FOLDER = "D:\\tempFrs\\";
    public static String STG_ARTIFACTORY_USER = "plm.vehicle.dev";
    public static String STG_ARTIFACTORY_PASS = "@Vf202020";
    public static String STG_ARTIFACTORY_URL = "https://artifactory.vingroup.net/artifactory";
    public static String STG_ARTIFACTORY_REPO = "SW_Warehouse_NonConfidential_ALL";
    public static String STG_MULESOFT_USER = "c84456a6d2874b6793eb1172bc9fb9a7";
    public static String STG_MULESOFT_PASS = "661C89923591480caDFe18BDC315Cd31";
    public static String STG_MULESOFT_USER_PASS = "Yzg0NDU2YTZkMjg3NGI2NzkzZWIxMTcyYmM5ZmI5YTc6NjYxQzg5OTIzNTkxNDgwY2FERmUxOEJEQzMxNUNkMzE=";
    public static String STG_API_IMPORT_XML = "https://test-api.vinfast.vn/capp/trackings/ecu-release/import-xml";

    public static String PRO_ARTIFACTORY_USER = "plm.vehicle.prod";
    public static String PRO_ARTIFACTORY_PASS = "@Vf202020";
    public static String PRO_ARTIFACTORY_URL = "https://artifactory.vingroup.net/artifactory";
    public static String PRO_ARTIFACTORY_REPO = "DVTM_Firmware_Confidential_ALL";
    public static String PRO_MULESOFT_USER = "da52be2786404fb7a8cfd598e803997a";
    public static String PRO_MULESOFT_PASS = "e5663E7279F84AFBa85C6cB6D6c79714";
    public static String PRO_MULESOFT_USER_PASS = "ZGE1MmJlMjc4NjQwNGZiN2E4Y2ZkNTk4ZTgwMzk5N2E6ZTU2NjNFNzI3OUY4NEFGQmE4NUM2Y0I2RDZjNzk3MTQ=";
    public static String PRO_API_IMPORT_XML = "https://api.vinfast.vn/capp/trackings/ecu-release/import-xml";

    public static String STG_FEATURE_INTAKE_URL = "https://fod-staging.excelfore.com/fodadmin/feature-intake";

    public static LinkedHashMap<String, String> BOM_PROP = new LinkedHashMap<String, String>(){{
        put("UID", "bl_item_current_id");
        put("Level", "bl_level_starting_0");
        put("Seq Number", "bl_sequence_no");
        put("Item ID", "bl_item_item_id");
        put("Part Name Vietnamese", "bl_item_vf4_item_name_vi");
        put("Part Name", "bl_rev_object_name");
        put("Color Information", "");
        put("Qty", "bl_quantity");
        put("Unit of Measure (UOM)", "bl_uom");
        put("Figure No", "");
        put("Revision Number", "bl_rev_item_revision_id");
        put("Type", "bl_item_object_type");
        put("Year", "");
        put("Additional information", "");
        put("Original part number", "");
        put("Interchangeability of parts", "");
        put("Old Stock", "");
        put("Effective Date", "");
        put("First Vin new part", "");
        put("Remarks", "");
    }};

    public static LinkedHashMap<String, String> MODEL_MAPPING = new LinkedHashMap<String, String>(){{
        put("T206", "ECO");
        put("T207", "PLUS");
    }};
}