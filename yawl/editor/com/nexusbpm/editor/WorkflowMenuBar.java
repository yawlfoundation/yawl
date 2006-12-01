package com.nexusbpm.editor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import com.nexusbpm.command.CommandExecutor.CommandCompletionListener;
import com.nexusbpm.command.CommandExecutor.ExecutionResult;

public class WorkflowMenuBar extends JMenuBar implements
		CommandCompletionListener {

	private JMenu fileMenu;
	private JMenu editMenu;
	private JMenu helpMenu;
	private JMenu monitoringMenu;
	private JMenu windowMenu;
	private JMenuItem aboutMenuItem;
	private JMenuItem contentsMenuItem;
	private JMenuItem copyMenuItem;
	private JMenuItem cutMenuItem;
	private JMenuItem deleteMenuItem;
	private JMenuItem instancesMenuItem;
	private JMenuItem noWindowOpenItem;
	private JMenuItem openMenuItem;
	private JMenuItem pasteMenuItem;
	private JMenuItem preferencesMenuItem;
	private JMenuItem redoMenuItem;
	private JMenuItem exitMenuItem;
	private JMenuItem workflowScheduleMenuItem;
	private JMenuItem saveAsMenuItem;
	private JMenuItem saveMenuItem;
	private JMenuItem scheduledEventsMenuItem;
	private JMenuItem undoMenuItem;

	public void commandCompleted(ExecutionResult result) {
		undoMenuItem.setEnabled(result.canUndo());
		redoMenuItem.setEnabled(result.canRedo());
	}
	
	public WorkflowMenuBar() {
		super();
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);

		openMenuItem = new JMenuItem("Open");
		fileMenu.add(openMenuItem);

		saveMenuItem = new JMenuItem("Save");
		fileMenu.add(saveMenuItem);

		saveAsMenuItem = new JMenuItem("Save As ...");
		fileMenu.add(saveAsMenuItem);

		exitMenuItem = new JMenuItem("Exit");
		fileMenu.add(exitMenuItem);

		///////////////////////
		// create the edit menu
		editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);

		undoMenuItem = new JMenuItem("Undo");
		undoMenuItem.setEnabled(false);
		editMenu.add(undoMenuItem);

		redoMenuItem = new JMenuItem("Redo");
		redoMenuItem.setEnabled(false);
		editMenu.add(redoMenuItem);

		editMenu.add(new JSeparator());

		cutMenuItem = new JMenuItem("Cut");
		editMenu.add(cutMenuItem);

		copyMenuItem = new JMenuItem("Copy");
		editMenu.add(copyMenuItem);

		pasteMenuItem = new JMenuItem("Paste");
		editMenu.add(pasteMenuItem);

		deleteMenuItem = new JMenuItem("Delete");
		editMenu.add(deleteMenuItem);

		editMenu.add(new JSeparator());

		preferencesMenuItem = new JMenuItem("Preferences");
		editMenu.add(preferencesMenuItem);
		/////////////////////////////
		// create the monitoring menu
		monitoringMenu = new JMenu("Monitoring");
		monitoringMenu.setMnemonic(KeyEvent.VK_M);

		instancesMenuItem = new JMenuItem("View All Instances");
		instancesMenuItem.setEnabled(false);
		monitoringMenu.add(instancesMenuItem);

		monitoringMenu.add(new JSeparator());

		scheduledEventsMenuItem = new JMenuItem("Scheduled Events");
		scheduledEventsMenuItem.setEnabled(false);
		monitoringMenu.add(scheduledEventsMenuItem);

		workflowScheduleMenuItem = new JMenuItem("Workflow Schedule");
		workflowScheduleMenuItem.setEnabled(true);
		monitoringMenu.add(workflowScheduleMenuItem);

		/////////////////////////
		// create the window menu
		windowMenu = new JMenu("Window");
		windowMenu.setMnemonic(KeyEvent.VK_W);

		noWindowOpenItem = new JMenuItem("None");
		noWindowOpenItem.setEnabled(false);
		windowMenu.add(noWindowOpenItem);

		///////////////////////
		// create the help menu
		helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);

		contentsMenuItem = new JMenuItem("Contents");
		helpMenu.add(contentsMenuItem);

		aboutMenuItem = new JMenuItem("About");
		helpMenu.add(aboutMenuItem);

		///////////////////////////////
		// create and set the main menu

		this.add(fileMenu);
		this.add(editMenu);
		this.add(monitoringMenu);
		this.add(windowMenu);
		this.add(helpMenu);
	}
	
	public JMenuItem addWindowItem(JInternalFrame f) {
        windowMenu.remove( noWindowOpenItem );
        JMenuItem item = new JFrameMenuItem(f);
        windowMenu.add( item );
        return item;
	}
	
	public void removeWindowItem(JInternalFrame f) {
		for (int i = 0; i < windowMenu.getItemCount(); i++) {
			if (windowMenu.getItem(i) instanceof JFrameMenuItem) {
				JFrameMenuItem item = ((JFrameMenuItem)windowMenu.getItem(i));
				if (item.getJFrame() == f) {
					windowMenu.remove( item );
				}
			}
		}
        if (windowMenu.getItemCount() == 0) {
            windowMenu.add( noWindowOpenItem );
        }
	}
	
	public static class JFrameMenuItem extends JRadioButtonMenuItem {
		private JInternalFrame theFrame;
		public JFrameMenuItem(JInternalFrame aFrame) {
			super();
			this.setText(aFrame.getTitle());
			this.theFrame = aFrame;
		}
		public JInternalFrame getJFrame() {return theFrame;}
		@Override
		public void setAction(Action a) {
			String oldValue = this.getText();
			super.setAction(a);
			if ((a.getValue(Action.NAME) == null || a.getValue(Action.NAME).equals(""))
					&& (oldValue != null && !oldValue.equals(""))) {
				this.setText(oldValue);
			}
		}
	}
	
	public void setAction(JMenuItem item, Action action) {
		String oldValue = item.getText();
		boolean isEnabled = item.isEnabled();
		item.setAction(action);
		if ((action.getValue(Action.NAME) == null || action.getValue(Action.NAME).equals(""))
				&& (oldValue != null && !oldValue.equals(""))) {
			item.setText(oldValue);
		}
		item.setEnabled(isEnabled);
	}
	

	public JMenuItem getCopyMenuItem() {
		return copyMenuItem;
	}

	public JMenuItem getCutMenuItem() {
		return cutMenuItem;
	}

	public JMenuItem getDeleteMenuItem() {
		return deleteMenuItem;
	}

	public JMenuItem getExitMenuItem() {
		return exitMenuItem;
	}

	public JMenuItem getInstancesMenuItem() {
		return instancesMenuItem;
	}

	public JMenuItem getNoWindowOpenItem() {
		return noWindowOpenItem;
	}

	public JMenuItem getOpenMenuItem() {
		return openMenuItem;
	}

	public JMenuItem getPasteMenuItem() {
		return pasteMenuItem;
	}

	public JMenuItem getPreferencesMenuItem() {
		return preferencesMenuItem;
	}

	public JMenuItem getRedoMenuItem() {
		return redoMenuItem;
	}

	public JMenuItem getScheduledEventsMenuItem() {
		return scheduledEventsMenuItem;
	}

	public JMenuItem getUndoMenuItem() {
		return undoMenuItem;
	}

	public JMenuItem getWorkflowScheduleMenuItem() {
		return workflowScheduleMenuItem;
	}

	public JMenu getWindowMenu() {
		return windowMenu;
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		WorkflowMenuBar bar = new WorkflowMenuBar();
		bar.getWorkflowScheduleMenuItem().setAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {}});
		frame.setJMenuBar(bar);
		frame.pack();
		frame.setVisible(true);
	}
	
}
