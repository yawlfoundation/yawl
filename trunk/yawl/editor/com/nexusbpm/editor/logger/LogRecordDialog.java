package com.nexusbpm.editor.logger;

import java.awt.Dimension;

import com.nexusbpm.editor.WorkflowEditor;

/**
 * The dialog window that pops up when you double click on a log record.
 *
 * @author Dean Mao
 * @author Daniel Gredler
 * @created September 8, 2003
 */
class LogRecordDialog extends InfoPopup {

	/**
	 * Constructor for a log record dialog.
	 * @param record the log record to display.
	 */
	public LogRecordDialog( LogRecordI record ) {

		super( WorkflowEditor.getInstance(), "Log Record", true );

		String msg = record.getMessage();
		if( record.getThrowableMessage() != null && record.getThrowableMessage().length() > 0 ) {
			msg += "\n\n" + record.getThrowableMessage();
		}

		setMessage( msg );
	}

	/**
	 * Returns the preferred size of this container.
	 * @see InfoPopup#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		return new Dimension( 700, 450 );
	}

}
