package com.teamcenter.integration.arch;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.teamcenter.integration.model.MCNInformation;
import com.teamcenter.integration.ulti.TCExtension;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.vinfast.integration.model.ActionCommand;
import com.vinfast.integration.model.ActionCommand.Command;
import com.vinfast.integration.model.NoticeMessage;
import com.vinfast.integration.model.ProcessStatus;
import com.vinfast.integration.model.ServerInformation;
import com.vinfast.sap.util.PropertyDefines;

public abstract class IntegrationModuleAbstract extends AbstractHandler {
	public void setDialog(DialogAbstract dialog) {
		this.dialog = dialog;
	}

	public DialogAbstract getDialog() {
		return dialog;
	}

	private DialogAbstract dialog = null;

	public IntegrationModuleAbstract() {
		System.out.println("IntegrationModuleAbstract");
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// initiate
			ProcessorManager.getInstance().addListener(this);
			ProcessorManager.getInstance().reset();
			initModule();
			ProcessorManager.getInstance().startAllProcessor();

			//
			TCHelper.getInstance().session = (TCSession) AIFUtility.getCurrentApplication().getSession();
			TCHelper.getInstance().dmService = DataManagementService.getService(TCHelper.getInstance().session);
			AbstractAIFUIApplication app = AIFUtility.getCurrentApplication();
			InterfaceAIFComponent[] targetComponents = app.getTargetComponents();
			validObjectSelect(targetComponents);
			if (TCHelper.getInstance().changeObject == null) {
				MessageBox.post("Please Select 1 MCN Revision.", "Error", MessageBox.ERROR);
				return null;
			}

			dialog.setClientSession(TCHelper.getInstance().session);

			if (!TCExtension.checkPermissionAccess(TCHelper.getInstance().changeObject, "WRITE", TCHelper.getInstance().session)) {
				MessageBox.post("You do not have access on selected MCN to transfer.", "Error", MessageBox.ERROR);
				return null;
			}

			if (!validate())
				return null;

			// ui
			dialog.create();
			Button transferBtn = dialog.getBtnTransfer();
			Button prepareBtn = dialog.getBtnPrepare();
			transferBtn.setEnabled(false);
			prepareBtn.setEnabled(true);

			Combo server = dialog.getComboServer();
			server.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							sendMessage(PropertyDefines.BOM_PROCESSOR_NAME, new ServerInformation(dialog.getServer(), dialog.getServerIP()));
							sendMessage(PropertyDefines.CONNECTION_PROCESSOR_NAME, new ServerInformation(dialog.getServer(), dialog.getServerIP()));
						}
					});
				}
			});

//			Combo ip = dialog.getComboIP();
//			ip.addListener(SWT.Selection, new Listener() {
//				public void handleEvent(Event e) {
//					Display.getDefault().asyncExec(new Runnable() {
//						@Override
//						public void run() {
//							sendMessage(PropertyDefines.BOM_PROCESSOR_NAME, new ServerInformation(dialog.getServer(), dialog.getServerIP()));
//							sendMessage(PropertyDefines.CONNECTION_PROCESSOR_NAME, new ServerInformation(dialog.getServer(), dialog.getServerIP()));
//						}
//					});
//				}
//			});

			prepareBtn.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							prepareBtn.setEnabled(false);
							sendMessage(PropertyDefines.BOM_PROCESSOR_NAME, new ActionCommand(Command.COMMAND_PREPARE));
						}
					});
				}
			});

			transferBtn.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							transferBtn.setEnabled(false);
							sendMessage(PropertyDefines.BOM_PROCESSOR_NAME, new ActionCommand(Command.COMMAND_TRANSFER));
						}
					});
				}
			});

			dialog.getShell().addListener(SWT.Dispose, new Listener() {
				@Override
				public void handleEvent(Event e) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							ProcessorManager.getInstance().stopAllProcessor();
						}
					});
				}
			});

			sendMessage(PropertyDefines.BOM_PROCESSOR_NAME, new ActionCommand(Command.COMMAND_LOAD_MCN_INFO));
			dialog.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void sendMessage(String name, ModelAbstract msg) {
		ProcessorManager.getInstance().sendMessage(name, msg);
	}

	protected abstract void initModule();

	protected abstract boolean validate();

	protected void onUpdateUiEvent(ModelAbstract event) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				switch (event.getModelType()) {
				case MCN_INFORMATION:
					onUpdateMCN((MCNInformation) event);
					break;
				case NOTICE_MESSAGE:
					onNoticeMessage((NoticeMessage) event);
					break;
				case PROCESS_STATUS:
					onProcessStatus((ProcessStatus) event);
					break;
				case ACTION_COMMAND:
					onCommand((ActionCommand) event);
					break;
				default:
					break;
				}
			}
		});
	}

	protected abstract void onUpdateMCN(MCNInformation mcn);

	protected void onNoticeMessage(NoticeMessage notice) {
		switch (notice.getType()) {
		case INFO:
			MessageBox.post(notice.getNotice(), "Info", MessageBox.INFORMATION);
			break;
		case ERROR:
			MessageBox.post(notice.getNotice(), "Error", MessageBox.ERROR);
			break;
		case WARNING:
			MessageBox.post(notice.getNotice(), "Warning", MessageBox.WARNING);
			break;
		default:
			break;
		}
	}

	protected void onProcessStatus(ProcessStatus status) {
		switch (status.getUpdateType()) {
		case INIT_TOTAL:
			this.dialog.setTotal(status.getTotal());
			break;
		case UPDATE_PROCESS_BAR:
			this.dialog.updateprogressBar();
			break;
		case UPDATE_PROCESS_BAR_PERCENT:
			this.dialog.setProcessPercent(status.getPercent());
			break;
		case UPDATE_PROCESS_LABEL_STATUS:
			if (this.dialog.getLbProcessStatus() != null) {
				this.dialog.setLabelProcessStatus(status.getProcessLabelStatus());
			}
			break;
		case UPDATE_RUNNING_INDICATOR:
			if (this.dialog.getLbProcessStatus() != null) {
				String curText = this.dialog.getLbProcessStatus().getText();
				String lb = curText.replaceAll("\\.", "").trim();
				this.dialog.setLabelProcessStatus(String.format("%s %s", lb, status.getIndicator()));
			}
			break;
		default:
			break;
		}
	}

	protected void onCommand(ActionCommand cmd) {
		switch (cmd.getCommand()) {
		case ALLOW_TO_TRANSFER:
			this.dialog.getBtnTransfer().setEnabled(true);
			break;
		default:
			break;
		}
	}

	private void validObjectSelect(InterfaceAIFComponent[] targetComponents) {
		if (targetComponents == null)
			return;
		if (targetComponents.length > 1)
			return;
		if (targetComponents[0] instanceof TCComponentItemRevision) {
			TCHelper.getInstance().changeObject = (TCComponentItemRevision) targetComponents[0];
		}
	}
}
