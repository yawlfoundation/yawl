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

package org.yawlfoundation.yawl.editor.ui.net;

import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.swing.element.LabelElementDialog;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ElementDoubleClickListener extends MouseAdapter {

    private final NetGraph net;

    public ElementDoubleClickListener(NetGraph net) {
        this.net = net;
    }

    public void mouseClicked(MouseEvent e) {
        if (! (e.getClickCount() == 2 && net.getSelectionCount() == 1)) {
            return;
        }
        Object selectedCell = net.getSelectionCell();

        if (selectedCell instanceof VertexContainer) {
            selectedCell = ((VertexContainer) selectedCell).getVertex();
        }
        if (selectedCell instanceof YAWLVertex) {
            showLabelElementDialog((YAWLVertex) selectedCell);
        }
    }

    private void showLabelElementDialog(YAWLVertex vertex) {
        LabelElementDialog labelConditionDialog = new LabelElementDialog();
        labelConditionDialog.setVertex(vertex, net);
        labelConditionDialog.setVisible(true);
    }
}
