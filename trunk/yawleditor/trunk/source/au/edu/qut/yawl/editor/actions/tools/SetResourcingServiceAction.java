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

package au.edu.qut.yawl.editor.actions.tools;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.FocusTraversalPolicy;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;
import javax.swing.JButton;

import au.edu.qut.yawl.editor.actions.YAWLBaseAction;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.swing.ActionAndFocusListener;
import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;
import au.edu.qut.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;

public class SetResourcingServiceAction extends YAWLBaseAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final ResourceServiceDialog dialog = new ResourceServiceDialog();

  private boolean invokedAtLeastOnce = false;
  
  {
    putValue(Action.SHORT_DESCRIPTION, " Specify login detail for a running resourcing service. ");
    putValue(Action.NAME, "Set Resourcing Service");
    putValue(Action.LONG_DESCRIPTION, "Specify login detail for a running resourcing service.");
//    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
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
  private JPasswordField resourcingServiceVerifyPasswordField;
  
  private JButton testButton;
  private JLabel testMessage = new JLabel();
  
  public ResourceServiceDialog() {
    super("Specify Resourcing Service", true);
    setContentPanel(getEngineDetailPanel());

    getDoneButton().addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e) {

         YAWLEngineProxy.getInstance().disconnect(); 
         
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

    JLabel uriLabel = new JLabel("Resourcing Service URI :");
    uriLabel.setDisplayedMnemonic('U');
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

    JLabel userLabel = new JLabel("User Name :");
    userLabel.setDisplayedMnemonic('N');
    panel.add(userLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getResourcingServiceUserField(), gbc);
    userLabel.setLabelFor(resourcingServiceUserField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.EAST;

    JLabel passwordLabel = new JLabel("Password :");
    passwordLabel.setDisplayedMnemonic('P');
    panel.add(passwordLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getResourcingServicePasswordField(), gbc);
    passwordLabel.setLabelFor(resourcingServicePasswordField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.EAST;

    JLabel verifyPasswordLabel = new JLabel("Verify Password :");
    verifyPasswordLabel.setDisplayedMnemonic('V');
    panel.add(verifyPasswordLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getResourcingServiceVerifyPasswordField(), gbc);
    verifyPasswordLabel.setLabelFor(resourcingServiceVerifyPasswordField);
    
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
          return resourcingServiceVerifyPasswordField;
        }
        if (aComponent.equals(resourcingServiceVerifyPasswordField)) {
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
          return resourcingServiceVerifyPasswordField;
        }
        if (aComponent.equals(resourcingServiceVerifyPasswordField)) {
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
    resourcingServiceURIField = new JTextField(20);
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

  private JTextField getResourcingServiceVerifyPasswordField() {
    resourcingServiceVerifyPasswordField = new JPasswordField(10);
    
    new ActionAndFocusListener(resourcingServiceVerifyPasswordField) {
      public void focusGained(FocusEvent event) {} // don't process on focus gain.
      
      public void process(Object eventSource) {
        if (!passwordsMatch(
                resourcingServicePasswordField.getPassword(),
                resourcingServiceVerifyPasswordField.getPassword()
             )) {

        JOptionPane.showMessageDialog(
            resourcingServiceVerifyPasswordField, 
            "The password specified does not match it's verification", 
            "Passwords do not Match", 
            JOptionPane.ERROR_MESSAGE
        );
        
        resourcingServicePasswordField.setText("");
        resourcingServiceVerifyPasswordField.setText("");
        resourcingServicePasswordField.requestFocus();
      }
    }};
    return resourcingServiceVerifyPasswordField;
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
       boolean connectionResult = ResourcingServiceProxy.getInstance().testConnection(
           resourcingServiceURIField.getText(),
           resourcingServiceUserField.getText(),
           new String(resourcingServicePasswordField.getPassword())
       );
       
       if (connectionResult == false) {
         testMessage.setText("Failed to connect to a running resourcing service with the above detail.");
       } else {
         testMessage.setText("Successfully connected to a running resourcing service with the above detail.");
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
      if (resourcingServicePasswordField.getPassword().equals("")) {
        resourcingServicePasswordField.setText(
            prefs.get(
                "resourcingServiceUserPassword", 
                ResourcingServiceProxy.DEFAULT_RESOURCING_SERVICE_USER_PASSWORD
            )
        );
        resourcingServiceVerifyPasswordField.setText(
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
}