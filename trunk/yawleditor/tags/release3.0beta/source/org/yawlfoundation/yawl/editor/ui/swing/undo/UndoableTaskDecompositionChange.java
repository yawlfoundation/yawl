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
import org.yawlfoundation.yawl.elements.YDecomposition;

import javax.swing.undo.AbstractUndoableEdit;

public class UndoableTaskDecompositionChange extends AbstractUndoableEdit {

  private final YDecomposition oldDecomposition;
  private final YDecomposition newDecomposition;
  private final YAWLTask task;
    
  public UndoableTaskDecompositionChange(YAWLTask      task,
                                         YDecomposition oldDecomposition,
                                         YDecomposition newDecomposition) {
    this.task = task;
    
    this.oldDecomposition = oldDecomposition;
    this.newDecomposition = newDecomposition;
  }
  
  public void redo() {
    task.setDecomposition(newDecomposition);
  }
  
  public void undo() {
    task.setDecomposition(oldDecomposition);
  }
}
