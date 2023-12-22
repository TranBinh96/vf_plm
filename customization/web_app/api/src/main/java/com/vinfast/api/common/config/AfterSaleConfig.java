package com.vinfast.api.common.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;

public class AfterSaleConfig {
    private static final Logger LOGGER = LogManager.getLogger(AfterSaleConfig.class);
    public static String BOM_REPORT_FOLDER = "C:\\temp\\customservice\\";
    public static String VEHICLE_TYPE = "Car";
    public static String VEHICLE_MODEL = "All";
    public static String VEHICLE_YEAR = "2020";

    public static String AZURE_STORAGE_CONNECTION = "DefaultEndpointsProtocol=https;AccountName=vfstpblob;AccountKey=hGCxMS/qzwABcPtQf2qPuEp4fR6b3nbTtkErwSZXT+0R++8oXiwHeGOHaQZQN0JbXMa/1lOycB5zchv3kcYe3g==;EndpointSuffix=core.windows.net";
    public static LinkedHashMap<String, String> BOM_PROP = new LinkedHashMap<String, String>() {{
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

    public static LinkedHashMap<String, String> CHANGE_NOTE_PROP = new LinkedHashMap<String, String>() {{
        put("Additional information", "");
        put("Original part number", "vf4_old_base_part_number");
        put("Interchangeability of parts", "vf4_part_interchangeability");
        put("Old Stock", "vf4_old_stock");
        put("Effective Date", "vf4_effective_date");
        put("First Vin new part", "vf4_1st_VIN_new_part");
        put("Remarks", "");
    }};
}