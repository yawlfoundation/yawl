package au.edu.qut.yawl.editor.swing.resourcing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.resourcing.ResourceMapping;

public class SpecifyDistributionSetFiltersPanel extends ResourcingWizardPanel {

  private static final long serialVersionUID = 1L;

  private RuntimeConstraintsPanel runtimeConstraintsPanel;
  
  public SpecifyDistributionSetFiltersPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  protected void buildInterface() {
    setBorder(new EmptyBorder(5,5,5,5));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.weighty = 0.666;
    gbc.insets = new Insets(0,5,0,5);
    gbc.fill = GridBagConstraints.BOTH;
    
    add(buildFilterPanel(), gbc);

    gbc.gridy++;
    gbc.weighty = 0.333;
    
    add(buildRuntimeConstraintPanel(), gbc);
    
    gbc.gridy++;
    
  }

  public String getWizardStepTitle() {
    return "Specify Distribution Set Filter(s)";
  }
  
  private JPanel buildFilterPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(
        new TitledBorder("Filters")
    );
    
    panel.add(new JLabel("Filters go here."));
    
    return panel;
  }
  
  private JPanel buildRuntimeConstraintPanel() {
    runtimeConstraintsPanel = new RuntimeConstraintsPanel(this);
    return runtimeConstraintsPanel;
  }
  
  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  public void doBack() {}

  public void doNext() {}     

  void refresh() {
    runtimeConstraintsPanel.setTask(
        (YAWLAtomicTask) getTask()
    );
  }
    
  public boolean shouldDoThisStep() {
    return getResourceMapping().getOfferInteractionPoint() == 
      ResourceMapping.SYSTEM_INTERACTION_POINT &&
      getResourceMapping().getRetainFamiliarTask() == null;
  }
}

class RuntimeConstraintsPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private FamiliarTaskComboBox familiarTaskBox; 
  
  private SpecifyDistributionSetFiltersPanel  filterPanel;
  private JCheckBox separationOfDutiesCheckBox;
  private JCheckBox piledExecutionCheckBox;
  
  public RuntimeConstraintsPanel(SpecifyDistributionSetFiltersPanel filterPanel) {
    super();
    this.filterPanel = filterPanel;
    buildContent();
  }
  
  protected ResourceMapping getResourceMapping() {
    return filterPanel.getResourceMapping();
  }
  
  private void buildContent() {
    setBorder(
        new TitledBorder("Runtime Constraints")
    );

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(10,5,10,5);
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    add(buildPiledExecutionCheckBox(), gbc);
    
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.insets = new Insets(10,5,10,5);
    
    add(buildSeparationOfDutiesCheckBox(), gbc);
    
    gbc.gridx++;
    add(buildSeparationOfDutiesFamilarTaskBox(), gbc);
  }
  
  private JCheckBox buildPiledExecutionCheckBox() {
    piledExecutionCheckBox = new JCheckBox("Allow workitems of this task to be piled across cases to a single user.");
    piledExecutionCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            filterPanel.getResourceMapping().enablePrivilege(
                ResourceMapping.CAN_PILE_PRIVILEGE, 
                piledExecutionCheckBox.isSelected()
            );
          }
        }
    );

    return piledExecutionCheckBox;
  }
  
  private JCheckBox buildSeparationOfDutiesCheckBox() {
    separationOfDutiesCheckBox = new JCheckBox("Do not allow users who've done workitems of the following previous task to also do this task: ");
    separationOfDutiesCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            familiarTaskBox.setEnabled(
                separationOfDutiesCheckBox.isSelected()    
            );
          }
        }
    );

    return separationOfDutiesCheckBox;
  }
  
  private JComboBox buildSeparationOfDutiesFamilarTaskBox() {
    familiarTaskBox = new FamiliarTaskComboBox();
    
    return familiarTaskBox;
  }
  
  public void setTask(YAWLAtomicTask task) {
    familiarTaskBox.setTask(task);

    if (familiarTaskBox.getFamiliarTaskNumber() == 0) {
      separationOfDutiesCheckBox.setEnabled(false);
      familiarTaskBox.setEnabled(false);
    } else {
      if (getResourceMapping().getSeparationOfDutiesTask()!= null) {
        separationOfDutiesCheckBox.setEnabled(true);
        separationOfDutiesCheckBox.setSelected(true);
        familiarTaskBox.setEnabled(true);
        familiarTaskBox.setSelectedFamiliarTask(
            getResourceMapping().getSeparationOfDutiesTask()    
        );
      } else {
        separationOfDutiesCheckBox.setEnabled(true);
        separationOfDutiesCheckBox.setSelected(false);
        familiarTaskBox.setEnabled(false);
      }
    }
    
    piledExecutionCheckBox.setSelected(
      this.getResourceMapping().isPrivilegeEnabled(
          ResourceMapping.CAN_PILE_PRIVILEGE    
      )    
    );
  }
}