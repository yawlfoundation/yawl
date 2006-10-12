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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.AbstractWizardDialog;
import au.edu.qut.yawl.editor.swing.AbstractWizardPanel;
import au.edu.qut.yawl.editor.swing.JFormattedAlphaNumericField;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.swing.data.AbstractXMLStyledDocument;
import au.edu.qut.yawl.editor.swing.data.JProblemReportingEditorPane;
import au.edu.qut.yawl.editor.swing.data.ValidityEditorPane;
import au.edu.qut.yawl.editor.swing.resourcing.AllocationStrategyComboBox;
import au.edu.qut.yawl.editor.swing.resourcing.ResourceAllocationComboBox;
import au.edu.qut.yawl.editor.swing.resourcing.ResourceOfferingComboBox;
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
  
  private boolean administratorOffersWork = false;
  
  protected void initialise() {
    setPanels(
        new AbstractWizardPanel[] {
            new GuidedOrAdvancedWizardPanel(this),
            new WorkDistributionOptionsPanel(this),
            new DistributeToRolesAndIndividualsPanel(this),
            new DistributeToUserAndRoleVariablesPanel(this),
            new FilteringByFamiliarityPanel(this),
            new SelectionByOrganisationalRequirementsPanel(this),
            new FilterByCapabilityPanel(this),
            new ReviewResourceAllocationOfferingPanel(this)
        }
    );
  }
  
  public boolean adminsitratorOffersWork() {
    return this.administratorOffersWork;
  }
  
  public void setAdministratorOffersWork(boolean adminOffersWork) {
    this.administratorOffersWork = adminOffersWork;
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
    setSize(800,400);
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

class WorkDistributionOptionsPanel extends AbstractWizardPanel {

  private ResourceOfferingComboBox offerTypeComboBox;
  private JComboBox startingTypeComboBox;
  private ResourceAllocationComboBox allocationTypeComboBox;
  private AllocationStrategyComboBox allocationStrategyComboBox;
  
  private JCheckBox singleUserAllocationCheckBox;
    
  public WorkDistributionOptionsPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardStepTitle() {
    return "Work Distribution Options";
  }
  
  protected void initialise() {
    // TODO :Widget initialisation.
  }
  
  protected void buildInterface() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 3;
    gbc.weighty = 0;
    gbc.insets = new Insets(5,5,10,5);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;

    add(new JLabel(
        "Describe how work items of this task are to be offered, allocated and started below:"
        ), gbc
    );
    
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.weightx = 0.333;
    gbc.insets = new Insets(5,5,2,5);
    gbc.anchor = GridBagConstraints.WEST;
    
    add(new JLabel("Offering:"),gbc);
    
    gbc.gridx++;
    add(new JLabel("Allocation:"),gbc);
    
    gbc.gridx++;
    add(new JLabel("Starting:"),gbc);
    
    gbc.gridy++;
    gbc.gridx = 0;
    gbc.insets = new Insets(2,5,5,5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.CENTER;
        
    add(buildOfferList(), gbc);
  
    gbc.gridx++;
    add(buildAllocationList(), gbc);

    gbc.gridx++;
    add(buildStartingList(), gbc);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(10,5,5,5);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;
    
    add(buildSingleUserAllocationCheckBox(),gbc);
    
    
    gbc.gridx = gbc.gridx + 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.CENTER;
    
    add(buildAllocationStrategyList(), gbc);
    
    bindRelatedWidgetEvents();
  }
  
  private void bindRelatedWidgetEvents() {
    allocationTypeComboBox.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if (allocationTypeComboBox.getAllocation() == ResourceAllocationComboBox.SYSTEM_ALLOCATION) {
            allocationStrategyComboBox.setSystemAllocationRequired(true);
            singleUserAllocationCheckBox.setEnabled(true);
          } else {
            allocationStrategyComboBox.setSystemAllocationRequired(false);
            singleUserAllocationCheckBox.setEnabled(false);
          }
        }
      }
    );

    allocationStrategyComboBox.setSystemAllocationRequired(
        allocationTypeComboBox.getAllocation() == ResourceAllocationComboBox.SYSTEM_ALLOCATION
    );
    
    singleUserAllocationCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            allocationStrategyComboBox.setSingleAllocationRequired(
                singleUserAllocationCheckBox.isSelected()
            );
          }
        }
    );
    
    allocationStrategyComboBox.setSingleAllocationRequired(
        singleUserAllocationCheckBox.isSelected()
    );
  }
  
  private ResourceOfferingComboBox buildOfferList() {
    this.offerTypeComboBox = new ResourceOfferingComboBox();
    this.offerTypeComboBox.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          ((ManageResourcingDialog) getDialog()).setAdministratorOffersWork(
              offerTypeComboBox.getOfferType() == ResourceOfferingComboBox.ADMINISTRATOR_OFFERS
          );
        }
      }
    );
    
    return offerTypeComboBox;
  }
  
  private JCheckBox buildSingleUserAllocationCheckBox() {
    singleUserAllocationCheckBox = new JCheckBox(
        "Ensure only one user is allocated the work-item via strategy:"
    );
    
    return singleUserAllocationCheckBox;
  };

  private ResourceAllocationComboBox buildAllocationList() {
    allocationTypeComboBox = new ResourceAllocationComboBox();
    return allocationTypeComboBox;
  }


  private JComboBox buildStartingList() {

    startingTypeComboBox = new JComboBox(
        new String[] {
          "System starts work-item upon allocation.",
          "User informs system of work-item commencement."
        }
    );

    return startingTypeComboBox;
  }

  private AllocationStrategyComboBox buildAllocationStrategyList() {
    allocationStrategyComboBox = new AllocationStrategyComboBox();
    return allocationStrategyComboBox;
  }
  
  public void doBack() {}

   public void doNext() {
     if (offerTypeComboBox.getOfferType() == ResourceOfferingComboBox.ADMINISTRATOR_OFFERS) {
       getDialog().doLast();
     }
   }     
}

