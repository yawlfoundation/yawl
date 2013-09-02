/*
 * Created on 27/04/2004
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

package org.yawlfoundation.yawl.editor.ui.swing.undo;

import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YNet;

import javax.swing.undo.AbstractUndoableEdit;

public class UndoableNetAddition extends AbstractUndoableEdit {

    private NetGraphModel addedNet;

    public UndoableNetAddition(NetGraphModel addedNet) {
        this.addedNet = addedNet;
    }

    public void redo() {
        SpecificationModel.getInstance().getNets().addNoUndo(addedNet);
        addedNet.getGraph().getFrame().setVisible(true);
        YAWLEditor.getNetsPane().add(addedNet.getName(), addedNet.getGraph().getFrame());
        SpecificationModel.getHandler().getControlFlowHandler().addNet(
                (YNet) addedNet.getDecomposition());
    }

    public void undo() {
        YAWLEditor.getNetsPane().remove(addedNet.getGraph().getFrame());
        SpecificationModel.getInstance().getNets().removeNoUndo(addedNet);
        try {
            SpecificationModel.getHandler().getControlFlowHandler().removeNet(
                    addedNet.getDecomposition().getID());
        }
        catch (YControlFlowHandlerException ycfhe) {
            // tried to remove the root net
        }
    }
}
