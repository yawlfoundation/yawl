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

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class SetSelectedElementsFillColourAction extends YAWLSelectedNetAction
                                implements TooltipTogglingWidget, GraphStateListener {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  {
    putValue(Action.SHORT_DESCRIPTION, " Set the selected elements background colour. ");
    putValue(Action.NAME, "Set Selected Fill Colour...");
    putValue(Action.LONG_DESCRIPTION, "Set the fill colour for the selected elements.");
    putValue(Action.SMALL_ICON, getPNGIcon("color_swatch"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_F));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("F"));
  }

  public SetSelectedElementsFillColourAction() {
    super();
      Publisher.getInstance().subscribe(this,
              Arrays.asList(GraphState.NoElementSelected,
                      GraphState.MultipleVerticesSelected));
  }

    public void actionPerformed(ActionEvent event) {
        Color newColor = JColorChooser.showDialog(
                YAWLEditor.getInstance(),
                "Set Selected Element Background Color",
                UserSettings.getVertexBackgroundColour()
        );
        if (newColor != null) {
            final NetGraph graph = getGraph();
            if (graph != null) {
                Object[] selected = graph.getSelectionCells();
                for (Object o : selected) {
                    YAWLVertex vertex = getSelectedVertexIfAppropriate(o) ;
                    if (vertex != null) {
                        vertex.setBackgroundColor(newColor);
                        graph.changeVertexBackground(vertex, newColor);
                    }
                }
                graph.resetCancellationSet();
            }
        }
    }

    private YAWLVertex getSelectedVertexIfAppropriate(Object o) {
        YAWLVertex result = null;
        if (o instanceof VertexContainer) {
            result = ((VertexContainer) o).getVertex();
        }
        else if (o instanceof YAWLVertex) {
            result = (YAWLVertex) o;
        }
        if ((result != null) &&
            ((result instanceof InputCondition) || (result instanceof OutputCondition))) {
            result = null;
        }
        return result ;
    }

    public String getEnabledTooltipText() {
      return " Change the background colour of the selected element(s) ";
    }

    public String getDisabledTooltipText() {
      return " You must have a one or more net elements selected ";
    }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        setEnabled(state == GraphState.MultipleVerticesSelected);
    }

}