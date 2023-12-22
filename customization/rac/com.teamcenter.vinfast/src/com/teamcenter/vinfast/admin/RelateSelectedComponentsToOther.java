package com.teamcenter.vinfast.admin;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.SWT;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.vinfast.bom.update.OriginPart_Handler;
import com.vf.utils.TCExtension;
import com.vinfast.sc.utilities.Utilities;

import vfplm.soa.common.service.VFUtility;

public class RelateSelectedComponentsToOther extends AbstractHandler {

	private final Logger logger;

	public RelateSelectedComponentsToOther() {
		super();
		logger = Logger.getLogger(this.getClass());
	}

	@Override
	public Object execute(ExecutionEvent evt) throws ExecutionException {
		AIFClipboard clipBoard = AIFPortal.getClipboard();
		if (!clipBoard.isEmpty()) {
			String input = "ELE00007800=ECXMRPMV7SVNM\n";
			Map<String, String> vfCodeAndEEBOM = new HashMap<String, String>();
			Map<String, TCComponent> idAndObject = new HashMap<String, TCComponent>();
			String eebomIds = "";
			for (String words : input.split("\n")) {
				String vfCode = words.split("=")[0];
				String eebom = words.split("=")[1];
				eebomIds += eebom + ";";
				vfCodeAndEEBOM.put(vfCode, eebom);
			}

			LinkedHashMap<String, String> inputQuery = new LinkedHashMap<String, String>();
			inputQuery.put("Item ID", eebomIds);
			inputQuery.put("Type", "VF*Design;Scooter*");
			AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
			TCSession session = (TCSession) application.getSession();
			TCComponent[] eebomObjs = TCExtension.queryItem(session, inputQuery, "Item...");

			for (TCComponent eebomObj : eebomObjs) {
				String eebomId = "";
				try {
					eebomId = eebomObj.getProperty("item_id");
				} catch (TCException e) {
					e.printStackTrace();
				}
				idAndObject.put(eebomId, eebomObj);
			}
			
			Vector<TCComponent> clipboardComponents = clipBoard.toVector();
			for (TCComponent vfCodeObj : clipboardComponents) {
				try {
					String vfCodeID = vfCodeObj.getProperty("item_id");
					String eebom = vfCodeAndEEBOM.get(vfCodeID);
					TCComponent eeBomObj = idAndObject.get(eebom);
					System.out.println("");
					vfCodeObj.setRelated("EC_reference_item_rel", new TCComponent[] {eeBomObj});
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}