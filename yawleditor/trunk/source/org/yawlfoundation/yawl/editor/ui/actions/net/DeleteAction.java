/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
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
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandler;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class DeleteAction extends YAWLSelectedNetAction implements TooltipTogglingWidget, GraphStateListener {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final DeleteAction INSTANCE = new DeleteAction();
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Delete");
    putValue(Action.LONG_DESCRIPTION, "Deletes currently selected net elements.");
    putValue(Action.SMALL_ICON, getPNGIcon("bin_empty"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_D));
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
  }
  
  private DeleteAction() {
      Publisher.getInstance().subscribe(this,
              Arrays.asList(GraphState.NoElementSelected,
                      GraphState.ElementsSelected,
                      GraphState.DeletableElementSelected));
  }
  
  public static DeleteAction getInstance() {
    return INSTANCE; 
  }

    public void actionPerformed(ActionEvent event) {
        final NetGraph graph = getGraph();
        if (graph != null) {
            YControlFlowHandler handler = SpecificationModel.getHandler().getControlFlowHandler();
            for (Object o : graph.getNetModel().getConnectingFlows(graph.getSelectionCells())) {
                YAWLFlowRelation flow = (YAWLFlowRelation) o;
                handler.removeFlow(graph.getName(), flow.getSourceID(), flow.getTargetID());
            }

            for (Object o : graph.removeSelectedCellsAndTheirEdges()) {
                if (o instanceof Condition) {
                    handler.removeCondition(graph.getName(), ((Condition) o).getID());
                }
                else if (o instanceof AtomicTask)  {
                    handler.removeAtomicTask(graph.getName(), ((AtomicTask) o).getID());
                }
                else if (o instanceof MultipleAtomicTask) {
                    handler.removeAtomicTask(graph.getName(), ((MultipleAtomicTask) o).getID());
                }
                else if (o instanceof CompositeTask) {
                    handler.removeCompositeTask(graph.getName(), ((CompositeTask) o).getID());
                }
                else if (o instanceof MultipleCompositeTask) {
                    handler.removeCompositeTask(graph.getName(), ((MultipleCompositeTask) o).getID());
                }
            }
        }
    }
  
  public String getEnabledTooltipText() {
    return " Delete currently selected net elements ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a number of net elements selected" + 
           " to delete them ";
  }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        setEnabled(state == GraphState.DeletableElementSelected);
    }
}
