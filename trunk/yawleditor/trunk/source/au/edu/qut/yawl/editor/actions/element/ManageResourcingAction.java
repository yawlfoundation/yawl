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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.AbstractWizardDialog;
import au.edu.qut.yawl.editor.swing.AbstractWizardPanel;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.swing.data.AbstractXMLStyledDocument;
import au.edu.qut.yawl.editor.swing.data.JProblemReportingEditorPane;
import au.edu.qut.yawl.editor.swing.data.ValidityEditorPane;
import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;
import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

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
            new UserTaskAuthorisationPanel(this),
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
    //setResizable(false);
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
  
  private JButton basicButton;
  private JButton advancedButton;

  public GuidedOrAdvancedWizardPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardStepTitle() {
    return "Choose Basic or Advanced Resource Management";
  }
  
  protected void initialise() {}
  
  protected void buildInterface() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.weighty = 0.333;
    gbc.insets = new Insets(10,0,10,0);
    gbc.anchor = GridBagConstraints.WEST;

    add(new JLabel("This wizard will help you to construct an expression that tells the " +
            "workflow engine how work for this task should be allocated. "), gbc);

    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.weighty = 0;
    gbc.anchor = GridBagConstraints.EAST;

    add(new JLabel("To be guided through the process of building the expression click here: "), gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.CENTER;
    
    add(buildBasicButton(),gbc);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.EAST;

    add(new JLabel("If you are familiar with resourcing expressions, you can skip to the end by clicking here: "), gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.CENTER;
    
    add(buildAdvancedButton(),gbc);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbc.weighty = 0.666;
    gbc.anchor = GridBagConstraints.CENTER;
    add(new JLabel(),gbc);

    LinkedList buttonList = new LinkedList();

    buttonList.add(basicButton);
    buttonList.add(advancedButton);
      
    JUtilities.equalizeComponentSizes(buttonList);
   }
  
  private JButton buildBasicButton() {
    basicButton = new JButton("Begin Wizard");
    basicButton.setMnemonic(KeyEvent.VK_B);
    basicButton.setMargin(new Insets(2,11,3,12));
    basicButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          getDialog().doNext();
        }
      }
    );
    return basicButton; 
  }

  private JButton buildAdvancedButton() {
    advancedButton = new JButton("Edit Resource Expression");
    advancedButton.setMnemonic(KeyEvent.VK_E);
    advancedButton.setMargin(new Insets(2,11,3,12));
    advancedButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          getDialog().doLast();
        }
      }
    );
    return advancedButton; 
  }

  public void doBack() {}

  public void doNext() {}     
}

class DistributeToRolesAndIndividualsPanel extends AbstractWizardPanel {

  public DistributeToRolesAndIndividualsPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardStepTitle() {
    return "Distribute To Individuals and/or Roles";
  }

  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 3;
    gbc.weighty = 0;
    gbc.insets = new Insets(10,0,10,0);
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;

    add(new JLabel("<html><body>Below are lists of available individuals and roles.<p>Chose from both lists, " +
            "those individuals and/or roles you wish work items of this task to be distributed to. </body></html>"), gbc);
    
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.weighty = 1;
    gbc.weightx = 0.4;
    gbc.insets = new Insets(10,0,0,0);
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.EAST;
    
    add(buildIndividualsPanel(), gbc);
    
    gbc.gridx++;
    gbc.weightx = 0;
    gbc.insets = new Insets(20,10,10,10);
    gbc.fill = GridBagConstraints.VERTICAL;
    gbc.anchor = GridBagConstraints.CENTER;

    add(new JLabel("and/or"),gbc);

    gbc.weightx = 0.4;
    gbc.gridx++;
    gbc.insets = new Insets(10,0,0,0);
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.WEST;

