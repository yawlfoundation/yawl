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
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class AlternatingListCellRenderer extends JLabel implements ListCellRenderer {
	
	TextRenderer renderer;
	
	     public AlternatingListCellRenderer(TextRenderer renderer) {
	         setOpaque(true);
	         this.renderer = renderer;
	     }
	     public Component getListCellRendererComponent(
	         JList list,
	         Object value,
	         int index,
	         boolean isSelected,
	         boolean cellHasFocus)
	     {
	    	 if (value != null) this.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
	         setText(value == null ? "" : renderer.render(value));
	         boolean even = index % 2 == 0;
	         if (even) {
	        	 if (isSelected) {
	        		 setForeground( SystemColor.textHighlightText);
	        		 setBackground(SystemColor.textHighlight);
	        	 } else {
	        		 setForeground(SystemColor.textText);
	        		 setBackground(new Color(255,255,220));
	        	 }
	         } else {
	        	 if (isSelected) {
	        		 setForeground( SystemColor.textHighlightText);
	        		 setBackground(SystemColor.textHighlight);
	        	 } else {
	        		 setForeground(SystemColor.textText);
	        		 setBackground(SystemColor.controlLtHighlight);
	        	 }
	         }
	         return this;
	     }
	 }