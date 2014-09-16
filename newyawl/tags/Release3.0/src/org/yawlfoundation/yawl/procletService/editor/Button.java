package org.yawlfoundation.yawl.procletService.editor;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * <p>Title: TeamWorkFlow</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: TU/e</p>
 *
 * @author Maja Pesic
 * @version 1.0
 */
public class Button
    extends JButton {
  public Button() {
    super();
  }

  public Button(Icon icon) {
    super(icon);
  }

  public Button(String text) {
    super(text);
  }

  public Button(Action a) {
    super(a);
  }

  public Button(String text, Icon icon) {
    super(text, icon);
  }
}
