/*
 * Created on 07/06/2007
 * YAWLEditor v1.4.6
 * 
 * Copyright (C) 2007 Lindsay Bradford
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.yawlfoundation.yawl.editor.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

public class AbstractOrderedTablePanel extends JPanel implements ListSelectionListener {

  public static enum ElementState {NO_ELEMENTS, SOME_ELEMENTS};
  
  private ElementState elementState = ElementState.NO_ELEMENTS;
  
  private JOrderedSingleSelectTable table;

  private JButton moveRowUpButton;
  private JButton moveRowDownButton;
  
  private static final long serialVersionUID = 1L;
  
  public AbstractOrderedTablePanel() {
    super();
  }
  
  public JOrderedSingleSelectTable getOrderedTable() {
    return table;
  }

  public void setOrderedTable(JOrderedSingleSelectTable table) {
    this.table = table;
    buildContent();
    table.getSelectionModel().addListSelectionListener(this);
    updateState();
  }

  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) {
      return;  // The mouse button has not yet been released
    }
    moveRowUpButton.setEnabled(true);
    moveRowDownButton.setEnabled(true);
    
    int row = getOrderedTable().getSelectedRow();

    if (row == 0) {
      moveRowUpButton.setEnabled(false);
    } 
    if (row == (getOrderedTable().getRowCount() - 1)) {
      moveRowDownButton.setEnabled(false);
    } 
  }

  
  private void buildContent() {
    setBorder(new EmptyBorder(12,12,0,11));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridheight = 5;
    gbc.weightx = 1;
    gbc.insets = new Insets(0,0,0,2);
    gbc.fill = GridBagConstraints.BOTH;
    
    add(new JScrollPane(getOrderedTable()),gbc);

    gbc.gridx = 1;
    gbc.gridheight = 1;
    gbc.weightx = 0;
    gbc.weighty = 0.5;
    gbc.insets = new Insets(0,0,0,0);
    add(Box.createVerticalGlue(),gbc);

    gbc.gridy++;
    gbc.weighty = 0;
    gbc.insets = new Insets(0,2,2,0);
    gbc.anchor = GridBagConstraints.CENTER;
    add(getMoveRowUpButton(), gbc);

    gbc.gridy++;
    gbc.insets = new Insets(2,2,0,0);
    add(getMoveRowDownButton(), gbc);

    gbc.gridy++;
    gbc.weighty = 0.5;
    gbc.insets = new Insets(0,0,0,0);
    gbc.fill = GridBagConstraints.BOTH;
    add(Box.createVerticalGlue(),gbc);
    
    LinkedList<JButton> buttonList = new LinkedList<JButton>();
    buttonList.add(moveRowUpButton);
    buttonList.add(moveRowDownButton);
    
    JUtilities.equalizeComponentSizes(buttonList);
  }

  private JButton getMoveRowUpButton() {
    moveRowUpButton = new JButton();
    moveRowUpButton.setIcon(getIconByName("Up"));
    moveRowUpButton.setMargin(new Insets(0,0,0,0));
    moveRowUpButton.setToolTipText(" Move selected row up in the list ");
    moveRowUpButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          doMoveRowUpAction();
        }
      }
    );
    return moveRowUpButton; 
   }

  protected void doMoveRowUpAction() {
    getOrderedTable().moveRowUp();
  }
  
  private JButton getMoveRowDownButton() {
    moveRowDownButton = new JButton();
    moveRowDownButton.setIcon(getIconByName("Down"));
    moveRowDownButton.setMargin(new Insets(0,0,0,0));
    moveRowDownButton.setToolTipText(" Move selected row down in the list ");
    moveRowDownButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          doMoveRowDownAction();
        }
      }
    );
    return moveRowDownButton; 
   }
  
  protected void doMoveRowDownAction() {
    getOrderedTable().moveRowDown();
  }
  
  public void updateState() {
    if (getOrderedTable() == null) {
      setElementState(ElementState.NO_ELEMENTS); 
      return;
    }
    
    if (getOrderedTable().getRowCount() > 0) {
      setElementState(ElementState.SOME_ELEMENTS); 
    } else {
      setElementState(ElementState.NO_ELEMENTS); 
    }
  }

  public void setElementState(ElementState newState) {
    this.elementState = newState;
    switch(newState) {
      case NO_ELEMENTS: {
        moveRowUpButton.setEnabled(false);
        moveRowDownButton.setEnabled(false);
        break;
      }
      case SOME_ELEMENTS: {
        moveRowUpButton.setEnabled(true);
        moveRowDownButton.setEnabled(true);
        break;
      }
    }
  }
  
  private ImageIcon getIconByName(String iconName) {
    return ResourceLoader.getImageAsIcon(
           "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/"
           + iconName + "24.gif");
  }
}
