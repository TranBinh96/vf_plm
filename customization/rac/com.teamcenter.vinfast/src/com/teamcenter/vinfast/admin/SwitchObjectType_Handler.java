package com.teamcenter.vinfast.admin;

import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ClientMetaModel;
import com.teamcenter.soa.client.model.Lov;
import com.teamcenter.soa.client.model.LovInfo;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.teamcenter.soa.client.model.Type;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.model.OwnerTransferModel;
import com.vf.utils.TCExtension;

public class SwitchObjectType_Handler extends AbstractHandler {
	private TCSession session;
	private SwitchObjectType_Dialog dlg;
	private TCComponent selectedObject = null;
	private List<OwnerTransferModel> dataList;
	private LinkedHashMap<String, LinkedHashMap<String, String>> reportList;

	private static String GROUP_PERMISSION = "dba";

	public SwitchObjectType_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();

			if (!CheckPermission()) {
				MessageBox.post("You are not authorized to baseline.", "Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			selectedObject = (TCComponent) targetComp[0];
			dataList = new LinkedList<OwnerTransferModel>();

			// Init data

			// Init UI
			dlg = new SwitchObjectType_Dialog(new Shell());
			dlg.create();
			dlg.setTitle("Owner Transfer Trigger");
			dlg.setMessage("Define business object create information");

//			dlg.btnOpenFile.addSelectionListener(new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					FileDialog fileDialog = new FileDialog(new Shell(), SWT.OPEN);
//					fileDialog.setText("Open");
//					fileDialog.setFilterPath("C:/");
//					String[] filterExt = { "*.xlsx", "*.xls" };
//					fileDialog.setFilterExtensions(filterExt);
//					ReadExcelFile(fileDialog.open());
//				}
//			});

			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ActionProcess();
				}
			});
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void ActionProcess() {
		try {
			ChangeOriginID((TCComponentItem) selectedObject);
		} catch (Exception e) {

		}
	}

	private void ChangeOriginID(TCComponentItem item) throws TCException, NotLoadedException {
//		String originID = item.getPropertyDisplayableValue("item_id");
//		String replaceID = "R_" + originID;
//		
//		item.setStringProperty("item_id", replaceID);
		Connection connection = session.getSoaConnection();
		ClientMetaModel metaModel = connection.getClientMetaModel();
		List<Type> types = metaModel.getTypes(new String[] { dlg.txtObjectType.getText() }, connection);

		for (Type type : types) {
			Map<String, String> typeConstants = type.getConstants();
			for (String constant : new TreeSet<String>(typeConstants.keySet())) {
				dlg.lstOrigin.add("Constant: " + constant + " = " + typeConstants.get(constant));
//                dlg.lstOrigin.add("    Constant: " + constant + " = " + typeConstants.get(constant));
			}
			Hashtable<String, PropertyDescription> propDescs = type.getPropDescs();
			for (String propName : new TreeSet<String>(propDescs.keySet())) {
				dlg.lstOrigin.add("    " + propName);
				PropertyDescription propDesc = propDescs.get(propName);
				dlg.lstOrigin.add("        Display:" + propDesc.getUiName());
				dlg.lstOrigin.add("        FieldType " + fieldTypeToString(propDesc.getFieldType()));
				dlg.lstOrigin.add("        ServerType " + serverTypeToString(propDesc.getServerType()));
				dlg.lstOrigin.add("        ServerPropertyType " + serverPropertyTypeToString(propDesc.getServerPropertyType()));
				dlg.lstOrigin.add("        ClientType " + clientPropTypeToString(propDesc.getType()));
				dlg.lstOrigin.add("        CompoundType " + propDesc.getCompoundObjectType());
				dlg.lstOrigin.add("        IsArray " + propDesc.isArray());
				dlg.lstOrigin.add("        IsDisplayable " + propDesc.isDisplayable());
				dlg.lstOrigin.add("        IsEnabled " + propDesc.isEnabled());
				dlg.lstOrigin.add("        IsModifiable " + propDesc.isModifiable());
				dlg.lstOrigin.add("        LOV " + safeGetLOVName(propDesc.getLovReference()));
			}
		}

//		java.util.Map<String, String> propertyMap = item.getProperties();
//		for (java.util.Map.Entry<String, String> entry : propertyMap.entrySet()) {
//			dlg.lstOrigin.add(entry.getKey());
//		}
	}

	private void CreateNewItem(String id) {

	}

	private void ReadExcelFile(String path) {
		if (!path.isEmpty()) {
			try {
				FileInputStream fis = new FileInputStream(path);
				XSSFWorkbook wb = new XSSFWorkbook(fis);
				XSSFSheet sheet = wb.getSheetAt(0);
				int cellCount = 0;
				int rowCount = 0;

				for (Row row : sheet) {
					rowCount++;
					cellCount = 0;
					if (rowCount > 1) {
						OwnerTransferModel newItem = new OwnerTransferModel(session);
						for (Cell cell : row) {
							cellCount++;
							if (cellCount == 1)
								newItem.setPartID(cell.getStringCellValue());
							else if (cellCount == 2)
								newItem.setUserID(cell.getStringCellValue());
							else
								newItem.setGroupID(cell.getStringCellValue());
						}
						newItem.CheckReviewer();
						newItem.CheckReferenced();
						dataList.add(newItem);
					}
				}
				dlg.setMessage("Import excel file success", IMessageProvider.INFORMATION);
			} catch (Exception e) {

			}
		}
	}

	private boolean CheckPermission() {
		try {
			TCComponentGroupMember groupMember = TCExtension.getCurrentGroupMember(session);
			TCComponentGroup group = groupMember.getGroup();
			if (group.toString().compareToIgnoreCase(GROUP_PERMISSION) == 0)
				return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	private String safeGetLOVName(Lov lovReference) {
		if (lovReference == null)
			return "";
		LovInfo lovInfo = lovReference.getLovInfo();
		if (lovInfo == null)
			return "nullLovInfo";
		return lovInfo.getName();
	}

	private String serverTypeToString(int serverPropertyType) {
		String type = "*UNKNOWN*";
		switch (serverPropertyType) {
		case PropertyDescription.SERVER_PROP_char:
			type = "char";
			break;
		case PropertyDescription.SERVER_PROP_date:
			type = "date";
			break;
		case PropertyDescription.SERVER_PROP_double:
			type = "double";
			break;
		case PropertyDescription.SERVER_PROP_external_reference:
			type = "ExternalReference";
			break;
		case PropertyDescription.SERVER_PROP_float:
			type = "float";
			break;
		case PropertyDescription.SERVER_PROP_int:
			type = "int";
			break;
		case PropertyDescription.SERVER_PROP_logical:
			type = "logical";
			break;
		case PropertyDescription.SERVER_PROP_note:
			type = "note";
			break;
		case PropertyDescription.SERVER_PROP_short:
			type = "short";
			break;
		case PropertyDescription.SERVER_PROP_string:
			type = "string";
			break;
		case PropertyDescription.SERVER_PROP_typed_reference:
			type = "TypedReference";
			break;
		case PropertyDescription.SERVER_PROP_typed_relation:
			type = "TypedRelation";
			break;
		case PropertyDescription.SERVER_PROP_untyped:
			type = "untyped";
			break;
		case PropertyDescription.SERVER_PROP_untyped_reference:
			type = "UntypedReference";
			break;
		case PropertyDescription.SERVER_PROP_untyped_relation:
			type = "UntypedRelation";
			break;
		}
		return type;
	}

	private String serverPropertyTypeToString(int serverPropType) {
		String type = "*UNKNOWN*";
		switch (serverPropType) {
		case PropertyDescription.PROPERTY_TYPE_UNKNOWN:
			type = "Unknown";
			break;
		case PropertyDescription.PROPERTY_TYPE_POM_ATTRIBUTE:
			type = "POM_Attribute";
			break;
		case PropertyDescription.PROPERTY_TYPE_POM_REFERENCE:
			type = "POM_Reference";
			break;
		case PropertyDescription.PROPERTY_TYPE_IMAN_RELATION:
			type = "IMAN_Relation";
			break;
		case PropertyDescription.PROPERTY_TYPE_PROP_FROM_ANOTHER_TYPE:
			type = "PropFromOtherType";
			break;
		case PropertyDescription.PROPERTY_TYPE_COMPUTED:
			type = "Computed";
			break;
		case PropertyDescription.PROPERTY_TYPE_SOURCE_PROPERTY:
			type = "SourceProperty";
			break;
		}
		return type;
	}

	private String fieldTypeToString(int fieldType) {
		String type = "*UNKNOWN*";
		switch (fieldType) {
		case PropertyDescription.FIELD_TYPE_UNKNOWN:
			type = "Unknown";
			break;
		case PropertyDescription.FIELD_TYPE_SIMPLE:
			type = "Simple";
			break;
		case PropertyDescription.FIELD_TYPE_COMPOND_OBJECT:
			type = "CompoundObject";
			break;
		}
		return type;
	}

	private String clientPropTypeToString(int fieldType) {
		String type = "*UNKNOWN*";
		switch (fieldType) {
		case PropertyDescription.CLIENT_PROP_TYPE_char:
			type = "char";
			break;
		case PropertyDescription.CLIENT_PROP_TYPE_date:
			type = "date";
			break;
		case PropertyDescription.CLIENT_PROP_TYPE_double:
			type = "double";
			break;
		case PropertyDescription.CLIENT_PROP_TYPE_float:
			type = "float";
			break;
		case PropertyDescription.CLIENT_PROP_TYPE_int:
			type = "int";
			break;
		case PropertyDescription.CLIENT_PROP_TYPE_bool:
			type = "bool";
			break;
		case PropertyDescription.CLIENT_PROP_TYPE_short:
			type = "short";
			break;
		case PropertyDescription.CLIENT_PROP_TYPE_string:
			type = "string";
			break;
		case PropertyDescription.CLIENT_PROP_TYPE_ModelObject:
			type = "ModelObject";
			break;
		}
		return type;
	}
}
