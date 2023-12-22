package com.vinfast.api.model.common;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeatureIntakeRequest {
    private String featureKey = "";
    private int ecuID;
    private String platform = "";
    private String model = "";
    private String[] subModels;
    private String market = "";
    private String description = "";
    private String vehicleVersion = "";
    private String name = "";
    private String[] domains;
    private String[] subDomains;
//    private ArrayList<FeatureAssetSetModel> assets;
    private String options = "";
    private String vendorData = "";
}
