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

import au.edu.qut.yawl.editor.actions.YAWLBaseAction;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.swing.ActionAndFocusListener;
import au.edu.qut.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;

public class SetOrganisationDatabaseAction extends YAWLBaseAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final OrganisationDatabaseDialog dialog = new OrganisationDatabaseDialog();

  private boolean invokedAtLeastOnce = false;
  
  {
    putValue(Action.SHORT_DESCRIPTION, " Specify login detail for a running Organisation Database. ");
    putValue(Action.NAME, "Set Organisation Database Detail");
    putValue(Action.LONG_DESCRIPTION, "Specify login detail for a running Organisation Database.");
//    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_O));
  }
  
  public SetOrganisationDatabaseAction() {}
  
  public void actionPerformed(ActionEvent event) {
    if (!invokedAtLeastOnce) {
      invokedAtLeastOnce = true;
      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
    }
    dialog.setVisible(true);
  }
}

class OrganisationDatabaseDialog extends AbstractDoneDialog {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  private JTextField databaseURIField;
  
  public OrganisationDatabaseDialog() {
    super("Specify Organisation Database Detail", true);
    setContentPanel(getDatabaseDetailPanel());

    getDoneButton().addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e) {
         prefs.put(
             "resourcingServiceURI", 
             databaseURIField.getText()
         );
       }
    });
  }

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }
  
  private JPanel getDatabaseDetailPanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel uriLabel = new JLabel("Organisation Database Server URI :");
    uriLabel.setDisplayedMnemonic('U');
    panel.add(uriLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getDatabaseURIField(), gbc);
    uriLabel.setLabelFor(databaseURIField);
    
    return panel;
  }
  
  private JTextField getDatabaseURIField() {
    databaseURIField = new JTextField(20);
    return databaseURIField;
  }

  
  public void setVisible(boolean visible) {
    if (visible){
      if (databaseURIField.getText().equals("")) {
        databaseURIField.setText(
            prefs.get("resourcingServiceURI", 
            ResourcingServiceProxy.DEFAULT_RESOURCING_SERVICE_URI)
        );
      }
    }
    super.setVisible(visible);
  }
}