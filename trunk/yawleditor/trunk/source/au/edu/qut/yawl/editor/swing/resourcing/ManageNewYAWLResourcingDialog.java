package au.edu.qut.yawl.editor.swing.resourcing;

import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;

import au.edu.qut.yawl.editor.net.NetGraph;

import au.edu.qut.yawl.editor.resourcing.NewYawlResourceMapping;

import au.edu.qut.yawl.editor.swing.AbstractWizardDialog;
import au.edu.qut.yawl.editor.swing.AbstractWizardPanel;
import au.edu.qut.yawl.editor.swing.JUtilities;

public class ManageNewYAWLResourcingDialog extends AbstractWizardDialog {
  private static final long serialVersionUID = 1L;
  
  private YAWLTask task;
  private NetGraph net;
  
  private NewYawlResourceMapping resourceMapping;
  
  protected void initialise() {
    setPanels(
        new AbstractWizardPanel[] {
            new SetInteractionPointBehaviourPanel(this),
            new SetSystemOfferBehaviourPanel(this),
            new SpecifyBaseDistributionSetPanel(this),
            new SpecifyDistributionSetFiltersPanel(this),
            new SetSystemAllocateBehaviourPanel(this),
            new SetRuntimePrivilegesPanel(this)
        }
    );
  }
  
  public String getTitlePrefix() {
    return "Manage Resourcing Wizard for ";
  }
  
  public String getTitleSuffix() {
    if (task.getLabel() != null && !task.getLabel().equals("")) {
      return " \"" + task.getLabel() + "\"";
    }
    return "";
  }
  
  protected void makeLastAdjustments() {
    //pack();
    setSize(800,450);
    //setResizable(false);
  }
  
  public void setTask(YAWLTask task, NetGraph net) {
    this.task = task;
    this.net = net;

    //TODO: tie into task state.
    
    this.resourceMapping = new NewYawlResourceMapping((YAWLAtomicTask) task);
    
    for(AbstractWizardPanel panel : getPanels()) {
      ((ResourcingWizardPanel) panel).refresh();
    }
    
    doFirst();
    
    setTitle(
        getTitlePrefix() + 
        task.getType() + 
        getTitleSuffix()
    );
  }
  
  public YAWLTask getTask() {
    return this.task;
  }
  
  public void doFinish() {
    System.out.println(
        getResourceMapping()
    );
  }
  
  public void setVisible(boolean state) {
    if (state == true) {
      JUtilities.centreWindowUnderVertex(net, this, task, 10);
    }
    super.setVisible(state);
  }
  
  public NewYawlResourceMapping getResourceMapping() {
    return resourceMapping;
  }
}