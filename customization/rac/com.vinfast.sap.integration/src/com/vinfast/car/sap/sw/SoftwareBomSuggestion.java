package com.vinfast.car.sap.sw;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.vinfast.sap.dialogs.UpdateSoftwareSuggestionFrame;
import com.vinfast.sap.util.PropertyDefines;
import com.vinfast.sap.util.UIGetValuesUtility;

public class SoftwareBomSuggestion extends AbstractHandler {

	private UpdateSoftwareSuggestionFrame fr;
	private TCSession session; 
	private SoftwareBomSuggestionHandler handler;
			
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createDialogCompare();
			}
		});
		return null;
	}
	
	private void createDialogCompare() {
		
		fr = new UpdateSoftwareSuggestionFrame();
		fr.setTitle("MBOM & SWBOM Compare");
		fr.btnUpdateSWBOM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				TCComponent component = UIGetValuesUtility.getComponent();
				if(session == null) {
					session = component.getSession();
				}
				String err = UpdateSWBOMValidator.isValidSWBOM(component, session);
				if(err.isEmpty()) {
					try {
						fr.setSWBOMID(component.getProperty(PropertyDefines.BOM_ITEM_ID));
						fr.setSWBOM(component);
					} catch (TCException e) {
						e.printStackTrace();
					}
				}else {
					MessageBox.post(err, "Error", MessageBox.ERROR);
				}
			}
		});
		
		fr.btnUpdateSubGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				TCComponent component = UIGetValuesUtility.getComponent();
				if(session == null) {
					session = component.getSession();
				}
				String err = UpdateSWBOMValidator.isValidSubGroup(component, session);
				if(err.isEmpty()) {
					try {
						fr.setSubGroupID(component.getProperty(PropertyDefines.BOM_ITEM_ID));
						fr.setSubGroup(component);
					} catch (TCException e) {
						e.printStackTrace();
					}
				}else {
					MessageBox.post(err, "Error", MessageBox.ERROR);
				}
			}
		});
		
		
		
		fr.btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				fr.dispose();
			}
		});
		
		
		fr.btnExec.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				fr.btnExec.setEnabled(false);
				if(!fr.isFilledInput()) {
					MessageBox.post("Please select both Subgroup & SWBOM", "Error", MessageBox.ERROR);
				}else {
					handler = new SoftwareBomSuggestionHandler();
					handler.setFr(fr);
					handler.start();
				}
				fr.btnExec.setEnabled(true);
			}
		});

		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
	}
	

	
	

}
