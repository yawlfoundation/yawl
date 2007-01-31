/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors.specification;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.DefaultGraphCell;

import com.nexusbpm.editor.editors.net.cells.NexusCell;
import com.nexusbpm.editor.editors.net.cells.DefaultView;
import com.nexusbpm.editor.editors.net.renderer.IconLabel;
import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.icon.ApplicationIcon;
import com.nexusbpm.editor.persistence.EditorDataProxy;


/**
 * This is for "Zubin's long description under the icon" renderer. Puts a
 * paragraph under flow components when in the flow graph.
 * 
 * 
 * @author Dean Mao
 * @created Jun 15, 2004
 */
public class NetCellRenderer extends JComponent implements CellViewRenderer {

	private JLabel nameLabel;
//	private IconLabel iconLabel;

	/**
	 * Default constructor.
	 */
	public NetCellRenderer() {
			
//		iconLabel = new IconLabel();
//		iconLabel.setOpaque( true );
		nameLabel = new JLabel();
		nameLabel.setOpaque(true);

		GridBagLayout gbl = new GridBagLayout();
		setLayout( gbl );

		GridBagConstraints nameLabelConstraints = new GridBagConstraints();
		nameLabelConstraints.gridx = 0;
		nameLabelConstraints.gridy = 0;
		nameLabelConstraints.gridwidth = 1;
		nameLabelConstraints.gridheight = 1;
		nameLabelConstraints.weightx = 1;
		nameLabelConstraints.weighty = 0;
		nameLabelConstraints.anchor = GridBagConstraints.SOUTH;
		nameLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
		nameLabelConstraints.insets = new Insets( 0, 0, 3, 0 );
		this.add( nameLabel, nameLabelConstraints );
//		GridBagConstraints iconLabelConstraints = new GridBagConstraints();
//		iconLabelConstraints.gridx = 0;
//		iconLabelConstraints.gridy = 1;
//		iconLabelConstraints.gridwidth = 1;
//		iconLabelConstraints.gridheight = 1;
//		iconLabelConstraints.weightx = 1;
//		iconLabelConstraints.weighty = 0;
//		iconLabelConstraints.anchor = GridBagConstraints.CENTER;
//		iconLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
//		this.add( iconLabel, iconLabelConstraints );
	}

	/**
	 * @see JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.setSize( dim.getWidth() + 40, dim.getHeight() );
		return dim;
	}

	/**
	 * @see CellViewRenderer#getRendererComponent(org.jgraph.JGraph, org.jgraph.graph.CellView, boolean, boolean, boolean)
	 */
	public java.awt.Component getRendererComponent( JGraph graph, CellView view, boolean sel, boolean focus, boolean preview ) {
		nameLabel.setText(((NetCellView) view).getLabel());
		nameLabel.setIcon(((NetCellView) view).getIcon());
		return this;
	}

}