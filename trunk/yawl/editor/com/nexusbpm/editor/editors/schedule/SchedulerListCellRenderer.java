/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors.schedule;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class SchedulerListCellRenderer extends JLabel implements ListCellRenderer {
	protected static Border noFocusBorder;
	public SchedulerListCellRenderer() {
		if( noFocusBorder == null ) {
			noFocusBorder = new EmptyBorder( 1, 1, 1, 1 );
		}
		setOpaque( true );
		setBorder( noFocusBorder );
	}
	
	public Component getListCellRendererComponent(
			JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
		setText( value == null ? "" : value.toString() );
		
		Color foreground = null;
		
		if( value instanceof ScheduledContent ) {
			ScheduledContent content = (ScheduledContent) value;
			foreground = content.getColor();
		}
		
		setForeground( foreground );
		
		if( isSelected ) {
			setBackground( ScheduledContent.deriveColor(
					list.getBackground(), .6,
					Color.YELLOW, .4 ) );
		}
		else {
			setBackground( list.getBackground() );
		}
		
		Border border = null;
        if( cellHasFocus ) {
			if( isSelected ) {
				border = UIManager.getBorder( "List.focusSelectedCellHighlightBorder" );
			}
			if( border == null ) {
				border = UIManager.getBorder( "List.focusCellHighlightBorder" );
			}
		}
		else {
			border = noFocusBorder;
		}
		setBorder( border );
		
		return this;
	}
}
