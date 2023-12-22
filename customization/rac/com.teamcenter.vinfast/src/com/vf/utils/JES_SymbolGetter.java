package com.vf.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.teamcenter.rac.kernel.TCSession;

public class JES_SymbolGetter {
	public enum LOV {
		CRITICAL,
		MANDATORY,
		QUALITY,
		SAFETY;
	}
	
	private Map<LOV, String> symbolLovAndImageDataset;

	private String JES_SYMBOL_DATASET_SAFETY = "JES_Symbol_safety.png";
	private String JES_SYMBOL_DATASET_MANDATORY = "JES_Symbol_mandatory.png";
	private String JES_SYMBOL_DATASET_QUALITY = "JES_Symbol_quality.png";
	private String JES_SYMBOL_DATASET_CRITICAL = "JES_Symbol_critical.png";
	
	public JES_SymbolGetter() {
		symbolLovAndImageDataset = new HashMap<LOV, String>();
		symbolLovAndImageDataset.put(LOV.CRITICAL, JES_SYMBOL_DATASET_CRITICAL);
		symbolLovAndImageDataset.put(LOV.MANDATORY, JES_SYMBOL_DATASET_MANDATORY);
		symbolLovAndImageDataset.put(LOV.QUALITY, JES_SYMBOL_DATASET_QUALITY);
		symbolLovAndImageDataset.put(LOV.SAFETY, JES_SYMBOL_DATASET_SAFETY);
	}
	
	public File getImage(TCSession session, String stepSymbol) throws Exception {
		if (stepSymbol != null) stepSymbol = stepSymbol.trim();
		
		LOV lov = null;
		if (stepSymbol.equalsIgnoreCase("Critical")) {
			lov = LOV.CRITICAL;
		} else if (stepSymbol.equalsIgnoreCase("Quality")) {
			lov = LOV.QUALITY;
		} else if (stepSymbol.equalsIgnoreCase("Safety")) {
			lov = LOV.SAFETY;
		} else if (stepSymbol.equalsIgnoreCase("Mandatory")) {
			lov = LOV.MANDATORY;
		} 
		
		if (lov != null) {
			String datasetName = symbolLovAndImageDataset.get(lov);
			String tempDir = System.getenv("tmp");
			File symbolImage = Query.downloadFirstNameRefOfDatasetWithPrefix(session, Query.QUERY_JES_DATASET, Query.QUERY_JES_ENTRY_DATASET_NAME, datasetName, tempDir, "JES_symbol_image_");
			return symbolImage;			
		} 
		
		return null;
	}
	
}
