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

import org.yawlfoundation.yawl.editor.ui.swing.net.YAWLEditorNetPanel;

import javax.swing.undo.AbstractUndoableEdit;

public class UndoableNetFrameTitleChange extends AbstractUndoableEdit {

  private final YAWLEditorNetPanel frame;
    
  private final String oldName;
  private final String newName;
    
  public UndoableNetFrameTitleChange(YAWLEditorNetPanel frame, String oldName, String newName) {
    this.frame = frame;
    this.oldName = oldName;
    this.newName = newName;
  }
  
  public void redo() {
    frame.setTitle(newName);
  }
  
  public void undo() {
    frame.setTitle(oldName);
  }
}
