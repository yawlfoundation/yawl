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

package org.yawlfoundation.yawl.editor.actions.net;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionListener;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionSubscriber;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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
  }

  public SetSelectedElementsFillColourAction() {
    super();
    SpecificationSelectionListener.getInstance().subscribe(
        this,
        new int[] { SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED,
                    SpecificationSelectionListener.ONE_OR_MORE_VERTEX_SELECTED }
    );
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
                    if ((o instanceof YAWLVertex) &&
                            ! ((o instanceof InputCondition) || (o instanceof OutputCondition))) {
                        ((YAWLVertex) o).setBackgroundColor(newColor);
                        graph.changeVertexBackground((YAWLVertex) o, newColor);
                    }
                }
                graph.resetCancellationSet();
            }
        }
    }

    public String getEnabledTooltipText() {
      return " Change the background colour of the selected element(s) ";
    }

    public String getDisabledTooltipText() {
      return " You must have a one or more net elements selected ";
    }

    public void receiveGraphSelectionNotification(int state, GraphSelectionEvent event) {
        switch(state) {
          case SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED: {
            setEnabled(false);
            break;
          }
          case SpecificationSelectionListener.ONE_OR_MORE_VERTEX_SELECTED: {
            setEnabled(true);
            break;
          }
       }
    }

}