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

package au.edu.qut.yawl.editor.actions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;

import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.swing.ActionAndFocusListener;
import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;

public class SetEngineDetailAction extends YAWLBaseAction {
  private static final EngineDetailDialog dialog = new EngineDetailDialog();

  private boolean invokedAtLeastOnce = false;
  
  {
    putValue(Action.SHORT_DESCRIPTION, " Specify login detail for a running YAWL Engine. ");
    putValue(Action.NAME, "Set Engine Detail");
    putValue(Action.LONG_DESCRIPTION, "Specify login detail for a running YAWL Engine.");
//    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
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
  
  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  private JTextField engineURIField;
  private JTextField engineUserField;
  private JPasswordField enginePasswordField;
  private JPasswordField engineVerifyPasswordField;
  
  public EngineDetailDialog() {
    super("Specify Engine Detail", true);
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

    JLabel uriLabel = new JLabel("YAWL Engine URI :");
    uriLabel.setDisplayedMnemonic('U');
    panel.add(uriLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getEngineURIField(), gbc);
    uriLabel.setLabelFor(engineURIField);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.insets = new Insets(5,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel userLabel = new JLabel("User Name :");
    userLabel.setDisplayedMnemonic('N');
    panel.add(userLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getEngineUserField(), gbc);
    userLabel.setLabelFor(engineUserField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.EAST;

    JLabel passwordLabel = new JLabel("Password :");
    passwordLabel.setDisplayedMnemonic('P');
    panel.add(passwordLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getEnginePasswordField(), gbc);
    passwordLabel.setLabelFor(enginePasswordField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.EAST;

    JLabel verifyPasswordLabel = new JLabel("Verify Password :");
    verifyPasswordLabel.setDisplayedMnemonic('V');
    panel.add(verifyPasswordLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getEngineVerifyPasswordField(), gbc);
    verifyPasswordLabel.setLabelFor(engineVerifyPasswordField);
    
    return panel;
  }
  
  private JTextField getEngineURIField() {
    engineURIField = new JTextField(20);
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

  private JTextField getEngineVerifyPasswordField() {
    engineVerifyPasswordField = new JPasswordField(10);
    
    new ActionAndFocusListener(engineVerifyPasswordField) {
      public void focusGained(FocusEvent event) {} // don't process on focus gain.
      
      public void process(Object eventSource) {
        if (!passwordsMatch(
                enginePasswordField.getPassword(),
                engineVerifyPasswordField.getPassword()
             )) {

        JOptionPane.showMessageDialog(
            engineVerifyPasswordField, 
            "The password specified does not match it's verification", 
            "Passwords do not Match", 
            JOptionPane.ERROR_MESSAGE
        );
        
        enginePasswordField.setText("");
        engineVerifyPasswordField.setText("");
        enginePasswordField.requestFocus();
      }
    }};
    return engineVerifyPasswordField;
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
        engineVerifyPasswordField.setText(
            prefs.get("engineUserPassword", 
            YAWLEngineProxy.DEFAULT_ENGINE_ADMIN_PASSWORD)
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