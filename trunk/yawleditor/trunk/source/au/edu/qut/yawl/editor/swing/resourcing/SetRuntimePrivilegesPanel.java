package au.edu.qut.yawl.editor.swing.resourcing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.resourcing.NewYawlResourceMapping;

public class SetRuntimePrivilegesPanel extends ResourcingWizardPanel {

  private static final long serialVersionUID = 1L;

  private RuntimePrivilegePanel suspendWorkItemPanel;
  private RuntimePrivilegePanel reallocateStatelessWorkItemPanel;
  private RuntimePrivilegePanel reallocateStatefulWorkItemPanel;
  private RuntimePrivilegePanel deallocateWorkItemPanel;
  private RuntimePrivilegePanel delegateWorkItemPanel;
  private RuntimePrivilegePanel skipWorkItemPanel;
  private RuntimePrivilegePanel pileWorkItemPanel;
  
  public SetRuntimePrivilegesPanel(ManageNewYAWLResourcingDialog dialog) {
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

    gbc.gridy++;
    gbc.insets = new Insets(0,0,0,0);
    
    pileWorkItemPanel = getPilePanel();
    add(pileWorkItemPanel, gbc);
  }
  
  private RuntimePrivilegePanel getSuspendWorkItemPanel() {
    return new RuntimePrivilegePanel(
        "Can a user suspend a started work item of this task?",
        NewYawlResourceMapping.RuntimeUserPrivilege.CAN_SUSPEND,
        this
    );
  }

  private RuntimePrivilegePanel getReallocateStatelessPanel() {
    return new RuntimePrivilegePanel(
        "Can a user reallocate a work item of this task to another user, resetting state?",
        NewYawlResourceMapping.RuntimeUserPrivilege.CAN_REALLOCATE_STATELESS,
        this
    );
  }

  private RuntimePrivilegePanel getReallocateStatefulPanel() {
    return new RuntimePrivilegePanel(
        "Can a user reallocate a work item of this task to another user, retaining state?",    
        NewYawlResourceMapping.RuntimeUserPrivilege.CAN_REALLOCATE_STATEFUL,
        this
    );
  }

  private RuntimePrivilegePanel getDeallocatePanel() {
    return new RuntimePrivilegePanel(
        "Can a user deallocate themselves from a work item of this task?",
        NewYawlResourceMapping.RuntimeUserPrivilege.CAN_DEALLOCATE,
        this
    );
  }

  private RuntimePrivilegePanel getDelegatePanel() {
    return new RuntimePrivilegePanel(
        "Can a user delegate a work item of this task to some other user?",
        NewYawlResourceMapping.RuntimeUserPrivilege.CAN_DELEGATE,
        this
    );
  }

  private RuntimePrivilegePanel getSkipPanel() {
    return new RuntimePrivilegePanel(
        "Can a user skip a work item of this task?",
        NewYawlResourceMapping.RuntimeUserPrivilege.CAN_SKIP,
        this
    );
  }

  private RuntimePrivilegePanel getPilePanel() {
    return new RuntimePrivilegePanel(
        "Can a user pile work items of this task together?",
        NewYawlResourceMapping.RuntimeUserPrivilege.CAN_PILE,
        this
    );
  }
  
  public String getWizardStepTitle() {
    return "Establish Default User Runtime Privileges";
  }
  
  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  public void doBack() {}

  public void doNext() {}     

  public void refresh() {
    suspendWorkItemPanel.refresh();
    reallocateStatelessWorkItemPanel.refresh();
    reallocateStatefulWorkItemPanel.refresh();
    deallocateWorkItemPanel.refresh();
    delegateWorkItemPanel.refresh();
    skipWorkItemPanel.refresh();
    pileWorkItemPanel.refresh();
  }
}