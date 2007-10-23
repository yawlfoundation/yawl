package au.edu.qut.yawl.editor.swing.resourcing;

import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.resourcing.ResourceMapping;
import au.edu.qut.yawl.editor.swing.AbstractWizardPanel;

abstract class ResourcingWizardPanel extends AbstractWizardPanel {
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

  abstract void refresh();
}
