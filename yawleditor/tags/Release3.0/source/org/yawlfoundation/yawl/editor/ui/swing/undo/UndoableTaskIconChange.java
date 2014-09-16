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

package org.yawlfoundation.yawl.editor.ui.swing.undo;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

import javax.swing.undo.AbstractUndoableEdit;

public class UndoableTaskIconChange extends AbstractUndoableEdit {

  private final NetGraph net;
  private final YAWLTask task;
  
  private final String oldIconPath;
  private final String newIconPath;


  public UndoableTaskIconChange(NetGraph net, YAWLVertex vertex,
                                String oldIconPath, String newIconPath) {
    this.net = net;
    this.task = (YAWLTask) vertex;
    this.oldIconPath = oldIconPath;
    this.newIconPath = newIconPath;
  }
  
  public void redo() {
    task.setIconPath(newIconPath);
    net.repaint();
  }
  
  public void undo() {
    task.setIconPath(oldIconPath);
    net.repaint();
  }
}
