/*
 * Created on 30/12/2003, 15:40:03
 * YAWLEditor v1.0 
 * 
 * Copyright (C) 2003 Lindsay Bradford
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
 *
 */

package org.yawlfoundation.yawl.editor.swing;

import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

public abstract class AbstractDoneDialog extends JDialog {
  private JButton doneButton = buildDoneButton();
  private JButton cancelButton = buildCancelButton();
  private JPanel buttonPanel;
  private boolean showCancelButton;
  
  public static final int DONE_BUTTON = 0;
  public static final int CANCEL_BUTTON = 1;
  
  private int buttonSelected = CANCEL_BUTTON;

  protected boolean closeCancelled = false;

  public AbstractDoneDialog(String title, 
                            boolean modality,
                            JPanel contentPanel, 
                            boolean showCancelButton) {
    super();
    this.setTitle(title);
    this.setModal(modality);
    this.showCancelButton = showCancelButton;
    setContentPanel(bindContentAndButton(contentPanel));
    setUndecorated(false);
  }
    
  public AbstractDoneDialog(String title, boolean modality, boolean showCancelButton) {
    super();
    this.setTitle(title);
    this.setModal(modality);
    this.showCancelButton = showCancelButton;
  }

  public AbstractDoneDialog(String title, boolean modality) {
    super();
    this.setTitle(title);
    this.setModal(modality);
    this.showCancelButton = true;
  }
  
  public AbstractDoneDialog() {
    super();
  }
    
  public void setContentPanel(JPanel contentPanel) {
    getContentPane().add(
        bindContentAndButton(contentPanel), 
        BorderLayout.CENTER
    );
    makeLastAdjustments(); 
  }
  
  protected void makeLastAdjustments() {} // override as necessary

  private JPanel bindContentAndButton(JPanel contentPanel) {
    JPanel panel = new JPanel(new BorderLayout());
  
    panel.add(contentPanel, BorderLayout.CENTER);
    panel.add(buildButtonPanel(), BorderLayout.SOUTH);
  
    return panel;
  }

  private JButton buildDoneButton() {
   JButton button = new JButton("Done");
   button.setMnemonic(KeyEvent.VK_D);
   button.setMargin(new Insets(2,11,3,12));
   final JDialog dialog = this;
   button.addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e) {
         if (! closeCancelled) dialog.setVisible(false);
         SpecificationUndoManager.getInstance().setDirty(true);
         buttonSelected = DONE_BUTTON;
         closeCancelled = false;            // reset for next time  
       }
     }
   );
   if (!showCancelButton) {
     button.setDefaultCapable(true);
   }
   return button; 
  }

  private JButton buildCancelButton() {
    JButton button = new JButton("Cancel");
    button.setMnemonic(KeyEvent.VK_C);
    button.setMargin(new Insets(2,11,3,12));
    final JDialog dialog = this;
    button.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          dialog.setVisible(false);
          buttonSelected = CANCEL_BUTTON;
        }
      }
    );
    if (showCancelButton) {
      button.setDefaultCapable(true);
    }
    return button; 
  }
  
  public JButton getDoneButton() {
    return doneButton;
  }
  
  public JButton getCancelButton() {
    return cancelButton;
  }
  
  private JPanel buildButtonPanel() {
    buttonPanel = new JPanel();
    buttonPanel.setBorder(new EmptyBorder(17,12,11,11));
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
  
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(doneButton);

    if (showCancelButton) {
      buttonPanel.add(Box.createHorizontalStrut(10));
      buttonPanel.add(cancelButton);

      LinkedList<JButton> buttonList = new LinkedList<JButton>();

      buttonList.add(doneButton);
      buttonList.add(cancelButton);
      
      JUtilities.equalizeComponentSizes(buttonList);
    }
    buttonPanel.add(Box.createHorizontalGlue());
  
    return buttonPanel;
  }

  protected JPanel getButtonPanel() {
      return buttonPanel;
  }

  public boolean cancelButtonSelected() {
    return this.buttonSelected == CANCEL_BUTTON;
  }
}
