package org.yawlfoundation.yawl.editor.ui.swing.resourcing;

import org.yawlfoundation.yawl.editor.ui.client.YConnector;
import org.yawlfoundation.yawl.editor.ui.resourcing.DataVariableContent;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourcingParticipant;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourcingRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

public class SpecifyBaseDistributionSetPanel extends ResourcingWizardPanel {

  private static final long serialVersionUID = 1L;

  private UserPanel userPanel;
  private RolesPanel rolesPanel;
  private TaskInputParameterPanel parameterPanel;
  
  public SpecifyBaseDistributionSetPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  protected void buildInterface() {
    setBorder(new EmptyBorder(5,5,11,5));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0,0,15,0);

    JLabel discussion = new JLabel(
        "<html><body>A task may be offered to one or more participants and/or roles. " +
                "Please choose below the participant(s) and/or role(s) the task is to " +
                "be offered to." +
                "<br><br>" +
                "One or more net parameters (if available) may also be chosen below. " +
                "These are parameters that, at runtime, may contain either a userid " +
                "(set the 'Refers To' value to 'Participant') or a " +
                "role name (set to 'Role') that is to be included in the set of resources " +
                "the task is offered to.</body></html>"
    );
    
    add(discussion,gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.333;
    gbc.weighty = 1;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0,5,0,5);
    
    add(buildUserPanel(), gbc);

    gbc.gridx++;
    
    add(buildRolesPanel(), gbc);
    
    gbc.gridx++;

    add(buildTaskInputParameterPanel(), gbc);
  }

  public String getWizardStepTitle() {
    return "System Offer";
  }
  
  private JPanel buildUserPanel() {
    userPanel = new UserPanel(this);
    
    return userPanel;
  }
  
  private JPanel buildRolesPanel() {
    rolesPanel = new RolesPanel(this);
    return rolesPanel;
  }

  private JPanel buildTaskInputParameterPanel() {
    parameterPanel = new TaskInputParameterPanel(this);
    return parameterPanel;
  }
  

  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  public void doBack() {
    parameterPanel.stopEditing();
  }

  public boolean doNext() {
    parameterPanel.stopEditing();
    return true;
  }

  public void refresh() {
      List<ResourcingParticipant> liveList = YConnector.getResourcingParticipants();
      super.setUserList(liveList);
      userPanel.setUserList(liveList);
      userPanel.setSelectedUsers(getResourceMapping().getBaseUserDistributionList());

      List<ResourcingRole> liveRoles = YConnector.getResourcingRoles();
      super.setRoleList(liveRoles);
      rolesPanel.setRoles(liveRoles);
      rolesPanel.setSelectedRoles(getResourceMapping().getBaseRoleDistributionList());
    
      getResourceMapping().syncWithDataPerspective();
      parameterPanel.setVariableContentList(getResourceMapping().getBaseVariableContentList());
  }
  
  public ManageResourcingDialog getResourcingDialog() {
    return (ManageResourcingDialog) getDialog();
  }
  
  public boolean shouldDoThisStep() {
    return getResourceMapping().getOfferInteractionPoint() == 
      ResourceMapping.SYSTEM_INTERACTION_POINT;
  }
}

class UserPanel extends JPanel implements ListSelectionListener {
  
  private static final long serialVersionUID = 1L;

  private UserList userList;
  
  private SpecifyBaseDistributionSetPanel parent;
  
  public UserPanel(SpecifyBaseDistributionSetPanel parent) {
    super();
    this.parent = parent;
    buildInterface();
  }
  
  private void buildInterface() {
    setBorder(new TitledBorder("Participants"));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1;
      gbc.weighty = 0.95;
    gbc.insets = new Insets(0,5,0,5);
    gbc.fill = GridBagConstraints.BOTH;
    
    add(buildUserList(), gbc);
    addUnselectButton(gbc);
  }

    private void addUnselectButton(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.05;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0,0,0,0);
        JButton unselectButton = new JButton("Unselect All");
        unselectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userList.clearSelection();
                userList.setSelectedIndices(new int[0]);
            }
        });

        add(unselectButton, gbc);
    }

  private JScrollPane buildUserList() {
    userList = new UserList();
    userList.getSelectionModel().addListSelectionListener(this);

    return new JScrollPane(userList);
  }
  
  public void setUserList(List<ResourcingParticipant> users) {
    userList.setUsers(users);
  }
  
  public void setSelectedUsers(List<ResourcingParticipant> selectedUsers) {
    userList.setSelectedUsers(selectedUsers);
  }
  
  protected ResourceMapping getResourceMapping() {
    return parent.getResourceMapping();
  }

  public void valueChanged(ListSelectionEvent e) {
    if (userList.isEnabled()) {
      getResourceMapping().setBaseUserDistributionList(
          userList.getSelectedUsers()
      );
    }
  }
}

class UserList extends JList {
  private static final long serialVersionUID = 1L;

  private List<ResourcingParticipant> users;

  public UserList() {
    super();
  }
  
  public void setUsers(List<ResourcingParticipant> users) {
   setEnabled(false);
    
    this.users = users;
    String[] userNames = new String[users.size()];
    for(int i = 0; i < users.size(); i++) {
      userNames[i] = users.get(i).getName();
    }
    setListData(userNames);
    setEnabled(true);
  }
  
  public void setSelectedUsers(List<ResourcingParticipant> selectedUsers) {
    setEnabled(false);
    clearSelection();

    if (selectedUsers == null) {
      setEnabled(true);
      return;
    }
    
    int[] selectedUserIndicies = new int[selectedUsers.size()];
    boolean selectionValid = false ;
    int j = 0;
    for(int i = 0; i < users.size(); i ++) {
      for(ResourcingParticipant selectedUser : selectedUsers) {
        if (users.get(i).equals(selectedUser)) {
          selectedUserIndicies[j] = i;
          j++;
          selectionValid = true;
        }
      }
    }

    if (selectionValid) setSelectedIndices(selectedUserIndicies);

    setEnabled(true);
  }
  
