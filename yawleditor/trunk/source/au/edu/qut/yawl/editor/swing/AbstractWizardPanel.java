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
  
  public String getWizardTitle() {
    return "<no title given>";
  }
  
  protected abstract void buildInterface();
  protected abstract void initialise();
  
  public abstract void doBack();
  public abstract void doNext();
}
