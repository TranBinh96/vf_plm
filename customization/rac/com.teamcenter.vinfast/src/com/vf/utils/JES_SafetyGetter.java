package com.vf.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.teamcenter.rac.kernel.TCSession;

public class JES_SafetyGetter {
	public enum LOV {
		GLASSES,
		GLASSES_NOT_REQ,
		HARD_FACE_MASK,
		HARD_FACE_MASK_NOT_REQ;
	}
	
	private Map<LOV, String> symbolLovAndImageDataset;

	private String JES_GLASSES = "JES_SAFETY_Glasses.png";
	private String JES_GLASSES_NOT_REQ = "JES_SAFETY_Glasses_NOTREQ.png";
	private String JES_HARD_FACE_MASK = "JES_SAFETY_Hard Face Mask.png";
	private String JES_HARD_FACE_MASK_NOT_REQ = "JES_SAFETY_Hard Face Mask_NOTREQ.png";
	
	public JES_SafetyGetter() {
		symbolLovAndImageDataset = new HashMap<LOV, String>();
		symbolLovAndImageDataset.put(LOV.GLASSES, JES_GLASSES);
		symbolLovAndImageDataset.put(LOV.GLASSES_NOT_REQ, JES_GLASSES_NOT_REQ);
		symbolLovAndImageDataset.put(LOV.HARD_FACE_MASK, JES_HARD_FACE_MASK);
		symbolLovAndImageDataset.put(LOV.HARD_FACE_MASK_NOT_REQ, JES_HARD_FACE_MASK_NOT_REQ);
	}
	
	public File getImage(TCSession session, String stepSymbol, boolean isTicked) throws Exception {
		if (stepSymbol != null) stepSymbol = stepSymbol.trim();
		
		LOV lov = null;
		if (stepSymbol.equalsIgnoreCase("Glasses")) {
			lov = isTicked ? LOV.GLASSES : LOV.GLASSES_NOT_REQ;
		} else if (stepSymbol.equalsIgnoreCase("Hard Face Mask")) {
			lov = isTicked ? LOV.HARD_FACE_MASK : LOV.HARD_FACE_MASK_NOT_REQ;
		}
		
		if (lov != null) {
			String datasetName = symbolLovAndImageDataset.get(lov);
			String tempDir = System.getenv("tmp");
			File image = Query.downloadFirstNameRefOfDatasetWithPrefix(session, Query.QUERY_JES_DATASET, Query.QUERY_JES_ENTRY_DATASET_NAME, datasetName, tempDir, "JES_safety_image_");
			return image;			
		} 
		
		return null;
	}
	
}