  public List<ResourcingParticipant> getSelectedUsers() {
    int[] selectedRoleIndices = getSelectedIndices();
    List<ResourcingParticipant> selectedRoles = new LinkedList<ResourcingParticipant>();
    for(int i = 0; i < selectedRoleIndices.length; i++) {
      selectedRoles.add(
        users.get(selectedRoleIndices[i])    
      );
    }
    return selectedRoles;
  }
}


class RolesPanel extends JPanel implements ListSelectionListener {

  private static final long serialVersionUID = 1L;
  
  private RoleList roleList;

  private SpecifyBaseDistributionSetPanel parent;
  
  public RolesPanel(SpecifyBaseDistributionSetPanel parent) {
    super();
    this.parent = parent;
    buildInterface();
  }
  
  private void buildInterface() {
    setBorder(new TitledBorder("Roles"));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.weighty = 0.95;
    gbc.insets = new Insets(0,5,0,5);
    gbc.fill = GridBagConstraints.BOTH;

    add(buildRoleList(), gbc);
    addUnselectButton(gbc);
  }

  private void addUnselectButton(GridBagConstraints gbc) {
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.weighty = 0.05;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.fill = GridBagConstraints.NONE;
      gbc.insets = new Insets(0,0,0,0);
      JButton unselectButton = new JButton("Unselect All");
      unselectButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              roleList.clearSelection();
              roleList.setSelectedIndices(new int[0]);
          }
      });

      add(unselectButton, gbc);      
  }
  
  private JScrollPane buildRoleList() {
    roleList = new RoleList();
    roleList.getSelectionModel().addListSelectionListener(this);

    return new JScrollPane(roleList);
  }
  
  public void setRoles(List<ResourcingRole> roles) {
    roleList.setRoles(roles);
  }
  
  public void setSelectedRoles(List<ResourcingRole> selectedRoles) {
    roleList.setSelectedRoles(selectedRoles);
  }

  protected ResourceMapping getResourceMapping() {
    return parent.getResourceMapping();
  }
  
  public void valueChanged(ListSelectionEvent e) {
    if (roleList.isEnabled())  {
      getResourceMapping().setBaseRoleDistributionList(
          roleList.getSelectedRoles()
      );
    }
  }
}

class RoleList extends JList {
  private static final long serialVersionUID = 1L;

  private List<ResourcingRole> roles;

  public RoleList() {
    super();
  }
  
  public void setRoles(List<ResourcingRole> roles) {
    setEnabled(false);
    this.roles = roles;
    String[] roleNames = new String[roles.size()];
    for(int i = 0; i < roles.size(); i++) {
      roleNames[i] = roles.get(i).getName();
    }
    setListData(roleNames);
    setEnabled(true);
  }
  
  public void setSelectedRoles(List<ResourcingRole> selectedRoles) {
    setEnabled(false);
    clearSelection();
    
    if (selectedRoles == null) {
      setEnabled(true);
      return;
    }
    
    int[] selectedRoleIndicies = new int[selectedRoles.size()];
    boolean selectionValid = false ;
    int j = 0;
    for(int i = 0; i < roles.size(); i ++) {
      for(ResourcingRole selectedRole : selectedRoles) {
        if (roles.get(i).equals(selectedRole)) {
          selectedRoleIndicies[j] = i;
          j++;
          selectionValid = true ;
        }
      }
    }
    if (selectionValid) setSelectedIndices(selectedRoleIndicies);
    setEnabled(true);
  }
  
  public List<ResourcingRole> getSelectedRoles() {
    int[] selectedRoleIndices = getSelectedIndices();
    List<ResourcingRole> selectedRoles = new LinkedList<ResourcingRole>();
    for(int i = 0; i < selectedRoleIndices.length; i++) {
      selectedRoles.add(
        roles.get(selectedRoleIndices[i])    
      );
    }
    return selectedRoles;
  }
}

class TaskInputParameterPanel extends JPanel {
  
  private static final long serialVersionUID = 1L;
  
  private SpecifyBaseDistributionSetPanel parent;
  
  private ResourcingInputParamTable resourcingTable;
  
  public TaskInputParameterPanel(SpecifyBaseDistributionSetPanel parent) {
    super();
    this.parent = parent;
    buildInterface();
  }
  
  protected ResourceMapping getResourceMapping() {
    return parent.getResourceMapping();
  }
  
  private void buildInterface() {
    setBorder(new TitledBorder("Net Parameters"));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weighty = 1;
    gbc.weightx = 1;
    gbc.insets = new Insets(0,5,5,5);
    gbc.fill = GridBagConstraints.BOTH;
    
    add(buildResourcingTable(), gbc);
  }
  
  /**
   * Flushes any unfinished table edits of the resourcing table 
   * back into the underling model.  This must be explicitly
   * called whenever GUI Events outside the control of the 
   * table embedded in the panel would cause the panel
   * to be finished with.
   */
  
  public void stopEditing() {
    if (resourcingTable.getCellEditor() != null) {
      resourcingTable.getCellEditor().stopCellEditing();    
    }
  }

  private JScrollPane buildResourcingTable() {
    resourcingTable = new ResourcingInputParamTable();
    return new JScrollPane(resourcingTable);
  }
  
  public void setVariableContentList(List<DataVariableContent> variableContentList) {
    resourcingTable.setVariableContentList(variableContentList);
  }
}