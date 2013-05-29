package org.yawlfoundation.yawl.editor.ui.swing.resourcing;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractWizardDialog;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractWizardPanel;
import org.yawlfoundation.yawl.editor.ui.swing.JUtilities;

import javax.swing.*;

public class ManageResourcingDialog extends AbstractWizardDialog {
  private static final long serialVersionUID = 1L;
  
  private YAWLTask task;
  private NetGraph net;
  
  protected void initialise() {
    setPanels(
        new AbstractWizardPanel[] {
            new SetInteractionPointBehaviourPanel(this),
            new SpecifyBaseDistributionSetPanel(this),
            new SpecifyDistributionSetFiltersPanel(this),
            new SetSecondaryResourcesPanel(this),
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
    setSize(810,500);
    JUtilities.setMinSizeToCurrent(this);
  }
  
  public void setTask(YAWLTask task, NetGraph net) {
    this.task = task;
    this.net = net;

    if (task.getResourceMapping() == null) {
      task.setResourceMapping(
          new ResourceMapping((YAWLAtomicTask) task, true));
    }

    for(AbstractWizardPanel panel : getPanels()) {
      ((ResourcingWizardPanel) panel).refresh();
    }

    if (! YConnector.isResourceConnected()) {
      JOptionPane.showMessageDialog(this,
              "A Connection to a running resource service could not be established.\n" +
              "Attempting to use a local cache of resource data, if possible.",
              "Resource Service Connection Warning", JOptionPane.WARNING_MESSAGE);
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


  public boolean doFinish() {
      if (getCurrentPanel().doNext()) {
          SpecificationUndoManager.getInstance().setDirty(true);    // 'save' needed flag
          return true;
      }
      return false;
  }

  public void setVisible(boolean state) {
    if (state) {
      JUtilities.centreWindowUnderVertex(net, this, task, 10);
    }
    super.setVisible(state);
  }
  
  public ResourceMapping getResourceMapping() {
    if (getTask() != null)
       return getTask().getResourceMapping();
    else
       return null;
  }
}