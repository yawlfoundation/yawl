/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

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
