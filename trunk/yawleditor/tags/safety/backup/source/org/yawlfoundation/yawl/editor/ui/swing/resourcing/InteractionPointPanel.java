package org.yawlfoundation.yawl.editor.ui.swing.resourcing;

import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class InteractionPointPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  
  private JRadioButton userButton;
  private JRadioButton systemButton;
  
  public InteractionPointPanel(String label) {
    super();
    buildInterface(label);
  }
  
  private void buildInterface(String interactionPointString) {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,0,20);
    gbc.anchor = GridBagConstraints.WEST;

    JLabel interactionPointLabel = new JLabel(interactionPointString);
    interactionPointLabel.setHorizontalAlignment(SwingConstants.LEADING);
    add(interactionPointLabel, gbc);

    gbc.gridx++;
    gbc.insets = new Insets(0,0,0,0);
    gbc.anchor = GridBagConstraints.WEST;

    userButton = getUserButton();
    add(userButton, gbc);

    gbc.gridx++;
    
    systemButton = getSystemButton();
    add(systemButton, gbc);

    ButtonGroup buttons = new ButtonGroup();
    buttons.add(userButton);
    buttons.add(systemButton);
    
    buttons.setSelected(userButton.getModel(), true);
  }
  
  public void setInteractionPointValue(int setting) {
    switch(setting) {
      case ResourceMapping.SYSTEM_INTERACTION_POINT: {
        systemButton.setSelected(true);
        break;
      }
      case ResourceMapping.USER_INTERACTION_POINT: {
        userButton.setSelected(true);
        break;
      }
    }
  }
  
  private JRadioButton getUserButton() {
    JRadioButton userButton = new JRadioButton("User");
    userButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doUserButtonAction();
      }
    });
    return userButton;
  }

  private JRadioButton getSystemButton() {
    JRadioButton systemButton = new JRadioButton("System");
    systemButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doSystemButtonAction();
      }
    });
    return systemButton;
  }
  
  protected abstract void doUserButtonAction();
  protected abstract void doSystemButtonAction();
}
