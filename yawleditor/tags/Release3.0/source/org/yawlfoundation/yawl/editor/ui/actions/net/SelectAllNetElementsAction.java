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

import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SelectAllNetElementsAction extends YAWLSelectedNetAction {

  private static final SelectAllNetElementsAction INSTANCE 
    = new SelectAllNetElementsAction();
  {
    putValue(Action.SHORT_DESCRIPTION, " Select all net elements ");
    putValue(Action.NAME, "Select all");
    putValue(Action.LONG_DESCRIPTION, "Select all net elements.");
  }
  
  private SelectAllNetElementsAction() {};  
  
  public static SelectAllNetElementsAction getInstance() {
    return INSTANCE; 
  }

  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
      graph.setSelectionCells(graph.getRoots());
    }
  }
}
