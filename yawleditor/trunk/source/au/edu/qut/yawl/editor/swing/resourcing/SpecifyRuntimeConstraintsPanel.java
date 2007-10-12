package au.edu.qut.yawl.editor.swing.resourcing;

import java.awt.BorderLayout;

import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.resourcing.NewYawlResourceMapping;

public class SpecifyRuntimeConstraintsPanel extends ResourcingWizardPanel {

  private static final long serialVersionUID = 1L;

  public SpecifyRuntimeConstraintsPanel(ManageNewYAWLResourcingDialog dialog) {
    super(dialog);
  }
  
  protected void buildInterface() {

    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(12,12,0,11));

  }

  public String getWizardStepTitle() {
    return "Specify Runtime Constraints";
  }
  
  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  public void doBack() {}

  public void doNext() {}     

  public void refresh() {
    // TODO: fill in with resourcing changes to UI.
  }

  public boolean shouldDoThisStep() {
    return getResourceMapping().getOfferInteractionPoint() == 
      NewYawlResourceMapping.InteractionPointSetting.SYSTEM &&
      getResourceMapping().getRetainFamiliarTask() == null;
   }
}