package com.teamcenter.vinfast.car.engineering.specdoc;

import org.eclipse.swt.widgets.Composite;

public class SpecDocumentCreateFactory {
	public static SpecDocumentCreateAbstract generateComposite(Composite c, String docType) {
		if (docType.compareTo("VFDS") == 0) {
			return new VFDSDocumentCreate_Composite(c);
		} else if (docType.compareTo("VFTE") == 0) {
			return new VFTEDocumentCreate_Composite(c);
		} else if (docType.compareTo("VFFS") == 0) {
			return new VFFSDocumentCreate_Composite(c);
		} else {
			return new CommonDocumentCreate_Composite(c);
		}
	}
}
