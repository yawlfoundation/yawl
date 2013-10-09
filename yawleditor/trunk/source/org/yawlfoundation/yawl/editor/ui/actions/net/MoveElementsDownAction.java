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

package org.yawlfoundation.yawl.editor.ui.actions.net;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

public class MoveElementsDownAction extends YAWLSelectedNetAction {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final MoveElementsDownAction INSTANCE 
    = new MoveElementsDownAction();
  {
    putValue(Action.SHORT_DESCRIPTION, " Move selected items down");
    putValue(Action.NAME, "Move Items Down");
    putValue(Action.LONG_DESCRIPTION, "Move currently selected thigies down.");
  }
  
  private MoveElementsDownAction() {};  
  
  public static MoveElementsDownAction getInstance() {
    return INSTANCE; 
  }

  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
      graph.moveSelectedElementsDown();
    }
  }
}
