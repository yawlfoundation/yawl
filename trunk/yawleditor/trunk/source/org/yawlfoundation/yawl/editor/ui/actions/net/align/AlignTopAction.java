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

package org.yawlfoundation.yawl.editor.ui.actions.net.align;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class AlignTopAction extends YAWLSelectedNetAction implements TooltipTogglingWidget, GraphStateListener {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final AlignTopAction INSTANCE = new AlignTopAction();
    
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Align along Top Edges");
    putValue(Action.LONG_DESCRIPTION, "Align the selected elements their top edges.");
    putValue(Action.SMALL_ICON, getPNGIcon("shape_align_top"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_T));
  }
  
  private AlignTopAction() {
      Publisher.getInstance().subscribe(this,
              Arrays.asList(GraphState.NoElementSelected,
                      GraphState.ElementsSelected,
                      GraphState.MultipleVerticesSelected));
  }
  
  public static AlignTopAction getInstance() {
    return INSTANCE; 
  }

  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
      NetCellUtilities.alignCellsAlongTop(graph, graph.getSelectionCells());
    }
  }
  
  public String getEnabledTooltipText() {
    return " Align the selected elements along their top edges ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a number of net elements selected" + 
           " to align them ";
  }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        setEnabled(state == GraphState.MultipleVerticesSelected);
    }

}