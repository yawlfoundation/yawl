/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.procletService.editor.block;

import org.yawlfoundation.yawl.procletService.util.EntityMID;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SelectEmid extends JPanel implements ActionListener {


	public SelectEmid() {
		super(new BorderLayout());
		List<EntityMID> emids = new ArrayList<EntityMID>();
		emids.add(new EntityMID("1"));
		JComboBox petList = new JComboBox(emids.toArray());
		petList.setSelectedIndex(0);
		petList.addActionListener(this);
	
		//Lay out the demo.
		add(petList, BorderLayout.PAGE_START);
		setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
	}

	/** Listens to the combo box. */
	public void actionPerformed(ActionEvent e) {
		JComboBox cb = (JComboBox)e.getSource();
		EntityMID emid = (EntityMID) cb.getSelectedItem();
		
	}

	/**
	* Create the GUI and show it.  For thread safety,
	* this method should be invoked from the
	* event-dispatching thread.
	*/
	public static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("ComboBoxDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		//Create and set up the content pane.
		JComponent newContentPane = new SelectEmid();
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);
	
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}
}

