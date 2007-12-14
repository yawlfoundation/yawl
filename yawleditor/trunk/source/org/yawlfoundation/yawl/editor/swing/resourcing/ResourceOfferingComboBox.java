package au.edu.qut.yawl.editor.swing.resourcing;

import javax.swing.JComboBox;

public class ResourceOfferingComboBox extends JComboBox {

  private static final long serialVersionUID = 1L;

  public static final int SYSTEM_OFFERS = 0;
  public static final int ADMINISTRATOR_OFFERS = 1;
  
  private static final String[] OFFER_TYPE_LABELS = new String[] {
    "System offers work-item",
    "Administrator offers work-item"
  };

  public ResourceOfferingComboBox() {
    super(OFFER_TYPE_LABELS);
  }
  
  public int getOfferType() {
    return getSelectedIndex();
  }
}
