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

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCompositeTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.NetModelSet;

import javax.swing.undo.AbstractUndoableEdit;
import java.util.Set;

public class UndoableNetDeletion extends AbstractUndoableEdit {

    private NetGraphModel deletedNet;
    private boolean wasRootNet;
    private NetGraphModel newRootNet;
    private Set<YAWLCompositeTask> changedTasks;
    private NetModelSet nets;

    public UndoableNetDeletion(NetModelSet nets, NetGraphModel deletedNet,
                               NetGraphModel newRootNet,
                               Set<YAWLCompositeTask> changedTasks) {
        this.nets = nets;
        this.deletedNet = deletedNet;
        this.wasRootNet = deletedNet.isRootNet();
        this.newRootNet = newRootNet;
        this.changedTasks = changedTasks;
   }

    public void redo() {
        deletedNet.getGraph().getFrame().setVisible(false);
        nets.removeNoUndo(deletedNet);
        if (newRootNet != null) {
            nets.setRootNet(newRootNet);
        }
        nets.resetUnfoldingCompositeTasks(deletedNet);
    }

    public void undo() {
        nets.addNoUndo(deletedNet);
        if (wasRootNet) {
            nets.setRootNet(deletedNet);
        }
        for (YAWLCompositeTask changedTask : changedTasks) {
            YAWLTask task = (YAWLTask) changedTask;
            task.setDecomposition(deletedNet.getDecomposition());
        }
        deletedNet.getGraph().getFrame().setVisible(true);
    }
}
