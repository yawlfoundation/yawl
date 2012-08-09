/*
 * Created on 2/07/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * 
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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

public abstract class AbstractTableUpdatePanel extends JPanel {
  public static final int NO_ELEMENTS = 0;
  public static final int SOME_ELEMENTS = 1;
  public static final int ALL_ELEMENTS = 2;
    
  private JButton createButton;
  private JButton updateButton;
  private JButton removeButton;
  
  private JOrderedSingleSelectTable table;
  
  private AbstractOrderedTablePanel orderedTablePanel;
  
  public AbstractTableUpdatePanel() {
    super();
    orderedTablePanel = new AbstractOrderedTablePanel();
    buildContent();
  }
  
  private AbstractOrderedTablePanel getOrderedTablePanel() {
    return this.orderedTablePanel;
  }
  
  private void buildContent() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    setTable(buildTable());
    
    getOrderedTablePanel().setOrderedTable(getTable());

    buildCreateButton();
    buildUpdateButton();
    buildRemoveButton();

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridheight = 5;
    gbc.weightx = 1;
    gbc.insets = new Insets(0,0,0,5);
    gbc.fill = GridBagConstraints.BOTH;
    
    add(getOrderedTablePanel(),gbc);

    gbc.gridx = 1;
    gbc.gridheight = 1;
    gbc.weightx = 0;
    gbc.weighty = 0.5;
    gbc.insets = new Insets(0,5,5,0);
    add(Box.createVerticalGlue(),gbc);

    gbc.gridy++;
    gbc.weighty = 0;
    gbc.anchor = GridBagConstraints.CENTER;
    add(createButton, gbc);

    gbc.gridy++;
    add(updateButton, gbc);

    gbc.gridy++;
    add(removeButton, gbc);

    gbc.gridy++;
    gbc.weighty = 0.5;
    gbc.fill = GridBagConstraints.BOTH;
    add(Box.createVerticalGlue(),gbc);
  }
  
  private JButton buildCreateButton() {
   createButton = new JButton("Create...");
   createButton.setMnemonic(KeyEvent.VK_C);
   createButton.setMargin(new Insets(2,11,3,12));
   final AbstractTableUpdatePanel panel = this;
   createButton.addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e) {
          panel.doCreateButtonAction();
       }
     }
   );
   return createButton; 
  }
  
  private JButton buildUpdateButton() {
   updateButton = new JButton("Update...");
   updateButton.setMnemonic(KeyEvent.VK_U);
   updateButton.setMargin(new Insets(2,11,3,12));
   final AbstractTableUpdatePanel panel = this;
   updateButton.addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e) {
         panel.doUpdateButtonAction();
       }
     }
   );
   
   table.addMouseListener(new MouseAdapter(){
     public void mouseClicked(MouseEvent e){
       if (e.getClickCount() == 2){
         if (table.getSelectedRow() >= 0) {
           panel.doUpdateButtonAction();
         }
       }
     }
   });
   
   return updateButton; 
  }

  private JButton buildRemoveButton() {
   removeButton = new JButton("Remove...");
   removeButton.setMnemonic(KeyEvent.VK_R);
   removeButton.setMargin(new Insets(2,11,3,12));
   final AbstractTableUpdatePanel  panel = this;
   removeButton.addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e) {
         panel.doRemoveButtonAction();
       }
     }
   );
   return removeButton; 
  }

  public void updateState() {
    
    if (getTable() == null || 
        getTable().getRowCount() == 0) {
      setState(NO_ELEMENTS);
      return;
    }
    
    if (getTable().getRowCount() >= 
        rowLimit()) {
      setState(ALL_ELEMENTS);
      return;
    }
    setState(SOME_ELEMENTS);
  }

  public void setState(int state) {
    if (!isEnabled()) {
      createButton.setEnabled(false);
      updateButton.setEnabled(false);
      removeButton.setEnabled(false);
      return;
    }
    if (state == NO_ELEMENTS) {
      createButton.setEnabled(true);
      updateButton.setEnabled(false);
      removeButton.setEnabled(false);
    }
    if (state == SOME_ELEMENTS) {
      createButton.setEnabled(true);
      updateButton.setEnabled(true);
      removeButton.setEnabled(true);
    }
    if (state == ALL_ELEMENTS) {
      createButton.setEnabled(false);
      updateButton.setEnabled(true);
      removeButton.setEnabled(true);
    }
  }
  
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    updateState();
  }

  public boolean hasElements() {
    return (getTable().getRowCount() > 0);
  }
  
  public JOrderedSingleSelectTable getTable() {
    return table;
  }
  
  public void setTable(JOrderedSingleSelectTable table) {
    this.table = table;
  }
  
  protected abstract JOrderedSingleSelectTable buildTable();
  
  protected abstract void doCreateButtonAction();

  protected abstract void doUpdateButtonAction();

  protected abstract void doRemoveButtonAction();
  
  protected abstract int rowLimit();
}
