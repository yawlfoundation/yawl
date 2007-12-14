package au.edu.qut.yawl.editor.swing.resourcing;

import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;

import au.edu.qut.yawl.editor.net.NetGraph;

import au.edu.qut.yawl.editor.resourcing.ResourceMapping;

import au.edu.qut.yawl.editor.swing.AbstractWizardDialog;
import au.edu.qut.yawl.editor.swing.AbstractWizardPanel;
import au.edu.qut.yawl.editor.swing.JUtilities;

public class ManageResourcingDialog extends AbstractWizardDialog {
  private static final long serialVersionUID = 1L;
  
  private YAWLTask task;
  private NetGraph net;
  
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
    setSize(800,450);
    setResizable(false);
  }
  
  public void setTask(YAWLTask task, NetGraph net) {
    this.task = task;
    this.net = net;

    if (task.getResourceMapping() == null) {
      task.setResourceMapping(
          new ResourceMapping(
              (YAWLAtomicTask) task
          )
      );
    }
    
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

  /*
  public void doFirst() {
    System.out.println("----Start resource mapping----");
    System.out.println(
        getResourceMapping()
    );
    super.doFirst();
  }
  */

  public void doFinish() {
  /*
    System.out.println("----Finish resource mapping----");
    System.out.println(
        getResourceMapping()
    );
  */
  }

  public void setVisible(boolean state) {
    if (state == true) {
      JUtilities.centreWindowUnderVertex(net, this, task, 10);
    }
    super.setVisible(state);
  }
  
  public ResourceMapping getResourceMapping() {
    return getTask().getResourceMapping();
  }
}