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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

public abstract class DesignInternalFrame
    extends JInternalFrame implements ActionListener, MouseListener {
  static final int xOffset = 30, yOffset = 30;

  protected DesignInternalFrame(String title) {
    super(title,
          true, //resizable
          true, //closable
          true, //maximizable
          true); //iconifiable
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    //...Create the GUI and put it in the window...

    //...Then set the window size or call pack...
    setSize(300, 300);
  }

  public abstract void actionPerformed(ActionEvent e);

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a
   * component.
   *
   * @param e MouseEvent
   * @todo Implement this java.awt.event.MouseListener method
   */
  public void mouseClicked(MouseEvent e) {
  }

  /**
   * Invoked when the mouse enters a component.
   *
   * @param e MouseEvent
   * @todo Implement this java.awt.event.MouseListener method
   */
  public void mouseEntered(MouseEvent e) {
  }

  /**
   * Invoked when the mouse exits a component.
   *
   * @param e MouseEvent
   * @todo Implement this java.awt.event.MouseListener method
   */
  public void mouseExited(MouseEvent e) {
  }

  /**
   * Invoked when a mouse button has been pressed on a component.
   *
   * @param e MouseEvent
   * @todo Implement this java.awt.event.MouseListener method
   */
  public void mousePressed(MouseEvent e) {
  }

  /**
   * Invoked when a mouse button has been released on a component.
   *
   * @param e MouseEvent
   * @todo Implement this java.awt.event.MouseListener method
   */
  public void mouseReleased(MouseEvent e) {
  }

  /**
   * fillList
   *
   * @param anList List
   * @param anJList JList
   */
  protected void fillList(Collection anList, JList anJList) {
    FrameUtil.fillList(anList, anJList);
  }

  /**
   * fillList
   *
   * @param anJList JList
   * @return Object
   */
  protected Object getSelecetdList(JList anJList) {
    return FrameUtil.getSelecetdList(anJList);
  }

  /**
   */
  public void maximize() {
    try {
      this.setVisible(true);
      this.setMaximum(true);
      this.setSelected(true);
    }
    catch (java.beans.PropertyVetoException e) {}
  }
}

