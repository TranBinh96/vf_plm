package com.vf.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class WaitDialog {
	private JDialog dialog;

	public void makeWait(String msg, ActionEvent evt) {

		Window win = SwingUtilities.getWindowAncestor((AbstractButton) evt.getSource());
		dialog = new JDialog(win, msg, JDialog.ModalityType.APPLICATION_MODAL);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(progressBar, BorderLayout.CENTER);
		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(win);
		dialog.setAlwaysOnTop(true);
		dialog.setResizable(false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setVisible(true);
	}
	
	public void makeWait(String msg, Window win) {
		dialog = new JDialog(win, msg, JDialog.ModalityType.APPLICATION_MODAL);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(progressBar, BorderLayout.CENTER);
		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(win);
		dialog.setAlwaysOnTop(true);
		dialog.setResizable(false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void close() {
		dialog.dispose();
	}

	public static void removeCloseButton(Component comp) {
		if (comp instanceof JMenu) {
			Component[] children = ((JMenu) comp).getMenuComponents();
			for (int i = 0; i < children.length; ++i)
				removeCloseButton(children[i]);
		} else if (comp instanceof AbstractButton) {
			Action action = ((AbstractButton) comp).getAction();
			String cmd = (action == null) ? "" : action.toString();
			if (cmd.contains("CloseAction")) {
				comp.getParent().remove(comp);
			}
		} else if (comp instanceof Container) {
			Component[] children = ((Container) comp).getComponents();
			for (int i = 0; i < children.length; ++i)
				removeCloseButton(children[i]);
		}
	}
}
