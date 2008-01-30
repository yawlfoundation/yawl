package org.yawlfoundation.yawl.editor.swing.resourcing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.DataVariableUtilities;
import org.yawlfoundation.yawl.editor.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.resourcing.DataVariableContent;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingParticipant;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingRole;
import org.yawlfoundation.yawl.editor.swing.JUtilities;
import org.yawlfoundation.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;

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
        "<html><body>The offer process involves choossing a number of users that should be " +
        "informed of the existance of the work item, one of whom should eventually do this " +
        "work. As you have asked that the system automatically do this, you must now specify " +
        "how the system should automatically go about offering the work item. Begin by" +
        "specifying a set of users to distribute offers of work to.</body></html>"
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

    LinkedList<JComponent> panels = new LinkedList<JComponent>();
  }

  public String getWizardStepTitle() {
    return "Specify System Behaviour when Offering a Work Item";
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

  public void doNext() {
    parameterPanel.stopEditing();
  }

  public void refresh() {
    userPanel.setUserList(
        ResourcingServiceProxy.getInstance().getAllParticipants()
    );
    
    userPanel.setSelectedUsers(
      getResourceMapping().getBaseUserDistributionList()    
    );

    rolesPanel.setRoles(
        ResourcingServiceProxy.getInstance().getAllRoles()
    );

    rolesPanel.setSelectedRoles(
        getResourceMapping().getBaseRoleDistributionList()    
    );
    
    getResourceMapping().syncWithDataPerspective();
    parameterPanel.setVariableContentList(
        getResourceMapping().getBaseVariableContentList()
    );
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
    setBorder(new TitledBorder("Users"));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.insets = new Insets(0,5,5,5);
    gbc.fill = GridBagConstraints.BOTH;
    
    add(buildUserList(), gbc);
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
    getResourceMapping().setBaseUserDistributionList(
        userList.getSelectedUsers()
    );
  }
}

class UserList extends JList {
  private static final long serialVersionUID = 1L;

  private List<ResourcingParticipant> roles;

  public UserList() {
    super();
  }
  
  public void setUsers(List<ResourcingParticipant> users) {
    this.roles = users;
    String[] roleNames = new String[users.size()];
    for(int i = 0; i < users.size(); i++) {
      roleNames[i] = users.get(i).getName();
    }
    setListData(roleNames);
  }
  
  public void setSelectedUsers(List<ResourcingParticipant> selectedRoles) {
    if (selectedRoles == null) {
      return;
    }
    
    int[] selectedRoleIndicies = new int[selectedRoles.size()];

    int j = 0;
    for(int i = 0; i < roles.size(); i ++) {
      for(ResourcingParticipant selectedRole : selectedRoles) {
        if (roles.get(i).equals(selectedRole)) {
          selectedRoleIndicies[j] = i;
          j++;
        }
      }
    }
    setSelectedIndices(selectedRoleIndicies);
  }
  
  public List<ResourcingParticipant> getSelectedUsers() {
    int[] selectedRoleIndices = getSelectedIndices();
    List<ResourcingParticipant> selectedRoles = new LinkedList<ResourcingParticipant>();
    for(int i = 0; i < selectedRoleIndices.length; i++) {
      selectedRoles.add(
        roles.get(selectedRoleIndices[i])    
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
    gbc.weighty = 1;
    gbc.insets = new Insets(0,5,5,5);
    gbc.fill = GridBagConstraints.BOTH;
    
    add(buildRoleList(), gbc);
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
    getResourceMapping().setBaseRoleDistributionList(
        roleList.getSelectedRoles()
    );
  }
}

class RoleList extends JList {
  private static final long serialVersionUID = 1L;

  private List<ResourcingRole> roles;

  public RoleList() {
    super();
  }
  
  public void setRoles(List<ResourcingRole> roles) {
    this.roles = roles;
    String[] roleNames = new String[roles.size()];
    for(int i = 0; i < roles.size(); i++) {
      roleNames[i] = roles.get(i).getName();
    }
    setListData(roleNames);
  }
  
  public void setSelectedRoles(List<ResourcingRole> selectedRoles) {
    if (selectedRoles == null) {
      return;
    }
    
    int[] selectedRoleIndicies = new int[selectedRoles.size()];

    int j = 0;
    for(int i = 0; i < roles.size(); i ++) {
      for(ResourcingRole selectedRole : selectedRoles) {
        if (roles.get(i).equals(selectedRole)) {
          selectedRoleIndicies[j] = i;
          j++;
        }
      }
    }
    setSelectedIndices(selectedRoleIndicies);
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
    setBorder(new TitledBorder("Task Input Parameters"));

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