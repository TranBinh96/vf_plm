package com.teamcenter.vinfast.general.create.mepart;

import org.eclipse.swt.widgets.Composite;

public class MePartCreateFactory {
	public static MEPartCreateAbstract generateComposite(Composite c, String type, String subCategory) {
		if (type.compareToIgnoreCase("Scooter") == 0) {
			return new ScooterPartCreate_Composite(c);
		} else {
			if (subCategory.compareToIgnoreCase("COI") == 0)
				return new COILPartCreate_Composite(c);
			else if (subCategory.compareToIgnoreCase("BLN") == 0)
				return new BLANKPartCreate_Composite(c);
			else
				return new CommonPartCreate_Composite(c);
		}
	}
}
