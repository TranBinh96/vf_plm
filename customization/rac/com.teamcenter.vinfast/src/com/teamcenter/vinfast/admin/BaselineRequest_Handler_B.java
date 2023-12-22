package com.teamcenter.vinfast.admin;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

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
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.bom.StructureManagementService;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.BaselineInput;
import com.teamcenter.services.rac.bom._2008_06.StructureManagement.BaselineResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateOrUpdateRelativeStructureResponse;
import com.teamcenter.services.rac.cad._2007_12.StructureManagement;
import com.teamcenter.services.rac.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructureInfo2;
import com.teamcenter.services.rac.cad._2007_12.StructureManagement.CreateOrUpdateRelativeStructurePref2;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.GetNextIdsResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.InfoForNextId;
import com.teamcenter.services.rac.manufacturing._2011_06.DataManagement.OpenContextInfo;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.vinfast.utils.Utils;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class BaselineRequest_Handler_B extends AbstractHandler {
	private TCSession session;
	private BaselineRequest_Dialog dlg;

	private TCComponentBOMLine selectedObject;
	private LinkedHashMap<String, LinkedHashMap<String, String>> reportMap = null;
	private LinkedList<PackingItem> packageList = null;
	private String[] reportHeader = { "Package Number", "Part Number", "Revision", "Make/Buy", "Donor Vehicle",
			"Part Category", "Item Checkout", "Item Release Status" };

	private static String GROUP_PERMISSION = "dba";

	public BaselineRequest_Handler_B() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			if (!CheckPermission()) {
				MessageBox.post("You are not authorized to baseline.",
						"Please change to group: " + GROUP_PERMISSION + " and try again.", "Access", MessageBox.ERROR);
				return null;
			}

			InterfaceAIFComponent[] targetComp = AIFUtility.getCurrentApplication().getTargetComponents();
			if (targetComp[0] instanceof TCComponentBOMLine) {
				selectedObject = (TCComponentBOMLine) targetComp[0];
			} else {
				MessageBox.post("Select a bomline.", "", MessageBox.ERROR);
				return null;
			}
			reportMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
			packageList = new LinkedList<PackingItem>();

//			TCComponentBOMLine topLine = (TCComponentBOMLine) selectedObject;
//			if (topLine.getBOMViewRevision().isCheckedOut()) {
//				MessageBox.post("BOMView Need to be checked in", "Error", MessageBox.ERROR);
//				return null;
//			}

			dlg = new BaselineRequest_Dialog(new Shell());
			dlg.create();

			// Init Data
			String[] revRulesDataForm = TCExtension.GetRevisionRules(session);
			String[] baselineTemplateDataForm = TCExtension.GetPreferenceValues("Baseline_release_procedures", session);
			// Init UI

			dlg.cbRevisionRule.setItems(revRulesDataForm);
			dlg.cbBaselineTemplate.setItems(baselineTemplateDataForm);
			dlg.btnCreate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						packingProcess();
					} catch (Exception e2) {

					}
				}
			});

			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void packingProcess() {
		LinkedList<TCComponentBOMLine> allBOMLineInWindow = new LinkedList<>();
		HashSet<TCComponentBOMLine> singlePartBOMLine = new HashSet<TCComponentBOMLine>();

		HashMap<String, HashSet<TCComponentBOMLine>> assyByLevel = new HashMap<String, HashSet<TCComponentBOMLine>>();

		allBOMLineInWindow = TCExtension.expandAllBOMLines((TCComponentBOMLine) selectedObject, session);

		int numberEachPackage = 10;
		String numberPackage = dlg.txtPackageQuantity.getText();

		if (StringExtension.isInteger(numberPackage, 10)) {
			numberEachPackage = Integer.parseInt(numberPackage);
		}

		int i = 0;
		TCComponentBOMLine topBom = null;
		HashSet<String> uniqueID = new HashSet<>();
		String partNumber = "";
		String packageNumber = "";
		String hasChildren = "";
		String releaseStatus = "";
		String levelBOMLine = "";

		try {
			for (TCComponentBOMLine oneLine : allBOMLineInWindow) {
				partNumber = oneLine.getPropertyDisplayableValue("bl_item_item_id");
				releaseStatus = oneLine.getPropertyDisplayableValue("bl_item_release_statuses");

				if (uniqueID.contains(partNumber) || !releaseStatus.isEmpty())
					continue;
				uniqueID.add(partNumber);

				hasChildren = oneLine.getPropertyDisplayableValue("bl_has_children");

				if (Boolean.parseBoolean(hasChildren)) {
					levelBOMLine = oneLine.getPropertyDisplayableValue("bl_level_starting_0");

					if (assyByLevel.containsKey(levelBOMLine)) {
						assyByLevel.put(levelBOMLine, new HashSet<TCComponentBOMLine>());
						assyByLevel.get(levelBOMLine).add(oneLine);
					} else {
						assyByLevel.get(levelBOMLine).add(oneLine);
					}
				} else {
					singlePartBOMLine.add(oneLine);
				}
			}

			if (i % numberEachPackage == 0) {
				TCComponentItemRevision packageItemRev = createPackage();
				packageNumber = packageItemRev.getItem().getPropertyDisplayableValue("item_id");
				packageList.add(new PackingItem(packageNumber, packageItemRev));

				if (packageItemRev != null) {
					boolean createBomView = createBomViewRevision(packageItemRev, true);
					if (createBomView) {
						topBom = getTopBom(packageItemRev);
					}
				}
			}
		} catch (Exception e) {

		}
		if (dlg.ckbBaseline.getSelection()) {
			baselineProcess();
		}
	}

	private void packageForAssembly(HashMap<String, HashSet<TCComponentBOMLine>> assyByLevel) {
		Set<String> levelSet = assyByLevel.keySet();

		for (String level : levelSet) {
			int lvl = Integer.parseInt(level);
			HashSet<TCComponentBOMLine> assySet = assyByLevel.get(level);

			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					TCComponentBOMLine topBom;
					TCComponentItemRevision packageItemRev = createPackage(level);
					String packageNumber = "";
						try {
							packageNumber = packageItemRev.getItem().getPropertyDisplayableValue("item_id");
						} catch (NotLoadedException | TCException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (packageItemRev != null) {
							boolean createBomView = createBomViewRevision(packageItemRev, true);
							if (createBomView) {
								topBom = getTopBom(packageItemRev);
								for (TCComponentBOMLine assyBOMLine : assySet) {
									addToBOMLine(assyBOMLine, topBom);
								}
							}
						}
					packageList.add(new PackingItem(packageNumber, packageItemRev));

				}
			});
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void baselineProcess() {
		String releaseProcess = dlg.cbBaselineTemplate.getText();
		TCComponentRevisionRule revRule = TCExtension.GetRevisionRule(dlg.cbRevisionRule.getText(), session);
		String desc = dlg.txtDesc.getText();

		for (PackingItem packing : packageList) {
			try {
				if (packing.isBaseline)
					createBaseline(packing.object, releaseProcess, revRule, desc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private TCComponentItemRevision createBaseline(TCComponentItemRevision itemRevision, String releaseProcess,
			TCComponentRevisionRule revRule, String desc) throws NotLoadedException {
		BaselineInput input = new BaselineInput();
		input.clientID = itemRevision.getUid();
		input.baselineJobName = "Baseline_" + itemRevision.getPropertyDisplayableValue("item_id") + "/"
				+ itemRevision.getPropertyDisplayableValue("current_revision_id");
		input.baselineJobDescription = desc;
		input.itemRev = itemRevision;
		input.dryrun = false;
		input.precise = true;
		input.releaseProcess = releaseProcess;
		input.revRule = revRule;
		input.viewType = "view";

		StructureManagementService service = StructureManagementService.getService(session);
		BaselineResponse response = service.createBaseline(new BaselineInput[] { input });
		if (response.output.length > 0) {
			dlg.setMessage("Baseline success", IMessageProvider.INFORMATION);
			return response.output[0].baselineItemRev;
		} else {
			dlg.setMessage(Utils.HanlderServiceData(response.serviceData), IMessageProvider.ERROR);
			return null;
		}
	}

	private TCComponentItemRevision createPackage() {
		TCComponentItemRevision cfgContext = null;

		try {
			String objectType = "VF4_Study_Part";
			DataManagementService dms = DataManagementService.getService(session);
			InfoForNextId nextID = new InfoForNextId();
			nextID.propName = "item_id";
			nextID.typeName = objectType;
			nextID.pattern = "NNNNNN";

			GetNextIdsResponse IDReponse = dms.getNextIds(new InfoForNextId[] { nextID });
			String[] ids = IDReponse.nextIds;

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = objectType;
			itemDef.data.stringProps.put("item_id", ids[0]);
			itemDef.data.stringProps.put("object_name", "Part package");

			CreateInput revDef = new CreateInput();
			revDef.boName = objectType + "Revision";
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				for (TCComponent rev : response.output[0].objects) {
					if (rev.getType().equals(objectType + "Revision")) {
						cfgContext = (TCComponentItemRevision) rev;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cfgContext;
	}

	private TCComponentItemRevision createPackage(String packageName) {
		TCComponentItemRevision cfgContext = null;

		try {
			String objectType = "VF4_Study_Part";
			DataManagementService dms = DataManagementService.getService(session);
			InfoForNextId nextID = new InfoForNextId();
			nextID.propName = "item_id";
			nextID.typeName = objectType;
			nextID.pattern = "NNNNNN";

			GetNextIdsResponse IDReponse = dms.getNextIds(new InfoForNextId[] { nextID });
			String[] ids = IDReponse.nextIds;

			CreateIn itemDef = new CreateIn();
			itemDef.clientId = "1";
			itemDef.data.boName = objectType;
			itemDef.data.stringProps.put("item_id", ids[0]);
			itemDef.data.stringProps.put("object_name", packageName);

			CreateInput revDef = new CreateInput();
			revDef.boName = objectType + "Revision";
			itemDef.data.compoundCreateInput.put("revision", new CreateInput[] { revDef });

			CreateResponse response = dms.createObjects(new CreateIn[] { itemDef });

			if (response.serviceData.sizeOfPartialErrors() == 0) {
				for (TCComponent rev : response.output[0].objects) {
					if (rev.getType().equals(objectType + "Revision")) {
						cfgContext = (TCComponentItemRevision) rev;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cfgContext;
	}

	private TCComponentBOMLine getTopBom(TCComponentItemRevision itemRevision) {
		OpenContextInfo[] createdBOMViews = TCExtension.CreateContextViews(itemRevision, session);
		if (createdBOMViews != null) {
			if (createdBOMViews.length > 0) {
				if (createdBOMViews[0].context.getType().equals("BOMLine")) {
					return (TCComponentBOMLine) createdBOMViews[0].context;
				}
			}
		}
		return null;
	}

	private boolean createBomViewRevision(TCComponentItemRevision revision, Boolean precise) {
		try {
			CreateOrUpdateRelativeStructureInfo2[] info = new CreateOrUpdateRelativeStructureInfo2[1];
			info[0] = new CreateOrUpdateRelativeStructureInfo2();
			info[0].lastModifiedOfBVR = Calendar.getInstance();
			info[0].parent = revision;
			info[0].precise = true;

			CreateOrUpdateRelativeStructurePref2 prefs = new CreateOrUpdateRelativeStructurePref2();
			prefs.cadOccIdAttrName = null;
			prefs.itemTypes = null;
			prefs.overwriteForLastModDate = true;

			StructureManagement smService = com.teamcenter.services.rac.cad.StructureManagementService
					.getService(session);

			CreateOrUpdateRelativeStructureResponse response = smService.createOrUpdateRelativeStructure(info,
					"PR4D_cad", true, prefs);
			if (response.serviceData.sizeOfPartialErrors() > 0) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private void addToBOMLine(TCComponentBOMLine newRev, TCComponentBOMLine topBom) {
		try {
			topBom.add(newRev.getItem(), newRev.getItemRevision(), null, false);
			topBom.window().save();
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private void resetDialog() {
		dlg.txtDesc.setText("");
		dlg.cbBaselineTemplate.deselectAll();
		dlg.cbRevisionRule.deselectAll();
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

	class PackingItem {
		public String partID;
		public TCComponentItemRevision object;
		public boolean isBaseline;

		public PackingItem(String _partID, TCComponentItemRevision _object) {
			partID = _partID;
			object = _object;
			isBaseline = true;
		}
	}
}
