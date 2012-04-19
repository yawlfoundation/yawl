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
import org.yawlfoundation.yawl.editor.client.YConnector;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;
import org.yawlfoundation.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;

public class SetResourcingServiceAction extends YAWLBaseAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final ResourceServiceDialog dialog = new ResourceServiceDialog();

  private boolean invokedAtLeastOnce = false;
  
  {
    putValue(Action.SHORT_DESCRIPTION, " Specify login details for a running resourcing service. ");
    putValue(Action.NAME, "Resource Service Connection...");
    putValue(Action.LONG_DESCRIPTION, "Specify login details for a running resourcing service.");
    putValue(Action.SMALL_ICON, getPNGIcon("user_go"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("shift R"));
  }
  
  public SetResourcingServiceAction() {}
  
  public void actionPerformed(ActionEvent event) {
    if (!invokedAtLeastOnce) {
      invokedAtLeastOnce = true;
      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
    }
    dialog.setVisible(true);
  }
}

class ResourceServiceDialog extends AbstractDoneDialog {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  private JTextField resourcingServiceURIField;
  private JTextField resourcingServiceUserField;
  private JPasswordField resourcingServicePasswordField;

  private JButton testButton;
  private JLabel testMessage = new JLabel();
  
  public ResourceServiceDialog() {
    super("Resource Service Connection Settings", true);
    setContentPanel(buildResourcingServiceDetailPanel());

    final ResourceServiceDialog dlg = this;
    getDoneButton().addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e) {
         if (! hasValidURIPath(resourcingServiceURIField.getText())) {
           JOptionPane.showMessageDialog(
             dlg,
             "The URI supplied must be absolute and\nhave the path '/resourceService/gateway'.",
             "Invalid URI",
             JOptionPane.ERROR_MESSAGE
           );
           dlg.closeCancelled = true;
           return;
         }

         YConnector.disconnectResource();
         
         prefs.put(
             "resourcingServiceURI", 
             resourcingServiceURIField.getText()
         );

         prefs.put(
             "resourcingServiceUserID", 
             resourcingServiceUserField.getText()
         );

         prefs.put(
             "resourcingServiceUserPassword", 
             new String(resourcingServicePasswordField.getPassword())
         );

         YConnector.setResourceUserID(resourcingServiceUserField.getText());
         YConnector.setResourcePassword(new String(resourcingServicePasswordField.getPassword()));
         YConnector.setResourceURL(resourcingServiceURIField.getText());
         YAWLEditor.setStatusMode("resource", YConnector.isResourceConnected());

         SpecificationUndoManager.getInstance().setDirty(true);                            
       }
    });
  }

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }
  
  private JPanel buildResourcingServiceDetailPanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel uriLabel = new JLabel("Resource Service URI:");
    uriLabel.setDisplayedMnemonic('R');
    panel.add(uriLabel, gbc);
    
    gbc.gridx++;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getResourcingServiceURIField(), gbc);
    uriLabel.setLabelFor(resourcingServiceURIField);
    
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

    panel.add(getResourcingServiceUserField(), gbc);
    userLabel.setLabelFor(resourcingServiceUserField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.EAST;

    JLabel passwordLabel = new JLabel("Password:");
    passwordLabel.setDisplayedMnemonic('P');
    panel.add(passwordLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getResourcingServicePasswordField(), gbc);
    passwordLabel.setLabelFor(resourcingServicePasswordField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

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
        return resourcingServiceURIField;
      }
      
      public Component getFirstComponent(Container focusCycleRoot) {
        return resourcingServiceURIField;
      }

      public Component getDefaultComponent(Container focusCycleRoot) {
        return getDoneButton();
      }
      
      public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
        if (aComponent.equals(resourcingServiceURIField)) {
          return resourcingServiceUserField;
        }
        if (aComponent.equals(resourcingServiceUserField)) {
          return resourcingServicePasswordField;
        }
        if (aComponent.equals(resourcingServicePasswordField)) {
          return testButton;
        }
        if (aComponent.equals(testButton)) {
          return getDoneButton();
        }
        if (aComponent.equals(getDoneButton())) {
          return getCancelButton();
        }
        if (aComponent.equals(getCancelButton())) {
          return resourcingServiceURIField;
        }
        
        return getCancelButton();
      }

      public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
        if (aComponent.equals(testButton)) {
          return resourcingServicePasswordField;
        }
        if (aComponent.equals(resourcingServicePasswordField)) {
          return resourcingServiceUserField;
        }
        if (aComponent.equals(resourcingServiceUserField)) {
          return resourcingServiceURIField;
        }
        if (aComponent.equals(resourcingServiceURIField)) {
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
  
  private JTextField getResourcingServiceURIField() {
    resourcingServiceURIField = new JTextField(30);
    return resourcingServiceURIField;
  }

  private JTextField getResourcingServiceUserField() {
    resourcingServiceUserField = new JTextField(10);
    return resourcingServiceUserField;
  }

  private JPasswordField getResourcingServicePasswordField() {
    resourcingServicePasswordField = new JPasswordField(10);
    return resourcingServicePasswordField;
  }


  private JButton getTestConnectionButton() {
   testButton = new JButton("Test Connection"); 
   testButton.setMnemonic('T');
   
   if (!YAWLEngineProxy.engineLibrariesAvailable()) {
     testButton.setEnabled(false);
   }
   
   final ResourceServiceDialog detailDialog = this;

      testButton.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
              String uriStr = resourcingServiceURIField.getText();
              if (hasValidURIPath(uriStr)) {
                  if (YConnector.testResourceServiceParameters(
                          uriStr,
                          resourcingServiceUserField.getText(),
                          new String(resourcingServicePasswordField.getPassword())
                  )) {
                      testMessage.setText("Successfully connected to a running resource service.");
                      testMessage.setForeground(Color.BLACK);
                  }
                  else {
                      testMessage.setText("Failed to connect to a running resource service " +
                              "with the specified details.");
                      testMessage.setForeground(Color.RED);
                  }
              }
              else {
                  testMessage.setText("Invalid URI: it must be absolute and have the path" +
                          " '/resourceService/gateway'.");
                  testMessage.setForeground(Color.RED);
              }
              testMessage.setVisible(true);
              detailDialog.pack();
          }
      });
   
   return testButton;
  }
  
  public void setVisible(boolean visible) {
    if (visible){
      if (resourcingServiceURIField.getText().equals("")) {
        resourcingServiceURIField.setText(
            prefs.get(
                "resourcingServiceURI", 
                ResourcingServiceProxy.DEFAULT_RESOURCING_SERVICE_URI
            )
        );
      }
      if (resourcingServiceUserField.getText().equals("")) {
        resourcingServiceUserField.setText(
            prefs.get(
                "resourcingServiceUserID", 
                ResourcingServiceProxy.DEFAULT_RESOURCING_SERVICE_USERID
            )
        );
      }
      if (resourcingServicePasswordField.getPassword().length == 0) {
        resourcingServicePasswordField.setText(
            prefs.get(
                "resourcingServiceUserPassword", 
                ResourcingServiceProxy.DEFAULT_RESOURCING_SERVICE_USER_PASSWORD
            )
        );
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


  private boolean hasValidURIPath(String uriStr) {
      try {
          URI uri = new URI(uriStr);
          return uri.getPath().equals("/resourceService/gateway");
      }
      catch (URISyntaxException use) {
          return false;
      }
  }
}