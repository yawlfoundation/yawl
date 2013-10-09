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

package org.yawlfoundation.yawl.editor.ui.swing.element;

import javax.swing.JPanel;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

public abstract class AbstractTaskDoneDialog extends AbstractVertexDoneDialog {
  
  public AbstractTaskDoneDialog(String title, boolean modality, boolean showCancelButton) {
    super(title,modality, showCancelButton);
  }

  public AbstractTaskDoneDialog(String title, 
                                boolean modality,
                                JPanel contentPanel, 
                                boolean showCancelButton) {
    super(title, modality, contentPanel, showCancelButton);
  }  
  
  
  public void setTask(YAWLTask task, NetGraph graph) {
    super.setVertex(task, graph);
  }
  
  public YAWLTask getTask() {
    return (YAWLTask) getVertex();
  }
}
