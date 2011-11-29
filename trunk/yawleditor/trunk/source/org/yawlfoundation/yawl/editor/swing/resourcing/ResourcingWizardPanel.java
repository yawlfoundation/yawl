package org.yawlfoundation.yawl.editor.swing.resourcing;

import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingParticipant;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingRole;
import org.yawlfoundation.yawl.editor.swing.AbstractWizardPanel;

import java.util.List;

abstract class ResourcingWizardPanel extends AbstractWizardPanel {

    List<ResourcingParticipant> userList;
    List<ResourcingRole> roleList;

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

    public List<ResourcingParticipant> getUserList() {
        return userList;
    }

    public void setUserList(List<ResourcingParticipant> userList) {
        this.userList = userList;
    }

    public List<ResourcingRole> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<ResourcingRole> roleList) {
        this.roleList = roleList;
    }

    abstract void refresh();
}
