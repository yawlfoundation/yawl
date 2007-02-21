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

package au.edu.qut.yawl.editor.actions.net;

import java.awt.event.ActionEvent;
import au.edu.qut.yawl.editor.net.NetGraphModel;

import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;

import java.awt.event.ActionListener;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.util.Set;
import java.util.Arrays;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.YAWLEditor;

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
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
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
  
  public StartingNetDialog() {
    super("Choose Starting Net", true);
    setContentPanel(getStartingNetPanel());
    getDoneButton().addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (netComboBox.isEnabled()) {
              SpecificationModel.getInstance().setStartingNet(
                  SpecificationModel.getInstance().getNetModelFromName(
                      (String) netComboBox.getSelectedItem())
              );
            }
          }
        }
    );
  }

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }

  public void setVisible(boolean state) {
    if (state == true) {
      populateComboBox();
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

    return panel;
  }
  
  private void populateComboBox() {
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
      if (SpecificationModel.getInstance().getNetModelFromName(netSetNames[i]).isStartingNet()) {
        netComboBox.setSelectedItem(netSetNames[i]);
      }
    }
    netComboBox.setEnabled(true);
    pack();
  }
}
