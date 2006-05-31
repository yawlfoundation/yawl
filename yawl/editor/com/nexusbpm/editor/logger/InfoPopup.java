package com.nexusbpm.editor.logger;

import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.nexusbpm.editor.WorkflowEditor;


/**
 * Popup informational message that can be copied to the clipboard.
 * Copied from LogRecordDialog - which now extends this class instead.
 *
 * Will be used in help about message.
 *
 * @author Mitchell J. Friedman
 * @created May 27, 2005
 */
public class InfoPopup extends JDialog {
    /**
     * Creates a modal or non-modal popup dialog with the specified title 
     * and the specified owner <code>Frame</code>. If <code>owner</code>
     * is <code>null</code>, a shared, hidden frame will be set as the
     * owner of this dialog. All constructors defer to this one.
     * 
     * @see JDialog#JDialog(java.awt.Frame, java.lang.String, boolean)
     * 
     * @param owner the <code>Frame</code> from which the dialog is displayed
     * @param title  the <code>String</code> to display in the dialog's
     *          title bar
     * @param modal  true for a modal dialog, false for one that allows
     *               other windows to be active at the same time
     */
	public InfoPopup( Frame owner, String title, boolean modal ) {
		super( owner, title, modal );
	}
    
    /**
     * Creates a modal popup dialog with the specified title and message.
     * 
     * @param title the <code>String</code> to display in the dialog's title bar
     * @param message the message to display on the popup dialog
     */
	public InfoPopup( String title, String message ) {
		super( WorkflowEditor.getInstance(), title, true );
		setMessage( message );
	}
    
    /**
     * Sets the message to be displayed on the popup dialog.
     * @param message the message to display.
     */
	protected void setMessage( String message ) {
		JTextArea ta = new JTextArea();
		ta.setEditable( false );
		ta.setLineWrap( true );
		ta.setText( message );
		ta.setPreferredSize( getPreferredSize() );
		ta.setOpaque( false );
		JOptionPane optionPane = new JOptionPane( ta, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION );
		getContentPane().add( optionPane );
		pack();

		optionPane.addPropertyChangeListener( new PropertyChangeListener() {
			public void propertyChange( PropertyChangeEvent e ) {
				setVisible( false );
			}
		} );
		pack();

		setLocationRelativeTo( null );
	}
    
    /**
     * Returns the preferred size of this container.  
     * @return    an instance of <code>Dimension</code> that represents 
     *                the preferred size of this container.
     * @see java.awt.Container#getPreferredSize()
     */
	public Dimension getPreferredSize() {
		return new Dimension( 450, 500 );
	}
}
