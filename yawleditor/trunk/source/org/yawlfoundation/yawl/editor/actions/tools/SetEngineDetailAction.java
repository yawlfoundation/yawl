/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.actions.tools;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

public class SetEngineDetailAction extends YAWLBaseAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final EngineDetailDialog dialog = new EngineDetailDialog();

  private boolean invokedAtLeastOnce = false;
  
  {
    putValue(Action.SHORT_DESCRIPTION, " Specify login details for a running YAWL Engine.");
    putValue(Action.NAME, "Engine Connection Settings...");
    putValue(Action.LONG_DESCRIPTION, "Specify login details for a running YAWL Engine.");
    putValue(Action.SMALL_ICON, getPNGIcon("disconnect"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_E));
  }
  
  public SetEngineDetailAction() {}
  
  public void actionPerformed(ActionEvent event) {
    if (!invokedAtLeastOnce) {
      invokedAtLeastOnce = true;
      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
    }
    dialog.setVisible(true);
  }
}

class EngineDetailDialog extends AbstractDoneDialog {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  private JTextField engineURIField;
  private JTextField engineUserField;
  private JPasswordField enginePasswordField;
//  private JPasswordField engineVerifyPasswordField;
  
  private JButton testButton;
  private JLabel testMessage = new JLabel();
  
  public EngineDetailDialog() {
    super("YAWL Engine Connection Settings", true);
    setContentPanel(getEngineDetailPanel());

    getDoneButton().addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e) {

         YAWLEngineProxy.getInstance().disconnect(); 
         
         prefs.put(
             "engineURI", 
             engineURIField.getText()
         );

         prefs.put(
             "engineUserID", 
             engineUserField.getText()
         );

         prefs.put(
             "engineUserPassword", 
             new String(enginePasswordField.getPassword())
         );

         SpecificationUndoManager.getInstance().setDirty(true);
       }
    });
  }

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }
  
  private JPanel getEngineDetailPanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel uriLabel = new JLabel("YAWL Engine URI:");
    uriLabel.setDisplayedMnemonic('U');
    panel.add(uriLabel, gbc);
    
    gbc.gridx++;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getEngineURIField(), gbc);
    uriLabel.setLabelFor(engineURIField);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.insets = new Insets(5,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel userLabel = new JLabel("User Name:");
    userLabel.setDisplayedMnemonic('N');
    panel.add(userLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getEngineUserField(), gbc);
    userLabel.setLabelFor(engineUserField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.EAST;

    JLabel passwordLabel = new JLabel("Password:");
    passwordLabel.setDisplayedMnemonic('P');
    panel.add(passwordLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getEnginePasswordField(), gbc);
    passwordLabel.setLabelFor(enginePasswordField);

    gbc.gridx = 0;
    gbc.gridy++;
//    gbc.anchor = GridBagConstraints.EAST;
//
//    JLabel verifyPasswordLabel = new JLabel("Verify Password :");
//    verifyPasswordLabel.setDisplayedMnemonic('V');
//    panel.add(verifyPasswordLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

//    panel.add(getEngineVerifyPasswordField(), gbc);
//    verifyPasswordLabel.setLabelFor(engineVerifyPasswordField);
    
    gbc.gridx = 2;
    gbc.gridy = 1;
    gbc.gridheight = 3;
    gbc.insets = new Insets(5,5,5,5);
    gbc.anchor = GridBagConstraints.CENTER;
    
    panel.add(getTestConnectionButton(), gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridheight = 1;
    gbc.gridwidth = 3;
    gbc.anchor = GridBagConstraints.CENTER;
    panel.add(testMessage, gbc);
    
    testMessage.setVisible(false);

    /* 
     * Below, I replace the default component traversal policy with my own to
     * ensure that the passwword field and password verification field are together
     * in the focus traversal order.
     */
    
    setFocusTraversalPolicy(new FocusTraversalPolicy() {
      
      public Component getInitialComponent(Window window) {
        return engineURIField;
      }
      
      public Component getFirstComponent(Container focusCycleRoot) {
        return engineURIField;
      }

      public Component getDefaultComponent(Container focusCycleRoot) {
        return getDoneButton();
      }
      
      public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
        if (aComponent.equals(engineURIField)) {
          return engineUserField;
        }
        if (aComponent.equals(engineUserField)) {
          return enginePasswordField;
        }
        if (aComponent.equals(enginePasswordField)) {
//          return engineVerifyPasswordField;
//        }
//        if (aComponent.equals(engineVerifyPasswordField)) {
          return testButton;
        }
        if (aComponent.equals(testButton)) {
          return getDoneButton();
        }
        if (aComponent.equals(getDoneButton())) {
          return getCancelButton();
        }
        if (aComponent.equals(getCancelButton())) {
          return engineURIField;
        }
        
        return getCancelButton();
      }

      public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
        if (aComponent.equals(testButton)) {
//          return engineVerifyPasswordField;
//        }
//        if (aComponent.equals(engineVerifyPasswordField)) {
          return enginePasswordField;
        }
        if (aComponent.equals(enginePasswordField)) {
          return engineUserField;
        }
        if (aComponent.equals(engineUserField)) {
          return engineURIField;
        }
        if (aComponent.equals(engineURIField)) {
          return getCancelButton();
        }
        if (aComponent.equals(getCancelButton())) {
          return getDoneButton();
        }
        if (aComponent.equals(getDoneButton())) {
          return testButton;
        }
        
        return getCancelButton();
      }
      
      public Component getLastComponent(Container focusCycleRoot) {
        return getCancelButton();
      }

    });
    
    return panel;
  }
  
  private JTextField getEngineURIField() {
    engineURIField = new JTextField(30);
    return engineURIField;
  }

  private JTextField getEngineUserField() {
    engineUserField = new JTextField(10);
    return engineUserField;
  }

  private JPasswordField getEnginePasswordField() {
    enginePasswordField = new JPasswordField(10);
    return enginePasswordField;
  }

