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
