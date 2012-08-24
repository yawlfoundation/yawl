package org.yawlfoundation.yawl.editor.ui.swing.resourcing;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractWizardPanel;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import java.util.List;

abstract class ResourcingWizardPanel extends AbstractWizardPanel {

    List<Participant> userList;
    List<Role> roleList;

  public ResourcingWizardPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  protected ManageResourcingDialog getNewYAWLResourcingDialog() {
    return (ManageResourcingDialog) getDialog();
  }
  
  protected ResourceMapping getResourceMapping() {
    //TODO: this ultimately needs to be sourced from the task.
    return getNewYAWLResourcingDialog().getResourceMapping();
  }
  
  protected YAWLTask  getTask() {
    return getNewYAWLResourcingDialog().getTask();
  }

    public List<Participant> getUserList() {
        return userList;
    }

    public void setUserList(List<Participant> userList) {
        this.userList = userList;
    }

    public List<Role> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }

    abstract void refresh();
}
