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

package org.yawlfoundation.yawl.editor.ui.swing.undo;

import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YNet;

import javax.swing.undo.AbstractUndoableEdit;

public class UndoableNetAddition extends AbstractUndoableEdit {

    private final NetGraphModel addedNet;

    public UndoableNetAddition(NetGraphModel addedNet) {
        this.addedNet = addedNet;
    }

    public void redo() {
        SpecificationModel.getNets().addNoUndo(addedNet);
        addedNet.getGraph().getFrame().setVisible(true);
        YAWLEditor.getNetsPane().add(addedNet.getName(), addedNet.getGraph().getFrame());
        try {
            SpecificationModel.getHandler().getControlFlowHandler().addNet(
                    (YNet) addedNet.getDecomposition());
        }
        catch (YControlFlowHandlerException ycfhe) {
            // do nothing, will only happen if no spec loaded
        }
    }

    public void undo() {
        YAWLEditor.getNetsPane().remove(addedNet.getGraph().getFrame());
        SpecificationModel.getNets().removeNoUndo(addedNet);
        try {
            SpecificationModel.getHandler().getControlFlowHandler().removeNet(
                    addedNet.getDecomposition().getID());
        }
        catch (YControlFlowHandlerException ycfhe) {
            // tried to remove the root net
        }
    }
}
