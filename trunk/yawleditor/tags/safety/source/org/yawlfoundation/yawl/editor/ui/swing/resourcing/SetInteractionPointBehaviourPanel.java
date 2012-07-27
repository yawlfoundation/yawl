package org.yawlfoundation.yawl.editor.ui.swing.resourcing;

import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SetInteractionPointBehaviourPanel extends ResourcingWizardPanel {
  
  private static final long serialVersionUID = 1L;

  private InteractionPointPanel offerPanel;
  private InteractionPointPanel allocationPanel;
  private InteractionPointPanel startPanel;
  
  public SetInteractionPointBehaviourPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardStepTitle() {
    return "Interaction Points";
  }
  
  protected void initialise() {}
  
  protected void buildInterface() {
    setBorder(new EmptyBorder(12,12,0,11));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0,0,20,0);

    String text = "<html><body>Each task passes through three <i>interaction</i>  " +
            "(or decision) points before a " +
            "participant begins working on it. For each of the interaction points " +
            "below, please specify whether the task is to be handled by the " +
            "<i>System</i> (dynamically, based on the settings chosen later in this " +
            "wizard) or by the <i>User</i> (manually, by a participant or an " +
            "administrator) when the task is executed.</body></html>" ;

    JLabel discussion = new JLabel(text);
    add(discussion,gbc);
    
    gbc.gridy++;
    gbc.insets = new Insets(0,0,10,0);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;

    offerPanel = getOfferPanel();
    add(offerPanel, gbc);
    
    gbc.gridy++;

    allocationPanel = getAllocationPanel();
    add(allocationPanel, gbc);

    gbc.gridy++;
    
    startPanel = getStartPanel();
    add(startPanel, gbc);
  }
  
  private InteractionPointPanel getOfferPanel() {
    return new InteractionPointPanel(
        "Offer - The task is made available to a number of participants:"
    ) {
      private static final long serialVersionUID = 1L;

      protected void doSystemButtonAction() {
        getResourceMapping().setOfferInteractionPoint(
          ResourceMapping.SYSTEM_INTERACTION_POINT    
        );
      }

      protected void doUserButtonAction() {
        getResourceMapping().setOfferInteractionPoint(
            ResourceMapping.USER_INTERACTION_POINT
          );
      }
    };
  }

  private InteractionPointPanel getAllocationPanel() {
    return new InteractionPointPanel(
        "Allocate - The task is assigned to a single participant:"
    ) {
      private static final long serialVersionUID = 1L;

      protected void doSystemButtonAction() {
        getResourceMapping().setAllocateInteractionPoint(
          ResourceMapping.SYSTEM_INTERACTION_POINT
        );
      }

      protected void doUserButtonAction() {
        getResourceMapping().setAllocateInteractionPoint(
            ResourceMapping.USER_INTERACTION_POINT
        );
      }
    };
  }

  private InteractionPointPanel getStartPanel() {
    return new InteractionPointPanel(
        "Start - Work begins on the task:"
    ) {
      private static final long serialVersionUID = 1L;

      protected void doSystemButtonAction() {
        getResourceMapping().setStartInteractionPoint(
          ResourceMapping.SYSTEM_INTERACTION_POINT   
        );
      }

      protected void doUserButtonAction() {
        getResourceMapping().setStartInteractionPoint(
            ResourceMapping.USER_INTERACTION_POINT
        );
      }
    };
  }
  
  public void doBack() {}

  public boolean doNext() { return true; }
  
  public void refresh() {
    offerPanel.setInteractionPointValue(
        getResourceMapping().getOfferInteractionPoint()
    );
    allocationPanel.setInteractionPointValue(
        getResourceMapping().getAllocateInteractionPoint()
    );
    startPanel.setInteractionPointValue(
        getResourceMapping().getStartInteractionPoint()
    );
  }
}