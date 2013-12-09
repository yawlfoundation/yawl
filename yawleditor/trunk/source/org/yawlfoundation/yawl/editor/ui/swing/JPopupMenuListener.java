/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.swing;

import javax.swing.JPopupMenu;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JPopupMenuListener extends MouseAdapter {
  private final JPopupMenu menu;

  public JPopupMenuListener(JPopupMenu menu) {
    this.menu= menu;    
  }

  public void mouseReleased(MouseEvent e) {
    processMouseEvent(e);
  }

  public void mousePressed(MouseEvent e) {
    processMouseEvent(e);
  }

  private void processMouseEvent(MouseEvent e) {
    if (e.isPopupTrigger()) {
      menu.show(e.getComponent(), e.getX(), e.getY());
    }
  }
}
