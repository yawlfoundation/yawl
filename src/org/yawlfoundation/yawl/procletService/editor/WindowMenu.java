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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

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
class WindowMenu extends JMenu {

  private Collection<WindowMenuItem> frames;
  ButtonGroup group;
  WindowMenuListener listener = null;

  /**
   * Frames
   */
  WindowMenu() {
    super("Window");
    this.frames = new HashSet<WindowMenuItem>();
    this.group = new BlankButtonGroup();
    this.setVisibility();
  }

  /**
   *
   */
  private void setVisibility() {
    this.setVisible(frames.size() > 0);
  }

  /**
   *
   * @param active boolean
   * @param frame JInternalFrame
   */
  void add(boolean active, JInternalFrame frame) {
    WindowMenuItem item = new WindowMenuItem(frame);
    frames.add(item);
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof WindowMenuItem) {
          changed( (WindowMenuItem) e.getSource());
        }
      }
    });
    this.add(item);
    this.group.add(item);
    this.activate(active, frame);
    this.setVisibility();
  }

  /**
   *
   * @param active boolean
   * @param frame JInternalFrame
   */
  void activate(boolean active, JInternalFrame frame) {
    WindowMenuItem item = this.get(frame);
    if (item != null) {
      group.setSelected(item.getModel(), active);
    }
  }

  /**
   *
   * @param frame JInternalFrame
   */
  void remove(JInternalFrame frame) {
    WindowMenuItem item = this.get(frame);
    if (item != null) {
      frames.remove(item);
      this.remove(item);
      this.group.remove(item);
      this.setVisibility();
    }
  }

  /**
   *
   * @param frame JInternalFrame
   * @return FrameMenuItem
   */
  WindowMenuItem get(JInternalFrame frame) {
    WindowMenuItem item = null;
    boolean found = false;
    Iterator<WindowMenuItem> iterator = frames.iterator();
    while (iterator.hasNext() && !found) {
      item = iterator.next();
      found = item.frame(frame);
    }
    return found ? item : null;
  }

  /**
   *
   * @param item ItemEvent
   */
  private void changed(WindowMenuItem item) {
    if (item != null) {
      if (!item.isSelected()) {
        item.setSelected(true);
      }
      else {
        if (listener != null) {
          listener.itemSelected(item.getFrame(), true);
        }
      }
    }
  }

  public void frameChanged(JInternalFrame frame) {
    WindowMenuItem item = this.get(frame);
    if (item != null) {
      item.frameChanged();
    }
  }

  public void addListener(WindowMenuListener l) {
    this.listener = l;
  }
}

class WindowMenuItem
    extends JCheckBoxMenuItem {
  private JInternalFrame frame;
  /**
   * JFrameManuItem
   *
   * @param frame JInternalFrame
   */
  public WindowMenuItem(JInternalFrame frame) {
    super();
    this.frame = frame;
    this.frameChanged();
  }

  boolean frame(JInternalFrame frame) {
    return this.frame == frame;
  }

  void activate(boolean active) {
    int style = active ? Font.BOLD : Font.PLAIN;
    Font old = this.getFont();
    Font font = new Font(old.getFontName(), style, old.getSize());
    this.setFont(font);

  }

  void frameChanged() {
    this.setText(frame.getTitle());
  }

  JInternalFrame getFrame() {
    return this.frame;
  }
}

interface WindowMenuListener {
  void itemSelected(JInternalFrame frame, boolean active);
}

