package au.edu.qut.yawl.editor.swing.resourcing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import au.edu.qut.yawl.editor.resourcing.ResourceMapping;

public class RuntimePrivilegePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private JRadioButton noButton;
  private JRadioButton yesButton;
  
  private ResourceMapping.RuntimeUserPrivilege privilege;
  
  private SetRuntimePrivilegesPanel privilegesPanel;
  
  public RuntimePrivilegePanel(String label, 
                               ResourceMapping.RuntimeUserPrivilege privilege,
                               SetRuntimePrivilegesPanel privilegesPanel) {
    super();
    this.privilege = privilege;
    this.privilegesPanel = privilegesPanel;
    buildInterface(label);
  }
  
  private void buildInterface(String privilegeLabelString) {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,0,20);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel privilegeLabel = new JLabel(privilegeLabelString);
    add(privilegeLabel, gbc);

    gbc.gridx++;
    gbc.insets = new Insets(0,0,0,0);
    gbc.anchor = GridBagConstraints.WEST;

    noButton = getNoButton();
    add(noButton, gbc);

    gbc.gridx++;
    
    yesButton = getYesButton();
    add(yesButton, gbc);

    ButtonGroup buttons = new ButtonGroup();
    buttons.add(noButton);
    buttons.add(yesButton);
    
    buttons.setSelected(noButton.getModel(), true);
  }
  
  private JRadioButton getNoButton() {
    JRadioButton noButton = new JRadioButton("No");
    noButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            privilegesPanel.getResourceMapping().enablePrivilege(privilege, false);
          }
        }
    );
    return noButton;
  }

  private JRadioButton getYesButton() {
    JRadioButton yesButton = new JRadioButton("Yes");
    yesButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            privilegesPanel.getResourceMapping().enablePrivilege(privilege, true);
          }
        }
    );
    return yesButton;
  }
  
  public void grantPrivilege(boolean privilegeGranted) {
    if (privilegeGranted) {
      yesButton.setSelected(true);
    } else {
      noButton.setSelected(true);
    }
  }
  
  public boolean privilegeGranted() {
    return yesButton.isSelected();
  }
  
  public void refresh() {
    if (privilegesPanel.getResourceMapping().isPrivilegeEnabled(privilege)) {
      yesButton.setSelected(true);
    } else {
      noButton.setSelected(true);
    }
  }
}