package org.yawlfoundation.yawl.procletService.editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MainFrame
    extends JFrame implements ActionListener {

  private final String NAME = "Interaction Definition Editor";

  //JDesktopPane desktop;
  //private JInternalFrame frame = null;

  public MainFrame() {
    try {
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  /**
   * Component initialization.
   *
   * @throws java.lang.Exception
   */
  private void jbInit() throws Exception {
    //Make the big window be indented 50 pixels from each edge
    //of the screen.
    setTitle(NAME);
    int inset = 50;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setBounds(inset, inset,
              screenSize.width - inset * 2,
              screenSize.height - inset * 2);
  }

  //React to menu selections.
  public void actionPerformed(ActionEvent e) {
    /* if ("new".equals(e.getActionCommand())) { //new
         createFrame();
     } else { //quit
         quit();
     }*/
  }

  //Quit the application.
  protected void quit() {
    System.exit(0);
  }

}
