package au.edu.qut.yawl.editor.swing.resourcing;

import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

public class AllocationStrategyComboBox extends JComboBox {
  
  private JLabel label;

  public static final int STRATEGY_RANDOM_SELECTION = 0;
  public static final int STRATEGY_ROUND_ROBIN = 1;
  public static final int STRATEGY_SHORTEST_QUEUE = 2;
  
  private static final String[] ALLOCATION_STRATEGY_LABELS = new String[] {
    "Random selection",
    "Round-robin",
    "Shortest queue"
  };
  
  private boolean singleAllocationRequired = false;
  private boolean systemAllocatesWorkitem  = false;
  
  public AllocationStrategyComboBox() {
    super(ALLOCATION_STRATEGY_LABELS);
    updateEnablementAsAppropriate();
  }
  
  public void setSystemAllocationRequired(boolean systemAllocatesWorkitem) {
    this.systemAllocatesWorkitem = systemAllocatesWorkitem;
    updateEnablementAsAppropriate();
  }
  
  public void setLabel(JLabel label) {
    this.label = label;
  }
  
  public boolean systemAllocationRequired() {
    return this.systemAllocatesWorkitem;
  }
  
  public void setSingleAllocationRequired(boolean singleAllocationRequired) {
    this.singleAllocationRequired = singleAllocationRequired;
    updateEnablementAsAppropriate();
  }
  
  public boolean singleAllocationRequired() {
    return this.singleAllocationRequired;
  }
  
  public void updateEnablementAsAppropriate() {
    if (systemAllocationRequired() && singleAllocationRequired()) {
      setEnabled(true);
      if (label != null) {
        label.setEnabled(true);
      }
    } else {
      setEnabled(false);
      if (label != null) {
        label.setEnabled(false);
      }
    }
  }
}
