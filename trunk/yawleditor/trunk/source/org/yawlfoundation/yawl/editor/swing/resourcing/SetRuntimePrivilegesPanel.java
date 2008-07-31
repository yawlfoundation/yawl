package org.yawlfoundation.yawl.editor.swing.resourcing;

import org.yawlfoundation.yawl.editor.resourcing.ResourceMapping;

import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SetRuntimePrivilegesPanel extends ResourcingWizardPanel {

  private static final long serialVersionUID = 1L;

  private RuntimePrivilegePanel suspendWorkItemPanel;
  private RuntimePrivilegePanel reallocateStatelessWorkItemPanel;
  private RuntimePrivilegePanel reallocateStatefulWorkItemPanel;
  private RuntimePrivilegePanel deallocateWorkItemPanel;
  private RuntimePrivilegePanel delegateWorkItemPanel;
  private RuntimePrivilegePanel skipWorkItemPanel;
  
  public SetRuntimePrivilegesPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  protected void buildInterface() {
    setBorder(new EmptyBorder(12,12,0,11));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,10,0);
    gbc.anchor = GridBagConstraints.EAST;


    suspendWorkItemPanel = getSuspendWorkItemPanel();
    add(suspendWorkItemPanel, gbc);
    
    gbc.gridy++;

    reallocateStatelessWorkItemPanel = getReallocateStatelessPanel();
    add(reallocateStatelessWorkItemPanel, gbc);

    gbc.gridy++;
    
    reallocateStatefulWorkItemPanel = getReallocateStatefulPanel();
    add(reallocateStatefulWorkItemPanel, gbc);

    gbc.gridy++;
    
    deallocateWorkItemPanel = getDeallocatePanel();
    add(deallocateWorkItemPanel, gbc);

    gbc.gridy++;
    
    delegateWorkItemPanel = getDelegatePanel();
    add(delegateWorkItemPanel, gbc);

    gbc.gridy++;
    
    skipWorkItemPanel = getSkipPanel();
    add(skipWorkItemPanel, gbc);
  }
  
  private RuntimePrivilegePanel getSuspendWorkItemPanel() {
    return new RuntimePrivilegePanel(
        "Can a participant suspend a started work item of this task?",
        ResourceMapping.CAN_SUSPEND_PRIVILEGE,
        this
    );
  }

  private RuntimePrivilegePanel getReallocateStatelessPanel() {
    return new RuntimePrivilegePanel(
        "Can a participant reallocate a work item of this task to another participant, resetting state?",
        ResourceMapping.CAN_REALLOCATE_STATELESS_PRIVILEGE,
        this
    );
  }

  private RuntimePrivilegePanel getReallocateStatefulPanel() {
    return new RuntimePrivilegePanel(
        "Can a participant reallocate a work item of this task to another participant, retaining state?",
        ResourceMapping.CAN_REALLOCATE_STATEFUL_PRIVILEGE,
        this
    );
  }

  private RuntimePrivilegePanel getDeallocatePanel() {
    return new RuntimePrivilegePanel(
        "Can a participant deallocate themselves from a work item of this task?",
        ResourceMapping.CAN_DEALLOCATE_PRIVILEGE,
        this
    );
  }

  private RuntimePrivilegePanel getDelegatePanel() {
    return new RuntimePrivilegePanel(
        "Can a participant delegate a work item of this task to another participant?",
        ResourceMapping.CAN_DELEGATE_PRIVILEGE,
        this
    );
  }

  private RuntimePrivilegePanel getSkipPanel() {
    return new RuntimePrivilegePanel(
        "Can a participant skip a work item of this task?",
        ResourceMapping.CAN_SKIP_PRIVILEGE,
        this
    );
  }

  public String getWizardStepTitle() {
    return "Establish Default User Runtime Privileges for this Task";
  }
  
  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  public void doBack() {}

  public boolean doNext() { return true; }     

  public void refresh() {
    suspendWorkItemPanel.refresh();
    reallocateStatelessWorkItemPanel.refresh();
    reallocateStatefulWorkItemPanel.refresh();
    deallocateWorkItemPanel.refresh();
    delegateWorkItemPanel.refresh();
    skipWorkItemPanel.refresh();
  }
}