//  private JTextField getEngineVerifyPasswordField() {
//    engineVerifyPasswordField = new JPasswordField(10);
//
//    new ActionAndFocusListener(engineVerifyPasswordField) {
//      public void focusGained(FocusEvent event) {} // don't process on focus gain.
//
//      public void process(Object eventSource) {
//        if (!passwordsMatch(
//                enginePasswordField.getPassword(),
//                engineVerifyPasswordField.getPassword()
//             )) {
//
//        JOptionPane.showMessageDialog(
//            engineVerifyPasswordField,
//            "The password specified does not match it's verification",
//            "Passwords do not Match",
//            JOptionPane.ERROR_MESSAGE
//        );
//
//        enginePasswordField.setText("");
//        engineVerifyPasswordField.setText("");
//        enginePasswordField.requestFocus();
//      }
//    }};
//    return engineVerifyPasswordField;
//  }
  
  private JButton getTestConnectionButton() {
   testButton = new JButton("Test Connection"); 
   testButton.setMnemonic('T');
   
   if (!YAWLEngineProxy.engineLibrariesAvailable()) {
     testButton.setEnabled(false);
   }
   
   final EngineDetailDialog detailDialog = this;
   
   testButton.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent e) {
       boolean connectionResult = YAWLEngineProxy.getInstance().testConnection(
           engineURIField.getText(),
           engineUserField.getText(),
           new String(enginePasswordField.getPassword())
       );
       
       if (connectionResult == false) {
         testMessage.setText("Failed to connect to a running engine with the specified details.");
         testMessage.setForeground(Color.RED);
       } else {
         testMessage.setText("Successfully connected to a running YAWL engine.");
         testMessage.setForeground(Color.BLACK);
       }
       testMessage.setVisible(true);
       detailDialog.pack();
     }
   });
   
   return testButton;
  }
  
  public void setVisible(boolean visible) {
    if (visible){
      if (engineURIField.getText().equals("")) {
        engineURIField.setText(
            prefs.get("engineURI", 
            YAWLEngineProxy.DEFAULT_ENGINE_URI)
        );
      }
      if (engineUserField.getText().equals("")) {
        engineUserField.setText(
            prefs.get("engineUserID", 
            YAWLEngineProxy.DEFAULT_ENGINE_ADMIN_USER)
        );
      }
      if (enginePasswordField.getPassword().equals("")) {
        enginePasswordField.setText(
            prefs.get("engineUserPassword", 
            YAWLEngineProxy.DEFAULT_ENGINE_ADMIN_PASSWORD)
        );
//        engineVerifyPasswordField.setText(
//            prefs.get("engineUserPassword",
//            YAWLEngineProxy.DEFAULT_ENGINE_ADMIN_PASSWORD)
//        );
      }
    }
    super.setVisible(visible);
  }
  
  private boolean passwordsMatch(char[] password, char[] verifyPassword) {
    if (password.length != verifyPassword.length) {
      return false;
    }

    for(int i = 0; i < password.length; i++) {
      if (password[i] != verifyPassword[i]) {
        return false;
      }
    }

    return true;
  }
}