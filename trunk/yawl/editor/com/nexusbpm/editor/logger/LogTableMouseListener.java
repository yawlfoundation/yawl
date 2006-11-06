/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.logger;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 * @author    Dean Mao
 * @author    Daniel Gredler
 * @created   May 26 2004
 */
public class LogTableMouseListener extends MouseAdapter implements ClipboardOwner
{
	private JTable _table;

	/**
	 *Constructor for the MyMouseAdaptor object
	 *
	 * @param  table  Description of the Parameter
	 */
	public LogTableMouseListener(JTable table)
	{
		_table = table;
	}

	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseClicked(MouseEvent e)
	{
		int clicks = e.getClickCount();
		int button = e.getButton();

		if(clicks == 2 && button == MouseEvent.BUTTON1)
		{
			// Double-click with the left mouse button.
			int row = _table.rowAtPoint(e.getPoint());
			CapselaLog log = (CapselaLog) _table.getModel();
			LogRecordI record = (LogRecordI) log.getElementAt(row);
			LogRecordDialog dialog = new LogRecordDialog(record);
			dialog.setVisible(true);
		}
		else if(clicks == 1 && button == MouseEvent.BUTTON3)
		{
			// Single right click.
			int row = _table.rowAtPoint(e.getPoint());
			_table.getSelectionModel().setSelectionInterval(row, row);
			CapselaLog log = (CapselaLog) _table.getModel();
			LogRecordI record = (LogRecordI) log.getElementAt(row);
			JPopupMenu menu = createPopupMenu(log, record);
			if(menu != null)
			{
				menu.show(_table, e.getX(), e.getY());
			}
		}
	}

	private JPopupMenu createPopupMenu(final CapselaLog log, final LogRecordI record)
	{
		JPopupMenu menu = new JPopupMenu();

		menu.add(new AbstractAction("Copy")
		{
			public void actionPerformed(ActionEvent e)
			{
				StringSelection contents = new StringSelection(record.toString());
				ClipboardOwner owner = LogTableMouseListener.this;
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(contents, owner);
			}
		});

		menu.add(new AbstractAction("Copy All")
		{
			public void actionPerformed(ActionEvent e)
			{
				StringSelection contents = new StringSelection(log.toString());
				ClipboardOwner owner = LogTableMouseListener.this;
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(contents, owner);
			}
		});

		return menu;
	}

	/**
	 * @see ClipboardOwner#lostOwnership(Clipboard, Transferable)
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		// Empty.
	}
}