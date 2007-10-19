package au.edu.qut.yawl.editor.swing.resourcing;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.resourcing.NewYawlResourceMapping;

public class SpecifyDistributionSetFiltersPanel extends ResourcingWizardPanel {

  private static final long serialVersionUID = 1L;

  public SpecifyDistributionSetFiltersPanel(ManageNewYAWLResourcingDialog dialog) {
    super(dialog);
  }
  
  protected void buildInterface() {
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(12,12,0,11));
    
    add(new JLabel("Both filters and runtime constraints to go here. Constraints first, they'll be easier."));
  }

  public String getWizardStepTitle() {
    return "Specify Distribution Set Filter(s)";
  }
  
  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  public void doBack() {}

  public void doNext() {}     

  void refresh() {
    // TODO Auto-generated method stub
  }
  
  public boolean shouldDoThisStep() {
    return getResourceMapping().getOfferInteractionPoint() == 
      NewYawlResourceMapping.InteractionPointSetting.SYSTEM &&
      getResourceMapping().getRetainFamiliarTask() == null;
  }
}