    add(buildRolesPanel(), gbc);
  }
  
  JPanel buildIndividualsPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(
      new CompoundBorder(
        new TitledBorder("Individuals"),
        new EmptyBorder(0,5,5,5)
      )    
    );
    
    // TODO: Source real data
    
    String individuals[] = {"All Staff", "John", 
        "Eric", "Moses", "Elijah", 
        "Arthur", "Henry", "Frederick", 
        "Adam", "Michael", "Johnathon",
        "Lindsay", "Tore", "Lachlan"
    };
    
    panel.setLayout(new BorderLayout());
    JList list = new JList(individuals);
    JScrollPane scrollpane = new JScrollPane(list);
    scrollpane.getViewport().setMaximumSize(
      list.getPreferredScrollableViewportSize()
    );
    
    
    panel.add(scrollpane, BorderLayout.CENTER);

    return panel;
  }

  JPanel buildRolesPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(
      new CompoundBorder(
        new TitledBorder("Roles"),
        new EmptyBorder(0,5,5,5)
      )    
    );

    // TODO: Source real data
    
    String roles[] = {"All Roles", "Safety Officer", 
        "Approving Officer", "Admin Officer", "Pantry IC", 
        "Meeting IC"
    };
    
    panel.setLayout(new BorderLayout());
    JList list = new JList(roles);
    JScrollPane scrollpane = new JScrollPane(list);
    scrollpane.getViewport().setMaximumSize(
      list.getPreferredScrollableViewportSize()
    );
    
    panel.add(scrollpane, BorderLayout.CENTER);

    return panel;
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
  
  public String getWizardStepTitle() {
    return "Distribute To User and/or Role Variables";
  }

  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
    setLayout(new BorderLayout());
    add(new JLabel(
            "Don't understand what's being asked for here. Discuss."
        ), BorderLayout.CENTER
    ); 
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
  
  public String getWizardStepTitle() {
    return "Selection By Familiarity";
  }

  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
    
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 3;
    gbc.weighty = 0;
    gbc.insets = new Insets(10,0,10,0);
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.WEST;

    add(new JLabel("from the following options, selecte how familiarity with this task effects it's allocation."), gbc);
    
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
  
  public String getWizardStepTitle() {
    return "Selection By Organisational Requirements";
  }

  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
    setLayout(new BorderLayout());
    add(new JLabel(
            "Work in progress"
        ), BorderLayout.CENTER
    ); 
  }
  
  public void doBack() {
    System.out.println("some back processing here");
  }

   public void doNext() {
     System.out.println("some next processing here");
   }     
}

class UserTaskAuthorisationPanel extends AbstractWizardPanel {

  public UserTaskAuthorisationPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardStepTitle() {
    return "User Task Authorisation";
  }

  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
    setLayout(new BorderLayout());
    add(new JLabel(
            "Nothing has been supplied UI wise for this step yet."
        ), BorderLayout.CENTER
    ); 
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
  
  public String getWizardStepTitle() {
    return "Work Distribution Options";
  }
  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  protected void buildInterface() {
    setLayout(new BorderLayout());
    add(new JLabel(
            "Work in progress"
        ), BorderLayout.CENTER
    ); 
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
  
  protected void buildInterface() {

    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(12,12,0,11));

    add(new ResourcingExpressionEditorPane(),BorderLayout.CENTER);
  }

  public String getWizardStepTitle() {
    return "Review Resource Allocation/Offering Expression";
  }
  
  protected void initialise() {
    System.out.println("some initialising here");
  }
  
  public void doBack() {
    System.out.println("some back processing here");
  }

   public void doNext() {
     System.out.println("some next processing here");
   }     
}

class ResourcingExpressionEditorPane extends JProblemReportingEditorPane {

  public ResourcingExpressionEditorPane() {
    super(new ResourcingExpressionEditor());
  }
}

class ResourcingExpressionEditor extends ValidityEditorPane {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ResourcingExpressionEditor() {
    super();
    setDocument(new ResourceExpressionDocument(this));
    this.setText("Resource is \"Safety Officer\'\n  and is \"supervisor\"...");
  }

  class ResourceExpressionDocument extends AbstractXMLStyledDocument {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ResourceExpressionDocument(ValidityEditorPane editor) {
      super(editor);
    }
    
    public List getProblemList() {
      /* TODO:
      return YAWLEngineProxy.getInstance().getSchemaValidationResults(
          getEditor().getText()
      );*/
      return null;
    }
    
    public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
      // deliberately does nothing.
    }

    public void checkValidity() {
      
      if (getEditor().getText().equals("")) {
        setContentValid(true);
        return;
      }
      
      List validationResults = getProblemList();
 
      setContentValid(validationResults == null ? true : false);
    }
  }
}