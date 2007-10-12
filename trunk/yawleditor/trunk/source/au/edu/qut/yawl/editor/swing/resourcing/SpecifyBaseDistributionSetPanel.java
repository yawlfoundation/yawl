package au.edu.qut.yawl.editor.swing.resourcing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import au.edu.qut.yawl.editor.resourcing.NewYawlResourceMapping;

public class SpecifyBaseDistributionSetPanel extends ResourcingWizardPanel {

  private static final long serialVersionUID = 1L;

  private UserPanel userPanel;
  private RolesPanel rolesPanel;
  
  public SpecifyBaseDistributionSetPanel(ManageNewYAWLResourcingDialog dialog) {
    super(dialog);
  }
  
  protected void buildInterface() {
    setBorder(new EmptyBorder(8,5,11,5));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.333;
    gbc.weighty = 1;
    gbc.insets = new Insets(0,5,0,5);
    gbc.fill = GridBagConstraints.BOTH;
    
    add(buildUserPanel(), gbc);

    gbc.gridx++;
    
    add(buildRolesPanel(), gbc);
    
    gbc.gridx++;

    add(buildTaskInputParameterPanel(), gbc);

  }

  public String getWizardStepTitle() {
    return "Specify Base Distribution Set";
  }
  
  private JPanel buildUserPanel() {
    userPanel = new UserPanel();
    
    return userPanel;
  }
  
  private JPanel buildRolesPanel() {
    rolesPanel = new RolesPanel();
    return rolesPanel;
  }

  private JPanel buildTaskInputParameterPanel() {
    return new TaskInputParameterPanel();
  }

  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  public void doBack() {}

  public void doNext() {}

  public void refresh() {
    // TODO: fill in with resourcing changes to UI.
    
    userPanel.setUserList(
      getResourceMapping().getUserList()    
    );

    
    // TODO: hilight selected items in list.
    
    rolesPanel.setRoles(
      getResourceMapping().getRoles()    
    );
  }
  
  public boolean shouldDoThisStep() {
    return getResourceMapping().getOfferInteractionPoint() == 
      NewYawlResourceMapping.InteractionPointSetting.SYSTEM &&
      getResourceMapping().getRetainFamiliarTask() == null;
  }
}

class UserPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private JList userList;
  
  public UserPanel() {
    super();
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
    gbc.insets = new Insets(5,5,5,5);
    gbc.fill = GridBagConstraints.BOTH;
    
    add(buildUserList(), gbc);
  }
  
  private JScrollPane buildUserList() {
    userList = new JList();
    
    return new JScrollPane(userList);
  }
  
  public void setUserList(String[] users) {
    userList.setListData(users);
  }

}

class RolesPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  
  private JList roleList;
  
  public RolesPanel() {
    super();
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
    gbc.insets = new Insets(5,5,5,5);
    gbc.fill = GridBagConstraints.BOTH;
    
    add(buildRoleList(), gbc);
  }
  
  private JScrollPane buildRoleList() {
    roleList = new JList();
    
    return new JScrollPane(roleList);
  }
  
  public void setRoles(String[] roles) {
    roleList.setListData(roles);
  }
}

class TaskInputParameterPanel extends JPanel {
  
  private static final long serialVersionUID = 1L;
  
  public TaskInputParameterPanel() {
    super();
    buildInterface();
  }
  
  private void buildInterface() {
    setBorder(new TitledBorder("Task Input Parameters"));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
  }
}