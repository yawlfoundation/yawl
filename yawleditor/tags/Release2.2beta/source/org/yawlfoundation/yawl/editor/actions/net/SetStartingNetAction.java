/*
 * Created on 9/10/2003
 * YAWLEditor v1.0 
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
 *
 */

package org.yawlfoundation.yawl.editor.actions.net;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.specification.SpecificationUtilities;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class SetStartingNetAction extends YAWLExistingNetAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final StartingNetDialog dialog = new StartingNetDialog();
  private boolean isFirstInvocation = false;

  {
    putValue(Action.SHORT_DESCRIPTION, " Specify the net workflow execution starts in. ");
    putValue(Action.NAME, "Set Starting Net...");
    putValue(Action.LONG_DESCRIPTION, "Specify the net workflow execution starts in.");
    putValue(Action.SMALL_ICON, getPNGIcon("table_key"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("T"));
  }

  public SetStartingNetAction() {
  }
 
  public void actionPerformed(ActionEvent event) {
    if (!isFirstInvocation) {
      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
      isFirstInvocation = true;       
    }
    dialog.setVisible(true);
  }
}

class StartingNetDialog extends AbstractDoneDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  protected JComboBox netComboBox;
  protected JComboBox dbGatewayComboBox;

  
  public StartingNetDialog() {
    super("Choose Starting Net", true);
    setContentPanel(getStartingNetPanel());
    getDoneButton().addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (netComboBox.isEnabled()) {
              SpecificationModel.getInstance().setStartingNet(
                  SpecificationUtilities.getNetModelFromName(
                      SpecificationModel.getInstance(),
                      (String) netComboBox.getSelectedItem()
                  )
              ); 
            }
              String gateway = null ;
              if (dbGatewayComboBox.isEnabled() && (dbGatewayComboBox.getSelectedIndex() > 0)) {
                  gateway = (String) dbGatewayComboBox.getSelectedItem(); 
              }
              SpecificationModel.getInstance().getStartingNet().setExternalDataGateway(gateway);
              
              SpecificationUndoManager.getInstance().setDirty(true);
            }
        }
    );
  }

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }

  public void setVisible(boolean state) {
    if (state) {
      populateNetComboBox();
      populateDbGatewayComboBox();
      pack();
    }
    super.setVisible(state);
  }
  
  private JPanel getStartingNetPanel() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,0,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel label = new JLabel("Execution of the workflow starts in net:");
    label.setDisplayedMnemonicIndex(26);
    panel.add(label, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    netComboBox = new JComboBox();
    label.setLabelFor(netComboBox);
    panel.add(netComboBox, gbc);

      gbc.gridx = 0;
      gbc.gridy++;
      gbc.anchor = GridBagConstraints.EAST;

      label = new JLabel("External data gateway for case data:");
      label.setDisplayedMnemonicIndex(9);
      panel.add(label, gbc);

      gbc.gridx++;
      gbc.anchor = GridBagConstraints.WEST;

      dbGatewayComboBox = new JComboBox();
      label.setLabelFor(dbGatewayComboBox);
      panel.add(dbGatewayComboBox, gbc);

    return panel;
  }
  
  private void populateNetComboBox() {
    netComboBox.setEnabled(false);
    netComboBox.removeAllItems();

    Set netSet = SpecificationModel.getInstance().getNets();
    
    Object[] netSetArray = netSet.toArray();
    String[] netSetNames = new String[netSet.size()];
    for(int i = 0; i < netSet.size(); i++) {
      netSetNames[i] = ((NetGraphModel) netSetArray[i]).getName();
    }

    try {
      Arrays.sort(netSetNames);
    } catch (Exception e) {};
    
    for(int i = 0; i < netSetNames.length; i++) {
      netComboBox.addItem(netSetNames[i]);
      if (SpecificationUtilities.getNetModelFromName(SpecificationModel.getInstance(), netSetNames[i]).isStartingNet()) {
        netComboBox.setSelectedItem(netSetNames[i]);
      }
    }
    netComboBox.setEnabled(true);
  }

    private void populateDbGatewayComboBox() {
        dbGatewayComboBox.setEnabled(false);
        dbGatewayComboBox.removeAllItems();
        dbGatewayComboBox.addItem("None");
        Map<String, String> map = YAWLEngineProxy.getInstance().getExternalDataGateways();
        if (map != null) {
            for (String name : map.keySet()) {
                dbGatewayComboBox.addItem(name);
            }
            dbGatewayComboBox.setEnabled(true);
        }
    }
}
