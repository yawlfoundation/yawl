/*
 * Created on 05/12/2003
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

package org.yawlfoundation.yawl.editor.ui.net;

import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.swing.menu.FlowPopupMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.PalettePopupMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.VertexPopupMenu;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NetPopupListener extends MouseAdapter {

    private static final PalettePopupMenu palettePopup = new PalettePopupMenu();
    private NetGraph graph;

    public NetPopupListener(NetGraph graph) {
        this.graph = graph;
    }

    public void mousePressed(MouseEvent event) {
        if (! SwingUtilities.isRightMouseButton(event)) {
            return;
        }

        YAWLVertex vertex = NetCellUtilities.getVertexFromCell(
                graph.getFirstCellForLocation(event.getX(), event.getY()));

        if (vertex instanceof YAWLTask || vertex instanceof Condition) {
            new VertexPopupMenu(vertex, graph).show(graph, event.getX(), event.getY());
            return;
        }

        YAWLFlowRelation flow = NetCellUtilities.getFlowRelationFromCell(
                graph.getFirstCellForLocation(event.getX(), event.getY()));

        if (flow != null) {
            FlowPopupMenu flowPopup = new FlowPopupMenu(graph, flow, event.getPoint());
            flowPopup.show(graph, event.getX(), event.getY());
            return;
        }

        palettePopup.show(graph, event.getX(), event.getY());
    }

}