class DistributeToRolesAndIndividualsPanel extends AbstractWizardPanel {

  public DistributeToRolesAndIndividualsPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardStepTitle() {
    return "Add Individuals and/or Roles to Distribution Pool";
  }

  
  protected void initialise() {
    // TODO: initialise widgets
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

  public void doBack() {}

   public void doNext() {}     
}

class DistributeToUserAndRoleVariablesPanel extends AbstractWizardPanel {

  public DistributeToUserAndRoleVariablesPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardStepTitle() {
    return "Add Variables identifying users or rolls to Distibution Pool";
  }

  
  protected void initialise() {
    // TODO: Initialise widgets
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

class FilteringByFamiliarityPanel extends AbstractWizardPanel {
  
  private JRadioButton distributeVlaCompletedButton;
  private JRadioButton distributeViaNonCompleteButton;
  private JRadioButton noDistirbuteButton;

  private ButtonGroup buttonGroup;

  private JComboBox taskComboBox;
  private JComboBox familarityTypeComboBox;
  
  public FilteringByFamiliarityPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardStepTitle() {
    return "Filter Distribution Pool By Familiarity";
  }

  
  protected void initialise() {
    // TODO: widget initialsiation
  }
  
  protected void buildInterface() {
    
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 4;
    gbc.weighty = 0;
    gbc.insets = new Insets(5,0,0,0);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;

    add(new JLabel(
        "From the following options, select how familiarity with a task filters out users to distribute work to."
        ), gbc
    );

    gbc.gridy++;
    gbc.insets = new Insets(0,5,0,0);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;

    add(buildNoDistributionRadioButton(), gbc);

    gbc.gridy++;
    gbc.insets = new Insets(10,0,0,2);

    add(new JLabel(
        "Filter allocation of this task to those users of a running case who:"
        ), gbc
    );

    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.insets = new Insets(0,5,0,0);

    add(buildCompletedRadioButton(), gbc);

    gbc.insets = new Insets(0,5,0,0);
    gbc.gridy++;
    
    add(buildNonCompletedRadioButton(), gbc);

    buttonGroup = new ButtonGroup();
    
    buttonGroup.add(noDistirbuteButton);
    buttonGroup.add(distributeVlaCompletedButton);
    buttonGroup.add(distributeViaNonCompleteButton);

    buttonGroup.setSelected(noDistirbuteButton.getModel(),true);

    gbc.gridx = 1;
    gbc.gridy = gbc.gridy - 1;
    gbc.insets = new Insets(0,5,0,5);
    gbc.gridheight = 2;
    
    add(new JLabel("the task"),gbc);
    
    gbc.gridx++;
    
    add(buildTaskComboBox(),gbc);

    gbc.gridx++;
    
    add(buildFamiliarityTypeComboBox(),gbc);
  }
  
  private JRadioButton buildCompletedRadioButton() {
    distributeVlaCompletedButton = new JRadioButton("have completed");
    return distributeVlaCompletedButton;
  }
  
  private JRadioButton buildNonCompletedRadioButton() {
    distributeViaNonCompleteButton = new JRadioButton("have not completed");
    return distributeViaNonCompleteButton;
  }

  private JRadioButton buildNoDistributionRadioButton() {
    noDistirbuteButton = new JRadioButton("Do not filter work-items of this task based on familiarity, or");
    return noDistirbuteButton;
  }
  
  private JComboBox buildFamiliarityTypeComboBox() {
    familarityTypeComboBox = new JComboBox(
      new String[] {
          "the most number of times.",
          "the least number of times.",
          "most recently.",
          "least recently."
      }
    );
    
    return familarityTypeComboBox;
  }

  private JComboBox buildTaskComboBox() {
    taskComboBox = new JComboBox(
      new String[] {
          "this task",
          "some other task"
      }
    );
    
    return taskComboBox;
  }

  
  public void doBack() {}

   public void doNext() {}     
}

class SelectionByOrganisationalRequirementsPanel extends AbstractWizardPanel {
  
  private JRadioButton distributeToPositionButton;
  private JRadioButton distributeToNearbyPositionButton;
  private JRadioButton noDistirbuteButton;

  private ButtonGroup buttonGroup;

  private JComboBox nearbyPositionComboBox;
  private JList positionList;
  private JList groupList;
  
  public SelectionByOrganisationalRequirementsPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardStepTitle() {
    return "Filter Distribution Pool By Organisational Requirements";
  }
  
  protected void initialise() {
    // TODO: initialise widgets
  }
  
  protected void buildInterface() {
    
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 4;
    gbc.weighty = 0;
    gbc.insets = new Insets(5,0,0,0);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;

    add(new JLabel(
        "From the following options, select how organisation structure filters out users that this task can be allocated to."
        ), gbc
    );

    gbc.gridy++;
    gbc.insets = new Insets(0,5,0,0);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;

    add(buildNoDistributionRadioButton(), gbc);

    gbc.gridy++;
    gbc.insets = new Insets(10,0,0,2);

    add(new JLabel(
        "Distribute work-items of this task to:"
        ), gbc
    );

    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.insets = new Insets(0,5,0,0);

    add(buildDistributieToPositionRadioButton(), gbc);

    gbc.gridx++;
    
    add(new JLabel("users holding"),gbc);
    
    gbc.insets = new Insets(0,5,0,0);
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.gridy++;
    gbc.gridx--;
    
    add(buildDistributeToNearbyPositionRadioButton(), gbc);

    buttonGroup = new ButtonGroup();
    
    buttonGroup.add(noDistirbuteButton);
    buttonGroup.add(distributeToPositionButton);
    buttonGroup.add(distributeToNearbyPositionButton);

    buttonGroup.setSelected(noDistirbuteButton.getModel(),true);

    gbc.insets = new Insets(0,5,5,5);
    gbc.gridx++;
    
    add(buildNearbyPositionComboBox(),gbc);

    gbc.gridy--;
    gbc.gridx++;
    gbc.gridheight = 2;
    
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.NONE;
    
    add(new JLabel("the position"),gbc);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridx++;
    gbc.weighty = 0.5;
    gbc.gridheight = 3;
    
    add(buildPositionList(),gbc);
    
    gbc.gridy = gbc.gridy + 3;
    gbc.gridx = 0;
    gbc.weighty = 0;
    gbc.gridwidth = 3;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.fill = GridBagConstraints.NONE;
    
    add(new JLabel("�n the following organisational groups:"),gbc);

    gbc.gridx=3;
    gbc.weighty = 0.5;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.BOTH;
    
    add(buildGroupList(),gbc);
  }
  
  private JRadioButton buildDistributieToPositionRadioButton() {
    distributeToPositionButton = new JRadioButton();
    return distributeToPositionButton;
  }
  
  private JRadioButton buildDistributeToNearbyPositionRadioButton() {
    distributeToNearbyPositionButton = new JRadioButton();
    return distributeToNearbyPositionButton;
  }

  private JRadioButton buildNoDistributionRadioButton() {
    noDistirbuteButton = new JRadioButton("Do not filter users that can be alllocated work-items of this task based on organisation, or");
    return noDistirbuteButton;
  }
  
  private JScrollPane buildPositionList() {

    final String[] positions = new String[] {
      "All Positions",
      "Manager",
      "General Manager",
      "Clerk",
      "Accountant",
      "Marketing Executive"
    };
    
    positionList = new JList(positions);
    JScrollPane scrollpane = new JScrollPane(positionList);
    scrollpane.getViewport().setMaximumSize(
      positionList.getPreferredScrollableViewportSize()
    );
    
    return scrollpane;
  }

  private JScrollPane buildGroupList() {

    final String[] groups = new String[] {
      "All Positions",
      "Manager",
      "General Manager",
      "Clerk",
      "Accountant",
      "Marketing Executive"
    };
    
    groupList = new JList(groups);
    JScrollPane scrollpane = new JScrollPane(groupList);
    scrollpane.getViewport().setMaximumSize(
      groupList.getPreferredScrollableViewportSize()
    );
    
    return scrollpane;
  }

  private JComboBox buildNearbyPositionComboBox() {
    nearbyPositionComboBox = new JComboBox(
      new String[] {
          "All subordinates of",
          "All superiors of"
      }
    );
    
    return nearbyPositionComboBox;
  }

  
  public void doBack() {}

   public void doNext() {}     
}

class FilterByCapabilityPanel extends AbstractWizardPanel {

  public FilterByCapabilityPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  public String getWizardStepTitle() {
    return "Filter Distirbution Pool by Resource Capabilities";
  }

  
  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  protected void buildInterface() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 5;
    gbc.weighty = 0;
    gbc.insets = new Insets(5,5,5,5);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;

    add(new JLabel(
        "From below, construct a number of capabilities that resources must posses to remain in the distribution pool:"
        ), gbc
    );
    
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.EAST;
    
    add(new JLabel("Select users with capability"),gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.CENTER;
    
    add(new JFormattedAlphaNumericField(15), gbc);

    gbc.gridx++;
    gbc.weightx = 0;
    gbc.insets = new Insets(5,2,5,2);

    add(new JLabel("of"),gbc);

    gbc.gridx++;
    gbc.insets = new Insets(5,5,5,5);
    gbc.anchor = GridBagConstraints.WEST;

    add(new JFormattedAlphaNumericField(15), gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.CENTER;

    add(new JButton("Add"),gbc);
    
    gbc.gridy++;
    gbc.gridx = 0;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    add(new JLabel("Capabilities:"),gbc);
    
    gbc.gridx++;
    gbc.gridwidth = 3;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.BOTH;
    
    add(new JScrollPane(new JList(new String[] {"\"10 years\" of \"experience\"","\"tastiness\" of \"waffles\"" } )),gbc);
    
    gbc.gridx = gbc.gridx + 3;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.NONE;
    
    add(new JButton("Remove"),gbc);

    /*  TODO: equalize buttons. 
    buttonList.add(basicButton);
    buttonList.add(advancedButton);
      
    JUtilities.equalizeComponentSizes(buttonList);
*/
  }
  
  public void doBack() {}

   public void doNext() {}     
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
    // TODO: Initialise widgets
  }
  
  public void doBack() {
    if (((ManageResourcingDialog) getDialog()).adminsitratorOffersWork()) {
      getDialog().doStep(2);
    } 
  }

  public void doNext() {}     
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