/*
 * Created on 09/10/2003
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

package au.edu.qut.yawl.editor.actions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.thirdparty.wofyawl.WofYAWLProxy;


public class ConfigureAnalysisToolAction extends YAWLBaseAction {
  private static final ConfigureAnalysisDialog dialog = new ConfigureAnalysisDialog();

  private boolean invokedAtLeastOnce = false;
  
  {
    putValue(Action.SHORT_DESCRIPTION, " Configure Specification Analysis tool ");
    putValue(Action.NAME, "Configure Analysis Tool");
    putValue(Action.LONG_DESCRIPTION, "Configure Specification Analysis Tool.");
//    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
  }
  
  public ConfigureAnalysisToolAction() {}
  
  public void actionPerformed(ActionEvent event) {
    if (!invokedAtLeastOnce) {
      invokedAtLeastOnce = true;
      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
    }
    dialog.setVisible(true);
  }
}


class ConfigureAnalysisDialog extends AbstractDoneDialog {
  
  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  private JCheckBox relaxedSoundnessCheckBox;
  private JCheckBox transitionInvariantCheckBox;
  private JCheckBox extendedCoverabilityCheckBox;
  
  public ConfigureAnalysisDialog() {
    super("Configure Analysis Tool", true);
    setContentPanel(getConfigurationPanel());

    getDoneButton().addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e) {
         prefs.putBoolean(
             WofYAWLProxy.STRUCTURAL_ANALYSIS_PREFERENCE,
             relaxedSoundnessCheckBox.isSelected()
         );

         prefs.putBoolean(
             WofYAWLProxy.BEHAVIOURAL_ANALYSIS_PREFERENCE,
             transitionInvariantCheckBox.isSelected()
         );

         prefs.putBoolean(
             WofYAWLProxy.EXTENDED_COVERABILITY_PREFERENCE,
             extendedCoverabilityCheckBox.isSelected()
         );
       }
    });
  }

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }
  
  private JPanel getConfigurationPanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,5,5);
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getRelaxedSoundnessCheckBox(),gbc);
    gbc.gridy++;

    panel.add(getTransitionInvariantCheckBox(),gbc);

    gbc.anchor = GridBagConstraints.CENTER;
    gbc.gridy++;
    
    panel.add(
      new JLabel(
          "<html>These tests may overlap in identifying unreachable tasks in the<br>" + 
          "specification, potential deadlocks and possible unfinished work in<br>" +
          "completed workflow cases.</html>"
      ),
      gbc
    );

    gbc.gridy++;
    gbc.insets = new Insets(5,12,5,12);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    panel.add(new JSeparator(),gbc);  

    gbc.gridy++;
    gbc.insets = new Insets(0,0,5,5);
    gbc.anchor = GridBagConstraints.WEST;
    
    panel.add(getExtendedCoverabilityCheckBox(),gbc);
    
    return panel;
  }
  
  private JCheckBox getRelaxedSoundnessCheckBox() {
    relaxedSoundnessCheckBox = new JCheckBox(
        "Structural check for relaxed soundness in a bounded analysis net"
    );
    relaxedSoundnessCheckBox.setMnemonic(KeyEvent.VK_R);
    relaxedSoundnessCheckBox.setDisplayedMnemonicIndex(2);
    relaxedSoundnessCheckBox.setSelected(true);
    return relaxedSoundnessCheckBox;
  }
  
  private JCheckBox getTransitionInvariantCheckBox() {
    transitionInvariantCheckBox = new JCheckBox(
        "Behaviourial check for semi-positive transition invariants in a short-circuited analysis net"
    );
    transitionInvariantCheckBox.setMnemonic(KeyEvent.VK_S);
    transitionInvariantCheckBox.setSelected(true);
    return transitionInvariantCheckBox;
  }

  private JCheckBox getExtendedCoverabilityCheckBox() {
    extendedCoverabilityCheckBox = new JCheckBox(
        "Extend coverability graph of an unbounded analysis net (slow)"
    );
    extendedCoverabilityCheckBox.setMnemonic(KeyEvent.VK_E);
    extendedCoverabilityCheckBox.setSelected(true);
    return extendedCoverabilityCheckBox;
  }
  
  public void setVisible(boolean visible) {
    if (visible){
      relaxedSoundnessCheckBox.setSelected(
          prefs.getBoolean(WofYAWLProxy.STRUCTURAL_ANALYSIS_PREFERENCE, true)
      );
      
      transitionInvariantCheckBox.setSelected(
          prefs.getBoolean(WofYAWLProxy.BEHAVIOURAL_ANALYSIS_PREFERENCE, true)
      );

      extendedCoverabilityCheckBox.setSelected(
          prefs.getBoolean(WofYAWLProxy.EXTENDED_COVERABILITY_PREFERENCE, true)
      );
    }
    super.setVisible(visible);
  }
}
