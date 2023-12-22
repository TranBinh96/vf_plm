package com.vf.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;

public class BOMExtension {
	public static LinkedHashMap<String, Set<String>> getMakeBuyMatchingPurchase(TCSession session) {
		LinkedHashMap<String, Set<String>> purchaseLevelValidate = new LinkedHashMap<String, Set<String>>();
		String[] valuePurchases = TCExtension.GetPreferenceValues("VF_PART_CREATE_PURCHASELEVEL_VALIDATE", session);
		if (valuePurchases != null && valuePurchases.length > 0) {
			if (valuePurchases.length > 0) {
				for (String purchase : valuePurchases) {
					if (purchase.contains(";")) {
						String[] strArray = purchase.split(";");
						String partMakeBuy = strArray[0];
						Set<String> purchaseList = new HashSet<String>();
						if (strArray[1].contains(",")) {
							String[] strArray2 = strArray[1].split(",");
							for (String value : strArray2) {
								purchaseList.add(value);
							}
						} else {
							purchaseList.add(strArray[1]);
						}
						purchaseLevelValidate.put(partMakeBuy, purchaseList);
					}
				}
			}
		}

		return purchaseLevelValidate;
	}
}
