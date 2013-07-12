package org.yawlfoundation.yawl.procletService.editor;

import javax.swing.*;

public class BlankButtonGroup
    extends ButtonGroup {
  AbstractButton blank;

  public BlankButtonGroup() {
    super();
    blank = new Button();
    blank.setVisible(false);
    blank.setEnabled(false);
    add(blank);
    setSelected(blank.getModel(), true);
  }

  /**
   * Sets the selected value for the <code>ButtonModel</code>.
   * Only one button in the group may be selected at a time.
   * If a button is unselected, then the hidden button 'blank' becomes selected.
   * @param m the <code>ButtonModel</code>
   * @param b <code>true</code> if this button is to be
   *   selected, otherwise <code>false</code>
   */
  public void setSelected(ButtonModel m, boolean b) {
    if (b && m != blank.getModel()) {
      super.setSelected(m, b);
    }
    else {
      super.setSelected(blank.getModel(), true);
    }
  }
}

