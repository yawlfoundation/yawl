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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.actions.CopyAction;
import org.yawlfoundation.yawl.editor.ui.actions.CutAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.DeleteAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.GotoSubNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCompositeTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;

import javax.swing.*;

public class VertexPopupMenu extends JPopupMenu {

    private final YAWLCell cell;

    public VertexPopupMenu(YAWLCell cell) {
        super();
        this.cell = cell;
        addMenuItems();
    }

    private void addMenuItems() {
        YAWLVertex vertex = (YAWLVertex) cell;
        addGraphSpecificMenuItems(vertex);
    }

    private void addGraphSpecificMenuItems(YAWLVertex vertex) {
        addCopyableMenuItems(vertex);
        addRemoveableMenuItems(vertex);
        addSubNetMenuItem(vertex);
    }


    private void addCopyableMenuItems(YAWLVertex vertex) {
        if (!vertex.isCopyable()) {
            return;
        }
        add(new YAWLPopupMenuItem(CutAction.getInstance()));
        add(new YAWLPopupMenuItem(CopyAction.getInstance()));
    }

    private void addRemoveableMenuItems(YAWLVertex vertex) {
        if (!vertex.isRemovable()) {
            return;
        }
        add(new YAWLPopupMenuItem(DeleteAction.getInstance()));
    }


    private void addSubNetMenuItem(YAWLVertex vertex) {
        if (vertex instanceof YAWLCompositeTask) {
            YAWLPopupMenuItem item = new YAWLPopupMenuItem(
                    new GotoSubNetAction((YAWLCompositeTask) vertex));
            item.setEnabled(((YAWLCompositeTask) vertex).getDecomposition() != null);
            add(item);
        }
    }


    public YAWLCell getCell() {
        return cell;
    }

}
