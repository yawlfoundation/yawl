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
import org.yawlfoundation.yawl.editor.api.connection.YEngineConnection;
import org.yawlfoundation.yawl.editor.client.YConnector;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.swing.menu.MenuUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
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
    putValue(Action.NAME, "Engine Connection...");
    putValue(Action.LONG_DESCRIPTION, "Specify login details for a running YAWL Engine.");
    putValue(Action.SMALL_ICON, getPNGIcon("disconnect"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_E));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("shift E"));
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

  private JButton testButton;
  private JLabel testMessage = new JLabel();
  
  public EngineDetailDialog() {
    super("YAWL Engine Connection Settings", true);
    setContentPanel(getEngineDetailPanel());

    final EngineDetailDialog dlg = this;
    getDoneButton().addActionListener(new ActionListener(){

       public void actionPerformed(ActionEvent e) {
           if (! hasValidURIPath(engineURIField.getText())) {
             JOptionPane.showMessageDialog(
               dlg,
               "The URI supplied must be absolute\nand have the path '/yawl/ia'.",
               "Invalid URI",
               JOptionPane.ERROR_MESSAGE
             );
             dlg.closeCancelled = true;
             return;
           }

         String dataSchema = SpecificationModel.getInstance().getSchemaValidator().getDataTypeSchema();
           YConnector.disconnectEngine();

         String password = Arrays.toString(enginePasswordField.getPassword());

         prefs.put(
             "engineURI", 
             engineURIField.getText()
         );

         prefs.put(
             "engineUserID", 
             engineUserField.getText()
         );

         prefs.put(
             "engineUserPassword", password
         );

           YConnector.setEngineUserID(engineURIField.getText());
           YConnector.setEnginePassword(password);
           YConnector.setEngineURL(engineURIField.getText());
           if (dataSchema != null) {
               SpecificationModel.getInstance().getSchemaValidator().setDataTypeSchema(dataSchema);
           }
           YAWLEditor.setStatusMode("engine", YConnector.isEngineConnected());

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


  private JButton getTestConnectionButton() {
   testButton = new JButton("Test Connection"); 
   testButton.setMnemonic('T');

   final EngineDetailDialog detailDialog = this;
   
   testButton.addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e) {
           String uriStr = engineURIField.getText();
           if (hasValidURIPath(uriStr)) {
               if (YConnector.testEngineParameters(
                       uriStr,
                       engineUserField.getText(),
                       new String(enginePasswordField.getPassword())
               )) {
                   testMessage.setText("Successfully connected to a running YAWL engine.");
                   testMessage.setForeground(Color.BLACK);
               }
               else {
                   testMessage.setText("Failed to connect to a running engine with the specified details.");
                   testMessage.setForeground(Color.RED);
               }
           }
           else {
               testMessage.setText("Invalid URI: it must be absolute and have the path '/yawl/ia'.");
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
      if (engineURIField.getText().equals("")) {
        engineURIField.setText(
            prefs.get("engineURI", YEngineConnection.DEFAULT_URL)
        );
      }
      if (engineUserField.getText().equals("")) {
        engineUserField.setText(
            prefs.get("engineUserID", YEngineConnection.DEFAULT_USERID)
        );
      }
      if (enginePasswordField.getPassword().length == 0) {
        enginePasswordField.setText(
            prefs.get("engineUserPassword", YEngineConnection.DEFAULT_PASSWORD)
        );
      }
    }
      testMessage.setVisible(false);    // reset      
    super.setVisible(visible);
  }


  private boolean hasValidURIPath(String uriStr) {
      try {
          URI uri = new URI(uriStr);
          return uri.getPath().equals("/yawl/ia");
      }
      catch (URISyntaxException use) {
          return false;
      }
  }
}