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

import org.yawlfoundation.yawl.elements.YDecomposition;

import javax.swing.undo.AbstractUndoableEdit;

public class UndoableDecompositionLabelChange extends AbstractUndoableEdit {

    private YDecomposition decomposition;
    private String oldLabel;
    private String newLabel;


    public UndoableDecompositionLabelChange(YDecomposition decomposition,
                                            String oldName, String newName) {
        this.decomposition = decomposition;
        this.oldLabel = oldName;
        this.newLabel = newName;
    }

    public void redo() {
        decomposition.setName(newLabel);
    }

    public void undo() {
        decomposition.setName(oldLabel);
    }
}
