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

