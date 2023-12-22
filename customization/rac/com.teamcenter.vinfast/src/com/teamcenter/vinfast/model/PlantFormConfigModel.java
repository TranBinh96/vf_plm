package com.teamcenter.vinfast.model;

import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ClientMetaModel;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.teamcenter.soa.client.model.Type;

public class PlantFormConfigModel {
	private String plantFormObjectType = "";
	private String relationName = "";
	private String plantProperty = "";
	private String procurementProperty = "";
	private String partMakeBuyProperty = "";

	public PlantFormConfigModel(String objectType, String objectTypeForm, TCSession session) {
		Connection connection = session.getSoaConnection();
		plantFormObjectType = objectTypeForm;
		ClientMetaModel metaModel = connection.getClientMetaModel();
		List<Type> types = metaModel.getTypes(new String[] { objectType, objectTypeForm }, connection);
		if (types.size() > 1) {
			Type type = types.get(0);
			Hashtable<String, PropertyDescription> propDescs = type.getPropDescs();
			for (String propName : new TreeSet<String>(propDescs.keySet())) {
				PropertyDescription propDesc = propDescs.get(propName);
				if (propDesc.getUiName().compareToIgnoreCase("Plant Information") == 0 || propDesc.getUiName().compareToIgnoreCase("Acar Plant Form") == 0) {
					relationName = propName;
				}
				if (propDesc.getUiName().compareToIgnoreCase("Part Make/Buy") == 0) {
					partMakeBuyProperty = propName;
				}
			}

			Type formType = types.get(1);
			Hashtable<String, PropertyDescription> propDescsForm = formType.getPropDescs();
			for (String propName : new TreeSet<String>(propDescsForm.keySet())) {
				PropertyDescription propDesc = propDescsForm.get(propName);
				if (propDesc.getUiName().compareToIgnoreCase("Plant") == 0 || propDesc.getUiName().compareToIgnoreCase("Plant Code") == 0) {
					plantProperty = propName;
				}
				if (propDesc.getUiName().compareToIgnoreCase("Procurement Type") == 0 || propDesc.getUiName().compareToIgnoreCase("Make/Buy") == 0) {
					procurementProperty = propName;
				}
			}
		}
	}

	public String getRelationName() {
		return relationName;
	}

	public String getPlantProperty() {
		return plantProperty;
	}

	public String getProcumentProperty() {
		return procurementProperty;
	}

	public String getPartMakeBuyProperty() {
		return partMakeBuyProperty;
	}

	public String getPlantFormObjectType() {
		return plantFormObjectType;
	}

	public void setPlantFormObjectType(String plantFormObjectType) {
		this.plantFormObjectType = plantFormObjectType;
	}

}
