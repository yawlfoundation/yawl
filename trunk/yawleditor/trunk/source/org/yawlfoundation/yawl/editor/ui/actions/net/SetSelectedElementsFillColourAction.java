/*
 * Created on 9/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.ui.actions.net;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationSelectionSubscriber;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class SetSelectedElementsFillColourAction extends YAWLSelectedNetAction
                                implements TooltipTogglingWidget, SpecificationSelectionSubscriber {

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
                SpecificationModel.getInstance().getDefaultVertexBackgroundColor()
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