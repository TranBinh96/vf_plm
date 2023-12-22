package com.teamcenter.vinfast.admin;

import com.teamcenter.rac.util.MessageBox;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

public class PartExtraInforUpdate_Dialog extends TitleAreaDialog {
	private Composite area;

	private Composite container;

	public Button btnAccept;

	public Browser brwReport;

	public PartExtraInforUpdate_Dialog(Shell parentShell) {
		super(parentShell);
	}

	protected Control createDialogArea(Composite parent) {
		this.area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) this.area.getLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		try {
			this.container = new Composite(this.area, 0);
			GridLayout gl_container = new GridLayout(1, false);
			this.container.setLayout((Layout) gl_container);
			this.container.setLayoutData(new GridData(1808));
			this.brwReport = new Browser(this.container, 0);
			this.brwReport.setLayoutData(new GridData(4, 4, true, true, 1, 1));
		} catch (Exception ex) {
			MessageBox.post(ex.toString(), "ERROR", 1);
		}
		return (Control) this.area;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Part Extra Information");
	}

	protected Point getInitialSize() {
		return new Point(400, 300);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		this.btnAccept = createButton(parent, 12, "Update", false);
	}
}
