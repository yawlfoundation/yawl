/*
 * Created on 26/01/2004, 18:10:04
 * YAWLEditor v1.0 
 * 
 * Copyright (C) 2004 Lindsay Bradford
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.swing;

import javax.swing.JPopupMenu;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JPopupMenuListener extends MouseAdapter {
  private JPopupMenu menu;

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
