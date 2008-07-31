package org.yawlfoundation.yawl.editor.swing.resourcing;

import org.yawlfoundation.yawl.editor.resourcing.ResourceMapping;

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
    return "Choose Behaviour At Interaction Points";
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

    JLabel discussion = new JLabel(
        "<html><body>There are three key decision points for managing the resourcing of " +
        "work items spawned from a task. At each of these <em>interaction points</em>, " +
        "you may choose to have the system dynamic make a decision on resourcing at each point, " +
        "or alternately, allow a user to manually make each decision. Each interaction point is " +
        "briefly described below:<ul><li>Offer: The point at which it is decided that a number of participants " +
        "<em>could</em> undertake the work item.<li>Allocation: The point at which one of the participants " +
        "offered the work item is <em>nominated to do</em> that work item." +
        "<li>Start: The point at which the participant allocated a work item <em>begins</em> working on it." +
        "</ul></body></html>"
    );
    
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
        "Offering a work item for this task to a number of participants is to be done by:"
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
        "Allocating a work item for this task to one of the offered participants is to be done by:"
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
        "Starting an allocated work item of this task is to be done by:"
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