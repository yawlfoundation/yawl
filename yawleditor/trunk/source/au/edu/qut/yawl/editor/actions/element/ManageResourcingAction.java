/*
 * Created on 27/05/2005
 * YAWLEditor v1.3 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package au.edu.qut.yawl.editor.actions.element;

import javax.swing.JLabel;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.AbstractWizardDialog;
import au.edu.qut.yawl.editor.swing.AbstractWizardPanel;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;

import java.awt.event.ActionEvent;

import javax.swing.Action;

public class ManageResourcingAction extends YAWLSelectedNetAction
                                    implements TooltipTogglingWidget {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private YAWLTask task;
  private NetGraph graph;
  
  private ManageResourcingDialog dialog = new ManageResourcingDialog();
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Manage Resourcing");
    putValue(Action.LONG_DESCRIPTION, "Manage the resourcing requirements of this task.");
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
  }
  
  public ManageResourcingAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }  

  public void actionPerformed(ActionEvent event) {
    dialog.setTask(task, graph);
    dialog.setVisible(true);
  }
 
  public String getEnabledTooltipText() {
    return " Manage the resourcing requirements of this task ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have an atomic task with a worklist decomposition selected" + 
           " to update its resourcing requirements ";
  }
  
  public boolean shouldBeEnabled() {
    if (task.getDecomposition() != null && task.getDecomposition().invokesWorklist()) {
      return true;
    }
    return false;
  }
}

class ManageResourcingDialog extends AbstractWizardDialog {
  private YAWLTask task;
  private NetGraph net;
  
  protected void initialise() {
    setPanels(
        new AbstractWizardPanel[] {
            new GuidedOrAdvancedWizardPanel(this),
            new DistributeToRolesAndIndividualsPanel(this),
            new DistributeToUserAndRoleVariablesPanel(this),
            new SelectionByFamiliarityPanel(this),
            new SelectionByOrganisationalRequirementsPanel(this),
            new WorkDistributionOptionsPanel(this),
            new ReviewResourceAllocationOfferingPanel(this)
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
    pack();
  }
  
  public void setTask(YAWLTask task, NetGraph net) {
    this.task = task;
    this.net = net;

    setTitle(getTitlePrefix() + task.getType() + getTitleSuffix());
  }
  
  public void doFinish() {
    System.out.println("Finish Button Pressed");
  }
  
  public void setVisible(boolean state) {
    if (state == true) {
      JUtilities.centreWindowUnderVertex(net, this, task, 10);
    }
    super.setVisible(state);
  }
}

class GuidedOrAdvancedWizardPanel extends AbstractWizardPanel {

  public GuidedOrAdvancedWizardPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardTitle() {
    return "Choose Basic or Advanced Resource Management";
  }

  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
    this.add(new JLabel("some text for panel " + this.getWizardTitle())); 
   }
  
  public void doBack() {
    System.out.println("some back processing here");
  }

   public void doNext() {
     System.out.println("some next processing here");
   }     
}

class DistributeToRolesAndIndividualsPanel extends AbstractWizardPanel {

  public DistributeToRolesAndIndividualsPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardTitle() {
    return "Distribute To Roles and Individuals";
  }

  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
   this.add(new JLabel("some text for panel " + this.getWizardTitle())); 
  }
  
  public void doBack() {
    System.out.println("some back processing here");
  }

   public void doNext() {
     System.out.println("some next processing here");
   }     
}

class DistributeToUserAndRoleVariablesPanel extends AbstractWizardPanel {

  public DistributeToUserAndRoleVariablesPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardTitle() {
    return "Distribute To User And Role Variables";
  }

  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
   this.add(new JLabel("some text for panel " + this.getWizardTitle())); 
  }
  
  public void doBack() {
    System.out.println("some back processing here");
  }

   public void doNext() {
     System.out.println("some next processing here");
   }     
}

class SelectionByFamiliarityPanel extends AbstractWizardPanel {

  public SelectionByFamiliarityPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardTitle() {
    return "Selection By Familiarity";
  }

  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
   this.add(new JLabel("some text for panel " + this.getWizardTitle())); 
  }
  
  public void doBack() {
    System.out.println("some back processing here");
  }

   public void doNext() {
     System.out.println("some next processing here");
   }     
}

class SelectionByOrganisationalRequirementsPanel extends AbstractWizardPanel {

  public SelectionByOrganisationalRequirementsPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardTitle() {
    return "Selection By Organisational Requirements";
  }

  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
   this.add(new JLabel("some text for panel " + this.getWizardTitle())); 
  }
  
  public void doBack() {
    System.out.println("some back processing here");
  }

   public void doNext() {
     System.out.println("some next processing here");
   }     
}


class WorkDistributionOptionsPanel extends AbstractWizardPanel {

  public WorkDistributionOptionsPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardTitle() {
    return "Work Distribution Options";
  }
  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
   this.add(new JLabel("some text for panel " + this.getWizardTitle())); 
  }
  
  public void doBack() {
    System.out.println("some back processing here");
  }

   public void doNext() {
     System.out.println("some next processing here");
   }     
}

class ReviewResourceAllocationOfferingPanel extends AbstractWizardPanel {

  public ReviewResourceAllocationOfferingPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardTitle() {
    return "Review Resource Allocation/Offering";
  }
  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
   this.add(new JLabel("some text for panel " + this.getWizardTitle())); 
  }
  
  public void doBack() {
    System.out.println("some back processing here");
  }

   public void doNext() {
     System.out.println("some next processing here");
   }     
}