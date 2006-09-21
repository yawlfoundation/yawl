package au.edu.qut.yawl.editor.swing;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public abstract class AbstractWizardPanel extends JPanel {
  
  private AbstractWizardDialog dialog;
  
  public AbstractWizardPanel(AbstractWizardDialog dialog) {
    super();
    this.dialog = dialog;
    this.setBorder(new EmptyBorder(10,10,10,10));

    buildInterface();
    initialise();
  }
  
  public String getWizardStepTitle() {
    return "<no title given>";
  }
  
  public AbstractWizardDialog getDialog() {
    return this.dialog;
  }
  
  protected abstract void buildInterface();
  protected abstract void initialise();
  
  public abstract void doBack();
  public abstract void doNext();
}
