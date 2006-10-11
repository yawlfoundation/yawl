package au.edu.qut.yawl.editor.swing.resourcing;

import javax.swing.JComboBox;

public class ResourceAllocationComboBox extends JComboBox {
  public static final int SYSTEM_ALLOCATION = 0;
  public static final int USER_ALLOCATION   = 1;
  
  private static final String[] ALLOCATION_LABELS = {
    "System allocates work-item to users",
    "User self-allocates from offered work-items"
  };
  
  public ResourceAllocationComboBox() {
    super(ALLOCATION_LABELS);
  }
  
  

}
